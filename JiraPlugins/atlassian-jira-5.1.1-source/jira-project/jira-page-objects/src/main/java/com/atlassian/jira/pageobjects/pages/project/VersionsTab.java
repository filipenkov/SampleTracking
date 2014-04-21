package com.atlassian.jira.pageobjects.pages.project;

import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.openqa.selenium.By;

import javax.inject.Inject;

/**
 * 'Browse Versions' project tab,
 *
 * @since v4.4
 */
public class VersionsTab extends AbstractProjectTab
{
    public static String LINK_ID = "versions-panel-panel";


    @ElementBy(id="versions_panel")
    private PageElement versionsTable;

    @Inject
    private PageBinder pageBinder;

    public VersionsTab(String projectKey)
    {
        super(LINK_ID, projectKey);
    }


    public Iterable<VersionRow> getVersions()
    {
        return Iterables.transform(versionsTable.findAll(By.tagName("tr")), new Function<PageElement, VersionRow>()
        {
            @Override
            public VersionRow apply(PageElement from)
            {
                return pageBinder.bind(VersionRow.class, projectKey, from);
            }
        });
    }

    public VersionRow getVersion(final String versionName)
    {
        // can;t do this that way now, CSS selectors in WebDriver suck:
//        final PageElement versionRow = versionsTable.find(By.cssSelector(String.format("tr[data-version-name='%s']", versionName)));
//        return pageBinder.bind(VersionRow.class, projectKey, versionRow);
        return Iterables.find(getVersions(), new Predicate<VersionRow>()
        {
            @Override
            public boolean apply(VersionRow input)
            {
                return input.getName().equals(versionName);
            }
        });

    }
}
