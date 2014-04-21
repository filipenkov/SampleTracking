// Create the menu
AJS.$(function()
{
    AJS.$("#main-nav li").each(function () {

        var $this = AJS.$(this),
            trigger = $this.hasClass("admin-menu-link") ? $this : AJS.$(".drop", this);

        if ($this.hasClass("lazy")) {
            AJS.Dropdown.create({
                alignment: AJS.LEFT,
                offsetTarget: $this,
                trigger: trigger,
                styleClass: "main-nav-dropdown",
                ajaxOptions: {
                    url: contextPath + "/rest/api/1.0/menus/" + trigger.attr("rel"),
                    dataType: "json",
                    cache: false,
                    formatSuccess: JIRA.FRAGMENTS.menuFragment
                }
            });
        } else if ($this.hasClass("nonlazy")) {
            AJS.Dropdown.create({
                alignment: AJS.LEFT,
                offsetTarget: $this,
                styleClass: "main-nav-dropdown",
                trigger: trigger,
                content: $this.find(".aui-list")
            });
        }
    });

    AJS.Dropdown.create({
        alignment: AJS.RIGHT,
        trigger: AJS.$("#header-details-user .drop"),
        content: AJS.$("#user-options-list")
    });

    AJS.$("#admin-quicknav-trigger").click(function(e) {
        e.preventDefault();
        jira.app.adminQuickNavDialog.show();
    });
});


