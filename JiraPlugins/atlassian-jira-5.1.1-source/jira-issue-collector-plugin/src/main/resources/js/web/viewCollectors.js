AJS.$(function() {

    var ajaxOperation = function(method, url, confirmTxt) {
        var answer = true;
        if(confirmTxt) {
            answer = confirm(confirmTxt);
        }
        if(answer) {
            AJS.$.ajax({
                type: method,
                url: url,
                success: function() {
                    window.location.reload();
                },
                error:function(response) {
                    var msgContainer = AJS.$(".ajax-msg-container");
                    AJS.messages.error(msgContainer, {
                        title:AJS.I18n.getText("collector.plugin.template.ajaxerror"),
                        body: "<p>" + AJS.I18n.getText("collector.plugin.template.ajaxerror.msg") + "</p>",
                        closeable: true
                    });
                }
            });
        }
    };

    AJS.$(".disable-collector-lnk").click(function(e) {
        e.preventDefault();
        ajaxOperation("delete", AJS.$(this).attr("href"));
    });

    AJS.$(".enable-collector-lnk").click(function(e) {
        e.preventDefault();
        ajaxOperation("post", AJS.$(this).attr("href"));
    });

    AJS.$(".delete-collector-lnk").click(function(e) {
        e.preventDefault();
        ajaxOperation("delete", AJS.$(this).attr("href"), AJS.I18n.getText("collector.plugin.admin.collector.delete.confirm"));
    });

    AJS.$(".clear-errors-lnk").click(function(e) {
        e.preventDefault();
        ajaxOperation("delete", AJS.$(this).attr("href"));
    });

    AJS.$(".activitysparkline").sparkline('html', {lineWidth:1, spotRadius:0});

    AJS.$(".script-source").click(function(e) {
        AJS.$(this).focus().select().unbind("click");
    });

    AJS.$("input.embed-type").click(function(e) {
        AJS.$("#scriptlet-val .script-source").removeClass("hidden").hide();
        AJS.$("#script-source-" + AJS.$(this).val()).show();
    });

    AJS.$(".url-to-copy").focus(function(){
        this.select();
    }).mouseup(function(e) {
                e.preventDefault();
            });
});