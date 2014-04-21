/**
 * Support code to provide appropriate behavior for HTML elements created by
 * ApplicationLinkUIService; also, public functions that can be used from JS code
 * in other plugins to create the same kinds of HTML elements, in case it's not
 * convenient to do so on the back end.
 */
var ApplinksUtils = ApplinksUtils || (function($) {

    var pendingRequests = {},
        pendingConfirmations = {};
    
    // Provide a well-defined name for the authentication window/tab we create with
    // window.open() - may be useful in debugging, and is used by integration tests.
    // Note, IE8 does not allow this name to contain hyphens or periods.
    var authWindowName = "com_atlassian_applinks_authentication";

    /**
     * This function is basically duplicated from messages.js in AJS, because some products
     * only provide a stub version of messages.js within gadgets.
     */
    function makeCloseable(message) {
        var $icon = $('<span class="aui-icon icon-close"></span>').click(function () {
            message.trigger("messageClose", [this]).remove();
        });
        message.append($icon);
        $icon.each(AJS.icons.addIcon.init);
    }

    /**
     * Helper function to get the DOM object of the current iframe.
     * @return {Object} a DOM object, or null if we are not in an iframe
     */
    function getCurrentIframe() {
        if (window === parent.window) {
            return null;
        }
        var ret = null,
            myFrameWindow = window;
        $('iframe', parent.document.body).each(function(index) {
            if (this.contentWindow.window === myFrameWindow) {
                ret = this;
            }
        });
        return ret;
    }

    /**
     * Event handler that is called by the applinks authorization completion servlet.  It triggers
     * the completion function for any pending authorization request that matches the given applink
     * ID, and also redispatches the event to any other iframes in the current window.
     * @param {Object} eventObject  JQuery event object
     * @param {string} applinkId  application link ID
     * @param {boolean} success  true if the request was approved
     * @param {string} authAdminUri  URI of the "OAuth Access Tokens" page (will be displayed in the
     *   confirmation message)
     * @param {boolean} wasRedispatched  true if the event has been retriggered from another frame
     */
    function onAuthCompletion(eventObject, applinkId, success, authAdminUri, wasRedispatched) {
        if (applinkId in pendingRequests) {
            var request = pendingRequests[applinkId];
            if (success) {
                request.authAdminUri = authAdminUri;
                delete pendingRequests[applinkId];
            }
            completeAuthRequest(request, success);
        }
        if (!wasRedispatched && parent && (parent !== window)) {
            var myWindow = window;
            $('iframe', parent.document.body).each(function(index, frame) {
                var scope = frame.contentWindow;
                if (scope !== myWindow) {
                    if (scope.AJS && scope.AJS.$) {
                        scope.AJS.$(scope.document).trigger('applinks.auth.completion',
                            [applinkId, success, authAdminUri, true]);
                    }
                }
            });
        }
    }
      
    /**
     * Fires the appropriate event when the authorization flow has completed.  On approval, reloads
     * the window/frame unless an event handler calls {@code preventDefault()} on the event.
     * @param {Object} applinkProperties  has the same properties passed to {@link createAuthRequestBanner}
     * @param {boolean} approved  true if the request was approved
     */
    function completeAuthRequest(applinkProperties, approved) {
        var $scope = $(document);
        if (approved) {
            // Temporarily bind an event handler so our handler runs after any other handlers that
            // may exist.
            var defaultApprovalHandler = function (eventObject) {
                if (eventObject.isDefaultPrevented()) {
                    // Don't reload, just show the confirmation message
                    showAuthConfirmationBanner(applinkProperties);
                } else {
                    // Reload, but first save a reminder to make us show a confirmation message
                    // after we've reloaded.
                    registerPendingConfirmation(applinkProperties);
                    document.location.reload(true);
                }
            };
            $scope.bind('applinks.auth.approved', defaultApprovalHandler);
            $scope.trigger('applinks.auth.approved', applinkProperties);
            $scope.unbind('applinks.auth.approved', defaultApprovalHandler);
        } else {
            // There's no default behavior for a request that was denied, but fire an event in case
            // anyone is interested.
            $scope.trigger('applinks.auth.denied', applinkProperties);
        }
    }

    /**
     * Used internally to make the applink support code aware of a "please authenticate" message
     * element that has been displayed, by wiring the appropriate event handlers and adding the
     * applink's properties to an internal list of authentication requests.
     * @param $element {Object}  a JQuery object
     * @param applinkProperties {Object}  has the same properties passed to {@link createAuthRequestBanner}
     */
    function initAuthRequest($element, applinkProperties) {
        var $authLink = $element.find("a.applink-authenticate");
        
        if ($element.hasClass('aui-message')) {
            // Workaround for incomplete AJS availability in some products
            makeCloseable($element);
        }
        
        $authLink.click(function(e) {
            window.open(applinkProperties.authUri, authWindowName);
            e.preventDefault();
        });
        
        pendingRequests[applinkProperties.id] = applinkProperties;
        
        return $element;
    }
    
    /**
     * Used internally to ensure that {@link initAuthRequest} is called for every
     * authorisation request element that was generated as HTML from the back end,
     * rather than by calling {@link createAuthRequestBanner} or
     * {@link createAuthRequestInline}.  The parameters of the request are passed
     * from the back end in hidden input elements.
     */
    function initAuthRequestElements() {
        $('.applinks-auth-request').each(function(index) {
            var $e = $(this),
                applinkId = $e.find(".applinkId").val(),
                appName = $e.find(".appName").val(),
                appUri = $e.find(".appUri").val(),
                authUri = $e.find(".authUri").val();
            // Ignore request banners that have already been created (by {@link createAuthRequestBanner} or
            // {@link createAuthRequestInline})
            if (applinkId && authUri) {
                initAuthRequest($e, {
                    id: applinkId,
                    appName: appName,
                    appUri: appUri,
                    authUri: authUri});
            }
        });
    }

    /**
     * Builds a "please authenticate" banner (in a standard AUI message box) containing a link that
     * that will start authorization for an application link that needs credentials.
     * <p>
     * On completion of the authorization flow, a JQuery event will be triggered on the document,
     * with the event type "applinks.auth.approved" or "applinks.auth.denied", and an additional
     * parameter equal to the {@code applinkProperties} parameter that was passed here.
     * <p>
     * If authorization is granted (event "applinks.auth.approved"), the default behavior is for the
     * window or frame to be reloaded; also, a confirmation banner will be displayed either within
     * a &lt;div&gt; element of class "applinks-auth-confirmation-container" if one exists, or at the top of
     * the document otherwise.  Reloading of the window/frame can be disabled by having an event
     * handler call {@code preventDefault()} on the event.
     *
     * @param {Object} applinkProperties contains the following application link properties:
     *   {@code id}: the application link identifier;
     *   {@code appName}: the name of the remote application;
     *   {@code appUri}: the base URI of the remote application;
     *   {@code authUri}: the URI for starting the authorization flow
     * @return {Object} a JQuery object referring to a {@code <div>} element, which has not yet
     *   been inserted anywhere on the page; its class is "applinks-auth-request"
     */
    function createAuthRequestBanner(applinkProperties) {
        var $banner = $('<div class="aui-message warning closeable shadowed applinks-auth-request"><p><span class="aui-icon icon-applinks-key"></span></p></div>');
        // Note that we can't just use the AJS.messages.warning() function, because it will put a
        // standard warning icon in the message box and we want a custom icon.
        $banner.append(AJS.I18n.getText("applinks.util.auth.request",
                                        AJS.escapeHtml(applinkProperties.authUri),
                                        AJS.escapeHtml(applinkProperties.appUri),
                                        AJS.escapeHtml(applinkProperties.appName)));
        initAuthRequest($banner, applinkProperties);
        return $banner;
    }

    /**
     * Builds a "please authenticate" message suitable for displaying inline (in a span
     * with the class "applinks-auth-request"), containing a link that will start authorization.
     * This behaves identically to {@link createAuthRequestBanner}, except it creates a {@code <span>}
     * element instead of a {@code div} and also allows additional text to be displayed.
     * 
     * @param {string} content  optional HTML content to be displayed within the inline
     *   element (e.g. a description of the entity for which authorization is required);
     *   will not be escaped; may be null
     * @param {Object} applinkProperties  see {@link createAuthRequestBanner}
     * @return {Object} a JQuery object referring to a {@code <span>} element, which has not yet
     *   been inserted anywhere on the page; its class is "applinks-auth-request"
     */
    function createAuthRequestInline(content, applinkProperties) {
        var $lozenge = $('<span class="applinks-auth-request"></span>'),
            $contentSpan = $('<span class="applinks-request-description"></span>'),
            message = AJS.I18n.getText("applinks.util.auth.request.inline", AJS.escapeHtml(applinkProperties.authUri));
        if (content) {
            $contentSpan.append(content);
            $contentSpan.append(" - ");
        }
        $contentSpan.append(message);
        $lozenge.append($contentSpan);
        initAuthRequest($lozenge, applinkProperties);
        return $lozenge;
    }

    /**
     * Used internally to remember the fact that we have just completed authorizing an
     * applink and are about to refresh the iframe associated with it, so that we can
     * display a confirmation message after the iframe is refreshed.
     */
    function registerPendingConfirmation(applinkProperties) {
        var frame = getCurrentIframe();
        if ((!frame) || (!frame.id)) {
            return;
        }
        if (! parent.ApplinksUtils.pendingConfirmations) {
            parent.ApplinksUtils.pendingConfirmations = { };
        }
        if (!(frame.id in parent.ApplinksUtils.pendingConfirmations)) {
            parent.ApplinksUtils.pendingConfirmations[frame.id] = [];
        }
        parent.ApplinksUtils.pendingConfirmations[frame.id].push(applinkProperties);
        return;
    }

    /**
     * Called after a page load, to see if we've been refreshed due to a successful authorization.
     * If we're in an iframe, a variable will have been set on the parent window to tell us that
     * this happened.  If so, insert a confirmation banner at the top of the iframe.
     */
    function checkForPendingConfirmations() {
        if (parent && parent.ApplinksUtils && parent.ApplinksUtils.pendingConfirmations) {
            var myFrame = getCurrentIframe();
            if (myFrame) {
                if (myFrame.id in parent.ApplinksUtils.pendingConfirmations) {
                    var pendingConfirmations = parent.ApplinksUtils.pendingConfirmations[myFrame.id];
                    delete parent.ApplinksUtils.pendingConfirmations[myFrame.id];
                    for (var i = 0, n = pendingConfirmations.length; i < n; i++) {
                        showAuthConfirmationBanner(pendingConfirmations[i]);
                    }
                }
            }
        }
    }

    /**
     * Displays a confirmation banner.  If an element exists with the class
     * "applinks-auth-confirmation-contianer", it is inserted there, otherwise at the top of the
     * document.
     */
    function showAuthConfirmationBanner(applinkProperties) {
        var scope = $(document),
            banner = $('<div class="aui-message success closeable shadowed applinks-auth-confirmation"><p><span class="aui-icon icon-applinks-key-success"></span></p></div>'),
            container = scope.find('div.applinks-auth-confirmation-container');
        if (!container.length) {
            container = scope.find('body');
        }
        banner.append(AJS.I18n.getText("applinks.util.auth.confirmation",
                                        AJS.escapeHtml(applinkProperties.appUri),
                                        AJS.escapeHtml(applinkProperties.appName),
                                        AJS.escapeHtml(applinkProperties.authAdminUri)));
        makeCloseable(banner);
        container.prepend(banner);
        setTimeout(function() {
            banner.fadeOut(1000, function() {
                $(this).remove();
            });
        }, 5000);
    }
    
    /**
     * Initialization function to be called once at document ready time.
     */
    function setup() {
        // If we're in an iframe, set up an object in the parent window that we can use to
        // keep track of state even if the iframe is refreshed.
        if (parent && !(parent === window)) {
            if (! parent.ApplinksUtils) {
                parent.ApplinksUtils = { };
            }
        }
        
        $(document).bind('applinks.auth.completion', onAuthCompletion);

        initAuthRequestElements();
        checkForPendingConfirmations();
    }

    $(document).ready(setup);
    
    return {      
        createAuthRequestBanner: createAuthRequestBanner,
        createAuthRequestInline: createAuthRequestInline
    };
})(AJS.$);
