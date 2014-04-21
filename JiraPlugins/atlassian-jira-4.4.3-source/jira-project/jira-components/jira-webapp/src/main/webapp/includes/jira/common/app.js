// Add classNames describing the browser, i.e name and version, to html tag.
AJS.describeBrowser();

// Preparing all over labels
jQuery(function () {
    AJS.$("label.overlabel").overlabel();
});

// For switching tabs on field screens (edit, transition, create)
AJS.$(function(){
    AJS.$(".fieldTabs li").click(function(e){
        e.preventDefault();
        e.stopPropagation();
        var $this = AJS.$(this);
        if (!$this.hasClass("active")){
            AJS.$(".fieldTabs li.active").removeClass("active");
            $this.addClass("active");
            AJS.$(".fieldTabArea.active").removeClass("active");
            AJS.$("#" + $this.attr("rel")).addClass("active");
        }
    });
});

/**
 * Toggle form accessKeys. JRA-16102
 */
jQuery(function () {

    jQuery("form").handleAccessKeys();

    jQuery(document).bind("dialogContentReady", function () {
        jQuery("form", this.$content).handleAccessKeys({
            selective: false // replace all access keys, not just ones in this form
        });
    })
});

/* Hide all inline dialogs if we press escape (JRADEV-5811) */
jQuery(document).keydown(function(e) {
    if (AJS.InlineDialog.current && e.which == 27 && !AJS.$(e.target).is(":input")) {
        AJS.InlineDialog.current.hide();
    }
});

/*
 Sets the width of the issue navigator results wrapper.
 Keeps the right hand page elements within the browser view when the results table is wider than the browser view.
 Also fixes rendering issue with IE8 (JRA-18224)
 */
jQuery(function() {

    var $issueNav = jQuery("div.results"),
        $issueNavWrapWidth = $issueNav.width();

    $issueNav.bind("resultsWidthChanged", function () {
        var $issueNavWrap = jQuery(this);

        $issueNavWrap.css("width", 100 / $issueNavWrapWidth * ($issueNavWrapWidth - (parseInt(jQuery(document.documentElement).attr("scrollWidth"), 10) - jQuery(window).width())) + "%");
    });

    jQuery(window).resize(function () {
        $issueNav.trigger("resultsWidthChanged");
    });

    $issueNav.trigger("resultsWidthChanged");
});

// Todo: Fix JRADEV-900
//jQuery(function(){
//    AJS.$(".admin-item-link").click(function(e)
//    {
//        e.preventDefault();
//        var $this = AJS.$(this);
//
//        jQuery.ajax({
//            url : contextPath + "/rest/api/1.0/adminHistory/store",
//            data: JSON.stringify({
//                id : $this.attr("id"),
//                // we still want to add history if you click the menu items
//                url : $this.attr("href") ? $this.attr("href") : $this.find("a:first").attr('href')
//            }),
//            contentType: "application/json",
//            type:  "POST"
//        });
//        window.location.href = $this.attr("href");
//    });
//});

// twixi blocks
jQuery(function () {

    var actionTwixi;

    /* Twixi Blocks use the following markup:
        <div id="must-be-unique" class="twixi-block expanded">
            <div class="twixi-wrap verbose">
                <a href="#" class="twixi"><span class="icon twixi-opened"><span>$i18n.getText("admin.common.words.hide")</span></span></a>
                <div class="action-details">...</div>
                <div class="action-body flooded">...</div>
            </div>
            <div class="twixi-wrap concise">
                <a href="#" class="twixi"><span class="icon twixi-closed"><span>$i18n.getText("admin.common.words.show")</span></span></a>
                <div class="action-details flooded">...</div>
            </div>
        </div>
     */

    actionTwixi = new JIRA.ToggleBlock({
            blockSelector: ".twixi-block",
            cookieCollectionName: "twixi"
        })
        .addCallback("toggle", function () {
            jQuery("#stalker").trigger("stalkerHeightUpdated");
        })
        .addTrigger(".action-details", "dblclick");

    // Becuse these are inverted need to switch expanded/collapsed calsses.  Yes, I know it confusing.
    new JIRA.ToggleBlock({
        blockSelector: ".inverted-twixi-block",
        collapsedClass: "expanded",
        expandedClass: "collapsed",
        cookieCollectionName: "inverted-twixi"
    })
    .addCallback("toggle", function () {
        jQuery("#stalker").trigger("stalkerHeightUpdated");
    });


    // Collapsing for the Simple Section
    // Default state is collapsed, so need to reverse the classes
    // Except for the text area, which is the oposite
    new JIRA.ToggleBlock({
        blockSelector: "#issue-filter .toggle-wrap:not(#navigator-filter-subheading-textsearch-group)",
        triggerSelector: ".toggle-trigger",
        collapsedClass: "expanded",
        expandedClass: "collapsed",
        cookieCollectionName: "navSimpleSearch"
    });
    new JIRA.ToggleBlock({
        blockSelector: "#navigator-filter-subheading-textsearch-group",
        triggerSelector: ".toggle-trigger",
        cookieCollectionName: "navSimpleSearchText"
    });

    // If a section has an error contained in it, it should be shown
    AJS.$("#issue-filter .error").parents(".toggle-wrap").removeClass("collapsed").addClass("expanded");

    // Collapsing for the Advanced section
    new JIRA.ToggleBlock({
        blockSelector: "#queryBoxTable.toggle-wrap",
        triggerSelector: ".toggle-trigger",
        cookieCollectionName: "navAdvanced"
    });

    // Generic twixi block
    new JIRA.ToggleBlock({
        blockSelector: ".twixi-block",
        triggerSelector: ".twixi-trigger",
        collapsedClass: "expanded",
        expandedClass: "collapsed",
        cookieCollectionName: "twixi"
    });
});

