package org.jcvi.jira.importer.mappers;

import org.jcvi.glk.ctm.*;
import org.jcvi.jira.importer.jiramodel.*;

import java.util.*;

/**
 * Some CTM tasks are mapped to subtasks and some are mapped to statuses.
 * If a task is mapped to a status it is converted into a set of Change Group
 * objects.
 *
 * Problem cases:
 *
 * Multiple simultaneous status tasks:
 *  A sample can only be in one state at a time. In these cases it is not clear
 *  which status to use.
 * Task updates after changing state:
 *  If a task is assigned after the sample has changed states how should that
 *  assignment relate to the main sample (especially if it is assigned to
 *  an other user?)
 *
 * Initial Solution:
 *  To start with all of the actions on the task will be converted into comments
 *
 */
public class TaskMapper {
    public static SubTask addTaskToSample(Sample sample, Task ctmTask)
            throws UnmappedField {
        TaskType ctmTaskType = ctmTask.getType();

        IssueType subtaskType = IssueTypeMapper.getTaskType(ctmTaskType);
        User creator = ctmTask.getCreator();
        Date createdDate = ctmTask.getCreationDate();

        //get history information

        //add any status change to the parent issue
        Status impliedStatus  = IssueTypeMapper.getImpliedStatus(ctmTaskType);
        if (impliedStatus != null) {
            //add a changeGroup to update the state
            ChangeGroup update = ChangeGroupFactory.createStatusChangeGroup(
                                                         impliedStatus,
                                                         createdDate,
                                                         creator, //assignee
                                                         creator);//editor
            Comment comment = CommentFactory.createComment(
                                "Status change from Task Creation.\n"+
                                "The task type was: "+ctmTask.getType().getName(),
                                createdDate,
                                creator
                                );
            sample.addChangeGroup(update);
            sample.addComment(comment);
        }

        if (subtaskType == null) {
            //no equivalent subtask, store the information as comments in the
            //main Issue
            Set<Comment> changes  = getChangeGroupsAsComments(ctmTask);
            Set<Comment> comments = getComments(ctmTask);

            //this is just a status update, add the history to the 'parent'
            sample.addComments(changes);
            sample.addComments(comments);
            return null; //to indicate no subtask was created
        } else {
            Set<ChangeGroup> changes  = getChangeGroups(ctmTask);
            Set<Comment>     comments = getComments(ctmTask);

            //add a note as multiple ctmTask types map to the same JIRA subtask
            //and we might want to know which it started out as
            Comment comment = CommentFactory.createComment(
                    "SubTask created from CTM Task of type: "
                            +ctmTask.getType().getName(),
                    createdDate,
                    creator
                    );
            comments.add(comment);
            final SubTask subtask = SubTaskFactory.createSubTask(ctmTask,
                                                                 sample,
                                                                 subtaskType);
            subtask.addComments(comments);
            subtask.addChangeGroups(changes);
            return subtask;
        }
    }

    private static Set<ChangeGroup> getChangeGroups(Task ctmTask)
            throws UnmappedField {
        Set<ChangeGroup> changes  = new HashSet<ChangeGroup>();
        //map the ctm_reference_history
        for(TaskHistory event: ctmTask.getHistory()) {
            ChangeGroup update = createChangeGroup(event);
            changes.add(update);
        }
        return changes;
    }

    private static Set<Comment> getChangeGroupsAsComments(Task ctmTask)
            throws UnmappedField {
        Set<Comment> comments = new HashSet<Comment>();
        Set<ChangeGroup> changes  = getChangeGroups(ctmTask);
        //copy the changes into comments to be added to the main task
        for(ChangeGroup changeGroup : changes) {
            comments.add(changeGroup.asComment("CTM Task "+
                    ctmTask.getType().getName()+
                    " "+ ctmTask.getId()));
        }
        return comments;
    }

    private static Set<Comment> getComments(Task ctmTask)
            throws UnmappedField {
        Set<Comment>     comments = new HashSet<Comment>();
        //map the ctm_reference_history
        for(TaskHistory event: ctmTask.getHistory()) {
            Comment comment = CommentFactory.createComment(event);
            comments.add(comment);
        }
        return comments;
    }
    ////////////////////////////////////////////////////////////////////////////
    //FACTORIES
    ////////////////////////////////////////////////////////////////////////////
    //Used to avoid creating ChangeGroups that contain no changes
    ////////////////////////////////////////////////////////////////////////////
    private static ChangeGroup createChangeGroup(TaskHistory history)
            throws UnmappedField {
        TaskStatus status = history.getCurrentStatus();
        //Map the status
        Status jiraStatus = StatusMapper.getStatus(status);
        if (jiraStatus == null) {
            throw new UnmappedField("SubTask Status",status.getName());
        }
        return ChangeGroupFactory.createStatusChangeGroup(
                                       jiraStatus,
                                       history.getLastModified(),
                                       history.getCurrentlyAssignedTo(),
                                       history.getLastEditedBy());
    }
}
