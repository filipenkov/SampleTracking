/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package pageobjects;

import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.Conditions;
import com.atlassian.pageobjects.elements.query.TimedCondition;

public class UpdatePopMailServerPage extends AbstractEditMailServerPage
{
    @ElementBy (cssSelector = "option[value=pop3]")
    private PageElement popOption;

    @Override
    public TimedCondition isAt()
    {
        return Conditions.and(updateButton.timed().isPresent(), popOption.timed().isPresent());
    }

    public UpdatePopMailServerPage test() {
        testButton.click();
        return pageBinder.bind(UpdatePopMailServerPage.class);
    }

    public IncomingServersPage update()
    {
        updateButton.click();
        return pageBinder.bind(IncomingServersPage.class);
    }
}
