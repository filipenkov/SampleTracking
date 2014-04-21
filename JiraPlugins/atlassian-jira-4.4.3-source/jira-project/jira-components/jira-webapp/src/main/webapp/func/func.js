if (typeof console === undefined) {
    console = {
        log: function(){},
        warn: function() {},
        error: function() {}
    }
}

/**
 * @function funcmaster_onload
 */
funcmaster_onload = function ()
{
    //
    // add an event the text commands
    var removeCommandTextHandler = function() {
        jQuery('#commandsText').css("display","none");
    }

    jQuery("#commandsText").blur(removeCommandTextHandler);
    jQuery('#frameHolder').load(preparePageEvents);

    preparePageEvents();
}

var eventsToListen = new Array();

/**
 * @function addEventsToListen
 * @param {String} tagName
 * @param {String} eventName
 * @param {Function} alternativeHandlerMethod
 */
function addEventsToListen(tagName, eventName, alternativeHandlerMethod)
{
    var eventToListen = new Object();
	eventToListen.tagName = tagName;
	eventToListen.eventName = eventName;
    if (alternativeHandlerMethod)
    {
        eventToListen.alternativeHandlerMethod = alternativeHandlerMethod;
    }

    eventsToListen[eventsToListen.length] = eventToListen;
}

/**
 * @function preparePageEvents
 */
function preparePageEvents()
{
    var delayedClosure = function() {
        //debug('Preparing Page Events');
        jQuery('#currentUrl').val(idoc().location);
        for (var j = 0; j < eventsToListen.length ; j++)
        {
            var anchors = idoc().getElementsByTagName(eventsToListen[j].tagName);
            for (var i = 0; i < anchors.length ; i++)
            {
                if (eventsToListen[j].alternativeHandlerMethod)
                {
                    jQuery(anchors[i])[eventsToListen[j].eventName](eventsToListen[j].alternativeHandlerMethod);
                }
                else
                {
                    jQuery(anchors[i])[eventsToListen[j].eventName](handleNewEvent);
                }
            }
        }
    }
    delayedClosure();
}

/**
 * @function idoc
 * @return {HTMLelement}
 */
function idoc()
{
    return frames['frameHolder'].document;
}

/**
 * @function ifr
 * @return {HTMLelement}
 */
function ifr()
{
    return frames['frameHolder'];
}

/**
 * @function loadPage
 * @return {Boolean}
 */
function loadPage()
{
    jQuery('#frameHolder').attr("src", jQuery('#currentUrl').val());
    return false;
}

/**
 * @function handleNewEvent
 * @param {Object} e
 */
function handleNewEvent(e)
{
    var o = e.currentTarget;
    var url = 'http://localhost:8080/jira/secure/FuncTestWriterAddEvent.jspa';
    var pars = 'decorator=none';
    if (o.id)
    {
        pars = pars + '&elementId=' + o.id;
    }


    pars = pars + '&tagName=' + o.nodeName ;
    pars = pars + '&eventType=' + e.type ;
    pars = pars + '&innerHtml=' + o.innerHTML;

    info('Params: ' + pars);
    callAjax(url, pars);

    return true;
}



function callAjax(url, pars)
{
    var hiddenIframe = frames['hiddenIFrame'];
    jQuery('#hiddenIFrame').load(handleDodgyAjax);
    hiddenIframe.window.location = url + '?' + pars;
}

function handleDodgyAjax()
{
    var hiddenIframe = frames['hiddenIFrame'];
    var data = hiddenIframe.document.getElementById('data');
    if (data && data.innerHTML && data.innerHTML != '')
    {
        info('Response: ' + data.innerHTML);
        addToList(data.innerHTML);
    }
    hiddenIframe.onload = null;
}

function saveUrl()
{
    saveCookie('currentUrl', jQuery('#currentUrl').val());
}

function loadUrl()
{
    var url = readCookie('currentUrl');
    if (url && url != '')
    {
        jQuery('#currentUrl').val(url);
        loadPage();
    }
}

function getSelectedLabel(selectObject)
{
    var selectedValues = '';
    for (var i = 0; i < selectObject.length; i++)
    {
        if(selectObject.options[i].selected)
        {
            return trimLine(selectObject.options[i].innerHTML);
        }
    }

    return selectedValues;
}

function trimLine(str)
{
    return str.replace(/^[\s|\n]+|[\s|\n]+$/, '');
}


function getSel()
{
	var txt = '';
	txt = idoc().getSelection();
    return txt;
}

