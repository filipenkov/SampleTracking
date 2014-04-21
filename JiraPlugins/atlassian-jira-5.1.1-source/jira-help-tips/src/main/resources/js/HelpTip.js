(function($) {
    function nope() { return false; }
    function yep() { return true; }

    var cidCounter = 0, seed = new Date().getTime();
    var restUrl = AJS.contextPath() + "/rest/helptips/1.0/tips";

    // to un-fuck graphite event names
    function cleanAnalyticsName(name) {
        name = "" + (name || "");
        return name.replace(/\./g, "-");
    }

    function analytics(eventId, model) {
        if (AJS.EventQueue && model && model.attributes.id) {
            var event = {};
            var cleanId = cleanAnalyticsName(model.attributes.id);
            event.name = "helptips." + cleanId + "." + eventId;
            event.properties = {};
            AJS.EventQueue.push(event);
        }
    }

    function getCid() {
        return "c" + seed + (cidCounter++);
    }

    var Manager = {
        dismissedTipIds: [],
        loaded: $.Deferred(),
        url: function() { return restUrl; },
        sync: function(verb, data) {
            verb || (verb = "get");
            data || (data = null);
            return $.ajax(this.url(), {
                type: verb,
                context: this,
                dataType: "json",
                contentType: "application/json",
                data: data && JSON.stringify(data),
                processData: false
            }).promise();
        },
        fetch: function() {
            var result = this.sync();
            result.done(function(response) {
                $.merge(this.dismissedTipIds, response);
                this.loaded.resolve();
            });
            result.fail(AJS.log);
            return result.promise();
        },
        show: function(showFunction) {
            this.loaded.done(showFunction);
        },
        dismiss: function(tip) {
            var id = tip.attributes.id;
            if (!id) {
                tip._dismissed = true;
            } else {
                this.dismissedTipIds.push(id);
                this.sync("post", {id:id});
            }
        },
        undismiss: function(tip) {
            var id = tip.attributes.id;
            if (!id) {
                tip._dismissed = false;
            } else {
                this.dismissedTipIds.splice($.inArray(id, this.dismissedTipIds), 1);
                this.sync("delete", {id:id});
            }
        },
        isDismissed: function(tip) {
            var id = tip.attributes.id;
            return (id) ? $.inArray(id, this.dismissedTipIds) >= 0 : tip._dismissed;
        }
    };

    var HelpTip = AJS.HelpTip = function(attributes) {
        var anchor;
        this.attributes = $.extend({}, attributes);
        this.attributes.id || (this.attributes.id = false);
        this.cid = getCid();
        anchor = this.attributes['anchor'];
        delete this.attributes['anchor'];

        this.view = (anchor) ? new AnchoredView(this, anchor) : new UnanchoredView(this);
        this.view.$el.addClass('aui-help-tip');
    };

    AJS.HelpTip.Manager = Manager;

    $.extend(HelpTip.prototype, {
        show: function() {
            var self = this;
            AJS.HelpTip.Manager.show(function() {
                if (!self.isDismissed()) {
                    self.view.show();
                    analytics("shown", self);
                }
            });
        },
        dismiss: function() {
            var reason = cleanAnalyticsName(arguments[0] || "programmatically");
            this.view.dismiss();
            if (!this.isDismissed()) {
                AJS.HelpTip.Manager.dismiss(this);
                analytics("dismissed." + reason, this);
            }
        },
        isVisible: function() {
            return this.view.$el.is(":visible");
        },
        isDismissed: function() {
            return AJS.HelpTip.Manager.isDismissed(this);
        }
    });

    var AnchoredView = function(model, anchor) {
        this.initialize(model, anchor);
    };

    $.extend(AnchoredView.prototype, {
        initialize: function(model, anchor) {
            var self = this;
            this.model = model;
            this.beforeHide = nope;
            this.closeButton = $(AJS.Templates.HelpTip.tipClose());
            this.closeButton.click(function(e) {
                model.dismiss("close-button");
                e.preventDefault();
            });
            this.popup = AJS.InlineDialog(anchor, model.cid, function(content, trigger, show) {
                content.html(AJS.Templates.HelpTip.tipContent(model.attributes));
                content.prepend(self.closeButton);
                content.unbind('mouseover mouseout');
                content.find(".helptip-link").click(function() {
                    analytics("learn-more.clicked", model);
                });
                show();
            }, {
                noBind: true,
                preHideCallback: function() { return self.beforeHide() }
            });
            this._popupHide = this.popup.hide;
            this.popup.hide = nope;
            this.$el = $(this.popup[0]);
            AJS.$(document).bind("showLayer",function(e,type,layer) {
                if (type === "inlineDialog" && layer.id === model.cid) {
                    AJS.InlineDialog.current = null; // Tips shouldn't be considered InlineDialogs.
                    AJS.$(document.body).unbind("click."+model.cid+".inline-dialog-check");
                    layer._validateClickToClose = nope;
                    layer.hide = nope;
                }
            });
        },
        show: function() {
            this.popup.show();
        },
        dismiss: function() {
            this.beforeHide = yep;
            this._popupHide();
        }
    });

    var UnanchoredView = function(model) {
        this.initialize(model);
    };

    $.extend(UnanchoredView.prototype, {
        initialize: function() {
            this.$el = $("<div></div>");
        },
        show: function() { },
        dismiss: function() { }
    });

    // Load up the user's dismissed tips.
    Manager.fetch();
})(AJS.$);