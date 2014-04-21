package com.atlassian.streams.spi;

import java.util.Collection;
import java.util.Date;
import java.util.NoSuchElementException;
import java.util.Set;

import com.atlassian.streams.api.ActivityObjectType;
import com.atlassian.streams.api.ActivityObjectTypes;
import com.atlassian.streams.api.ActivityRequest;
import com.atlassian.streams.api.ActivityVerb;
import com.atlassian.streams.api.StreamsEntry;
import com.atlassian.streams.api.StreamsFilterType.Operator;
import com.atlassian.streams.api.UserProfile;
import com.atlassian.streams.api.common.Function2;
import com.atlassian.streams.api.common.Option;
import com.atlassian.streams.api.common.Pair;
import com.atlassian.streams.api.common.Pairs;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;

import static com.atlassian.streams.api.StreamsFilterType.Operator.AFTER;
import static com.atlassian.streams.api.StreamsFilterType.Operator.BEFORE;
import static com.atlassian.streams.api.StreamsFilterType.Operator.NOT;
import static com.atlassian.streams.api.common.Fold.foldl;
import static com.atlassian.streams.api.common.Option.none;
import static com.atlassian.streams.api.common.Option.some;
import static com.atlassian.streams.api.common.Pairs.firsts;
import static com.atlassian.streams.api.common.Pairs.mkPairs;
import static com.atlassian.streams.api.common.Predicates.contains;
import static com.atlassian.streams.spi.StandardStreamsFilterOption.ACTIVITY_KEY;
import static com.atlassian.streams.spi.StandardStreamsFilterOption.ACTIVITY_OBJECT_VERB_SEPARATOR;
import static com.atlassian.streams.spi.StandardStreamsFilterOption.ISSUE_KEY;
import static com.atlassian.streams.spi.StandardStreamsFilterOption.PROJECT_KEY;
import static com.atlassian.streams.spi.StandardStreamsFilterOption.UPDATE_DATE;
import static com.atlassian.streams.spi.StandardStreamsFilterOption.USER;
import static com.google.common.base.Predicates.alwaysTrue;
import static com.google.common.base.Predicates.and;
import static com.google.common.base.Predicates.not;
import static com.google.common.base.Predicates.or;
import static com.google.common.collect.Iterables.any;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.get;
import static com.google.common.collect.Iterables.isEmpty;
import static com.google.common.collect.Iterables.size;
import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Sets.difference;
import static com.google.common.collect.Sets.intersection;
import static com.google.common.collect.Sets.union;

/**
 * An assortment of methods for creating {@link Predicate}s and {@link Function}s useful when applying filtering
 * parameters.
 */
public final class Filters
{
    /**
     * Creates a {@code Predicate} from the {@link IS} and {@link NOT} operators in the list of filters.  The returned
     * {@code Predicate} uses the {@link Function}s returned by {@link isIn} and {@link notIn} to construct the sub
     * {@code Predicate}s for each {@code Operator}, and then {@link and}s them all together.
     *
     * @param filters filters to create a predicate for
     * @return {@code Predicate} from the {@code IS} and {@code NOT} filters
     */
    public static Predicate<String> isAndNot(Iterable<Pair<Operator, Iterable<String>>> filters)
    {
        return isAndNot(filters, isIn(), notIn());
    }

    /**
     * Creates a {@code Predicate} from the {@link IS} and {@link NOT} operators in the list of filters.  The returned
     * {@code Predicate} uses the {@link Function}s {@code is} and {@link not} to construct the sub
     * {@code Predicate}s for each {@code Operator}, and then {@link and}s them all together.
     *
     * @param filters filters to create a predicate for
     * @return {@code Predicate} from the {@code IS} and {@code NOT} filters
     */
    public static Predicate<String> isAndNot(Iterable<Pair<Operator, Iterable<String>>> filters, Function<Iterable<String>, Predicate<String>> is, Function<Iterable<String>, Predicate<String>> not)
    {
        Predicate<String> alwaysTrue = Predicates.<String>alwaysTrue();
        if (filters == null)
        {
            return alwaysTrue;
        }
        return foldl(filters, alwaysTrue, new IsAndNot(is, not));
    }

    /**
     * @return a {@code Function} which takes a list of {@code String}s and returns a {@code Predicate} which determines
     * if a {@code String} is in that list of values.
     */
    public static Function<Iterable<String>, Predicate<String>> isIn()
    {
        return IsIn.INSTANCE;
    }

