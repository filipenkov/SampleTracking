AJS.$(function() {
	var collectorPreview = AJS.$(".collector-preview");
    collectorPreview.load(function () {
		collectorPreview[0].contentWindow.postMessage("{}", "@baseUrl");
    });
});

