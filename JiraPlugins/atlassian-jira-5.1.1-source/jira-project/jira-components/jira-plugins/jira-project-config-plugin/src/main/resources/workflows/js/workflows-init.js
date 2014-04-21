AJS.$(function($) {

    $("a.project-config-icon-viewworkflow").each(function() {
        $(this).fancybox({
            type: "image",
            href: this.href,
            title: $(this).closest(".project-config-scheme-item-header").find(".project-config-workflow-name").text(),
            titlePosition: "outside",
            imageScale: true,
            centerOnScroll: true,
            overlayShadow: true
        });
    });

    $("a.project-config-workflow-default").click(function(e) {
        e.preventDefault();
        var href = $(this).attr("href");
        var dialog = new JIRA.FormDialog({
            id: "wait-migrate-dialog",
            content: function (callback) {
                callback('<form class="aui">'
                        + '<div class="form-body">'
                        + '<p>' + AJS.I18n.getText("admin.project.workflow.scheme.migration.prompt.message") + '</p>'
                        + '<p>' + AJS.I18n.getText("admin.project.workflow.scheme.migration.prompt.question") + '</p>'
                        + '</div>'
                        + '<div class="buttons-container form-footer"><div class="buttons">'
                        + '<input type="submit" class="button" value="' + AJS.I18n.getText('common.forms.continue') + '"/>'
                        + '<a href="#" class="cancel" id="aui-dialog-close">' + AJS.I18n.getText('common.words.cancel') + '</a>'
                        + '</div></div>'
                        + '</form>');
            },
            submitHandler: function(e, callback) {
                e.preventDefault();
                copyWorkflow(href, dialog);
                callback();
            }
        });
        dialog.addHeading(AJS.I18n.getText("admin.project.workflow.scheme.migration.prompt.heading"));
        dialog.show();
    });

    function copyWorkflow(href, dialog) {
        JIRA.SmartAjax.makeWebSudoRequest({
            url: contextPath + "/rest/projectconfig/latest/workflow",
            type: 'POST',
            data: JIRA.ProjectConfig.getId(),
            contentType: 'application/json',
            dataType: 'json',
            success: function(data) {
                var taskId = data.taskId;
                var workflowName = data.workflowName;
                if (taskId) {
                    //we have to do this here to ensure that the dialog has content so we can change the elements.
                    dialog.show();
                    dialog.handleCancel = function() {};
                    $("#wait-migrate-dialog .buttons").css('visibility', 'hidden');
                    dialog.addHeading(AJS.I18n.getText("admin.project.workflow.scheme.migration.wait.heading"));
                    $("#wait-migrate-dialog .form-body").html('<div class="aui-message info">'
                            + '<span class="aui-icon icon-info"></span>'
                            + AJS.I18n.getText("admin.project.workflow.scheme.migration.wait.message")
                            + '</div>'
                            + '<div id="progress-bar-container"></div>');
                    $("#progress-bar-container").progressBar(0, {
                        height: "20px",
                        showPercentage: true
                    });
                    checkIfFinished(taskId, href, workflowName);
                } else {
                    editWorkflow(href, workflowName);
                }
            },
            error: function(xhr, statusText, errorThrown, smartAjaxResult) {
                var data;
                try {
                    data = smartAjaxResult.data && $.parseJSON(smartAjaxResult.data);
                } catch (e) {
                    data = null;
                }

                if (data && data.errorMessages) {
                    showErrorDialog(data.errorMessages);
                } else {
                    showErrorDialog(JIRA.SmartAjax.buildSimpleErrorContent(smartAjaxResult));
                }
            }
        });
    }

    function checkIfFinished(taskId, successHref, workflowName) {
        JIRA.SmartAjax.makeRequest({
            url: contextPath + "/rest/projectconfig/latest/workflow/" + taskId,
            type: 'GET',
            contentType: 'application/json',
            dataType: 'json',
            success: function (taskStatus) {
                if (taskStatus.finished) {
                    $("#progress-bar-container").progressBar(100);

                    if (!taskStatus.successful || taskStatus.numberOfFailedIssues > 0) {
                        var params = $.param({
                            projectId: JIRA.ProjectConfig.getId(),
                            taskId: taskId
                        });
                        AJS.reloadViaWindowLocation(contextPath + "/secure/project/SelectProjectWorkflowSchemeStep3.jspa?" + params);
                    } else {
                        deleteTask(taskId, function() {
                            editWorkflow(successHref, workflowName);
                        });
                    }
                } else {
                    var progress = taskStatus.progress;
                    $("#progress-bar-container").progressBar(progress);
                    setTimeout(function() {
                        checkIfFinished(taskId, successHref, workflowName);
                    }, 1000);
                }
            },
            error: function(a, b, c, response) {
                deleteTask(taskId);
                showErrorDialog(JIRA.SmartAjax.buildSimpleErrorContent(response));
            }
        });
    }

    function deleteTask(taskId, callback) {
        JIRA.SmartAjax.makeRequest({
            url: contextPath + "/rest/projectconfig/latest/workflow/" + taskId,
            type: 'DELETE',
            contentType: 'application/json',
            dataType: 'json',
            complete: callback
        });
    }

    function editWorkflow(href, workflow) {
        href = href.replace(/wfName=[^&]*/, "wfName=" + encodeURIComponent(workflow));
        AJS.reloadViaWindowLocation(href);
    }

    function showErrorDialog(error) {
        new JIRA.FormDialog({
            id: "server-error-dialog",
            content: function (callback) {
                callback(JIRA.Templates.Common.serverErrorDialog({
                    message: error.join(' ')
                }));
            }
        }).show();
    }
});