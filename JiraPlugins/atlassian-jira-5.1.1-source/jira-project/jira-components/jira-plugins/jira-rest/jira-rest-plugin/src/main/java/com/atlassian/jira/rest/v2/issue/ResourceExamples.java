package com.atlassian.jira.rest.v2.issue;

import com.atlassian.jira.issue.fields.rest.json.beans.CommentJsonBean;
import com.atlassian.jira.issue.fields.rest.json.beans.IssueLinkJsonBean;
import com.atlassian.jira.issue.fields.rest.json.beans.IssueLinkTypeJsonBean;
import com.atlassian.jira.issue.fields.rest.json.beans.IssueRefJsonBean;
import com.atlassian.jira.issue.fields.rest.json.beans.LinkIssueRequestJsonBean;
import com.atlassian.jira.issue.fields.rest.json.beans.StatusJsonBean;
import com.atlassian.jira.issue.fields.rest.json.beans.VisibilityJsonBean;
import com.atlassian.jira.rest.api.issue.FieldOperation;
import com.atlassian.jira.rest.api.issue.IssueFields;
import com.atlassian.jira.rest.api.issue.IssueUpdateRequest;
import com.atlassian.jira.rest.api.issue.ResourceRef;

import java.util.ArrayList;
import java.util.List;

/**
 * @since v5.0
 */
public class ResourceExamples
{
        static final CommentJsonBean DOC_COMMENT_LINK_ISSUE_EXAMPLE = new CommentJsonBean();
        static {
            DOC_COMMENT_LINK_ISSUE_EXAMPLE.setBody("Linked related issue!");
            DOC_COMMENT_LINK_ISSUE_EXAMPLE.setVisibility(new VisibilityJsonBean(VisibilityJsonBean.VisibilityType.group, "jira-users"));
        }

    /**
     * Example representation for use in auto-generated docs.
     */
    public static final LinkIssueRequestJsonBean LINK_ISSUE_REQUEST_EXAMPLE = new LinkIssueRequestJsonBean(new IssueRefJsonBean().key("HSP-1"), new IssueRefJsonBean().key("MKY-1"), new IssueLinkTypeJsonBean().name("Duplicate"), ResourceExamples.DOC_COMMENT_LINK_ISSUE_EXAMPLE);

    static final IssueLinkTypeJsonBean ISSUE_LINK_TYPE_EXAMPLE;
    static final IssueLinkTypeJsonBean ISSUE_LINK_TYPE_EXAMPLE_2;
    static
    {
        ISSUE_LINK_TYPE_EXAMPLE = new IssueLinkTypeJsonBean(1000l, "Duplicate", "Duplicated by", "Duplicates", Examples.restURI("/issueLinkType/1000"));
        ISSUE_LINK_TYPE_EXAMPLE_2 = new IssueLinkTypeJsonBean(1010l, "Blocks", "Blocked by", "Blocks", Examples.restURI("/issueLinkType/1010"));
    }

    static final IssueUpdateRequest UPDATE_DOC_EXAMPLE;
    static
    {
        IssueUpdateRequest issueUpdateRequest = new IssueUpdateRequest();
        List<FieldOperation> summaryOperations = new ArrayList<FieldOperation>();
        FieldOperation summaryOperation = new FieldOperation();
        summaryOperation.init("set", "Bug in business logic");
        summaryOperations.add(summaryOperation);
        issueUpdateRequest.update().put("summary", summaryOperations);

        List<FieldOperation> componentsOperations = new ArrayList<FieldOperation>();
        FieldOperation componentsOperation = new FieldOperation();
        componentsOperation.init("set", "");
        componentsOperations.add(componentsOperation);
        issueUpdateRequest.update().put("components", componentsOperations);

        List<FieldOperation> labelOperations = new ArrayList<FieldOperation>();
        FieldOperation labelOperation1 = new FieldOperation();
        labelOperation1.init("add", "triaged");
        labelOperations.add(labelOperation1);

        FieldOperation labelOperation2 = new FieldOperation();
        labelOperation2.init("remove", "blocker");
        labelOperations.add(labelOperation2);

        issueUpdateRequest.update().put("labels", labelOperations);

        UPDATE_DOC_EXAMPLE = issueUpdateRequest;
    }


    static final IssueUpdateRequest TRANSITION_DOC_EXAMPLE;
    static
    {
        IssueUpdateRequest issueUpdateRequest = new IssueUpdateRequest();
        issueUpdateRequest.transition(ResourceRef.withId("5"));
        IssueFields issueFields = new IssueFields();
        issueFields.resolution(ResourceRef.withName("Fixed"));
        issueFields.assignee(ResourceRef.withName("bob"));
        issueUpdateRequest.fields(issueFields);

        List<FieldOperation> commentOperations = new ArrayList<FieldOperation>();
        FieldOperation commentOperation = new FieldOperation();
        CommentJsonBean commentJsonBean = new CommentJsonBean();
        commentJsonBean.setBody("Bug has been fixed.");
        commentOperation.init("add", commentJsonBean);
        commentOperations.add(commentOperation);
        issueUpdateRequest.update().put("comment", commentOperations);

        TRANSITION_DOC_EXAMPLE = issueUpdateRequest;
    }

    static final IssueLinkJsonBean ISSUE_LINK_EXAMPLE;
    static
    {
        StatusJsonBean status = new StatusJsonBean().name("Open").iconUrl(Examples.jiraURI("/images/icons/status_open.gif").toString());
        IssueLinkJsonBean issueLinkJsonBean = new IssueLinkJsonBean().id("10001").type(ISSUE_LINK_TYPE_EXAMPLE);
        issueLinkJsonBean.outwardIssue(new IssueRefJsonBean("10004L", "PRJ-2", Examples.restURI("issue/PRJ-2"), new IssueRefJsonBean.Fields().status(status)));
        issueLinkJsonBean.inwardIssue(new IssueRefJsonBean("10004", "PRJ-3", Examples.restURI("issue/PRJ-3"), new IssueRefJsonBean.Fields().status(status)));
        ISSUE_LINK_EXAMPLE = issueLinkJsonBean;
    }

}
