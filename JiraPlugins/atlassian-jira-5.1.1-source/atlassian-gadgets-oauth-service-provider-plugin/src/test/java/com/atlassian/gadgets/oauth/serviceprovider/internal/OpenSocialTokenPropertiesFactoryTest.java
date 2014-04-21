package com.atlassian.gadgets.oauth.serviceprovider.internal;

import java.net.URI;

import com.atlassian.gadgets.spec.GadgetSpecFactory;
import com.atlassian.oauth.Request;
import com.atlassian.oauth.Request.HttpMethod;
import com.atlassian.oauth.serviceprovider.TokenPropertiesFactory;

import com.google.common.collect.ImmutableList;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.gadgets.oauth.serviceprovider.internal.OpenSocial.OPENSOCIAL_APP_URL;
import static com.atlassian.gadgets.oauth.serviceprovider.internal.OpenSocial.XOAUTH_APP_URL;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;

@RunWith(MockitoJUnitRunner.class)
public class OpenSocialTokenPropertiesFactoryTest
{
    @Mock GadgetSpecFactory gadgetSpecFactory;
    
    TokenPropertiesFactory propertiesFactory;

    @Before
    public void setUp()
    {
        propertiesFactory = new OpenSocialTokenPropertiesFactory(gadgetSpecFactory);
    }
    
    @Test
    public void assertThatMapReturnedFromGetTokenPropertiesFromOAuthMessageContainsGadgetUrlWhenXOAuthAppUrlIsAParameter()
    {
        assertThat(
            propertiesFactory.newRequestTokenProperties(newRequest(XOAUTH_APP_URL, "http://container/gadget.xml")), 
            hasEntry(OpenSocial.XOAUTH_APP_URL, "http://container/gadget.xml")
        );
    }
    
    @Test
    public void assertThatMapReturnedFromGetTokenPropertiesFromOAuthMessageContainsGadgetUrlWhenOpenSocialAppUrlIsAParameter()
    {
        assertThat(
            propertiesFactory.newRequestTokenProperties(newRequest(OPENSOCIAL_APP_URL, "http://container/gadget.xml")),
            hasEntry(OpenSocial.XOAUTH_APP_URL, "http://container/gadget.xml")
        );
    }

    @Test
    public void assertThatMapReturnedFromGetTokenPropertiesFromOAuthMessageDoesNotContainGadgetUrlWhenXOAuthAppUrlIsAnInvalidUri()
    {
        assertThat(
            propertiesFactory.newRequestTokenProperties(newRequest(XOAUTH_APP_URL, "http://container/gadget.xml blah")),
            not(hasKey(OpenSocial.XOAUTH_APP_URL))
        );
    }

    @Test
    public void assertThatMapReturnedFromGetTokenPropertiesFromOAuthMessageDoesNotContainGadgetUrlWhenOpenSocialAppUrlIsAnInvalidUri()
    {
        assertThat(
            propertiesFactory.newRequestTokenProperties(newRequest(OPENSOCIAL_APP_URL, "http://container/gadget.xml blah")),
            not(hasKey(OpenSocial.XOAUTH_APP_URL))
        );
    }

    private Request newRequest(String parameterName, String parameterValue)
    {
        return new Request(HttpMethod.GET, URI.create("http://sp/request-token"), 
                ImmutableList.of(new Request.Parameter(parameterName, parameterValue)));
    }
}
