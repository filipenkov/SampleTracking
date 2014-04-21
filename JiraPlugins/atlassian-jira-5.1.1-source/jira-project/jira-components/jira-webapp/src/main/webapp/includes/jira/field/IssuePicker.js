/**
 * A multiselect list for querying and selecting issues. Issues can also be selected via a popup. 
 *
 * @constructor JIRA.IssuePicker
 * @extends AJS.MultiSelect
 */
JIRA.IssuePicker = AJS.MultiSelect.extend({

    /**
     * Format response into a JSON object that can be digested by @see AJS.SelectModel.appendOptionsFromJSON
     *
     * Note: We could probably have the server return in a format that can be digested by appendOptionsFromJSON, but
     * we currently have a legacy issue picker that uses the same end point.
     *
     * @method _formatResponse
     * @param {Object} response
     */
    _formatResponse: function (response) {
        var ret = [],
            canonicalBaseUrl = (function(){
                var uri = parseUri(window.location);
                return uri.protocol + "://" + uri.authority;
            })();

        if (response && response.sections) {

            AJS.$(response.sections).each(function(i, section) {

                var groupDescriptor = new AJS.GroupDescriptor({
                    weight: i, // order or groups in suggestions dropdown
                    label: section.label, // Heading of group
                    description: section.sub // description for the group heading
                });

                if (section.issues && section.issues.length > 0){

                    AJS.$(section.issues).each(function(){

                        groupDescriptor.addItem(new AJS.ItemDescriptor({
                            value: this.key, // value of item added to select
                            label: this.key + " - " + this.summaryText, // title of lozenge
                            icon: this.img ? canonicalBaseUrl + contextPath + this.img : null, // Need to have the canonicalBaseUrl for IE7 to avoid mixed content warnings when viewing the issuepicker over https
                            html: this.keyHtml + " - " + this.summary // html used in suggestion
                        }));
                    });
                }
                
                ret.push(groupDescriptor);
                
            });
        }
        
        return ret;
    },

    /**
     * When there is no query we remove the currentJQL value which causes the serverside to return history results ONLY
     *
     * @method getAjaxOptions
     * @override
     * @return {Object}
     */
    getAjaxOptions: function () {
        var ajaxOptions = this._super();
        if (this.$field.val().length === 0) {
            delete ajaxOptions.data.currentJQL;
        }
        return ajaxOptions;
    },

    /**
     * We only want to show a user inputted option if there is an input
     *
     * @method hasUserInputtedOption
     * @override
     * @return {Boolean}
     */
    hasUserInputtedOption: function () {
        return this.$field.val().length !== 0;
    },

    /**
     * Launches a popup window, where issues can be fixed based on filter/history and current search. Installs
     * a callback in the current window that can be used by the popup window to add items to the control.
     *
     * @method _launchPopup
     * @override
     */
    _launchPopup: function () { 

        function getWithDefault(value, def) {
            if(typeof value == "undefined" || value == null){
                return def;
            } else {
                return value;
            }
        }

        var url, urlParam, vWinUsers, options, instance = this;

        JIRA.IssuePicker.callback = function (items) {
            if (typeof items === "string") {
                items = JSON.parse(items);
            }
            instance._addMultipleItems(items, true);
            instance.$field.focus();
        };

        options = this.options.ajaxOptions.data;
        url = contextPath + '/secure/popups/IssuePicker.jspa?';
        urlParam = {
            singleSelectOnly: "false",
            decorator: "popup",
            currentIssue: options.currentIssueKey || "",
            showSubTasks: getWithDefault(options.showSubTasks, false),
            /* Note the slightly different option name here showSubTasksParent vs. showSubTaskParent */
            showSubTasksParent: getWithDefault(options.showSubTaskParent, false)
        };

        if (options.currentProjectId) {
            urlParam["currentProjectId"] = options.currentProjectId;
        }

        url += AJS.$.param(urlParam);

        vWinUsers = window.open(url, 'IssueSelectorPopup', 'status=no,resizable=yes,top=100,left=200,width=' + this.options.popupWidth + ',height=' + this.options.popupHeight + ',scrollbars=yes,resizable');
        vWinUsers.opener = self;
        vWinUsers.focus();
    },

    /**
     * Adds popup link next to picker and assigns event to open popup window
     *
     * @param {Boolean} disabled - Adds a standard text box instead of ajax picker if set to true
     * @override
     */
    _createFurniture: function (disabled) {
        var $popupLink;

        this._super(disabled);

        $popupLink = this._render("popupLink");

        this._assignEvents("popupLink", $popupLink);
        this.$container.addClass('hasIcon');
        $popupLink.appendTo(this.$container);

    },

    handleFreeInput: function() {
        var values = this.$field.val().toUpperCase().match(/\S+/g);

        if (values) {
            this._addMultipleItems(jQuery.map(values, function(value) {
                return { value: value, label: value };
            }));
        }

        this.$field.val("");
    },

    _events: {
        popupLink: {
            click: function (e) {
                this._launchPopup();
                e.preventDefault();
            }
        }
    },

    _renders: {
        popupLink: function () {
            return AJS.$("<a class='issue-picker-popup' />")
                    .attr({
                        href: "#",
                        title: this.options.popupLinkMessage
                    })
                    .text("" + this.options.popupLinkMessage + "");
        }
    }

});

/** Preserve legacy namespace
    @deprecated jira.issuepicker */
AJS.namespace("jira.issuepicker", null, JIRA.IssuePicker);

/** Preserve legacy namespace
    @deprecated AJS.IssuePicker */
AJS.namespace("AJS.IssuePicker", null, JIRA.IssuePicker);
