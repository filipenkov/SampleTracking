package com.atlassian.jira.jql.values;

import com.atlassian.jira.local.MockControllerTestCase;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.filter.SearchRequestService;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.sharing.search.SharedEntitySearchParametersBuilder;
import com.atlassian.jira.sharing.search.SharedEntitySearchResult;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.query.QueryImpl;

import java.util.Collections;

/**
 * @since v4.0
 */
public class TestSavedFilterValuesGenerator extends MockControllerTestCase
{

    private SearchRequestService searchRequestService;
    private SavedFilterValuesGenerator valuesGenerator;

    @Before
    public void setUp() throws Exception
    {
        searchRequestService = mockController.getMock(SearchRequestService.class);
        valuesGenerator = new SavedFilterValuesGenerator(searchRequestService);
    }

    @Test
    public void testGetPossibleValuesNoMatchingValuesNullPassedValue() throws Exception
    {
        final SharedEntitySearchParametersBuilder builder = new SharedEntitySearchParametersBuilder().setName("");

        final SharedEntitySearchResult result = mockController.getMock(SharedEntitySearchResult.class);
        result.getResults();
        mockController.setReturnValue(Collections.emptyList());

        searchRequestService.search(new JiraServiceContextImpl(null), builder.toSearchParameters(), 0, 10);
        mockController.setReturnValue(result);
        mockController.replay();

        final ClauseValuesGenerator.Results possibleValues = valuesGenerator.getPossibleValues(null, "filter", null, 10);
        assertEquals(0, possibleValues.getResults().size());

        mockController.verify();
    }

    @Test
    public void testGetPossibleValuesNoMatchingValues() throws Exception
    {
        final SharedEntitySearchParametersBuilder builder = new SharedEntitySearchParametersBuilder().setName("");

        final SharedEntitySearchResult result = mockController.getMock(SharedEntitySearchResult.class);
        result.getResults();
        mockController.setReturnValue(Collections.emptyList());

        searchRequestService.search(new JiraServiceContextImpl(null), builder.toSearchParameters(), 0, 10);
        mockController.setReturnValue(result);
        mockController.replay();

        final ClauseValuesGenerator.Results possibleValues = valuesGenerator.getPossibleValues(null, "filter", "", 10);
        assertEquals(0, possibleValues.getResults().size());

        mockController.verify();
    }

    @Test
    public void testGetPossibleValuesFindValues() throws Exception
    {
        final SharedEntitySearchParametersBuilder builder = new SharedEntitySearchParametersBuilder().setName("a");

        final SharedEntitySearchResult result = mockController.getMock(SharedEntitySearchResult.class);
        result.getResults();
        mockController.setReturnValue(CollectionBuilder.newBuilder(new SearchRequest(new QueryImpl(), null, "Aa sr", "desc"), new SearchRequest(new QueryImpl(), null, "A sr", "desc")).asList());

        searchRequestService.search(new JiraServiceContextImpl(null), builder.toSearchParameters(), 0, 10);
        mockController.setReturnValue(result);
        mockController.replay();

        final ClauseValuesGenerator.Results possibleValues = valuesGenerator.getPossibleValues(null, "filter", "a", 10);
        assertEquals(2, possibleValues.getResults().size());
        assertEquals(possibleValues.getResults().get(0), new ClauseValuesGenerator.Result("Aa sr"));
        assertEquals(possibleValues.getResults().get(1), new ClauseValuesGenerator.Result("A sr"));

        mockController.verify();
    }

}
