package com.atlassian.jira.scheme.distiller;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.notification.NotificationSchemeManager;
import com.atlassian.jira.permission.PermissionSchemeManager;
import com.atlassian.jira.project.*;
import com.atlassian.jira.scheme.*;
import com.atlassian.jira.upgrade.tasks.UpgradeTask1_2;
import com.atlassian.jira.upgrade.tasks.UpgradeTask_Build35;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.util.JiraUtils;
import com.atlassian.core.util.map.EasyMap;
import com.mockobjects.constraint.IsEqual;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;
import org.ofbiz.core.entity.GenericValue;

import java.util.*;

/**
 */
public class TestSchemeDistiller extends AbstractSchemeTest
{
    private SchemeDistiller schemeDistiller;


    public void testSchemeSmoosherPermissionSchemes()
    {
        Mock mockSchemeManagerFactory = new Mock(SchemeManagerFactory.class);

        Mock mockPermmissionSchemeManager = new Mock(PermissionSchemeManager.class);
        mockPermmissionSchemeManager.expectAndReturn("getScheme", P.ANY_ARGS, null);
        mockPermmissionSchemeManager.expectAndReturn("getProjects", P.ANY_ARGS, Collections.EMPTY_LIST);

        mockSchemeManagerFactory.expectAndReturn("getSchemeManager", P.args(new IsEqual(SchemeManagerFactory.PERMISSION_SCHEME_MANAGER)), mockPermmissionSchemeManager.proxy());

        schemeDistiller = new SchemeDistillerImpl((SchemeManagerFactory) mockSchemeManagerFactory.proxy(), null, null);

        doSmooshTest(getSchemesForType(SchemeManagerFactory.PERMISSION_SCHEME_MANAGER));
    }

    public void testSchemeSmoosherNotificationSchemes()
    {
        Mock mockSchemeManagerFactory = new Mock(SchemeManagerFactory.class);

        Mock mockNotificationSchemeManager = new Mock(NotificationSchemeManager.class);
        mockNotificationSchemeManager.expectAndReturn("getScheme", P.ANY_ARGS, null);
        mockNotificationSchemeManager.expectAndReturn("getProjects", P.ANY_ARGS, Collections.EMPTY_LIST);

        mockSchemeManagerFactory.expectAndReturn("getSchemeManager", P.args(new IsEqual(SchemeManagerFactory.NOTIFICATION_SCHEME_MANAGER)), mockNotificationSchemeManager.proxy());

        schemeDistiller = new SchemeDistillerImpl((SchemeManagerFactory) mockSchemeManagerFactory.proxy(), null, null);

        doSmooshTest(getSchemesForType(SchemeManagerFactory.NOTIFICATION_SCHEME_MANAGER));
    }

    public void testPersistNewSchemeMappingsForNotificationSchemes() throws Exception
    {
        // Make sure we are using the real projectManager instead of the caching one so we don't run into any
        // issues.
        ManagerFactory.addService(ProjectManager.class, JiraUtils.loadComponent(DefaultProjectManager.class));

        // Running the upgrade task will ensure that we have a real representation of the default notificaiton
        // scheme so that we can exercise all the possible strange bits in a scheme.
        UpgradeTask_Build35 upgradeTask_build35 = new UpgradeTask_Build35();
        upgradeTask_build35.doUpgrade(false);

        doTestPersistNewSchemeMappingsForSchemes("Default Notification Scheme", true);
    }

    public void testPersistNewSchemeMappingsForPermissionSchemes() throws Exception
    {
        // Make sure we are using the real projectManager instead of the caching one so we don't run into any
        // issues.
        ManagerFactory.addService(ProjectManager.class, JiraUtils.loadComponent(DefaultProjectManager.class));

        // Running the upgrade task will ensure that we have a real representation of the default permission
        // scheme so that we can exercise all the possible strange bits in a scheme.
        UpgradeTask1_2 upgradeTask1_2 = new UpgradeTask1_2();
        upgradeTask1_2.doUpgrade(false);

        doTestPersistNewSchemeMappingsForSchemes("Default Permission Scheme", false);
    }

