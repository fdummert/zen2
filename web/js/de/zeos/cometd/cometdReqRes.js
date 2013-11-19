define(function() {
    var CometDRequestResponse = function(cm, reqTopic, resTopic, scope, timeout, idField) {
        if ((this instanceof CometDRequestResponse) === false)
            throw "CometDRequestResponse must be instantiated: new CometDRequestResponse()";
        
        var pendingRequests = {};
        var ID_FIELD = idField || "requestId";
        
        function ReqResMessageListener(listener) {
            this.messageReceived = function(topic, msg) {
                var id = msg[ID_FIELD];
                if (id != null) {
                    var resDescr = pendingRequests[id];
                    if (resDescr) {
                        delete pendingRequests[id];
                        clearTimeout(resDescr.timerHandle);
                        cm.unregisterMessageListener(resTopic, scope, resDescr.listener);
                        listener.success(msg);
                    } else {
                        if (console && console.log)
                            console.log("unknown response message received (matching no request)", msg);
                    }
                    
                } else {
                    if (console && console.log)
                        console.log("unknown response message received without " + ID_FIELD, msg);
                }
            };
        }
        
        this.sendRequest = function(msg, listener) {
            var reqResMsgListener = new ReqResMessageListener(listener);
            cm.registerMessageListener(resTopic, scope, reqResMsgListener);
            var id = msg[ID_FIELD];
            if (id == null) {
                id = (Math.random() * new Date().getTime()).toString(36);
                msg[ID_FIELD] = id;
            }
            var timerHandle = setTimeout(function() {
                var resDescr = pendingRequests[id]; 
                if (resDescr) {
                    delete pendingRequests[id];
                    if (resDescr.msgId > 0)
                        cm.removeFromQueue(resDescr.msgId);
                    cm.unregisterMessageListener(resTopic, scope, reqResMsgListener);
                    if (listener && listener.timeout) {
                        listener.timeout();
                    }
                }
            }, timeout);
            pendingRequests[id] = {
                listener: reqResMsgListener,
                timerHandle: timerHandle,
                msgId: cm.sendMessage(reqTopic, scope, msg)
            };
        };
    };
    return CometDRequestResponse;
    
});