// This javascript binds the toggle function for switching between the Log Work form and the Remaining Estimate form
// in the WorklogSystemField. Toggling occurs when the "Log Work" checkbox is changed.
jQuery(function () {
    jQuery("#log-work-activate").change(function() {
        jQuery("#worklog-logworkcontainer").toggleClass("hidden");
        if (jQuery("#worklog-timetrackingcontainer").size() > 0) {
            jQuery("#worklog-timetrackingcontainer").toggleClass("hidden");
        }
    });
});
