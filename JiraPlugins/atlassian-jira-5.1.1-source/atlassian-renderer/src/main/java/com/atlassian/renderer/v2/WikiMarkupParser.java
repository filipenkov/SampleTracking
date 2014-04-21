package com.atlassian.renderer.v2;

import com.atlassian.renderer.v2.macro.Macro;
import com.atlassian.renderer.v2.macro.MacroManager;
import com.atlassian.renderer.v2.components.WikiContentHandler;
import com.atlassian.renderer.v2.components.MacroTag;

/**
 * A wiki parser which delegates the processing of certain markup to the
 * {@link com.atlassian.renderer.v2.components.WikiContentHandler}. Currently
 * the parser differentiates between macro body text and other text.
 */
public class WikiMarkupParser
{
    WikiContentHandler wikiContentHandler;
    private MacroManager macroManager;

    public WikiMarkupParser(MacroManager macroManager, WikiContentHandler wikiContentHandler)
    {
        this.wikiContentHandler = wikiContentHandler;
        this.macroManager = macroManager;
    }

    public String parse(String wiki)
    {
        StringBuffer out = new StringBuffer(wiki.length());
        if (wiki.indexOf("{") == -1)
        {
            wikiContentHandler.handleText(out, wiki);
            return out.toString();
        }

        int lastStart = 0;
        boolean inEscape = false;

        for (int i = 0; i < wiki.length(); i++)
        {
            char c = wiki.charAt(i);
            if (!inEscape)
            {
                switch (c)
                {
                    case '\\':
                        inEscape = true;
                        continue;
                    case '{':
                        // ignore phrase formatting characters
                        if (wiki.length() > i + 1 && "{*?^_-+~".indexOf(wiki.charAt(i + 1)) != -1 )
                        {
                            i++;
                            continue;
                        }

                        // Copy everything before the macro into the output buffer
                        wikiContentHandler.handleText(out,wiki.substring(lastStart, i));
                        lastStart = i + 1;

                        // Handle the macro, advancing the pointer to the end of the region we've handled.
                        // If no macro was found, we'll stay where we started, no harm done.
                        i = handlePotentialMacro(wiki, i, out);
                        lastStart = i + 1;
                }
            }
            else
            {
                inEscape = false;
            }
        }

        if (lastStart < wiki.length())
        {
            wikiContentHandler.handleText(out, wiki.substring(lastStart));
        }

        return out.toString();

    }

    private int handlePotentialMacro(String wiki, int i, StringBuffer out)
    {
        MacroTag startTag = MacroTag.makeMacroTag(wiki, i);
        if (startTag != null)
        {
            Macro macro = getMacroByName(startTag.command);
            int endTagOffset = 0;

            if (macro == null || macro.hasBody())
            {
                endTagOffset = findEndTagOffset(wiki, startTag);
            }

            if (endTagOffset > 0)
            {
                makeMacro(out, wiki, startTag, endTagOffset);
                i = endTagOffset + startTag.command.length() + 1;
            }
            else
            {
                makeMacro(out, startTag);
                i = startTag.endIndex;
            }

        }
        else
        {
            out.append('{');
        }

        return i;
    }

    private void makeMacro(StringBuffer buffer, MacroTag startTag)
    {
        makeMacro(buffer, startTag, "", false);
    }

    private void makeMacro(StringBuffer buffer, String wiki, MacroTag startTag, int endTagOffset)
    {
        String body = wiki.substring(startTag.endIndex + 1, endTagOffset);
        boolean hasEndTag = (endTagOffset > 0);
        makeMacro(buffer, startTag, body, hasEndTag);
    }

    private void makeMacro(StringBuffer buffer, MacroTag startTag, String body, boolean hasEndTag)
    {
        wikiContentHandler.handleMacro(buffer,startTag,body, hasEndTag);
    }

    private Macro getMacroByName(String name)
    {
        if (name == null)
        {
            return null;
        }

        return macroManager.getEnabledMacro(name.toLowerCase());
    }

    private int findEndTagOffset(String wiki, MacroTag startTag)
    {
        boolean inEscape = false;

        for (int i = startTag.startIndex + startTag.originalText.length(); i < wiki.length(); i++)
        {
            char c = wiki.charAt(i);

            if (inEscape)
            {
                inEscape = false;
                continue;
            }

            if (c == '{')
            {
                MacroTag endTag = MacroTag.makeMacroTag(wiki, i);
                if (endTag != null && startTag.command.equals(endTag.command) && endTag.argString.length() == 0)
                {
                    return i;
                }
            }
            else if (c == '\\')
            {
                inEscape = true;
            }
        }

        return 0;
    }
   
}
