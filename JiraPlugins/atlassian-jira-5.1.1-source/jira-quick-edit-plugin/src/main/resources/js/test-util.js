function expectSuccessfulAjax(withData) {

    var oldAjax = jQuery.ajax;

    jQuery.ajax = function (options) {
        if (options.success)
        {
            options.success(withData, "success", {
                status: 200
            });
        }

        if (options.complete)
        {
            options.complete("success", {
                status: 200
            });
        }

        jQuery.ajax = oldAjax;
    }
}

function assertInvocationNotToUseAjax(msg, invocation) {
    var usedAjax = false,
        oldAjax = jQuery.ajax;

    jQuery.ajax = function () {
        usedAjax = true;
    };

    invocation();

    jQuery.ajax = oldAjax;

    ok(!usedAjax, msg)
}

function assertInvocationUsedAjax(msg, invocation) {
    var usedAjax = false,
        oldAjax = jQuery.ajax;

    jQuery.ajax = function () {
        usedAjax = true;
    };

    invocation();

    jQuery.ajax = oldAjax;

    ok(usedAjax, msg)
}