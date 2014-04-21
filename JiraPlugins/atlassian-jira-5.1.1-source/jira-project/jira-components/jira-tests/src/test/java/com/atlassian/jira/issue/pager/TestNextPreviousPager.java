
package com.atlassian.jira.issue.pager;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.mock.MockApplicationProperties;
import com.atlassian.jira.mock.MockSearchProvider;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.query.Query;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.index.TermFreqVector;
import org.apache.lucene.index.TermPositions;
import org.apache.lucene.index.TermVectorMapper;
import org.apache.lucene.search.Collector;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class TestNextPreviousPager extends MockControllerTestCase
{
    private User user;


    @Before
    public void setUp() throws Exception
    {
        user = new MockUser("admin");
    }

    @After
    public void tearDown() throws Exception
    {
        user = null;

    }

    @Test
    public void testCacheSizeSetting()
    {

        assertEquals(NextPreviousPager.DEFAULT_CACHE_SIZE, new NextPreviousPager((ApplicationProperties) null).getCacheSize());
        assertEquals(NextPreviousPager.DEFAULT_CACHE_SIZE, new NextPreviousPager(getApplicationProps(null)).getCacheSize());
        assertEquals(NextPreviousPager.DEFAULT_CACHE_SIZE, new NextPreviousPager(getApplicationProps("")).getCacheSize());
        assertEquals(NextPreviousPager.DEFAULT_CACHE_SIZE, new NextPreviousPager(getApplicationProps("   ")).getCacheSize());
        assertEquals(NextPreviousPager.DEFAULT_CACHE_SIZE, new NextPreviousPager(getApplicationProps("abc")).getCacheSize());
        assertEquals(NextPreviousPager.DEFAULT_CACHE_SIZE, new NextPreviousPager(getApplicationProps("-1")).getCacheSize());
        assertEquals(NextPreviousPager.DEFAULT_CACHE_SIZE, new NextPreviousPager(getApplicationProps("0")).getCacheSize());
        assertEquals(NextPreviousPager.DEFAULT_CACHE_SIZE, new NextPreviousPager(getApplicationProps("1")).getCacheSize());
        assertEquals(NextPreviousPager.DEFAULT_CACHE_SIZE, new NextPreviousPager(getApplicationProps("2")).getCacheSize());


        assertEquals(3, new NextPreviousPager(getApplicationProps("3")).getCacheSize());
        assertEquals(4, new NextPreviousPager(getApplicationProps("4")).getCacheSize());
        assertEquals(5, new NextPreviousPager(getApplicationProps("5")).getCacheSize());
        assertEquals(99, new NextPreviousPager(getApplicationProps("99")).getCacheSize());
        assertEquals(999, new NextPreviousPager(getApplicationProps("999")).getCacheSize());
        assertEquals(1000, new NextPreviousPager(getApplicationProps("1000")).getCacheSize());
    }

    @Test
    public void testPreUpdate()
    {
        NextPreviousPager pager = new NextPreviousPager((ApplicationProperties) null);

        assertEquals(0, pager.getCurrentPosition());
        assertEquals(0, pager.getCurrentSize());
        assertNull(pager.getCurrentKey());
        assertNull(pager.getNextKey());
        assertNull(pager.getPreviousKey());
    }

    @Test
    public void testCopyConstructor()
    {
        NextPreviousPager source = new NextPreviousPager(getApplicationProps("3"));
        NextPreviousPager pager = new NextPreviousPager(source);

        assertEquals(3, pager.getCacheSize());
        assertEquals(0, pager.getCurrentPosition());
        assertEquals(0, pager.getCurrentSize());
        assertNull(pager.getCurrentKey());
        assertNull(pager.getNextKey());
        assertNull(pager.getPreviousKey());
    }

    @Test
    public void testNullParams() throws IOException, SearchException
    {

        NextPreviousPager pager = new NextPreviousPager((ApplicationProperties) null);

        pager.update(null, null, null);
        testPreUpdate();
        pager.update(null, null, "TEST-1234");
        testPreUpdate();
        pager.update(null, user, "Test-1234");
        testPreUpdate();

        SearchRequest sr = new SearchRequest();

        pager.update(sr, user, null);
        testPreUpdate();

    }

    @Test
    public void testKeyDoesNotExist() throws IOException, SearchException
    {
        TermDocs docs = mockController.getMock(TermDocs.class);
        docs.next();
        mockController.setReturnValue(false);

        mockController.replay();

        final MockIndexReader indexReader = new MockIndexReader(docs);

        NextPreviousPager pager = getPager(docs, null, indexReader);

        SearchRequest sr = new SearchRequest();

        pager.update(sr, user, "TEST-1234");

        assertEquals("key", indexReader.getTerm().field());
        assertEquals("TEST-1234", indexReader.getTerm().text());

        testPreUpdate();

        mockController.verify();
    }

    private NextPreviousPager getPager(TermDocs docs, final List<Integer> ids, final IndexReader indexReader)
    {

        final SearchProvider searchProvider = new MockSearchProvider(){
            @Override
            public void searchAndSort(Query query, User user, Collector collector, PagerFilter pagerFilter) throws SearchException
            {
                for (Integer id : ids)
                {
                    try
                    {
                        collector.collect(id);
                    }
                    catch (IOException e)
                    {
                        throw new SearchException(e);
                    }
                }
            }
        };

        NextPreviousPager pager = new NextPreviousPager((ApplicationProperties) null)
        {
            @Override
            IndexReader getReader()
            {
                return indexReader;
            }

            @Override
            SearchProvider getSearchProvider()
            {
                return searchProvider;
            }
        };
        return pager;
    }

    private ApplicationProperties getApplicationProps(final String returnVal)
    {
        return new MockApplicationProperties()
        {
            @Override
            public String getDefaultBackedString(String name)
            {
                return returnVal;
            }
        };
    }


}



