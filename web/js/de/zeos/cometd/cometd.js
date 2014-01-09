define(["dojo/_base/unload", "dojox/cometd", "dojo/_base/lang", "dojox/cometd/ack", "dojo/domReady!"], function(unloader, cometDInstance, lang) {
    var CometD = function() {
        if ((this instanceof CometD) === false)
            throw "CometD must be instantiated: new CometD()";
        
        var commListener = null;
        var configured = false;
        var initialized = false;
        var connected = false;
        var abort = false;

        var subscriptionHandles = {};
        var extensionNames = {};
        var hh = null;
        var sh = null;
        var ch = null;
        var dh = null;

        var cometD = cometDInstance;
        cometD.onListenerException = function(ex) {
            console.log("uncaught exception:", ex);
        };

        unloader.addOnUnload(function() {
            cometD.disconnect(true);
        });

        function isFunction(f) {
            return Object.prototype.toString.call(f) === '[object Function]';
        }
        
        function initListener(msg) {
            clearSubscriptions();
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
                            if (abort)
                                cometD.disconnect();
                        }
                    }
                }
            }
            initialized = msg.successful === true;
            if (publish && commListener.initialized && isFunction(commListener.initialized))
                commListener.initialized(msg.successful, msg.clientId, error, msg);
        }
        
        function subscriptionListener(msg) {
            var error = null;
            if (msg.successful !== true) {
                removeSubscription(msg.subscription);
                error = msg.error;
                if (error == null)
                    error = resolveError(msg);
            }
            if (commListener.subscription && isFunction(commListener.subscription))
                commListener.subscription(msg.successful, msg.subscription, error);
        }
        
        function connectListener(msg) {
            if (cometD.isDisconnected()) {
                connected = false;
                return;
            }
            var wasConnected = connected;
            connected = msg.successful;
            if (!wasConnected && msg.successful) {
                if (commListener.connected && isFunction(commListener.connected))
                    commListener.connected();
            } else if (!msg.successful) {
                var sessionLost = false;
                var error = null;
                if (msg.error) {
                    error = msg.error;
                    if (error != null && error.indexOf("402::") == 0) {
                        clearSubscriptions(false);
                        sessionLost = true;
                    }
                } else {
                    error = resolveError(msg);
                }
                if (commListener.broken && isFunction(commListener.broken))
                    commListener.broken(sessionLost, wasConnected, error, msg);
            }
        }
        
        function disconnectListener(msg) {
            connected = false;
            if (msg.successful === true) {
                if (commListener.disconnected && isFunction(commListener.disconnected))
                    commListener.disconnected();
                clearConnection();
            }
        }
        
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
        
        function clearSubscriptions() {
            initialized = false;
            subscriptionHandles = {};
        }
        
        function removeSubscription(sub) {
            var handle = subscriptionHandles[sub];
            if (handle) {
                cometD.removeListener(handle);
                delete subscriptionHandles[sub];
            }
        }
        
        function clearConnection() {
            cometD.removeListener(hh);
            hh = null;
            cometD.removeListener(sh);
            sh = null;
            cometD.removeListener(ch);
            ch = null;
            cometD.removeListener(dh);
            dh = null;
            clearSubscriptions();
        }
        
        this.isConnected = function() {
            return connected && !cometD.isDisconnected();
        };

        this.configure = function(communicationListener) {
            if (connected)
                throw "already connected";
            if (communicationListener == null)
                throw "commListener required";
            commListener = communicationListener;
            configured = true;
        };
        
        this.connect = function(credentials, abortOnConnectionError) {
            if (connected)
                throw "already connected";
            if (!configured)
                throw "not configured";
            abort = (abortOnConnectionError === true);
            if (hh == null) {
                hh = cometD.addListener("/meta/handshake", initListener);
            }
            if (sh == null) {
                sh = cometD.addListener("/meta/subscribe", subscriptionListener);
            }
            if (ch == null) {
                ch = cometD.addListener("/meta/connect", connectListener);
            }
            if (dh == null) {
                dh = cometD.addListener("/meta/disconnect", disconnectListener);
            }
            var handshakeProps = null;
            if (credentials != null) {
                var application = "zen2";
                var cred = {};
                if (typeof credentials === "string") {
                    application = credentials;
                    credentials = null;
                } else {
                    lang.mixin(cred, credentials);
                    if (cred.application) {
                        application = cred.application;
                        delete cred.application;
                    }
                }
                
                handshakeProps = {
                    ext : {
                        "de.zeos.zen2.security" : cred,
                        "de.zeos.zen2.application": application
                    }
                };
            }
            cometD.init({
                url : "http://localhost:8080/zen2/cometd",
                logLevel : "info",
                autoBatch : true
            }, handshakeProps);
        };
        
        this.subscribe = function(channel, subscriptionListener) {
            if (!initialized)
                throw "not initialized";
            var sub = subscriptionHandles[channel];
            if (sub)
                throw "already subscribed to " + channel;
            subscriptionHandles[channel] = cometD.subscribe(channel, function(msg) {
                if (subscriptionListener && isFunction(subscriptionListener))
                    subscriptionListener(channel, msg.data);
            });
        };
        
        this.isSubscribed = function(channel) {
            return subscriptionHandles[channel] != null;
        };
        
        this.unsubscribe = function(channel) {
            if (!connected)
                throw "not connected";
            var sub = subscriptionHandles[channel];
            if (sub == null)
                throw "not subscribed to " + channel;
            delete subscriptionHandles[channel];
            cometD.unsubscribe(sub);
        };
        
        this.publish = function(channel, msg) {
            if (!connected)
                throw "not connected";
            var confirmation = null;
            if (commListener.published && isFunction(commListener.published)) {
                confirmation = function(res) {
                    var error = null;
                    if (!res.successful) {
                        error = res.error;
                        if (error == null)
                            error = resolveError(res);
                    }
                    if (commListener.published && isFunction(commListener.published))
                        commListener.published(res.successful, error, res);
                };
            }
            cometD.publish(channel, msg, confirmation);
        };
        
        this.disconnect = function() {
            cometD.disconnect();
        };
        
        this.terminate = function() {
            var t = cometD.getTransport();
            if (t != null)
                t.abort();
            if (connected) {
                connected = false;
                clearConnection();
            }
            commListener = null;
            configured = false;
            for (var name in extensionNames) {
                this.unregisterExtension(name);
            }
        };
        
        this.registerExtension = function(name, ext) {
            cometD.registerExtension(name, ext);
            extensionNames[name] = null;
        };
        
        this.unregisterExtension = function(name) {
            cometD.unregisterExtension(name);
            delete extensionNames[name];
        };
    };
    return CometD; 
});