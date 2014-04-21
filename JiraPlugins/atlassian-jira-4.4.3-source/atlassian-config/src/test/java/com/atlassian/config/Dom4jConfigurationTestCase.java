package com.atlassian.config;

import com.atlassian.config.xml.AbstractDom4jXmlConfigurationPersister;
import junit.framework.TestCase;

import java.io.File;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: ROSS
 * Date: 16/03/2004
 * Time: 14:22:16
 * To change this template use File | Settings | File Templates.
 */
public class Dom4jConfigurationTestCase extends TestCase
{
    public static final String TEST_RESOURCES_PATH = "src/test/resources";

    public void testSimpleConfig() throws Exception
    {
        ConfigurationPersister config = new AbstractDom4jXmlConfigurationPersister()
        {
            public String getRootName()
            {
                return "test-configuration";
            }

            public void save(String path, String fileName) throws ConfigurationException
            {
                addConfigElement("Hello Updated", "property1");
                addConfigElement("GoodBye Updated", "property2");
                addConfigElement(null, "property3");
                saveDocument(path, fileName);
            }

            public Object load(String configPath, String configFile) throws ConfigurationException
            {
                File file = new File(configPath + "/" + configFile);
                try
                {
                    this.loadDocument(file);
                }
                catch (Exception e)
                {
                    throw new ConfigurationException("Failed to load config from file: " + configPath + "/" + configFile, e);
                }
                Map props = new HashMap();
                props.put("property1", getConfigElement(String.class, "property1"));
                props.put("property2", getConfigElement(String.class, "property2"));

                return props;
            }
        };

        Map props = (Map) config.load(TEST_RESOURCES_PATH, "simple-config.xml");
        assertEquals(2, props.size());
        Map.Entry entry;
        for (Iterator iterator = props.entrySet().iterator(); iterator.hasNext();)
        {
            entry = (Map.Entry) iterator.next();
            if (entry.getKey().equals("property1"))
            {
                assertEquals("Hello", entry.getValue());
            }
            else if (entry.getKey().equals("property2"))
            {
                assertEquals("Goodbye", entry.getValue());
            }
            else
            {
                fail("What the hell is " + entry.getKey() + "!");
            }
        }
        config.save(TEST_RESOURCES_PATH, "simple-config2.xml");
    }

    public void testNestedConfig() throws Exception
    {
        ConfigurationPersister config = new AbstractDom4jXmlConfigurationPersister()
        {
            public String getRootName()
            {
                return "test-configuration";
            }

            public void save(String path, String fileName) throws ConfigurationException
            {
                addConfigElement("Hello Updated", "nest1/property1");
                //Test adding by context
//                Element e = getElement("nest1/nest2");
//                addConfigElement("GoodBye Updated", "property2", e);
                addConfigElement("GoodBye Updated", "nest1/nest2/property2");
                saveDocument(path, fileName);
            }

            public Object load(String configPath, String configFile) throws ConfigurationException
            {
                File file = new File(configPath + "/" + configFile);
                try
                {
                    this.loadDocument(file);
                }
                catch (Exception e)
                {
                    throw new ConfigurationException("Failed to load config from file: " + configPath + "/" + configFile, e);
                }
                Map props = new HashMap();
                props.put("property1", getConfigElement(String.class, "nest1/property1"));
                props.put("property2", getConfigElement(String.class, "nest1/nest2/property2"));

                return props;
            }
        };

        Map props = (Map) config.load(TEST_RESOURCES_PATH, "nested-config.xml");
        assertEquals(2, props.size());
        Map.Entry entry;
        for (Iterator iterator = props.entrySet().iterator(); iterator.hasNext();)
        {
            entry = (Map.Entry) iterator.next();
            if (entry.getKey().equals("property1"))
            {
                assertEquals("Hello", entry.getValue());
            }
            else if (entry.getKey().equals("property2"))
            {
                assertEquals("Goodbye", entry.getValue());
            }
            else
            {
                fail("What the hell is " + entry.getKey() + "!");
            }
        }
        config.save(TEST_RESOURCES_PATH, "nested-config2.xml");
    }


    public void testMapConfig() throws Exception
    {
        ConfigurationPersister config = new AbstractDom4jXmlConfigurationPersister()
        {
            public String getRootName()
            {
                return "test-configuration";
            }

            public void save(String path, String fileName) throws ConfigurationException
            {
                Map map = new HashMap();
                map.put("property1", "Hello Updated");
                map.put("property2", "Goodbye Updated");
                addConfigElement(map, "properties");
                saveDocument(path, fileName);
            }

            public Object load(String configPath, String configFile) throws ConfigurationException
            {
                File file = new File(configPath + "/" + configFile);
                try
                {
                    this.loadDocument(file);
                }
                catch (Exception e)
                {
                    throw new ConfigurationException("Failed to load config from file: " + configPath + "/" + configFile, e);
                }
                Map props = (Map) getConfigElement(Map.class, "properties");

                return props;
            }
        };

        Map props = (Map) config.load(TEST_RESOURCES_PATH, "map-config.xml");
        assertEquals(2, props.size());
        Map.Entry entry;
        for (Iterator iterator = props.entrySet().iterator(); iterator.hasNext();)
        {
            entry = (Map.Entry) iterator.next();
            if (entry.getKey().equals("property1"))
            {
                assertEquals("Hello", entry.getValue());
            }
            else if (entry.getKey().equals("property2"))
            {
                assertEquals("Goodbye", entry.getValue());
            }
            else
            {
                fail("What the hell is " + entry.getKey() + "!");
            }
        }
        config.save(TEST_RESOURCES_PATH, "map-config2.xml");
    }

    public void testListConfig() throws Exception
    {
        ConfigurationPersister config = new AbstractDom4jXmlConfigurationPersister()
        {
            public String getRootName()
            {
                return "test-configuration";
            }

            public void save(String path, String fileName) throws ConfigurationException
            {
                List list = new ArrayList();
                list.add("Hello Updated");
                list.add("Goodbye Updated");
                addConfigElement(list, "alist");
                saveDocument(path, fileName);
            }

            public Object load(String configPath, String configFile) throws ConfigurationException
            {
                File file = new File(configPath + "/" + configFile);
                try
                {
                    this.loadDocument(file);
                }
                catch (Exception e)
                {
                    throw new ConfigurationException("Failed to load config from file: " + configPath + "/" + configFile, e);
                }
                List list = (List) getConfigElement(List.class, "alist");

                return list;
            }
        };

        List list = (List) config.load(TEST_RESOURCES_PATH, "list-config.xml");
        assertEquals(2, list.size());
        assertEquals("Hello", list.get(0).toString());
        assertEquals("Goodbye", list.get(1).toString());

        config.save(TEST_RESOURCES_PATH, "list-config2.xml");
    }

    protected void tearDown() throws Exception
    {
//        File f = new File(TEST_RESOURCES_PATH + "/simple-config2.xml");
//        if(f.exists()) f.delete();
//
//        f = new File(TEST_RESOURCES_PATH + "/nested-config2.xml");
//        if(f.exists()) f.delete();
    }
}
