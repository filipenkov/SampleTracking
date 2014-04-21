package org.jcvi.jira.importer.mappers;

import org.jcvi.glk.ctm.ReferenceHistory;
import org.jcvi.glk.ctm.TaskHistory;
import org.jcvi.glk.ctm.User;
import org.jcvi.jira.importer.jiramodel.Comment;
import org.jcvi.jira.importer.jiramodel.JIRAUser;

import java.util.Date;

/**
 * The bridge between a ctm_reference_history.comment or a ctm_task_history.comment
 * and a JIRA Action.
 */
public class CommentFactory {
    //factory method to avoid creating comment objects when there isn't a
    //comment
    public static Comment createComment(String comment,
                                        Date date,
                                        User user)
            throws UnmappedField {
        JIRAUser jiraEditor = UserMapper.getUser(user);
        return createComment(comment,date,jiraEditor);
    }

    public static Comment createComment(String comment,
                                        Date date,
                                        JIRAUser user)
            throws UnmappedField {
        //check there is actually a comment to parse
        if (comment == null || comment.trim().isEmpty()) {
            return null;
        }
        return new Comment(date,
                           comment.trim(),
                           user);
    }

    //Using a Reference History object
    public static Comment createComment(ReferenceHistory history)
            throws UnmappedField {
        return createComment(history.getComment(),
                             history.getDate(),
                             history.getEditedBy());
    }

    //Using a Task object
    public static Comment createComment(TaskHistory history)
            throws UnmappedField {
        return createComment(history.getComment(),
                             history.getDate(),//todo: could be getLastModified
                             history.getLastEditedBy());//todo: could be getCurrentlyAssignedTo
    }
}
