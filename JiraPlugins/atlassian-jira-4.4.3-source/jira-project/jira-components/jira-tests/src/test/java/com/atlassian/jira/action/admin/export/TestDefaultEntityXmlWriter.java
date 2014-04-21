package com.atlassian.jira.action.admin.export;

import com.atlassian.jira.local.ListeningTestCase;
import org.junit.Test;
import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.util.map.EasyMap;
import org.dom4j.DocumentException;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import org.xml.sax.SAXException;
import com.atlassian.jira.local.testutils.UtilsForTestSetup;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

public class TestDefaultEntityXmlWriter extends ListeningTestCase
{
    static {
        UtilsForTestSetup.loadDatabaseDriver();
    }


    @Test
    public void testSingleEntity() throws GenericEntityException, DocumentException, IOException, ParserConfigurationException, SAXException
    {
        GenericValue gv = UtilsForTests.getTestEntity("Issue", EasyMap.build("id", "1001", "key", "TST-10"));
        String expectedXMLText = "<Issue id=\"1001\" key=\"TST-10\"/>";
        EntityWriterTestUtils.assertExportProducesXML(new DefaultEntityXmlWriter(), gv, expectedXMLText);
    }

    @Test
    public void testSingleEntityWithLineBreaks() throws GenericEntityException, DocumentException, IOException, ParserConfigurationException, SAXException
    {
        GenericValue gv = UtilsForTests.getTestEntity("Issue", EasyMap.build("id", "1001", "description", "abc\ndef"));
        //for some reason I can't get dom4j to output XML without keeping formatting - so we need to format this exactly the same.
        String expectedXMLText = "<Issue id=\"1001\">\n        <description><![CDATA[abc\ndef]]></description>\n    </Issue>";
        EntityWriterTestUtils.assertExportProducesXML(new DefaultEntityXmlWriter(), gv, expectedXMLText);
    }
    

}
