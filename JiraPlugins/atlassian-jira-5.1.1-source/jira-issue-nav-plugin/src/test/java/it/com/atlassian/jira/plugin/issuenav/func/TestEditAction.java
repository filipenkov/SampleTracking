package it.com.atlassian.jira.plugin.issuenav.func;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.issue.fields.rest.json.beans.FieldHtmlBean;
import com.atlassian.jira.issue.fields.rest.json.beans.FieldTab;
import com.atlassian.jira.plugin.issuenav.action.EditFields;
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

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

@WebTest ({ Category.FUNC_TEST, Category.ISSUES })
public class TestEditAction extends FuncTestCase
{
    public static final String URI = "/secure/EditAction!default.jspa?issueId=%d&decorator=none";

    private Gson gson;

    @Override
    protected void setUpTest()
    {
        administration.restoreData("TestEditForm.xml");
        gson = new Gson();
    }

    @Test
    public void testEditPermissions() throws IOException
    {
        final HttpClient anonymousClient = createClientForUser(null);
        //this issue exists, but anonymous shouldn't be able to edit!
        HttpResponse response = anonymousClient.execute(new HttpGet(getEnvironmentData().getBaseUrl() + getEditUri(10000)));
        EditFields editFields = gson.fromJson(IOUtils.toString(response.getEntity().getContent()), EditFields.class);
        assertTrue(editFields.getErrorCollection().getErrorMessages().iterator().next().contains("You do not have the permission to see the specified issue"));

        final HttpClient client = createClientForUser("admin");
        //this issue doesn't exist
        HttpGet get = new HttpGet(getEnvironmentData().getBaseUrl() + getEditUri(100));
        response = client.execute(get);

        editFields = gson.fromJson(IOUtils.toString(response.getEntity().getContent()), EditFields.class);
        assertEquals("The issue no longer exists.", editFields.getErrorCollection().getErrorMessages().iterator().next());

        //finally we have permission. Should be able to see the fields!
        response = client.execute(new HttpGet(getEnvironmentData().getBaseUrl() + getEditUri(10000)));
        editFields = gson.fromJson(IOUtils.toString(response.getEntity().getContent()), EditFields.class);

        assertEquals(13, editFields.getFields().size());
        //lets test one field to make sure it's all good
        FieldHtmlBean expectedSummaryField = new FieldHtmlBean("summary", "Summary", true,
                "<div class=\"field-group\" >\n                                                    <input class=\"text long-field\" id=\"summary\" maxlength=\"255\" name=\"summary\" type=\"text\" value=\"Buggy bug bug!\" />\n                                    </div>", new FieldTab("Field Tab", 0));
        assertEquals(expectedSummaryField, editFields.getFields().get(0));
    }

    private String getEditUri(final long issueId)
    {
        return String.format(URI, issueId);
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