// Create Issue Dialog
AJS.$(function()
{
    AJS.$("#create_link_params").each(function()
    {
        var $this = AJS.$(this);
        var params = {};
        AJS.$(this).find("input").each(function()
        {
            var $this = AJS.$(this);
            params[$this.attr("id")] = $this.val();
        });

        // this is set during the inti callback
        var hideFunction = function()
        {};

        var createDialog = AJS.InlineDialog(AJS.$("#" + $this.attr("rel")), "create_issue_popup", function(contents, trigger, doShowPopup)
        {


            var displayContent = function(args)
            {
                contents.css({
                    width: "auto",
                    minWidth: "150px"
                }).parent().addClass("active").click(function (e) {
                    e.stopPropagation();
                });

                var projects;
                var recentProjects;
                var project;
                var types;
                var type;

                var getProject = function(pid)
                {
                    var proj;
                    AJS.$(projects).each(function()
                    {
                        if (this.id === parseInt(pid))
                        {
                            proj = this;
                            return false;
                        }
                    });
                    return proj;
                };

                var initializeProject = function()
                {
                    projects = args.projects;
                    recentProjects = args.recentProjects;
                    var projectId;

                    if (recentProjects)
                    {
                        project = recentProjects[0];
                    }
                    else if (projects)
                    {
                        project = projects[0];
                    }
                }();

                var getType = function(typeId)
                {
                    var type;
                    AJS.$(types).each(function()
                    {
                        if (this.id === typeId)
                        {
                            type = this;
                            return false;
                        }
                    });

                    return type;
                };

                var initializeIssueTypes = function()
                {
                    types = args.types;
                    var issueTypeId = args.defaultType;
                    if (!issueTypeId || issueTypeId === "-1")
                    {
                        issueTypeId = types[0].id;
                    }
                    type = getType(issueTypeId);
                    if (!type)
                    {
                        type = types[0];
                    }

                    AJS.$(types).each(function()
                    {
                        if (!/^http/.test(this.url))
                        {
                            this.url = contextPath + this.url;
                        }
                    });
                }();

                var getScheme = function(schemeId)
                {
                    var scheme;
                    AJS.$(args.schemes).each(function()
                    {
                        if (this.id === schemeId)
                        {
                            scheme = this;
                            return false;
                        }
                    });
                    return scheme;
                };

                var setTypesForProject = function(project, defaultType)
                {
                    var scheme = getScheme(project.scheme);
                    AJS.$("#quick-issuetype").empty();
                    var isValidType = false;
                    var createTypeUnknownOption = AJS.$("<option />");

                    createTypeUnknownOption
                        .val("-1")
                        .text(params.createItem_issuetype_select)
                        .appendTo("#quick-issuetype");

                    type = getType("-1");


                    AJS.$(scheme.types).each(function()
                    {
                        var newType = getType(this + "");
                        if (newType)
                        {
                            var option = AJS.$("<option/>").val(newType.id).text(newType.name).css("background-image", "url(" + newType.url + ")");
                            if ((!defaultType && newType.id === scheme.defaultId) || (newType.id === defaultType))
                            {
                                type = newType;
                                option.attr("selected", "selected");
                                isValidType = true;
                            }
                            AJS.$("#quick-issuetype").append(option);
                        }
                    });
                    if (isValidType) {
                        createTypeUnknownOption.remove();
                    }
                };
                contents.empty().append(
                        AJS.$('<form id="issue-create-quick" />').addClass("aui top-label").attr("action", contextPath + "/secure/CreateIssue.jspa").append(
                                AJS.$("<fieldset>").append(
                                        AJS.$('<div class="field-group"/>').append(
                                                AJS.$("<label/>").attr("for", "quick-pid").text(params.createItem_project + ":")
                                                ).append(
                                                AJS.$("<select/>").addClass("imagebacked").addClass("select").attr({
                                                    id: "quick-pid",
                                                    name: "pid",
                                                    title: params.createItem_select_project
                                                }).change(function()
                                                {
                                                    AJS.$("#invalid-type").hide();
                                                    var pid = AJS.$("#quick-pid").val();
                                                    project = getProject(pid);
                                                    setTypesForProject(project);
                                                })
                                                )
                                        ).append(
                                        AJS.$('<div class="field-group"/>').append(
                                                AJS.$("<label/>").attr("for", "quick-issuetype").text(params.createItem_issuetype + ":").append(
                                                        AJS.$("<span/>").attr("id", "invalid-type").addClass("hidden error").text(params.createItem_issuetype_invalid)
                                                        )
                                                ).append(
                                                AJS.$("<select/>").addClass("imagebacked").addClass("select").attr({
                                                    id: "quick-issuetype",
                                                    name: "issuetype",
                                                    title: params.createItem_select_type
                                                }).change(function()
                                                {
                                                    var typeId = AJS.$("#quick-issuetype").val();
                                                    type = getType(typeId);
                                                    AJS.$("#invalid-type").hide();
                                                })
                                                )
                                        )
                                ).append(
                                AJS.$('<div class="buttons-container" />')
                                    .append(
                                        AJS.$('<div class="buttons" />').append(
                                            AJS.$("<input type='submit' />").addClass("button").addClass("save").attr({
                                                title: params.createItem_create_desc,
                                                name: "Create",
                                                id: "quick-create-button"
                                            }).val(params.createItem_create)
                                        ).append(
                                            AJS.$("<a/>").attr("href", "#").attr("id", "quick-create-cancel").text(params.createItem_cancel).click(function(e)
                                            {
                                                hideFunction();
                                                e.preventDefault();
                                            })
                                        )
                                    )
                                ).submit(function(e)
                                {
                                    if (!type)
                                    {
                                        AJS.$("#invalid-type").show();
                                        return false;
                                    }
                                })
                        );
                var parent = AJS.$("#quick-pid");
                var first = true;
                if (recentProjects)
                {
                    var group = AJS.$("<optgroup/>").attr("label", params.createItem_project_recent);
                    AJS.$(recentProjects).each(function()
                    {
                        var option = AJS.$("<option/>").val(this.id).text(this.name).css("background-image", "url(" + contextPath + "/secure/projectavatar?size=small&pid=" + this.id + "&avatarId=" + this.img + ")");
                        if (first)
                        {
                            option.attr("selected", "selected");
                            first = false;
                            setTypesForProject(project, args.defaultType);
                        }
                        group.append(option);
                    });
                    parent.append(group);
                    group = AJS.$("<optgroup/>").attr("label", params.createItem_project_all);
                    parent.append(group);
                    parent = group;
                }

                AJS.$(projects).each(function()
                {
                    var option = AJS.$("<option/>").val(this.id).text(this.name).css("background-image", "url(" + contextPath + "/secure/projectavatar?size=small&pid=" + this.id + "&avatarId=" + this.img + ")");
                    if (first)
                    {
                        option.attr("selected", "selected");
                        first = false;
                        setTypesForProject(project, args.defaultType);
                    }
                    parent.append(option);

                });
                contents.parent().addClass("aui-dialog-content-ready");
            };

            var successHandler = function(args)
            {

                if (!args.isEmpty)
                {
                    displayContent(args);
                    if (AJS.InlineDialog.current) {
                        AJS.InlineDialog.current.reset();
                    }
                }
                else
                {
                    if (args.currentUser)
                    {
                        //no permission
                        contents.html(
                                AJS.$("<div/>").addClass("message").append(
                                        AJS.$("<p/>").text(params.createItem_no_permission)
                                        )
                                );
                    }
                    else
                    {
                        //log in
                        contents.html(
                                AJS.$("<div/>").addClass("message").append(
                                        AJS.$("<p/>").text(params.createItem_log_in_desc)
                                        ).append(
                                        AJS.$("<p/>").append(
                                                AJS.$("<a/>").attr("href", contextPath + "/login.jsp?os_destination=%2Fsecure%2FCreateIssue%21default.jspa").text(params.createItem_log_in)
                                                )
                                        )
                                );
                    }

                    contents.append(
                            AJS.$("<div/>").addClass("button-panel").append(
                                    AJS.$("<a/>").attr("href", "#").attr("id", "quick-create-cancel").text(params.createItem_cancel).click(function(e)
                                    {
                                        hideFunction();
                                        e.preventDefault();
                                    })
                                    )
                            );
                }

                if (AJS.InlineDialog.current) {
                    AJS.InlineDialog.current.reset();
                }
            };


            var displayLoading = function()
            {

                contents.empty();
                contents.append(
                        AJS.$("<div/>").addClass("loading-small").css("height", 50)
                        );

                doShowPopup();

                AJS.$.ajax({
                    type: "GET",
                    url: contextPath + "/rest/api/1.0/admin/issuetypeschemes",
                    data: {
                        includeRecent: true
                    },
                    dataType: "json",
                    success: successHandler,
                    error: function (XMLHttpRequest, textStatus, errorThrown)
                    {
                        window.location = contextPath + "/secure/CreateIssue!default.jspa";
                    }
                });
            }();

        },
        {
            width:250,
            cacheContent: false,
            initCallback: function()
            {
                hideFunction = this.hide;
            },
            hideCallback: function () {
                this.popup.removeClass("active").removeClass("aui-dialog-content-ready");
                // JIRADEV-328: safari needs this otherwise keyboard commands will not work as a field is still in focus.
                if (AJS.$.browser.safari) {
                    jQuery(":input").blur();
                }
                JIRA.setFocus.popConfiguration();
            }
        });
    });
});


AJS.$(document).bind("showLayer", function (e, type, inlineLayer) {
    if (type === "inlineDialog" && AJS.InlineDialog.current.id === "create_issue_popup") {
        // HACK - we dont have an event for the end of the reset function in InlineDialog
        // when AJS-459 is done recode this.
        AJS.InlineDialog.current.reset = (function (reset) {
            return function () {
                reset.call(this);
                var triggerConfig = new JIRA.setFocus.FocusConfiguration();
                triggerConfig.context = inlineLayer.popup[0];
                triggerConfig.focusElementSelector = '#quick-create-button';
                JIRA.setFocus.pushConfiguration(triggerConfig);
                JIRA.setFocus.triggerFocus();
            };
        })(AJS.InlineDialog.current.reset);
    }
});

AJS.$(function() {
    var adminSummaryLink = AJS.$("#admin_summary");

    if (adminSummaryLink) {
        adminSummaryLink.parent().click(function (event) {
            window.location = adminSummaryLink.attr("href");
        });
    }
});