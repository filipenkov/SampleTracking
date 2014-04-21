package com.atlassian.jira.pageobjects.pages.viewissue;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;

import javax.inject.Inject;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Represents view issue page.
 *
 * @since v4.4
 */
public class ViewIssuePage extends AbstractJiraPage
{
    private static final String URI_TEMPLATE = "/browse/%s";

    private final String issueKey;
    private final String uri;

    @ElementBy(id = "stalker")
    private PageElement stalkerBar;

    @ElementBy(id = "key-val")
    private PageElement issueHeaderLink;

    @Inject
    private PageBinder pageBinder;

    private IssueMenu issueMenu;

    public ViewIssuePage(String issueKey)
    {
        this.issueKey = checkNotNull(issueKey);
        this.uri = String.format(URI_TEMPLATE, issueKey);
    }

    @Init
    public void initComponents()
    {
        issueMenu = pageBinder.bind(IssueMenu.class, this);
    }

    @Override
    public TimedCondition isAt()
    {
        return issueHeaderLink.timed().hasText(issueKey);
    }

    @Override
    public String getUrl()
    {
        return uri;
    }

    public String readKeyFromPage()
    {
        return issueHeaderLink.getText();
    }

}
