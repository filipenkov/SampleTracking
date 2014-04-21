package com.atlassian.jira.upgrade;

import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.core.util.ClassLoaderUtils;
import com.atlassian.jira.JiraTestUtil;
import com.atlassian.jira.upgrade.util.SearchRequestConverter;
import com.atlassian.jira.upgrade.util.XsltSearchRequestTransformer;
import com.atlassian.jira.local.ListeningTestCase;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class TestUpgradeTask_Build96 extends ListeningTestCase
{
    private static final String TEST_FILE = JiraTestUtil.TESTS_BASE + "/upgrade/upgradetask96_before.xml";
    private static final String NEW_FILE = JiraTestUtil.TESTS_BASE + "/upgrade/upgradetask96_after.xml";

    @Test
    public void testSearchRequestUpgrade() throws Exception
    {
        SearchRequestConverter converter = new XsltSearchRequestTransformer("upgrade_build96_searchrequest.xsl");
        Document document = converter.process(_readDocument(TEST_FILE));
        assertNotNull(document);
        Document newDoc = _readDocument(NEW_FILE);
        assertTrue((StringUtils.replace(_getDocumentAsString(document), " ", "")).equals(StringUtils.replace(_getDocumentAsString(newDoc), " ", "")));
    }

    private Document _readDocument(String xml) throws DocumentException, FileNotFoundException
    {
        SAXReader xmlReader = new SAXReader();
        InputStream is = ClassLoaderUtils.getResourceAsStream(xml, this.getClass());
        return xmlReader.read(is);
    }

    private String _getDocumentAsString(Document result) throws IOException
    {
        return result.asXML();
    }
}
