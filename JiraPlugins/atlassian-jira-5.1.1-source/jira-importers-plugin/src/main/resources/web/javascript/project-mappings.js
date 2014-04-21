(function() {

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
				// making ajaxOptions a function, as otherwise I wouldn't have the access to this.option which is not initialized before
				// _getDefaultOptions is finished
				ajaxOptions: function() {
					// new add project dialog (as of JIRA 5.0.3) will auto-propose project key, which screw our existing key in edit mode
					var isEditingProject = this.options.editedModel ? "true" : "false";
					return {
						url: contextPath + "/secure/admin/AddProject!default.jspa",
						data: {
							inline: true,
							decorator: "dialog",
							keyEdited: isEditingProject
						}
					}
				},
				submitAjaxOptions: {
					type: "post",
					dataType: "json",
					url: "../../rest/jira-importers-plugin/1.0/validation/validateProject"
				},
				onDialogFinished: function() {},
				onContentRefresh: function() {
					AJS.$("#add-project-hint").remove();
					var form = this.$form;
					form.attr("action", "../../rest/jira-importers-plugin/1.0/validation/validateProject");

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

			this._setFieldError(nameField, data.projectName);
			this._setFieldError(keyField, data.projectKey);
			this._setFieldError(leadField, data.projectLead);
			var noErrors = this._verifyAlreadyUsed(nameField, keyField, this.options.editedModel);
			if (noErrors && data.valid) {
				var newModel = this.updateProjectData(this.options.editedModel, data.key, data.name, data.lead);
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
		},
		// without this we will end up with multiple (invisible) rubbish <div> elements for each opened dialog (every cancel and edit makes it even worse)
		// that makes testing on ids very difficult
		hide: function (undim, options) {
			if (this._super(undim, options) === false) {
				return false;
			}
			this.get$popup().remove();
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
            this.canCreate = this.options.canCreate;
			this.projectSelect = new importer.NextButtonAwareSingleSelect({
				element: this.$(".project_select"),
				overlabel: "Select a project",
				itemAttrDisplayed: "label",
				mappingModel: this.model
			});

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
					this.projectSelect.selectByKey(projectModel.cid);
				}
			} else {
				this.editLink.css("visibility", "hidden");
				this.iconRequired.toggle(selected);
				this.projectSelect.$field.val("");
				this.projectSelect.clearSelection();
			}

			this.projectSelect.updateOverlabel();

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
				select.selectByKey(previousCId);
				new importer.NewProjectDataDialog({
					mappingModel: this.model,
					projectsModel: this.options.projectsModel,
					id: "jim-create-project-dialog"
				}).show();
			} else {
				var selectedModel = item.properties.projectModel;
				this.model.set({projectModel: selectedModel});
			}
		},

		projectUnselected: function () {
			this.model.unset("projectModel");
		},

		projectEdit: function () {
			var popup = new importer.NewProjectDataDialog({
				editedModel: this.model.get("projectModel"),
				mappingModel: this.model,
				projectsModel: this.options.projectsModel,
				id: "jim-create-project-dialog"
			});
			popup.show();
		},

		_enableProjectImport: function () {
			this.projectSelect.enable();
			this.$(".error").show(); // created dynamically, don't store
		},

		_disableProjectImport: function () {
			this.projectSelect.disable();
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

			this.projectSelect.model.appendOptionsFromJSON(this.canCreate ? [groupNew, groupProjects] : [groupProjects]);

			var selectedDescriptor = this.projectSelect.getSelectedDescriptor();
			if (changedModel && selectedDescriptor && selectedDescriptor.value() == changedModel.cid) {
				this.projectSelect.setSelection(selectedDescriptor); // so the name gets updated
			}
		}

	});
})();