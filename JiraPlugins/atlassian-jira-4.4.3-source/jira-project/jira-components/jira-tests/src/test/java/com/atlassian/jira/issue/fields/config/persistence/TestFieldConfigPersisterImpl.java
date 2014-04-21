package com.atlassian.jira.issue.fields.config.persistence;

import com.atlassian.core.ofbiz.CoreFactory;
import com.atlassian.core.ofbiz.util.EntityUtils;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.jira.mock.MockConstantsManager;
import com.atlassian.jira.ofbiz.DefaultOfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import org.ofbiz.core.entity.GenericDelegator;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.Iterator;

public class TestFieldConfigPersisterImpl extends LegacyJiraMockTestCase
{
    public void testRemove() throws Exception
    {
        GenericDelegator genericDelegator = CoreFactory.getGenericDelegator();
        OfBizDelegator ofBizDelegator = new DefaultOfBizDelegator(genericDelegator);
        final FieldConfigPersister persister = new FieldConfigPersisterImpl(ofBizDelegator);

        // set up a FieldConfig and associated options and generic config
        final String fieldConfigIdFirst = "10001";
        final String fieldConfigIdSecond = "10010";
        createFieldConfig(fieldConfigIdFirst);
        createFieldConfig(fieldConfigIdSecond);

        createGenericConfig("10000", fieldConfigIdFirst);
        createGenericConfig(fieldConfigIdSecond, fieldConfigIdSecond);

        createOptionConfig("10011", fieldConfigIdFirst);
        createOptionConfig("10012", fieldConfigIdFirst);
        createOptionConfig("10013", fieldConfigIdFirst);
        createOptionConfig("10014", fieldConfigIdSecond);
        createOptionConfig("10015", fieldConfigIdSecond);
        createOptionConfig("10016", fieldConfigIdSecond);
        createOptionConfig("10017", fieldConfigIdSecond);

        FieldConfig fc1 = persister.getFieldConfig(new Long(fieldConfigIdFirst));
        FieldConfig fc2 = persister.getFieldConfig(new Long(fieldConfigIdSecond));

        // check row counts
        assertEquals(2, ofBizDelegator.findAll("FieldConfiguration").size());

        // remove the first
        persister.remove(fc1);

        // check row counts
        assertEquals(1, ofBizDelegator.findAll("FieldConfiguration").size());

        // remove the second
        persister.remove(fc2);

        // check row counts
        assertEquals(0, ofBizDelegator.findAll("FieldConfiguration").size());
    }

