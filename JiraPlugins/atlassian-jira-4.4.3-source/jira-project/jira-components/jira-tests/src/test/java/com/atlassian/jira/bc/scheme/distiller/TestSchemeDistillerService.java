package com.atlassian.jira.bc.scheme.distiller;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.mock.MockPermissionManager;
import com.atlassian.jira.scheme.AbstractSchemeTest;
import com.atlassian.jira.scheme.SchemeFactory;
import com.atlassian.jira.scheme.SchemeManager;
import com.atlassian.jira.scheme.SchemeManagerFactory;
import com.atlassian.jira.scheme.distiller.DistilledSchemeResult;
import com.atlassian.jira.scheme.distiller.SchemeDistiller;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;
import org.easymock.MockControl;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

/**
 * 
 */
public class TestSchemeDistillerService extends AbstractSchemeTest
{
    private SchemeDistillerService schemeDistillerService;

    public void testPersistNewSchemesValidationHappyPath() throws GenericEntityException
    {
        //bit of a hack to get the testScheme* objects created
        getSchemesForType("anytype");
        MockControl controlSchemeManager = MockControl.createStrictControl(SchemeManager.class);
        SchemeManager mockSchemeManager = (SchemeManager) controlSchemeManager.getMock();

        Mock mockSchemeDistiller = new Mock(SchemeDistiller.class);
        Mock mockSchemeManagerFactory = new Mock(SchemeManagerFactory.class);
        Mock mockSchemeFactory = new Mock(SchemeFactory.class);
        mockSchemeDistiller.expectAndReturn("persistNewSchemeMappings", P.ANY_ARGS, null);
        mockSchemeFactory.expectAndReturn("getSchemeWithEntitiesComparable", P.ANY_ARGS, testScheme1);

        mockSchemeManager.getScheme(testScheme1.getName());
        controlSchemeManager.setReturnValue(null, 1);
        controlSchemeManager.setReturnValue(new MockGenericValue("someScheme") ,1);
        controlSchemeManager.replay();

        mockSchemeManagerFactory.expectAndReturn("getSchemeManager", P.ANY_ARGS, mockSchemeManager);

        schemeDistillerService = new DefaultSchemeDistillerService((SchemeDistiller)mockSchemeDistiller.proxy(), (SchemeManagerFactory) mockSchemeManagerFactory.proxy(),
                getPermissionManager(false, true), ComponentAccessor.getJiraAuthenticationContext(), (SchemeFactory) mockSchemeFactory.proxy());
        SimpleErrorCollection errorCollection = new SimpleErrorCollection();

        DistilledSchemeResult distilledSchemeResult = new DistilledSchemeResult("anytype", EasyList.build(testScheme1) ,null,null);
        distilledSchemeResult.setResultingSchemeTempName(testScheme1.getName());

        schemeDistillerService.persistNewSchemeMappings(null, distilledSchemeResult, errorCollection);

        assertFalse("No errors occurred when persisting.", errorCollection.hasAnyErrors());
        mockSchemeDistiller.verify();
        mockSchemeFactory.verify();
        controlSchemeManager.verify();
        mockSchemeManagerFactory.verify();
    }

    public void testPersistNewSchemesValidationOriginalSchemeDeleted() throws GenericEntityException
    {
        //bit of a hack to get the testScheme* objects created
        getSchemesForType("anytype");
        MockControl controlSchemeManager = MockControl.createStrictControl(SchemeManager.class);
        SchemeManager mockSchemeManager = (SchemeManager) controlSchemeManager.getMock();

        Mock mockSchemeManagerFactory = new Mock(SchemeManagerFactory.class);

        mockSchemeManager.getScheme(testScheme1.getName());
        controlSchemeManager.setReturnValue(null, 2);
        controlSchemeManager.replay();

        mockSchemeManagerFactory.expectAndReturn("getSchemeManager", P.ANY_ARGS, mockSchemeManager);

        schemeDistillerService = new DefaultSchemeDistillerService(null, (SchemeManagerFactory) mockSchemeManagerFactory.proxy(),
                getPermissionManager(false, true), ComponentAccessor.getJiraAuthenticationContext(), null);
        SimpleErrorCollection errorCollection = new SimpleErrorCollection();

        DistilledSchemeResult distilledSchemeResult = new DistilledSchemeResult("anytype", EasyList.build(testScheme1) ,null,null);
        distilledSchemeResult.setResultingSchemeTempName(testScheme1.getName());

        schemeDistillerService.persistNewSchemeMappings(null, distilledSchemeResult, errorCollection);

        assertTrue("Errors occurred when persisting.", errorCollection.hasAnyErrors());
        assertTrue("Test the correct error message exists", errorCollection.getErrors().containsKey(testScheme1.getName()));
        assertEquals("Test the correct error message is shown",
                "Some of the original schemes (<strong>" + testScheme1.getName() +
                                "</strong>) have been modified/deleted since the merged scheme was generated. " +
                                "The merged scheme will not be saved and no project associations will be changed.",
                errorCollection.getErrors().get(testScheme1.getName()));

        controlSchemeManager.verify();
        mockSchemeManagerFactory.verify();
    }

