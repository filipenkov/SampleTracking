/*
 * Created by IntelliJ IDEA.
 * User: owen
 * Date: Jan 23, 2003
 * Time: 3:11:30 PM
 * CVS Revision: $Revision: 1.2 $
 * Last CVS Commit: $Date: 2004/08/03 01:11:46 $
 * Author of last CVS Commit: $Author: mcannon $
 * To change this template use Options | File Templates.
 */
package com.atlassian.configurable;

import com.atlassian.jira.local.ListeningTestCase;
import org.junit.Test;
import static org.junit.Assert.*;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

public class TestXMLObjectConfigurationProperty extends ListeningTestCase
{
    @Test
    public void testConstuctElementNull()
    {
        XMLValuesObjectConfigurationProperty values = new XMLValuesObjectConfigurationProperty("name", "description", "default", 1, (Element) null, null);
        assertTrue(values.isEmpty());
    }

    @Test
    public void testContructElement() throws DocumentException
    {
        Document doc = DocumentHelper.parseText("<values><value><key>key1</key><value>value1</value></value>" +
                "<value><key>key2</key><value>value2</value></value>" +
                "<value><key>key3</key><value>value3</value></value></values>");
        XMLValuesObjectConfigurationProperty values = new XMLValuesObjectConfigurationProperty("name", "description", "default", 1, doc.getRootElement(), null);
        assertEquals("value1", values.get("key1"));
        assertEquals("value2", values.get("key2"));
        assertEquals("value3", values.get("key3"));
        assertTrue(values.isEnabled());
    }

    @Test
    public void testNotEnabled() throws DocumentException
    {
        Document doc = DocumentHelper.parseText("<values><value><key>key1</key><value>value1</value></value>" +
                "<value><key>key2</key><value>value2</value></value>" +
                "<value><key>key3</key><value>value3</value></value></values>");
        XMLValuesObjectConfigurationProperty values = new XMLValuesObjectConfigurationProperty("name", "description", "default", 1, doc.getRootElement(), NotEnabledCondition.class.getName());
        assertFalse(values.isEnabled());
    }
}
