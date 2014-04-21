/**
 * @constructor
 * @extends JIRA.Dialog
 */
JIRA.IssueActionsDialog = JIRA.Dialog.extend({

    _getDefaultOptions: function () {
        return AJS.$.extend(this._super(), {
            cached: false,
            id: "issue-actions-dialog",
            widthClass: "small"
        });
    },

    _setContent: function (content, decorate) {
        if (content) {
            this._super(content, decorate);
        } else {
            this._super(AJS.$([
                "<form id='issue-actions-dialog-form' class='aui ajs-dirty-warning-exempt'>",
                    "<div class='form-body'>",
                        "<div id='issueactions-suggestions' class='aui-list' />",
                        "<div class='description'>",
                        AJS.I18n.getText("issueactions.start.typing"),
                        "</div>",
                    "</div>",
                "</form>"
            ].join('')), true);
        }
        if (JIRA.Dialog.current === this) {
            var triggerConfig = new JIRA.setFocus.FocusConfiguration();
            triggerConfig.context = this.get$popup()[0];
            triggerConfig.parentElementSelectors = ['.form-body'];
            JIRA.setFocus.pushConfiguration(triggerConfig);
            JIRA.setFocus.triggerFocus();
        }
    },

    _formatActionsResponse: function (response) {

        function addSelected(issueId) {

             var url = window.location.href,
                newUrl = url;

            if (/selectedIssueId=[0-9]*/.test(url)) {
                newUrl = newUrl.replace(/selectedIssueId=[0-9]*/g, "selectedIssueId=" + issueId);
            } else {
                if (JIRA.IssueNavigator.isNavigator()) {
                    if (/\?/.test(url)) {
                        newUrl = newUrl + "&";
                    } else {
                        newUrl = newUrl + "?";          
                    }
                    newUrl = newUrl + "selectedIssueId=" + issueId;
                }
            }
            return encodeURIComponent(newUrl);
        }

        function formatWorkflowResponse(workflowResponse) {

            var workflows = new AJS.GroupDescriptor({
                label: AJS.I18n.getText("opsbar.more.transitions")
            });

            AJS.$(workflowResponse).each(function() {

                workflows.addItem(new AJS.ItemDescriptor({
                    href: contextPath + "/secure/WorkflowUIDispatcher.jspa?id=" + response.id + "&action=" + this.action + "&atl_token=" + response.atlToken + "&returnUrl=" + addSelected(response.id),
                    label: this.name,
                    styleClass: "issueaction-workflow-transition"
                }));

            });

            return workflows;
        }

        function formatOperationResonse(operationsResponse) {

            var operations = new AJS.GroupDescriptor({
                label: AJS.I18n.getText("common.words.actions")
            });

            AJS.$(operationsResponse).each(function() {

                var _returnUrl;

                if (this.name === "Clone") {
                    if (JIRA.IssueNavigator.isNavigator()) {
                          _returnUrl=addSelected(response.id);
                    } else {
                          _returnUrl="";
                    }
                } else {
                    _returnUrl=addSelected(response.id);
                }


                operations.addItem(new AJS.ItemDescriptor({
                    href: this.url + "&returnUrl=" + _returnUrl,
                    label: this.name,
                    styleClass: this.styleClass
                }));
            });
            return operations;
        }

        var res = [];

        if (response) {
            if (response.actions && response.actions.length != 0) {
                res.push(formatWorkflowResponse(response.actions));
            }

            if (response.operations && response.operations.length != 0) {
                res.push(formatOperationResonse(response.operations));
            }

        }

        return res;
    },

    decorateContent: function () {

        var instance = this,
            issueKey = JIRA.IssueNavigator.getSelectedIssueKey(),
            issueId = JIRA.Issue.getIssueId() || JIRA.IssueNavigator.getSelectedIssueId();

        if (issueKey) {
            this.addHeading(AJS.I18n.getText("common.words.operations") + ": <span>" + issueKey + "</span>");
        } else {
            this.addHeading(AJS.I18n.getText("common.words.operations"));
        }

        this.queryControl = new AJS.QueryableDropdownSelect({
            id: "issueactions",
            element: this.$content.find("#issueactions-suggestions"),
            ajaxOptions: {
                minQueryLength: 1,
                dataType: "json",
                url: AJS.format(contextPath + "/rest/api/1.0/issues/{0}/ActionsAndOperations?atl_token={1}", issueId, atl_token()),
                formatResponse: this._formatActionsResponse
            },
            showDropdownButton: true,
            loadOnInit: true
        });
        this.queryControl._handleServerError  = function(smartAjaxResult) {
            var errMsg = JIRA.SmartAjax.buildSimpleErrorContent(smartAjaxResult);
            var errorClass = smartAjaxResult.status === 401?'warning':'error';
            instance._setContent(AJS.$('<div class="ajaxerror"><div class="aui-message ' + errorClass+'"><p>' + errMsg + '</p></div></div>'), false);
            instance._addCloseLink();
        };


        this.timeoutId = undefined;
        this._addCloseLink();

    },

    _addCloseLink : function() {
        var instance=this,$closeLink, $buttonContainer, $buttons;
        
        $buttonContainer = AJS.$('<div class="buttons-container form-footer"></div>').appendTo(this.get$popupContent());
        $buttons = AJS.$('<div class="buttons"/>').appendTo($buttonContainer);
        $closeLink = AJS.$("<a href='#' class='cancel' id='aui-dialog-close'>" + AJS.I18n.getText("admin.common.words.close") + "</a>");
        $closeLink.appendTo($buttons, this.get$popupContent()).click(function(e) {
            instance.hide();
            e.preventDefault();
        });
        this.get$popupContent().append($buttonContainer);
    },

    hide: function (undim) {

        if (this._super(undim) === false) {
            return false;
        }

        JIRA.setFocus.popConfiguration();        
    }
});


/** Preserve legacy namespace
    @deprecated AJS.FormPopup */
AJS.namespace("AJS.IssueActionsPopup", null, JIRA.IssueActionsDialog);
