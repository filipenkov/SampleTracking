/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package pageobjects;

import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import org.openqa.selenium.By;

public class DeleteHandlerPage extends AbstractMailPage
{
    @ElementBy (id="delete_submit")
    private PageElement deleteButton;

    @Override
    public TimedCondition isAt()
    {
        return deleteButton.timed().isPresent();
    }

    public IncomingServersPage cancel() {
        driver.findElement(By.id("cancelButton")).click();
        return pageBinder.bind(IncomingServersPage.class);
    }

    public IncomingServersPage delete() {
        deleteButton.click();
        return pageBinder.bind(IncomingServersPage.class);
    }
}
