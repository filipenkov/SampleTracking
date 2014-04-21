package com.atlassian.streams.spi.renderer;

import com.atlassian.streams.api.Html;

import java.net.URI;

import org.junit.Test;

import static com.atlassian.streams.api.Html.html;
import static com.atlassian.streams.spi.renderer.Renderers.replaceText;
import static com.atlassian.streams.spi.renderer.Renderers.replaceTextWithHyperlink;
import static com.atlassian.streams.spi.renderer.Renderers.stripBasicMarkup;
import static com.atlassian.streams.spi.renderer.Renderers.unescapeLineBreaks;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

public class RenderersTest
{
    private static final String GADGET_CONTENT = "{gadget:url=rest/gadgets/1.0/g/com.atlassian.streams.confluence:activitystream-gadget"
                        + "/gadgets/conf-activitystream-gadget.xml}rules=%257B%2522providers%2522%253A%255B%257B%"
                        + "2522provider%2522%253A%2522streams%2522%252C%2522rules%2522%253A%255B%257B%2522rule%2522"
                        + "%253A%2522update-date%2522%252C%2522operator%2522%253A%2522after%2522%252C%2522value%2522"
                        + "%253A%252226%252FApril%252F2011%2522%252C%2522type%2522%253A%2522date%2522%257D%255D%"
                        + "257D%255D%257D&refresh=false&numofentries=10&title=Activity%2520Stream&isConfigured=true"
                        + "&isReallyConfigured=true{gadget}";
    private static final String ESCAPED_GADGET_CONTENT = "\\{gadget:url=rest/gadgets/1.0/g/com.atlassian.streams.confluence:activitystream-gadget"
                        + "/gadgets/conf-activitystream-gadget.xml}rules=%257B%2522providers%2522%253A%255B%257B%"
                        + "2522provider%2522%253A%2522streams%2522%252C%2522rules%2522%253A%255B%257B%2522rule%2522"
                        + "%253A%2522update-date%2522%252C%2522operator%2522%253A%2522after%2522%252C%2522value%2522"
                        + "%253A%252226%252FApril%252F2011%2522%252C%2522type%2522%253A%2522date%2522%257D%255D%"
                        + "257D%255D%257D&refresh=false&numofentries=10&title=Activity%2520Stream&isConfigured=true"
                        + "&isReallyConfigured=true\\{gadget}";
    @Test
    public void testStripBasicMarkupRemovesUnescapedMacro()
    {
        assertThat(stripBasicMarkup("{noformat} this is not formatted {noformat}"), not(containsString("{noformat}")));
    }

    @Test
    public void testStripBasicMarkupPreservesEscapedMacro()
    {
        assertThat(stripBasicMarkup("\\{noformat} this is formatted \\{noformat}"), containsString("{noformat}"));
    }

    @Test
    public void testStripBasicMarkupRemovesChangesetMacroAndInitialColon()
    {
        assertThat(stripBasicMarkup("{cs:id=121182|rep=STRM}: STRM-1202 - remove colon on start of crucible create entries"),
                   allOf(not(containsString("{cs")),
                         not(containsString(":"))));
    }

    @Test
    public void testStripBasicMarkupPreservesLiteralText()
    {
        assertThat(stripBasicMarkup("{{literal}}"), is(equalTo("literal")));
    }

    @Test
    public void testStripBasicMarkupPreservesEscapedFormatting()
    {
        assertThat(stripBasicMarkup("\\*wink\\*"), is(equalTo("*wink*")));
    }

    @Test
    public void testStripBasicMarkupPreservesExclamationMarkText()
    {
        assertThat(stripBasicMarkup("exclamation! marks!! are!!! cool\\!\\!\\!\\!"), is(equalTo("exclamation! marks!! are!!! cool!!!!")));
    }

    @Test
    public void testStripBasicMarkupRemovesExclamationMarkMediaContent()
    {
        assertThat(stripBasicMarkup("My picture !MyImage.jpg!"), is(equalTo("My picture ")));
    }

