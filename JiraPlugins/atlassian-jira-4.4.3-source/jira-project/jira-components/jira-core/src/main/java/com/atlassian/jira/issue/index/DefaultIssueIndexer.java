package com.atlassian.jira.issue.index;

import com.atlassian.jira.index.CompositeResultBuilder;
import com.atlassian.jira.index.Index;
import com.atlassian.jira.index.Index.Operation;
import com.atlassian.jira.index.Index.Result;
import com.atlassian.jira.index.Index.UpdateMode;
import com.atlassian.jira.index.IndexingStrategy;
import com.atlassian.jira.index.MultiThreadedIndexingConfiguration;
import com.atlassian.jira.index.MultiThreadedIndexingStrategy;
import com.atlassian.jira.index.Operations;
import com.atlassian.jira.index.SimpleIndexingStrategy;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.changehistory.ChangeHistoryGroup;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.issue.index.IndexDirectoryFactory.Mode;
import com.atlassian.jira.issue.index.IndexDirectoryFactory.Name;
import com.atlassian.jira.task.context.Context;
import com.atlassian.jira.util.Consumer;
import com.atlassian.jira.util.Function;
import com.atlassian.jira.util.NotNull;
import com.atlassian.jira.util.Supplier;
import com.atlassian.jira.util.collect.EnclosedIterable;
import com.atlassian.util.concurrent.ManagedLock;
import net.jcip.annotations.GuardedBy;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static com.atlassian.jira.index.Operations.newCompletionDelegate;
import static com.atlassian.jira.index.Operations.newCreate;
import static com.atlassian.jira.index.Operations.newDelete;
import static com.atlassian.jira.index.Operations.newUpdate;
import static com.atlassian.jira.util.collect.CollectionUtil.transform;
import static com.atlassian.jira.util.dbc.Assertions.notNull;
import static com.atlassian.util.concurrent.ManagedLocks.weakManagedLockFactory;
import static java.util.Collections.unmodifiableList;

public class DefaultIssueIndexer implements IssueIndexer
{
    private static final MultiThreadedIndexingConfiguration multiThreadedIndexingConfiguration = new MultiThreadedIndexingConfiguration()
    {
        public int minimumBatchSize()
        {
            return 50;
        }

        public int maximumQueueSize()
        {
            return 1000;
        }

        public int noOfThreads()
        {
            return 10;
        }
    };

    //
    // members
    //

    private final CommentRetriever commentRetriever;
    private final ChangeHistoryRetriever changeHistoryRetriever;

    private final Lifecycle lifecycle;

    private final IssueDocumentFactory issueDocumentBuilder = new DefaultIssueDocumentFactory();

    private final CommentDocumentBuilder commentDocumentBuilder = new CommentDocumentBuilder();

    private final ChangeHistoryDocumentBuilder changeHistoryDocumentBuilder = new ChangeHistoryDocumentBuilder();

    /**
     * simple indexing strategy just asks the operation for its result.
     */
    private final IndexingStrategy simpleIndexingStrategy = new SimpleIndexingStrategy();

    private final DocumentCreationStrategy documentCreationStrategy = new IssueLockDocumentCreationStrategy();

    //
    // ctors
    //

    public DefaultIssueIndexer(@NotNull final IndexDirectoryFactory indexDirectoryFactory, @NotNull final CommentRetriever commentRetriever, @NotNull final ChangeHistoryRetriever changeHistoryRetriever)
    {
        lifecycle = new Lifecycle(indexDirectoryFactory);
        this.commentRetriever = notNull("commentRetriever", commentRetriever);
        this.changeHistoryRetriever = notNull("changeHistoryReriever", changeHistoryRetriever);
    }

    //
    // methods
    //