    /**
     * @return a {@code Function} which takes a list of {@code String}s and returns a {@code Predicate} which determines
     * if a {@code String} is not in that list of values.
     */
    public static Function<Iterable<String>, Predicate<String>> notIn()
    {
        return NotIn.INSTANCE;
    }

    private enum IsIn implements Function<Iterable<String>, Predicate<String>>
    {
        INSTANCE;

        public Predicate<String> apply(final Iterable<String> xs)
        {
            return contains(xs);
        }
    }

    private enum NotIn implements Function<Iterable<String>, Predicate<String>>
    {
        INSTANCE;

        public Predicate<String> apply(final Iterable<String> xs)
        {
            return not(IsIn.INSTANCE.apply(xs));
        }
    }

    private static final class IsAndNot implements Function2<Pair<Operator, Iterable<String>>, Predicate<String>, Predicate<String>>
    {
        private final Function<Iterable<String>, Predicate<String>> is;
        private final Function<Iterable<String>, Predicate<String>> not;

        IsAndNot(Function<Iterable<String>, Predicate<String>> is, Function<Iterable<String>, Predicate<String>> not)
        {
            this.is = is;
            this.not = not;
        }

        public Predicate<String> apply(Pair<Operator, Iterable<String>> filter, Predicate<String> predicate)
        {
            switch (filter.first())
            {
                case IS:
                    return and(predicate, is.apply(filter.second()));
                case NOT:
                    return and(predicate, not.apply(ImmutableSet.copyOf(filter.second())));
                default:
                    return predicate;
            }
        }
    }

    /**
     * Creates a {@code Predicate} from the {@link CONTAINS} and {@link DOES_NOT_CONTAIN} operators in the list of filters.  The returned
     * {@code Predicate} uses the {@link Function}s returned by {@link isContaining} and {@link isNotContaining} to construct the sub
     * {@code Predicate}s for each {@code Operator}, and then {@link and}s them all together.
     *
     * @param filters filters to create a predicate for
     * @return {@code Predicate} from the {@code CONTAINS} and {@code DOES_NOT_CONTAIN} filters
     */
    public static Predicate<String> containsAndDoesNotContain(Iterable<Pair<Operator, Iterable<String>>> filters)
    {
        return containsAndDoesNotContain(filters, isContaining(), isNotContaining());
    }

    /**
     * Creates a {@code Predicate} from the {@link CONTAINS} and {@link DOES_NOT_CONTAIN} operators in the list of filters.  The returned
     * {@code Predicate} uses the {@link Function}s {@code contains} and {@link doesNotContain} to construct the sub
     * {@code Predicate}s for each {@code Operator}, and then {@link and}s them all together.
     *
     * @param filters filters to create a predicate for
     * @return {@code Predicate} from the {@code CONTAINS} and {@code DOES_NOT_CONTAIN} filters
     */
    public static Predicate<String> containsAndDoesNotContain(Iterable<Pair<Operator, Iterable<String>>> filters, Function<Iterable<String>, Predicate<String>> contains, Function<Iterable<String>, Predicate<String>> doesNotContain)
    {
        Predicate<String> alwaysTrue = Predicates.<String>alwaysTrue();
        if (filters == null)
        {
            return alwaysTrue;
        }
        return foldl(filters, alwaysTrue, new ContainsAndDoesNotContain(contains, doesNotContain));
    }

    /**
     * @return a {@code Function} which takes a list of {@code String}s and returns a {@code Predicate} which determines
     * if a {@code String} contains any value in that list.
     */
    public static Function<Iterable<String>, Predicate<String>> isContaining()
    {
        return IsContaining.INSTANCE;
    }

    /**
     * @return a {@code Function} which takes a list of {@code String}s and returns a {@code Predicate} which determines
     * if a {@code String} contains any value in that list.
     */
    public static Function<Iterable<String>, Predicate<String>> isNotContaining()
    {
        return IsNotContaining.INSTANCE;
    }

    private enum IsContaining implements Function<Iterable<String>, Predicate<String>>
    {
        INSTANCE;

        public Predicate<String> apply(final Iterable<String> xs)
        {
            return new ContainsPredicate(xs);
        }

