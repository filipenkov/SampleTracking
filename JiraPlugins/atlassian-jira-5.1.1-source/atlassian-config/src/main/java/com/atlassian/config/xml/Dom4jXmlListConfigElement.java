package com.atlassian.config.xml;

import com.atlassian.config.ConfigurationException;
import org.dom4j.Element;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: ROSS
 * Date: 17/03/2004
 * Time: 10:35:52
 * To change this template use File | Settings | File Templates.
 */
public class Dom4jXmlListConfigElement extends Dom4jXmlStringConfigElement
{
    public Dom4jXmlListConfigElement(String name, Element context, AbstractDom4jXmlConfigurationPersister config)
    {
        super(name, context, config);
    }

    public Class getObjectClass()
    {
        return List.class;
    }

    public void saveConfig(Object object) throws ConfigurationException
    {
        List list = (List) object;
        Element listElement = getOrMakeElement(getPropertyName());
        listElement.clearContent();

        Element itemElement;
        String item;
        for (Iterator iterator = list.iterator(); iterator.hasNext();)
        {
            item = (String) iterator.next();
            itemElement = listElement.addElement("item");

            if (useCData)
            {
                itemElement.addCDATA(item);
            }
            else
            {
                itemElement.setText(item);
            }
        }
    }

    public Object loadConfig() throws ConfigurationException
    {
        Element element = (Element) context.selectSingleNode(getPropertyName());
        String item;
        List list = new ArrayList();

        for (Iterator iterator = element.elementIterator(); iterator.hasNext();)
        {
            item = (String) getConfiguration().getConfigElement(String.class, "item", (Element) iterator.next());
            list.add(item);
        }
        return list;
    }
}
