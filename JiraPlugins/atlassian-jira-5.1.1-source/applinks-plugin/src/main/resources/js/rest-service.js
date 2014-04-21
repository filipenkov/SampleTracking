/**
 * The triggering of AppLinks initialisation can be customised by setting a function on AJS.AppLinksInitialisationBinder.
 * The binder function should take a single argument which is a zero-arg function to run and should execute this function
 * when appropriate.
 */
AppLinks = AJS.$.extend(window.AppLinks || {}, {
    Event: {
        NAMESPACE: "applinks",
        PREREADY: this.NAMESPACE + ".preready",
        READY: this.NAMESPACE + ".ready"
    }
});

// Is there an overridden initialisation binder?
if (AJS.AppLinksInitialisationBinder) {
    AppLinks.initialisationBinder = AJS.AppLinksInitialisationBinder;
} else {
    // The default bind if no specific binder is specified
    AppLinks.initialisationBinder = function(f) {
        AJS.toInit(f);
    }
}

AppLinks.initialisationBinder(function() {
    AppLinks = AJS.$.extend(window.AppLinks || {}, {
        failure: function(data) {
            if (data.status == 401) {
                window.location.reload();
            } else {
                var message = AppLinks.parseError(data);
                var errorDivs = AJS.$('.page-error');
    
                if (errorDivs.length > 0) {
                    errorDivs.html(message).fadeIn('slow');
                }
                else alert("REST request failed: " + message);
            }
        },
        jsonRequest: function(url, type, data, success, error) {
            if (data) {
                data = JSON.stringify(data);
            }
            AJS.$(".page-error").fadeOut('fast');
            if (!error) error = AppLinks.failure;
            jQuery.ajax({
                url: url,
                type: type,
                data: data,
                dataType: 'json',
                contentType: "application/json; charset=utf-8",
                cache: false,
                success: success,
                error: error
            });
        },
        parseError: function(errorData) {
            var error;
            try {
                error = JSON.parse(errorData.responseText);
            } catch (e) {
                if (errorData.statusText) {
                    return error = errorData.statusText;
                } else {
                    return errorData;
                }
            }
            if (error.message) {
                if (AJS.$.isArray(error.message)) {
                    return error.message.join(' ');
                }
                return error.message;
            }
            else {
                return errorData.statusText;
            }
        },
        put: function(url, data, success, error) {
            AppLinks.jsonRequest(url, 'PUT', data, success, error);
        },
        post: function(url, data, success, error) {
            AppLinks.jsonRequest(url, 'POST', data, success, error);
        },
        update: function(data, success, error) {
            AppLinks.put(AppLinks.self_link(data), data, success, error);
        },
        get: function(url, success, error) {
            AppLinks.jsonRequest(url, 'GET', null, success, error);
        },
        self_link: function(item) {
            for (var i = 0, _i = item.link.length; i < _i; i++) {
                var link = item.link[i];
                if (link.rel == "self") return link.href;
            }

            throw "No self-link found";
        },
        del: function(urlOrObject, success, error) {
            var url;
            if (typeof(urlOrObject) == 'string') url = urlOrObject;
            else url = AppLinks.self_link(urlOrObject);
            AppLinks.jsonRequest(url, 'DELETE', null, success, error);
        },
        SPI: {
            BASE_URL: AJS.contextPath() + "/rest/applinks/1.0",
            getAllLinks: function(success, failure) {
                var url = AppLinks.SPI.BASE_URL + "/applicationlink";
                AppLinks.get(url, success, failure);
            },
            getAllLinksWithAuthInfo: function(success, failure) {
                var url = AppLinks.SPI.BASE_URL + "/listApplicationlinks";
                AppLinks.get(url, success, failure);
            },
            getLinksOfType: function(typeId, success, failure) {
                var url = AppLinks.SPI.BASE_URL + "/applicationlink/type/" + typeId;
                AppLinks.get(url, success, failure);
            },
            tryToFetchManifest: function(url, success, failure) {
                var restUrl = AppLinks.SPI.BASE_URL + '/applicationlinkForm/manifest.json?url=' + encodeURIComponent(url);
                AppLinks.get(restUrl, success, failure);
            },
            getManifestFor: function(id, success, failure) {
                var url = AppLinks.SPI.BASE_URL + '/manifest/' + id + ".json";
                AppLinks.get(url, success, failure);
            },
            createStaticUrlAppLink: function(applicationType, success, failure) {
                var restUrl = AppLinks.SPI.BASE_URL + '/applicationlinkForm/createStaticUrlAppLink?typeId=' + applicationType;
                AppLinks.post(restUrl, null, success, failure);
            },
            createLink: function(applicationLink, username, password, createTwoWayLink, customRpcUrl, rpcUrl, configFormValues, success, failure) {
                var url = AppLinks.SPI.BASE_URL + '/applicationlinkForm/createAppLink';
                var data = {
                    applicationLink: applicationLink,
                    username: username,
                    password: password,
                    createTwoWayLink: createTwoWayLink,
                    customRpcURL: customRpcUrl,
                    rpcUrl: rpcUrl,
                    configFormValues: configFormValues
                };
                AppLinks.post(url, data, success, failure);
            },
            createLinkWithOrphanedTrust : function(applicationLink, username, password, createTwoWayLink, customRpcUrl, rpcUrl, configFormValues, orphanedTrust, success, failure) {
                var url = AppLinks.SPI.BASE_URL + '/applicationlinkForm/createAppLink';
                var data = {
                    applicationLink: applicationLink,
                    username: username,
                    password: password,
                    createTwoWayLink: createTwoWayLink,
                    customRpcURL: customRpcUrl,
                    rpcUrl: rpcUrl,
                    configFormValues: configFormValues,
                    orphanedTrust: orphanedTrust
                };
                AppLinks.post(url, data, success, failure);
            },
            verifyTwoWayLinkDetails : function (remoteUrl, rpcUrl, username, password, success, failure) {
                var url = AppLinks.SPI.BASE_URL + '/applicationlinkForm/details';
                var data = {
                    username: username,
                    password: password,
                    remoteUrl: remoteUrl,
                    rpcUrl: rpcUrl
                }
                AppLinks.post(url, data, success, failure);
            },
            getApplicationLinkInfo: function (appId, success, error) {
                var url = AppLinks.SPI.BASE_URL + "/applicationlinkInfo/id/" + appId;
                AppLinks.get(url, success, error);
            },
            deleteLink: function(applicationLink, reciprocate, success, error) {
                var url = AppLinks.SPI.BASE_URL + "/applicationlink/" + applicationLink.id;
                if (reciprocate) url += "?reciprocate=true";
                AppLinks.del(url, success, error);
            },
            makePrimary: function(applicationLink, success) {
                var url = AppLinks.SPI.BASE_URL + "/applicationlink/primary/" + applicationLink.id;
                AppLinks.post(url, null, success);
            },
            relocate: function(applicationLink, newUrl, suppressWarnings, success, error) {
                var url = AppLinks.SPI.BASE_URL + "/relocateApplicationlink/" + applicationLink.id + "?newUrl=" + encodeURIComponent(newUrl) +
                        "&nowarning=" + (suppressWarnings ? "true" : "false");
                AppLinks.post(url, null, success, error);
            },
            legacyUpgrade: function(applicationLink, success, error) {
                var url = AppLinks.SPI.BASE_URL + "/upgrade/legacy/" + applicationLink.id;
                AppLinks.post(url, null, success, error);
            },
            ualUpgrade: function(applicationLink, body, success, error) {
                var url = AppLinks.SPI.BASE_URL + "/upgrade/ual/" + applicationLink.id;
                AppLinks.post(url, body, success, error);
            },
            getEntityTypesForApplicationType: function(applicationType, success, error) {
                var url = AppLinks.SPI.BASE_URL + "/type/entity/" + applicationType;
                AppLinks.get(url, success, error);
            },
            getLocalEntitiesWithLinksToApplication: function(applicationLinkId, success, error) {
                var url = AppLinks.SPI.BASE_URL + "/entitylink/localEntitiesWithLinksTo/" + applicationLinkId + ".json";
                AppLinks.get(url, success, error);
            },
            getEntityLinksForApplication: function(applicationLinkId, success, error) {
                var url = AppLinks.SPI.BASE_URL + "/entities/" + applicationLinkId + ".json";
                AppLinks.get(url, success, error);
            },
            getEntityLinksForApplicationUsingAnonymousAccess: function(applicationLinkId, success, error) {
                var url = AppLinks.SPI.BASE_URL + "/entities/anonymous/" + applicationLinkId + ".json";
                AppLinks.get(url, success, error);
            },
            createNonUalEntityLink: function(localType, localKey, applicationId, remoteTypeId, remoteKey, name, success, error) {
                var url = AppLinks.SPI.BASE_URL + "/entitylink/" + localType + "/" + localKey + "?reciprocate=false";
                var data = {
                    applicationId: applicationId,
                    typeId: remoteTypeId,
                    key: remoteKey,
                    name: name,
                    isPrimary: false
                };
                AppLinks.put(url, data, success, error);
            },
            createEntityLink: function(localType, localKey, entity, reciprocate, success, failure) {
                var url = AppLinks.SPI.BASE_URL + "/entitylink/" + localType + "/" + localKey + "?reciprocate=";
                url += (reciprocate ? "true" : "false");
                AppLinks.put(url, entity, success, failure);
            },
            getConfiguredEntityLinks: function(localType, localKey, success, error) {
                var url = AppLinks.SPI.BASE_URL + "/entitylink/primaryLinks/" + localType + "/" + localKey + ".json";
                AppLinks.get(url, success, error);
            },
            deleteEntityLink: function(localTypeId, localKey, entity, reciprocate, success, error) {
                var url = AppLinks.SPI.BASE_URL + "/entitylink/" + localTypeId + "/" + localKey + "?typeId=" + entity.typeId + "&key=" + entity.key + "&applicationId=" + entity.applicationId + "&reciprocate=" + reciprocate;
                AppLinks.del(url, success, error);
            },
            makePrimaryEntityLink: function(localTypeID, localKey, entity, success, error) {
                var url = AppLinks.SPI.BASE_URL + "/entitylink/primary/" + localTypeID + "/" + localKey + "?typeId=" + entity.typeId + "&key=" + entity.key + "&applicationId=" + entity.applicationId;
                AppLinks.post(url, null, success, error);
            },
            canDeleteAppLink: function(applicationId, success, error) {
                var url = AppLinks.SPI.BASE_URL + "/permission/reciprocate-application-delete/" + applicationId;
                AppLinks.get(url, success, error);
            },
            canDeleteEntityLink: function(localTypeId, localKey, entity, success, error) {
                var url = AppLinks.SPI.BASE_URL + "/permission/reciprocate-entity-delete/" + entity.applicationId + "/" + localTypeId + "/" + localKey + "/" + entity.typeId + "/" + entity.key;
                AppLinks.get(url, success, error);
            },
            canCreateReciprocateEntityLink: function(applicationId, success, error) {
                var url = AppLinks.SPI.BASE_URL + "/permission/reciprocate-entity-create/" + applicationId;
                AppLinks.get(url, success, error);
            },
            processPermissionCode: function(settings) {
                var config = {
                    noPermission: function() {},
                    missing: function() {},
                    credentialsRequired: function(authUrl) {},
                    authenticationFailed: function(authUrl) {},
                    noAuthentication: function(authUrl) {},
                    noAuthenticationConfigured: function() {},
                    noConnection: function() {},
                    allowed: function() {},
                    unrecognisedCode: function(code) {},
                    updateView: function(message, icon, button) {}
                };

                if (!settings) settings = {};

                settings = AJS.$.extend(config, settings);

                return function(data) {
                    var code = data.code;
                    if (code == "NO_PERMISSION") {
                        settings.noPermission();
                    } else if (code == "MISSING") {
                        settings.missing();
                    } else if (code == "CREDENTIALS_REQUIRED") {
                        settings.credentialsRequired(data.url);
                    } else if (code == "AUTHENTICATION_FAILED") {
                        settings.authenticationFailed(data.url);
                    } else if (code == "NO_AUTHENTICATION") {
                        settings.noAuthentication(data.url);
                    } else if (code == "NO_AUTHENTICATION_CONFIGURED") {
                        settings.noAuthenticationConfigured();
                    } else if (code == "NO_CONNECTION") {
                        settings.noConnection();
                    } else if (code == "ALLOWED") {
                        settings.allowed();
                    } else {
                        settings.unrecognisedCode(data.code);
                    }
                };
            },
            addAuthenticationTrigger: function(target, authUrl, callbacks) {
                if (!callbacks) {
                    callbacks = {};
                }

                if (typeof callbacks.onSuccess == "undefined") {
                    callbacks.onSuccess = function() {
                        location.reload();
                    }
                }

                if (typeof callbacks.onFailure == "undefined") {
                    callbacks.onFailure = function() {
                        return true;
                    }
                }
                //Unbind previous click listener, otherwise we might end up opening multiple windows.
                AJS.$(target).unbind('click');
                AJS.$(target).click(function() {
                    if (callbacks.before) {
                        callbacks.before();
                    }
                    AppLinks.authenticateRemoteCredentials(authUrl, callbacks.onSuccess, callbacks.onFailure);
                });
            },
            deleteOrphanedTrust: function(id, type, success, error) {
                var url = AppLinks.SPI.BASE_URL + "/orphaned-trust/" + type + "/" + id;
                AppLinks.del(url, success, error);
            },
            getOrphanedTrust: function(success, error) {
                var url = AppLinks.SPI.BASE_URL + "/orphaned-trust/";
                AppLinks.get(url, success, error);
            }
        }
    });

    var i18nRootKey = 'applinks';

    // very simple i18n param interpolation, doesn't attempt to respect escaping
    var interpolate = function(text, params) {
        if (params) {
            if (!AJS.$.isArray(params)) {
                // single arg
                params = [new String(params)];
            }
            for (var i = 0; i < params.length; i++) {
                var rx = new RegExp("\\{" + i + "\\}", "g");
                text = text.replace(rx, params[i]);
            }
        }
        return text;
    };

    AppLinks.UI = {
        showInfoBox: function(message) {
            AJS.$('.aui-message.success').remove();
            AppLinks.UI.createMessage('success', message, 'page-info');
        },
        hideInfoBox: function() {
            AJS.$('.aui-message.success').remove();
        },
        showErrorBox: function(message) {
            AppLinks.UI.createMessage('error', message, 'page-error');
        },
        hideErrorBox: function() {
            AJS.$('.aui-message.error').remove();
        },
        showWarningBox: function(messages) {
            if (AJS.$.isArray(messages) && messages.length > 0) {
                var ulEl = AJS.$("<ul></ul>");
                AJS.$(messages).each(function(index) {
                    ulEl.append(AJS.$("<li/>", {
                        text: messages[index]
                    }));
                });
                var messageEl = AJS.$('<div class="page-warning"></div>').append(ulEl);
                AppLinks.UI.createMessage('warning', messageEl.html(), 'page-warning');
            } else {
                AppLinks.UI.createMessage('warning', messages, 'page-warning');
            }
        },
        hideWarningBox: function() {
            AJS.$('.aui-message.warning').remove();
        },
        shortenString: function(message, maxLength) {
            if (message.length  > maxLength) {
               message = message.substring(0, maxLength) + "...";
            }
            return message;
        },
        createMessage: function(type, message, cssClass) {
          var messageEl = AJS.$('<div class="' + cssClass + '">');
          messageEl.html(message);
            AJS.messages[type](".applinks-message-bar", {
                title: "",
                body: messageEl.wrap('<div></div>').parent().html(),
                closeable: true,
                shadowed: true
            });
        },
        displayValidationErrorMessages: function (errorClass, rootEl, messages) {
            if (AJS.$.isArray(messages)) {
                AJS.$(messages).each(function(i,v) {
                   var d = AJS.$('<div class="error applinks-error">');
                   d.text(v);
                   AJS.$(rootEl).find("." + errorClass).append(d);
                });
            } else {
                var d = AJS.$('<div class="error applinks-error">');
                d.text(messages.toString());
                AJS.$(rootEl).find("." + errorClass).append(d);
            }
        },
        displayValidationError: function(errorClass, rootEl, errorFn) {
            return function(xhr) {
                if (xhr.status == 401) {
                    window.location.reload();
                    return;
                }
                
                AJS.$('.applinks-error').remove();
                AJS.$('.loading').remove();
                var respJSON = xhr.responseText;
                var respObj = AJS.$.parseJSON(respJSON);
                var messages = respObj.message;
                if (typeof respObj.fields == "undefined") {
                    AppLinks.UI.displayValidationErrorMessages(errorClass, rootEl, messages);
                } else {
                    var fields = respObj.fields;
                    AJS.$(fields).each(function(index) {
                        var d = AJS.$('<div class="error applinks-error" id="' + fields[index] + '-error">');
                        d.text(messages[index]);
                        if (AJS.$(rootEl).find('.' + fields[index]).length > 0) {
                          d.insertAfter(AJS.$(rootEl).find('.' + fields[index]));
                        } else {
                          d.insertAfter(AJS.$(rootEl).find('.' + errorClass).append(d));
                        }
                    });
                }
                AJS.$(rootEl).find('.' + errorClass).addClass("fully-populated-errors")
                if (errorFn) {
                 errorFn();
                }
            }
        },
        addProtocolToURL : function(url) {
            var newUrl = AJS.$.trim(url);
            var tempURL = newUrl.toLowerCase();
            var hasProtocol = false;
            if (tempURL.length >= 7) {
                if (tempURL.substring(0,7).indexOf('http') != -1) {
                    hasProtocol = true;
                }
            }
            //default protocol is http
            if (!hasProtocol) {
                newUrl = 'http://' + newUrl;
            }
            return newUrl;
        },
        /**
         * Similar to the standard Javascript join() method, but nicer in that
         * it uses a different delimiter for the last node (by default "and"),
         * so that:
         * {code}
         * "1, 2 and 3" == prettyJoin(['1', '2', '3'], function(value) {return value;});
         * {code}
         *
         * @param inputArray
         * @param resolveFn
         * @param finalDelimiter
         */
        prettyJoin : function(inputArray, resolveFn, finalDelimiter) {
            if (!finalDelimiter) {
                finalDelimiter = AppLinks.I18n.getText('applinks.and');
            }
            var maxLength = inputArray.length;
            var message = "";
            AJS.$.each(inputArray, function(index, value) {
                if (index == (maxLength - 1) && maxLength > 1) {
                  message += " " + finalDelimiter + "  " + resolveFn(value);
                } else {
                  message += resolveFn(value);
                  if (index + 2 < maxLength) {
                      message += ", ";
                  }
                }
            });
            return message;
        },
        showLoadingIcon: function(element) {
            AJS.$('<span class="loading">&nbsp;</span>').insertAfter(element);
        },
        hideLoadingIcon: function(element) {
            AJS.$(element).next('.loading').remove();
        },
        findUrl: function(text) {
            var url = undefined;
            var lcText = text.toLowerCase();
            var startOfUrl = lcText.indexOf('http:');
            if (startOfUrl == -1) {
                startOfUrl = lcText.indexOf('https:');
            }
            if (startOfUrl > -1) {
                var endOfUrl = lcText.indexOf(' ', startOfUrl);
                if (endOfUrl == -1) {
                    endOfUrl = lcText.length;
                }
                url = text.substring(startOfUrl, endOfUrl); // use _case-sensitive_ version to retrieve the actual URL
            }
            return url;
        },
        findApplicationType : function(id) {
            id = id.toLowerCase();
            if (id.indexOf("jira") != -1) {
                return "jira";
            } else if (id.indexOf("fisheye") != -1) {
                return "fecru";
            } else if (id.indexOf("confluence") != -1) {
                return "confluence";
            } else if (id.indexOf("refapp") != -1) {
                return "refapp";
            } else {
                return undefined;
            }
        },
        escapeSelector: function(selector) {
            // based on http://samuelsjoberg.com/archive/2009/09/escape-jquery-selectors
            return selector.replace(/([#;&,\.\+\*\~':"\!\^$\[\]\(\)=>\|])/g, "\\$1");
        },
        sanitiseHTML: function(input) {
            var replacements = {
                "<": "&lt;",
                '"': "&quot;",
                "&": "&amp;"
            };
            return input.replace(/[<"&]/g, function(match) {
                return replacements[match];
            });
        },
        refreshOrphanedTrust: function() {
            // post dialog -- check whether we need to remove any orphaned-trust entries
            var updateOrphanedTrust = function(data) {
                AJS.$("tr.orphaned-trust-row").each(function() {
                    var $this = AJS.$(this);
                    var id = $this.attr("data-id");
                    var type = $this.attr("data-type");
                    var stillExists = false;
                    for (var i = 0; i < data.orphanedTrust.length; i++) {
                        var ot = data.orphanedTrust[i];
                        if (id == ot.id && type == ot.type) {
                            stillExists = true;
                            break;
                        }
                    }
                    if (!stillExists) {
                        $this.remove();
                        if (data.orphanedTrust.length == 0) {
                            // we just removed the last orphaned trust cert, hide warning!
                            AJS.$(".orphaned-trust-warning").hide();
                        }
                    }
                });
            };

            AppLinks.SPI.getOrphanedTrust(updateOrphanedTrust);
        },
        removeCssClass: function(element, prefix) {
            AJS.$(element).removeClass( function(index, className) {
                   var classes = className.split(' ');
                   var classToRemove = "";
                   AJS.$.each(classes, function(index, value) {
                       if (value.indexOf(prefix) != -1) {
                           classToRemove = value;
                       }
                   });
                   return classToRemove;
            } );
        }
    };

    AppLinks.I18n = {
        getTextWithPrefix: function(key, params) {
            return interpolate(appLinksI18n.entries[i18nRootKey + "." + key], params);
        },
        getText: function(key, params) {
            return interpolate(AppLinks.I18n.resolveValue(key), params);
        },
        getApplicationTypeName: function(typeId) {
            return appLinksI18n.entries["applinks.application.type." + typeId];
        },
        getEntityTypeName: function(typeId) {
            return appLinksI18n.entries["applinks.entity.type." + typeId];
        },
        getPluralizedEntityTypeName: function(typeId) {
            return appLinksI18n.entries["applinks.entity.type.plural." + typeId];
        },
        getAuthenticationTypeName: function(type) {
            return appLinksI18n.entries["applinks.auth.provider." + type];
        },
        resolveValue: function(key) {
            var value = appLinksI18n.entries[key];
            return typeof value == "undefined" ? key : value;
        }
    };

    AppLinks.Docs = {
        /**
         * NOTE: this is a dynamically generated version of the link build in _help_link.vm, any update here should be 
         * applied there.
         * @method createDocLink
         * @param pageKey a key that maps to a page in ual-help-paths.properties
         * @param sectionKey (Optional) a key that maps to an anchor section id in ual-help-paths.properties
         * @return an html &lt;a&gt; element targeting the specified page & section
         */
        createDocLink: function(pageKey, sectionKey, css) {
            if (!css) {
                css = "";
            } else {
                css = " " + css;
            }
            return AJS.$("<a/>", {
                "class": "ual-help-link" + css,
                href: AppLinks.Docs.getDocHref(pageKey, sectionKey),
                target: "_blank",
                text: AppLinks.I18n.getText("applinks.help"),
                title: AppLinks.I18n.getText("applinks.help")
            });
        },
        /**
         * @method getDocHref
         * @param pageKey a key that maps to a page in ual-help-paths.properties
         * @param sectionKey (Optional) a key that maps to an anchor section id in ual-help-paths.properties
         * @return the url of the given page and section (if specified)
         */
        getDocHref: function(pageKey, sectionKey) {
            var link = AppLinks.Docs.resolveValue('applinks.docs.root') + '/' + AppLinks.Docs.resolveValue(pageKey);
            if (sectionKey) {
                link += '#' + AppLinks.Docs.resolveValue(sectionKey);
            }
            return link;
        },
        resolveValue: function(key) {
            var value = applinksDocs.entries[key];
            return typeof value == "undefined" ? key : value;
        }
    };

    AJS.$(document).trigger(AppLinks.Event.PREREADY);
    AJS.$(document).trigger(AppLinks.Event.READY);
});
