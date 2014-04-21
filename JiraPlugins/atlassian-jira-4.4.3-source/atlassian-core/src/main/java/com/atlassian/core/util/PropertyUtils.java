/*
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: 28/02/2002
 * Time: 11:48:12
 * To change template for new class use
 * Code Style | Class Templates options (Tools | IDE Options).
 */
package com.atlassian.core.util;

import com.opensymphony.module.propertyset.PropertySet;
import org.apache.log4j.Category;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;
import java.io.*;

public class PropertyUtils
{
    private static final Logger log = Logger.getLogger(PropertyUtils.class);

    public static Properties getProperties(String resource, Class callingClass)
    {
        return getPropertiesFromStream(ClassLoaderUtils.getResourceAsStream(resource, callingClass));
    }

    public static Properties getPropertiesFromFile(File file)
    {
        try
        {
            return getPropertiesFromStream(new FileInputStream(file));
        }
        catch (FileNotFoundException e)
        {
            log.error("Error loading properties from file: " + file.getPath() + ". File does not exist.", e);
            return null;
        }
    }

    public static Properties getPropertiesFromStream(InputStream is)
    {
        if (is == null)
            return null;

        Properties props = new Properties();
        try
        {
            props.load(is);
        }
        catch (IOException e)
        {
            log.error("Error loading properties from stream.", e);
        }
        finally
        {
            FileUtils.shutdownStream(is);
        }

        return props;
    }


    /**
     * Check to see if the two propertySet contain the same values and types
     * NOTE If both PropertySets are null then <i>true</i> is returned
     * @param pThis First PropertySet
     * @param pThat Second PropertySet
     * @return Are the two PropertySets identical
     */
    public static boolean identical(PropertySet pThis, PropertySet pThat)
    {
        //Check to see if both of the collections are null
        if (pThis == null && pThat == null)
            return true;

        //Check to see if either of the collections are null
        if (pThis == null || pThat == null)
            return false;

        Collection thisKeys = pThis.getKeys();
        Collection thatKeys = pThat.getKeys();

        if (!thisKeys.containsAll(thatKeys) || !thatKeys.containsAll(thisKeys))
            return false;

        Iterator thisKeysIterator = thisKeys.iterator();
        String key;
        int keyType;
        while (thisKeysIterator.hasNext())
        {
            key = (String) thisKeysIterator.next();
            keyType = pThis.getType(key);
            if (PropertySet.BOOLEAN == keyType)
            {
                if (pThis.getBoolean(key) != pThat.getBoolean(key))
                    return false;
            }
            else if (PropertySet.DATA == keyType)
            {
                throw new IllegalArgumentException("DATA Comparision has not been implemented in PropertyUtil");
            }
            else if (PropertySet.DATE == keyType)
            {
                if (!pThis.getDate(key).equals(pThat.getDate(key)))
                    return false;
            }
            else if (PropertySet.DOUBLE == keyType)
            {
                if (pThis.getDouble(key) != pThat.getDouble(key))
                    return false;
            }
            else if (PropertySet.INT == keyType)
            {
                if (pThis.getInt(key) != pThat.getInt(key))
                    return false;
            }
            else if (PropertySet.OBJECT == keyType)
            {
                throw new IllegalArgumentException("OBJECT Comparision has not been implemented in PropertyUtil");
            }
            else if (PropertySet.PROPERTIES == keyType)
            {
                throw new IllegalArgumentException("PROPERTIES Comparision has not been implemented in PropertyUtil");
            }
            else if (PropertySet.LONG == keyType)
            {
                if (pThis.getLong(key) != pThat.getLong(key))
                    return false;
            }
            else if (PropertySet.STRING == keyType)
            {
                if (!pThis.getString(key).equals(pThat.getString(key)))
                    return false;
            }
            else if (PropertySet.TEXT == keyType)
            {
                if (!pThis.getText(key).equals(pThat.getText(key)))
                    return false;
            }
            else if (PropertySet.XML == keyType)
            {
                throw new IllegalArgumentException("XML Comparision has not been implemented in PropertyUtil");
            }
        }

        return true;
    }

}
