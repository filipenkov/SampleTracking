function displayPercentComplete(percent) {
		AJS.$(".stp-progress-amount").css("width",percent+"%");
		AJS.$(".stp-progress-bar-percent").html(percent);
		
		if (percent < 100) {
			refreshPercentComplete(2);
		}
		else {
			refreshHerculesModule();
		}
}

function refreshHerculesModule() {
	var servletHomePath = AJS.$("#hercules-module").data("servletHomePath");
	getPageViaAjax("hercules-module",servletHomePath + "/hercules/execute");
}

function refreshPercentComplete(delay) {
	var servletHomePath = AJS.$("#hercules-module").data("servletHomePath");
	setTimeout(function () { retrievePercentComplete(servletHomePath + "/hercules-percent-read/execute") }, delay * 1000);
}

function herculesToggleLogOptions() {
	var selectedOption = AJS.$("#herculesLogToggle").val();
	if (selectedOption != "Custom Log") 
	{
		AJS.$("#logFilePath").hide();
		AJS.$("#logFilePath").val(selectedOption);
	}
	else 
	{
		AJS.$("#logFilePath").show();
	}
}

function updateButtons() {
	if (startPosition > 0) {
		AJS.$("#stp-hercules-results-previous").removeClass('disabled');
		AJS.$("#stp-hercules-results-first").removeClass('disabled');
	}
	else {
		AJS.$("#stp-hercules-results-previous").addClass('disabled');
		AJS.$("#stp-hercules-results-first").addClass('disabled');
	}
	
	if (endPosition < numResults) {
		AJS.$("#stp-hercules-results-next").removeClass('disabled');
		AJS.$("#stp-hercules-results-last").removeClass('disabled');
	}
	else {
		AJS.$("#stp-hercules-results-next").addClass('disabled');
		AJS.$("#stp-hercules-results-last").addClass('disabled');
	}
}

function setViewport() {
	AJS.$("#stp-hercules-results table tbody tr").show();
	
	if (startPosition > 0) {
		AJS.$("#stp-hercules-results table tbody tr:lt(" + startPosition + ")").hide();
	}
	if (endPosition < numResults) {
		AJS.$("#stp-hercules-results table tbody tr:gt(" + (endPosition-1) + ")").hide();
	}
	AJS.$("#stp-hercules-results-start-position").text(startPosition+1);
	AJS.$("#stp-hercules-results-end-position").text(endPosition);
	
	updateButtons();
}												

function moveHigher() {
	if (startPosition < numResults - 10) { 
		startPosition+=10; 
		if (numResults < startPosition+10) {
			endPosition = numResults;
		}
		else {
			endPosition = startPosition + 10;
		}
		setViewport();
	}
}

function moveLower() {
	if (startPosition > 0) {
		startPosition -= 10;
		endPosition = startPosition + 10;
		setViewport();
	}
}

function moveToStart() {
	if (startPosition > 0) {
		startPosition = 0;
		endPosition = startPosition + 10;
		setViewport();
	}
}

function moveToEnd() {
		if (endPosition < numResults) {
			startPosition = numResults - (numResults%10);
			endPosition = numResults;
			setViewport();
		}
}
