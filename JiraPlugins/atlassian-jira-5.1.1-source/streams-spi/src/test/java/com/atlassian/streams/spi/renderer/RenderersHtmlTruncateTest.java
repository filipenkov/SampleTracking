package com.atlassian.streams.spi.renderer;

import com.atlassian.streams.api.Html;

import org.junit.Test;

import static com.atlassian.streams.api.Html.html;
import static com.atlassian.streams.spi.renderer.Renderers.truncate;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class RenderersHtmlTruncateTest
{
    @Test
    public void assertThatOutputIsSameAsInputIfShorterThanDesiredLength()
    {
        Html html = new Html("<p>this is a test</p>");
        assertThat(truncate(50, html), is(equalTo(html)));
    }

    @Test
    public void assertThatHtmlEntitiesArePreserved()
    {
        Html html = new Html("<p>this &amp; that </p>");
        assertThat(truncate(50, html), is(equalTo(html)));
    }

    @Test
    public void assertThatSelfClosingInputTagsArePreserved()
    {
        Html html = new Html("<p>this is a <input type='text' /></p>");
        assertThat(truncate(50, html), is(equalTo(html)));
    }

    @Test
    public void assertThatSelfClosingBrTagsArePreserved()
    {
        Html html = new Html("this is <br/>a test");
        assertThat(truncate(50, html), is(equalTo(html)));
    }

    @Test
    public void assertThatAttributesArePreserved()
    {
        Html html = new Html("<p class='test'>this is a test</p>");
        assertThat(truncate(50, html), is(equalTo(html)));
    }

    @Test
    public void assertThatTruncatedHtmlAddsInClosingTag()
    {
        Html html = new Html("<p>this is a test</p>");
        assertThat(truncate(4, html), is(equalTo(new Html("<p>this</p>"))));
    }

    @Test
    public void assertThatTruncatedHtmlAddsInClosingTagsForNestedTags()
    {
        Html html = new Html("<p>this <b>is a</b> test</p>");
        assertThat(truncate(7, html), is(equalTo(new Html("<p>this <b>is</b></p>"))));
    }

    @Test
    public void assertThatHtmlIsCroppedOnWordBoundaries()
    {
        Html html = new Html("<p>this is a veryveryveryverylong word</p>");
        assertThat(truncate(12, html), is(equalTo(new Html("<p>this is a</p>"))));
    }

    @Test
    public void assertThatAVeryLongWordWithoutSpacesIsTruncatedOnSpecifiedLength()
    {
        Html html = new Html("<p>thisisaverylongwordwithoutaspace</p>");
        assertThat(truncate(11, html), is(equalTo(new Html("<p>thisisavery</p>"))));
    }

    @Test
    public void assertThatBrTagDoesNotNeedToBeClosed()
    {
        Html html = new Html("<p>this is<br>a test</p><p>of the emergency broadcast system</p>");
        assertThat(truncate(15, html), is(equalTo(new Html("<p>this is<br>a test</p><p>of</p>"))));
    }

    @Test
    public void assertThatImgTagDoesNotNeedToBeClosed()
    {
        Html html = new Html("<p>this is<img src='emergency.png'>a test</p><p>of the emergency broadcast system</p>");
        assertThat(truncate(15, html), is(equalTo(new Html("<p>this is<img src='emergency.png'>a test</p><p>of</p>"))));
    }

    @Test
    public void assertThatHtmlEntitiesAreCountedProperly()
    {
        Html html = new Html("<p>this is <span>&quot;a&quot;</span> test</p>");
        assertThat(truncate(11, html), is(equalTo(new Html("<p>this is <span>&quot;a&quot;</span></p>"))));
    }

    @Test
    public void assertThatUnbalancedTagsAreBalancedIfPossible()
    {
        Html html = new Html("<p><b><del>hello</b></del></p>");
        assertThat(truncate(2, html), is(equalTo(new Html("<p><b><del>he</del></b></p>"))));
    }

    @Test
    public void assertThatExtraneousCloseTagIsDiscarded()
    {
        Html html = new Html("<p><b>hello</del> bold world</b></p>");
        assertThat(truncate(10, html), is(equalTo(new Html("<p><b>hello bold</b></p>"))));
    }

    @Test
    public void assertThatPartialHTMLDoesntReturnAnError()
    {
        Html content = html("<p>HiThere /n</p><nothing");
        assertThat(truncate(10, content), is(equalTo(html("<p>HiThere /n</p>"))));
    }

    @Test
    public void assertThatPartialScriptDoesntReturnAnError()
    {
        Html content = html("<span><p>HiThere /n</p><br><script>var hi = 'hi'; document.write(hi); document.write(hi); document.write(hi); ");
        assertThat(truncate(20, content), is(equalTo(html("<span><p>HiThere /n</p><br></span>"))));
    }

    @Test
    public void assertThatRegularAndPartialScriptDoesntReturnAnError()
    {
        Html content = html("<span><script>var i = 10;</script><p>HiThere /n</p><br><script>var hi = 'hi'; document.write(hi); document.write(hi); document.write(hi); ");
        assertThat(truncate(30, content), is(equalTo(html("<span><script>var i = 10;</script><p>HiThere /n</p><br></span>"))));
    }
}
