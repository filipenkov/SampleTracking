package org.jcvi.jira.importer.jiramodel;

import noNamespace.EntityEngineXmlType;
import org.jcvi.jira.importer.mappers.UnmappedField;
import org.jcvi.jira.importer.utils.UID;

import java.util.Date;

/**
 */
public class SubTask extends Issue {
    private final org.jcvi.jira.importer.jiramodel.Sample sample;
    //private final short subtaskNumber; //0 for the 1st sub-task
    private final int issueLinkID;

    private final Priority priority;
    private final JIRAUser assignee;

    /**
     *
     * @param sample    This is used to populate parts of the subtask
     *                  the sub task is not automatically added to the
     *                  sample.
     */
    public SubTask(Sample sample,
                   IssueType type,
                   Date created,
                   Status status,
                   Priority priority,
                   JIRAUser assignee) {
        //MAP type & status
        super(sample.getProject(),
                type,
                created,
                status,
                null); //no customfields are set on the sub-tasks at the moment
        this.sample        = sample;
        //SubTask is actually used for the IssueLink uid
        //JIRAIssue is used for the Issue uid
        this.issueLinkID   = UID.getUID(SubTask.class);

        this.priority = priority;
        this.assignee = assignee;

        //todo: does this need any customFields?
    }

    @Override
    public String getSummary() {
        return sample.getGLKData().getCollectionCode()+"_"+
               sample.getGLKData().getSampleNumber()+"_"+
               getType().getName();
    }

    @Override
    public JIRAUser getAssignee() {
        return assignee;
    }

    @Override
    public Priority getPriority() {
        return priority;
    }

    /**
     */
    public void addToXML(EntityEngineXmlType xml) {
        super.addToXML(xml); //add the basic issue information
    }

    //This adds the sub-task link back to the parent task
    public void addIssueLinkToXML(EntityEngineXmlType xml, short subtaskNumber) {
        noNamespace.IssueLinkType link = xml.addNewIssueLink();
        link.setId((short)issueLinkID);
        link.setLinktype(IssueLinkType.subTaskIssueLinkType.getId());
        link.setSource(sample.getUid());
        link.setDestination(this.getUid());
        link.setSequence(subtaskNumber);
    }

    public void fixStatus() throws UnmappedField {
        super.fixStatus();
        Status status = getIssueStatus();
        if ("Open".equals(status.getName())) {
//            get the date of the next status change in the parent
//            FAIL, the next status change could be from addTaskToSample() subtask -> status.
//            this needs to be somewhere else
//            close the subtask
            throw new UnmappedField("Status","Final status of subtask was Open");
        }
    }
}
