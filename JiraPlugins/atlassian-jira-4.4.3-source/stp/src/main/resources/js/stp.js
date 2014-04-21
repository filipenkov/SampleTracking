// A function to submit a form's data and update a container with the results
// This function should only be bounce to the submit method of a form
function submitFormViaAjax(event) {
	// "this" = the submit button contained in the form we care about
	event.preventDefault();

	var moduleId = AJS.$(this).parents(".stp-module").attr("id");

	if (moduleId != null) {
		var form = AJS.$("#"+moduleId+" form.aui");
		var formData = form.serialize();
		var formAction = form.attr("action");
		
		var container = AJS.$("#"+moduleId+"-details");
		if (container != null) {
			container.data("oldContent",container.html());
			var columnContainer = container.find("#stp-module-main-block");
			
			if (columnContainer != null) {
				columnContainer.html("");
				columnContainer.addClass("loading");
				columnContainer.removeClass("loaded");
			}
			else {
				container.html("");
				container.addClass("loading");
				container.removeClass("loaded");
			}
			
			AJS.$.ajax({
				async:"true",
				cacheBoolean:"false",
				data:formData,
				dataType:"html",
				error:function(XMLHttpRequest, textStatus, errorThrown) { displayAjaxError(moduleId,XMLHttpRequest,textStatus,errorThrown);},
				success:function(data, textStatus, XMLHttpRequest){displayAjaxResults(moduleId,data);},
				timeout:10000,
				type:"POST",
				url:formAction
			});
		}
	}
}

// when someone clicks a placeholder link to switch to another module, expand the module and contract any others
function expandHighlightedModule(event) {
	var moduleHandle = AJS.$(this).attr("href");
	AJS.$("li.menu-item a[href='" + moduleHandle + "']'").click();
}


// A function to convert a link into an AJAX call.  Should be bound to a link.
function loadLinkViaAjax(event) {
	event.preventDefault()
	
	// "this" = the link we're bound to
	var moduleId = AJS.$(this).parents(".stp-module").attr("id");
	
	if (moduleId != null) {
		getPageViaAjax(moduleId,AJS.$(this).attr("href"));
	}
}

function getPageViaAjax(moduleId,URL) {
	var container = AJS.$("#"+moduleId+"-details");
	if (container != null) {
		container.data("oldContent",container.html());
		
		var columnContainer = container.find("#stp-module-main-block");
		if (columnContainer != null) {
			columnContainer.html("");
			columnContainer.addClass("loading");
			columnContainer.removeClass("loaded");
		}
		else {
			container.html("");
			container.addClass("loading");
			container.removeClass("loaded");
		}
		
		AJS.$.ajax({
			async:"true",
			cacheBoolean:"false",
			dataType:"html",
			error:function(XMLHttpRequest, textStatus, errorThrown) { displayAjaxError(moduleId,XMLHttpRequest,textStatus,errorThrown);},
			success:function(data, textStatus, XMLHttpRequest){displayAjaxResults(moduleId,data);},
			timeout:10000,
			type:"GET",
			url:URL
		});
	}
}

function displayAjaxResults(moduleId,data) {
	// the container we'll put our results into
	var container = AJS.$("#"+moduleId+"-details");
	if (container != null) {
		container.html(data);	
		
		// We need to bind the usual functions to the new content
		convertLinksAndForms(container);
	}
	container.addClass("loaded");
	container.removeClass("loading");
}

function displayAjaxError(moduleId,XMLHttpRequest,textStatus,errorThrown) {
	// the container we'll put our results into
	var container = AJS.$("#"+moduleId+"-details");
	if (container != null) {
		var errorContent;
		if (XMLHttpRequest.status == 403) {
			errorContent = "<p class=\"title\">Error loading content, please reload, log in as an administrator, and try again.</p>"
		}
		else {
			errorContent = "<p class=\"title\">Error retrieving results:</p><p>" + errorThrown + "</p>";
		}		
		// prepend the error to the previous page so that we at least have our original content
		var prettyError = "<div class=\"aui-message error\">"+errorContent+"</div> "
		container.html(prettyError+container.data("oldContent"));
		
		// Bind the usual functions to the new content
		convertLinksAndForms(container);
	}
	container.addClass("loaded");
	container.removeClass("loading");
}

function retrievePercentComplete(url) {
	var moduleId = "hercules-module";
	var container = AJS.$("#"+moduleId+"-details");
	container.data("oldContent",container.html());
	AJS.$.ajax({
		async:"true",
		cacheBoolean:"false",
		dataType:"html",
		error:function(XMLHttpRequest, textStatus, errorThrown) { displayPercentComplete("0");},
		success:function(data, textStatus, XMLHttpRequest){displayPercentComplete(data);},
		timeout:10000,
		type:"GET",
		url:url
	});
}

function convertLinksAndForms(domObject) {
	domObject.find('.module-ajax-link').click(loadLinkViaAjax);
	domObject.find('form.aui').submit(submitFormViaAjax);
	domObject.find("a.stp-module-link").click(expandHighlightedModule);
}

AJS.$(document).ready(function() {
	// Bind "our" links to an AJAX function that retrieves their data
	convertLinksAndForms(AJS.$(document));

    var $tabs,
    $tabMenu,
    REGEX = /#.*/,
    ACTIVE_TAB = "active-tab",
    ACTIVE_PANE = "active-pane";
	
	// Remove after AUI has been updated to use this change method.
	AJS.tabs.change = function ($a, e) {
        var $pane = AJS.$($a.attr("href").match(REGEX)[0]);
 
        $pane.addClass(ACTIVE_PANE).siblings().removeClass(ACTIVE_PANE);
        $a.closest('ul.tabs-menu').find('.menu-item').removeClass(ACTIVE_TAB);
        $a.parent("li.menu-item").addClass(ACTIVE_TAB);
 
        $a.trigger("tabSelect", {
            tab: $a,
            pane: $pane
        });
	}
});
