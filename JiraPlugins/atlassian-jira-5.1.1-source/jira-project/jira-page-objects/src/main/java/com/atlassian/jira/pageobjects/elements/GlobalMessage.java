package com.atlassian.jira.pageobjects.elements;

import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.timeout.TimeoutType;
import org.openqa.selenium.By;

/**
 * Global AUI message. The one that shows floating at the top of page.
 *
 *
 * @since v5.0
 */
public class GlobalMessage extends AuiMessage
{
    public GlobalMessage()
    {
        super(By.className("global-msg"));
    }
}
