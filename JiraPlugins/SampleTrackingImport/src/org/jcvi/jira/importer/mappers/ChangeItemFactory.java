package org.jcvi.jira.importer.mappers;

import noNamespace.ChangeItemFieldJiraEnum;
import org.jcvi.jira.importer.jiramodel.*;

/**
 * Created with IntelliJ IDEA.
 * User: pedworth
 * Date: 2/25/13
 * Time: 9:08 AM
 * Used to create ChangeItems, each of which contains a single field
 * that is to be modified
 */
public class ChangeItemFactory {
    public static ChangeItem createStatusChangeItem(Status newStatus) {
        if (newStatus != null) {
            //update the issue
            return new ChangeItem(
                    ChangeItemFieldJiraEnum.STATUS,
                    newStatus);
        }
        return null;
    }

    public static ChangeItem createResolutionChangeItem(Resolution resolution) {
        //Even null resolutions can be used to make change Items, they clear the value of the resolution
//        if (resolution == null) {
        return new ChangeItem(
                ChangeItemFieldJiraEnum.RESOLUTION,
                resolution);
    }

    public static ChangeItem createAssigneeChangeItem(JIRAUser newAssignee) {
        if (newAssignee == null) {
            //don't change the assignee from a known user to blank
            return null;
        }
        return new ChangeItem(
                ChangeItemFieldJiraEnum.ASSIGNEE,
                newAssignee);
    }
}
