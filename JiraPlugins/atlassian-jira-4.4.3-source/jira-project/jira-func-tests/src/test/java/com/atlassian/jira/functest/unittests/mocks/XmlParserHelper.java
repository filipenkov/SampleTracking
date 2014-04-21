package com.atlassian.jira.functest.unittests.mocks;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;

/**
 * A helper class to parse and create XML objects
 *
 * @since v3.13
 */
public class XmlParserHelper
{
    public static Document parseXml(String xmlText) throws ParserConfigurationException, IOException, SAXException
    {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(false);

        InputSource inputSource = new InputSource(new StringReader(xmlText));
        // Create the builder and parse the file
        return factory.newDocumentBuilder().parse(inputSource);
    }
}
