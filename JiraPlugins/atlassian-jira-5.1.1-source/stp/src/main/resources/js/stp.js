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
		var mainContainer =  AJS.$("#"+moduleId+" .home-page-wrapper");

		if (container != null  && container.length > 0) {
			container.data("oldContent",container.html());
			container.html("");
			container.addClass("loading");
			container.removeClass("loaded");
		}
		else if (mainContainer != null  && mainContainer.length > 0) {
			mainContainer.data("oldContent",mainContainer.html());
			mainContainer.html("");
			mainContainer.addClass("loading");
			mainContainer.removeClass("loaded");
		}

		AJS.$.ajax({
			async:"true",
			// This has to be quite high to allow mail timeouts to function correctly.
			timeout:325000,
			cacheBoolean:"false",
			data:formData,
			dataType:"html",
			error:function(XMLHttpRequest, textStatus, errorThrown) { displayAjaxError(moduleId,XMLHttpRequest,textStatus,errorThrown);},
			success:function(data, textStatus, XMLHttpRequest){displayAjaxResults(moduleId,data);},
			type:"POST",
			url:formAction
		});
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
	var mainContainer =  AJS.$("#"+moduleId+" .home-page-wrapper");

	if (container != null && container.length > 0) {
		container.data("oldContent",container.html());
		
		container.html("");
		container.addClass("loading");
		container.removeClass("loaded");
	}
	else if (mainContainer != null && mainContainer.length > 0) {
		mainContainer.data("oldContent",mainContainer.html());
		
		mainContainer.html("");
		mainContainer.addClass("loading");
		mainContainer.removeClass("loaded");
	}
		
	AJS.$.ajax({
		async:"true",
		cacheBoolean:"false",
		dataType:"html",
		error:function(XMLHttpRequest, textStatus, errorThrown) { displayAjaxError(moduleId,XMLHttpRequest,textStatus,errorThrown);},
		success:function(data, textStatus, XMLHttpRequest){displayAjaxResults(moduleId,data);},
		// This has to be quite high to allow mail timeouts to function correctly.
		timeout:325000,
		type:"GET",
		url:URL
	});
}

function displayAjaxResults(moduleId,data) {
	// the container we'll put our results into
	var container = AJS.$("#"+moduleId+"-details");
	var mainContainer =  AJS.$("#"+moduleId+" .home-page-wrapper");
	
	if (container != null && container.length > 0) {
		container.html(data);	
		
		// We need to bind the usual functions to the new content
		convertLinksAndForms(container);
		container.addClass("loaded");
		container.removeClass("loading");
	}
	else if (mainContainer != null && mainContainer.length > 0) {
		mainContainer.html(data);	
		
		// We need to bind the usual functions to the new content
		convertLinksAndForms(mainContainer);
		mainContainer.addClass("loaded");
		mainContainer.removeClass("loading");
	}
}

function displayAjaxError(moduleId,XMLHttpRequest,textStatus,errorThrown) {
	// the container we'll put our results into
	var container = AJS.$("#"+moduleId+"-details");
	var mainContainer =  AJS.$("#"+moduleId+" .home-page-wrapper");
	
	var errorContent;
	if (XMLHttpRequest.status == 403) {
		errorContent = "<p class=\"title\">Error loading content, please reload, log in as an administrator, and try again.</p>"
	}
	else if (errorThrown.match(/timeout/)) {
		errorContent = "<p class=\"title\">Your request timed out.  Please try again.  If you're sending a support request, please check your mail settings.</p>";
	}
	else {
		errorContent = "<p class=\"title\">Error retrieving results:</p><p>" + errorThrown + "</p>";
	}		
	
	// prepend the error to the previous page so that we at least have our original content
	var prettyError = "<div class=\"aui-message error\">"+errorContent+"</div> "
	
	if (container != null && container.length > 0) {
		container.html(prettyError+container.data("oldContent"));
		
		// Bind the usual functions to the new content
		convertLinksAndForms(container);
		container.addClass("loaded");
		container.removeClass("loading");
	}
	else if (mainContainer != null && mainContainer.length > 0) {
		mainContainer.html(prettyError+mainContainer.data("oldContent"));
		
		// Bind the usual functions to the new content
		convertLinksAndForms(mainContainer);
		mainContainer.addClass("loaded");
		mainContainer.removeClass("loading");
	}
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
