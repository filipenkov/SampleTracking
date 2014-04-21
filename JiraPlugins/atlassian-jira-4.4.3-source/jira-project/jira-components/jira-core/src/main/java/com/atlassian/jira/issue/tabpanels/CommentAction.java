package com.atlassian.jira.issue.tabpanels;

import com.atlassian.jira.datetime.DateTimeFormatter;
import com.atlassian.jira.datetime.DateTimeFormatterFactory;
import com.atlassian.jira.datetime.DateTimeStyle;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.plugin.issuetabpanel.AbstractIssueAction;
import com.atlassian.jira.plugin.issuetabpanel.IssueTabPanelModuleDescriptor;
import org.apache.log4j.Logger;

import java.util.Date;
import java.util.Map;

import static com.atlassian.jira.datetime.DateTimeStyle.COMPLETE;
import static org.apache.commons.lang.StringEscapeUtils.escapeHtml;

/**
 * This class is the wrapper around the comment object and is used when displaying comments in the View Issue page,
 * on the 'Comment' issue tab panel.
 */
@SuppressWarnings ( { "UnusedDeclaration" })
public class CommentAction extends AbstractIssueAction
{
    private final Comment comment;
    private final Issue issue;
    private final boolean canEditComment;
    private final boolean canDeleteComment;
    private final RendererManager rendererManager;
    private final FieldLayoutManager fieldLayoutManager;
    private final DateTimeFormatterFactory dateTimeFormatterFactory;
    private static final Logger log = Logger.getLogger(CommentAction.class);

    public CommentAction(IssueTabPanelModuleDescriptor descriptor, Comment comment,
            boolean canEditComment, boolean canDeleteComment,
            RendererManager rendererManager, FieldLayoutManager fieldLayoutManager,
            DateTimeFormatterFactory dateTimeFormatterFactory)
    {
        super(descriptor);
        this.comment = comment;
        this.dateTimeFormatterFactory = dateTimeFormatterFactory;
        this.issue = comment.getIssue();
        this.canEditComment = canEditComment;
        this.canDeleteComment = canDeleteComment;
        this.rendererManager = rendererManager;
        this.fieldLayoutManager = fieldLayoutManager;
    }

    /**
     * Returns the comment created date
     * @return the comment created date
     */
    public Date getTimePerformed()
    {
        return comment.getCreated();
    }

    /**
     * This will populate the passed in map witht this object referenced as "action" and the rendered comment body as
     * "renderedContent".
     * @param params map of params to populate
     */
    protected void populateVelocityParams(Map params)
    {
        params.put("action", this);

        try
        {
            FieldLayoutItem fieldLayoutItem = fieldLayoutManager.getFieldLayout(issue.getProject(), issue.getIssueTypeObject().getId()).getFieldLayoutItem(IssueFieldConstants.COMMENT);
            if (fieldLayoutItem != null)
            {
                params.put("renderedContent", rendererManager.getRenderedContent(fieldLayoutItem.getRendererType(), comment.getBody(), issue.getIssueRenderContext()));
            }
        }
        catch (DataAccessException e)
        {
            log.error(e);
        }
    }

    //-------------------------------------------------------------------------------- Methods used by velocity template

    /**
     * Returns the comment
     * @return the comment
     */
    public Comment getComment()
    {
        return comment;
    }

    /**
     * Returns issue related to this comment
     * @return issue related to this comment
     */
    public Issue getIssue()
    {
        return issue;
    }

    /**
     * Returns true is comment is editable, false otherwise
     * @return true is comment is editable, false otherwise
     */
    public boolean isCanEditComment()
    {
        return canEditComment;
    }

    /**
     * Returns true is comment can be deleted, false otherwise
     * @return true is comment can be deleted, false otherwise
     */
    public boolean isCanDeleteComment()
    {
        return canDeleteComment;
    }

    public String formatDisplayHtml(Date date)
    {
        if (date == null)
        {
            return null;
        }

        DateTimeFormatter completeFormatter = dateTimeFormatter().withStyle(COMPLETE);
        return escapeHtml(completeFormatter.format(date));
    }

    @SuppressWarnings ( { "UnusedDeclaration" })
    public String formatIso8601Html(Date date)
    {
        if (date == null)
        {
            return null;
        }

        DateTimeFormatter iso8601Formatter = dateTimeFormatter().withStyle(DateTimeStyle.ISO_8601_DATE_TIME);
        return escapeHtml(iso8601Formatter.format(date));
    }

    /**
     * Returns a DateTimeFormatter for the logged in user.
     *
     * @return a DateTimeFormatter
     */
    protected DateTimeFormatter dateTimeFormatter()
    {
        return dateTimeFormatterFactory.formatter().forLoggedInUser();
    }
}
