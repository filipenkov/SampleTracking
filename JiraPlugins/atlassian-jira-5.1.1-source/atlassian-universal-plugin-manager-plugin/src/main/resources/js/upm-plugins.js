/*
 * This file drives the manage / install page and the update check page.
 */
(function($) {

    // Wait for UPM to load
    var upmReady = function() {

        // Stop waiting for UPM to load
        $(window).unbind('upmready', upmReady);

        var $plugins,
            modules,
            isIE = upm.browser.isIE,
            isIE7 = upm.browser.isIE7;
            // Find out which page we are on
            isUpdateCheck = !!$('#upm-panel-compatibility').length;

        // Functionality shared by the update check and plugins page

        // hover and click functions for plugin lists
        $plugins = $('div.upm-plugin-list.expandable div.upm-plugin', upm.$container);
        if (isIE7) {
            $plugins.live('mouseover', function() {
                $(this).addClass('hover');
            });
            $plugins.live('mouseout', function() {
                $(this).removeClass('hover');
            });
        }
        $plugins.find('div.upm-plugin-row').live('click', upm.pluginRowClick);

        // hover and click functions for plugin modules
        modules = $('div.upm-module', upm.$container);
        if (isIE7) {
            modules.live('mouseover', function() {
                $(this).addClass('hover');
            });
            modules.live('mouseout', function() {
                $(this).removeClass('hover');
            });
        }

        // Toggle viewing of plugin modules
        $('a.upm-module-toggle', upm.$container).live('click', function(e) {
            var target = $(e.target).blur();
            e.preventDefault();
            target.closest('div.upm-plugin-modules').toggleClass('expanded');
        });

        // If toolbar buttons have disabled class, we want to kill the event
        $('a.toolbar-trigger', upm.$container).live('click', upm.checkButtonDisabledState);

        // UPM-930: IE lets you click on buttons even if they're disabled
        if (isIE) {
            $('button', upm.$container).live('click', upm.checkButtonDisabledState);
        }

        // plugin download button
        $('a.upm-download', upm.$container).live('click', upm.showDownloadDialog);

        // plugin disable button
        $('a.upm-disable', upm.$container).live('click', upm.disablePlugin);

        // plugin enable button
        $('a.upm-enable', upm.$container).live('click', upm.enablePlugin);

        $('a.upm-plugin-license-edit', upm.$container).live('click', upm.togglePluginLicenseEdit);
        
        // plugin cancel entering license button
        $('a.upm-license-cancel', upm.$container).live('click', upm.togglePluginLicenseEdit);

        // module disable button
        $('a.upm-module-disable', upm.$container).live('click', upm.disableModule);

        // module enable button
        $('a.upm-module-enable', upm.$container).live('click', upm.enableModule);

        // expand all plugin details
        $('a.upm-expand-all', upm.$container).live('click', upm.expandAllPluginDetails);

        // collapse all plugin details
        $('a.upm-collapse-all', upm.$container).live('click', upm.collapseAllPluginDetails);

        // enable safe mode link
        $('#upm-safe-mode-enable').click(function(e) {
            e.preventDefault();
            upm.showConfirmDialog(AJS.params.upmTextSafeModeConfirm, upm.enableSafeMode);
        });

        // exit safe mode link
        $('#upm-safe-mode-restore').click(function(e) {
            e.preventDefault();
            upm.showConfirmDialog(AJS.params.upmTextSafeModeRestoreConfirm, upm.restoreFromSafeMode);
        });

        // exit safe mode link
        $('#upm-safe-mode-keep-state').click(function(e) {
            e.preventDefault();
            upm.showConfirmDialog(AJS.params.upmTextSafeModeExitConfirm, upm.exitSafeMode);
        });

        // requires restart details link
        $('#upm-requires-restart-show').live('click', function (e) {
            e.preventDefault();
            e.target.blur();
            upm.toggleRequiresRestartDetails($(e.target));
        });

        // requires restart action cancellation link
        $('a.upm-requires-restart-cancel', upm.$container).live('click', upm.cancelActionRequiringRestart);

        // Bindings and code specific to the manage / install page
        if (!isUpdateCheck) {

            // Create the carousel
            upm.bindOnce('pluginsLoaded.install', function(e, response) {
                var banners = [],
                    i = 0,
                    $carousel = $('<ul class="cloud-carousel"></ul>'),
                    $installType = $('#upm-install-type'),
                    plugin, firstSlider;

                // For every plugin with a banner and a marketing label of upm-carousel create a new list element
                for (; plugin = response.plugins[i++];) {
                    if (plugin.links && plugin.links['plugin-banner']
                            && plugin.marketingLabels && ($.inArray('upm-carousel', plugin.marketingLabels) !== -1)) {
                        banners.push($('<li class="carousel-item-wrap" data-expand="' + plugin.key + '"><a href="#" class="carousel-item"><img src="' + plugin.links['plugin-banner'] + '"></a></li>').get(0));
                    }
                }

                // Only load banners if we have at least 3
                if (banners.length >= 3) {
                    // First append each banner row to the carousel holder
                    $.each(banners, function(i, banner) {
                        $carousel.append(banner);
                    });
                    $carousel.appendTo($('#carousel'));
                    
                    // Then initialize the carousel
                    firstSlider = $carousel.cloudCarousel({
                        // On clicking the center item, expand that plugin
                        click: function(e, slider) {
                            if (slider.position !== 'center') {
                                return;
                            }

                            var attemptToExpand = function() {
                                    return upm.focusPlugin(slider.$container.attr('data-expand'), 'install');
                                },
                                cleanUp = function() {
                                    center.removeClass('loading');
                                    $carousel.resume();
                                },
                                $option;
                                // Prevent further clicks on the carousel until the plugin focuses
                                center = slider.$container.find('a').addClass('loading');

                            $carousel.pause();

                            e.preventDefault();

                            // When the plugin is done expanding / focusing, remove the loading class and rewire clicks
                            upm.bindOnce('pluginFocused.banner', cleanUp);

                            // If the plugin isn't in the current list...
                            if (!attemptToExpand()) {
                                // Listen for the new plugin list load...
                                upm.bindOnce('pluginsLoaded.install', function(e, response) {
                                    // And then expand it. If we still didn't find it, it may no longer be in the list (may have
                                    // been installed). unfreeze the carousel
                                    if(!attemptToExpand()) {
                                        cleanUp();
                                        upm.unbind('pluginFocused.banner');
                                    }
                                });

                                // Simple hack to select the first item in the "plugins to show" dropdown, which is "featured", which is
                                // the plugin list the banners come from, unless we are in a search
                                $option = $installType.find('option').eq(0);
                                if($option.attr('id') === 'upm-search-option') {
                                    $option = $option.next();
                                }

                                $option.attr('selected', 'selected').trigger('change');
                            }
                        }
                    // When all images are done loading, remove the loading spinner
                    }).bind('loaded', function() {
                        firstSlider.removeClass('loading');

                    // Add the loading spinner initially
                    }).getSlider(0).$container.find('a').addClass('loading');
                }
            });

            // tab-based navigation
            $('#upm-tabs li a').click(upm.swapTab);

            // search box -- install tab
            upm.clearOnBlur('#upm-install-search-box', AJS.params.upmTextInstallSearchBox);

            // search box -- osgi tab
            upm.clearOnBlur('#upm-osgi-search-box', AJS.params.upmTextOsgiSearchBox);

            // search clear button -- install tab
            $('#upm-install-search-clear-button').click(upm.clearInstallSearch);

            // search clear button -- osgi tab
            $('#upm-osgi-search-clear-button').click(upm.clearBundleSearch);

            // filter box -- manage tab
            upm.clearOnBlur('#upm-manage-filter-box', AJS.params.upmTextFilterPlugins);

            // filter box -- osgi tab
            upm.clearOnBlur('#upm-osgi-filter-box', AJS.params.upmTextFilterOsgiBundles);

            // plugin type select
            $('#upm-install-type').change(upm.changeDisplayedPlugins);

            // plugin update button
            $('a.upm-update', upm.$container).live('click', upm.updatePlugin);

            // update all plugins
            $('#upm-update-all').live('click', upm.updateAllPlugins);

            // plugin install button
            $('a.upm-install', upm.$container).live('click', upm.installPlugin);

            // plugin buy and try buttons (all they do is install, without getting a license)
            $('a.upm-buy', upm.$container).live('click', upm.installPlugin);
            $('a.upm-try', upm.$container).live('click', upm.installPlugin);

            // plugin uninstall button
            $('a.upm-uninstall', upm.$container).live('click', function(e) {
                upm.showConfirmDialog(AJS.params.upmTextUninstallConfirm, upm.uninstallPlugin, [e]);
            });

            // toggle system plugins into view
            $('.upm-manage-toggle-system').live('click', function (e) {
                e.preventDefault();
                upm.toggleSystemPlugins();
            });

            // toggle system plugins into view
            $('.upm-pricing-toggle').live('click', function (e) {
                e.preventDefault();
                upm.togglePluginPricing(e);
            });

            // pac search box
            $('#upm-install-search-form', upm.$container).submit(upm.searchForPlugins);

            // osgi bundle search box
            $('#upm-osgi-search-form', upm.$container).submit(upm.filterBundles);

            // upload plugin link
            $('#upm-upload').click(upm.showUploadDialog);

            // Banner click to update the UPM by secretly doing what a user would do if the UPM row were visible. The UPM row is not in the DOM
            // but it cached, so we are going to fake clicks on it to expand it, then click the update button. This is currently the path of 
            // least resistence to make UPM updatable by a banner, since the update code path is reliant on clicking on the update button inline.
            $('a.upm-banner-update', upm.$container).live('click', function(e) {
                var target = $(e.target),
                    pluginElement = upm.upmPluginContainer.closest('div.upm-plugin'),
                    details = upm.upmPluginContainer.find('div.upm-details');
                e.preventDefault();

                upm.startProgress(AJS.params.upmTextSelfUpdateWorking);

                // Hack to close the banner
                $(e.target).closest('div.aui-message').remove();

                upm.bindOnce('pluginLoaded.update', function() {
                    upm.updatePlugin({
                        target: pluginElement
                    });
                });
                // The URL to talk to doesn't exist until the UPM row (a hidden element) is expanded, so expand it
                upm.togglePluginDetails(pluginElement);
            });


            // osgi bundle cross-references
            $('a.upm-osgi-bundle-xref').live('click', function (e) {
                e.preventDefault();
                e.target.blur();
                upm.focusPlugin($(e.target).next('input').attr('value'), 'osgi');
            });

            // make back button work in browsers that support 'onhashchange'
            $(window).bind('hashchange', function() {
                var hash = upm.getLocationHash(),
                        tab = hash.tab;
                if ($('#upm-panel-' + tab).is(':hidden')) {
                    upm.loadTab(tab, hash.key);
                } else if (hash.key) {
                    upm.focusPlugin(hash.key, tab);
                }
            });

            upm.loadInitialTab();
        }
        upm.checkPacAvailable();
    };

    $(window).bind('upmready', upmReady);
})(AJS.$);
