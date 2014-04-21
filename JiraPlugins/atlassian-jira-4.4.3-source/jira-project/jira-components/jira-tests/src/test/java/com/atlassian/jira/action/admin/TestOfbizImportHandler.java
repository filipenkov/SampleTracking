package com.atlassian.jira.action.admin;

import com.atlassian.jira.local.MockControllerTestCase;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import static com.atlassian.jira.action.admin.OfbizImportHandler.OSPROPERTY_ENTRY;
import static com.atlassian.jira.action.admin.OfbizImportHandler.OSPROPERTY_STRING;
import static com.atlassian.jira.action.admin.OfbizImportHandler.OSPROPERTY_TEXT;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.license.LicenseStringFactory;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import org.xml.sax.Attributes;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.TreeMap;

/**
 * Copyright All Rights Reserved. Created: christo 16/10/2006 16:39:14
 */
public class TestOfbizImportHandler extends MockControllerTestCase
{
    private LicenseStringFactory licenseStringFactory;
    private OfbizImportHandler handler;
    private static final String PROPERTY_KEY = "propertyKey";
    private static final String VALUE_NAME = "value";
    private static final String LIC_MESSAGE = "LIC_MESSAGE";
    private static final String LIC_HASH = "LIC_HASH";
    private static final String LIC_HASH_NEVER = "EVER";
    private static final String LIC_MESSAGE_NEVER = "NEVER";
    private static final String LIC_STRING = "LIC_STRING";

    private static final String ID = "666";
    private static final String ID2 = "664";
    private static final String ID3 = "665";
    private static final String ID4 = "667";
    private static final String ID5 = "999";
    private static final String BUILD_NUMBER = "12345";
    private static final String INDEX_PATH = "67789";
    private static final String ATTACHMENT_PATH = "88333";

    @Before
    public void setUp() throws Exception
    {
        licenseStringFactory = getMock(LicenseStringFactory.class);
    }

    @Test
    public void testBuildNumberNotPresentInXml() throws Exception
    {
        handler = instantiate(OfbizImportHandler.class);

        handler.createBuildNumber();
        assertNull(handler.getBuildNumber());
    }

    @Test
    public void testBuildPresentInXml()
    {
        handler = instantiate(OfbizImportHandler.class);

        // faking SAX events...
        handler.recordElementsInfo(OSPROPERTY_ENTRY, saxAttr(ID, PROPERTY_KEY, APKeys.JIRA_PATCHED_VERSION));
        handler.recordElementsInfo(OSPROPERTY_STRING, saxAttr(ID, VALUE_NAME, BUILD_NUMBER));

        handler.createBuildNumber();
        assertEquals(BUILD_NUMBER, handler.getBuildNumber());
    }

    @Test
    public void testIndexPathInXml()
    {
        handler = instantiate(OfbizImportHandler.class);

        // faking SAX events...
        handler.recordElementsInfo(OSPROPERTY_ENTRY, saxAttr(ID, PROPERTY_KEY, APKeys.JIRA_PATH_INDEX));
        handler.recordElementsInfo(OSPROPERTY_STRING, saxAttr(ID, VALUE_NAME, INDEX_PATH));

        handler.createIndexPath();

        assertEquals(INDEX_PATH, handler.getIndexPath());
    }

    @Test
    public void testIndexPathNotInXml()
    {
        handler = instantiate(OfbizImportHandler.class);

        handler.createIndexPath();

        assertNull(handler.getIndexPath());
    }

    @Test
    public void testAttachmentPathInXml()
    {
        handler = instantiate(OfbizImportHandler.class);

        // faking SAX events...
        handler.recordElementsInfo(OSPROPERTY_ENTRY, saxAttr(ID, PROPERTY_KEY, APKeys.JIRA_PATH_ATTACHMENTS));
        handler.recordElementsInfo(OSPROPERTY_STRING, saxAttr(ID, VALUE_NAME, ATTACHMENT_PATH));

        handler.createAttachmentPath();

        assertEquals(ATTACHMENT_PATH, handler.getAttachmentPath());
    }

    @Test
    public void testAttachmentPathNotInXml()
    {
        handler = instantiate(OfbizImportHandler.class);
                                                                                                   
        handler.createAttachmentPath();

        assertNull(handler.getAttachmentPath());
    }

    @Test
    public void testNewLicenseKeyIsDetected() throws Exception
    {
        handler = instantiate(OfbizImportHandler.class);

        // faking SAX events...
        handler.recordElementsInfo(OSPROPERTY_ENTRY, saxAttr(ID, PROPERTY_KEY, APKeys.JIRA_LICENSE));
        handler.recordElementsInfo(OSPROPERTY_TEXT, saxAttr(ID, VALUE_NAME, LIC_STRING));

        handler.createLicenseString();
        assertEquals(LIC_STRING, handler.getLicenseString());
    }

