/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package pageobjects;

import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import javax.annotation.Nullable;
import java.util.List;

public class EditServicePage extends AbstractMailPage
{
    @ElementBy (id = "update_submit")
    private PageElement updateButton;

    @Override
    public TimedCondition isAt()
    {
        return updateButton.timed().isPresent();
    }

    @Nullable
    public String getHandlerParams() {
        List<WebElement> elements = driver.findElements(By.name("handler.params"));
        return elements.size() < 1 ? null : elements.get(0).getAttribute("value");
    }

    public EditServicePage setHandlerParams(@Nullable String value) {
        WebElement element = driver.findElement(By.name("handler.params"));
        element.clear();
        element.sendKeys(value);
        return this;
    }

    public ViewServicesPage update() {
        updateButton.click();
        return pageBinder.bind(ViewServicesPage.class);
    }
}
