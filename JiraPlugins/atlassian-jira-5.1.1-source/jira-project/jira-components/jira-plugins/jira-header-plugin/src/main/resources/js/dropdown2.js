(function($) {

    var $document = $(document),
            ie8detected = (jQuery.browser.msie && parseInt(jQuery.browser.version, 10) == 8);

    var BUTTON = (function() {
        var isMouseDown = false;
        function onmousedown(event) {
            if (!isMouseDown && event.which === 1) { // Left-click only
                isMouseDown = true;
                $document.bind("mouseup mouseleave", onmouseup);
                $(this).trigger("aui-button-invoke");
            }
        }
        function onmouseup() {
            $document.unbind("mouseup mouseleave", onmouseup);
            setTimeout(function() {
                isMouseDown = false;
            }, 0);
        }
        function onclick() {
            if (!isMouseDown) {
                $(this).trigger("aui-button-invoke");
            }
        }
        function preventDefault(event) {
            event.preventDefault();
        }
        if (typeof document.addEventListener === "undefined") {
            return {
                "click": onclick,
                "click selectstart": preventDefault,
                "mousedown": function(event) {
                    onmousedown.call(this, event);
                    var currentTarget = this;
                    var activeElement = document.activeElement;
                    if (activeElement !== null) {
                        activeElement.attachEvent("onbeforedeactivate", onbeforedeactivate);
                        setTimeout(function() {
                            activeElement.detachEvent("onbeforedeactivate", onbeforedeactivate);
                        }, 0);
                    }
                    function onbeforedeactivate(event) {
                        // Prevent this "mousedown" event from moving focus
                        // to currentTarget, or away from activeElement.
                        switch (event.toElement) {
                            case null:
                            case currentTarget:
                            case document.body:
                            case document.documentElement:
                                event.returnValue = false;
                        }
                    }
                }
            };
        }
        return {
            "click": onclick,
            "click mousedown": preventDefault,
            "mousedown": onmousedown
        };
    })();

    var DROPDOWN_TRIGGER = {
        "aui-button-invoke": function(event) {
            var $dropdown = $(getTargetElement(this));
            var $trigger = $(this).addClass("active");

            var $menu = $trigger.closest(".aui-dropdown2-trigger-group");
            selectItem($dropdown.find("a").first());

            var ITEM = {
                "click": function() {
                    if (!$(this).hasClass("interactive")) {
                        hide();
                    }
                },
                "mousemove": function() {
                    selectItem($(this));
                }
            };
            var DOCUMENT = {
                "click focusin mousedown": function(event) {
                    var target = event.target;
                    if (!inside(target, $dropdown[0]) && !inside(target, $trigger[0])) {
                        hide();
                    }
                },
                "keydown": function(event) {
                    if(event.shiftKey && event.keyCode == 9) {
                        selectNextItem(-1); // Shift tab
                    } else {
                        switch (event.keyCode) {
                            case 13: // Return
                                var item = $dropdown.find("a.active")[0];
                                if (item) {
                                    click(item);
                                }
                                break;
                            case 27: // Escape
                                hide();
                                break;
                            case 37: // Left
                                selectNextMenu(-1);
                                break;
                            case 38: // Up
                                selectNextItem(-1);
                                break;
                            case 39: // Right
                                selectNextMenu(1);
                                break;
                            case 40: // Down
                                selectNextItem(1);
                                break;
                            case 9: // Tab
                                selectNextItem(1);
                                break;
                            default:
                                // Don't prevent the default action for other keys.
                                return;
                        }
                    }
                    event.preventDefault();
                }
            };

            // ARIA - sets item role (checkbox or radio)
            // for radio, also sets containing UL to role=radiogroup
            function Dropdown2AriaMenus($elements,role) {
                $elements.each(function () {
                    var $el = $(this);
                    $el.attr("role", role);
                    if ( $el.hasClass("checked") ) {
                        $el.attr("aria-checked", "true");
                        if (role == "radio") {
                            $el.closest("ul").attr("role", "radiogroup");
                        }
                    } else {
                        $el.attr("aria-checked", "false");
                    }
                });
            };

            $trigger.attr("aria-controls", $trigger.attr("aria-owns"));
            $dropdown.find(".disabled").attr("aria-disabled", "true");
            $dropdown.find("li.hidden > a").addClass("disabled").attr("aria-disabled", "true");
            Dropdown2AriaMenus($dropdown.find(".aui-dropdown2-checkbox"), "checkbox");
            Dropdown2AriaMenus($dropdown.find(".aui-dropdown2-radio"), "radio");

            var trOffset = $trigger.offset();
            var trWidth  = $trigger.outerWidth();
            var ddWidth  = $dropdown.outerWidth();
            var docWidth = $document.width();
            var minWidth = Math.max(parseInt($dropdown.css("min-width"), 10), trWidth);
            var ddContainer = $trigger.data("container") || false;

            // IE8: deduct border width from width and min-width
            if (ie8detected) {
                var horizontalBorderWidth = parseInt($dropdown.css("border-left-width") + parseInt($dropdown.css("border-right-width")) );
                ddWidth = ddWidth - horizontalBorderWidth;
                minWidth = minWidth - horizontalBorderWidth;
            };

            $dropdown.css({
                "display": "block",
                "top": trOffset.top + $trigger.outerHeight() + "px",
                "min-width": minWidth + "px"
            }).attr("aria-hidden","false");
            // Ensure the dropdown element is always document.body.lastChild to
            // preserve z-axis stacking order.
            $dropdown.appendTo(document.body);
            var left = trOffset.left;
            // If there isn't enough available space to left-align the dropdown,
            // make it right-aligned instead.
            if (docWidth < left + ddWidth && ddWidth <= left + trWidth) {
                left += trWidth - ddWidth;
            }

            // where dropdown declares a container element other than body,
            // check if it needs to flip to right-aligned
            // todo: rationalise this and the default logic
            if ( ddContainer ) {
                var container = $trigger.closest(ddContainer),
                        containerRight = container.offset().left + container.outerWidth(),
                        triggerRight = $trigger.offset().left + $trigger.outerWidth(),
                        dropdownRight = triggerRight + ddWidth;

                // first-load bug only seems to affect custom-boundary elements
                if ( minWidth >= ddWidth ) {
                    ddWidth = minWidth;
                }

                // if the dropdown don't fit you must acquit. and align right.
                if ( dropdownRight > triggerRight ) {
                    left = triggerRight - ddWidth;
                }

                // IE8 needs to be reminded about borders
                if (ie8detected) {
                    left -= horizontalBorderWidth;
                }
            }

            // Integration with AUI Toolbar
            if ( $trigger.hasClass("toolbar-trigger") ) {
                $dropdown.addClass("aui-dropdown2-in-toolbar")
            }

            $dropdown.css("left", left + "px");
            $dropdown.trigger("aui-dropdown2-show");
            setEvents("on");
            function hide() {
                setEvents("off");
                // Event handlers that are currently running may expect the dropdown
                // element to remain  within the document. Wait until these handlers
                // complete before removing the dropdown element.
                setTimeout(function() {
                    // Hide the dropdown element but don't remove it from the document
                    // so that its contents remains accessible to external code.
                    $dropdown.css("display", "none").insertAfter($trigger).attr("aria-hidden","true");
                    $trigger.removeClass("active");
                    $dropdown.trigger("aui-dropdown2-hide");
                }, 0);
            }
            function selectItem($next) {
                $dropdown.find("a.active").removeClass("active");
                $next.addClass("active");
            }
            function selectNextItem(offset) {
                selectItem(getByOffset($dropdown.find("a:not(.disabled)"), offset, true));
            }
            function selectMenu($next) {
                if ($next.length > 0) {
                    hide();
                    $next.trigger("aui-button-invoke");
                }
            }
            function selectNextMenu(offset) {
                selectMenu(getByOffset($menu.find(".aui-dropdown2-trigger:not([aria-disabled=true])"), offset, false));
            }
            function getByOffset($collection, offset, wrap) {
                var i = $collection.index($collection.filter(".active"));
                i += (i < 0 && offset < 0) ? 1 : 0; // Correct for case where i == -1.
                i += offset;
                if (wrap) {
                    i %= $collection.length;
                } else if (i < 0) {
                    i = $collection.length; // Out of bounds
                }
                return $collection.eq(i);
            }
            function replaceMenu() {
                selectMenu($(this));
            }
            function setEvents(state) {
                var bind = "bind";
                var delegate = "delegate";
                if (state !== "on") {
                    bind = "unbind";
                    delegate = "undelegate";
                }
                $document[bind](DOCUMENT);
                $menu[delegate](".aui-dropdown2-trigger:not(.active)", "mousemove", replaceMenu);
                $trigger[bind]("aui-button-invoke", hide);
                $dropdown[delegate]("a:not(.disabled)", ITEM);
            }
        },
        "mousedown": function(event) {
            if (event.which === 1) { // Left-click only
                $(this).bind(SIMULATE_CLICK_ENABLE);
            }
        }
    };

    var SIMULATE_CLICK_ENABLE = {
        "mouseleave": function() {
            $document.bind(SIMULATE_CLICK);
        },
        "mouseup mouseleave": function() {
            $(this).unbind(SIMULATE_CLICK_ENABLE);
        }
    };

    var SIMULATE_CLICK = {
        "mouseup": function(event) {
            var target = $(event.target).closest(".aui-dropdown2 a, .aui-dropdown2-trigger")[0];
            if (target) {
                setTimeout(function() {
                    click(target);
                }, 0);
            }
        },
        "mouseup mouseleave": function() {
            $(this).unbind(SIMULATE_CLICK);
        }
    };

    function click(element) {
        if (element.click) {
            element.click();
        } else {
            var event = document.createEvent("MouseEvents");
            event.initMouseEvent("click",
                    true, true, window, 0, 0, 0, 0, 0, false, false, false, false, 0, null);
            element.dispatchEvent(event);
        }
    }

    function inside(target, container) {
        return (target === container) || $.contains(container, target);
    }

    function getTargetElement(triggerElement) {
        var id = triggerElement.getAttribute("aria-owns"),
                haspopup = triggerElement.getAttribute("aria-haspopup"),
                el = document.getElementById(id);
        if (el) {
            return el;
        } else {
            if (!id) {
                throw new Error("Dropdown 2 trigger required attribute not set: aria-owns");
            }
            if (!haspopup) {
                throw new Error("Dropdown 2 trigger required attribute not set: aria-haspopup");
            }
            if (!el) {
                throw new Error("Dropdown 2 trigger aria-owns attr set to nonexistent id: " + id);
            }
            throw new Error("Dropdown 2 trigger unknown error. I don't know what you did, but there's smoke everywhere. Consult the documentation.");
        }
    }

    // Initialise events for default dropdown className.
    $document.delegate(".aui-dropdown2-trigger", BUTTON);
    $document.delegate(".aui-dropdown2-trigger:not(.active):not([aria-disabled=true])", DROPDOWN_TRIGGER);

    // Checkboxes
    $document.delegate(".aui-dropdown2-checkbox:not(.disabled)", "click", function() {
        var $checkbox = $(this);
        if ($checkbox.hasClass("checked")) {
            $checkbox.removeClass("checked").attr("aria-checked", "false");
            $checkbox.trigger("aui-dropdown2-item-uncheck");
        } else {
            $checkbox.addClass("checked").attr("aria-checked", "true");
            $checkbox.trigger("aui-dropdown2-item-check");
        }
    });

    // Radio button groups
    $document.delegate(".aui-dropdown2-radio:not(.checked):not(.disabled)", "click", function() {
        var $next = $(this);
        var $prev = $next.closest("ul").find(".checked");
        $prev.removeClass("checked").attr("aria-checked", "false").trigger("aui-dropdown2-item-uncheck");
        $next.addClass("checked").attr("aria-checked", "true").trigger("aui-dropdown2-item-check");
    });

    // Disabled items
    $document.delegate(".aui-dropdown2 a.disabled", "click", function(event) {
        event.preventDefault();
    });

})(jQuery);