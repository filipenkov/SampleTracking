package com.atlassian.gadgets.renderer.internal;

import com.atlassian.sal.api.ApplicationProperties;

import org.apache.shindig.common.ContainerConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.gadgets.renderer.internal.AtlassianContainerConfig.MAKE_REQUEST_PATH;
import static com.atlassian.gadgets.renderer.internal.AtlassianContainerConfig.RPC_RELAY_PATH;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AtlassianContainerConfigTest
{
    @Mock ApplicationProperties applicationProperties;
    
    ContainerConfig config;
    
    @Before
    public void setUp() throws Exception
    {
        when(applicationProperties.getBaseUrl())
            .thenReturn("http://host1/base/url")
            .thenReturn("http://host1/base/url")
            .thenReturn("http://host2/other/base/url");

        config = new AtlassianContainerConfig(applicationProperties);
    }
    
    @Test
    public void assertThatAtlassianUtilBaseUrlChangesWhenApplicationPropertiesBaseUrlChanges()
    {
        assertThat(config.get("atlassian", "gadgets.features/atlassian.util/baseUrl"), is(equalTo("http://host1/base/url")));
        assertThat(config.get("atlassian", "gadgets.features/atlassian.util/baseUrl"), is(equalTo("http://host2/other/base/url")));
    }

    @Test
    public void assertThatMakeRequestUrlChangesWhenApplicationPropertiesBaseUrlChanges()
    {
        assertThat(config.get("atlassian", "gadgets.features/core.io/jsonProxyUrl"), is(equalTo("/base/url" + MAKE_REQUEST_PATH)));
        assertThat(config.get("atlassian", "gadgets.features/core.io/jsonProxyUrl"), is(equalTo("/other/base/url" + MAKE_REQUEST_PATH)));
    }

    @Test
    public void assertThatRpcRelayUrlChangesWhenApplicationPropertiesBaseUrlChanges()
    {
        assertThat(config.get("atlassian", "gadgets.features/rpc/parentRelayUrl"), is(equalTo("/base/url" + RPC_RELAY_PATH)));
        assertThat(config.get("atlassian", "gadgets.features/rpc/parentRelayUrl"), is(equalTo("/other/base/url" + RPC_RELAY_PATH)));
    }
}
