package com.atlassian.jira.pageobjects.project.summary;

import com.atlassian.jira.pageobjects.pages.admin.SelectCvsModules;
import com.atlassian.jira.pageobjects.pages.admin.UalConfigurePage;
import com.atlassian.jira.pageobjects.pages.admin.ViewCvsModules;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.google.inject.Inject;
import org.openqa.selenium.By;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Page object for the Settings Panel.
 *
 * @since v4.4
 */
public class SettingsPanel extends AbstractSummaryPanel
{
    @Inject
    private PageBinder binder;

    @ElementBy(id ="project-config-webpanel-summary-settings")
    private PageElement settingsElement;

    @ElementBy(id = "project-config-cvs-change")
    private PageElement cvsChangeElement;

    @ElementBy(id = "configure_ual")
    private PageElement ualElement;

    public List<CvsModule> getModules()
    {
        List<PageElement> cvs = settingsElement.findAll(By.cssSelector(".project-config-cvs-repo"));
        final List<CvsModule> modules = new ArrayList<CvsModule>();
        for (PageElement element : cvs)
        {
            modules.add(new CvsModule(element));
        }
        return modules;
    }

    public boolean hasCvsChangeLink()
    {
        return cvsChangeElement.isPresent();
    }

    public boolean hasUalLink()
    {
        return ualElement.isPresent();
    }

    public UalConfigurePage gotoUalConfigure()
    {
        assertTrue("UAL link is not present.", ualElement.isPresent());
        ualElement.click();
        return binder.bind(UalConfigurePage.class, getProjectKey());
    }

    public SelectCvsModules gotoCvsChangeLink()
    {
        assertTrue("CVS link is not present.", cvsChangeElement.isPresent());
        cvsChangeElement.click();
        return binder.bind(SelectCvsModules.class, getProjectId());
    }

    public List<PageElement> getPluginElements()
    {
        return settingsElement.findAll(By.cssSelector(".project-config-operation-link"));
    }

    public class CvsModule
    {
        private final String name;
        private final PageElement url;

        public CvsModule(PageElement element)
        {
            this.name = element.getText();
            if (element.getAttribute("href") != null)
            {
                this.url = element;
            }
            else
            {
                this.url = null;
            }
        }

        public boolean hasLink()
        {
            return url != null;
        }

        public String getName()
        {
            return name;
        }

        public ViewCvsModules gotoViewCvsModules()
        {
            assertNotNull("The module has no link.", url);
            url.click();
            return binder.bind(ViewCvsModules.class);
        }

        @Override
        public String toString()
        {
            return name;
        }
    }
}
