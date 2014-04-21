package com.atlassian.jira.charts;

import com.atlassian.jira.local.ListeningTestCase;
import org.junit.Test;

import static org.junit.Assert.*;

import com.atlassian.core.util.DateUtils;
import com.atlassian.jira.util.LuceneUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.search.Searcher;
import org.easymock.MockControl;
import org.easymock.classextension.MockClassControl;
import org.jfree.data.time.Day;
import org.jfree.data.time.RegularTimePeriod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @since v4.0
 */
public class TestDateRangeTimeChart extends ListeningTestCase
{

    @Test
    public void testCollect()
    {
        final String creationDateConstant = "creationDate";
        final String otherDateConstant = "otherDate";
        final Map<RegularTimePeriod, List<Long>> result = new HashMap<RegularTimePeriod, List<Long>>();
        final List<Document> documents = new ArrayList<Document>();

        Document doc = new Document();
        doc.add(new Field(creationDateConstant, "19700101", Field.Store.YES, Field.Index.UN_TOKENIZED));
        doc.add(new Field(otherDateConstant, "19700102", Field.Store.YES, Field.Index.UN_TOKENIZED));
        documents.add(doc);

        doc = new Document();
        doc.add(new Field(creationDateConstant, "19700101", Field.Store.YES, Field.Index.UN_TOKENIZED));
        doc.add(new Field(otherDateConstant, "19700103", Field.Store.YES, Field.Index.UN_TOKENIZED));
        documents.add(doc);

        doc = new Document();
        doc.add(new Field(creationDateConstant, "19700101", Field.Store.YES, Field.Index.UN_TOKENIZED));
        doc.add(new Field(otherDateConstant, "19700103", Field.Store.YES, Field.Index.UN_TOKENIZED));
        documents.add(doc);

        MockControl mockSearcherControl = MockClassControl.createNiceControl(Searcher.class);
        Searcher mockSearcher = (Searcher) mockSearcherControl.getMock();
        mockSearcherControl.replay();

        final DateRangeTimeChart.DateRangeObjectHitCollector documentHitCollector =
                new DateRangeTimeChart.DateRangeObjectHitCollector(creationDateConstant, otherDateConstant, result, mockSearcher, Day.class, RegularTimePeriod.DEFAULT_TIME_ZONE);

        for (final Document document : documents)
        {
            documentHitCollector.collect(document);
        }


        assertEquals(2, result.size());
        List<Long> values = result.get(RegularTimePeriod.createInstance(Day.class, LuceneUtils.stringToDate("19700102"), RegularTimePeriod.DEFAULT_TIME_ZONE));
        assertEquals(1, values.size());
        assertEquals(DateUtils.DAY_MILLIS, values.get(0).longValue());

        values = result.get(RegularTimePeriod.createInstance(Day.class, LuceneUtils.stringToDate("19700103"), RegularTimePeriod.DEFAULT_TIME_ZONE));
        assertEquals(2, values.size());
        assertEquals(DateUtils.DAY_MILLIS * 2, values.get(0).longValue());
        assertEquals(DateUtils.DAY_MILLIS * 2, values.get(1).longValue());
    }
}