    @GuardedBy("external index read lock")
    public Index.Result deindexIssues(@NotNull final EnclosedIterable<Issue> issues, @NotNull final Context context)
    {
        return perform(issues, simpleIndexingStrategy, context, new IndexOperation()
        {
            public Index.Result perform(final Issue issue, final Context.Task task)
            {
                final Term issueTerm = issueDocumentBuilder.getIdentifyingTerm(issue);
                final Operation delete = newDelete(issueTerm, UpdateMode.INTERACTIVE);
                final Operation onCompletion = newCompletionDelegate(delete, new TaskCompleter(task));
                final CompositeResultBuilder results = new CompositeResultBuilder();
                results.add(lifecycle.getIssueIndex().perform(onCompletion));
                results.add(lifecycle.getCommentIndex().perform(delete));
                results.add(lifecycle.getChangeHistoryIndex().perform(delete));
                return results.toResult();
            }
        });
    }

    @GuardedBy("external index read lock")
    public Index.Result indexIssues(@NotNull final EnclosedIterable<Issue> issues, @NotNull final Context context)
    {
        return perform(issues, simpleIndexingStrategy, context, new IndexIssuesOperation(UpdateMode.INTERACTIVE));
    }

    /**
     * No other index operations should be called while this method is being called
     */
    @GuardedBy("external index write lock")
    public Index.Result indexIssuesBatchMode(@NotNull final EnclosedIterable<Issue> issues, @NotNull final Context context)
    {
        if (issues.size() < multiThreadedIndexingConfiguration.minimumBatchSize())
        {
            return indexIssues(issues, context);
        }

        lifecycle.close();
        lifecycle.setMode(Mode.DIRECT);
        try
        {
            return perform(issues, new MultiThreadedIndexingStrategy(simpleIndexingStrategy, multiThreadedIndexingConfiguration, "IssueIndexer"),
                context, new IndexIssuesOperation(UpdateMode.BATCH));
        }
        finally
        {
            lifecycle.close();
            lifecycle.setMode(Mode.QUEUED);
        }
    }

    @GuardedBy("external index read lock")
    public Index.Result reindexIssues(@NotNull final EnclosedIterable<Issue> issues, @NotNull final Context context)
    {
        return perform(issues, simpleIndexingStrategy, context, new IndexOperation()
        {
            public Index.Result perform(final Issue issue, final Context.Task task)
            {
                final UpdateMode mode = UpdateMode.INTERACTIVE;
                final Documents documents = documentCreationStrategy.get(issue);
                final Term issueTerm = documents.getIdentifyingTerm();
                final Operation update = newUpdate(issueTerm, documents.getIssue(), mode);
                final Operation onCompletion = newCompletionDelegate(update, new TaskCompleter(task));
                final CompositeResultBuilder results = new CompositeResultBuilder();
                results.add(lifecycle.getIssueIndex().perform(onCompletion));
                results.add(lifecycle.getCommentIndex().perform(newUpdate(issueTerm, documents.getComments(), mode)));
                results.add(lifecycle.getChangeHistoryIndex().perform(newUpdate(issueTerm, documents.getChanges(), mode)));
                return results.toResult();
            }
        });
    }

    public void deleteIndexes()
    {
        for (final Index.Manager manager : lifecycle)
        {
            manager.deleteIndexDirectory();
        }
    }

    public IndexSearcher getCommentSearcher()
    {
        return lifecycle.get(Name.COMMENT).getSearcher();
    }

    public IndexSearcher getIssueSearcher()
    {
        return lifecycle.get(Name.ISSUE).getSearcher();
    }

    public IndexSearcher getChangeHistorySearcher()
    {
        return lifecycle.get(Name.CHANGE_HISTORY).getSearcher();
    }

    public Index.Result optimize()
    {
        final CompositeResultBuilder builder = new CompositeResultBuilder();
        for (final Index.Manager manager : lifecycle)
        {
            builder.add(manager.getIndex().perform(Operations.newOptimize()));
        }
        return builder.toResult();
    }

    public void shutdown()
    {
        lifecycle.close();
    }

    public List<String> getIndexPaths()
    {
        return lifecycle.getIndexPaths();
    }

    public String getIndexRootPath()
    {
        return lifecycle.getIndexRootPath();
    }

