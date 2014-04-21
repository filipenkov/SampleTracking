package com.atlassian.streams.internal.feed;

import com.atlassian.streams.api.FeedContentSanitizer;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

public class FeedContentSanitizerTest
{
    private FeedContentSanitizer sanitizer;

    @Before
    public void createSanitizer()
    {
        sanitizer = new FeedContentSanitizerImpl();
    }

    @Test
    public void assertThatPlainTextIsLeftAsIs()
    {
        String result = sanitizer.sanitize("Hello World!");
        assertThat(result, equalTo("Hello World!"));
    }

    @Test
    public void assertThatHtmlWithSafeTagsIsLeftAsIs()
    {
        String result = sanitizer.sanitize("Hello <i>World</i>!");
        assertThat(result, equalTo("Hello <i>World</i>!"));
    }

    @Test
    public void assertThatUnsafeAttributesAreRemovedFromSafeTags()
    {
        String result = sanitizer.sanitize("Hello <i onclick='alert(\"pwned!\")'>World</i>!");
        assertThat(result, equalTo("Hello <i>World</i>!"));
    }

    @Test
    public void assertThatUnsafeTagsAreRemoved()
    {
        String result = sanitizer.sanitize("Hello <i>World</i><script>alert(\"pwned!\");</script>!");
        assertThat(result, equalTo("Hello <i>World</i>!"));
    }

    @Test
    public void assertThatStyleTagsAreRemoved()
    {
        String result = sanitizer.sanitize("Hello <i>World</i><style>#main{display:none;}</style>!");
        assertThat(result, equalTo("Hello <i>World</i>!"));
    }

    @Test
    public void assertThatOnloadAttributeIsRemovedFromImgTags()
    {
        String result = sanitizer.sanitize("Hello World!<img src='hello-world.png' onload='alert(\"bwahahahaha\")'>");
        assertThat(result, not(containsString("onload")));
    }
}
