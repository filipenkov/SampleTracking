package com.atlassian.jira.issue.statistics.util;

import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.search.DefaultReaderCache;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.easymock.classextension.EasyMock;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestOneDimensionalTermHitCollector extends AbstractHitCollectorTestCase
{
    private static final String FIELD = "field";
    private Map result = new HashMap();
    private OneDimensionalTermHitCollector hitCollector;

    private final Document doc = new Document();
    private final Document doc2 = new Document();
    private final Document doc3 = new Document();

    /**
     * Test that the OneDimensionalTermHitCollector tracks the number of hits
     * @throws java.io.IOException if unable to index to RAM
     */
    @Test
    public void testOneDimensionalTermHitCollectorHitCount() throws IOException
    {
        index(doc, FIELD, "1");
        index(doc, SystemSearchConstants.forProject().getIndexField(), "10000");
        index(doc, SystemSearchConstants.forIssueType().getIndexField(), "1");
        index(doc2, FIELD, "22");
        index(doc2, SystemSearchConstants.forProject().getIndexField(), "10000");
        index(doc2, SystemSearchConstants.forIssueType().getIndexField(), "1");
        index(doc3, FIELD, "333");
        index(doc3, SystemSearchConstants.forProject().getIndexField(), "10000");
        index(doc3, SystemSearchConstants.forIssueType().getIndexField(), "1");

        List docsList = EasyList.build();
        collectStats(docsList);
        assertEquals(0, hitCollector.getHitCount());
        assertEquals(0, hitCollector.getIrrelevantCount());

        docsList = EasyList.build(doc);
        collectStats(docsList);
        assertEquals(1, hitCollector.getHitCount());
        assertEquals(0, hitCollector.getIrrelevantCount());

        docsList = EasyList.build(doc, doc2);
        collectStats(docsList);
        assertEquals(2, hitCollector.getHitCount());
        assertEquals(0, hitCollector.getIrrelevantCount());

        docsList = EasyList.build(doc, doc2, doc3);
        collectStats(docsList);
        assertEquals(3, hitCollector.getHitCount());
        assertEquals(0, hitCollector.getIrrelevantCount());

        docsList = EasyList.build(doc, doc, doc2, doc3, doc, doc3);
        collectStats(docsList);
        assertEquals(6, hitCollector.getHitCount());
        assertEquals(0, hitCollector.getIrrelevantCount());
    }

    @Test
    public void testOneDimensionalTermHitCollectorIrrelevantHitCount() throws IOException
    {
        index(doc, FIELD, "1");
        index(doc, SystemSearchConstants.forProject().getIndexField(), "10000");
        index(doc, SystemSearchConstants.forIssueType().getIndexField(), "1");
        index(doc2, "OTHER_FIELD", "22");
        index(doc2, SystemSearchConstants.forProject().getIndexField(), "10000");
        index(doc2, SystemSearchConstants.forIssueType().getIndexField(), "1");
        index(doc3, FIELD, "333");
        index(doc3, SystemSearchConstants.forProject().getIndexField(), "10000");
        index(doc3, SystemSearchConstants.forIssueType().getIndexField(), "1");

        List docsList = EasyList.build(doc, doc2, doc3);
        collectStatsWithIrrelevant(docsList);
        assertEquals(3, hitCollector.getHitCount());
        assertEquals(1, hitCollector.getIrrelevantCount());
    }

    @Test
    public void testOneDimensionalTermHitCollectorIrrelevantHitCountWithRelevantNullValues() throws IOException
    {
        index(doc, FIELD, "1");
        index(doc, SystemSearchConstants.forProject().getIndexField(), "10000");
        index(doc, SystemSearchConstants.forIssueType().getIndexField(), "1");
        index(doc2, "OTHER_FIELD", "22");
        index(doc2, SystemSearchConstants.forProject().getIndexField(), "10000");
        index(doc2, SystemSearchConstants.forIssueType().getIndexField(), "1");
        index(doc3, FIELD, "333");
        index(doc3, SystemSearchConstants.forProject().getIndexField(), "10000");
        index(doc3, SystemSearchConstants.forIssueType().getIndexField(), "1");

        List docsList = EasyList.build(doc, doc2, doc3, doc2);
        collectStatsWithIrrelevantAndRealNullValue(docsList);
        assertEquals(4, hitCollector.getHitCount());
        assertEquals(1, hitCollector.getIrrelevantCount());
    }

    private void collectStats(Collection docs) throws IOException
    {
        final IndexReader reader = writeToRAM(docs);

        final FieldVisibilityManager fieldVisibilityManager = EasyMock.createMock(FieldVisibilityManager.class);
        EasyMock.replay(fieldVisibilityManager);

        hitCollector = new OneDimensionalTermHitCollector(FIELD, result, reader, fieldVisibilityManager, new DefaultReaderCache());

        IndexSearcher searcher = new IndexSearcher(reader);
        Query query = new MatchAllDocsQuery();
        searcher.search(query, hitCollector);
    }

    private void collectStatsWithIrrelevant(Collection docs) throws IOException
    {
        final IndexReader reader = writeToRAM(docs);
        final FieldVisibilityManager fieldVisibilityManager = EasyMock.createMock(FieldVisibilityManager.class);

        EasyMock.expect(fieldVisibilityManager.isFieldHidden(10000L, FIELD, "1")).andReturn(true);
        EasyMock.expect(fieldVisibilityManager.isFieldHidden(10000L, FIELD, "1")).andReturn(false);
        EasyMock.expect(fieldVisibilityManager.isFieldHidden(10000L, FIELD, "1")).andReturn(true);
        EasyMock.expect(fieldVisibilityManager.isFieldHidden(10000L, FIELD, "1")).andReturn(true);
        hitCollector = new OneDimensionalTermHitCollector(FIELD, result, reader, fieldVisibilityManager, new DefaultReaderCache());
        EasyMock.replay(fieldVisibilityManager);

        IndexSearcher searcher = new IndexSearcher(reader);
        Query query = new MatchAllDocsQuery();
        searcher.search(query, hitCollector);
    }

    private void collectStatsWithIrrelevantAndRealNullValue(Collection docs) throws IOException
    {
        final IndexReader reader = writeToRAM(docs);
        final FieldVisibilityManager fieldVisibilityManager = EasyMock.createMock(FieldVisibilityManager.class);

        EasyMock.expect(fieldVisibilityManager.isFieldHidden(10000L, FIELD, "1")).andReturn(true);
        EasyMock.expect(fieldVisibilityManager.isFieldHidden(10000L, FIELD, "1")).andReturn(false);
        EasyMock.expect(fieldVisibilityManager.isFieldHidden(10000L, FIELD, "1")).andReturn(false);
        EasyMock.expect(fieldVisibilityManager.isFieldHidden(10000L, FIELD, "1")).andReturn(true);
        hitCollector = new OneDimensionalTermHitCollector(FIELD, result, reader, fieldVisibilityManager, new DefaultReaderCache());
        EasyMock.replay(fieldVisibilityManager);

        IndexSearcher searcher = new IndexSearcher(reader);
        Query query = new MatchAllDocsQuery();
        searcher.search(query, hitCollector);
    }

}
