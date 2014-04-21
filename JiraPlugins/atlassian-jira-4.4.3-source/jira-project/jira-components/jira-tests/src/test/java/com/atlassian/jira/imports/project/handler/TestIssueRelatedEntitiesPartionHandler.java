package com.atlassian.jira.imports.project.handler;

import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.external.beans.ExternalProject;
import com.atlassian.jira.imports.project.core.BackupProject;
import com.atlassian.jira.imports.project.core.BackupProjectImpl;
import com.atlassian.jira.local.ListeningTestCase;
import org.ofbiz.core.entity.model.MockModelEntity;
import org.easymock.MockControl;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.Map;

/**
 * @since v3.13
 */
public class TestIssueRelatedEntitiesPartionHandler extends ListeningTestCase
{
    private static String NL;

    public TestIssueRelatedEntitiesPartionHandler()
    {
        NL = System.getProperty("line.separator");
    }

    @Test
    public void testChangeItemGroupParseExceptions()
    {
        // Set up a mock ChangeGroup
        // <ChangeGroup id="10033" issue="10000" author="admin" created="2008-01-24 16:03:24.325"/>
        final MockModelEntity mockChangeGroupModelEntity = new MockModelEntity("ChangeGroup");
        mockChangeGroupModelEntity.setFieldNames(EasyList.build("id", "issue", "author", "created"));
        // <ChangeItem id="10000" group="10000" fieldtype="jira" field="Project" oldvalue="10000" oldstring="homosapien" newvalue="10001" newstring="monkey"/>
        final MockModelEntity mockChangeItemModelEntity = new MockModelEntity("ChangeItem");
        mockChangeItemModelEntity.setFieldNames(EasyList.build("id", "group", "fieldtype", "field", "oldvalue", "oldstring", "newvalue", "newstring", "xxx"));

        // Create a simple PrintWriter
        StringWriter writer = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(writer);

        // Create a simple PrintWriter
        StringWriter changeItem = new StringWriter();
        final PrintWriter changeItemPrintWriter = new PrintWriter(changeItem);

        BackupProject backupProject = new BackupProjectImpl(new ExternalProject(), Collections.EMPTY_LIST, Collections.EMPTY_LIST,
                Collections.EMPTY_LIST, EasyList.build(new Long(12), new Long(14)));

        // Create our handler
        IssueRelatedEntitiesPartionHandler issueRelatedEntitiesPartionHandler = new IssueRelatedEntitiesPartionHandler(backupProject, printWriter, changeItemPrintWriter, EasyList.build(mockChangeGroupModelEntity, mockChangeItemModelEntity), "UTF-8");
        // Now fire XML parse events at it to handle.
        issueRelatedEntitiesPartionHandler.startDocument();
        try
        {
            issueRelatedEntitiesPartionHandler.handleEntity("ChangeGroup", EasyMap.build("id", "abc", "issue", "12", "author", "dudette", "created", "2009-04-01 12:34:56.789"));
            fail("A parse exception should have been thrown.");
        }
        catch (ParseException e)
        {
            // expected
        }
        // Test a change group with no id
        try
        {
            issueRelatedEntitiesPartionHandler.handleEntity("ChangeGroup", EasyMap.build("issue", "12", "author", "dudette", "created", "2009-04-01 12:34:56.789"));
            fail("A parse exception should have been thrown.");
        }
        catch (ParseException e)
        {
            // expected
        }
        try
        {
            issueRelatedEntitiesPartionHandler.handleEntity("ChangeItem", EasyMap.build("id", "10501", "group", "def", "fieldtype", "jira", "field", "A"));
            fail("A parse exception should have been thrown.");
        }
        catch (ParseException e)
        {
            //expected
        }

    }

