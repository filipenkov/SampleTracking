package com.atlassian.jira.functest.framework.backdoor;

import com.atlassian.jira.functest.framework.dump.FuncTestTimer;
import com.atlassian.jira.functest.framework.dump.TestInformationKit;
import com.atlassian.jira.functest.framework.log.FuncTestLogger;
import com.atlassian.jira.functest.framework.log.FuncTestLoggerImpl;
import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.RestApiClient;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;

import javax.ws.rs.core.MediaType;

/**
 * Parent class for Backdoor controllers making func-test REST requests to
 * set up test state without the UI overhead.
 *
 * See the Backdoor classes in com.atlassian.jira.pageobjects.config of the
 * jira-func-test-plugin module for more.
 *
 * @since v5.0
 */
public abstract class BackdoorControl<T extends BackdoorControl<T>> extends RestApiClient<T> implements FuncTestLogger
{
    private static final String BACKDOOR = "Backdoor Shenanigans";
    public final int restPathRootLength;

    private String rootPath;
    private FuncTestLoggerImpl logger;

    public BackdoorControl(JIRAEnvironmentData environmentData)
    {
        super(environmentData);
        this.rootPath = environmentData.getBaseUrl().toExternalForm();
        this.restPathRootLength = (environmentData.getContext() + "/rest/func-test/1.0").length();
        this.logger = new FuncTestLoggerImpl(2);
    }

    /**
     * Helper method for making easy GET calls. Feel free to add overloads to accept paths and parameter maps.
     *
     * @param webResource web resource
     * @return response string
     */
    protected String get(final WebResource webResource)
    {
        final FuncTestTimer timer = TestInformationKit.pullTimer(BACKDOOR);

        String result = webResource.get(String.class);

        logTime(webResource, "GET", timer.end());
        return result;
    }

    protected <T> T get(WebResource webResource, GenericType<T> returnType)
    {
        final FuncTestTimer timer = TestInformationKit.pullTimer(BACKDOOR);

        T result = webResource.type(MediaType.APPLICATION_JSON_TYPE).get(returnType);

        logTime(webResource, "GET", timer.end());
        return result;
    }

    protected <T> T get(WebResource webResource, Class<T> returnClass)
    {
        final FuncTestTimer timer = TestInformationKit.pullTimer(BACKDOOR);

        T result = webResource.type(MediaType.APPLICATION_JSON_TYPE).get(returnClass);

        logTime(webResource, "GET", timer.end());
        return result;
    }

    protected long getId(final WebResource webResource)
    {
        return Long.parseLong(get(webResource));
    }

    protected void post(WebResource webResource)
    {
        final FuncTestTimer timer = TestInformationKit.pullTimer(BACKDOOR);

        webResource.post();

        logTime(webResource, "POST", timer.end());
    }

    protected <T> T post(WebResource webResource, Object bean, Class<T> returnClass)
    {
        final FuncTestTimer timer = TestInformationKit.pullTimer(BACKDOOR);
        T result = webResource.type(MediaType.APPLICATION_JSON_TYPE).post(returnClass, bean);
        logTime(webResource, "POST", timer.end());
        return result;
    }

    protected void post(WebResource webResource, Object bean)
    {
        final FuncTestTimer timer = TestInformationKit.pullTimer(BACKDOOR);
        webResource.type(MediaType.APPLICATION_JSON_TYPE).post(bean);
        logTime(webResource, "POST", timer.end());
    }

    protected void delete(WebResource webResource)
    {
        final FuncTestTimer timer = TestInformationKit.pullTimer(BACKDOOR);
        webResource.type(MediaType.APPLICATION_JSON_TYPE).delete();
        logTime(webResource, "DELETE", timer.end());
    }

    /**
     * Creates the resource that corresponds to the root of the func-test REST API.
     *
     * @return a WebResource for the REST API root
     */
    protected WebResource createResource()
    {
        return resourceRoot(rootPath).path("rest").path("func-test").path("1.0");
    }

    @Override
    public void log(Object logData)
    {
        logger.log(logData);
    }

    @Override
    public void log(Throwable t)
    {
        logger.log(t);
    }

    private void logTime(WebResource webResource, String type, long howLong)
    {
        String relativePath = webResource.getURI().getPath().substring(restPathRootLength);
        log(String.format("Backdoor %4s in %5dms  %s", type, howLong, relativePath));
    }
}
