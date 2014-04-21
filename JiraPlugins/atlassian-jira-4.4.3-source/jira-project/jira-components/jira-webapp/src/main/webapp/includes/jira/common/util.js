var JIRA = window.JIRA || {};

/**
 * Get/set the value at a compound namespace, gracefully adding values where missing.
 * @param {string} namespace
 * @param {!Object} context
 * @param value
 */
AJS.namespace = function(namespace, context, value) {
    var names = namespace.split(".");
    context = context || window;
    for (var i = 0, n = names.length - 1; i < n; i++) {
        var x = context[names[i]];
        context = (x != null) ? x : context[names[i]] = {};
    }
    return context[names[i]] = value || {};
};

/**
 * Determines if you can access the contents of an iframe. This is only possible if the iframe's src has the same
 * domain, port and protocal as the parent window.
 *
 * @param {HTMLElement, jQuery} iframe
 * @return {Boolean}
 */
AJS.canAccessIframe = function (iframe) {
    var $iframe = AJS.$(iframe);

    return !/^(http|https):\/\//.test($iframe.attr("src")) ||
            (AJS.params.baseURL && (AJS.$.trim($iframe.attr("src")).indexOf(AJS.params.baseURL) === 0));
};


// Utility methods for enabling/disabling of page scrolling via keyboard

(function() {

    function preventScrolling(e) {
        var keyCode = e.keyCode,
            keys = AJS.$.ui.keyCode;

        if (!jQuery(e.target).is("textarea, :text, select, :radio") && (keyCode === keys.DOWN || keyCode === keys.UP ||
                keyCode === keys.LEFT || keyCode === keys.RIGHT)) {
            e.preventDefault();
        }
    }

    AJS.disableKeyboardScrolling = function () {
        AJS.$(document).bind("keypress keydown", preventScrolling);
    };

    AJS.enableKeyboardScrolling = function () {
        AJS.$(document).unbind("keypress keydown", preventScrolling);
    };

})();




/**
 * @deprecated jQuery.namespace, AJS.$.namespace
 * Masquerade as jQuery plugin to preserve legacy compatibility.
 */
AJS.$.namespace = function(namespace) {
    return AJS.namespace(namespace);
};

// This is here so that we find bugs like JRA-19245 ASAP
jQuery.noConflict();
jQuery.ajaxSettings.traditional = true;

// constants
contextPath = typeof contextPath === "undefined" ? "" : contextPath;
AJS.LEFT = "left";
AJS.RIGHT = "right";
AJS.ACTIVE_CLASS = "active";
AJS.BOX_SHADOW_CLASS = "box-shadow";
AJS.LOADING_CLASS = "loading";
AJS.INTELLIGENT_GUESS = "Intelligent Guess";