        private final class ContainsPredicate implements Predicate<String>
        {
            private final Iterable<String> xs;

            private ContainsPredicate(Iterable<String> xs)
            {
                this.xs = xs;
            }

            public boolean apply(String input)
            {
                //input will be null if a StreamsEntry does not have a description
                if (input != null)
                {
                    for (String x : xs)
                    {
                        if (input.toLowerCase().contains(x.toLowerCase()))
                        {
                            return true;
                        }
                    }
                }
                return false;
            }

            @Override
            public String toString()
            {
                return "isContaining(" + Iterables.toString(xs) + ")";
            }
        }

        @Override
        public String toString()
        {
            return "isContaining()";
        }
    }

    private enum IsNotContaining implements Function<Iterable<String>, Predicate<String>>
    {
        INSTANCE;

        public Predicate<String> apply(final Iterable<String> xs)
        {
            return not(IsContaining.INSTANCE.apply(xs));
        }
    }

    private static final class ContainsAndDoesNotContain implements Function2<Pair<Operator, Iterable<String>>, Predicate<String>, Predicate<String>>
    {
        private final Function<Iterable<String>, Predicate<String>> contains;
        private final Function<Iterable<String>, Predicate<String>> doesNotContain;

        ContainsAndDoesNotContain(Function<Iterable<String>, Predicate<String>> contains, Function<Iterable<String>, Predicate<String>> doesNotContain)
        {
            this.contains = contains;
            this.doesNotContain = doesNotContain;
        }

        public Predicate<String> apply(Pair<Operator, Iterable<String>> filter, Predicate<String> predicate)
        {
            switch (filter.first())
            {
                case CONTAINS:
                    return and(predicate, contains.apply(filter.second()));
                case DOES_NOT_CONTAIN:
                    return and(predicate, doesNotContain.apply(ImmutableSet.copyOf(filter.second())));
                default:
                    return predicate;
            }
        }
    }

    /**
     * @return a {@code Function} which takes a list of {@code String}s and returns a {@code Predicate} which determines
     * if a {@code String} matches the {@code Predicate} created from {@code f} in a case insensitive way.  An example
     * of this is in practice is allowing a case insensitive check if a {@code String} is in a list {@code String}s,
     * {@code caseInsensitive(isIn())}.
     */
    public static Function<Iterable<String>, Predicate<String>> caseInsensitive(final Function<Iterable<String>, Predicate<String>> f)
    {
        return new AsCaseInsensitive(f);
    }

    private static final class AsCaseInsensitive implements Function<Iterable<String>, Predicate<String>>
    {
        private final class CaseInsensitive implements Predicate<String>
        {
            private final Predicate<String> p;

            private CaseInsensitive(Iterable<String> xs)
            {
                p = f.apply(transform(xs, ToUpperCase.INSTANCE));
            }

            public boolean apply(String x)
            {
                return p.apply(x.toUpperCase());
            }
        }

        private final Function<Iterable<String>, Predicate<String>> f;

        private AsCaseInsensitive(Function<Iterable<String>, Predicate<String>> f)
        {
            this.f = f;
        }

        public Predicate<String> apply(final Iterable<String> xs)
        {
            return new CaseInsensitive(xs);
        }
    }

    private enum ToUpperCase implements Function<String, String>
    {
        INSTANCE;

        public String apply(String x)
        {
            return x.toUpperCase();
        }
    }

    /**
     * Creates a {@code Predicate} to determine if any of several usernames match the filters from the request.
     * If the request does not contain any author filters, the returned {@code Predicate} will return {@code true}
     * for every username. Only a single match is needed for the {@code Predicate} to return true.
     *
     * @param request request to generate the user predicate from
     * @return {@code Predicate} to determine if a set of usernames match the filters from the request
     */
    public static Predicate<Iterable<String>> anyInUsers(ActivityRequest request)
    {
        if (isEmpty(getIsValues(request.getStandardFilters().get(USER.getKey()))))
        {
            return alwaysTrue();
        }
        return new AnyInUsers(request);
    }

    private static final class AnyInUsers implements Predicate<Iterable<String>>
    {
        private final Predicate<String> inUsers;

        public AnyInUsers(ActivityRequest request)
        {
            inUsers = inUsers(request);
        }

        public boolean apply(Iterable<String> users)
        {
            return any(users, inUsers);
        }
    }

