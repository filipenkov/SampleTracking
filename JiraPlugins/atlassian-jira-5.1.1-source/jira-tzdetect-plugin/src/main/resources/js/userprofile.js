AJS.$(function () {
    if (location.hash.indexOf('openTzPrefs') > -1) {
        var $link = AJS.$('#edit_prefs_lnk');
        if ($link.length) {
            $link.click();
        }
    }
});
