package com.atlassian.jira.imports.xml;

import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.imports.project.handler.ChainedSaxHandler;
import com.atlassian.jira.imports.project.handler.ImportEntityHandler;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;
import com.atlassian.jira.local.ListeningTestCase;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;

/**
 * @since v3.13
 */
public class TestDefaultBackupXmlParser extends ListeningTestCase
{
    public static final String FS = File.separator;

    @Test
    public void testParseBackupXml() throws IOException, ParserConfigurationException, SAXException
    {
        Mock mockApplicationProperties = new Mock(ApplicationProperties.class);
        mockApplicationProperties.expectAndReturn("getOption", P.args(P.eq(APKeys.JIRA_IMPORT_CLEAN_XML)), Boolean.FALSE);
        DefaultBackupXmlParser parser = new DefaultBackupXmlParser((ApplicationProperties) mockApplicationProperties.proxy());

        final Map entityMap = new HashMap();
        ChainedSaxHandler handler = new ChainedSaxHandler();
        handler.registerHandler(new ImportEntityHandler()
        {
            public void handleEntity(String entityName, Map attributes) throws ParseException
            {
                entityMap.put(entityName, attributes);
            }

            public void startDocument()
            {
            }

            public void endDocument()
            {
            }
        });

        parser.parseBackupXml(getFilePath("TestBackupParser.xml"), handler);

        // Verify the stuff in our map
        assertEquals(3, entityMap.size());
        assertTrue(entityMap.containsKey("Action"));
        assertTrue(entityMap.containsKey("Issue"));
        assertTrue(entityMap.containsKey("Project"));
        assertEquals(7, ((Map) entityMap.get("Action")).size());
        assertEquals(16, ((Map) entityMap.get("Issue")).size());
        assertEquals(3, handler.getEntityCount());
    }

    @Test
    public void testParseBackupZip() throws IOException, ParserConfigurationException, SAXException
    {
        Mock mockApplicationProperties = new Mock(ApplicationProperties.class);
        mockApplicationProperties.expectAndReturn("getOption", P.args(P.eq(APKeys.JIRA_IMPORT_CLEAN_XML)), Boolean.FALSE);
        DefaultBackupXmlParser parser = new DefaultBackupXmlParser((ApplicationProperties) mockApplicationProperties.proxy());

        final Map entityMap = new HashMap();
        ChainedSaxHandler handler = new ChainedSaxHandler();
        handler.registerHandler(new ImportEntityHandler()
        {
            public void handleEntity(String entityName, Map attributes) throws ParseException
            {
                entityMap.put(entityName, attributes);
            }
            public void startDocument()
            {
            }

            public void endDocument()
            {
            }

        });

        parser.parseBackupXml(getFilePath("TestBackupParser.zip"), handler);

        // Verify the stuff in our map
        assertEquals(3, entityMap.size());
        assertTrue(entityMap.containsKey("Action"));
        assertTrue(entityMap.containsKey("Issue"));
        assertTrue(entityMap.containsKey("Project"));
        assertEquals(7, ((Map) entityMap.get("Action")).size());
        assertEquals(16, ((Map) entityMap.get("Issue")).size());
        assertEquals(3, handler.getEntityCount());
    }

    @Test
    public void testSaxExceptionThrown() throws IOException, ParserConfigurationException
    {
        Mock mockApplicationProperties = new Mock(ApplicationProperties.class);
        mockApplicationProperties.expectAndReturn("getOption", P.args(P.eq(APKeys.JIRA_IMPORT_CLEAN_XML)), Boolean.FALSE);
        DefaultBackupXmlParser parser = new DefaultBackupXmlParser((ApplicationProperties) mockApplicationProperties.proxy());

        ChainedSaxHandler handler = new ChainedSaxHandler();
        handler.registerHandler(new ImportEntityHandler()
        {
            public void handleEntity(String entityName, Map<String, String> attributes) throws ParseException
            {
                throw new ParseException("F**k off");
            }
            public void startDocument()
            {
            }

            public void endDocument()
            {
            }
        });

        try
        {
            parser.parseBackupXml(getFilePath("TestBackupParser.zip"), handler);
        }
        catch (SAXException e)
        {
            // expected
        }
    }

    @Test
    public void testFileNotFound() throws ParserConfigurationException, SAXException, IOException
    {
        DefaultBackupXmlParser parser = new DefaultBackupXmlParser(null);
        try
        {
            parser.parseBackupXml("/iamafilethatwillneverexist", null);
            fail();
        }
        catch (FileNotFoundException e)
        {
            // expected
        }
    }

    private String getFilePath(String fileName)
    {
        return new File(this.getClass().getResource("/" + this.getClass().getName().replace('.', '/') + ".class").getFile()).getParent() + "/" + fileName;
    }
}
