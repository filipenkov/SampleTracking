package com.atlassian.jira.action.admin.export;

import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.local.ListeningTestCase;
import org.dom4j.DocumentException;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import org.xml.sax.SAXException;
import com.atlassian.jira.local.testutils.UtilsForTestSetup;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.List;

public class TestAnonymisingEntityXmlWriter extends ListeningTestCase
{
    static {
        UtilsForTestSetup.loadDatabaseDriver();
    }

    private final List descriptonEntity = EasyList.build(new AnonymousEntity("Issue", "description"));
    private final List issueEntity = EasyList.build(new AnonymousEntity("Issue", null)); //match all fields in an issue
    private final List descriptionField = EasyList.build(new AnonymousEntity(null, "description")); //match all 'description' fields



    @Test
    public void testSingleEntity() throws GenericEntityException, DocumentException, IOException, ParserConfigurationException, SAXException
    {
        GenericValue gv = UtilsForTests.getTestEntity("Issue", EasyMap.build("id", "1001", "description", "abc def"));
        String expectedXMLText = "<Issue id=\"1001\" description=\"xxx xxx\"/>";
        EntityWriterTestUtils.assertExportProducesXML(new AnonymisingEntityXmlWriter(descriptonEntity), gv, expectedXMLText);
    }
    @Test
    public void testSingleEntityIgnoresPunctuation() throws GenericEntityException, DocumentException, IOException, ParserConfigurationException, SAXException
    {
        GenericValue gv = UtilsForTests.getTestEntity("Issue", EasyMap.build("id", "1001", "description", "abc()--def"));
        String expectedXMLText = "<Issue id=\"1001\" description=\"xxx()--xxx\"/>";
        EntityWriterTestUtils.assertExportProducesXML(new AnonymisingEntityXmlWriter(descriptonEntity), gv, expectedXMLText);
    }

    @Test
    public void testSingleEntityIsMatchedWithNullFieldName() throws GenericEntityException, DocumentException, IOException, ParserConfigurationException, SAXException
    {
        GenericValue gv = UtilsForTests.getTestEntity("Issue", EasyMap.build("id", "1001", "key", "TST-001", "description", "abc()--def"));
        String expectedXMLText = "<Issue id=\"xxxx\" key=\"xxx-xxx\" description=\"xxx()--xxx\"/>";
        EntityWriterTestUtils.assertExportProducesXML(new AnonymisingEntityXmlWriter(issueEntity), gv, expectedXMLText);
    }
    @Test
    public void testSingleEntityIsMatchedWithNullEntityName() throws GenericEntityException, DocumentException, IOException, ParserConfigurationException, SAXException
    {
        GenericValue gv = UtilsForTests.getTestEntity("Issue", EasyMap.build("id", "1001", "key", "TST-001", "description", "abc()--def"));
        String expectedXMLText = "<Issue id=\"1001\" key=\"TST-001\" description=\"xxx()--xxx\"/>";
        EntityWriterTestUtils.assertExportProducesXML(new AnonymisingEntityXmlWriter(descriptionField), gv, expectedXMLText);
    }

    @Test
    public void testSingleEntityWithLineBreaks() throws GenericEntityException, DocumentException, IOException, ParserConfigurationException, SAXException
    {
        GenericValue gv = UtilsForTests.getTestEntity("Issue", EasyMap.build("id", "1001", "description", "abc\ndef"));
        //for some reason I can't get dom4j to output XML without keeping formatting - so we need to format this exactly the same.
        String expectedXMLText = "<Issue id=\"1001\">\n        <description><![CDATA[xxx\nxxx]]></description>\n    </Issue>";
        EntityWriterTestUtils.assertExportProducesXML(new AnonymisingEntityXmlWriter(descriptonEntity), gv, expectedXMLText);
    }

    @Test
    public void testAnonymousEntityEqualsAndHashcode()
    {
        AnonymousEntity anonymousEntity1 = new AnonymousEntity("entity1", "field1");
        AnonymousEntity anonymousEntity2 = new AnonymousEntity("entity1", null);
        AnonymousEntity anonymousEntity3 = new AnonymousEntity(null, "field1");
        AnonymousEntity anonymousEntity4 = new AnonymousEntity("entity1", "field1");

        assertEquals(anonymousEntity1, anonymousEntity2);
        assertEquals(anonymousEntity2, anonymousEntity3);
        assertEquals(anonymousEntity3, anonymousEntity4);

        assertEquals(anonymousEntity1.hashCode(), anonymousEntity2.hashCode());
        assertEquals(anonymousEntity2.hashCode(), anonymousEntity3.hashCode());
        assertEquals(anonymousEntity3.hashCode(), anonymousEntity4.hashCode());

    }


}