    /**
     * Creates a {@code Predicate} to determine if a user name matches the filters from the request.  If the request
     * does not contain any user filters, the returned {@code Predicate} will return {@code true} for every user name.
     *
     * @param request request to generate the user name predicate from
     * @return {@code Predicate} to determine if a user name matches the filters from the request
     */
    public static Predicate<String> inUsers(final ActivityRequest request)
    {
        return isAndNot(request.getStandardFilters().get(USER.getKey()));
    }

    /**
     * {@code Predicate} for removing any {@code StreamsEntry}s that match notted "filterUser" filters.
     *
     * @param request the {@code ActivityRequest} containing the filters
     * @return the {@code Predicate}
     */
    public static Predicate<Iterable<String>> notInUsers(ActivityRequest request)
    {
        return new NotInUsers(request);
    }

    private static final class NotInUsers implements Predicate<Iterable<String>>
    {
        private final Iterable<String> nottedUsers;

        public NotInUsers(ActivityRequest request)
        {
            nottedUsers = getAllValues(NOT, request.getStandardFilters().get(USER.getKey()));
        }

        public boolean apply(Iterable<String> users)
        {
            return intersection(ImmutableSet.copyOf(nottedUsers), ImmutableSet.copyOf(users)).isEmpty();
        }
    }

    /**
     * {@code Predicate} for filtering involving a {@code StreamEntry}'s authors.
     *
     * @param authorPredicate the author predicate to filter on
     * @return the {@code Predicate}
     */
    public static Predicate<StreamsEntry> entryAuthors(Predicate<Iterable<String>> authorPredicate)
    {
        return new EntryAuthors(authorPredicate);
    }

    private static final class EntryAuthors implements Predicate<StreamsEntry>
    {
        private final Predicate<Iterable<String>> authorPredicate;

        public EntryAuthors(Predicate<Iterable<String>> authorPredicate)
        {
            this.authorPredicate = authorPredicate;
        }

        public boolean apply(StreamsEntry entry)
        {
            return authorPredicate.apply(transform(entry.getAuthors(), getUsername()));
        }
    }

    private static Function<UserProfile, String> getUsername()
    {
        return GetUsername.INSTANCE;
    }

    private enum GetUsername implements Function<UserProfile, String>
    {
        INSTANCE;

        public String apply(UserProfile profile)
        {
            return profile.getUsername();
        }
    }

    /**
     * Creates a {@code Predicate} to determine if a project key matches the filters from the request.  If the request
     * does not contain any project key filters, the returned {@code Predicate} will return {@code true} for every
     * project key.
     *
     * @param request request to generate the project key predicate from
     * @return {@code Predicate} to determine if a project key matches the filters from the request
     */
    public static Predicate<String> inProjectKeys(ActivityRequest request)
    {
        return isAndNot(request.getStandardFilters().get(PROJECT_KEY));
    }

    /**
     * Creates a {@code Predicate} to determine if an issue key matches the filters from the request.  If the request
     * does not contain any issue key filters, the returned {@code Predicate} will return {@code true} for every issue
     * key.
     *
     * @param request request to generate the issue key predicate from
     * @return {@code Predicate} to determine if an issue key matches the filters from the request
     */
    public static Predicate<String> inIssueKeys(ActivityRequest request)
    {
        return isAndNot(request.getStandardFilters().get(ISSUE_KEY.getKey()));
    }

    /**
     * Creates a {@code Predicate} to determine if an issue key matches the filters from the request.  If the request
     * does not contain any issue key filters, the returned {@code Predicate} will return {@code true} for every issue
     * key.
     *
     * @param request request to generate the issue key predicate from
     * @param is {@code Function} which is used to construct the {@code Predicate} for checking if an issue key is in
     *           the list of issue keys from the filter
     * @param not {@code Function}s which is used to construct the {@code Predicate} for checking if an issue key is not
     *           in the list of issue keys from the filter
     * @return {@code Predicate} to determine if an issue key matches the filters from the request
     */
    public static Predicate<String> inIssueKeys(ActivityRequest request, Function<Iterable<String>, Predicate<String>> is, Function<Iterable<String>, Predicate<String>> not)
    {
        return isAndNot(request.getStandardFilters().get(ISSUE_KEY.getKey()), is, not);
    }

