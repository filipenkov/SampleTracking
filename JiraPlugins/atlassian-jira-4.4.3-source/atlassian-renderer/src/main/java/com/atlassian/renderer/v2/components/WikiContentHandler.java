package com.atlassian.renderer.v2.components;

/**
 * This interface is used for handling wiki markup content during the parsing process.
 * It is used in conjunction with {@link com.atlassian.renderer.v2.WikiMarkupParser}
 *
 * In future it can be extended to handle more specific situations.
 */
public interface WikiContentHandler
{
    /**
     * Handle macro and output the result into the passed buffer.
     *
     * Note that nested macros will NOT be handled separately, only the outer ones will be passed to this method.
     * Implementation will need to handle inner macros that might be present inside the <code>body</code>.
     *
     * @param buffer the buffer to output the result 
     * @param macroTag a macro descriptor for the macro to be handled
     * @param body
     * @param hasEndTag
     */
    void handleMacro(StringBuffer buffer, MacroTag macroTag, String body, boolean hasEndTag);

    /**
     * Handle text, i.e. anything that is not inside a macro tag.
     * Output the result to the given buffer.
     *
     * @param buffer
     * @param s
     */
    void handleText(StringBuffer buffer, String s);
}
