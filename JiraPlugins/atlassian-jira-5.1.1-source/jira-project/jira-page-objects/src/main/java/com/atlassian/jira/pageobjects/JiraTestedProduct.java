package com.atlassian.jira.pageobjects;

import com.atlassian.jira.functest.framework.backdoor.Backdoor;
import com.atlassian.jira.pageobjects.components.JiraHeader;
import com.atlassian.jira.pageobjects.config.AdaptingConfigProvider;
import com.atlassian.jira.pageobjects.config.EnvironmentBasedProductInstance;
import com.atlassian.jira.pageobjects.config.FuncTestPluginDetector;
import com.atlassian.jira.pageobjects.config.JiraConfigProvider;
import com.atlassian.jira.pageobjects.config.JiraSetup;
import com.atlassian.jira.pageobjects.config.ProductInstanceBasedEnvironmentData;
import com.atlassian.jira.pageobjects.config.RestoreJiraData;
import com.atlassian.jira.pageobjects.config.RestoreJiraDataProvider;
import com.atlassian.jira.pageobjects.config.SimpleJiraSetup;
import com.atlassian.jira.pageobjects.config.TestEnvironment;
import com.atlassian.jira.pageobjects.framework.MessageBoxPostProcessor;
import com.atlassian.jira.pageobjects.navigator.AdvancedSearch;
import com.atlassian.jira.pageobjects.pages.DashboardPage;
import com.atlassian.jira.pageobjects.pages.JiraAdminHomePage;
import com.atlassian.jira.pageobjects.pages.JiraLoginPage;
import com.atlassian.jira.pageobjects.pages.LogoutPage;
import com.atlassian.jira.pageobjects.pages.viewissue.ViewIssuePage;
import com.atlassian.jira.pageobjects.websudo.JiraWebSudoBanner;
import com.atlassian.jira.pageobjects.websudo.JiraWebSudoPage;
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
import com.atlassian.pageobjects.component.WebSudoBanner;
import com.atlassian.pageobjects.elements.ElementModule;
import com.atlassian.pageobjects.elements.timeout.PropertiesBasedTimeouts;
import com.atlassian.pageobjects.elements.timeout.TimeoutsModule;
import com.atlassian.pageobjects.page.AdminHomePage;
import com.atlassian.pageobjects.page.HomePage;
import com.atlassian.pageobjects.page.LoginPage;
import com.atlassian.pageobjects.page.WebSudoPage;
import com.atlassian.selenium.visualcomparison.VisualComparableClient;
import com.atlassian.webdriver.AtlassianWebDriver;
import com.atlassian.webdriver.AtlassianWebDriverModule;
import com.atlassian.webdriver.pageobjects.DefaultWebDriverTester;
import com.atlassian.webdriver.pageobjects.WebDriverTester;
import com.atlassian.webdriver.visualcomparison.WebDriverVisualComparableClient;
import com.google.inject.Binder;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Scopes;

import javax.annotation.Nonnull;

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
    private final JIRAEnvironmentData environmentData;
    private final InjectPageBinder pageBinder;

    private final WindowSession windowSession;
    private final Backdoor backdoor;

    public JiraTestedProduct(TestedProductFactory.TesterFactory<WebDriverTester> testerFactory, ProductInstance productInstance)
    {
        this.webDriverTester = testerFactory != null ? testerFactory.create() : new DefaultWebDriverTester();
        this.productInstance = checkNotNull(productInstance);
        this.environmentData = buildEnvironmentData(productInstance);
        this.backdoor = new Backdoor(environmentData);
        this.pageBinder = new InjectPageBinder(productInstance, webDriverTester,
                new StandardModule(this),
                new AtlassianWebDriverModule(this),
                new ElementModule(),
                new TimeoutsModule(PropertiesBasedTimeouts.fromClassPath(TIMEOUTS_PATH)),
                new EnvironmentDataModule(),
                new JiraUtilsModule(webDriverTester.getDriver()),
                new JiraInjectionPostProcessors());
        this.pageBinder.override(Header.class, JiraHeader.class);
        this.pageBinder.override(HomePage.class, DashboardPage.class);
        this.pageBinder.override(AdminHomePage.class, JiraAdminHomePage.class);
        this.pageBinder.override(LoginPage.class, JiraLoginPage.class);
        this.pageBinder.override(WebSudoBanner.class, JiraWebSudoBanner.class);
        this.pageBinder.override(WebSudoPage.class, JiraWebSudoPage.class);
        this.windowSession = injector().getProvider(WindowSession.class).get();
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

    public AdvancedSearch goToIssueNavigator()
    {
        return pageBinder.navigateToAndBind(AdvancedSearch.class);
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

    public JiraTestedProduct logout()
    {
        pageBinder.navigateToAndBind(LogoutPage.class).logout();
        return this;
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
        return environmentData;
    }

    @Nonnull
    private JIRAEnvironmentData buildEnvironmentData(@Nonnull final ProductInstance productInstance)
    {
        if (productInstance instanceof EnvironmentBasedProductInstance)
        {
            return ((EnvironmentBasedProductInstance)productInstance).environmentData();
        }
        else
        {
            return new ProductInstanceBasedEnvironmentData(productInstance);
        }
    }

    public Backdoor backdoor()
    {
        return backdoor;
    }

    public Injector injector()
    {
        return pageBinder.injector();
    }

    public WindowSession windowSession()
    {
        return windowSession;
    }

    private class EnvironmentDataModule implements Module
    {
        @Override
        public void configure(Binder binder)
        {
            binder.bind(JIRAEnvironmentData.class).toInstance(environmentData);
            binder.bind(Backdoor.class).toInstance(backdoor);
            binder.bind(TestEnvironment.class).toInstance(new TestEnvironment());
        }
    }

    private static class JiraUtilsModule implements Module
    {
        private final AtlassianWebDriver driver;

        public JiraUtilsModule(AtlassianWebDriver driver)
        {
            this.driver = driver;
        }

        @Override
        public void configure(Binder binder)
        {
            binder.bind(FuncTestPluginDetector.class).in(Scopes.SINGLETON);
            binder.bind(RestoreJiraData.class).toProvider(RestoreJiraDataProvider.class);
            binder.bind(JiraConfigProvider.class).to(AdaptingConfigProvider.class).in(Scopes.SINGLETON);
            binder.bind(JiraSetup.class).to(SimpleJiraSetup.class);
            binder.bind(VisualComparableClient.class).toInstance(new WebDriverVisualComparableClient(driver));
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
