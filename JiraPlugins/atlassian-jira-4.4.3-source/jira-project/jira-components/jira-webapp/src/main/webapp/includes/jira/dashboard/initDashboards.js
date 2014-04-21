jQuery(function(){
    var opts ={
        customInit : function(){

            /**
             * Called to bind the operations cog drop downs to the markup
             */
            var bindOperationDropDowns = function() {
                var $container = AJS.$("#main-content");

                AJS.Dropdown.create({
                    trigger: $container.find(".aui-dd-link"),
                    content: $container.find(".aui-list"),
                    alignment: AJS.RIGHT
                });
            }


            var favouriteHandler = function() {
                bindOperationDropDowns();
                JIRA.FavouritePicker.init(AJS.$(".active-area"));
            };

            var searchHandler = function() {
                bindOperationDropDowns();
                var ajaxRequest = function(url){
                    AJS.$.ajax({
                        method: "get",
                        dataType: "html",
                        url: url + "&decorator=none&contentOnly=true&Search=Search",
                        success: function(result){
                            AJS.$(".active-area").html(result);
                            favouriteHandler();
                            searchHandler();
                            AJS.$("#pp_browse tr:first a, .filterPaging a").click(function(e){
                                ajaxRequest(AJS.$(this).attr("href"));
                                e.preventDefault();
                                e.stopPropagation();
                            });
                        }
                    });
                };
                JIRA.UserAutoComplete.init(AJS.$("form#pageSearchForm"));
                AJS.$("form#pageSearchForm").submit(function(){
                    ajaxRequest(contextPath + "/secure/ConfigurePortalPages!default.jspa?" + AJS.$(this).serialize());
                    return false;
                });
            };

            var dialogInitializer = function() {
                new JIRA.FormDialog({
                        trigger: ".active-area a.delete_dash"
                });
            };

            JIRA.TabManager.navigationTabs.addLoadEvent("my-dash-tab", favouriteHandler);
            JIRA.TabManager.navigationTabs.addLoadEvent("favourite-dash-tab", favouriteHandler);
            JIRA.TabManager.navigationTabs.addLoadEvent("popular-dash-tab", favouriteHandler);
            JIRA.TabManager.navigationTabs.addLoadEvent("search-dash-tab", favouriteHandler);
            JIRA.TabManager.navigationTabs.addLoadEvent("search-dash-tab", searchHandler);

            JIRA.TabManager.navigationTabs.addLoadEvent("my-dash-tab", dialogInitializer);
            JIRA.TabManager.navigationTabs.addLoadEvent("favourite-dash-tab", dialogInitializer);

            dialogInitializer();
            searchHandler();


            AJS.$(document).delegate(".active-area .dash-reorder a", "click", function(e){
                e.preventDefault();
                var $this = AJS.$(this);

                AJS.$.ajax({
                    method: "get",
                    dataType: "html",
                    url: $this.attr("href") + "&decorator=none&contentOnly=true",
                    success: function(result){
                        AJS.$(".active-area").html(result);
                        favouriteHandler();
                        recolourSimpleTableRows($this.closest('table').attr('id'));
                    }
                });

            });
        },
        getTabRegEx: /view=.*/,
        checkQualifiedUrlRegEx: /\?(?=view=)/
    };
    JIRA.TabManager.navigationTabs.init(opts);
});
