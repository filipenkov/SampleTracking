package it.com.atlassian.jira.quickedit;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.issue.fields.rest.json.beans.FieldHtmlBean;
import com.atlassian.jira.issue.fields.rest.json.beans.FieldTab;
import com.atlassian.jira.quickedit.MyErrorCollection;
import com.atlassian.jira.quickedit.rest.api.field.QuickEditFields;
import com.google.gson.Gson;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.junit.Test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;

import static junitx.framework.StringAssert.assertContains;

@WebTest ({ Category.FUNC_TEST, Category.ISSUES })
public class TestQuickCreateAction extends FuncTestCase
{
    public static final String GET_URI = "/secure/QuickCreateIssue!default.jspa?pid=%d&issuetype=%d&decorator=none";
    public static final String POST_URI = "/secure/QuickCreateIssue.jspa?pid=%d&issuetype=%d&decorator=none";

    private Gson gson;

    @Override
    protected void setUpTest()
    {
        administration.restoreData("TestQuickEditForm.xml");
        gson = new Gson();
    }

    @Test
    public void testQuickCreatePermissions() throws IOException
    {
        final HttpClient anonymousClient = createClientForUser(null);
        //this issue exists, but anonymous shouldn't be able to edit!
        HttpResponse response = anonymousClient.execute(new HttpGet(getEnvironmentData().getBaseUrl() + getQuickCreateUri(10000, 1)));
        MyErrorCollection errors = gson.fromJson(IOUtils.toString(response.getEntity().getContent()), MyErrorCollection.class);
        assertEquals("You are not logged in, and do not have the permissions required to create an issue in this project as a guest.", errors.getErrorMessages().iterator().next());

        final HttpClient client = createClientForUser("admin");
        //this project doesn't exist
        HttpGet get = new HttpGet(getEnvironmentData().getBaseUrl() + getQuickCreateUri(100, 1));
        response = client.execute(get);

        errors = gson.fromJson(IOUtils.toString(response.getEntity().getContent()), MyErrorCollection.class);
        assertEquals("The project selected is invalid.", errors.getErrors().get("pid"));

        //finally we have permission. Should be able to see the fields!
        response = client.execute(new HttpGet(getEnvironmentData().getBaseUrl() + getQuickCreateUri(10000, 1)));
        final QuickEditFields quickEditFields = gson.fromJson(IOUtils.toString(response.getEntity().getContent()), QuickEditFields.class);

        assertEquals(13, quickEditFields.getFields().size());
        //lets test one field to make sure it's all good
        FieldHtmlBean expectedSummaryField = new FieldHtmlBean("summary", "Summary", true,
                "<div class=\"field-group\" >\n                                                    <input class=\"text long-field\" id=\"summary\" maxlength=\"255\" name=\"summary\" type=\"text\" value=\"Buggy bug bug!\" />\n                                    </div>", new FieldTab("Field Tab", 0));
        assertEquals(expectedSummaryField, quickEditFields.getFields().get(2));
    }

    @Test
    public void testQuickCreateValidation() throws IOException
    {
        final HttpClient anonymousClient = createClientForUser(null);
        //this issue exists, but anonymous shouldn't be able to edit!

        HttpResponse postResponse = anonymousClient.execute(getPost("my new bug summary!"));

        MyErrorCollection errors = gson.fromJson(IOUtils.toString(postResponse.getEntity().getContent()), MyErrorCollection.class);
        //diff between branch & trunk. Argh!
        if (errors.getErrors().isEmpty())
        {
            assertEquals("pid: Anonymous users do not have permission to create issues in this project. Please try logging in first.", errors.getErrorMessages().iterator().next());
        }
        else
        {
            assertEquals("Anonymous users do not have permission to create issues in this project. Please try logging in first.", errors.getErrors().get("pid"));
        }

        final HttpClient client = createClientForUser("admin");
        postResponse = client.execute(getPost(""));

        MyErrorCollection myErrors = gson.fromJson(IOUtils.toString(postResponse.getEntity().getContent()), MyErrorCollection.class);
        assertEquals("You must specify a summary of the issue.", myErrors.getErrors().get("summary"));

        postResponse = client.execute(getPost("Yo Dawg: This is my new summary!"));
        assertEquals(200, postResponse.getStatusLine().getStatusCode());

        // assert that the json response contains issue key
        assertContains("issueKey", IOUtils.toString(postResponse.getEntity().getContent()));

        //ensure the issue has not been modified!
        navigation.issueNavigator().createSearch("text ~ \"Yo Dawg: This is my new summary!\"");
        text.assertTextPresent("Yo Dawg: This is my new summary!");
    }

