package com.atlassian.jira.issue.search;

import com.atlassian.jira.bc.filter.SearchRequestService;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.index.indexers.FieldIndexer;
import com.atlassian.jira.issue.index.indexers.impl.CurrentEstimateIndexer;
import com.atlassian.jira.issue.index.indexers.impl.IssueIdIndexer;
import com.atlassian.jira.issue.index.indexers.impl.IssueKeyIndexer;
import com.atlassian.jira.issue.index.indexers.impl.OriginalEstimateIndexer;
import com.atlassian.jira.issue.index.indexers.impl.ParentIssueIndexer;
import com.atlassian.jira.issue.index.indexers.impl.SecurityIndexer;
import com.atlassian.jira.issue.index.indexers.impl.TimeSpentIndexer;
import com.atlassian.jira.issue.index.indexers.impl.VoterIndexer;
import com.atlassian.jira.issue.index.indexers.impl.VotesIndexer;
import com.atlassian.jira.issue.index.indexers.impl.WatcherIndexer;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.security.IssueSecurityLevelManager;
import com.atlassian.jira.issue.vote.VoteManager;
import com.atlassian.jira.issue.watchers.WatcherManager;
import com.atlassian.jira.jql.ClauseHandler;
import com.atlassian.jira.jql.DefaultClauseHandler;
import com.atlassian.jira.jql.DefaultValuesGeneratingClauseHandler;
import com.atlassian.jira.jql.NoOpClauseHandler;
import com.atlassian.jira.jql.context.AllTextClauseContextFactory;
import com.atlassian.jira.jql.context.ClauseContextFactory;
import com.atlassian.jira.jql.context.IssueIdClauseContextFactory;
import com.atlassian.jira.jql.context.IssueSecurityLevelClauseContextFactory;
import com.atlassian.jira.jql.context.MultiClauseDecoratorContextFactory;
import com.atlassian.jira.jql.context.ProjectCategoryClauseContextFactory;
import com.atlassian.jira.jql.context.SavedFilterClauseContextFactory;
import com.atlassian.jira.jql.context.SimpleClauseContextFactory;
import com.atlassian.jira.jql.context.ValidatingDecoratorContextFactory;
import com.atlassian.jira.jql.permission.ClausePermissionHandler;
import com.atlassian.jira.jql.permission.ClauseSanitiser;
import com.atlassian.jira.jql.permission.DefaultClausePermissionHandler;
import com.atlassian.jira.jql.permission.FieldClausePermissionChecker;
import com.atlassian.jira.jql.permission.IssueClauseValueSanitiser;
import com.atlassian.jira.jql.permission.IssueParentPermissionChecker;
import com.atlassian.jira.jql.permission.NoOpClausePermissionChecker;
import com.atlassian.jira.jql.permission.TimeTrackingPermissionChecker;
import com.atlassian.jira.jql.permission.VotePermissionChecker;
import com.atlassian.jira.jql.permission.WatchPermissionChecker;
import com.atlassian.jira.jql.query.AllTextClauseQueryFactory;
import com.atlassian.jira.jql.query.ClauseQueryFactory;
import com.atlassian.jira.jql.query.CurrentEstimateClauseQueryFactory;
import com.atlassian.jira.jql.query.IssueIdClauseQueryFactory;
import com.atlassian.jira.jql.query.IssueParentClauseQueryFactory;
import com.atlassian.jira.jql.query.IssueSecurityLevelClauseQueryFactory;
import com.atlassian.jira.jql.query.OriginalEstimateClauseQueryFactory;
import com.atlassian.jira.jql.query.ProjectCategoryClauseQueryFactory;
import com.atlassian.jira.jql.query.SavedFilterClauseQueryFactory;
import com.atlassian.jira.jql.query.TimeSpentClauseQueryFactory;
import com.atlassian.jira.jql.query.VoterClauseQueryFactory;
import com.atlassian.jira.jql.query.VotesClauseQueryFactory;
import com.atlassian.jira.jql.query.WatcherClauseQueryFactory;
import com.atlassian.jira.jql.query.WatchesClauseQueryFactory;
import com.atlassian.jira.jql.validator.AllTextValidator;
import com.atlassian.jira.jql.validator.ClauseValidator;
import com.atlassian.jira.jql.validator.CurrentEstimateValidator;
import com.atlassian.jira.jql.validator.IssueIdValidator;
import com.atlassian.jira.jql.validator.IssueParentValidator;
import com.atlassian.jira.jql.validator.IssueSecurityLevelClauseValidator;
import com.atlassian.jira.jql.validator.OperatorUsageValidator;
import com.atlassian.jira.jql.validator.OriginalEstimateValidator;
import com.atlassian.jira.jql.validator.ProjectCategoryValidator;
import com.atlassian.jira.jql.validator.SavedFilterClauseValidator;
import com.atlassian.jira.jql.validator.TimeSpentValidator;
import com.atlassian.jira.jql.validator.UserCustomFieldValidator;
import com.atlassian.jira.jql.validator.VotesValidator;
import com.atlassian.jira.jql.validator.WatchesValidator;
import com.atlassian.jira.jql.values.ProjectCategoryClauseValuesGenerator;
import com.atlassian.jira.jql.values.SavedFilterValuesGenerator;
import com.atlassian.jira.jql.values.SecurityLevelClauseValuesGenerator;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.util.ComponentFactory;
import com.atlassian.jira.util.ComponentLocator;
import com.atlassian.jira.util.collect.CollectionBuilder;
import net.jcip.annotations.GuardedBy;

