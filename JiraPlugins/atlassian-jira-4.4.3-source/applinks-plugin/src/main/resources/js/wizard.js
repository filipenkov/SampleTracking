AJS.$(document).bind(AppLinks.Event.PREREADY, function() {

    (function($) {

        $.fn.wizard = function(settings) {
            var config = {
                width: 500,
                height: 350,
                onshow: function(popup, configuration) {
                    return true;
                },
                aftershow: function() {
                    return true;
                },
                oncancel: function() {
                    return true;
                },
                onsubmit: function() {
                    return true;
                },
                aftersubmit: function() {
                    return true;
                },
                onnext: function() {
                    return true;
                },
                onprevious: function() {
                    return true;
                },
                cancelLabel: AppLinks.I18n.getText("applinks.cancel"),
                submitLabel: AppLinks.I18n.getText("applinks.create"),
                nextLabel: AppLinks.I18n.getText("applinks.next"),
                previousLabel: AppLinks.I18n.getText("applinks.previous"),
                id: ""
            };

            if (!settings) settings = {};

            settings = $.extend(config, settings);

            var result = this;

            this.each(function() {
                var obj = $(this);
                var popup = new AJS.Dialog(settings.width, settings.height, settings.id);
                var showFn = show(popup, settings.onshow, settings.aftershow);
                var cancelFn = cancel(popup, settings.oncancel);
                var submitFn = submit(popup, settings.onsubmit, settings.aftersubmit);
                var previousFn = previous(popup, settings.onprevious)
                var nextFn = next(popup, settings.onnext);

                var disableNextFn = disableNext(popup);
                var enableNextFn  = enableNext(popup);

                var disableSubmitFn = disableSubmit(popup);
                var enableSubmitFn  = enableSubmit(popup);

                var disablePreviousFn = disablePrevious(popup);
                var enablePreviousFn  = enablePrevious(popup);

                if (settings.showButtonId) {
                    $('#' + settings.showButtonId).click(showFn);
                }

                var pages = findPages(obj);


                for (var pageIndex = 0; pageIndex < pages.length; pageIndex++) {
                    var page = pages[pageIndex];
                    createPage(popup, page);
                    if (page.className) {
                        popup.addHeader(page.title, page.className + "-header");
                    } else {
                        popup.addHeader(page.title);
                    }

                    if (pageIndex != 0 && $(page.div).attr("previous") != "false") {
                        popup.addButton(settings.previousLabel, previousFn, "applinks-previous-button");
                    }

                    if (pageIndex < pages.length - 1 && $(page.div).attr("submit") != "true" && $(page.div).attr("next") != "false") {
                        popup.addButton(settings.nextLabel, nextFn, "applinks-next-button");
                    }

                    if ($(page.div).attr("submit") == "true") {
                        popup.addButton(settings.submitLabel, submitFn, "wizard-submit");
                    }

                    if (!popup.getPage(pageIndex).buttonpanel) {
                        //THIS IS A DUMMY BUTTON, which gets remove afterwards
                        //THE DUMMY BUTTON will cause the cancel text link to appear on the correct position.
                        //IT IS A WORKAROUND so I don't have to change the dialog.js code
                        popup.addButton("", null);
                        $(popup.getPage(pageIndex).buttonpanel).empty();
                        var cancelLink = $('<a class="button-panel-button applinks-cancel-link">' + settings.cancelLabel + '</a>');
                        popup.getPage(pageIndex).buttonpanel.append(cancelLink);
                        cancelLink.click(cancelFn);
                    } else {
                        var cancelLink = $('<a class="applinks-cancel-link">' + settings.cancelLabel + '</a>');
                        $(popup.getPage(pageIndex).buttonpanel).append(cancelLink);
                        cancelLink.click(cancelFn);
                    }

                    if (pageIndex < pages.length - 1) {
                        popup.addPage();
                    }
                }

                result = {
                    dialog: popup,
                    nextPage: nextFn,
                    prevPage: previousFn,
                    submit: submitFn,
                    cancel: cancelFn,
                    show: showFn,
                    disableNextBtn     : disableNextFn,
                    enableNextBtn      : enableNextFn,
                    disableSubmitBtn   : disableSubmitFn,
                    enableSubmitBtn    : enableSubmitFn,
                    disablePreviousBtn : disablePreviousFn,
                    enablePreviousBtn  : enablePreviousFn
                };
                popup.gotoPage(0);
                popup.gotoPanel(0);
            });

            return result;
        };

        function disablePrevious(popup) {
            return function() {
                disable(getButton(popup, 'applinks-previous-button'));
            }
        }

        function enablePrevious(popup) {
            return function() {
                enable(getButton(popup, 'applinks-previous-button'));
            }
        }

        function disableNext(popup) {
            return function() {
                disable(getButton(popup, 'applinks-next-button'));
            }
        }

        function enableNext(popup) {
            return function() {
                enable(getButton(popup, 'applinks-next-button'));
            }
        }

        function disableSubmit(popup) {
            return function(showLoading) {
                var buttonEl = getButton(popup, 'wizard-submit');
                disable(buttonEl);
                if (typeof(showLoading) == 'undefined' || showLoading) {
                    $('<span class="loading">&nbsp;</span>').insertBefore(buttonEl);
                } else {
                    buttonEl.parent().find('.loading').remove();
                }
            }
        }

        function enableSubmit(popup) {
            return function() {
                var buttonEl = getButton(popup, 'wizard-submit');
                enable(buttonEl);
                buttonEl.parent().find('.loading').remove();
            }
        }

        function getButton(popup, cssClass) {
            return $(popup.getPage(popup.curpage).buttonpanel).find('.' + cssClass);
        }

        function resetForms(popup) {
             $(popup.popup.element).find('form').each( function() {
                  this.reset();
             });
        }

        function enable(element) {
            element.attr('disabled', '');
        }

        function disable(element) {
            element.attr('disabled', 'true');
        }
		
        function show(popup, onshow, aftershow) {
            return function(configuration) {
				if (onshow(popup, configuration) !== false) {
                    popup.gotoPage(0);
                    popup.gotoPanel(0);
                    $(document).unbind('keydown.ual.dialog');
                    $(document).bind('keydown.ual.dialog', attachKeypressListener(popup));
					popup.show();
                    aftershow();
                }
            }
        }

        function cancel(popup, oncancel) {
            return function() {
                if (oncancel(popup) !== false) {
                    popup.hide();
                    resetForms(popup);
                }
            }
        }

        function previous(popup, onprevious) {
            return function() {
                if (onprevious(popup) !== false) {
                    popup.prevPage();
                }
            }
        }

        function next(popup, onnext) {
            return function() {
                if (onnext(popup) !== false) {
                    popup.nextPage();
                }
            }
        }

        function attachKeypressListener(popup) {
            return function(e) {
                if (e.keyCode === 27) {
                    resetForms(popup);
                    $(document).unbind('keydown.ual.dialog');
                }
            }
        }

        function submit(popup, onSubmit, afterSubmit) {
            return function() {
                if (onSubmit(popup) !== false) {
                    afterSubmit(popup);
                    resetForms(popup);
                }
            }
        }

        function createPage(popup, page) {
            var panelDivs = $("> div[panel]", page.div);
            if (panelDivs.length > 0) {
                panelDivs.each(function(index) {
                    var popupPage = popup.addPanel(panelDivs[index].title, null, panelDivs[index].className);
                    popupPage.getCurrentPanel().body.append(panelDivs[index]);
                });
            }
            else {
                var popupPage = popup.addPanel(page.title);
                popupPage.getCurrentPanel().body.append(page.div);
            }
        }


        function findPages(containerDiv) {
            var pagesDivs = $(" > div", containerDiv);
            var pages = [];
            pagesDivs.each(function(index) {
                var pageDiv = $(this);
                pages[index] = {
                    title: pageDiv.attr('title'),
                    className: pageDiv.attr('class'),
                    div: pageDiv
                };
            });
            return pages;
        }
    })(jQuery)
});
