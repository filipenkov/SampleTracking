AJS.$(function() {
    if (AJS.HelpTip) {
        initNewUsersTip();
    }

    function initNewUsersTip() {
        var newUsersTip, tipAnchor;
        var inviteUserButton = AJS.$("#invite_user"), createUserButton = AJS.$("#create_user");
        if (inviteUserButton.length) {
            tipAnchor = AJS.$("<div></div>").css({
                "float": "right",
                "z-index":" -1",
                "height": "1px",
                "width": "1px",
                "position": "relative",
                "right": "-12px"
            }).appendTo(inviteUserButton);
            if (AJS.$.browser.msie) {
                tipAnchor.css({"bottom":"-20px"});
            }
            newUsersTip = new AJS.HelpTip({
                id: "add.new.users",
                title: AJS.I18n.getText("helptips.add.new.users.title"),
                body: AJS.I18n.getText("helptips.add.new.users.body"),
                url: createUserButton.data('url'),
                anchor: tipAnchor
            });
            inviteUserButton.click(function() { newUsersTip.dismiss("inviteuser"); });
            createUserButton.click(function() { newUsersTip.dismiss("createuser"); });

            newUsersTip.show();
        }
    }
});