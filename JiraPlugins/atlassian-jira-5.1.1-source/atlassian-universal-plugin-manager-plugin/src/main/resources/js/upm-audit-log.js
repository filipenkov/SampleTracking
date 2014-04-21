(function($) {
    var $desc,
        $logContainer,
        $messageContainer,
        safeMode;

    /**
     * Event handler for audit log page button clicks
     * @method logPagingEventHandler
     * @param {Event} e Event object
     */
    function logPagingEventHandler(e) {
        var $target = $(e.target),
            id = $target.attr('id');
        e.preventDefault();
        if (!$target.hasClass('disabled')) {
            buildLogEntries($('#' + id + '-url').val());
        }
    }

    /**
     * Sets the number of days that audit log entries are purged after
     * @method changeAuditLogPurgePolicy
     * @param {Event} e Event object
     */
    function changeAuditLogPurgePolicy(e) {
        var $configDayInput = $('#upm-log-configuration-days'),
            numDays = $configDayInput.val(),
            maxDays = 100000;
        e.preventDefault();
        // UPM-666 - validate that the input is a valid int and in specified range
        if (!numDays.match(/^\d+$/) || numDays <= 0 || numDays > maxDays) {
            $configDayInput.val($configDayInput.attr('data-lastValid'));
            upm.displayMessage($messageContainer, {
                body: AJS.format(AJS.params.upmAuditLogErrorInvalidPurgeAfter, maxDays),
                type: 'error',
                fadeOut: true
            });
        } else {
            $.ajax({
                type: 'PUT',
                url: upm.resources['audit-log-purge-after-manage'],
                dataType: 'json',
                contentType: upm.contentTypes['purge-after'],
                data: upm.json.stringify({'purgeAfter': numDays}),
                success: function(response) {
                    $configDayInput.attr('data-lastValid',numDays);
                    setPurgePolicyText(numDays);
                    toggleAuditLogConfiguration();
                },
                error: function(request) {
                    upm.handleAjaxError($messageContainer, request, "");
                }
            });
        }
    }

    /**
     * Changes the audit log text to reflect the latest purge policy
     * @method setPurgePolicyText
     * @param {Number} days Number of days audit log entries are being kept for
     */
    function setPurgePolicyText(days) {
        var text = days == 1 ? AJS.params.upmTextAuditLogDescriptionSingular : AJS.format(AJS.params.upmTextAuditLogDescription, days);
        $('#upm-log-policy').text(text);
    }

    /**
     * Hides or shows the audit log configuration form
     * @method toggleAuditLogConfiguration
     */
    function toggleAuditLogConfiguration() {
        var $container = $('#upm-log-configuration');
        if ($container.hasClass('hidden')) {
            $container.removeClass('hidden');
            $('#upm-log-configuration-days').select();
        } else {
            $container.addClass('hidden');
        }
    }

    /**
     * Creates and returns a list of entries from the audit log
     * @method buildLogEntries
     * @param {String} logPageUrl the url at which the desired page of audit log results will be found, or null
     */
    function buildLogEntries(logPageUrl) {
        buildLogEntries.template = buildLogEntries.template || $($('#upm-log-template').html());
        buildLogEntries.rowTemplate = buildLogEntries.rowTemplate || $($('#upm-log-row-template').html());
        $logContainer.addClass('loading').removeClass('loaded');
        $('#upm-audit-log').remove();

        $.ajax({
            url: logPageUrl || upm.resources['audit-log'],
            type: 'get',
            cache: false,
            dataType: (upm.browser.isIE) ? 'text' : 'xml',
            success: function(response) {
                var xml = upm.getXml(response),
                        entries = $('entry', xml),
                        firstPageHref = $('link[rel=\'first\']', xml).attr('href'),
                        previousPageHref = $('link[rel=\'previous\']', xml).attr('href'),
                        nextPageHref = $('link[rel=\'next\']', xml).attr('href'),
                        lastPageHref = $('link[rel=\'last\']', xml).attr('href'),
                        log = buildLogEntries.template.clone(),
                        table = log.find('table tbody'),
                        totalEntries = $('totalEntries:first', xml).text(),
                        startIndex = parseInt($('startIndex:first', xml).text(), 10),
                        resultsCount = entries.length;
                if (entries.length == 0) {
                    $logContainer.removeClass('loading').append($('<div id="upm-audit-log"></div>').text(AJS.params.upmTextEmptyLog)).addClass('loaded');
                } else {
                    entries.each(function(i) {
                        var row = buildLogEntries.rowTemplate.clone(),
                            profile = $('author:first', this),
                            profileUri = $('uri', profile),
                            username = $('name', profile).text();
                        $('td.message', row).text($('title:first', this).text());

                        if (profileUri.size()) {
                            $('td.username', row).append($("<a></a>").attr('href', profileUri.text()).text(username));
                        } else {
                            $('td.username', row).text(username);
                        }

                        $('td.date', row).text(upm.prettyDate($('updated:first', this).text()));
                        if (i % 2 == 1) {
                            row.addClass('zebra');
                        }
                        table.append(row);
                    });
                    $logContainer.append(log).removeClass('loading').addClass('loaded');

                    $('#upm-audit-log-first-url').val(firstPageHref);
                    $('#upm-audit-log-next-url').val(nextPageHref);
                    $('#upm-audit-log-previous-url').val(previousPageHref);
                    $('#upm-audit-log-last-url').val(lastPageHref);

                    $('#upm-audit-log-first').toggleClass('disabled', !firstPageHref);
                    $('#upm-audit-log-previous').toggleClass('disabled', !previousPageHref);
                    $('#upm-audit-log-next').toggleClass('disabled', !nextPageHref);
                    $('#upm-audit-log-last').toggleClass('disabled', !lastPageHref);

                    // hide pagination stuff if there's only one page
                    $('#upm-audit-log-pagination').toggleClass('hidden', !(firstPageHref || previousPageHref || nextPageHref || lastPageHref));

                    $('#upm-audit-log-count').text(AJS.format(AJS.params.upmTextAuditLogCount, startIndex + 1, startIndex + resultsCount, totalEntries));
                }
            },
            error: function(response) {
                var xml = response.responseXML || upm.getXml(response.responseText),
                    sudoError = false,
                    jsonError;
                // We should reload if it was a webSudo error
                try {
                    jsonError = upm.json.parse(response.responseText);
                } catch (e) {
                    AJS.log('Failed to parse response text: ' + e);
                }
                var status = response.status || jsonError["status-code"];
                sudoError = upm.reloadIfUnauthorizedStatus(status) || upm.reloadIfWebSudoError(jsonError.subCode);
                if (!sudoError) {
                    $logContainer.removeClass('loading');
                    upm.displayMessage($messageContainer, {
                        body: AJS.params.upmTextLogError,
                        type: 'error'
                    });
                }
            }
        });
    }

    AJS.toInit(function() {
        $desc = $('#upm-log-description');
        $logContainer = $('#upm-panel-log');
        $messageContainer = AJS.$('#upm-messages');

        $('#upm-audit-log-feed').attr('href', upm.resources['audit-log']);

        buildLogEntries();

        // we can't get safe mode, pending task, or requires restart info from log feed, so make an extra call if we don't have it already
        if (upm.safeMode === undefined) {
            $.ajax({
                url: upm.resources['root'],
                type: 'get',
                cache: false,
                dataType: 'json'
            });
        }

        if ($desc.hasClass('hidden')) {
            $.ajax({
                url: upm.resources['audit-log-purge-after'],
                type: 'get',
                cache: false,
                dataType: 'json',
                success: function(response) {
                    var numDays = response.purgeAfter,
                        $configDayInput = $('#upm-log-configuration-days');
                    if (upm.resources['audit-log-purge-after-manage']) {
                        $('#upm-log-configure').removeClass('hidden');
                    }
                    setPurgePolicyText(numDays);
                    $desc.removeClass('hidden');
                },
                error: function(request) {
                    upm.handleAjaxError($messageContainer, request, "");
                }
            });
        }
        $logContainer.trigger('panelLoaded');

        // audit log refresh
        $('#upm-audit-log-refresh').live('click', function (e) {
            e.preventDefault();
            buildLogEntries();
        });

        // audit log first button
        $('#upm-audit-log-first').live('click', logPagingEventHandler);

        // audit log previous button
        $('#upm-audit-log-previous').live('click', logPagingEventHandler);

        // audit log next button
        $('#upm-audit-log-next').live('click', logPagingEventHandler);

        // audit log last button
        $('#upm-audit-log-last').live('click', logPagingEventHandler);

        // audit log configure link
        $('#upm-log-configure-link').click(function (e) {
            e.preventDefault();
            e.target.blur();
            toggleAuditLogConfiguration();
        });

        // audit log configuration form
        $('#upm-log-configuration').submit(changeAuditLogPurgePolicy);

        // audit log configuration cancel link
        $('#upm-log-configuration-cancel').click(function (e) {
            e.preventDefault();
            toggleAuditLogConfiguration();
        });
    });
})(AJS.$);
