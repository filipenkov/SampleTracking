jira.widget.jsonLister = function (options) {
    options = options || {};
    AJS.$.extend(options, {
        ajaxOptions: function() {
           return {
               url: this.trigger.attr("href"),
               data: "json=true&decorator=none",
               dataType: "json"
           };
        },
        formatResults: function (response) {
           var html;
           AJS.$(response).each(function(){
                var listElNode = AJS.$("<li>"), linkNode;
                if (this.groupmarker) {
                    listElNode.addClass("groupmarker");
                }
                if (this.text && this.url && this.url !== "") {
                    linkNode = AJS.$("<a>").addClass("item").attr("href", this.url).text(this.text).appendTo(listElNode);
                    if (this.text.length > 100) {
                        linkNode.css({whiteSpace:"normal", width: 400 + "px"});
                    }
                } else if (this.text) {
                    listElNode.addClass("none").text(this.text);
                }
               if (!html) {
                    html = AJS.$(listElNode);
               } else {
                    html = html.add(listElNode);
               }

           });
           return html;
        }
    });
    return AJS.dropDown.Ajax.call(this, options);
};

AJS.$.fn.jsonLister = function (options) {
    return jira.widget.jsonLister.call(this, options);
};
