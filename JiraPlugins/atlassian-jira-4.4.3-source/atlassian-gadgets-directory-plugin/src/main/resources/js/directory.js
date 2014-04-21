AJS.toInit(function($) {
    var subscribedGadgetFeedsUrl = AJS.params["subscribedGadgetFeedsUrl"];
    var confirmRemoveGadgetFeedText = AJS.params["confirmRemoveSubscriptionToGadgetFeed-1"] + "\n\n"
                                    + AJS.params["confirmRemoveSubscriptionToGadgetFeed-2"];
    
    var resolveApplicationFeed = function(baseUri) {
        return (baseUri.charAt(baseUri.length - 1) == "/" ? baseUri : baseUri + "/") + "rest/gadgets/1.0/g/feed";
    };

    var highlight = function(element) {
        $(element).css("background-color", "#fffe83").animate({ backgroundColor: "transparent" }, 5000, "linear", function() {
            $(this).css("background-color", "");
        });
    };

    var highlightFeed = function(feedUrl) {
        highlight($("#gadget-feed-subscriptions li a[href=" + feedUrl + "]").parent());
    };

    // Subscribe to a new gadget feed
    var subscribeToGadgetFeed = function() {
        if ($('#add-gadget-feed-subscription-url').val() != "") {
            var originalButtonValue = $('#add-subscription-submit').val(),
                newFeedUrl = resolveApplicationFeed($('#add-gadget-feed-subscription-url').val());
            $('#add-subscription-submit').attr("disabled", "disabled").val(AJS.params["adding"]);
            var xhr = $.ajax({
                type: "POST",
                url: subscribedGadgetFeedsUrl,
                data: JSON.stringify({
                    url: newFeedUrl
                }),
                contentType: "application/json",
                processData: false,
                success: function(directory) {
                    displaySubscribedGadgetFeeds(function() {
                        highlightFeed(newFeedUrl);
                    });
                    $('#add-gadget-feed-subscription-url').val("");
                    $('#add-subscription-submit').val(originalButtonValue).removeAttr("disabled");
                },
                error: function(request, textStatus, errorThrown) {
                    if (request.status == 403 || request.status == 401) {
                        showError(AJS.params["dashboardErrorDirectoryPermissions"]);
                    } else {
                        showError(AJS.params["dashboardErrorFailedToAddGadgetSubscription"]);
                    }
                    $('#add-subscription-submit').val(originalButtonValue).removeAttr("disabled");
                }
            });

            $(xhr).throbber({target: $("#add-subscription-throbber")});
        }
    };
    
    var removeSubscriptionToGadgetFeed = function(feedResource, success) {
        $.ajax({
            type: "DELETE",
            url: feedResource,
            success: success,
            error: function(request, textStatus, errorThrown) {
                if (request.status == 403 || request.status == 401) {
                    showError(AJS.params["dashboardErrorDirectoryPermissions"]);
                } else {
                    showError(AJS.params["dashboardErrorFailedToRemoveGadgetSubscription"]);
                }
            }
        });
    };

    var displaySubscribedGadgetFeeds = function(onLoad) {
        $("#gadget-feed-subscriptions ul").empty();
        var xhr = $.ajax({
            type: "GET",
            url: subscribedGadgetFeedsUrl,
            dataType: "json",
            processData: false,
            success: function(result) {
                if (result.feeds) {
                    $.each(result.feeds, function() {
                        var applicationRepresentation;
                        if (this.icon) {
                            applicationRepresentation = $("<img/>")
                                .attr("src", this.icon)
                                .attr("width", "16")
                                .attr("height", "16")
                                .attr("alt", this.name)
                                .attr("title", this.name)
                                .addClass("app");
                        } else {
                            applicationRepresentation = $("<span/>")
                                .addClass("app")
                                .text(this.name)
                        }
                        $("#gadget-feed-subscriptions ul")
                            .append($("<li></li>")
                                .append(applicationRepresentation)
                                .append($(" <a></a>")
                                    .attr("href", this.feed.href)
                                    .attr("title", this.feed.href)
                                    .text(this.base ? AJS.format(AJS.params["subscribedFeedDescription"], this.base) : this.title)
                                )
                                .append($("<a></a>")
                                    .attr("href", this.self.href)
                                    .attr("title", AJS.params["unsubscribeFromFeed"])
                                    .addClass("remove")
                                    .text(AJS.params["unsubscribeFromFeed"])
                                    .click(function(event) {
                                        if (confirm(confirmRemoveGadgetFeedText)) {
                                            var li = $(this).parent();
                                            removeSubscriptionToGadgetFeed(this.href, function() {
                                                li.remove();
                                            });
                                        }
                                        return AJS.stopEvent(event);
                                    })
                                )
                            );
                    });
                    if ($.isFunction(onLoad)) {
                        onLoad();
                    }
                }
            },
            error: function(request, textStatus, errorThrown) {
                if (request.status == 403 || request.status == 401) {
                    showError(AJS.params["dashboardErrorDirectoryPermissions"]);
                } else {
                    showError(AJS.params["dashboardErrorFailedToGetGadgetSubscriptions"]);
                }
            }
        });
        $(xhr).throbber({target: $("#gadget-subscriptions-throbber")});
        $("#gadget-feed-subscriptions").removeClass("hidden");
        $("#add-gadget-feed-subscription").addClass("hidden");
    };

    var showError = function(message) {
        alert(message); // TODO: AG-95 show this in the UI, not an alert.
    };
    
    $("button.add").click(function() {
        $("#gadget-feed-subscriptions").addClass("hidden");
        $("#add-gadget-feed-subscription").removeClass("hidden");
        $('#add-gadget-feed-subscription-url').focus();
    });
        
    $("button.back").click(displaySubscribedGadgetFeeds);
    
    // when the user hits 'enter' in the text box, click the add-subscription-submit button
    $('#add-gadget-feed-subscription').keydown(function(e) {
       if (e.keyCode == 13) {
           $('#add-subscription-submit').click();
       }
    });
    // Button for adding a gadget by URL
    $('#add-subscription-submit').click(function() {
        subscribeToGadgetFeed();
    });

    displaySubscribedGadgetFeeds();
})(AJS.$);
