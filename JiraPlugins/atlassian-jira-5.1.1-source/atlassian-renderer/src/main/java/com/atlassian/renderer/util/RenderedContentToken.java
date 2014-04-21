package com.atlassian.renderer.util;

import java.util.regex.Pattern;

/**
 * Responsible for working with processing tokens that are added and removed from the stream.
 * <p>
 * These should never leave the stream, using them as input is not supported at all will 
 * cause major problems.
 * <p>
 * These are of the format: {@link Annotation#ANCHOR}tok(idx){@link Annotation#SEPARATOR}(token)tok{@link Annotation#TERMINATOR}
 * where (idx) is the stack depth and (token) is the token type.
 */
public class RenderedContentToken
{
    private static final class Annotation
    {
        private static final String TOKEN_ID = "tok"; // for some reason, this needs to be at the beginning AND end of the annotation
        /**
         *  marks start of annotated text
         */
        static final String ANCHOR = "\u0001" + TOKEN_ID; // 0001 is the ascii ctrl char STS, FFF9 is the unicode version
        /**
         *  marks end of index field
         */
        static final String SEPARATOR = "-";//"\u0017","\uFFFA"; NOTE: cannot use a control character for field separator here as it stuffs up the other regexes for some reason
        /**
         *  marks end of annotating text
         */
        static final String TERMINATOR = SEPARATOR + TOKEN_ID + "\u0002"; // 0002 is the ascii ctrl char STE, FFFC is the unicode version
    }

    //private static final String PREFIX = InterlinearAnnotation.ANCHOR;
    private static final String PREFIX = Annotation.ANCHOR;

    /**
     * Given a String rendered by {@link #token(int)}, returns the index. An improperly formatted string will cause exceptions.
     * @param token rendered by content token.
     * @return the index
     * @throws NumberFormatException
     * @throws StringIndexOutOfBoundsException
     */
    public static int getIndex(final String token) throws NumberFormatException, StringIndexOutOfBoundsException
    {
        // must match the ctor String pattern format and the token(int) format
        final int length = PREFIX.length(); // first token is ANCHOR
        final int separatorIndex = token.indexOf(Annotation.SEPARATOR);
        final String substring = token.substring(length, separatorIndex);
        return Integer.parseInt(substring);
    }

    //
    // members
    //

    private final String string;
    private final Pattern pattern;
    private final String token;

    //
    // ctors
    //

    public RenderedContentToken(final String token)
    {
        // must match the token(int) format
        string = PREFIX + "\\d+" + Annotation.SEPARATOR + token + Annotation.TERMINATOR;

        pattern = Pattern.compile(string, Pattern.CANON_EQ);
        this.token = token;
    }

    public String token(final int index)
    {
        // must match the ctor String pattern format
        return PREFIX + index + Annotation.SEPARATOR + token + Annotation.TERMINATOR;
    }

    //
    // accessors
    //

    public String getString()
    {
        return string;
    }

    public boolean matches(final CharSequence input)
    {
        return pattern.matcher(input).find();
    }

    public Pattern getPattern()
    {
        return pattern;
    }

    public String toString()
    {
        return getString();
    }
}
