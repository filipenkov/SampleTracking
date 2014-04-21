package com.atlassian.jira.functest.unittests.config;

import com.atlassian.jira.functest.config.CheckOptions;
import com.atlassian.jira.functest.config.CheckOptionsUtils;
import com.atlassian.jira.functest.config.ConfigException;
import junit.framework.TestCase;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Comment;
import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.Text;
import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Test for {@link CheckOptionsUtils}.
 *
 * @since v4.1
 */
public class TestCheckOptionsUtils extends TestCase
{
    public void testAllOptions() throws Exception
    {
        final CheckOptions checkOptions = CheckOptionsUtils.allOptions();
        for (String id : new String[] { "I", "really", "dont", "care", "what", "this", "is", null })
        {
            assertTrue(checkOptions.checkEnabled(id));
        }

        assertTrue(checkOptions.asSuppressChecks().isEmpty());
    }

    public void testNoOptions() throws Exception
    {
        final CheckOptions checkOptions = CheckOptionsUtils.noOptions();
        for (String id : new String[] { "I", "really", "dont", "care", "what", "this", "is", null })
        {
            assertFalse(checkOptions.checkEnabled(id));
        }
        assertEquals(Collections.singleton("all"), checkOptions.asSuppressChecks());
    }

    public void testDisabledOptions() throws Exception
    {
        final String[] strings = { "I", "really", "dont", "care", "what", "this", "is", null};
        final CheckOptions checkOptions = CheckOptionsUtils.disabled(strings);
        for (String id : strings)
        {
            if (id != null)
            {
                assertFalse(checkOptions.checkEnabled(id));
                assertFalse(checkOptions.checkEnabled(id.toUpperCase(Locale.ENGLISH)));
            }
        }

        assertTrue(checkOptions.checkEnabled(null));
        assertTrue(checkOptions.checkEnabled(""));
        assertTrue(checkOptions.checkEnabled("OtherWord"));

        assertEquals(cleanWords(strings), checkOptions.asSuppressChecks());
    }

    public void testDisabledOptionsNone() throws Exception
    {
        final String[] strings = { "I", "really", "dont", "care", "what", "this", "is" };
        final CheckOptions checkOptions = CheckOptionsUtils.disabled();
        for (String id : strings)
        {
            assertTrue(checkOptions.checkEnabled(id));
            assertTrue(checkOptions.checkEnabled(id.toUpperCase(Locale.ENGLISH)));
        }

        assertTrue(checkOptions.asSuppressChecks().isEmpty());
    }

    public void testParseOptionsNoSuppress() throws Exception
    {
        final Document document = createWithComments("test");
        final CheckOptions checkOptions = CheckOptionsUtils.parseOptions(document);
        final String[] strings = { "I", "really", "dont", "care", "what", "this", "is" };
        for (String string : strings)
        {
            assertTrue(checkOptions.checkEnabled(string));
        }
    }

    public void testParseOptionsSuppressOne() throws Exception
    {
        final Document document = createWithComments("test", "blarg\nsuppresschecks: me                  \ndag");
        final CheckOptions checkOptions = CheckOptionsUtils.parseOptions(document);
        final String[] strings = { "I", "really", "dont", "care", "what", "this", "is" };
        for (String string : strings)
        {
            assertTrue(checkOptions.checkEnabled(string));
        }
        assertFalse(checkOptions.checkEnabled(" me    "));
    }

    public void testParseOptionsSuppressMany() throws Exception
    {
        final Document document = createWithComments("test", "blarg\nsuppresschecks: me    , other              \ndag");
        final CheckOptions checkOptions = CheckOptionsUtils.parseOptions(document);
        final String[] strings = { "I", "really", "dont", "care", "what", "this", "is" };
        for (String string : strings)
        {
            assertTrue(checkOptions.checkEnabled(string));
        }
        assertFalse(checkOptions.checkEnabled(" me    "));
        assertFalse(checkOptions.checkEnabled(" other    "));
    }

