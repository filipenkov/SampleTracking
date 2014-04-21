/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package pageobjects;

import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import javax.annotation.Nonnull;
import javax.ws.rs.core.UriBuilder;
import java.util.List;

public class ViewServicesPage extends AbstractMailPage
{
    @ElementBy (id = "addservice_submit")
    private PageElement addServiceButton;

    @Override
    public String getUrl()
    {
        return "/secure/admin/ViewServices!default.jspa";
    }

    @Override
    public TimedCondition isAt()
    {
        return addServiceButton.timed().isPresent();
    }

    @Nonnull
    public String getServiceId(@Nonnull String name)
    {
        String query = String.format("//span[strong='%s']", name);
        return driver.findElement(By.xpath(query)).getAttribute("id").replace("service-name-", "");
    }

    @Nonnull
    public String getServiceClass(@Nonnull String id)
    {
        return driver.findElement(By.cssSelector("#service-class-" + id)).getText();
    }

    @Nonnull
    public List<String> getServiceProperties(@Nonnull String id)
    {
        return Lists.newArrayList(Collections2.transform(
                driver.findElements(By.cssSelector(String.format("#service-%s td", id))).get(1).findElements(By.cssSelector("ul li")),
                new Function<WebElement, String>()
                {
                    @Override
                    public String apply(WebElement from)
                    {
                        return from.getText();
                    }
                }));
    }

    // this is a hack to be able to edit handler parameters in the old way
    // our tests want to use it and we could either get rid of these tests or add a backdoor.
    // however that was the minimum effort hack at the time I was fixing a JMP issue
    public EditServicePage editServiceHack(@Nonnull String id) {
        final WebElement element = driver.findElement(By.id("del_" + id));
        String href = element.getAttribute("href");
        href = href.replace("ViewServices.jspa", "EditService!default.jspa");
        href = href.replace("delete=", "id=");
        driver.navigate().to(href);
        return pageBinder.bind(EditServicePage.class);
    }

    public boolean hasWarningAboutObsoleteSettings()
    {
        return !driver.findElements(By.cssSelector("#obsolete-settings-warning div.warning")).isEmpty();
    }

    public boolean hasWarningAboutObsoleteSettings(String serviceId)
    {
        return !driver.findElements(By.cssSelector("tr#service-" + serviceId + " > td.cell-type-icon > span.obsolete-settings-hover")).isEmpty();
    }

}
