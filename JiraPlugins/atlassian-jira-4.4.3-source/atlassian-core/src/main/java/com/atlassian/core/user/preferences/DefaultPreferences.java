/*
 * Atlassian Source Code Template.
 * User: owen
 * Date: Oct 15, 2002
 * Time: 11:06:58 AM
 * CVS Revision: $Revision: 1.10 $
 * Last CVS Commit: $Date: 2003/10/20 04:53:30 $
 * Author of last CVS Commit: $Author: amazkovoi $
 */
package com.atlassian.core.user.preferences;

import com.atlassian.core.AtlassianCoreException;
import com.atlassian.core.util.ClassLoaderUtils;
import com.atlassian.core.util.XMLUtils;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.module.propertyset.PropertySetManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.io.InputStream;

public class DefaultPreferences implements Preferences
{
    static Preferences _instance;
    private PropertySet backingPS;

    public DefaultPreferences()
    {
        backingPS = PropertySetManager.getInstance("memory", null);

        InputStream defaults = ClassLoaderUtils.getResourceAsStream("preferences-default.xml", this.getClass());
        try
        {
            DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document xmlDoc = db.parse(defaults);
            Element root = xmlDoc.getDocumentElement();
            NodeList preferences = root.getElementsByTagName("preference");
            for (int i = 0; i < preferences.getLength(); i++)
            {
                Element preference = (Element) preferences.item(i);
                String operation = preference.getAttribute("type");
                if (operation == null)
                    operation = "String";
                String name = XMLUtils.getContainedText(preference, "name");
                String value = XMLUtils.getContainedText(preference, "value");
                if ("String".equals(operation))
                    backingPS.setString(name, value);
                else if ("Long".equals(operation))
                    backingPS.setLong(name, new Long(value).longValue());
                else if ("Boolean".equals(operation))
                    backingPS.setBoolean(name, new Boolean(value).booleanValue());
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        try
        {
            defaults.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public static Preferences getPreferences()
    {
        if (_instance == null)
        {
            _instance = new DefaultPreferences();
        }

        return _instance;
    }

    public long getLong(String key)
    {
        return backingPS.getLong(key);
    }

    public void setLong(String key, long value) throws AtlassianCoreException
    {
        throw new AtlassianCoreException("Trying to set a Default preference this is not allowed");
    }

    public String getString(String key)
    {
        return backingPS.getString(key);
    }

    public void setString(String key, String value) throws AtlassianCoreException
    {
        throw new AtlassianCoreException("Trying to set a Default preference this is not allowed");
    }

    public boolean getBoolean(String key)
    {
        return backingPS.getBoolean(key);
    }

    public void setBoolean(String key, boolean b) throws AtlassianCoreException
    {
        throw new AtlassianCoreException("Trying to set a Default preference this is not allowed");
    }

    public void remove(String key) throws AtlassianCoreException
    {
        throw new AtlassianCoreException("Trying to set a Default preference this is not allowed");
    }
}
