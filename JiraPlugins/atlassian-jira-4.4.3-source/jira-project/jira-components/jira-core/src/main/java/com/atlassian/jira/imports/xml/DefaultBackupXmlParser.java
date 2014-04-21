package com.atlassian.jira.imports.xml;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.util.XmlReader;
import com.atlassian.jira.util.xml.JiraFileInputStream;
import com.atlassian.jira.util.xml.XMLCleaningReader;
import org.apache.log4j.Logger;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * @since v3.13
 */
public class DefaultBackupXmlParser implements BackupXmlParser
{
    private static final Logger log = Logger.getLogger(DefaultBackupXmlParser.class);
    private final ApplicationProperties applicationProperties;

    public DefaultBackupXmlParser(final ApplicationProperties applicationProperties)
    {
        this.applicationProperties = applicationProperties;
    }

    public void parseBackupXml(final String fileName, final DefaultHandler handler) throws IOException, SAXException
    {
        final SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser saxParser;
        try
        {
            saxParser = factory.newSAXParser();
        }
        catch (final ParserConfigurationException e)
        {
            // This is such a bad exception that we just want to throw it all the way out to the 500 page
            throw new RuntimeException(e);
        }
        final InputSource inputSource = getInputSource(fileName);

        log.debug("Start parsing XML with SAX Parser");
        saxParser.parse(inputSource, handler);
        log.debug("XML successfully parsed");

    }

    private InputSource getInputSource(final String fileName) throws IOException
    {
        // Get an InputStream for the named file.
        final InputStream is = new JiraFileInputStream(new File(fileName));
        // Check if we want to "clean" problematic characters out of the XML
        if (cleanXml())
        {
            final Reader reader = getFilteredReader(is);
            return new InputSource(reader);
        }
        else
        {
            return new InputSource(is);
        }
    }

    private Reader getFilteredReader(final InputStream is) throws IOException
    {
        // TODO: Could be an easier way to get Encoding, and should be able to pass "is", instead of xmlReader.getInputStream()
        // Create an XmlReader, just so we can get the encoding.
        final XmlReader xmlReader = XmlReader.createReader(is);
        return new XMLCleaningReader(new InputStreamReader(xmlReader.getInputStream(), xmlReader.getEncoding()));
    }

    private boolean cleanXml()
    {
        return applicationProperties.getOption(APKeys.JIRA_IMPORT_CLEAN_XML);
    }

}
