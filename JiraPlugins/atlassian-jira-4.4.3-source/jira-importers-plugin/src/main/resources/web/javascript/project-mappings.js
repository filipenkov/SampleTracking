(function() {
	importer.NextButtonAwareSingleSelect = AJS.SingleSelect.extend({
		submitForm: function () {
			if (!this.suggestionsVisible) {
            	this.handleFreeInput();
				AJS.$("#nextButton:enabled").click();
        	}
		}
	});

	importer.ProjectModel = Backbone.Model.extend({
	});

	importer.MappingModel = Backbone.Model.extend({
	});


	// Expected options: projectsModel, editedModel(optional), mappingModel
	importer.NewProjectDataDialog = JIRA.FormDialog.extend({
		// Override
		_getDefaultOptions: function() {
			return AJS.$.extend(this._super(), {
				type: "ajax",
				ajaxOptions: {
					url: contextPath + "/secure/admin/AddProject!default.jspa",
					data: {
                    	inline: true,
                    	decorator: "dialog"
                	}
				},
				submitAjaxOptions: {
					type: "post",
					dataType: "json",
					url: contextPath + "/rest/jira-importers-plugin/1.0/validation/validateProject"
				},
				url: '/', // force absolute AJAX url for submit
				onDialogFinished: function() {},
				onContentRefresh: function() {
					AJS.$("#add-project-hint").remove();
					var form = this.$form;
					form.attr("action", contextPath + "/rest/jira-importers-plugin/1.0/validation/validateProject");

					if (this.options.editedModel) {
						form.find("input[name=name]").val(this.options.editedModel.get("name"));
						form.find("input[name=key]").val(this.options.editedModel.get("key"));
					}
				}
			});
		},

		// override
		_setContent: function(content, decorate) {
			var selectedUser = content && this.options.editedModel && this.options.editedModel.get("lead");
			if (selectedUser) {
				content = AJS.$("<div>" + content + "<div>")
						.find("select#lead")
						.empty()
						.append('<option selected="selected" value="' + selectedUser + '" title="">' + selectedUser + '</option>')
						.end()
						.html();
			}
			this._super(content, decorate);
		},

		updateProjectData: function(editedModel, key, name, lead) {
			var map = { key: key, name: name, lead: lead, editable: true };
			if (editedModel) {
				editedModel.set(map);
				return editedModel;
			} else {
				var model = new importer.MappingModel(map);
				this.options.projectsModel.add(model);
				return model;
			}
		},

		// override
		// this is where REST call ends - validate + "OK" action
 		_handleServerSuccess: function (data, xhr, textStatus, smartAjaxResult) {
			var form = this.$form;
			var nameField = form.find("input[name=name]");
			var keyField = form.find("input[name=key]");
			var leadField = form.find("[name=lead]");

			this._setFieldError(nameField, data['projectName']);
			this._setFieldError(keyField, data['projectKey']);
			this._setFieldError(leadField, data['projectLead']);
			var noErrors = this._verifyAlreadyUsed(nameField, keyField, this.options.editedModel);
			if (noErrors && AJS.$.isEmptyObject(smartAjaxResult.data)) {
				var leadVal = leadField.val();
				var lead = _.isArray(leadVal) ? leadVal.pop() : leadVal;
				var newModel = this.updateProjectData(this.options.editedModel, keyField.val(), nameField.val(), lead);
				this.options.mappingModel.set({projectModel: newModel});
				this.hide();
			} else {
				form.find(":submit").removeAttr("disabled");
			}
		},

		_setFieldError: function(field, message) {
			field.siblings("div.error").remove();
			if (message) {
				field.after('<div class="error">' + message + '</div>');
			}
		},

		// verifies repeated name/key wrt newly created projects, except for Edit mode (editedKey != false)
		// to be done on top of server-side verification
		_verifyAlreadyUsed: function(nameField, keyField, editedModel) {
			var hasErrors = false;
			var name = nameField.val();
			var key = keyField.val();
			var error = this._setFieldError;
			this.options.projectsModel.forEach( function(val) { // existing projects handled in AJAX
				if (val == editedModel) {
					return; // this one is going away on OK anyway
				}
				if (val.get("key") == key) {
					error(keyField, "Key already used for a new project. Choose the project from the drop down or pick a different key.")
					hasErrors = true;
				}
				if (val.get("name") == name) {
					error(nameField, "Name already used for a new project. Choose the project from the drop down or pick a different name.")
					hasErrors = true;
				}
			});
			return !hasErrors;
		}
	});

	importer.ProjectMappingView = Backbone.View.extend({
		events: {
			"click    .project_edit"	: "projectEdit",
			"selected .project_select"	: "projectSelected",
			"unselect .project_select"	: "projectUnselected"
		},

		initialize: function () {
			_.bindAll(this, "render", "_projectDetailsChange", "submitCleanup");
			this.projectsModel = this.options.projectsModel;
			this.projectSelect = new importer.NextButtonAwareSingleSelect({
				element: this.$(".project_select"),
				overlabel: "Select a project",
				itemAttrDisplayed: "label",
				mappingModel: this.model
			});
			this.projectSelect.$field.select(function() {return false;}); // disable ugly text selection

			this.editLink = this.$(".project_edit");
			this.iconRequired = this.$(".aui-icon.icon-required");

			this.model.bind("change", this.render);
			this.projectsModel.bind("add", this._projectDetailsChange);
			this.projectsModel.bind("change", this._projectDetailsChange);

			AJS.$(this.el).parents("form").bind("submit", this.submitCleanup);

			this._projectDetailsChange();
		},

		render: function () {
			var selected = this.model.get("selected");
			selected ? this._enableProjectImport() : this._disableProjectImport();

			var projectModel = this.model.get("projectModel");
			if (projectModel) {
				this.editLink.css("visibility", selected && projectModel.get("editable") ? "visible" : "hidden");
				this.iconRequired.hide();
				var previousDescriptor = this.projectSelect.getSelectedDescriptor();
				var previousModel = previousDescriptor && previousDescriptor.properties.projectModel;
				if (projectModel != previousModel) {
					var selectedDesctiptor = this.projectSelect.model.getDescriptor(projectModel.cid);
					this.projectSelect.setSelection(selectedDesctiptor);
				}
			} else {
				this.editLink.css("visibility", "hidden");
				this.iconRequired.toggle(selected);
				this.projectSelect.$field.val("");
				this.projectSelect.clearSelection();
			}

			return this;
		},

		submitCleanup: function () {
			if (!this.projectSelect.getSelectedDescriptor()) {
				this.model.unset("projectModel", {silent: true});
			}
		},

		projectSelected: function (event, item, select) {
			if ("create-new" == item.value()) {
				var previousCId = (this.model.get("projectModel") || {}).cid;
				if (previousCId) {
					select.setSelection(select.model.getDescriptor(previousCId));
					select.$field.focus(); // hack for overlabel not disappearing
				} else {
					select.$field.val("");
					select.clearSelection();
				}
				new importer.NewProjectDataDialog({
					mappingModel: this.model,
					projectsModel: this.options.projectsModel
				}).show();
			} else {
				var selectedModel = item.properties.projectModel;
				this.model.set({projectModel: selectedModel});
				select.$field.focus();
			}
		},

		projectUnselected: function () {
			this.model.unset("projectModel");
		},

		projectEdit: function () {
			var popup = new importer.NewProjectDataDialog({
				editedModel: this.model.get("projectModel"),
				mappingModel: this.model,
				projectsModel: this.options.projectsModel
			});
			popup.show();
		},

		_enableProjectImport: function () {
			this.projectSelect.$field.removeAttr("disabled");
			this.projectSelect.enable();
			this.projectSelect._assignEvents("dropdownAndLoadingIcon", this.projectSelect.$dropDownIcon);
			this.$(".error").show(); // created dynamically, don't store
		},

		_disableProjectImport: function () {
			this.projectSelect.$field.attr("disabled", "disabled");
			this.projectSelect.disable();
			this.projectSelect._unassignEvents("dropdownAndLoadingIcon", this.projectSelect.$dropDownIcon);
			this.$(".error").hide();
		},

		_projectDetailsChange: function (changedModel) {
			var groupNew = new AJS.GroupDescriptor({
				showLabel: false,
				type: "optgroup",
				weight: 0,
				items: [ new AJS.ItemDescriptor({
					value: "create-new",
					label: "Create New"
				}) ]
			});

			var groupProjects = new AJS.GroupDescriptor({
				label: "Compatible Projects",
				type: "optgroup",
				weight: 1,
				items: this.projectsModel.map(function (model) {
					var suffix = " [" + model.get("key") + "]";
					var item = new AJS.ItemDescriptor({
						value: model.cid,
						label: model.get("name"),
						keywords: model.get("key"),
						projectModel: model
					});
					item.labelSuffix = function () { return suffix; };
					return item;
				})
			});

			this.projectSelect.model.appendOptionsFromJSON([groupNew, groupProjects]);

			var selectedDescriptor = this.projectSelect.getSelectedDescriptor();
			if (changedModel && selectedDescriptor && selectedDescriptor.value() == changedModel.cid) {
				this.projectSelect.setSelection(selectedDescriptor); // so the name gets updated
			}
		}

	});
})();