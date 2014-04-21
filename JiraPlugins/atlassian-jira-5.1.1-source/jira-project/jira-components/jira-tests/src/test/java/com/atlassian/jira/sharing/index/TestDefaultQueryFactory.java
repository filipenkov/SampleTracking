package com.atlassian.jira.sharing.index;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.mock.controller.MockController;
import com.atlassian.jira.sharing.search.SharedEntitySearchParameters;
import com.atlassian.jira.sharing.search.SharedEntitySearchParametersBuilder;
import com.atlassian.jira.sharing.type.ProjectShareQueryFactory;
import com.atlassian.jira.user.MockUser;
import org.apache.lucene.search.Query;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * A test case for DefaultQueryFactory
 *
 * @since v3.13
 */
public class TestDefaultQueryFactory extends MockControllerTestCase
{
    MockController extraController;

    @Before
    public void setUp()
    {
        createUser("admin");
        extraController = new MockController();
    }

    @After
    public void tearDown()
    {
        extraController.verify();
    }

    protected User createUser(final String userName)
    {
        return new MockUser(userName);
    }

    @Test
    public void testSimpleCreate_WithUserFred()
    {
        final DefaultQueryFactory queryFactory = createBasicDefaultQueryFactory();

        final SharedEntitySearchParameters searchParameters = new SharedEntitySearchParametersBuilder().setUserName("fred").toSearchParameters();
        final Query query = queryFactory.create(searchParameters);
        assertNotNull(query);
        assertEquals("+owner:fred", query.toString());
    }

    @Test
    public void testSimpleCreate_WithName()
    {
        final DefaultQueryFactory queryFactory = createBasicDefaultQueryFactory();

        final SharedEntitySearchParameters searchParameters = new SharedEntitySearchParametersBuilder().setName("input").toSearchParameters();
        final Query query = queryFactory.create(searchParameters);
        assertNotNull(query);
        assertEquals("+(name:input)", query.toString());
    }

    @Test
    public void testSimpleCreate_WithNameCaseInsensitive()
    {
        final DefaultQueryFactory queryFactory = createBasicDefaultQueryFactory();

        final SharedEntitySearchParameters searchParameters = new SharedEntitySearchParametersBuilder().setName("INput").setTextSearchMode(SharedEntitySearchParameters.TextSearchMode.EXACT).toSearchParameters();
        final Query query = queryFactory.create(searchParameters);
        assertNotNull(query);
        assertEquals("+(+nameCaseless:input)", query.toString());
    }

    @Test
    public void testSimpleCreate_WithDesc()
    {
        final DefaultQueryFactory queryFactory = createBasicDefaultQueryFactory();

        final SharedEntitySearchParameters searchParameters = new SharedEntitySearchParametersBuilder().setDescription("input").toSearchParameters();
        final Query query = queryFactory.create(searchParameters);
        assertNotNull(query);
        assertEquals("+(description:input)", query.toString());
    }

    @Test
    public void testSimpleCreate_WithDescInsensitive()
    {
        final DefaultQueryFactory queryFactory = createBasicDefaultQueryFactory();

        final SharedEntitySearchParameters searchParameters = new SharedEntitySearchParametersBuilder().setDescription("INput").setTextSearchMode(SharedEntitySearchParameters.TextSearchMode.EXACT).toSearchParameters();
        final Query query = queryFactory.create(searchParameters);
        assertNotNull(query);
        assertEquals("+(+descriptionSort:input)", query.toString());
    }

    @Test
    public void testSimpleCreate_WithUserAndNameAndDesc()
    {
        final DefaultQueryFactory queryFactory = createBasicDefaultQueryFactory();

        final SharedEntitySearchParameters searchParameters = new SharedEntitySearchParametersBuilder().setDescription("inputDesc").setName(
            "inputName").setUserName("userName").toSearchParameters();
        final Query query = queryFactory.create(searchParameters);
        assertNotNull(query);
        // name should no longer be stemmed, owner/userName should not!
        assertEquals("+(name:inputname description:inputdesc) +owner:username", query.toString());
    }

    @Test
    public void testSimpleCreate_WithUserCaseInsensitive()
    {
        final DefaultQueryFactory queryFactory = createBasicDefaultQueryFactory();

        final SharedEntitySearchParameters searchParameters = new SharedEntitySearchParametersBuilder()
                .setUserName("USERNAME").toSearchParameters();
        final Query query = queryFactory.create(searchParameters);
        assertNotNull(query);
        // name should no longer be stemmed, owner/userName should not!
        assertEquals("+owner:username", query.toString());
    }

    private DefaultQueryFactory createBasicDefaultQueryFactory()
    {
        final ProjectShareQueryFactory projectShareQueryFactory = createProjectShareQueryFactory();
        mockController.addObjectInstance(new PermissionQueryFactory(projectShareQueryFactory, null));
        final DefaultQueryFactory queryFactory = (DefaultQueryFactory) mockController.instantiate(DefaultQueryFactory.class);
        return queryFactory;
    }

    private ProjectShareQueryFactory createProjectShareQueryFactory()
    {
        final ProjectShareQueryFactory projectShareQueryFactory = (ProjectShareQueryFactory) extraController.instantiate(ProjectShareQueryFactory.class);
        return projectShareQueryFactory;
    }
}