    @Test
    public void testOldLicenseKeyIsDetected() throws Exception
    {
        expect(licenseStringFactory.create(LIC_MESSAGE, LIC_HASH)).andReturn(LIC_STRING);

        handler = instantiate(OfbizImportHandler.class);

        // faking SAX events...
        handler.recordElementsInfo(OSPROPERTY_ENTRY, saxAttr(ID, PROPERTY_KEY, APKeys.JIRA_LICENSE_V1_HASH));
        handler.recordElementsInfo(OSPROPERTY_TEXT, saxAttr(ID, VALUE_NAME, LIC_HASH));

        handler.recordElementsInfo(OSPROPERTY_ENTRY, saxAttr(ID2, PROPERTY_KEY, APKeys.JIRA_LICENSE_V1_MESSAGE));
        handler.recordElementsInfo(OSPROPERTY_TEXT, saxAttr(ID2, VALUE_NAME, LIC_MESSAGE));


        handler.createLicenseString();
        assertEquals(LIC_STRING, handler.getLicenseString());
    }

    @Test
    public void testReallyOldLicenseKeyIsDetected() throws Exception
    {
        expect(licenseStringFactory.create(LIC_MESSAGE, LIC_HASH)).andReturn(LIC_STRING);

        handler = instantiate(OfbizImportHandler.class);

        // faking SAX events...
        handler.recordElementsInfo(OSPROPERTY_ENTRY, saxAttr(ID, PROPERTY_KEY, APKeys.JIRA_OLD_LICENSE_V1_HASH));
        handler.recordElementsInfo(OSPROPERTY_STRING, saxAttr(ID, VALUE_NAME, LIC_HASH));

        handler.recordElementsInfo(OSPROPERTY_ENTRY, saxAttr(ID2, PROPERTY_KEY, APKeys.JIRA_OLD_LICENSE_V1_MESSAGE));
        handler.recordElementsInfo(OSPROPERTY_STRING, saxAttr(ID2, VALUE_NAME, LIC_MESSAGE));


        handler.createLicenseString();
        assertEquals(LIC_STRING, handler.getLicenseString());
    }

    @Test
    public void testThatOldLicensesTakePrecedenceOverRealyOld() throws Exception
    {
        expect(licenseStringFactory.create(LIC_MESSAGE, LIC_HASH)).andReturn(LIC_STRING);

        handler = instantiate(OfbizImportHandler.class);

        // faking SAX events...
        handler.recordElementsInfo(OSPROPERTY_ENTRY, saxAttr(ID, PROPERTY_KEY, APKeys.JIRA_OLD_LICENSE_V1_HASH));
        handler.recordElementsInfo(OSPROPERTY_STRING, saxAttr(ID, VALUE_NAME, LIC_HASH_NEVER));

        handler.recordElementsInfo(OSPROPERTY_ENTRY, saxAttr(ID2, PROPERTY_KEY, APKeys.JIRA_OLD_LICENSE_V1_MESSAGE));
        handler.recordElementsInfo(OSPROPERTY_STRING, saxAttr(ID2, VALUE_NAME, LIC_MESSAGE_NEVER));


        handler.recordElementsInfo(OSPROPERTY_ENTRY, saxAttr(ID3, PROPERTY_KEY, APKeys.JIRA_LICENSE_V1_HASH));
        handler.recordElementsInfo(OSPROPERTY_TEXT, saxAttr(ID3, VALUE_NAME, LIC_HASH));

        handler.recordElementsInfo(OSPROPERTY_ENTRY, saxAttr(ID4, PROPERTY_KEY, APKeys.JIRA_LICENSE_V1_MESSAGE));
        handler.recordElementsInfo(OSPROPERTY_TEXT, saxAttr(ID4, VALUE_NAME, LIC_MESSAGE));

        handler.createLicenseString();
        assertEquals(LIC_STRING, handler.getLicenseString());
    }

    @Test
    public void testThatNewLicensesTakePrecedence() throws Exception
    {
        handler = instantiate(OfbizImportHandler.class);

        handler.recordElementsInfo(OSPROPERTY_ENTRY, saxAttr(ID, PROPERTY_KEY, APKeys.JIRA_OLD_LICENSE_V1_HASH));
        handler.recordElementsInfo(OSPROPERTY_STRING, saxAttr(ID, VALUE_NAME, LIC_HASH_NEVER));

        handler.recordElementsInfo(OSPROPERTY_ENTRY, saxAttr(ID2, PROPERTY_KEY, APKeys.JIRA_OLD_LICENSE_V1_MESSAGE));
        handler.recordElementsInfo(OSPROPERTY_STRING, saxAttr(ID2, VALUE_NAME, LIC_MESSAGE_NEVER));


        handler.recordElementsInfo(OSPROPERTY_ENTRY, saxAttr(ID3, PROPERTY_KEY, APKeys.JIRA_LICENSE_V1_HASH));
        handler.recordElementsInfo(OSPROPERTY_TEXT, saxAttr(ID3, VALUE_NAME, LIC_HASH));

        handler.recordElementsInfo(OSPROPERTY_ENTRY, saxAttr(ID4, PROPERTY_KEY, APKeys.JIRA_LICENSE_V1_MESSAGE));
        handler.recordElementsInfo(OSPROPERTY_TEXT, saxAttr(ID4, VALUE_NAME, LIC_MESSAGE));

        handler.recordElementsInfo(OSPROPERTY_ENTRY, saxAttr(ID5, PROPERTY_KEY, APKeys.JIRA_LICENSE));
        handler.recordElementsInfo(OSPROPERTY_TEXT, saxAttr(ID5, VALUE_NAME, LIC_STRING));

        handler.createLicenseString();
        assertEquals(LIC_STRING, handler.getLicenseString());
    }

