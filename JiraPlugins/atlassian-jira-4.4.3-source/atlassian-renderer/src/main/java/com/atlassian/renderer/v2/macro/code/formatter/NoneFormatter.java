package com.atlassian.renderer.v2.macro.code.formatter;

import com.atlassian.renderer.v2.macro.code.formatter.AbstractFormatter;

/**
 * Created by IntelliJ IDEA.
 * User: Tomd
 * Date: 22/04/2005
 * Time: 14:15:36
 * To change this template use File | Settings | File Templates.
 */
public class NoneFormatter extends AbstractFormatter
{
    private static final String[] SUPPORTED_LANGUAGES = new String[] { "none" };

    public NoneFormatter()
    {
    }

    public String[] getSupportedLanguages()
    {
        return SUPPORTED_LANGUAGES;
    }
}
