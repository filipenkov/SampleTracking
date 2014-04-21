/*
	Toggles the admin menu on & off (using cookies)
*/

var areAllMenusOpen = true;

function openMenu(sSectionID) 
{
	var eHeader = document.getElementById(sSectionID);
	var eSection = document.getElementById(sSectionID + "_body");

    eHeader.className = "headerOpen";
    eSection.style.display = "";
//    saveToConglomerateCookie("jira.conglomerate.cookie", sSectionID, '1', 365);
    eraseFromConglomerateCookie("jira.conglomerate.cookie", sSectionID);
}

function closeMenu(sSectionID)
{
	var eHeader = document.getElementById(sSectionID);
	var eSection = document.getElementById(sSectionID + "_body");

    eHeader.className = "headerClosed";
    eSection.style.display = "none";
    saveToConglomerateCookie("jira.conglomerate.cookie", sSectionID, '0');

    areAllMenusOpen = false;
}

function isMenuOpened(sSectionID)
{
    var eHeader = document.getElementById(sSectionID);
    var eSection = document.getElementById(sSectionID + "_body");
    return (eHeader.className == "headerOpen") && (eSection.style.display == "");
}

function toggleMenu(sSectionID)
{
    if (isMenuOpened(sSectionID))
    {
        closeMenu(sSectionID);
        restoreShowHideAllMenu();
	}
	else
	{
        openMenu(sSectionID);
	}
}

/*
	Restores a state of a menu
*/

function restoreMenu(sSectionID)
{
	if (readFromConglomerateCookie("jira.conglomerate.cookie", sSectionID, '1') == "1")
	{
        openMenu(sSectionID);
	}
    else
    {
        closeMenu(sSectionID);
    }
}

function restoreShowHideAllMenu()
{
    var eHideAll = document.getElementById("hideAllMenu");
    var eShowAll = document.getElementById("showAllMenu");
    if (areAllMenusOpen)
    {
        eHideAll.style.display = "";
        eShowAll.style.display = "none";
    }
    else
    {
        eHideAll.style.display = "none";
        eShowAll.style.display = "";
    }
}
