package com.atlassian.jira.collector.plugin.page;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.atlassian.pageobjects.elements.timeout.TimeoutType;
import org.junit.Assert;
import org.openqa.selenium.By;

import java.util.logging.Logger;

import static com.atlassian.pageobjects.elements.query.Poller.by;
import static com.atlassian.pageobjects.elements.query.Poller.waitUntil;
import static org.hamcrest.Matchers.is;

public class PageWithCollector extends AbstractJiraPage
{
    private final String pageUrl;

    private PageElement trigger;


    public PageWithCollector(final String pageUrl)
    {
        this.pageUrl = pageUrl;
    }

    @Override
    public TimedCondition isAt()
    {
        driver.navigate().to(pageUrl);
        trigger = elementFinder.find(By.id("atlwdg-trigger"));
        return trigger.timed().isPresent();
    }

    public String getTriggerText()
    {
        return trigger.getText();
    }
    
    public PageWithCollector openCollector()
    {
        trigger.click();
        final PageElement iframe = elementFinder.find(By.id("atlwdg-frame"));
        waitUntil(iframe.timed().isVisible(), is(true));

		driver.switchTo().defaultContent().switchTo().frame("atlwdg-frame");
		final PageElement frameBody = elementFinder.find(By.id("atlScriptlet"));
		waitUntil(frameBody.timed().isVisible(), is(true));
		driver.switchTo().defaultContent();

        return this;
    }
    
    public PageWithCollector summary(String summary)
    {
        return updateField("summary", summary);
    }

    public PageWithCollector description(String description)
    {
        return updateField("description", description);
    }

    public String submitFeedback()
    {
        String issueKeyValue = null;
		Logger logger = Logger.getLogger(getClass().getName());
		logger.info("iframe: src=" + elementFinder.find(By.tagName("iframe")).getAttribute("src"));
		driver.switchTo().defaultContent().switchTo().frame("atlwdg-frame");
		Assert.assertFalse(elementFinder.find(By.id("description")).getValue().trim().isEmpty());
		final PageElement post = elementFinder.find(By.cssSelector("form.aui"));
		logger.info("Action: " + post.getAttribute("method") + " " + post.getAttribute("action"));
		final PageElement submit = elementFinder.find(By.className("submit-button"));
        submit.withTimeout(TimeoutType.PAGE_LOAD).click();

        final PageElement resultContainer = elementFinder.find(By.className("msg-container"));
        waitUntil(resultContainer.timed().isVisible(), is(true));
        final PageElement issueKey = resultContainer.find(By.className("issue-key"));
        if(issueKey.isPresent())
        {
            issueKeyValue = issueKey.getText();
        }
        driver.switchTo().defaultContent();

        final PageElement iframe = elementFinder.find(By.id("atlwdg-frame"));
        waitUntil(iframe.timed().isVisible(), is(false), by(10000));
        return issueKeyValue;
    }

    public boolean isDisabled()
    {
        boolean disabled = false;
        driver.switchTo().defaultContent().switchTo().frame("atlwdg-frame");
        PageElement errorMsg = elementFinder.find(By.className("error-msg"));
        waitUntil(errorMsg.timed().isPresent(), is(true));
        if(errorMsg.getText().contains("currently out of action!"))
        {
            disabled = true;
        }

        driver.switchTo().defaultContent();
        return disabled;
    }

    @Override
    public String getUrl()
    {
        return pageUrl;
    }

    private PageWithCollector updateField(String fieldId, final String summary)
    {
        driver.switchTo().defaultContent().switchTo().frame("atlwdg-frame");
        final PageElement field = elementFinder.find(By.id(fieldId));
        waitUntil(field.timed().isVisible(), is(true));
        field.clear().type(summary);
        driver.switchTo().defaultContent();
        return this;
    }

    public String getCustomMessage()
    {
        final String msg;
        driver.switchTo().defaultContent().switchTo().frame("atlwdg-frame");
        final PageElement message = elementFinder.find(By.className("custom-msg"));
        msg = message.getText();
        driver.switchTo().defaultContent();
        return msg; 
    }
}
