package org.jcvi.jira.importer.jiramodel;

import noNamespace.ChangeItemFieldJiraEnum;
import noNamespace.EntityEngineXmlType;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jcvi.jira.importer.mappers.ChangeGroupFactory;
import org.jcvi.jira.importer.mappers.StatusMapper;
import org.jcvi.jira.importer.mappers.UnmappedField;
import org.jcvi.jira.importer.utils.DateWriter;
import org.jcvi.jira.importer.utils.UID;

import java.util.*;

/**
 * Three stage process to allow errors to be discovered before any writing
 * has taken place.
 * Phase 1. data-collection
 * Phase 2. Validation
 * Phase 3. XML Generation
 */
public abstract class Issue {
    private static final Logger log = Logger.getLogger(Issue.class);
    static {
        log.setLevel(Level.DEBUG);
    }
    //identifiers
    private final int              uid;
    private final Project          project;
    private final int              issueNumber;

    //immutable State information
    //Associated JIRA objects
    private   final IssueType      type; //fixed from when the issue is created
    private   final Workflow       workflow;
    private   final Resolution     resolution; //null for open issues
    private   final JIRAUser       reporter;
    //private Date dueDate;
    private   final Date           created;
    //Data collection objects
    private   final Map<CustomField,String> customFieldValues;

    //These aren't fixed for the life of the Issue
    //but their final values are known when the issue is created
    //information about the interim values will be loaded later.
    private final Status           status;


    //Comments
    private SortedSet<Comment> comments
            = new TreeSet<Comment>(new Comparator<Comment>() {
        public int compare(Comment o1, Comment o2) {
            if (o1 == null && o2 == null) {
                return 0;
            } else if (o1 == null) {
                return -1;
            } else if (o2 == null) {
                return 1;
            }
            return o1.getDateOfCommentsCreation()
                    .compareTo(o2.getDateOfCommentsCreation());
        }
    });

    //flag to keep track of
    //Change History
    private SortedSet<ChangeGroup> updates
            = new TreeSet<ChangeGroup>(new Comparator<ChangeGroup>() {
        public int compare(ChangeGroup o1, ChangeGroup o2) {
            if (o1 == null && o2 == null) {
                return 0;
            } else if (o1 == null) {
                return -1;
            } else if (o2 == null) {
                return 1;
            }
            return o1.getCreated().compareTo(o2.getCreated());
        }
    });

    //This is the final person assigned to the task.
    //at the end this should match with currentAssignee...
    public abstract JIRAUser getAssignee();


    public Issue(Project project,
                 IssueType type,
                 Date created,
                 Status status,
                 HashMap<CustomField, String> customFields) {
        this.project           = project;
        this.type              = type;
        this.created           = created;
        this.status            = status;
        this.workflow          = Workflow.getWorkflow(getType());
        this.customFieldValues = customFields;
        //ctm doesn't store a 'reporter'
        this.reporter          = JIRAUser.getSystemUser();

        this.uid               = UID.getUID(Issue.class);
        this.issueNumber       = uid; //only one project in the file.

        this.resolution        = StatusMapper.getImpliedResolution(status);
    }

    //The updates and comments are re-ordered by date
    public void addComment(Comment comment) {
        if (comment != null) {
            this.comments.add(comment);
        }
    }

    public void addChangeGroup(ChangeGroup change) {
        if (change != null) {
            this.updates.add(change);
        }
    }

    public void addComments(Collection<Comment> commentCollection) {
        commentCollection.remove(null);
        this.comments.addAll(commentCollection);
        if (comments.contains(null)) {
            throw new NullPointerException("trying to add a group of comments that includes a null");
        }
    }

    public void addChangeGroups(Collection<ChangeGroup> changes) {
        changes.remove(null);
        this.updates.addAll(changes);
        if (updates.contains(null)) {
            throw new NullPointerException("trying to add a group of updates that includes a null");
        }
    }

    public Workflow getWorkflow() {
        return workflow;
    }

    //data retrieval for XML generation
    public int getUid() {
        return uid;
    }

    public IssueType getType() {
        return type;
    }

    public Project getProject() {
        return project;
    }

    public abstract String getSummary();

    public abstract Priority getPriority();

    public String getKey() {
        return project.getAbbreviation()+"-"+issueNumber;
    }

    public Status getIssueStatus() {
        return status;
    }

    public ChangeGroup getFinalStatusUpdate() {
        //state starts off empty
        Map<ChangeItemFieldJiraEnum.Enum,NameIDPair> state =
            new HashMap<ChangeItemFieldJiraEnum.Enum, NameIDPair>();
        ChangeGroup lastChangeGroup = null;
        for(ChangeGroup change: updates) {
            //ignore repeats of previous changes
            if (change.testUpdate(state)
                    && change.containsField(ChangeItemFieldJiraEnum.STATUS)) {
                lastChangeGroup = change;
            }
            change.updateStates(state);
        }
        return lastChangeGroup;
    }

    //XML OUTPUT
    public void addToXML(EntityEngineXmlType xml) {
        addIssueOnlyToXML(xml);
        addCommentXML(xml);
        addChangeGroupXML(xml);
        addCustomFieldsToXML(xml);
    }

