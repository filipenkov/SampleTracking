package com.atlassian.streams.jira.search;

import java.util.Collection;
import java.util.Date;

import com.atlassian.jira.jql.builder.ConditionBuilder;
import com.atlassian.jira.jql.builder.JqlClauseBuilder;
import com.atlassian.query.clause.Clause;
import com.atlassian.streams.api.ActivityRequest;
import com.atlassian.streams.api.StreamsFilterType.Operator;
import com.atlassian.streams.api.common.Function2;
import com.atlassian.streams.api.common.Pair;

import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;

import static com.atlassian.jira.jql.builder.JqlQueryBuilder.newClauseBuilder;
import static com.atlassian.streams.api.common.Fold.foldl;
import static com.atlassian.streams.api.common.Iterables.drop;
import static com.atlassian.streams.api.common.Pairs.mkPairs;
import static com.atlassian.streams.jira.JiraFilterOptionProvider.ISSUE_TYPE;
import static com.atlassian.streams.spi.Filters.toDate;
import static com.atlassian.streams.spi.StandardStreamsFilterOption.USER;
import static com.atlassian.streams.spi.StandardStreamsFilterOption.ISSUE_KEY;
import static com.atlassian.streams.spi.StandardStreamsFilterOption.PROJECT_KEY;
import static com.atlassian.streams.spi.StandardStreamsFilterOption.UPDATE_DATE;
import static com.google.common.collect.Iterables.get;
import static com.google.common.collect.Iterables.isEmpty;
import static com.google.common.collect.Iterables.transform;

public final class Jql
{
    /**
     * Too few issue results means that we may incorrectly order activity for small requests (e.g. to determine paging
     * in FishEye's activity streams). Hence the minimum query from internal JIRA services is 10.
     */
    public static final int MIN_PAGING_RESULTS = 10;

    static Clause filterByProject(final ActivityRequest request)
    {
        return filterBy(request.getStandardFilters().get(PROJECT_KEY), toProjectClause());
    }

    static Clause filterByDate(final ActivityRequest request)
    {
        return filterBy(request.getStandardFilters().get(UPDATE_DATE.getKey()), toCreatedOrUpdatedClause());
    }

    static Clause filterByIssueType(final ActivityRequest request)
    {
        return filterBy(request.getProviderFilters().get(ISSUE_TYPE), toIssueTypeClause());
    }

    static Clause filterByIssueKey(final ActivityRequest request)
    {
        return filterBy(request.getStandardFilters().get(ISSUE_KEY.getKey()), toIssueKeyClause());
    }

    static Clause filterByUser(final ActivityRequest request)
    {
        return filterBy(request.getStandardFilters().get(USER.getKey()), toReporterClause());
    }

    private static Clause filterBy(Collection<Pair<Operator, Iterable<String>>> filters, Function<Pair<Operator, Iterable<String>>, Clause> toClause)
    {
        return and(transform(filters, toClause));
    }

    private static ToProjectClause toProjectClause()
    {
        return ToProjectClause.INSTANCE;
    }

    private enum ToProjectClause implements Function<Pair<Operator, Iterable<String>>, Clause>
    {
        INSTANCE;

        public Clause apply(Pair<Operator, Iterable<String>> projectFilter)
        {
            JqlClauseBuilder builder = newClauseBuilder();
            switch (projectFilter.first())
            {
                case IS:
                    builder.project().inStrings(ImmutableList.copyOf(projectFilter.second()));
                    break;
                case NOT:
                    builder.project().notInStrings(ImmutableList.copyOf(projectFilter.second()));
                    break;
            }
            return builder.buildClause();
        }
    }

    private static ToIssueKeyClause toIssueKeyClause()
    {
        return ToIssueKeyClause.INSTANCE;
    }

    private enum ToIssueKeyClause implements Function<Pair<Operator, Iterable<String>>, Clause>
    {
        INSTANCE;

        public Clause apply(Pair<Operator, Iterable<String>> projectFilter)
        {
            JqlClauseBuilder builder = newClauseBuilder();
            switch (projectFilter.first())
            {
                case IS:
                    builder.issue().inStrings(ImmutableList.copyOf(projectFilter.second()));
                    break;
                case NOT:
                    builder.issue().notInStrings(ImmutableList.copyOf(projectFilter.second()));
                    break;
            }
            return builder.buildClause();
        }
    }

    private static ToReporterClause toReporterClause()
    {
        return ToReporterClause.INSTANCE;
    }

    private enum ToReporterClause implements Function<Pair<Operator, Iterable<String>>, Clause>
    {
        INSTANCE;

        public Clause apply(Pair<Operator, Iterable<String>> userFilter)
        {
            JqlClauseBuilder builder = newClauseBuilder();
            switch (userFilter.first())
            {
                case IS:
                    builder.reporter().inStrings(ImmutableList.copyOf(userFilter.second()));
                    break;
                case NOT:
                    builder.reporter().notInStrings(ImmutableList.copyOf(userFilter.second()));
                    break;
            }
            return builder.buildClause();
        }
    }

    private static ToIssueTypeClause toIssueTypeClause()
    {
        return ToIssueTypeClause.INSTANCE;
    }

    private enum ToIssueTypeClause implements Function<Pair<Operator, Iterable<String>>, Clause>
    {
        INSTANCE;

