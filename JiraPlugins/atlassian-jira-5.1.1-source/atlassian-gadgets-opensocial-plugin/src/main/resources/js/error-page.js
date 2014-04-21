AJS.toInit(function($) {
    var setIframeHeightToCurrentScrollHeight = function() {
        parent.document.getElementById(window.name).height =
            parent.document.getElementById(window.name).contentWindow.document.body.scrollHeight + 10;
        parent.AG.DashboardManager.getLayout().refresh();
    };

    AJS.$(".renderer-error-reason").hide();
    setIframeHeightToCurrentScrollHeight();
    AJS.$(".renderer-error-details").hover(
        function(){
            AJS.$(".renderer-error-toggle").addClass("hover");
        },
        function(){
            AJS.$(".renderer-error-toggle").removeClass("hover");
        }
    );

    AJS.$(".renderer-error-details").click(function() {
        AJS.$(".renderer-error-reason").slideToggle(100, setIframeHeightToCurrentScrollHeight);
    });
});

