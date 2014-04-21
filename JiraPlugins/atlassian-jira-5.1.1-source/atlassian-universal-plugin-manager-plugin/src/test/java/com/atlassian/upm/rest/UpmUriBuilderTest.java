package com.atlassian.upm.rest;

import java.net.URI;

import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.message.I18nResolver;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UpmUriBuilderTest
{
    @Mock ApplicationProperties applicationProperties;
    @Mock I18nResolver i18nResolver;

    UpmUriBuilder uriBuilder;

    @Before
    public void createUriBuilder()
    {
        when(applicationProperties.getBaseUrl()).thenReturn("http://baseurl/upm");
        when(i18nResolver.getText("upm.auditLog.anonymous")).thenReturn("anonymous");
        uriBuilder = new UpmUriBuilder(applicationProperties);
    }

    @Test
    public void assertThatMakeAbsoluteDoesNotChangeAlreadyAbsoluteUri()
    {
        URI uri = URI.create("http://absolute/uri");
        assertThat(uriBuilder.makeAbsolute(uri), is(equalTo(uri)));
    }

    @Test
    public void assertThatMakeAbsoluteTurnsARelativeUriToAnAbsoluteUri()
    {
        assertThat(uriBuilder.makeAbsolute(URI.create("/upm/path")), is(equalTo(URI.create("http://baseurl/upm/path"))));
    }

    @Test
    public void assertUpmUrlIsCorrectWhenBaseUrlHasNoContextPath()
    {
        when(applicationProperties.getBaseUrl()).thenReturn("http://baseurl:3990");
        assertThat(uriBuilder.buildUpmUri(), is(equalTo(URI.create("http://baseurl:3990/plugins/servlet/upm"))));
    }

    @Test
    public void assertUpmUrlIsCorrectWhenBaseUrlHasContextPath()
    {
        when(applicationProperties.getBaseUrl()).thenReturn("http://baseurl:3990/jira");
        assertThat(uriBuilder.buildUpmUri(), is(equalTo(URI.create("http://baseurl:3990/jira/plugins/servlet/upm"))));
    }
}
