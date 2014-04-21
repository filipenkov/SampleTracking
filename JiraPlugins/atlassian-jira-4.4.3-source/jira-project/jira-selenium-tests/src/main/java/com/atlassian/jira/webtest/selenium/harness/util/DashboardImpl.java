package com.atlassian.jira.webtest.selenium.harness.util;

import com.atlassian.selenium.SeleniumClient;
import com.atlassian.jira.webtests.util.JIRAEnvironmentData;

public class DashboardImpl extends AbstractSeleniumUtil implements Dashboard
{
    private final String dashboardId;

    public DashboardImpl(String dashboardId, SeleniumClient selenium, JIRAEnvironmentData environmentData)
    {
        super(selenium, environmentData);
        this.dashboardId = dashboardId;
    }

    public void view()
    {
        if (dashboardId == null)
        {
            selenium.open(environmentData.getBaseUrl() + "/secure/Dashboard.jspa");
        }
        else
        {
            selenium.open(environmentData.getBaseUrl() + "/secure/Dashboard.jspa?selectPageId=" + dashboardId);
        }
        selenium.waitForPageToLoad(PAGE_LOAD_WAIT);      
    }

    public int getGadgetCount()
    {
        final String eval = selenium.getEval("this.browserbot.getCurrentWindow().jQuery('.gadget-container').size()");
        return Integer.valueOf(eval);
    }

    public void dragGadgetToTab(String gadgetId, String targetTabName, int targetShimIndex)
    {
        String locator = "gadget-" + gadgetId + "-title";
        String targetLocator = "//ul[contains(@class, 'tabs')]//a//span[@title='" + targetTabName + "']";
        //copied from selenium-api.js: doDragAndDropToObject(). First we get the delta in X,Y coordinates by how much
        //the gadget needs to move.
        final String movementsString = selenium.getEval("var locatorOfObjectToBeDragged = \"" + locator + "\";\n"
                + "var locatorOfDragDestinationObject = \"" + targetLocator + "\";\n"
                + "var startX = this.getElementPositionLeft(locatorOfObjectToBeDragged);\n"
                + "   var startY = this.getElementPositionTop(locatorOfObjectToBeDragged);\n"
                + "   \n"
                + "   var destinationLeftX = this.getElementPositionLeft(locatorOfDragDestinationObject);\n"
                + "   var destinationTopY = this.getElementPositionTop(locatorOfDragDestinationObject);\n"
                + "   var destinationWidth = this.getElementWidth(locatorOfDragDestinationObject);\n"
                + "   var destinationHeight = this.getElementHeight(locatorOfDragDestinationObject);\n"
                + "\n"
                + "   var endX = Math.round(destinationLeftX + (destinationWidth / 2));\n"
                + "   var endY = Math.round(destinationTopY + (destinationHeight / 2));\n"
                + "   \n"
                + "   var deltaX = endX - startX;\n"
                + "   var deltaY = endY - startY;\n"
                + "   \n"
                + "   \"\" + deltaX + \",\" + deltaY;");

        //copied from selenium-api.js: doDragAndDrop(): Fire move events for the coordinates, then fire a moseup event
        //on the hotspot shim for the target tab.
        selenium.getEval("var locator = '" + locator + "';\n"
                + "var movementsString = '" + movementsString + "';\n"
                + " var element = this.browserbot.findElement(locator);\n"
                + " var targetElement = this.browserbot.findElement(\"//div[contains(@class, 'hotspot-shim')][" + targetShimIndex + "]\");\n"
                + "    var clientStartXY = getClientXY(element)\n"
                + "    var clientStartX = clientStartXY[0];\n"
                + "    var clientStartY = clientStartXY[1];\n"
                + "    \n"
                + "    var movements = movementsString.split(/,/);\n"
                + "    var movementX = Number(movements[0]);\n"
                + "    var movementY = Number(movements[1]);\n"
                + "    \n"
                + "    var clientFinishX = ((clientStartX + movementX) < 0) ? 0 : (clientStartX + movementX);\n"
                + "    var clientFinishY = ((clientStartY + movementY) < 0) ? 0 : (clientStartY + movementY);\n"
                + "    \n"
                + "    var mouseSpeed = this.mouseSpeed;\n"
                + "    var move = function(current, dest) {\n"
                + "        if (current == dest) return current;\n"
                + "        if (Math.abs(current - dest) < mouseSpeed) return dest;\n"
                + "        return (current < dest) ? current + mouseSpeed : current - mouseSpeed;\n"
                + "    }\n"
                + "    \n"
                + "    this.browserbot.triggerMouseEvent(element, 'mousedown', true, clientStartX, clientStartY);\n"
                + "    this.browserbot.triggerMouseEvent(element, 'mousemove',   true, clientStartX, clientStartY);\n"
                + "    var clientX = clientStartX;\n"
                + "    var clientY = clientStartY;\n"
                + "    \n"
                + "    while ((clientX != clientFinishX) || (clientY != clientFinishY)) {\n"
                + "        clientX = move(clientX, clientFinishX);\n"
                + "        clientY = move(clientY, clientFinishY);\n"
                + "        this.browserbot.triggerMouseEvent(element, 'mousemove', true, clientX, clientY);\n"
                + "    }\n"
                + "    \n"
                + "    this.browserbot.triggerMouseEvent(element, 'mousemove',   true, clientFinishX, clientFinishY);\n"
                + "    this.browserbot.triggerMouseEvent(targetElement, 'mouseup',   true, clientFinishX, clientFinishY);");
        try
        {
            //need to wait for a little while so the browser has enough time to fire the request to the server for the
            //move.
            Thread.sleep(500);
        }
        catch (InterruptedException e)
        {
            throw new RuntimeException(e);
        }
    }
}
