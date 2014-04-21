package com.atlassian.jira.pageobjects.navigator;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.pageobjects.Page;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.atlassian.pageobjects.elements.timeout.TimeoutType;
import org.openqa.selenium.By;

import javax.annotation.Nullable;
import javax.inject.Inject;

/**
 * @since v4.4
 */
public class AdvancedSearch extends AbstractJiraPage implements Page
{

    @Inject
    private PageBinder pageBinder;

    @ElementBy(id="jqltext")
    protected PageElement jqlInput;

    @ElementBy(id="jqlrunquery")
    protected PageElement submitButton;

    @ElementBy(id="jqlerror")
    protected PageElement jqlError;

    @ElementBy(id="issuenav", timeoutType = TimeoutType.PAGE_LOAD)
    protected  PageElement mainContent;

    @ElementBy(cssSelector = ".jqlerror-container .info")
    protected PageElement jqlInfo;

    @Nullable
    protected Long filterId;

    public AdvancedSearch() {
        // empty
    }

    public AdvancedSearch(Long filterId) {
        this.filterId = filterId;
    }

    @Override
    public TimedCondition isAt()
    {
        return mainContent.timed().isPresent();
    }

    public AdvancedSearch enterQuery(final String query)
    {
        jqlInput.clear().type(query);
        return this;
    }

    public AdvancedSearch submit()
    {
        submitButton.click();
        return pageBinder.bind(this.getClass());
    }

    public String getJQLError()
    {
        return jqlError.getText();
    }

    public String getJQLInfo()
    {
        return jqlInfo.getText();
    }

    public IssueNavigatorResults getResults()
    {
        return pageBinder.bind(IssueNavigatorResults.class);
    }

    @Override
    public String getUrl()
    {
        if (filterId != null) {
            return "/secure/IssueNavigator.jspa?navType=advanced&mode=show&requestId=" + filterId;
        } else {
            return "/secure/IssueNavigator!switchView.jspa?navType=advanced&mode=show&createNew=true";
        }
    }
    
    public String returnJQLErrorMessage()
    {
        return elementFinder.find(By.id("jqlerror")).getText();
    }
}
