/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package pageobjects;

import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import org.apache.commons.lang.StringUtils;
import org.openqa.selenium.By;

import javax.annotation.Nonnull;

public class IncomingServersPage extends AbstractMailPage
{
    @ElementBy (id = "add-pop-mail-server")
    private PageElement addPopMailServer;

    @ElementBy (id = "add-incoming-mail-handler")
    private PageElement addMailHandler;

    @ElementBy (className = "hints-section")
    private PageElement hints;

    @Override
    public String getUrl()
    {
        return "/secure/admin/IncomingMailServers.jspa";
    }

    @Override
    public TimedCondition isAt()
    {
        return addPopMailServer.timed().isPresent();
    }

    @Nonnull
    public String getServerId(@Nonnull String name)
    {
        String query = String.format("//table[@id='pop-mail-servers-table']//tr[.//span[@class='mail-server-name']/strong='%s']//ul[@class='operations-list']//a", name);
        return driver.findElement(By.xpath(query)).getAttribute("id").replace("edit-pop-", "");
    }

    @Nonnull
    public String getHandlerId(@Nonnull String name)
    {
        String query = String.format("//td[strong='%s']", name);
        return driver.findElement(By.xpath(query)).getAttribute("id").replace("handler-name-", "");
    }

    public boolean isHandlerPresent(@Nonnull String name)
    {
        String query = String.format("//td[strong='%s']", name);
        return !driver.findElements(By.xpath(query)).isEmpty();
    }

    @Nonnull
    public String getHandlerServer(@Nonnull String id)
    {
        return driver.findElement(By.cssSelector("#handler-server-" + id)).getText();
    }

    @Nonnull
    public String getHandlerParams(@Nonnull String id)
    {
        return StringUtils.replace(driver.findElement(By.cssSelector("#handler-params-" + id)).getText(), "\n", " ");
    }

    public EditServerDetailsPage editHandler(@Nonnull String id) {
        driver.findElement(By.id("edit-handler-" + id)).click();
        return pageBinder.bind(EditServerDetailsPage.class);
    }

    public DeleteHandlerPage deleteHandler(@Nonnull String id) {
        driver.findElement(By.id("delete-handler-" + id)).click();
        return pageBinder.bind(DeleteHandlerPage.class);
    }

    public EditServerDetailsPage addHandler() {
        addMailHandler.click();
        return pageBinder.bind(EditServerDetailsPage.class);
    }

    public AddPopMailServerPage addServer() {
        addPopMailServer.click();
        return pageBinder.bind(AddPopMailServerPage.class);
    }

    public AddPopMailServerPage addServerFromNoServersHint() {
        hints.find(By.linkText("create")).click();
        return pageBinder.bind(AddPopMailServerPage.class);
    }

    public boolean hasWarningAboutObsoleteSettings()
    {
        return !driver.findElements(By.cssSelector(".obsolete-settings-warning")).isEmpty();
    }

    public boolean hasWarningAboutObsoleteSettings(String serviceId)
    {
        return !driver.findElements(By.cssSelector("td#handler-name-" + serviceId + " span.obsolete-settings-hover")).isEmpty();
    }

    public boolean hasNoServersHint() {
        return hints.isPresent() && hints.isVisible() && hints.find(By.linkText("create")).isPresent();
    }

    public boolean isAddHandlerEnabled() {
        return addMailHandler.getTagName().equals("a");
    }

    public UpdatePopMailServerPage editServer(String serverId)
    {
        driver.findElement(By.id("edit-pop-" + serverId)).click();
        return pageBinder.bind(UpdatePopMailServerPage.class);
    }
}