import java.util.Collection;
import java.util.Collections;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * @since v4.0
 */
public class DefaultSystemClauseHandlerFactory implements SystemClauseHandlerFactory
{
    private final ComponentLocator locator;
    private final ComponentFactory factory;
    private final FieldClausePermissionChecker.Factory fieldClausePermissionHandlerFactory;

    @GuardedBy("this")
    private Collection<SearchHandler> systemClauseSearchHandlers = null;

    public DefaultSystemClauseHandlerFactory(final ComponentLocator locator, final ComponentFactory factory, final FieldClausePermissionChecker.Factory fieldClausePermissionHandlerFactory)
    {
        this.factory = notNull("factory", factory);
        this.locator = notNull("locator", locator);
        this.fieldClausePermissionHandlerFactory = notNull("fieldClausePermissionHandlerFactory", fieldClausePermissionHandlerFactory);
    }

    public synchronized Collection<SearchHandler> getSystemClauseSearchHandlers()
    {
        if (systemClauseSearchHandlers == null)
        {

            /*
              We must create these SearchHandlers outside of the constructor to ensure that we don't end up with a circular
              dependency. For example,

              SavedFilterClauseValidator -> JqlOperandResolver -> QueryRegisry -> DefaultSearchHandlerManager -> SavedFieldClausedValidator.
             */
            systemClauseSearchHandlers = CollectionBuilder.<SearchHandler> list(createSavedFilterSearchHandler(), createIssueKeySearchHandler(),
                createIssueParentSearchHandler(), createCurrentEstimateSearchHandler(), createOriginalEstimateSearchHandler(),
                createTimeSpentSearchHandler(), createSecurityLevelSearchHandler(), createVotesSearchHandler(), createVoterSearchHandler(),
                createWatchesSearchHandler(), createWatcherSearchHandler(), createAllTextSearchHandler(), createProjectCategoryHandler(),
                createSubTaskSearchHandler(), createProgressSearchHandler());

        }
        return systemClauseSearchHandlers;
    }

    private SearchHandler createSavedFilterSearchHandler()
    {
        final ClauseQueryFactory clauseFactory = locator.getComponentInstanceOfType(SavedFilterClauseQueryFactory.class);
        final ClauseValidator clauseValidator = locator.getComponentInstanceOfType(SavedFilterClauseValidator.class);
        final ClauseContextFactory clauseContextFactory = locator.getComponentInstanceOfType(SavedFilterClauseContextFactory.class);

        final ClauseHandler savedFilterClauseHandler = new DefaultValuesGeneratingClauseHandler(SystemSearchConstants.forSavedFilter(),
            clauseFactory, clauseValidator, createNoOpClausePermissionHandler(), decorateWithValidatingContextFactory(clauseContextFactory),
            new SavedFilterValuesGenerator(locator.getComponentInstanceOfType(SearchRequestService.class)));
        final SearchHandler.ClauseRegistration savedFilterClauseRegistration = new SearchHandler.ClauseRegistration(savedFilterClauseHandler);

        // We are returning a SearchHandler not for this implementation but because others may want to include an indexer
        return new SearchHandler(Collections.<FieldIndexer> emptyList(), null, Collections.singletonList(savedFilterClauseRegistration));
    }

