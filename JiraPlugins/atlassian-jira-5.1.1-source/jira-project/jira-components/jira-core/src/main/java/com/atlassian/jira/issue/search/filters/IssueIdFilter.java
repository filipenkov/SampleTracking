package com.atlassian.jira.issue.search.filters;

/**
 * This filter will return only the list of issues that match the issue Ids passed in.
 * <p>
 * This is useful for queries that query other data sources, before being combined with
 * an issue search (eg comment or change history).   It was removed with the JQL work undertaken in JIRA 4.0,
 * but has been resurrected to get rid of the too many clauses error that often accompany these searches  see JRA-22453
 *
 * @since v4.3
 */

import com.atlassian.jira.issue.index.DocumentConstants;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.Filter;
import org.apache.lucene.util.DocIdBitSet;

import java.io.IOException;
import java.util.BitSet;
import java.util.Set;

/**
 *
 */
public class IssueIdFilter extends Filter
{
    private final Set<String> issuesIds;

    /**
     * @param issuesIds The list of issue ids to include in this filter
     */
    public IssueIdFilter(Set<String> issuesIds)
    {
        this.issuesIds = issuesIds;
    }

    @Override
    public DocIdSet getDocIdSet(IndexReader indexReader) throws IOException
    {
        BitSet bits = new BitSet(indexReader.maxDoc());

        int[] docs = new int[1];
        int[] freqs = new int[1];

        for (String issueId : issuesIds)
        {
            if (issueId != null)
            {
                TermDocs termDocs = indexReader.termDocs(new Term(DocumentConstants.ISSUE_ID, issueId));
                int count = termDocs.read(docs, freqs);
                if (count > 0)
                    bits.set(docs[0]);
            }
        }
        return new DocIdBitSet(bits);
    }
}
