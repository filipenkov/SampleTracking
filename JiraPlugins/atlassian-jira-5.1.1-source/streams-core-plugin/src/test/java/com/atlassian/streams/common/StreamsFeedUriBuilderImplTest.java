package com.atlassian.streams.common;

import java.net.URI;

import com.atlassian.streams.api.StreamsFilterType.Operator;
import com.atlassian.streams.api.common.Pair;
import com.atlassian.streams.api.common.uri.Uris;
import com.atlassian.streams.spi.UriAuthenticationParameterProvider;

import com.google.common.collect.ImmutableList;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.streams.api.common.Option.some;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class StreamsFeedUriBuilderImplTest
{
    private static final String BASE_URL = "http://localhost:3990/streams";
    private static final String BASE_PATH = "/activity";
    private static final String SERVLET_PATH = "/plugins/servlet/streams";

    StreamsFeedUriBuilderImpl uriBuilder;
    @Mock UriAuthenticationParameterProvider authProvider;
    
    @Before
    public void setUp() throws Exception
    {
        uriBuilder = new StreamsFeedUriBuilderImpl(BASE_URL, authProvider);
    }

    @Test
    public void testDefaultPathIsActivityAliasUri()
    {
        assertThat(uriBuilder.getUri(), equalTo(URI.create(BASE_URL + BASE_PATH)));
    }

    @Test
    public void testServletUri()
    {
        assertThat(uriBuilder.getServletUri(), equalTo(URI.create(BASE_URL + SERVLET_PATH)));
    }
    
    @Test
    public void testMaxResults()
    {
        uriBuilder.setMaxResults(10);
        assertThat(uriBuilder.getUri(), equalTo(URI.create(BASE_URL + BASE_PATH + "?maxResults=10")));
    }

    @Test
    public void testTimeout()
    {
        uriBuilder.setTimeout(2000);
        assertThat(uriBuilder.getUri(), equalTo(URI.create(BASE_URL + BASE_PATH + "?timeout=2000")));
    }
    
    @Test
    public void testLocalOnly()
    {
        uriBuilder.addLocalOnly(true);
        assertThat(uriBuilder.getUri(), equalTo(URI.create(BASE_URL + BASE_PATH + "?local=true")));
    }
	
    @Test
    public void testSingleProviderFilter()
    {
        uriBuilder.addProviderFilter("foo", "bar",
                                     Pair.<Operator, Iterable<String>>pair(Operator.IS, ImmutableList.of("baz")));
        assertThat(uriBuilder.getUri(), equalTo(URI.create(BASE_URL + BASE_PATH + "?foo=bar+IS+baz")));
    }
	
	@Test
    public void testMultipleFiltersForSameProvider()
    {
        uriBuilder.addProviderFilter("foo", "bar",
                                     Pair.<Operator, Iterable<String>>pair(Operator.IS, ImmutableList.of("baz")));
        uriBuilder.addProviderFilter("foo", "car",
                                     Pair.<Operator, Iterable<String>>pair(Operator.IS, ImmutableList.of("biz")));
        assertThat(uriBuilder.getUri(), equalTo(URI.create(BASE_URL + BASE_PATH + "?foo=bar+IS+baz&foo=car+IS+biz")));
    }

	@Test
    public void testProviderFilterWithMultipleValues()
    {
        uriBuilder.addProviderFilter("foo", "bar",
                                     Pair.<Operator, Iterable<String>>pair(Operator.IS, ImmutableList.of("baz", "biz")));
        assertThat(uriBuilder.getUri(), equalTo(URI.create(BASE_URL + BASE_PATH + "?foo=bar+IS+baz+biz")));
    }

    @Test
    public void testSpacesInProviderFilterValueAreEscaped()
    {
        uriBuilder.addProviderFilter("foo", "bar",
                                     Pair.<Operator, Iterable<String>>pair(Operator.IS, ImmutableList.of("b a z")));
        assertThat(uriBuilder.getUri(), equalTo(URI.create(BASE_URL + BASE_PATH + "?foo=bar+IS+b_a_z")));
    }

    @Test
    public void testUnderscoresInProviderFilterValueAreEscaped()
    {
	    String escapedUnderscore = Uris.encode("\\_");
        uriBuilder.addProviderFilter("foo", "bar",
                                     Pair.<Operator, Iterable<String>>pair(Operator.IS, ImmutableList.of("b_a_z")));
        assertThat(uriBuilder.getUri(), equalTo(URI.create(BASE_URL + BASE_PATH +
                                                           "?foo=bar+IS+b" + escapedUnderscore + "a"  + escapedUnderscore + "z")));
    }
	
    @Test
    public void testSingleStandardFilter()
    {
        uriBuilder.addStandardFilter("bar",
                                     Pair.<Operator, Iterable<String>>pair(Operator.IS, ImmutableList.of("baz")));
        assertThat(uriBuilder.getUri(), equalTo(URI.create(BASE_URL + BASE_PATH + "?streams=bar+IS+baz")));
    }
	
	@Test
    public void testMultipleStandardFilters()
    {
        uriBuilder.addStandardFilter("bar",
                                     Pair.<Operator, Iterable<String>>pair(Operator.IS, ImmutableList.of("baz")));
        uriBuilder.addStandardFilter("car",
                                     Pair.<Operator, Iterable<String>>pair(Operator.IS, ImmutableList.of("biz")));
        assertThat(uriBuilder.getUri(), equalTo(URI.create(BASE_URL + BASE_PATH + "?streams=bar+IS+baz&streams=car+IS+biz")));
    }

	@Test
    public void testStandardFilterWithMultipleValues()
    {
        uriBuilder.addStandardFilter("bar",
                                     Pair.<Operator, Iterable<String>>pair(Operator.IS, ImmutableList.of("baz", "biz")));
        assertThat(uriBuilder.getUri(), equalTo(URI.create(BASE_URL + BASE_PATH + "?streams=bar+IS+baz+biz")));
    }

    @Test
    public void testAuthenticationParameter()
    {
	    when(authProvider.get()).thenReturn(some(Pair.pair("os_authType", "basic")));
	    uriBuilder.addAuthenticationParameterIfLoggedIn();
	    assertThat(uriBuilder.getUri(), equalTo(URI.create(BASE_URL + BASE_PATH + "?os_authType=basic")));
    }
}