    private void addIssueOnlyToXML(EntityEngineXmlType xml) {
        Date oldestUpdate = updates.first().getCreated();
        if (created != null && created.before(oldestUpdate)) {
            oldestUpdate = created;
        }
        Date newestUpdate = updates.last().getCreated();

        noNamespace.IssueType xmlIssue = xml.addNewIssue();
        xmlIssue.setSummary2(getSummary());//attribute
        //xmlIssue.setSummary();//element

        xmlIssue.setId(getUid());
        xmlIssue.setKey(getKey());
        xmlIssue.setProject(project.getID());
        xmlIssue.setReporter(reporter.getName());
        xmlIssue.setAssignee(getAssignee().getName());
        xmlIssue.setType(type.getID());

        xmlIssue.setPriority(getPriority().getID());
        if (resolution != null) {
            xmlIssue.setResolution(resolution.getID());
            //hack, assume that the most recent update was the one that closed
            //it. The only other type of update would be if it were re-assigned
            //which would be odd once it was finished
            xmlIssue.setResolutiondate(DateWriter.convertToJIRADate(newestUpdate));
        }

        xmlIssue.setCreated(DateWriter.convertToJIRADate(oldestUpdate));
        xmlIssue.setUpdated(DateWriter.convertToJIRADate(newestUpdate));

        if (status != null) {
            xmlIssue.setStatus(status.getID());
        } else {
            //todo: probably an error
            xmlIssue.setStatus((short)1);
        }


        //fixed values, no votes and no watchers
        xmlIssue.setVotes((short)0);
        xmlIssue.setWorkflowId(workflow.getID());

        //we aren't using these
        //xmlIssue.setEnvironment();
        //xmlIssue.setEnvironment2();

        //xmlIssue.setSecurity(); //reference to a SecurityLevel
        //xmlIssue.setFixfor(); //reference to a Version
        //xmlIssue.setComponent(); //reference to a Component

        //we aren't using the time tracking part
        //xmlIssue.setTimeestimate();
        //xmlIssue.setTimespent();
        //xmlIssue.setTimeoriginalestimate();

        //xmlIssue.setWatches((short)0); v5+
    }

    private void addCommentXML(EntityEngineXmlType xml) {
        for(Comment comment : comments) {
            comment.addToXML(this,xml);
        }
    }

    private void addChangeGroupXML(EntityEngineXmlType xml) {
        //state starts off empty, it is used to suppress unnecessary change items/groups
        Map<ChangeItemFieldJiraEnum.Enum,NameIDPair> state =
                new HashMap<ChangeItemFieldJiraEnum.Enum, NameIDPair>();
        for(ChangeGroup change: updates) {
            change.addToXml(this,xml,state);
            change.updateStates(state);
        }
        //check what the final status is:
//System.out.println("Issues Final State = " + state.get(ChangeItemFieldJiraEnum.STATUS).getName() + "," + status.getName());
    }

    private void addCustomFieldsToXML(EntityEngineXmlType xml) {
        if (customFieldValues == null) {
            return;
        }
        //todo: use state to check all customFields are added
        //Create a CustomFieldValue object for each
        for(Map.Entry<CustomField,String> entry :customFieldValues.entrySet()) {
            CustomFieldValue customFieldValue
                    = new CustomFieldValue(this,
                                           entry.getKey(),
                                           entry.getValue());
            customFieldValue.addToXml(xml);
        }
    }

        //called between the build the model section and the output the xml part
    public void fixStatus() throws UnmappedField {
            //check the final status
            Status status = getIssueStatus();
            ChangeGroup finalStatusUpdate = getFinalStatusUpdate();
            NameIDPair finalStatus = finalStatusUpdate.getField(ChangeItemFieldJiraEnum.STATUS);
            if ( status != finalStatus) {
                //hack just add an extra update to reach the final state
                addChangeGroup(ChangeGroupFactory.createStatusChangeGroup(status,
                        finalStatusUpdate.getCreated(),
                        finalStatusUpdate.getAuthor(),
                        finalStatusUpdate.getAuthor()));
//                throw new UnmappedField("Status","Miss-match between last update value of status and current status: "+safeToString(status)+"!="+safeToString(finalStatus));
            }
    }
//
//    private String safeToString(NameIDPair status) {
//        if (status == null) {
//            return "NULL";
//        }
//        return status.getName();
//    }

    //no-longer used functions:

    //    public ChangeGroup getStatusUpdateAfter(Date date) {
    //        //state starts off empty
    //        Map<ChangeItemFieldJiraEnum.Enum,NameIDPair> state =
    //            new HashMap<ChangeItemFieldJiraEnum.Enum, NameIDPair>();
    //        for(ChangeGroup change: updates) {
    //            //ignore repeats of previous changes
    //            if (change.testUpdate(state)
    //                    && change.containsField(ChangeItemFieldJiraEnum.STATUS)
    //                    && change.getCreated().after(date)) {
    //                return change;
    //            }
    //            change.updateStates(state);
    //        }
    //        return null;//no state change found after that date.
    //    }

}