function assertPresent()
{
    var t = getSel();
    if (t && t != '')
        addToList("tester.assertTextPresent(\""+t+"\");");
}

function assertCollapsedText()
{
    var t = getSel();
    if (t && t != '')
    {
        // Collapse whitespace within the User's selected text
        t = collapseWhitespace(t);
        addToList("text.assertTextPresent(new WebPageLocator(tester), \""+t+"\");");
    }
}

var beforeTxt = '';
function assertBefore()
{
    var t = getSel();
    if (t && t != '')
    {
        if (beforeTxt != '')
        {
            // TODO: How to deal with assertTextPresentBeforeText in the new framework??
            addToList("assertTextPresentBeforeText(\""+beforeTxt+"\", \""+t+"\");");
            beforeTxt = '';
            jQuery('#assertBefore').html('Assert Before...');
        }
        else
        {
            beforeTxt = t;
            jQuery('#assertBefore').html('Assert after \"' + beforeTxt + '\"');
        }
    }
}

function displayAllIssues()
{
    var searchBox = idoc().getElementById('quickSearchInput');
    searchBox.value = '';
    searchBox.form.submit();
    addToList("// Display all issues in the Issue Navigator.");
    addToList("navigation.issueNavigator().displayAllIssues();");
}

function removeListItem(o)
{
    var parentLi = findParentNodeOfTag('LI', o);

       jQuery(parentLi).slideUp("fast", function(){
           jQuery(this).remove();
       });
    
}


function findParentNodeOfTag(nodeName, o)
{
    var parentNode = o.parentNode;
    while (parentNode)
    {
        if (parentNode.nodeName == nodeName)
            return parentNode;
        else
            parentNode = parentNode.parentNode;
    }

    return;
}

htmlEsc = function(str)
{
    var div = document.createElement('div');
    var text = document.createTextNode(str);
    div.appendChild(text);
    return div.innerHTML;
}


var commands = [];

function removeAllCommands()
{
    jQuery('#commands li').remove();
    commands = [];
}

function addResponseToList(originalRequest)
{
    info('Response: ' + originalRequest.responseText)
    addToList(originalRequest.responseText);
}

function addToList(message)
{

    commands.push(message);

    jQuery("#commandsList").append(jQuery(document.createElement("li")).html("<a class='deleteLink' href='#' onclick='removeListItem(this);return false;'>del</a>" + htmlEsc(message)));


    /**
     * Calculates how far it needs to scroll to make the entire dropdown visible
     * @function {private} getScrollPosition
     * @return {Integer} position to scroll to
     */
    var getScrollPosition = function() {
        return (jQuery("#commandsInner").get(0).scrollHeight - jQuery("#commandsInner").outerHeight());

    };

    if (getScrollPosition() > jQuery("#commandsInner").scrollTop()) {
        jQuery("#commandsInner").animate({scrollTop: getScrollPosition()}, 300, "linear");
    }
    
   // jQuery("#" + commandsText).css("display",'none');
}

function extractCommands()
{
    jQuery("#commandsText").css("display",'');

    //var commandsLis = $('commandsList').getElementsByTagName('LI');
    var commandsLis = commands;
    var s = '';
    for (var i=0;i<commandsLis.length;i++)
    {
        //var command = commandsLis[i].lastChild.data;
        var command = commandsLis[i];
        s = s + command + '\n';
    }
    jQuery('#commandsText').val(s);

    jQuery('#commandsText').focus();
}

/**
 * Refreshes the values in the Table and Form drop-downs in the toolbar
 */
function refreshTools()
{
    refreshTableTool();
    refreshFormTool();
}

function refreshTableTool()
{
    // Refresh the tables drop-down.
    var ddTable = document.getElementById("ddTable");
    // First remove the current options
    ddTable.innerHTML = "";

    // Create a blank <option> element
    var blankOption = document.createElement("option");
    // Add text to the element
    blankOption.appendChild(document.createTextNode(""));
    // Add this <option> to the drop-down
    ddTable.appendChild(blankOption);

    // Find all the tables in the JIRA page:
    var tables = idoc().getElementsByTagName('table');
    for (var i = 0; i < tables.length; i++)
    {
        // Get the id of this table
        var tableID = tables[i].getAttribute("id");
        if (tableID)
        {
            // Create a new <option> element
            var newOption = document.createElement("option");
            // Add text to the element
            newOption.appendChild(document.createTextNode(tableID));
            // Add this <option> to the drop-down
            ddTable.appendChild(newOption);
        }
    }
}

