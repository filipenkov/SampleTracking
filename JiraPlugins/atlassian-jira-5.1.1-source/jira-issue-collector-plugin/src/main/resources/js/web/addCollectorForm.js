JIRA.CollectorFieldStore = Class.extend({

    init: function (options) {
        this.baseUrl = options.baseUrl;
        this.activeFieldIds = [];
        this.title = "";
        this.fieldLabels = {};
        this.createReporterPicker();
    },

    createReporterPicker: function() {
        var control = new JIRA.CollectorReporterPicker({
            element: AJS.$(".js-reporter-picker")
         });
    },


    refresh: function (options) {
        var instance = this, issueType = options.issueType;
        instance.activeFieldIds = [];

        AJS.$.ajax({
            url: this.baseUrl + "&issueType=" + issueType,
            contentType: "application/json",
            success: function(data) {
                instance.fields = data.fields;
                AJS.$(instance.fields).each(function(index, field) {
                    field.isActive = field.required;
                    if(field.isActive) {
                        instance.activeFieldIds.push(field.id);
                    }
                });
                var fieldVal = AJS.$("#custom-template-fields").val();
                var titleVal = AJS.$("#custom-template-title").val();
                var labelVal = AJS.$("#custom-template-labels").val();
                if(fieldVal) {
                    instance.activeFieldIds = JSON.parse(fieldVal);
                    AJS.$(instance.fields).each(function(i, field) {
                        field.isActive = AJS.$.inArray(field.id, instance.activeFieldIds) !== -1;
                    });
                }
                if(titleVal) {
                    instance.title = titleVal;
                }
                if(labelVal) {
                    instance.fieldLabels = JSON.parse(labelVal);
                }
                AJS.$(instance).trigger("fieldsRefreshed");
            },
            error: function(response) {
                var msgContainer = AJS.$(".ajax-msg-container");
                AJS.messages.error(msgContainer, {
                    title:AJS.I18n.getText("collector.plugin.template.ajaxerror"),
                    body: "<p>" + AJS.I18n.getText("collector.plugin.template.ajaxerror.msg") + "</p>",
                    closeable: true
                });
            }
        });
    },

    getTitle: function() {
        return this.title;
    },

    setTitle: function(title) {
        this.title = title;
        AJS.$("#custom-template-title").val(title);
    },

    getFields: function() {
        return this.fields;
    },

    getActiveFields: function() {
        var ret = [], instance = this;
        AJS.$(this.activeFieldIds).each(function(i, fieldId) {
            ret.push(instance.getField(fieldId));
        });
        return ret;
    },

    getFieldLabel: function(fieldId) {
        return this.fieldLabels[fieldId];
    },

    setFieldLabel: function(fieldId, label) {
        this.fieldLabels[fieldId] = label;
        AJS.$("#custom-template-labels").val(JSON.stringify(this.fieldLabels));
    },

    updateActiveFieldIds: function($activeFields) {
        var instance = this;
        this.activeFieldIds = [];
        $activeFields.each(function() {
            instance.activeFieldIds.push(AJS.$(this).data("field-id"));
        });
        AJS.$(instance.fields).each(function(i, field) {
            field.isActive = AJS.$.inArray(field.id, instance.activeFieldIds) !== -1;
        });
        AJS.$("#custom-template-fields").val(JSON.stringify(instance.activeFieldIds));
    },

    getField: function(fieldId) {
        var theField;
        AJS.$(this.getFields()).each(function(index, field) {
            if(field.id === fieldId) {
                theField = field;
                return false;
            }
        });

        return theField;
    }
});

