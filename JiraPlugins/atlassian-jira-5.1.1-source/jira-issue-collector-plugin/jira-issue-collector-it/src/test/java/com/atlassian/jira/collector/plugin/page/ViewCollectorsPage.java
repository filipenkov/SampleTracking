package com.atlassian.jira.collector.plugin.page;

import com.atlassian.jira.pageobjects.components.DropDown;
import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.atlassian.webdriver.AtlassianWebDriver;
import org.openqa.selenium.By;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import static com.atlassian.pageobjects.elements.query.Poller.waitUntil;
import static org.hamcrest.Matchers.is;

public class ViewCollectorsPage extends AbstractJiraPage
{
    public static final String URI = "/secure/ViewCollectors!default.jspa?projectKey=";

    @Inject
    private PageElementFinder elementFinder;

	@ElementBy (id = "add_collector")
	private PageElement addCollectorButton;

	@ElementBy (id = "collector-list")
    private PageElement collectorList;

    @ElementBy (cssSelector = ".aui-message.error")
    private PageElement errorList;

    private final String projectKey;
    private List<String> errors;

    public ViewCollectorsPage(final String projectKey)
    {
        this.projectKey = projectKey;
    }

	@Override
    public TimedCondition isAt()
    {
        return collectorList.timed().isPresent();
    }

    public int getCollectorCount()
    {
        return collectorList.findAll(By.className("collector-name")).size();
    }

    public List<Collector> getCollectors()
    {
        final List<Collector> ret = new ArrayList<Collector>();
        final List<PageElement> rows = collectorList.findAll(By.cssSelector("tbody tr"));
        for (PageElement row : rows)
        {
            ret.add(new Collector(driver, row));
        }
        return ret;
    }

    public Collector getCollectorById(String id)
    {
        for (Collector collector : getCollectors())
        {
            if (collector.getId().equals(id))
            {
                return collector;
            }
        }
        return null;
    }

    public AddCollectorPage addCollector()
    {
		addCollectorButton.click();
		return pageBinder.bind(AddCollectorPage.class, projectKey);
    }

    public String getUrl()
    {
        return URI + projectKey;
    }

    public ViewCollectorsPage deleteCollector(final String collectorId)
    {
        final Collector collector = getCollectorById(collectorId);
        collector.delete();
        return pageBinder.navigateToAndBind(ViewCollectorsPage.class, projectKey);
    }

    public List<String> getErrors()
    {
        final List<String> ret = new ArrayList<String>();
        if(errorList.isVisible())
        {
            for(PageElement li : errorList.find(By.className("error-list")).findAll(By.tagName("li")))
            {
                ret.add(li.getText());
            }
        }
        return ret;
    }

    public static class Collector
    {
        private String id;
        private final String name;
        private final String creator;
        private final String issueType;

        private final String description;
        private final PageElement disable;
        private final PageElement delete;
        private final AtlassianWebDriver driver;
        private final PageElement row;

        public Collector(final AtlassianWebDriver driver, final PageElement row)
        {
            this.driver = driver;
            this.row = row;
            this.id = row.getAttribute("data-collector-id");
            this.name = row.find(By.className("collector-name")).getText();
            this.creator = row.find(By.className("collector-creator")).getText();
            this.issueType = row.find(By.className("collector-issue-type")).getText();
            this.description = row.find(By.className("collector-desc")).getText();
            this.disable = row.find(By.className("disable-collector-lnk"));
            this.delete = row.find(By.className("delete-collector-lnk"));
        }

        public String getId()
        {
            return id;
        }

        public String getName()
        {
            return name;
        }

        public String getCreator()
        {
            return creator;
        }

        public String getIssueType()
        {
            return issueType;
        }

        public String getDescription()
        {
            return description;
        }

        public void disable()
        {
            disable.click();
            waitUntil(row.timed().hasClass("disabled"), is(true));
        }

        public void delete()
        {
            delete.click();
            driver.switchTo().alert().accept();
            waitUntil(row.timed().isPresent(), is(false));
        }
    }
}