    public void testParseOptionsSuppressManyMultipleComments() throws Exception
    {
        final Document document = createWithComments("test", "blarg\n     suppresschecks: me    , other              \n\rdag\n   suppresscheck bbb", "suppresschecks:aaa");
        final CheckOptions checkOptions = CheckOptionsUtils.parseOptions(document);
        final String[] strings = { "I", "really", "dont", "care", "what", "this", "is" };
        for (String string : strings)
        {
            assertTrue(checkOptions.checkEnabled(string));
        }
        assertFalse(checkOptions.checkEnabled(" me    "));
        assertFalse(checkOptions.checkEnabled(" other    "));
        assertFalse(checkOptions.checkEnabled(" aaa    "));
        assertFalse(checkOptions.checkEnabled(" bbb    "));
    }

    public void testParseOptionsSuppressAll() throws Exception
    {
        final Document document = createWithComments("test", "blarg\n     suppresschecks: me    , other              \ndag", "suppresschecks:aaa", "suppresscheck AlL");
        final CheckOptions checkOptions = CheckOptionsUtils.parseOptions(document);
        final String[] strings = { "I", "really", "dont", "care", "what", "this", "is", "me", "other", "aaa" };
        for (String string : strings)
        {
            assertFalse(string, checkOptions.checkEnabled(string));
        }
    }

    public void testWriteOptionsWithCurrentSuppressChangeFail() throws Exception
    {
        final IMocksControl control = EasyMock.createControl();
        final CheckOptions checkOptions = control.createMock(CheckOptions.class);

        EasyMock.expect(checkOptions.asSuppressChecks()).andReturn(Collections.singleton("what"));

        control.replay();

        final Document document = createWithComments("test", "suppresschecks:aaa", "suppresscheck AlL");
        try
        {
            CheckOptionsUtils.writeOptions(document, checkOptions);
            fail("Should have failed when overwriting suppresscheck.");
        }
        catch (ConfigException e)
        {
            //expected.
        }

        control.verify();
    }

    public void testWriteOptionsWithCurrentSuppressNoChangeFail() throws Exception
    {
        final IMocksControl control = EasyMock.createControl();
        final CheckOptions checkOptions = control.createMock(CheckOptions.class);

        EasyMock.expect(checkOptions.asSuppressChecks()).andReturn(Collections.<String>emptySet());

        control.replay();

        final Document document = createWithComments("test", "suppresschecks:aaa", "suppresscheck AlL");
        try
        {
            CheckOptionsUtils.writeOptions(document, checkOptions);
            fail("Should have failed when overwriting suppresscheck.");
        }
        catch (ConfigException e)
        {
            //expected.
        }

        control.verify();
    }

    public void testWriteOptionsWithNoCommentsChange() throws Exception
    {
        final IMocksControl control = EasyMock.createControl();
        final CheckOptions checkOptions = control.createMock(CheckOptions.class);

        EasyMock.expect(checkOptions.asSuppressChecks()).andReturn(asSet("what"));

        control.replay();

        final Document document = createWithComments("test");
        CheckOptionsUtils.writeOptions(document, checkOptions);

        @SuppressWarnings ({ "unchecked" }) List<Node> nodes = document.content();

        //Comment should be added with new-line.
        assertTrue(nodes.get(0) instanceof Comment);
        final Comment comment = (Comment)nodes.get(0);
        assertEquals(String.format("%n%n    suppresschecks: what%n%n"), comment.getText());

        //Lets check the newline
        assertTrue(nodes.get(1) instanceof Text);
        final Text ws = (Text)nodes.get(1);
        assertEquals("\n", ws.getText());

        //The next node should be the root.
        assertTrue(nodes.get(2) instanceof Element);
        final Element el = (Element)nodes.get(2);
        assertEquals("test", el.getName());

        control.verify();
    }

    public void testWriteOptionsWithCommentsChangeBlank() throws Exception
    {
        final IMocksControl control = EasyMock.createControl();
        final CheckOptions checkOptions = control.createMock(CheckOptions.class);

        EasyMock.expect(checkOptions.asSuppressChecks()).andReturn(asSet("what", "is", "this"));

        control.replay();

        final Document document = createWithComments("test", "    \t\n", "othercomment");
        CheckOptionsUtils.writeOptions(document, checkOptions);
        assertComments(document, String.format("%n%n    suppresschecks: what, is, this%n%n"), "othercomment");

        control.verify();
    }

