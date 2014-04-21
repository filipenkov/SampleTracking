(function($) {
    /**
     * This wrapper for JIRA.SmartAjax.makeRequest() to ensure that WebSudo authentication is active for the current user.
     *
     * It will pop up a dialog if necessary to perform the WebSudo login request by posting the WebSudo form to authenticate the user.
     *
     * There are a couple of gotchas in the way WebSudo responds and they way certain *cough* IE *cough*
     * handle said response.
     *
     * 1. When the WebSudo authentication is successful, a 302 redirect is performed and the header
     * X-Atlassian-WebSudo is set to Has-Authentication.
     *
     * 2. WebSudo the WebSudo authentication fails, the responseText is then taken and
     * inserted into the dialog showing the error message.
     *
     * 3. IE 8 and 9 fail to process the 302 redirect at all. They respond with xhr.status equal to zero (0) and
     * xhr.aborted as undefined. This combination is important as a zero status can be returned if a request is
     * aborted. In this case, the only way to continue for IE is to assume WebSudo has been successfully
     * granted and to retry the plugin install. If the WebSudo process has not completed successfully
     * the worst that can happen is that the WebSudo dialog is shown again.
     *
     * This dodgy behaviour was shown to occur when using XMLHttpRequest natively (i.e. without jQuery) meaning
     * that it is more than likely an internal IE problem.
     *
     * @method makeWebSudoRequest
     * @param {Object} ajaxOptions - the options to control the ajax call
     * @returns the xhr object just like jQuery.ajax() does
     */
    JIRA.SmartAjax.makeWebSudoRequest = function(ajaxOptions) {
        var errorHandler = function(xhr, statusText, errorThrown, smartAjaxResult) {
            if (xhr.status === 401 && xhr.responseText.match(/websudo/i)) {
                var dialog = new JIRA.FormDialog({
                    id: "smart-websudo",
                    type: "ajax",
                    ajaxOptions: {
                        url: contextPath + "/secure/admin/WebSudoAuthenticate!default.jspa?close=true"
                    },
                    submitHandler: function(e) {
                        e.preventDefault();
                        var form = $(e.target);
                        JIRA.SmartAjax.makeRequest({
                            url: form.attr("action"),
                            data: form.serialize(),
                            type: "POST",
                            complete: function(xhr, textStatus, response) {
                                if (xhr.getResponseHeader("X-Atlassian-WebSudo") === "Has-Authentication" ||
                                        (jQuery.browser.msie && response.aborted === undefined && xhr.status === 0)) {
                                    JIRA.SmartAjax.makeWebSudoRequest(ajaxOptions);
                                } else {
                                    dialog._setContent(xhr.responseText, true);
                                }
                            }
                        });
                    }
                });
                dialog.show();
            } else if ($.isFunction(ajaxOptions.error)) {
                ajaxOptions.error(xhr, statusText, errorThrown, smartAjaxResult);
            }
        };

        // recursively merge the default and user-provided options
        var ourAjaxOptions = $.extend(true, {}, ajaxOptions);

        // but use our our handlers that delegate back to them
        ourAjaxOptions.error = errorHandler;

        return JIRA.SmartAjax.makeRequest(ourAjaxOptions);
    };
})(AJS.$);