    @Test
    public void testStripBasicMarkupReplacesNewlinesWithLineBreaks()
    {
        assertThat(stripBasicMarkup("Hello there,\nnewline!"), is(equalTo("Hello there,<br>newline!")));
    }

    @Test
    public void testStripBasicMarkupReplacesCarriageReturnsWithLineBreaks()
    {
        assertThat(stripBasicMarkup("Hello there,\rreturn!"), is(equalTo("Hello there,<br>return!")));
    }

    @Test
    public void testStripBasicMarkupCondensesWhitespace()
    {
        assertThat(stripBasicMarkup("     I      love   \n   whitespace   "), is(equalTo(" I love <br> whitespace ")));
    }

    @Test
    public void testStripBasicMarkupRemovesExcessNewlines()
    {
        assertThat(stripBasicMarkup("one\ntwo\n\nthree\n   \n  \nfour\n \n \n \nfive"), is(equalTo("one<br>two<br><br>three<br><br>four<br><br>five")));
    }

    @Test
    public void testStripBasicMarkupMaintainsBrTags()
    {
        assertThat(stripBasicMarkup("Hello there,<br>newline!"), is(equalTo("Hello there,<br>newline!")));
    }

    @Test
    public void testUnescapeLineBreaks()
    {
        assertThat(unescapeLineBreaks("&lt;br&gt;new line&lt;br&gt;"), is(equalTo("<br>new line<br>")));
    }

    @Test
    public void testStripBasicMarkupRemovesGadgetBodyWhenGadgetIsTheOnlyContent()
    {
        assertThat(stripBasicMarkup(GADGET_CONTENT), is(equalTo("")));
    }

    @Test
    public void testStripBasicMarkupRemovesGadgetBodyWhenOnPageWithOtherContent()
    {
        String content = "some text before " + GADGET_CONTENT + " some text after";
        assertThat(stripBasicMarkup(content), is(equalTo("some text before some text after")));
    }

    @Test
    public void testStripBasicMarkupPreservesGadgetBodyWhenGadgetIsEscaped()
    {
        // STRM-1401: we *do* want to remove the backslash in front of an escaped \{gadget}
        assertThat(stripBasicMarkup(ESCAPED_GADGET_CONTENT), is(equalTo(GADGET_CONTENT)));
    }

    @Test
    public void testStripBasicMarkupPreservesGadgetBodyWhenGadgetIsEscaped2()
    {
        String content = "some text before " + ESCAPED_GADGET_CONTENT + " some text after";
        String strippedContent = "some text before " + GADGET_CONTENT + " some text after";
        assertThat(stripBasicMarkup(content), is(equalTo(strippedContent)));
    }
    
    @Test
    public void testReplaceTextDoesNothingWhenTextNotFound()
    {
        Html content = html("this is some text");
        assertThat(replaceText("foo", "bar", content), equalTo(content));
    }
    
    @Test
    public void testReplaceTextReplacesOnlyTextOutsideTags()
    {
        Html content = html("issue FOO is on <a href=\"http://bar/FOO\">the FOO page</a>");
        assertThat(replaceText("FOO", "BAR", content),
                   equalTo(html("issue BAR is on <a href=\"http://bar/FOO\">the BAR page</a>")));
    }
    
    @Test
    public void testReplaceTextWithHyperlinkDoesNothingWhenTextNotFound()
    {
        Html content = html("this is some text");
        assertThat(replaceTextWithHyperlink("foo", URI.create("http://bar")).apply(content), equalTo(content));
    }
    
    @Test
    public void testReplaceTextWithHyperlinkReplacesOnlyTextOutsideTags()
    {
        Html content = html("issue FOO is on <a href=\"http://bar/FOO\">the FOO page</a>");
        assertThat(replaceTextWithHyperlink("FOO", URI.create("http://baz")).apply(content),
                   equalTo(html("issue <a href=\"http://baz\">FOO</a> is on <a href=\"http://bar/FOO\">the <a href=\"http://baz\">FOO</a> page</a>")));
    }
}
