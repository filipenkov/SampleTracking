package com.atlassian.jira.jql.values;

import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.user.MockCrowdService;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import com.atlassian.jira.MockProviderAccessor;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.user.search.UserPickerSearchService;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.opensymphony.user.User;

import java.util.Collections;

/**
 * @since v4.0
 */
public class TestUserClauseValuesGenerator extends MockControllerTestCase
{
    private UserPickerSearchService userPickerSearchService;
    private UserClauseValuesGenerator valuesGenerator;

    @Before
    public void setUp() throws Exception
    {
        userPickerSearchService = mockController.getMock(UserPickerSearchService.class);

        valuesGenerator = new UserClauseValuesGenerator(userPickerSearchService);
    }

    @Test
    public void testGetPossibleValuesNoPrefix() throws Exception
    {
        final JiraServiceContextImpl ctx = new JiraServiceContextImpl(null);
        userPickerSearchService.canPerformAjaxSearch(ctx);
        mockController.setReturnValue(true);

        userPickerSearchService.getResultsSearchForEmptyQuery(ctx, "");
        mockController.setReturnValue(Collections.emptyList());

        mockController.replay();

        final ClauseValuesGenerator.Results possibleValues = valuesGenerator.getPossibleValues(null, "assignee", "", 10);

        assertEquals(0, possibleValues.getResults().size());

        mockController.verify();
    }

    @Test
    public void testGetPossibleValuesAjaxSearchDisabled() throws Exception
    {
        userPickerSearchService.canPerformAjaxSearch(new JiraServiceContextImpl(null));
        mockController.setReturnValue(false);
        mockController.replay();

        final ClauseValuesGenerator.Results possibleValues = valuesGenerator.getPossibleValues(null, "assignee", "a", 10);

        assertEquals(0, possibleValues.getResults().size());

        mockController.verify();
    }

    @Test
    public void testGetPossibleValuesHappyPath() throws Exception
    {
        final User user1 = new User("adude", new MockProviderAccessor("A Dude", "adude@example.com"), new MockCrowdService());
        final User user2 = new User("aadude", new MockProviderAccessor("Aa Dude", "aadude@example.com"), new MockCrowdService());
        final User user3 = new User("bdude", new MockProviderAccessor("B Dude", "bdude@example.com"), new MockCrowdService());
        final User user4 = new User("cdude", new MockProviderAccessor("C Dude", "cdude@example.com"), new MockCrowdService());

        final JiraServiceContextImpl ctx = new JiraServiceContextImpl(null);
        userPickerSearchService.canPerformAjaxSearch(ctx);
        mockController.setReturnValue(true);

        userPickerSearchService.getResultsSearchForEmptyQuery(ctx, "a");
        mockController.setReturnValue(CollectionBuilder.newBuilder(user1, user2, user3, user4).asList());
        mockController.replay();

        final ClauseValuesGenerator.Results possibleValues = valuesGenerator.getPossibleValues(null, "assignee", "a", 10);

        assertEquals(4, possibleValues.getResults().size());
        assertEquals(new ClauseValuesGenerator.Result(user1.getName(), new String[] {user1.getDisplayName(), "- " + user1.getEmail(), " (" + user1.getName() + ")"}), possibleValues.getResults().get(0));
        assertEquals(new ClauseValuesGenerator.Result(user2.getName(), new String[] {user2.getDisplayName(), "- " + user2.getEmail(), " (" + user2.getName() + ")"}), possibleValues.getResults().get(1));
        assertEquals(new ClauseValuesGenerator.Result(user3.getName(), new String[] {user3.getDisplayName(), "- " + user3.getEmail(), " (" + user3.getName() + ")"}), possibleValues.getResults().get(2));
        assertEquals(new ClauseValuesGenerator.Result(user4.getName(), new String[] {user4.getDisplayName(), "- " + user4.getEmail(), " (" + user4.getName() + ")"}), possibleValues.getResults().get(3));

        mockController.verify();
    }

    @Test
    public void testGetPossibleValuesMatchToLimit() throws Exception
    {
        final User user1 = new User("adude", new MockProviderAccessor("A Dude", "adude@example.com"), new MockCrowdService());
        final User user2 = new User("aadude", new MockProviderAccessor("Aa Dude", "aadude@example.com"), new MockCrowdService());
        final User user3 = new User("bdude", new MockProviderAccessor("B Dude", "bdude@example.com"), new MockCrowdService());
        final User user4 = new User("cdude", new MockProviderAccessor("C Dude", "cdude@example.com"), new MockCrowdService());

        final JiraServiceContextImpl ctx = new JiraServiceContextImpl(null);
        userPickerSearchService.canPerformAjaxSearch(ctx);
        mockController.setReturnValue(true);

        userPickerSearchService.getResultsSearchForEmptyQuery(ctx, "a");
        mockController.setReturnValue(CollectionBuilder.newBuilder(user1, user2, user3, user4).asList());
        mockController.replay();

        final ClauseValuesGenerator.Results possibleValues = valuesGenerator.getPossibleValues(null, "assignee", "a", 3);

        assertEquals(3, possibleValues.getResults().size());
        assertEquals(new ClauseValuesGenerator.Result(user1.getName(), new String[] {user1.getDisplayName(), "- " + user1.getEmail(), " (" + user1.getName() + ")"}), possibleValues.getResults().get(0));
        assertEquals(new ClauseValuesGenerator.Result(user2.getName(), new String[] {user2.getDisplayName(), "- " + user2.getEmail(), " (" + user2.getName() + ")"}), possibleValues.getResults().get(1));
        assertEquals(new ClauseValuesGenerator.Result(user3.getName(), new String[] {user3.getDisplayName(), "- " + user3.getEmail(), " (" + user3.getName() + ")"}), possibleValues.getResults().get(2));

        mockController.verify();
    }
    
}
