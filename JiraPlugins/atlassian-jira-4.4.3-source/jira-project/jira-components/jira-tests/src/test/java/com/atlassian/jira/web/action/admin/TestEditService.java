package com.atlassian.jira.web.action.admin;

import com.atlassian.jira.local.MockControllerTestCase;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import com.atlassian.configurable.ObjectConfiguration;
import com.atlassian.configurable.ObjectConfigurationException;
import com.atlassian.jira.service.ServiceManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestEditService extends MockControllerTestCase
{
    private final List defaultList = new ArrayList();
    private final List valueList = new ArrayList();
    private final List arrayList = new ArrayList();
    private final List integerList = new ArrayList();

    private final ObjectConfiguration ocNull = new MockObjectConfiguration(null)
    {
        public String getFieldDefault(String key) throws ObjectConfigurationException
        {
            return null;
        }
    };

    private final ObjectConfiguration ocValue = new MockObjectConfiguration(null)
    {
        public String getFieldDefault(String key) throws ObjectConfigurationException
        {
            return "value 123";
        }
    };

    private final ObjectConfiguration ocException = new MockObjectConfiguration(null)
    {
        public String getFieldDefault(String key) throws ObjectConfigurationException
        {
            throw new ObjectConfigurationException("some message");
        }
    };
    private ServiceManager serviceManager;

    public TestEditService()
    {
        defaultList.add("value 123");
        valueList.add("v1");
        arrayList.add("123");
        arrayList.add("abc");
        integerList.add("789");
    }

    @Before
    public void setUp() throws Exception
    {
        serviceManager = getMock(ServiceManager.class);
    }

    @Test
    public void testGetParamValues() throws Exception
    {
        expect(serviceManager.containsServiceWithId(null)).andStubReturn(false);
        replay(serviceManager);

        EditService es1 = new EditService(serviceManager)
        {
            public ObjectConfiguration getObjectConfiguration() throws Exception
            {
                return ocNull;
            }
        };
        EditService es2 = new EditService(serviceManager)
        {
            public ObjectConfiguration getObjectConfiguration() throws Exception
            {
                return ocValue;
            }
        };
        EditService es3 = new EditService(serviceManager)
        {
            public ObjectConfiguration getObjectConfiguration() throws Exception
            {
                return ocException;
            }
        };

        assertEquals(Collections.EMPTY_LIST, es1.getParamValues(null));
        assertEquals(defaultList, es2.getParamValues(null));
        assertEquals(Collections.EMPTY_LIST, es3.getParamValues(null));

        Map params = new HashMap();
        params.put("k1", "v1");
        params.put("k3", new String[]{"123", "abc"});
        params.put("k4", 789);
        es1.setParameters(params);
        es2.setParameters(params);
        es3.setParameters(params);

        assertEquals(Collections.EMPTY_LIST, es1.getParamValues(null));
        assertEquals(defaultList, es2.getParamValues(null));
        assertEquals(Collections.EMPTY_LIST, es3.getParamValues(null));

        assertEquals(valueList, es1.getParamValues("k1"));
        assertEquals(valueList, es2.getParamValues("k1"));
        assertEquals(valueList, es3.getParamValues("k1"));

        assertEquals(Collections.EMPTY_LIST, es1.getParamValues("k2"));
        assertEquals(defaultList, es2.getParamValues("k2"));
        assertEquals(Collections.EMPTY_LIST, es3.getParamValues("k2"));

        assertEquals(arrayList, es1.getParamValues("k3"));
        assertEquals(arrayList, es2.getParamValues("k3"));
        assertEquals(arrayList, es3.getParamValues("k3"));

        assertEquals(integerList, es1.getParamValues("k4"));
        assertEquals(integerList, es2.getParamValues("k4"));
        assertEquals(integerList, es3.getParamValues("k4"));
    }
}
