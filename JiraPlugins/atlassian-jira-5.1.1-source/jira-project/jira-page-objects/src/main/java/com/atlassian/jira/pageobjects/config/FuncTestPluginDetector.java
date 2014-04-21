package com.atlassian.jira.pageobjects.config;

import com.atlassian.pageobjects.ProductInstance;
import com.google.inject.Inject;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.atlassian.jira.pageobjects.config.HttpClientCloser.closeQuietly;

/**
 * Detects whether the func test plugin has been installed in tested JIRA instance.
 *
 * @since v4.4
 */
public class FuncTestPluginDetector
{
    private static final Logger logger = LoggerFactory.getLogger(FuncTestPluginDetector.class);

    @Inject
    private ProductInstance jiraProduct;

    private final HttpClient client = new DefaultHttpClient();

    private Boolean installed = null;

    public boolean isFuncTestPluginInstalled()
    {
        if (installed == null)
        {
            installed = checkInstalled();
        }
        return installed;
    }

    private boolean checkInstalled()
    {
        final String uri = jiraProduct.getBaseUrl() + "/rest/func-test/1.0/config-info";
        HttpResponse response = null;
        try
        {
            response = client.execute(new HttpGet(uri));
            return (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK);
        }
        catch (Exception e)
        {
            logger.warn("Exception while checking for jira-func-test-plugin", e);
            return false;
        }
        finally
        {
            closeQuietly(response);
        }
    }


}