    private SearchHandler createIssueKeySearchHandler()
    {
        final ClauseQueryFactory clauseFactory = locator.getComponentInstanceOfType(IssueIdClauseQueryFactory.class);
        final ClauseValidator clauseValidator = locator.getComponentInstanceOfType(IssueIdValidator.class);
        final IssueIdClauseContextFactory.Factory contextCreator = locator.getComponentInstanceOfType(IssueIdClauseContextFactory.Factory.class);
        final ClauseContextFactory clauseContextFactory = decorateWithMultiContextFactory(contextCreator.create(true));

        final ClausePermissionHandler clausePermissionHandler = createNoOpClausePermissionHandler(factory.createObject(IssueClauseValueSanitiser.class));
        final DefaultClauseHandler issueKeySearchHandler = new DefaultClauseHandler(SystemSearchConstants.forIssueKey(), clauseFactory,
            clauseValidator, clausePermissionHandler, clauseContextFactory);
        final SearchHandler.ClauseRegistration savedFilterClauseRegistration = new SearchHandler.ClauseRegistration(issueKeySearchHandler);

        final CollectionBuilder<FieldIndexer> builder = CollectionBuilder.<FieldIndexer> newBuilder(factory.createObject(IssueKeyIndexer.class),
            factory.createObject(IssueIdIndexer.class));
        return new SearchHandler(builder.asList(), null, Collections.singletonList(savedFilterClauseRegistration));
    }

    private SearchHandler createSubTaskSearchHandler()
    {
        final String fieldId = IssueFieldConstants.SUBTASKS;
        final SubTaskManager subTaskManager = locator.getComponentInstanceOfType(SubTaskManager.class);
        final ClausePermissionHandler clausePermissionHandler = new DefaultClausePermissionHandler(new IssueParentPermissionChecker(subTaskManager));
        final ClauseHandler noOpClauseHandler = new NoOpClauseHandler(clausePermissionHandler, fieldId, new ClauseNames(fieldId),
            "jira.jql.validation.field.not.searchable");
        final SearchHandler.ClauseRegistration clauseRegistration = new SearchHandler.ClauseRegistration(noOpClauseHandler);
        return new SearchHandler(Collections.<FieldIndexer> emptyList(), null, Collections.singletonList(clauseRegistration));
    }

    private SearchHandler createProgressSearchHandler()
    {
        final String fieldId = IssueFieldConstants.PROGRESS;
        final ClauseHandler noOpClauseHandler = new NoOpClauseHandler(createTimeTrackingPermissionHandler(), fieldId, new ClauseNames(fieldId),
            "jira.jql.validation.field.not.searchable");
        final SearchHandler.ClauseRegistration clauseRegistration = new SearchHandler.ClauseRegistration(noOpClauseHandler);
        return new SearchHandler(Collections.<FieldIndexer> emptyList(), null, Collections.singletonList(clauseRegistration));
    }

    private SearchHandler createIssueParentSearchHandler()
    {
        final ClauseQueryFactory clauseFactory = locator.getComponentInstanceOfType(IssueParentClauseQueryFactory.class);
        final ClauseValidator clauseValidator = locator.getComponentInstanceOfType(IssueParentValidator.class);
        final IssueIdClauseContextFactory.Factory contextCreator = locator.getComponentInstanceOfType(IssueIdClauseContextFactory.Factory.class);
        final ClauseContextFactory clauseContextFactory = decorateWithMultiContextFactory(contextCreator.create(false));
        final SubTaskManager subTaskManager = locator.getComponentInstanceOfType(SubTaskManager.class);

        final ClausePermissionHandler clausePermissionHandler = new DefaultClausePermissionHandler(new IssueParentPermissionChecker(subTaskManager),
            factory.createObject(IssueClauseValueSanitiser.class));
        final DefaultClauseHandler issueKeySearchHandler = new DefaultClauseHandler(SystemSearchConstants.forIssueParent(), clauseFactory,
            clauseValidator, clausePermissionHandler, clauseContextFactory);
        final SearchHandler.ClauseRegistration clauseRegistration = new SearchHandler.ClauseRegistration(issueKeySearchHandler);

        return new SearchHandler(Collections.<FieldIndexer> singletonList(factory.createObject(ParentIssueIndexer.class)), null,
            Collections.singletonList(clauseRegistration));
    }