JIRA.CollectorFieldRenderer = Class.extend({

    init: function (options) {
        this.container = options.container;
        this.formSelector = options.formSelector;
        this.store = options.store;
        this.titleSelector = options.titleSelector;
        this.infoBox = options.infoBox;
        this.customFieldsHint = options.customFieldsHint;
        this.initTitleEvents();
    },

    initSortable: function($fields) {
        return $fields.sortable({
            items: $fields.find(".draggable")
        });
    },

    initTitleEvents: function() {
        var instance = this;
        var $input = AJS.$('<input class="title-input" type="text" value="' + instance.store.getTitle() + '" /><input class="title-submit" type="submit" value="Done"/>');

        var submitInput = function (e) {
            var $title = AJS.$(instance.titleSelector);
            var $field = $title.find(".title-input");
            e.preventDefault();
            if (!$field.size()) return; // if there's no input field, don't update anything.
            instance.store.setTitle($field.val());
            setTimeout(function() { instance.renderTitle() }, 0);
        };

        AJS.$(instance.titleSelector)
            .live("click", function (e) {
                var $title = AJS.$(this);
                var $field = $title.find(".title-input");
                e.preventDefault();
                if ($field.size()) return; // if we've already got the input don't put it there again...
                $title.html($input);
                $title.find(".title-input").focus();
            })
            .find(".title-input").live({
                "keydown": function(e) {
                    if (e.keyCode === 13) submitInput(e);
                },
                "blur": submitInput
            })
            .find(".title-submit").live("click", submitInput);
    },

    render: function(fields) {
        var instance = this;
        var $customFields = AJS.$(this.formSelector).find(".custom-fields-container");

        $customFields.hide(); // avoid repaints / reflows while we adjust things
        $customFields.empty();

        AJS.$(instance.store.getActiveFields()).each(function(index, field) {
            $customFields.append(instance.renderField(field));
        });

        this.initSortable($customFields);
        this._makeFieldsNonInteractive($customFields);

        $customFields.show(); // repaint here

        this.container.html(JIRA.Templates.fieldsToAdd({fields: fields}));
        this.container.find(".user-form-field").click(function(e) {
            e.preventDefault();
            var fieldId = AJS.$(this).data("field-id");
            var field = instance.store.getField(fieldId);

            if(!field.isActive) {
                $customFields.append(instance.renderField(field));
                AJS.$(this).find(".icon").addClass("icon-tick").removeClass("icon-add12");
            } else {
                $customFields.find("#" + fieldId + "-group").remove();
                AJS.$(this).find(".icon").addClass("icon-add12").removeClass("icon-tick");
            }

            instance.store.updateActiveFieldIds($customFields.find(".draggable"));
            instance.initSortable($customFields);
            instance._makeFieldsNonInteractive($customFields);
        });
        this.infoBox.show();
        this.container.parent().show();
        this.customFieldsHint.show();

        this.renderTitle();
    },

    renderTitle: function() {
        var $title = AJS.$(this.titleSelector);
        var defaultTemplateTitle = AJS.I18n.getText("collector.plugin.template.custom.title");
        var storedTitle = this.store.getTitle();

        $title.text(storedTitle || defaultTemplateTitle);
    },

    renderField: function(field) {
        var instance = this;
        var $field = AJS.$(field.editHtml).attr("data-field-id", field.id).addClass("draggable").attr("id", field.id + "-group");
        var $fieldLabel = instance._getFieldLabel($field);
        $field.prepend('<span class="drag-handle"></span>');

        instance._labelEditHandler($fieldLabel,instance,field);

        return $field;
    },

    renderStaticFields: function() {
        var instance = this;
        AJS.$(".contact-form-fields").each(function (index, field) {
            var $fieldLabel = instance._getFieldLabel(field);
            instance._labelEditHandler($fieldLabel, instance, field);
        });
    },

    hideFields: function() {
        this.infoBox.hide();
        this.container.parent().hide();
        this.customFieldsHint.hide();
    },

    /**
     * Gets the correct 'label' element for a field, avoiding both
     * the label's other children and other markup of the field's values.
     *
     * @param field the root jQuery or DOM element for the field.
     * @return a jQuery element that represents the field's label.
     * @private
     */
    _getFieldLabel: function(field) {
        var $field = (field instanceof AJS.$) ? field : AJS.$(field);
        var $label = $field.children("label, legend").first();
        return $label;
    },

    _makeLabelEditable: function($label) {
        var $labelWrapper = ($label.is("legend")) ? $label.find("span") : $label;
        var $text = $labelWrapper.contents().first(); // will be a text node.
        $text.wrap('<span class="custom-label-wrapper"><span class="custom-label"></span></span>');
        return $labelWrapper.children('.custom-label-wrapper');
    },

    _labelEditHandler : function(label, instance, field) {
        var $labelWrapper = instance._makeLabelEditable(label);
        var customLabel = instance.store.getFieldLabel(field.id);
        if(customLabel) {
			$labelWrapper.find(".custom-label").text(customLabel);
        }

        label.click(function(e) {
            e.preventDefault();
            var $this = AJS.$(this);
            var $input = AJS.$("<input class=\"label-input text\" type=\"text\" />");
            //don't render the input if there's already one there
            if($this.find("input").length > 0) {
                return;
            }

            //some labels may have other stuff like 'Required' spans as children. We only want the label text!
            var $children = $this.clone().children();
            var text = $children.find(".custom-label").text();
            $input.val(text);
            $this.html($input.wrap("<span></span>").parent());

            var submitInput = function(e) {
                e.preventDefault();

                var newLabel = $input.val();
                instance.store.setFieldLabel(field.id, newLabel);
                $children.find(".custom-label").text(newLabel);
                $this.html($children);
            };

            $input.focus().keydown(function(e) {
                if(e.keyCode === 13) {
                    submitInput(e);
                }
            }).blur(submitInput);
        });
    },

    _makeFieldsNonInteractive: function($fields) {
        $fields.find("label").removeAttr("for");
        $fields.find("a, input, select, textarea")
            .attr("tabindex", "-1")
            .attr("readonly", "readonly")
            .bind({
                "click": function(e) { e.preventDefault(); },
                "focus": function(e) { AJS.$(this).blur(); }
            });
        return $fields;
    }
});



