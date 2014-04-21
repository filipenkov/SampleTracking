AJS.MacroBrowser = {
    // clones the element specified the selector and removes the id attribute
    // TODO: move this to AJS?
    clone: function(selector) {
        return AJS.$(selector).clone().removeAttr("id");
    }
};

AJS.toInit(function($) {
    var GADGET_BROWSER_PAGE = 0, 
        EXTERNAL_DIRECTORIES_PAGE = 2,
        ADD_EXTERNAL_DIRECTORY_PAGE = 3;

    AJS.activeColumn = 0;

    // Don't initialize if User can't modify Dashboard
    if (!AG.param.get("writable")) {
        return;
    }

    var dashboardDirectoryResourceUrl = AG.param.get("dashboardDirectoryResourceUrl");
    var dashboardSubscribedGadgetFeedsUrl = AG.param.get("dashboardSubscribedGadgetFeedsUrl");
    var dashboardResourceUrl = AG.param.get("dashboardResourceUrl");
    var errorStatus = AG.param.get("defaultErrorMessage");
    var removeButtonText = AG.param.get("removeFromDirectory");
    var browserContentLoading = false;
    var browserContentLoaded = false;
    var localeLang = AJS.$('.locale-lang','#i18n-settings').val();
    var localeCountry = AJS.$('.locale-country','#i18n-settings').val();

    var addGadgetToDashboard = function(gadgetUrl, callbacks) {
        var reqParams = {
            type: "POST",
            url: dashboardResourceUrl + ".json?" + (localeLang ? "locale.lang="+localeLang : "") + (localeCountry ? "&locale.country="+localeCountry : ""),
            contentType: "application/json",
            dataType: "json",
            data: JSON.stringify({
                url: gadgetUrl,
                columnIndex: AJS.activeColumn
            }),
            processData: false
        };
        $.extend(reqParams, callbacks);
        $.ajax(reqParams);
    };

    var insertGadgetIntoLiveDashboard = function(data) {
        // clear gadget frameheight cookie before adding a new gadget
        AG.Cookie.erase("gadget-" + data.id + "-fh");

        AG.DashboardManager.addGadget(data, AJS.activeColumn);
    };

    var callbacks = function(extraData, success, error) {
        return {
            success: function(data, textStatus) {
                success(extraData, data, textStatus);
            },
            error: function(request, textStatus, errorThrown) {
                error(extraData, request, textStatus, errorThrown);
            }
        };
    };

    var addingEntry = function(button) {
        var button = $(button);

        // re-enable all gadget's  buttons
        $('.macro-button-add').removeAttr("disabled");
        $('.macro-button-remove').removeAttr("disabled");
        button.val(AG.param.get("addItNow"));
        button.parents(".macro-list-item").removeAttr("style");
        button.blur();
    };

    var addGadgetToDashboardSuccess = function(button, data, textStatus) {
        insertGadgetIntoLiveDashboard(data);
        var button = $(button);
        button.parents(".macro-list-item").css("background-color", "#fffe83").animate({
            backgroundColor: "transparent"
        },
        2000, "linear",
        function() {
            addingEntry(button);
        });
    };

    var addGadgetToDashboardError = function(button, request, textStatus, errorThrown) {
        console.log("addItNowError = " + errorThrown);
        $(button).removeClass("macro-button-add").addClass("macro-button-add-broken");
        $(button).siblings('.macro-button-remove').removeClass("macro-button-remove").addClass("macro-button-remove-broken");
        $(button).parents(".macro-list-item").css("background-color", "#faa");

        // re-enable other gadget's  buttons
        $('.macro-button-add').removeAttr("disabled");
        $('.macro-button-remove').removeAttr("disabled");
        button.blur();
    };

    var removeGadgetFromDirectorySuccess = function(button, data, textStatus) {
        showError(AG.param.get("gadgetRemovedSuccess"));
        var button = $(button);
        button.parents(".macro-list-item").remove();
    };

    var removeGadgetFromDirectoryError = function(button, request, textStatus, errorThrown) {
        if (request.status == 403 || request.status == 401) {
            showError(AG.param.get("dashboardErrorDirectoryPermissions"));
        } else {
            showError(AG.param.get("dashboardErrorFailedToAddGadget"));
        }
        console.log("addItNowError = " + errorThrown);
        $(button).siblings('.macro-button-add').removeClass("macro-button-add").addClass("macro-button-add-broken");
        $(button).removeClass("macro-button-remove").addClass("macro-button-remove-broken");
        $(button).parents(".macro-list-item").css("background-color", "#faa");
        button.val(AG.param.get("removeFromDirectory"));
        button.blur();
    };

    var findGadgetsByUrl = function(urls) {
        if (!AJS.$.isArray(urls)) {
            urls = new Array(urls);
        }
        var gadgetSelector = $.map(urls, function(url) {
            return ".macro-hidden-uri[value='"+ url +"']";
        }).join(',');
        return $(gadgetSelector).parents('.macro-list-item');
    };
    
    // Add a new Gadget to Directory by URL
    var addGadgetToDirectory = function() {
        if ($('#add-gadget-url').val() != "") {
            $('#add-gadget-submit').attr("disabled", "disabled").val(AG.param.get("adding"));
            var xhr = $.ajax({
                type: "POST",
                url: dashboardDirectoryResourceUrl,
                data: JSON.stringify({
                    url: $('#add-gadget-url').val()
                }),
                contentType: "application/json",
                processData: false,
                success: function() {
                    var gadgetUrl = $('#add-gadget-url').val();

                    clearBrowser();
                    loadBrowser(false, function() {
                        scrollToGadget(findGadgetsByUrl(gadgetUrl));
                        $('#add-gadget-url').val("");
                        $('#add-gadget-submit').val(AG.param.get("addGadget")).removeAttr("disabled");
                    });
                },
                error: function(request, textStatus, errorThrown) {
                    if (request.status == 403 || request.status == 401) {
                        showError(AG.param.get("dashboardErrorDirectoryPermissions"));
                    } else {
                        var errorMsg = AG.param.get("dashboardErrorFailedToAddGadget");
                        if(request.responseText) {
                            errorMsg += "\n\n" + request.responseText;
                        }
                        showError(errorMsg);
                    }
                    $('#add-gadget-url').select();
                    $('#add-gadget-submit').val(AG.param.get("addGadget")).removeAttr("disabled");
                }
            });

            $(xhr).throbber({target: $("#dir-throbber")});
        }
    };

    var resolveApplicationFeed = function(baseUri) {
        return (baseUri.charAt(baseUri.length - 1) == "/" ? baseUri : baseUri + "/") + "rest/gadgets/1.0/g/feed";
    };

    var highlightFeed = function(feedUrl) {
        highlight($("#gadget-feed-subscriptions li a[href='" + feedUrl + "']").parent());
    };
    
    // Subscribe to a new gadget feed
    var subscribeToGadgetFeed = function() {
        if ($('#add-gadget-feed-subscription-url').val() != "") {
            var originalButtonValue = $('#add-subscription-submit').val(),
                newFeedUrl = resolveApplicationFeed($('#add-gadget-feed-subscription-url').val());
            $('#add-gadget-feed-subscription-url').attr("disabled", "disabled");
            $('#add-subscription-submit').attr("disabled", "disabled").val(AG.param.get("adding"));
            $("#add-subscription-throbber").addClass("loading");
            $.ajax({
                type: "POST",
                url: dashboardSubscribedGadgetFeedsUrl,
                data: JSON.stringify({
                    url: newFeedUrl
                }),
                contentType: "application/json",
                processData: false,
                success: function(directory) {
                    clearBrowser();
                    loadBrowser(false, function() {
                        $('#add-gadget-feed-subscription-url').removeAttr("disabled").val("");
                        $('#add-subscription-submit').val(originalButtonValue).removeAttr("disabled");
                        $("#add-subscription-throbber").removeClass("loading");
                        displaySubscribedGadgetFeeds(function() {
                            highlightFeed(newFeedUrl);
                        });
                    });
                },
                error: function(request, textStatus, errorThrown) {
                    if (request.status == 403 || request.status == 401) {
                        showError(AG.param.get("dashboardErrorDirectoryPermissions"));
                    } else {
                        showError(AG.param.get("dashboardErrorFailedToAddGadgetSubscription"));
                    }
                    $("#add-subscription-throbber").removeClass("loading");
                    $('#add-gadget-feed-subscription-url').removeAttr("disabled").select();
                    $('#add-subscription-submit').val(originalButtonValue).removeAttr("disabled");
                }
            });
        }
    };
    
    var removeSubscriptionToGadgetFeed = function(feedResource, success) {
        $.ajax({
            type: "DELETE",
            url: feedResource,
            success: success,
            error: function(request, textStatus, errorThrown) {
                if (request.status == 403 || request.status == 401) {
                    showError(AG.param.get("dashboardErrorDirectoryPermissions"));
                } else {
                    showError(AG.param.get("dashboardErrorFailedToRemoveGadgetSubscription"));
                }
            }
        });
    };
    
    var displaySubscribedGadgetFeeds = function(onLoad) {
        $("#gadget-feed-subscriptions ul").empty();
        var xhr = $.ajax({
            type: "GET",
            url: dashboardSubscribedGadgetFeedsUrl,
            dataType: "json",
            processData: false,
            success: function(result) {
                if (result.feeds) {
                    $("#gadget-subscriptions-header")
                        .html(AG.param.get("directoryGadgetFeedSubscriptionExplanation"));
                    $.each(result.feeds, function() {
                        var applicationRepresentation,
                                deleteLink = $("<a></a>")
                                        .attr("href", this.self.href)
                                        .attr("title", AG.param.get("unsubscribeFromFeed"))
                                        .addClass("remove")
                                        .text(AG.param.get("unsubscribeFromFeed"))
                                        .click(function(event) {
                                            if (confirm(AG.param.get("confirmRemoveSubscriptionToGadgetFeed"))) {
                                                var element = $(this);
                                                var li = element.parent();
                                                removeSubscriptionToGadgetFeed(this.href, function() {
                                                    element.remove();
                                                    li.append(AJS.$('<div class="throbber loading"></div>'));
                                                    clearBrowser();
                                                    loadBrowser(false, function() {
                                                        if (!li.siblings().length) {
                                                            $("#gadget-subscriptions-header")
                                                                .html(AG.param.get("directoryGadgetFeedSubscriptionEmptyExplanation"));
                                                        }
                                                        li.remove();
                                                    });
                                                });
                                            }
                                            return AJS.stopEvent(event);
                                        });

                        if(!this.invalid) {
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
                                    .text(this.name);
                            }
                            $("#gadget-feed-subscriptions ul")
                                .append($("<li></li>")
                                    .append(applicationRepresentation)
                                    .append($(" <a></a>")
                                        .attr("href", this.feed.href)
                                        .attr("title", this.feed.href)
                                        .text(this.base ? AJS.format(AG.param.get("subscribedFeedDescription"), this.base) : this.title)
                                    ).append(deleteLink));
                        } else {
                            $("#gadget-feed-subscriptions ul").append($("<li class='aui-message aui-message-error aui-override'/>").
                                    append(AJS.format(AG.param.get("dashboardErrorLoadingFeed"),
                                    "<a href=\"" + this.feed.href + "\">" + this.feed.href + "</a>")).append(deleteLink));
                        }
                    });
                    if ($.isFunction(onLoad)) {
                        onLoad();
                    }
                }
            },
            error: function(request, textStatus, errorThrown) {
                if (request.status == 403 || request.status == 401) {
                    showError(AG.param.get("dashboardErrorDirectoryPermissions"));
                } else {
                    showError(AG.param.get("dashboardErrorFailedToGetGadgetSubscriptions"));
                }
                AJS.MacroBrowser.dialog.gotoPage(GADGET_BROWSER_PAGE);
            }
        });
        AJS.MacroBrowser.dialog.gotoPage(EXTERNAL_DIRECTORIES_PAGE);
        $(xhr).throbber({target: $("#gadget-subscriptions-throbber"), isLatentThreshold: 200});
    };
    
    // Remove a Gadget from the Directory that was added by URL
    var removeGadgetFromDirectory = function(gadgetUri, callbacks) {
        var reqParams = {
            type: "DELETE",
            url: gadgetUri
        };
        $.extend(reqParams, callbacks);
        $.ajax(reqParams);
    };

    // Scroll to a particular gadget
    var scrollToGadget = function(gadget) {
        highlightGadget(gadget);

        var divOffset = $('.dialog-panel-body').offset().top;
        var pOffset = gadget.offset().top;
        var pScroll = pOffset - divOffset;
        $('.dialog-panel-body').animate({scrollTop: '+=' + pScroll + 'px'}, 1000);
    };

    // Highlight the gadget(s)
    var highlightGadget = function(gadgets) {
        AJS.MacroBrowser.dialog.gotoPage(GADGET_BROWSER_PAGE);
        highlight(gadgets);
    };
    
    var highlight = function(element) {
        $(element).css("background-color", "#fffe83").animate({ backgroundColor: "transparent" }, 5000, "linear", function() {
            $(this).css("background-color", "");
        });
    };

    // Fill macro Summary Template
    var fillMacroTemplate = function(gadgetDiv, gadget) {

        var onAdd = function(event) {
            if (AG.DashboardManager.getLayout().getGadgets().length >= AG.param.get("maxGadgets")) {
                showError(AG.param.get("dashboardErrorTooManyGadgets"));
                return false;
            }

            var button = $(".macro-button-add", gadgetDiv);
            button.attr("disabled", "disabled").val(AG.param.get("adding"));

            // disable gadget's  buttons to prevent overlapping calls
            $('.macro-button-add').attr("disabled", "disabled");
            $('.macro-button-remove').attr("disabled", "disabled");
            var gadgetUrl = button.siblings('.macro-hidden-uri').attr("value");
            addGadgetToDashboard(gadgetUrl, callbacks(button, addGadgetToDashboardSuccess, addGadgetToDashboardError));
            return false;
        };

        if (gadget.thumbnailUri && gadget.thumbnailUri != "") {
            gadgetDiv.prepend("<img src='" + gadget.thumbnailUri + "' alt='' width='120' height='60'/>");
        }

        if (gadget.description && gadget.description != "") {
            $(".macro-desc", gadgetDiv).append(gadget.description).attr("title", gadget.description);
        } else {
            $(".macro-desc", gadgetDiv).append("<span class='unknown'>" + AG.param.get("descriptionNotAvailable") + "</span>");
        }

        if (gadget.authorName && gadget.authorName != "") {
            $(".macro-author", gadgetDiv).append(AJS.format(AG.param.get("gadgetAuthor"), gadget.authorEmail ?
                    "<a href='mailto:" + gadget.authorEmail + "'>" + gadget.authorName + "</a>" :
                    gadget.authorName));
        } else {
            $(".macro-author", gadgetDiv).append("<span class='unknown'>" + AG.param.get("authorUnknown") + "</span>");
        }

        $(".macro-title", gadgetDiv).append("<a href=''>" + gadget.title + "</a>");
        $(".macro-title", gadgetDiv).click(onAdd);

        if (gadget.titleUri && gadget.titleUri != "") {
            $(".macro-title-uri-link", gadgetDiv).attr("href", gadget.titleUri);
        } else {
            $(".macro-title-uri", gadgetDiv).hide();
        }

        // add base url to the Gadget URL link if gadgetSpecUri is a relative path
        var gadgetUri = (gadget.gadgetSpecUri.match("^https?://") ? "" : AG.param.get("dashboardDirectoryBaseUrl"))
            + gadget.gadgetSpecUri;
        $(".macro-uri", gadgetDiv).attr("href", gadgetUri).attr("title", gadgetUri).text(gadgetUri);

        $(".macro-hidden-uri", gadgetDiv).val(gadget.gadgetSpecUri);

        $(".macro-button-add", gadgetDiv).click(onAdd);

        // Directory Admins only
        if (AG.param.get("canAddExternalGadgetsToDirectory") == "true") {
            if (gadget.isDeletable) {
                // Make the button visible and hook it up to the remove function
                $(".macro-button-remove", gadgetDiv).css("display", "block").click(function(event) {
                    if (confirm(AG.param.get("removeGadget"))) {
                        var button = $(this);
                        button.attr("disabled", "disabled").val(AG.param.get("removing"));
                        button.siblings('.macro-button-add').attr("disabled", "disabled");
                        var gadgetUrl = button.siblings('.macro-hidden-uri').attr("value");
                        removeGadgetFromDirectory(gadget.self, callbacks(button, removeGadgetFromDirectorySuccess, removeGadgetFromDirectoryError));
                    }
                    return false;
                });

            }
        }
    };

    // Construct the dialog without any data
    var constructBrowser = function() {
        var mb = AJS.MacroBrowser.dialog = new AJS.Dialog(860, 530, "macro-browser-dialog");
        mb.addHeader(AG.param.get("gadgetDirectory"));

        // Add buttons to page one of dialog if the user has perms
        if (AG.param.get("canAddExternalGadgetsToDirectory") == "true") {
            mb.addButton(
                AG.param.get("addByUrlButtonLabel"),
                function(dialog) {
                    dialog.nextPage();
                    $('#add-gadget-url').focus();
                },
                "add-by-url left"
            );
            mb.addButton(
                AG.param.get("subscribedGadgetFeedsButtonLabel"),
                function(dialog) {
                    displaySubscribedGadgetFeeds();
                },
                "display-gadget-feed-subscriptions left"
            );
        }

        if (!mb.page[0].buttonpanel) {
            mb.page[0].buttonpanel = AJS("div").addClass("dialog-button-panel");
            mb.page[0].element.append(mb.page[0].buttonpanel);
        }

        // Add Help Links
        var helpLink = AJS("span").attr("class", "directory-help-link");
        if (AG.param.get("showMarketingPluginHints") == true) {
            AJS("a").html(AG.param.get("helpLinkPluginHintGadgets")).attr("href", AG.param.get("helpLinkPluginHintGadgetsUrl")).attr("target", "_blank").appendTo(helpLink);
            AJS("a").html(AG.param.get("helpLinkCreateYourOwnGadget")).attr("href", AG.param.get("helpLinkCreateYourOwnGadgetUrl")).attr("target", "_blank").appendTo(helpLink);
        }
        mb.page[0].buttonpanel.append(helpLink);

        mb.addButton(AG.param.get("finishButtonLabel"),
        function(dialog) {
            dialog.hide();
        },
        "finish");

        // Directory Admins only
        if (AG.param.get("canAddExternalGadgetsToDirectory") == "true") {
            // Add 2nd page and buttons - Add by URL
            mb.addPage().addPanel("addByUrl", $("#add-by-url-template"))
                .addButton(AG.param.get("backButtonLabel"),
                    function(dialog) {
                        dialog.prevPage();
                    },
                    "back"
                ).addButton(AG.param.get("finishButtonLabel"),
                    function(dialog) {
                        dialog.hide();
                    },
                    "finish"
                );
            mb.page[1].addHeader(AG.param.get("addGadgetByUrl"));
            // AG-917 : when the user hits 'enter' in the text box, click the add-gadget-submit button
            $('#add-gadget-url').keydown(function(e) {
               if (e.keyCode == 13) {
                   $('#add-gadget-submit').click();
               }
            });
            // Button for adding a gadget by URL
            $('#add-gadget-submit').click(function() {
                addGadgetToDirectory();
            });

            mb.addPage().addPanel("gadgetFeedSubscriptions", $("#gadget-feed-subscriptions-template"))
                .addButton(AG.param.get("addGadgetFeedSubscriptionButtonLabel"),
                    function(dialog) {
                        dialog.gotoPage(ADD_EXTERNAL_DIRECTORY_PAGE);
                        $('#add-gadget-feed-subscription-url').focus();
                    },
                    "add left"
                ).addButton(AG.param.get("backButtonLabel"),
                    function(dialog) {
                        dialog.gotoPage(GADGET_BROWSER_PAGE);
                    },
                    "back"
                ).addButton(AG.param.get("finishButtonLabel"),
                    function(dialog) {
                        dialog.hide();
                    },
                    "finish"
                );
            mb.page[2].addHeader(AG.param.get("gadgetFeedSubscriptions"));
            
            // Add 3rd page and buttons - Add External Directory
            mb.addPage().addPanel("addGadgetFeedSubscription", $("#add-gadget-feed-subscription-template"))
                .addButton(AG.param.get("backButtonLabel"),
                    function(dialog) {
                        dialog.gotoPage(EXTERNAL_DIRECTORIES_PAGE);
                    },
                    "back"
                ).addButton(AG.param.get("finishButtonLabel"),
                    function(dialog) {
                        dialog.hide();
                    },
                    "finish"
                );
            mb.page[3].addHeader(AG.param.get("addGadgetFeedSubscription"));
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
        }

        // add throbber to indicate macro browser loading
        mb.page[0].header.prepend(AJS.$('<div id="macro-browser-throbber" class="throbber"></div>'));

        // add search box to top right
        var filterResults = function(e) {
            var text = AJS.$(e.target).val();
            var macroSummaries = AJS.$("#macro-browser-dialog .dialog-panel-body .macro-list-item");

            if (text !== "") {
                var lowerCaseText = text.toLocaleLowerCase();
                macroSummaries.each(function() {
                    var $element = AJS.$(this);
                    if($element.text().toLocaleLowerCase().indexOf(lowerCaseText) >= 0) {
                        $element.show();
                    } else {
                        $element.hide();
                    }
                });

            } else {
                resetSearchResults();
            }
        };
        var searchInput =   AJS.$("<input type='search'/>").attr("id", "macro-browser-search").bind('search', function(e) {
            // HTML5 triggers a search event when the clear button is pressed
            var text = AJS.$(e.target).val();
            if (text == "") {
                filterResults(e);
            }
        }).keyup(filterResults).focus(function(e) {
            var searchInput = AJS.$(e.target);
            if (searchInput.hasClass("blank-search")) {
                searchInput.removeClass("blank-search").val("");
            }
        }).blur(function(e) {
            var searchInput = AJS.$(e.target);
            if (searchInput.val() == "") {
                searchInput.addClass("blank-search").val(AG.param.get("blankSearchText"));
            }
        }).blur();
        mb.page[0].header.prepend(searchInput);
    };

    // Loads the categories and macros into the dialog
    var loadBrowserContent = function(data, after) {
        var mb = AJS.MacroBrowser.dialog;

        // sort the categories and macros
        data.categories.sort(function(one, two) {
            return (one.name > two.name ? 1 : -1);
        });
        data.gadgets.sort(function(one, two) {
            return (one.title > two.title ? 1 : -1);
        });

        var makeCategoryList = function(id) {
            return $("#macro-summaries-template").clone().attr("id", "category-" + id);
        };

        var getScrollbarWidth = function() {
            var div = $('<div style="width:100px;height:50px;overflow:hidden;position:absolute;top:-1000px;left:-1000px;"><div style="height:200px;"></div></div>');
            // Append our div, do our calculation and then remove it
            $('body').append(div);
            var w1 = $('div', div).innerWidth();
            div.css('overflow', 'auto');
            var w2 = $('div', div).innerWidth();
            $(div).remove();
            return (w1 - w2);
        };
        var scrollbarWidth = getScrollbarWidth();

        // Create and fill each node that contains an item
        var makeGadgetSummary = function(gadget) {
            var macroDiv = AJS.MacroBrowser.clone("#macro-summary-template");
            fillMacroTemplate(macroDiv, gadget);
            return macroDiv;
        };

        // Initialize list of categories
        var categoryDivs = {
            all: makeCategoryList("all")
        };

        // Fill items on the right
        $(data.gadgets).each(function(i, gadget) {
            // Remove all characters that are not valid in an HTML Id.
            var title = gadget.title.replace(/[^A-Za-z0-9_]/g,'');
            var macroDiv = makeGadgetSummary(gadget).attr("id", "macro-" + title);
            categoryDivs.all.append(macroDiv);
            $([gadget.categories]).each(function(i, catKey) {
                if (catKey.constructor == Array) { // if there are multiple categories for this gadget, loop through all of them
                    for (var j = 0; j < catKey.length; ++j) {
                        categoryDivs[catKey[j]] = categoryDivs[catKey[j]] || makeCategoryList(j);
                        categoryDivs[catKey[j]].append(makeGadgetSummary(gadget).attr("id", catKey[j] + "-macro-" + title));
                    }
                } else {
                    categoryDivs[catKey] = categoryDivs[catKey] || makeCategoryList(j);
                    categoryDivs[catKey].append(makeGadgetSummary(gadget).attr("id", catKey + "-macro-" + title));
                }
            });
        });

        // Fill category menu on the left
        mb.page[0].addPanel(AG.param.get("all") + " (" + categoryDivs["all"].children().length + ")", categoryDivs["all"]);

        $(data.categories).each(function() {
            if (categoryDivs[this.name]) {
                mb.page[0].addPanel(this.name + " (" + categoryDivs[this.name].children().length + ")", categoryDivs[this.name], this.name);
            }
        });

        mb.page[0].gotoPanel(0, 0);
        $(mb.page).each( function() {
            this.recalcSize();
        });

        if ($.isFunction(after)) {
            after();
        }

        // AG-683
//        $(document).bind('DOMMouseScroll', function(e){
//            if (e.ctrlKey) { // if ctrl is pressed, zoom is triggered
//                var sbWidth = getScrollbarWidth();
//
//                if (e.detail > 0 && sbWidth > defaultScrollWidth) { // wheel down
//                    sbWidth = (sbWidth * 2) - defaultScrollWidth;
//                }
//
//                // change the size of the macroDiv depending on the scrollbar width (changes as the zoom value change)
//                if (sbWidth > defaultScrollWidth) {
//                    $(".macro-list-item").css("width", (defaultMacroDivWidth - (sbWidth - defaultScrollWidth)/2) + "px");
//                } else {
//                    $(".macro-list-item").css("width", "152px");
//                }
//            }
//            return true;
//        });

        mb.ready = true;
    };

    // Load data from Directory Resource
    var loadBrowser = function(displayDialog, after) {
        browserContentLoading = true;
        $("#macro-browser-throbber").addClass("loading");
        var xhr = $.ajax({
                type: "GET",
                dataType: "json",
                global: "false",
                cache: false,
                url: dashboardDirectoryResourceUrl + ".json",
                success: function(data) {
                    browserContentLoading = false;
                    browserContentLoaded = true;
                    loadBrowserContent(data, after);
                    $("#macro-browser-throbber").removeClass("loading");
                    if(displayDialog) {
                        showDialog();
                    }
                },
                error: function(request, textStatus, errorThrown) {
                    browserContentLoading = false;
                    $("#macro-browser-throbber").removeClass("loading");
                    if (request.status == 403 || request.status == 401) {
                        showError(AG.param.get("dashboardErrorDashboardPermissions"));
                    } else {
                        showError(AG.param.get("failedToLoadError"));
                    }
                }
            });
        //only display the throbber if we're displaying the dialog.
        if(displayDialog) {
            $(xhr).throbber({target: $("#dash-throbber")});
        }
        $(xhr).throbber({target: $("#dir-throbber")});
    };

    // Clear Dialog Content
    var clearBrowser = function() {
        var mb = AJS.MacroBrowser.dialog;
        $(mb.page[0].panel).each(function() {
            this.body.remove();
        });
        $(mb.page[0].panel).each(function() {
            this.remove();
        });
        mb.page[0].curtab = 0;
        browserContentLoaded = false;
    };

    var resetSearchResults = function () {
        AJS.$("#macro-browser-dialog .dialog-panel-body .macro-list-item").show();
    };

    var showDialog = function() {
        var mb = AJS.MacroBrowser;
        if (mb.dialog && mb.dialog.ready) {
            // bind escape to close dialog
            $(document).keyup(function(e) {
                if (e.keyCode == 27) {
                    mb.dialog.hide();
                    $(document).unbind("keyup", arguments.callee);
                    $("#macro-browser-search").val("");
                    return AJS.stopEvent(e);
                }
            });

            if (mb.selectedMacro) {
                AJS.log("selectedMacro: " + mb.selectedMacro.name);
                replicateSelectMacro(mb.selectedMacro.name);
            } else {
                mb.dialog.show(); // we must show then go to panel - this order is important for IE6
                mb.dialog.gotoPanel(0, 0);
                AJS.$("#macro-browser-search").val("").focus();
                resetSearchResults();
            }
        } else {
            showError(errorStatus);
        }
    };

    var showError = function(message) {
        alert(message); // TODO: AG-95 show this in the UI, not an alert.
    };

    // Initialize Everything
    constructBrowser();

    // Hook up the button
    $(".add-gadget-link").live("click", function(e) {

        AJS.activeColumn = 0;
        // button id = "add-gadget-#"
        if($(this).attr("id").substring(11)){
            AJS.activeColumn = $(this).attr("id").substring(11);
        }

        if (browserContentLoaded) {
            showDialog();
        } else if (!browserContentLoading) {
            loadBrowser(true);
        }
        return AJS.stopEvent(e);
    });
})(AJS.$);