    /**
     * Creates a {@code Predicate} to determine if an {@code ActivityObjectType}/{@code ActivityVerb} pair matches the
     * activity filters from the request. If the request does not contain any activity filters, the returned
     * {@code Predicate} will return {@code true} for every combination.
     *
     * @param request request to generate the activity predicate from
     * @return {@code Predicate} to determine if a activity matches the filters from the request
     */
    public static Predicate<Pair<ActivityObjectType, ActivityVerb>> inActivities(ActivityRequest request)
    {
        if (!providerFilterExists(request, ACTIVITY_KEY))
        {
            return alwaysTrue();
        }
        return new InActivities(request.getProviderFilters().get(ACTIVITY_KEY));
    }

    private static final class InActivities implements Predicate<Pair<ActivityObjectType, ActivityVerb>>
    {
        private final Predicate<String> inActivities;

        public InActivities(Collection<Pair<Operator, Iterable<String>>> activities)
        {
            this.inActivities = isAndNot(activities);
        }

        public boolean apply(Pair<ActivityObjectType, ActivityVerb> activity)
        {
            return inActivities.apply(activity.first().key() + ACTIVITY_OBJECT_VERB_SEPARATOR + activity.second().key());
        }
    }

    /**
     * Creates a {@code Predicate} to determine if an {@code ActivityObjectType}/{@code ActivityVerb} pair
     * matches the activity filters from the request. If the request does not contain any activity filters, the returned
     * {@code Predicate} will return {@code true} for every combination.
     *
     * @param request request to generate the activity predicate from
     * @return {@code Predicate} to determine if a activity matches the filters from the request
     */
    public static Predicate<Option<Pair<ActivityObjectType, ActivityVerb>>> inOptionActivities(ActivityRequest request)
    {
        if (!providerFilterExists(request, ACTIVITY_KEY))
        {
            return alwaysTrue();
        }
        return new InOptionActivities(request.getProviderFilters().get(ACTIVITY_KEY));
    }

    private static final class InOptionActivities implements Predicate<Option<Pair<ActivityObjectType, ActivityVerb>>>
    {
        private final Predicate<String> inActivities;

        public InOptionActivities(Collection<Pair<Operator, Iterable<String>>> activities)
        {
            this.inActivities = isAndNot(activities);
        }

        public boolean apply(Option<Pair<ActivityObjectType, ActivityVerb>> activity)
        {
            for (Pair<ActivityObjectType, ActivityVerb> activityPair : activity)
            {
                return inActivities.apply(
                    activityPair.first().key() + ACTIVITY_OBJECT_VERB_SEPARATOR + activityPair.second().key());
            }

            return false;
        }

        @Override
        public String toString()
        {
            return String.format("inOptionActivities(%s)", inActivities);
        }
    }

    /**
     * Creates a {@code Predicate} to determine if a {@code StreamsEntry}'s object-type/verb matches the activity
     * filters from the request.  If the request does not contain any activity filters, the returned {@code Predicate}
     * will return {@code true} for every activity combination.
     *
     * @param request request to generate the activity predicate from
     * @return {@code Predicate} to determine if a activity matches the filters from the request
     */
    public static Predicate<StreamsEntry> entriesInActivities(ActivityRequest request)
    {
        if (!providerFilterExists(request, ACTIVITY_KEY))
        {
            return alwaysTrue();
        }
        return new EntriesInActivities(request.getProviderFilters().get(ACTIVITY_KEY));
    }

    private static final class EntriesInActivities implements Predicate<StreamsEntry>
    {
        private final Predicate<Pair<ActivityObjectType, ActivityVerb>> inActivities;

        public EntriesInActivities(Collection<Pair<Operator, Iterable<String>>> activities)
        {
            this.inActivities = new InActivities(activities);
        }

        public boolean apply(StreamsEntry entry)
        {
            return any(
                transform(
                    ActivityObjectTypes.getActivityObjectTypes(entry.getActivityObjects()),
                    Pairs.<ActivityObjectType, ActivityVerb>pairWith(entry.getVerb())),
                inActivities);
        }
    }

    private static boolean providerFilterExists(ActivityRequest request, String filterKey)
    {
        return filterExists(request.getProviderFilters(), filterKey);
    }

    private static boolean filterExists(Multimap<String, Pair<Operator, Iterable<String>>> filters, String filterKey)
    {
        return filters.get(filterKey) != null && !filters.get(filterKey).isEmpty();
    }