function refreshFormTool()
{
    // Refresh the forms drop-down.
    var ddForm = document.getElementById("ddForm");
    // First remove the current options
    ddForm.innerHTML = "";

    // Create a blank <option> element
    var blankOption = document.createElement("option");
    // Add text to the element
    blankOption.appendChild(document.createTextNode(""));
    // Add this <option> to the drop-down
    ddForm.appendChild(blankOption);

    // Find all the forms in the JIRA page:
    var forms = idoc().getElementsByTagName('form');
    for (var i = 0; i < forms.length; i++)
    {
        // Get the name of this table
        var formName = forms[i].getAttribute("name");
        if (formName)
        {
            // Create a new <option> element
            var newOption = document.createElement("option");
            // Add text to the element
            newOption.appendChild(document.createTextNode(formName));
            // Add this <option> to the drop-down
            ddForm.appendChild(newOption);
        }
    }
}

/**
 * Called when the user clicks "Do It" on the Table toolbar.
 * Creates appropriate commands for the selected table and action.
 */
function doTableAction()
{
    // Get the selected Table ID
    var ddTable = document.getElementById("ddTable");
    var tableID = ddTable.options[ddTable.selectedIndex].value;

    // Find the action requested by the user
    // First get the Table Action Drop down
    var ddTableAction = document.getElementById("ddTableAction");
    var action = ddTableAction.options[ddTableAction.selectedIndex].value;

    if (action == "AssertAllRowsInOrder")
    {
        doTableActionAssertAllRowsInOrder(tableID);
    }
    else if (action == "AssertAllRowsVerbose")
    {
        doTableActionAssertAllRowsVerbose(tableID);
    }
    else if (action == "AssertAllRowsNoOrder")
    {
        doTableActionAssertAllRowsNoOrder(tableID);
    }
    else if (action == "GetWebTableObject")
    {
        doTableActionGetWebTableObject(tableID);
    }
    else
    {
        /* [alert] */
        alert("ERROR: Unknown action '" + action + "'");
        /* [alert] end */
    }
}

/**
 * Creates the java test code for an "AssertAllRowsInOrder".
 * @param tableID ID of the selected <table>
 */
function doTableActionAssertAllRowsInOrder(tableID)
{
    addToList("// Assert the table '" + tableID + "'");

    // Get the <table> element
    var table = idoc().getElementById(tableID);
    // Now get the Table's rows
    var rows = table.rows;

    // Loop through the rows of the table
    addToList("final ExpectedTable expectedTable = new ExpectedTable();");
    for (var i = 0; i < rows.length; i++)
    {
        var codeForRow = "expectedTable.appendRow(new ExpectedRow(new String[] {";
        // loop through cells of row
        var cells = rows[i].cells;
        var firstCell = true;
        for (var cellNum = 0; cellNum < cells.length; cellNum++)
        {
            // Put a comma before all cells except the first.
            if (firstCell)
            {
                firstCell = false;
            }
            else
            {
                codeForRow = codeForRow + ", ";
            }

            // Strip the text out of this subsection of HTML
            var text = stripText(cells[cellNum]);
            // Now we remove leading and trailing whitespace, then escape special characters so we can write a Java String.
            // eg '  My name is "Michael Cain".     '
            // will become 'My name is \"Michael Cain\".'
            text = escapeTextForJavaString(trim(text));
            codeForRow = codeForRow + '"' + text + '"';
        }
        codeForRow = codeForRow + "}));";
        addToList(codeForRow);
    }

    addToList("tester.assertTableEquals(\"" + tableID + "\", expectedTable);");
}

/**
 * Creates the java test code for an "AssertAllRowsVerbose".
 * @param tableID ID of the selected <table>
 */
