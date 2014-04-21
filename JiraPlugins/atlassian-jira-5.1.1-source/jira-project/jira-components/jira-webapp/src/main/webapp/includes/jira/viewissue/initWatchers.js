AJS.$(function () {

    /**
     * Backbone model to add and remove watchers on server
     */
    var WatcherModel = Backbone.Model.extend({

        // From Offical Rest API:  https://developer.atlassian.com/static/rest/jira/5.0.html#id200116
        url: [AJS.contextPath(), "rest/api/2/issue", AJS.Meta.get("issue-key"), "watchers"].join("/"),

        initialize: function () {
            this.set("canBrowseUsers", AJS.Meta.get("can-search-users"));
            this.set("isReadOnly", !AJS.Meta.get("can-edit-watchers"));
        },

        /**
         * Attempts to parse raw response to JSON. If parsing fails, shows a global error message and returns null
         * @param responseText raw http response data
         */
        _parseResponse: function(responseText) {
            try {
                return JSON.parse(responseText);
            } catch (e) {
                // parse JSON failed
                this._showFatalErrorMessage();
                return null;
            }
        },

        _showFatalErrorMessage: function() {
            // TODO: would be nice to extract this error from smartAjax and make it uniform in JIRA
            var msg = '<p>' + AJS.I18n.getText("common.forms.ajax.error.dialog.heading") + '</p>' +
                    '<p>' + AJS.I18n.getText("common.forms.ajax.error.dialog") + '</p>';
            JIRA.Messages.showErrorMsg(msg, {
                closeable: true
            });
        },

        getUser: function (username) {
            return AJS.$.ajax({
                url: AJS.contextPath() + "/rest/api/2/user?username=" + username,
                error:_.bind(function (xhr) {
                    if (xhr.status !== 404) {
                        this.handleErrorResponse(xhr, AJS.I18n.getText("issue.operations.watching.add.error"));
                    }
                }, this)
            });
        },

        handleErrorResponse: function (xhr, msg) {
            var errorCollection = this._parseResponse(xhr.responseText);
            if (errorCollection.errorMessages) {
                var html = JIRA.Templates.Issue.error({
                    msg: msg,
                    errors: errorCollection.errorMessages
                });
                JIRA.Messages.showErrorMsg(html, {
                    closeable: true
                });
            }
            this.trigger("errorOccurred");
        },

        /**
         * Adds watcher by username
         *
         * @param username
         * @return {jQuery.Deferred}
         */
        addWatcher: function (username) {
            return AJS.$.ajax({
                type: "post",
                url: this.url,
                global: false,
                data: JSON.stringify(username),
                contentType: "application/json",
                error:_.bind(function (xhr) {
                    this.handleErrorResponse(xhr, AJS.I18n.getText("issue.operations.watching.add.error"));
                }, this)
            });
        },

        /**
         * Removes watcher by username
         *
         * @param username
         * @return {jQuery.Deferred}
         */
        removeWatcher: function (username) {
            return AJS.$.ajax({
                type: "delete",
                global: false,
                url: this.url + "?username=" + username,
                error:_.bind(function (xhr) {
                    this.handleErrorResponse(xhr, AJS.I18n.getText("issue.operations.watching.remove.error"));
                }, this)
            });
        }
    });

    var AbstractWatchersView = Backbone.View.extend({

        /**
         * @constructor
         */
        initialize: function () {
            _.bindAll(this);
        },

        renderNoWatchers: function () {
            if (this.$(".recipients li").length === 0) {
                var $empty = AJS.$("<div class='aui-message info watchers-empty' />").html("<span class='aui-icon icon-info' /> "+ AJS.I18n.getText("watcher.manage.nowatchers"));
                this.$("fieldset").append($empty);
            } else {
                this.$(".watchers-empty").remove();
            }
        },

        /**
         * Goes to server to get watchers before rendering contents
         *
         * @return {*}
         */
        render: function () {
            var deferred = jQuery.Deferred();
            this.model.fetch().done(_.bind(function () {
                this._render();
                this.renderNoWatchers();
                deferred.resolve(this.$el);
                window.setTimeout(_.bind(function () {
                    this.focus();
                }, this), 0)
            }, this));
            return deferred.promise();
        },

        watch: function () {
            AJS.$("#watchers-val .action-text").text(AJS.I18n.getText("common.concepts.watching"));
        },

        unwatch: function () {
            AJS.$("#watchers-val .action-text").text(AJS.I18n.getText("common.concepts.watch"));
        },

        // implemented by subclasses
        focus: AJS.$.noop,

        /**
         * Increments watcher count by 1
         * @private
         */
        _incrementWatcherCount: function () {
            var $el = AJS.$("#watcher-data");
            var currentCount = parseInt($el.text(), 10);
            $el.text(currentCount + 1);
            this.renderNoWatchers();
        },

        /**
         * Decrements watcher count by 1
         * @private
         */
        _decrementWatcherCount: function () {
            var $el = AJS.$("#watcher-data");
            var currentCount = parseInt($el.text(), 10);
            $el.text(currentCount - 1);
            this.renderNoWatchers();
        }
    });

    /**
     * View to handles internal content of inline dialog
     *
     * @type {*}
     */
    var WatchersView = AbstractWatchersView.extend({

        events: {
            selected: "addWatcherToModel",
            unselect: "removeWatcherFromModel"
        },

        /**
         * Renders contents. Should only be called when watchers have been fetched.
         * @private
         */
        _render: function () {
            this.$el.html(JIRA.Templates.Issue.watchersWithBrowse(this.model.toJSON()));
            var picker = new JIRA.MultiUserListPicker({
                element: this.$el.find(".watchers-user-picker"),
                width: 220
            });
        },

        /**
         * Focuses input field
         */
        focus: function () {
            this.$el.find("#watchers-textarea").focus();
        },

        /**
         * Adds watcher on server
         * @param e
         * @param descriptor
         */
        addWatcherToModel: function (e, descriptor) {
            e.preventDefault();
            this.model.addWatcher(descriptor.value()).done(_.bind(function () {
                this._incrementWatcherCount();
                if (descriptor.value() === AJS.Meta.get("remote-user")) {
                    this.watch();
                }
            }, this));
        },

        /**
         * Removes watcher on server
         * @param e
         * @param descriptor
         */
        removeWatcherFromModel: function (e, descriptor) {
            this.model.removeWatcher(descriptor.value()).done(_.bind(function () {
                this._decrementWatcherCount();
                if (descriptor.value() === AJS.Meta.get("remote-user")) {
                    this.unwatch();
                    JIRA.trace("jira.issue.watcher.deleted");
                }
            }, this));
        }
    });

    var WatchersNoBrowseView = AbstractWatchersView.extend({

        events: {
            "click .remove-recipient" : "removeWatcher",
            "submit" : "addWatcher"
        },

        addWatcher: function (e) {
            e.preventDefault();
            this.removeInlineError();
            var $field = AJS.$("#watchers-nosearch");
            var username = AJS.$.trim(AJS.$("#watchers-nosearch").val());
            $field.attr("disabled", "disabled");
            if (this.hasUsername(username)) {
                $field.removeAttr("disabled");
                this.showInlineError(AJS.I18n.getText("watching.manage.user.already.watching", username));
                $field.val("");
            } else {
                this.model.getUser(username).done(_.bind(function (data) {
                    var html = JIRA.Templates.Fields.recipientUsername({
                        icon: data.avatarUrls["16x16"],
                        username: data.name,
                        displayName: data.displayName
                    });
                    if (username === AJS.Meta.get("remote-user")) {
                        this.watch();
                    }
                    $field.val("");
                    this.$(".watchers").append(html);
                    this.model.addWatcher(data.name);
                    this._incrementWatcherCount();
                }, this)).fail(_.bind(function (xhr) {
                    if (xhr.status === 404) {
                        this.showInlineError(AJS.I18n.getText("admin.viewuser.user.does.not.exist.title"));
                    }
                }, this)).always(function () {
                    $field.removeAttr("disabled").focus();
                });
            }

        },

        hasUsername: function (username) {
            var result = false;
            this.$(".watchers li").each(function () {
                if (AJS.$(this).attr("data-username") === username) {
                    result = true;
                    return false;
                }
            });
            return result;
        },

        removeInlineError: function () {
            this.$(".error").remove();
        },

        showInlineError: function (msg) {
            AJS.$("<div />").addClass("error").text(msg).insertAfter(this.$(".description"));
        },

        focus: function () {
            AJS.$("#watchers-nosearch").focus();
        },

        removeWatcher: function (e) {
            e.preventDefault();
            var $item = AJS.$(e.target).closest("li");
            var username = $item.attr("data-username");
            if (username) {
                $item.remove();
                this.model.removeWatcher(username);
                this._decrementWatcherCount();
                if (username === AJS.Meta.get("remote-user")) {
                    this.unwatch();
                }
            }
            JIRA.trace("jira.issue.watcher.deleted");
        },
        _render: function () {
            this.$el.html(JIRA.Templates.Issue.watchersNoBrowse(this.model.toJSON()));
        }
    });

    var WatchersReadOnly = AbstractWatchersView.extend({
        _render: function () {
            this.$el.html(JIRA.Templates.Issue.watchersReadOnly(this.model.toJSON()));
        }
    });


    function getView(model) {
        if (model.get("isReadOnly")) {
            return WatchersReadOnly;
        } else if (model.get("canBrowseUsers")) {
            return WatchersView;
        } else {
            return WatchersNoBrowseView;
        }
    }



    // Wire up inline dialog to our Backbone view
    var dialog = AJS.InlineDialog("#view-watcher-list", "watchers", function (contents, trigger, doShowPopup) {
        AJS.$("#watchers-val .icon:first").addClass("loading");
        var model = new WatcherModel();
        var view = getView(model);
        new view({
            model: model
        }).render().done(function (viewHtml) {
            contents.html(viewHtml);
            contents.find(".cancel").click(function (e) {
                dialog.hide();
                e.preventDefault();
            });
            AJS.$("#watchers-val .icon:first").removeClass("loading");
            doShowPopup();
        });
        model.on("errorOccurred", function () {
            dialog.hide();
        });
    },
    {
        width:240,
        useLiveEvents: true,
        items: "#view-watcher-list",
        preHideCallback: function () {
            return !AJS.InlineLayer.current; // Don't close if we have inline layer shown
        }
    });

    AJS.$(document).bind("keydown", function (e) {
        // special case for when user hover is open at same time
        if (e.keyCode === 27 && AJS.InlineDialog.current != dialog && dialog.is(":visible")) {
            if (AJS.InlineDialog.current) {
                AJS.InlineDialog.current.hide();
            }
            dialog.hide();
        }
    });

    //this is a hack, but it's necessary to stop click on the multi-select autocomplete from closing the
    //inline dialog. See JRADEV-8136
    AJS.$(document).bind("showLayer", function(e, type, hash) {
        if(type && type === "inlineDialog" && hash && hash.id && hash.id === "watchers") {
            AJS.$("body").unbind("click.watchers.inline-dialog-check");
        }
    });
});