class MockIndexReader extends IndexReader
{

    private Term term;
    private TermDocs docs;

    public MockIndexReader(TermDocs docs)
    {
        this.docs = docs;
    }


    public TermDocs termDocs() throws IOException
    {
        return docs;
    }

    public Term getTerm()
    {
        return term;
    }

    public TermDocs termDocs(Term term) throws IOException
    {
        this.term = term;

        return docs;
    }

    @Override
    public long getVersion()
    {
        return 9999;
    }

    public Document document(int n, FieldSelector fieldSelector) throws CorruptIndexException, IOException
    {
        return null;
    }


    public TermFreqVector[] getTermFreqVectors(int docNumber) throws IOException
    {
        return new TermFreqVector[0];
    }

    public TermFreqVector getTermFreqVector(int docNumber, String field) throws IOException
    {
        return null;
    }

    public void getTermFreqVector(int docNumber, String field, TermVectorMapper mapper) throws IOException
    {
    }

    public void getTermFreqVector(int docNumber, TermVectorMapper mapper) throws IOException
    {
    }

    public int numDocs()
    {
        return 0;
    }

    public int maxDoc()
    {
        return 0;
    }

    public boolean isDeleted(int n)
    {
        return false;
    }

    public boolean hasDeletions()
    {
        return false;
    }

    public byte[] norms(String field) throws IOException
    {
        return new byte[0];
    }

    public void norms(String field, byte[] bytes, int offset) throws IOException
    {
    }

    protected void doSetNorm(int doc, String field, byte value) throws CorruptIndexException, IOException
    {
    }

    public TermEnum terms() throws IOException
    {
        return null;
    }

    public TermEnum terms(Term t) throws IOException
    {
        return null;
    }

    public int docFreq(Term t) throws IOException
    {
        return 0;
    }

    public TermPositions termPositions() throws IOException
    {
        return null;
    }

    protected void doDelete(int docNum) throws CorruptIndexException, IOException
    {
    }

    protected void doUndeleteAll() throws CorruptIndexException, IOException
    {
    }

    protected void doCommit(Map<String, String> commitUserData) throws IOException
    {
    }

    protected void doClose() throws IOException
    {
    }

    public Collection getFieldNames(FieldOption fldOption)
    {
        return null;
    }
}
