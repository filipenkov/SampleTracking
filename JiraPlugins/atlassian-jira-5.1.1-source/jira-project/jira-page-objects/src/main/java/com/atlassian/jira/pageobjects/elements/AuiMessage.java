package com.atlassian.jira.pageobjects.elements;

import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.binder.WaitUntil;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import org.openqa.selenium.By;

import javax.inject.Inject;

import static com.atlassian.pageobjects.elements.query.Conditions.and;
import static com.atlassian.pageobjects.elements.query.Poller.waitUntilTrue;

/**
 *
 * Generic AUI messages, error warning success
 *
 * @since v5.0
 */
public class AuiMessage
{
    private final By context;

    public AuiMessage(By context)
    {
        this.context = context;
    }

    public enum Type {
        SUCCESS,
        WARNING,
        ERROR,
        UNKNOWN(null);

        private final String className;

        Type()
        {
            this.className = name().toLowerCase();
        }

        Type(String name)
        {
            this.className = name;
        }

        public String className()
        {
            return className;
        }

        public boolean isClassifiable()
        {
            return this != UNKNOWN;
        }

    }

    @Inject
    private PageElementFinder elementFinder;

    private PageElement message;

    @WaitUntil
    private void messageVisible()
    {
        final PageElement parent = elementFinder.find(context);
        waitUntilTrue(and(parent.timed().isPresent(), parent.find(By.className("aui-message")).timed().isPresent()));
    }

    @Init
    private void setMessage()
    {
        message = elementFinder.find(context).find(By.className("aui-message"));
    }

    public void dismiss() {
        message.find(By.className("icon-close")).click();
    }

    /**
     * Gets text of message
     *
     * @return text of message
     */
    public String getMessage () {
        return message.getText();
    }

    /**
     * Does it have a X that when clicked dismisses message
     *
     * @return if closeable or not
     */
    public boolean isCloseable() {
        return message.find(By.className("icon-close")).isPresent();
    }

    /**
     * Type of message - Error, Warning, Success & UNKOWN
     * @return message type
     */
    public Type getType () {
        if (message.hasClass("success"))
        {
            return Type.SUCCESS;
        }
        else if (message.hasClass("warning"))
        {
            return Type.WARNING;
        }
        else if (message.hasClass("error"))
        {
            return Type.ERROR;
        }
        return Type.UNKNOWN;
    }
}