    private SearchHandler createCurrentEstimateSearchHandler()
    {
        final ClauseQueryFactory clauseFactory = locator.getComponentInstanceOfType(CurrentEstimateClauseQueryFactory.class);
        final ClauseValidator clauseValidator = locator.getComponentInstanceOfType(CurrentEstimateValidator.class);
        final ClauseContextFactory clauseContextFactory = new SimpleClauseContextFactory();

        final DefaultClauseHandler currentEstimateClauseHandler = new DefaultClauseHandler(SystemSearchConstants.forCurrentEstimate(), clauseFactory,
            clauseValidator, createTimeTrackingPermissionHandler(), clauseContextFactory);
        final SearchHandler.ClauseRegistration currentEstimateClauseRegistration = new SearchHandler.ClauseRegistration(currentEstimateClauseHandler);

        final CollectionBuilder<FieldIndexer> builder = CollectionBuilder.<FieldIndexer> newBuilder(factory.createObject(CurrentEstimateIndexer.class));
        return new SearchHandler(builder.asList(), null, Collections.singletonList(currentEstimateClauseRegistration));
    }

    private SearchHandler createOriginalEstimateSearchHandler()
    {
        final ClauseQueryFactory clauseFactory = locator.getComponentInstanceOfType(OriginalEstimateClauseQueryFactory.class);
        final ClauseValidator clauseValidator = locator.getComponentInstanceOfType(OriginalEstimateValidator.class);
        final ClauseContextFactory clauseContextFactory = new SimpleClauseContextFactory();

        final DefaultClauseHandler originalEstimateClauseHandler = new DefaultClauseHandler(SystemSearchConstants.forOriginalEstimate(),
            clauseFactory, clauseValidator, createTimeTrackingPermissionHandler(), clauseContextFactory);
        final SearchHandler.ClauseRegistration originalEstimateClauseRegistration = new SearchHandler.ClauseRegistration(
            originalEstimateClauseHandler);

        final CollectionBuilder<FieldIndexer> builder = CollectionBuilder.<FieldIndexer> newBuilder(factory.createObject(OriginalEstimateIndexer.class));
        return new SearchHandler(builder.asList(), null, Collections.singletonList(originalEstimateClauseRegistration));
    }

    private SearchHandler createTimeSpentSearchHandler()
    {
        final ClauseQueryFactory clauseFactory = locator.getComponentInstanceOfType(TimeSpentClauseQueryFactory.class);
        final ClauseValidator clauseValidator = locator.getComponentInstanceOfType(TimeSpentValidator.class);
        final ClauseContextFactory clauseContextFactory = new SimpleClauseContextFactory();

        final DefaultClauseHandler timeSpentClauseHandler = new DefaultClauseHandler(SystemSearchConstants.forTimeSpent(), clauseFactory,
            clauseValidator, createTimeTrackingPermissionHandler(), clauseContextFactory);
        final SearchHandler.ClauseRegistration timeSpentClauseRegistration = new SearchHandler.ClauseRegistration(timeSpentClauseHandler);

        final CollectionBuilder<FieldIndexer> builder = CollectionBuilder.<FieldIndexer> newBuilder(factory.createObject(TimeSpentIndexer.class));
        return new SearchHandler(builder.asList(), null, Collections.singletonList(timeSpentClauseRegistration));
    }

    private SearchHandler createSecurityLevelSearchHandler()
    {
        final ClauseQueryFactory clauseFactory = locator.getComponentInstanceOfType(IssueSecurityLevelClauseQueryFactory.class);
        final ClauseValidator clauseValidator = locator.getComponentInstanceOfType(IssueSecurityLevelClauseValidator.class);
        final IssueSecurityLevelManager issueSecurityLevelManager = locator.getComponentInstanceOfType(IssueSecurityLevelManager.class);
        final IssueSecurityLevelClauseContextFactory.Creator issueSecurityLevelClauseContextFactoryCreator = locator.getComponentInstanceOfType(IssueSecurityLevelClauseContextFactory.Creator.class);
        final ClauseContextFactory clauseContextFactory = issueSecurityLevelClauseContextFactoryCreator.create();
        final ClauseHandler securityLevelClauseHandler = new DefaultValuesGeneratingClauseHandler(SystemSearchConstants.forSecurityLevel(),
            clauseFactory, clauseValidator, createClausePermissionHandler(SystemSearchConstants.forSecurityLevel().getFieldId()),
            decorateWithMultiContextFactory(clauseContextFactory), new SecurityLevelClauseValuesGenerator(issueSecurityLevelManager));
        final SearchHandler.ClauseRegistration securityLevelClauseRegistration = new SearchHandler.ClauseRegistration(securityLevelClauseHandler);

        final CollectionBuilder<FieldIndexer> builder = CollectionBuilder.<FieldIndexer> newBuilder(factory.createObject(SecurityIndexer.class));
        return new SearchHandler(builder.asList(), null, Collections.singletonList(securityLevelClauseRegistration));
    }

