package com.atlassian.jira.issue.comments.util;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.issue.comment.CommentService;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.util.SimpleErrorCollection;
import net.jcip.annotations.NotThreadSafe;
import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.Hit;
import org.apache.lucene.search.Hits;

import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * This class iterates across a series of comment hits from the comment index.
 */
@NotThreadSafe
public final class LuceneCommentIterator implements CommentIterator
{
    private static final Logger log = Logger.getLogger(LuceneCommentIterator.class);

    private final CommentService commentService;
    private final User user;
    private final Hits hits;
    private final Iterator<Hit> luceneHitsIterator;

    private Comment nextComment = null;

    public LuceneCommentIterator(final User user, final CommentService commentService, final Hits hits)
    {
        this.commentService = notNull("commentService", commentService);
        this.hits = hits;
        this.luceneHitsIterator = (hits == null) ? null : hits.iterator();
        this.user = user;
    }

    public boolean hasNext()
    {
        populateNextCommentIfNull();
        return nextComment != null;
    }

    public Comment nextComment()
    {
        populateNextCommentIfNull();
        if (nextComment == null)
        {
            throw new NoSuchElementException();
        }

        final Comment comment = nextComment;
        nextComment = null;
        return comment;
    }

    private void populateNextCommentIfNull()
    {
        if (nextComment == null)
        {
            pullNextComment();
        }
    }

    private Iterator<Hit> getLuceneHitsIterator()
    {
        return luceneHitsIterator;
    }

    private void pullNextComment()
    {
        final Iterator<Hit> iterator = getLuceneHitsIterator();

        if (iterator == null || !iterator.hasNext())
        {
            nextComment = null;
            return;
        }

        do
        {
            try
            {
                final Hit hit = iterator.next();
                final Document doc = hit.getDocument();
                final Long commentId = new Long(doc.getField(DocumentConstants.COMMENT_ID).stringValue());
                nextComment = commentService.getCommentById(user, commentId, new SimpleErrorCollection());
            }
            catch (final NoSuchElementException e)
            {
                return;
            }
            catch (final IOException e)
            {
                log.error("Failed to retrieve Lucene comment document", e);
                return;
            }
        }
        while (nextComment == null && iterator.hasNext());
    }

    public void close()
    {
        // do nothing
    }

    public Comment next()
    {
        return nextComment();
    }

    public void remove()
    {
        throw new UnsupportedOperationException();
    }

    public int size()
    {
        if (hits != null)
        {
            return hits.length();
        }
        else
        {
            return 0;
        }
    }
}
