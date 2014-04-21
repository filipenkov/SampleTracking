package com.atlassian.jira.upgrade.tasks;

import com.atlassian.core.ofbiz.util.EntityUtils;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.issue.comments.CommentManager;
import com.atlassian.jira.issue.comments.DefaultCommentManager;
import com.atlassian.jira.issue.index.IssueIndexManager;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.jira.mock.MockApplicationProperties;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.task.context.Context;

import org.ofbiz.core.entity.GenericValue;

import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;

import java.sql.Timestamp;

/**
 */
public class TestUpgradeTask_Build206 extends LegacyJiraMockTestCase
{
    private final static String COLUMN_ID = "id";
    private final static String COLUMN_AUTHOR = "author";
    private final static String COLUMN_CREATED = "created";
    private final static String COLUMN_UPDATE_AUTHOR = "updateauthor";
    private final static String COLUMN_UPDATED = "updated";
    private final static String COLUMN_TYPE = "type";

    private final static Long ID01 = new Long(1);
    private final static Long ID10 = new Long(10);
    private final static Long ID20 = new Long(20);

    private OfBizDelegator delegator;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        EntityUtils.createValue(DefaultCommentManager.COMMENT_ENTITY, EasyMap.build(COLUMN_ID, ID01, COLUMN_AUTHOR, "dude", COLUMN_CREATED,
            new Timestamp(System.currentTimeMillis()), COLUMN_TYPE, "comment"));

        EntityUtils.createValue(DefaultCommentManager.COMMENT_ENTITY, EasyMap.build(COLUMN_ID, ID10, COLUMN_AUTHOR, "mate", COLUMN_UPDATE_AUTHOR,
            "nobody", // "noboby" will be overwritten by author, which is "mate"
            COLUMN_CREATED, new Timestamp(System.currentTimeMillis()), COLUMN_TYPE, "comment"));

        // Create a jiraaction of a different type than comment and make sure it does not get updated
        EntityUtils.createValue(DefaultCommentManager.COMMENT_ENTITY, EasyMap.build(COLUMN_ID, ID20, COLUMN_AUTHOR, "mate", COLUMN_UPDATE_AUTHOR,
            "nobody", // "noboby" will be overwritten by author, which is "mate"
            COLUMN_CREATED, new Timestamp(System.currentTimeMillis()), COLUMN_TYPE, "otherthing"));