    private SearchHandler createVotesSearchHandler()
    {
        final ClauseQueryFactory clauseFactory = locator.getComponentInstanceOfType(VotesClauseQueryFactory.class);
        final ClauseValidator clauseValidator = locator.getComponentInstanceOfType(VotesValidator.class);
        final ClauseContextFactory clauseContextFactory = locator.getComponentInstanceOfType(SimpleClauseContextFactory.class);
        final VoteManager voteManager = locator.getComponentInstanceOfType(VoteManager.class);

        final DefaultClauseHandler votesClauseHandler = new DefaultClauseHandler(SystemSearchConstants.forVotes(), clauseFactory, clauseValidator,
            new DefaultClausePermissionHandler(new VotePermissionChecker(voteManager)), clauseContextFactory);
        final SearchHandler.ClauseRegistration votesClauseRegistration = new SearchHandler.ClauseRegistration(votesClauseHandler);

        final CollectionBuilder<FieldIndexer> builder = CollectionBuilder.<FieldIndexer> newBuilder(factory.createObject(VotesIndexer.class));
        return new SearchHandler(builder.asList(), null, Collections.singletonList(votesClauseRegistration));
    }

    private SearchHandler createVoterSearchHandler()
    {
        final ClauseQueryFactory clauseFactory = locator.getComponentInstanceOfType(VoterClauseQueryFactory.class);
        final ClauseValidator clauseValidator = locator.getComponentInstanceOfType(UserCustomFieldValidator.class);
        final ClauseContextFactory clauseContextFactory = locator.getComponentInstanceOfType(SimpleClauseContextFactory.class);
        final VoteManager voteManager = locator.getComponentInstanceOfType(VoteManager.class);

        final DefaultClauseHandler clauseHandler = new DefaultClauseHandler(SystemSearchConstants.forVoters(), clauseFactory, clauseValidator,
            new DefaultClausePermissionHandler(new VotePermissionChecker(voteManager)), clauseContextFactory);
        final SearchHandler.ClauseRegistration clauseRegistration = new SearchHandler.ClauseRegistration(clauseHandler);

        final CollectionBuilder<FieldIndexer> builder = CollectionBuilder.<FieldIndexer> newBuilder(factory.createObject(VoterIndexer.class));
        return new SearchHandler(builder.asList(), null, Collections.singletonList(clauseRegistration));
    }

    private SearchHandler createWatchesSearchHandler()
    {
        final ClauseQueryFactory clauseFactory = locator.getComponentInstanceOfType(WatchesClauseQueryFactory.class);
        final ClauseValidator clauseValidator = locator.getComponentInstanceOfType(WatchesValidator.class);
        final ClauseContextFactory clauseContextFactory = locator.getComponentInstanceOfType(SimpleClauseContextFactory.class);
        final WatcherManager watcherManager = locator.getComponentInstanceOfType(WatcherManager.class);

        final DefaultClauseHandler clauseHandler = new DefaultClauseHandler(SystemSearchConstants.forWatches(), clauseFactory, clauseValidator,
            new DefaultClausePermissionHandler(new WatchPermissionChecker(watcherManager)), clauseContextFactory);
        final SearchHandler.ClauseRegistration clauseRegistration = new SearchHandler.ClauseRegistration(clauseHandler);

        final CollectionBuilder<FieldIndexer> builder = CollectionBuilder.<FieldIndexer> newBuilder(factory.createObject(WatcherIndexer.class));
        return new SearchHandler(builder.asList(), null, Collections.singletonList(clauseRegistration));
    }

    private SearchHandler createWatcherSearchHandler()
    {
        final ClauseQueryFactory clauseFactory = locator.getComponentInstanceOfType(WatcherClauseQueryFactory.class);
        final ClauseValidator clauseValidator = locator.getComponentInstanceOfType(UserCustomFieldValidator.class);
        final ClauseContextFactory clauseContextFactory = locator.getComponentInstanceOfType(SimpleClauseContextFactory.class);
        final WatcherManager watcherManager = locator.getComponentInstanceOfType(WatcherManager.class);

        final DefaultClauseHandler clauseHandler = new DefaultClauseHandler(SystemSearchConstants.forWatchers(), clauseFactory, clauseValidator,
            new DefaultClausePermissionHandler(new WatchPermissionChecker(watcherManager)), clauseContextFactory);
        final SearchHandler.ClauseRegistration clauseRegistration = new SearchHandler.ClauseRegistration(clauseHandler);

        final CollectionBuilder<FieldIndexer> builder = CollectionBuilder.<FieldIndexer> newBuilder(factory.createObject(WatcherIndexer.class));
        return new SearchHandler(builder.asList(), null, Collections.singletonList(clauseRegistration));
    }

