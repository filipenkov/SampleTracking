AJS.$(function() {
    new JIRA.ToggleBlock({
        blockSelector: ".toggle-wrap",
        triggerSelector: ".mod-header h3",
        originalTargetIgnoreSelector: "a",
        storageCollectionName: "x-i-am-not-used",
        persist: false
    });
});