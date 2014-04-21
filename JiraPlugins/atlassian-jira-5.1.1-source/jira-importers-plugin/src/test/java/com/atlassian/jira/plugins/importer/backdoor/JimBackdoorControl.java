package com.atlassian.jira.plugins.importer.backdoor;

import com.atlassian.jira.functest.framework.backdoor.BackdoorControl;
import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import com.sun.jersey.api.client.WebResource;

public class JimBackdoorControl<T extends com.atlassian.jira.functest.framework.backdoor.BackdoorControl<T>>  extends BackdoorControl<T> {

    protected final String rootPath;

    public JimBackdoorControl(JIRAEnvironmentData environmentData) {
        super(environmentData);
        rootPath = environmentData.getBaseUrl().toExternalForm();
    }

    @Override
    protected WebResource createResource()
    {
        return resourceRoot(rootPath).path("rest").path("jim-func-test-plugin").path("1.0");
    }
}
