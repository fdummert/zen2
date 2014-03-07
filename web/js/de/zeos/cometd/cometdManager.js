define(["dojo/_base/unload", "dojox/cometd", "dojo/_base/lang", "dojox/cometd/ack", "dojo/domReady!"], function(unloader, cometDInstance, lang) {
    var CometDManager = null;
    CometDManager = function(credentials, communicationListener) {
        if ((this instanceof CometDManager) === false)
            throw "CometDManager must be instantiated: new CometDManager()";
        
        var cometD = cometDInstance;
        var status = CometDManager.Status.DISCONNECTED;
        
        var clientId = null;
        var appProperties = null;
        var application = null;
        var topicSubscriptions = {};
        var connectionListeners = [];
        var msgId = 1;
        var queue = [];
        var connected = false;
        var manualReconnect = false;
        var manualDisconnect = false;
        var cm = this;
        
        cometD.onListenerException = function(ex) {
            console.log("uncaught exception:", ex);
        };

        unloader.addOnUnload(function() {
            cometD.disconnect(true);
        });
        
        cometD.addListener("/meta/handshake", initListener);
        cometD.addListener("/meta/subscribe", subscriptionListener);
        cometD.addListener("/meta/connect", connectListener);
        cometD.addListener("/meta/disconnect", disconnectListener);
        
        function isFunction(f) {
            return Object.prototype.toString.call(f) === '[object Function]';
        }
        
        function ScopedTopic(topic, scope) {
            var stringRep = topic;
            if (scope != null)
                stringRep += "/" + scope;
            this.topic = topic;
            this.scope = scope;
            this.stringRep = stringRep;
        }
        ScopedTopic.prototype.toString = function() {
            return this.stringRep;
        };
        
        function resolveError(msg) {
            var failure = msg.failure;
            if (failure != null) {
                var args = "";
                var reason = null;
                if (failure.connectionType) {
                    args += failure.connectionType;
                }
                var websocketCode = failure.webSocketCode;
                var httpCode = failure.httpCode;
                if (websocketCode != null || httpCode != null) {
                    if (args.length > 0)
                        args += ",";
                    args += websocketCode != null ? websocketCode : httpCode;
                }
                if (failure.reason)
                    reason = failure.reason;
                else if (failure.exception) {
                    var exception = failure.exception;
                    if (exception instanceof String)
                        reason = exception;
                    else if (exception.message) {
                        reason = exception.message;
                    }
                }
                if (reason == null || reason.length == 0)
                    reason = "server error";
                return "901:" + args + ":" + reason;;       
            }
            return null;
        }
        
        function initListener(msg) {
            var error = null;
            var publish = true;
            if (msg.successful !== true) {
                error = msg.error;
                if (error == null) {
                    var failure = msg.failure;
                    if (failure != null) {
                        if (failure.websocketCode) {
                            publish = false;
                        } else {
                            var statusCode = failure.httpCode;
                            if (statusCode == null || statusCode == 0)
                                error = "900::no connection to server";
                            else
                                error = resolveError(msg);
                        }
                    }
                }
            }
            if (publish) {
                if (msg.successful) {
                    clientId = msg.clientId;
                    var ext = msg.ext;
                    if (ext != null) {
                        application = ext["de.zeos.zen2.application"];
                        appProperties = ext["de.zeos.zen2.security"];
                    }
                    initialized();
                } else {
                    cometD.disconnect();
                    status = CometDManager.Status.DISCONNECTED;
                }
                if (communicationListener && communicationListener.initialized && isFunction(communicationListener.initialized))
                    communicationListener.initialized(msg.successful, clientId, error, msg);
            }
        }
        
        function subscriptionListener(msg) {
            var error = null;
            if (msg.successful !== true) {
                topicSubscriptions[msg.subscription].handle = null;
                error = msg.error;
                if (error == null)
                    error = resolveError(msg);
            }
            if (msg.successful)
                subscriptionActive(msg.subscription);
            else
                subscriptionBroken(msg.subscription, error);
            if (communicationListener && communicationListener.subscription && isFunction(communicationListener.subscription))
                communicationListener.subscription(msg.successful, msg.subscription, error);
        }
        
        function connectListener(msg) {
            if (cometD.isDisconnected()) {
                connected = false;
                return;
            }
            var wasConnected = connected;
            connected = msg.successful;
            if (!wasConnected && msg.successful) {
                status = CometDManager.Status.CONNECTED;
                connectionAvailable();
                if (communicationListener && communicationListener.connected && isFunction(communicationListener.connected))
                    communicationListener.connected();
                if (manualDisconnect) {
                    manualDisconnect = false;
                    setTimeout(function() {
                        status = CometDManager.Status.DISCONNECTING;
                        cometD.disconnect();
                    }, 1);
                }
            } else if (!msg.successful) {
                var sessionLost = false;
                var error = null;
                if (msg.error) {
                    error = msg.error;
                    if (error != null && error.indexOf("402::") == 0) {
                        sessionLost = true;
                    }
                } else {
                    error = resolveError(msg);
                }
                status = CometDManager.Status.BROKEN;
                if (wasConnected) {
                    connectionUnavailable(false);
                }
                if (communicationListener && communicationListener.broken && isFunction(communicationListener.broken))
                    communicationListener.broken(sessionLost, wasConnected, error, msg);
            }
        }
        
        function disconnectListener(msg) {
            connected = false;
            if (msg.successful === true) {
                status = CometDManager.Status.DISCONNECTED;
                connectionUnavailable(true);
                if (communicationListener && communicationListener.disconnected && isFunction(communicationListener.disconnected))
                    communicationListener.disconnected();
                if (manualReconnect) {
                    manualReconnect = false;
                    setTimeout(function() {cm.start(credentials);}, 1);
                }
            }
        }
        
        function publish(channel, msg) {
            var confirmation = null;
            if (communicationListener && communicationListener.published && isFunction(communicationListener.published)) {
                confirmation = function(res) {
                    var error = null;
                    if (!res.successful) {
                        error = res.error;
                        if (error == null)
                            error = resolveError(res);
                    }
                    communicationListener.published(res.successful, error, msg, res);
                };
            }
            cometD.publish(channel, msg, confirmation);
        }
        
        function initialized() {
            for (var key in topicSubscriptions) {
                var desc = topicSubscriptions[key];
                desc.handle = null;
                var scopedTopic = desc.scopedTopic;
                var currentTopicStr = scopedTopic.topic;
                if (scopedTopic.scope != null)
                    currentTopicStr += "/" + scopedTopic.scope;
                if (currentTopicStr != scopedTopic.stringRep) {
                    for (var i = 0; i < desc.messageListeners.length; i++) {
                        cm.registerMessageListener(scopedTopic.topic, scopedTopic.scope, desc.messageListeners[i]);
                    }
                    delete topicSubscriptions[key];
                } else {
                    subscribe(scopedTopic);
                }
            }
        }
        
        function connectionAvailable() {
            for (var i = 0; i < connectionListeners.length; i++) {
                connectionListeners[i].connected();
            }
            var channel;
            while (queue.length > 0 && !cometD.isDisconnected()) {
                channel = queue[0].scopedTopic.topic;
                if (queue[0].scopedTopic.scope != null)
                    channel += "/" + queue[0].scopedTopic.scope;
                publish(channel, queue[0].msg);
                queue.shift();
            }
        }
        
        function connectionUnavailable(sessionEnd) {
            for (var key in topicSubscriptions) {
                var desc = topicSubscriptions[key];
                var idx = desc.messageListeners.length;
                while (idx--) {
                    var listener = desc.messageListeners[idx];
                    if (listener.disconnected && isFunction(listener.disconnected))
                        listener.disconnected(sessionEnd);
                    if (sessionEnd && listener.unregisterOnSessionEnd !== false)
                        desc.messageListeners.splice(idx, 1);
                }
                if (desc.messageListeners.length == 0)
                    delete topicSubscriptions[key];
            }
            
            var idx = connectionListeners.length;
            while (idx--) {
                var listener = connectionListeners[idx];
                listener.disconnected(sessionEnd);
                if (sessionEnd && listener.unregisterOnSessionEnd !== false)
                    connectionListeners.splice(idx, 1);
            }
        }
        
        function subscriptionActive(topic) {
            if (typeof topic === 'string')
                topic = new ScopedTopic(topic);
            var listeners = findListeners(topic);
            for (var i = 0; i < listeners.length; i++)
                if (listeners[i].connected && isFunction(listeners[i].connected))
                    listeners[i].connected();
        }
        
        function subscriptionBroken(topic, error) {
            if (typeof topic === 'string')
                topic = new ScopedTopic(topic);
            var listeners = findListeners(topic);
            for (var i = 0; i < listeners.length; i++)
                if (listeners[i].error && isFunction(listeners[i].error))
                    listeners[i].error(error);
        }
        
        function findListeners(scopedTopic) {
            var listeners = [];
            if (topicSubscriptions[scopedTopic])
                listeners = listeners.concat(topicSubscriptions[scopedTopic].messageListeners);
            var topic = scopedTopic.stringRep;
            var pos = topic.lastIndexOf("/");
            topic = topic.substring(0, pos);
            scopedTopic = new ScopedTopic(topic + "/*");
            if (topicSubscriptions[scopedTopic])
                listeners = listeners.concat(topicSubscriptions[scopedTopic].messageListeners);
            while (true) {
                scopedTopic = new ScopedTopic(topic + "/**");
                if (topicSubscriptions[scopedTopic]) {
                    listeners = listeners.concat(topicSubscriptions[scopedTopic].messageListeners);
                }
                pos = topic.lastIndexOf("/");
                if (pos < 0)
                    break;
                topic = topic.substring(0, pos);
            }
            return listeners;
        }
        
        function subscribe(scopedTopic) {
            var topic = scopedTopic.toString();
            if (!cometD.isDisconnected()) {
                if (topicSubscriptions[topic].handle == null) {
                    topicSubscriptions[topic].handle = cometD.subscribe(topic, function(msg) {
                        var topic = msg.channel;
                        var listeners = findListeners(new ScopedTopic(topic));
                        for (var i = 0; i < listeners.length; i++) {
                            listeners[i].messageReceived(topic, msg.data);
                        }
                    });
                } else {
                    subscriptionActive(scopedTopic);
                }
            }
        }
        
        this.ApplicationScope = function(property) {
            this.toString = function() {
                var scope = cm.getApplication();
                if (property) {
                    var props = cm.getAppProperties();
                    var prop = null;
                    if (props == null)
                        prop = "<invalid>";
                    else
                        prop = props[property] || "<invalid>";
                    scope += "/" + prop;
                }
                return scope;
            };
        };
        
        this.getClientId = function() {
            return clientId;
        };
        
        this.getApplication = function() {
            return application;
        };
        
        this.getAppProperties = function() {
            return appProperties;
        };
        
        this.registerConnectionListener = function(listener) {
            connectionListeners.push(listener);
        };
        
        this.unregisterConnectionListener = function(listener) {
            var idx = connectionListeners.indexOf(listener);
            if (idx >= 0)
                connectionListeners.splice(idx, 1);
        };
        
        this.registerMessageListener = function(topic, scope, listener) {
            var scopedTopic = new ScopedTopic(topic, scope);
            if (!topicSubscriptions[scopedTopic]) {
                topicSubscriptions[scopedTopic] = {handle: null, scopedTopic: scopedTopic, messageListeners: []};
            }
            topicSubscriptions[scopedTopic].messageListeners.push(listener);
            subscribe(scopedTopic);
        };
        
        this.unregisterMessageListener = function(topic, scope, listener) {
            var scopedTopic = new ScopedTopic(topic, scope);
            if (listener == null) {
                var handle = topicSubscriptions[scopedTopic].handle;
                delete topicSubscriptions[scopedTopic];
                if (handle != null)
                    cometD.unsubscribe(handle);
            } else {
                var desc = topicSubscriptions[scopedTopic];
                var idx = desc.messageListeners.indexOf(listener);
                if (idx >= 0)
                    desc.messageListeners.splice(idx, 1);
                if (desc.messageListeners.length == 0)
                    this.unregisterMessageListener(topic, scope);
            }
        };
        
        this.isRegistered = function(topic, scope) {
            return topicSubscriptions[new ScopedTopic(topic, scope)] != null;
        };
        
        this.getStatus = function() {
            return status;
        };
        
        this.sendMessage = function(topic, scope, msg) {
            var scopedTopic = new ScopedTopic(topic, scope);
            if (!cometD.isDisconnected()) {
                publish(scopedTopic.toString(), msg);
                return 0;
            } else {
                var id = msgId++;
                queue.push({scopedTopic: scopedTopic, msg: msg, id: id});
                return id;
            }
        };
        
        this.isQueued = function(msgId) {
            for (var i = 0; i < queue.length; i++) {
                if (queue[i].id == msgId)
                    return true;
            }
            return false;
        };
        
        this.removeFromQueue = function(msgId) {
            for (var i = 0; i < queue.length; i++) {
                if (queue[i].id == msgId) {
                    queue.splice(i, 1);
                    return true;
                }
            }
            return false;
        };
        
        this.start = function(credentialsOrApplication) {
            if (status == CometDManager.Status.DISCONNECTED) {
                status = CometDManager.Status.CONNECTING;
                credentials = credentialsOrApplication;
                var cred = {};
                var app = "zen2";
                if (typeof credentialsOrApplication === "string") {
                    app = credentialsOrApplication;
                } else {
                    lang.mixin(cred, credentialsOrApplication);
                    if (cred.application) {
                        app = cred.application;
                        delete cred.application;
                    }
                }
                var handshakeProps = {
                    ext : {
                        "de.zeos.zen2.security" : cred,
                        "de.zeos.zen2.application": app
                    }
                };
                cometD.configure({
                    url : "http://localhost:8080/zen2/cometd",
                    logLevel : "info",
                    autoBatch : true
                });
                cometD.handshake(handshakeProps);
            }
            else if (status == CometDManager.Status.DISCONNECTING) {
                manualReconnect = true;
            }
        };
        this.stop = function() {
            if (status == CometDManager.Status.DISCONNECTED || status == CometDManager.Status.DISCONNECTING)
                return;
            if (status == CometDManager.Status.CONNECTING) {
                manualDisconnect = true;
            } else {
                status = CometDManager.Status.DISCONNECTING;
                cometD.disconnect();
            }
        };
    };
    CometDManager.Status = {DISCONNECTED: "disconnected", CONNECTING: "connecting", CONNECTED: "connected", BROKEN: "broken", DISCONNECTING: "disconnecting"};
    return CometDManager; 
});