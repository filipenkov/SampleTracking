package org.jcvi.jira.importer.jiramodel;

import noNamespace.ActionType;
import noNamespace.EntityEngineXmlType;
import org.jcvi.jira.importer.utils.DateWriter;

import java.util.Date;

/**
 * An object representing a comment.
 *
 */
public class Comment {
    //a uid for the action, I'm not sure if this is
    //used for anything though
    private static int uid = 1000;
    //Action Elements in the XML are only used for one thing, comments.
    //The type is always set to comment.
    public static final String ACTION_TYPE="comment";
    //The data about a comment, somewhat abstracted from the CTM and JIRA's view
    private final Date dateOfCommentsCreation;
    private final String contentOfComment;
    private final JIRAUser userWhoWroteComment;
    private final int commentUID;

    public Date getDateOfCommentsCreation() {
        return dateOfCommentsCreation;
    }

    public Comment(Date created,
                   String content,
                   JIRAUser author) {
        this.dateOfCommentsCreation = created;
        this.contentOfComment = content;
        this.userWhoWroteComment = author;
        this.commentUID = uid++;
    }

    public void addToXML(Issue parent, EntityEngineXmlType entityXML) {
        ActionType comment = entityXML.addNewAction();
        comment.setType(ACTION_TYPE);
        comment.setId(commentUID);
        comment.setIssue(parent.getUid()); //the link is based on the uid, not the key
        comment.setAuthor(userWhoWroteComment.getName());
        comment.setCreated(DateWriter.convertToJIRADate(dateOfCommentsCreation));
        //CTM doesn't allow the editing of comments and so these parts are left
        //blank
        //comment.setUpdateauthor();
        //comment.setUpdated();

        //CTM doesn't limit the visibility of comments and so these parts are
        //left blank
        //comment.setLevel();    //a reference to an IssueSecurityLevel?
        //comment.setRolelevel();//a reference to a ProjectRole.id

        //a date based uid? not seen in the backup and so not included
        //comment.setNumvalue();

        //The content can be placed in the header as an attribute
        //We will always use the real body though
        //comment.setBody2();

        comment.setBody(contentOfComment);

        //no need to return an object, this has already been added to the XML
    }
}
