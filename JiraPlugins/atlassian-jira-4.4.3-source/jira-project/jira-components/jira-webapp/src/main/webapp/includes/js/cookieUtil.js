  // Original JavaScript code by Duncan Crombie: dcrombie at chirp.com.au
  // Please acknowledge use of this code by including this header.

function getCookie(name) // use: getCookie("name");
{
    return readFromConglomerateCookie("jira.dashboard.conglomerate.cookie", name, "1");
}

var today = new Date();
var expiry = new Date(today.getTime() + 28 * 24 * 60 * 60 * 1000); // plus 28 days

function setCookie(name, value) // use: setCookie("name", value);
{
    if(value == "show")
    {
        eraseFromConglomerateCookie("jira.dashboard.conglomerate.cookie", name);
    }
    else if(value == "hide")
    {
        saveToConglomerateCookie("jira.dashboard.conglomerate.cookie", name, "0");
    }
}

var CookieUtil = {
    //ignoreCookie is a (optional) boolean param to specify whether not to persist the visibility to a cookie
    toggleVisibility: function (id, showString, hideString, ignoreCookie)
    {
        var element = document.getElementById(id);
        with (element.style)
        {
            if ( display == "none" )
            {
                display = "";
                if (!ignoreCookie)
                {
                    setCookie(id, "show");
                }
            }
            else
            {
                display = "none";
                if (!ignoreCookie)
                {
                    setCookie(id, "hide");
                }
            }
        }
        var text = document.getElementById(id + "-switch").firstChild;
        if (element.style.display == "")
        {
            text.nodeValue = hideString;
        }
        else
        {
            text.nodeValue = showString;
        }
    }
};

function toggle(id, showString, hideString)
{
    CookieUtil.toggleVisibility(id, showString, hideString);
}

function cookieHide(id, showString)
{
    if (getCookie(id) == "0")
    {
         var element = document.getElementById(id);
         element.style.display = "none";
         var text = document.getElementById(id + "-switch").firstChild;
         text.nodeValue = showString;
    }
}
