jQuery(document).ready(function(){
    var opts ={
        customInit : function(){
            var $container = AJS.$(".content-container > .content-body");

            var favouriteHandler = function(){
                JIRA.FavouritePicker.init($container);
            };

            var searchHandler = function(){
                var ajaxRequest = function(url){
                    AJS.$("#filter_search_results").empty();
                    AJS.$.ajax({
                        method: "get",
                        dataType: "html",
                        url: url + "&decorator=none&contentOnly=true&Search=Search",
                        success: function(result){
                            $container.html(result);
                            favouriteHandler();
                            searchHandler();
                            AJS.$("#mf_browse tr:first a, .filterPaging a").click(function(e){
                                ajaxRequest(AJS.$(this).attr("href"));
                                e.preventDefault();
                                e.stopPropagation();
                            });
                        }
                    });
                };
                JIRA.UserAutoComplete.init(AJS.$("form#filterSearchForm"));
                AJS.$("form#filterSearchForm").submit(function(){
                    ajaxRequest(contextPath + "/secure/ManageFilters.jspa?" + AJS.$(this).serialize());
                    return false;
                });
            };

            var dialogInitializer = function() {
                $container.find("a.delete-filter").each(function() {
                    var linkId = this.id;
                    new JIRA.FormDialog({
                        trigger: "#" + linkId,
                        autoClose : true
                    });
                });
            };

            JIRA.TabManager.navigationTabs.addLoadEvent("my-filters-tab", favouriteHandler);
            JIRA.TabManager.navigationTabs.addLoadEvent("fav-filters-tab", favouriteHandler);
            JIRA.TabManager.navigationTabs.addLoadEvent("popular-filters-tab", favouriteHandler);
            JIRA.TabManager.navigationTabs.addLoadEvent("search-filters-tab", favouriteHandler);
            JIRA.TabManager.navigationTabs.addLoadEvent("search-filters-tab", searchHandler);

            JIRA.TabManager.navigationTabs.addLoadEvent("my-filters-tab", dialogInitializer);
            JIRA.TabManager.navigationTabs.addLoadEvent("fav-filters-tab", dialogInitializer);

            dialogInitializer();
            searchHandler();

        }
    };
    JIRA.TabManager.navigationTabs.init(opts);
}); // I need to be first

