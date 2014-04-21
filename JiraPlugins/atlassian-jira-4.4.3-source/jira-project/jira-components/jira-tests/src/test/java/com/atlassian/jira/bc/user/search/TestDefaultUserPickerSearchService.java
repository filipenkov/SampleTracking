package com.atlassian.jira.bc.user.search;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.MockJiraServiceContext;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.mock.MockApplicationProperties;
import com.atlassian.jira.mock.MockPermissionManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.user.util.MockUserManager;
import com.atlassian.jira.local.ListeningTestCase;

import java.util.Collection;
import java.util.List;

/**
 * Tests DefaultUserPickerSearchService without being a JiraMockTestCase.
 *
 * @see TestUserPickerSearchService
 */
public class TestDefaultUserPickerSearchService extends ListeningTestCase
{
    private JiraServiceContext jiraCtx;
    private ApplicationProperties applicationProperties = new MockApplicationProperties();
    private final PermissionManager permissionManager = new MockPermissionManager(true);

    @Before
    public void setUp() throws Exception
    {
        jiraCtx = new MockJiraServiceContext();
        applicationProperties.setString(APKeys.JIRA_AJAX_USER_PICKER_LIMIT, "100");
        applicationProperties.setString(APKeys.JIRA_OPTION_EMAIL_VISIBLE, "show");
    }
    
    @Test
    public void testEmptyQuery()
    {
        DefaultUserPickerSearchService userPickerSearchService = new DefaultUserPickerSearchService(null, null, null);

        final List<User> results = userPickerSearchService.findUsers(jiraCtx, "");
        assertNotNull(results);
        assertEquals(0, results.size());
    }


    @Test
    public void testEmptyResults()
    {
        MockUserManager userManager = new MockUserManager();
        userManager.addUser(new MockUser("a", "Adam Smith", ""));
        userManager.addUser(new MockUser("b", "Bea Smith", ""));
        userManager.addUser(new MockUser("c", "Bea Hive", ""));

        DefaultUserPickerSearchService searchService = new DefaultUserPickerSearchService(userManager, applicationProperties, permissionManager);

        Collection results = searchService.findUsers(jiraCtx, "Adam");
        assertNotNull(results);
        assertEquals(1, results.size());
        assertTrue(results.contains(new MockUser("a")));

        results = searchService.findUsers(jiraCtx, "noob");
        assertNotNull(results);
        assertEquals(0, results.size());
    }


    @Test
    public void testMultipleMatches()
    {
        MockUserManager userManager = new MockUserManager();
        userManager.addUser(new MockUser("a", "Adam Smith", ""));
        userManager.addUser(new MockUser("b", "Bea Smith", ""));
        userManager.addUser(new MockUser("c", "Bea Hive", ""));

        DefaultUserPickerSearchService searchService = new DefaultUserPickerSearchService(userManager, applicationProperties, permissionManager);

        Collection results = searchService.findUsers(jiraCtx, "Smith");
        assertNotNull(results);
        assertEquals(2, results.size());

        assertTrue(results.contains(new MockUser("a")));
        assertTrue(results.contains(new MockUser("b")));

        results = searchService.findUsers(jiraCtx, "Bea");
        assertNotNull(results);
        assertEquals(2, results.size());

        assertTrue(results.contains(new MockUser("b")));
        assertTrue(results.contains(new MockUser("c")));
    }

    @Test
    public void testEmailAddress()
    {
        MockUserManager userManager = new MockUserManager();
        userManager.addUser(new MockUser("a", "Adam Smith", "asmith@example.com"));
        userManager.addUser(new MockUser("b", "Bea Smith", "bsmith@example.com"));
        userManager.addUser(new MockUser("c", "Bob Shiver", "bshiver@example.com"));

        DefaultUserPickerSearchService searchService = new DefaultUserPickerSearchService(userManager, applicationProperties, permissionManager);

        // First make email invisible
        applicationProperties.setString(APKeys.JIRA_OPTION_EMAIL_VISIBLE, "zwitix");
        Collection results = searchService.findUsers(jiraCtx, "as");
        assertEquals(0, results.size());

        // Now set email visible
        applicationProperties.setString(APKeys.JIRA_OPTION_EMAIL_VISIBLE, "show");
        results = searchService.findUsers(jiraCtx, "as");
        assertEquals(1, results.size());
        assertTrue(results.contains(new MockUser("a")));

        results = searchService.findUsers(jiraCtx, "asmith");
        assertEquals(1, results.size());
        assertTrue(results.contains(new MockUser("a")));

        results = searchService.findUsers(jiraCtx, "asmith@");
        assertEquals(1, results.size());
        assertTrue(results.contains(new MockUser("a")));

        results = searchService.findUsers(jiraCtx, "asmith@ex");
        assertEquals(1, results.size());
        assertTrue(results.contains(new MockUser("a")));

        results = searchService.findUsers(jiraCtx, "asmith@example.com");
        assertEquals(1, results.size());
        assertTrue(results.contains(new MockUser("a")));

        results = searchService.findUsers(jiraCtx, "@example.com");
        assertEquals(0, results.size());

        results = searchService.findUsers(jiraCtx, "bs");
        assertEquals(2, results.size());
        assertTrue(results.contains(new MockUser("b")));
        assertTrue(results.contains(new MockUser("c")));
    }

    @Test
    public void testUserName()
    {
        MockUserManager userManager = new MockUserManager();
        userManager.addUser(new MockUser("zzz", "Adam Smith", "asmith@example.com"));
        userManager.addUser(new MockUser("zzw", "Bea Smith", "bsmith@example.com"));
        userManager.addUser(new MockUser("xxx", "Bob Shiver", "bshiver@example.com"));

        DefaultUserPickerSearchService searchService = new DefaultUserPickerSearchService(userManager, applicationProperties, permissionManager);

        // First make email invisible
        Collection results = searchService.findUsers(jiraCtx, "zz");
        assertEquals(2, results.size());
        assertTrue(results.contains(new MockUser("zzz")));
        assertTrue(results.contains(new MockUser("zzw")));
    }

    @Test
    public void testMixed()
    {
        MockUserManager userManager = new MockUserManager();
        userManager.addUser(new MockUser("smithy", "Adam Smith", "asmith@example.com"));
        userManager.addUser(new MockUser("fff", "Bea Smith", "bsmith@example.com"));
        userManager.addUser(new MockUser("xxx", "Random Freak", "bshiver@example.com"));

        DefaultUserPickerSearchService searchService = new DefaultUserPickerSearchService(userManager, applicationProperties, permissionManager);

        // First make email invisible
        Collection results = searchService.findUsers(jiraCtx, "smith");
        assertEquals(2, results.size());
        assertTrue(results.contains(new MockUser("smithy")));
        assertTrue(results.contains(new MockUser("fff")));
    }


}
