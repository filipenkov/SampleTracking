AG.sysMsg = function () {

    var $sysMsg = jQuery("<div id='ag-sys-msg' />").appendTo("body"),
        templates = AG.templates.messages;

    // we do not want to append the same message more than once, so search if we already have one
    function getMessageWithText(msg) {
        var $msg;

        $sysMsg.find(".aui-message p").each(function () {
            var $this = jQuery(this);
            if ($this.text() === jQuery("<div />").html(msg).text()) {
                $msg = $this.closest(".aui-message");
                return false;
            }
        });

        return $msg;
    }

    return {

        _add: function (template, options) {

            var $existingMsg,
                $msg;

            if (typeof options === "string") {
                options = {
                    message: options
                }
            }

            $existingMsg = getMessageWithText(options.message);

            if ($existingMsg) {
                $existingMsg.remove();                                      
            }

            $msg = jQuery(templates.info({
                message: options.message
            }));

            $msg.appendTo($sysMsg)

            $sysMsg.show();

            gadgets.window.adjustHeight();

            return $msg;
            
        },

        addError: function (options) {
            return this._add(templates.error, options);
        },

        addInfo: function (options) {
            return this._add(templates.info, options);
        }
    }

}();


AG.sysMsg.addOauthApprovalMsg = function () {

    var approvalMessages = {};

    function getApprovalBase (approvalUrl) {
        var queryIdx = approvalUrl.indexOf("?");
        if (queryIdx) {
            return approvalUrl.substring(0, queryIdx);
        } else {
            return approvalUrl
        }
    }

    return function (options) {

        var url,
            $msg,
            $prevRequest,
            $request,
            $requestLink,
            approvalMsgKey;

        if (options && options.oauthApprovalUrl) {

            approvalMsgKey = getApprovalBase(options.oauthApprovalUrl);

            if (approvalMessages[approvalMsgKey]) {

                $msg = approvalMessages[approvalMsgKey];

                $msg.find(".oauth-approve")
                        .replaceWith(AG.ajax.OAuth.getApprovalButton(options.oauthApprovalUrl, options));

            } else {
                $msg = AG.sysMsg.addInfo({
                    message: AG.getText("gadget.common.oauth.approve.message")
                });

                $msg.append("<ul class='ag-requests' />");

                $msg.find(".ag-show-requests").click(function (e) {
                    $msg.find(".ag-requests").show();
                    gadgets.window.adjustHeight();
                    e.preventDefault();
                });

                $msg.append(AG.ajax.OAuth.getApprovalButton(options.oauthApprovalUrl, options));

                approvalMessages[approvalMsgKey] = $msg;
            }

            $prevRequest = $msg.find("a[href='" + options.url + "']");

            if ($prevRequest.length === 0) {

                url = AG.getAbsoluteURL(options.url);

                $request = jQuery("<li />").appendTo($msg.find(".ag-requests"));

                $requestLink = jQuery("<a />").attr({
                        href: url
                    })
                    .text(AG.shrinkText(url, 50))
                    .appendTo($request);
            }

            gadgets.window.adjustHeight();
        }
    };
    
}()