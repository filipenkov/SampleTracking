package com.atlassian.jira.action.issue.customfields.persistence;

import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.issue.customfields.persistence.CustomFieldValuePersister;
import com.atlassian.jira.issue.customfields.persistence.OfBizCustomFieldValuePersister;
import com.atlassian.core.util.collection.EasyList;
import org.ofbiz.core.entity.GenericValue;
import com.atlassian.jira.mock.ofbiz.MockOfBizDelegator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MockCustomFieldValuePersister extends OfBizCustomFieldValuePersister implements CustomFieldValuePersister
{

    public MockCustomFieldValuePersister(List genericValues, List expectedGenericValues)
    {
        super(new MockOfBizDelegator(genericValues, expectedGenericValues));
    }

    public static GenericValue _getCustValue1()
    {
        Map entityFields = new HashMap();
        entityFields.put(ENTITY_ISSUE_ID, new Long(1));
        entityFields.put(ENTITY_VALUE_TYPE, null);
        entityFields.put(ENTITY_PARENT_KEY, null);
        entityFields.put(ENTITY_CUSTOMFIELD_ID, new Long(10001));
        entityFields.put(FIELD_TYPE_STRING, "1000");
        return new MockGenericValue(TABLE_CUSTOMFIELD_VALUE, entityFields);
    }

    public static GenericValue _getCustValue2()
    {
        Map entityFields = new HashMap();
        entityFields.put(ENTITY_ISSUE_ID, new Long(2));
        entityFields.put(ENTITY_VALUE_TYPE, null);
        entityFields.put(ENTITY_PARENT_KEY, null);
        entityFields.put(ENTITY_CUSTOMFIELD_ID, new Long(10001));
        entityFields.put(FIELD_TYPE_STRING, "Value 2");
        return new MockGenericValue(TABLE_CUSTOMFIELD_VALUE, entityFields);
    }

    public static GenericValue _getCustValue3()
    {
        Map entityFields = new HashMap();
        entityFields.put(ENTITY_ISSUE_ID, new Long(3));
        entityFields.put(ENTITY_VALUE_TYPE, null);
        entityFields.put(ENTITY_PARENT_KEY, null);
        entityFields.put(ENTITY_CUSTOMFIELD_ID, new Long(10001));
        entityFields.put(FIELD_TYPE_STRING, "Value 3");
        return new MockGenericValue(TABLE_CUSTOMFIELD_VALUE, entityFields);
    }



    public static GenericValue _getCustValue4()
    {
        Map entityFields = new HashMap();
        entityFields.put(ENTITY_ISSUE_ID, new Long(1));
        entityFields.put(ENTITY_VALUE_TYPE, null);
        entityFields.put(ENTITY_PARENT_KEY, null);
        entityFields.put(ENTITY_CUSTOMFIELD_ID, new Long(10002));
        entityFields.put(FIELD_TYPE_STRING, "Value 1 Field 1");
        return new MockGenericValue(TABLE_CUSTOMFIELD_VALUE, entityFields);
    }

    public static List _getList()
    {
        return EasyList.build(_getCustValue1(), _getCustValue2(), _getCustValue3());
    }

    public MockOfBizDelegator getDelegator()
    {
        return (MockOfBizDelegator) delegator;
    }

    public List findAll()
    {
        return delegator.findAll(TABLE_CUSTOMFIELD_VALUE);
    }
}
