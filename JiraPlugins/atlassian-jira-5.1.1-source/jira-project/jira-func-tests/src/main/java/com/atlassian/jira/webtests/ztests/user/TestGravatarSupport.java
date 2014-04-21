package com.atlassian.jira.webtests.ztests.user;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.RestFuncTest;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.User;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.UserClient;
import com.meterware.httpunit.ClientProperties;
import com.meterware.httpunit.WebResponse;
import org.w3c.dom.Node;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.junit.Assert.assertThat;

@WebTest ({ Category.FUNC_TEST })
public class TestGravatarSupport extends RestFuncTest
{
    private static final String ADMIN_MD5 = "e64c7d89f26bd1972efa854d13d7dd61";
    private static final String FRED_MD5 = "6255165076a5e31273cbda50bb9f9636";

    private String adminGravatarSmall;
    private String adminGravatarLarge;

    private String fredGravatarSmall;
    private String fredGravatarLarge;

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        try
        {
            String defaultUserAvatarSmall = URLEncoder.encode(getEnvironmentData().getBaseUrl().toString() + "/secure/useravatar?size=small&avatarId=10062", "UTF-8");
            String defaultUserAvatarLarge = URLEncoder.encode(getEnvironmentData().getBaseUrl().toString() + "/secure/useravatar?avatarId=10062", "UTF-8");

            adminGravatarSmall = String.format("http://www.gravatar.com/avatar/%s?d=%s&s=16", ADMIN_MD5, defaultUserAvatarSmall);
            adminGravatarLarge = String.format("http://www.gravatar.com/avatar/%s?d=%s&s=48", ADMIN_MD5, defaultUserAvatarLarge);

            fredGravatarSmall = String.format("http://www.gravatar.com/avatar/%s?d=%s&s=16", FRED_MD5, defaultUserAvatarSmall);
            fredGravatarLarge = String.format("http://www.gravatar.com/avatar/%s?d=%s&s=48", FRED_MD5, defaultUserAvatarLarge);
        }
        catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException(e);
        }

        // set up database
        administration.restoreBlankInstance();
        administration.generalConfiguration().useGravatars(true);
    }

    public void testGravatarShouldBeDisplayedInUserProfile() throws Exception
    {
        navigation.userProfile().gotoUserProfile("admin");

        assertThat(locator.css("img.avatar-image").getNode().getAttributes().getNamedItem("src").getTextContent(), equalTo(adminGravatarLarge));
        assertThat(locator.css("#heading-avatar img").getNode().getAttributes().getNamedItem("src").getTextContent(), equalTo(adminGravatarLarge));
    }

    public void testGravatarShouldBeDisplayedInIssueComments() throws Exception
    {
        String key = navigation.issue().createIssue("homosapien", "Bug", "an issue");

        navigation.issue().addComment(key, "comment 10000");

        Node adminAvatarLink = locator.css("#commentauthor_10000_concise").getNode();
        assertEquals(adminAvatarLink.getAttributes().getNamedItem("style").getTextContent(), String.format("background-image:url(%s);", adminGravatarSmall));


        navigation.login(FRED_USERNAME);
        navigation.issue().addComment(key, "comment 10001");

        Node fredAvatarLink = locator.css("#commentauthor_10001_concise").getNode();
        assertEquals(fredAvatarLink.getAttributes().getNamedItem("style").getTextContent(), String.format("background-image:url(%s);", fredGravatarSmall));
    }

    public void testGravatarShouldBeDisplayedInUserResource() throws Exception
    {
        UserClient userClient = new UserClient(environmentData);

        User fred = userClient.get(FRED_USERNAME);
        assertThat(fred.avatarUrls, hasEntry("16x16", fredGravatarSmall));
        assertThat(fred.avatarUrls, hasEntry("48x48", fredGravatarLarge));
    }

    /*
     * Tests that we handle backward compatibility for plugins that build the avatar URL instead of calling the
     * AvatarService.
     *
     * This is only possible when the ownerId param is provided.
     */
    public void testAvatarServletShouldRedirectToGravatar() throws Exception
    {
        ClientProperties clientProperties = tester.getDialog().getWebClient().getClientProperties();
        boolean redirect = clientProperties.isAutoRedirect();
        clientProperties.setAutoRedirect(false);
        try
        {
            // make sure we get a 302 redirect for large avatars
            WebResponse largeAvatarResponse = GET(String.format("secure/useravatar?ownerId=%s", FRED_USERNAME));
            assertThat(largeAvatarResponse.getResponseCode(), equalTo(302));
            assertThat(largeAvatarResponse.getHeaderField("Location"), equalTo(fredGravatarLarge));

            // make sure we get a 302 redirect for small avatars
            WebResponse smallAvatarResponse = GET(String.format("secure/useravatar?ownerId=%s&size=small", FRED_USERNAME));
            assertThat(smallAvatarResponse.getResponseCode(), equalTo(302));
            assertThat(smallAvatarResponse.getHeaderField("Location"), equalTo(fredGravatarSmall));

            // anon requests should get a 404
            navigation.logout();
            WebResponse anonAvatarResponse = GET(String.format("secure/useravatar?ownerId=%s", FRED_USERNAME));
            assertThat(anonAvatarResponse.getResponseCode(), equalTo(404));
        }
        finally
        {
            clientProperties.setAutoRedirect(redirect);
        }
    }
}
