JIRA.FRAGMENTS.menuFragment = function (response) {

    var html = AJS.$("<div class='aui-list' />");
    var isFirst = true;
    var listNode;
    AJS.$(response.sections).each(function()
    {
        listNode = AJS.$("<ul class='aui-list-section' />");
        var listItemNode, linkNode;

        if (this.id)
        {
            listNode.attr("id", this.id);
        }
        if (this.style)
        {
            listNode.addClass(this.style);
        }
        if (this.items  && this.items.length != 0)
        {
            if (isFirst)
            {
                listNode.addClass("first");
                isFirst = false;
            }
            if (this.label)
            {
                html.append(AJS.$("<h5/>").text(this.label));
            }
            AJS.$(this.items).each(function()
            {
                listItemNode = AJS.$("<li class='aui-list-item' />");
                if (this.id)
                {
                    listItemNode.attr("id", this.id);
                }
                if (this.style)
                {
                    listItemNode.addClass(this.style);
                }
                linkNode = AJS.$("<a class='aui-list-item-link' />").attr("href", this.url);
                if (this.id)
                {
                    linkNode.attr("id", this.id + "_lnk");
                }
                if (this.title)
                {
                    linkNode.attr("title", this.title);
                }
                if (this.iconUrl)
                {
                    linkNode.addClass("aui-iconised-link").css("background-image", "url('" + this.iconUrl + "')");
                }
                if (this.label)
                {
                    linkNode.text(this.label);
                }
                listItemNode.append(linkNode);
                listNode.append(listItemNode);

            });
            html.append(listNode);
        }
    });

    listNode.addClass("aui-last");

    return html;

};