    /**
     * Perform an {@link IndexOperation} on some {@link EnclosedIterable issues} using a particular 
     * {@link IndexingStrategy strategy}. There is a {@link Context task context} that must be
     * updated to provide feedback to the user.
     * <p>
     * The implementation needs to be thread-safe, as it may be run in parallel and maintain a 
     * composite result to return to the caller.
     * 
     * @param issues the issues to index/deindex/reindex
     * @param strategy single or multi-threaded
     * @param context task context for status feedback
     * @param operation deindex/reindex/index etc.
     * @return the {@link Result} may waited on or not.
     */
    private static Index.Result perform(final EnclosedIterable<Issue> issues, final IndexingStrategy strategy, final Context context, final IndexOperation operation)
    {
        try
        {
            notNull("issues", issues);
            // thread-safe handler for the asynchronous Result 
            final CompositeResultBuilder builder = new CompositeResultBuilder();
            // perform the operation for every issue in the collection
            issues.foreach(new Consumer<Issue>()
            {
                public void consume(final Issue issue)
                {
                    // wrap the updater task in a Job and give it a Context.Task so we can tell the user what's happening
                    final Context.Task task = context.start(issue);
                    // ask the Strategy for the Result, this may be performed on a thread-pool
                    // the result may be a future if asynchronous
                    final Result result = strategy.get(new Supplier<Index.Result>()
                    {
                        public Index.Result get()
                        {
                            // the actual index operation
                            return operation.perform(issue, task);
                        }
                    });
                    builder.add(result);
                }
            });
            return builder.toResult();
        }
        finally
        {
            strategy.close();
        }
    }

    //
    // inner classes
    //

    public interface CommentRetriever extends Function<Issue, List<Comment>>
    {}

    public interface ChangeHistoryRetriever extends Function<Issue, List<ChangeHistoryGroup>>
    {}

    /**
     * Manage the life-cycle of the three index managers.
     */
    private static class Lifecycle implements Iterable<Index.Manager>
    {
        private final AtomicReference<Map<IndexDirectoryFactory.Name, Index.Manager>> ref = new AtomicReference<Map<IndexDirectoryFactory.Name, Index.Manager>>();
        private final IndexDirectoryFactory factory;

        public Lifecycle(@NotNull final IndexDirectoryFactory factory)
        {
            this.factory = notNull("factory", factory);
        }

        public Iterator<Index.Manager> iterator()
        {
            return open().values().iterator();
        }

        void close()
        {
            final Map<IndexDirectoryFactory.Name, Index.Manager> indexes = ref.getAndSet(null);
            if (indexes == null)
            {
                return;
            }
            for (final Index.Manager manager : indexes.values())
            {
                manager.close();
            }
        }

        Map<IndexDirectoryFactory.Name, Index.Manager> open()
        {
            Map<IndexDirectoryFactory.Name, Index.Manager> result = ref.get();
            while (result == null)
            {
                ref.compareAndSet(null, factory.get());
                result = ref.get();
            }
            return result;
        }

        Index getIssueIndex()
        {
            return get(Name.ISSUE).getIndex();
        }

        Index getCommentIndex()
        {
            return get(Name.COMMENT).getIndex();
        }

        Index getChangeHistoryIndex()
        {
            return get(Name.CHANGE_HISTORY).getIndex();
        }

        Index.Manager get(final Name key)
        {
            return open().get(key);
        }

        List<String> getIndexPaths()
        {
            return factory.getIndexPaths();
        }

        String getIndexRootPath()
        {
            return factory.getIndexRootPath();
        }

        void setMode(final Mode type)
        {
            factory.setIndexingMode(type);
        }
    }

    /**
     * Used when indexing to do the actual indexing of an issue.
     */
    private class IndexIssuesOperation implements IndexOperation
    {
        final UpdateMode mode;

        IndexIssuesOperation(final UpdateMode mode)
        {
            this.mode = mode;
        }

