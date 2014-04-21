/*
 * Some update check functionality. Note that many bindings are registered in upm-plugins.js
 * since they share much functionailty overlap.
 */
(function($) {
    var $desc,
        $updateContainer,
        safeMode;

    /**
    * Checks the compatibility of installed plugins against a specified product version
    * @method checkCompatibility
    * @param {Event} e The event object
    */
    function checkCompatibility(e) {
        var container = AJS.$('#upm-compatibility-content'),
                recentProductMessage = AJS.$('#upm-recent-product-release-container'),
                compatiblePlugins = AJS.$('#upm-compatible-plugins'),
                needUpdatePlugins = AJS.$('#upm-need-update-plugins'),
                incompatiblePlugins = AJS.$('#upm-incompatible-plugins'),
                productUpdatePlugins = AJS.$('#upm-need-product-update-plugins'),
                unknownPlugins = AJS.$('#upm-unknown-plugins'),
                selected = AJS.$('#upm-compatibility-version option:selected'),
                url = selected.val(),
                options = {isExpandable: true, isInstalledList: true, className: 'compatibility'},
                updatableOptions = {isExpandable: true, isInstalledList: true, className: 'compatibility', isUpdatable: true};

        e.preventDefault();
        if (url) {
            container.addClass('loading');
            recentProductMessage.toggleClass('hidden', !selected.hasClass('upm-recent'));
            compatiblePlugins.addClass('hidden');
            needUpdatePlugins.addClass('hidden');
            productUpdatePlugins.addClass('hidden');
            incompatiblePlugins.addClass('hidden');
            unknownPlugins.addClass('hidden');
            AJS.$('span.upm-product-version', container).text(selected.text());
            container.show();
            AJS.$.ajax({
                type: 'GET',
                cache: false,
                url: url,
                dataType: 'json',
                contentType: upm.contentTypes['json'],
                success: function(response) {
                    var compatible = response['compatible'],
                            incompatible = response['incompatible'],
                            updateRequired = response['updateRequired'],
                            productUpdate = response['updateRequiredAfterProductUpdate'],
                            unknown = response['unknown'],
                            showCompatibilityResults = function(pluginsContainer, plugins, opts) {
                                pluginsContainer.find('div.upm-plugin-list-container').remove();
                                pluginsContainer.find('div.upm-expand-collapse-all').remove();
                                pluginsContainer.removeClass('hidden').append(upm.buildPluginList(plugins, opts));
                            };
                    container.removeClass('loading');
                    if (compatible.length) {
                        showCompatibilityResults(compatiblePlugins, compatible, updatableOptions);
                    }
                    if (incompatible.length) {
                        showCompatibilityResults(incompatiblePlugins, incompatible, options);
                    }
                    if (updateRequired.length) {
                        showCompatibilityResults(needUpdatePlugins, updateRequired, updatableOptions);
                    }
                    if (productUpdate.length) {
                        showCompatibilityResults(productUpdatePlugins, productUpdate, options);
                    }
                    if (unknown.length) {
                        showCompatibilityResults(unknownPlugins, unknown, options);
                    }
                    if (compatible.length == 0 && unknown.length == 0 && productUpdate.length == 0 && updateRequired.length == 0 && incompatible.length == 0) {
                        AJS.$('#upm-no-userinstalled').removeClass('hidden');
                    }
                },
                error: function(request) {
                    AJS.$('div.loading', container).removeClass('loading');
                    container.hide();
                    upm.handleAjaxError(upm.$messageContainer, request, '');
                }
            });
        }
    }

    // Wait for UPM to be ready, then load update check page. This is essentially the old
    // loadTab function for the update check tab.
    var upmReady = function() {
        $(window).unbind('upmready', upmReady);

        // update compatibility form
        $('#upm-compatibility-form').submit(checkCompatibility);

        var selectElement = $('#upm-compatibility-version'),
            $select = selectElement.clone(),
            $check = $('input.submit', $updateContainer);

        $updateContainer = $('#upm-panel-compatibility');

        upm.checkPacAvailable();

        $('div.upm-compatibility-category', $updateContainer).addClass('hidden');
        $('#upm-no-userinstalled').addClass('hidden');

        $select.removeAttr('disabled');
        $check.attr('disabled', 'disabled');
        $updateContainer.addClass('loading');

        $.ajax({
            url: upm.resources['product-updates'],
            type: 'get',
            cache: false,
            dataType: 'json',
            success: function(response) {
                var versions = response.versions,
                        len = versions.length,
                        option;
                if (!len) {
                    $('#upm-compatibility-no-versions').removeClass('hidden');
                } else {
                    $('#upm-compatibility-versions-available').removeClass('hidden');
                    for (var i = 0; i < len; i++) {
                        option = $('<option></option>')
                            .val(versions[i].links.self)
                            .text(versions[i].version)
                            .appendTo($select);
                        if (versions[i].recent) {
                            option.addClass('upm-recent');
                        }
                    }
                    selectElement.replaceWith($select);
                    $check.removeAttr('disabled');
                }
                $updateContainer.addClass('loaded').trigger('panelLoaded').removeClass('loading');
            },
            error: function(request) {
                $updateContainer.removeClass('loading');
                upm.handleAjaxError(upm.$messageContainer, request, "");
            }
        });
    };

    $(window).bind('upmready', upmReady);
})(AJS.$);
