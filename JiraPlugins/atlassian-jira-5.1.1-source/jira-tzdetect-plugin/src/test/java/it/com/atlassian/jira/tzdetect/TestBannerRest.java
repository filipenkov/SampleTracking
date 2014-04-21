package it.com.atlassian.jira.tzdetect;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;

import javax.ws.rs.core.MediaType;


/**
 * Test for REST resource backing the time zone preference banner.
 *
 * @since v1.0
 */
public class TestBannerRest extends FuncTestCase
{
    @Override
    public void setUpTest()
    {
        administration.restoreData("basic-user-data.xml");
    }

    public void testNoThanksResource() throws Exception
    {
        navigation.login("brisbane");
        navigation.gotoDashboard();
        // I'm sure there's a better way to do this...
        text.assertTextPresent("tzdetect.pref.nothanks\" value=\"\"");

        ClientConfig clientConfig = new DefaultClientConfig();
        Client client = Client.create(clientConfig);
        WebResource nothanks = client.resource("http://localhost:2990/jira/rest/tzdetect/1/nothanks");
        nothanks.addFilter(new HTTPBasicAuthFilter("brisbane", "brisbane"));
        nothanks.type(MediaType.APPLICATION_JSON_TYPE).post(ClientResponse.class, "Australia/Sydney");

        navigation.gotoDashboard();
        text.assertTextPresent("tzdetect.pref.nothanks\" value=\"Australia/Sydney\"");
    }

    public void testUpdateResource() throws Exception
    {
        navigation.login("newyork");
        navigation.gotoDashboard();
        text.assertTextPresent("tzdetect.pref.tzid\" value=\"America/New_York\"");
        text.assertTextPresent("tzdetect.pref.tzoffset\" value=\"(GMT-05:00)\"");

        ClientConfig clientConfig = new DefaultClientConfig();
        Client client = Client.create(clientConfig);
        WebResource update = client.resource("http://localhost:2990/jira/rest/tzdetect/1/update");
        update.addFilter(new HTTPBasicAuthFilter("newyork", "newyork"));
        update.type(MediaType.APPLICATION_JSON_TYPE).post(ClientResponse.class, "Australia/Sydney");

        navigation.gotoDashboard();
        text.assertTextPresent("tzdetect.pref.tzid\" value=\"Australia/Sydney\"");
        text.assertTextPresent("tzdetect.pref.tzoffset\" value=\"(GMT+10:00)\"");
    }
}