        public Index.Result perform(final Issue issue, final Context.Task task)
        {
            final Documents documents = documentCreationStrategy.get(issue);
            final Operation issueCreate = newCreate(documents.getIssue(), mode);
            final Operation onCompletion = newCompletionDelegate(issueCreate, new TaskCompleter(task));
            final CompositeResultBuilder results = new CompositeResultBuilder();
            results.add(lifecycle.getIssueIndex().perform(onCompletion));
            if (!documents.getComments().isEmpty())
            {
                final Operation commentsCreate = newCreate(documents.getComments(), mode);
                results.add(lifecycle.getCommentIndex().perform(commentsCreate));
            }
            if (!documents.getChanges().isEmpty())
            {
                final Operation changeHistoryCreate = newCreate(documents.getChanges(), mode);
                results.add(lifecycle.getChangeHistoryIndex().perform(changeHistoryCreate));
            }
            return results.toResult();
        }
    }

    /**
     * An {@link IndexOperation} performs the actual update to the index for a specific {@link Issue}.
     */
    private interface IndexOperation
    {
        Index.Result perform(Issue issue, Context.Task task);
    }

    private static class TaskCompleter implements Runnable
    {
        private final Context.Task task;

        public TaskCompleter(final Context.Task task)
        {
            this.task = task;
        }

        public void run()
        {
            task.complete();
        }
    }

    interface DocumentCreationStrategy extends Function<Issue, Documents>
    {}

    class Documents
    {
        private final Document issueDocument;
        private final List<Document> comments;
        private final List<Document> changes;
        private final Term term;

        Documents(final Issue issue, final Document issueDocument, final List<Document> comments, final List<Document> changes)
        {
            this.issueDocument = issueDocument;
            this.comments = unmodifiableList(comments);
            this.changes = unmodifiableList(changes);
            term = issueDocumentBuilder.getIdentifyingTerm(issue);
        }

        Document getIssue()
        {
            return issueDocument;
        }

        List<Document> getComments()
        {
            return comments;
        }
        List<Document> getChanges()
        {
            return changes;
        }

        Term getIdentifyingTerm()
        {
            return term;
        }
    }

    /**
     * Get the list of comment documents for indexing
     */
    class CommentDocumentBuilder implements Function<Issue, List<Document>>
    {
        private final CommentDocumentFactory commentDocumentFactory = new DefaultCommentDocumentFactory();

        public List<Document> get(final Issue issue)
        {
            return transform(commentRetriever.get(issue), new Function<Comment, Document>()
            {
                public Document get(final Comment comment)
                {
                    return commentDocumentFactory.get(comment);
                }
            });
        }
    }

     /**
     * Get the list of change documents for indexing
     */
    class ChangeHistoryDocumentBuilder implements Function<Issue, List<Document>>
    {
        private final ChangeHistoryDocumentFactory changeHistoryDocumentFactory = new DefaultChangeHistoryDocumentFactory();

        public List<Document> get(final Issue issue)
        {

            return transform(changeHistoryRetriever.get(issue), new Function<ChangeHistoryGroup, Document>()
            {
                public Document get(final ChangeHistoryGroup changeHistory)
                {
                    return changeHistoryDocumentFactory.get(changeHistory);
                }
            });
        }


    }


    /**
     * Get the documents (issue and comments) for the issue under a lock per issue.
     */
    class IssueLockDocumentCreationStrategy implements DocumentCreationStrategy
    {
        private final com.atlassian.util.concurrent.Function<Issue, ManagedLock> lockManager = weakManagedLockFactory(new Function<Issue, Integer>()
        {
            public Integer get(final Issue issue)
            {
                return issue.getId().intValue();
            }
        });

        public Documents get(final Issue issue)
        {
            return lockManager.get(issue).withLock(new com.atlassian.util.concurrent.Supplier<Documents>()
            {
                public Documents get()
                {
                    return new Documents(issue, issueDocumentBuilder.get(issue), commentDocumentBuilder.get(issue), changeHistoryDocumentBuilder.get(issue));
                }
            });
        }
    }
}
