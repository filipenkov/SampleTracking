package com.atlassian.jira.issue.comments;

import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.local.ListeningTestCase;

import java.util.Date;

public class TestCommentImpl extends ListeningTestCase
{
    private static final String COMMENT_BODY = "This is a comment";
    private static final String COMMENT_AUTHOR = "bob";
    private static final String COMMENT_GROUP_1 = "Group 1";
    private static final Long COMMENT_ROLE_1 = new Long(2);
    private static final Long COMMENT_ID = new Long(23);

    @Test
    public void testInvalidConstruction()
    {
        try
        {
            // null author and body is allowed
            // (with the RPC plubin it is possible to create comments with a null author.
            // It may therefore be possible, that data with null authors exists)
            new CommentImpl(null, null, null, null, null, null, null, null, null);
        }
        catch (IllegalArgumentException e)
        {
            fail();
        }

        try
        {
            // null body is allowed (see JRA-11522)
            new CommentImpl(null, COMMENT_AUTHOR, null, null, null, null, null, null, null);
        }
        catch (IllegalArgumentException e)
        {
            fail();
        }

        try
        {
            new CommentImpl(null, COMMENT_AUTHOR, null, COMMENT_BODY, COMMENT_GROUP_1, COMMENT_ROLE_1, null, null, null);
            fail();
        }
        catch (IllegalArgumentException e)
        {
            // setting both group and role level is not allowed
        }
    }

    @Test
    public void testIntegrity()
    {
        CommentImpl comment = new CommentImpl(null, COMMENT_AUTHOR, null, COMMENT_BODY, COMMENT_GROUP_1, null, null, null, null);
        assertNull(comment.getId());
        assertEquals(COMMENT_AUTHOR, comment.getAuthor());
        assertEquals(COMMENT_BODY, comment.getBody());
        assertEquals(COMMENT_GROUP_1, comment.getGroupLevel());
        assertNull(comment.getRoleLevelId());
        assertNotNull(comment.getCreated());

        comment = new CommentImpl(null, COMMENT_AUTHOR, null, COMMENT_BODY, null, COMMENT_ROLE_1, null, null, null);
        assertNull(comment.getId());
        assertEquals(COMMENT_AUTHOR, comment.getAuthor());
        assertEquals(COMMENT_BODY, comment.getBody());
        assertNull(comment.getGroupLevel());
        assertEquals(COMMENT_ROLE_1, comment.getRoleLevelId());
        assertNotNull(comment.getCreated());

        Date now = new Date();
        comment = new CommentImpl(null, COMMENT_AUTHOR, null, COMMENT_BODY, null, COMMENT_ROLE_1, now, null, null);
        assertNull(comment.getId());
        assertEquals(COMMENT_AUTHOR, comment.getAuthor());
        assertEquals(COMMENT_BODY, comment.getBody());
        assertNull(comment.getGroupLevel());
        assertEquals(COMMENT_ROLE_1, comment.getRoleLevelId());
        assertEquals(now, comment.getCreated());
        // The reason we are testing this is that when you specify a create date with no updated date we expect
        // the create date to be the same as the updated date.
        assertEquals(comment.getCreated(), comment.getUpdated());

        Date later = new Date();
        comment = new CommentImpl(null, COMMENT_AUTHOR, null, COMMENT_BODY, null, COMMENT_ROLE_1, now, later, null);
        assertNull(comment.getId());
        assertEquals(COMMENT_AUTHOR, comment.getAuthor());
        assertEquals(COMMENT_BODY, comment.getBody());
        assertNull(comment.getGroupLevel());
        assertEquals(COMMENT_ROLE_1, comment.getRoleLevelId());
        assertEquals(now, comment.getCreated());
        assertEquals(later, comment.getUpdated());

        comment.setId(COMMENT_ID);
        assertEquals(COMMENT_ID, comment.getId());
    }

    @Test
    public void testNullAuthorFullNameIsNull()
    {
        CommentImpl comment = new CommentImpl(null, null, null, COMMENT_BODY, COMMENT_GROUP_1, null, null, null, null);
        assertNull(comment.getAuthorFullName());
        assertNull(comment.getUpdateAuthorFullName());
    }
}