    /**
     * Determines the authors based on the {@code ActivityRequest}.
     *
     * @param request the {@code ActivityRequest} containing the filter information
     * @return the authors to be searched
     */
    public static Iterable<String> getAuthors(ActivityRequest request)
    {
        return getIsValues(request.getStandardFilters().get(USER.getKey()));
    }

    /**
     * Determines the project keys based on the {@code ActivityRequest}.
     *
     * @param request the {@code ActivityRequest} containing the filter information
     * @return the project keys to be searched
     */
    public static Iterable<String> getProjectKeys(ActivityRequest request)
    {
        return getIsValues(request.getStandardFilters().get(PROJECT_KEY));
    }

    /**
     * Determines the project keys to exclude based on the {@code ActivityRequest}.
     *
     * @param request the {@code ActivityRequest} containing the filter information
     * @return the project keys to be excluded
     */
    public static Iterable<String> getNotProjectKeys(ActivityRequest request)
    {
        return getNotValues(request.getStandardFilters().get(PROJECT_KEY));
    }

    /**
     * Determines the issue keys based on the {@code ActivityRequest}
     * @param request the {@code ActivityRequest} containing the filter information
     * @return the issue keys to be searched
     */
    public static Iterable<String> getIssueKeys(ActivityRequest request)
    {
        return getIsValues(request.getStandardFilters().get(ISSUE_KEY.getKey()));
    }

    /**
     * Determines the issue keys to exclude based on the {@code ActivityRequest}
     * @param request the {@code ActivityRequest} containing the filter information
     * @return the issue keys to be excluded
     */
    public static Iterable<String> getNotIssueKeys(ActivityRequest request)
    {
        return getNotValues(request.getStandardFilters().get(ISSUE_KEY.getKey()));
    }

    /**
     * Determines the minimum date filter based on the {@code ActivityRequest}. Returns null if none is specified.
     *
     * @param request the {@code ActivityRequest} containing the filter information
     * @return the minimum date
     */
    public static Option<Date> getMinDate(ActivityRequest request)
    {
        Collection<Pair<Operator, Iterable<String>>> filters = request.getStandardFilters().get(UPDATE_DATE.getKey());

        //first try with BEFORE operator
        Option<Long> minDate = parseLongSafely(getFirstValue(AFTER, filters));

        if (!minDate.isDefined() && size(filters) > 0)
        {
            return getDateRange(filters).map(Pairs.<Date, Date>first());
        }
        return minDate.map(toDate);
    }

    /**
     * Determines the requested activity object types based on the {@code ActivityRequest}.
     *
     * @param request the {@code ActivityRequest} containing the filter information
     * @param activities the set of possible activities
     * @return the {@link ActivityObjectType}s to be searched
     */
    public static Iterable<ActivityObjectType> getRequestedActivityObjectTypes(ActivityRequest request, Iterable<Pair<ActivityObjectType, ActivityVerb>> activities)
    {
        return firsts(filter(activities, inActivities(request)));
    }

    public static Function<ActivityRequest, Iterable<Pair<ActivityObjectType, ActivityVerb>>> inSupportedActivities(final Iterable<Pair<ActivityObjectType, ActivityVerb>> supported)
    {
        return new Function<ActivityRequest, Iterable<Pair<ActivityObjectType, ActivityVerb>>>()
        {
            public Iterable<Pair<ActivityObjectType, ActivityVerb>> apply(ActivityRequest activityRequest)
            {
                return filter(supported, inActivities(activityRequest));
            }
        };
    }

    private static Function<Long,Date> toDate = new Function<Long, Date>()
    {
        public Date apply(final Long l)
        {
            return new Date(l);
        }
    };

    /**
     * Determines the maximum date filter based on the {@code ActivityRequest}. Returns null if none is specified.
     *
     * @param request the {@code ActivityRequest} containing the filter information
     * @return the maximum date
     */
    public static Option<Date> getMaxDate(ActivityRequest request)
    {
        Collection<Pair<Operator, Iterable<String>>> filters = request.getStandardFilters().get(UPDATE_DATE.getKey());

        //first try with BEFORE operator
        Option<Long> maxDate = parseLongSafely(getFirstValue(BEFORE, filters));

        if (!maxDate.isDefined() && size(filters) > 0)
        {
            return getDateRange(filters).map(Pairs.<Date, Date>second());
        }
        return maxDate.map(toDate);
    }