    @Test
    public void testWhenThereIsNoLicensePresentAtAll() throws Exception
    {
        handler = instantiate(OfbizImportHandler.class);

        handler.createLicenseString();
        assertNull(handler.getLicenseString());
    }

    @Test
    public void testCreateZeroDeadlock() throws Exception
    {
        GenericValue gv = getMock(GenericValue.class);
        expect(gv.create()).andReturn(gv);

        handler = instantiate(OfbizImportHandler.class);

        handler.createWithDeadlockRetry(gv);
        verify();
    }

    @Test
    public void testCreateOneDeadlock() throws Exception
    {
        GenericValue gv = getMock(GenericValue.class);
        expect(gv.create()).andThrow(getSQLDeadlockException());
        expect(gv.create()).andReturn(gv);

        handler = instantiate(OfbizImportHandler.class);

        handler.createWithDeadlockRetry(gv);
        verify();
    }

    @Test
    public void testCreateThreeDeadlocks() throws Exception
    {
        GenericValue gv = getMock(GenericValue.class);
        expect(gv.create()).andThrow(getSQLDeadlockException()).times(3);
        expect(gv.create()).andReturn(gv);

        handler = instantiate(OfbizImportHandler.class);

        handler.createWithDeadlockRetry(gv);
        verify();
    }

    @Test
    public void testCreateFourDeadlocks() throws Exception
    {
        GenericValue gv = getMock(GenericValue.class);
        expect(gv.create()).andThrow(getSQLDeadlockException()).times(4);

        handler = instantiate(OfbizImportHandler.class);

        try
        {
            handler.createWithDeadlockRetry(gv);
            fail("This should have thrown an error");
        }
        catch (GenericEntityException e)
        {
            // Expected
        }
        verify();
    }

    @Test
    public void testCreateDeeplyNestedDeadlocks() throws Exception
    {
        GenericValue gv = getMock(GenericValue.class);
        expect(gv.create()).andThrow(getSQLDeadlockException()).times(1);
        expect(gv.create()).andThrow(getSQLDeadlockNestedException()).times(2);
        expect(gv.create()).andReturn(gv);

        handler = instantiate(OfbizImportHandler.class);

        handler.createWithDeadlockRetry(gv);
        verify();
    }

    private Exception getSQLDeadlockException()
    {
        SQLException sqlEx = new SQLException("Dummy Deadlock", "40001");
        return new GenericEntityException(sqlEx.getMessage(), sqlEx);
    }

    private Exception getSQLDeadlockNestedException()
    {
        SQLException sqlEx = new SQLException("Dummy Deadlock", "40001");
        Exception ex2 = new GenericEntityException(sqlEx.getMessage(), sqlEx);
        Exception ex3 = new GenericEntityException(sqlEx.getMessage(), ex2);
        return new GenericEntityException(sqlEx.getMessage(), ex3);
    }

    private MockAttributes saxAttr(final String id, final String...keyAndValuePairs)
    {
        MockAttributes entryAttr = new MockAttributes();
        entryAttr.put("id", id);

        assertTrue(keyAndValuePairs.length % 2 == 0);
        for (int i = 0; i < keyAndValuePairs.length; i+=2)
        {
            String attrName = keyAndValuePairs[i];
            String attrValue = keyAndValuePairs[i+1];
            entryAttr.put(attrName, attrValue);

        }
        return entryAttr;
    }


    private static class MockAttributes implements Attributes
    {
        private TreeMap map = new TreeMap();

        public void put(String key, String value)
        {
            map.put(key, value);
        }

        public int getIndex(String qName)
        {
            throw new UnsupportedOperationException("what?");
        }

        public int getIndex(String uri, String localName)
        {
            throw new UnsupportedOperationException("what?");
        }

        public int getLength()
        {
            return map.size();
        }

        public String getLocalName(int index)
        {
            throw new UnsupportedOperationException("what?");
        }

        public String getQName(int index)
        {
            throw new UnsupportedOperationException("what?");
        }

        public String getType(int index)
        {
            throw new UnsupportedOperationException("what?");
        }

        public String getType(String qName)
        {
            throw new UnsupportedOperationException("what?");
        }

        public String getType(String uri, String localName)
        {
            throw new UnsupportedOperationException("what?");
        }

        public String getURI(int index)
        {
            throw new UnsupportedOperationException("what?");
        }

        public String getValue(int index)
        {
            return (String) new ArrayList(map.keySet()).get(index);
        }

        public String getValue(String qName)
        {
            return (String) map.get(qName);
        }

        public String getValue(String uri, String localName)
        {
            throw new UnsupportedOperationException("what?");
        }

        public String toString()
        {
            return map.toString();
        }
    }

}
