
AJS.$(function() {
    var $auiform = AJS.$(".collector-dialog form.aui");

    var defaultFieldValues = {};
	var errorContainerMap = {};

	var $customCollectorForm = AJS.$(".custom-collector");
	$customCollectorForm.addClass("hidden");

	var errorHandler = function(errorsObject) {
        var msgContainer = AJS.$("<div class=\"msg-container\"></div>");

        if(errorsObject.errors !== undefined && errorsObject.errors !== {}) {
            AJS.$.each(errorsObject.errors, function(field, error) {
				var errorContainer = errorContainerMap[field] || $auiform.find("#" + field).closest(".field-group,form");
				errorContainer.append("<div class=\"error\">" + error + "</div>");
            });
            $auiform.removeClass("disabled").find(".submit-button").removeAttr("disabled");
        } else {
            AJS.messages.error(msgContainer, {
                title:AJS.I18n.getText("collector.plugin.template.error"),
                body: "<p>" + AJS.I18n.getText("collector.plugin.template.error.msg") + "</p>",
                closeable: false
            });
            $auiform.prepend(msgContainer);
        }
    };

    var showErrorMessage = function(fieldId, errorMessage) {
        $auiform.find("#" + fieldId).after("<div class=\"error\">" + errorMessage + "</div>");
    };

    var setDefaultValues = function() {
        if (!AJS.$.isEmptyObject(defaultFieldValues)) {
            for (var value in defaultFieldValues) {
                AJS.$("#" + value).val(defaultFieldValues[value]);
            }
        }
    };

    $auiform.ajaxForm({
        skipEncodingOverride:true,
        dataType: "json",
        beforeSubmit: function(arr, $form, options) {
            $form.find(".error").remove();

            //basic validation.
            if(!AJS.params.customTemplate) {
                var $description = $form.find("#description"), $rating = $form.find("input[name=rating]");
                if($rating.length > 0) {
                    var result = true;

                    var rating = $form.find("input[name=rating]:checked").val();
                    if(!rating || rating.length == 0) {
                        showErrorMessage("feedback-rating",AJS.I18n.getText("collector.plugin.form.error.rating"));
                        result = false;
                    }

                    var descGood = AJS.$.trim($form.find("#description-good").val());
                    if (!descGood || descGood.length == 0) {
                        showErrorMessage("description-good",AJS.I18n.getText("collector.plugin.form.error.what.you.like"));
                        result = false;
                    }

                    var descBad = AJS.$.trim($form.find("#description-bad").val());
                    if (!descBad|| descBad.length == 0) {
                        showErrorMessage("description-bad",AJS.I18n.getText("collector.plugin.form.error.what.needs.improvement"));
                        result = false;
                    }

                    if (!result) {
                        return false;
                    }
                }
                if($description.length === 1) {
                    var desc = AJS.$.trim($description.val());
                    if(!desc || desc.length == 0) {
                        showErrorMessage("description",AJS.I18n.getText("collector.plugin.form.error.what.went.wrong"));
                        return false;
                    }
                }
            }
            $form.addClass("disabled").find(".submit-button").attr("disabled", "disabled");
            return true;
        },
        success: function(response, status, xhr, $form) {
            var msgContainer = AJS.$("<div class=\"msg-container\"></div>");
            var successMsg;

            if(response.errors !== undefined) {
                errorHandler(response);
                return;
            }

            if(response.url !== undefined) {
                var linkHtml = "<a class=\"issue-key\" target=\"_blank\" href=\"" + response.url + "\">" + response.key + "</a>";
                successMsg = "<p>" + AJS.format(AJS.I18n.getText("collector.plugin.template.thanks.recorded"), linkHtml) + "</p>";
            } else {
                successMsg = "<p>" + AJS.I18n.getText("collector.plugin.template.thanks.no.permission") + "</p>";
            }
            AJS.messages.success(msgContainer, {
                title: AJS.I18n.getText("collector.plugin.template.thank.you"),
                body: successMsg,
                closeable: false
            });
            $form.prepend(msgContainer);
            setTimeout(function() {
                window.top.postMessage("cancelFeedbackDialog", "*");
            }, 5000);
        },
        error: function(response, status, xhr) {
            errorHandler(response.status === 400 ? JSON.parse(response.responseText) : {});
        }
    });

    var closeDialog = function(e) {
        e.preventDefault();
        window.top.postMessage("cancelFeedbackDialog", "*");
    };

    AJS.$(window).keydown(function(e) {
        if (e.keyCode === 27) {
            closeDialog(e);
        }
    });
    AJS.$(".dialog-button-panel .cancel").click(function(e) {
        closeDialog(e);
    });

    AJS.$("#not-you-lnk").click(function(e) {
        e.preventDefault();
        AJS.$("#name-group").show();
        AJS.$("#email-group").show();
        AJS.$(".login-msg").hide();
    });

    AJS.InlineAttach.AjaxPresenter.DEFAULT_URL = "@contextPath/rest/collectors/1.0/tempattachment/" + AJS.params.collectorId;
    AJS.InlineAttach.FormPresenter.DEFAULT_URL = "@contextPath/rest/collectors/1.0/tempattachment/multipart/" + AJS.params.collectorId;

    AJS.$(window).bind("message", function(e) {
        // *********************** DANGER *******************
		try {
			var parsedMessage = JSON.parse(e.originalEvent.data);
			//CAREFUL! NEVER EVER eval any text here or it's a massive security hole.  Always ensure it's properly
			//escaped before inserting into the DOM!
			AJS.$("#webInfo").val(parsedMessage.feedbackString);
			// *********************** DANGER *******************
			defaultFieldValues = parsedMessage.fieldValues;
		} catch (ex) {
			// do nothing in case "cancelFeedbackDialog" message
		}

        // set default values on fields if they haven't already been set
        setDefaultValues();

        if(AJS.params.customTemplate) {
            AJS.$.ajax({
                url: "@contextPath/secure/CreateFields!default.jspa?decorator=none&projectKey=" + AJS.params.projectKey + "&issueType=" + AJS.params.issueType,
                contentType: "application/json",
                success: function(data) {
                    var $container = AJS.$(".custom-fields-container");
                    var fieldMap = {};
                    var customLabels = {};

                    var setCustomLabel = function(id, field) {
                        var $field = (field instanceof AJS.$) ? field : AJS.$(field);
                        var $label, $textNode;
                        if (customLabels[id]) {
                            $label = $field.children("label, legend").first();
                            if ($label.is("legend")) $label = $label.find("span");
                            $textNode = $label.contents().first();
                            $textNode.replaceWith(document.createTextNode(customLabels[id]));
                        }
                    };

                    if(AJS.params.customLabels) {
                        customLabels = JSON.parse(AJS.params.customLabels);
                    }
                    AJS.$(data.fields).each(function(i, field) {
                        fieldMap[field.id] = field;
                    });

                    AJS.$(".contact-form-fields").each(function(index, field) {
                        setCustomLabel(field.id, field);
                    });

                    AJS.$(JSON.parse(AJS.params.fields)).each(function(i, field) {
                        if(fieldMap[field]) {
                            var $field = AJS.$(fieldMap[field].editHtml);
                            setCustomLabel(field, $field);
                            // fill in default values if they're available
                            if (defaultFieldValues !== undefined && defaultFieldValues.hasOwnProperty(field)) {
                                $field.find("#" + field).val(defaultFieldValues[field]);
                            }
							$field = $field.appendTo($container);
							function findContainer(html) {
								var group = html.filter(".field-group,fieldset.group");
								if (group.length > 0) {
									return group.last();
								}

								group = html.find(".field-group,fieldset.group");
								if (group.length > 0) {
									return group.last();
								}

								var any = html.filter(":has(input)");
								if (any.length > 0) {
									return any.last();
								}

								return $container;
							}
							errorContainerMap[field] = findContainer($field);
                        }
                    });
                    //todo: This can be removed in JIRA 4.4.4 and replaced by:
                    //JIRA.trigger(JIRA.Events.NEW_CONTENT_ADDED, [$container]);
                    var dialogStub = {
                        get$popupContent: function() {
                            return $container;
                        }
                    };

                    AJS.InlineAttach.AjaxPresenter.DEFAULT_URL = "@contextPath/rest/collectors/1.0/tempattachment/" + AJS.params.collectorId;
                    AJS.InlineAttach.FormPresenter.DEFAULT_URL = "@contextPath/rest/collectors/1.0/tempattachment/multipart/" + AJS.params.collectorId;

                    AJS.$(document).trigger("dialogContentReady", [dialogStub]);
					$customCollectorForm.removeClass("hidden");
                },
                error:function(response) {
					$customCollectorForm.removeClass("hidden");
                    errorHandler(response);
                }
            });
        }
    });
});