    /**
     * Given a collection of date filters, extract the first date range.
     *
     * @param filters the date filters
     * @return the first date range found
     */
    private static Option<Pair<Date, Date>> getDateRange(Collection<Pair<Operator, Iterable<String>>> filters)
    {
        Pair<Operator, Iterable<String>> firstFilter = get(filters, 0);
        Iterable<Date> dates = transform(firstFilter.second(), toDate());

        if (size(dates) < 2)
        {
            //no range exists
            return none();
        }

        Iterable<Pair<Date, Date>> ranges = mkPairs(dates);
        return some(get(ranges, 0));
    }

    private static Option<Long> parseLongSafely(Option<String> minDate)
    {
        for (String min : minDate)
        {
            return some(Long.parseLong(min));
        }
        return none();
    }

    /**
     * Creates a {@code Predicate} to determine if a date matches the filters from the request.  If the request
     * does not contain any update date filters, the returned {@code Predicate} will return {@code true} for every date.
     *
     * @param request request to generate the updated date predicate from
     * @return {@code Predicate} to determine if a date matches the filters from the request
     */
    public static Predicate<Date> inDateRange(ActivityRequest request)
    {
        Predicate<Date> alwaysTrue = Predicates.<Date>alwaysTrue();
        return foldl(request.getStandardFilters().get(UPDATE_DATE.getKey()), alwaysTrue, ContainsDate.INSTANCE);
    }

    private enum ContainsDate implements Function2<Pair<Operator, Iterable<String>>, Predicate<Date>, Predicate<Date>>
    {
        INSTANCE;

        public Predicate<Date> apply(Pair<Operator, Iterable<String>> filter, Predicate<Date> predicate)
        {
            Iterable<Date> dates = transform(filter.second(), toDate());
            switch (filter.first())
            {
                case BEFORE:
                    return and(predicate, or(transform(dates, ToBeforePredicate.INSTANCE)));
                case AFTER:
                    return and(predicate, or(transform(dates, ToAfterPredicate.INSTANCE)));
                case BETWEEN:
                    Iterable<Pair<Date, Date>> ranges = mkPairs(dates);
                    return and(predicate, or(transform(ranges, ToBetweenPredicate.INSTANCE)));
                default:
                    return predicate;
            }
        }
    }

    private enum ToBeforePredicate implements Function<Date, Predicate<Date>>
    {
        INSTANCE;

        public Predicate<Date> apply(Date date)
        {
            return new BeforePredicate(date);
        }

        private static final class BeforePredicate implements Predicate<Date>
        {
            private final Date date;

            public BeforePredicate(Date date)
            {
                this.date = date;
            }

            public boolean apply(Date input)
            {
                return input.before(date);
            }
        }
    }

    private enum ToAfterPredicate implements Function<Date, Predicate<Date>>
    {
        INSTANCE;

        public Predicate<Date> apply(Date date)
        {
            return new AfterPredicate(date);
        }

        private static final class AfterPredicate implements Predicate<Date>
        {
            private final Date date;

            public AfterPredicate(Date date)
            {
                this.date = date;
            }

            public boolean apply(Date input)
            {
                return input.after(date);
            }
        }
    }

    private enum ToBetweenPredicate implements Function<Pair<Date, Date>, Predicate<Date>>
    {
        INSTANCE;

        public Predicate<Date> apply(Pair<Date, Date> range)
        {
            return new BetweenPredicate(range);
        }

        private static final class BetweenPredicate implements Predicate<Date>
        {
            private final Pair<Date, Date> range;

            public BetweenPredicate(Pair<Date, Date> range)
            {
                this.range = range;
            }

            public boolean apply(Date input)
            {
                return input.after(range.first()) && input.before(range.second());
            }
        }
    }

    /**
     * Extracts the set of values to include from the request.  It does this by getting the set of values that should be
     * included - those with the "is" operator - and the set of values that should not be included - those with the
     * "is not" operator - and taking the difference.
     *
     * Useful only when you want to know the set of values to include.  The result of this method does not tell you
     * anything about which values *not* to include.
     */
    public static Set<String> getIsValues(Iterable<Pair<Operator, Iterable<String>>> filters)
    {
        Pair<Set<String>, Set<String>> isAndNotValues = getIsAndNotValues(filters);
        return difference(isAndNotValues.first(), isAndNotValues.second());
    }

