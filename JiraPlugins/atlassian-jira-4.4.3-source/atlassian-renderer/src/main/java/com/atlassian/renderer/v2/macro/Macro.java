package com.atlassian.renderer.v2.macro;

import com.atlassian.renderer.v2.RenderMode;
import com.atlassian.renderer.RenderContext;

import java.util.Map;

/**
 * A pluggable, programmatic module for inserting interesting things into a wiki page.
 *
 * @see http://confluence.atlassian.com/display/DOC/Macro+Plugins
 */
public interface Macro
{
    static final String RAW_PARAMS_KEY = ": = | RAW | = :";

    /**
     * Determine if the macro is an "inline" element in the resulting HTML. Inline elements will have paragraphs
     * drawn around them, or be incorporated into paragraphs they are included in. As a rule of thumb, if your
     * macro produces a paragraph, table or div (or so on), you should return false. If it produces a span,
     * replacement text or text decoration, return true.
     *
     * <p>Most macros will want to return false here.
     *
     * @return true if the macro output is included inline within the surrounding HTML, false if it forms its
     *         own HTML block.
     */
    boolean isInline();

    /**
     * Determine if the macro is a one-shot macro, or one that takes a body. If this method returns false, the
     * renderer will NOT look for an end-tag for the macro. If the method returns true, the renderer will look
     * for an end-tag, but if the end-tag is not found then the macro will be processed with an empty body.
     */
    boolean hasBody();

    /**
     * If the macro has a body, return the mode in which the body of the macro
     * should be rendered. The body of the macro will be rendered <i>before</i>
     * the macro is executed!
     *
     * <p>If this method returns null, it causes the macro processor to treat the macro
     * as one that returns wiki-text rather than HTML. The body of the macro will be
     * passed in un-rendered, and the macro's output will be inserted back into the
     * page for further normal processing by the wiki-engine.
     *
     * @return the RenderMode in which the body of this macro should be rendered, or null
     *         if the macro is substituting wiki-text
     */
    RenderMode getBodyRenderMode();

    /**
     * Execute the macro. Macros should write any output to the writer (it will
     * be rendered in the RenderMode returned in {@link #getBodyRenderMode()}).
     *
     * <p>Macros are expected to output HTML. The output of macros will not be subjected to any
     * further processing by the wiki-engine. If your macro produces wiki-text, you are responsible
     * for rendering that text to HTML yourself using a {@link com.atlassian.renderer.v2.SubRenderer}
     * or {@link com.atlassian.renderer.WikiStyleRenderer}. If your macro returns pure wiki-text, you
     * can force further processing in the normal chain by returning null from {@link #getBodyRenderMode}
     *
     * @param parameters the parameters included in the macro
     * @param body the content of the body of the macro
     * @param renderContext the rendering context in which the macro was executed
     * @throws MacroException if the macro fails in some unremarkable way. If the
     *         macro fails in a way that is important to the server maintainer
     *         (i.e. something is badly wrong), throw a RuntimeException instead.
     * @return the output of the macro
     */
    String execute(Map parameters, String body, RenderContext renderContext) throws MacroException;

    /**
     * Suppress surrounding div/span during Wysiwyg rendering. This should return true if the macro provides it's own
     * Wysiwyg processing HTML.
     */
    boolean suppressSurroundingTagDuringWysiwygRendering();

    /**
     * Suppress the rendering of the macro -- the macro's body may still be rendered (depending on the render mode of
     * the macro), but the HTML the macro adds will not be created.
     * @return
     */
    boolean suppressMacroRenderingDuringWysiwyg();
}
