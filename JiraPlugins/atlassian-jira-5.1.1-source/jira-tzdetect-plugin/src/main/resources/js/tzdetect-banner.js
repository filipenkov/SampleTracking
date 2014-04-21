/**
 * The main module
 *
 * @context atl.general
 */

(function ($) {

    $(function () {
        var $banner = $('#timezoneDiffBanner');
        if ($banner.length) {
            return;
        }

        if (location.hash.indexOf('openTzPrefs') == -1) {
            detectUserTimezone();
        }
    });

    function detectUserTimezone() {
        var prefix = 'tzdetect.pref.',
            tzPrefId = AJS.params[prefix + 'tzid'],
            tzPrefName = AJS.params[prefix + 'tzname'],
            tzPrefOffset = AJS.params[prefix + 'tzoffset'],
            tzPrefHasDST = AJS.params[prefix + 'tzdst'],
            tzPrefNoThanks = AJS.params[prefix + 'nothanks'];
        if (!tzPrefId) {
            return;
        }
        var tz = jstz.determine_timezone(),
            tzName = tz.name(),
            tzOffset = '(GMT' + tz.offset() + ')',
            tzHasDST = tz.dst();
        if (tzName == 'Etc/UTC') {
            tzName = 'Etc/GMT'; // To make JIRA happy
        }
        if (tzOffset == '(GMT00:00)') {
            tzOffset = '(GMT+00:00)';
        }
        if (tzName != tzPrefId && tzName != tzPrefNoThanks && (tzOffset != tzPrefOffset || tzHasDST != tzPrefHasDST)) {
            AJS.log('TZDetect: Detected timezone as ' + tzOffset + ' ' + tzName + ', user prefs set to ' + tzPrefOffset + ' ' + tzPrefId);

            var linkBase = contextPath + '/secure/ViewProfile.jspa';
            var link = linkBase + '#openTzPrefs';
            var restPath = contextPath + '/rest/tzdetect/1';

            var msg = AJS.I18n.getText("tz.no.match", tzPrefName, "<a class='tz-yes' href='" + link + "'>", "</a>", "<a class='tz-no' href='#hide'>", "</a>");

            var $banner = AJS.$('<div class="global-warning" id="timezoneDiffBanner"/>').html(msg);
            $banner.find('.tz-no').click(function (e) {
                e.preventDefault();
                var xhr = JIRA.SmartAjax.makeRequest({
                    url: restPath + '/nothanks',
                    type: "POST",
                    data: tzName,
                    contentType: 'application/json',
                    complete: function (xhr, status, smartAjaxResult) {
                        if (status != 'abort') {
                            if (smartAjaxResult.successful) {
                                removeBanner($banner);
                            }
                        }
                    }
                });
            });

            $banner.find('.tz-yes').click(function (e) {
                e.preventDefault();
                var xhr = JIRA.SmartAjax.makeRequest({
                    url: restPath + '/update',
                    type: "POST",
                    data: tzName,
                    contentType: 'application/json',
                    complete: function (xhr, status, smartAjaxResult) {
                        if (status != 'abort') {
                            if (smartAjaxResult.successful) {
                                removeBanner($banner);
                                if (JIRA.Messages) {
                                    var data = smartAjaxResult.data;
                                    var tzMsg = AJS.I18n.getText("tz.updated", data.gmtOffset + " " + data.city);
                                    JIRA.Messages.showSuccessMsg(tzMsg, {closeable: true});
                                }
                            }
                        }
                    }
                });
            });

            $banner.prependTo('body');
        }
    }

    function removeBanner($banner) {
        $banner.slideUp('fast', function () {
            $banner.remove();
        });
    }

})(AJS.$);
