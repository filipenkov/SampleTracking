/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package pageobjects;

import com.atlassian.pageobjects.elements.query.Conditions;
import com.atlassian.pageobjects.elements.query.TimedCondition;

public class AddSmtpMailServerPage extends AbstractEditMailServerPage
{

    @Override
    public TimedCondition isAt()
    {
        return Conditions.and(addButton.timed().isPresent(), jndiLocation.timed().isPresent());
    }

    public AddSmtpMailServerPage test() {
        testButton.click();
        return pageBinder.bind(AddSmtpMailServerPage.class);
    }

    public OutgoingServersPage update()
    {
        addButton.click();
        return pageBinder.bind(OutgoingServersPage.class);
    }

}
