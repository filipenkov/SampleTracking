package com.atlassian.jira.rest.v2.issue;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.avatar.AvatarService;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.user.search.UserPickerSearchService;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.rest.v1.users.UserPickerResource;
import com.atlassian.jira.rest.v2.issue.context.ContextI18n;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.user.util.UserManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import static com.atlassian.jira.rest.assertions.ResponseAssertions.assertResponseBody;
import static junit.framework.Assert.assertEquals;
import static org.mockito.Matchers.eq;

/**
 * @since v5.0.2
 */
@RunWith(MockitoJUnitRunner.class)
public class TestUserPickerResource
{
    @Mock
    private UserPickerSearchService searchService;

    @Mock
    private UserManager userManager;

    @Mock
    private AvatarService avatarService;
    
    @Mock
    private JiraAuthenticationContext authenticationContext;

    @Mock
    private ApplicationProperties applicationProperties;

    @Mock
    private JiraServiceContext ctx;

    @Mock
    protected PermissionManager permissionManager;

    @Mock
    private ContextI18n i18nHelper;

    @Test
    public void testExcludedUsers()
    {
        List<User> matchingUsers = new ArrayList<User>();

        for (int i = 0; i < 3; ++i)
        {
            matchingUsers.add(new MockUser("User" + String.valueOf(i), "Full Name" + String.valueOf(i), "user" + String.valueOf(i) + "@somewhere.com"));
        }

        Mockito.stub(searchService.findUsers(Mockito.<JiraServiceContext>any(), eq("User"))).toReturn(matchingUsers);
        Mockito.stub(searchService.canPerformAjaxSearch(Mockito.<JiraServiceContext>any())).toReturn(true);
        Mockito.stub(i18nHelper.getText(Mockito.<String>any(), Mockito.<String>any(), Mockito.<String>any())).toReturn("");
        Mockito.stub(authenticationContext.getI18nHelper()).toReturn(i18nHelper);

        UserPickerResource resource = new UserPickerResource(authenticationContext,
                i18nHelper, searchService, applicationProperties, avatarService) {

        };

        List<UserPickerResource.UserPickerUser> expectedUsers = new ArrayList<UserPickerResource.UserPickerUser>();
        expectedUsers.add(new UserPickerResource.UserPickerUser("User2", "Full Name2", "<div  id=\"blah_i_User2\" class=\"yad\" >Full Name2&nbsp;(<b>User</b>2)</div>", null));

        final UserPickerResource.UserPickerResultsWrapper expectedResult = new UserPickerResource.UserPickerResultsWrapper(expectedUsers, "", 1);

        List<String> excluded = new ArrayList<String>();
        excluded.add("User0");
        excluded.add("User1");

        Response response = resource.getUsersResponse("blah", "User", false, excluded);

        assertEquals(expectedResult.toString(), response.getEntity().toString());
    }

}
