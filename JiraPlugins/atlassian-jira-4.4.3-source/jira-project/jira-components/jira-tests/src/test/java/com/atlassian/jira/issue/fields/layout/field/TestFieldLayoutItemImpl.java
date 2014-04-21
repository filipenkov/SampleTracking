package com.atlassian.jira.issue.fields.layout.field;

import com.atlassian.jira.local.MockControllerTestCase;
import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.OrderableField;

/**
 * @since v4.0
 */
public class TestFieldLayoutItemImpl extends MockControllerTestCase
{
    @Test
    public void testGetFieldDescriptionCustomField()
    {
        final String customFieldDesc = "Custom field description";
        final String fieldDescription = "Supplied description";

        final CustomField mockCustomField = mockController.getMock(CustomField.class);
        mockCustomField.getId();
        mockController.setReturnValue("ID");
        mockCustomField.getDescription();
        mockController.setReturnValue(customFieldDesc);

        final FieldManager mockFieldManager = mockController.getMock(FieldManager.class);
        mockFieldManager.isCustomField(mockCustomField);
        mockController.setReturnValue(true);
        mockFieldManager.getCustomField("ID");
        mockController.setReturnValue(mockCustomField);

        mockController.replay();

        // set a null description; assert that the custom field description is returned
        FieldLayoutItemImpl fieldLayoutItem = new FieldLayoutItemImpl(mockCustomField, null, false, true, null, null, mockFieldManager)
        {
            @Override
            FieldManager getFieldManager()
            {
                return mockFieldManager;
            }
        };
        assertEquals(customFieldDesc, fieldLayoutItem.getFieldDescription());

        // set a non-null description; assert that it is returned
        fieldLayoutItem = new FieldLayoutItemImpl(mockCustomField, fieldDescription, false, true, null, null, mockFieldManager)
        {
            @Override
            FieldManager getFieldManager()
            {
                return mockFieldManager;
            }
        };
        assertEquals(fieldDescription, fieldLayoutItem.getFieldDescription());
    }

    @Test
    public void testGetFieldDescriptionNonCustomField()
    {
        final String fieldDescription = "Supplied description";

        final OrderableField mockOrderableField = mockController.getMock(OrderableField.class);

        final FieldManager mockFieldManager = mockController.getMock(FieldManager.class);
        mockFieldManager.isCustomField(mockOrderableField);
        mockController.setReturnValue(false);

        mockController.replay();

        // set a null description; assert that it is returned (as field is not a CF)
        FieldLayoutItemImpl fieldLayoutItem = new FieldLayoutItemImpl(mockOrderableField, null, false, true, null, null, mockFieldManager)
        {
            @Override
            FieldManager getFieldManager()
            {
                return mockFieldManager;
            }
        };
        assertNull(fieldLayoutItem.getFieldDescription());

        // set a non-null description; assert that it is returned
        fieldLayoutItem = new FieldLayoutItemImpl(mockOrderableField, fieldDescription, false, true, null, null, mockFieldManager)
        {
            @Override
            FieldManager getFieldManager()
            {
                return mockFieldManager;
            }
        };
        assertEquals(fieldDescription, fieldLayoutItem.getFieldDescription());
    }
}