function doTableActionAssertAllRowsVerbose(tableID)
{
    addToList("// Assert the cells in table '" + tableID + "'.");
    addToList("WebTable " + tableID + " = tester.getDialog().getWebTableBySummaryOrId(\"" + tableID + "\");");

    // Get the <table> element
    var table = idoc().getElementById(tableID);
    // Now get the Table's rows
    var rows = table.rows;

    // Loop through the rows of the table
    for (var rowNum = 0; rowNum < rows.length; rowNum++)
    {
        var commentForRow = "// Assert row " + rowNum + ": |";
        // loop through cells of row
        var cells = rows[rowNum].cells;
        // declare an array
        var assertion = [];
        for (var cellNum = 0; cellNum < cells.length; cellNum++)
        {
            // Strip the text out of this subsection of HTML
            var text = stripText(cells[cellNum]);
            commentForRow = commentForRow + collapseWhitespace(text) + '|';
            // Now we remove leading and trailing whitespace, then escape special characters so we can write a Java String.
            // eg '  My name is "Michael Cain".     '
            // will become 'My name is \"Michael Cain\".'
            var escapedText = escapeTextForJavaString(trim(text));
            assertion[cellNum] = "assertEquals(\"Cell (" + rowNum + ", " + cellNum + ") in table '" + tableID + "' should be '" +
                                  escapedText + "'.\", \"" + escapedText + "\", " + tableID + ".getCellAsText(" + rowNum + ", " + cellNum + ").trim());"
        }
        // Add the comment for the row first
        addToList(commentForRow);
        // And the assertions after
        for (var cellNum = 0; cellNum < cells.length; cellNum++)
        {
            addToList(assertion[cellNum]);
        }
    }
}

/**
 * Creates the java test code for a "GetWebTableObject".
 * @param tableID ID of the selected <table>
 */
function doTableActionGetWebTableObject(tableID)
{
    addToList("WebTable " + tableID + " = tester.getDialog().getWebTableBySummaryOrId(\"" + tableID + "\");");
}

/**
 * Creates the java test code for a "AssertAllRowsNoOrder".
 * @param tableID ID of the selected <table>
 */
function doTableActionAssertAllRowsNoOrder(tableID)
{
    addToList("// Assert the table '" + tableID + "'");

    // Get the <table> element
    var table = idoc().getElementById(tableID);
    // Now get the Table's rows
    var rows = table.rows;
    addToList("WebTable " + tableID + " = tester.getDialog().getWebTableBySummaryOrId(\"" + tableID + "\");");
    addToList("assertEquals(\"Expected table to have " + rows.length + " rows, but found \" + " +
              tableID + ".getRowCount(), " + rows.length + ", " + tableID + ".getRowCount());");

    // Make rows that look like:
    // assertions.getTableAssertions().assertTableContainsExactlyOneRow(move_confirm_table, new String[] {"Project", "Bovine", "Rattus"});
    for (var i = 0; i < rows.length; i++)
    {
        var codeForRow = "assertions.getTableAssertions().assertTableContainsRowOnce(" + tableID + ", new String[] {";
        // loop through cells of row
        var cells = rows[i].cells;
        var firstCell = true;
        for (var cellNum = 0; cellNum < cells.length; cellNum++)
        {
            // Put a comma before all cells except the first.
            if (firstCell)
            {
                firstCell = false;
            }
            else
            {
                codeForRow = codeForRow + ", ";
            }

            // Strip the text out of this subsection of HTML
            var text = stripText(cells[cellNum]);
            // Now we remove leading and trailing whitespace, then escape special characters so we can write a Java String.
            // eg '  My name is "Michael Cain".     '
            // will become 'My name is \"Michael Cain\".'
            text = collapseWhitespace(text);
            text = escapeTextForJavaString(trim(text));
            codeForRow = codeForRow + '"' + text + '"';
        }
        codeForRow = codeForRow + "});";
        addToList(codeForRow);
    }

}

/**
 * Called when the user clicks "Do It" on the Form toolbar.
 * Creates appropriate commands for the selected Form/element and action.
 */
function doFormAction()
{
    // Get the selected Form
    var form = getSelectedForm();

    // Find the action requested by the user
    // First get the Form Action Drop down
    var ddFormAction = document.getElementById("ddFormAction");
    var action = ddFormAction.options[ddFormAction.selectedIndex].value;

    // Find the component(s) requested by the user
    // First get the Form Action Drop down
    var ddFormComponent = document.getElementById("ddFormComponent");
    var componentName = ddFormComponent.options[ddFormComponent.selectedIndex].value;

    if (componentName == "VISIBLE")
    {
        doFormActionAssertComponents(form, action, false);
    }
    else if (componentName == "ALL")
    {
        doFormActionAssertComponents(form, action, true);
    }
    else
    {
        // Get the compent with the given name:
        var component = getElementNamed(form, componentName);
        doFormActionAssertSingleComponent(component, action);
    }
}

/**
 * Returns the element object with the given name that belongs to the given <form> element.
 * @param form Form element
 * @param elementName Name of the element that we want.
 */
