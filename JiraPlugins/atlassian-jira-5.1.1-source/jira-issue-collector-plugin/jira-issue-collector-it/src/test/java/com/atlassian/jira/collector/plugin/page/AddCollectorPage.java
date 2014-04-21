package com.atlassian.jira.collector.plugin.page;

import com.atlassian.jira.pageobjects.components.fields.SingleSelect;
import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import org.openqa.selenium.By;

import javax.inject.Inject;

import static com.atlassian.pageobjects.elements.query.Poller.waitUntil;
import static org.hamcrest.core.Is.is;

public class AddCollectorPage extends AbstractJiraPage
{
    public static enum TriggerStyle
    {
        PROMINENT, SUBTLE, CUSTOM
    }

    public static enum Template
    {
        RAISE_BUG, FEEDBACK, CUSTOM
    }

    public static final String URI = "/secure/AddCollector!default.jspa?projectKey=";

    @Inject
    private PageElementFinder elementFinder;

    @ElementBy (id = "collector-name-container")
    private PageElement nameContainer;

    @ElementBy (id = "name")
    private PageElement nameInput;

    @ElementBy (id = "reporter-container")
    private PageElement reporterContainer;

	private SingleSelect reporterSelect;

    @ElementBy (id = "collector-match-reporter")
    private PageElement useCredentials;
    
    @ElementBy (id = "trigger-text")
    private PageElement triggerText;

    @ElementBy (id = "add-collector-submit")
    private PageElement submit;

    @ElementBy (id = "customMessage")
    private PageElement customMessage;

	@ElementBy (id = "template-raise-bug")
	private PageElement raiseBugRadio;

    private final String projectKey;

    public AddCollectorPage(final String projectKey)
    {
        this.projectKey = projectKey;
    }

	@Init
	public void init() {
		reporterSelect = pageBinder.bind(SingleSelect.class, reporterContainer);
	}

    @Override
    public TimedCondition isAt()
    {
        return nameInput.timed().isPresent();
    }

    public AddCollectorPage name(String name)
    {
        nameInput.clear().type(name);
        return this;
    }

    public AddCollectorPage reporter(String reporter) {
		reporterSelect.select(reporter);
        return this;
    }
    
    public AddCollectorPage useCredentials()
    {
        useCredentials.click();
        return this;
    }

	public AddCollectorPage useRaiseBugTemplate() {
		raiseBugRadio.click();
		return this;
	}

    public CustomTemplateDialog template(Template templateType)
    {
        elementFinder.find(By.id("template-" + templateType.toString().toLowerCase())).click();
        return pageBinder.bind(CustomTemplateDialog.class);
    }

    public CongratulationsPage submit()
    {
      waitUntil(submit.timed().isEnabled(), is(true));
      submit.click();
	  return pageBinder.bind(CongratulationsPage.class, projectKey);
    }

    public AddCollectorPage submitExpectingError()
    {
        waitUntil(submit.timed().isEnabled(), is(true));
        submit.click();
      	return this;
    }

    public AddCollectorPage trigger(final TriggerStyle trigger, final String triggerText)
    {
        elementFinder.find(By.id("position-" + trigger.toString().toLowerCase())).click();
        this.triggerText.clear().type(triggerText);
        return this;
    }

    public AddCollectorPage customMessage(final String message)
    {
        customMessage.clear().type(message);
        return this;
    }

    public String getUrl()
    {
        return URI + projectKey;
    }

    public String getCollectorNameError()
    {
        PageElement error = nameContainer.find(By.className("error"));
        return (null != error) ? error.getText() : null;
    }

    public String getIssueReporterError()
    {
         return reporterSelect.getError();
    }

    public String getIssueReporterValue()
    {
        return reporterSelect.getValue();
    }
}
