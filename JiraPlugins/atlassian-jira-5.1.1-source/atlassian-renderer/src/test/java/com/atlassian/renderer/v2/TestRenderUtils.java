package com.atlassian.renderer.v2;

import junit.framework.TestCase;

/**
 * @since v3.14.4
 */
public class TestRenderUtils extends TestCase
{

    private static final String WITHOUT_CONTROL_CHARS = "[foo]tok2-inline-tok[foo]";
    private static final String WITH_CONTROL_CHARS = "[foo]\u0001tok2-inline-tok\u0002[foo]";
    private static final String ONLY_CONTROL_CHARS = "\u0001\u0002\u0001\u0002\u0001\u0002";
    
    public void testStripControlCharactersWithoutControlCharacters()
    {
        final String strippedContent = RenderUtils.stripControlCharacters(WITHOUT_CONTROL_CHARS);
        assertEquals(WITHOUT_CONTROL_CHARS, strippedContent);
    }
    
    public void testStripControlCharactersWithNull()
    {
        try
        {
            RenderUtils.stripControlCharacters(null);
            fail("Expected exception with null input");
        }
        catch (NullPointerException e)
        {
            // Suceeded, do nothing
        }
    }

    public void testStripControlCharactersWithControlCharacters()
    {
        final String strippedContent = RenderUtils.stripControlCharacters(WITH_CONTROL_CHARS);
        assertEquals(WITHOUT_CONTROL_CHARS, strippedContent);
    }

    public void testStripControlCharactersWithOnlyControlCharacters()
    {
        final String strippedContent = RenderUtils.stripControlCharacters(ONLY_CONTROL_CHARS);
        assertEquals("", strippedContent);
    }


}
