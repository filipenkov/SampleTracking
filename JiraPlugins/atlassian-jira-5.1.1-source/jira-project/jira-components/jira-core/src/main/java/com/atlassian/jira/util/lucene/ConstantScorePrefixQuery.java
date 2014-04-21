package com.atlassian.jira.util.lucene;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.ConstantScoreQuery;
import org.apache.lucene.search.PrefixFilter;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

import java.io.IOException;

/**
 * PrefixQuery that doesn't rewrite into a {@link BooleanQuery} with all matching 
 * {@link TermQuery terms} in the field. This query returns a constant score equal 
 * to its boost for all documents with the matching prefix term.
 * <p>
 * This can be significantly cheaper and faster if there are a lot of matching terms.
 * It is very slightly slower if the number of matched terms is one or two.
 * <p>
 * @see http://jira.atlassian.com/browse/JRA-17623
 * @since 4.0
 */
public class ConstantScorePrefixQuery extends PrefixQuery
{
    public ConstantScorePrefixQuery(final Term term)
    {
        super(term);
    }

    @Override
    public Query rewrite(final IndexReader reader) throws IOException
    {
        final Query q = new ConstantScoreQuery(new PrefixFilter(getPrefix()));
        q.setBoost(getBoost());
        return q;
    }
}
