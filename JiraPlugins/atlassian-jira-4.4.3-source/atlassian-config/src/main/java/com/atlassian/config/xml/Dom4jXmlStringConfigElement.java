package com.atlassian.config.xml;

import com.atlassian.config.AbstractConfigElement;
import com.atlassian.config.ConfigurationException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;

/**
 * Created by IntelliJ IDEA.
 * User: ROSS
 * Date: 16/03/2004
 * Time: 10:49:24
 * To change this template use File | Settings | File Templates.
 */
public class Dom4jXmlStringConfigElement extends AbstractConfigElement
{
    protected Element context;
    protected boolean useCData = true;

    public Dom4jXmlStringConfigElement(String name, Element context, AbstractDom4jXmlConfigurationPersister config)
    {
        super(name, context, config);
        this.useCData = ((AbstractDom4jXmlConfigurationPersister) getConfiguration()).isUseCData();
    }


    public Class getObjectClass()
    {
        return String.class;
    }

    public void saveConfig(Object object) throws ConfigurationException
    {
        Element element = getOrMakeElement(getPropertyName());
        if (useCData)
        {
            element.addCDATA((String) object);
        }
        else
        {
            element.setText((String) object);
        }
    }

    public Object loadConfig() throws ConfigurationException
    {
        //"/" + context.getName()+ "/" + getPropertyName()
        Node n = context.selectSingleNode(getPropertyName());
        if (n != null)
        {
            return n.getText();
        }
        else
        {
            return context.getText();
        }
    }

    protected Element getOrMakeElement(String path)
    {
        Element element = (Element) context.selectSingleNode(getPropertyName());
        if (element == null)
        {
            element = DocumentHelper.makeElement(context, getPropertyName());
        }
        else
        {
            element.clearContent();
        }
        return element;
    }

    public Object getContext()
    {
        return context;
    }

    public void setContext(Object context)
    {
        if (context instanceof Element)
        {
            this.context = (Element) context;
        }
        else
        {
            throw new IllegalArgumentException("Context must be a: " + Element.class.getName());
        }
    }

    public boolean isUseCData()
    {
        return useCData;
    }
}
