package com.atlassian.streams.spi;

import java.net.URI;
import java.util.Date;

import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.streams.api.ActivityObjectType;
import com.atlassian.streams.api.ActivityRequest;
import com.atlassian.streams.api.ActivityVerb;
import com.atlassian.streams.api.Html;
import com.atlassian.streams.api.StreamsEntry;
import com.atlassian.streams.api.StreamsEntry.ActivityObject;
import com.atlassian.streams.api.StreamsEntry.HasAlternateLinkUri;
import com.atlassian.streams.api.StreamsEntry.HasApplicationType;
import com.atlassian.streams.api.StreamsEntry.HasAuthors;
import com.atlassian.streams.api.StreamsEntry.HasId;
import com.atlassian.streams.api.StreamsEntry.HasPostedDate;
import com.atlassian.streams.api.StreamsEntry.HasRenderer;
import com.atlassian.streams.api.StreamsEntry.HasVerb;
import com.atlassian.streams.api.StreamsEntry.Renderer;
import com.atlassian.streams.api.StreamsFilterType.Operator;
import com.atlassian.streams.api.UserProfile;
import com.atlassian.streams.api.common.ImmutableNonEmptyList;
import com.atlassian.streams.api.common.Option;
import com.atlassian.streams.api.common.Pair;
import com.atlassian.streams.spi.StreamsFilterOptionProvider.ActivityOption;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;