AJS.$(function() {
    var $issueTypeSelect = AJS.$("#issuetype"),
        $previewTrigger = AJS.$(".issue-collector-trigger-preview .issue-collector-trigger"),
        $previewContainer = AJS.$("#triggerPreview"),
        $customTrigger = AJS.$("#customTrigger"),
        $templateId = AJS.$("input[name=templateId]"),
        $simpleTemplates = $templateId.not("#template-custom");

    var fieldStore = new JIRA.CollectorFieldStore({
        baseUrl: contextPath + "/secure/CreateFields!default.jspa?decorator=none&projectKey=" + AJS.params.projectKey
    });
    var fieldRenderer = new JIRA.CollectorFieldRenderer({
        store: fieldStore,
        container:AJS.$(".custom-fields .field-container"),
        infoBox:AJS.$(".template-preview .custom-info"),
        formSelector:".collector-preview form.aui .content-body",
        titleSelector: ".collector-preview .collector-dialog.custom-collector > .dialog-title",
        customFieldsHint:  AJS.$(".custom-fields-hint")
    });

	var fieldIssueMapping = JSON.parse(AJS.$("#missingFields").val());
	var MissingFieldsModel = Backbone.Model.extend({
        contains: function(val, missingFields) {
            for(var prop in missingFields) {
                if(missingFields.hasOwnProperty(prop) && missingFields[prop] === val) {
                    return true;
                }
            }
            return false;
        },
        areFieldsMissing: function() {
            var missingFields = fieldIssueMapping[this.get("issueType")];
            if (missingFields === undefined) {
                return false;
            }
            if (this.contains("environment", missingFields) && this.get("collectData")) {
                return true;
            }
            if (this.contains("description", missingFields) && this.get("templateType") !== "custom") {
                return true;
            }
            return false;
        }
    });
    var missingFieldModel = new MissingFieldsModel;

    var requiredInvalidFieldsMapping = JSON.parse(AJS.$("#requiredInvalidFields").val());
    var RequiredInvalidFieldsModel = Backbone.Model.extend({
       getInvalidFields: function() {
           return requiredInvalidFieldsMapping[this.get("issueType")];
       }
    });
    var requiredInvalidFields = new RequiredInvalidFieldsModel;


	var init = function() {
        $issueTypeSelect.change();
        $templateId.filter(":checked").change();

        JIRA.UserAutoComplete.init();

		missingFieldModel.bind('change', function(e) {
			if (missingFieldModel.areFieldsMissing()) {
				AJS.$(".fields-missing-warning").show();
			} else {
				AJS.$(".fields-missing-warning").hide();
			}
		});

		missingFieldModel.set({"issueType":  $issueTypeSelect.val(), "collectData":  AJS.$("#recordWebInfo")[0].checked,
			"templateType":  $templateId.val()});

        requiredInvalidFields.bind('change',function(e) {
            var msgContainer = AJS.$(".ajax-msg-container");
            msgContainer.children().remove();
            var invalidFields = requiredInvalidFields.getInvalidFields();
            if (invalidFields.length > 0) {
                AJS.messages.error(msgContainer, {
                    title:AJS.I18n.getText("common.words.error"),
                    body: "<p>" + AJS.I18n.getText("collector.plugin.admin.error.not.allowed.fields",invalidFields.join(", ")) + "</p>",
                    closeable: false
                });
            }
        });

        requiredInvalidFields.set({"issueType":  $issueTypeSelect.val()});
	};

    $issueTypeSelect.change(function(e) {
        AJS.$(AJS.$.ajax({
            url: contextPath + "/secure/CreateFields!default.jspa?decorator=none&projectKey=" + AJS.params.projectKey + "&issueType=" + AJS.$(this).val(),
            contentType:"application/json",
            beforeSend: function() {
                AJS.$("#add-collector-submit").attr("disabled","true");
            },
            complete: function() {
                AJS.$("#add-collector-submit").removeAttr("disabled");
            },
            success: function(resp) {
                var required = false;
                AJS.$(resp.fields).each(function(index, field) {
                    if(field.required && field.id !== "summary") {
                        required = true;
                        return false;
                    }
                });
                if (required) {
                    $simpleTemplates.attr("disabled", "disabled").removeAttr("checked");
                    AJS.$("input.radio#template-custom").attr("checked", "checked").change();
                    AJS.$("#custom-template-only-info").show();
                } else {
                    $simpleTemplates.removeAttr("disabled");
                    AJS.$("#custom-template-only-info").hide();
                    $templateId.filter(":checked").change();
                }
            },
            error:function(response) {
                var msgContainer = AJS.$(".ajax-msg-container");
                AJS.messages.error(msgContainer, {
                    title:AJS.I18n.getText("collector.plugin.template.ajaxerror"),
                    body: "<p>" + AJS.I18n.getText("collector.plugin.template.ajaxerror.msg") + "</p>",
                    closeable: true
                });
            }
        })).throbber({target: AJS.$(".buttons-container").find(".throbber")});

		missingFieldModel.set({"issueType": e.currentTarget.value});
        requiredInvalidFields.set({"issueType": e.currentTarget.value});

    });

    $templateId.change(function(e) {
		var activeFieldIds = AJS.$(".custom-fields-container .draggable");
		if (activeFieldIds.length > 0) {
			fieldStore.updateActiveFieldIds(activeFieldIds);
		}
        var templateId = AJS.$(this).val();
        var $preview = AJS.$(".collector-preview");
        AJS.$.get(contextPath + "/rest/collectors/1.0/template/" + templateId + "?preview=true", function(data) {
            $preview.hide(); // avoid repaints / reflows while we adjust things.

            $preview.html(data);

            if(templateId === "custom") {
                fieldStore.refresh({issueType:$issueTypeSelect.val()});
            } else {
                fieldRenderer.hideFields();
            }

            $preview.find(".dialog-button-panel input").attr("disabled", "disabled");
            $preview.find(".dialog-button-panel a").removeAttr("href");
            fieldRenderer._makeFieldsNonInteractive($preview);

            //this reinserts the custom message
            AJS.$("#customMessage").keyup();

            $preview.show(); // repaint here.
        });

		missingFieldModel.set({"templateType":e.currentTarget.value})
    });

    AJS.$(".triggerPosition").change(function(e) {
        var position = AJS.$(this).val();
        if(position === "CUSTOM") {
            $previewContainer.hide();
            $customTrigger.removeClass("hidden");
        } else {
            $customTrigger.addClass("hidden");
            $previewContainer.show();
            $previewTrigger.removeClass("TOP RIGHT SUBTLE CUSTOM").addClass(position);
        }

    });


    var wikiTimeout = undefined;
    AJS.$("#customMessage").keyup(function(e) {
        if(wikiTimeout) {
            clearTimeout(wikiTimeout);
        }
        var text = AJS.$(this).val(), $customMsg = AJS.$(".collector-preview .content-body .custom-msg"),
                isEmpty = (AJS.$.trim(text) === "");

        if(isEmpty) {
            $customMsg.remove();
        } else if ($customMsg.length === 0) {
            $customMsg = AJS.$("<div class=\"aui-message info custom-msg\"><span class=\"aui-icon icon-info\"></span><p></p></div>");
            AJS.$(".collector-preview .content-body").prepend($customMsg);
        }

        if(!isEmpty) {
            wikiTimeout = setTimeout(function() {
                AJS.$(AJS.$.ajax({
                    url: contextPath + "/rest/api/1.0/render",
                    type:"post",
                    dataType:"html",
                    data: JSON.stringify({
                        "rendererType": "atlassian-wiki-renderer",
                        "unrenderedMarkup": text,
                        "issueKey": ""
                    }),
                    contentType: "application/json",
                    success:function(resp) {
                        $customMsg.find("p").html(resp);
                    }
                })).throbber({target: AJS.$("#customMessage").parent().find(".throbber")});
            }, 500);
        }
    });

    AJS.$(".triggerPosition:checked").change();

    AJS.$("#trigger-text").keyup(function(e) {
         $previewTrigger.text(AJS.$(this).val());
    }).keyup();

    AJS.$(fieldStore).bind("fieldsRefreshed", function() {
        fieldRenderer.render(fieldStore.getFields());
        fieldRenderer.renderStaticFields();
    });

    AJS.$("#add-collector-form").submit(function(e) {
        fieldStore.updateActiveFieldIds(AJS.$(".custom-fields-container .draggable"));
    });

	AJS.$("#recordWebInfo").change(function(e) {
		missingFieldModel.set({"collectData":e.currentTarget.checked});
	});

    init();
});

