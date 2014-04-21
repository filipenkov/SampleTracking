(function ($) {

    // Preparing all over labels
    function initOverlabels() {
        $("label.overlabel").overlabel();
    }

    /*
     Sets the width of the issue navigator results wrapper.
     Keeps the right hand page elements within the browser view when the results table is wider than the browser view.
     Also fixes rendering issue with IE8 (JRA-18224)
     */
    function initIssueNavContainment() {
        var $issueNav = $("div.results"),
                $issueNavWrapWidth = $issueNav.width();
        $issueNav.bind("resultsWidthChanged", function () {
            var $issueNavWrap = $(this);

            $issueNavWrap.css("width", 100 / $issueNavWrapWidth * ($issueNavWrapWidth - (parseInt($(document.documentElement).prop("scrollWidth"), 10) - $(window).width())) + "%");
        });
        $(window).resize(function () {
            $issueNav.trigger("resultsWidthChanged");
        });
        $issueNav.trigger("resultsWidthChanged");

        $("#issuenav").bind("contractBlock expandBlock", function () {
            $(".results").trigger("resultsWidthChanged");
        });

    }

    // For switching tabs on field screens (edit, transition, create)
    function initFieldTabs() {
        $(".fieldTabs li").click(function(e){
            e.preventDefault();
            e.stopPropagation();
            var $this = $(this);
            if (!$this.hasClass("active")){
                $(".fieldTabs li.active").removeClass("active");
                $this.addClass("active");
                $(".fieldTabArea.active").removeClass("active");
                $("#" + $this.attr("rel")).addClass("active");
            }
        });
    }

    // Toggle form accessKeys. JRA-16102
    function initHandleAccessKeys() {
        $("form").handleAccessKeys();
        $(document).bind("dialogContentReady", function () {
            $("form", this.$content).handleAccessKeys({
                selective: false // replace all access keys, not just ones in this form
            });
        })
    }

    // Hide all inline dialogs if we press escape (JRADEV-5811)
    function initHandleInlineLayerHide() {
        $(document).keydown(function(e) {
            if (AJS.InlineDialog.current && e.which == 27 && !$(e.target).is(":input")) {
                AJS.InlineDialog.current.hide();
            }
        });
    }

    function initToggleBlocks() {
        new JIRA.ToggleBlock({
            blockSelector: ".twixi-block",
            storageCollectionName: "twixi"
        })
                .addCallback("toggle", function () {
            $("#stalker").trigger("stalkerHeightUpdated");
        })
                .addTrigger(".action-details", "dblclick");

        // Becuse these are inverted need to switch expanded/collapsed calsses.  Yes, I know it confusing.
        new JIRA.ToggleBlock({
            triggerSelector: ".inverted-twixi",
            blockSelector: ".inverted-twixi-block",
            collapsedClass: "expanded",
            expandedClass: "collapsed",
            storageCollectionName: "inverted-twixi"
        })
        .addCallback("toggle", function () {
            $("#stalker").trigger("stalkerHeightUpdated");
        });


        // Collapsing for the Simple Section
        // Default state is collapsed, so need to reverse the classes
        // Except for the text area, which is the oposite
        new JIRA.ToggleBlock({
            blockSelector: "#issue-filter .toggle-wrap:not(#navigator-filter-subheading-textsearch-group)",
            triggerSelector: ".toggle-trigger",
            collapsedClass: "expanded",
            expandedClass: "collapsed",
            storageCollectionName: "navSimpleSearch"
        });
        new JIRA.ToggleBlock({
            blockSelector: "#navigator-filter-subheading-textsearch-group",
            triggerSelector: ".toggle-trigger",
            storageCollectionName: "navSimpleSearchText"
        });


        // Collapsing for the Advanced section
        new JIRA.ToggleBlock({
            blockSelector: "#queryBoxTable.toggle-wrap",
            triggerSelector: ".toggle-trigger",
            storageCollectionName: "navAdvanced"
        });

        // Generic twixi block
        new JIRA.ToggleBlock({
            blockSelector: ".twixi-block",
            triggerSelector: ".twixi-trigger",
            storageCollectionName: "twixi"
        });

        new JIRA.ToggleBlock({
            blockSelector: "#issuenav",
            triggerSelector: "a.toggle-lhc",
            collapsedClass: "lhc-collapsed",
            storageCollectionName: "lhc-state",
            autoFocusTrigger: false
        });

        // If a section has an error contained in it, it should be shown
        $("#issue-filter .error").parents(".toggle-wrap").removeClass("collapsed").addClass("expanded");

        $("fieldset.content-toggle input[type='radio']").live("change", function(){
            var $this = $(this);
            $this.closest(".content-toggle").find("input[type='radio']").each(function(){
                $("#" + $(this).attr("name") + "-" + $(this).val() + "-content").hide();
            });

            $("#" + $this.attr("name") + "-" + $this.val() + "-content").show();
        });
    }

    /* Logwork radio behaviour to disable/enable corresponding text inputs */
    function initLogWork() {
        $('#log-work-adjust-estimate-new-value,#log-work-adjust-estimate-manual-value').attr('disabled','disabled');
        $('#log-work-adjust-estimate-'+$('input[name=worklog_adjustEstimate]:checked,input[name=adjustEstimate]:checked').val()+'-value').removeAttr('disabled');
        $('input[name=worklog_adjustEstimate],input[name=adjustEstimate]').change(function(){
            $('#log-work-adjust-estimate-new-value,#log-work-adjust-estimate-manual-value').attr('disabled','disabled');
            $('#log-work-adjust-estimate-'+$(this).val()+'-value').removeAttr('disabled');
        });
    }

    // Make sure that we display one of the panels on page load (if there is a selected radio).
    function initLogin() {
        var radio = $('input:checked');
        if (radio.length !== 0) {
            if (radio.attr('id') === 'forgot-login-rb-forgot-password') {
                $('#username,#password').addClass('hidden');
                $('#password').removeClass('hidden');
            }
            else if (radio.attr('id') === 'forgot-login-rb-forgot-username') {
                $('#username,#password').addClass('hidden');
                $('#username').removeClass('hidden');
            }
        }

        // Swap the panels if one of the radio's is selected
        $('#forgot-login-rb-forgot-password').change(function() {
            $('#username,#password').addClass('hidden');
            $('#password').removeClass('hidden');
        });
        $('#forgot-login-rb-forgot-username').change(function() {
            $('#username,#password').addClass('hidden');
            $('#username').removeClass('hidden');
        });
    }

    /* File input field-group repeaters */
    function initFileRadio() {
        $("input.upfile").each(function() {
            var input = $(this),
                container = input.closest(".field-group");
            input.change(function () {
                if (input.val().length > 0) {
                    container.next('.field-group').removeClass('hidden');
                }
            });
        });
    }

    /**
     * Ctrl-Enter should work for text areas
     */
    function initHandleEnterInTextarea() {
        $("textarea").keypress(submitOnCtrlEnter);
        // For the JQL text box - we want to submit on Enter instead of Ctrl+Enter.
        var $jql = $("#jqltext");
        if ($jql.length === 1) {
            $jql.unbind("keypress", submitOnCtrlEnter).keypress(submitOnEnter);
        }
    }

    /**
     * Warn if using an unsupported browser
     */
    function initUnsupportedBrowserWarning() {
        var $warning = $("#browser-warning");
        $(".icon-close",$warning).click(function () {
            $warning.slideUp("fast");
            saveCookie("UNSUPPORTED_BROWSER_WARNING", "handled");
        });
    }

    /**
     * Make normal forms (non-ajax) still conform to the api we have for dialog forms etc. Disabling submission
     * by preventing default on before submit event.
     */
    function initHandleFormSubmit() {
        $("form").submit(function(e) {
            var event = new $.Event("before-submit");
            $(this).trigger(event);
            if (event.isDefaultPrevented()) {
                e.preventDefault();
            }
        });
    }

    /**
     * Textareas that expand on input.
     */
    function initExpandOnInput() {
        var $document = $(document),
                selector = '#comment, #environment, #description',
                maxTextareaHeight = 200;

        $document.bind('tabSelect', function (e, data) {
            data.pane.find(selector).expandOnInput();
        });

        if (JIRA.Events.INLINE_EDIT_STARTED) {
            JIRA.bind(JIRA.Events.INLINE_EDIT_STARTED, function (e, id, type, $editEls, $editingModule) {
                if ($editingModule && $editingModule.length > 0) {
                    var originalHeight = $editingModule.data("originalHeight");
                    originalHeight = Math.max(originalHeight, 60);
                    $editingModule.find(".textarea")
                            .css("height", originalHeight)
                            .expandOnInput();
                }
            });
        }

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
    }

    function initAuiTabHandling() {
        // Ensure tabs are initiated in dialogs. Used in Quick Edit/Quick Create
        JIRA.bind("dialogContentReady", function () {
            AJS.tabs.setup();

            // we are using AUI tabs classes for CSS but get the Javascript behaviour also. Removing JavaScript behaviour
            //for the admin tabs since they do a full page reload.  AJS-638
            $(".page-type-admin .content-container.aui-tabs > .content-related .tabs-menu a").unbind("click");
        });
    }

    // We want people to cancel forms like they used to when cancel was a button.
    // JRADEV-1823 - Alt-` does not work in IE
    function initCancelFormHandling() {
        var $auiForm = $("form.aui");
        var $cancel = $("a.cancel", $auiForm);
        if ($.browser.msie && $cancel.attr("accessKey")) {
            $cancel.focus(function(e){
                if (e.altKey) {
                    //simulate a click (for the dirty form filter) then follow the link!
                    $(this).mousedown();
                    window.location.href = $cancel.attr("href");
                }
            });
        }
    }

    // Initialise the bulk edit screen to make checkboxes autoselect on :input change events.
    function initBulkEditCheckboxes() {
        var checkRow = function(input){
            $(input).closest(".availableActionRow").find("td:first :checkbox").attr('checked', true);
        };
        var $rows = $("#availableActionsTable tr.availableActionRow");
        $rows.children("td:last-child").find(":input").change(function(e){
            checkRow(this);
        });
    }

    function initPerformanceMonitor() {
        if(AJS.params.showmonitor) {
            var $div = $("<div class='perf-monitor'/>");
            var slowRequest = AJS.params["jira.request.server.time"] > 2000,
                    tooManySql = AJS.params.jiraSQLstatements > 50;
            if(slowRequest) {
                $div.addClass("tooslow");
            }
            if(tooManySql) {
                $div.addClass("toomanysql")
            }

            $("#header-top").append($div);


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
    }

    function initShareItem() {
        $(".shared-item-trigger").each(function() {
            var target = $(this).attr('href');
            AJS.InlineDialog(this, target.substring(1), function(contents, trigger, showPopup){
                contents.html($(target).html());
                contentLoaded = true; showPopup();
            }, { width: 240 });
        });
    }

    function initClickables() {
        $(".clickable").click(function() {
            window.location.href = $(this).find("a").attr("href");
        });
    }

    // document ready
    $(function () {
        initToggleBlocks();
        initOverlabels();
        initIssueNavContainment();
        initFieldTabs();
        initHandleAccessKeys();
        initLogWork();
        initLogin();
        initFileRadio();
        initHandleEnterInTextarea();
        initUnsupportedBrowserWarning();
        initHandleFormSubmit();
        initExpandOnInput();
        initCancelFormHandling();
        initBulkEditCheckboxes();
        initHandleInlineLayerHide();
        initPerformanceMonitor();
        initShareItem();
        initClickables();
    });

    // Run straight away
    AJS.describeBrowser(); // Add classNames describing the browser, i.e name and version, to html tag.
    initAuiTabHandling();

})(AJS.$);
