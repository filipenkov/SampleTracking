package com.atlassian.renderer.v2.macro.code;

/**
 * Strategy for converting a block of source code into pretty-printed HTML. SourceCodeFormatters MUST be forgiving:
 * they will be dealing with user-supplied input, so they can't afford to blow up on bad data.
 */
public interface SourceCodeFormatter
{
    /**
     * Inform the CodeMacro which languages this formatter supports. So if someone writes {code:java}, then only
     * the formatter that returns "java" from this method will be used to format it.
     *
     * @return an array of languages that this formatter supports
     */
    String[] getSupportedLanguages();

    /**
     * Convert source code into HTML.
     *
     * @param code the source code as a string
     * @param language the programming language that it is believed this code is written in
     * @return the source code formatted as HTML
     */
    String format(String code, String language);
}