    private void doTestPersistNewSchemeMappingsForSchemes(String defaultSchemeName, boolean notification) throws Exception
    {

        // Get the deps that we need
        ProjectFactory projectFactory = ComponentAccessor.getProjectFactory();
        SchemeManagerFactory schemeManagerFactory = (SchemeManagerFactory) ComponentManager.getComponentInstanceOfType(SchemeManagerFactory.class);
        SchemeFactory schemeFactory = (SchemeFactory) ComponentManager.getComponentInstanceOfType(SchemeFactory.class);

        SchemeManager schemeManager = notification ? schemeManagerFactory.getSchemeManager(SchemeManagerFactory.NOTIFICATION_SCHEME_MANAGER)
                : schemeManagerFactory.getSchemeManager(SchemeManagerFactory.PERMISSION_SCHEME_MANAGER);

        // Create a working SchemeDistiller
        schemeDistiller = new SchemeDistillerImpl(schemeManagerFactory, null, null);

        // Create a project and get the Object representation of it.
        GenericValue projectGV = UtilsForTests.getTestEntity("Project", EasyMap.build("id", new Long(1), "key", "TST", "name", "project 1"));
        Project project1 = projectFactory.getProject(projectGV);

        // Create a second project and get the Object representation of it.
        GenericValue projectGV2 = UtilsForTests.getTestEntity("Project", EasyMap.build("id", new Long(2), "key", "TST2", "name", "project 2"));
        Project project2 = ComponentAccessor.getProjectFactory().getProject(projectGV2);

        // Create a third project and get the Object representation of it.
        GenericValue projectGV3 = UtilsForTests.getTestEntity("Project", EasyMap.build("id", new Long(3), "key", "TST3", "name", "project 3"));
        Project project3 = ComponentAccessor.getProjectFactory().getProject(projectGV3);

        // Get the defaultNotification scheme and copy it twice
        GenericValue defaultSchemeGV = schemeManager.getScheme(defaultSchemeName);
        GenericValue copiedNotificationScheme1 = schemeManager.copyScheme(defaultSchemeGV);
        GenericValue copiedNotificationScheme2 = schemeManager.copyScheme(defaultSchemeGV);

        // Get the object representations of the schemes we just created
        Scheme defaultScheme = schemeFactory.getSchemeWithEntitiesComparable(defaultSchemeGV);
        Scheme scheme1 = schemeFactory.getSchemeWithEntitiesComparable(copiedNotificationScheme1);
        Scheme scheme2 = schemeFactory.getSchemeWithEntitiesComparable(copiedNotificationScheme2);

        // Associate one project with one scheme so that we have three different project associated with 3 different
        // schemes.
        schemeManager.addSchemeToProject(project1, defaultScheme);
        schemeManager.addSchemeToProject(project2, scheme1);
        schemeManager.addSchemeToProject(project3, scheme2);

        // Smoosh the schemes, this should smoosh down to 1 scheme
        DistilledSchemeResults results = schemeDistiller.distillSchemes(EasyList.build(defaultScheme, scheme1, scheme2));

        assertEquals(1, results.getDistilledSchemeResults().size());
        assertEquals(0, results.getUnDistilledSchemes().size());

        // Test that we can persist the result
        Scheme scheme = schemeDistiller.persistNewSchemeMappings((DistilledSchemeResult) new ArrayList(results.getDistilledSchemeResults()).get(0));
        assertNotNull(scheme);

        // get the associated projects for the new scheme
        List projects = schemeManager.getProjects(scheme);

        // The new scheme should now be associated with the 3 projects
        assertEquals(3, projects.size());

        // Make sure that the three projects are the 3 that we created.
        List projectNames = new ArrayList();
        for (Iterator iterator = projects.iterator(); iterator.hasNext();)
        {
            Project project = (Project) iterator.next();
            projectNames.add(project.getName());
        }
        assertTrue(projectNames.contains(project1.getName()));
        assertTrue(projectNames.contains(project2.getName()));
        assertTrue(projectNames.contains(project3.getName()));
    }

    private void doSmooshTest(Collection schemes)
    {
        DistilledSchemeResults distilledSchemeResults = schemeDistiller.distillSchemes(schemes);

        // We should get 2 results, one that has smooshed the testScheme1 & 2 and a result with an unsmooshed scheme
        // for testScheme3.
        assertEquals(1, distilledSchemeResults.getDistilledSchemeResults().size());
        assertEquals(1, distilledSchemeResults.getUnDistilledSchemes().size());

        // Check that the unSmooshed scheme is the one it should be, testScheme3
        assertTrue(testScheme3.containsSameEntities((Scheme) new ArrayList(distilledSchemeResults.getUnDistilledSchemes()).get(0)));


        DistilledSchemeResult distilledSchemeResult = (DistilledSchemeResult) new ArrayList(distilledSchemeResults.getDistilledSchemeResults()).get(0);
        assertEquals(2, distilledSchemeResult.getOriginalSchemes().size());
        assertTrue(testScheme1.containsSameEntities(distilledSchemeResult.getResultingScheme()));
    }

}