import org.hamcrest.Matcher;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.streams.api.ActivityObjectTypes.article;
import static com.atlassian.streams.api.ActivityObjectTypes.comment;
import static com.atlassian.streams.api.ActivityObjectTypes.file;
import static com.atlassian.streams.api.ActivityObjectTypes.status;
import static com.atlassian.streams.api.ActivityVerbs.post;
import static com.atlassian.streams.api.ActivityVerbs.update;
import static com.atlassian.streams.api.Html.html;
import static com.atlassian.streams.api.StreamsFilterType.Operator.AFTER;
import static com.atlassian.streams.api.StreamsFilterType.Operator.BEFORE;
import static com.atlassian.streams.api.StreamsFilterType.Operator.BETWEEN;
import static com.atlassian.streams.api.StreamsFilterType.Operator.IS;
import static com.atlassian.streams.api.StreamsFilterType.Operator.NOT;
import static com.atlassian.streams.api.common.Option.none;
import static com.atlassian.streams.api.common.Option.some;
import static com.atlassian.streams.api.common.Pair.pair;
import static com.atlassian.streams.spi.ActivityOptions.toActivityOptionKey;
import static com.atlassian.streams.spi.Filters.entryAuthors;
import static com.atlassian.streams.spi.Filters.getAuthors;
import static com.atlassian.streams.spi.Filters.getMaxDate;
import static com.atlassian.streams.spi.Filters.getMinDate;
import static com.atlassian.streams.spi.Filters.getNotProjectKeys;
import static com.atlassian.streams.spi.Filters.getProjectKeys;
import static com.atlassian.streams.spi.Filters.inDateRange;
import static com.atlassian.streams.spi.Filters.notInUsers;
import static com.atlassian.streams.spi.StandardStreamsFilterOption.ACTIVITY_KEY;
import static com.atlassian.streams.spi.StandardStreamsFilterOption.PROJECT_KEY;
import static com.atlassian.streams.spi.StandardStreamsFilterOption.UPDATE_DATE;
import static com.atlassian.streams.spi.StandardStreamsFilterOption.USER;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.transform;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.iterableWithSize;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FiltersTest
{
    @Mock I18nResolver i18nResolver;
    @Mock ActivityRequest request;
    @Mock Multimap<String, Pair<Operator, Iterable<String>>> standardFilters;
    @Mock Multimap<String, Pair<Operator, Iterable<String>>> providerFilters;
    @Mock ApplicationProperties applicationProperties;

    @Before
    public void setup()
    {
        when(request.getStandardFilters()).thenReturn(standardFilters);
        when(request.getProviderFilters()).thenReturn(providerFilters);
        when(applicationProperties.getDisplayName()).thenReturn("Confluence");
    }

    @Test
    public void testMinDateIsNotSpecified()
    {
        assertThat(getMinDate(request), is(equalTo(none(Date.class))));
    }

    @Test
    public void testMinDateIsSpecified()
    {
        Long dateVal = new DateTime().getMillis();
        Iterable<String> dates = ImmutableList.of(dateVal.toString());
        ImmutableList<Pair<Operator, Iterable<String>>> values = ImmutableList.of(pair(AFTER, dates));

        when(standardFilters.get(UPDATE_DATE.getKey())).thenReturn(values);

        assertThat(getMinDate(request), is(equalTo(some(new Date(dateVal)))));
    }

    @Test
    public void testMaxDateIsNotSpecified()
    {
        assertThat(getMaxDate(request), is(equalTo(none(Date.class))));
    }

    @Test
    public void testMaxDateIsSpecified()
    {
        Long dateVal = new DateTime().getMillis();
        Iterable<String> dates = ImmutableList.of(dateVal.toString());
        ImmutableList<Pair<Operator, Iterable<String>>> values = ImmutableList.of(pair(BEFORE, dates));

        when(standardFilters.get(UPDATE_DATE.getKey())).thenReturn(values);

        assertThat(getMaxDate(request).get(), is(equalTo(new Date(dateVal))));
    }

    @Test
    public void testMinDateIsSpecifiedFromRange()
    {
        Long startVal = new DateTime().withYear(2009).getMillis();
        Long endVal = new DateTime().withYear(2010).getMillis();
        Iterable<String> dates = ImmutableList.of(startVal.toString(), endVal.toString());
        ImmutableList<Pair<Operator, Iterable<String>>> values = ImmutableList.of(pair(BETWEEN, dates));

        when(standardFilters.get(UPDATE_DATE.getKey())).thenReturn(values);

        assertThat(getMinDate(request).get(), is(equalTo(new Date(startVal))));
    }

    @Test
    public void testMaxDateIsSpecifiedFromRange()
    {
        Long startVal = new DateTime().withYear(2009).getMillis();
        Long endVal = new DateTime().withYear(2010).getMillis();
        Iterable<String> dates = ImmutableList.of(startVal.toString(), endVal.toString());
        ImmutableList<Pair<Operator, Iterable<String>>> values = ImmutableList.of(pair(BETWEEN, dates));

        when(standardFilters.get(UPDATE_DATE.getKey())).thenReturn(values);

        assertThat(getMaxDate(request).get(), is(equalTo(new Date(endVal))));
    }

    @Test
    public void testMaxDateWhenMinDateIsSpecified()
    {
        Long dateVal = new DateTime().getMillis();
        Iterable<String> dates = ImmutableList.of(dateVal.toString());
        ImmutableList<Pair<Operator, Iterable<String>>> values = ImmutableList.of(pair(AFTER, dates));

        when(standardFilters.get(UPDATE_DATE.getKey())).thenReturn(values);

        assertThat(getMaxDate(request), is(equalTo(none(Date.class))));
    }

    @Test
    public void testMinDateWhenMaxDateIsSpecified()
    {
        Long dateVal = new DateTime().getMillis();
        Iterable<String> dates = ImmutableList.of(dateVal.toString());
        ImmutableList<Pair<Operator, Iterable<String>>> values = ImmutableList.of(pair(BEFORE, dates));

        when(standardFilters.get(UPDATE_DATE.getKey())).thenReturn(values);

        assertThat(getMinDate(request), is(equalTo(none(Date.class))));
    }

    @Test
    public void testContainsDateBetweenOperatorIncludesTimeBetween()
    {
        DateTime rightNow = new DateTime();
        Long startVal = rightNow.getMillis();
        Long endVal = rightNow.plusSeconds(5).getMillis();
        Iterable<String> dates = ImmutableList.of(startVal.toString(), endVal.toString());
        ImmutableList<Pair<Operator, Iterable<String>>> values = ImmutableList.of(pair(BETWEEN, dates));

        when(standardFilters.get(UPDATE_DATE.getKey())).thenReturn(values);

        Date toTest = rightNow.plusSeconds(1).toDate();
        assertThat(inDateRange(request).apply(toTest), is(true));
    }

    @Test
    public void testContainsDateBetweenOperatorDoesNotIncludeTimeJustSecondsBeforeStart()
    {
        DateTime rightNow = new DateTime();
        Long startVal = rightNow.getMillis();
        Long endVal = rightNow.plusSeconds(5).getMillis();
        Iterable<String> dates = ImmutableList.of(startVal.toString(), endVal.toString());
        ImmutableList<Pair<Operator, Iterable<String>>> values = ImmutableList.of(pair(BETWEEN, dates));

        when(standardFilters.get(UPDATE_DATE.getKey())).thenReturn(values);

        Date toTest = rightNow.minusSeconds(1).toDate();
        assertThat(inDateRange(request).apply(toTest), is(false));
    }

    @Test
    public void testContainsDateBetweenOperatorDoesNotIncludeTimeJustSecondsAfterEnd()
    {
        DateTime rightNow = new DateTime();
        Long startVal = rightNow.getMillis();
        Long endVal = rightNow.plusSeconds(5).getMillis();
        Iterable<String> dates = ImmutableList.of(startVal.toString(), endVal.toString());
        ImmutableList<Pair<Operator, Iterable<String>>> values = ImmutableList.of(pair(BETWEEN, dates));

        when(standardFilters.get(UPDATE_DATE.getKey())).thenReturn(values);

        Date toTest = rightNow.plusSeconds(6).toDate();
        assertThat(inDateRange(request).apply(toTest), is(false));
    }

    @Test
    public void testContainsDateBeforeOperatorIncludesTimeJustSecondsBefore()
    {
        DateTime rightNow = new DateTime();
        Long dateVal = rightNow.getMillis();
        Iterable<String> dates = ImmutableList.of(dateVal.toString());
        ImmutableList<Pair<Operator, Iterable<String>>> values = ImmutableList.of(pair(BEFORE, dates));

        when(standardFilters.get(UPDATE_DATE.getKey())).thenReturn(values);

        Date toTest = rightNow.minusSeconds(1).toDate();
        assertThat(inDateRange(request).apply(toTest), is(true));
    }

    @Test
    public void testContainsDateBeforeOperatorDoesNotIncludeTimeJustSecondsAfter()
    {
        DateTime rightNow = new DateTime();
        Long dateVal = rightNow.getMillis();
        Iterable<String> dates = ImmutableList.of(dateVal.toString());
        ImmutableList<Pair<Operator, Iterable<String>>> values = ImmutableList.of(pair(BEFORE, dates));

        when(standardFilters.get(UPDATE_DATE.getKey())).thenReturn(values);

        Date toTest = rightNow.plusSeconds(1).toDate();
        assertThat(inDateRange(request).apply(toTest), is(false));
    }

    @Test
    public void testContainsDateAfterOperatorIncludesTimeJustSecondsAfter()
    {
        DateTime rightNow = new DateTime();
        Long dateVal = rightNow.getMillis();
        Iterable<String> dates = ImmutableList.of(dateVal.toString());
        ImmutableList<Pair<Operator, Iterable<String>>> values = ImmutableList.of(pair(AFTER, dates));

        when(standardFilters.get(UPDATE_DATE.getKey())).thenReturn(values);

        Date toTest = rightNow.plusSeconds(1).toDate();
        assertThat(inDateRange(request).apply(toTest), is(true));
    }

    @Test
    public void testContainsDateAfterOperatorDoesNotIncludeTimeJustSecondsBefore()
    {
        DateTime rightNow = new DateTime();
        Long dateVal = rightNow.getMillis();
        Iterable<String> dates = ImmutableList.of(dateVal.toString());
        ImmutableList<Pair<Operator, Iterable<String>>> values = ImmutableList.of(pair(AFTER, dates));

        when(standardFilters.get(UPDATE_DATE.getKey())).thenReturn(values);

        Date toTest = rightNow.minusSeconds(1).toDate();
        assertThat(inDateRange(request).apply(toTest), is(false));
    }

    @Test
    public void testAuthorsOfIses()
    {
        String[] is = new String[] { "user1", "user2" };
        ImmutableList<Pair<Operator, Iterable<String>>> values = ImmutableList.of(pair(IS, iterable(is)));

        when(standardFilters.get(USER.getKey())).thenReturn(values);

        assertThat(getAuthors(request), onlyHasItems(is));
    }

    /**
     * I wouldn't expect this to ever be executed in real life, but I just wanted to make
     * sure that NOT usernames were removed from the list.
     */
    @Test
    public void testAuthorsOfIsesAndNots()
    {
        String[] is = new String[] { "user1", "user2" };
        String[] not = new String[] { "user1" };
        ImmutableList<Pair<Operator, Iterable<String>>> values = ImmutableList.of(pair(IS, iterable(is)), pair(NOT, iterable(not)));

        when(standardFilters.get(USER.getKey())).thenReturn(values);

        assertThat(getAuthors(request), onlyHasItems("user2"));
    }

    @Test
    public void testAuthorsOfNots()
    {
        String[] not = new String[] { "user1" };
        ImmutableList<Pair<Operator, Iterable<String>>> values = ImmutableList.of(pair(NOT, iterable(not)));

        when(standardFilters.get(USER.getKey())).thenReturn(values);

        StreamsEntry entry1 = new StreamsEntry(newEntryParams().authors(ImmutableNonEmptyList.of(newUserProfile("user1"))), i18nResolver);
        StreamsEntry entry2 = new StreamsEntry(newEntryParams().authors(ImmutableNonEmptyList.of(newUserProfile("user2"))), i18nResolver);
        StreamsEntry entry3 = new StreamsEntry(newEntryParams().authors(ImmutableNonEmptyList.of(newUserProfile("user1"), newUserProfile("user2"))), i18nResolver);
        Iterable<StreamsEntry> entries = ImmutableList.of(entry1, entry2, entry3);

        assertThat(filter(entries, entryAuthors(notInUsers(request))), onlyHasItems(entry2));
    }

    private UserProfile newUserProfile(String username)
    {
        return new UserProfile.Builder(username).build();
    }

    @Test
    public void testProjectKeysOfIses()
    {
        String[] is = new String[] { "space1", "space2" };
        ImmutableList<Pair<Operator, Iterable<String>>> values = ImmutableList.of(pair(IS, iterable(is)));

        when(standardFilters.get(PROJECT_KEY)).thenReturn(values);

        assertThat(getProjectKeys(request), onlyHasItems(is));
    }

    @Test
    public void testProjectKeysOfNots()
    {
        String[] isNot = new String[] { "space1", "space2" };
        ImmutableList<Pair<Operator, Iterable<String>>> values = ImmutableList.of(pair(NOT, iterable(isNot)));

        when(standardFilters.get(PROJECT_KEY)).thenReturn(values);

        assertThat(getNotProjectKeys(request), onlyHasItems(isNot));
    }

    @Test
    public void testGetRequestedActivityObjectTypesForIs()
    {
        Iterable<Pair<ActivityObjectType, ActivityVerb>> activities =
            ImmutableList.of(pair(article(), post()),
                             pair(article(), update()),
                             pair(status(), update()),
                             pair(file(), post()));
        ActivityObjectType[] expected = new ActivityObjectType[] { article(), file() };
        ImmutableList<ActivityOption> is = ImmutableList.of(new ActivityOption("article posted", article(), post()),
                                                            new ActivityOption("file posted", file(), post()));
        ImmutableList<Pair<Operator, Iterable<String>>> filters = ImmutableList.of(pair(IS, transform(is, toActivityOptionKey())));

        when(providerFilters.get(ACTIVITY_KEY)).thenReturn(filters);

        assertThat(Filters.getRequestedActivityObjectTypes(request, activities), onlyHasItems(expected));
    }

    @Test
    public void testGetRequestedActivityObjectTypesForNot()
    {
        Iterable<Pair<ActivityObjectType, ActivityVerb>> activities =
            ImmutableList.of(pair(article(), post()),
                             pair(article(), update()),
                             pair(status(), update()),
                             pair(file(), post()));
        ActivityObjectType[] expected = new ActivityObjectType[] { article(), status() };
        ImmutableList<ActivityOption> not = ImmutableList.of(new ActivityOption("article posted", article(), post()),
                                                            new ActivityOption("file posted", file(), post()));
        ImmutableList<Pair<Operator, Iterable<String>>> filters = ImmutableList.of(pair(NOT, transform(not, toActivityOptionKey())));

        when(providerFilters.get(ACTIVITY_KEY)).thenReturn(filters);

        assertThat(Filters.getRequestedActivityObjectTypes(request, activities), onlyHasItems(expected));
    }

    private StreamsEntry.Parameters<HasId, HasPostedDate, HasAlternateLinkUri, HasApplicationType, HasRenderer, HasVerb, HasAuthors> newEntryParams()
    {
        return StreamsEntry.params().
            id(URI.create("http://example.com")).
            postedDate(new DateTime()).
            alternateLinkUri(URI.create("http://example.com")).
            applicationType("test").
            authors(ImmutableNonEmptyList.of(new UserProfile.Builder("someone").fullName("Some One").build())).
            addActivityObject(new ActivityObject(ActivityObject.params().
                    id("activity").
                    title(some("Some activity")).
                    alternateLinkUri(URI.create("http://example.com")).
                    activityObjectType(comment()))).
            verb(post()).
            renderer(newRenderer("title", "content"));
    }

    private Renderer newRenderer(String title, String content)
    {
        return newRenderer(title, some(content), none(String.class));
    }

    private Renderer newRenderer(String title, Option<String> content, Option<String> summary)
    {
        Renderer renderer = mock(Renderer.class);
        when(renderer.renderTitleAsHtml(any(StreamsEntry.class))).thenReturn(new Html(title));
        when(renderer.renderContentAsHtml(any(StreamsEntry.class))).thenReturn(content.map(html()));
        when(renderer.renderSummaryAsHtml(any(StreamsEntry.class))).thenReturn(summary.map(html()));
        return renderer;
    }

    /**
     * Verifies that the specified elements exist and that no other elements do.
     */
    private static <T> Matcher<Iterable<T>> onlyHasItems(T... ts)
    {
        return allOf(size(ts), hasItems(ts));
    }

    /**
     * Need to break this out into separate function in order to properly cast generics type
     */
    private static <T> Matcher<Iterable<T>> size(T... ts)
    {
        return iterableWithSize(ts.length);
    }

    /**
     * Another workaround for generics/casting compilation problem
     */
    private static <T> Iterable<T> iterable(T[] array)
    {
        return ImmutableList.<T>of(array);
    }
}