/* Logwork radio behaviour to disable/enable corresponding text inputs */
AJS.$(function (){
    AJS.$('#log-work-adjust-estimate-new-value,#log-work-adjust-estimate-manual-value').attr('disabled','disabled');
    AJS.$('#log-work-adjust-estimate-'+AJS.$('input[name=worklog_adjustEstimate]:checked,input[name=adjustEstimate]:checked').val()+'-value').removeAttr('disabled');
    AJS.$('input[name=worklog_adjustEstimate],input[name=adjustEstimate]').change(function(){
        AJS.$('#log-work-adjust-estimate-new-value,#log-work-adjust-estimate-manual-value').attr('disabled','disabled');
        AJS.$('#log-work-adjust-estimate-'+AJS.$(this).val()+'-value').removeAttr('disabled');
    });
});

/* Forgot login details panel swapper */
AJS.$(function(){

    // Make sure that we display one of the panels on page load (if there is a selected radio).
    var radio = AJS.$('input:checked');
    if (radio.length !== 0) {
        if (radio.attr('id') === 'forgot-login-rb-forgot-password') {
            AJS.$('#username,#password').addClass('hidden');
            AJS.$('#password').removeClass('hidden');
        }
        else if (radio.attr('id') === 'forgot-login-rb-forgot-username') {
                AJS.$('#username,#password').addClass('hidden');
                AJS.$('#username').removeClass('hidden');
        }
    }

    // Swap the panels if one of the radio's is selected
    AJS.$('#forgot-login-rb-forgot-password').change(function() {
        AJS.$('#username,#password').addClass('hidden');
        AJS.$('#password').removeClass('hidden');
    });
    AJS.$('#forgot-login-rb-forgot-username').change(function() {
        AJS.$('#username,#password').addClass('hidden');
        AJS.$('#username').removeClass('hidden');
    });
});

/* File input field-group repeaters */
AJS.$(function (){
    AJS.$("input.upfile").each(function() {
        var input = AJS.$(this),
            container = input.closest(".field-group");

        input.change(function () {
           if (input.val().length > 0) {
               container.next('.field-group').removeClass('hidden');
           }
        });
    });
});



jQuery(function () {
    new JIRA.ToggleBlock({
        blockSelector: "#iss-wrap",
        triggerSelector: "a.toggle-lhc",
        collapsedClass: "lhc-collapsed",
        cookieCollectionName: "lhc-state",
        autoFocusTrigger: false
    });

    new AJS.SecurityLevelSelect(jQuery("#commentLevel"));

    AJS.$("#iss-wrap").bind("contractBlock expandBlock", function () {
        jQuery(".results").trigger("resultsWidthChanged");
    });
});

/**
 * Ctrl-Enter should work for text areas
 */
jQuery(function()
{
    jQuery("textarea").keypress(submitOnCtrlEnter);
});

/**
 * For the JQL text box - we want to submit on Enter instead of Ctrl+Enter.
 */
jQuery(function() {
    var $jql = jQuery("#jqltext");
    if ($jql.length === 1) {
        $jql.unbind("keypress", submitOnCtrlEnter).keypress(submitOnEnter);
    }
});