    public void testPersistNewSchemesValidationSchemeEntitiesHaveChanged() throws GenericEntityException
    {
        //bit of a hack to get the testScheme* objects created
        getSchemesForType("anytype");
        MockControl controlSchemeManager = MockControl.createStrictControl(SchemeManager.class);
        SchemeManager mockSchemeManager = (SchemeManager) controlSchemeManager.getMock();

        Mock mockSchemeManagerFactory = new Mock(SchemeManagerFactory.class);
        Mock mockSchemeFactory = new Mock(SchemeFactory.class);
        mockSchemeFactory.expectAndReturn("getSchemeWithEntitiesComparable", P.ANY_ARGS, testScheme3);

        mockSchemeManager.getScheme(testScheme1.getName());
        controlSchemeManager.setReturnValue(null, 1);
        controlSchemeManager.setReturnValue(new MockGenericValue("someScheme") ,1);
        controlSchemeManager.replay();

        mockSchemeManagerFactory.expectAndReturn("getSchemeManager", P.ANY_ARGS, mockSchemeManager);

        schemeDistillerService = new DefaultSchemeDistillerService(null, (SchemeManagerFactory) mockSchemeManagerFactory.proxy(),
                getPermissionManager(false, true), ComponentAccessor.getJiraAuthenticationContext(), (SchemeFactory) mockSchemeFactory.proxy());
        SimpleErrorCollection errorCollection = new SimpleErrorCollection();

        DistilledSchemeResult distilledSchemeResult = new DistilledSchemeResult("anytype", EasyList.build(testScheme1) ,null,null);
        distilledSchemeResult.setResultingSchemeTempName(testScheme1.getName());

        schemeDistillerService.persistNewSchemeMappings(null, distilledSchemeResult, errorCollection);

        assertTrue("Errors occurred when persisting.", errorCollection.hasAnyErrors());
        assertTrue("Test the correct error message exists", errorCollection.getErrors().containsKey(testScheme1.getName()));
        assertEquals("Test the correct error message is shown",
                "Some of the original schemes (<strong>" + testScheme1.getName() +
                                "</strong>) have been modified/deleted since the merged scheme was generated. " +
                                "The merged scheme will not be saved and no project associations will be changed.",
                errorCollection.getErrors().get(testScheme1.getName()));
        mockSchemeFactory.verify();
        controlSchemeManager.verify();
        mockSchemeManagerFactory.verify();
    }

    public void testPersistNewSchemesValidationSchemeNameHasChanged() throws GenericEntityException
    {
        //bit of a hack to get the testScheme* objects created
        getSchemesForType("anytype");
        MockControl controlSchemeManager = MockControl.createStrictControl(SchemeManager.class);
        SchemeManager mockSchemeManager = (SchemeManager) controlSchemeManager.getMock();

        Mock mockSchemeManagerFactory = new Mock(SchemeManagerFactory.class);
        Mock mockSchemeFactory = new Mock(SchemeFactory.class);
        mockSchemeFactory.expectAndReturn("getSchemeWithEntitiesComparable", P.ANY_ARGS, testScheme2);

        mockSchemeManager.getScheme(testScheme1.getName());
        controlSchemeManager.setReturnValue(null, 1);
        controlSchemeManager.setReturnValue(new MockGenericValue("someScheme") ,1);
        controlSchemeManager.replay();

        mockSchemeManagerFactory.expectAndReturn("getSchemeManager", P.ANY_ARGS, mockSchemeManager);

        schemeDistillerService = new DefaultSchemeDistillerService(null, (SchemeManagerFactory) mockSchemeManagerFactory.proxy(),
                getPermissionManager(false, true), ComponentAccessor.getJiraAuthenticationContext(), (SchemeFactory) mockSchemeFactory.proxy());
        SimpleErrorCollection errorCollection = new SimpleErrorCollection();

        DistilledSchemeResult distilledSchemeResult = new DistilledSchemeResult("anytype", EasyList.build(testScheme1) ,null,null);
        distilledSchemeResult.setResultingSchemeTempName(testScheme1.getName());

        schemeDistillerService.persistNewSchemeMappings(null, distilledSchemeResult, errorCollection);

        assertTrue("Errors occurred when persisting.", errorCollection.hasAnyErrors());
        assertTrue("Test the correct error message exists", errorCollection.getErrors().containsKey(testScheme1.getName()));
        assertEquals("Test the correct error message is shown",
                "Some of the original schemes (<strong>" + testScheme1.getName() +
                                "</strong>) have been modified/deleted since the merged scheme was generated. " +
                                "The merged scheme will not be saved and no project associations will be changed.",
                errorCollection.getErrors().get(testScheme1.getName()));
        mockSchemeFactory.verify();
        controlSchemeManager.verify();
        mockSchemeManagerFactory.verify();
    }

