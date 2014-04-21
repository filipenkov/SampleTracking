/**
 * @namespace JIRA.Project
 * @author Scott Harwood
 */
JIRA.Project = (function() {

    /**
     * Checks if the user has permissions
     * @function {Private} permissionsCheck
     * @param {Object} response
     * @param {String} url
     */
    var permissionsCheck = function(xhr, response, url) {
        var loginPageRedirect = xhr && xhr.getResponseHeader('X-Atlassian-Dialog-Control') == 'permissionviolation';
        var permissionError = response && response.permissionError;
        if (loginPageRedirect || permissionError) {
            window.location.href = url.replace(/\?.*/,"");
            return false;
        } else {
            return true;
        }
    };

    return {

        /**
         * Singleton that adds ajax functionality to tabs to make requests async
         * @function {Public} navigationTabs
         * @returns {Function}
         */
        navigationTabs: function () {

            var
            loadEvents = {},// {Object} - contains a hash map of functions to be executed after specified tabs are loaded
            loadTab,        // {Object} - tab that is active when the page first loads
            projectTab,     // {Object} container where response from ajax tabs is injected
            activeTab,      // {Object} */
            previousTab,    // {Object} we hold the previous tab here, just in case something goes wrong we can switch back
            xhrObject,      // {Object} we keep reference to the xhrObject in case we need to abort
            tabs,
            
            CONST = {
                projectTabSelector: "#project-tab", // where async response is loaded
                tabsSelector: "ul.vertical.tabs li", // actual tab links
                requestParams: "decorator=none&contentOnly=true", // params to ensure response is not decorated with furniture
                stateRequestParams: "decorator=none&contentOnly=true&updateState=true",
                loadedTabClass: "loaded", // class applied to selected and loaded tabs
                activeTabClass: "active", // class applied to selected tabs
                hashSuffix: "-panel" // this string is appended to the hash value in the address bar to locate the active tab
            },
            /**
             * @function {Private} runTabLoadEvent
             * @param {String} hashMapID
             */
            runTabLoadEvent = function (hashMapID) {
                if (loadEvents[hashMapID] && loadEvents[hashMapID] instanceof Array) {
                    jQuery(loadEvents[hashMapID]).each(function () {
                        this();
                    });
                }
            },
            /**
             * Toggles active state from previous to specified tab and makes a request using the "href" attribute as the url
             * @function {Private} navigateToTab
             * @param {Object} tab - Anchor element of browse action
             */
            navigateToTab = function (tab, historyEvent, url) {

                var tabAnchor = jQuery(tab).find("a"), id = url.replace(/^[^\?]*\?/,""),

                populateTab = function (contentObj, textStatus, xhr) {
                    
                    if (permissionsCheck(xhr, contentObj, tabAnchor.attr("href"))) {
                        // removes event handlers
                        // append evaluates scripts, which we need because we have taken out the content are putting it back in again
                        projectTab.empty();
                        projectTab.append(contentObj.content);
                        if (!historyEvent) {
                            // add to the history stack
                            dhtmlHistory.add(id);
                        }
                        runTabLoadEvent(tabAnchor.attr("id"));
                        JIRA.userhover(projectTab);
                        activeTab.addClass(CONST.loadedTabClass);
                    }
                };

                if (/\?selectedTab=/.test(window.location.href)) {
                    window.location.href = window.location.href.replace(/\?(.*)/,"#" + id);
                    return;
                }

                // We require a url to continue, otherwise there will be errors
                if (url && (activeTab.get(0) !== tab || url)) {
                    if (activeTab && activeTab.length) {
                        // remove active styling (css) from previously active tab
                        previousTab = activeTab.removeClass(CONST.activeTabClass).removeClass(CONST.loadedTabClass);
                    }
                    // apply active styling (css)
                    activeTab = jQuery(tab).addClass("active");
                    // handling for race conditions 
                    // if someone is a little click happy, we don't want their previous requests to be successful
                    if (xhrObject && xhrObject.get(0) && xhrObject.get(0).readyState !== 4 && xhrObject.get(0).abort) {
                        if (jQuery.isFunction(xhrObject.hideThrobber)) {
                            xhrObject.hideThrobber();
                        }
                        // abort abort! We might show the incorrect content otherwise
                        xhrObject.get(0).abort();
                    }
                    // lets appear like our response is instantaneous by injecting some html straight away
                    projectTab.html("<h2>" + tabAnchor.html()  + "</h2>");
                    // finally perform the ajax request
                    xhrObject = jQuery(jQuery.ajax({
                        method: "get",
                        dataType: "json",
                        url: url,
                        data: CONST.requestParams,
                        success: populateTab,
                        error: function (xhr, error) {
                            if (error === "parsererror") {
                                window.location.href = url;
                            } else {
                                permissionsCheck(xhr, null, url);

                                projectTab.html("<div style=\"padding:0 20px\">" +
                                        AJS.extractBodyFromResponse(xhr.responseText) + "</div>");
                            }
                        }
                    })).throbber({target: tab}); // lets use the throbber plugin, we will only see the throbber when the request is latent...
                }
            },
            /**
             * Handler for history events. e.g browser back and forward buttons
             * @function {Private} handleBrowserNavigation
             * @param {String} newLocation - hash history flag
             */
            handleBrowserNavigation = function (newLocation) {
                var node; /* {Object} tab to be loaded */

                if (activeTab.find("a").attr("href").replace(/.*\?/,"") === newLocation) {
                    return;
                }

                if (newLocation && newLocation !== "") {
                    // there is a hash in the address bar, lets try and get the associated tab. Need to escape the selector.
                    node = getTab(newLocation);
                    newLocation = jQuery(node).find("a").attr("href").replace(/\?.*/, "?" + newLocation);
                } else if (newLocation === "") {
                    // there is no hash! We must be back to where we first started, before we started creating
                    // asynchronous requests
                    node = jQuery(loadTab);
                    newLocation = node.find("a").attr("href");
                }
                // if there was an associated tab, then it would have a length
                if (node) {
                    // we have an associated tab so lets navigate to it, and pass "true" so that we
                    // don't register another history event
                    navigateToTab(node, true, newLocation);
                }
            },

            getTab = function (url) {
                var tabRegExp = /selectedTab=[^(?:\:|%3A)]*(?:\:|%3A)([^&]*)/, tabToTarget = url.match(tabRegExp), tab;
                jQuery(tabs).each(function() {
                    var tabToCompare = jQuery(this).find("a").attr("href").match(tabRegExp);
                    if (tabToTarget && tabToTarget.length > 0 && tabToCompare[1] === tabToTarget[1]) {
                        tab = this;
                    }
                });
                return tab;
            };

            /**
             * Internet Explorer as usual is sh*t and we can't use the standard jQuery methods to initialise
             * dhtmlHistory because the jQuery "ready" event fires to early.
             *
             * We use a closure here to gain reference to the old window.onload. We will then execute it within our own context
             * so we don't override previous functionality.
             *
             * @function {Global} onload
             */
            window.onload = function (onload) {
                return function () {            
                    if (jQuery.isFunction(onload)) {
                        // execute previous onload
                        onload();
                    }
                    // setup ajax history
                    dhtmlHistory.initialize();
                    // this listener will handler all history events
                    dhtmlHistory.addListener(handleBrowserNavigation);
                };
            }(window.onload);

            return {

                /**
                 * @function getActiveTab
                 * @return {Object}
                 */
                getActiveTab: function () {
                    return activeTab;
                },

                /**
                 * @function getProjectTab
                 * @return {Object}
                 */
                getProjectTab: function () {
                    return projectTab;
                },


                /**
                 * Events to call after a tab is loaded via ajax. Commonly used to assign event handlers to new content.
                 * @function addLoadEvent
                 * @return {Object}
                 */
                addLoadEvent: function (tabName, handler) {
                    loadEvents[tabName] = loadEvents[tabName] || [];
                    if (jQuery.isFunction(handler)) {
                        loadEvents[tabName].push(handler);
                    }
                },


                /**
                 * @function {Public} init
                 */
                init: function () {
                    var addressTab;
                    // this is where we will inject all our html fragments
                    projectTab = jQuery(CONST.projectTabSelector);
                    // lets loop through and apply our event handlers
                    tabs = jQuery(CONST.tabsSelector).each(function () {
                        if (jQuery(this).hasClass(CONST.activeTabClass)) {
                            // stores active tab
                            activeTab = jQuery(this);
                            // if not then this must be the tab the user wants, so I will set it as the initial "loadTab"
                            loadTab = jQuery("#" + activeTab.find("a").attr("id")).parent();
                        }


                    });
                    addressTab = getTab(window.location.href);
                    // check if the user wants a different tab then the one that is loaded
                    if (dhtmlHistory.getCurrentHash() && addressTab && activeTab && activeTab.find("a").attr("href").replace(/.*\?/,"") !== dhtmlHistory.getCurrentHash()) {
                        // lets go ahead and load it for them then
                        navigateToTab(jQuery(addressTab), true, jQuery(addressTab).find("a").attr("href").replace(/\?.*/, "?" + dhtmlHistory.getCurrentHash()));
                    }
                     // Having report param in links plays silly buggers with ajax navigation, so removing it.
                    jQuery(document).click(function(e){
                        var node = e.target;
                        if (node && node.nodeName !== "A") {
                            node = node.parentNode;
                        }
                        if (node && node.nodeName === "A") {
                            var tab = getTab(node.href);
                            if (tab) {
                                navigateToTab(tab, false, node.href);
                                e.preventDefault();
                            }
                        }
                    });
                }
            };
        }(),

        /**
         * Singleton that handles dropdowns. We also delegate tab load events here, as some of these dropdowns are loaded
         * via ajax and as a result will not be initialised by jQuery(document).ready()
         * @function {Public} pagination
         * @returns {Function}
         */
        dropdowns: function () {

            var quickLinks = function () {

                var quickLinks = AJS.$("#quicklinks");

                AJS.Dropdown.create({
                    trigger: quickLinks.find(".aui-dd-link"),
                    content: quickLinks.find(".aui-list"),
                    alignment: AJS.RIGHT
                });

                AJS.$("#quicklinks .aui-dd-link").linkedMenu();
                return arguments.callee;
            }();

            var $quickCreate = AJS.$("#create-issue");

            AJS.Dropdown.create({
                trigger: $quickCreate.find(".aui-dd-link"),
                content: $quickCreate.find(".aui-list"),
                alignment: AJS.RIGHT
            });

            AJS.$(["summary-panel-panel", "version-summary-panel-panel", "component-summary-panel-panel"]).each(function(){
                    JIRA.Project.navigationTabs.addLoadEvent(this, quickLinks);
            });
        },

        /**
         * Singleton that handles async expanding fragments
         * @function {Public} expandos
         * @returns {Function}
         */
        expandos: function () {

            var CONST = {
                containerSelector: "li.expando", // this is where the click listener will be
                linkSelector: ".versionBanner-link", // we will use this link's "href" attribute to make the request
                contentClass: "versionBanner-content", // we will inject the html fragment here
                activeClass: "active", // applied to container when expanded
                tweenSpeed: "fast", // speed of expand/contract
                requestParams: "decorator=none&contentOnly=true&noTitle=true", // params to ensure response is not decorated with furniture
                collapseVersionParam: "collapseVersion", // this value is toggled in the href attribute request correct fragment
                expandVersionParam: "expandVersion" // this value is toggled in the href attribute request correct fragme
            };

            return function () {

                var handler = function () {
                    // we are using event delegation to avoid assigning event handlers each time the tab is loaded via ajax
                    JIRA.Project.navigationTabs.getProjectTab().find(".versionBanner-header").click(function(e) {
                        // lets use event delegation, to check if what we are click on is an expando
                        var parent = jQuery(this).parent(), contentElement = parent.find("." + CONST.contentClass),
                                linkTarget = jQuery(this).find(CONST.linkSelector);
                        // if we click on a link then bail out and follow link
                        if (e.target.nodeName === "A" || jQuery(e.target).parent().get(0).nodeName === "A") {
                            return;
                        }
                        // if this element is not active then I assume we are expanding it
                        if (!parent.hasClass(CONST.activeClass) && !contentElement.is(":animated")) {
                            // we are now active
                            parent.addClass(CONST.activeClass);

                            var url = linkTarget.attr("href");
                            var throbberTarget = {target: JIRA.Project.navigationTabs.getActiveTab()};
                            // make request
                            jQuery(jQuery.ajax({

                                url: url,
                                data: CONST.requestParams,
                                dataType: "json",
                                error: function(xhr) {
                                    permissionsCheck(xhr, null, url);
                                },
                                success: function (response, textStatus, xhr) {
                                    if (permissionsCheck(xhr, response, url)) {
                                        if (contentElement.length === 0) {
                                            // if we don't have a place to inject the response lets make one
                                            contentElement = jQuery("<div>").css({
                                                display: "block",
                                                overflow: "hidden",
                                                height: "0"
                                            }).addClass(CONST.contentClass).appendTo(parent).click(function (e) {
                                                e.stopPropagation();
                                            });
                                        }
                                        // lets add content, I am assuming there is no event handlers on this content,
                                        // otherwise this approach has the potential to create memory leaks
                                        contentElement.html(response.content);
                                        // expand (had issues with slide toggle for ie7, so using animate instead)
                                        contentElement.css({display: "block", overflow: "hidden"}).animate({height: contentElement.attr("scrollHeight")},  CONST.tweenSpeed,function(){
                                            // get ready for the next time we click(contract)
                                            linkTarget.attr("href", linkTarget.attr("href").replace(CONST.expandVersionParam, CONST.collapseVersionParam));
                                            parent.addClass("expanded");
                                        });
                                    }
                                }
                            })).throbber(throbberTarget);  // lets use the throbber plugin, we will only see the throbber when the request is latent...
                        // if this element is active then I assume we are contracting it
                        } else if (parent.hasClass(CONST.activeClass) && !parent.hasClass("locked")) {
                            // retains hidden state if we reload the page
                            jQuery.get(linkTarget.attr("href") + "&" + CONST.requestParams, function () {
                                // we are not active anymore
                                parent.removeClass(CONST.activeClass);
                                // expand (had issues with slide toggle for ie7, so using animate instead)
                                contentElement.css({overflow: "hidden"}).animate({
                                    height: 0
                                }, CONST.tweenSpeed, function () {
                                    contentElement.css({display: "none"});
                                    // get ready for the next time we click(expand)
                                    linkTarget.attr("href", linkTarget.attr("href").replace(CONST.collapseVersionParam, CONST.expandVersionParam));
                                    parent.removeClass("expanded");
                                });

                            });

                        }
                    });
                    return arguments.callee;
                }();
                JIRA.Project.navigationTabs.addLoadEvent("roadmap-panel-panel", handler);
                JIRA.Project.navigationTabs.addLoadEvent("changelog-panel-panel", handler);
                JIRA.Project.navigationTabs.addLoadEvent("component-roadmap-panel-panel", handler);
                JIRA.Project.navigationTabs.addLoadEvent("component-changelog-panel-panel", handler);
            };
        }()
    };
})();

// Initialise on page load to ensure that all HTMLelements are available to be manipulated
jQuery(document).ready(JIRA.Project.navigationTabs.init); // I need to be first
jQuery(document).ready(JIRA.Project.expandos);
jQuery(document).ready(JIRA.Project.dropdowns);

/** Preserve legacy namespace
    @deprecated jira.app.browseProject */
AJS.namespace("jira.app.browseProject", null, JIRA.Project);
