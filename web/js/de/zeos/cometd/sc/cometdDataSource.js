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
                        if (that.messageResolver) err = that.messageResolver(err) || err;
                        dsResponse.status = isc.DSResponse.STATUS_FAILURE;
                        dsResponse.data = err;
                        
                    } else if (dsResponse.validationErrors) {
                        var errors = {};
                        for (var field in dsResponse.validationErrors) {
                            var err = dsResponse.validationErrors[field]; 
                            if (that.messageResolver) err = that.messageResolver(err) || err;
                            errors[field] = err;
                        }
                        dsResponse.status = isc.DSResponse.STATUS_VALIDATION_ERROR;
                        dsResponse.errors = errors;
                    } else if (dsResponse.result) {
                        dsResponse.status = 0;
                        dsResponse.data = ds.recordsFromObjects(dsResponse.result);
                        for (var i = 0; i < dsResponse.data.length; i++) {
                            var d = dsResponse.data[i]; 
                            if (d._system === true) {
                                d._canEdit = false;
                                d._canRemove = false;
                            }
                            d._persistent = true;
                        }
                    }
                    this.finished();
                    ds.processResponse(requestId, dsResponse);
                };
                this.error = function(err) {
                    if (err.startsWith("403:"))
                        err = that.messageResolver("errSecurity") || "General security error";
                    var dsResponse = {
                        status: isc.DSResponse.STATUS_FAILURE,
                        data: err
                    };
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
            
            if (data._constructor) {
                console.log("NEED TO IMPLEMENT ADVANCED CRIT", dsRequest.data.criteria);
            } else {
                function filter(fields, dataObj) {
                    var critObj = {};
                    for (var p in dataObj) {
                        if (fields[p]) {
                            var value = dataObj[p];
                            if ((params.mode == "UPDATE" || params.mode == "CREATE") && fields[p].canEdit !== false) {
                                var type = fields[p].type;
                                var typeDS = type != null ? isc.DataSource.get(type) : null;
                                if (typeDS) {
                                    var nestedObj = dataObj[p];
                                    if (isc.isAn.Array(nestedObj)) {
                                        var arr = [];
                                        for (var i = 0; i < nestedObj.length; i++) {
                                            arr[i] = filter(typeDS.fields, nestedObj[i]);
                                        }
                                    } else {
                                        value = filter(typeDS.fields, nestedObj);
                                    }
                                }
                            }
                            critObj[p] = value;
                        }
                    }
                    return critObj;
                }
                params.criteria = filter(this.fields, data);
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