AJS.$(function(){

    AJS.$("a.project-config-icon-viewworkflow").each(function() {
        AJS.$(this).fancybox({
            type: "image",
            href: this.href,
            title: AJS.$(this).closest(".project-config-scheme-item-header").find(".project-config-workflow-name").text(),
            titlePosition: "outside",
            imageScale: true,
            centerOnScroll: true,
            overlayShadow: true
        });
    });
});