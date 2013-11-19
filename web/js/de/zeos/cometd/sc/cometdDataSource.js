define(["../cometdReqRes"], function(CometDRequestResponse) {
    isc.defineClass("CometDDataSource", "DataSource");
    isc.CometDDataSource.addProperties({
        dataProtocol: "clientCustom",
        dataFormat: "custom",
        clientOnly: true,
        
        sendMetaData: true,
        metaDataPrefix: "_",
        dataSourceRootChannel: "/org/zeos/cometd/ds",
        dataSourceRequestChannel: null,
        dataSourceResponseChannel: null,
        dataSourcePushChannel: null,
        requestClass: "de.zeos.ds.DataSourceRequest",
        timeout: 10000,
        scope: null,
        scopeResolver: null,
        messageResolver: null,
        pushScopes: null,
        cm: null,
        
        transformRequest: function(dsRequest) {
            var that = this;
            function DSResponseListener(ds, requestId) {
                this.success = function(dsResponse) {
                    if (dsResponse.status == isc.DSResponse.STATUS_FAILURE) {
                        if (that.messageResolver && dsResponse.data) {
                            var err = that.messageResolver(dsResponse.data);
                            dsResponse.data = err || dsResponse.data;
                        }
                    } else if (dsResponse.status == isc.DSResponse.STATUS_VALIDATION_ERROR && dsResponse.errors && that.messageResolver) {
                        for (var field in dsResponse.errors) {
                            var err = that.messageResolver(dsResponse.errors[field]);
                            dsResponse.errors[field] = err || dsResponse.errors[field];
                        }
                    } else if (dsResponse.status == isc.DSResponse.STATUS_SUCCESS) {
                        if (dsResponse.data != null)
                            dsResponse.data = ds.recordsFromObjects(dsResponse.data);
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
            params.data = isc.addProperties({}, params.data, dsRequest.data);
            if (this.sendMetaData) {
                if (!this.parameterNameMap) {
                    var map = {};
                    
                    map[this.metaDataPrefix + "operationType"] = "operationType";
                    map[this.metaDataPrefix + "startRow"] = "startRow";
                    map[this.metaDataPrefix + "endRow"] = "endRow";
                    map[this.metaDataPrefix + "sortBy"] = "sortBy";
                    map[this.metaDataPrefix + "textMatchStyle"] = "textMatchStyle";
                    map[this.metaDataPrefix + "oldValues"] = "oldValues";
                    map[this.metaDataPrefix + "parentNode"] = "parentNode";

                    this.parameterNameMap = map;
                }
                
                for (var parameterName in this.parameterNameMap) {
                    var value = dsRequest[this.parameterNameMap[parameterName]];
                    if (value != null) {
                        if (parameterName == "_parentNode") {
                            params[parameterName] = isc.Tree.getCleanNodeData(value);
                        } else {
                            params[parameterName] = value;
                        }
                    }
                }
                params[this.metaDataPrefix + "dataSource"] = this.getID();
                params["isc_metaDataPrefix"] = this.metaDataPrefix;
            }
            if (this.requestClass)
                params.className = this.requestClass;
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
            return this.dataSourceRootChannel + infix + "/" + this.name;
        }
    });
    return null;
});