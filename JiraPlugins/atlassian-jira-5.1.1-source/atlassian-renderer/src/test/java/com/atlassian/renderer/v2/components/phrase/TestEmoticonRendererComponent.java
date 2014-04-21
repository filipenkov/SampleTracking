package com.atlassian.renderer.v2.components.phrase;

import com.atlassian.renderer.v2.components.AbstractRendererComponentTest;
import com.atlassian.renderer.v2.RenderMode;
import com.atlassian.renderer.IconManager;
import com.atlassian.renderer.Icon;
import com.atlassian.renderer.DefaultIconManager;

import java.text.MessageFormat;

public class TestEmoticonRendererComponent extends AbstractRendererComponentTest
{
    private IconManager iconManager;

    protected void setUp() throws Exception
    {
        super.setUp();
        iconManager = new DefaultIconManager();
        component = new EmoticonRendererComponent(iconManager);
        renderContext.setImagePath("/images");
    }

    protected long getRequiredRenderModeFlags()
    {
        return RenderMode.F_PHRASES;
    }

    public void testSimpleEmoticon()
    {
        testAllEmoticons("{0}");
    }

    public void testWhitespaceSurroudings()
    {
        testAllEmoticons("Hello {0}");
        testAllEmoticons("{0} Hello");
        testAllEmoticons("Hello {0} World");
        testAllEmoticons("Hello\n{0} World");
        testAllEmoticons("Hello {0}\nWorld");
    }

    public void testEndOfWord()
    {
        testAllEmoticons("I like fish{0}.");
        testAllEmoticons("I like fish{0}");
    }

    public void testOtherMarkup()
    {
        testAllEmoticons("I like _fish_{0}.");
        testAllEmoticons("I like _fish{0}_");
    }

    public void testOtherHtml()
    {
        testAllEmoticons("I like <b>fish</b>{0}.");
        testAllEmoticons("I like <b>fish{0}</b>");
    }

    public void testNotWithWordsAfter()
    {
        testAllEmoticonsNoReplacement("I like monk{0}eys");
        testAllEmoticonsNoReplacement("I like {0}monkeys");
    }

    public void testHtmlEntityInBrackets()
    {
        assertNothingChanged("(&#123;)");
        assertNothingChanged("&#123;)");
        assertNothingChanged("&amp;)");
        assertNothingChanged("&FF;)");
    }

    private void testAllEmoticons(String template)
    {
        String[] emoticons = iconManager.getEmoticonSymbols();

        for (int i = 0; i < emoticons.length; i++)
        {
            String emoticon = emoticons[i];
            Icon icon = iconManager.getEmoticon(emoticon);
            String input = MessageFormat.format(template, new String[] { emoticon });

            String imageTag = "<img class=\"emoticon\" src=\"/images/" + icon.path + "\" height=\"" + icon.height +"\" width=\"" + icon.width + "\" align=\"absmiddle\" alt=\"\" border=\"0\"/>";
            String output = MessageFormat.format(template, new String[] { imageTag});
            assertEquals("Rendering: " + emoticon, output, component.render(input, renderContext));
        }
    }

    private void testAllEmoticonsNoReplacement(String template)
    {
        String[] emoticons = iconManager.getEmoticonSymbols();

        for (int i = 0; i < emoticons.length; i++)
        {
            String emoticon = emoticons[i];
            String input = MessageFormat.format(template, new String[] { emoticon });
            assertNothingChanged("Rendering: " + emoticon, input);
        }
    }

    private void assertNothingChanged(String wiki)
    {
        assertNothingChanged("Rendering: " + wiki, wiki);
    }

    private void assertNothingChanged(String message, String wiki)
    {
        assertEquals(message, wiki, component.render(wiki, renderContext));
    }
}