    private SearchHandler createAllTextSearchHandler()
    {
        final ClauseQueryFactory clauseFactory = locator.getComponentInstanceOfType(AllTextClauseQueryFactory.class);
        final ClauseValidator clauseValidator = locator.getComponentInstanceOfType(AllTextValidator.class);
        final ClauseContextFactory clauseContextFactory = locator.getComponentInstanceOfType(AllTextClauseContextFactory.class);

        // no permission handler for this clause
        final DefaultClauseHandler allTextClauseHandler = new DefaultClauseHandler(SystemSearchConstants.forAllText(), clauseFactory,
            clauseValidator, new DefaultClausePermissionHandler(NoOpClausePermissionChecker.NOOP_CLAUSE_PERMISSION_CHECKER), clauseContextFactory);
        final SearchHandler.ClauseRegistration allTextClauseRegistration = new SearchHandler.ClauseRegistration(allTextClauseHandler);

        // no indexers for this clause
        return new SearchHandler(Collections.<FieldIndexer> emptyList(), null, Collections.singletonList(allTextClauseRegistration));
    }

    private SearchHandler createProjectCategoryHandler()
    {
        final ClauseQueryFactory clauseFactory = locator.getComponentInstanceOfType(ProjectCategoryClauseQueryFactory.class);
        final ClauseValidator clauseValidator = locator.getComponentInstanceOfType(ProjectCategoryValidator.class);
        final ProjectManager projectManager = locator.getComponentInstanceOfType(ProjectManager.class);
        final ClauseContextFactory clauseContextFactory = locator.getComponentInstanceOfType(ProjectCategoryClauseContextFactory.class);

        final ClauseHandler projectCategoryClauseHandler = new DefaultValuesGeneratingClauseHandler(SystemSearchConstants.forProjectCategory(),
            clauseFactory, clauseValidator, createNoOpClausePermissionHandler(), decorateWithMultiContextFactory(clauseContextFactory),
            new ProjectCategoryClauseValuesGenerator(projectManager));
        final SearchHandler.ClauseRegistration projectCategoryClauseRegistration = new SearchHandler.ClauseRegistration(projectCategoryClauseHandler);

        final CollectionBuilder<FieldIndexer> builder = CollectionBuilder.newBuilder();
        return new SearchHandler(builder.asList(), null, Collections.singletonList(projectCategoryClauseRegistration));
    }

    private ClausePermissionHandler createTimeTrackingPermissionHandler()
    {
        final ApplicationProperties applicationProperties = locator.getComponentInstanceOfType(ApplicationProperties.class);
        return new DefaultClausePermissionHandler(new TimeTrackingPermissionChecker(fieldClausePermissionHandlerFactory, applicationProperties));
    }

    private ClausePermissionHandler createClausePermissionHandler(final String fieldId)
    {
        return new DefaultClausePermissionHandler(fieldClausePermissionHandlerFactory.createPermissionChecker(fieldId));
    }

    private ClausePermissionHandler createNoOpClausePermissionHandler()
    {
        return new DefaultClausePermissionHandler(NoOpClausePermissionChecker.NOOP_CLAUSE_PERMISSION_CHECKER);
    }

    private ClausePermissionHandler createNoOpClausePermissionHandler(final ClauseSanitiser sanitiser)
    {
        return new DefaultClausePermissionHandler(NoOpClausePermissionChecker.NOOP_CLAUSE_PERMISSION_CHECKER, sanitiser);
    }

    private ClauseContextFactory decorateWithMultiContextFactory(final ClauseContextFactory factory)
    {
        final MultiClauseDecoratorContextFactory.Factory multiFactory = locator.getComponentInstanceOfType(MultiClauseDecoratorContextFactory.Factory.class);
        return multiFactory.create(factory);
    }

    private ClauseContextFactory decorateWithValidatingContextFactory(final ClauseContextFactory factory)
    {
        return new ValidatingDecoratorContextFactory(locator.getComponentInstanceOfType(OperatorUsageValidator.class), factory);
    }
}