    public void testWriteOptionsWithCommentsChangeBlankAtEnd() throws Exception
    {
        final IMocksControl control = EasyMock.createControl();
        final CheckOptions checkOptions = control.createMock(CheckOptions.class);

        EasyMock.expect(checkOptions.asSuppressChecks()).andReturn(asSet("what", "this"));

        control.replay();

        final Document document = createWithComments("test", "    \t\nwashere\n\n\n\n", "othercomment");
        CheckOptionsUtils.writeOptions(document, checkOptions);
        assertComments(document, String.format("%n%n    suppresschecks: what, this%n%nwashere\n\n\n\n"), "othercomment");

        control.verify();
    }

    public void testWriteOptionsWithCommentsChangeBlankInMiddle() throws Exception
    {
        final IMocksControl control = EasyMock.createControl();
        final CheckOptions checkOptions = control.createMock(CheckOptions.class);

        EasyMock.expect(checkOptions.asSuppressChecks()).andReturn(asSet("what", "this"));

        control.replay();

        final Document document = createWithComments("test", "    \t\nwashere\n\n\n\nbrenden", "othercomment");
        CheckOptionsUtils.writeOptions(document, checkOptions);
        assertComments(document, String.format("%n%n    suppresschecks: what, this%n%nwashere\n\n\n\nbrenden"), "othercomment");

        control.verify();
    }

    public void testWriteOptionsWithCommentsChangeNoBlank() throws Exception
    {
        final IMocksControl control = EasyMock.createControl();
        final CheckOptions checkOptions = control.createMock(CheckOptions.class);

        EasyMock.expect(checkOptions.asSuppressChecks()).andReturn(asSet("what", "this"));

        control.replay();

        final Document document = createWithComments("test", "washere", "othercomment");
        CheckOptionsUtils.writeOptions(document, checkOptions);
        assertComments(document, String.format("%n%n    suppresschecks: what, this%n%nwashere"), "othercomment");

        control.verify();
    }

    public void testWriteOptionsWithCommentsNoChange() throws Exception
    {
        final IMocksControl control = EasyMock.createControl();
        final CheckOptions checkOptions = control.createMock(CheckOptions.class);

        EasyMock.expect(checkOptions.asSuppressChecks()).andReturn(Collections.<String>emptySet());

        control.replay();

        final Document document = createWithComments("test", "    \t\n", "othercomment");
        CheckOptionsUtils.writeOptions(document, checkOptions);
        assertComments(document, "    \t\n", "othercomment");

        control.verify();
    }

    public void testWriteOptionsWithNpCommentsNoChange() throws Exception
    {
        final IMocksControl control = EasyMock.createControl();
        final CheckOptions checkOptions = control.createMock(CheckOptions.class);

        EasyMock.expect(checkOptions.asSuppressChecks()).andReturn(Collections.<String>emptySet());

        control.replay();

        final Document document = createWithComments("test");
        CheckOptionsUtils.writeOptions(document, checkOptions);
        assertComments(document);

        control.verify();
    }

    private Document createWithComments(String element, String... comments)
    {
        final DocumentFactory instance = DocumentFactory.getInstance();
        final Document document = instance.createDocument();

        for (int i = 0; i < comments.length; i += 2)
        {
            document.addComment(comments[i]);
        }

        document.addElement(element);

        for (int i = 1; i < comments.length; i += 2)
        {
            document.addComment(comments[i]);
        }
        return document;
    }

    private Set<String> cleanWords(final String[] words)
    {
        Set<String> strs = new HashSet<String>(words.length);
        for (String word : words)
        {
            final String tmp = StringUtils.trimToNull(word);
            if (tmp != null)
            {
                strs.add(tmp.toLowerCase(Locale.ENGLISH));
            }
        }
        return strs;
    }

    private void assertComments(Document document, String... expectedComments)
    {
        assertEquals(Arrays.asList(expectedComments), getCommentsFromTopLevel(document));
    }

    private static List<String> getCommentsFromTopLevel(Document document)
    {
        @SuppressWarnings ({ "unchecked" }) final List<Comment> comments = document.selectNodes("/comment()");
        final List<String> commentStrings = new ArrayList<String>(comments.size());

        for (Comment comment : comments)
        {
            commentStrings.add(comment.getText());
        }

        return commentStrings;
    }

    private<T> Set<T> asSet(T...items)
    {
        return new LinkedHashSet<T>(Arrays.asList(items));
    }
}