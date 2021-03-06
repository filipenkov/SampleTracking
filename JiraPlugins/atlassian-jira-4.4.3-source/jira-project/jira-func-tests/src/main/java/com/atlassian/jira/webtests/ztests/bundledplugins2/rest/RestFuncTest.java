package com.atlassian.jira.webtests.ztests.bundledplugins2.rest;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.jira.util.json.JSONObject;
import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.HeaderOnlyWebRequest;
import com.meterware.httpunit.HttpUnitOptions;
import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.WebResponse;
import org.apache.commons.lang.StringUtils;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Base class for REST func tests.
 *
 * @since v4.2
 */
public abstract class RestFuncTest extends FuncTestCase
{
    /**
     * The base URL used during func tests.
     */
    private String baseUrl;

    /**
     * Creates a new RestFuncTest.
     */
    protected RestFuncTest()
    {
    }

    protected String getBaseUrl()
    {
        return baseUrl;
    }

    protected String getBaseUrlPlus(String... paths)
    {
        String path = paths != null ? StringUtils.join(paths, '/') : "";

        return String.format("%s/%s", getBaseUrl(), path);
    }

    protected URI getBaseUriPlus(String... paths)
    {
        try
        {
            return new URI(getBaseUrlPlus(paths));
        }
        catch (URISyntaxException e)
        {
            throw new RuntimeException(e);
        }
    }

    protected String getRestApiUrl(String... paths)
    {
        List<String> list = CollectionBuilder.<String>newBuilder("rest", "api", "2.0.alpha1").addAll(paths).asList();
        return getBaseUrlPlus(list.toArray(new String[list.size()]));
    }

    protected URI getRestApiUri(String... paths)
    {
        List<String> list = CollectionBuilder.<String>newBuilder("rest", "api", "2.0.alpha1").addAll(paths).asList();
        return getBaseUriPlus(list.toArray(new String[list.size()]));
    }

    public JSONObject getJSON(final String url, String... expand) throws JSONException
    {
        String queryString = (expand != null && expand.length > 0) ? ("?expand=" + StringUtils.join(expand, ',')) : "";
        tester.gotoPage(url + queryString);

        return new JSONObject(tester.getDialog().getResponseText());
    }

    public WebResponse GET(final String url) throws IOException, SAXException
    {
        return GET(url, Collections.<String, String>emptyMap());
    }

    public WebResponse GET(final String url, Map<String, String> headers) throws IOException, SAXException
    {
        tester.getDialog().getWebClient().setExceptionsThrownOnErrorStatus(false);
        HttpUnitOptions.setExceptionsThrownOnErrorStatus(false);
        for (Map.Entry<String, String> headerField : headers.entrySet())
        {
            tester.getDialog().getWebClient().setHeaderField(headerField.getKey(), headerField.getValue());
        }

        final GetMethodWebRequest request = new GetMethodWebRequest(getBaseUrlPlus(url));
        return tester.getDialog().getWebClient().sendRequest(request);
    }

    public WebResponse DELETE(final String url) throws IOException, SAXException
    {
        tester.getDialog().getWebClient().setExceptionsThrownOnErrorStatus(false);
        HttpUnitOptions.setExceptionsThrownOnErrorStatus(false);

        final HeaderOnlyWebRequest delete = new HeaderOnlyWebRequest(getBaseUrlPlus(url))
        {
            @Override
            public String getMethod()
            {
                return "DELETE";
            }

            // If you don't override this then the above getMethod() never gets called and the request goes through
            // as a GET. Thanks httpunit.
            @Override
            protected void completeRequest(final URLConnection connection) throws IOException
            {
                ((HttpURLConnection) connection).setRequestMethod(getMethod());
                super.completeRequest(connection);
            }
        };

        return tester.getDialog().getWebClient().sendRequest(delete);
    }

    public WebResponse POST(final String url, final JSONObject json) throws IOException, SAXException
    {
        return POST(url, json.toString());
    }

    public WebResponse POST(final String url, final String postBody) throws IOException, SAXException
    {
        tester.getDialog().getWebClient().setExceptionsThrownOnErrorStatus(false);
        HttpUnitOptions.setExceptionsThrownOnErrorStatus(false);

        final PostMethodWebRequest request = new PostMethodWebRequest(getBaseUrlPlus(url), new ByteArrayInputStream(postBody.getBytes()), "application/json");
        return tester.getDialog().getWebClient().sendRequest(request);
    }

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        baseUrl = getEnvironmentData().getBaseUrl().toExternalForm();
    }

    @Override
    protected void setUpHttpUnitOptions()
    {
        HttpUnitOptions.setDefaultCharacterSet("UTF-8");
    }

    @Override
    protected void tearDownTest()
    {
        HttpUnitOptions.resetDefaultCharacterSet();
        super.tearDownTest();
    }

    protected void assertEqualDateStrings(final String expected, final String actual)
    {
        final String[] expected_split = expected.split("(\\+|\\-)\\d{4}$");
        final String[] actual_split = actual.split("(\\+|\\-)\\d{4}$");
        assertEquals(expected_split[0], actual_split[0]);
    }
}
