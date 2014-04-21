/**
 * @constructor
 */
JIRA.LabelsDialog = JIRA.FormDialog.extend((function () {

    var impl = {};

    impl.init = function (options) {
        this._super(options);
        this.issueId = null;
        this.customFieldId = null;
        this.labelsProvider = this.initLabelsProvider();
        this.labelPicker = null;
    },

    impl.initLabelsProvider = function() {
        if (this.options.labelsProvider && AJS.$.isFunction(this.options.labelsProvider)) {
            return this.options.labelsProvider;
        } else if (this.options.labels) {
            return this._getLabelsFromOptions;
        } else {
            return this._getLabelsFromTrigger;
        }
    },

    impl._getLabelsFromOptions = function() {
        return AJS.$(this.options.labels);
    },

    impl._getLabelsFromTrigger = function() {
        return this.$activeTrigger.closest('.labels-wrap');
    },

    impl.decorateContent = function () {
        this._super();

        var $content = this.get$popupContent();

        this.issueId = $content.find('input[name=id]').val();
        var $customFieldId = $content.find('input[name=customFieldId]');
        if ($customFieldId.length === 1) {
            this.customFieldId = $customFieldId.val();
        } else {
            this.customFieldId = null;
        }

    };

    impl.focusLabelPicker = function () {
        this.get$popupContent().find('textarea').focus();
    };

    impl.show = function () {
        if(this._super()) {
            this.focusLabelPicker();
        }
    };

    impl._handleSubmitResponse = function (data, xhr, smartAjaxResult) {
        if (this.serverIsDone) {
            if (this.options.onSuccessfulSubmit) {
                this.options.onSuccessfulSubmit.call(this, data, xhr, smartAjaxResult);
            }
            //need to set this *before* we hide the dialog because hide() clears their values!
            var issueIdVal = this.get$popupContent().find('input[name=id]').val(),
                noLinkVal = this.get$popupContent().find('input[name=noLink]').val();

            if (this.options.autoClose) {
                this.hide();
            }
            JIRA.IssueNavigator.Shortcuts.flashIssueRow(this.issueId);

            var postData = {
                id: issueIdVal,
                decorator: 'none',
                noLink: noLinkVal
            };
            if (this.customFieldId) {
                postData.customFieldId = this.customFieldId;
            }
            var instance = this;
            var $labelsWrap = instance.labelsProvider(this);
            if ($labelsWrap) {
                jQuery(jQuery.ajax({
                    url: contextPath + '/secure/EditLabels!viewLinks.jspa',
                    data: postData,
                    success: function (html) {
                        var $newLabelsWrap = jQuery('<div>').html(html).find('.labels-wrap');
                        //don't show the edit icon on the issuenavigator.
                        if(JIRA.IssueNavigator.isNavigator()) {
                            $newLabelsWrap.find("a.edit-labels").remove();
                        }
                        $labelsWrap.html($newLabelsWrap.html());
                    }
                })).throbber({ target: $labelsWrap });
            }
        }
    };

    impl.handleCancel = function () {
        this._super();
        // Clear the content of the dialog so that it is retrieved from the server the next time it is opened.
        this.$content = null;
    };

    return impl;

})());


/** Preserve legacy namespace
    @deprecated AJS.LabelsPopup */
AJS.namespace("AJS.LabelsPopup", null, JIRA.LabelsDialog);