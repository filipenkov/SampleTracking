package com.atlassian.jira.imports.project.handler;

import com.atlassian.jira.local.ListeningTestCase;
import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.imports.project.core.BackupProject;
import com.atlassian.jira.exception.ParseException;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import org.easymock.MockControl;
import org.easymock.classextension.MockClassControl;
import org.ofbiz.core.entity.model.MockModelEntity;
import org.ofbiz.core.entity.model.ModelEntity;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @since v3.13
 */
public class TestIssuePartitonHandler extends ListeningTestCase
{
    private static String NL;

    public TestIssuePartitonHandler()
    {
        NL = System.getProperty("line.separator");
    }

    @Test
    public void testPartionFile() throws ParseException
    {
        final MockControl mockModelEntityControl = MockClassControl.createControl(ModelEntity.class);
        final MockModelEntity mockModelEntity = new MockModelEntity("Issue");
        mockModelEntity.setFieldNames(EasyList.build("id", "key", "desc"));

        mockModelEntityControl.replay();

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

        IssuePartitonHandler issuePartitonHandler = new IssuePartitonHandler(mockBackupProject, printWriter, mockModelEntity, "UTF-8");

        issuePartitonHandler.startDocument();
        issuePartitonHandler.handleEntity("Issue", EasyMap.build("id", "10", "key", "HSP-1", "desc", "Sheet happened."));
        issuePartitonHandler.handleEntity("Issue", EasyMap.build("id", "12", "key", "MNK-16", "desc", "Stuff happened."));
        issuePartitonHandler.handleEntity("Issue", EasyMap.build("id", "14", "key", "MNK-19", "desc", "Some really bad gear happened." + NL + "Yes it did."));
        issuePartitonHandler.handleEntity("Issue", EasyMap.build("id", "16", "key", "HSP-13", "desc", "More stuff happened."));
        issuePartitonHandler.handleEntity("NotIssue", EasyMap.build("id", "16", "key", "HSP-13", "desc", "More stuff happened."));
        issuePartitonHandler.endDocument();

        printWriter.close();
        String xml = writer.toString();
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + NL
                     + "<entity-engine-xml>" + NL
                     + "    <Issue id=\"12\" key=\"MNK-16\" desc=\"Stuff happened.\"/>" + NL
                     + "    <Issue id=\"14\" key=\"MNK-19\">" + NL
                     + "        <desc><![CDATA[Some really bad gear happened." + NL
                     + "Yes it did.]]></desc>" + NL
                     + "    </Issue>" + NL
                     + "</entity-engine-xml>", xml);

        assertEquals(2, issuePartitonHandler.getEntityCount());
        mockBackupProjectControl.verify();

    }
}
