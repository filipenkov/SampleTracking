package com.atlassian.jira.issue.fields.config;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.test.util.DuckTypeProxy;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.context.persistence.FieldConfigContextPersister;
import com.atlassian.jira.local.LegacyJiraMockTestCase;

import org.ofbiz.core.entity.GenericValue;

import com.mockobjects.dynamic.Mock;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * User: Justin
 * Date: 6/09/2006
 * Time: 12:18:31
 */
public class TestFieldConfigSchemeImpl extends LegacyJiraMockTestCase
{
    private FieldConfigScheme fieldConfigScheme = null;

    private Map configMap = null;
    private Mock constantsManager = null;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        // Add 'anything' to the contexts so we get past is isEnabled() check
        final Object contextPersisterDuck = new Object()
        {
            public List getAllContextsForConfigScheme(final FieldConfigScheme fieldConfigScheme)
            {
                return EasyList.build("one value");
            }
        };
        final FieldConfigContextPersister mockContextPersister = (FieldConfigContextPersister) DuckTypeProxy.getProxy(
            FieldConfigContextPersister.class, contextPersisterDuck);
        fieldConfigScheme = new FieldConfigScheme.Builder().setName("test scheme").setDescription("scheme description").setFieldConfigContextPersister(mockContextPersister).toFieldConfigScheme();
    }

    @Override
    protected void tearDown() throws Exception
    {
        fieldConfigScheme = null;
        configMap = null;
        constantsManager = null;
        super.tearDown();
    }

    public void testGetAssociatedIssueTypes()
    {
        // Setup the requirements for the test
        // Create an issue type to use for the test
        final GenericValue issueTypeGV = UtilsForTests.getTestEntity("IssueType", EasyMap.build("id", "1", "name", "Bug", "description", "A Bug"));

        // Set these into the config, for now don't worry about the FieldConfigImpl
        configMap = EasyMap.build(issueTypeGV.getString("id"), null);
        fieldConfigScheme = new FieldConfigScheme.Builder(fieldConfigScheme).setConfigs(configMap).toFieldConfigScheme();

        // Mock out the contants manager and mock out the getIssueType call
        constantsManager = new Mock(ConstantsManager.class);
        constantsManager.expectAndReturn("getIssueType", issueTypeGV.getString("id"), issueTypeGV);
        ManagerFactory.addService(ConstantsManager.class, (ConstantsManager) constantsManager.proxy());

        // Make the call to test
        final Set issueTypes = fieldConfigScheme.getAssociatedIssueTypes();

        // Assert that we have not returned null and have a non-empty Set
        assertNotNull(issueTypes);
        assertFalse(issueTypes.isEmpty());

        // Assert that we have returned a GV in the (for now!)
        assertTrue(issueTypes.contains(issueTypeGV));
    }
}
