/**
 * @constructor IssueAutoComplete
 * @param options
 */
JIRA.IssueAutoComplete = function(options) {

    // prototypial inheritance (http://javascript.crockford.com/prototypal.html)
    var that = begetObject(JIRA.RESTAutoComplete);


    that.getAjaxParams = function(){
        return {
            url: contextPath + "/rest/api/1.0/issues/picker",
            data: options.ajaxData,
            dataType: "json",
            type: "GET"
        };
    };

    /**
     * @method renderSuggestions
     * @param {Object} response
     */
    that.renderSuggestions = function(response) {

        var resultsContainer, suggestionNodes = [];

        // remove previous results
        this.clearResponseContainer();

        if (response && response.sections && response.sections.length > 0) {

            resultsContainer = AJS.$("<ul/>").appendTo(this.responseContainer);

            AJS.$(response.sections).each(function() {
                var section = this;
                var subSection = AJS.$("<div/>").attr("id", options.fieldID + "_s_" + section.id).addClass("yag").text(section.label);
                if (section.sub){
                    subSection.append(AJS.$("<span/>").addClass("yagt").text("(" + section.sub + ")"));
                }
                resultsContainer.append(AJS.$("<li/>").append(subSection).mouseover(function(){
                        AJS.$(this).addClass("active");
                    }).mouseout(function(){
                        AJS.$(this).removeClass("active");
                    })
                );

                if (section.msg){
                    // add message node
                    var msg = AJS.$("<div/>").attr("id", options.fieldID + "_i_" + section.id + "_n").addClass("yad").text(section.msg);
                    resultsContainer.append(AJS.$("<li/>").append(msg).mouseover(function(){
                            AJS.$(this).addClass("active");
                        }).mouseout(function(){
                            AJS.$(this).removeClass("active");
                        })
                    );
                }

                if (section.issues && section.issues.length > 0){
                    AJS.$(section.issues).each(function(){
                        // add issue
                        var imgUrl;
                        if (/^http/.test(this.img)){
                            imgUrl = this.img;
                        } else {
                            imgUrl =  contextPath + this.img;
                        }
                        var issueNode = AJS.$("<li/>").append(
                            AJS.$("<div/>").attr("id", options.fieldID + "_i_" + section.id + "_" + this.key).addClass("yad").append(
                                AJS.$("<table/>").addClass("yat").attr({
                                    cellpadding: "0",
                                    cellspacing: "0"
                                }).append(
                                    AJS.$("<tr/>").append(
                                        AJS.$("<td/>").append(
                                            AJS.$("<img/>").attr("src", imgUrl)
                                        )
                                    ).append(
                                         AJS.$("<td/>").append(
                                            AJS.$("<div/>").addClass("yak").html(this.keyHtml)
                                        )
                                    ).append(
                                         AJS.$("<td/>").css("width", "100%").html(this.summary)
                                    )
                                )
                            )
                        );

                        resultsContainer.append(issueNode);
                        suggestionNodes.push([issueNode, this.key]);
                    });
                }
            });

            that.addSuggestionControls(suggestionNodes);

            return suggestionNodes;

        }
    };
    options.minQueryLength = 1;
    options.queryDelay = 0.25;

    that.init(options);

    return that;

};

JIRA.IssueAutoComplete.init = function(){

    AJS.$("fieldset.issue-picker-params").each(function(){
        var params = JIRA.parseOptionsFromFieldset(AJS.$(this)),
            $container = AJS.$("#" + params.fieldId + "-container").add("#" + params.fieldName + "_container");

        $container.find("a.popup-trigger").click(function(e){
            var url = contextPath + '/secure/popups/IssuePicker.jspa?';
            url += 'currentIssue=' + params.currentIssueKey + '&';
            url += 'singleSelectOnly=' + params.singleSelectOnly + '&';
            url += 'showSubTasks=' + params.showSubTasks + '&';
            url += 'showSubTasksParent=' + params.showSubTaskParent;
            if (params.currentProjectId && params.currentProjectId != "")
            {
                url += '&selectedProjectId=' + params.currentProjectId;
            }

            /**
             * Provide a callback to the window for execution when the user selects an issue. This implies that only one
             * popup can be displayed at a time.
             *
             * @param keysMap the issue keys selected.
             */
            JIRA.IssuePicker.callback = function(keysMap){
                var $formElement, keys = [];

                keysMap = JSON.parse(keysMap);

                if (params.fieldId && keys) {
                    $formElement = AJS.$("#" + params.fieldId);
                    if ($formElement){
                        AJS.$.each(keysMap, function () {
                            keys.push(this.value);
                        });
                        $formElement.val(keys.join(", "));
                    }
                }
            };

            var vWinUsers = window.open(url, 'IssueSelectorPopup', 'status=no,resizable=yes,top=100,left=200,width=620,height=500,scrollbars=yes,resizable');
            vWinUsers.opener = self;
            vWinUsers.focus();
            e.preventDefault();
        });

        if (!params.fieldId) {
            params.fieldId = params.fieldName;
        }

        if (params.issuePickerEnabled === true){
            JIRA.IssueAutoComplete({
                fieldID: params.fieldId,
                delimChar: params.singleSelectOnly === true ? undefined : ",",
                ajaxData: params
            });
        }
    });
};

/** Preserve legacy namespace
    @deprecated jira.widget.autocomplete.Issues */
AJS.namespace("jira.widget.autocomplete.Issues", null, JIRA.IssueAutoComplete);
