package com.atlassian.jira.issue.index;

import com.atlassian.jira.config.ReindexMessageManager;
import com.atlassian.jira.config.util.IndexPathManager;
import com.atlassian.jira.config.util.MockIndexPathManager;
import com.atlassian.jira.config.util.MockIndexingConfiguration;
import com.atlassian.jira.index.MockResult;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.util.SimpleMockIssueFactory;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.util.EasyList;
import org.easymock.EasyMock;
import org.easymock.MockControl;
import org.easymock.internal.AlwaysMatcher;
import org.junit.Before;
import org.junit.Test;

/**
 * This test suite does not extend JiraMockTestCase.
 *
 * @see TestDefaultIssueIndexManager for other Unit Tests on DefaultIndexManager
 * @since v3.13
 */
public class TestDefaultIndexManager extends MockControllerTestCase
{
    static private final IndexPathManager indexPath = new MockIndexPathManager();
    private ReindexMessageManager reindexMessageManager;

    @Before
    public void setUp() throws Exception
    {
        reindexMessageManager = EasyMock.createMock(ReindexMessageManager.class);
        replay(reindexMessageManager);
    }

    @Test
    public void testOptimizeIfNecessary_totalCount() throws Exception
    {

        // Mock IssueIndexer
        final MockControl mockIssueIndexerControl = MockControl.createStrictControl(IssueIndexer.class);
        final IssueIndexer mockIssueIndexer = (IssueIndexer) mockIssueIndexerControl.getMock();
        mockIssueIndexer.reindexIssues(null, null);
        mockIssueIndexerControl.setMatcher(new AlwaysMatcher());
        mockIssueIndexerControl.setReturnValue(new MockResult());
        // Note that the first reindexIssues() doesn't trigger optimize.
        mockIssueIndexer.reindexIssues(null, null);
        mockIssueIndexerControl.setReturnValue(new MockResult());
        // the second reindexIssues() does trigger optimize, because threshold = 2
        mockIssueIndexer.optimize();
        mockIssueIndexerControl.setReturnValue(new MockResult());
        mockIssueIndexerControl.replay();

        final DefaultIndexManager defaultIndexManager = new DefaultIndexManager(new MockIndexingConfiguration().maxReindexes(2),
                mockIssueIndexer, indexPath, reindexMessageManager)
        {
            @Override
            IssueFactory getIssueFactory()
            {
                return new SimpleMockIssueFactory();
            }
        };
        // reindex an issue - this one shouldn't trigger an optimize
        defaultIndexManager.reIndex(new MockIssue(1));
        // reindex another issue - reached the threshold of 2 - now we trigger optimize
        defaultIndexManager.reIndex(new MockIssue(2));

        // Verify Mock IssueIndexer
        mockIssueIndexerControl.verify();
    }

    @Test
    public void testOptimizeIfNecessary_bulkCount() throws Exception
    {

        // Mock IssueIndexer
        final MockControl mockIssueIndexerControl = MockControl.createStrictControl(IssueIndexer.class);
        final IssueIndexer mockIssueIndexer = (IssueIndexer) mockIssueIndexerControl.getMock();
        mockIssueIndexer.reindexIssues(null, null);
        mockIssueIndexerControl.setMatcher(new AlwaysMatcher());
        mockIssueIndexerControl.setReturnValue(new MockResult());
        // optimize() triggered
        mockIssueIndexer.optimize();
        mockIssueIndexerControl.setReturnValue(new MockResult());
        mockIssueIndexer.reindexIssues(null, null);
        mockIssueIndexerControl.setReturnValue(new MockResult());
        // optimize() NOT triggered
        mockIssueIndexerControl.replay();

        final DefaultIndexManager defaultIndexManager = new DefaultIndexManager(new MockIndexingConfiguration()
        {
            @Override
            public int getIssuesToForceOptimize()
            {
                return 3;
            }

            @Override
            public int getMaxReindexes()
            {
                return 0;
            }
        }, mockIssueIndexer, indexPath, reindexMessageManager)
        {
            @Override
            IssueFactory getIssueFactory()
            {
                return new SimpleMockIssueFactory();
            }

        };
        // reindex an issue - we have reached the threshold of 3 issues - so optimize should be called.
        defaultIndexManager.reIndexIssueObjects(EasyList.build(new MockIssue(1), new MockIssue(2), new MockIssue(3)));
        // reindex an issue - less than 3 issues - so optimize should NOT be called.
        defaultIndexManager.reIndexIssueObjects(EasyList.build(new MockIssue(1), new MockIssue(2)));

        // Verify Mock IssueIndexer
        mockIssueIndexerControl.verify();
    }
}
