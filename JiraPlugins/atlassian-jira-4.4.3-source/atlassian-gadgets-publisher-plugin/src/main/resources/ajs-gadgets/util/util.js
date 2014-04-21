

/*
 * This is a bridge for jQuery ajax to open social make request. Please note that some options have been ommitted, Some
 * others have been added.
 *
 * Available options include ->
 *
 * authorization:
 * The type of authorization to use for this request. Must be one of the values of the gadgets.io.AuthorizationType
 * enum. See http://www.opensocial.org/Technical-Resources/opensocial-spec-v08/gadgets-reference08#gadgets.io.AuthorizationType
 *
 * beforeSend:
 * You may return false in function to cancel the request.
 *
 * complete:
 * A function to be called when the request finishes (after success and error callbacks are executed). The
 * function gets passed two arguments: The response object and settings
 *
 * contentType:
 * When sending data to the server, use this content-type. Default is "application/x-www-form-urlencoded",
 * which is fine for most cases.
 *
 * data:
 * Data to be sent to the server. It is converted to a query string, if not already a string. It's appended to the
 * url for GET-requests. Object must be Key/Value pairs. If value is an Array, jQuery serializes multiple values with
 * same key i.e. {foo:["bar1", "bar2"]} becomes '&foo=bar1&foo=bar2'.
 *
 * dataType:
 * The type of data that you're expecting back from the server.
 *
 * error:
 * A function to be called if the request fails.
 * The function is passed two arguments: the response object & settings
 *
 * global:
 * Whether to trigger global AJAX event handlers for this request. The default is true.
 * Set to false to prevent the global handlers like ajaxStart or ajaxStop from being triggered.
 * This can be used to control various .
 *
 * oauthServiceName:
 * The name of the OAuth service, as specified in the gadget's OAuth <Service> element, to use for this request. This is
 * used for one of two reasons: when a gadget needs to access multiple, independent OAuth services, and when a gadget
 * requires manually-entered consumer information. When a gadget needs to access multiple OAuth services (for example,
 * to mash up data from two different applications) the service name is used simply to distinguish between the two sets
 * of service URLs when making requests. When a gadget needs to use manually-entered consumer information (which is
 * often required by non-Atlassian OAuth service providers) the oauthServiceName parameter, and the name attribute of
 * the <Service> element in the gadget spec, must match the name configured by the administrator in the OAuth Service
 * Providers administration page. Defaults to "" if not specified, which will match a <Service> element with a missing
 * or empty name attribute.  This option is ignored if the authorization option is omitted or set to
 * gadgets.io.AuthorizationType.NONE.
 *
 * signOwner:
 * Whether to tell the container to include information about the gadget owner in OAuth requests.
 *
 * signViewer:
 * Whether to tell the container to include information about the gadget viewer in OAuth requests.
 *
 * success: A function to be called if the request succeeds. The function gets passed two arguments: The data returned
 * from the server, formatted according to the 'dataType' parameter, and a string describing the status.
 *
 * type:
 * The type of request to make ("POST" or "GET"), default is "GET". Note: Other HTTP request methods, such as PUT and
 * DELETE, can also be used here, but they are not supported by all browsers.
 *
 * url: The URL to request.
 * 
 */

/* need to override serializeArray because it will not serialize multi selects if no selection is made. See JRA-17827. */
AJS.$.fn.serializeArray = function() {
    return this.map(function(){
        return jQuery.nodeName(this, "form") ?
            jQuery.makeArray(this.elements) : this;
    })
    .filter(function(){
        return this.name && !this.disabled &&
            (this.checked || /select|textarea/i.test(this.nodeName) ||
                /text|hidden|password/i.test(this.type));
    })
    .map(function(i, elem){
        var val = jQuery(this).val();

        if (val == null) {
            val = "";
        }

        return val.constructor == Array ?
            jQuery.map( val, function(val, i){
                return {name: elem.name, value: val};
            }) :
            {name: elem.name, value: val};
    }).get();
};


