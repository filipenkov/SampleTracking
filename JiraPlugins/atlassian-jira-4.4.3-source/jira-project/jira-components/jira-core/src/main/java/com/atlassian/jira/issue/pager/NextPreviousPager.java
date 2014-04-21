package com.atlassian.jira.issue.pager;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.issue.index.IssueIndexManager;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.util.NotNull;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.jira.web.bean.PagerFilter;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.document.SetBasedFieldSelector;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.search.HitCollector;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.atlassian.jira.ComponentManager.getComponentInstanceOfType;
import static com.atlassian.jira.issue.index.DocumentConstants.ISSUE_KEY;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;

@edu.umd.cs.findbugs.annotations.SuppressWarnings(value="MT_CORRECTNESS", justification="TODO Needs to be fixed")
public class NextPreviousPager implements Serializable
{
    private static final Logger log = Logger.getLogger(NextPreviousPager.class);

    static final int DEFAULT_CACHE_SIZE = 40;

    private static final FieldSelector FIELD_SELECTOR = new SetBasedFieldSelector(singleton(ISSUE_KEY), emptySet());

    private List<Integer> docIds;

    // Used for quick look up of a docs position
    private Map<Integer, Integer> docIdToPos;

    // Issue key cache
    private List<String> issueKeys;

    private int currentKeyPos = -1;
    private int searchRequestHashCode = -1;

    // The version of the reader that was used to generate the docId list.  Doc ids are specific to a reader.
    private long readerVersion;

    // The offset of the cache from the beginning of the complete doc id list
    private int offset;

    private int cacheSize = DEFAULT_CACHE_SIZE;
    

    public NextPreviousPager(NextPreviousPager that)
    {
        this.currentKeyPos = that.currentKeyPos;
        this.searchRequestHashCode = that.searchRequestHashCode;
        this.readerVersion = that.readerVersion;
        this.offset = that.offset;
        this.cacheSize = that.cacheSize;

        this.docIds = that.docIds == null ? that.docIds : new ArrayList<Integer>(that.docIds);
        this.docIdToPos = that.docIdToPos == null ? that.docIdToPos : new HashMap<Integer, Integer>(that.docIdToPos);
        this.issueKeys = that.issueKeys == null ? that.issueKeys : new ArrayList<String>(that.issueKeys);
    }
    
    public NextPreviousPager(final ApplicationProperties applicationProperties)
    {
        if (applicationProperties != null)
        {
            final String cacheSizeStr = applicationProperties.getDefaultBackedString(APKeys.JIRA_PREVIOUS_NEXT_CACHE_SIZE);
            if (!StringUtils.isBlank(cacheSizeStr))
            {
                try
                {
                    cacheSize = Integer.parseInt(cacheSizeStr);
                    if (cacheSize < 3)
                    {

                        log.warn("Issue key cache size can not be less than 3, Setting it to default.");
                        cacheSize = DEFAULT_CACHE_SIZE;
                    }
                }
                catch (final NumberFormatException nfe)
                {
                    log.warn(
                        "Exception thrown while trying to convert jira-application.properties key '" + APKeys.JIRA_PREVIOUS_NEXT_CACHE_SIZE + "'. Ignoring and setting to default: '" + DEFAULT_CACHE_SIZE + "'",
                        nfe);
                }
            }
        }
    }

    public boolean isHasCurrentKey()
    {
        return getCurrentKey() != null;
    }

    public String getCurrentKey()
    {
        return getKeyForPosition(currentKeyPos);
    }

    public int getCurrentPosition()
    {
        // must add 1 as it is not 0 based
        return currentKeyPos + 1;
    }

    public int getCurrentSize()
    {
        return docIds == null ? 0 : docIds.size();
    }

    int getCacheSize()
    {
        return cacheSize;
    }

    public String getNextKey()
    {
        return (getCurrentKey() == null) ? null : getKeyForPosition(currentKeyPos + 1);
    }

    public String getPreviousKey()
    {
        return (getCurrentKey() == null) ? null : getKeyForPosition(currentKeyPos - 1);
    }

    /**
     * IMPORTANT!!  This method must be called before current, previous or next issue is called.  Otherwise they will return null.
     * <p/>
     * This method keeps track of the current position in a search list and allows the user to easily navigate from issue to another.
     *
     * @param searchRequest The search request to keep track of.
     * @param user          The user performing the search
     * @param currentKey    the current issue that the user is browsing.
     * @throws IOException     if there is a problem while trying to read from the index
     * @throws SearchException thrown if there is an exception thrown during the search
     */
    public synchronized void update(final SearchRequest searchRequest, final User user, final String currentKey) throws IOException, SearchException
    {
        currentKeyPos = -1;
        if (searchRequest == null)
        {
            log.debug("NextPreviousPager being updated with null serachrequest");
            return;
        }

        if (currentKey == null)
        {
            log.debug("NextPreviousPager being updated with null currentKey");
            return;
        }

        final Integer currentIssueDocId = getCurrentDocId(currentKey);
        if (currentIssueDocId == null)
        {
            log.debug("NextPreviousPager being updated with currentKey that does not exist in index - " + currentKey);
            return;
        }

        // If the search request has changed, redo search
        if (searchRequestHashCode != searchRequest.hashCode())
        {
            log.debug("SearchRequest has changed from previous update, reloading doc ids");
            currentKeyPos = populateDocIdsAndGetCurrentKeyPosition(searchRequest, user, currentIssueDocId);
        }
        else
        {
            // if we can get the previous, current and next directly from the key cache, do so (really quick)
            currentKeyPos = getCurrentKeyPositionFromKeyCache(currentKey);
            if (currentKeyPos != -1)
            {
                log.debug("NextPreviousPager conains all needed key in cache, no need to load keys");
                return;
            }

            // If the searcher has been changed out from underneath us, we need to do the search again
            if (readerVersion != getReader().getVersion())
            {
                log.debug("Need to load issue keys, though reader has been swapped out so we need to repopulate doc ids");
                currentKeyPos = populateDocIdsAndGetCurrentKeyPosition(searchRequest, user, currentIssueDocId);
            }
            else
            {
                // We can find the position of the current doc.
                log.debug("Loading issue keys from same reader");
                currentKeyPos = getCurrentKeyPositionFromDocIds(currentIssueDocId);
            }
        }

        // no need to populate chache if key not found.
        if (currentKeyPos != -1)
        {
            populateKeyCache();
        }
    }

