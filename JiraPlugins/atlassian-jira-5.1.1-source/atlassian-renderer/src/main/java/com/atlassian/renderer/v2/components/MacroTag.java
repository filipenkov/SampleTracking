package com.atlassian.renderer.v2.components;

import com.atlassian.renderer.v2.RenderUtils;

public class MacroTag
{
    public final int startIndex;
    public final int endIndex;
    public final String originalText;
    public final String command;
    public final String argString;

    public static MacroTag makeMacroTag(String wiki, int startIndex)
    {
        if (wiki.charAt(startIndex) != '{' || startIndex + 3 > wiki.length())
            return null;

        boolean inCommand = true;
        boolean escapeNext = false;
        StringBuffer command = new StringBuffer();
        StringBuffer args = new StringBuffer();

        for (int i = startIndex + 1; i < wiki.length(); i++)
        {
            char c = wiki.charAt(i);

            if (!escapeNext)
            {
                switch (c)
                {
                    case '}':
                        return makeMacroTag(wiki, startIndex, i, command.toString(), args.toString());
                    case '\n':
                    case '\r':
                    case '\\':
                        escapeNext = true;
                        continue;
                    case '{':
                        return null;
                    case ':':
                        if (inCommand)
                            inCommand = false;
                        else
                            args.append(':');
                        continue;
                }
            }
            else
            {
                escapeNext = false;
            }

            if (inCommand)
                command.append(c);
            else
                args.append(c);
        }

        return null;
    }

    private static MacroTag makeMacroTag(String wiki, int startIndex, int i, String command, String args)
    {
        if (RenderUtils.isBlank(command) || command.startsWith("$"))
            return null;

        String originalText = wiki.substring(startIndex, i + 1);

        if (evenNumberOfBracketsAt(wiki, i))
            return null;

        return new MacroTag(startIndex, originalText, command, args);
    }

    private static boolean evenNumberOfBracketsAt(String wiki, int i)
    {
        for (int j = 0; j + i < wiki.length(); j++)
        {
            if (wiki.charAt(j + i) != '}')
            {
                return j % 2 == 0;
            }
        }

        return false;
    }

    private MacroTag(int startIndex, String originalText, String command, String argString)
    {
        this.startIndex = startIndex;
        this.endIndex = startIndex + originalText.length() - 1;
        this.originalText = originalText;
        this.command = command;
        this.argString = argString;
    }

    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MacroTag macroTag = (MacroTag) o;

        if (endIndex != macroTag.endIndex) return false;
        if (startIndex != macroTag.startIndex) return false;
        if (argString != null ? !argString.equals(macroTag.argString) : macroTag.argString != null) return false;
        if (command != null ? !command.equals(macroTag.command) : macroTag.command != null) return false;
        if (originalText != null ? !originalText.equals(macroTag.originalText) : macroTag.originalText != null)
            return false;

        return true;
    }

    public int hashCode()
    {
        int result;
        result = startIndex;
        result = 31 * result + endIndex;
        result = 31 * result + (originalText != null ? originalText.hashCode() : 0);
        result = 31 * result + (command != null ? command.hashCode() : 0);
        result = 31 * result + (argString != null ? argString.hashCode() : 0);
        return result;
    }
}