    @Test
    public void testNodeAssociationOnly() throws ParseException
    {
        // Set up a mock ModelEntity
        //    <NodeAssociation sourceNodeId="10001" sourceNodeEntity="Issue" sinkNodeId="10002" sinkNodeEntity="Version" associationType="IssueFixVersion"/>
        //    <NodeAssociation sourceNodeId="10001" sourceNodeEntity="Issue" sinkNodeId="10001" sinkNodeEntity="Component" associationType="IssueComponent"/>
        //    <NodeAssociation sourceNodeId="10001" sourceNodeEntity="Issue" sinkNodeId="10001" sinkNodeEntity="Version" associationType="IssueVersion"/>
        final MockModelEntity mockModelEntity = new MockModelEntity("NodeAssociation");
        mockModelEntity.setFieldNames(EasyList.build("sourceNodeId", "sourceNodeEntity", "sinkNodeId", "sinkNodeEntity", "associationType"));

        // Create a simple PrintWriter
        StringWriter writer = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(writer);

        BackupProject backupProject = new BackupProjectImpl(new ExternalProject(), Collections.EMPTY_LIST, Collections.EMPTY_LIST,
                Collections.EMPTY_LIST, EasyList.build(new Long(12), new Long(14)));

        // Create our handler
        IssueRelatedEntitiesPartionHandler issueRelatedEntitiesPartionHandler = new IssueRelatedEntitiesPartionHandler(backupProject, printWriter, null, EasyList.build(mockModelEntity), "UTF-8");
        // Now fire XML parse events at it to handle.
        issueRelatedEntitiesPartionHandler.startDocument();
        issueRelatedEntitiesPartionHandler.handleEntity("Issue", EasyMap.build("id", "12", "key", "MNK-16", "desc", "Stuff happened."));
        // This is a Affects Version node
        issueRelatedEntitiesPartionHandler.handleEntity("NodeAssociation", EasyMap.build("sourceNodeId", "12", "sourceNodeEntity", "Issue", "sinkNodeId", "123", "sinkNodeEntity", "Version", "associationType", "IssueVersion"));
        // Fix version for the same issue
        issueRelatedEntitiesPartionHandler.handleEntity("NodeAssociation", EasyMap.build("sourceNodeId", "12", "sourceNodeEntity", "Issue", "sinkNodeId", "124", "sinkNodeEntity", "Version", "associationType", "IssueFixVersion"));
        // Component for different issue
        issueRelatedEntitiesPartionHandler.handleEntity("NodeAssociation", EasyMap.build("sourceNodeId", "14", "sourceNodeEntity", "Issue", "sinkNodeId", "125", "sinkNodeEntity", "Component", "associationType", "IssueComponent"));
        // This should be ignored, wrong association type
        issueRelatedEntitiesPartionHandler.handleEntity("NodeAssociation", EasyMap.build("sourceNodeId", "14", "sourceNodeEntity", "Issue", "sinkNodeId", "125", "sinkNodeEntity", "Component", "associationType", "WrongStuff"));
        // This should be ignored, wrong sourceEntityType
        issueRelatedEntitiesPartionHandler.handleEntity("NodeAssociation", EasyMap.build("sourceNodeId", "14", "sourceNodeEntity", "WrongType", "sinkNodeId", "125", "sinkNodeEntity", "Component", "associationType", "IssueComponent"));
        // This should be ignored, not related to our issues
        issueRelatedEntitiesPartionHandler.handleEntity("NodeAssociation", EasyMap.build("sourceNodeId", "15", "sourceNodeEntity", "Issue", "sinkNodeId", "125", "sinkNodeEntity", "Component", "associationType", "IssueComponent"));

        issueRelatedEntitiesPartionHandler.endDocument();

        printWriter.close();
        String xml = writer.toString();
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + NL
                     + "<entity-engine-xml>" + NL
                     + "    <NodeAssociation sourceNodeId=\"12\" sourceNodeEntity=\"Issue\" sinkNodeId=\"123\" sinkNodeEntity=\"Version\" associationType=\"IssueVersion\"/>" + NL
                     + "    <NodeAssociation sourceNodeId=\"12\" sourceNodeEntity=\"Issue\" sinkNodeId=\"124\" sinkNodeEntity=\"Version\" associationType=\"IssueFixVersion\"/>" + NL
                     + "    <NodeAssociation sourceNodeId=\"14\" sourceNodeEntity=\"Issue\" sinkNodeId=\"125\" sinkNodeEntity=\"Component\" associationType=\"IssueComponent\"/>" + NL
                     + "</entity-engine-xml>", xml);
        assertEquals(3, issueRelatedEntitiesPartionHandler.getEntityCount());
    }

    @Test
    public void testIssueLinkOnly() throws ParseException
    {
        // Set up a mock ModelEntity
        //     <IssueLink id="10000" linktype="10000" source="10000" destination="10001"/>
        final MockModelEntity mockModelEntity = new MockModelEntity("IssueLink");
        mockModelEntity.setFieldNames(EasyList.build("id", "linktype", "source", "destination"));

        // Create a simple PrintWriter
        StringWriter writer = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(writer);

        BackupProject backupProject = new BackupProjectImpl(new ExternalProject(), Collections.EMPTY_LIST, Collections.EMPTY_LIST,
                Collections.EMPTY_LIST, EasyList.build(new Long(12), new Long(14)));

        // Create our handler
        IssueRelatedEntitiesPartionHandler issueRelatedEntitiesPartionHandler = new IssueRelatedEntitiesPartionHandler(backupProject, printWriter, null, EasyList.build(mockModelEntity), "UTF-8");
        // Now fire XML parse events at it to handle.
        issueRelatedEntitiesPartionHandler.startDocument();
        issueRelatedEntitiesPartionHandler.handleEntity("Issue", EasyMap.build("id", "12", "key", "MNK-16", "desc", "Stuff happened."));
        // For this link neither issue is in the project
        issueRelatedEntitiesPartionHandler.handleEntity("IssueLink", EasyMap.build("id", "10000", "linktype", "10000", "source", "10", "destination", "16"));
        // Source is in the project.
        issueRelatedEntitiesPartionHandler.handleEntity("IssueLink", EasyMap.build("id", "10001", "linktype", "10000", "source", "12", "destination", "10"));
        // Destination in the project
        issueRelatedEntitiesPartionHandler.handleEntity("IssueLink", EasyMap.build("id", "10002", "linktype", "10002", "source", "44", "destination", "14"));
        // Both source and destination in the project.
        issueRelatedEntitiesPartionHandler.handleEntity("IssueLink", EasyMap.build("id", "10003", "linktype", "10002", "source", "12", "destination", "14"));
        issueRelatedEntitiesPartionHandler.endDocument();
        
        printWriter.close();
        String xml = writer.toString();
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + NL
                     + "<entity-engine-xml>" + NL
                     + "    <IssueLink id=\"10001\" linktype=\"10000\" source=\"12\" destination=\"10\"/>" + NL
                     + "    <IssueLink id=\"10002\" linktype=\"10002\" source=\"44\" destination=\"14\"/>" + NL
                     + "    <IssueLink id=\"10003\" linktype=\"10002\" source=\"12\" destination=\"14\"/>" + NL
                     + "</entity-engine-xml>", xml);
     }

    @Test
    public void testCustomFieldValue() throws ParseException
    {
        //     <CustomFieldValue id="10000" issue="10010" customfield="10000" stringvalue="Future"/>
        final MockModelEntity mockModelEntity = new MockModelEntity("CustomFieldValue");
        mockModelEntity.setFieldNames(EasyList.build("id", "issue", "customfield", "stringvalue"));

        StringWriter writer = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(writer);

        final MockControl mockBackupProjectControl = MockControl.createStrictControl(BackupProject.class);
        final BackupProject mockBackupProject = (BackupProject) mockBackupProjectControl.getMock();
        mockBackupProject.containsIssue("10");
        mockBackupProjectControl.setReturnValue(false);
        mockBackupProject.containsIssue("12");
        mockBackupProjectControl.setReturnValue(true);
        mockBackupProject.containsIssue("14");
        mockBackupProjectControl.setReturnValue(true);
        mockBackupProject.containsIssue("16");
        mockBackupProjectControl.setReturnValue(false);
        mockBackupProjectControl.replay();

        IssueRelatedEntitiesPartionHandler customFieldValuePartitonHandler = new IssueRelatedEntitiesPartionHandler(mockBackupProject, printWriter, null, EasyList.build(mockModelEntity), "UTF-16");

        customFieldValuePartitonHandler.startDocument();
        customFieldValuePartitonHandler.handleEntity("CustomFieldValue", EasyMap.build("id", "10000", "issue", "10", "customfield", "41", "stringvalue", "A"));
        customFieldValuePartitonHandler.handleEntity("CustomFieldValue", EasyMap.build("id", "10001", "issue", "12", "customfield", "43", "stringvalue", "B"));
        customFieldValuePartitonHandler.handleEntity("CustomFieldValue", EasyMap.build("id", "10002", "issue", "14", "customfield", "44", "stringvalue", "This one is a has a new-line" + NL + "There - told you so."));
        customFieldValuePartitonHandler.handleEntity("CustomFieldValue", EasyMap.build("id", "10003", "issue", "16", "customfield", "46", "stringvalue", "D"));
        customFieldValuePartitonHandler.handleEntity("Issue", EasyMap.build("id", "12", "key", "MNK-16", "desc", "Stuff happened."));
        customFieldValuePartitonHandler.endDocument();

        printWriter.close();
        String xml = writer.toString();
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-16\"?>" + NL
                     + "<entity-engine-xml>" + NL
                     + "    <CustomFieldValue id=\"10001\" issue=\"12\" customfield=\"43\" stringvalue=\"B\"/>" + NL
                     + "    <CustomFieldValue id=\"10002\" issue=\"14\" customfield=\"44\">" + NL
                     + "        <stringvalue><![CDATA[This one is a has a new-line" + NL
                     + "There - told you so.]]></stringvalue>" + NL
                     + "    </CustomFieldValue>" + NL
                     + "</entity-engine-xml>", xml);

        mockBackupProjectControl.verify();

    }
    
    @Test
    public void testChangeItemsAndChangeGroups() throws ParseException
    {
        // Set up a mock ChangeGroup
        // <ChangeGroup id="10033" issue="10000" author="admin" created="2008-01-24 16:03:24.325"/>
        final MockModelEntity mockChangeGroupModelEntity = new MockModelEntity("ChangeGroup");
        mockChangeGroupModelEntity.setFieldNames(EasyList.build("id", "issue", "author", "created"));
        // <ChangeItem id="10000" group="10000" fieldtype="jira" field="Project" oldvalue="10000" oldstring="homosapien" newvalue="10001" newstring="monkey"/>
        final MockModelEntity mockChangeItemModelEntity = new MockModelEntity("ChangeItem");
        mockChangeItemModelEntity.setFieldNames(EasyList.build("id", "group", "fieldtype", "field", "oldvalue", "oldstring", "newvalue", "newstring", "xxx"));

        // Create a simple PrintWriter
        StringWriter writer = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(writer);

        // Create a simple PrintWriter
        StringWriter changeItem = new StringWriter();
        final PrintWriter changeItemPrintWriter = new PrintWriter(changeItem);

        BackupProject backupProject = new BackupProjectImpl(new ExternalProject(), Collections.EMPTY_LIST, Collections.EMPTY_LIST,
                Collections.EMPTY_LIST, EasyList.build(new Long(12), new Long(14)));

        // Create our handler
        IssueRelatedEntitiesPartionHandler issueRelatedEntitiesPartionHandler = new IssueRelatedEntitiesPartionHandler(backupProject, printWriter, changeItemPrintWriter, EasyList.build(mockChangeGroupModelEntity, mockChangeItemModelEntity), "UTF-8");
        // Now fire XML parse events at it to handle.
        issueRelatedEntitiesPartionHandler.startDocument();
        issueRelatedEntitiesPartionHandler.handleEntity("ChangeGroup", EasyMap.build("id", "10033", "issue", "10", "author", "dude", "created", "2008-04-01 12:34:56.789"));
        issueRelatedEntitiesPartionHandler.handleEntity("ChangeGroup", EasyMap.build("id", "10034", "issue", "12", "author", "dudette", "created", "2009-04-01 12:34:56.789"));
        issueRelatedEntitiesPartionHandler.handleEntity("ChangeGroup", EasyMap.build("id", "10035", "issue", "14", "author", "dude", "created", "2010-04-01 12:34:56.789"));
        issueRelatedEntitiesPartionHandler.handleEntity("ChangeGroup", EasyMap.build("id", "10036", "issue", "16", "author", "dudette", "created", "2011-04-01 12:34:56.789"));
        // Fire some Change Items - we should only include ones that have ChangeGroups that we stored.
        final Map attributes = EasyMap.build("id", "10501", "group", "10033", "fieldtype", "jira", "field", "A", "oldvalue", "10000", "oldstring", "level1");
        attributes.put("newvalue", "10001");
        attributes.put("newstring", "level2");;
        issueRelatedEntitiesPartionHandler.handleEntity("ChangeItem", attributes);
        final Map attributes1 = EasyMap.build("id", "10502", "group", "10034", "fieldtype", "jira", "field", "B", "oldvalue", "10000", "oldstring", "level1");
        attributes1.put("newvalue", "10001");
        attributes1.put("newstring", "level2");
        issueRelatedEntitiesPartionHandler.handleEntity("ChangeItem", attributes1);
        final Map attributes2 = EasyMap.build("id", "10503", "group", "10035", "fieldtype", "jira", "field", "C", "oldvalue", "10000", "oldstring", "level1");
        attributes2.put("newstring", "level2");;
        attributes2.put("newvalue", "10001");
        issueRelatedEntitiesPartionHandler.handleEntity("ChangeItem", attributes2);
        final Map attributes3 = EasyMap.build("id", "10504", "group", "10036", "fieldtype", "jira", "field", "B", "oldvalue", "10000", "oldstring", "level1");
        attributes3.put("newvalue", "10001");
        attributes3.put("newstring", "level2");;
        issueRelatedEntitiesPartionHandler.handleEntity("ChangeItem", attributes3);

        issueRelatedEntitiesPartionHandler.endDocument();

        printWriter.close();
        String xml = writer.toString();
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + NL
                     + "<entity-engine-xml>" + NL
                     + "    <ChangeGroup id=\"10034\" issue=\"12\" author=\"dudette\" created=\"2009-04-01 12:34:56.789\"/>" + NL
                     + "    <ChangeGroup id=\"10035\" issue=\"14\" author=\"dude\" created=\"2010-04-01 12:34:56.789\"/>" + NL
                     + "</entity-engine-xml>", xml);

        changeItemPrintWriter.close();
        String changeItemXml = changeItem.toString();
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + NL
                     + "<entity-engine-xml>" + NL
                     + "    <ChangeItem id=\"10502\" group=\"10034\" fieldtype=\"jira\" field=\"B\" oldvalue=\"10000\" oldstring=\"level1\" newvalue=\"10001\" newstring=\"level2\"/>" + NL
                     + "    <ChangeItem id=\"10503\" group=\"10035\" fieldtype=\"jira\" field=\"C\" oldvalue=\"10000\" oldstring=\"level1\" newvalue=\"10001\" newstring=\"level2\"/>" + NL
                     + "</entity-engine-xml>", changeItemXml);
    }

    @Test
    public void testVotersAndWatchers() throws ParseException
    {
        // Set up a mock UserAssociation
        // <UserAssociation sourceName="admin" sinkNodeId="10000" sinkNodeEntity="Issue" associationType="VoteIssue"/>
        final MockModelEntity mockUserAssociationModelEntity = new MockModelEntity("UserAssociation");
        mockUserAssociationModelEntity.setFieldNames(EasyList.build("sourceName", "sinkNodeId", "sinkNodeEntity", "associationType"));

        // Create a simple PrintWriter
        StringWriter writer = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(writer);

        BackupProject backupProject = new BackupProjectImpl(new ExternalProject(), Collections.EMPTY_LIST, Collections.EMPTY_LIST,
                Collections.EMPTY_LIST, EasyList.build(new Long(12), new Long(14)));

        // Create our handler
        IssueRelatedEntitiesPartionHandler issueRelatedEntitiesPartionHandler = new IssueRelatedEntitiesPartionHandler(backupProject, printWriter, null, EasyList.build(mockUserAssociationModelEntity), "UTF-8");
        // Now fire XML parse events at it to handle, fire Voters.
        issueRelatedEntitiesPartionHandler.startDocument();
        issueRelatedEntitiesPartionHandler.handleEntity("UserAssociation", EasyMap.build("sourceName", "fee", "sinkNodeId", "10", "sinkNodeEntity", "Issue", "associationType", "VoteIssue"));
        issueRelatedEntitiesPartionHandler.handleEntity("UserAssociation", EasyMap.build("sourceName", "fi", "sinkNodeId", "12", "sinkNodeEntity", "Issue", "associationType", "VoteIssue"));
        issueRelatedEntitiesPartionHandler.handleEntity("UserAssociation", EasyMap.build("sourceName", "fo", "sinkNodeId", "14", "sinkNodeEntity", "Issue", "associationType", "VoteIssue"));
        issueRelatedEntitiesPartionHandler.handleEntity("UserAssociation", EasyMap.build("sourceName", "fum", "sinkNodeId", "16", "sinkNodeEntity", "Issue", "associationType", "VoteIssue"));
        // Fire some Watchers- we should only include ones that have ChangeGroups that we stored.
        issueRelatedEntitiesPartionHandler.handleEntity("UserAssociation", EasyMap.build("sourceName", "zee", "sinkNodeId", "10", "sinkNodeEntity", "Issue", "associationType", "WatchIssue"));
        issueRelatedEntitiesPartionHandler.handleEntity("UserAssociation", EasyMap.build("sourceName", "zi", "sinkNodeId", "12", "sinkNodeEntity", "Issue", "associationType", "WatchIssue"));
        issueRelatedEntitiesPartionHandler.handleEntity("UserAssociation", EasyMap.build("sourceName", "zo", "sinkNodeId", "14", "sinkNodeEntity", "Issue", "associationType", "WatchIssue"));
        issueRelatedEntitiesPartionHandler.handleEntity("UserAssociation", EasyMap.build("sourceName", "zum", "sinkNodeId", "16", "sinkNodeEntity", "Issue", "associationType", "WatchIssue"));

        // Fire some crap ones
        issueRelatedEntitiesPartionHandler.handleEntity("UserAssociation", EasyMap.build("sourceName", "zum", "sinkNodeId", "16", "sinkNodeEntity", "Issue", "associationType", "SomeInvalidType"));        
        issueRelatedEntitiesPartionHandler.handleEntity("SomeEntity", EasyMap.build("sourceName", "zi", "sinkNodeId", "12", "sinkNodeEntity", "Issue", "associationType", "WatchIssue"));

        issueRelatedEntitiesPartionHandler.endDocument();

        printWriter.close();
        String xml = writer.toString();
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + NL
                     + "<entity-engine-xml>" + NL
                     + "    <UserAssociation sourceName=\"fi\" sinkNodeId=\"12\" sinkNodeEntity=\"Issue\" associationType=\"VoteIssue\"/>" + NL
                     + "    <UserAssociation sourceName=\"fo\" sinkNodeId=\"14\" sinkNodeEntity=\"Issue\" associationType=\"VoteIssue\"/>" + NL
                     + "    <UserAssociation sourceName=\"zi\" sinkNodeId=\"12\" sinkNodeEntity=\"Issue\" associationType=\"WatchIssue\"/>" + NL
                     + "    <UserAssociation sourceName=\"zo\" sinkNodeId=\"14\" sinkNodeEntity=\"Issue\" associationType=\"WatchIssue\"/>" + NL
                     + "</entity-engine-xml>", xml);
    }

}
