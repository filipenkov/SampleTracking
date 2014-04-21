(function($){
    if (typeof JBAM === "undefined") {
        JBAM = {};
    }
    if (typeof JBAM.CONFIG === "undefined") {
        JBAM.CONFIG = {
            $planSelect: $(),
            $buildTypes: $(),
            buildResultsContainerId: "bamboo-build-results",
            stagesContainerId: "bamboo-plan-stages",
            variablesContainerId: "bamboo-override-variables",
            initialised: false,
            updateTimeout: null,
            init: function () {
                if (JBAM.CONFIG.initialised) { return; }

                $(document).bind("dialogContentReady", function (e) {
                    // Set up dialog contents when it opens
                    var buildResultsContainerId = JBAM.CONFIG.buildResultsContainerId,
                        stagesContainerId = JBAM.CONFIG.stagesContainerId,
                        variablesContainerId = JBAM.CONFIG.variablesContainerId;

                    JBAM.CONFIG.$planSelect = $("#bamboo-plan").change(JBAM.CONFIG.onPlanChange);

                    JBAM.CONFIG.BuildResults.init({
                        container: {
                            id: buildResultsContainerId,
                            insertAfter: JBAM.CONFIG.$planSelect.closest(".field-group")
                        }
                    });
                    JBAM.CONFIG.Stages.init({
                        container: {
                            id: stagesContainerId,
                            insertAfter: "#" + buildResultsContainerId
                        }
                    });
                    JBAM.CONFIG.Variables.init({
                        container: {
                            id: variablesContainerId,
                            insertAfter: "#" + stagesContainerId
                        }
                    });

                    JBAM.CONFIG.$buildTypes = $("input:radio[name='buildType']").change(JBAM.CONFIG.onBuildTypeChange).filter(":checked").change().end();
                });
                JBAM.CONFIG.initialised = true;
            }
        };

        /**
         * @class Spinner
         * @namespace JBAM.CONFIG
         * @constructor
         * @param container {String}, {DOMElement} or {jQuery Object} to append the spinner to
         * @param beforeShow {Function} to execute before spinner is shown
         */
        JBAM.CONFIG.Spinner = function (container, beforeShow) {
            this.beforeShow = beforeShow;
            this.add = function () {
                return $(AJS.template.load(JBAM.templates.icon).fill({ type: "loading" }).toString()).appendTo(container);
            };
        };
        JBAM.CONFIG.Spinner.prototype.show = function () {
            var $spinner = this.$spinner;

            this.beforeShow();
            if (!$spinner || ($spinner && (!$spinner.length || !$spinner.parent().length))) { // checks if spinner exists and is in DOM
                this.$spinner = this.add();
            } else {
                $spinner.show();
            }
            return this;
        };
        JBAM.CONFIG.Spinner.prototype.hide = function () {
            this.$spinner.hide();
            return this;
        };
    }

    JBAM.CONFIG.onPlanChange = function (e) {
        var isExistingBuild = $("#existing-build").is(":checked"),
            $select = $(this),
            $planSelectAndBuildTypes = $select.add(JBAM.CONFIG.$buildTypes).attr("disabled", true),
            $selectedPlan = $select.find("option:selected"),
            selectedBuildResult = (isExistingBuild ? $selectedPlan.attr("data-selected-build-result") : null),
            selectedStages = (isExistingBuild ? null : $selectedPlan.attr("data-selected-stages")),
            selectedVariables = (isExistingBuild ? null : $selectedPlan.attr("data-selected-variables")),
            requestUrl, requestData, successCallback;

        if (selectedVariables) {
            selectedVariables = $.parseJSON(selectedVariables);
        }

        JBAM.CONFIG.BuildResults.updateSelectedBuildResult(selectedBuildResult);
        JBAM.CONFIG.Stages.updateSelectedStages(selectedStages);
        JBAM.CONFIG.Variables.updateSelectedVariables(selectedVariables);
        JBAM.CONFIG.Variables.hide();

        if (isExistingBuild) {
            JBAM.CONFIG.Stages.hide();
            JBAM.CONFIG.BuildResults.showSpinner();

            requestUrl = JBAM.restURL + "history/" + JBAM.jiraProjectKey + "/" + $select.val() + "/5";
            requestData = { continuable: true, buildstate: "successful" };
            successCallback = function (data) {
                if (data.results.result && data.results.result.length) {
                    JBAM.CONFIG.BuildResults.show(data.results.result);
                } else {
                    JBAM.CONFIG.BuildResults.showNoResultsMsg();
                }
                $planSelectAndBuildTypes.attr("disabled", false);
            };
        } else {
            JBAM.CONFIG.BuildResults.hide();
            JBAM.CONFIG.Stages.showSpinner();

            requestUrl = JBAM.restURL + "plan/" + JBAM.jiraProjectKey + "/" + $select.val();
            successCallback = function (data) {
                if (data.stages.stage && data.stages.stage.length) {
                    JBAM.CONFIG.Stages.show(data.stages.stage);
                } else {
                    JBAM.CONFIG.Stages.showNoStagesMsg();
                }
                if (data.variableContext.variable && data.variableContext.variable.length) {
                    JBAM.CONFIG.Variables.show(data.variableContext.variable);
                }
                $planSelectAndBuildTypes.attr("disabled", false);
            };
        }

        $.ajax({
            url: requestUrl,
            dataType: "json",
            data: requestData,
            success: successCallback,
            error: function (xhr, textStatus) {
                var json, title, body, xhrResponse,
                    $newDialogContent = $('<div class="form-body"></div><div class="form-footer"><a class="cancel">Cancel</a></div>');

                $newDialogContent.find(".cancel").click(function (e) {
                    if (JBAM.CONFIG.FormDialog && JBAM.CONFIG.FormDialog.hide) {
                        JBAM.CONFIG.FormDialog.hide();
                    }
                });

                try {
                    xhrResponse = xhr.responseText || xhr.response;
                    if (xhrResponse) {
                        json = $.parseJSON(xhrResponse);
                        title = json.message;
                        body = (json.messageBody ? '<p>' + json.messageBody + '</p>' : null);
                        if (json.oauthCallback) {
                            $newDialogContent.filter(".content-footer").prepend('<a href="' + json.oauthCallback + '&amp;redirectUrl=' + encodeURIComponent(JBAM.baseURL) + '">Login &amp; Approve</a> | ');
                        }
                    } else {
                        throw "noJSONResponse";
                    }
                }
                catch (e) {
                    title = "An error occurred while trying to retrieve the build information.";
                }

                AJS.messages.warning($newDialogContent.filter(".form-body"), {
                    title: title,
                    body: body,
                    closeable: false
                });

                $select.closest("form").replaceWith($newDialogContent);
            }
        });
    };

    JBAM.CONFIG.onBuildTypeChange = function (e) {
        var $radio = $(this),
            $buildTypeFieldset = $radio.closest("fieldset.group"),
            $releaseBuildFields = $buildTypeFieldset.nextAll(".field-group,.group"),
            $buildResultsFieldset = $("#" + JBAM.CONFIG.buildResultsContainerId);

        switch ($radio.val()) {
            case "no-build":
                $releaseBuildFields.hide();
                $buildTypeFieldset.css({ // mimick AUI's form.aui fieldset:last-child so we don't get huge amounts of unnecessary whitespace
                    "margin-bottom": 0,
                    "padding-bottom": 0
                });
                break;
            case "new-build":
                $releaseBuildFields.show();
                $buildResultsFieldset.hide();
                $buildTypeFieldset.css({
                    "margin-bottom": "",
                    "padding-bottom": ""
                });
                JBAM.CONFIG.$planSelect.change();
                break;
            case "existing-build":
                $releaseBuildFields.show();
                $buildTypeFieldset.css({
                    "margin-bottom": "",
                    "padding-bottom": ""
                });
                JBAM.CONFIG.$planSelect.change();
                break;
        }
    };

    JBAM.CONFIG.Stages = (function () {
        var defaults = {
                container: {
                    id: null,
                    insertAfter: null,
                    i18n: {
                        legend: "Stages"
                    }
                },
                selectedStages: null
            },
            options,
            $container,
            containerInitialContent,
            createContainer = function () {
                $container = $("<fieldset/>", {
                    id: options.container.id,
                    "class": "group",
                    html: containerInitialContent
                })
                    .insertAfter(options.container.insertAfter)
                    .delegate("input:checkbox", "click", checkboxClick);
            },
            createStagesList = function (stages) {
                var hasManualStage = false,
                    $preselectedStage,
                    i, ii;

                for (i = 0, ii = stages.length; i < ii; i++) {
                    var stage = stages[i],
                        alreadyRun = !!(stage.state && stage.state != "Unknown"),
                        checked = !!(stage.manual),
                        disabled = !!(!stage.manual || alreadyRun),
                        $stage,
                        $input,
                        inputId = "stage-" + i,
                        $label;

                    if (!hasManualStage && stage.manual && !alreadyRun) {
                        hasManualStage = true;
                    }

                    $input = $('<input type="checkbox" />').attr({
                        name: "selectedStages",
                        id: inputId,
                        "class": "checkbox",
                        checked: checked,
                        disabled: disabled
                    }).val(stage.name);

                    $label = $('<label />', {
                        text: stage.name,
                        "for": inputId
                    });

                    $stage = $("<div/>", {
                        "class": "checkbox" + (stage.manual ? (alreadyRun ? " already-run" : " manual") : "") + (!hasManualStage ? " selected" : "")
                    })
                            .append($input)
                            .append($label)
                            .appendTo($container);

                    if (!alreadyRun && checked) {
                        $preselectedStage = $stage;
                    }
                }
                if ($preselectedStage) {
                    $preselectedStage.find("input:checkbox").trigger("click", true);
                }
            },
            removeStagesList = function () {
                $container.find("div.checkbox,p").remove();
            },
            checkboxClick = function (e, preventDefault) {
                if (preventDefault) { e.preventDefault(); }
                var $checkbox = $(this),
                    checked = $checkbox.is(":checked"),
                    prevManualFound = false;

                $checkbox.closest("div.checkbox").toggleClass("selected", checked).prevAll("div.checkbox").each(function (i) {
                    var $checkboxContainer = $(this),
                        $checkbox = $checkboxContainer.children("input:checkbox"),
                        isManual = $checkboxContainer.hasClass("manual");

                    if (checked) {
                        $checkboxContainer.addClass("selected");
                        if (isManual) {
                            $checkbox.attr("checked", "checked").attr("disabled", "disabled");
                        }
                    } else if (isManual && !prevManualFound) {
                        $checkbox.removeAttr("disabled");
                        prevManualFound = true;
                    }
                }).end().nextAll("div.checkbox").each(function (i) {
                    var $checkboxContainer = $(this);

                    if ($checkboxContainer.hasClass("manual")) {
                        return false; // breaks the loop
                    } else {
                        $checkboxContainer.toggleClass("selected", checked);
                    }
                });
            },
            spinner;
        return {
            init: function (opts) {
                options = $.extend(true, defaults, opts);
                containerInitialContent = '<legend><span>' + options.container.i18n.legend + '</span></legend>';
                createContainer();
                spinner = new JBAM.CONFIG.Spinner($container, removeStagesList);
            },
            show: function (stages) {
                $container.html(containerInitialContent).show();
                createStagesList(stages);
            },
            hide: function () {
                $container.hide().html(containerInitialContent);
            },
            showNoStagesMsg: function () {
                spinner.hide();
                $container.append($("<p/>", { text: "No stages found." }));
            },
            showSpinner: function () { spinner.show(); },
            updateSelectedStages: function (selectedStages) {
                options.selectedStages = selectedStages;
            }
        }
    })();

    JBAM.CONFIG.Variables = (function () {
        var defaults = {
                container: {
                    id: null,
                    insertAfter: null,
                    i18n: {
                        legend: "Build Variables"
                    }
                },
                selectedVariables: null
            },
            options,
            $container,
            $variableSelect,
            containerInitialContent,
            createContainer = function () {
                $container = $("<fieldset/>", {
                    id: options.container.id,
                    "class": "group",
                    html: containerInitialContent
                })
                    .insertAfter(options.container.insertAfter)
                    .delegate("#override-another", "click", addVariable)
                    .delegate(".bamboo-icon-variable-delete", "click", removeVariable)
                    .delegate("select", "change", keyChange);
            },
            createVariableSelect = function (variables) {
                var currentVariableType,
                    $currentOptGroup,
                    i, ii;

                $variableSelect = $('<select/>', { "class": "select" });

                for (i = 0, ii = variables.length; i < ii; i++) {
                    var variable = variables[i];

                    if (currentVariableType != variable.variableType) {
                        currentVariableType = variable.variableType;
                        $currentOptGroup = $('<optgroup/>', { label: currentVariableType }).appendTo($variableSelect)
                    }
                    $('<option/>', {
                        text: variable.key,
                        data: { "server-value": variable.value }
                    }).appendTo($currentOptGroup);
                }
            },
            addVariable = function () {
                var $variableDD = $variableSelect.clone(true),
                    $fieldGroup,
                    $selectedVariable,
                    $input,
                    inputValue = "";

                if (arguments.length == 2) {
                    $selectedVariable = $variableDD.find("option[text='" + arguments[0] + "']");
                    inputValue = arguments[1];
                } else {
                    $selectedVariable = $variableDD.find("option:first");
                }

                $selectedVariable.attr("selected", "selected");

                $input = $('<input type="text" />').attr({
                    name: 'variable_' + $selectedVariable.val(),
                    placeholder: $selectedVariable.data("server-value"),
                    "class": "text medium-field"
                }).val(inputValue);

                $fieldGroup = $('<div/>', {
                    "class": "field-group"
                })
                    .append($variableDD)
                    .append($input)
                    .append(AJS.template.load(JBAM.templates.icon).fill({ type: "variable-delete" }).toString())
                    .insertBefore($container.children("a"));
            },
            removeVariable = function (e) {
                $(this).closest(".field-group").remove();
            },
            keyChange = function (e) {
                var $variableDD = $(this),
                    $selectedVariable = $variableDD.find("option:selected");

                $variableDD.nextAll(".text").attr({
                    name: "variable_" + $selectedVariable.val(),
                    "placeholder": $selectedVariable.data("server-value")
                });
            };
        return {
            init: function (opts) {
                options = $.extend(true, defaults, opts);
                containerInitialContent = '<legend><span>' + options.container.i18n.legend + '</span></legend><a id="override-another">Override variable<span class="icon icon-edit-sml"></span></a>';
                createContainer();
            },
            show: function (variables) {
                createVariableSelect(variables);
                if (options.selectedVariables && options.selectedVariables.variables) {
                    $.each(options.selectedVariables.variables, addVariable); // adds a variable override for every preselected variable
                }
                $container.show();
            },
            hide: function () {
                $container.hide().html(containerInitialContent);
            },
            updateSelectedVariables: function (selectedVariables) {
                options.selectedVariables = selectedVariables;
            }
        }
    })();

    JBAM.CONFIG.BuildResults = (function () {
        var defaults = {
                container: {
                    id: null,
                    insertAfter: null,
                    i18n: {
                        legend: "Existing Build"
                    }
                },
                selectedBuildResult: null
            },
            options,
            $container,
            containerInitialContent,
            createContainer = function () {
                $container = $("<fieldset/>", {
                    id: options.container.id,
                    "class": "group",
                    html: containerInitialContent
                })
                    .insertAfter(options.container.insertAfter)
                    .delegate("input:radio", "change", radioChange)
                    .delegate("a", "click", function (e) {
                        e.preventDefault();
                        window.open(this.href);
                    });
            },
            addResult = function (buildResult) {
                var $field,
                    $input,
                    inputId = "build-result-" + buildResult.number,
                    $label,
                    status = (buildResult.lifeCycleState == "Finished" ? (buildResult.continuable ? "SuccessfulPartial" : buildResult.state) : buildResult.lifeCycleState);

                $input = $('<input type="radio" />').attr({
                    name: "buildResult",
                    id: inputId,
                    "class": "radio",
                    checked: (buildResult.key == options.selectedBuildResult)
                })
                    .val(buildResult.key)
                    .data("stages", (buildResult.stages.stage && buildResult.stages.stage.length ? buildResult.stages.stage : []));

                $label = $('<label />', {
                    html: '<a href="' + JBAM.bambooBaseURL + '/browse/' + buildResult.key + '" class="' + status + '">#' + buildResult.number + '</a><span class="trigger-reason">' + JBAM.replaceWithBambooIcon(buildResult.buildReason) + '</span>',
                    "for": inputId
                })
                    .prepend(AJS.template.load(JBAM.templates.icon).fill({ type: status }).toString());

                $field = $('<div/>', {
                    "class": "radio"
                })
                    .append($input)
                    .append($label)
                    .appendTo($container);
            },
            removeBuildResultsList = function () {
                $container.find("div.radio,p").remove();
            },
            radioChange = function (e) {
                var stages = $(this).data("stages");

                if (stages && stages.length) {
                    JBAM.CONFIG.Stages.show(stages);
                } else {
                    JBAM.CONFIG.Stages.showNoStagesMsg();
                }
            },
            spinner;
        return {
            init: function (opts) {
                options = $.extend(true, defaults, opts);
                containerInitialContent = '<legend><span>' + options.container.i18n.legend + '</span></legend>';
                createContainer();
                spinner = new JBAM.CONFIG.Spinner($container, removeBuildResultsList);
            },
            show: function (buildResults) {
                $container.html(containerInitialContent).show();
                for (var i = 0, ii = buildResults.length; i < ii; i++) {
                    addResult(buildResults[i]);
                }
            },
            hide: function () {
                $container.hide().html(containerInitialContent);
            },
            showNoResultsMsg: function () {
                spinner.hide();
                $container.append($("<p/>", { text: "No builds found with unbuilt manual stages." })).show();
            },
            showSpinner: function () { spinner.show(); },
            updateSelectedBuildResult: function (selectedBuildResult) {
                options.selectedBuildResult = selectedBuildResult;
            }
        }
    })();

    JBAM.CheckReleaseBuildStatus = function (buildResultKey, $duration) {
        $.ajax({
            url: JBAM.restURL + "status/" + JBAM.jiraVersionId + "/" + buildResultKey,
            dataType: "json",
            success: function (json) {
                var result = json.bamboo,
                    status = (result.lifeCycleState == "Finished" ? (result.continuable ? "SuccessfulPartial" : result.state) : result.lifeCycleState),
                    $buildDetails;

                if ((result.lifeCycleState != "InProgress" && result.lifeCycleState != "Queued") || (json.jira && json.jira.forceRefresh)) {
                    AJS.reloadViaWindowLocation();
                } else if (result.lifeCycleState == "InProgress" && result.progress) {
                    if (!$duration) {
                        $buildDetails = $("#build-details");
                        $duration = $(AJS.template.load(JBAM.templates.buildDetail).fill({
                            key: "Duration",
                            keyClass: "duration",
                            "value:html": result.progress.prettyBuildTime + ' &ndash; <span>' + result.progress.prettyTimeRemaining + '</span>'
                        }).toString()).appendTo($buildDetails).find("dd");
                        $buildDetails.find(".build + dd > span.bamboo-icon").replaceWith(AJS.template.load(JBAM.templates.icon).fill({ type: status }).toString());
                    } else {
                        $duration.html(result.progress.prettyBuildTime + ' &ndash; <span>' + result.progress.prettyTimeRemaining + '</span>');
                    }
                }
                clearTimeout(JBAM.CONFIG.updateTimeout);
                JBAM.CONFIG.updateTimeout = setTimeout(function () { JBAM.CheckReleaseBuildStatus(buildResultKey, $duration); }, 5000);
            },
            error: function (xhr, textStatus) {
                var json, title, body, xhrResponse,
                    $buildDetails = $("#build-details").empty();

                try {
                    xhrResponse = xhr.responseText || xhr.response;
                    if (xhrResponse) {
                        json = $.parseJSON(xhrResponse);
                        title = json.message;
                        body = (json.messageBody ? '<p>' + json.messageBody + '</p>' : "");
                        if (json.oauthCallback) {
                            body += '<p><a href="' + json.oauthCallback + '&amp;redirectUrl=' + encodeURIComponent(JBAM.baseURL) + '">Login &amp; Approve</a></p>';
                        }
                    } else {
                        throw "noJSONResponse";
                    }
                }
                catch (e) {
                    title = "An error occurred while trying to retrieve the build information.";
                }

                AJS.messages.warning($buildDetails, {
                    title: title,
                    body: body,
                    closeable: false
                });
            }
        });
    };

    JBAM.ShowBuildReleaseDetails = function (buildResultKey) {
        var $buildDetails = $("#build-details"),
            $buildDetailsLoadingIndicator = $(AJS.template.load(JBAM.templates.icon).fill({ type: "loading" }).toString());

        if ($buildDetails.length) {
            $buildDetailsLoadingIndicator.appendTo($buildDetails);
            $.ajax({
                url: JBAM.restURL + "status/" + JBAM.jiraVersionId + "/" + buildResultKey,
                dataType: "json",
                success: function (json) {
                    var result = json.bamboo,
                        status = (result.lifeCycleState == "Finished" ? (result.continuable ? "SuccessfulPartial" : result.state) : result.lifeCycleState),
                        labels = [], i, ii,
                        artifacts = [], j, jj,
                        $duration;

                    if (json.jira && json.jira.forceRefresh) {
                        AJS.reloadViaWindowLocation();
                    }
                    $buildDetailsLoadingIndicator.remove();

                    // Build status icon, number and name
                    $buildDetails.append(AJS.template.load(JBAM.templates.buildDetail).fill({
                        key: "Build",
                        keyClass: "build",
                        "value:html": AJS.template.load(JBAM.templates.icon).fill({ type: status }).toString() + ' <a href="' + JBAM.bambooBaseURL + '/browse/' + buildResultKey + '"><span class="' + status + '">#' + result.number + '</span> ' + result.planName + '</a>'
                    }).toString());

                    // Trigger reason
                    $buildDetails.append(AJS.template.load(JBAM.templates.buildDetail).fill({
                        key: "Trigger",
                        keyClass: "trigger",
                        "value:html": JBAM.replaceWithBambooIcon(result.buildReason)
                    }).toString());

                    // VCS revision
                    if (result.vcsRevisionKey) {
                        $buildDetails.append(AJS.template.load(JBAM.templates.buildDetail).fill({
                            key: "Revision",
                            keyClass: "revision",
                            "value:html": result.vcsRevisionKey
                        }).toString());
                    }

                    // Labels
                    if (result.labels.label && result.labels.label.length) {
                        for (i = 0, ii = result.labels.label.length; i < ii; i++) {
                            var label = result.labels.label[i];

                            labels.push(AJS.template.load(JBAM.templates.labelItem).fill({
                                name: label.name
                            }).toString());
                        }
                        $buildDetails.append(AJS.template.load(JBAM.templates.buildDetail).fill({
                            key: "Labels",
                            keyClass: "labels",
                            "value:html": '<ul class="labels">' + labels.join("") + '</ul>'
                        }).toString());
                    }

                    if (result.lifeCycleState == "InProgress" || result.lifeCycleState == "Queued") {
                        // Duration
                        if (result.lifeCycleState == "InProgress" && result.progress) {
                            $duration = $(AJS.template.load(JBAM.templates.buildDetail).fill({
                                key: "Duration",
                                keyClass: "duration",
                                "value:html": result.progress.prettyBuildTime + ' &ndash; <span>' + result.progress.prettyTimeRemaining + '</span>'
                            }).toString()).appendTo($buildDetails).find("dd");
                        }
                        clearTimeout(JBAM.CONFIG.updateTimeout);
                        JBAM.CONFIG.updateTimeout = setTimeout(function () { JBAM.CheckReleaseBuildStatus(buildResultKey, $duration); }, 5000);
                    } else if (result.lifeCycleState == "Finished") {
                        // Completed date/time
                        if (result.buildCompletedTime) {
                            $buildDetails.append(AJS.template.load(JBAM.templates.buildDetail).fill({
                                key: "Completed",
                                keyClass: "completed",
                                "value:html": '<time datetime="' + result.buildCompletedTime + '">' + result.prettyBuildCompletedTime + ' &ndash; <span>' + result.buildRelativeTime + '</span></time>'
                            }).toString());
                        }

                        // Duration
                        if (result.buildDuration) {
                            $buildDetails.append(AJS.template.load(JBAM.templates.buildDetail).fill({
                                key: "Duration",
                                keyClass: "duration",
                                "value:html": result.buildDurationDescription
                            }).toString());
                        }
                    } else if (result.lifeCycleState == "NotBuilt") {
                        // Started date/time
                        if (result.buildStartedTime) {
                            $buildDetails.append(AJS.template.load(JBAM.templates.buildDetail).fill({
                                key: "Started",
                                keyClass: "started",
                                "value:html": '<time datetime="' + result.buildStartedTime + '">' + result.prettyBuildStartedTime + '</time>'
                            }).toString());
                        }

                        // Completed date/time
                        if (result.buildCompletedTime) {
                            $buildDetails.append(AJS.template.load(JBAM.templates.buildDetail).fill({
                                key: "Completed",
                                keyClass: "completed",
                                "value:html": '<time datetime="' + result.buildCompletedTime + '">' + result.prettyBuildCompletedTime + ' &ndash; <span>' + result.buildRelativeTime + '</span></time>'
                            }).toString());
                        }

                        // Status
                        $buildDetails.append(AJS.template.load(JBAM.templates.buildDetail).fill({
                            key: "Status",
                            keyClass: "status",
                            "value:html": (result.buildStartedTime && !result.buildCompletedTime ? "Reason for not completing is unknown" : "Reason for not building is unknown")
                        }).toString());
                    }

                    // Artifacts
                    if (result.artifacts.artifact && result.artifacts.artifact.length) {
                        for (j = 0, jj = result.artifacts.artifact.length; j < jj; j++) {
                            var artifact = result.artifacts.artifact[j];

                            artifacts.push(AJS.template.load(JBAM.templates.artifactItem).fill({
                                id: artifact.name,
                                url: artifact.link.href,
                                name: artifact.name,
                                size: artifact.prettySizeDescription
                            }).toString());
                        }
                        $buildDetails.append(AJS.template.load(JBAM.templates.buildDetail).fill({
                            key: "Artifacts",
                            keyClass: "artifacts",
                            "value:html": '<ul id="shared-artifacts">' + artifacts.join("") + '</ul>'
                        }).toString());
                    }
                },
                error: function (xhr, textStatus) {
                    var json, title, body, xhrResponse;

                    $buildDetailsLoadingIndicator.remove();

                    try {
                        xhrResponse = xhr.responseText || xhr.response;
                        if (xhrResponse) {
                            json = $.parseJSON(xhrResponse);
                            title = json.message;
                            body = (json.messageBody ? '<p>' + json.messageBody + '</p>' : "");
                            if (json.oauthCallback) {
                                body += '<p><a href="' + json.oauthCallback + '&amp;redirectUrl=' + encodeURIComponent(JBAM.baseURL) + '">Login &amp; Approve</a></p>';
                            }
                        } else {
                            throw "noJSONResponse";
                        }
                    }
                    catch (e) {
                        title = "An error occurred while trying to retrieve the build information.";
                    }

                    AJS.messages.warning($buildDetails, {
                        title: title,
                        body: body,
                        closeable: false
                    });
                }
            });
        }
    };

    JBAM.replaceWithBambooIcon = function (original) {
        return original.replace(/(\b)icon(\b)/g, "$1bamboo-icon$2");
    };

    JBAM.init = (function () {
        JBAM.CONFIG.init();

        $(function () {
            var $releaseButton = $("#runRelease");

            if ($releaseButton.length) {
                JBAM.CONFIG.FormDialog = new JIRA.FormDialog({
                    trigger: $releaseButton,
                    widthClass: "large",
                    autoClose: true
                });

                if (/showReleaseDialog=true/.test(window.location.href)) {
                    $releaseButton.click();
                }
            }
        });
    })();
})(AJS.$);
