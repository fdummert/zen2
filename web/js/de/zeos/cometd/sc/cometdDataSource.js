define(["../cometdReqRes"], function(CometDRequestResponse) {
    isc.defineClass("CometDDataSource", "DataSource");
    isc.CometDDataSource.addProperties({
        dataProtocol: "clientCustom",
        dataFormat: "custom",
        clientOnly: true,
        
        dataSourceRootChannel: "/service",
        dataSourceRequestChannel: null,
        dataSourceResponseChannel: null,
        dataSourcePushChannel: null,
        timeout: 10000,
        scope: null,
        scopeResolver: null,
        messageResolver: null,
        pushScopes: null,
        cm: null,
        dataView: null,
        
        transformRequest: function(dsRequest) {
            var that = this;
            function DSResponseListener(ds, requestId) {
                this.success = function(dsResponse) {
                    if (dsResponse.error) {
                        var err = dsResponse.error;
                        if (that.messageResolver) err = that.messageResolver(err);
                        dsResponse.data = err;
                        
                    } else if (dsResponse.validationErrors) {
                        var errors = {};
                        for (var field in dsResponse.validationErrors) {
                            var err = dsResponse.validationErrors[field]; 
                            if (that.messageResolver) err = that.messageResolver(err);
                            errors[field] = err;
                        }
                        dsResponse.errors = errors;
                    } else if (dsResponse.result) {
                        dsResponse.data = ds.recordsFromObjects(dsResponse.result);
                    }
                    this.finished();
                    ds.processResponse(requestId, dsResponse);
                };
                this.timeout = function() {
                    var dsResponse = {
                        status: isc.DSResponse.STATUS_SERVER_TIMEOUT
                    };
                    this.finished();
                    ds.processResponse(requestId, dsResponse);
                };
                this.finished = function() {
                    isc.EH.hideClickMask("cometDS");
                };
            }
        
            var params = isc.addProperties({}, dsRequest.params);
            var data = isc.addProperties({}, params.data, dsRequest.data);
            if (data._constructor) {
                console.log("NEED TO IMPLEMENT ADVANCED CRIT", dsRequest.data.criteria);
            } else {
                params.criteria = {};
                for (var p in data) {
                    params.criteria[p] = data[p];
                }
            }
            
            if (dsRequest.operationType) {
                var val = dsRequest.operationType;
                switch (val) {
                    case "add":
                        val = "CREATE";
                        break;
                    case "fetch":
                        val = "READ";
                        break;
                    case "update":
                        val = "UPDATE";
                        break;
                    case "remove":
                        val = "DELETE";
                        break;
                }
                params.mode = val;
            }
            if (dsRequest.startRow != null) {
                params.pageFrom = dsRequest.startRow;
            }
            if (dsRequest.endRow != null) {
                params.pageTo = dsRequest.endRow - 1;
            }
            if (dsRequest.sortBy) {
                console.log("NEED TO IMPLEMENT SORTING", dsRequest.sortBy);
                params.sorts = [];
            }
            if (dsRequest.parentNode != null) {
                params.parent = isc.Tree.getCleanNodeData(value);
            }
            var reqChannel = this.dataSourceRequestChannel || this.getChannel("/req");
            var resChannel = this.dataSourceResponseChannel || this.getChannel("/res");
            var scope = this.scope;
            if (scope == null && this.scopeResolver != null && isc.isA.Function(this.scopeResolver)) {
                scope = this.scopeResolver();
            }
            if (dsRequest.showPrompt === true) {
                isc.EH.clickMaskProperties = { cursor: "wait" };
                isc.EH.showClickMask(null, "hard", null, "cometDS");
            }
            var crr = new CometDRequestResponse(this.cm, reqChannel, resChannel, scope, this.timeout);
            crr.sendRequest(params, new DSResponseListener(this, dsRequest.requestId));
            return params;
        },
        
        registerPushListener: function(scopes) {
            if (this.pushListener == null) {
                var that = this;
                this.pushListener = {
                    messageReceived: function(topic, dsResponse) {
                        dsResponse.data = that.recordsFromObjects(dsResponse.data);
                        if (that.processPushResponse && isc.isA.Function(that.processPushResponse)) {
                            if (that.processPushResponse(topic, dsResponse) === true) {
                                that.updateCaches(dsResponse);
                                if (that.processedPushResponse && isc.isA.Function(that.processedPushResponse)) {
                                    that.processedPushResponse(topic, dsResponse);
                                }
                            }
                        }
                    }
                };
            }
            var pushChannel = this.dataSourcePushChannel || this.getChannel("/push");
            if (scopes != null)
                scopes = isc.isAn.Array(scopes) ? scopes : [ scopes ];
            if (this.pushScopes != null) {
                for (var i = 0; i < this.pushScopes.length; i++) {
                    this.cm.unregisterMessageListener(pushChannel, this.pushScopes[i], this.pushListener);
                }
            }
            this.pushScopes = scopes;
            if (this.pushScopes != null) {
                for (var i = 0; i < this.pushScopes.length; i++) {
                    this.cm.registerMessageListener(pushChannel, this.pushScopes[i], this.pushListener);
                }
            }
        },
        
        getChannel: function(infix) {
            return this.dataSourceRootChannel + "/" + this.cm.getApplication() + "/dv" + infix + "/" + this.dataView;
        }
    });
    return null;
});