        public Clause apply(Pair<Operator, Iterable<String>> issueTypeFilter)
        {
            JqlClauseBuilder builder = newClauseBuilder();
            switch (issueTypeFilter.first())
            {
                case IS:
                    builder.issueType().inStrings(ImmutableList.copyOf(issueTypeFilter.second()));
                    break;
                case NOT:
                    builder.issueType().notInStrings(ImmutableList.copyOf(issueTypeFilter.second()));
                    break;
            }
            return builder.buildClause();
        }
    }

    private static ToCreatedOrUpdatedClauses toCreatedOrUpdatedClause()
    {
        return ToCreatedOrUpdatedClauses.INSTANCE;
    }

    private enum ToCreatedOrUpdatedClauses implements Function<Pair<Operator, Iterable<String>>, Clause>
    {
        INSTANCE;

        public Clause apply(Pair<Operator, Iterable<String>> dateFilter)
        {
            JqlClauseBuilder builder = newClauseBuilder().sub();
            ImmutableList<Date> dates = ImmutableList.copyOf(transform(dateFilter.second(), toDate()));
            switch (dateFilter.first())
            {
                case BEFORE:
                    builder.addClause(or(before(created(), dates))).or().addClause(or(before(updated(), dates)));
                    break;
                case AFTER:
                    builder.addClause(or(after(created(), dates))).or().addClause(or(after(updated(), dates)));
                    break;
                case BETWEEN:
                    Iterable<Pair<Date, Date>> ranges = mkPairs(dates);
                    builder.addClause(or(between(created(), ranges))).or().addClause(or(between(updated(), ranges)));
                    break;
            }
            return builder.endsub().buildClause();
        }
    }

    private static Clause and(Iterable<Clause> clauses)
    {
        if (isEmpty(clauses))
        {
            return newClauseBuilder().buildClause();
        }
        return foldl(drop(1, clauses), newClauseBuilder(get(clauses, 0)), AndClauses.INSTANCE).buildClause();
    }

    private enum AndClauses implements Function2<Clause, JqlClauseBuilder, JqlClauseBuilder>
    {
        INSTANCE;

        public JqlClauseBuilder apply(Clause clause, JqlClauseBuilder builder)
        {
            return builder.and().addClause(clause);
        }
    }

    private static Clause or(Iterable<Clause> clauses)
    {
        if (isEmpty(clauses))
        {
            return newClauseBuilder().buildClause();
        }
        return foldl(drop(1, clauses), newClauseBuilder(get(clauses, 0)), OrClauses.INSTANCE).buildClause();
    }

    private enum OrClauses implements Function2<Clause, JqlClauseBuilder, JqlClauseBuilder>
    {
        INSTANCE;

        public JqlClauseBuilder apply(Clause clause, JqlClauseBuilder builder)
        {
            return builder.or().addClause(clause);
        }
    }

    private static Iterable<Clause> before(Supplier<ConditionBuilder> supplier, Iterable<Date> dates)
    {
        return transform(dates, toBeforeClause(supplier));
    }

    private static Function<Date, Clause> toBeforeClause(Supplier<ConditionBuilder> supplier)
    {
        return new ToBeforeClause(supplier);
    }

    private static final class ToBeforeClause implements Function<Date, Clause>
    {
        private final Supplier<ConditionBuilder> supplier;

        public ToBeforeClause(Supplier<ConditionBuilder> supplier)
        {
            this.supplier = supplier;
        }

        public Clause apply(Date d)
        {
            return supplier.get().ltEq(d.getTime()).buildClause();
        }
    }

    private static Iterable<Clause> after(Supplier<ConditionBuilder> supplier, Iterable<Date> dates)
    {
        return transform(dates, toAfterClause(supplier));
    }

    private static Function<Date, Clause> toAfterClause(Supplier<ConditionBuilder> supplier)
    {
        return new ToAfterClause(supplier);
    }

    private static final class ToAfterClause implements Function<Date, Clause>
    {
        private final Supplier<ConditionBuilder> supplier;

        public ToAfterClause(Supplier<ConditionBuilder> supplier)
        {
            this.supplier = supplier;
        }

        public Clause apply(Date d)
        {
            return supplier.get().gtEq(d.getTime()).buildClause();
        }
    }

    private static Iterable<Clause> between(Supplier<ConditionBuilder> supplier, Iterable<Pair<Date, Date>> dates)
    {
        return transform(dates, toBetweenClause(supplier));
    }

    private static Function<Pair<Date, Date>, Clause> toBetweenClause(Supplier<ConditionBuilder> supplier)
    {
        return new ToBetweenClause(supplier);
    }

    private static final class ToBetweenClause implements Function<Pair<Date, Date>, Clause>
    {
        private final Supplier<ConditionBuilder> supplier;

        public ToBetweenClause(Supplier<ConditionBuilder> supplier)
        {
            this.supplier = supplier;
        }

        public Clause apply(Pair<Date, Date> ds)
        {
            return supplier.get().range(ds.first().getTime(), ds.second().getTime()).buildClause();
        }
    }

    private static Supplier<ConditionBuilder> created()
    {
        return CreatedConditionBuilderSupplier.INSTANCE;
    }

    private enum CreatedConditionBuilderSupplier implements Supplier<ConditionBuilder>
    {
        INSTANCE;

        public ConditionBuilder get()
        {
            return newClauseBuilder().created();
        }
    }

    private static Supplier<ConditionBuilder> updated()
    {
        return UpdatedConditionBuilderSupplier.INSTANCE;
    }

    private enum UpdatedConditionBuilderSupplier implements Supplier<ConditionBuilder>
    {
        INSTANCE;

        public ConditionBuilder get()
        {
            return newClauseBuilder().updated();
        }
    }
}
