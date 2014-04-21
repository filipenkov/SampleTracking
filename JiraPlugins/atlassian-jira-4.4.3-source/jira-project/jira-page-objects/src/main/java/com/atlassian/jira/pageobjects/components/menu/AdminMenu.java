package com.atlassian.jira.pageobjects.components.menu;

import com.atlassian.jira.pageobjects.pages.AbstractJiraAdminPage;
import com.atlassian.pageobjects.DelayedBinder;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import org.openqa.selenium.By;

import javax.inject.Inject;
import java.util.List;

import static com.atlassian.pageobjects.elements.query.Poller.waitUntilTrue;


/**
 * Represents menu providing access to all administration pages.
 *
 * @since 4.4
 */
public class AdminMenu
{

    @Inject private PageBinder binder;
    @Inject private PageElementFinder elementFinder;

    private List<PageElement> linkDropdowns;

    @Init
    public void initDropdowns()
    {
        linkDropdowns = elementFinder.find(By.id("main-nav")).findAll(By.className("aui-dd-parent"));
    }

    public <T extends AbstractJiraAdminPage> T goToAdminPage(Class<T> pageType) {
        final DelayedBinder<T> delayed = binder.delayedBind(pageType);
        final String id = delayed.get().linkId();
        for (PageElement parent : linkDropdowns)
        {
            if (parent.find(By.id(id)).isPresent())
            {
                // TODO doesnt work now cause layout if links in admin section is changing in a very agile manner
                // this will be added to the body after opening the dropdown
                PageElement theLink = elementFinder.find(By.id(id));
                // we need to open the dropdown first
                parent.find(By.className("aui-dd-link")).click();
                waitUntilTrue(theLink.timed().isVisible());
                theLink.click();
                return delayed.bind();
            }
        }
        throw new IllegalArgumentException("Could not find admin link with ID " + id + " in any admin menu");
    }
}
