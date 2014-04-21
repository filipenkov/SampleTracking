package com.atlassian.jira.webtests.ztests.bundledplugins2.rest;

import com.atlassian.jira.functest.framework.FunctTestConstants;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.jira.util.json.JSONObject;
import com.meterware.httpunit.WebResponse;
import org.xml.sax.SAXException;

import java.io.IOException;

import static com.atlassian.jira.functest.matcher.HeaderValue.header;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Testing for the /auth/session login/logout resource
 *
 * @since v4.2
 */
@WebTest ({ Category.FUNC_TEST, Category.REST, Category.SECURITY })
public class TestLogin extends RestFuncTest
{
    public static final int CAPTCHA_MAX_TRIES = 10;
    public static final String X_AUTHENTICATION_DENIED_REASON = "X-Authentication-Denied-Reason";

    private JSONObject fredBadCredentials;
    private JSONObject fredGoodCredentials;

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        administration.restoreBlankInstance();

        try
        {
            fredBadCredentials = new JSONObject();
            fredBadCredentials.put("username", FunctTestConstants.FRED_USERNAME);
            fredBadCredentials.put("password", FunctTestConstants.FRED_PASSWORD + "zzz");

            fredGoodCredentials = new JSONObject();
            fredGoodCredentials.put("username", FunctTestConstants.FRED_USERNAME);
            fredGoodCredentials.put("password", FunctTestConstants.FRED_PASSWORD);
        }
        catch (JSONException e)
        {
            throw new RuntimeException(e);
        }
    }

    public void testCurrentUser() throws Exception
    {
        final JSONObject json = getJSON("/rest/auth/latest/session");
        assertEquals(ADMIN_USERNAME, json.getString("name"));
        assertTrue(json.has("loginInfo"));
    }

    public void testCurrentUserAnon() throws Exception
    {
        navigation.logout();

        final WebResponse response = GET("/rest/auth/latest/session");
        assertEquals(401, response.getResponseCode());
    }

    public void testLogin() throws Exception
    {
        navigation.logout();

        JSONObject json = new JSONObject();
        json.put("username", ADMIN_USERNAME);
        json.put("password", "BAD-PASSWORD");

        WebResponse response = loginAs(json);
        assertEquals(401, response.getResponseCode());
        assertEquals("JIRA REST POST", response.getHeaderField("WWW-Authenticate"));

        // JRADEV-2313
        JSONObject userThatDoesntExist = new JSONObject().put("username", "wtf").put("password", "kljasdfjkl;dfs");
        assertEquals("should return 401 if user doesn't exist", 401, loginAs(userThatDoesntExist).getResponseCode());

        json = new JSONObject();
        json.put("username", ADMIN_USERNAME);
        json.put("password", ADMIN_USERNAME);
        response = loginAs(json);

        assertEquals(200, response.getResponseCode());
        final JSONObject responseJson = new JSONObject(response.getText());

        final JSONObject session = responseJson.getJSONObject("session");
        assertEquals("JSESSIONID", session.getString("name"));
        assertEquals(tester.getDialog().getWebClient().getCookieValue("JSESSIONID"), session.getString("value"));

        final JSONObject loginInfo = responseJson.getJSONObject("loginInfo");
        assertTrue(loginInfo.has("previousLoginTime"));
        assertTrue(loginInfo.has("lastFailedLoginTime"));
        assertEquals(3L, loginInfo.getLong("loginCount"));
        assertEquals(1L, loginInfo.getLong("failedLoginCount"));
    }

    // JRA-22172 This test is similar to above. But this time we not only login but we try to use it somewhere else.
    public void testWhenTheLoginResourceGivesYouACookieYouShouldBeAbleToActuallyDoSomethingWithIt() throws Exception
    {
        final String key = navigation.issue().createIssue("homosapien", "Bug", "this is a summary");
        navigation.logout();

        JSONObject json = new JSONObject();
        json.put("username", ADMIN_USERNAME);
        json.put("password", ADMIN_USERNAME);

        WebResponse response = loginAs(json);
        final JSONObject responseJson = new JSONObject(response.getText());
        final JSONObject session = responseJson.getJSONObject("session");
        final String jsessionId = session.getString("value");
        tester.getDialog().getWebClient().clearCookies();
        response = GET("/rest/api/latest/issue/" + key, MapBuilder.<String, String>newBuilder().add("Cookie", "JSESSIONID=" + jsessionId).toImmutableMap());
        assertThat(response.getResponseCode(), equalTo(200));
        final JSONObject issue = new JSONObject(response.getText());
        assertTrue(issue.has("key"));
    }

    public void testLoginsThatAreDeniedDueToCaptchaProtectionShouldReturn403() throws Exception
    {
        navigation.logout();

        // attempt to trigger a CAPTCHA request
        WebResponse response = provokeCaptchaFailure(fredBadCredentials);

        // now check the headers
        assertThat(response.getResponseCode(), equalTo(403));
        assertThat(response, header(X_AUTHENTICATION_DENIED_REASON,
                equalTo(String.format("CAPTCHA_CHALLENGE; login-url=%s", getBaseUrlPlus("login.jsp")))));
    }

    public void testCaptchaFailureWithWrongPasswordIsIdenticalToCaptchaFailureWithRightPassword() throws Exception
    {
        navigation.logout();

        WebResponse wrongPassResponse = provokeCaptchaFailure(fredBadCredentials);
        WebResponse rightPassResponse = loginAs(fredGoodCredentials);

        assertThat(rightPassResponse.getHeaderField(X_AUTHENTICATION_DENIED_REASON), equalTo(wrongPassResponse.getHeaderField(X_AUTHENTICATION_DENIED_REASON)));
    }

    protected WebResponse loginAs(JSONObject json) throws IOException, SAXException
    {
        return POST("/rest/auth/latest/session", json);
    }

    protected WebResponse provokeCaptchaFailure(JSONObject badCredentials) throws IOException, SAXException
    {
        int tries = CAPTCHA_MAX_TRIES;
        WebResponse response;
        do
        {
            response = loginAs(badCredentials);
            navigation.logout();
        }
        while (response.getResponseCode() == 401 && --tries > 0);

        if (tries == 0)
        {
            fail(String.format("Captcha did not kick in after %d failed logins", CAPTCHA_MAX_TRIES));
        }
        return response;
    }

    public void testLogout() throws Exception
    {
        navigation.login(ADMIN_USERNAME);
        WebResponse response = DELETE("/rest/auth/latest/session");

        assertEquals(204, response.getResponseCode());

        // anonymous users should get a different response
        response = DELETE("/rest/auth/latest/session");
        assertEquals(401, response.getResponseCode());
    }
}
