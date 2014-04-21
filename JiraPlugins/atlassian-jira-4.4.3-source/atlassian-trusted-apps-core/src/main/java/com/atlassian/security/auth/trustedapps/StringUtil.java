package com.atlassian.security.auth.trustedapps;

/*
 * Portions Copyright (c) 2002 JSON.org Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following
 * conditions: The above copyright notice and this permission notice shall be included in all copies or substantial
 * portions of the Software. The Software shall be used for Good, not Evil. THE SOFTWARE IS PROVIDED "AS IS", WITHOUT
 * WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR
 * A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * String manipulation methods.
 */
class StringUtil
{
    /**
     * Splits a comma separated list of values. Values must be quoted and can be encased in square-brackets (JSON
     * style).
     * 
     * @param the
     *            source String
     * @return an array of String values
     */
    static String[] split(final String source)
    {
        Null.not("source", source);

        final List<String> result = new JSONList(source.trim());
        return result.toArray(new String[result.size()]);
    }

    static String toString(final String[] source)
    {
        Null.not("source", source);

        return new JSONList(source).toString();
    }

    /**
     * subset of JSONArray that simply takes a String and converts to a list.
     */
    private static class JSONList extends LinkedList<String>
    {
        private static final long serialVersionUID = -8317241062936626298L;

        JSONList(final String source)
        {
            final Tokenizer tokenizer = new Tokenizer(source);

            if (tokenizer.nextClean() != '[')
            {
                tokenizer.syntaxError("String must start with square bracket");
            }
            switch (tokenizer.nextClean())
            {
                case ']':
                case 0:
                    return;
            }
            tokenizer.back();
            for (;;)
            {
                if (tokenizer.nextClean() == ',')
                {
                    tokenizer.back();
                    this.add(null);
                }
                else
                {
                    tokenizer.back();
                    this.add(tokenizer.nextValue());
                }
                final char nextClean = tokenizer.nextClean();
                switch (nextClean)
                {
                    case ';':
                    case ',':
                        switch (tokenizer.nextClean())
                        {
                            case ']':
                            case 0:
                                return;
                        }
                        tokenizer.back();
                        break;
                    case ']':
                    case 0:
                        return;
                    default:
                        tokenizer.syntaxError("Expected a ',' or ']' rather than: " + (int) nextClean);
                }
            }
        }

        JSONList(final String[] source)
        {
            super(Arrays.asList(source));
        }

        String join(final String separator)
        {
            final int len = size();
            final StringBuffer sb = new StringBuffer();

            for (int i = 0; i < len; i += 1)
            {
                if (i > 0)
                {
                    sb.append(separator);
                }
                sb.append(quote(get(i)));
            }
            return sb.toString();
        }

        /**
         * Produce a string in double quotes with backslash sequences in all the right places. A backslash will be
         * inserted within </, allowing JSON text to be delivered in HTML. In JSON text, a string cannot contain a
         * control character or an unescaped quote or backslash.
         * 
         * @param string
         *            A String
         * @return A String correctly formatted for insertion in a JSON text.
         */
        String quote(final String string)
        {
            if ((string == null) || (string.length() == 0))
            {
                return "\"\"";
            }

            char b;
            char c = 0;
            int i;
            final int len = string.length();
            final StringBuffer sb = new StringBuffer(len + 4);
            String t;

            sb.append('"');
            for (i = 0; i < len; i += 1)
            {
                b = c;
                c = string.charAt(i);
                switch (c)
                {
                    case '\\':
                    case '"':
                        sb.append('\\');
                        sb.append(c);
                        break;
                    case '/':
                        if (b == '<')
                        {
                            sb.append('\\');
                        }
                        sb.append(c);
                        break;
                    case '\b':
                        sb.append("\\b");
                        break;
                    case '\t':
                        sb.append("\\t");
                        break;
                    case '\n':
                        sb.append("\\n");
                        break;
                    case '\f':
                        sb.append("\\f");
                        break;
                    case '\r':
                        sb.append("\\r");
                        break;
                    default:
                        if ((c < ' ') || ((c >= '\u0080') && (c < '\u00a0')) || ((c >= '\u2000') && (c < '\u2100')))
                        {
                            t = "000" + Integer.toHexString(c);
                            sb.append("\\u").append(t.substring(t.length() - 4));
                        }
                        else
                        {
                            sb.append(c);
                        }
                }
            }
            sb.append('"');
            return sb.toString();
        }

        @Override
        public String toString()
        {
            try
            {
                return '[' + join(",") + ']';
            }
            catch (final Exception e)
            {
                return "";
            }
        }

        /**
         * Tokenizer takes a source string and extracts characters and tokens from it. Adapted from JSONTokener in the
         * json.org package.
         * <p>
         * Kept as an inner class - or rather ghetto - so outer class can deal with Iterator conversion. Not the
         * prettiest code in the world by a long shot.
         */
        private static class Tokenizer
        {
            /**
             * The source string being tokenized.
             */
            private final String source;

            /**
             * The index of the next character.
             */
            private int index = 0;

            /**
             * Construct a JSONTokener from a string.
             * 
             * @param s
             *            A source string.
             */
            Tokenizer(final String s)
            {
                source = s;
            }