    /*
     *  Get the document id of the current issue based off key
     */
    private Integer getCurrentDocId(final String currentKey) throws IOException
    {
        final TermDocs docs = getReader().termDocs(new Term(DocumentConstants.ISSUE_KEY, currentKey));
        return (!docs.next()) ? null : docs.doc();
    }

    /*
     *  Popluate the key cache from the reader based off doc ids
     */
    private void populateKeyCache() throws IOException
    {

        log.debug("Loading issue key cache");
        // populate issue key cache
        offset = currentKeyPos - (cacheSize / 2);
        if (offset < 0)
        {
            offset = 0;
        }
        issueKeys = new ArrayList<String>(cacheSize);

        for (int i = offset; (i < offset + cacheSize) && (i < docIds.size()); i++)
        {
            issueKeys.add(getReader().document(docIds.get(i), FIELD_SELECTOR).get(DocumentConstants.ISSUE_KEY));
        }
    }

    /*
     *  Load all doc ids from the search and mark the current issue position
     */
    private int populateDocIdsAndGetCurrentKeyPosition(@NotNull final SearchRequest searchRequest, final User user, final int currentIssueDocId) throws SearchException, IOException
    {
        Assertions.notNull("searchRequest", searchRequest);
        final NextPreviousHitCollector collector = new NextPreviousHitCollector(currentIssueDocId);

        // initialise docId list
        getSearchProvider().searchAndSort(searchRequest.getQuery(), user, collector, PagerFilter.getUnlimitedFilter());

        docIds = collector.getDocIds();
        docIdToPos = collector.getDocIdToPosMap();
        searchRequestHashCode = searchRequest.hashCode();
        readerVersion = getReader().getVersion();
        return collector.getCurrentKeyPos();
    }

    /**
     * Determines whether we can get all the required keys (previous, current, next) for the current issue from the
     * issue cache.
     *
     * @param currentKey The current issue being viewed
     * @return key position if you can get all required keys from the cache, -1 otherwise
     */
    private int getCurrentKeyPositionFromKeyCache(final String currentKey)
    {
        if (issueKeys == null)
        {
            return -1;
        }

        final int i = issueKeys.indexOf(currentKey);
        final int newPos = offset + i;

        // if it is the first key in the cache and the first of the whole set
        if ((i == 0) && (offset == 0))
        {
            return newPos;
        }

        // if it is not the first and not the last but somewheere in between
        if ((i > 0) && (i < issueKeys.size() - 1))
        {
            return newPos;
        }
        // if it is the last in the cache and last of the whole set
        if ((i == issueKeys.size() - 1) && (offset + issueKeys.size() == docIds.size()))
        {
            return newPos;
        }
        return -1;
    }

    private int getCurrentKeyPositionFromDocIds(final int currentIssueDocId)
    {
        // where does the current doc sit in the doc id list
        return (docIdToPos == null) || !docIdToPos.containsKey(currentIssueDocId) ? -1 : docIdToPos.get(currentIssueDocId);
    }

    private String getKeyForPosition(final int position)
    {
        if ((position < 0) || (position > docIds.size() - 1))
        {
            return null;
        }

        final int relativePos = position - offset;
        if ((relativePos < 0) || (relativePos > issueKeys.size() - 1))
        {
            return null;
        }
        return issueKeys.get(relativePos);
    }

    IndexReader getReader()
    {
        return getComponentInstanceOfType(IssueIndexManager.class).getIssueSearcher().getIndexReader();
    }

    SearchProvider getSearchProvider()
    {
        return getComponentInstanceOfType(SearchProvider.class);
    }

    class NextPreviousHitCollector extends HitCollector
    {
        private final int currentIssueDocId;
        private final List<Integer> docIds = new ArrayList<Integer>();
        private final Map<Integer, Integer> docIdToPos = new HashMap<Integer, Integer>();

        int pos = 0;
        int currentKeyPos = -1;

        NextPreviousHitCollector(final int currentIssueDocId)
        {
            this.currentIssueDocId = currentIssueDocId;
        }

        @Override
        public void collect(final int doc, final float score)
        {
            docIds.add(doc);
            docIdToPos.put(doc, pos);
            if (currentIssueDocId == doc)
            {
                currentKeyPos = pos;
            }
            pos++;
        }

        List<Integer> getDocIds()
        {
            return docIds;
        }

        Map<Integer, Integer> getDocIdToPosMap()
        {
            return docIdToPos;
        }

        int getCurrentKeyPos()
        {
            return currentKeyPos;
        }
    }
}
