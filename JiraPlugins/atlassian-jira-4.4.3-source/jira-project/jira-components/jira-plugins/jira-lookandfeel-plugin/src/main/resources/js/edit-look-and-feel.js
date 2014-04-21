
(function($) {

    $(function(){
        initEditLookAndFeel();
    });

    function initEditLookAndFeel() {
        initLogoOptions();
        initFaviconOptions();
    }

    var OptionsSelector = function(options) {
        var currentSelectedId;

        options.click(function() {
            var $this = $(this);

            if (currentSelectedId) {
                $("#" + currentSelectedId + "-fields").hide();
                $("#" + currentSelectedId).removeClass('selected');
            }

            currentSelectedId = $this.attr('id');
            var radioOption = $('#' + currentSelectedId + '-input');
            $this.addClass('selected');
            radioOption.attr('checked', true);

            $("#" + currentSelectedId + "-fields").show();

        });

        for (var i = 0; i < options.length; i++) {
            var option = $(options.get(i));
            if (option.hasClass('selected')) {
                currentSelectedId = option.attr('id');
                $('#' + currentSelectedId + '-input').attr('checked', true);
                $("#" + currentSelectedId + "-fields").show();
            }
        }
    }

    function initLogoOptions() {
        var optionsSelector = "div#logo-options div.option";
        var logoOptions = $(optionsSelector);
        OptionsSelector(logoOptions);
    }

    function initFaviconOptions() {
        var optionsSelector = "div#favicon-options div.option";
        var faviconOptions = $(optionsSelector);
        OptionsSelector(faviconOptions);
    }

})(AJS.$);
