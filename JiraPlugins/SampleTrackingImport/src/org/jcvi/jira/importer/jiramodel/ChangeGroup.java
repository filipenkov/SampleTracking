package org.jcvi.jira.importer.jiramodel;

import noNamespace.*;
import org.jcvi.jira.importer.mappers.CommentFactory;
import org.jcvi.jira.importer.mappers.UnmappedField;
import org.jcvi.jira.importer.utils.DateWriter;
import org.jcvi.jira.importer.utils.UID;

import java.util.*;

/**
 * This represents an event where a sample/sub-task was modified
 * The group object doesn't hold much, it is mostly used to associate the
 * actual changes with a single event
 */
public class ChangeGroup {
    private final int uid;
    private final Set<ChangeItem> changes;
    private final Date created;
    private final JIRAUser author;


    //constructors
    public ChangeGroup( Set<ChangeItem> changes,
                        Date created,
                        JIRAUser author) {
        this.uid     = UID.getUID(ChangeGroup.class);
        this.changes = changes;
        this.created = created;
        this.author  = author;
    }

    public Date getCreated() {
        return created;
    }

    public int getUID() {
        return uid;
    }

    public JIRAUser getAuthor() {
        return author;
    }

    public void addToXml(Issue parent,
                     EntityEngineXmlType xml,
                     Map<ChangeItemFieldJiraEnum.Enum,NameIDPair> state) {
        if (!testUpdate(state)) {
            return;
        }
        ChangeGroupType changeGroup = xml.addNewChangeGroup();
        changeGroup.setAuthor(author.getName());
        changeGroup.setCreated(DateWriter.convertToJIRADate(created));
        changeGroup.setId(uid);
        changeGroup.setIssue(parent.getUid());
        //add the changeItems
        for(ChangeItem change: changes) {
            change.addToXml(this,xml,state);
        }
    }

    public Comment asComment(String subtaskID)
            throws UnmappedField {
        StringBuilder commentText = new StringBuilder(subtaskID);
        commentText.append("\n\n");
        //add the changeItems
        for(ChangeItem change: changes) {
            commentText.append("Field: '");
            commentText.append(change.getField());
            commentText.append("'Changed value to: '");
            commentText.append(change.getValue());
            commentText.append("'\n");
        }
        return CommentFactory.createComment(commentText.toString(), created, author);
    }

    public boolean containsField(ChangeItemFieldJiraEnum.Enum field) {
        if (field == null) {
            return false; //silly case, possibly should be an error
        }
        for (ChangeItem change: changes) {
            if (field.equals(change.getField())) {
                return true;
            }
        }
        return false;
    }

    public NameIDPair getField(ChangeItemFieldJiraEnum.Enum field) {
        if (field == null) {
            return null; //silly case, possibly should be an error
        }
        for (ChangeItem change: changes) {
            if (field.equals(change.getField())) {
                return change.getValue();
            }
        }
        return null;
    }

    public boolean testUpdate(Map<ChangeItemFieldJiraEnum.Enum,NameIDPair> state) {
        for(ChangeItem change: changes) {
            if (change.testUpdate(state)) {
                return true;
            }
        }
        return false;
    }

    public void updateStates(Map<ChangeItemFieldJiraEnum.Enum,NameIDPair> state) {
        for(ChangeItem change: changes) {
            change.updateState(state);
        }
    }
}
