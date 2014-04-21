package com.atlassian.jira.charts;

import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.issue.statistics.util.DocumentHitCollector;
import com.atlassian.jira.util.LuceneUtils;
import com.atlassian.jira.local.ListeningTestCase;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.jfree.data.time.Day;
import org.jfree.data.time.RegularTimePeriod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @since v4.0
 */
public class TestRecentlyCreatedChart extends ListeningTestCase
{
    @Test
    public void testCollect()
    {
        final Map resolvedMap = new HashMap();
        final Map unresolvedMap = new HashMap();

        final List docs = new ArrayList();
        Document doc;

        doc = new Document();
        doc.add(new Field(DocumentConstants.ISSUE_CREATED, "19700102", Field.Store.YES, Field.Index.UN_TOKENIZED));
        doc.add(new Field(DocumentConstants.ISSUE_RESOLUTION, "-1", Field.Store.YES, Field.Index.UN_TOKENIZED));
        docs.add(doc);

        doc = new Document();
        doc.add(new Field(DocumentConstants.ISSUE_CREATED, "19700102", Field.Store.YES, Field.Index.UN_TOKENIZED));
        doc.add(new Field(DocumentConstants.ISSUE_RESOLUTION, "-1", Field.Store.YES, Field.Index.UN_TOKENIZED));
        docs.add(doc);

        doc = new Document();
        doc.add(new Field(DocumentConstants.ISSUE_CREATED, "19700101", Field.Store.YES, Field.Index.UN_TOKENIZED));
        doc.add(new Field(DocumentConstants.ISSUE_RESOLUTION, "Completed", Field.Store.YES, Field.Index.UN_TOKENIZED));
        docs.add(doc);

        doc = new Document();
        doc.add(new Field(DocumentConstants.ISSUE_CREATED, "19700102", Field.Store.YES, Field.Index.UN_TOKENIZED));
        doc.add(new Field(DocumentConstants.ISSUE_RESOLUTION, "Cancelled", Field.Store.YES, Field.Index.UN_TOKENIZED));
        docs.add(doc);


        DocumentHitCollector docHitCollector = new RecentlyCreatedChart.ResolutionSplittingCreatedIssuesHitCollector(resolvedMap, unresolvedMap, null, Day.class, RegularTimePeriod.DEFAULT_TIME_ZONE);

        for (final Iterator i = docs.iterator(); i.hasNext();)
        {
            docHitCollector.collect((Document) i.next());
        }

        assertEquals(2, resolvedMap.size());
        assertEquals(new Integer(1), resolvedMap.get(
                RegularTimePeriod.createInstance(
                        Day.class,
                        LuceneUtils.stringToDate("19700101"),
                        RegularTimePeriod.DEFAULT_TIME_ZONE)));
        assertEquals(new Integer(1), resolvedMap.get(
                RegularTimePeriod.createInstance(
                        Day.class,
                        LuceneUtils.stringToDate("19700102"),
                        RegularTimePeriod.DEFAULT_TIME_ZONE)));


        assertEquals(1, unresolvedMap.size());
        assertEquals(new Integer(2), unresolvedMap.get(
                RegularTimePeriod.createInstance(
                        Day.class,
                        LuceneUtils.stringToDate("19700102"),
                        RegularTimePeriod.DEFAULT_TIME_ZONE)));
    }
}
