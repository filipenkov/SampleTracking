/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package pageobjects;

import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.Option;
import com.atlassian.pageobjects.elements.Options;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.SelectElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.atlassian.pageobjects.elements.timeout.TimeoutType;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import javax.annotation.Nullable;
import java.util.List;

public class EditServerDetailsPage extends AbstractMailPage
{
    @FindBy(name="serviceName")
    protected WebElement serviceName;

    @ElementBy(name = "mailServer")
    protected SelectElement mailServer;

    @FindBy(name="delay")
    protected WebElement delay;

    @ElementBy(name = "handler")
    protected SelectElement handler;

    @ElementBy(name = "folder")
    protected PageElement folder;

    @ElementBy (id = "nextButton", timeoutType = TimeoutType.PAGE_LOAD)
    private PageElement next;

    @Override
    public String getUrl()
    {
        return "/secure/admin/EditServerDetails!default.jspa";
    }

    public EditServerDetailsPage nextWithErrors() {
        next.click();
        return pageBinder.bind(EditServerDetailsPage.class);
    }

    @Override
    public TimedCondition isAt()
    {
        return next.timed().isPresent();
    }

    public String getServiceName() {
        return serviceName.getAttribute("value");
    }

    public EditServerDetailsPage setServiceName(String name) {
        serviceName.clear();
        serviceName.sendKeys(name);
        return this;
    }

    public EditServerDetailsPage setDelay(String delay) {
        this.delay.clear();
        this.delay.sendKeys(delay);
        return this;
    }

    public String getDelay() {
        return delay.getAttribute("value");
    }

    public EditServerDetailsPage setHandlerByKey(String handlerKey) {
        handler.select(Options.value(handlerKey));
        return this;
    }

    public String getHandlerKey() {
        return handler.getSelected().value();
    }

    public boolean isFolderVisible() {
        return folder.isPresent() && folder.isVisible();
    }

    public String getFolder()
    {
        return folder.getValue();
    }

    public EditServerDetailsPage setFolder(String folder)
    {
        this.folder.clear();
        this.folder.type(folder);
        return this;
    }

    public EditHandlerDetailsPage next() {
        next.click();
        return pageBinder.bind(EditHandlerDetailsPage.class);
    }

     public String getMailServerName()
    {
        return mailServer.getSelected().text();
    }

    public EditServerDetailsPage setMailServerByName(String mailServerName)
    {
        mailServer.select(Options.text(mailServerName));
        return this;
    }

    public List<String> getMessageHandlerNames() {
        return ImmutableList.copyOf(Collections2.transform(handler.getAllOptions(), new Function<Option, String>()
        {
            @Override
            public String apply(@Nullable Option from)
            {
                return from != null ? from.text() : "";
            }
        }));
    }
}