        delegator = ComponentManager.getComponentInstanceOfType(OfBizDelegator.class);
    }

    public void testDoUpdateNoReindex() throws Exception
    {
        // Set this up so we will skip the reindexing, we don't want to test that here.
        final ApplicationProperties applicationProperties = new MockApplicationProperties();
        applicationProperties.setOption(APKeys.JIRA_OPTION_INDEXING, false);

        // Setup the upgrade task
        final UpgradeTask_Build206 upgradeTask_build206 = new UpgradeTask_Build206(delegator, applicationProperties, null);
        upgradeTask_build206.doUpgrade(false);

        // Check at the GenericValue level that the values we thought should be udpated were.
        final GenericValue commentGV1 = delegator.findByPrimaryKey(DefaultCommentManager.COMMENT_ENTITY, EasyMap.build(COLUMN_ID, ID01));
        assertEquals("dude", commentGV1.getString(COLUMN_UPDATE_AUTHOR));
        assertEquals(commentGV1.getTimestamp(COLUMN_CREATED), commentGV1.getTimestamp(COLUMN_UPDATED));

        final GenericValue commentGV2 = delegator.findByPrimaryKey(DefaultCommentManager.COMMENT_ENTITY, EasyMap.build(COLUMN_ID, ID10));
        assertEquals("mate", commentGV2.getString(COLUMN_UPDATE_AUTHOR));
        assertEquals(commentGV2.getTimestamp(COLUMN_CREATED), commentGV2.getTimestamp(COLUMN_UPDATED));

        // Make sure the non-comment did not get modified.
        final GenericValue noncommentGV1 = delegator.findByPrimaryKey(DefaultCommentManager.COMMENT_ENTITY, EasyMap.build(COLUMN_ID, ID20));
        assertEquals("nobody", noncommentGV1.getString(COLUMN_UPDATE_AUTHOR));
        assertNull(noncommentGV1.getTimestamp(COLUMN_UPDATED));

        // Now check that the objects we get from the manager also reflect the changes that were made.
        final CommentManager commentManager = ComponentManager.getComponentInstanceOfType(CommentManager.class);

        final Comment comment1 = commentManager.getCommentById(ID01);
        assertEquals(ID01, comment1.getId());
        assertNotNull(comment1.getAuthor());
        assertEquals(comment1.getAuthor(), comment1.getUpdateAuthor());
        assertNotNull(comment1.getCreated());
        assertEquals(comment1.getCreated(), comment1.getUpdated());

        final Comment comment2 = commentManager.getCommentById(ID10);
        assertEquals(ID10, comment2.getId());
        assertNotNull(comment2.getAuthor());
        assertEquals(comment2.getAuthor(), comment2.getUpdateAuthor());
        assertNotNull(comment2.getCreated());
        assertEquals(comment2.getCreated(), comment2.getUpdated());
    }

    public void testDoUpdateWithReindex() throws Exception
    {
        // Set this up so we will skip the reindexing, we don't want to test that here.
        final ApplicationProperties applicationProperties = new MockApplicationProperties();
        applicationProperties.setOption(APKeys.JIRA_OPTION_INDEXING, true);

        final Mock mockIndexManager = new Mock(IssueIndexManager.class);
        mockIndexManager.expectAndReturn("reIndexAll", P.args(P.isA(Context.class)), new Long(10));

        // Setup the upgrade task
        final UpgradeTask_Build206 upgradeTask_build206 = new UpgradeTask_Build206(delegator, applicationProperties,
            (IssueIndexManager) mockIndexManager.proxy());
        upgradeTask_build206.doUpgrade(false);

        // Check at the GenericValue level that the values we thought should be udpated were.
        final GenericValue commentGV1 = delegator.findByPrimaryKey(DefaultCommentManager.COMMENT_ENTITY, EasyMap.build(COLUMN_ID, ID01));
        assertEquals("dude", commentGV1.getString(COLUMN_UPDATE_AUTHOR));
        assertEquals(commentGV1.getTimestamp(COLUMN_CREATED), commentGV1.getTimestamp(COLUMN_UPDATED));

        final GenericValue commentGV2 = delegator.findByPrimaryKey(DefaultCommentManager.COMMENT_ENTITY, EasyMap.build(COLUMN_ID, ID10));
        assertEquals("mate", commentGV2.getString(COLUMN_UPDATE_AUTHOR));
        assertEquals(commentGV2.getTimestamp(COLUMN_CREATED), commentGV2.getTimestamp(COLUMN_UPDATED));

        // Make sure the non-comment did not get modified.
        final GenericValue noncommentGV1 = delegator.findByPrimaryKey(DefaultCommentManager.COMMENT_ENTITY, EasyMap.build(COLUMN_ID, ID20));
        assertEquals("nobody", noncommentGV1.getString(COLUMN_UPDATE_AUTHOR));
        assertNull(noncommentGV1.getTimestamp(COLUMN_UPDATED));

        // Now check that the objects we get from the manager also reflect the changes that were made.
        final CommentManager commentManager = ComponentManager.getComponentInstanceOfType(CommentManager.class);

        final Comment comment1 = commentManager.getCommentById(ID01);
        assertEquals(ID01, comment1.getId());
        assertNotNull(comment1.getAuthor());
        assertEquals(comment1.getAuthor(), comment1.getUpdateAuthor());
        assertNotNull(comment1.getCreated());
        assertEquals(comment1.getCreated(), comment1.getUpdated());

        final Comment comment2 = commentManager.getCommentById(ID10);
        assertEquals(ID10, comment2.getId());
        assertNotNull(comment2.getAuthor());
        assertEquals(comment2.getAuthor(), comment2.getUpdateAuthor());
        assertNotNull(comment2.getCreated());
        assertEquals(comment2.getCreated(), comment2.getUpdated());

        mockIndexManager.verify();
    }
}