    public void testPersistNewSchemesValidationSchemeDescHasChanged() throws GenericEntityException
    {
        //bit of a hack to get the testScheme* objects created
        getSchemesForType("anytype");
        MockControl controlSchemeManager = MockControl.createStrictControl(SchemeManager.class);
        SchemeManager mockSchemeManager = (SchemeManager) controlSchemeManager.getMock();

        Mock mockSchemeManagerFactory = new Mock(SchemeManagerFactory.class);
        Mock mockSchemeFactory = new Mock(SchemeFactory.class);
        testScheme2.setName(testScheme1.getName());
        testScheme2.setDescription("I am a desc");
        mockSchemeFactory.expectAndReturn("getSchemeWithEntitiesComparable", P.ANY_ARGS, testScheme2);

        mockSchemeManager.getScheme(testScheme1.getName());
        controlSchemeManager.setReturnValue(null, 1);
        controlSchemeManager.setReturnValue(new MockGenericValue("someScheme") ,1);
        controlSchemeManager.replay();

        mockSchemeManagerFactory.expectAndReturn("getSchemeManager", P.ANY_ARGS, mockSchemeManager);

        schemeDistillerService = new DefaultSchemeDistillerService(null, (SchemeManagerFactory) mockSchemeManagerFactory.proxy(),
                getPermissionManager(false, true), ComponentAccessor.getJiraAuthenticationContext(), (SchemeFactory) mockSchemeFactory.proxy());
        SimpleErrorCollection errorCollection = new SimpleErrorCollection();

        DistilledSchemeResult distilledSchemeResult = new DistilledSchemeResult("anytype", EasyList.build(testScheme1) ,null,null);
        distilledSchemeResult.setResultingSchemeTempName(testScheme1.getName());

        schemeDistillerService.persistNewSchemeMappings(null, distilledSchemeResult, errorCollection);

        assertTrue("Errors occurred when persisting.", errorCollection.hasAnyErrors());
        assertTrue("Test the correct error message exists", errorCollection.getErrors().containsKey(testScheme1.getName()));
        assertEquals("Test the correct error message is shown",
                "Some of the original schemes (<strong>" + testScheme1.getName() +
                                "</strong>) have been modified/deleted since the merged scheme was generated. " +
                                "The merged scheme will not be saved and no project associations will be changed.",
                errorCollection.getErrors().get(testScheme1.getName()));
        mockSchemeFactory.verify();
        controlSchemeManager.verify();
        mockSchemeManagerFactory.verify();
    }

    public void testIsNewSchemeNameValidNameDoesNotExist()
    {
        Mock mockSchemeManagerFactory = new Mock(SchemeManagerFactory.class);
        Mock mockSchemeManager = new Mock(SchemeManager.class);
        mockSchemeManager.expectAndReturn("getScheme", P.ANY_ARGS, null);
        mockSchemeManagerFactory.expectAndReturn("getSchemeManager", P.ANY_ARGS, mockSchemeManager.proxy());

        schemeDistillerService = new DefaultSchemeDistillerService(null, (SchemeManagerFactory)mockSchemeManagerFactory.proxy(), getPermissionManager(false, true), ComponentAccessor.getJiraAuthenticationContext(), null);
        SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        schemeDistillerService.isValidNewSchemeName(null, "fieldName", "schemeThatDoesNotExist", "does not matter", errorCollection);

        mockSchemeManagerFactory.verify();
        mockSchemeManager.verify();
        assertFalse("The error collection is empty.", errorCollection.hasAnyErrors());
    }

    public void testIsNewSchemeNameValidNameDoesExist()
    {
        Mock mockSchemeManagerFactory = new Mock(SchemeManagerFactory.class);
        Mock mockSchemeManager = new Mock(SchemeManager.class);
        mockSchemeManager.expectAndReturn("getScheme", P.ANY_ARGS, new MockGenericValue("BS ENTITY"));
        mockSchemeManagerFactory.expectAndReturn("getSchemeManager", P.ANY_ARGS, mockSchemeManager.proxy());

        schemeDistillerService = new DefaultSchemeDistillerService(null, (SchemeManagerFactory)mockSchemeManagerFactory.proxy(), getPermissionManager(false, true), ComponentAccessor.getJiraAuthenticationContext(), null);
        SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        schemeDistillerService.isValidNewSchemeName(null, "fieldName", "schemeThatDoesNotExist", "does not matter", errorCollection);

        mockSchemeManagerFactory.verify();
        mockSchemeManager.verify();
        assertTrue("The error collection is empty.", errorCollection.hasAnyErrors());
        assertTrue("The error for the field we sent through is there.", errorCollection.getErrors().containsKey("fieldName"));
        assertEquals("A scheme with the name you entered already exists. Please enter a different scheme name.", errorCollection.getErrors().get("fieldName"));
    }

    private PermissionManager getPermissionManager(final boolean projectAdminPermission, final boolean adminPermission)
    {
        return new MockPermissionManager()
        {
            public boolean hasPermission(int permissionsId, GenericValue entity, com.atlassian.crowd.embedded.api.User u)
            {
                return projectAdminPermission;
            }

            public boolean hasPermission(int permissionsId, com.atlassian.crowd.embedded.api.User u)
            {
                return adminPermission;
            }
        };
    }
}
