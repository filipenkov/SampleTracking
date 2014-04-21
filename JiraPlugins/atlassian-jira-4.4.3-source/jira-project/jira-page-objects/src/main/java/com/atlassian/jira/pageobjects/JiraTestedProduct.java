package com.atlassian.jira.pageobjects;

import com.atlassian.jira.pageobjects.components.JiraHeader;
import com.atlassian.jira.pageobjects.config.AdaptingConfigProvider;
import com.atlassian.jira.pageobjects.config.EnvironmentBasedProductInstance;
import com.atlassian.jira.pageobjects.config.FuncTestPluginDetector;
import com.atlassian.jira.pageobjects.config.JiraConfigProvider;
import com.atlassian.jira.pageobjects.config.JiraSetup;
import com.atlassian.jira.pageobjects.config.RestoreJiraData;
import com.atlassian.jira.pageobjects.config.RestoreJiraDataFromUi;
import com.atlassian.jira.pageobjects.config.SimpleJiraSetup;
import com.atlassian.jira.pageobjects.config.TestEnvironment;
import com.atlassian.jira.pageobjects.config.WebSudoControl;
import com.atlassian.jira.pageobjects.framework.MessageBoxPostProcessor;
import com.atlassian.jira.pageobjects.pages.DashboardPage;
import com.atlassian.jira.pageobjects.pages.JiraAdminHomePage;
import com.atlassian.jira.pageobjects.pages.JiraLoginPage;
import com.atlassian.jira.pageobjects.pages.LogoutPage;
import com.atlassian.jira.pageobjects.pages.viewissue.ViewIssuePage;
import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import com.atlassian.pageobjects.Defaults;
import com.atlassian.pageobjects.DelayedBinder;
import com.atlassian.pageobjects.Page;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.ProductInstance;
import com.atlassian.pageobjects.TestedProduct;
import com.atlassian.pageobjects.TestedProductFactory;
import com.atlassian.pageobjects.binder.InjectPageBinder;
import com.atlassian.pageobjects.binder.StandardModule;
import com.atlassian.pageobjects.component.Header;
import com.atlassian.pageobjects.elements.ElementModule;
import com.atlassian.pageobjects.elements.timeout.PropertiesBasedTimeouts;
import com.atlassian.pageobjects.elements.timeout.TimeoutsModule;
import com.atlassian.pageobjects.page.AdminHomePage;
import com.atlassian.pageobjects.page.HomePage;
import com.atlassian.pageobjects.page.LoginPage;
import com.atlassian.webdriver.AtlassianWebDriverModule;
import com.atlassian.webdriver.pageobjects.DefaultWebDriverTester;
import com.atlassian.webdriver.pageobjects.WebDriverTester;
import com.google.inject.Binder;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Scopes;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * JIRA implementation of {@link com.atlassian.pageobjects.TestedProduct}.
 *
 * @since 4.4
 */
@Defaults (instanceId = "jira", contextPath = "/jira", httpPort = 2990)
public class JiraTestedProduct implements TestedProduct<WebDriverTester>
{
    private static final String TIMEOUTS_PATH = "com/atlassian/jira/pageobjects/pageobjects-timeouts.properties";
    
    private final WebDriverTester webDriverTester;
    private final ProductInstance productInstance;
    private final InjectPageBinder pageBinder;


    public JiraTestedProduct(TestedProductFactory.TesterFactory<WebDriverTester> testerFactory, ProductInstance productInstance)
    {
        this.webDriverTester = testerFactory != null ? testerFactory.create() : new DefaultWebDriverTester();
        this.productInstance = checkNotNull(productInstance);
        this.pageBinder = new InjectPageBinder(productInstance, webDriverTester,
                new StandardModule(this),
                new AtlassianWebDriverModule(this),
                new ElementModule(),
                new TimeoutsModule(PropertiesBasedTimeouts.fromClassPath(TIMEOUTS_PATH)),
                new EnvironmentDataModule(),
                new JiraUtilsModule(),
                new JiraInjectionPostProcessors());
        this.pageBinder.override(Header.class, JiraHeader.class);
        this.pageBinder.override(HomePage.class, DashboardPage.class);
        this.pageBinder.override(AdminHomePage.class, JiraAdminHomePage.class);
        this.pageBinder.override(LoginPage.class, JiraLoginPage.class);
    }

    public JiraTestedProduct(ProductInstance productInstance)
    {
        this(null, productInstance);
    }

    public DashboardPage gotoHomePage()
    {
        return pageBinder.navigateToAndBind(DashboardPage.class);
    }

    public JiraAdminHomePage gotoAdminHomePage()
    {
        return pageBinder.navigateToAndBind(JiraAdminHomePage.class);
    }

    public JiraLoginPage gotoLoginPage()
    {
        return pageBinder.navigateToAndBind(JiraLoginPage.class);
    }

    public ViewIssuePage goToViewIssue(String issueKey)
    {
        return pageBinder.navigateToAndBind(ViewIssuePage.class, issueKey);
    }

    /**
     * Synonyme to {@link #visit(Class, Object...)}.
     *
     * @param pageClass page class
     * @param params params
     * @param <P> page type
     * @return page instance
     */
    public <P extends Page> P goTo(Class<P> pageClass, Object... params)
    {
        return visit(pageClass, params);
    }

    public void logout()
    {
        pageBinder.navigateToAndBind(LogoutPage.class).logout();
    }

    public <P extends Page> P visit(Class<P> pageClass, Object... args)
    {
        return pageBinder.navigateToAndBind(pageClass, args);
    }

    public <P extends Page> DelayedBinder<P> visitDelayed(Class<P> pageClass, Object... args)
    {
        DelayedBinder<P> binder = pageBinder.delayedBind(pageClass, args);
        webDriverTester.gotoUrl(productInstance.getBaseUrl() + binder.get().getUrl());
        return binder;
    }

    public PageBinder getPageBinder()
    {
        return pageBinder;
    }

    public ProductInstance getProductInstance()
    {
        return productInstance;
    }

    public WebDriverTester getTester()
    {
        return webDriverTester;
    }

    public JIRAEnvironmentData environmentData()
    {
        if (productInstance instanceof EnvironmentBasedProductInstance)
        {
            return ((EnvironmentBasedProductInstance)productInstance).environmentData();
        }
        else
        {
            return null;
        }
    }

    public Injector injector()
    {
        return pageBinder.injector();
    }

    public WindowSession openWindowSession()
    {
        return new WindowSession(webDriverTester.getDriver());
    }

    private class EnvironmentDataModule implements Module
    {
        @Override
        public void configure(Binder binder)
        {
            JIRAEnvironmentData data = environmentData();
            if (data != null)
            {
                binder.bind(JIRAEnvironmentData.class).toInstance(data);
            }
            binder.bind(TestEnvironment.class).toInstance(new TestEnvironment());
        }
    }

    private static class JiraUtilsModule implements Module
    {
        @Override
        public void configure(Binder binder)
        {
            binder.bind(FuncTestPluginDetector.class).in(Scopes.SINGLETON);
            binder.bind(RestoreJiraData.class).to(RestoreJiraDataFromUi.class);
            binder.bind(JiraConfigProvider.class).to(AdaptingConfigProvider.class).in(Scopes.SINGLETON);
            binder.bind(JiraSetup.class).to(SimpleJiraSetup.class);
            binder.bind(WebSudoControl.class);
        }
    }

    private static class JiraInjectionPostProcessors implements Module
    {
        @Override
        public void configure(Binder binder)
        {
            binder.bind(MessageBoxPostProcessor.class);
        }
    }
}
