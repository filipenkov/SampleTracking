/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package pageobjects;

import com.atlassian.pageobjects.elements.query.Conditions;
import com.atlassian.pageobjects.elements.query.TimedCondition;

public class UpdateSmtpMailServerPage extends AbstractEditMailServerPage
{
    @Override
    public TimedCondition isAt()
    {
        return Conditions.and(updateButton.timed().isPresent(), jndiLocation.timed().isPresent());
    }

    public UpdateSmtpMailServerPage test() {
        testButton.click();
        return pageBinder.bind(UpdateSmtpMailServerPage.class);
    }

    public OutgoingServersPage update()
    {
        updateButton.click();
        return pageBinder.bind(OutgoingServersPage.class);
    }

}