function getElementNamed(form, elementName)
{
    // Find the child elements
    var elements = form.elements;
    for (var i = 0; i < elements.length; i++)
    {
        if (elements[i].name == elementName)
        {
            return elements[i];
        }
    }
    return null;
}

/**
 * Creates assertions for elements in the given form.
 * @param form The form
 * @param action What kind of assertions we want. This is the selected value in the action drop-down.
 * @param includeHidden If true we include all the elements of the form, if false we ignore the "hidden" elements.
 */
function doFormActionAssertComponents(form, action, includeHidden)
{
    addToList("// Assert the values of form '" + form.name + "'.");
    addToList("tester.assertFormPresent(\"" + form.name + "\");");

    // Find the child elements
    var elements = form.elements;
    for (var i = 0; i < elements.length; i++)
    {
        var component = elements[i];
        if (includeHidden || component.type != "hidden")
        {
            doFormActionAssertSingleComponent(component, action);
        }
    }
}

/**
 * Creates an assertion for the given component.
 * @param component Component (element) to create an assertion for.
 * @param action What we want to assert. This is the selected value in the action drop-down.
 */
function doFormActionAssertSingleComponent(component, action)
{
    if (action == "AssertComponentValue")
    {
        // TODO: include the "Display Value" for select components (drop-downs and lists)        
        addToList("// Assert that the value of " + component.type + " component '" + component.name +
                    "' = \"" + component.value + "\".");
        // TODO: Fix this for multiple select
        addToList("tester.assertFormElementEquals(\"" + component.name + "\", \"" + component.value + "\");");
    }
    else if (action == "AssertComponentPresent")
    {
        addToList("// Assert that the " + component.type + " component '" + component.name + "' is present.");
        addToList("tester.assertFormElementPresent(\"" + component.name + "\");");
    }
    else
    {
        /* [alert] */
        alert("Unknown action '" + action + "'");
        /* [alert] end */
    }
}

/**
 * Gathers all the text out of a subtree of our DOM.
 * Note that this is a recursive function.
 * @param node The node of the DOM tree that we are getting text from.
 */
function stripText(node)
{
    // If this is a text node - return its text value.
    if (node.nodeName == "#text")
    {
        // TEXT
        //debug(node.nodeValue);
        return node.nodeValue;
    }

    // else loop through children
    var children = node.childNodes;
    if (children)
    {
        var text = "";
        for (var i = 0; i < children.length; i++)
        {
            text = text + stripText(children[i]);
        }
        return text;
    }
    else
    {
        // No children - just return an empty string.
        return "";
    }
}

/**
 *  Modifies the given string so it is able to be used in a Java String Constant.
 *  eg it must escape a double-quote character with '\"'
 * @param str The string to be escaped.
 */
function escapeTextForJavaString(str) {
    var newStr = str;
    // Escape backslash character - need to do this FIRST, else we would escape the slash from the other transformations!
    newStr = newStr.replace(new RegExp("\\\\", "g"), "\\\\");
    // Escape double-quote character
    newStr = newStr.replace(new RegExp("\"", "g"), "\\\"");
    // Escape tab character
    newStr = newStr.replace(new RegExp("\t", "g"), "\\t");
    // Escape new-line character
    newStr = newStr.replace(new RegExp("\n", "g"), "\\n");
    // Escape return character
    newStr = newStr.replace(new RegExp("\r", "g"), "\\r");
    return newStr;
}

/**
 * Collapse all contiguous whitespace into a single space char.
 * @param str The text to alter.
 */
function collapseWhitespace(str)
{
    var newStr;
    newStr = str.replace(new RegExp("\\s+", "g"), " ");
    return trim(newStr);
}

/**
 * Trims the selected chars from the start and end of the given String.
 * If the chars parameter is omitted, then we will trim out whitespace.
 * @param str String to be trimmed.
 * @param chars Characters to trim off (defaults to whitespace.
 */
function trim(str, chars) {
    return ltrim(rtrim(str, chars), chars);
}

/**
 * Trims the selected chars from the left of the given String.
 * If the chars parameter is omitted, then we will trim out whitespace.
 * @param str String to be trimmed.
 * @param chars Characters to trim off (defaults to whitespace.
 */
function ltrim(str, chars) {
    chars = chars || "\\s";
    return str.replace(new RegExp("^[" + chars + "]+", "g"), "");
}

/**
 * Trims the selected chars from the right of the given String.
 * If the chars parameter is omitted, then we will trim out whitespace.
 * @param str String to be trimmed.
 * @param chars Characters to trim off (defaults to whitespace.
 */
