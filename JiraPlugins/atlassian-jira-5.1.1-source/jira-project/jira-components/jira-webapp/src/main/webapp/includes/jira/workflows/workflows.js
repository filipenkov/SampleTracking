AJS.$(function($) {
    $(new Image()).load(function() {
        var MIN_ZOOM_RATIO = 1.2;
        var MIN_HEIGHT = 400;

        var diagramView = $('#workflow-view-diagram');
        var container = diagramView.find('#workflow-diagram-container');
        var dragEl = container.find('.workflow-diagram');
        var img = $(this);

        function destroyDraggable() {
            dragEl.addClass('zoomed-out').removeAttr('style').draggable('destroy');
            diagramView.removeClass('left top right bottom');
            diagramView.find('#zoom-out').parent().addClass('active');
            diagramView.find('#zoom-in').parent().removeClass('active');
        }

        function resetDraggable() {
            var containerOffset = container.offset();
            var x = containerOffset.left;
            var y = containerOffset.top;
            if (container.width() < img.width()) {
                x += container.width() - img.width();
            }
            if (container.height() < img.height()) {
                y += container.height() - img.height();
            }
            dragEl.draggable('option', 'containment', [ x, y, containerOffset.left, containerOffset.top ]);
        }

        var origHeight = this.height;
        var origWidth = this.width;
        var minHeight = Math.min(origHeight, MIN_HEIGHT);

        function setContainerHeight(maxHeight) {
            container.css('max-height', maxHeight);
            container.css('height', '');
            container.css('height', container.height());
        }

        function resizeImageContainer() {
            var isOnDemand = AJS.DarkFeatures.isEnabled('com.atlassian.jira.config.CoreFeatures.ON_DEMAND');
            if (!isOnDemand) {
                var footer = $('#footer');
                //need to remove float on footer to calculate heights correctly in some browsers
                footer.css('float', 'none');
                var maxHeight = Math.max(minHeight, $(window).height() - $(document.body).height() + container.height());
                footer.css('float', '');
                setContainerHeight(maxHeight);
                if (container.height() > minHeight) {
                    $(document.documentElement).css('overflow-y', 'hidden');
                } else {
                    $(document.documentElement).css('overflow-y', '');
                }
            } else {
                var maxHeight = Math.max(minHeight, $(window).height() - container.offset().top - parseInt(diagramView.css('margin-top')));
                setContainerHeight(maxHeight);
            }
            if (origHeight < MIN_ZOOM_RATIO * container.height() && origWidth < MIN_ZOOM_RATIO * container.width()) {
                diagramView.removeClass('zoom');
                destroyDraggable();
            } else {
                diagramView.addClass('zoom');
            }
            if (container.width() > img.width()) {
                dragEl.addClass('extend-width');
            } else {
                dragEl.removeClass('extend-width');
            }
            if (dragEl.hasClass('ui-draggable')) {
                resetDraggable();
            }
        }

        //using .css() instead of .hide() because firefox incorrectly sets the 'olddisplay' (used internally by jQuery) to 'block'
        img.css('display', 'none').appendTo('.workflow-diagram').fadeIn();
        resizeImageContainer();

        $(window).resize(resizeImageContainer);
        diagramView.bind('show', resizeImageContainer);

        $('.workflow-zoom-toggle').click(function(e) {
            e.preventDefault();
            $('.workflow-zoom-toggle').each(function() {
                $(this).parent().removeClass('active');
            });
            $(this).parent().addClass('active');

            if ($(this).attr('id') == 'zoom-in') {
                dragEl.removeClass('zoomed-out');
                dragEl.draggable({
                    drag: function(e, ui) {
                        var containerOffset = container.offset();
                        var x = containerOffset.left;
                        var y = containerOffset.top;
                        if (container.width() < img.width()) {
                            x += container.width() - img.width();
                        }
                        if (container.height() < img.height()) {
                            y += container.height() - img.height();
                        }
                        if (ui.position.left == 0) {
                            diagramView.removeClass('left');
                        } else {
                            diagramView.addClass('left');
                        }
                        if (ui.position.top == 0) {
                            diagramView.removeClass('top');
                        } else {
                            diagramView.addClass('top');
                        }
                        if (ui.offset.left == x) {
                            diagramView.removeClass('right');
                        } else {
                            diagramView.addClass('right');
                        }
                        if (ui.offset.top == y) {
                            diagramView.removeClass('bottom');
                        } else {
                            diagramView.addClass('bottom');
                        }
                    }
                });
            } else {
                destroyDraggable();
            }
            resizeImageContainer();
        });
    }).attr('src', $('.workflow-diagram').data('workflow-diagram'));
});

// Display Toggle for Diagram/Text modes
AJS.$(function($) {
    var MODE_DIAGRAM = 'diagram', MODE_TEXT = 'text', COOKIE_NAME = 'workflow-mode';

    function sanitiseMode(mode) {
        return mode === MODE_TEXT ? MODE_TEXT : MODE_DIAGRAM
    }

    function readMode() {
        return sanitiseMode(AJS.Cookie.read(COOKIE_NAME) || MODE_DIAGRAM);
    }

    function saveMode(mode) {
        AJS.Cookie.save(COOKIE_NAME, sanitiseMode(mode));
    }

    function switchMode(mode){
        var $trigger = $('#workflow-' + mode);
        if (!$trigger.parent().hasClass('active')) {
            $('.workflow-view-toggle').each(function() {
                var $link = $(this);
                if ($trigger.is($link)) {
                    $link.parent().addClass('active');
                } else {
                    $link.parent().removeClass('active');
                }
            });
            $('.workflow-view').addClass('hidden');
            $($trigger.attr('href')).removeClass('hidden').trigger($.Event('show'));
            $(document.documentElement).css('overflow-y', '');
        }
    }

    //If there are toggle links then we are in view mode.
    if ($('.workflow-view-toggle').length > 0) {
        $('.workflow-view-toggle').click(function(e) {
            e.preventDefault();
            var mode = sanitiseMode($(this).data('mode'));
            saveMode(mode);
            switchMode(mode);
        });
    } else { //edit mode.
        //Save the current mode by looking for the workflow designer.
        saveMode($('#jwd').length > 0 ? MODE_DIAGRAM : MODE_TEXT);
    }

    new JIRA.FormDialog({
        id: 'edit-workflow-dialog',
        trigger: '#edit-workflow-trigger'
    });

    var $publish = $('#publish_draft_workflow');
    if (AJS.HelpTip && $publish.length) {
        var helpTip = new AJS.HelpTip({
            id: 'publish-draft-workflow',
            title: AJS.I18n.getText('helptips.publish.draft.workflow.title'),
            body: AJS.I18n.getText('helptips.publish.draft.workflow.body'),
            url: $publish.data('url'),
            anchor: $publish
        });
        helpTip.show();

        $('#publish_draft_workflow, #discard_draft_workflow').click(function() {
            helpTip.dismiss();
        });
    }
});
