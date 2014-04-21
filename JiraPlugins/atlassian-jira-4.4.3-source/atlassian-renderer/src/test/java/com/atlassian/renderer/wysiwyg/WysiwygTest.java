package com.atlassian.renderer.wysiwyg;

import com.atlassian.renderer.RenderContext;
import com.atlassian.renderer.RendererFactory;
import com.atlassian.renderer.v2.macro.DefaultMacroManager;
import junit.framework.TestCase;


public abstract class WysiwygTest extends TestCase
{

    public DefaultWysiwygConverter getConverter()
    {
        return converter;
    }

    private DefaultWysiwygConverter converter;

    public RenderContext getContext()
    {
        return context;
    }

    private RenderContext context;
    protected DefaultMacroManager macroManager;

    public void setUp() throws Exception
    {
        super.setUp();
        converter = RendererFactory.getDefaultWysiwygConverter();
        context = RendererFactory.getRenderContext();
        macroManager = RendererFactory.getMacroManager();
    }

    /**
     * Call this method from a test to ensure that the DefaultConfluenceWysiwygConverter can convert rendered XHTML
     * back into the same markup.
     *
     * Of course this doesn't test that the generated XHTML is correct, or that the wysiwyg editor won't
     * produce XHTML which doesn't work.
     *
     * The tests also assume that the incoming markup doesn't have redundant white space -- for instance | a | renders
     * the same as |a|, so it isn't a bug that the wysiwyg code doesn't preserve the spaces in the markup.
     */
    public void testMarkup(String markup)
    {
        String xhtml = converter.convertWikiMarkupToXHtml(context, markup);
        String newMarkup = converter.convertXHtmlToWikiMarkup(xhtml);
        assertEquals(markup, newMarkup);
        testFinalMarkupIsStable(markup);
    }

    /**
     * Markup containing emoticons will result in different markup in the round trip.
     * For example: emoticons immediately followed by text will result in a space inbetween
     */
    public void testEmoticonMarkup(String markup, String expectedEmoticonMarkup)
    {
        String xhtml = converter.convertWikiMarkupToXHtml(context, markup);
        String newMarkup = converter.convertXHtmlToWikiMarkup(xhtml);
        assertEquals(expectedEmoticonMarkup, newMarkup);
        testFinalMarkupIsStable(expectedEmoticonMarkup);
    }

    /**
     * Sometimes markup should change during a round trip, but we do want to test that the HTML produced by the
     * non-canonical markup is OK.
     *
     * @param initialMarkup
     * @param finalMarkup
     */
    public void testMarkupWhichShouldntBePreserved(String initialMarkup, String finalMarkup)
    {
        String xhtml = converter.convertWikiMarkupToXHtml(context, initialMarkup);
        String newMarkup = converter.convertXHtmlToWikiMarkup(xhtml);
        assertEquals(finalMarkup, newMarkup);
        testFinalMarkupIsStable(finalMarkup);
    }

    public void testXHTML(String xhtml, String expectedMarkup)
    {
        testMarkup(expectedMarkup);
        testXHTMLWithoutTestingMarkupStability(xhtml, expectedMarkup);
    }

    /**
     * Some markup generated from XHTML doesn't roundtrip stably, but is OK.
     * @param xhtml
     * @param expectedMarkup
     */
    public void testXHTMLWithoutTestingMarkupStability(String xhtml, String expectedMarkup)
    {
        String generatedMarkup = converter.convertXHtmlToWikiMarkup(xhtml);
        // round trip the generated markup again
        String newXhtml = converter.convertWikiMarkupToXHtml(context, generatedMarkup);
        String newMarkup = converter.convertXHtmlToWikiMarkup(newXhtml);
        assertEquals(expectedMarkup, newMarkup);
    }

    public void testByComparingXHTML(String markup)
    {
        String originalXHTML = converter.convertWikiMarkupToXHtml(context, markup);
        String newMarkup = converter.convertXHtmlToWikiMarkup(originalXHTML);
        String newXHTML = converter.convertWikiMarkupToXHtml(context, newMarkup);
        assertEquals(normaliseXHTML(originalXHTML), normaliseXHTML(newXHTML));
    }

    /**
     * Check that two generations of markup produced from original markup are the same.
     * This doesn't prove that the markup has been correctly recreated from the XHTML, but it does prove that a certain set of pathological
     * things aren't happening.
     * @param markup
     */
    public void testFinalMarkupIsStable(String markup)
    {
        String originalXHTML = converter.convertWikiMarkupToXHtml(context, markup);
        String newMarkup = converter.convertXHtmlToWikiMarkup(originalXHTML);
        String newXHTML = converter.convertWikiMarkupToXHtml(context, newMarkup);
        String reallyNewMarkup = converter.convertXHtmlToWikiMarkup(newXHTML);
        assertEquals(newMarkup, reallyNewMarkup);
     }

    private String normaliseXHTML(String originalXHTML)
    {
        return originalXHTML.replaceAll("\n","");
    }


}
