package com.atlassian.jira.issue.customfields.persistence;

import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import org.junit.Test;
import static org.junit.Assert.*;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.issue.fields.CustomFieldImpl;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.util.EasyList;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 */
public class TestOfBizCustomFieldValuePersister extends MockControllerTestCase
{
    @Test
    public void testRemoveAllValuesNullId() throws Exception
    {
        final OfBizCustomFieldValuePersister persister = mockController.instantiate(OfBizCustomFieldValuePersister.class);
        try
        {
            persister.removeAllValues((String) null);
            fail("Should have thrown exception!");
        }
        catch (IllegalArgumentException e)
        {
            // yay
        }
    }

    @Test
    public void testRemoveAllValues() throws Exception
    {
        final OfBizDelegator ofBizDelegator = mockController.getMock(OfBizDelegator.class);
        ofBizDelegator.findByAnd("CustomFieldValue", EasyMap.build("customfield", 10000L));
        final GenericValue mockGV1 = new MockGenericValue("CustomFieldValue", EasyMap.build("issue", -1L));
        final GenericValue mockGV2 = new MockGenericValue("CustomFieldValue", EasyMap.build("issue", 10200L));
        final GenericValue mockGV3 = new MockGenericValue("CustomFieldValue", EasyMap.build("issue", 10300L));
        mockController.setReturnValue(EasyList.build(mockGV1, mockGV2, mockGV3));

        ofBizDelegator.removeAll(EasyList.build(mockGV1, mockGV2, mockGV3));


        final OfBizCustomFieldValuePersister persister = mockController.instantiate(OfBizCustomFieldValuePersister.class);

        final Set<Long> issueIdsRemoved = persister.removeAllValues("customfield_10000");
        assertTrue(issueIdsRemoved.contains(10200L));
        assertTrue(issueIdsRemoved.contains(10300L));
        assertFalse(issueIdsRemoved.contains(-1L));
        assertEquals(2, issueIdsRemoved.size());
    }

    @Test
    public void testRemoveAllValuesDeprecated()
    {
        final AtomicBoolean removeAllValuesWithIdCalled = new AtomicBoolean(false);
        final OfBizCustomFieldValuePersister persister = new OfBizCustomFieldValuePersister(null)
        {
            @Override
            public Set removeAllValues(final String customFieldId)
            {
                removeAllValuesWithIdCalled.set(true);
                return Collections.emptySet();
            }
        };

        final Set set = persister.removeAllValues(new CustomFieldImpl(new MockGenericValue("CustomField", EasyMap.build("id", 1000L)), null, null, null, null, null, null, null));
        assertEquals(Collections.emptySet(), set);
        assertTrue(removeAllValuesWithIdCalled.get());
    }
}
