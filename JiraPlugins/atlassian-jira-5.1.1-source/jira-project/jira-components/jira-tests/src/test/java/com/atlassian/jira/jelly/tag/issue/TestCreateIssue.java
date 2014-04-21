package com.atlassian.jira.jelly.tag.issue;

import com.atlassian.core.ofbiz.CoreFactory;
import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.JiraTestUtil;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.IssueRelationConstants;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.issue.comparator.ComponentComparator;
import com.atlassian.jira.issue.context.persistence.FieldConfigContextPersister;
import com.atlassian.jira.issue.customfields.persistence.CustomFieldValuePersister;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.CustomFieldDescription;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigSchemeManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenManager;
import com.atlassian.jira.issue.managers.DefaultCustomFieldManager;
import com.atlassian.jira.jelly.AbstractJellyTestCase;
import com.atlassian.jira.jql.context.FieldConfigSchemeClauseContextUtil;
import com.atlassian.jira.notification.NotificationSchemeManager;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.permission.PermissionSchemeManager;
import com.atlassian.jira.plugin.JiraHostContainer;
import com.atlassian.jira.plugin.JiraModuleDescriptorFactory;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.scheme.SchemeEntity;
import com.atlassian.jira.scheme.SchemeManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.type.GroupDropdown;
import com.atlassian.jira.upgrade.UpgradeTask;
import com.atlassian.jira.upgrade.tasks.UpgradeTask_Build101;
import com.atlassian.jira.upgrade.tasks.UpgradeTask_Build83;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.ImportUtils;
import com.atlassian.jira.util.JiraUtils;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.PluginController;
import com.atlassian.plugin.PluginSystemLifecycle;
import com.atlassian.plugin.event.impl.DefaultPluginEventManager;
import com.atlassian.plugin.loaders.PluginLoader;
import com.atlassian.plugin.loaders.SinglePluginLoader;
import com.atlassian.plugin.manager.DefaultPluginManager;
import com.atlassian.plugin.manager.store.MemoryPluginPersistentStateStore;
import electric.xml.Document;
import electric.xml.Element;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.ofbiz.core.entity.GenericValue;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class TestCreateIssue extends AbstractJellyTestCase
{
    private User u;
    private PluginAccessor oldPluginAccessor;
    private PluginController oldPluginController;
    private PluginSystemLifecycle oldPluginSystemLifecycle;
    private CustomFieldManager oldCustomFieldManager;
    private Group g;

    @Mock
    private CustomFieldDescription customFieldDescription;
    @Mock
    private I18nHelper.BeanFactory i18nFactory;

    public TestCreateIssue(final String s)
    {
        super(s);
    }

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        MockitoAnnotations.initMocks(this);

        final File file = new File(getPath(), "test-customfield-types.xml");
        final SinglePluginLoader loader = new SinglePluginLoader(file.toURI().toURL());

        final DefaultPluginManager pluginManager = new DefaultPluginManager(new MemoryPluginPersistentStateStore(),
            Collections.<PluginLoader> singletonList(loader), new JiraModuleDescriptorFactory(new JiraHostContainer()), new DefaultPluginEventManager());
        pluginManager.init();

        oldPluginAccessor = ComponentAccessor.getPluginAccessor();
        oldPluginController = ComponentAccessor.getPluginController();
        oldPluginSystemLifecycle = ComponentManager.getInstance().getPluginSystemLifecycle();

        ManagerFactory.addService(PluginAccessor.class, pluginManager);
        ManagerFactory.addService(PluginController.class, pluginManager);
        ManagerFactory.addService(PluginSystemLifecycle.class, pluginManager);

        oldCustomFieldManager = ComponentManager.getComponentInstanceOfType(CustomFieldManager.class);

        final DefaultCustomFieldManager defaultCustomFieldManager = new DefaultCustomFieldManager(pluginManager, ComponentManager.getComponentInstanceOfType(OfBizDelegator.class),
                ComponentManager.getComponentInstanceOfType(FieldConfigSchemeManager.class),
                ComponentManager.getComponentInstanceOfType(JiraAuthenticationContext.class),
                ComponentManager.getComponentInstanceOfType(ConstantsManager.class),
                ComponentManager.getComponentInstanceOfType(ProjectManager.class),
                ComponentManager.getComponentInstanceOfType(PermissionManager.class),
                ComponentManager.getComponentInstanceOfType(FieldConfigContextPersister.class),
                ComponentManager.getComponentInstanceOfType(FieldScreenManager.class),
                ComponentManager.getComponentInstanceOfType(RendererManager.class),
                ComponentManager.getComponentInstanceOfType(CustomFieldValuePersister.class),
                ComponentManager.getComponentInstanceOfType(NotificationSchemeManager.class),
                ComponentManager.getComponentInstanceOfType(FieldManager.class),
                ComponentManager.getComponentInstanceOfType(FieldConfigSchemeClauseContextUtil.class),
                ComponentManager.getComponentInstanceOfType(EventPublisher.class), customFieldDescription, i18nFactory);


        ManagerFactory.addService(CustomFieldManager.class, defaultCustomFieldManager);

        u = createMockUser("logged-in-user");
        g = createMockGroup("jira-user");
        addUserToGroup(u, g);
        JiraTestUtil.loginUser(u);

        ComponentAccessor.getApplicationProperties().setOption(APKeys.JIRA_OPTION_ALLOWUNASSIGNED, true);
        ManagerFactory.getGlobalPermissionManager().addPermission(Permissions.ADMINISTER, null);

        final SchemeManager permManager = ManagerFactory.getPermissionSchemeManager();
        final GenericValue scheme = permManager.createDefaultScheme();
        final PermissionManager pm = ManagerFactory.getPermissionManager();
        pm.addPermission(Permissions.CREATE_ISSUE, scheme, "jira-user", "group");
        pm.addPermission(Permissions.BROWSE, scheme, "jira-user", "group");

        UtilsForTests.getTestEntity("IssueType", EasyMap.build("id", "1", "name", "Bug", "sequence", 1L));

        // CReate field screens
        final UpgradeTask upgradeTask = JiraUtils.loadComponent(UpgradeTask_Build83.class);
        upgradeTask.doUpgrade(false);

        final UpgradeTask upgradeTask101 = JiraUtils.loadComponent(UpgradeTask_Build101.class);
        upgradeTask101.doUpgrade(false);
    }

    @Override
    protected void tearDown() throws Exception
    {
        u = null;
        ImportUtils.setSubvertSecurityScheme(false);
        ManagerFactory.addService(PluginAccessor.class, oldPluginAccessor);
        ManagerFactory.addService(PluginController.class, oldPluginController);
        ManagerFactory.addService(PluginSystemLifecycle.class, oldPluginSystemLifecycle);
        ManagerFactory.addService(CustomFieldManager.class, oldCustomFieldManager);
        oldPluginAccessor = null;
        oldPluginController = null;
        oldPluginSystemLifecycle = null;
        super.tearDown();
    }

    public void testCreateIssue() throws Exception
    {
        final JiraAuthenticationContext authenticationContext = ComponentAccessor.getJiraAuthenticationContext();
        // Log in user
        authenticationContext.setLoggedInUser(u);
        final GenericValue project = UtilsForTests.getTestEntity("Project", EasyMap.build("key", "ABC", "name", "A Project", "lead", u.getName(),
            "counter", 1L));
        ComponentAccessor.getIssueTypeScreenSchemeManager().associateWithDefaultScheme(project);
        ManagerFactory.getProjectManager().refresh();

        final PermissionSchemeManager permissionSchemeManager = ManagerFactory.getPermissionSchemeManager();
        final GenericValue defaultScheme = permissionSchemeManager.createDefaultScheme();
        final SchemeEntity schemeEntity = new SchemeEntity(GroupDropdown.DESC, Permissions.CREATE_ISSUE);
        final SchemeEntity schemeEntity2 = new SchemeEntity(GroupDropdown.DESC, Permissions.BROWSE);
        final SchemeEntity schemeEntity3 = new SchemeEntity(GroupDropdown.DESC, Permissions.ASSIGN_ISSUE);
        permissionSchemeManager.createSchemeEntity(defaultScheme, schemeEntity);
        permissionSchemeManager.createSchemeEntity(defaultScheme, schemeEntity2);
        permissionSchemeManager.createSchemeEntity(defaultScheme, schemeEntity3);
        permissionSchemeManager.addSchemeToProject(project, defaultScheme);

        final String scriptFilename = "create-issue.test.create-issue.jelly";
        final Document document = runScript(scriptFilename);

        final Element root = document.getRoot();
        assertEquals(0, root.getElements().size());

        //Check to see if project was created.
        final Collection issues = CoreFactory.getGenericDelegator().findAll("Issue");
        assertFalse(issues.isEmpty());
        assertEquals(1, issues.size());

        final GenericValue issue = (GenericValue) issues.iterator().next();
        assertEquals(issue.getLong("id").toString() + ":" + issue.getString("key"), root.getTextString().trim());
        // Log out user
        authenticationContext.setLoggedInUser(null);
    }

    public void testCreateIssueFromProject() throws Exception
    {
        final String scriptFilename = "create-issue.test.create-issue-from-project.jelly";
        final Document document = runScript(scriptFilename);

        final Element root = document.getRoot();
        assertEquals(0, root.getElements().size());

        //Check to see if project was created.
        final Collection issues = CoreFactory.getGenericDelegator().findAll("Issue");
        assertFalse(issues.isEmpty());
        assertEquals(1, issues.size());

        final GenericValue issue = (GenericValue) issues.iterator().next();
        assertEquals(issue.getLong("id").toString() + ":" + issue.getString("key"), root.getTextString().trim());
    }

    public void testCreateIssueWithDifferentReporter() throws Exception
    {
        final JiraAuthenticationContext authenticationContext = ComponentAccessor.getJiraAuthenticationContext();
        // Log in user
        authenticationContext.setLoggedInUser(u);
        final User repo = createMockUser("reporter-user");
        addUserToGroup(repo, g);
        final GenericValue project = UtilsForTests.getTestEntity("Project", EasyMap.build("key", "ABC", "name", "A Project", "lead", u.getName(),
            "counter", 1L));
        ComponentAccessor.getIssueTypeScreenSchemeManager().associateWithDefaultScheme(project);
        ManagerFactory.getProjectManager().refresh();

        final PermissionSchemeManager permissionSchemeManager = ManagerFactory.getPermissionSchemeManager();
        final GenericValue defaultScheme = permissionSchemeManager.createDefaultScheme();
        final SchemeEntity schemeEntity = new SchemeEntity(GroupDropdown.DESC, Permissions.CREATE_ISSUE);
        final SchemeEntity schemeEntity2 = new SchemeEntity(GroupDropdown.DESC, Permissions.BROWSE);
        final SchemeEntity schemeEntity3 = new SchemeEntity(GroupDropdown.DESC, Permissions.ASSIGN_ISSUE);
        permissionSchemeManager.createSchemeEntity(defaultScheme, schemeEntity);
        permissionSchemeManager.createSchemeEntity(defaultScheme, schemeEntity2);
        permissionSchemeManager.createSchemeEntity(defaultScheme, schemeEntity3);
        permissionSchemeManager.addSchemeToProject(project, defaultScheme);

        final String scriptFilename = "create-issue.test.create-issue-with-reporter.jelly";
        final Document document = runScript(scriptFilename);

        final Element root = document.getRoot();
        assertEquals(0, root.getElements().size());

        //Check to see if project was created.
        final Collection issues = CoreFactory.getGenericDelegator().findAll("Issue");
        assertFalse(issues.isEmpty());
        assertEquals(1, issues.size());

        final GenericValue issue = (GenericValue) issues.iterator().next();
        assertEquals(issue.getLong("id").toString() + ":" + issue.getString("key"), root.getTextString().trim());
        assertEquals("reporter-user", issue.getString("reporter"));

        // Log out user
        authenticationContext.setLoggedInUser(null);
    }

    public void testCreateIssueWithVersionsAndComponents() throws Exception
    {
        final IssueManager issueManager = ComponentAccessor.getIssueManager();
        final String scriptFilename = "create-issue.test.create-issue-with-multi-versions-components.jelly";
        final Document document = runScript(scriptFilename);

        final Element root = document.getRoot();
        assertEquals(0, root.getElements().size());

        //Check to see if project was created.
        final Collection issues = CoreFactory.getGenericDelegator().findAll("Issue");
        assertFalse(issues.isEmpty());
        assertEquals(1, issues.size());

        final GenericValue issue = (GenericValue) issues.iterator().next();
        assertEquals(issue.getLong("id").toString() + ":" + issue.getString("key"), root.getTextString().trim());

        // Get all the components for the issue
        final List components = issueManager.getEntitiesByIssue(IssueRelationConstants.COMPONENT, issue);
        assertEquals(4, components.size());
        Collections.sort(components, ComponentComparator.COMPARATOR);
        assertEquals("Comp 1", ((GenericValue) components.get(0)).getString("name"));
        assertEquals("Comp 2", ((GenericValue) components.get(1)).getString("name"));
        assertEquals("Comp 3", ((GenericValue) components.get(2)).getString("name"));
        assertEquals("Comp 4", ((GenericValue) components.get(3)).getString("name"));

        // Get all the fix versions for the issue
        final List affectsVersions = issueManager.getEntitiesByIssue(IssueRelationConstants.VERSION, issue);
        assertEquals(4, affectsVersions.size());
        // I know this looks strange but the component comparator will work for versions since it sorts by
        // name on a genericValue
        Collections.sort(affectsVersions, ComponentComparator.COMPARATOR);
        assertEquals("Ver 1", ((GenericValue) affectsVersions.get(0)).getString("name"));
        assertEquals("Ver 2", ((GenericValue) affectsVersions.get(1)).getString("name"));
        assertEquals("Ver 3", ((GenericValue) affectsVersions.get(2)).getString("name"));
        assertEquals("Ver 4", ((GenericValue) affectsVersions.get(3)).getString("name"));

    }

    public void testCreateIssuewithCustomField() throws Exception
    {

        final JiraAuthenticationContext authenticationContext = ComponentAccessor.getJiraAuthenticationContext();
        // Log in user
        authenticationContext.setLoggedInUser(u);

        final String scriptFilename = "create-issue-with-custom-field.jelly";

        final Document document = runScript(scriptFilename);

        if (document.getElementsByTagName("Error").getLength() != 0)
        {
            fail("there were errors");
        }

        final CustomFieldManager cfm = ComponentAccessor.getCustomFieldManager();
        final Collection allAversions = cfm.getCustomFieldObjectsByName("aversion");
        assertEquals(1, allAversions.size());

        final CustomField customField = (CustomField) allAversions.iterator().next();

        final IssueManager issueManager = ComponentAccessor.getIssueManager();
        for (int i = 1; i <= 3; i++)
        {
            final String issueKey = "ABC-" + i;
            final Issue issue = issueManager.getIssueObject(issueKey);
            final Collection versions =  (Collection) issue.getCustomFieldValue(customField);
            assertNotNull("Looks like no version custom field values were created on issue " + issueKey, versions);
            assertEquals("wrong number of custom field values", 1, versions.size());
            final Version version = (Version) versions.iterator().next();
            assertEquals("checking version is right on loop " + i, "V1", version.getName());
        }

        // cleanup
        authenticationContext.setLoggedInUser(null);

    }

    @Override
    protected String getRelativePath()
    {
        return "tag" + FS + "issue" + FS;
    }
}
