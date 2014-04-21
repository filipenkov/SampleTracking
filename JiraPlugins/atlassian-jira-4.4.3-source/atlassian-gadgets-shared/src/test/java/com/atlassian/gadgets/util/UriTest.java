package com.atlassian.gadgets.util;

import java.net.URI;

import org.junit.Test;

import static com.atlassian.gadgets.util.Uri.ensureTrailingSlash;
import static com.atlassian.gadgets.util.Uri.relativizeUriAgainstBase;
import static com.atlassian.gadgets.util.Uri.resolveUriAgainstBase;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class UriTest
{
    @Test
    public void ensureThatAbsoluteUriParamIsReturned()
    {
        URI absoluteUri = URI.create("http://www.example.com");
        assertThat(absoluteUri, is(equalTo(resolveUriAgainstBase("http://localhost:8080", absoluteUri))));
    }

    @Test
    public void ensureThatRelativeUriParamIsResolved()
    {
        URI relativeUri = URI.create("foo/bar");
        assertThat(URI.create("http://localhost:8080/foo/bar"), is(equalTo(resolveUriAgainstBase("http://localhost:8080", relativeUri))));
    }

    @Test
    public void ensureThatRelativeUriParamIsResolvedAgainstBaseWithTrailingSlash()
    {
        URI relativeUri = URI.create("foo/bar");
        assertThat(URI.create("http://localhost:8080/foo/bar"), is(equalTo(resolveUriAgainstBase("http://localhost:8080/", relativeUri))));
    }

    @Test
    public void ensureThatAbsoluteUriStringParamIsReturned()
    {
        String absoluteUriString = "http://www.example.com";
        assertThat(URI.create(absoluteUriString), is(equalTo(resolveUriAgainstBase("http://localhost:8080", absoluteUriString))));
    }

    @Test
    public void ensureThatRelativeUriStringParamIsResolved()
    {
        assertThat(URI.create("http://localhost:8080/foo/bar"), is(equalTo(resolveUriAgainstBase("http://localhost:8080", "foo/bar"))));
    }

    @Test
    public void ensureThatRelativeUriStringParamIsResolvedAgainstBaseWithTrailingSlash()
    {
        assertThat(URI.create("http://localhost:8080/foo/bar"), is(equalTo(resolveUriAgainstBase("http://localhost:8080/", "foo/bar"))));
    }

    @Test
    public void ensureThatTrailingSlashIsAdded()
    {
        assertThat("foo/bar/", is(equalTo(ensureTrailingSlash("foo/bar"))));
    }

    @Test
    public void ensureThatExtraTrailingSlashIsNotAdded()
    {
        assertThat("foo/bar/", is(equalTo(ensureTrailingSlash("foo/bar/"))));
    }

    @Test
    public void ensureThatAbsoluteUriParamIsRelativized()
    {
        URI absoluteUri = URI.create("http://localhost:8080/foo/bar");
        assertThat(URI.create("foo/bar"), is(equalTo(relativizeUriAgainstBase("http://localhost:8080", absoluteUri))));
    }

    @Test
    public void ensureThatAbsoluteUriParamIsRelativizedAgainstBaseWithTrailingSlash()
    {
        URI absoluteUri = URI.create("http://localhost:8080/foo/bar");
        assertThat(URI.create("foo/bar"), is(equalTo(relativizeUriAgainstBase("http://localhost:8080/", absoluteUri))));
    }

    @Test
    public void ensureThatAbsoluteUriStringParamIsRelativized()
    {
        assertThat(URI.create("foo/bar"), is(equalTo(relativizeUriAgainstBase("http://localhost:8080", "http://localhost:8080/foo/bar"))));
    }

    @Test
    public void ensureThatAbsoluteUriStringParamIsRelativizedAgainstBaseWithTrailingSlash()
    {
        assertThat(URI.create("foo/bar"), is(equalTo(relativizeUriAgainstBase("http://localhost:8080/", "http://localhost:8080/foo/bar"))));
    }

    @Test
    public void ensureThatRelativeUriParamIsReturnedWhenRelativized()
    {
        URI relativeUri = URI.create("foo/bar");
        assertThat(relativeUri, is(equalTo(relativizeUriAgainstBase("http://localhost:8080", relativeUri))));
    }

    @Test
    public void ensureThatRelativeUriParamIsReturnedWhenRelativizedAgainstBaseWithTrailingSlash()
    {
        URI relativeUri = URI.create("foo/bar");
        assertThat(relativeUri, is(equalTo(relativizeUriAgainstBase("http://localhost:8080/", relativeUri))));
    }
}
