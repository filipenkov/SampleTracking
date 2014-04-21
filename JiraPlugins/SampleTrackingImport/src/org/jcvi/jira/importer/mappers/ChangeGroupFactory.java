package org.jcvi.jira.importer.mappers;

import org.jcvi.glk.ctm.ReferenceHistory;
import org.jcvi.glk.ctm.User;
import org.jcvi.jira.importer.jiramodel.*;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Bridge between ctm_reference_history / ctm_task_history and updates
 * to JIRA fields.
 * This only wraps the change items that are generated else where.
 *
 * It contains almost the same code for TaskHistory as ReferenceHistory
 * but the interfaces don't inherit and so I
 * can't use polymorphism. Altering the Hibernate mappings would solve this
 * but it's over kill for something that will only be used once.
 */
public class ChangeGroupFactory {
    public static ChangeGroup createChangeGroup(
                       Date updated,
                       JIRAUser updatedBy,
                       Set<ChangeItem> changes) {
        if (changes.isEmpty()) {
            return null; //don't create empty groups
        }
        return new ChangeGroup(
                changes,
                updated,
                updatedBy
                );
    }

    public static ChangeGroup createStatusChangeGroup( Status status,
                                                       Date updated,
                                                       User assignee,
                                                       User editor)
            throws UnmappedField {
        JIRAUser jiraAssignee = UserMapper.getUser(assignee);
        JIRAUser jiraEditor   = UserMapper.getUser(editor);
        return createStatusChangeGroup(status,updated,jiraAssignee,jiraEditor);
    }

    public static ChangeGroup createStatusChangeGroup( Status status,
                                                       Date updated,
                                                       JIRAUser assignee,
                                                       JIRAUser editor)
            throws UnmappedField {
        Set<ChangeItem> updates = new HashSet<ChangeItem>();

        if (status != null) {
            updates.add(ChangeItemFactory.
                    createStatusChangeItem(status));
            Resolution resolution = StatusMapper.getImpliedResolution(status);
            updates.add(ChangeItemFactory.
                    createResolutionChangeItem(resolution));
        }
        updates.add(ChangeItemFactory.
                createAssigneeChangeItem(assignee));
        //only create if there is an update
        updates.remove(null);

        return createChangeGroup(
                updated,
                editor,
                updates);
    }

    public static ChangeGroup createChangeGroup(
                       ReferenceHistory history,
                       Set<ChangeItem> changes)
            throws UnmappedField {
        return createChangeGroup(
                                 history.getEditedDate(),
                                 UserMapper.getUser(history.getEditedBy()),
                                 changes
                                 );
    }

}
