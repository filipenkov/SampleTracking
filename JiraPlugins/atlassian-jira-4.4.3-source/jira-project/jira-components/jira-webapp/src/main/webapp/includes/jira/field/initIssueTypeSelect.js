(function () {


    var IssueTypePicker = Class.extend({

        init: function (options) {

            var val,
                instance = this;

            this.$projectSelect = options.projectSelect;
            this.$refIssueTypeSelect = jQuery(options.issueTypeSelect).clone(true, true);
            this.$issueTypeSelect = options.issueTypeSelect;
            this.$projectIssueTypesSchemes = options.projectIssueTypesSchemes;
            this.$issueTypeSchemeIssueDefaults = options.issueTypeSchemeIssueDefaults;
            this.projectIssueTypeSchemes = this.getProjectIssueTypeSchemesFromDom();
            this.issueTypesSchemeDefaults = this.getIssueTypeSchemeIssueDefaults();


            //may not have a project select on the edit issue page!
            if(instance.$projectSelect.length > 0) {
                val = instance.$projectSelect.val();
                instance.setIssueTypeScheme(instance.getIssueTypeSchemeForProject(val));

                this.$projectSelect.change(function () {
                    var val = instance.$projectSelect.val();
                    instance.setIssueTypeScheme(instance.getIssueTypeSchemeForProject(val));
                });
            }
        },

        getIssueTypeSchemeForProject: function (projectId) {
            return this.projectIssueTypeSchemes[projectId];
        },

        getDefaultIssueTypeForScheme: function (issueTypeSchemeId) {
            return this.issueTypesSchemeDefaults[issueTypeSchemeId];
        },

        setIssueTypeScheme: function (issueTypeSchemeId) {

            var instance = this;

            this.$issueTypeSelect.empty();

            this.$refIssueTypeSelect.find("optgroup").each(function () {
                var $optgroup = jQuery(this);
                if ($optgroup.is("[data-scheme-id='" + issueTypeSchemeId + "']")) {
                    instance.$issueTypeSelect.append($optgroup.clone(true).children());
                    return false;
                }
            });


            if (this.$refIssueTypeSelect.find("[selected='selected']").length === 0) {
                this.setDefaultIssueType(this.getDefaultIssueTypeForScheme(issueTypeSchemeId));
            }
        },

        setDefaultIssueType: function (defaultIssueType) {
            this.$issueTypeSelect.find("option[value='" + defaultIssueType + "']"  ).attr("selected", "selected");
        },

        getProjectIssueTypeSchemesFromDom: function () {

            var projectIssueTypes = {};

            this.$projectIssueTypesSchemes.find("input").each(function (i, input) {
                var $input = jQuery(input),
                    project = $input.attr("title"),
                    issueTypes = $input.val();

                projectIssueTypes[project] = issueTypes;
            });

            return projectIssueTypes;
        },

        getIssueTypeSchemeIssueDefaults: function () {
            var issueTypesSchemeDefaults = {};

            this.$issueTypeSchemeIssueDefaults.find("input").each(function (i, input) {
                var $input = jQuery(input),
                    issueTypeScheme = $input.attr("title"),
                    defaultIssueType = $input.val();

                issueTypesSchemeDefaults[issueTypeScheme] = defaultIssueType;
            });

            return issueTypesSchemeDefaults;
        }

    });

    function findProjectAndIssueTypeSelectAndConvertToPicker(ctx) {

        var $ctx = ctx || jQuery("body"),
            $projectSelect = $ctx.find("#project"),
            $issueTypeSelect = $ctx.find("#issuetype"),
            $projectIssueTypes = $ctx.find("#project-issue-types"),
            $defaultProjectIssueTypes = $ctx.find("#default-project-issue-type");


        new IssueTypePicker({
            projectSelect: $projectSelect,
            issueTypeSelect: $issueTypeSelect,
            projectIssueTypesSchemes: $projectIssueTypes,
            issueTypeSchemeIssueDefaults: $defaultProjectIssueTypes
        })
    }


    AJS.$(function() {
        findProjectAndIssueTypeSelectAndConvertToPicker();
    });

    AJS.$(document).bind("dialogContentReady", function(e, dialog) {
        findProjectAndIssueTypeSelectAndConvertToPicker(dialog.get$popupContent());
    });


})();
