package com.atlassian.jira.imports.project.customfield;

import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.local.ListeningTestCase;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapper;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapperImpl;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetAssert;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.security.auth.trustedapps.MockI18nHelper;
import org.easymock.MockControl;

/**
 * @since v3.13
 */
public class TestGroupCustomFieldImporter extends ListeningTestCase
{
    @Test
    public void testGetMappedImportValue()
    {
        final ExternalCustomFieldValueImpl customFieldValue = new ExternalCustomFieldValueImpl("1", "2", "3");
        customFieldValue.setStringValue("n00bs");

        GroupCustomFieldImporter groupCustomFieldImporter = new GroupCustomFieldImporter(null);
        ProjectCustomFieldImporter.MappedCustomFieldValue mappedCustomFieldValue = groupCustomFieldImporter.getMappedImportValue(null, customFieldValue, null);

        assertEquals("n00bs", mappedCustomFieldValue.getValue());
        assertNull(mappedCustomFieldValue.getParentKey());
    }

    @Test
    public void testCanMapImportValue()
    {
        final ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(null, null);

        MockControl mockGroupManagerControl = MockControl.createStrictControl(GroupManager.class);
        GroupManager mockGroupManager = (GroupManager) mockGroupManagerControl.getMock();
        mockGroupManager.groupExists("n00bs");
        mockGroupManagerControl.setReturnValue(true);
        mockGroupManager.groupExists("l33tz");
        mockGroupManagerControl.setReturnValue(false);
        mockGroupManagerControl.replay();

        final ExternalCustomFieldValueImpl customFieldValue = new ExternalCustomFieldValueImpl("1", "2", "3");
        GroupCustomFieldImporter groupCustomFieldImporter = new GroupCustomFieldImporter(mockGroupManager);
        MockI18nHelper i18n = new MockI18nHelper();

        // check "n00bs"
        customFieldValue.setStringValue("n00bs");
        MessageSet messageSet = groupCustomFieldImporter.canMapImportValue(projectImportMapper, customFieldValue, null, i18n);
        assertFalse(messageSet.hasAnyMessages());
        // check "l33tz"
        customFieldValue.setStringValue("l33tz");
        messageSet = groupCustomFieldImporter.canMapImportValue(projectImportMapper, customFieldValue, null, i18n);
        MessageSetAssert.assert1ErrorNoWarnings(messageSet, "admin.errors.project.import.group.validation.does.not.exist l33tz");
        // throw in an empty String for coverage
        customFieldValue.setStringValue("");
        messageSet = groupCustomFieldImporter.canMapImportValue(projectImportMapper, customFieldValue, null, i18n);
        assertFalse(messageSet.hasAnyMessages());

        assertEquals(2, projectImportMapper.getGroupMapper().getRequiredOldIds().size());
        assertTrue(projectImportMapper.getGroupMapper().getRequiredOldIds().contains("n00bs"));
        assertTrue(projectImportMapper.getGroupMapper().getRequiredOldIds().contains("l33tz"));

        mockGroupManagerControl.verify();
    }
}