            /**
             * Back up one character. This provides a sort of lookahead capability, so that you can test for a digit or
             * letter before attempting to parse the next number or identifier.
             */
            private void back()
            {
                if (index > 0)
                {
                    index -= 1;
                }
            }

            /**
             * Determine if the source string still contains characters that next() can consume.
             * 
             * @return true if not yet at the end of the source.
             */
            private boolean more()
            {
                return index < source.length();
            }

            /**
             * Get the next character in the source string.
             * 
             * @return The next character, or 0 if past the end of the source string.
             */
            private char next()
            {
                if (more())
                {
                    final char c = source.charAt(index);
                    index += 1;
                    return c;
                }
                return 0;
            }

            /**
             * Get the next n characters.
             * 
             * @param n
             *            The number of characters to take.
             * @return A string of n characters.
             * @throws JSONException
             *             Substring bounds error if there are not n characters remaining in the source string.
             */
            private String next(final int n)
            {
                final int i = index;
                final int j = i + n;
                if (j >= source.length())
                {
                    throw new IllegalStateException("Substring bounds error");
                }
                index += n;
                return source.substring(i, j);
            }

            /**
             * Get the next char in the string, skipping whitespace and comments (slashslash, slashstar, and hash).
             * 
             * @return A character, or 0 if there are no more characters.
             * @throws JSONException
             *             in case of a syntax error
             */
            private char nextClean()
            {
                for (;;)
                {
                    char c = next();
                    if (c == '/')
                    {
                        switch (next())
                        {
                            case '/':
                                do
                                {
                                    c = next();
                                }
                                while ((c != '\n') && (c != '\r') && (c != 0));
                                break;
                            case '*':
                                for (;;)
                                {
                                    c = next();
                                    if (c == 0)
                                    {
                                        throw new IllegalStateException("Unclosed comment");
                                    }
                                    if (c == '*')
                                    {
                                        if (next() == '/')
                                        {
                                            break;
                                        }
                                        back();
                                    }
                                }
                                break;
                            default:
                                back();
                                return '/';
                        }
                    }
                    else if (c == '#')
                    {
                        do
                        {
                            c = next();
                        }
                        while ((c != '\n') && (c != '\r') && (c != 0));
                    }
                    else if ((c == 0) || (c > ' '))
                    {
                        return c;
                    }
                }
            }

            /**
             * Return the characters up to the next close quote character. Backslash processing is done. The formal JSON
             * format does not allow strings in single quotes, but an implementation is allowed to accept them.
             * 
             * @param quote
             *            The quoting character, either <code>"</code>&nbsp;<small>(double quote)</small> or
             *            <code>'</code>&nbsp;<small>(single quote)</small>.
             * @return A String.
             * @throws JSONException
             *             Unterminated string.
             */
            private String nextString(final char quote)
            {
                char c;
                final StringBuffer sb = new StringBuffer();
                for (;;)
                {
                    c = next();
                    switch (c)
                    {
                        case 0:
                        case '\n':
                        case '\r':
                            throw new IllegalStateException("Unterminated string");
                        case '\\':
                            c = next();
                            switch (c)
                            {
                                case 'b':
                                    sb.append('\b');
                                    break;
                                case 't':
                                    sb.append('\t');
                                    break;
                                case 'n':
                                    sb.append('\n');
                                    break;
                                case 'f':
                                    sb.append('\f');
                                    break;
                                case 'r':
                                    sb.append('\r');
                                    break;
                                case 'u':
                                    sb.append((char) Integer.parseInt(next(4), 16));
                                    break;
                                case 'x':
                                    sb.append((char) Integer.parseInt(next(2), 16));
                                    break;
                                default:
                                    sb.append(c);
                            }
                            break;
                        default:
                            if (c == quote)
                            {
                                return sb.toString();
                            }
                            sb.append(c);
                    }
                }
            }

            /**
             * Get the next value. The value can be a Boolean, Double, Integer, JSONArray, JSONObject, Long, or String,
             * or the JSONObject.NULL object.
             * 
             * @return An object.
             * @throws JSONException
             *             If syntax error.
             */
            private String nextValue()
            {
                char c = nextClean();
                String s;

                switch (c)
                {
                    case '"':
                    case '\'':
                        return nextString(c);
                }

                /*
                 * Handle unquoted text. This could be the values true, false, or null, or it can be a number. An
                 * implementation (such as this one) is allowed to also accept non-standard forms. Accumulate characters
                 * until we reach the end of the text or a formatting character.
                 */

                final StringBuffer sb = new StringBuffer();
                while ((c >= ' ') && (",:]}/\\\"[{;=#".indexOf(c) <= 0))
                {
                    sb.append(c);
                    c = next();
                }
                back();

                /*
                 * If it is true, false, or null, return the proper value.
                 */

                s = sb.toString().trim();
                if (s.equals(""))
                {
                    throw new IllegalStateException("Missing value" + toString());
                }
                if (s.equalsIgnoreCase("null"))
                {
                    return null;
                }

                return s;
            }

            void syntaxError(final String message)
            {
                throw new IllegalStateException(message + toString());
            }

            @Override
            public String toString()
            {
                return " at character " + index + " of " + source;
            }
        }
    }
}