AJS.$.extend({

    /**
     * @method put
     * @param url
     * @param data
     * @param callback
     * @param type
     */
    put: function (url, data, callback, type) {
        if ( jQuery.isFunction( data ) ) {
			callback = data;
			data = {};
		}

		return AJS.$.ajax({
			type: "PUT",
			url: url,
			data: data,
			success: callback,
			dataType: type
		});
    },

    del: function (url, data, callback, type) {
        if ( jQuery.isFunction( data ) ) {
			callback = data;
			data = {};
		}

		return AJS.$.ajax({
			type: "delete",
			url: url,
			data: data,
			success: callback,
			dataType: type
		});
    },

	ajax: function(ajax) {

        var activeReq = 0, dressResponse = function (xhr) {
            var res = {};
            res.rc = xhr.status;
            if (xhr.responseText !== "") {
                res.data = gadgets.json.parse(xhr.responseText);
            }
            return res;
        };

        AJS.$.each( "ajaxOpen,ajaxClosed,ajaxOauthApproval".split(","), function(i,o){
            AJS.$.fn[o] = function(f){
                return this.bind(o, f);
            };
        });


        AJS.$(document).ajaxSend(function(evt, req, options){
            if (++activeReq === 1) {
               AJS.$.event.trigger("ajaxOpen", [req, options]);
            }
        });

        AJS.$(document).ajaxComplete(function(evt, req, options){
            if (--activeReq === 0) {
                AJS.$.event.trigger("ajaxClosed", [req, options]);
            }
        });

        AJS.$.fn.ajaxComplete = function(f){
            f = function (actualF) {
                return function(evt, response, options) {
                    if (response.status) {
                        actualF.call(this, evt, response, options);
                    } else {
                        actualF.apply(this, arguments);
                    }
                };
            }(f);
            return this.bind("ajaxComplete", f);
        };

        AJS.$.fn.ajaxError = function(f){
            f = function (actualF) {
                return function(evt, response, options) {
                    if (response.status) {
                        actualF.call(this, evt, dressResponse(response), options);
                    } else {
                        actualF.apply(this, arguments);
                    }
                };
            }(f);
            return this.bind("ajaxError", f);
        };


        return function (usrOptions) {

            // creating seperate request options for opensocial params "just in case" of matching keys
            var reqOptions = {};

            // Our fake XHR object for makeRequests
            var xhr = {
                status: 0,
                abort: function () {
                    reqOptions.success = function () {};
                }
            };

            // for single param get requests
            if (typeof options === "string") {
                options = {url: options};
            }

            var options = usrOptions;

            for (var name in AJS.$.ajaxSettings) {
                if (!options[name] && options[name] !== false) {
                    options[name] = AJS.$.ajaxSettings[name];
                }
            }


            if (options.baseUrl) {
                if (!/^(http)|(https):\/\//.test(options.url)){
                    options.url = options.baseUrl + options.url;
                }
            }

            // we are on the same domain so we can use a standard ajax request, without the need to use shindig as a proxy
            if (typeof atlassian !== "undefined" && atlassian.util) {
                if (atlassian.util.getRendererBaseUrl() === options.baseUrl && new RegExp(options.baseUrl).test(options.url)) {
                    // make response object similar to response object from gadgets.io.makeRequest
                    if (options.error) {
                        options.error = function (error) {
                            return function (xhr, type) { 
                                if (!xhr.responseText) {
                                    options.success.apply(this, arguments);
                                } else {
                                    error.call(this, dressResponse(xhr));
                                }
                                options.error = error;
                            };
                        }(options.error);
                    }


                    return ajax.call(this, options);
                }
            }

            //gadgetContentType only supports DOM, FEED, JSON or TEXT.
            var gadgetContentType = options.dataType.toUpperCase();
            switch (gadgetContentType) {
                case "XML":
                    gadgetContentType = "DOM";
                case "JSON":
                case "TEXT":
                    reqOptions[gadgets.io.RequestParameters.CONTENT_TYPE] = gadgets.io.ContentType[gadgetContentType];
                    break;
                default:
                    if (options.error) {
                        options.error({status: 406}, options);
                    } else {
                        AJS.$.event.trigger("ajaxError", [{status: 406}, options] );
                    }
                    return;
            }
            reqOptions[gadgets.io.RequestParameters.METHOD] = gadgets.io.MethodType[options.type.toUpperCase()];
            reqOptions.url = options.url;

            reqOptions.OWNER_SIGNED = options.signOwner;
            reqOptions.VIEWER_SIGNED = options.signViewer;

            if (options.headers) {
                reqOptions[gadgets.io.RequestParameters.HEADERS] = options.headers;
            }

            if (options.authorization) {
                reqOptions[gadgets.io.RequestParameters.AUTHORIZATION] =
                gadgets.io.AuthorizationType[options.authorization.toUpperCase()];
            }

            if (options.useToken) {
                reqOptions["OAUTH_USE_TOKEN"] = options.useToken;
            }

            if (options.oauthServiceName) {
                reqOptions[gadgets.io.RequestParameters.OAUTH_SERVICE_NAME] = options.oauthServiceName;
            }

            if (options.summaries) {
                reqOptions[gadgets.io.RequestParameters.GET_SUMMARIES] = options.summaries;
            }

            if (options.entries) {
                reqOptions[gadgets.io.RequestParameters.NUM_ENTRIES] = options.entries;
            }

            if (options.data) {
                if (options.type.toLowerCase() == "get") {
                    // AG-1263 - the url may already have params
                    var prefix;
                    if (reqOptions.url.indexOf("?") >= 0) {
                        if(reqOptions.url.lastIndexOf("&") == reqOptions.url.length - 1) {
                            prefix = "";
                        } else {
                            prefix = "&"
                        }
                    } else {
                        prefix = "?";
                    }

                    if (typeof options.data == "string") {
                        reqOptions.url += (prefix + options.data.replace(/^\?/,""));
                    } else {
                        reqOptions.url += (prefix + gadgets.io.encodeValues(options.data).replace(/^\?/,""));
                    }
                } else {
                    reqOptions[gadgets.io.RequestParameters.POST_DATA] = options.data;
                }
            }

            reqOptions.success = function (response) {

                xhr.status = response.rc; // update our fake xhr request

                if (options.complete) {
                    options.complete(response, options);
                }

                if (options.global) {
                    AJS.$.event.trigger("ajaxComplete", [response, options] );
                }

                if (response.errors && response.errors.length || response.oauthError) {
                    if (options.error) {
                        options.error(response, options);
                    }
                    if (options.global) {
                        AJS.$.event.trigger("ajaxError", [response, options] );
                    }
                    return;
                }

                // parse error
                if (options.dataType.toLowerCase() === "json" && typeof response.data === "string") {
                    if (options.error) {
                        options.error(response, options);
                    }
                    if (options.global) {
                        AJS.$.event.trigger("ajaxError", [response, options] );
                    }
                    return;
                }

                if (response.rc !== 200) {
                    if (options.error) {
                        options.error(response, options);
                    }
                    if (options.global) {
                        AJS.$.event.trigger("ajaxError", [response, options] );
                    }
                    return;
                }
                if (options.success && (response || response.oauthApprovalUrl)) {
                    options.success(response.data || response, "success", xhr);
                }
                if (options.global) {
                    AJS.$.event.trigger("ajaxSuccess", [response.data, options] );
                }
            };

            if (options.ajaxSend) {
                options.ajaxSend(options);
            }
            if (options.global) {
                AJS.$.event.trigger("ajaxSend", [options] );
            }

            // Need to return as plain old javascript object because the Internet Explorer activeX object that
            // we get will not let us append arbitary attributes to it.

            gadgets.io.makeRequest(reqOptions.url, reqOptions.success,  reqOptions);

            return xhr;

        };
    }(AJS.$.ajax)

});