package com.atlassian.config.xml;

import com.atlassian.config.ConfigurationException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.XPath;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: ROSS
 * Date: 16/03/2004
 * Time: 11:29:26
 * To change this template use File | Settings | File Templates.
 */
public class Dom4jXmlMapConfigElement extends Dom4jXmlStringConfigElement
{
    public Dom4jXmlMapConfigElement(String name, Element context, AbstractDom4jXmlConfigurationPersister config)
    {
        super(name, context, config);
    }

    public Class getObjectClass()
    {
        return Map.class;
    }

    public void saveConfig(Object object) throws ConfigurationException
    {
        Map map = (Map) object;
        Element node = DocumentHelper.makeElement(context, getPropertyName());
        Map.Entry entry;
        for (Iterator iterator = map.entrySet().iterator(); iterator.hasNext();)
        {
            entry = (Map.Entry) iterator.next();
            getConfiguration().addConfigElement(entry, "property", node);
        }
    }

    public Object loadConfig() throws ConfigurationException
    {
        XPath xpath = DocumentHelper.createXPath("/" + context.getName() + "/" + getPropertyName());
        Element element = (Element) xpath.selectSingleNode(context);
        Map.Entry entry;
        Map map = new HashMap();

        for (Iterator iterator = element.elementIterator(); iterator.hasNext();)
        {
            entry = (Map.Entry) getConfiguration().getConfigElement(Map.Entry.class, "property", (Element) iterator.next());
            map.put(entry.getKey(), entry.getValue());
        }
        return map;
    }
}
