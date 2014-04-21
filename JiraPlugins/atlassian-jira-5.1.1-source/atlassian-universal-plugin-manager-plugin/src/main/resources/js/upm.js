// Even though this is the main UPM functionality file, we can't gaurantee it loads first,
// so don't assume we can create the upm object
var upm = upm || {};

(function() {
    var loadFn = {
            compatibility: loadCompatibilityTab,
            osgi: loadOsgiTab,
            install: loadInstallTab,
            manage: loadManageTab
        },
        detailsFn = {
            compatibility: buildCompatibilityDetails,
            osgi : buildOsgiBundleDetails,
            install: buildInstallDetails,
            manage: buildManageDetails,
            update: buildManageDetails   // Both manage and update load the same template
        },
        osgiXrefFn = {
            'DynamicImport-Package': upm.crossReferenceOsgiImportPackageHeaderClause,
            'Import-Package': upm.crossReferenceOsgiImportPackageHeaderClause,
            'Export-Package': upm.crossReferenceOsgiExportPackageHeaderClause
        },
        // Settings for the updatable plugins list
        updatePluginsOptions = {
            isExpandable: true,
            isInstalledList: true,
            className: 'update'
        },
        // Settings for the user plugins list
        userPluginsOptions = {
            isExpandable: true,
            isInstalledList: true,
            className: 'manage',
            isUserPluginList: true
        },
        // Settings for the system plugins list
        systemPluginsOptions = {
            isExpandable: true,
            isInstalledList: true,
            className: 'manage'
        },
        pricingMultipliers = {
            commercialPurchase: 1.0,
            commercialRenewal: 0.5,
            academicPurchase: 0.5,
            academicRenewal: 0.25
        },
        minimumPurchasePrice = 10,
        hostLicense, // host license properties which we'll cache from the installed plugins query
        isLatentThreshold = 100, // don't display progress popup if request is shorter than this
        minProgressDisplay = 1000, // if progress popup is displayed, keep it up for at least this long
        defaultSecondsBeforeMessageFadeout = 4,
        pluginReps = {},
        permissions,
        progressPopup,
        safeMode,
        $binder = AJS.$('<div></div>'),
        maxIconHeight = 400, // if plugin images are taller than this, they will be scaled down
        maxIconWidth = 500, // if plugin images are wider than this, they will be scaled down
        maxResults = 25, // default number of results to fetch per request when paginating
        minProgressHeight = 220, // to keep the progress popup from being too "jumpy" when resizing to fit content
        tryUploadOrInstallAgain = true,
        filterTypeDelayMs = 200, // How long after the user stops tying to trigger the filter search
        defaultTab = 'manage',
        isDevelopmentProductVersion,
        originalIsDirtyFn,
        isUnknownProductVersion,
        isIE = AJS.$.browser.msie,
        isIE7 = isIE && parseInt(AJS.$.browser.version, 10) < 8,
        skipLatentThreshold = isIE && AJS.Confluence && AJS.Confluence.runBinderComponents;

    upm.contentTypes = {
        'bundle': 'application/vnd.atl.plugins.osgi.bundle+json',
        'install': 'application/vnd.atl.plugins.install.uri+json',
        'json': 'application/json',
        'module': 'application/vnd.atl.plugins.plugin.module+json',
        'plugin': 'application/vnd.atl.plugins.plugin+json',
        'purge-after': 'application/vnd.atl.plugins.audit.log.purge.after+json',
        'requires-restart': 'application/vnd.atl.plugins.changes.requiring.restart+json',
        'safe-mode': 'application/vnd.atl.plugins.safe.mode.flag+json',
        'update-all': 'application/vnd.atl.plugins.updateall+json',
        'upm': 'application/vnd.atl.plugins+json'
    };

    upm.resources = {};

    upm.browser = {
        isIE: isIE,
        isIE7: isIE7
    };

    /**
     * Cross-browser method for getting an XML object from XHR response
     * @method getXml
     * @param {String|Object} data The xml representation as a string if IE, or as an object if another browser
     * @return {Object} XML object
     */
    upm.getXml = function(data) {
        if (typeof data == "string") {
            try {
                // We ask IE to return a string to get around it not liking the 'application/atom+xml' MIME type
                var xml = new ActiveXObject("Microsoft.XMLDOM");
                xml.async = false;
                xml.loadXML(data);
                return xml;
            } catch (e) {
                AJS.log('Failed to create an xml object from string: ' + e);
                return data;
            }
        } else {
            // Other browsers will already return an xml object
            return data;
        }
    };

    /**
     * Encodes text, converting invalid HTML characters to escaped values.
     * @method htmlEncode
     * @param {String} text Text to be encoded
     * @return {String} encoded text
     */
    function htmlEncode(text) {
        return AJS.$('<div/>').text(text).html();
    }

    /**
     * Scrolls to a given element
     * @method scrollTo
     * @param {HTMLElement} element The element to scroll to
     */
    function scrollTo(element) {
        var spacingFromWindowTop = 10;
        AJS.$(window).scrollTop(element.offset().top - spacingFromWindowTop);
    }

    /**
     * Expands and scrolls to a specified plugin
     * @method focusPlugin
     * @param {String} key The unique key of the plugin to be focused
     * @param {String} tab The tab to look for the plugin on (same plugin may appear on multiple tabs)
     * @param {String} messageCode (optional) String key of a message to display in the plugin's detail area
     */
    upm.focusPlugin = function(key, tab, messageCode) {
        var plugin = AJS.$('#upm-plugin-' + createHash(key, tab));

        // If the plugin isn't found on the installed list on the manage tab, try the update list
        if (!plugin.length && tab == 'manage') {
            plugin = AJS.$('#upm-plugin-' + createHash(key, 'update'));
        }

        if (plugin.length) {
            // if it's a system plugin, we need to show the system plugin list first
            if (plugin.is('.upm-system') && AJS.$('#upm-system-plugins').is(':hidden')) {
                upm.toggleSystemPlugins();
            }
            if (plugin.is(':visible')) {
                var messageParams;
                if (messageCode && AJS.params[messageCode]) {
                    messageParams = {
                        body: AJS.format(AJS.params[messageCode], plugin.find('.upm-plugin-name').text()),
                        type: (messageCode.indexOf('error') == -1) ? 'success' : 'error'    
                    };
                }
                if (!plugin.is('.expanded')) {
                    plugin.bind('pluginLoaded.message', function(e) {
                        if (messageParams) {
                            upm.displayMessage(AJS.$('div.upm-details', plugin), messageParams);
                        }
                        upm.trigger('pluginFocused', [plugin]);
                        plugin.unbind('pluignLoaded.message');
                    });
                    plugin.find('div.upm-plugin-row').click();
                } else {
                    if (messageParams) {
                        upm.displayMessage(AJS.$('div.upm-details', plugin), messageParams);
                    }
                    upm.trigger('pluginFocused', [plugin]);
                }
                scrollTo(plugin);

                return plugin;
            }
        }

        // No plugin found
        return false;
    };

    /**
     * Takes a string identifying a tab and returns a boolean indicating whether or not the user has permission
     * to view that tab
     * @method hasPermissionFor
     * @param {String} tab The tab to check permissions against
     * @return {Boolean} whether or not the user has permissions
     */
    function hasPermissionFor(tab) {
        return permissions[tab];
    }

    /**
     * Returns the default tab if the user has permissions to see it, or else the first available tab
     * @method getDefaultTab
     * @return {String} The default tab, or the first tab the user has permission for
     */
    function getDefaultTab() {
        var tab;
        if (hasPermissionFor(defaultTab)) {
            return defaultTab;
        } else {
            // if we don't have permission for the default tab, return the leftmost available tab
            tab = AJS.$('#upm-tabs a:first').attr('id');
            tab = tab.substring('upm-tab-'.length, tab.length);
            return tab;
        }
    }

    /**
     * Checks to see if an ansynchronous task is pending on page load
     * @method checkForPendingTasks
     * @param {Boolean} forceSynchronous If true, forces the pending tasks request to be synchronous
     * @param {Function} errorCallback Function to run if there's an error getting pending task info
     */
    function checkForPendingTasks(forceSynchronous, errorCallback) {
        if (upm.resources['pending-tasks']) {
            AJS.$.ajax({
                url: upm.resources['pending-tasks'],
                type: 'get',
                cache: false,
                dataType: 'json',
                async: !forceSynchronous,
                success: function(response) {
                    if (response.tasks.length > 0) {
                        var task = response.tasks[0];
                        if (task.username === AJS.params.upmCurrentUsername) {
                            upm.startProgress(AJS.params.upmTextProgressPendingTasks);
                            pollAsynchronousResource(task.links.self, task.pingAfter, upm.$messageContainer, function() {
                                stopProgress();
                            });
                        } else {
                            // task was not initiated by the current user
                            updatePendingTaskDetail(task);
                            upm.$container.addClass('upm-pending-tasks');
                            pollPendingTasks();
                        }
                    }
                },
                error: function(request) {
                    upm.handleAjaxError(upm.$messageContainer, request, "");
                    errorCallback && errorCallback();
                }
            });
        }
    }

    /**
     * Sets a class on the upm container that acts as a flag for Safe Mode. If undefined default to false.
     * @method setSafeModeClass
     */
    function setSafeModeClass() {
        upm.$container.toggleClass('upm-safe-mode', !!safeMode);
    }

    /**
     * Checks to see if any changes require a product restart to complete
     * @method checkForChangesRequiringRestart
     */
    function checkForChangesRequiringRestart() {
        AJS.$.ajax({
            url: upm.resources['changes-requiring-restart'],
            type: 'get',
            cache: false,
            dataType: 'json',
            success: function(response) {
                if (response.changes.length) {
                    for (var i = 0, len = response.changes.length; i < len; i++) {
                        addChangeRequiringRestart(response.changes[i]);
                    }
                } else {
                    upm.$container.removeClass('requires-restart');
                }
            },
            error: function(request) {
                upm.handleAjaxError(upm.$messageContainer, request, "");
            }
        });
    }

    /**
     * Adds an item to the list of changes that require a product restart
     * @method addChangeRequiringRestart
     * @param {Object} change Object containing details of the specified change
     * @param {HTMLElement} list (optional) List element to append item to
     */
    function addChangeRequiringRestart(change, list) {
        addChangeRequiringRestart.msgTemplate = addChangeRequiringRestart.msgTemplate || AJS.$(AJS.$('#upm-requires-restart-template').html());
        addChangeRequiringRestart.listTemplate = addChangeRequiringRestart.listTemplate || AJS.$(AJS.$('#upm-requires-restart-list-template').html());

        // Temporary solution until back end plugin representation restartState can return "update" instead of "upgrade"
        if (change.name == 'upgrade') {
            change.name = 'update';
        }
        if (change.action == 'upgrade') {
            change.action = 'update';
        }

        // Build an AUI message if this is the first requires-restart message
        if (!upm.$container.hasClass('requires-restart')) {
            upm.$container.addClass('requires-restart');

            AJS.messages.info(upm.$messageContainer, {
                body: addChangeRequiringRestart.msgTemplate.html(),
                shadowed: false,
                closeable: false
            });
        }

        var msg = AJS.format(AJS.params['upm.requiresRestart.' + change.action], change.name),
            li = addChangeRequiringRestart.listTemplate.clone(),
            existing;

        list = list || AJS.$('#upm-requires-restart-list');

        // If an element already exists for this plugin, we want to replace it
        existing = AJS.$('#' + escapeSelector('upm-restart-message-' + change.key), list);

        li.find('span').text(msg);
        li.find('input.upm-requires-restart-cancel-uri').val(change.links.self);
        li.attr("id", "upm-restart-message-" + change.key);
        li.find('a.upm-requires-restart-cancel').attr('id', 'upm-cancel-requires-restart-' + change.key);
        if (existing.length) {
            existing.replaceWith(li);
        } else {
            li.appendTo(list);
        }
        li.data('representation', change);
    }

    /**
     * Checks for changes to the state of UPM, including Safe Mode, long-running tasks, and changes requiring restart
     * @method checkForStateChanges
     * @param {Object} response Asynchronous request response object
     */
    function checkForStateChanges(response) {
        var restartLink = response.links['changes-requiring-restart'],
            pendingTasksLink = response.links['pending-tasks'],
            hostStatus = response.hostStatus;
        
        if (hostStatus) {
            safeMode = hostStatus.safeMode;
            hostLicense = hostStatus.hostLicense;
        }

        if (upm.resources['safe-mode']) {
            setSafeModeClass();
            if (response.links['enter-safe-mode'] || response.links['exit-safe-mode-restore'] || response.links['exit-safe-mode-keep']) {
                AJS.$('#upm-safe-mode-on, #upm-safe-mode-exit-links').removeClass('hidden');
            }
        }
        if (pendingTasksLink && !pollPendingTasks.timeout) {
            upm.resources['pending-tasks'] = pendingTasksLink;
            checkForPendingTasks();
        }
        if (restartLink) {
            upm.resources['changes-requiring-restart'] = restartLink;
            checkForChangesRequiringRestart();
        }

        AJS.$.extend(upm.resources, response.links);
        
        updatePacStatus(hostStatus && hostStatus.pacDisabled, hostStatus && hostStatus.pacUnavailable);
    }

    /**
     * Checks to see if there are pending tasks and if so shows a dialog explaining to the user that the requested action has been cancelled
     * @method hasPendingTasks
     * @param {Function} callbackFn Function to run if there are pending tasks
     * @return {Boolean} Whether or not there are pending tasks
     */
    function hasPendingTasks(callbackFn) {
        checkForPendingTasks(true);
        if (upm.$container.hasClass('upm-pending-tasks')) {
            // There are pending tasks, so we don't want to run the requested action
            callbackFn();
            showInfoDialog(AJS.params.upmTextPendingTaskConflictHeader, '<p>' + AJS.params.upmTextPendingTaskConflict + '</p>');
            return true;
        } else {
            return false;
        }
    }

    /**
     * Loads content for a specified tab, updates the browser hash and visually selects the tab
     * @method loadTab
     * @param {String} id Unique id of the tab to load
     * @param {String} pluginKey (optional) Key of a plugin to expand and scroll to once tab has loaded
     * @param {String} messageCode (optional) string key of a message to display within the selected plugin's detail area
     */
    upm.loadTab = function(id, pluginKey, updateHash, messageCode) {
        var panel = AJS.$('#upm-panel-' + id),
            tab = AJS.$('#upm-tab-' + id).closest('li'),
            currentHash = document.location.hash;

        updateHash = (updateHash !== false);

        if (hasPermissionFor(id)) {
            if (panel.length === 0) {
                // if bad tab id is specified, use default tab
                id = getDefaultTab();
                panel = AJS.$('#upm-panel-' + id);
                tab = AJS.$('#upm-tab-' + id).closest('li');
            }

            AJS.$('#upm-tabs li').removeClass('upm-selected');
            tab.addClass('upm-selected');
            AJS.$('#upm-content .upm-panel').removeClass('upm-selected');
            panel.addClass('upm-selected');
            AJS.$('#upm-title').text(AJS.params['upm.tabs.' + id]);

            if (tab.hasClass('hidden')) {
                tab.removeClass('hidden');
                AJS.Cookie.save('upm.show.' + id, 'true');
            }

            // Actions to take on tab loaded event
            upm.bindOnce('panelLoaded', function(e, response) {
                checkForStateChanges(response);
                if (pluginKey) {
                    upm.focusPlugin(pluginKey, id, messageCode);
                }
            });

            loadFn[id](panel);

            // give IE some breathing room to update its UI
            setTimeout(function() {
                var hash;
                if (id == 'install' || id == 'manage') {
                    getAndStoreAntiXsrfToken();
                }

                hash = pluginKey ? id + '/' + pluginKey : id;
                if (currentHash.indexOf('#') != -1) {
                    hash = '#' + hash;
                }
                // Don't set location.hash until the page has loaded, or in Firefox the default favicon is displayed instead of UPM's.
                // See UPM-798, UPM-1153, and Mozilla bug 408415.
                // Note that if there was a message code in the hash, we always want to recompute the hash to make it go away.
                if (updateHash || messageCode) {
                    document.location.hash = hash;
                }

                // remove any plugin elements marked for removal
                AJS.$('div.upm-plugin.to-remove', upm.$container).remove();

                AJS.Cookie.save('upm.tab', id);
            }, 25);
        }
    };

    /**
     * Gets a new anti-xsrf token from the server and stores it for use in uploading plugins
     * @method getAndStoreAntiXsrfToken
     * @param {Function} callbackFn Function to run after anti-xsrf token has been retrieved
     */
    function getAndStoreAntiXsrfToken(callbackFn) {
        AJS.$.ajax({
            url: upm.resources['root'],
            type: 'head',
            cache: false,
            complete: function(request, status) {
                if (status == 'success') {
                    AJS.$('#upm-install-token').val(request.getResponseHeader('upm-token'));
                    callbackFn && callbackFn();
                } else {
                    upm.handleAjaxError(upm.$messageContainer, request, "");
                }
            }
        });
    }

    /**
     * Pouplate the UPM resources from the hidden AJS form fields on the velocity template
     * @method populateResources
     */
    function populateResources() {
        var hash;
        upm.resources = {
            'root': AJS.params.upmUriRoot,
            'pac-status': AJS.params.upmUriPacStatus,
            'updates': AJS.params.upmUriUpdates,
            'product-updates': AJS.params.upmUriProductUpdates,
            'audit-log': AJS.params.upmUriAuditLog,
            'featured': AJS.params.upmUriFeatured,
            'popular': AJS.params.upmUriPopular,
            'supported': AJS.params.upmUriSupported,
            'available': AJS.params.upmUriAvailable,
            'install': AJS.params.upmUriInstall,
            'safe-mode': AJS.params.upmUriSafeMode,
            'audit-log-purge-after': AJS.params.upmUriPurgeAfter,
            'audit-log-purge-after-manage': AJS.params.upmUriManagePurgeAfter,
            'osgi-bundles' : AJS.params.upmUriOsgiBundles,
            'osgi-services' : AJS.params.upmUriOsgiServices,
            'osgi-packages' : AJS.params.upmUriOsgiPackages,
            'pending-tasks': AJS.params.upmUriPendingTasks,
            'product-version': AJS.params.upmUriProductVersion
        };
        permissions = {
            'manage': !!upm.resources['root'],
            'update': !!upm.resources['updates'],
            'install': !!upm.resources['available'],
            'log': !!upm.resources['audit-log'],
            'compatibility': !!upm.resources['product-updates'],
            'osgi': !!upm.resources['osgi-bundles']
        };

        if (upm.resources['product-version']) {
            AJS.$.ajax({
                url: upm.resources['product-version'],
                type: 'get',
                cache: false,
                contentType: upm.contentTypes['upm'],
                dataType: 'json',
                success: function(response) {
                    isDevelopmentProductVersion = response.development;
                    isUnknownProductVersion = response.unknown;
                }
            });
        }

        AJS.$.each(permissions, function(id) {
            if (AJS.Cookie.read('upm.show.' + id) == 'true') {
                AJS.$('#upm-tab-' + id).closest('li').removeClass('hidden');
            }
        });
    };

    /**
     * Read the current tab from the window hash
     * @method loadInitialTab
     */
    upm.loadInitialTab = function() {
        hash = upm.getLocationHash(AJS.Cookie.read('upm.tab'));

        // load the manage tab so we get the updates count, but wait until the selected tab is done loading
        if (hash.tab != 'manage') {
            upm.bindOnce('panelLoaded', function() {
                loadManageTab();
            });
        }
        upm.loadTab(hash.tab, hash.key, false, hash.message);
    };

    /**
     * Returns the parsed value of the current hash in an object
     * @method getLocationHash
     * @param {String} defaultHash (optional) Default to return if no hash is present.  If not specified, default tab is used.
     * @return {Object} Object containing a tab value for the current tab and key value if there is a plugin to focus
     */
    upm.getLocationHash = function(defaultHash) {
        var hash = document.location.hash || defaultHash || getDefaultTab(),
                arr = hash.split('/'),
                tab = arr[0],
                key = arr.length > 1 ? arr[1] : '',
                message = arr.length > 2 ? arr[2] : '';
        if (tab.charAt(0) == '#') {
            tab = tab.substring(1, tab.length);
        }
        if (!hasPermissionFor(tab)) {
            tab = getDefaultTab();
        }
        return {'tab': tab, 'key': key, 'message': message};
    };

    /**
     * Gets a list of plugins from a given location, builds DOM elements and inserts them into a specified container
     * @method loadPlugins
     * @param {String} url Location of the resource to hit to retrieve the list of plugins
     * @param {Object} listOptions Object defining list characteristics
     * @param {HTMLElement} listContainer Container to load plugin list into
     * @param {HTMLElement} parentContainer (optional) Container to mark as "loaded" when finished
     */
    function loadPlugins(url, listOptions, listContainer, parentContainer) {
        var
                // check to see if there's existing pagination data
                pagination = listContainer.data('pagination') || {
                    'start-index': 0,
                    'max-results': maxResults
                },
                // so that we can tell if we're starting from scratch or appending to an existing list
                isFreshLoad = (pagination['start-index'] === 0);

        parentContainer = parentContainer || listContainer;
        if (listContainer.hasClass('loading')) {
            // if we're already loading these plugins, don't do anything
            return;
        }
        if (isFreshLoad) {
            listContainer.addClass('loading');
        }

        parentContainer.removeClass('plugin-warning-unknown-version').removeClass('plugin-warning-development-version');
        AJS.$.ajax({
            url: url,
            type: 'get',
            cache: false,
            dataType: 'json',
            data: pagination,
            success: function(response) {
                var plugins = response.plugins,
                        list,
                        button,
                        pagerElement;
                if (isFreshLoad) {
                    // create a whole new plugin list
                    listContainer.removeClass('loading').append(upm.buildPluginList(plugins, listOptions));
                    list = AJS.$('div.upm-plugin-list', listContainer);
                } else {
                    // append to an existing plugin list
                    list = AJS.$('div.upm-plugin-list', listContainer);
                    buildPluginElements(plugins, list, listOptions);
                }
                // set the start index to the number of existing plugin elements
                pagination['start-index'] = AJS.$('div.upm-plugin', list).length;
                parentContainer.addClass('loaded');

                upm.trigger('pluginsLoaded.install', [response]);

                if (isUnknownProductVersion) {
                    if (isDevelopmentProductVersion) {
                        parentContainer.addClass('plugin-warning-development-version');
                    } else {
                        parentContainer.addClass('plugin-warning-unknown-version');
                        AJS.$('#upm-install-search-form-container').addClass('hidden');
                    }
                }

                pagerElement = AJS.$('div.upm-plugin-list-pager', listContainer).removeClass('loading');
                if (plugins.length == pagination['max-results']) {
                    pagerElement.removeClass('hidden');
                    button = AJS.$('button', pagerElement);
                    button.bind('click', function(e) {
                        var pagerElement = AJS.$('div.upm-plugin-list-pager', listContainer);
                        button.blur();
                        if (!pagerElement.hasClass('loading')) {
                            pagerElement.addClass('loading');
                            button.unbind('click');
                            loadPlugins(url, listOptions, listContainer, parentContainer);
                        }
                    });
                } else {
                    pagerElement.addClass('hidden');
                }
                // save the pagination data back to the container element
                listContainer.data('pagination', pagination);
            },
            error: function(request) {
                listContainer.removeClass('loading');
                AJS.$('div.upm-plugin-list-pager', listContainer).removeClass('loading');
                upm.handleAjaxError(upm.$messageContainer, request,'');
            }
        });
    }

    /**
     * If PAC is available, remove the warning at the top of the screen
     */
    upm.checkPacAvailable = function() {
        // don't check for PAC status when on demand
        if (AJS.params.isOnDemand) {
            AJS.$("#upm-pac-checking-availability").remove();
            return;
        }

        AJS.$.ajax({
            url: upm.resources['pac-status'],
            type: 'get',
            cache: false,
            dataType: 'json',
            data: null,
            success: function(response) {
                AJS.$("#upm-pac-checking-availability").remove();
                updatePacStatus(response && response.disabled, response && !response.reached);
            },
            error: function(request) {
                AJS.$("#upm-pac-checking-availability").remove();
                AJS.$("#upm-pac-unavailable").show().removeClass("hidden");
                upm.handleAjaxError(upm.$messageContainer, request, '');
            }
        });

        // The #upm-pac-checking-availability must be displayed after 30s if no answer was
        // returned.
        AJS.$("#upm-pac-checking-availability").hide().removeClass("hidden").delay(10000).fadeIn(0);
    };

    function updatePacStatus(pacDisabled, pacUnavailable) {

        //Only do this is we are not in on demand mode
        if (!AJS.params.isOnDemand) {
            if (pacDisabled) {
                // PAC was disabled by the system admin
                AJS.$("#upm-pac-disabled").show().removeClass("hidden");
            } else if (pacUnavailable) {
                // PAC is enabled but couldn't be reached
                AJS.$("#upm-pac-unavailable").show().removeClass("hidden");
            } else {
                AJS.$("#upm-pac-disabled").addClass("hidden");
                AJS.$("#upm-pac-unavailable").addClass("hidden");
            }

        }
    }
    
    /**
     * Creates and loads a list of installable plugins
     * @method loadInstallTab
     * @param {HTMLElement} container Container to load content into
     */
    function loadInstallTab(container) {
        if (upm.resources['install']) {
            AJS.$('#upm-upload').removeClass('hidden');
        }

        if (AJS.params.isOnDemand) {
            AJS.$('#upm-install-search-form-container').addClass('hidden');
        }

        upm.bindOnce('pluginsLoaded.install', function(e, response) {
            container.addClass('loaded');
            upm.trigger('panelLoaded', [response]);
        });
        
        upm.changeDisplayedPlugins();
    }

    /**
     * Given a list of plugins, build the availalbe updates list
     * @method buildAvailableUpdates
     * @param {Array} plugins Array of plugin representations
     * @param {Function} callbackFn Function to run if request is successful
     */
    function buildAvailableUpdates(plugins, callbackFn) {
        var availableUpdatesContainer = AJS.$('#upm-available-updates'),
            pluginsList = AJS.$('#upm-update-plugin-list'),
            options = updatePluginsOptions,
            pluginCount = plugins.length,
            updateAllButton = AJS.$('#upm-update-all'),
            updateAllContainer = AJS.$('#upm-update-all-container');

        availableUpdatesContainer.removeClass('plugin-warning-unknown-version plugin-warning-development-version no-updates');

        if (hasPermissionFor('update') && pluginCount) {
            availableUpdatesContainer.removeClass('hidden');
            pluginsList.empty().append(
                upm.buildPluginList(plugins, options)
            );

            if (isUnknownProductVersion) {
                if (isDevelopmentProductVersion) {
                    availableUpdatesContainer.addClass('plugin-warning-development-version');

                    // UPM-1679 hack to move the update all button below the banner. A better solution would be to restructure the
                    // velocity and css, this is a hotfix
                    updateAllContainer.css(
                        'margin-top',
                        // Move the button down the height of the banner, plus 7px of padding
                        (availableUpdatesContainer.find('div.upm-development-product-version').outerHeight() + 7) + 'px'
                    );
                } else {
                    availableUpdatesContainer.addClass('plugin-warning-unknown-version');
                }
            }

            setUpdateCount(pluginCount);

            // Disable update button in safe mode
            if (safeMode) {
                updateAllButton.closest('li.toolbar-item').addClass('disabled');
            } else {
                updateAllButton.closest('li.toolbar-item').removeClass('disabled');
            }

            if (!upm.resources['update-all']) {
                updateAllContainer.remove();
            }

            callbackFn && callbackFn(response);
        }
    }

    /**
     * Loads content for manage tab
     * @method loadManageTab
     * @param {HTMLElement} container Container to load content into
     */
    function loadManageTab(container) {
        var userContainer = AJS.$('#upm-user-plugins'),
            updateContainer = AJS.$('#upm-available-updates'),
            systemContainer = AJS.$('#upm-system-plugins');
            filterBox = AJS.$('#upm-manage-filter-box');
        if (hasPermissionFor('manage')) {
            // if we're already loading the list, don't do anything
            if (userContainer.hasClass('loading')) {
                return;
            }
            updateContainer.addClass('hidden');
            userContainer.empty().addClass('loading');
            systemContainer.empty().addClass('loading');
            AJS.$('#upm-update-all', container).removeClass('upm-filtered');
            AJS.$('#upm-safe-mode-enable', container).removeClass('upm-filtered');
            container = container || AJS.$('#upm-panel-manage');

            setInputFilterPending(filterBox);

            AJS.$.ajax({
                url: upm.resources['root'],
                type: 'get',
                cache: false,
                dataType: 'json',
                success: function(response) {
                    var plugins = response.plugins,
                        pluginCount = plugins.length,
                        bundledPlugins = [],
                        userPlugins = [],
                        updatePlugins = [],
                        i = 0;

                    upm.resources['update-all'] = response.links['update-all'];

                    for(; plugin = plugins[i++];) {
                        // If the plugin has an update ...
                        if (plugin.links['update-details']) {
                            // If it's UPM and the back end correclty tells us the higher version...
                            if (isUpm(plugin.key) && response.upmUpdateVersion) {
                                // Show the update banner
                                upm.higherVersion = response.upmUpdateVersion;
                                displayUpmUpdatableMessage();

                                // And add it to the bundled plugins list (not update, because you update it through
                                // the banner). Store the update container in a hidden DOM element
                                bundledPlugins.push(plugin);
                                upm.upmPluginContainer = buildPluginElement(plugin, 'update', updatePluginsOptions);
                            // Otherwise add it to whichever list
                            } else {
                                updatePlugins.push(plugin);
                            }
                        } else if (plugin.userInstalled) {
                            userPlugins.push(plugin);
                        } else {
                            bundledPlugins.push(plugin);
                        }
                    }

                    // Only show the updates section if there are updates
                    if (updatePlugins.length) {
                        updateContainer.removeClass('hidden');
                        buildAvailableUpdates(updatePlugins);
                    }

                    userContainer.removeClass('loading').append(upm.buildPluginList(userPlugins, userPluginsOptions));
                    systemContainer.removeClass('loading').append(upm.buildPluginList(bundledPlugins, systemPluginsOptions));

                    container.addClass('loaded');
                    upm.trigger('panelLoaded', [response]);
                    setInputFilterEnabled(filterBox, AJS.params.upmTextFilterPlugins);
                },
                error: function(request) {
                    userContainer.removeClass('loading');
                    systemContainer.removeClass('loading');
                    upm.handleAjaxError(upm.$messageContainer, request, "");
                }
            });
        }
    };

    /**
     * Loads content for developer tab
     * @method loadOsgiTab
     * @param {HTMLElement} container Container to load content into
     */
    function loadOsgiTab(container) {
        var bundleContainer = AJS.$('#upm-osgi-bundles'),
            filterBox = AJS.$('#upm-osgi-filter-box');

        if (bundleContainer.hasClass('loading')) {
            return;
        }
        bundleContainer.empty().addClass('loading');
        container = container || AJS.$('#upm-panel-osgi');

        setInputFilterPending(filterBox);
        AJS.$.ajax({
           url: upm.resources['osgi-bundles'],
           type: 'get',
           cache: false,
           dataType: 'json',
           success: function(response) {
               var bundles = upm.buildPluginList(response.entries, {isExpandable: true, isInstalledList: false, isBundle: true, className: 'osgi'});
               bundleContainer.append(bundles);
               bundleContainer.removeClass('loading');

               container.addClass('loaded');
               upm.trigger('panelLoaded', [response]);

               setInputFilterEnabled(filterBox, AJS.params.upmTextFilterOsgiBundles);
           },
           error: function(request) {
               bundleContainer.removeClass('loading');
               upm.handleAjaxError(upm.$messageContainer, request, "");
           }
        });
    }


    /**
     * Loads content for compatibility tab
     * @method loadCompatibilityTab
     * @param {HTMLElement} container Container to load content into
     */
    function loadCompatibilityTab(container) {
        var selectElement = AJS.$('#upm-compatibility-version'),
                select = selectElement.clone(),
                check = AJS.$('input.submit', container);

        AJS.$('div.upm-compatibility-category', container).addClass('hidden');
        AJS.$('#upm-no-userinstalled').addClass('hidden');

        if (!container.hasClass('loaded') && hasPermissionFor('compatibility')) {
            select.removeAttr('disabled');
            check.attr('disabled', 'disabled');
            container.addClass('loading');

            AJS.$.ajax({
                url: upm.resources['product-updates'],
                type: 'get',
                cache: false,
                dataType: 'json',
                success: function(response) {
                    var versions = response.versions,
                            len = versions.length,
                            option;
                    if (len == 0) {
                        AJS.$('#upm-compatibility-no-versions').removeClass('hidden');
                    } else {
                        AJS.$('#upm-compatibility-versions-available').removeClass('hidden');
                        for (var i = 0; i < len; i++) {
                            option = AJS.$('<option></option>');
                            option.val(versions[i].links.self)
                                    .text(versions[i].version)
                                    .appendTo(select);
                            if (versions[i].recent) {
                                option.addClass('upm-recent');
                            }
                        }
                        selectElement.replaceWith(select);
                        check.removeAttr('disabled');
                    }
                    container.addClass('loaded').removeClass('loading');

                    upm.trigger('panelLoaded', [response]);
                },
                error: function(request) {
                    container.removeClass('loading');
                    upm.handleAjaxError(upm.$messageContainer, request, '');
                }
            });
        }
    }

    // Handle a click on a plugin row
    upm.pluginRowClick = function(e) {
        upm.togglePluginDetails(AJS.$(e.target).closest('div.upm-plugin'));
    };

    /**
     * Toggles an html element representing a plugin between an expanded and collapsed state
     * @method togglePluginDetails
     * @param {HTMLElement} container The plugin details container
     */
    upm.togglePluginDetails = function(container) {
        var hash = getPluginHash(container),
            details;
        if (container.hasClass('expanded')) {
            container.removeClass('expanded');
            // remove any messages when plugin is collapsed
            removeMessage(container);
        } else {
            details = container.find('div.upm-details');
            if (!details.hasClass('loaded') && !details.hasClass('loading')) {
                container.addClass('loading');
                buildPluginDetails(hash, details, function() {
                    container
                        .removeClass('loading')
                        .addClass('expanded')
                        .trigger('pluginLoaded');
                });
            } else {
                container.addClass('expanded').trigger('pluginLoaded');
            }
        }
    };

    /**
     * Collapses all plugin details within target section
     * @param {Event} e The event object
     */
    upm.collapseAllPluginDetails = function(e) {
        toggleAllPluginDetails(e, false);
    };

    /**
     * Expands all plugin details within target section
     * @param {Event} e The event object
     */
    upm.expandAllPluginDetails = function(e) {
        toggleAllPluginDetails(e, true);
    };

    /**
     * Toggle the expanded state of all plugin details in the target section based on the specified expand flag.
     * @param {Event} e The event object
     * @param {Boolean} expand Set to true if all plugin details will be expanded, otherwise set to false if the
     * plugin details will be collapsed
     */
    function toggleAllPluginDetails(e, expand) {
        var target = AJS.$(e.target).blur(),
                container = target.closest('div.upm-plugin-list-container');

        if (expand) {
            AJS.$('div.upm-plugin:not(.expanded):visible div.upm-plugin-row', container).trigger('click');
        } else {
            AJS.$('div.upm-plugin.expanded:visible div.upm-plugin-row', container).trigger('click');
        }
        e.preventDefault();
    }

    /**
     * Fired when a tab element is clicked on. Swaps out the appropriate content and highlights the appropriate tab
     * @method swapTab
     * @param {Event} e The event object
     */
    upm.swapTab = function(e) {
        var el = AJS.$(e.target),
            id = el.attr('id');
        e.preventDefault();
        id = id.substring('upm-tab-'.length, id.length);
        upm.loadTab(id);
        el.blur();
    };

    /**
     * Clears the value of an input element on focus, and restores the default text on blur if the value is an empty string
     * @method clearOnBlur
     * @param {String|HTMLElement} element The input element to set focus and blur events on
     * @param {String} text The default text that appears in the input element
     */
    upm.clearOnBlur = function(element, text) {
        element = AJS.$(element).val(text);
        element.focus(function() {
            if (element.val() == text) {
                element.val('');
                element.addClass('upm-textbox-active');
            }
        }).blur(function() {
            if (element.val() == '') {
                element.val(text);
                element.removeClass('upm-textbox-active');
            }
        });
    };

    /**
     * Let a user type in a filter box, but just add a loading spinner, because results might not be loaded yet
     * @method setInputFilterPending
     * @param {HTMLElement} input The filter input
     */
    function setInputFilterPending(input) {
        input && input.unbind('keyup input propertychange')
            .bind('keyup input propertychange', filterInputBeforeLoad)
            .removeClass('disabled')
            .val('')
            .trigger('blur');
    }

    /**
     * Input (keypress / paste) handler for inputs before plugins are loaded
     * @method filterInputBeforeLoad
     * @param {Event} e The input event
     */
    function filterInputBeforeLoad(e) {
        var input = AJS.$(e.target);
        // If the text box is empty or inactive, hide the spinner
        input.closest('div.filter-box-wrap')
            .find('div.loading')
            .toggleClass('hidden', !input.hasClass('upm-textbox-active') || input.val() === '');
    }

    /**
     * Disable a filter box and remove key bindings
     * @method setInputFilterPending
     * @param {HTMLElement} input The filter input
     * @param {String} text The disabled text
     */
    function setInputFitlerDisabled(input, text) {
        input.attr('disabled', 'disabled').val(text)
            .unbind('keyup input propertychange', filterPluginsByName)
            .unbind('keyup input propertychange', filterInputBeforeLoad);
    }

    /**
     * Wire up a filter box to actually filter plugins when we know results are safely loaded
     * @method setInputFilterPending
     * @param {HTMLElement} input The filter input
     * @param {String} defaultText The default text for the input
     */
    function setInputFilterEnabled(input, defaultText) {
        if (input) {
            delete filterPluginsByName.val;

            // oninput catches mouse-based pasting in non-IE browsers, onpropertychange in IE
            input.removeClass('loading')
                .unbind('keyup input propertychange', filterInputBeforeLoad)
                .bind('keyup input propertychange', filterPluginsByName);

            // If there was a search during load time, trigger it now that we can actually filter
            if (input.val() != defaultText) {
                input.trigger('keyup');
            }
        }
    }

    /**
     * Displays a dialog that allows the user to upload a plugin
     * @method showUploadDialog
     * @param {Event} e The event object
     */
    upm.showUploadDialog = function(e) {
        // UPM-646 - in the uploadPlugin method we attach a method to handle the popup to the load of the iframe,
        // we need to unbind this everytime we pop the upload dialog up otherwise we will have multiple load
        // handlers and only the last one will be correct.
        AJS.$('#upm-upload-target').unbind('load').unbind('load.upload');
        AJS.$('#upm-upload-url, #upm-upload-file').val('');
        upm.showUploadDialog.dialog = upm.showUploadDialog.dialog || createUploadDialog();
        upm.showUploadDialog.dialog.show();

        // Clear the form fields, this method works cross browser to empty input type=file
        document.getElementById('upm-upload-form').reset();

        // Disable the upload button until the user types in something
        AJS.$('#upm-upload-dialog').find('button.button-panel-button:first').attr('disabled', 'disabled');
        
        focusDialog(upm.showUploadDialog.dialog);
        e.preventDefault();
    };

    /**
     * Creates a dialog for uploading plugins
     * @method createUploadDialog
     */
    function createUploadDialog() {
        var popup = new AJS.Dialog(400, 275, 'upm-upload-dialog'),
            canSubmit = false,
            changeFunction = function(e) {
                if (this.value) {
                    canSubmit = true;
                    uploadBtn.removeAttr('disabled');
                } else {
                    canSubmit = false;
                    uploadBtn.attr('disabled', 'disabled');
                }
            },
            uploadBtn;
        
        popup.addHeader(AJS.params.upmTextUploadPlugin);
        popup.addPanel('All', AJS.$('#upm-upload-form-template').html());
        popup.addButton(AJS.params.upmTextUpload, function (dialog) {
            AJS.$('#upm-upload-form').submit();
        });

        // Keep upload button disabled unless an input has a value
        AJS.$('#upm-upload-file,#upm-upload-url').change(changeFunction).keyup(changeFunction);
        uploadBtn = AJS.$('#upm-upload-dialog').find('button.button-panel-button:first');

        popup.addButton(AJS.params.upmTextCancel, function (dialog) {
            dialog.hide();
        });
        AJS.$('#upm-upload-form').submit(function(e) {
            if (!canSubmit) {
                e.preventDefault();
            } else {
                var uri = AJS.$('#upm-upload-url').val(),
                    file = AJS.$('#upm-upload-file').val();
                AJS.$('#upm-upload-form').attr('action', upm.resources['install'] + '?token=' + AJS.$('#upm-install-token').val());
                if (uri) {
                    e.preventDefault();
                    installPluginFromUri(uri);
                } else if (file) {
                    uploadPlugin();
                } else {
                    displayErrorMessage(upm.$messageContainer, {subCode : 'upm.install.upload.empty.error'}, '');
                }
                popup.hide();
            }
        });
        return popup;
    }

    /**
     * Called on click of the 'Download' button on non-deployable plugins. This shows the download dialog message.
     * @method showDownloadDialog
     * @param {Event} e The event object
     */
    upm.showDownloadDialog = function(e) {
        var target = AJS.$(e.target),
                plugin = target.hasClass('upm-plugin') ? target : target.closest('div.upm-plugin'),
                name = AJS.$('span.upm-plugin-name', plugin).text(),
                header = AJS.format(AJS.params.upmTextNonDeployableHeader, name),
                version = AJS.$('.upm-plugin-version', plugin).text(),
                binaryUrl = plugin.find('input.upm-plugin-binary').val(),
                template;
        upm.showDownloadDialog.template = upm.showDownloadDialog.template || AJS.$(AJS.$('#upm-download-nondeployable-template').html());
        template = upm.showDownloadDialog.template.clone();

        AJS.$('span.upm-nondeployable-instruction', template).text(AJS.format(AJS.params.upmTextNonDeployableInstruction, name));
        AJS.$('a.upm-nondeployable-homepage-link', template).attr('href', plugin.find('input.upm-plugin-homepage').val())
            .text(AJS.format(AJS.params.upmTextNonDeployableHomepage, name));

        if (binaryUrl) {
            AJS.$('a.upm-nondeployable-download-link', template).attr('href', binaryUrl)
                .html(upm.html_sanitize(AJS.format(AJS.params.upmTextNonDeployableDownload, name, version)));
        } else {
            AJS.$('a.upm-nondeployable-download-link', template).parent().addClass('hidden');
        }

        showInfoDialog(header, template.html());
        e.preventDefault();
    };

    /**
     * Called when trying to uninstall in JIRA, shows a message explaining it won't work
     * @method showJiraUninstallDialog
     * @param {Event} e The event object
     */
    function showJiraUninstallDialog(e) {
        showInfoDialog(AJS.params.upmTextJiraUninstallHeader, AJS.$('#upm-jira-uninstall-template').html());
        e.preventDefault();
    }

    /**
     * Shows an informational dialog with the specified content
     * @method showInfoDialog
     * @param {String} header Text to put in the dialog header
     * @param {String} header Text/html to put in the dialog body
     * @param {Boolean} dontSanitize true if the text is known to be safe and doesn't need to be sanitized, false (default) otherwise
     */
    function showInfoDialog(header, content, dontSanitize) {
        showInfoDialog.dialog = (showInfoDialog.dialog && changeDialogContent(showInfoDialog.dialog, header, content, dontSanitize))
                                     || createInfoDialog(header, content);
        showInfoDialog.dialog.show();
        focusDialog(showInfoDialog.dialog);

    }
    /**
     * Changes the header and body of the current panel of a specified dialog
     * @method changeDialogContent
     * @param {Object} dialog The dialog object to change
     * @param {String} header Text to put in the dialog header
     * @param {String} header Text/html to put in the dialog body
     * @param {Boolean} dontSanitize true if the text is known to be safe and doesn't need to be sanitized, false (default) otherwise
     */
    function changeDialogContent(dialog, header, content, dontSanitize) {
        dialog.addHeader(header);
        dialog.getCurrentPanel().html(dontSanitize ? content : upm.html_sanitize(content, htmlSanitizerUrlPolicy));
        return dialog;
    }

    /**
     * Creates a dialog for downloading non-deployable plugins
     * @method createInfoDialog
     * @param {String} header Text to put in the dialog header
     * @param {String} header Text/html to put in the dialog body
     */
    function createInfoDialog(header, content) {
        var popup = new AJS.Dialog(600, 215, 'upm-info-dialog');
        popup.addHeader(header);
        popup.addPanel("All", content);
        popup.addButton(AJS.params.upmTextClose, function (dialog) {
            dialog.hide();
        });
        return popup;
    }

    /**
     * For use when making UI changes before and after a synchronous requests.  Avoids "flickering" UI elements by:
     *  -- only running the the "startFn" if the request takes longer than a specified threshold
     *  -- if startFn is run, stopFn will not be run until a specified delay has passed
     * @method execCallbacksWithThreshold
     * @param {Function} startFn Function to run at the beginning of the request
     * @param {Function} stopFn Function to run once the request has been completed
     * @param {Number} latencyThreshold Amount of time to wait before calling startFn (in ms)
     * @param {Number} minShowTime Minimum amount of time between calling startFn and stopFn (in ms)
     * @return {Function} The function to run when request has completed (stopFn is ready to be executed)
     */
    function execCallbacksWithThreshold(startFn, stopFn, latencyThreshold, minShowTime) {
        latencyThreshold = latencyThreshold || 50;
        minShowTime = minShowTime || 1000;
        var stop,
                // Run a callback after specified delay
                delay;

        if (skipLatentThreshold) {
            // in Confluence/IE just run the callback immediately to lessen UI lag
            delay = function (callback) {
                callback && callback();
            };
        } else {
            delay = function (callback, l) {
                delay.t = setTimeout(function(){
                    clearTimeout(delay.t);
                    delay.t = undefined;
                    callback && callback();
                }, l);
            };
        }

        delay(function() {
            // if stop is already defined, returned fn has been called, so don't even execute startFn
            if (!stop) {
                startFn();
                delay(function() {
                    // if stop is defined here, returned fn was called during the delay, so call stop()
                    if (stop) {
                        stop();
                    }
                }, minShowTime);
            }
        }, latencyThreshold);

        return function() {
            // don't define stop until the returned function is called
            stop = stopFn;

            // only run stop() if no timeout is defined
            if (!delay.t) {
                stop();
            }
        };

    }

    /**
     * Shows a lightbox indicating that an action (installation, update, etc) is being performed
     * @method startProgress
     * @param {String} text The message to be displayed
     */
    upm.startProgress = function(text) {
        if (!progressPopup) {
            progressPopup = new AJS.popup({
                width: 400,
                height: 175,
                id: "upm-progress-popup",
                keypressListener: function() {} // don't let users hide progress popup by hitting the escape key
            });
            progressPopup.element.append(AJS.$('#upm-progress-template').html());
        }
        // store stopFn for later use (eg in stopProgress())
        progressPopup.stopFn = execCallbacksWithThreshold(
                function() {
                    updateProgressText(text);
                },
                function() {
                    progressPopup.hide();
                    // make sure the progress bar is hidden for next time
                    progressPopup.element.removeClass('upm-progress-download');
                },
                isLatentThreshold,
                minProgressDisplay);
        return progressPopup;
    };

    /**
     * Hides the 'In Progress' lightbox
     * @method stopProgress
     */
    function stopProgress() {
        progressPopup.stopFn();
    }

    /**
     * Updates the text of the "In Progress" lightbox and changes its size to fit
     * @method updateProgressText
     * @param {String} text Text to be inserted into progress element
     */
    function updateProgressText(text) {
        var buffer = 25,
                height = 0;
        AJS.$('div.upm-progress-text', progressPopup.element).html(upm.html_sanitize(text));
        height = AJS.$('#upm-progress').height() + buffer;
        if (minProgressHeight > height) {
            height = minProgressHeight;
        }
        progressPopup.changeSize(null, height);
        // some versions of dialog.changeSize() in AUI call dialog.show(), others don't
        if (!progressPopup.element.is(':visible')) {
            progressPopup.show();
        }
    }

    /**
     * Given a pending task response's content type, this function parses out the relevent task details and returns it in an object
     * @method parsePendingTaskContentType
     * @param {String} contentType Text to be inserted into progress element
     * @return {Object} Object containing 'type' and 'status' attributes
     */
    function parsePendingTaskContentType(contentType) {
        // content type for pending tasks will be of the form 'application/vnd.atl.plugins.{task}.{status}+json'
        var regex = /application\/vnd\.atl\.plugins\.(updateall|install|cancellable)\.(.*)\+json/,
            tmp,
            detail;
        if (contentType && regex.test(contentType)) {
            tmp = contentType.match(regex);
            detail = {type: tmp[1], status: tmp[2]};
        }
        return detail;
    }

    /**
     * Polls a resource to determine if an asynchronous request has finished
     * @method pollAsynchronousResource
     * @param {String} location URI of the asynchronous resource
     * @param {Number} delay Time, in ms, to wait before next poll
     * @param {HTMLElement} container Element to show error/success messages in
     * @param {Function} callbackFn Function to be executed if asynchronous request completes successfully
     */
    function pollAsynchronousResource(location, delay, container, callbackFn) {
        delay = delay || 100;
        try {
            AJS.$.ajax({
                type: 'GET',
                cache: false,
                url: location,
                contentType: upm.contentTypes['json'],
                // can't access the request object from the success fn (before jquery 1.4), so we have to use the
                // 'complete' callback
                complete: function(request) {
                    var statusCode = request.status,
                            contentType = request.getResponseHeader('Content-Type'),
                            taskDetail,
                            response,
                            status,
                            progressPercent = 0,
                            progressContainer = AJS.$('#upm-progress'),
                            msgParam;
                    if (statusCode == '200') {
                        response = upm.json.parse(request.responseText);
                        status = response.status;
                        if (status) {
                            if (status.numberComplete) {
                                pollAsynchronousResource.currentProgress = status.numberComplete + 1;
                            } else if (status.numberComplete == '0') {
                                pollAsynchronousResource.currentProgress = 1;
                            }
                            if (status.totalUpdates) {
                                pollAsynchronousResource.totalUpdates = status.totalUpdates;
                            }
                        }
                        taskDetail = parsePendingTaskContentType(contentType);

                        if (taskDetail && taskDetail.status == 'err') {
                            stopProgress();
                            displayErrorMessage(container, response.status, location);
                        } else if (taskDetail && !status.done) {
                            if (response.pingAfter) {
                                // if still working, content type is application/vnd.atl.plugins.pending-task+json
                                // and a pingAfter property was returned in the response
                                setTimeout(function() {
                                    pollAsynchronousResource(location, response.pingAfter, container, callbackFn);
                                }, delay);
                                if (taskDetail.status == 'downloading') {
                                    if (status.totalSize) {
                                        progressPercent = Math.floor((status.amountDownloaded / status.totalSize) * 100);
                                    }
                                    progressPopup.element.addClass('upm-progress-download');
                                    AJS.$('div.upm-progress-amount', progressContainer).width(progressPercent + '%');
                                    AJS.$('span.upm-progress-bar-percent', progressContainer).text(progressPercent);
                                } else {
                                    progressPopup.element.removeClass('upm-progress-download');
                                }

                                if (taskDetail.type == 'updateall' && (taskDetail.status == 'updateall' || taskDetail.status == 'downloading')) {
                                    pollAsynchronousResource.template = AJS.$(AJS.$('#upm-updateall-' + taskDetail.status + '-progress-template').html());
                                    var updateAllTemplate = pollAsynchronousResource.template.clone();
                                    AJS.$("#upm-updateall-current-name", updateAllTemplate).text(status.name || status.filename || status.source);
                                    AJS.$("#upm-updateall-current-version", updateAllTemplate).text(status.version);
                                    AJS.$("#upm-updateall-completed", updateAllTemplate).text(pollAsynchronousResource.currentProgress);
                                    AJS.$("#upm-updateall-total", updateAllTemplate).text(pollAsynchronousResource.totalUpdates);
                                    updateProgressText(updateAllTemplate.html());
                                } else {
                                    msgParam = 'upm.progress.' + taskDetail.type + '.' + taskDetail.status;

                                    // Message params specific to the final "updating all `n` plugins" status
                                    if (taskDetail.type == 'updateall' && taskDetail.status == 'updating') {
                                        updateProgressText((AJS.format(
                                            AJS.params[msgParam],
                                            status.totalUpdates
                                        )));
                                    } else {
                                        updateProgressText((AJS.format(
                                            AJS.params[msgParam],
                                            status.name || status.filename || status.source,
                                            status.version
                                        )));
                                    }
                                }

                            } else {
                                // if there was an error during installation, response won't have a pingAfter property
                                stopProgress();
                                upm.handleAjaxError(container, request, response.status.plugin);
                            }
                        } else {
                            if (response.status && (response.status.subCode || response.status.errorMessage)) {
                                stopProgress();
                                upm.handleAjaxError(container, request, response.status.plugin);
                            } else {
                                // if simpler async tasks are completed, 303 will redirect to plugin details resource, but content type will be different
                                // if complex async tasks are completed, 'status' property will be set to 'SUCCEEDED'
                                callbackFn && callbackFn(response);
                            }
                        }
                    } else if (statusCode == '202') {
                        // separate from above `if` to prevent false negative
                        if (callbackFn) {
                            response = upm.json.parse(request.responseText);
                            response.statusCode = statusCode;
                            callbackFn(response);
                        }
                    } else if (statusCode == '0') {
                        // we're offline : something is probably wrong with baseUrl settings
                        stopProgress();
                        displayErrorMessage(container, {'subCode' : 'upm.baseurl.connection.error'}, location);
                    } else {
                        // something went horribly/unexpectedly wrong
                        stopProgress();
                        upm.handleAjaxError(container, request, '');
                    }
                }, error: function(response) {
                    displayErrorMessage(upm.$messageContainer, response);
                }
            });
        } catch (e) {
            // UPM-842: IE freaks out if you try to do a cross-domain request, which might happen if the base url is set
            // incorrectly, so catch the error and display a relevant error message
            AJS.log('Error doing ajax request: ' + e);
            stopProgress();
            displayErrorMessage(container, {'subCode' : 'upm.baseurl.connection.error'}, location);
        }
    }

    /**
     * Updates (or creates) the pending task detail text
     * @method updatePendingTaskDetail
     * @param {Object} task Task detail object
     */
    function updatePendingTaskDetail(task) {
        var container = AJS.$('#upm-pending-tasks-details'),
            existing = container.find('li'),
            status = task && task.status,
            detail = parsePendingTaskContentType(status && status.contentType),
            text;
        if (detail && status) {
            text = AJS.format(
                AJS.params.upmTextPendingTaskDetail,
                AJS.format(
                    AJS.params['upm.progress.' + detail.type + '.' + detail.status],
                    status.name || status.filename || status.source,
                    status.version
                ),
                task.username,
                upm.prettyDate(task.timestamp)
            );
            if (existing.length) {
                existing.text(text);
            } else {
                container.append(
                    AJS.$('<li></li>').text(text)
                );
            }
        }
    }

    /**
     * Polls the pending tasks collection resource to determine if another user's task is running
     * @method pollPendingTasks
     */
    function pollPendingTasks() {
        clearTimeout(pollPendingTasks.timeout);
        AJS.$.ajax({
            url: upm.resources['pending-tasks'],
            type: 'get',
            cache: false,
            dataType: 'json',
            success: function(response) {
                var task;
                if (response.tasks.length > 0) {
                    task = response.tasks[0];
                    updatePendingTaskDetail(task);
                    pollPendingTasks.timeout = setTimeout(pollPendingTasks, task.pingAfter);
                } else {
                    upm.$container.removeClass('upm-pending-tasks');
                    pollPendingTasks.timeout = undefined;
                }
            },
            error: function(request) {
                upm.handleAjaxError(upm.$messageContainer, request, '');
            }
        });
    }

    /**
     * Initiates the uploading of a provided plugin
     * @method uploadPlugin
     */
    function uploadPlugin() {
        var filename = AJS.$('#upm-upload-file').val(),
            tmp = filename.split('\\');

        // only show the actual file name, not the whole path
        filename = tmp[tmp.length-1];
        upm.startProgress(AJS.format(AJS.params.upmTextProgressUpload, filename));
        
        if (!hasPendingTasks(stopProgress)) {
            AJS.$('#upm-upload-target').unbind('load.upload').bind('load.upload', function() {
                var textarea = AJS.$('#upm-upload-target').contents().find('textarea'),
                    response = upm.json.parse(textarea.val());

                if (response.links && response.links.self) {
                    pollAsynchronousResource(response.links.self, response.pingAfter, upm.$messageContainer, function(uploadResponse) {
                        // If response is 202, it's UPM plugin update time
                        if (uploadResponse.statusCode == '202') {
                            completeUPMUpdate(uploadResponse.status.nextTaskPostUri, upm.$messageContainer, function(upmResponse) {
                                onPluginInstallComplete(upmResponse);
                            });
                        } else {
                            onPluginInstallComplete(uploadResponse);
                        }
                    });
                    // UPM-977 When there is an error or a success we need to make sure we have an updated XSRF token
                    getAndStoreAntiXsrfToken();
                } else {
                    // try to submit plugin again, with new token, exactly once -- one retry per user request
                    if (response.subCode == 'upm.error.invalid.token' && tryUploadOrInstallAgain) {
                        getAndStoreAntiXsrfToken(function() {
                            // UPM-782 Executing a second submit to the upload form without calling stopProgress first
                            // will override the progressPopup.stopFn and will end up not calling the stopFn
                            // in the execCallbacksWithThreshold function, causing the throbber to display indefinitely
                            stopProgress();
                            tryUploadOrInstallAgain = false;
                            AJS.$('#upm-upload-form').submit();
                        });
                    } else {
                        tryUploadOrInstallAgain = true;
                        displayErrorMessage(upm.$messageContainer, textarea.val(), filename);
                        stopProgress();
                    }
                }
            });
        }
    }

    /**
     * Initiates the updating of a specified plugin
     * @method updatePlugin
     * @param {Event} e The event object
     */
    upm.updatePlugin = function(e) {
        var element = AJS.$(e.target),
            pluginElement = element.hasClass('upm-plugin') ? element : element.closest('div.upm-plugin'),
            header = AJS.$('div.upm-plugin-row', pluginElement),
            name = header.find('h4').text(),
            uri = pluginElement.find('input.upm-plugin-binary').val(),
            detailsElement = AJS.$('div.upm-details', pluginElement);

        e.preventDefault && e.preventDefault();

        upm.startProgress(AJS.format(AJS.params.upmTextProgressUpdate, name));

        if (!hasPendingTasks(stopProgress)) {
            if (uri) {
                AJS.$.ajax({
                    type: 'POST',
                    url: upm.resources['root'] + '?token=' + AJS.$('#upm-install-token').val(),
                    dataType: 'text',
                    contentType: upm.contentTypes['install'],
                    data: upm.json.stringify({ "pluginUri": uri }),
                    // can't access the request object from the success fn (before jquery 1.4), so we have to use the
                    // 'complete' callback
                    complete: function(request, status) {
                        var response = upm.json.parse(request.responseText);
                        if (status == 'success') {
                            var location = request.getResponseHeader('Location');
                            // Start listening for the update task to return. If it's a 202, we have some UPM updating to do
                            pollAsynchronousResource(location, response.pingAfter, detailsElement, function(updateResponse) {
                                if (updateResponse.statusCode == '202') {
                                    completeUPMUpdate(updateResponse.status.nextTaskPostUri, detailsElement, function(upmResponse) {
                                        onPluginUpdateComplete(pluginElement, detailsElement, upmResponse);
                                    });
                                // Otherwise a normal plugin was updated
                                } else {
                                    onPluginUpdateComplete(pluginElement, detailsElement, updateResponse);
                                }
                                tryUploadOrInstallAgain = true;
                                getAndStoreAntiXsrfToken(); // UPM-977 even in success, we need to get a new token for next time
                            });
                        } else {
                            // try to submit plugin again, with new token, exactly once -- one retry per user request
                            if (response.subCode == 'upm.error.invalid.token' && tryUploadOrInstallAgain) {
                                getAndStoreAntiXsrfToken(function() {
                                    // UPM-782 Executing a second stopProgress first
                                    // will override the progressPopup.stopFn and will end up not calling the stopFn
                                    // in the execCallbacksWithThreshold function, causing the throbber to display indefinitely
                                    stopProgress();
                                    tryUploadOrInstallAgain = false;
                                    upm.updatePlugin(e);
                                });
                            } else {
                                tryUploadOrInstallAgain = true;
                                stopProgress();
                                upm.handleAjaxError(detailsElement, request, 'error');
                            }
                        }
                    }
                });
            } else {
                stopProgress();
                upm.displayMessage(detailsElement, {
                    body: AJS.params.upmTextUpdateError,
                    type: 'error',
                    className: 'update'
                });
            }
        }
    };

    /**
     * Handle the initial long running task response for a UPM update, and finish the process
     * @method completeUPMUpdate
     * @param {String} url The url of the plugin install
     * @param {HTMLElement} detailsElement The element to show error/success messages in, passed through to pollAsynchronousResource
     * @param {Function} callbackFn The function to call when the update is complete
     */
    function completeUPMUpdate(url, detailsElement, callbackFn) {
        detailsElement = (detailsElement && detailsElement.is(':visible')) ? detailsElement : upm.$messageContainer;
        // POST to stub plugin uri which starts update of UPM, and returns URI for long running task
        AJS.$.ajax({
            type: 'POST',
            url: url,
            dataType: 'json',
            contentType: upm.contentTypes['json'],
            complete: function(request, status) {
                var location = request.getResponseHeader('Location'),
                    response = upm.json.parse(request.responseText);

                // When long running task is done we will need to DELETE to a URI, telling UPM to uninstall the stub
                pollAsynchronousResource(location, response.pingAfter, detailsElement, function(longRunningResponse) {
                    AJS.$.ajax({
                        type: 'DELETE',
                        url: longRunningResponse.status.cleanupDeleteUri,
                        dataType: 'json',
                        contentType: upm.contentTypes['json'],
                        complete: function(deleteResponse) {

                            // UPM-1209, long running task returns 'requires refresh' flag, so hack / combine it with the delete response
                            var responseJson = upm.json.parse(deleteResponse.responseText);
                            responseJson.requiresRefresh = longRunningResponse.status.requiresRefresh;

                            callbackFn(responseJson, detailsElement);
                        }, error: function() {
                            stopProgress();
                            upm.displayMessage(detailsElement, {
                                body: AJS.params.upmTextSelfUpdateError,
                                type: 'error'
                            });
                        }
                    });
                });
            }, error: function() {
                stopProgress();
                upm.displayMessage(detailsElement, {
                    body: AJS.params.upmTextSelfUpdateError,
                    type: 'error'
                });
            }
        });
    }

    /**
     * Handle the completion of a plugin update, after a long running task has completed
     * @method onPluginUpdateComplete
     * @param {HTMLElement} pluginElement
     * @param {HTMLElement} detailsElement
     * @param {Object} response The response message object
     */
    function onPluginUpdateComplete(pluginElement, detailsElement, response) {
        var restartState = response.restartState,
            refreshState = response.requiresRefresh,
            header = AJS.$('div.upm-plugin-row', pluginElement);

        stopProgress();
        if (restartState) {
            addChangeRequiringRestart({'action': restartState, 'name': response.name, 'key': response.key, 'links': {'self': response.links['change-requiring-restart']}});
            displayRestartMessage(detailsElement, restartState);
        } else if (refreshState) {
            displayRefreshMessage();
            upm.displayMessage(detailsElement, {
                body: AJS.params.upmTextUpdateSuccess,
                type: 'success',
                className: 'update'
            });
        } else {
            upm.displayMessage(detailsElement, {
                body: AJS.params.upmTextUpdateSuccess,
                type: 'success',
                className: 'update'
            });
        }
        AJS.$('a.upm-update', pluginElement).closest('li.toolbar-item').addClass('disabled');
        pluginElement.addClass('to-remove');
        header.click(function(e) {
            resortOnCollapse(e, pluginElement);
        });

        // If we are updating the UPM (last response will be the stub), don't subtract the count from the tab
        if (!isUpmStub(response.key)) {
            setUpdateCount(getUpdateCount() - 1);
        }

        refreshNotifications();
    }

    /*
     * Return the count of updates available
     * @method getUpdateCount
     * @return {Number} The update count
      */
    function getUpdateCount() {
        return parseInt(AJS.$('#upm-update-count').val(), 10);
     }
    
    /**
     * Sets the number of updates available
     * @method setUpdateCount
     * @param {Number} value The update count
     */
    function setUpdateCount(value) {
        return AJS.$('#upm-update-count').val(value);
    }

    /**
     * Given a click on a plugin row header, remove it and then insert it into the correct plugin
     *  list after it is collapsed + fades away
     * @method resortOnCollapse
     * @param {Event} e The event object
     * @param {HTMLElement} pluginElement The full plugin element to move
     */
    function resortOnCollapse(e, pluginElement) {
        // First fade it out...
        removeOnCollapse(e, function() {
            // Then insert it into..
            insertPluginHeaderInto(
                AJS.$(pluginElement.hasClass('user-installed') ?
                    // ...the user list, or the system list, depending on plugin type
                    '#upm-user-plugins' : '#upm-system-plugins'),
                pluginElement
            );
        });
    }

    /**
     * Insert a plugin row element alphabetically into a list, sorted by plugin title
     * @method insertPluginHeaderInto
     * @param {HTMLElement} container The list to stick it to
     * @param {HTMLElement} element The plugin element to insert
     */
    function insertPluginHeaderInto(container, element) {
        // Maybe the plugin list doesn't exist at all? Permissions?
        if (!container) {
            return;
        }

        var list = container.find('.upm-plugin-list'),
            pluginTitle = element.find('h4.upm-plugin-name').text();

        // If our list doesn't exist (maybe all plugins have updates, and the installed list is empty?)
        if (!list.length) {
            // Build the new list, using the correct options + forceCreation, so that even though
            // we pass in an empty array, it will still create a list, not the "no plugins" message
            upm.buildPluginList([], AJS.$.extend({
                        forceCreation: true
                    }, element.hasClass('user-installed') ? userPluginsOptions : systemPluginsOptions))
                // Append the list to the container...
                .appendTo(container.empty())
                // ...and insert our plugin row into the upm-plugin-list child
                .find('.upm-plugin-list')
                .append(element);
        } else {
            var children = list.children(),
                x = 0,
                row;
            // Otherwise our list exists, search through each plugin...
            for(; row = children[x++]; ) {
                // If our plugin title comes before the row's title alphabetically, insert it
                if (pluginTitle.toLowerCase() < AJS.$(row).find('h4.upm-plugin-name').text().toLowerCase()) {
                    element.insertBefore(row);
                    break;
                }
            }
            // If we didn't insert anything (for loop exited without breaking), insert at end of list
            if (!row) {
                list.append(element);
            }
        }
        element.removeClass('to-remove').find('div.upm-details').empty().removeClass('loaded');
        element.show();
    }

    /**
     * Initiates the updating of all plugins with available updates
     * @method updateAllPlugins
     * @param {Event} e The event object
     */
    upm.updateAllPlugins = function(e) {
        var button = AJS.$('#upm-update-all'),
        errorCallback = function() {
            button.closest('li.toolbar-item').removeClass('disabled');
            stopProgress();
        };
        button.closest('li.toolbar-item').addClass('disabled');
        e.preventDefault();

        upm.startProgress(AJS.format(AJS.params.upmTextProgressUpdateAll, getUpdateCount()));
        if (!hasPendingTasks(errorCallback)) {
            AJS.$.ajax({
                type: 'POST',
                url: upm.resources['update-all'] + '?token=' + AJS.$('#upm-install-token').val(),
                dataType: 'json',
                contentType: upm.contentTypes['update-all'],
                // can't access the request object from the success fn (before jquery 1.4), so we have to use the
                // 'complete' callback
                complete: function(request, status) {
                    if (status == 'success') {
                        var location = request.getResponseHeader('Location');
                        pollAsynchronousResource(location, upm.json.parse(request.responseText).pingAfter, upm.$messageContainer, function(response) {
                            var successes = response.status.successes,
                                numSuccess = successes.length,
                                failures = response.status.failures,
                                numFail = failures.length,
                                total = numSuccess + numFail,
                                expanded = 0,
                                messageType = numFail === 0 ? 'success' : numSuccess === 0 ? 'error' : 'info',
                                container = AJS.$('#upm-available-updates');
                            stopProgress();

                            if (numFail > 0) {
                                var displayPluginMessages = function(item, callbackFn) {
                                    var key = container.find('input.upm-plugin-key[value="' + item.key + '"]'),
                                        plugin = key.closest('div.upm-plugin'),
                                        row = plugin.find('div.upm-plugin-row'),
                                        details = plugin.find('div.upm-details');

                                    if (!plugin.hasClass('expanded')) {
                                        plugin.bind('pluginLoaded.message', function(e) {
                                            var el = AJS.$(e.target);
                                            callbackFn && callbackFn({
                                                pluginElement: plugin,
                                                detailsElement: el,
                                                rowElement: row,
                                                item: item
                                            });
                                            el.unbind('pluginLoaded.message');

                                            expanded++;
                                            if (expanded == total) {
                                                upm.displayMessage(upm.$messageContainer, {
                                                    body: AJS.format(AJS.params.upmTextUpdateAllComplete, numSuccess, total),
                                                    type: messageType,
                                                    className: 'update'
                                                });
                                            }
                                        });
                                        row.trigger('click');

                                    } else {
                                        callbackFn && callbackFn({pluginElement: plugin, detailsElement: details, rowElement: row, item: item});
                                    }
                                };

                                setUpdateCount(numFail);

                                for (var i = 0; i < numSuccess; i++) {
                                    displayPluginMessages(successes[i], function(obj) {
                                        upm.displayMessage(obj.detailsElement, {
                                            body: AJS.params.upmTextUpdateSuccess,
                                            type: 'success',
                                            className: 'install'
                                        });
                                        AJS.$('a.upm-update', obj.detailsElement).closest('li.toolbar-item').addClass('disabled');
                                        obj.pluginElement.addClass('to-remove');
                                        obj.rowElement.click(function(e) {
                                            resortOnCollapse(e, obj.pluginElement);
                                        });
                                    });
                                }
                                for (var i = 0; i < numFail; i++) {
                                    var failure = failures[i];
                                    displayPluginMessages(failure, function(obj) {
                                        var item = obj.item;
                                        if (item.subCode) {
                                            item.subCode = 'upm.pluginInstall.error.' + item.subCode;
                                        }
                                        displayErrorMessage(obj.detailsElement.removeClass('error'), item, item.source);
                                    });
                                }
                                tryUploadOrInstallAgain = true;
                                getAndStoreAntiXsrfToken();
                            } else {
                                // if there were no failures, load the manage tab
                                upm.loadTab('manage');
                            }
                            // UPM-884 - need to get UPM to check for requires restart for the updated plugins
                            if (response.status.links['changes-requiring-restart']) {
                                upm.resources['changes-requiring-restart'] = response.status.links['changes-requiring-restart'];
                                checkForChangesRequiringRestart();
                            }
                            refreshNotifications();
                        });
                    } else {
                    	// try to perform the update again, with new token, exactly once -- one retry per user request
                        if ((request.responseText.indexOf('upm.error.invalid.token') != -1) && tryUploadOrInstallAgain) {
                            getAndStoreAntiXsrfToken(function() {
                            	errorCallback();
                                tryUploadOrInstallAgain = false;
                                AJS.$('#upm-update-all').click();
                            });
                        } else {
                            tryUploadOrInstallAgain = true;
                            errorCallback();
                            upm.handleAjaxError(upm.$messageContainer, request, 'error');
                        }
                    }
                }
            });
        }
    };

    /**
     * Initiates the installation of a specified plugin
     * @method installPlugin
     * @param {Event} e The event object
     */
    upm.installPlugin = function(e) {
        var element = AJS.$(e.target),
            plugin = element.hasClass('upm-plugin') ? element : element.closest('div.upm-plugin'),
            header = AJS.$('div.upm-plugin-row', plugin),
            name = header.find('h4').text(),
            uri = plugin.find('input.upm-plugin-binary').val(),
            details = AJS.$('div.upm-details', plugin);

        e.preventDefault();

        if (uri) {
            upm.startProgress(AJS.format(AJS.params.upmTextProgressInstall, name));
            if (!hasPendingTasks(stopProgress)) {
                // First send a POST to initiate the plugin install
                AJS.$.ajax({
                    type: 'POST',
                    url: upm.resources['install'] + '?token=' + AJS.$('#upm-install-token').val(),
                    dataType: 'text',
                    contentType: upm.contentTypes['install'],
                    data: upm.json.stringify({ "pluginUri": uri }),
                    // can't access the request object from the success fn (before jquery 1.4), so we have to use the
                    // 'complete' callback
                    complete: function(request, status) {
                        var postResponse = upm.json.parse(request.responseText);

                        // The POST returns a URI that we then poll, and when the plugin install is complete, it will return
                        // details about the installed plugin
                        if (status == 'success') {
                            var location = request.getResponseHeader('Location');
                            pollAsynchronousResource(location, postResponse.pingAfter, details, function(pollResponse) {
                                var restartState = pollResponse.restartState,
                                    usesLicensing = pollResponse.links && pollResponse.links['update-license'],
                                    type = 'success',
                                    body;

                                stopProgress();
                                if (restartState) {
                                    addChangeRequiringRestart({'action': restartState, 'name': pollResponse.name, 'key': pollResponse.key, 'links': {'self': pollResponse.links['change-requiring-restart']}});
                                    displayRestartMessage(details, restartState);

                                // Let's display a message!
                                } else {
                                    if (pollResponse.enabledByDefault && !pollResponse.enabled) {
                                        body = AJS.params.upmTextInstallCannotBeEnabled;
                                        type = 'error';
                                    } else if (pollResponse.unrecognisedModuleTypes) {
                                        body = AJS.params.upmTextInstallUnrecognisedModuleTypes;
                                        type = 'warning';
                                    } else if (!pollResponse.enabledByDefault) {
                                        body = AJS.params.upmTextInstallSuccessNotEnabled;
                                        type = 'warning';
                                    } else if (usesLicensing) {
                                        if (!pollResponse.licenseDetails) {
                                            // this plugin requires licensing and was installed without a license (the most common scenario)

                                            // upm.displayMessage sanitizes out the href, so bypass it
                                            AJS.messages.warning(details.find('div.upm-message-container'), {
                                                // Link to the plugin
                                                body: AJS.format(AJS.params.upmTextInstallLicensingRequired, pollResponse.links['plugin-details']),
                                                shadowed: false,
                                                closable: true
                                            });
                                        } else if (pollResponse.licenseDetails.evaluation) {
                                            // this plugin requires licensing and already has an eval license set up (e.g. embedded in host license)
                                            body = AJS.params.upmTextInstallLicensingAlreadyEvaluating;
                                        } else {
                                            // this plugin requires licensing and already has a non-eval license set up (e.g. embedded in host license)
                                            body = AJS.params.upmTextInstallLicensingAlreadyPurchased;
                                        }
                                    } else {
                                        body = AJS.params.upmTextInstallSuccess;
                                    }

                                    body && upm.displayMessage(details, {
                                        body: body,
                                        type: type,
                                        className: 'install'
                                    });
                                }

                                AJS.$('a.upm-install', plugin).closest('li.toolbar-item').addClass('disabled');
                                AJS.$('a.upm-buy', plugin).closest('li.toolbar-item').addClass('disabled');
                                AJS.$('a.upm-try', plugin).closest('li.toolbar-item').addClass('disabled');
                                plugin.addClass('to-remove');
                                header.click(removeOnCollapse);
                                loadManageTab();

                                tryUploadOrInstallAgain = true;
                                getAndStoreAntiXsrfToken(); // UPM-977 even in success, we need to get a new token for next time
                                refreshNotifications();

                                //display the "redirect to MAC" dialog only if the plugin is capable of executing the specified license action
                                if (usesLicensing) {
                                    var parent = AJS.$(e.target).parent();
                                    if (parent.hasClass('upm-buy-item') && pollResponse.links['new']) {
                                        progressPopup.hide(); // popup hiding is delayed. let's force it to hide to prevent the dialogs from overlapping
                                        upm.showConfirmDialog(AJS.format(AJS.params.upmTextInstallLicensingFollowupBuy, pollResponse.name), marketplaceActionAfterInstall, ['new', pollResponse]);
                                    } else if (parent.hasClass('upm-try-item') && pollResponse.links['try']) {
                                        progressPopup.hide(); // popup hiding is delayed. let's force it to hide to prevent the dialogs from overlapping
                                        upm.showConfirmDialog(AJS.format(AJS.params.upmTextInstallLicensingFollowupTry, pollResponse.name), marketplaceActionAfterInstall, ['try', pollResponse]);
                                    }
                                }
                            });
                        } else {
                            // try to submit plugin again, with new token, exactly once -- one retry per user request
                            if (postResponse.subCode == 'upm.error.invalid.token' && tryUploadOrInstallAgain) {
                                getAndStoreAntiXsrfToken(function() {
                                    // UPM-782 Executing a second stopProgress first
                                    // will override the progressPopup.stopFn and will end up not calling the stopFn
                                    // in the execCallbacksWithThreshold function, causing the throbber to display indefinitely
                                    stopProgress();
                                    tryUploadOrInstallAgain = false;
                                    upm.installPlugin(e);
                                });
                            } else {
                                tryUploadOrInstallAgain = true;
                                stopProgress();
                                upm.handleAjaxError(details, request, 'error');
                            }
                        }
                    }
                });
            }
        }
    };

    /**
     * Submits the marketplace form to purchase either a Buy or Try license action.
     * @param type 'new' for Buy actions and 'try' for Try actions
     * @param plugin the plugin
     */
    function marketplaceActionAfterInstall(type, plugin) {
        buildMarketplaceButton(type, plugin)
            .addClass('hidden')
            .appendTo(document.body)
            .find('form').submit();
    }

    /**
     * Initiates the installation of a specified plugin
     * @method installPluginFromUri
     * @param {String} uri The uri of the plugin to install
     */
    function installPluginFromUri(uri) {
        if (uri) {
            upm.startProgress(AJS.format(AJS.params.upmTextProgressInstall, uri));
            if (!hasPendingTasks(stopProgress)) {
                AJS.$.ajax({
                    type: 'POST',
                    url: upm.resources['install'] + '?token=' + AJS.$('#upm-install-token').val(),
                    dataType: 'text',
                    contentType: upm.contentTypes['install'],
                    data: upm.json.stringify({ "pluginUri": uri }),
                    // can't access the request object from the success fn (before jquery 1.4), so we have to use the
                    // 'complete' callback
                    complete: function(request, status) {
                        var response = upm.json.parse(request.responseText);
                        if (status == 'success') {
                            var location = request.getResponseHeader('Location');
                            pollAsynchronousResource(location, response.pingAfter, upm.$messageContainer, function(installResponse) {
                                if (installResponse.statusCode == '202') {
                                    completeUPMUpdate(installResponse.status.nextTaskPostUri, upm.$messageContainer, function(upmResponse) {
                                        onPluginInstallComplete(upmResponse);
                                    });
                                } else {
                                    onPluginInstallComplete(installResponse);
                                }
                            });
                        } else {
                            // try to submit plugin again, with new token, exactly once -- one retry per user request
                            if (response.subCode == 'upm.error.invalid.token' && tryUploadOrInstallAgain) {
                                getAndStoreAntiXsrfToken(function() {
                                    // UPM-782 Executing a second stopProgress first
                                    // will override the progressPopup.stopFn and will end up not calling the stopFn
                                    // in the execCallbacksWithThreshold function, causing the throbber to display indefinitely
                                    stopProgress();
                                    tryUploadOrInstallAgain = false;
                                    installPluginFromUri(uri);
                                });
                            } else {
                                tryUploadOrInstallAgain = true;
                                stopProgress();
                                upm.handleAjaxError(upm.$messageContainer, request, uri);
                            }
                        }
                    }
                });
            }
        }
    }

    /**
     * Handle the response from a plugin installation completing, and update messages and dialogs
     * @method onPluginInstallComplete
     * @param {Object} response Asynchronous request response object
     */
    function onPluginInstallComplete(response) {
        var restartState = response.restartState,
            refreshState = response.requiresRefresh;

        upm.bindOnce('panelLoaded', function() {
            var prefix = '#upm-plugin-',
                hashManage = prefix + createHash(response.key, 'manage'),
                hashUpdate = prefix + createHash(response.key, 'update'),
                plugin = AJS.$(hashManage),
                details;

            // If this plugin isn't in the manage list, look for it in the update list. As in, you could have
            // installed an older plugin version that has an availalbe update, and will show up in the top list
            if (!plugin.length) {
                plugin = AJS.$(hashUpdate);
            }
            details = plugin.find('div.upm-details');

            if (refreshState) {
                displayRefreshMessage();
                upm.displayMessage(upm.$messageContainer, {
                    body: AJS.params.upmTextInstallSuccess,
                    type: 'success',
                    className: 'install'
                });
            }

            // Listen for the expanded plugin details loading event, and show a message when it occurs
            details.bind('detailsLoaded.installMessage', function() {
                var body, type;
                if (restartState) {
                    addChangeRequiringRestart({'action': restartState, 'name': response.name, 'key': response.key, 'links': {'self': response.links['change-requiring-restart']}});
                } else {
                    if (response.enabledByDefault && !response.enabled) {
                        body = AJS.params.upmTextInstallCannotBeEnabled;
                        type = 'error';
                    } else if (response.unrecognisedModuleTypes) {
                        body = AJS.params.upmTextInstallUnrecognisedModuleTypes;
                        type = 'warning';
                    } else if (!response.enabledByDefault) {
                        body = AJS.params.upmTextInstallSuccessNotEnabled;
                        type = 'warning';
                    } else if (isLicenseUpdatable(response) && isRestartRequiredForLicensing(response)) {
                        body = AJS.params.upmTextInstallNotAware;
                        type = 'info';
                    } else {
                        body = AJS.params.upmTextInstallSuccess;
                        type = 'success';
                    }
                    upm.displayMessage(details, {
                        body: body,
                        type: type,
                        className: 'install'
                    });
                    details.unbind('detailsLoaded.installMessage');
                }
            });

            // Fake a click on the plugin row, expanding it
            AJS.$('div.upm-plugin-row', plugin).trigger('click');
        });

        tryUploadOrInstallAgain = true;
        getAndStoreAntiXsrfToken(); // UPM-977 even in success, we need to get a new token for next time
        stopProgress();
        upm.loadTab('manage');
        refreshNotifications();
    }

    /**
     * Initiates the uninstallation of a specified plugin
     * @method uninstallPlugin
     * @param {Event} e The event object
     */
    upm.uninstallPlugin = function(e) {
        var element = AJS.$(e.target),
                plugin = element.hasClass('upm-plugin') ? element : element.closest('div.upm-plugin'),
                hash = getPluginHash(plugin),
                header = AJS.$('div.upm-plugin-row', plugin),
                name = header.find('h4').text(),
                url = plugin.find('input.upm-plugin-link-delete').val(),
                details = AJS.$('div.upm-details', plugin),
                data;

        e.preventDefault();

        if (upm.isJira && upm.productVersion < 4.2) {
            // don't allow plugins to be uninstalled in JIRA before version 4.2, as it causes the system to lock on restart
            showJiraUninstallDialog(e);
            return;
        }

        upm.startProgress(AJS.format(AJS.params.upmTextProgressUninstall, name));
        if (!hasPendingTasks(stopProgress)) {
            data = pluginReps[hash];
            if (data) {
                data.enabled = false;
                AJS.$.ajax({
                    type: 'DELETE',
                    url: url,
                    dataType: 'json',
                    contentType: upm.contentTypes['json'],
                    data: upm.json.stringify(data),
                    success: function(response) {
                        var restartState = response.restartState,
                            licenseDetails = AJS.$('div.upm-license-details', plugin);
                        pluginReps[hash] = response;
                        stopProgress();
                        if (restartState) {
                            addChangeRequiringRestart({'action': restartState, 'name': response.name, 'key': response.key, 'links': {'self': response.links['change-requiring-restart']}});
                            displayRestartMessage(details, restartState);
                        } else {
                            upm.displayMessage(details, {
                                body: AJS.params.upmTextUninstallSuccess,
                                type: 'info',
                                className: 'uninstall'
                            });
                        }
                        AJS.$('div.upm-plugin-modules', plugin).addClass('hidden');
                        AJS.$('li.toolbar-item', plugin).addClass('disabled');

                        licenseDetails.addClass('disabled');
                        AJS.$('input[type="submit"]', licenseDetails).attr('disabled', 'disabled');
                        AJS.$('textarea', licenseDetails).attr('disabled', 'disabled');
                        plugin.addClass('disabled to-remove');
                        header.click(removeOnCollapse);
                        refreshNotifications();

                        // If this plugin was in the updates list, update the tab count
                        if (plugin.closest('#upm-update-plugin-list').length) {
                            setUpdateCount(getUpdateCount() - 1);
                        }
                    },
                    error: function(request) {
                        stopProgress();
                        upm.handleAjaxError(details, request, '');
                    }
                });
            }
        }
    };

    /**
     * Disables a specified plugin
     * @method disablePlugin
     * @param {Event} e The event object
     */
    upm.disablePlugin = function(e) {
        var element = AJS.$(e.target),
                plugin = element.hasClass('upm-plugin') ? element : element.closest('div.upm-plugin'),
                hash = getPluginHash(plugin),
                name = AJS.$('div.upm-plugin-row h4', plugin).text(),
                url = plugin.find('input.upm-plugin-link-modify').val(),
                details = AJS.$('div.upm-details', plugin),
                data = pluginReps[hash];

        e.preventDefault();

        upm.startProgress(AJS.format(AJS.params.upmTextProgressDisable, name));
        if (!hasPendingTasks(stopProgress)) {
            if (data) {
                data.enabled = false;
                AJS.$.ajax({
                    type: 'PUT',
                    url: url,
                    dataType: 'json',
                    contentType: upm.contentTypes['plugin'],
                    data: upm.json.stringify(data),
                    success: function(response) {
                        pluginReps[hash] = response;
                        stopProgress();
                        plugin.addClass('disabled');
                        plugin.bind('detailsLoaded.disable', function() {
                            upm.displayMessage(details, {
                                body: AJS.params.upmTextDisableSuccess,
                                type: 'info',
                                fadeOut: true
                            });
                            plugin.unbind('detailsLoaded.disable');
                        });
                        refreshPlugin(plugin);
                    },
                    error: function(request) {
                        stopProgress();
                        upm.handleAjaxError(details, request, name);
                    }
                });
            }
        }
    };

    /**
     * Enables a specified plugin
     * @method enablePlugin
     * @param {Event} e The event object
     */
    upm.enablePlugin = function(e) {
        var element = AJS.$(e.target),
                plugin = element.hasClass('upm-plugin') ? element : element.closest('div.upm-plugin'),
                hash = getPluginHash(plugin),
                name = AJS.$('div.upm-plugin-row h4', plugin).text(),
                url = plugin.find('input.upm-plugin-link-modify').val(),
                details = AJS.$('div.upm-details', plugin),
                data = pluginReps[hash];

        e.preventDefault();

        upm.startProgress(AJS.format(AJS.params.upmTextProgressEnable, name));
        if (!hasPendingTasks(stopProgress)) {
            if (data) {
                // Modify the plugin representation to set enabled to true
                data.enabled = true;
                AJS.$.ajax({
                    type: 'PUT',
                    url: url,
                    dataType: 'json',
                    contentType: upm.contentTypes['plugin'],
                    data: upm.json.stringify(data),
                    success: function(response) {
                        pluginReps[hash] = response;
                        stopProgress();
                        plugin.removeClass('disabled');
                        plugin.bind('detailsLoaded.enable', function() {
                            upm.displayMessage(details, {
                                body: AJS.params.upmTextEnableSuccess,
                                type: 'success',
                                fadeOut: true
                            });
                            plugin.unbind('detailsLoaded.enable');
                        });
                        refreshPlugin(plugin);
                    },
                    error: function(request) {
                        // Set the plugin to disabled again if there was an error
                        data.enabled = false;
                        stopProgress();
                        upm.handleAjaxError(details, request, name);
                    }
                });
            }
        }
    };

    /**
     * Disables a specified plugin module
     * @method disableModule
     * @param {Event} e The event object
     */
    upm.disableModule = function(e) {
        var element = AJS.$(e.target),
                module = element.closest('div.upm-module'),
                plugin = module.closest('div.upm-plugin'),
                name = AJS.$('h5', module).text(),
                url = module.find('input.upm-module-link').val(),
                details = AJS.$('div.upm-details', plugin),
                upmModulesContainer = element.closest('div.upm-module-container'),
                upmPluginModules = element.closest('div.upm-plugin-modules'),
                data = {};

        e.preventDefault();

        upm.startProgress(AJS.format(AJS.params.upmTextProgressDisable, name));
        if (!hasPendingTasks(stopProgress)) {
            data.name = name;
            data.description = AJS.$('p', module).text();
            data.links = {self: url};
            data.enabled = false;
            AJS.$.ajax({
                type: 'PUT',
                url: url,
                dataType: 'json',
                contentType: upm.contentTypes['module'],
                data: upm.json.stringify(data),
                success: function(response) {
                    stopProgress();
                    module.addClass('upm-module-disabled');
                    totalModules = upmModulesContainer.children(".upm-module").length;
                    disabledModules = upmModulesContainer.children(".upm-module-disabled").length;
                    upmPluginModules.find('.upm-count-enabled').html(AJS.format(AJS.params.upmCountEnabled, totalModules - disabledModules, totalModules));
                    upm.displayMessage(details, {
                        body: AJS.format(AJS.params.upmTextModuleDisableSuccess, htmlEncode(name)),
                        type: 'info',
                        fadeOut: true
                    });
                },
                error: function(request) {
                    stopProgress();
                    upm.handleAjaxError(details, request, name);
                }
            });
        }
    };

    /**
     * Enables a specified plugin module
     * @method enableModule
     * @param {Event} e The event object
     */
    upm.enableModule = function(e) {
        var element = AJS.$(e.target),
                module = element.closest('div.upm-module'),
                plugin = module.closest('div.upm-plugin'),
                name = AJS.$('h5', module).text(),
                url = module.find('input.upm-module-link').val(),
                details = AJS.$('div.upm-details', plugin),
                upmModulesContainer = element.closest('div.upm-module-container'),
                upmPluginModules = element.closest('div.upm-plugin-modules'),
                data = {};

        e.preventDefault();

        upm.startProgress(AJS.format(AJS.params.upmTextProgressEnable, name));
        if (!hasPendingTasks(stopProgress)) {
            data.name = name;
            data.description = AJS.$('p', module).text();
            data.links = {self: url};
            data.enabled = true;
            AJS.$.ajax({
                type: 'PUT',
                url: url,
                dataType: 'json',
                contentType: upm.contentTypes['module'],
                data: upm.json.stringify(data),
                success: function(response) {
                    stopProgress();
                    module.removeClass('upm-module-disabled');
                    totalModules = upmModulesContainer.children(".upm-module").length;
                    disabledModules = upmModulesContainer.children(".upm-module-disabled").length;
                    upmPluginModules.find('.upm-count-enabled').html(AJS.format(AJS.params.upmCountEnabled, totalModules - disabledModules, totalModules));
                    upm.displayMessage(details, {
                        body: AJS.format(AJS.params.upmTextModuleEnableSuccess, htmlEncode(name)),
                        type: 'success',
                        fadeOut: true
                    });
                },
                error: function(request) {
                    stopProgress();
                    upm.handleAjaxError(details, request, name);
                }
            });
        }
    };

    /**
     * Removes the "Search" option from the Install tab dropdown and resets the search box
     * @method removeSearchOption
     */
    function removeSearchOption() {
        // remove search option from dropdown
        AJS.$('#upm-search-option').remove();
        // clear search term, if there is one
        AJS.$('#upm-install-search-box').val('').blur();
    }

    /**
     * Refresh UPM notifications.
     */
    function refreshNotifications() {
        AJS.$('#upm-notifications').trigger('refreshNotifications');
    }

    /**
     * Submit a plugin license form via AJAX and handle the response
     * @method submitPluginLicense
     * @param {Event} e The form submit event
     */
    function submitPluginLicense(e) {
        // Find and disable form elements
        var container = AJS.$(e.target).closest('div.upm-plugin-license-container'),
            form = container.find('div.upm-license-details'),
            elements = form.find('textarea, input').attr('disabled', 'disabled'),
            spinner = form.find('span.loading').show(),

            // Get plugin information and combine with license
            plugin = form.closest('div.upm-plugin'),
            details = plugin.find('div.upm-details'),
            hash = getPluginHash(plugin),
            data = pluginReps[hash];

        e.preventDefault();

        data.licenseDetails = data.licenseDetails || {};
        data.licenseDetails.rawLicense = AJS.$.trim(elements[0].value);

        // PUT the license key to the plugin `modify` url
        AJS.$.ajax({
            type: 'PUT',
            url: data.links['update-license'],
            contentType: upm.contentTypes.plugin,
            data: upm.json.stringify(data),

            success: function(response) {
                var msg,
                    isNewLicense = details.find('dd.upm-plugin-license-status').is(':hidden') 
                        // New license won't be empty
                        && data.licenseDetails.rawLicense;

                buildPluginDetails(hash, details, function(callbackContainer, callbackResponse) {
                    var pacResponse = callbackResponse.pacResponse;
                    // If the plugin was previously enabled, and the update disabled it, show a possible explanation message
                    if (data.enabled === true && response.enabled === false) {
                        msg = AJS.params.upmLicenseSubmitSuccessDisabled;
                    } else if (response.enabled === false && response.licensedetails && response.licenseDetails.valid === true) {
                        // Otherwise, if the plugin in disabled but the license is valid, suggest enabling the plugin
                        msg = AJS.params.upmLicenseSubmitSuccessPluginEnableable;
                    } else if (pacResponse && pacResponse.update && !pacResponse.update.licenseCompatible) {
                        // A message was already set by buildManageDetails. Don't overwrite it.
                    } else if (isNewLicense && response.licenseDetails && response.licenseDetails.evaluation) {
                        // If the plugin is enabled and this is a *new* evaluation license, thank the user for trying it.
                        msg = AJS.format(AJS.params.upmLicenseSubmitSuccessNewEvaluation, response.name);
                    } else if (isNewLicense && (!response.licenseDetails || !response.licenseDetails.evaluation)) {
                        // If the plugin is enabled and this is a *new* non-evaluation license, thank the user for purchasing it.
                        msg = AJS.format(AJS.params.upmLicenseSubmitSuccessNewPurchase, response.name);
                    } else if (!response.licenseDetails) {
                        // No possible errors while removing the license. Show the default removal success message
                        msg = AJS.params.upmLicenseSubmitSuccessRemoved;
                    } else {
                        // No possible errors while updating the license. Show the default success message
                        msg = AJS.params.upmLicenseSubmitSuccess;
                    }

                    if (msg) {
                        // Show success banner
                        upm.displayMessage(details, {
                            body: msg,
                            type: 'success',
                            fadeOut: true
                        });
                    }

                    refreshNotifications();
                });
            },
            
            // Show error banner with subCode message, or, if something goes horribly wrong, generic message
            error: function(request) {
                try {
                    var response = upm.json.parse(request.responseText),
                        subCode = response.subCode,
                        message;
                } catch (e) {
                    AJS.log('Error trying to parse response text: ' + e);
                }

                // Did we time out?
                if (!upm.reloadIfWebSudoError(subCode)) {
                    message = AJS.params[subCode || (request.statusText === 'timeout' && 'ajaxTimeout')
                            || (request.statusText === 'error' && 'ajaxCommsError') || 'ajaxServerError'];
                    upm.displayMessage(details, {
                        body: AJS.format(message, data.name),
                        type: 'error'
                    });
                    elements.removeAttr('disabled');
                    spinner.hide();
                }
            }
        });
    }

    /**
     * Changes which plugins are displayed on the "Install" tab.  Fired when the dropdown in "Install" tab is changed
     * @method changeDisplayedPlugins
     */
    upm.changeDisplayedPlugins = function() {
        var dropdown = AJS.$('#upm-install-type'),
                type = dropdown.val(),
                options = {isExpandable: true, isInstalledList: false, className: 'install'},
                container = AJS.$('#upm-install-' + type),
                parentContainer = AJS.$('#upm-install-container-' + type),
                searchTerm = AJS.$('#upm-install-search-box').val();

        if (type == 'search') {
            // handle case where dropdown is set to search option but there is no search term
            if (!searchTerm || searchTerm == AJS.params.upmTextInstallSearchBox) {
                removeSearchOption();
                // set the dropdown to the first visible option
                type = AJS.$('option:not(.hidden):first', dropdown).val();
                dropdown.val(type);
                container = AJS.$('#upm-install-' + type);
                parentContainer = AJS.$('#upm-install-container-' + type);
            } else {
                AJS.$('#upm-install-search-form').trigger('submit');
            }
        } else {
            removeSearchOption();
        }
        AJS.$('#upm-panel-install .upm-install-type').hide().removeClass('loaded');
        AJS.$('#upm-panel-install .upm-install-plugin-list').empty();
        parentContainer.show();
        if (upm.resources[type]) {
            container.removeData('pagination').find('div.upm-plugin-list-container').remove();
            container.find('div.upm-development-product-version,div.upm-unknown-product-version').remove();
            filterPlugins.container = null;
            AJS.$('p.upm-info', container).remove();
            loadPlugins(upm.resources[type], options, container, parentContainer);
        }
    };

    /**
     * Enables safe mode on installed plugins, for support/debugging purposes
     * @method enableSafeMode
     */
    upm.enableSafeMode = function() {
        upm.startProgress(AJS.params.upmTextProgressSafeModeEnable);
        if (!hasPendingTasks(stopProgress)) {
            AJS.$.ajax({
                type: 'PUT',
                url: upm.resources['enter-safe-mode'],
                dataType: 'json',
                contentType: upm.contentTypes['safe-mode'],
                data: upm.json.stringify({enabled: true, links : {} }),
                success: function(response) {
                    safeMode = true;
                    // setting safe mode before we reload the tab ensures that we won't display anything
                    // inappropriate for safe mode, such as a UPM self-update link
                    upm.$messageContainer.empty();
                    upm.loadTab('manage');
                    stopProgress();
                    upm.resources['exit-safe-mode-restore'] = upm.resources['exit-safe-mode-restore'] || response.links['exit-safe-mode-restore'];
                    upm.resources['exit-safe-mode-keep'] = upm.resources['exit-safe-mode-keep'] || response.links['exit-safe-mode-keep'];
                    setSafeModeClass();
                },
                error: function(request) {
                    stopProgress();
                    if (request.status == '409') {
                        // if 409 is returned, we're already in safe mode
                        refreshSafeModeState();
                    }
                    upm.handleAjaxError(upm.$messageContainer, request, '');
                }
            });
        }
    };

    /**
     * Exits safe mode and restores to the previous (saved) plugin configuration
     * @method restoreFromSafeMode
     */
    upm.restoreFromSafeMode = function() {
        upm.startProgress(AJS.params.upmTextProgressSafeModeRestore);
        if (!hasPendingTasks(stopProgress)) {
            AJS.$.ajax({
                type: 'PUT',
                url: upm.resources['exit-safe-mode-restore'],
                dataType: 'json',
                contentType: upm.contentTypes['safe-mode'],
                data: upm.json.stringify({enabled: false, links : {} }),
                success: function(response) {
                    var hash = upm.getLocationHash();
                    stopProgress();
                    upm.displayMessage(upm.$messageContainer, {
                        body: AJS.params.upmTextSafeModeRestoreSuccess,
                        type: 'success',
                        className: 'safeMode'
                    });
                    safeMode = false;
                    upm.loadTab(hash.tab, hash.key);
                    upm.resources['enter-safe-mode'] = upm.resources['enter-safe-mode'] || response.links['enter-safe-mode'];
                    setSafeModeClass();
                },
                error: function(request) {
                    stopProgress();
                    if (request.status == '409') {
                        // if 409 is returned, we're not in safe mode
                        refreshSafeModeState();
                    }
                    upm.handleAjaxError(upm.$messageContainer, request, '');
                }
            });
        }
    };

    /**
     * Exits safe mode, keeping the current plugin configuration
     * @method exitSafeMode
     */
    upm.exitSafeMode = function() {
        upm.startProgress(AJS.params.upmTextProgressSafeModeKeepState);
        if (!hasPendingTasks(stopProgress)) {
            AJS.$.ajax({
                type: 'PUT',
                url: upm.resources['exit-safe-mode-keep'],
                dataType: 'json',
                contentType: upm.contentTypes['safe-mode'],
                data: upm.json.stringify({enabled: false, links : {} }),
                success: function(response) {
                    var hash = upm.getLocationHash();
                    stopProgress();
                    upm.displayMessage(upm.$messageContainer, {
                        body: AJS.params.upmTextSafeModeKeepStateSuccess,
                        type: 'success',
                        className: 'safeMode'
                    });
                    safeMode = false;
                    upm.loadTab(hash.tab, hash.key);
                    upm.resources['enter-safe-mode'] = upm.resources['enter-safe-mode'] || response.links['enter-safe-mode'];
                    setSafeModeClass();
                },
                error: function(request) {
                    stopProgress();
                    if (request.status == '409') {
                        // if 409 is returned, we're not in safe mode
                        refreshSafeModeState();
                    }
                    upm.handleAjaxError(upm.$messageContainer, request, '');
                }
            });
        }
    };

    /**
     * Gets the current safe mode state and alters the UI accordingly
     * @method refreshSafeModeState
     */
    function refreshSafeModeState() {
        AJS.$.ajax({
            type: 'GET',
            cache: false,
            url: upm.resources['safe-mode'],
            dataType: 'json',
            contentType: upm.contentTypes['safe-mode'],
            success: function(response) {
                safeMode = response.enabled;
                setSafeModeClass();
                upm.loadTab('manage');
                AJS.$.extend(upm.resources, response.links);
            },
            error: function(request) {
                upm.handleAjaxError(upm.$messageContainer, request, '');
            }
        });
    }

    /**
     * Trigger a search on a regex constructed from the value of the event target
     * @method filterPluginsByName
     * @param {Event} e The event object
     */
    function filterPluginsByName(e) {
        var target = AJS.$(e.target),
            val = target.val(),
            // Escape characters that would cause regex construction to fail
            regexp = new RegExp(val.replace(/[-[\]{}()*+?.,\\^$|#\s]/g, "\\$&"), 'i');

        // Multiple events can fire this method. Save the search text, and if it's different than
        // the last save, we know this event is unique. If it's the same, another event got it first
        if (filterPluginsByName.val !== val) {
            filterPluginsByName.val = val;

            // Wait a short time after the user finishes typing so we don't perform a search
            // on every keystroke
            setTimeout(function() {
                // Finally, only do the search if the user has stopped typing, eg the search text after the
                // timeout hasn't changed
                if (target.val() == val) {
                    filterPlugins(e.type, target, function(plugin) {
                        var h4 = AJS.$('h4.upm-plugin-name', plugin),
                            text = h4.text(),
                            match = text.match(regexp);

                        // Highlight search results
                        if (match) {
                            return true;
                        }
                        return false;
                    });

                    // hide these buttons whenever filtering on the Manage tab
                    if (target.attr('id') == 'upm-manage-filter-box') {
                        if (!val) {
                            AJS.$('#upm-update-all').removeClass('upm-filtered');
                            AJS.$('#upm-safe-mode-enable').removeClass('upm-filtered');
                        } else {
                            AJS.$('#upm-update-all').addClass('upm-filtered');
                            AJS.$('#upm-safe-mode-enable').addClass('upm-filtered');
                        }
                    }
                }
            }, filterTypeDelayMs);
        }
    }

    upm.filterBundles = function(e) {
        var form = AJS.$(e.target),
                input = form.find('input'),
                term = input.val();
        if (term && term != AJS.params.upmTextOsgiSearchBox) {
            AJS.$.ajax({
                url: upm.resources['osgi-bundles'] + '?q=' + term,
                type: 'get',
                cache: false,
                dataType: 'json',
                success: function(response) {
                    var bundles = {};
                    AJS.$.each(response.entries, function() {
                        bundles['upm-plugin-' + createHash(upm.symbolicName, 'osgi')] = true;
                    });
                    filterPlugins(e.type, form, function(plugin) {
                        return bundles[plugin.id];
                    });
                },
                error: function(request) {
                    displayErrorMessage(upm.$messageContainer, request.responseText, "");
                }
            });
        }
        e.preventDefault();
    };

    /**
     * Filters the associated list(s) of plugins to match the entered text
     * @method filterPlugins
     * @param {String} type The event type
     * @param {HTMLElement} target The input in which the search text is held
     * @param {Function} matchFn The function to determine whether a plugin matches
     */
    function filterPlugins(type, target, matchFn) {
        if (type == 'propertychange' && target.val() == AJS.params.upmTextFilterPlugins) {
            // don't do anything if onpropertychange (IE only) was triggered and the text is the default text
            return;
        }

        // only recalculate the relevant plugins if we need to
        if (!filterPlugins.container || !filterPlugins.container.is(':visible')) {
            filterPlugins.container = target.closest('div.upm-panel');
            filterPlugins.plugins = [];
            AJS.$('div.upm-plugin-list-container', filterPlugins.container).each(function() {
                filterPlugins.plugins[AJS.$(this).parent().attr('id')] = AJS.$('div.upm-plugin', this);
            });
        }

        AJS.$('div.upm-plugin-list-container', filterPlugins.container).each(function() {
            var plugins = filterPlugins.plugins[AJS.$(this).parent().attr('id')],
                    hasMatchedPlugin = false;
            plugins.addClass('hidden');
            plugins.filter(function() {
                var matched = matchFn(this);
                hasMatchedPlugin = hasMatchedPlugin || matched;
                return matched;
            }).removeClass('hidden');

            AJS.$('p.filter-info', this).remove();
            if (hasMatchedPlugin) {
                AJS.$('div.upm-expand-collapse-all', this).removeClass('hidden');
            } else {
                AJS.$('div.upm-expand-collapse-all', this).addClass('hidden');
                AJS.$('div.upm-plugin-list', this).append(AJS.$('<p class="upm-info filter-info"></p>').text(AJS.params.upmTextFilterNoPlugins));
            }
        });
    }

    upm.clearInstallSearch = function(e) {
        AJS.$('#upm-install-search-box').val('').blur();
        upm.changeDisplayedPlugins();
    };

    upm.clearBundleSearch = function(e) {
        AJS.$('#upm-osgi-search-box').val('').blur();
        filterPluginsByName(e);
    };

    /**
     * Searches PAC for plugins that match the entered text
     * @method searchForPlugins
     * @param {Event} e The event object
     */
    upm.searchForPlugins = function(e) {
        var form = AJS.$(e.target),
                input = form.find('input'),
                term = input.val(),
                options = {isExpandable: true, isInstalledList: false, className: 'install'},
                container = AJS.$('#upm-install-search'),
                parentContainer = AJS.$('#upm-install-container-search'),
                dropdown = AJS.$('#upm-install-type');
        if (term && term != AJS.params.upmTextInstallSearchBox) {
            AJS.$('#upm-panel-install .upm-install-type').hide();
            container
                    .empty()
                    .removeData('pagination')
                    .append(AJS.$('<h3></h3>').text(AJS.format(AJS.params.upmTextSearchResults, term)));
            parentContainer.show();
            // set drop down to search option so that the other options can be selected
            if (dropdown.val() != 'search') {
                AJS.$('<option value="search" id="upm-search-option"></option>').text(AJS.params['upmTextSearchOption']).prependTo(dropdown);
            }
            dropdown.val('search');
            loadPlugins(upm.resources['available'] + '?' +  form.serialize(), options, container, parentContainer);
        }
        e.preventDefault();
    };

    /**
     * Returns the unique hash for a plugin given the dom element corresponding to that plugin
     * @method getPluginHash
     * @param {HTMLElement} plugin The dom element for the plugin in question
     * @return {String} The hash corresponding to the plugin
     */
    function getPluginHash(plugin) {
        var hash = plugin.attr('id');
        return hash.substring('upm-plugin-'.length, hash.length);
    }

    /**
     * Returns the link to the plugin details given the dom element corresponding to that plugin and if plugin type
     * is updatable
     * @method getPluginLink
     * @param {HTMLElement} plugin The dom element for the plugin in question
     * @param {Boolean} isUpdatable Set to true if plugin will use "available" plugin details link if it exist
     * @return {String} link to the plugin details
     */
    function getPluginLink(plugin, isUpdatable) {
        if (isUpdatable) {
            return plugin.links.available || plugin.links.self;
        } else {
            return plugin.links.self;
        }
    }

    /**
     * Send an Ajax request to get the "details" link from PAC, and puts it instead of the <span> represented by detailsLinkSpan.
     * @method fetchPacPluginDetailsLink
     * @param {HTMLElement} detailsLinkSpan a dom element that we would like to add the plugin details link to
     * @param {Object} plugin the plugin, with its .key and .version properties
     */
    function fetchPacPluginDetailsLink(detailsLinkSpan, plugin) {
        fetchPacPluginDetails(plugin, function(response) {
            setPacDetailsLink(detailsLinkSpan, response);
        });
    }

    /**
     * Sets the details link from PAC response information for a plugin
     * @method setPacDetailsLink
     * @param {HTMLELEMENT} detailsLinkSpan The span in which to insert the link
     * @param {Object} pacResponse response data from PAC
     */
    function setPacDetailsLink(detailsLinkSpan, pacResponse) {
        if (pacResponse && pacResponse.links && pacResponse.links.details) {
            var text = detailsLinkSpan.text();
            detailsLinkSpan
                .empty()
                .append(
                    AJS.$('<a href="" class="upm-plugin-details-link" target="_blank"></a>')
                        .attr('href', pacResponse.links.details)
                        .attr('title', AJS.params['upm.plugin.details.title'])
                        .text(text)
            );
        }
    }

    /**
     * Contact PAC to get plugin details, including update information
     * @method fetchPacPluginDetails
     * @param {Object} plugin The plugin representation
     * @param {Function} callback the function to execute when the AJAX call completes
     */
    function fetchPacPluginDetails(plugin, callback) {
        var response;
        AJS.$.ajax({
            url: plugin.links['pac-details'],
            type: 'get',
            dataType: 'json',
            success: function(pacResponse) {
                response = pacResponse;
            },
            complete: function() {
                callback(response);
            }
        });
    }

    /**
     * Escapes '.' and '#' characters for use in jQuery selectors
     * @method escapeSelector
     * @param {String} key A plugin key
     * @return {String} The escaped text
     */
    function escapeSelector(key) {
        return key ? key.replace(/\./g, '\\.').replace(/#/, '\\#') : key;
    }

    /**
     * A transform to apply to url attribute values.
     * @method htmlSanitizerUrlPolicy
     * @param url {String} URL to validate
     * @return {String} the url, if it passed the policy check, null otherwise
     */
    function htmlSanitizerUrlPolicy(url) {
        if (/^https?:\/\//.test(url)) {
            return url;
        }
        return null;
    }

    /*
     * Takes a timestamp and returns localize string representation
     * @method prettyDate
     * @param {String|Number} time A timestamp
     * @return {String} A localized representation of the timestamp
     */
    upm.prettyDate = function(time){
        var date,
            exp = /([0-9])T([0-9])/;
        if (typeof time == 'string') {
            if ((time).match(exp)) {
                // ISO time.  We need to do some formatting to be able to parse it into a date object
                time = time
                    // for the Date ctor to use UTC time
                    .replace(/Z$/, " -00:00")
                    // remove 'T' separator
                    .replace(exp,"$1 $2");
            }
            // more formatting to make it parseable
            time = time
                // replace dash-separated dates with forward-slash separated
                .replace(/([0-9]{4})-([0-9]{2})-([0-9]{2})/g,"$1/$2/$3")
                // get rid of semicolon and add space in front of timezone offset (for Safari, Chrome, IE6)
                .replace(/\s?([-\+][0-9]{2}):([0-9]{2})/, ' $1$2');
        }
        date = new Date(time || "");
        return date.toLocaleString();
    };

    /**
     * Creates and returns a plugin list element with text stating no managable plugins were found
     * @method buildEmptyUserInstalledPluginListForManaging
     * @return {HTMLElement} Element representing empty plugin list
     */
    function buildEmptyUserInstalledPluginListForManaging() {
        return AJS.$('<p class="upm-info"></p>').text(AJS.params.upmTextNotFoundManageUserInstalled);
    }

    /**
     * Creates and returns a plugin list element with text stating no installable plugins were found
     * that the user can install
     * @method buildEmptyPluginListForInstalling
     * @return {HTMLElement} Element representing empty plugin list
     */
    function buildEmptyPluginListForInstalling() {
        return AJS.$('<p class="upm-info"></p>').text(AJS.params.upmTextNotFoundInstall);
    }

    /**
     * Creates and returns a list of plugins for insertion into the dom
     * @method buildPluginList
     * @param {Array} plugins An array of plugin objects
     * @param {Object} options Whether individual plugins should be expandable to reveal plugin details
     * @return {HTMLElement} Element representing plugin list
     */
    upm.buildPluginList = function(plugins, options) {
        upm.buildPluginList.template = upm.buildPluginList.template || AJS.$(AJS.$('#upm-plugin-list-template').html());
        var listContainer = upm.buildPluginList.template.clone(),
            list = AJS.$('.upm-plugin-list', listContainer);
        if ((plugins && plugins.length) || options.forceCreation) {
            if (options.isExpandable) {
                list.addClass('expandable');
            }
            if (options.isInstalledList) {
                list.addClass('upm-installed-list');
            }
            if (options.className) {
                list.addClass(options.className);
            }
            buildPluginElements(plugins, list, options);
        } else {
            if (options.className == 'manage' && options.isUserPluginList) {
                listContainer = buildEmptyUserInstalledPluginListForManaging();
            } else {
                listContainer = buildEmptyPluginListForInstalling();
            }
        }
        return listContainer;
    };

    /**
     * Appends a group of plugin elements to a specified container
     * @method buildPluginElements
     * @param {Array} plugins An array of plugin objects
     * @param {HTMLElement} container Dom element to append to
     * @param {Object} options Plugin options
     */
    function buildPluginElements(plugins, container, options) {
        var type = getPluginTypeFromContainer(container);

        for (var i = 0, len = plugins.length; i < len; i++) {
            container.append(buildPluginElement(plugins[i], type, options));
        }
    }

    /**
     * Build a single plugin element
     * @method buildPluginElement
     * @param {Object} plugin The plugin data representation
     * @param {String} type The plugin type
     * @param {Object} options Plugin options
     */
    function buildPluginElement(plugin, type, options) {
        buildPluginElement.template = buildPluginElement.template || AJS.$(AJS.$('#upm-plugin-template').html());

        var pluginElement = buildPluginElement.template.clone(),
            hash = createHash(options.isBundle ? plugin.symbolicName : plugin.key, type),
            updateLink = plugin.links['update-details'];

        pluginElement.attr('id', 'upm-plugin-' + hash);
        if (options.isBundle) {
            pluginElement.find('.upm-plugin-name').text(plugin.id + " - " + (plugin.name || plugin.symbolicName));
        } else {
            pluginElement.find('.upm-plugin-name').html(upm.html_sanitize(plugin.name, htmlSanitizerUrlPolicy));
        }
        
        pluginElement.find('input.upm-plugin-key').val(plugin.key);

        if (plugin.summary || plugin.description) {
            pluginElement.find('p.upm-plugin-summary')
                    .html(upm.html_sanitize(plugin.summary || plugin.description, htmlSanitizerUrlPolicy))
                    .find('a').each(function() {
                        var el = AJS.$(this);
                        el.replaceWith(el.text());
                    });
        }
        pluginElement.find('input.upm-plugin-link-self').attr('id', 'upm-plugin-link-self-' + hash).val(getPluginLink(plugin, options.isUpdatable));
        pluginElement.find('input.upm-plugin-link-modify').attr('id', 'upm-plugin-link-modify-' + hash).val(plugin.links['modify']);
        pluginElement.find('input.upm-plugin-link-delete').attr('id', 'upm-plugin-link-delete-' + hash).val(plugin.links['delete']);

        if (updateLink) {
            pluginElement.find('input.upm-plugin-link-update-details').attr('id', 'upm-plugin-link-update-details-' + hash).val(updateLink);
        }

        var iconElement = AJS.$('<img src="" alt="" class="upm-plugin-icon" width="16">')
            .attr('src', plugin.links['plugin-icon'] ? plugin.links['plugin-icon'] : AJS.params['defaultPluginIcon']);
        AJS.$('.upm-plugin-icon-container', pluginElement).append(iconElement);

        // 'enabled' property isn't returned in all representations
        if (plugin.enabled === false) {
            pluginElement.addClass('disabled');
        }
        if (plugin.userInstalled) {
            pluginElement.addClass('user-installed');
        } else {
            pluginElement.addClass('upm-system');
        }
        if (plugin.restartState) {
            displayRestartMessage(pluginElement, plugin.restartState);
        }
        if (plugin['static']) {
            pluginElement.addClass('upm-static');
        }
        if (options.isBundle && plugin.state) {
            if (plugin.state != 'ACTIVE') {
                pluginElement.addClass('disabled');
            }
        }

        return pluginElement;
    }

    /**
     * Refreshes the details for a specified plugin
     * @method refreshPlugin
     * @param {HTMLElement} plugin The dom element of the plugin in question
     */
    function refreshPlugin(plugin) {
        var details = AJS.$('div.upm-details', plugin);
        if (AJS.$('div.upm-plugin-modules', details).hasClass('expanded')) {
            plugin.bind('detailsLoaded.refresh', function() {
                AJS.$('div.upm-plugin-modules', details).addClass('expanded');
                plugin.unbind('detailsLoaded.refresh');
            });
        }
        details.empty().removeClass('loaded').addClass('loading');
        buildPluginDetails(getPluginHash(plugin), details);
    }

    /**
     * Returns the tab type for a plugin based on its container
     * @method getPluginTypeFromContainer
     * @param {HTMLElement} container The dom element for the plugin list containing the plugin in question
     * @return {String} Tab type (one of 'update', 'install', 'osgi', 'compatibility')
     */
    function getPluginTypeFromContainer(container) {
        return container.hasClass('update') ? 'update' :
               container.hasClass('install') ? 'install' :
               container.hasClass('osgi') ? 'osgi' :
               container.hasClass('compatibility') ? 'compatibility' : 'manage';
    }

    /**
     * Given a dom element, makes any necessary changes to display aui toolbars correctly in all browsers.
     * This is necessary because AUI Toolbars are still experimental/prototypal in some of the AUI versions we need to support.
     * @method cleanupToolbarLayout
     * @param {HTMLElement} element The dom element to check for toolbars
     */
    function cleanupToolbarLayout(element) {
        // Add a "first" class to first toolbar item for browsers that don't support the :first-of-type psuedo-class
        AJS.$(element).find('ul.toolbar-group li.toolbar-item:first').addClass('first');
    }

    /**
     * Queries for plugin information and builds plugin details based on the response
     * @method buildPluginDetails
     * @param {String} pluginHash Unique hash of the plugin in question
     * @param {HTMLElement} container Plugin details container element. It will be emptied.
     * @param {Function} callback The function to return the details element and response to
     */
    function buildPluginDetails(pluginHash, container, callback) {
        var list = container.closest('div.upm-plugin-list'),
            escapedHash = escapeSelector(pluginHash),
            selfLinkSelector = '#upm-plugin-link-self-' + escapedHash,
            updateLinkSelector = '#upm-plugin-link-update-details-' + escapedHash,
            pluginType,
            url;

        // Normal plugin expand
        if (list.length) {
            pluginType = getPluginTypeFromContainer(list);
            url = AJS.$(selfLinkSelector).val();
        // Special case for UPM expand. UPM row is not in DOM, so it won't have a list it's inside
        } else {
            pluginType = 'update';
            url = AJS.$(selfLinkSelector, upm.upmPluginContainer).val();
        }

        AJS.$.ajax({
            type: 'get',
            cache: false,
            url: url,
            dataType: 'json',
            error: function(response) {
                var subcode,
                    status,
                    msg;

                try {
                    if (response.responseText) {
                        response = upm.json.parse(response.responseText);
                        msg = AJS.params.upmTextPluginDetailsError;
                        status = response["status-code"] || response.status;
                        subcode = response.subCode 
                            || (response.status && response.status.subCode)
                            || (response.details && response.details.error.subCode);
                        // We should reload if it was a webSudo error
                        if (upm.reloadIfUnauthorizedStatus(status)) {
                            msg = AJS.params['upm.error.unauthorized'];
                        } else {
                            upm.reloadIfWebSudoError(subcode);
                        }
                    }
                } catch (e) {
                    AJS.log('Error trying to parse response text: ' + e);
                }
                container
                    .addClass('loaded error')
                    .append(AJS.$('<div></div>').text(msg));

                // detailsLoaded is intentionally a separate event from pluginLoaded to
                // differentiate between a full plugin container expanding (pluginLoaded)
                // and the details element of a plugin refreshing / loading (detailsLoaded)
                container.trigger('detailsLoaded');

                callback && callback(container, response);
            },
            success: function(pluginResponse) {
                // The issue here is that plugins get thier update information from a call to PAC, so we can't show the plugin
                // update information until this call returns
                if (pluginType == 'update' || pluginType == 'manage') {
                    var combined = pluginResponse;

                    fetchPacPluginDetails(pluginResponse, function(pacResponse) {
                        // Append the pacResponse to the plugin. There is probably a more elegant way to do this
                        pluginResponse.pacResponse = pacResponse;
                        buildPluginMarkup(container.empty(), combined, pluginType, pluginHash);
                        callback && callback(container, combined);
                    });
                // However, if this plugin does NOT have an update/manage, we do not need to wait for the PAC call to return,
                // since it will not have plugin information. We can make the calls in parallel
                } else {
                    buildPluginMarkup(container.empty(), pluginResponse, pluginType, pluginHash);
                    callback && callback(container, pluginResponse);
                }
            }
        });
    }

    /**
     * Build the markup for a plugin from the plugin JSON from the server
     * @method buildPluginMarkup
     * @param {HTMLElement} detailsContainer The plugin details element
     * @param {Object} pluginResponse JSON representation of the plugin from the server
     * @param {String} pluginType The plugin type
     * @param {String} pluginHash (Optional) The plugin hash
     */
    function buildPluginMarkup(detailsContainer, pluginResponse, pluginType, pluginHash) {
        var details = detailsFn[pluginType](pluginResponse),
            pluginContainer = detailsContainer.closest('.upm-plugin'),
            hash = pluginHash || getPluginHash(pluginContainer),
            modules = pluginResponse.modules;

        // Plugin disabled, or OSGi bundle disabled
        if (!pluginResponse.enabled && !pluginResponse.state == 'ACTIVE') {
            pluginContainer.addClass('disabled');
        }

        isIE && cleanupToolbarLayout(details);

        pluginReps[hash] = pluginResponse;

        detailsContainer.append(details);

        // Disable update button for this plugin in safe mode
        if (safeMode) {
            detailsContainer.find('a.upm-update').closest('li.toolbar-item').addClass('disabled');
        }
        if (modules) {
            buildPluginModules(modules, detailsContainer);
        }

        detailsContainer
            .trigger('detailsLoaded')
            .removeClass('loading')
            .addClass('loaded');
    }

    /**
     * Creates an element containing a series of plugin module elements and inserts it into the dom
     * @method buildPluginModules
     * @param {Array} modules An array of plugin module objects
     * @param {HTMLElement} container Plugin details container element
     */
    function buildPluginModules(modules, container) {
        buildPluginModules.template = buildPluginModules.template || AJS.$(AJS.$('#upm-plugin-module-template').html());
        var pluginKey = AJS.$('dd.upm-plugin-key', container).text(),
                modulePresent   = AJS.$('.upm-module-present', container),
                moduleNone      = AJS.$('.upm-module-none', container),
                moduleContainer = AJS.$('div.upm-module-container', container),
                clone,
                modulesEnabled = 0;

        if (modules && modules.length) {
            clone = moduleContainer.clone();
            moduleContainer.addClass('loading');
            for (var i = 0, len = modules.length; i < len; i++) {
                var module = modules[i],
                        el = buildPluginModules.template.clone();
                el.attr("id", "upm-plugin-module-" + createHash(module.key));
                if (module.name) {
                    AJS.$('h5', el).text(module.name);
                    AJS.$('p.upm-module-key', el).text("(" + module.key + ")");
                } else {
                    AJS.$('h5', el).text(module.key);
                }
                if (module.description) {
                    AJS.$('p.upm-module-description', el).text(module.description);
                } else {
                    AJS.$('p.upm-module-description', el).remove();
                }
                
                if (module.links.self && module.recognisableType) {
                    AJS.$('input.upm-module-link', el).val(module.links.self);
                    AJS.$('a.upm-module-disable', el).attr('id', 'module-disable-' + module.completeKey);
                    AJS.$('a.upm-module-enable', el).attr('id', 'module-enable-' + module.completeKey);
                } else {
                    AJS.$('div.upm-module-actions', el)
                        .addClass('upm-module-cannot-disable')
                        .find('div.aui-toolbar').remove();
                }
                if (!module.enabled) {
                    el.addClass('upm-module-disabled');
                } else {
                    modulesEnabled++;
                }
                clone.append(el);
                if (!module.optional) {
                    AJS.$('div.upm-module-actions', el).addClass("upm-module-cannot-disable");
                }
            }
            moduleContainer.replaceWith(clone);
            AJS.$('span.upm-count-enabled', container).html(AJS.format(AJS.params.upmCountEnabled, modulesEnabled , modules.length));
            modulePresent.removeClass('hidden');

            if (isUpm(pluginKey)) {
                AJS.$('div.upm-module-actions', container).addClass('hidden');
            }
        } else {
            moduleNone.removeClass('hidden');
        }
    }

    /**
     * Adds an anchor element with the specified link (if any), otherwise just sets the text of the container
     * @method addLinkOrText
     * @param {Object} obj Object containing the text and link information
     * @param {HTMLElement} container Container element
     * @return {HTMLElement} Container element
     */
    function addLinkOrText(obj, container) {
        var anchor;
        if (obj && obj.text) {
            if (obj.link) {
                anchor = AJS.$('<a href="" target="_blank"></a>')
                    .text(obj.text)
                    .attr('href', obj.link);

                if (obj.title) {
                    anchor.attr('title', obj.title);
                }
                if (obj.linkClass) {
                    anchor.addClass(obj.linkClass);
                }

                anchor.appendTo(container);
            } else {
                container.text(obj.text);
            }
        } else {
            container.remove();
        }
        return container;
    }

    /**
     * Determines whether or not this plugin is installable by the UPM
     * @method isPluginInstallable
     * @param {Object} pacInfo Object containing the plugin information provided by PAC
     * @return {Boolean} If the plugin is installable
     */
    function isPluginInstallable(pacInfo) {
        // Must be marked as deployable
        return (pacInfo.deployable
            // Must be version two, or we're in confluence, which can install v1 plugins
            && (pacInfo.pluginSystemVersion === 'TWO' || upm.isConfluence)
            // And must have a url to fetch the jar from for installing
            && pacInfo.links.binary);
    }

    /**
     * Determines the appropriate size for the given icon and replaces the placeholder with the icon at that size
     * @method scaleAndReplaceIcon
     * @param {HTMLElement} element Placeholder icon element
     * @param {Object} icon Object containing the icon information
     */
    function scaleAndReplaceIcon(element, icon) {
        var scaleFactor = 1,
                width = icon.width,
                height = icon.height,
                link = icon.link,
                clone = element.clone();

        if (width > maxIconWidth) {
            scaleFactor = maxIconWidth / width ;
        }

        if (height > maxIconHeight) {
            var tmp = maxIconHeight / height;
            scaleFactor = (tmp < scaleFactor) ? tmp : scaleFactor;
        }

        height = height * scaleFactor;
        width = width * scaleFactor;
        // we have to clone the icon and replace it, otherwise the default icon is briefly scaled to the size of the actual icon
        clone.attr('src', link).attr('width', width).attr('height', height);
        element.replaceWith(clone);
    }

    /**
     * Creates and returns an html element with plugin details for the "Install" tab
     * @method buildInstallDetails
     * @param {Object} plugin Object containing the plugin detail information
     * @return {HTMLElement} Plugin details eleemnt
     */
    function buildInstallDetails(plugin) {
        buildInstallDetails.template = buildInstallDetails.template || AJS.$(AJS.$('#upm-plugin-details-install').html());
        var details = buildInstallDetails.template.clone(),
            releaseNotesLink = AJS.$('a.upm-release-notes-link', details);

        // plugin name, with link to plugin details on PAC (if it exists)
        addLinkOrText({
            text: plugin.name,
            link: plugin.links.details,
            title: AJS.params['upm.plugin.details.title'],
            linkClass: 'upm-plugin-details-link'
        }, AJS.$('span.upm-plugin-name', details));
        AJS.$('a.upm-plugin-details-link', details).html(upm.html_sanitize(plugin.name, htmlSanitizerUrlPolicy));
        // plugin version, with link to release notes (if available)
        addLinkOrText({
            text: plugin.version,
            link: plugin.releaseNotesUrl,
            title: AJS.params['upm.plugin.release.notes.title'],
            linkClass: 'upm-plugin-release-notes-link'
        }, AJS.$('span.upm-plugin-version', details));

        AJS.$('dd.upm-plugin-key', details).text(plugin.key || AJS.params.upmTextUnknown);
        AJS.$('dd.upm-plugin-system-version', details).text(plugin.pluginSystemVersion || AJS.params.upmTextUnknown);
        AJS.$('dd.upm-plugin-license', details).text(plugin.license || AJS.params.upmTextUnknown);
        AJS.$('div.upm-plugin-description', details)
            .html(upm.html_sanitize(plugin.description || AJS.params['upm.plugin.no.description'], htmlSanitizerUrlPolicy))
            .find('a').attr('target', '_blank');

        // vendor name, with link to vendor url if it exists
        addLinkOrText({
            text: plugin.vendor && plugin.vendor.name && unescapeHTMLEntities(AJS.format(AJS.params['upm.plugin.developer.by'], plugin.vendor.name)),
            link: plugin.vendor && plugin.vendor.link
        }, AJS.$('h4.upm-plugin-developer', details));

        if (!plugin.description) {
            AJS.$('div.upm-plugin-description', details).addClass('upm-no-description-text');
        }
        AJS.$('a.upm-install', details).attr('id', 'upm-install-' + plugin.key);
        AJS.$('a.upm-buy', details).attr('id', 'upm-install-' + plugin.key);
        AJS.$('a.upm-try', details).attr('id', 'upm-install-' + plugin.key);

        if (plugin.links.binary) {
            AJS.$('input.upm-plugin-binary', details).val(plugin.links.binary);
        }

        AJS.$('input.upm-plugin-homepage', details).val(plugin.links.details);

        var logoElement = AJS.$('<img src="" alt="" class="upm-plugin-logo" width="48">')
                             .attr('src', plugin.logo ? plugin.logo.link : AJS.params['defaultPluginLogo']);
        AJS.$('.upm-plugin-logo-container', details).append(logoElement);

        // Change the install button to a download button if the plugin isn't deployable. This includes if there is no binary url,
        // in which case clicking download will show a modal explaining what to do instead
        if (!isPluginInstallable(plugin)) {
            AJS.$('a.upm-install', details)
                .removeClass('upm-install')
                .addClass('upm-download')
                .text(AJS.params['upmTextDownload'])
                .closest('li.toolbar-item').removeClass('upm-install-item').addClass('upm-download-item');
        // non-deployable plugins cannot be sold on the marketplace
        } else if (plugin.soldOnMarketplace) {
            AJS.$('a.upm-install', details).closest('ul.toolbar-group').addClass('hidden');
            AJS.$('a.upm-buy', details).closest('ul.toolbar-group').removeClass('hidden');
        }

        if (plugin.pricingItems && plugin.pricingItems.length > 0) {
            AJS.$('div.upm-plugin-pricing', details).append(buildPluginPricingTable(plugin.pricingItems)).removeClass('hidden');
        }

        AJS.$('div.upm-plugin-actions .toolbar-dropdown', details).dropDown('Standard', {alignment: 'right'});

        return details;
    }

    function buildPluginPricingTable(pricingItems) {
        buildPluginPricingTable.template = buildPluginPricingTable.template || AJS.$(AJS.$('#upm-plugin-pricing-template').html());
        var pricingTemplate = buildPluginPricingTable.template.clone(),
            table = pricingTemplate.find('table tbody'),
            priceSelected = getSelectedUnitCount(pricingItems);

        for(var i=0, ii=pricingItems.length; i<ii; i++) {
            table.append(buildPluginPricingItem(pricingItems[i], pricingItems[i].unitCount == priceSelected));
        }

        return pricingTemplate;
    }

    function getSelectedUnitCount(pricingItems) {
        // check first if there's a similar user count on the pricing item and the host
        for(var i=0, ii=pricingItems.length; i<ii; i++) {
            if (hostLicense && (hostLicense.maximumNumberOfUsers == pricingItems[i].unitCount)) {
                return pricingItems[i].unitCount;
            }
        }

        // if not, return the next plugin pricing item after the host's max number of users
        for(var i=0, ii=pricingItems.length; i<ii; i++) {
            if (((hostLicense && hostLicense.maximumNumberOfUsers) || 0) < pricingItems[i].unitCount) {
                return pricingItems[i].unitCount;
            }
        }

        return -1;
    }

    function buildPluginPricingItem(pricingItem, highlight) {
        buildPluginPricingItem.template = buildPluginPricingItem.template || AJS.$(AJS.$('#upm-plugin-pricing-row-template').html());
        var item = buildPluginPricingItem.template.clone();

        AJS.$('td.description', item).text(pricingItem.description);
        AJS.$('td.commercial-purchase', item).text(getPricingDisplay(pricingItem.usdAmount, "commercialPurchase"));
        AJS.$('td.commercial-renewal', item).text(getPricingDisplay(pricingItem.usdAmount, "commercialRenewal"));
        AJS.$('td.academic-purchase', item).text(getPricingDisplay(pricingItem.usdAmount, "academicPurchase"));
        AJS.$('td.academic-renewal', item).text(getPricingDisplay(pricingItem.usdAmount, "academicRenewal"));

        if (highlight) {
            item.addClass('highlight-prices');
        }
        return item;
    }

    function getPricingDisplay(price, type) {
        if (isNaN(price) || price <= 0) {
            return '$0.00';
        }
        return '$' + Math.max(minimumPurchasePrice, price * (pricingMultipliers[type] || 1)).toFixed(2);
    }

    function buildOsgiBundleDetails(bundle) {
        buildOsgiBundleDetails.template = buildOsgiBundleDetails.template || AJS.$(AJS.$('#upm-osgi-bundle-details').html());
        var bundleNode = buildOsgiBundleDetails.template.clone(),
                metadataNode = AJS.$('dl.upm-osgi-bundle-metadata', bundleNode),
                unparsedHeadersNode = AJS.$('dl.upm-osgi-bundle-unparsed-headers', bundleNode),
                parsedHeadersNode = AJS.$('div.upm-osgi-bundle-parsed-headers', bundleNode),
                servicesRegisteredNode = AJS.$('div.upm-osgi-services-registered', bundleNode),
                servicesInUseNode = AJS.$('div.upm-osgi-services-in-use', bundleNode);
        metadataNode.append(AJS.$('<dt/>').text(AJS.params.upmTextOsgiBundleLocation));
        metadataNode.append(AJS.$('<dd/>').text(bundle.location || AJS.params.upmTextUnknown));
        AJS.$.each(bundle.unparsedHeaders, function(key, value) {
            unparsedHeadersNode.append(AJS.$('<dt/>').text(key));
            unparsedHeadersNode.append(AJS.$('<dd/>').text(value));
        });
        AJS.$.each(bundle.parsedHeaders, function(key, value) {
            parsedHeadersNode.append(buildOsgiParsedHeader(key, value));
        });
        buildOsgiServices(bundle.registeredServices, servicesRegisteredNode, crossReferenceServiceRegistered);
        buildOsgiServices(bundle.servicesInUse, servicesInUseNode, crossReferenceServiceInUse);
        return bundleNode;
    }

    function buildOsgiParsedHeader(name, clauses) {
        buildOsgiParsedHeader.template = buildOsgiParsedHeader.template || AJS.$(AJS.$('#upm-osgi-parsed-header').html());
        var headerNode = buildOsgiParsedHeader.template.clone(),
                nameNode = AJS.$('span.upm-osgi-parsed-header-name', headerNode),
                countNode = AJS.$('span.upm-count-osgi-parsed-header-entries', headerNode),
                clausesNode = AJS.$('div.upm-module-container', headerNode);
        nameNode.text(name);
        countEntries(clauses, countNode);
        AJS.$.each(clauses, function() {
            clausesNode.append(buildOsgiParsedHeaderClause(name, this));
        });
        return headerNode;
    }

    function buildOsgiParsedHeaderClause(name, clause) {
        buildOsgiParsedHeaderClause.template = buildOsgiParsedHeaderClause.template || AJS.$(AJS.$('#upm-plugin-module-template').html());
        var clauseNode = buildOsgiParsedHeaderClause.template.clone(),
                pathNode = AJS.$('h5.upm-module-name', clauseNode),
                parametersNode = AJS.$('p.upm-module-key', clauseNode),
                parameters = [],
                crossReferenceFn = osgiXrefFn[name];
        pathNode.text(clause.path);
        AJS.$.each(clause.parameters, function(key, value) {
            if (value.length > 64) {
                value = '[...]';
            }
            parameters.push(key + ': ' + value);
        });
        parametersNode.text(parameters.join(', '));
        crossReferenceFn && crossReferenceFn(clause, clauseNode);
        return clauseNode;
    }

    function crossReferenceOsgiImportPackageHeaderClause(clause, clauseNode) {
        if (clause.referencedPackage) {
            var descriptionNode = AJS.$('p.upm-module-description', clauseNode);
                    crossReference = buildOsgiBundleLink(clause.referencedPackage.exportingBundle);
            descriptionNode.html(AJS.format(AJS.params.upmOsgiProvidedBy, crossReference));
            clauseNode.addClass('upm-osgi-header-clause-resolved');
        } else {
            clauseNode.addClass(clause.parameters['resolution'] == 'optional' ? 'upm-osgi-header-clause-optional' : 'upm-osgi-header-clause-unresolved');
        }
    }

    function crossReferenceOsgiExportPackageHeaderClause(clause, clauseNode) {
        if (clause.referencedPackage) {
            var descriptionNode = AJS.$('p.upm-module-description', clauseNode),
                    crossReferences = [];
            AJS.$.each(clause.referencedPackage.importingBundles, function() {
                crossReferences.push(buildOsgiBundleLink(this));
            });
            if (crossReferences.length != 0) {
                descriptionNode.html(AJS.format(AJS.params.upmOsgiUsedBy, crossReferences.join(', ')));
                clauseNode.addClass('upm-osgi-header-clause-resolved');
            } else {
                clauseNode.addClass('upm-osgi-header-clause-optional');
            }
        } else {
            clauseNode.addClass('upm-osgi-header-clause-unresolved');
        }
    }

    function buildOsgiServices(services, container, crossReferenceFn) {
        var countNode = AJS.$('span.upm-count-osgi-services', container),
                servicesNode = AJS.$('div.upm-module-container', container);
        countEntries(services, countNode);
        if (services.length == 0) {
            container.addClass('hidden');
        }
        AJS.$.each(services, function() {
            servicesNode.append(buildOsgiService(this, crossReferenceFn));
        });
    }

    function buildOsgiService(service, crossReferenceFn) {
        buildOsgiService.template = buildOsgiService.template || AJS.$(AJS.$('#upm-plugin-module-template').html());
        var serviceNode = buildOsgiService.template.clone(),
                idNode = AJS.$('h5.upm-module-name', serviceNode),
                propertiesNode = AJS.$('p.upm-module-key', serviceNode);
        idNode.text(AJS.format(AJS.params.upmOsgiService, service.id));
        propertiesNode.text(service.objectClasses.join(', '));
        crossReferenceFn && crossReferenceFn(service, serviceNode);
        return serviceNode;
    }

    function crossReferenceServiceRegistered(service, serviceNode) {
        var descriptionNode = AJS.$('p.upm-module-description', serviceNode),
                usingBundles = service.usingBundles,
                crossReferences = [];
        AJS.$.each(usingBundles, function() {
            crossReferences.push(buildOsgiBundleLink(this));
        });
        if (crossReferences.length != 0) {
            descriptionNode.html(AJS.format(AJS.params.upmOsgiUsedBy, crossReferences.join(', ')));
        }
    }

    function crossReferenceServiceInUse(service, serviceNode) {
        var descriptionNode = AJS.$('p.upm-module-description', serviceNode),
                bundle = service.bundle,
                crossReference = buildOsgiBundleLink(bundle);
        descriptionNode.html(AJS.format(AJS.params.upmOsgiProvidedBy, crossReference));
    }

    function countEntries(entries, countNode) {
        countNode.text(AJS.format(
             entries.length == 1 ?
                 AJS.params.upmOsgiCountEntry :
                 AJS.params.upmOsgiCountEntries,
             entries.length));
     }

    function buildOsgiBundleLink(bundle) {
        buildOsgiBundleLink.template = buildOsgiBundleLink.template || AJS.$(AJS.$('#upm-osgi-bundle-xref-template').html());
        var containerNode = buildOsgiBundleLink.template.clone(),
                linkNode = AJS.$('a.upm-osgi-bundle-xref', containerNode),
                inputNode = AJS.$('input', containerNode);
        linkNode.text(bundle.name || bundle.symbolicName);
        inputNode.attr('value', bundle.symbolicName);
        return containerNode.html();
    }

    /**
     * Toggles the plugin license form
     * @method togglePluginLicenseEdit
     * @param {Event} e Event object
     */
    upm.togglePluginLicenseEdit = function(e) {
        var container = AJS.$(e.target).closest('div.upm-license-details');

        e.preventDefault();

        if (container.hasClass('edit-license')) {
            container.removeClass('edit-license');
        } else {
            container.addClass('edit-license');
            container.find('form.upm-license-form textarea').focus().select();
        }
    };

    /**
     * Shows the plugin's full license key (instead of the truncated version)
     * @method showFullPluginLicense
     * @param {Event} e Event object
     */
    function showFullPluginLicense(e) {
        var truncated = AJS.$(e.target);
        e.preventDefault();
        truncated.addClass('hidden');
        truncated.closest('dd').find('span.upm-plugin-license-raw').removeClass('hidden');
    }

    /**
     * Updates the plugin with the specified licensing details
     * @method updatePluginLicenseDetails
     * @param {HTMLElement} container License details element
     * @param {Object} licenseDetails Object containing licensing details
     */
    function updatePluginLicenseDetails(container, licenseDetails) {
        if (licenseDetails && licenseDetails.rawLicense) {
            var statusId = licenseDetails.error ?
                    ('#upmLicenseStatus_' + licenseDetails.error.toLowerCase())
                    : '#upmLicenseStatus_valid',
                statusDesc = AJS.$(statusId).val() || AJS.$('#upmLicenseStatus_unknown').val(),
                evaluationDesc = licenseDetails.evaluation ?
                    (AJS.params['upmLicenseStatus_evaluation'] + ' ') : '',
                licenseInfoDescKey = licenseDetails.maximumNumberOfUsers ?
                    ((licenseDetails.maximumNumberOfUsers == 1) ? 'upmTextLicenseInfoSingleUser' : 'upmTextLicenseInfoLimitedUsers')
                    : 'upmTextLicenseInfoUnlimitedUsers';
            
            container.removeClass('no-license');

            AJS.$('dd.upm-plugin-license-info', container).text(
                evaluationDesc + AJS.format(
                    AJS.params[licenseInfoDescKey],
                    licenseDetails.licenseType.toLowerCase(),
                    licenseDetails.maintenanceExpiryDateString,
                    licenseDetails.maximumNumberOfUsers)
                );

            AJS.$('dd.upm-plugin-license-status', container).text(statusDesc);

            AJS.$('a.upm-plugin-license-truncated', container)
                .text(licenseDetails.rawLicense.substr(0, 10) + '...')
                .attr('title', AJS.params['upmTextViewFullLicense'])
                .click(showFullPluginLicense);
            AJS.$('span.upm-plugin-license-raw', container).text(licenseDetails.rawLicense);
            AJS.$('textarea', container).val(licenseDetails.rawLicense);
        } else {
            container.addClass('no-license edit-license');
            AJS.$('form.upm-license-form', container).removeClass('hidden');
        }
    }

    /**
     * Creates and returns an element containing license details for a  plugin
     * @method buildPluginLicenseDetails
     * @param {Object} license Licensing details
     * @param {boolean} licenseReadOnly True if we can only display the license properties but not modify the license string
     * @param {String} licenseAdminUri URI of the plugin's license admin page (used only if licenseReadOnly is true)
     * @return {HTMLElement} License details element
     */
    function buildPluginLicenseDetails(license, licenseReadOnly, licenseAdminUri) {
        buildPluginLicenseDetails.template = buildPluginLicenseDetails.template || AJS.$(AJS.$('#upm-plugin-license-details').html());
        var details = buildPluginLicenseDetails.template.clone(),
            initialValue = (license && license.rawLicense) || '',
            $textarea,
            $update;

        updatePluginLicenseDetails(details, license);
        if (licenseReadOnly) {
            var licenseReadOnlyField = AJS.$('.upm-plugin-license-readonly dd', details)
                    .append(AJS.params['upmLicenseReadOnly']),
                licenseAdminLink = AJS.$('.plugin-license-admin-link', licenseReadOnlyField);
            if (licenseAdminUri) {
                licenseAdminLink.attr('href', licenseAdminUri);
            }
            else {
                licenseAdminLink.replaceWith(licenseAdminLink.html());
            }
            AJS.$('.upm-plugin-license-editable', details).remove();
        } else {
            AJS.$('.upm-plugin-license-readonly', details).remove();
            AJS.$('form.upm-license-form', details).submit(submitPluginLicense);

            $update = AJS.$('input.submit', details);

            // Disable the update button unless the value of the textarea changes
            $textarea = AJS.$('textarea', details).bind('keyup input propertychange', function() {
                // Level the playing field for line breaks. Replace all double line breaks (windows) with single (unix)
                if ($textarea.val().replace(/\r\n/g, '\n') === initialValue.replace(/\r\n/g, '\n') ) {
                    $update.attr('disabled', 'disabled');
                } else {
                    $update.removeAttr('disabled');
                }
            });
        }
        return details;
    }

    /**
     * Creates and returns an toolbar element containing the specified button type
     * @method buildMarketplaceButton
     * @param {String} type The type of button to build, eg 'upgrade'
     * @param {Object} plugin The plugin
     * @param {Function} callback The function to call when clicking the marketplace button, or submitting the url if unspecified
     * @return {HTMLElement} marketplace button element
     */
    function buildMarketplaceButton(type, plugin, callback) {
        buildMarketplaceButton.template = buildMarketplaceButton.template || AJS.$(AJS.$('#upm-plugin-button-form').html());

        var marketplace = buildMarketplaceButton.template.clone(),
            licenseDetails = plugin.licenseDetails,
            form = AJS.$('form', marketplace).attr('action', plugin.links[type] || '')[0],
            formElements = form.elements,
            $buttonContainer = AJS.$('.toolbar-item', marketplace).addClass('upm-plugin-button-' + type + '-item');

        // Modify the submit button to do what we want
        AJS.$('input.toolbar-trigger', marketplace)
            .val(AJS.params['upm.plugin.action.' + type])
            .attr('title', AJS.params['upm.plugin.action.' + type + '.title'] || '')
            .addClass('upm-plugin-button-' + type)
            .click(function(e) {
                // prevent form submit if button is disabled, or a custom callback is defined
                if ($buttonContainer.hasClass('disabled') || callback) {
                    e.preventDefault();
                }

                callback && callback(plugin);
            });

        formElements.callback.value = plugin.links['license-callback'];
        formElements.licensefieldname.value = 'license';
        
        if (hostLicense && hostLicense.supportEntitlementNumber) {
            formElements.parent_sen.value = hostLicense.supportEntitlementNumber;
        } else {
            form.removeChild(formElements.parent_sen); // host license might not have an SEN
        }

        if (licenseDetails && licenseDetails.organizationName) {
            formElements.organisation_name.value = licenseDetails.organizationName;
        } else if (hostLicense && hostLicense.organizationName) {
            formElements.organisation_name.value = hostLicense.organizationName;
        }

        if (licenseDetails && licenseDetails.supportEntitlementNumber) {
            formElements.addon_sen.value = licenseDetails.supportEntitlementNumber;
        } else {
            form.removeChild(formElements.addon_sen); // new licenses will not have this value
        }

        if (hostLicense && hostLicense.maximumNumberOfUsers) {
            formElements.users.value = hostLicense.maximumNumberOfUsers;
        } else {
            formElements.users.value = -1; // unlimited users
        }

        if (licenseDetails && licenseDetails.contactEmail) {
            formElements.owner.value = licenseDetails.contactEmail;
        } else {
            AJS.$('input[name="owner"]', marketplace).remove();
        }

        return marketplace;
    }

    /**
     * Checks to see if any marketplace (purchasing) buttons should be added to the plugin details
     * @method addMarketplaceButtons
     * @param {HTMLElement} container The element to add buttons to, typically the plugin details element
     * @param {Object} plugin The plugin
     */
    function addMarketplaceButtons(container, plugin) {
        var pluginActions = container.is('div.upm-plugin-actions') ? container : container.find('div.upm-plugin-actions'),
            toolbarGroup = AJS.$('<ul class="toolbar-group"></ul>'),
            newButtons = false;

        if (!container || !container.length) {
            return;
        }

        if (plugin.links['try']){
            toolbarGroup.append(buildMarketplaceButton('try', plugin));
            newButtons = true;
        }

        if (plugin.links['new']){
            toolbarGroup.append(buildMarketplaceButton('new', plugin));
            newButtons = true;
        }

        if (plugin.links['upgrade']) {
            toolbarGroup.append(buildMarketplaceButton('upgrade', plugin));
            newButtons = true;
        }

        if (plugin.links['renew']) {
            toolbarGroup.append(buildMarketplaceButton('renew', plugin));
            newButtons = true;
        } else if (plugin.links['renew-requires-contact']) {
            toolbarGroup.append(buildMarketplaceButton('renew', plugin, function(plugin) {
                showInfoDialog(AJS.params.upmTextLicenseFollowupTitle,
                               AJS.format(AJS.params.upmTextLicenseFollowupMessage,
                               plugin.links['renew-requires-contact'], plugin.vendor.name, plugin.name, plugin.licenseDetails.licenseType.toLowerCase()), true);
            }));
            newButtons = true;
        }

        if (newButtons) {
            pluginActions.append(toolbarGroup);
        }
    }

    /**
     * Build the toolbar buttons for a plugin
     * @method buildPluginToolbarButtons
     * @return {HTMLElement} Plugin details element
     * @return {HTMLElement} details The details element of the plugin
     * @return {String} type The plugin type (manage, update, etc)
     */
    function buildPluginToolbarButtons(plugin, details) {
        buildPluginToolbarButtons.template = buildPluginToolbarButtons.template
            || AJS.$(AJS.$('#upm-plugin-details-buttons').html());
        
        var buttons = buildPluginToolbarButtons.template.clone(),
            configureLink = AJS.$('a.upm-configure-link', buttons),
            updateButton = AJS.$('a.upm-update', buttons),
            toolbarGroup;

        // If the plugin is configurable and enabled...
        if (plugin.configureUrl && plugin.enabled) {
            // Disable configure button + tooltip if license is invalid
            if (plugin.licenseDetails && !plugin.licenseDetails.valid) {
                configureLink.attr('title', AJS.params['upm.plugin.license.noconfigure']);
                configureLink.closest('li.toolbar-item').addClass('disabled');
            } else {
                configureLink.attr('href', '../..' + plugin.configureUrl);
            }
        } else {
            configureLink.closest('li.toolbar-item').remove();
        }

        // Switch the button from disabled to enabled if that's the state
        if (!plugin.enabled) {
            AJS.$('a.upm-disable', buttons).attr('id', 'upm-enable-' + plugin.key);
        } else {
            AJS.$('a.upm-disable', buttons).attr('id', 'upm-disable-' + plugin.key);
        }

        // If the plugin is system, or there's no delete link, don't let the user uninstall it
        AJS.$('a.upm-uninstall', buttons).attr('id', 'upm-uninstall-' + plugin.key);
        if (!plugin.userInstalled || !plugin.links['delete']) {
            AJS.$('a.upm-uninstall', buttons).closest('li.toolbar-item').remove();
        }

        // Hide the disabled button if we can't modify this plugin, or it's required
        if (!plugin.links['modify'] || !plugin.optional || isUpm(plugin.key)) {
            AJS.$('a.upm-disable', buttons).closest('li.toolbar-item').remove();
        } else if (!plugin.enabled) {
            AJS.$('a.upm-disable', buttons).removeClass('upm-disable').addClass('upm-enable').text(AJS.params.upmTextEnable);
        }

        // Can this plugin have marketplace buttons?
        if (isLicenseUpdatable(plugin) && !isRestartRequiredForLicensing(plugin)) {
            addMarketplaceButtons(buttons, plugin);
        }

        // Updatable plugin buttons
        if (plugin.pacResponse && plugin.pacResponse.update) {
            updateButton.attr('id', 'upm-update-' + plugin.key);

            // Change install to download if plugin isn't installable
            if (!isPluginInstallable(plugin.pacResponse.update)) {
                updateButton
                    .removeClass('upm-update')
                    .addClass('upm-download')
                    .text(AJS.params['upmTextDownload']);
            }

            if (!plugin.pacResponse.update.links.binary && plugin.pacResponse.update.deployable) {
                toolbarGroup = updateButton.closest('ul.toolbar-group');
                updateButton.closest('li.toolbar-item').remove();
                if (!toolbarGroup.find('li.toolbar-item').length) {
                    toolbarGroup.remove();
                }
            }

            AJS.$('div.upm-plugin-actions .toolbar-dropdown', buttons)
                .dropDown('Standard', {alignment: 'right'});
        } else {
            toolbarGroup = updateButton.closest('ul.toolbar-group');
            updateButton.closest('li.toolbar-item').remove();
            if (!toolbarGroup.find('li.toolbar-item').length) {
                toolbarGroup.remove();
            }
        }

        buttons.appendTo(details.find('div.action-buttons'));
    }

    /**
     * Creates and returns an html element with plugin details for the "Manage" tab
     * @method buildManageDetails
     * @param {Object} plugin Object containing the plugin detail information
     * @return {HTMLElement} Plugin details element
     */
    function buildManageDetails(plugin) {
        buildManageDetails.template = buildManageDetails.template || AJS.$(AJS.$('#upm-plugin-details-manage').html());
        var details = buildManageDetails.template.clone(),
            title = AJS.$('span.upm-plugin-name', details),
            pacResponse = plugin.pacResponse,
            pluginUpdate = pacResponse && pacResponse.update,
            pricing = pacResponse && pacResponse.pricingItems;

        // plugin version, with link to release notes (if available)
        addLinkOrText({
            text: plugin.version,
            link: plugin.releaseNotesUrl,
            title: AJS.params['upm.plugin.release.notes.title'],
            linkClass: 'upm-plugin-release-notes-link'
        }, AJS.$('span.upm-plugin-installed-version', details));

        if (plugin.restartState) {
            displayRestartMessage(details, plugin.restartState);
        }

        buildPluginToolbarButtons(plugin, details);

        // Update information
        if (pluginUpdate) {
            title.text(plugin.name);

            // set plugin name, and make call to get plugin details link from PAC (if available)
            setPacDetailsLink(title, pacResponse);

            if (!pluginUpdate.licenseCompatible) {
                var action;
                if (AJS.$('input.upm-plugin-button-renew', details).length) {
                    action = 'renew';
                } else {
                    action = 'buy';
                }

                upm.displayMessage(details, {
                    body: AJS.params['upm.plugin.license.cannot.update.before.' + action],
                    type: 'info'
                });

                AJS.$('li.upm-update-item', details).closest('li.toolbar-item').addClass('disabled');
            }

            AJS.$('dd.upm-plugin-version', details).text(pluginUpdate.version);
            AJS.$('dd.upm-plugin-key', details).text(plugin.key || AJS.params.upmTextUnknown);
            AJS.$('dd.upm-plugin-system-version', details).text(pluginUpdate.pluginSystemVersion || AJS.params.upmTextUnknown);
            AJS.$('dd.upm-plugin-license', details).text(pluginUpdate.license || AJS.params.upmTextUnknown);
        } else {
            if (pacResponse) {
                setPacDetailsLink(AJS.$('span.upm-plugin-name', details).text(plugin.name), pacResponse);
            } else {
                // set plugin name, and make call to get plugin details link from PAC (if available)
                fetchPacPluginDetailsLink(AJS.$('span.upm-plugin-name', details).text(plugin.name), plugin);
            }

            AJS.$('dl.upm-update-details', details).addClass('hidden');
        }

        if (pricing && pricing.length > 0 && hasMarketplaceButton(plugin)) {
            AJS.$('div.upm-plugin-pricing', details).append(buildPluginPricingTable(pricing)).removeClass('hidden');
        }

        if (isLicenseUpdatable(plugin)) {
            if (isRestartRequiredForLicensing(plugin)) {
                upm.displayMessage(details, {
                    body: AJS.params['upm.plugin.license.notaware'],
                    type: 'info'
                });
            } else {
                buildPluginLicenseDetails(plugin.licenseDetails, plugin.licenseReadOnly,
                        plugin.enabled ? plugin.licenseAdminUri : null)
                    .appendTo(details.find('div.upm-plugin-license-container'));
            }
        }

        if (plugin.pacResponse && plugin.pacResponse.update) {
            AJS.$('input.upm-plugin-binary', details).val(plugin.pacResponse.update.links.binary);
            AJS.$('input.upm-plugin-homepage', details).val(plugin.pacResponse.update.links.details);
        }

        if (plugin.links['plugin-logo']) {
            AJS.$('img.upm-plugin-logo', details).attr('src', plugin.links['plugin-logo']);
        }

        // vendor name, with link to vendor url if it exists
        addLinkOrText({
            text: plugin.vendor && plugin.vendor.name && unescapeHTMLEntities(AJS.format(AJS.params['upm.plugin.developer.by'], plugin.vendor.name)),
            link: plugin.vendor && plugin.vendor.link
        }, AJS.$('h4.upm-plugin-developer', details));

        AJS.$('dd.upm-plugin-key', details).text(plugin.key || AJS.params.upmTextUnknown);

        AJS.$('div.upm-plugin-actions .toolbar-dropdown', details).dropDown("Standard", {alignment: "right"});

        return details;
    }

    function hasMarketplaceButton(plugin) {
        var links = plugin.links;
        return links['try'] || links['new'] || links['upgrade'] || links['renew'] || links['renew-requires-contact'];
    }

    /**
     * Safely turn html entities in text into regular characters
     * @method unescapeHTMLEntities
     * @param {String} text The text with html entities
     * @return {String} The text with html entities converted to their unicode equivalents, eg "&amp;" > " "
     */
    function unescapeHTMLEntities(text) {
        return AJS.$('<div></div>').html(text).text();
    }

    /**
     * Returns true if the plugin is licensed and can be updated by UPM, false if not.
     * @param {Object} plugin Object containing the plugin detail representation
     * @return {Boolean} true if the plugin is licensed and can be updated by UPM, false if not.
     */
    function isLicenseUpdatable(plugin) {
        return plugin.links && plugin.links['update-license'];
    }

    /**
     * Returns true if UPM needs to be restarted in order to update this plugin's license, false if not.
     * @param {Object} plugin Object containing the plugin detail representation
     * @return {Boolean} true if UPM needs to be restarted in order to update this plugin's license, false if not.
     */
    function isRestartRequiredForLicensing(plugin) {
        return !AJS.params['upm.licensing.aware'] && isLegacyAtlassianLicensingPlugin(plugin.key);
    }

    /**
     * Returns true if the given plugin key belongs to an Atlassian plugin with legacy licensing, false if not
     * @param {String} pluginKey the plugin key
     */
    function isLegacyAtlassianLicensingPlugin(pluginKey) {
        return pluginKey == 'com.atlassian.bonfire.plugin'
            || pluginKey == 'com.pyxis.greenhopper.jira'
            || pluginKey == 'com.atlassian.confluence.extra.team-calendars';
    }

    /**
     * Returns true if this plugin key represents UPM, false if not.
     * @param {String} pluginKey the plugin key
     * @return {Boolean} true if this plugin represents UPM, false if not.
     */
    function isUpm(pluginKey) {
        return pluginKey == 'com.atlassian.upm.atlassian-universal-plugin-manager-plugin';
    }

    /**
     * Returns true if this plugin key represents the UPM self update stub, false if not.
     * @param {String} pluginKey the plugin key
     * @return {Boolean} true if this plugin represents the UPM self update stub, false if not.
     */
    function isUpmStub(pluginKey) {
        return pluginKey == 'com.atlassian.upm.atlassian-universal-plugin-manager-selfupdate-plugin';
    }

    /**
     * Creates and returns an html element with plugin details for the "Compatibility check" tab
     * @method buildCompatibilityDetails
     * @param {Object} plugin Object containing the plugin detail information
     * @return {HTMLElement} Plugin details element
     */
    function buildCompatibilityDetails(plugin) {
        buildCompatibilityDetails.template = buildCompatibilityDetails.template || AJS.$(AJS.$('#upm-plugin-details-compatibility').html());
        var details = buildCompatibilityDetails.template.clone();
        AJS.$('dd.upm-plugin-key', details).text(plugin.key || AJS.params.upmTextUnknown);

        // plugin name, with link to plugin details on PAC (if it exists)
        addLinkOrText({
            text: plugin.name,
            link: plugin.links.details,
            title: AJS.params['upm.plugin.details.title'],
            linkClass: 'upm-plugin-details-link'
        }, AJS.$('span.upm-plugin-name', details));
        if (plugin.installedVersion) {
            AJS.$('div.upm-plugin-update-version', details).removeClass('hidden');
            AJS.$('span.upm-plugin-version', details).text(plugin.installedVersion);
            AJS.$('span.upm-plugin-update-version', details).text(plugin.version || AJS.params.upmTextUnknown);
        } else {
            AJS.$('span.upm-plugin-version', details).text(plugin.version);
        }

        if (plugin.links['plugin-logo']) {
            AJS.$('img.upm-plugin-logo', details)
                .attr('src', plugin.links['plugin-logo']);
        }

        if (plugin.links && plugin.links.binary) {
            AJS.$('input.upm-plugin-binary', details).val(plugin.links.binary);
        } else {
            AJS.$('a.upm-update', details).closest('li.toolbar-item').remove();
        }

        // vendor name, with link to vendor url if it exists
        addLinkOrText({
            text: plugin.vendor && plugin.vendor.name && unescapeHTMLEntities(AJS.format(AJS.params['upm.plugin.developer.by'], plugin.vendor.name)),
            link: plugin.vendor && plugin.vendor.link
        }, AJS.$('h4.upm-plugin-developer', details));

        if (plugin.restartState) {
            AJS.$('a.upm-update', details).closest('li.toolbar-item').remove();
            displayRestartMessage(details, plugin.restartState);
        }

        // Remove the disable button if this plugin is not modifiable, or already disabled
        if (!plugin.links['modify'] || !plugin.enabled) {
            AJS.$('a.upm-disable', details).closest('li.toolbar-item').remove();
        }

        if (plugin.links && plugin.links.details) {
            AJS.$('input.upm-plugin-homepage', details).val(plugin.links.details);
        }
        if (plugin.deployable === false) {
            AJS.$('a.upm-download', details).removeClass('hidden');
        }
        return details;
    }

    /**
     * Displays a restart message at the top of the given plugin details
     * @method displayRestartMessage
     * @param {HTMLElement} details The element insert the message into
     * @param {String} state The state (remove/install/update) to show the message for
     * @return {String} Message element
     */
    function displayRestartMessage(details, state) {
        var message;
        if (state == 'remove') {
            message = AJS.params.upmTextUninstallNeedsRestart;
        } else if (state == 'install') {
            message = AJS.params.upmTextInstallNeedsRestart;
        // Temporary solution until back end plugin representation restartState can return "update" instead of "upgrade"
        } else if (state == 'upgrade') {
            message = AJS.params.upmTextUpdateNeedsRestart;
        }
        if (message) {
            details.addClass('upm-plugin-requires-restart');
            AJS.$('div.upm-requires-restart-message', details).text(message);
        }
        return message;
    }

    /**
     * Display a modal dialog telling the user to refresh the page
     * @method displayRefreshMessage
     */
    function displayRefreshMessage() {
        var dialogContent = AJS.$('<div></div>'),
                popup = new AJS.Dialog({
                    width: 500,
                    height: 200,
                    id: 'upm-refresh-dialog'
                }),
                refresh = function(e) {
                    e && e.preventDefault && e.preventDefault();
                    window.location.href = window.location.pathname;
                };
        
        dialogContent.html(AJS.params.upmTextRequiresRefresh);
        dialogContent.find('a')
                .attr('href', '#')
                .click(refresh);

        popup.addHeader(AJS.params.upmTextRequiresRefreshHeader);
        popup.addPanel("All", dialogContent);
        popup.addButton(AJS.params.upmTextRefresh, refresh);

        popup.show();
    }

    /**
     * Displays an informational message at the top of a given element
     * @method displayMessage
     * @param {HTMLElement} container The element to prepend the message to
     * @param {Object} options
     */
    upm.displayMessage = function(container, options) {
        upm.displayMessage.counter = upm.displayMessage.counter ? upm.displayMessage.counter + 1 : 1;

        var target, message, timeOut,
            body = AJS.$('<div class="upm-message-text"></div>').html(upm.html_sanitize(options.body, htmlSanitizerUrlPolicy)),
            uid = 'upm-message-anchor-' + upm.displayMessage.counter,
            fade = options.fadeOut;

        options.closeable = (options.closeable !== false);
        options.type = (options.type && AJS.messages[options.type]) ? options.type : 'info';

        target = container.find('div.upm-message-container');
        if (!target.length) {
            target = container;
        }

        if (options.className) {
            body.addClass(options.className);
        }

        // Don't auto-remove the upm self update message if it's visible, but remove other closeable ones
        AJS.$('div.aui-message.closeable', target).filter(':not(:has(#upm-self-update-msg))').remove();

        AJS.messages[options.type](target, {
            closeable: options.closeable,
            shadowed: false,
            body: AJS.$('<div></div>').html(body)
                // Hack to make this banner targetable
                .append(AJS.$('<span></span>').attr('id', uid)).html()
        });

        message = AJS.$('#' + uid).parent();

        // Fade out this banner after n seconds?
        if (fade) {
            timeOut = setTimeout(function() {
                message.fadeOut('slow', function() {
                    message.trigger('messageClose');
                });
            }, (fade === true ? defaultSecondsBeforeMessageFadeout : fade)  * 1000);

            // If user clicks to close banner, cancel fade out
            message.bind('messageClose', function() {
                clearTimeout(timeOut);
            });
        }

        return message;
    };

    /**
     * From a click on skip this version text, set a cookie to hide the banner until the next UPM version
     * @method skipUpmUpdate
     * @param {Event} e The click event
     */
    function skipUpmUpdate(e) {
        e.preventDefault();

        upm.showConfirmDialog(AJS.format(AJS.params.upmSkipUpdateConfirm, upm.higherVersion), function() {
            // By default AJS.Cookie saves for 365 days, which should be fine
            AJS.Cookie.save('upm.selfUpdate.skip', upm.higherVersion);
            AJS.$(e.target).closest('div.aui-message').remove();
        });
    }

    /**
     * Remove self update message and set a cookie so it won't be shown for several days
     * @method dismissUpmUpdatableMessage
     * @param {Event} e Event object
     */
    function dismissUpmUpdatableMessage(e) {
        var target = AJS.$(e.target),
            defaultDaysToDismiss = 3;

        e.preventDefault();

        AJS.Cookie.save('upm.selfUpdate.dismiss', 'true', defaultDaysToDismiss);
        target.closest('div.aui-message').remove();
    }

    /**
     * Display a banner showing that the UPM is updatable
     * @method displayUpmUpdatableMessage
     */
    function displayUpmUpdatableMessage() {
        container = upm.$messageContainer;
        displayUpmUpdatableMessage.template = displayUpmUpdatableMessage.template || AJS.$('#upm-update-available-template').html();

        // don't show banner if it already exists, or if there's an existing dismiss cookie, or if we're in safe mode
        // Don't show in safe mode
        if (safeMode
                // don't show banner if it already exists,
                || AJS.$('#upm-self-update-msg').length
                // Or if they have dismissed the banner
                || AJS.Cookie.read('upm.selfUpdate.dismiss', false)
                // Or if they have skipped this version
                || AJS.Cookie.read('upm.selfUpdate.skip') === upm.higherVersion) {
       
            return;
        }

        AJS.messages.info(container, {
            closeable: false,
            shadowed: false,
            body: displayUpmUpdatableMessage.template
        });
        
        AJS.$('#upm-remind-later').click(dismissUpmUpdatableMessage);
        AJS.$('#upm-skip-version').click(skipUpmUpdate);

        AJS.Cookie.erase('upm.selfUpdate.dismiss');
        AJS.Cookie.erase('upm.selfUpdate.skip');
    }
    
    /**
     * Performs any actions needed based on the response eg. reload the page if the status is unauthorized
     * @param container
     * @param {XMLHttpRequest} request
     * @param pluginName
     */
    upm.handleAjaxError = function(container, request, pluginName) {
        var msg, response, status, subcode;

        try {
            response = upm.json.parse(request.responseText);
            subcode = response && response.subCode;
        } catch (e) {
            AJS.log('Failed to parse response text: ' + e);
            subcode = (request.statusText === 'timeout' && 'ajaxTimeout') || (request.statusText === 'error' && 'ajaxCommsError')
                      || 'ajaxServerError';
            response = {subCode: subcode};
        }

        status = request.status || response['status-code'];
        if (upm.reloadIfUnauthorizedStatus(status)) {
            msg = AJS.params['upm.error.unauthorized'];
            upm.displayMessage(container, {
                body: msg,
                type: 'error',
                closeable: false
            });
        } else if (!upm.reloadIfWebSudoError(subcode)) {
            displayErrorMessage(container, response, pluginName);
        }
    };

    /**
     * Displays an error message at the top of the specified element
     * @method displayErrorMessage
     * @param {HTMLElement} container The element to prepend the message to
     * @param {String|Object} response Message content
     * @param {String} pluginName Display name of the plugin
     */
    function displayErrorMessage(container, response, pluginName) {
        var msg, subcode, param;

        container = (container && container.is(':visible')) ? container : upm.$messageContainer;
        if (response) {
            if (typeof response == "string") {
                try {
                    response = upm.json.parse(response);
                } catch (e) {
                    AJS.log('Failed to parse response text: ' + e);
                }
            }
            msg = response.errorMessage || response.message || (response.status && response.status.errorMessage);
            subcode = response.subCode || (response.status && response.status.subCode) || (response.details && response.details.error.subCode);
            // Special case for descriptive error messages on enabling plugin licenses
            if (subcode && AJS.params[subcode]) {
                
                // If we are attempting to enable a plugin, and the response is the generic failed message...
                if (container.hasClass('upm-details') && subcode == 'upm.plugin.error.failed.to.enable'
                        // If this plugin is licensable...
                        && container.find('div.upm-plugin-license-container').length) {

                    // Is it a blank license? (will be undefined with no textarea)
                    if (container.find('textarea').val() === '') {
                        param = 'upm.plugin.error.failed.to.enable.no.license.entered';

                    // Does this plugin contain a known expired license?
                    } else if (container.find('dd.upm-plugin-license-status').text() == AJS.params.upmLicenseStatus_expired) {
                        param = 'upm.plugin.error.failed.to.enable.has.expired.license';

                    // Otherwise there is a saved license, or something in the text box
                    } else {
                        param = 'upm.plugin.error.failed.to.enable.has.license';
                    }
                } else {
                    param = subcode;
                }

                // if a subcode was provided, use it to get an i18n message
                msg = AJS.format(AJS.params[param], htmlEncode(pluginName || response.pluginName || (response.status && response.status.source) || AJS.params.upmTextUnknownPlugin),
                                 htmlEncode(response.moduleName || AJS.params.upmTextUnknownPluginModule));
            } else if (!msg || msg.match(/^[0-9][0-9][0-9]$/)) {
                // if there is no msg or the msg is just an error code, return an "unexpected" error
                msg = AJS.params['upm.plugin.error.unexpected.error'];
            }
        } else {
            msg = AJS.params['upm.plugin.error.unexpected.error'];
        }
        upm.displayMessage(container, {
            body: msg,
            type: 'error'
        });
    }

    /**
     * Checks an error message to see if it's caused by lack of websudo authentication, and if so reloads the page to
     * trigger the login challenge
     * @method reloadIfWebSudoError
     * @param {String} msg The returned error message
     * @return {Boolean} whether or not the page is reloaded
     */
    upm.reloadIfWebSudoError = function(subcode) {
        // This is a bit crap but I don't think we want to wrap the error just to have a nicer error code
        if (subcode === "upm.websudo.error") {
            // if there is a webSudo error then we need to redirect the UI to the UPM Servlet so the login challenge will occur
            window.location.reload(true);
            return true;
        }
        return false;
    };

    /**
     * Checks if the message from the server was a 401-Unauthorized
     *  if so reload the page because either the logged in user is not allowed to perform this action or their session has timed out
     *  and hence they will be asked to login again first.
     * @param {Number} status - the status code for the message
     * @return {Boolean} wheher or not the page is reloaded
     */
    upm.reloadIfUnauthorizedStatus = function(status) {
        if (status === 401) {
            window.location.reload(true);
            return true;
        }
        return false;
    };

    /**
     * Removes an informational message -- triggered by clicking message "close" link
     * @method removeMessage
     * @param {HTMLElement} element The element to remove the messages from
     */
    function removeMessage(element) {
        var message = element.closest('div.aui-message.closeable');
        if (message.length == 0) {
            message = element.closest('div.upm-plugin').find('div.aui-message.closeable');
        }
        message.trigger('close').remove();
    }

    /**
     * Cancels a pending action that is waiting for restart to take effect
     * @method cancelActionRequiringRestart
     * @param {Event} e Event object
     */
    upm.cancelActionRequiringRestart = function(e) {
        var target = AJS.$(e.target),
                li = target.closest('li'),
                uri = li.find('.upm-requires-restart-cancel-uri').val(),
                data = li.data('representation');
        e.preventDefault();
        target.blur();
        if (!hasPendingTasks()) {
            AJS.$.ajax({
                type: 'DELETE',
                url: uri,
                dataType: 'json',
                contentType: upm.contentTypes['requires-restart'],
                data: upm.json.stringify(data),
                success: function(response) {
                    var hash = upm.getLocationHash();
                    li.remove();
                    upm.displayMessage(upm.$messageContainer, {
                        body: AJS.format(AJS.params['upm.messages.requiresRestart.cancel.' + data.action], htmlEncode(data.name)),
                        type: 'info'
                    });
                    if (AJS.$('#upm-requires-restart-list li').length == 0) {
                        upm.$container.removeClass('requires-restart');
                    }
                    if (hash.tab !== 'update') {
                        // reload the current tab (but not if it's the update tab, since we're reloading that anyway)
                        upm.loadTab(hash.tab);
                    }
                    loadManageTab();
                },
                error: function(request) {
                    // UPM-986 Lets update the changes since there was some error with what was there.
                    checkForChangesRequiringRestart();
                    upm.handleAjaxError(upm.$messageContainer, request,  data.name);
                }
            });
        }
    };

    /**
     * Hides or shows the requires restart details and changes the link text accordingly
     * @method toggleRequiresRestartDetails
     */
    upm.toggleRequiresRestartDetails = function(link) {
        var list = AJS.$('#upm-requires-restart-list');

        if (list.hasClass('hidden')) {
            link.text(AJS.params.upmTextRequiresRestartShow);
            list.removeClass('hidden');
        } else {
            link.text(AJS.params.upmTextRequiresRestartHide);
            list.addClass('hidden');
        }
    };

    /**
     * Removes a plugin once it is collapsed
     * @method removeOnCollapse
     * @param {Event} e Event object
     * @param {Function} callbackFn Function to run after plugin removal
     */
    function removeOnCollapse(e, callbackFn) {
        var plugin = AJS.$(e.target).closest('div.upm-plugin'),
            pluginList = plugin.closest('div.upm-plugin-list'),
            isLastPlugin = pluginList.find('div.upm-plugin').length == 1,
            delay = 500,
            removalCallback = function() {
                plugin.remove();
                if (isLastPlugin) {
                    pluginList.replaceWith(buildEmptyPluginListForInstalling());
                }
                callbackFn && callbackFn();
            };
        if (plugin.hasClass('to-remove')) {
            if (plugin.fadeOut) {
                plugin.fadeOut(delay, removalCallback);
            } else {
                setTimeout(removalCallback, delay);
            }
        }
    }

    /**
     * Creates a unique hash from a string that is suitable for use in an id attribute
     * @method createHash
     * @param {String} input The text to create a hash from
     * @param {String} type (optional) If input may not be unique, this string can be used to differentiate
     */
    function createHash(input, type) {
        // Jenkins hash function. Updating this requires updating Ids#createHash.
        // Assumes ASCII input.
        var hash = 0;
        if (input) {
            if (type) {
                input += '-' + type;
            }
            for (var i = 0, len = input.length; i < len; i++) {
                hash += input.charCodeAt(i);
                hash += hash << 10;
                hash ^= hash >>> 6;
            }
            hash += hash << 3;
            hash ^= hash >>> 11;
            hash += hash << 15;
            return hash >>> 0;
        } else {
            return '';
        }
    }

    /**
     * Automatically updates height of dialog panels, to contain content without the need for scroll bars
     * NOTE: This is a wholesale ripoff of the AJS.Dialog.prototype.updateHeight function in AJS, but with some
     * additional padding tacked on to the calculated height to fix a problem in JIRA where the button panel was
     * getting cut off
     * @method updateDialogHeight
     * @param {AJS.Dialog} dialog The dialog object in question
     */
    function updateDialogHeight(dialog) {
        var height = 0;
        for (var i=0; dialog.getPanel(i); i++) {
            if (dialog.getPanel(i).body.css({height: "auto", display: "block"}).outerHeight() > height) {
                height = dialog.getPanel(i).body.outerHeight();
            }
            if (i !== dialog.page[dialog.curpage].curtab) {
                dialog.getPanel(i).body.css({display:"none"});
            }
        }
        for (i=0; dialog.getPanel(i); i++) {
            dialog.getPanel(i).body.css({height: height || dialog.height});
        }
        dialog.page[0].menu.height(height);
        dialog.height = height + 102;
        dialog.popup.changeSize(undefined, height + 102);
    }

    /**
     * Ensures that when a confirm dialog is displayed, highlight the first 'tabable' element on the page 
     * @method focusItem
     * @return the dialog element
     */
    function focusItem(item) {
        var hasFocus = false, 
            theChildren = item.children();

        if (item[0].tabIndex >= 0 && item.is(":visible")) {
            item.focus();
            hasFocus = true;
            return hasFocus;
        }

        for(var i=0, ii=theChildren.length; i<ii; i++) {
            hasFocus = focusItem(jQuery(theChildren[i]));
            if (hasFocus) {
                break;
            }
        }

        return hasFocus;
    }

    /**
     * Takes an AJS dialog item, wraps it in jQuery fluffyness and thow it to the lio...err focusItem function 
     * @method focusDialog
     * @return the dialog element
     */
    function focusDialog(dialog) {
        focusItem(AJS.$("#" + dialog.id));
        return dialog;
    }

    /**
     * Creates a confirmation dialog that fires a callback if accepted, does nothing if cancelled
     * @method createConfirmDialog
     * @return {Dialog} The dialog element
     */
    function createConfirmDialog() {
        var dialog = new AJS.Dialog(500, 300, 'upm-confirm-dialog');
        
        dialog.addPanel("", "panel1");
        dialog.addHeader(AJS.params.upmTextConfirmHeader);
            
        dialog.addButton(AJS.params.upmTextConfirmContinue, function(popup) {
            popup.callbackFn && popup.callbackFn.apply(this, popup.callbackFn.params || []);
            popup.hide();
        });

        dialog.addButton(AJS.params.upmTextConfirmCancel, function(popup) {
            popup.hide();
        });

        return dialog;
    }

    /**
     * Prompts the user to confirm an action before proceeding
     * @method showConfirmDialog
     * @param {String} text The text to display to the user
     * @param {Function} callbackFn The function to run if the user accepts
     * @param {Array} params (optional) The parameters to pass through to the callback function
     */
    upm.showConfirmDialog = function(text, callbackFn, params) {
        var dialog = upm.showConfirmDialog.dialog = upm.showConfirmDialog.dialog || createConfirmDialog(text);
        dialog.getCurrentPanel().html(text);
        dialog.callbackFn = callbackFn;
        dialog.callbackFn.params = params;
        dialog.show();
        updateDialogHeight(dialog);
        // Show/hide dance required to get dialog and shadow to be the correct size.
        // Fixing AJS-625 should make this unnecessary.
        // @aui-override
        // @see UPM-1091
        // @see AJS-625
        dialog.hide();
        dialog.show();
        // @aui-override-end

        focusDialog(dialog);
    };

    /**
     * Toggles the system plugin list on the "Manage" tab
     * @method toggleSystemPlugins
     */
    upm.toggleSystemPlugins = function() {
        AJS.$('#upm-system-plugins').toggle();
        AJS.$('#upm-manage-hide-system').toggle();
        AJS.$('#upm-manage-show-system').toggle();
    };

    /**
     * Toggles the plugin pricing details on the "Manage" or "Install" tab
     * @method togglePluginPricing
     */
    upm.togglePluginPricing = function(e) {
        var element = AJS.$(e.target),
            plugin = element.hasClass('upm-plugin') ? element : element.closest('div.upm-plugin');
        AJS.$('div.upm-pricing-container', plugin).toggle();
        AJS.$('a.upm-pricing-show', plugin).toggle();
        AJS.$('a.upm-pricing-hide', plugin).toggle();
    };

    /**
     * Checks to see if a button is disabled and stops event handler propogation if it is
     * @method checkButtonDisabledState
     * @param {Event} e Event object
     * @return {Boolean} Returns false if the button is disabled
     */
    upm.checkButtonDisabledState = function(e) {
        var el = AJS.$(this);
        if (el.closest('li.toolbar-item').hasClass('disabled') || el.attr('disabled')) {
            e.stopImmediatePropagation();
            return false;
        }
        return true;
    };

    upm.bind = function(eventName, fn) {
        $binder.bind(eventName, fn);
    };

    upm.bindOnce = function(eventName, fn) {
        var once = function() {
            $binder.unbind(eventName, once);
            fn.apply(boundTo, arguments);
        }, boundTo = this;

        $binder.bind(eventName, once);
    };

    upm.trigger = function(eventName, args) {
        $binder.trigger(eventName, args);
    };

    upm.unbind = function(eventName, fn) {
        $binder.unbind(eventName, fn);
    };

    // We are on a UPM page, not just loading upm resources, so tell the world
    upm.isUpm = true;

    AJS.toInit(function() {
        upm.version = AJS.params.upmVersion;

        populateResources();

        // UPM-951: JIRA 4.2 runs isDirty() on all forms on page unload, and displays a confirm dialog if the form has changed
        originalIsDirtyFn = AJS.$.fn.isDirty;
        if (originalIsDirtyFn) {
            AJS.$.fn.isDirty = function() {
                if (AJS.$(this).hasClass('skip-dirty-check')) {
                    return false;
                }
                return originalIsDirtyFn.apply(this, arguments);
            };
        }

        upm.$messageContainer = AJS.$('#upm-messages');
        upm.$container = AJS.$('#upm-container');

        // UPM-1804 Special case for Jira >= 5.0 because it has some different color styles
        if (upm.isJira && upm.productVersion >= 5) {
            upm.$container.addClass('jira5');
        }
        
        AJS.$(window).trigger('upmready');
    });
})();
