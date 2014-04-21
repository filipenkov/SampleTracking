/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package pageobjects;

import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.Conditions;
import com.atlassian.pageobjects.elements.query.TimedCondition;

public class AddPopMailServerPage extends AbstractEditMailServerPage
{
    @ElementBy(cssSelector = "option[value=pop3]")
    private PageElement popOption;

    @Override
    public TimedCondition isAt()
    {
        return Conditions.and(addButton.timed().isPresent(), popOption.timed().isPresent());
    }

    public AddPopMailServerPage test() {
        testButton.click();
        return pageBinder.bind(AddPopMailServerPage.class);
    }

    public IncomingServersPage update()
    {
        addButton.click();
        return pageBinder.bind(IncomingServersPage.class);
    }
}
