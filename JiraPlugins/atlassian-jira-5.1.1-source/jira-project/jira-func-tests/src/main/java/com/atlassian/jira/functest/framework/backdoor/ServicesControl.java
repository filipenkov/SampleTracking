package com.atlassian.jira.functest.framework.backdoor;

import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import com.sun.jersey.api.client.GenericType;

import java.util.List;
import java.util.Map;

/**
 * Use this class from func/selenium/page-object tests that need to manipulate or query Services.
 * <p/>
 * See ServicesBackdoor for the code this plugs into at the back-end.
 *
 * @since v5.0
 */
public class ServicesControl extends BackdoorControl<ServicesControl>
{
    public ServicesControl(JIRAEnvironmentData environmentData)
    {
        super(environmentData);
    }

    public List<ServiceBean> getServices()
    {
        return get(createResource().path("services"), new GenericType<List<ServiceBean>>() {});
    }

    public ServiceBean getService(long id)
    {
        return get(createResource().path("services/" + id), ServiceBean.class);
    }

    public static class ServiceBean
    {
        public Long id;
        public String name;
        public String serviceClass;
        public boolean usable;
        public Map<String, String> params;
    }
}