    @Test
    public void testSubTaskQuickCreate() throws IOException
    {
        administration.subtasks().enable();

        final HttpClient client = createClientForUser("admin");
        HttpPost post = getPost("This is a new subtask!");
        post.setURI(URI.create(getEnvironmentData().getBaseUrl() + getQuickCreatePostUri(10000, 5) + "&parentIssueId=10000"));
        HttpResponse postResponse = client.execute(post);

        assertEquals(200, postResponse.getStatusLine().getStatusCode());

        String response = IOUtils.toString(postResponse.getEntity().getContent());
        assertContains("issueKey", response);
        assertContains("HSP-2", response);

        navigation.issue().viewIssue("HSP-2");

        //make sure the issue got created and that it's a subtask of HSP-1!
        text.assertTextPresent("This is a new subtask!");
        text.assertTextPresent("HSP-1");
        text.assertTextPresent("Buggy bug bug!");
    }

    @Test
    public void testQuickCreateRetainsValues() throws IOException
    {
        final HttpClient client = createClientForUser("admin");

        final HttpPost method = new HttpPost(getEnvironmentData().getBaseUrl() + getQuickCreatePostUri(10000, 1));
        List<NameValuePair> formparams = new ArrayList<NameValuePair>();
        formparams.add(new BasicNameValuePair("summary", "Yo Dawg: This is my new summary!"));
        formparams.add(new BasicNameValuePair("reporter", "admin"));
        formparams.add(new BasicNameValuePair("assignee", "admin"));
        formparams.add(new BasicNameValuePair("fieldsToRetain", "summary"));
        formparams.add(new BasicNameValuePair("fieldsToRetain", "reporter"));
        method.setEntity(new UrlEncodedFormEntity(formparams, "UTF-8"));
        method.setHeader("X-Atlassian-Token", "no-check");

        HttpResponse postResponse = client.execute(method);
        final QuickEditFields quickEditFields = gson.fromJson(IOUtils.toString(postResponse.getEntity().getContent()), QuickEditFields.class);

        assertEquals(13, quickEditFields.getFields().size());
        assertTrue(quickEditFields.getFields().get(2).getEditHtml().contains("Yo Dawg: This is my new summary!"));
    }

    @Test
    public void testQuickCreateXsrfCheck() throws IOException
    {
        final HttpClient anonymousClient = createClientForUser("admin");
        HttpPost post = getPost("my new bug summary!");
        post.removeHeaders("X-Atlassian-Token");
        HttpResponse postResponse = anonymousClient.execute(post);

        assertTrue(IOUtils.toString(postResponse.getEntity().getContent()).contains("XSRF Security Token Missing"));
    }


    private HttpPost getPost(final String summary) throws UnsupportedEncodingException
    {
        final HttpPost method = new HttpPost(getEnvironmentData().getBaseUrl() + getQuickCreatePostUri(10000, 1));
        List<NameValuePair> formparams = new ArrayList<NameValuePair>();
        formparams.add(new BasicNameValuePair("summary", summary));
        formparams.add(new BasicNameValuePair("reporter", "admin"));
        formparams.add(new BasicNameValuePair("assignee", "admin"));
        method.setEntity(new UrlEncodedFormEntity(formparams, "UTF-8"));
        method.setHeader("X-Atlassian-Token", "no-check");

        return method;
    }

    private String getQuickCreateUri(final long pid, final long issueTypeId)
    {
        return String.format(GET_URI, pid, issueTypeId);
    }

    private String getQuickCreatePostUri(final long pid, final long issueTypeId)
    {
        return String.format(POST_URI, pid, issueTypeId);
    }

    private HttpClient createClientForUser(@Nullable final String user) throws IOException
    {
        HttpClient client = new DefaultHttpClient();
        client.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BROWSER_COMPATIBILITY);

        if (user != null)
        {
            HttpPost method = new HttpPost(getEnvironmentData().getBaseUrl() + "/login.jsp");
            List<NameValuePair> formparams = new ArrayList<NameValuePair>();
            formparams.add(new BasicNameValuePair("os_username", user));
            formparams.add(new BasicNameValuePair("os_password", user));
            formparams.add(new BasicNameValuePair("os_cookie", String.valueOf(true)));
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
            method.setEntity(entity);


            final HttpResponse response = client.execute(method);
            assertEquals("OK", response.getHeaders("X-Seraph-LoginReason")[0].getValue());
            IOUtils.closeQuietly(response.getEntity().getContent());
        }

        return client;
    }
}
