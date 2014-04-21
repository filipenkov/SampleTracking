/* Custom scripts for Embedded Crowd administration */
(function () {
    var $ = AJS.$;

    function getField($form, name) {
        return $("#" + $form.attr("id") + "-" + name, $form);
    }

    function setFieldEnabled(el, enabled) {
        if (enabled) {
            $(el).removeAttr("disabled");
        } else {
            $(el).attr("disabled", "disabled");
        }
    }

    function setFieldValue(el, value) {
        if ($(el).attr("type") == "checkbox") {
            $(el).attr("checked", value == "true" ? "checked" : null);
        } else {
            $(el).val(value);
        }
    }

    // get the directory config for the selected directory type from ldapTypes, found in a script tag in the body
    function getConfiguration(directoryType) {
        return typeof(ldapTypes) != "undefined" && ldapTypes[directoryType];
    }

    var fieldsWithDefaultsSet = []; // has to persist across calls to updateDefaults()
    function applyFieldDefaults($form, directoryType) {
        var config = getConfiguration(directoryType);
        if (!config) return;

        // clear all the previously set fields
        for (var i=0; i < fieldsWithDefaultsSet.length; i++) {
            setFieldValue(fieldsWithDefaultsSet[i], "");
        }
        fieldsWithDefaultsSet = [];

        // set the fields to their default values found in the configuration for this directory type
        for (var key in config.defaults) {
            if (config.defaults.hasOwnProperty(key)) {
                var id = key.replace(/-(.)/g, function () { return arguments[1].toUpperCase() });
                var $field = getField($form, id);
                if ($field.length) {
                    setFieldValue($field, config.defaults[key]);
                    fieldsWithDefaultsSet.push($field);
                }
            }
        }
        // Make sure the enablement of pagedResults is correct.
        var $pagedResults = getField($form, "ldapPagedresults");
        var $pageSize = getField($form, "ldapPagedresultsSize");
        setFieldEnabled($pageSize, $pagedResults.is(":checked"));
    }

    function applyHiddenFields($form, directoryType) {
        var config = getConfiguration(directoryType),
            id;
        if (!config) return;

        // show all previously hidden fields
        $(".hideable", $form).removeClass("hidden");

        // hide fields declared to be hidden in the configuration for this directory type
        for (var i=0; i< config.hidden.length; i++) {
            id = config.hidden[i].replace(/-(.)/g, function () { return arguments[1].toUpperCase() });
            getField($form, id).closest(".hideable").addClass('hidden');
        }
    }

    $(document).ready(function () {
        var $form = $("form", "#embcwd");
        var $type = getField($form, "type");

        AJS.InlineDialog($("button#new-directory", "#embcwd"), "new-directory", function ($popup, trigger, showPopup) {
            $("#new-directory-form").appendTo($popup).removeClass("hidden");
            $("#inline-dialog-new-directory").click(function (e) {
                e.stopPropagation();
            });
            showPopup();
        });
        // Make sure we close the dialog when esc is pressed
        $(document).keydown(function (e) {
            if ($("#inline-dialog-new-directory").is(":visible") && e.which == 27) {$("#inline-dialog-new-directory").hide();}
        });

        // enable or disable "page size" field based on whether "paged results" is checked
         var $pagedResults = getField($form, "ldapPagedresults");
         var $pageSize = getField($form, "ldapPagedresultsSize");
         $pagedResults.change(function () {
             setFieldEnabled($pageSize, $pagedResults.is(":checked"));
         });
         $form.bind("changed-type.embcwd", function () {
             setFieldEnabled($pageSize, $pagedResults.is(":checked"));
         });

        function isNewForm() {
            var directoryId = getField($form, "directoryId").val();
            var newForm = getField($form, "newForm").val();
            return (directoryId == "" || directoryId == "0") && (newForm == "" || newForm == "true");
        }

        function setNewForm(value) {
            var newForm = getField($form, "newForm");
            setFieldValue(newForm, value);
        }

        if (isNewForm()) {
            // if this is a new directory, we want to set default values based on Crowd's settings when changing types
            $form.bind("changed-type.embcwd", function () {
                applyFieldDefaults($(this), $type.val());
            });
            setNewForm("false");
        }
        // however, we always want to hide unnecessary fields, regardless of whether we're in edit or create mode
        $form.bind("changed-type.embcwd", function () {
            applyHiddenFields($(this), $type.val());
        });

        // trigger stuff that needs updating whenever the directory type is changed
        $form.trigger("changed-type.embcwd");
        $type.change(function () {
            $form.trigger("changed-type.embcwd");
        });

        // Handle checking/unchecking of SSL option
        getField($form, "useSSL").change(function () {
            getField($form, "port").val($(this).is(":checked") ? "636" : "389");
        });

        var $synchroniseGroupMemberships = getField($form, "synchroniseGroupMemberships"),
            isDelegatedLdapDirectory = $synchroniseGroupMemberships.length > 0;

        if (isDelegatedLdapDirectory) {
            function toggleSynchroniseGroupMembershipOptions() {
                $("#toggle-group-schema-settings").toggleClass("hidden", !$synchroniseGroupMemberships.is(":checked"));
                $("#toggle-membership-schema-settings").toggleClass("hidden", !$synchroniseGroupMemberships.is(":checked"));
            }
            $synchroniseGroupMemberships.change(toggleSynchroniseGroupMembershipOptions);
            toggleSynchroniseGroupMembershipOptions();

            // User Schema Settings and ldapAutoAddGroups are displayed only if
            // createUserOnAuth is checked in delegated ldap screen.
            var $createUserOnAuth = getField($form, "createUserOnAuth");
            function toggleCreateUserOnAuthOptions() {
                var $ldapAutoAddGroups = getField($form, "ldapAutoAddGroups"),
                    createUserOnAuthChecked = $createUserOnAuth.is(":checked");

                $("#user-schema-settings").toggleClass("hidden", !createUserOnAuthChecked);
                $createUserOnAuth.parent().find(".field-group").toggleClass("disabled-group", !createUserOnAuthChecked);

                if (createUserOnAuthChecked) {
                    $ldapAutoAddGroups.removeAttr("disabled");
                    $synchroniseGroupMemberships.removeAttr("disabled");
                } else {
                    $synchroniseGroupMemberships.attr("checked", false);
                    toggleSynchroniseGroupMembershipOptions();
                    $synchroniseGroupMemberships.attr("disabled", "disabled");
                    $ldapAutoAddGroups.attr("disabled", "disabled");
                }
            }
            $createUserOnAuth.change(toggleCreateUserOnAuthOptions);
            toggleCreateUserOnAuthOptions();
        } else {
            // ldapAutoAddGroups is displayed only if ldap directory has read only local groups permission.
            var $ldapPermissionOption = $("input[name='ldapPermissionOption']", $form);
            function toggleLdapAutoAddGroupsOptions() {
                $("#ldap-auto-add-groups-field-group", $form).toggleClass("hidden", !($ldapPermissionOption.filter(":checked").val() === "READ_ONLY_LOCAL_GROUPS"));
            }
            $ldapPermissionOption.change(toggleLdapAutoAddGroupsOptions);
            toggleLdapAutoAddGroupsOptions();
        }

        // section toggling
        $(".toggle-section", $form).each(function () {
            var $section = $(this);
            var $body = $section.find(".toggle-body");
            if ($body.find(".error").length > 0) {
                $section.removeClass("collapsed"); // show sections with validation errors
            }
            $section.find(".toggle-head").click(function () {
                if ($section.hasClass("updating")) return;
                $section.addClass("updating");
                if ($section.hasClass("collapsed")) {
                    $section.removeClass("collapsed updating");
                } else {
                    $section.addClass("collapsed").removeClass("updating");
                }
            });
        });

        var startTimer = function (id, xsrfTokenValue) {
            var intervalId = window.setInterval(function () {
                // Jira and Confluence use different methods to supply the context path AAhhhhhh!
                var path = AJS.General ? AJS.General.getContextPath() : contextPath;
                $.ajax({
                    "url": path + "/rest/crowd/1/directory/" + id,
                    "contentType": "application/json",
                    "dataType": "json",
                    "success": function (data) {
                        var currentStartTime = data.synchronisation.currentStartTime,
                            currentDuration = data.synchronisation.currentDuration,
                            lastStartTime = data.synchronisation.lastStartTime,
                            lastDuration = data.synchronisation.lastDuration,
                            syncStatus = data.synchronisation.syncStatus;

                        var templateData = {
                            "id": id,
                            "xsrfTokenValue": xsrfTokenValue,
                            "lastStartTime": lastStartTime,
                            "lastDuration": lastDuration,
                            "seconds": currentDuration
                        };

                        $(".sync-state.directory-"+id).text(syncStatus);

                        if (currentStartTime == 0) {
                            $(".sync-info.directory-"+id).replaceWith(AJS.template.load("synchronisation-complete").fill(templateData));
                            $(".sync-status.directory-"+id).replaceWith(AJS.template.load("synchronisation-link").fill(templateData));
                            $("a.sync.directory-"+id).click(function () {
                                synchronisationClick.call(this);
                                return false;
                            });
                            window.clearTimeout(intervalId);
                        } else {
                            $(".sync-status.directory-"+id).replaceWith(
                                    AJS.template.load("synchronisation-in-progress").fill(templateData)
                            );
                        }
                    }
                });
            }, 1000);
        };

        var synchronisationClick = function () {
            var op = $(this);
            var url = op.attr("href"), id = op.attr("data-id"), xsrfTokenValue = op.attr("data-xsrf-token-value");
            op.replaceWith(AJS.template.load("synchronisation-started").fill({id:id}));
            $.ajax({
                "url": url,
                "success": function () {
                    startTimer(id, xsrfTokenValue);
                }
            });
        };

        $("a.sync").click(function () {
            synchronisationClick.call(this);
            return false;
        });

        $(".sync-status.in-progress").each(function () {
            var op = $(this);
            var id = op.attr("data-id");
            op.replaceWith(AJS.template.load("synchronisation-started").fill({id:id}));
            startTimer(id);
        });

    });
})();