function rtrim(str, chars) {
    chars = chars || "\\s";
    return str.replace(new RegExp("[" + chars + "]+$", "g"), "");
}

/**
 * Called when the Table drop down changes value.
 * It highlights the selected table in the JIRA page.
 */
function ddTableValueChanged()
{
    var tableID = getSelectedTableID();
    var selectedTable = idoc().getElementById(tableID);
    selectObject(selectedTable);
}

/**
 * Called when the Form drop down changes value.
 * It highlights the selected form in the JIRA page, and populates the options in the "Form Component" drop down.
 */
function ddFormValueChanged()
{
    var form = getSelectedForm();

    selectObject(form);

    if (form)
    {
        // Selected an actual form.
        // Refresh the form components drop-down.
        var dropDown = document.getElementById("ddFormComponent");
        // First remove the current options
        dropDown.innerHTML = "";

        // Add hard-coded values
        // Visible
        var newOption = document.createElement("option");
        // Add text to the element
        newOption.appendChild(document.createTextNode("All Visible"));
        newOption.value = "VISIBLE"
        // Add this <option> to the drop-down
        dropDown.appendChild(newOption);
        // All
        newOption = document.createElement("option");
        // Add text to the element
        newOption.appendChild(document.createTextNode("All inc. hidden"));
        newOption.value = "ALL"
        // Add this <option> to the drop-down
        dropDown.appendChild(newOption);

        // Find all the forms in the JIRA page:
        var forms = idoc().getElementsByTagName('form');
        for (var i = 0; i < forms.length; i++)
        {
        }

        // Find the child elements
        var elements = form.elements;
        for (var i = 0; i < elements.length; i++)
        {
            // Get the name of this table
            var elementName = elements[i].getAttribute("name");
            if (elementName)
            {
                // Create a new <option> element
                newOption = document.createElement("option");
                // Add text to the element
                newOption.appendChild(document.createTextNode(elementName));
                // Add this <option> to the drop-down
                dropDown.appendChild(newOption);
            }
        }
    }
    else
    {
        // Selected the blank option.

        // Refresh the form components drop-down.
        var dropDown = document.getElementById("ddFormComponent");
        // First remove the current options
        dropDown.innerHTML = "";        
    }
}

/**
 * Called when the Form Component drop down changes value.
 * It highlights the selected component in the JIRA page, or the entire form if the user chooses one of the "All component" options.
 */
function ddFormComponentValueChanged()
{
    // Find the component(s) requested by the user
    // First get the Form Component Drop down
    var ddFormComponent = document.getElementById("ddFormComponent");
    var componentName = ddFormComponent.options[ddFormComponent.selectedIndex].value;

    if (componentName == "VISIBLE")
    {
        selectObject(getSelectedForm());
    }
    else if (componentName == "ALL")
    {
        selectObject(getSelectedForm());
    }
    else
    {
        // Get the compent with the given name:
        var component = getElementNamed(getSelectedForm(), componentName);
        selectObject(component);
    }    
}

var selectedObject;
var old_style;

/**
 * Changes the current "Selected element" in the JIRA page.
 * It will decorate the selected element to make it highlighted to the user.
 * @param newlySelected The new selected object.
 */
function selectObject(newlySelected)
{
    // Unselect the previously selected object, if any
    if (selectedObject)
    {
        selectedObject.setAttribute("style", old_style);
        selectedObject = null;
    }

    // Now put some style on the newly selected object
    if (newlySelected)
    {
        old_style = newlySelected.getAttribute("style");
        selectedObject = newlySelected;
        selectedObject.setAttribute("style", "border-style: solid; border-width: 5px; border-color: #FF0000;");
    }
}

function getSelectedForm()
{
    // Get the selected form name
    var ddForm = document.getElementById("ddForm");
    var idx = ddForm.selectedIndex;
    if (idx < 1)
    {
        return null;
    }
    // The drop-down includes an initial blank option - so we subtract 1 from the index
    return idoc().forms[ddForm.selectedIndex - 1]
}

function getSelectedFormName()
{
    // Get the selected form name
    var ddForm = document.getElementById("ddForm");
    return ddForm.options[ddForm.selectedIndex].value;
}

function getSelectedTableID()
{
    // Get the selected Table ID
    var ddTable = document.getElementById("ddTable");
    return ddTable.options[ddTable.selectedIndex].value;
}