(function() {
    var SPECIAL_CHARS = /[.*+?|^$()[\]{\\]/g;
    // Note: This escapes str for regex literal sequences -- not within character classes
    RegExp.escape = function(str) {
        return str.replace(SPECIAL_CHARS, "\\$&");
    };
})();

/**
 * A bunch of useful utilitiy javascript methods available to all jira pages
 */
(function($)
{
    /**
     * Reads data from a structured HTML, such as definition list and build a data object.
     * Even children represent names, odd children represent values.
     *
     * @param s jQuery selector
     */
    $.readData = function(s)
    {
        var r = {}, n = "";

        $(s).children().each(function(i)
        {   
            if (i % 2)
            {
                r[n] = jQuery.trim($(this).text());
            }
            else
            {
                n = jQuery.trim($(this).text());
            }
        }).remove();
        $(s).remove();
        return r;
    };
})(jQuery);

String.prototype.escapejQuerySelector = function () {
    return this.replace(/([:.])/g, "\\$1");
};

/**
 * Utility method for triggering a given event on a given target.
 *
 * @param {string | Object} event -- what event to trigger
 * @param {Object=} target -- what 
 * @returns {boolean} -- whether the default action was prevented
 */
AJS.trigger = function(event, target) {
    event = new jQuery.Event(event);
    jQuery(target || window.top.document).trigger(event);
    return !event.isDefaultPrevented();
};

// custom event when and iframe is appended. Useful for the dashboard where iframes are appended after the jQuery.ready
// event if fired.
jQuery.aop.after({target: jQuery, method: "append"}, function (elem) {
    var iframes;
    if (elem.attr("tagName") === "iframe" && AJS.canAccessIframe(elem)) {
        if (!elem.data("iframeAppendedFired")) {
            elem.data("iframeAppendedFired", true);
            jQuery(document).trigger("iframeAppended", elem);
        }
    }
    iframes = jQuery("iframe", elem);
    if (iframes.length > 0) {
        jQuery.each(iframes, function (i) {
            var iframe = iframes.eq(i);
            if (!iframe.data("iframeAppendedFired") && AJS.canAccessIframe(iframe)) {
                iframe.data("iframeAppendedFired", true);
                iframe.trigger("iframeAppended", iframe);
            }
        });
    }
    return elem;
});

/**
 * Determine whether Selenium is running.
 *
 * Note: This does not detect WebDriver. Please use very sparingly!
 *
 * @return {boolean}
 */
AJS.isSelenium = function () {
    return window.name.toLowerCase().indexOf("selenium") >= 0;
};

/**
 * Selenium marks the page with magic markers like :
 *
 * var marker = 'selenium' + new Date().getTime();
 * window.location[marker] = true;
 *
 * So a window.reload() causes this all to go away and keeps Selenium happy
 * about knowing when a page has been loaded.
 *
 * However this is bad for humans in that if the original page is a POST, then
 * on IE and FF, they get a Confirm RE-POST dialog.  So we do this different on
 * Selenium than we do for humans.  Both are meant to be the same result.
 *
 * @param url if this is passed in then it will be used otherwise window.location.href will be used
 */
AJS.reloadViaWindowLocation = function(url)
{
    var windowReload = function()
    {
        window.location.reload();
    };

    url = url || window.location.href;
    if (AJS.isSelenium())
    {
        windowReload();
    }
    else
    {
        /*
         * If the url has a # on it then the browser wont ever leave the page.  So we want to insert
         * an updated parameter so that window.location will cause a page reload.  We put a unique value
         * into the url so that it is truly unique!
         */
        var makeHashUrlsUnique = function(url)
        {
            var MAGIC_PARAM = 'jwupdated';
            var hashIndex = url.indexOf('#');
            if (hashIndex == -1)
            {
                return url;
            }
            var secondsSinceMidnight = function()
            {
                var now = new Date();
                var midnight = new Date(now.getFullYear(), now.getMonth(), now.getDate(), 0, 0, 0, 0);
                var secs = (now.getTime() - midnight.getTime()) / 1000;
                return Math.max(Math.floor(secs), 1);
            };
            var firstQuestionMark = url.indexOf('?');
            var magicParamValue = MAGIC_PARAM + '=' + secondsSinceMidnight();
            if (firstQuestionMark == -1)
            {
                // if we have no parameters, we can insert them before the #
                url = url.replace('#', '?' + magicParamValue + '#');
            }
            else
            {
                // if we already have a magic marker then just replace that
                if (url.indexOf(MAGIC_PARAM+'=') != -1)
                {
                    url = url.replace(/(jwupdated=[0-9]+)/, magicParamValue);
                }
                else
                {
                    url = url.replace('?', '?' + magicParamValue + '&');
                }
            }
            return url;
        };
        url = makeHashUrlsUnique(url);
        //Safari4 has issues with window.location.replace - JRADEV-2314, JRADEV-2315
        if(jQuery.browser.webkit && parseInt(jQuery.browser.version) < 533) {
            window.location = url;
        } else {
            window.location.replace(url);
        }
    }
};

/**
 * This function can extract the BODY tag text from a HttpRequest if its there are otherwise
 * return all the response text.
 * 
 * @param text the AJAX request text returned
 */
AJS.extractBodyFromResponse = function(text)
{
    var fragment = text.match(/<body[^>]*>([\S\s]*)<\/body[^>]*>/);
    if (fragment && fragment.length > 0)
    {
        return fragment[1];
    }
    return text;
};

AJS.escapeHTML = AJS.escapeHtml; // preserve case of JIRA's old name for this function

/**
 * Tries to run a function and return its value and if it throws an exception, returns the default value instead
 *
 * @param f the function to try
 * @param defaultVal the default value to return in case of an error
 */
function tryIt(f, defaultVal) {
    try {
        return f();
    } catch (ex) {
        return defaultVal;
    }
}

/**
 * @function begetObject
 * @return {Object} cloned object
 */
function begetObject(obj) {
    var f = function() {};
    f.prototype = obj;
    return new f;
}

/*
 * Submits an element's form if the enter key is pressed
 */
function submitOnEnter(e)
{
    if (e.keyCode == 13 && e.target.form && !e.ctrlKey && ! e.shiftKey)
    {
        jQuery(e.target.form).submit();
        return false;
    }
    return true;
}

/*
 Submits an element's form if the enter key and the control key is pressed
 */
function submitOnCtrlEnter(e)
{
    if (e.ctrlKey && e.target.form && (e.keyCode == 13 || e.keyCode == 10))
    {
        jQuery(e.target.form).submit();
        return false;
    }
    return true;
}

/*
 Returns a space delimited value of a select list. There's strangely no in-built way of doing this for multi-selects
 */
function getMultiSelectValues(selectObject)
{
    var selectedValues = '';
    for (var i = 0; i < selectObject.length; i++)
    {
        if (selectObject.options[i].selected)
        {
            if (selectObject.options[i].value && selectObject.options[i].value.length > 0)
                selectedValues = selectedValues + ' ' + selectObject.options[i].value;
        }
    }

    return selectedValues;
}

function getMultiSelectValuesAsArray(selectObject)
{
    var selectedValues = new Array();
    for (var i = 0; i < selectObject.length; i++)
    {
        if (selectObject.options[i].selected)
        {
            if (selectObject.options[i].value && selectObject.options[i].value.length > 0)
                selectedValues[selectedValues.length] = selectObject.options[i].value;
        }
    }
    return selectedValues;
}

/*
 Returns true if the value is the array
 */
function arrayContains(array, value)
{
    for (var i = 0; i < array.length; i++)
    {
        if (array[i] == value)
        {
            return true;
        }
    }

    return false;
}

/*
 Adds a class name to the given element
 */
function addClassName(elementId, classNameToAdd)
{
    var elem = document.getElementById(elementId);
    if (elem)
    {
        elem.className = elem.className + ' ' + classNameToAdd;
    }
}

/*
 Removes all class names to from the given element
 */
function removeClassName(elementId, classNameToRemove)
{
    var elem = document.getElementById(elementId);
    if (elem)
    {
        elem.className = (' ' + elem.className + ' ').replace(' ' + classNameToRemove + ' ', ' ');
    }
}

/*
 Returns the field as an encoded string (assuming that the id == the field name
 */
function getEscapedFieldValue(id)
{

    var e = document.getElementById(id);

    if (e.value)
    {
        return id + '=' + encodeURIComponent(e.value);
    }
    else
    {
        return '';
    }
}

/*
 Returns a concatenated version of getEscapedFieldValue
 */
function getEscapedFieldValues(ids)
{
    var s = '';
    for (var i = 0; i < ids.length; i++)
    {
        s = s + '&' + getEscapedFieldValue(ids[i]);
    }
    return s;
}

/* Manages Gui Preferences and stores them in the user's cookie. */
var GuiPrefs = {
    toggleVisibility: function(elementId)
    {
        var elem = document.getElementById(elementId);
        if (elem)
        {
            if (readFromConglomerateCookie("jira.conglomerate.cookie", elementId, '1') == '1')
            {
                elem.style.display = "none";
                removeClassName(elementId + 'header', 'headerOpened');
                addClassName(elementId + 'header', 'headerClosed');
                saveToConglomerateCookie("jira.conglomerate.cookie", elementId, '0');
            }
            else
            {
                elem.style.display = "";
                removeClassName(elementId + 'header', 'headerClosed');
                addClassName(elementId + 'header', 'headerOpened');
                eraseFromConglomerateCookie("jira.conglomerate.cookie", elementId);
            }
        }
    }
};

/*
 Toggles hide / unhide an element. Also attemots to change the "elementId + header" element to have the headerOpened / headerClosed class.
 Also saves the state in a cookie
 DEPRECATED: use GuiPrefs.toggleVisibility
 */
function toggle(elementId)
{
    GuiPrefs.toggleVisibility(elementId);
}

function toggleDivsWithCookie(elementShowId, elementHideId)
{
    var elementShow = document.getElementById(elementShowId);
    var elementHide = document.getElementById(elementHideId);
    if (elementShow.style.display == 'none')
    {
        elementHide.style.display = 'none';
        elementShow.style.display = 'block';
        saveToConglomerateCookie("jira.viewissue.cong.cookie", elementShowId, '1');
        saveToConglomerateCookie("jira.viewissue.cong.cookie", elementHideId, '0');
    }
    else
    {
        elementShow.style.display = 'none';
        elementHide.style.display = 'block';
        saveToConglomerateCookie("jira.viewissue.cong.cookie", elementHideId, '1');
        saveToConglomerateCookie("jira.viewissue.cong.cookie", elementShowId, '0');
    }
}

/*
 Similar to toggle. Run this on page load.
 */
function restoreDivFromCookie(elementId, cookieName, defaultValue)
{
    if (defaultValue == null)
        defaultValue = '1';

    var elem = document.getElementById(elementId);
    if (elem)
    {
        if (readFromConglomerateCookie(cookieName, elementId, defaultValue) != '1')
        {
            elem.style.display = "none";
            removeClassName(elementId + 'header', 'headerOpened');
            addClassName(elementId + 'header', 'headerClosed');
        }
        else
        {
            elem.style.display = "";
            removeClassName(elementId + 'header', 'headerClosed');
            addClassName(elementId + 'header', 'headerOpened');
        }
    }
}

/*
 Similar to toggle. Run this on page load.
 */
function restore(elementId)
{
    restoreDivFromCookie(elementId, "jira.conglomerate.cookie", '1');
}

// Cookie handling functions

function saveToConglomerateCookie(cookieName, name, value)
{
    var cookieValue = getCookieValue(cookieName);
    cookieValue = addOrAppendToValue(name, value, cookieValue);

    saveCookie(cookieName, cookieValue, 365);
}

function readFromConglomerateCookie(cookieName, name, defaultValue)
{
    var cookieValue = getCookieValue(cookieName);
    var value = getValueFromCongolmerate(name, cookieValue);
    if (value != null)
    {
        return value;
    }

    return defaultValue;
}

function eraseFromConglomerateCookie(cookieName, name)
{
    saveToConglomerateCookie(cookieName, name, "");
}

function getValueFromCongolmerate(name, cookieValue)
{
    // a null cookieValue is just the first time through so create it
    if (cookieValue == null)
    {
        cookieValue = "";
    }
    var eq = name + "=";
    var cookieParts = cookieValue.split('|');
    for (var i = 0; i < cookieParts.length; i++)
    {
        var cp = cookieParts[i];
        while (cp.charAt(0) == ' ')
        {
            cp = cp.substring(1, cp.length);
        }
        // rebuild the value string excluding the named portion passed in
        if (cp.indexOf(name) == 0)
        {
            return cp.substring(eq.length, cp.length);
        }
    }
    return null;
}

//either append or replace the value in the cookie string
function addOrAppendToValue(name, value, cookieValue)
{
    var newCookieValue = "";
    // a null cookieValue is just the first time through so create it
    if (cookieValue == null)
    {
        cookieValue = "";
    }

    var cookieParts = cookieValue.split('|');
    for (var i = 0; i < cookieParts.length; i++)
    {
        var cp = cookieParts[i];

        // ignore any empty tokens
        if (cp != "")
        {
            while (cp.charAt(0) == ' ')
            {
                cp = cp.substring(1, cp.length);
            }
            // rebuild the value string excluding the named portion passed in
            if (cp.indexOf(name) != 0)
            {
                newCookieValue += cp + "|";
            }
        }
    }

    // always append the value passed in if it is not null or empty
    if (value != null && value != '')
    {
        var pair = name + "=" + value;
        if ((newCookieValue.length + pair.length) < 4020)
        {
            newCookieValue += pair;
        }
    }
    return newCookieValue;
}

function getCookieValue(name)
{
    var eq = name + "=";
    var ca = document.cookie.split(';');
    for (var i = 0; i < ca.length; i++)
    {
        var c = ca[i];
        while (c.charAt(0) == ' ')
        {
            c = c.substring(1, c.length);
        }
        if (c.indexOf(eq) == 0)
        {
            return unescape(c.substring(eq.length, c.length));
        }
    }

    return null;
}

function saveCookie(name, value, days)
{

    if (typeof contextPath === "undefined") {
        return;
    }
    var path = contextPath;
    if (! path) {
        path = "/";
    }

    var ex;
    if (days)
    {
        var d = new Date();
        d.setTime(d.getTime() + (days * 24 * 60 * 60 * 1000));
        ex = "; expires=" + d.toGMTString();
    }
    else
    {
        ex = "";
    }
    document.cookie = name + "=" + escape(value) + ex + ";path=" + path;
}

/*
 Reads a cookie. If none exists, then it returns and
 */
function readCookie(name, defaultValue)
{
    var cookieVal = getCookieValue(name);
    if (cookieVal != null)
    {
        return cookieVal;
    }

    // No cookie found, then save a new one as on!
    if (defaultValue)
    {
        saveCookie(name, defaultValue, 365);
        return defaultValue;
    }
    else
    {
        return null;
    }
}

function eraseCookie(name)
{
    saveCookie(name, "", -1);
}

function recolourSimpleTableRows(tableId)
{
    recolourTableRows(tableId, "rowNormal", "rowAlternate", tableId + "_empty");
}

function recolourTableRows(tableId, rowNormal, rowAlternate, emptyTableId)
{
    var tbl = document.getElementById(tableId);
    var emptyTable = document.getElementById(emptyTableId);

    var alternate = false;
    var rowsFound = 0;
    var rows = tbl.rows;
    var firstVisibleRow = null;
    var lastVisibleRow = null;

    if (AJS.$(tbl).hasClass('aui')) {
        rowNormal = '';
        rowAlternate = 'zebra';
    }

    for (var i = 1; i < rows.length; i++)
    {
        var row = rows[i];
        if (row.style.display != "none")
        {
            if (!alternate)
            {
                row.className = rowNormal;
            }
            else
            {
                row.className = rowAlternate;
            }
            rowsFound++;
            alternate = !alternate;
        }

        if (row.style.display != "none")
        {
            if (firstVisibleRow == null)
            {
                firstVisibleRow = row;
            }
            lastVisibleRow = row;
        }
    }
    if (firstVisibleRow != null)
    {
        firstVisibleRow.className = firstVisibleRow.className + " first-row";
    }
    if (lastVisibleRow != null)
    {
        lastVisibleRow.className = lastVisibleRow.className + " last-row";
    }

    if (emptyTable)
    {
        if (rowsFound == 0)
        {

            tbl.style.display = "none";
            emptyTable.style.display = "";
        }
        else
        {
            tbl.style.display = "";
            emptyTable.style.display = "none";
        }
    }
}

function htmlEscape(str)
{
    var divE = document.createElement('div');
    divE.appendChild(document.createTextNode(str));
    return divE.innerHTML;
}

/**
 * Returns the meta tag that contains the XSRF atlassian token
 * or undefined if not on page
 */
function atl_token() {
    return jQuery("#atlassian-token").attr('content');
}


// Inline script moved here from date_picker.jsp to avoid blocking rendering in IE7
function openDateRangePicker(formName, previousFieldName, nextFieldName, fieldId)
{

    var previousFieldValue = document.forms[formName].elements[previousFieldName].value;
    var nextFieldValue = document.forms[formName].elements[nextFieldName].value;

    var url = contextPath + '/secure/popups/DateRangePicker.jspa?';
    url += 'formName=' + formName + '&';
    url += 'previousFieldName=' + escape(previousFieldName) + '&';
    url += 'nextFieldName=' + escape(nextFieldName) + '&';
    url += 'previousFieldValue=' + escape(previousFieldValue) + '&';
    url += 'nextFieldValue=' + escape(nextFieldValue) + '&';
    url += 'fieldId=' + escape(fieldId);

    var vWinUsers = window.open(url, 'DateRangePopup', 'status=no,resizable=yes,top=100,left=200,width=580,height=400,scrollbars=yes');
    vWinUsers.opener = self;
    vWinUsers.focus();
}
// Inline script moved here from date_picker.jsp to avoid blocking rendering in IE7
function show_calendar2(formName, fieldName)
{
    var form = document.forms[formName];
    var element = form.elements[fieldName];
    var vWinCal = window.open(contextPath + '/secure/popups/Calendar.jspa?form=' + formName + '&field=' + fieldName + '&value=' + escape(element.value) + '&decorator=none', 'Calendar','width=230,height=170,status=no,resizable=yes,top=220,left=200');
    vWinCal.opener = self;
    vWinCal.focus();
}
