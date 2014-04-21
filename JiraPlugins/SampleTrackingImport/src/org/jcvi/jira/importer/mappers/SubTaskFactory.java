package org.jcvi.jira.importer.mappers;

import org.jcvi.glk.ctm.Task;
import org.jcvi.jira.importer.jiramodel.*;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: pedworth
 * Date: 2/25/13
 * Time: 9:06 AM
 * Used to Encapsulate the logic for mapping ctm data to JIRA SubTasks
 * This contains some code that is very similar to SampleImporter because
 * TaskHistory and ReferenceHistory contain similar data but don't have any
 * shared interface.
 */
public class SubTaskFactory {
    public static SubTask createSubTask(Task ctmTask,
                                        Sample sample,
                                        IssueType type)
            throws UnmappedField {
        Date created = ctmTask.getCreationDate();
        Status status = StatusMapper.getStatus(ctmTask.getStatus());
        Priority priority = PriorityMapper.getPriority(ctmTask.getPriority());
        JIRAUser assignee = UserMapper.getUser(ctmTask.getAssignedTo());
        if(assignee == null) {
            assignee = JIRAUser.getUser(JIRAUser.SAMPLETRACKING_USER);
        }

        SubTask subtask = new SubTask(sample,
                                      type,
                                      created,
                                      status,
                                      priority,
                                      assignee);
        //test status is valid
        Workflow workflow = subtask.getWorkflow();
        if (! workflow.getValidStatuses().contains(status)) {
            throw new UnmappedField("Status", "invalid for workflow: "+workflow.getName()+" "+status.getName());
        }
        return subtask;
    }
}
