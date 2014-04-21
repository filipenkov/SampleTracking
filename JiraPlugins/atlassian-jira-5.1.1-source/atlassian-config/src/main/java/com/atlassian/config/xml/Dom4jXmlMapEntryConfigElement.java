package com.atlassian.config.xml;

import com.atlassian.config.ConfigurationException;
import org.dom4j.Element;

import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: ROSS
 * Date: 16/03/2004
 * Time: 13:00:44
 * To change this template use File | Settings | File Templates.
 */
public class Dom4jXmlMapEntryConfigElement extends Dom4jXmlStringConfigElement
{
    public Dom4jXmlMapEntryConfigElement(String name, Element context, AbstractDom4jXmlConfigurationPersister config)
    {
        super(name, context, config);
    }

    public Class getObjectClass()
    {
        return Map.Entry.class;
    }

    public void saveConfig(Object object) throws ConfigurationException
    {
        Map.Entry entry = (Map.Entry) object;
        String name = entry.getKey().toString();

        if (entry.getValue() == null)
            return;

        Element element = (Element) context.selectSingleNode(getPropertyName() + "[@name='" + name + "']");
        if (element == null)
        {
            element = context.addElement(getPropertyName());
            element.addAttribute("name", name);
        }
        else
        {
            element.clearContent();
        }
        if (useCData)
        {
            element.addCDATA(entry.getValue().toString());
        }
        else
        {
            element.setText(entry.getValue().toString());
        }
    }

    public Object loadConfig() throws ConfigurationException
    {
        String key = context.attribute("name").getValue();
        if (key == null)
        {
            throw new ConfigurationException("The attribute 'name' must be specified for element: " + getPropertyName());
        }
        return new Entry(key, context.getText());

    }

    private class Entry implements Map.Entry
    {
        private Object key;
        private Object value;

        public Entry(Object key, Object value)
        {
            this.key = key;
            this.value = value;
        }

        public Object getKey()
        {
            return key;
        }

        public Object getValue()
        {
            return value;
        }

        public Object setValue(Object value)
        {
            this.value = value;
            return this.value;
        }
    }
}