/**
 * Warn if using an unsupported browser - for now that means IE6
 */
jQuery(function()
{
    var $warning = AJS.$("#browser-warning");
    AJS.$(".icon-close",$warning).click(function () {
        $warning.slideUp("fast");
        saveCookie("UNSUPPORTED_BROWSER_WARNING", "handled");
    });
});

jQuery(function() {
    AJS.$("form").submit(function(event) {
        AJS.$(this).trigger("before-submit", event);
    });
});

/**
 * Textareas that expand on input.
 */
AJS.$(function ($) {
    var $document = $(document),
        selector = '#comment, #environment, #description',
        maxTextareaHeight = 200;

    $document.bind('tabSelect', function (e, data) {
        data.pane.find(selector).expandOnInput();
    });
    $(selector).expandOnInput(maxTextareaHeight);

    $document.bind('dialogContentReady', function (e, dialog) {
        dialog.get$popupContent()
            .bind('tabSelect', function (e, data) {
                data.pane.find(selector).expandOnInput(maxTextareaHeight);
            })
            .find(selector)
                .expandOnInput(maxTextareaHeight);
    });

    // Bind to the event triggered by toggling the wiki markup preview.
    $document.bind('showWikiInput', function (e, $container) {
        $container.find(selector).expandOnInput();
    });
});

// intialise aui forms
AJS.$(function() {

    var $auiForm = AJS.$("form.aui");

    AJS.$("#stqcform input:file").inlineAttach();
    AJS.$(".file-input-list input:file", $auiForm).inlineAttach();

    // We want people to cancel forms like they used to when cancel was a button.
    // JRADEV-1823 - Alt-` does not work in IE
    var $cancel = AJS.$("a.cancel", $auiForm);
    if (AJS.$.browser.msie && $cancel.attr("accessKey")) {
        $cancel.focus(function(e){
            if (e.altKey) {
                //simulate a click (for the dirty form filter) then follow the link!
                AJS.$(this).mousedown();
                window.location.href = $cancel.attr("href");
            }
        });
    }
});

// Initialise the bulk edit screen to make checkboxes autoselect on :input change events.
AJS.$(function(){
    var checkRow = function(input){
        AJS.$(input).closest(".availableActionRow").find("td:first :checkbox").attr('checked', true);
    };

    var $rows = AJS.$("#availableActionsTable tr.availableActionRow");
    $rows.children("td:last-child").find(":input").change(function(e){
        checkRow(this);
    });
});


// Show a performance monitor top right if enabled.
AJS.$(function() {
   if(AJS.params.showmonitor) {
       var $div = AJS.$("<div class='perf-monitor'/>");
       var slowRequest = AJS.params["jira.request.server.time"] > 2000,
               tooManySql = AJS.params.jiraSQLstatements > 50;
       if(slowRequest) {
           $div.addClass("tooslow");
       }
       if(tooManySql) {
           $div.addClass("toomanysql")
       }

       AJS.$("#header-top").append($div);


       AJS.InlineDialog($div, "perf-monitor-dialog",
               function($contents, control, show) {
                   var timingInfo = "<div>Page render time <strong>" + AJS.params["jira.request.server.time"] + " ms</strong>";
                   if(AJS.params.jiraSQLstatements) {
                       timingInfo += " / SQL <strong>" + AJS.params.jiraSQLstatements + "@" + AJS.params.jiraSQLtime + " ms</strong></br>";
                       timingInfo += "<a target=\"_blank\" href=" + contextPath + "/sqldata.jsp?requestId=" + AJS.params["jira.request.id"] + ">More...</a>";
                   } else {
                       timingInfo += " / No SQL statments";
                   }
                   timingInfo +="</div>";
                   $contents.empty().append(timingInfo);
                   show();
               });
   }
});

AJS.$(function(){

    AJS.$(".shared-item-trigger").each(function() {
        var target = AJS.$(this).attr('href');
        AJS.InlineDialog(this, target.substring(1), function(contents, trigger, showPopup){
            contents.html(AJS.$(target).html());
            contentLoaded = true; showPopup();
        }, { width: 240 });
    });

    // we are using AUI tabs classes for CSS but get the Javascript behaviour also. Removing JavaScript behaviour
    //for the admin tabs since they do a full page reload.  AJS-638
    AJS.$("#admin-config-content.aui-tabs .tabs-menu.admin-tabs a").unbind("click");
});