    public void testRemoveConfigsForConfigScheme() throws Exception
    {
        GenericDelegator genericDelegator = CoreFactory.getGenericDelegator();
        OfBizDelegator ofBizDelegator = new DefaultOfBizDelegator(genericDelegator);
        class MyConfigPersister extends FieldConfigPersisterImpl
        {
            MyConfigPersister(OfBizDelegator delegator)
            {
                super(delegator);
            }

            void removeConfigsForConfigScheme(final FieldConfigScheme scheme)
            {
                Collection fieldConfigs = getConfigsExclusiveToConfigScheme(scheme.getId());
                for (Iterator it = fieldConfigs.iterator(); it.hasNext();)
                {
                    remove((FieldConfig) it.next());
                }
            }
        }
        final MyConfigPersister configPersister = new MyConfigPersister(ofBizDelegator);
        final FieldConfigSchemePersister configSchemePersister = new FieldConfigSchemePersisterImpl(ofBizDelegator, new MockConstantsManager(), configPersister, null);

        createFieldConfig("10001");
        createFieldConfig("10010");
        createFieldConfig("10100");
        createFieldConfig("11000");

        createFieldConfigScheme("20001");
        createFieldConfigScheme("20010");
        createFieldConfigScheme("20100");
        createFieldConfigScheme("21000");

        createFieldConfigSchemeIssueType("30001", "10001", "20001", null);
        createFieldConfigSchemeIssueType("30010", "10010", "20010", null);
        createFieldConfigSchemeIssueType("30100", "10010", "20100", null);
        GenericValue staleGV =
                createFieldConfigSchemeIssueType("31000", "10100", "20100", null);

        createFieldConfigSchemeIssueType("40001", "11000", "21000", "1");
        createFieldConfigSchemeIssueType("40010", "11000", "21000", "2");

        FieldConfigScheme cs1 = configSchemePersister.getFieldConfigScheme(20001L);
        FieldConfigScheme cs2 = configSchemePersister.getFieldConfigScheme(20010L);
        FieldConfigScheme cs3 = configSchemePersister.getFieldConfigScheme(20100L);
        FieldConfigScheme cs4 = configSchemePersister.getFieldConfigScheme(21000L);

        // check row counts
        assertEquals(4, ofBizDelegator.findAll("FieldConfiguration").size());
        assertEquals(4, ofBizDelegator.findAll("FieldConfigScheme").size());
        assertEquals(6, ofBizDelegator.findAll("FieldConfigSchemeIssueType").size());

        // try remove configs for a scheme which is mapped to multiple issue types
        // NOTE: this is not currently possible from regular use of JIRA, but the database allows it, so we need to test
        // that the delete still works
        configPersister.removeConfigsForConfigScheme(cs4);

        // check row counts
        assertEquals(3, ofBizDelegator.findAll("FieldConfiguration").size());

        // try remove configs for a scheme which has no configs uniquely dependant on it
        configPersister.removeConfigsForConfigScheme(cs2);

        // check row counts
        assertEquals(3, ofBizDelegator.findAll("FieldConfiguration").size());

        // now try remove config for a scheme which has 1 uniquely dependant and 1 non-unique dependant
        configPersister.removeConfigsForConfigScheme(cs3);

        // check row counts
        assertEquals(2, ofBizDelegator.findAll("FieldConfiguration").size());

        // need to remove mapping to deleted config before refreshing scheme
        assertEquals(1, ofBizDelegator.removeValue(staleGV));
        cs3 = configSchemePersister.getFieldConfigScheme(20100L);

        // now try remove config for a scheme which has 1 uniquely dependant only
        configPersister.removeConfigsForConfigScheme(cs1);

        // check row counts
        assertEquals(1, ofBizDelegator.findAll("FieldConfiguration").size());

        // try again to remove configs for schemes which had non-unique dependants before - they should still fail
        configPersister.removeConfigsForConfigScheme(cs2);
        assertEquals(1, ofBizDelegator.findAll("FieldConfiguration").size());
        configPersister.removeConfigsForConfigScheme(cs3);
        assertEquals(1, ofBizDelegator.findAll("FieldConfiguration").size());
    }

    private GenericValue createFieldConfig(String id) throws GenericEntityException
    {
        return EntityUtils.createValue("FieldConfiguration", EasyMap.build("id", id, "name", "Test Config " + id, "description", "Test Desc", "fieldid", "customfield_" + id, "customfield", null));
    }

    private GenericValue createFieldConfigScheme(String id) throws GenericEntityException
    {
        return EntityUtils.createValue("FieldConfigScheme", EasyMap.build("id", id, "name", "Test Config " + id, "description", "Test Desc", "fieldid", "customfield_" + id, "customfield", null));
    }

    private GenericValue createFieldConfigSchemeIssueType(String id, String fieldConfigId, String fieldConfigSchemeId, final String issueType)
            throws GenericEntityException
    {
        return EntityUtils.createValue("FieldConfigSchemeIssueType", EasyMap.build("id", id, "issuetype", issueType, "fieldconfigscheme", fieldConfigSchemeId, "fieldconfiguration", fieldConfigId));
    }

    private GenericValue createGenericConfig(String id, String fieldConfigId) throws GenericEntityException
    {
        return EntityUtils.createValue("GenericConfiguration", EasyMap.build("id", id, "datatype", "DefaultValue", "datakey", fieldConfigId, "xmlvalue", "<string>" + fieldConfigId + "</string>"));
    }

    private GenericValue createOptionConfig(String id, String fieldConfigId) throws GenericEntityException
    {
        return EntityUtils.createValue("OptionConfiguration", EasyMap.build("id", id, "fieldid", "issuetype", "optionid", id, "fieldconfig", fieldConfigId, "sequence", id));
    }

}