    public static Set<String> getNotValues(Iterable<Pair<Operator, Iterable<String>>> filters)
    {
        Pair<Set<String>, Set<String>> isAndNotValues = getIsAndNotValues(filters);
        return isAndNotValues.second();
    }

    public static Pair<Set<String>, Set<String>> getIsAndNotValues(Iterable<Pair<Operator, Iterable<String>>> filters)
    {
        Pair<Set<String>, Set<String>> emptySets = Pair.<Set<String>, Set<String>>pair(ImmutableSet.<String>of(), ImmutableSet.<String>of());
        return foldl(filters, emptySets, extractIsAndNotValues());
    }

    private static Function2<Pair<Operator, Iterable<String>>, Pair<Set<String>, Set<String>>, Pair<Set<String>, Set<String>>> extractIsAndNotValues()
    {
        return ExtractIsAndNotValues.INSTANCE;
    }

    private enum ExtractIsAndNotValues implements Function2<Pair<Operator, Iterable<String>>, Pair<Set<String>, Set<String>>, Pair<Set<String>, Set<String>>>
    {
        INSTANCE;

        public Pair<Set<String>, Set<String>> apply(Pair<Operator, Iterable<String>> current, Pair<Set<String>, Set<String>> intermediate)
        {
            switch (current.first())
            {
                case IS:
                    return Pair.<Set<String>, Set<String>>pair(union(intermediate.first(), ImmutableSet.copyOf(current.second())), intermediate.second());
                case NOT:
                    return Pair.<Set<String>, Set<String>>pair(intermediate.first(), union(intermediate.second(), ImmutableSet.copyOf(current.second())));
                default:
                    return intermediate;
            }
        }
    }

    /**
     * @return {@code Function} for converting {@code String}s to {@code Date}s
     */
    public static Function<String, Date> toDate()
    {
        return ToDate.INSTANCE;
    }

    private enum ToDate implements Function<String, Date>
    {
        INSTANCE;

        public Date apply(String date)
        {
            return new Date(Long.parseLong(date));
        }
    }

    /**
     * Extracts all the values from the {@code filters} for a particular {@code Operation}.
     *
     * @param op operation to extract all the values for
     * @param filters filters to extract values from
     * @return all the values from the {@code filters} for a particular {@code Operation}
     */
    public static Iterable<String> getAllValues(Operator op, Collection<Pair<Operator, Iterable<String>>> filters)
    {
        return foldl(filters, ImmutableList.<String>builder(), new BuildAllValuesWithOp(op)).build();
    }

    public static final class BuildAllValuesWithOp implements Function2<Pair<Operator, Iterable<String>>, Builder<String>, Builder<String>>
    {
        private final Operator op;

        public BuildAllValuesWithOp(Operator op)
        {
            this.op = op;
        }

        public Builder<String> apply(Pair<Operator, Iterable<String>> filter, Builder<String> builder)
        {
            if (filter.first().equals(op))
            {
                builder.addAll(filter.second());
            }
            return builder;
        }
    }

    /**
     * Extracts only the first value encountered in the {@code filters} with the given {@code op}.
     *
     * @param op operation to find the first value of
     * @param filters filters to extract the value from
     * @return the first value encountered in the {@code filters} with the given {@code op}
     */
    public static Option<String> getFirstValue(Operator op, Iterable<Pair<Operator, Iterable<String>>> filters)
    {
        try
        {
            Iterable<String> values = Iterables.find(filters, withOp(op)).second();
            if (!isEmpty(values))
            {
                return some(get(values, 0));
            }
        }
        catch (NoSuchElementException e)
        {
            // ignore and return null
        }
        return none();
    }

    private static Predicate<Pair<Operator, Iterable<String>>> withOp(Operator op)
    {
        return new WithOperator(op);
    }

    private static final class WithOperator implements Predicate<Pair<Operator, Iterable<String>>>
    {
        private final Operator op;

        public WithOperator(Operator op)
        {
            this.op = op;
        }

        public boolean apply(Pair<Operator, Iterable<String>> input)
        {
            return input.first().equals(op);
        }
    }
}
