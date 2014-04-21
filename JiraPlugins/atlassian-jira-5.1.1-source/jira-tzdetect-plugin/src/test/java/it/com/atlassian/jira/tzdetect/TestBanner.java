package it.com.atlassian.jira.tzdetect;

import com.atlassian.jira.pageobjects.JiraTestedProduct;
import com.atlassian.jira.pageobjects.pages.DashboardPage;
import com.atlassian.pageobjects.TestedProductFactory;
import it.com.atlassian.jira.tzdetect.pageobject.TzDetectBanner;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestBanner
{
    static JiraTestedProduct jira;

    @BeforeClass
    public static void setUp() throws Exception
    {
        jira = TestedProductFactory.create(JiraTestedProduct.class);
    }

    @Test
    public void bannerShouldShowUpOnDashboard() throws Exception
    {
        jira.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);
        TzDetectBanner banner = jira.getPageBinder().bind(TzDetectBanner.class);
    }
}