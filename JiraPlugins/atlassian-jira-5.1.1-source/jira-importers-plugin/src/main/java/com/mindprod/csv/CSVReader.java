/*
 * [CSVReader.java]
 *
 * Summary: Read CSV (Comma Separated Value) files.
 *
 * Copyright: (c) 2002-2011 Roedy Green, Canadian Mind Products, http://mindprod.com
 *
 * Licence: This software may be copied and used freely for any purpose but military.
 *          http://mindprod.com/contact/nonmil.html
 *
 * Requires: JDK 1.5+
 *
 * Created with: JetBrains IntelliJ IDEA IDE http://www.jetbrains.com/idea/
 *
 * Version History:
 *  1.0 2002-03-27 initial release
 *  1.1 2002-03-28 close
 *                 configurable separator char
 *                 no longer sensitive to line-ending convention.
 *                 uses a categorise routine to message categories for use in case clauses.
 *                 faster skipToNextLine
 *  1.2 2002-04-17 put in to separate package
 *  1.3 2002-04-17
 *  1.4 2002-04-19 fix bug if last field on line is empty, was not counting as a field.
 *  1.5 2002-04-19
 *  1.6 2002-05-25 allow choice of " or ' quote char.
 *  1.7 2002-08-29 getAllFieldsInLine
 *  1.8 2002-11-12 allow Microsoft Excel format fields that can span several lines. sponsored by Steve Hunter of agilense.com
 *  1.9 2002-11-14 trim parameter to control whether fields are trimmed of lead/trail whitespace (blanks, Cr, Lf, Tab etc.)
 *  2.0 2003-08-10 getInt, getLong, getFloat, getDouble
 *  2.1 2005-07-16 reorganisation, new bat files.
 *  2.2 2005-08-28 add CSVAlign and CSVPack to the suite.
 *  2.3 2005-08-28 add CSVAlign and CSVPack to the suite.
 *                 Use java com.mindprod.CSVAlign somefile.csv
 *  2.4 2007-05-20 add icon and PAD
 *  2.5 2007-11-27 tidy comments
 *  2.6 2008-02-20 IntelliJ inspector, spell corrections, tightening code.
 *  2.7 2008-05-28 add CSVTab2Comma.
 *  2.8 2008-06-04 add CSVWriter put for various primitives.
 *  2.9 2009-03-27 refactor using enums, support comments.
 *                 major rewrite. Now supports #-style
 *                 comments. More efficient RAM use. You can configure the
 *                 separator character, quote character and comment character.
 *                 You can read seeing or hiding the comments. The API was
 *                 changed to support comments.
 *  3.0 2009-06-15 lookup table to speed CSVReader
 *  3.1 2009-12-03 add CSVSort
 *  3.2 2010-02-23 add hex sort 9x+ option to CSVSort
 *  3.3 2010-11-14 change default to no comments in input file for CSVTab2Comma.
 *  3.4 2010-12-03 add CSV2SRS
 *  3.5 2010-12-11 add CSVReshape
 *  3.6 2010-12-14 add Lines2CSV
 *  3.7 2010-12-17 add CSVDeDup
 *  3.8 2010-12-31 add CSVPatch
 *  3.9 2011-01-22 add CSVTuple
 *  4.0 2011-01-23 add CSVToTable and TableToCSV
 *  4.1 2011-01-24 add CSVEntify and CSVStripEntities
 *  4.2 2011-01-25 modify all utilities so you can specify the encoding, default to UTF-8.
 *  4.3 2011-02-08 add support for sorting by field length. Add CSVCondense.
 *  4.4 2011-02-09 add getYYYYMMDD to CSVReader, improve error exceptions in CSVReader.
 *  4.5 2011-02-17 new method wasLabelComment to detect ## field labelling comments.
 *  4.6 2011-02-25 new csvReader constructor parm trimUnquoted. Use Intellij to fill it in with true every place full constructor used.
 */
package com.mindprod.csv;

import com.mindprod.common11.BigDate;
import com.mindprod.fastcat.FastCat;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;

import static com.mindprod.csv.CSVCharCategory.*;
import static com.mindprod.csv.CSVReadState.*;
import static java.lang.System.out;
/*
TODO:
>I see the tests include embedded newlines, "\n", but not embedded
>carriage returns, "\r". On my platform, a descendant of BSD unix called
>Darwin, the result of `ant test` shows no errors.
That was intentional.
I figured it would be easier to deal with field results in canonical \n form, easy to split.
I need to check this out, and document the feature, and make sure if such fields
are written out CSVWriter honours the line separator convention.
*/

/**
 * Read CSV (Comma Separated Value) files.
 * <p/>
 * This format is used my Microsoft Word and Excel. Fields are separated by
 * commas, and enclosed in quotes if they contain commas or quotes. Embedded quotes are doubled. Embedded spaces do not
 * normally require surrounding quotes. The last field on the line is not followed by a comma. Null fields are
 * represented by two commas in a row. We optionally trim leading and trailing spaces on fields, even inside quotes.
 * File must normally end with a single CrLf, other wise you will get a null when trying to read a field on older JVMs.
 * <p/>
 * Must be combined with your own code.
 * <p/>
 * There is another CSVReader at: at http://ostermiller.org/utils/ExcelCSV.html
 * If this CSVReader is not suitable for you, try that one. <br /> There is one written in C# at
 * http://www.csvreader.com/
 * <p/>
 * Future ideas:
 * <p/>
 * 1. allow \ to be used for quoting characters.
 *
 * @author Roedy Green, Canadian Mind Products
 * @version 4.6 2011-02-25 new csvReader constructor parm trimUnquoted. Use Intellij to fill it in with true every place full constructor used.
 * @since 2002-03-27
 */
public final class CSVReader
    {
    // ------------------------------ CONSTANTS ------------------------------

    /**
     * true if want debugging output and debugging test harness code
     */
    private static final boolean DEBUGGING = false;

    /**
     * e.g. \n \r\n or \r, whatever system uses to separate lines in a text file. Only used inside multiline fields. The
     * file itself should use Windows format \r \n, though \n by itself will also work.
     */
    private static final String lineSeparator = System.getProperty( "line.separator" );

    // ------------------------------ FIELDS ------------------------------

    /**
     * Reader source of the CSV fields to be read.
     */
    private BufferedReader r;

    /**
     * table to for quick lookup of char category for chars 0..255
     */
    private CSVCharCategory[] lookup;

    /**
     * finite state automaton for parsing characters on the line
     */
    private CSVReadState state = SEEKING_FIELD;

    /**
     * what char start off a comment.
     */
    private final String commentChars;

    /**
     * true if comments are allowed
     */

    private final boolean allowComments;

    /**
     * state of the parser's finite state automaton. The line we are parsing. null means none read yet. Line contains
     * unprocessed chars. Processed ones are removed.
     */
    private String line = null;

    /**
     * true if reader should allow quoted fields to span more than one line. Microsoft Excel sometimes generates files
     * like this.
     */
    private final boolean allowMultiLineFields;

    /**
     * true means client does not see the comments.  It as if they were not there.
     */
    private final boolean hideComments;

    /**
     * true if quoted fields are trimmed of lead/trail blanks.  Usually true.
     */
    private final boolean trimQuoted;

    /**
     * true if unquoted fields are trimmed of lead/trail blanks. Usually true.
     */
    private final boolean trimUnquoted;

    /**
     * true if can use fast table lookup to categorise chars, later set true when table built.
     */
    private boolean useLookup = false;

    /**
     * true if last field returned via get was a comment, including a ## label comment..
     */
    private boolean wasComment;

    /**
     * true if last field returned via get was a ## label comment.
     */
    private boolean wasLabelComment;

    /**
     * quote character, usually '\"' '\'' for SOL used to enclose fields containing a separator character.
     */
    private final char quoteChar;

    /**
     * field separator character, usually ',' in North America, ';' in Europe and sometimes '\t' for tab.
     */
    private final char separatorChar;

    /**
     * how many fields have been processed on this line so far, not counting comments or EOL
     */
    private int fieldCount = 0;

    /**
     * How many lines we have read so far. Used in error messages.
     */
    private int lineCount = 0;

    /**
     * points to next character to process in the current line.
     */
    private int offset;

    // -------------------------- PUBLIC INSTANCE  METHODS --------------------------

    /**
     * Simplified convenience constructor to read a CSV file , default to comma separator, " for quote, no multiline fields, with trimming.
     *
     * @param r input Reader source of CSV Fields to read.  Should be buffered.
     */
    public CSVReader( Reader r )
        {
        // reader, separatorChar, quoteChar, commentChars, hideComments, trimQuoted, trimUnquoted, trimquoted, allowMultipleLineFields
        this( r, ',', '\"', "#", true, true /* trimQuoted */, true /* trimUnquoted */, false, false );
        }

    /**
     * Detailed constructor to read a CSV file
     *
     * @param r                    input Reader source of CSV Fields to read.  Should be a BufferedReader.
     * @param separatorChar        field separator character, usually ',' in North America, ';' in Europe and sometimes
     *                             '\t' for tab. Note this is a 'char' not a "string".
     * @param quoteChar            char to use to enclose fields containing a separator, usually '\"' . Use (char)0 if
     *                             you don't want a quote character, or any other char that will not appear in the file.
     *                             Note this is a 'char' not a "string".
     * @param commentChars         characters that mark the start of a comment, usually "#", but can be multiple chars.
     *                             Note this is a "string" not a 'char'.
     * @param hideComments         true if clients sees none of the comments.  false if client processes the comments.
     * @param trimQuoted           true if quoted fields are trimmed of lead/trail blanks.  Usually true.
     * @param trimUnquoted         true if unquoted fields are trimmed of lead/trail blanks. Usually true.
     * @param allowMultiLineFields true if reader should allow quoted fields to span more than one line. Microsoft Excel
     * @param allowComments        true if reader should allow comments
     */
    public CSVReader( final Reader r,
                      final char separatorChar,
                      final char quoteChar,
                      final String commentChars,
                      final boolean hideComments,
                      final boolean trimQuoted,
                      final boolean trimUnquoted,
                      final boolean allowMultiLineFields,
                      final boolean allowComments
    )
        {
        /* convert Reader to BufferedReader if necessary */
        this.r = ( r instanceof BufferedReader ) ? ( BufferedReader ) r : new BufferedReader( r );    /* default buffer size is 8K */
        this.separatorChar = separatorChar;
        this.quoteChar = quoteChar;
        this.commentChars = commentChars;
        this.hideComments = hideComments;
        this.trimQuoted = trimQuoted;
        this.trimUnquoted = trimUnquoted;
        this.allowMultiLineFields = allowMultiLineFields;
        this.allowComments = allowComments;
        buildLookup();
        }

    /**
     * Close the Reader.
     *
     * @throws IOException if problems closing
     */
    public void close() throws IOException
        {
        if ( r != null )
            {
            r.close();
            r = null;
            }
        }

    /**
     * Read one field from the CSV file. You can also use methods like getInt and getDouble to parse the String for you.
     * You can use getAllFieldsInLine to read the entire line including the EOL.
     *
     * @return String value, even if the field is numeric. Surrounded and embedded double quotes are stripped. possibly
     *         "". null means end of line.  Normally you use skiptoNextLine to start the next line rather then using get
     *         to read the eol.  Might also be a comment, with lead # stripped.
     *         If field was a comment, it is returned with lead # stripped. Check wasComment to see if it was a comment
     *         or a data field.
     * @throws EOFException at end of file after all the fields have been read.
     * @throws IOException  Some problem reading the file, possibly malformed data.
     * @noinspection UnusedLabel
     */
    public String get() throws IOException
        {
        // we need a StringBuilder since we analyse double " and handle multiline fields.
        // We can't an simply track start and end of string.
        // StringBuilder is better than FastCat for char by char work.
        StringBuilder field = new StringBuilder( allowMultiLineFields ? 512 : 64 );
        /* don't need to maintain state between fields, just offset where to continue processing line. */
        this.wasComment = false;
        this.wasLabelComment = false;
        lineLoop:
        while ( true )
            {
            getLineIfNeeded();
            charLoop:
            /* loop for each char in the line to find a field */
            /* guaranteed to leave early by hitting EOL */
            // pick up from where we left off getting last field.
            for ( int i = offset; i < line.length(); i++ )
                {
                char c = line.charAt( i );
                CSVCharCategory category = categorise( c );
                if ( DEBUGGING )
                    {
                    // for debugging
                    out.println( "char:"
                                 + c
                                 + " state:"
                                 + state
                                 + " field:"
                                 + field.length() );
                    }
                // nested switch state:char category
                switch ( state )
                    {
                    case AFTER_END_QUOTE:
                    {
                    /*
                    * In situation like this "xxx" which may turn out to be
                    * xxx""xxx" or "xxx", We find out here.
                    */
                    switch ( category )
                        {
                        case COMMENT_START:
                            // either skip over comment or process it later
                            offset = hideComments ? line.length() : i;
                            // handle pending field
                            state = SEEKING_FIELD;
                            fieldCount++;
                            return quotedField( field );
                        case ORDINARY:
                            throw new IOException( complaint( separatorChar, i ) );
                        case QUOTE:
                            /* was a double quotechar, e.g. a literal " */
                            field.append( c );
                            state = IN_QUOTED;
                            break;
                        case SEPARATOR:
                            /* we are done with field. */
                            offset = i + 1;
                            state = SEEKING_START;
                            fieldCount++;
                            return quotedField( field );
                        case WHITESPACE:
                            /* ignore trailing spaces up to separatorChar */
                            state = SKIPPING_TAIL;
                            break;
                        }
                    break;
                    }
                    case IN_PLAIN:
                    {
                    /* in middle of ordinary field */
                    switch ( category )
                        {
                        case COMMENT_START:
                            // either skip over comment, or handle it later
                            offset = hideComments ? line.length() : i;
                            // handle pending field
                            state = SEEKING_FIELD;
                            fieldCount++;
                            return unquotedField( field );
                        case ORDINARY:
                            field.append( c );
                            break;
                        case QUOTE:
                            throw new IOException( complaint( quoteChar, i ) );
                        case SEPARATOR:
                            /* done */
                            offset = i + 1;
                            state = SEEKING_START;
                            fieldCount++;
                            return unquotedField( field );
                        case WHITESPACE:
                            field.append( ' ' );
                            break;
                        }
                    break;
                    }
                    case IN_QUOTED:
                    {
                    /* in middle of field surrounded in quotes */
                    switch ( category )
                        {
                        case COMMENT_START:  // inside quotes only " is a special character.
                        case ORDINARY:
                        case SEPARATOR:
                        case WHITESPACE:
                            field.append( c );
                            break;
                        case QUOTE:
                            state = AFTER_END_QUOTE;
                            break;
                        }
                    break;
                    }
                    case SEEKING_FIELD:
                    {
                    /* in blanks before first field */
                    switch ( category )
                        {
                        case COMMENT_START:
                            if ( hideComments )
                                {
                                if ( fieldCount > 0 )
                                    { // bypass comment
                                    offset = line.length(); /* carry on at eol */
                                    state = SEEKING_FIELD;
                                    break charLoop;
                                    }
                                else
                                    {
                                    // comment on by itself. ignore the whole thing, including the EOL.
                                    line = null;
                                    state = SEEKING_FIELD;
                                    continue lineLoop;
                                    }
                                }
                            else
                                {
                                // entire rest of line is a comment.
                                offset = line.length();/* carry on at eol */
                                state = SEEKING_FIELD;
                                // don't increment fieldCount, this is a comment field.
                                wasComment = true;
                                // strip off lead #
                                final String comment = trimUnquoted ? line.substring( i + 1 ).trim() : line.substring( i + 1 );

                                if ( comment.length() > 0 && commentChars.indexOf( comment.charAt( 0 ) ) >= 0 )
                                    {
                                    wasLabelComment = true;
                                    }
                                return comment;
                                }
                        case QUOTE:
                            state = IN_QUOTED;
                            break;
                        case SEPARATOR:
                            /* end of empty field */
                            offset = i + 1;
                            state = SEEKING_START;
                            fieldCount++;
                            return "";
                        case ORDINARY:
                            field.append( c );
                            state = IN_PLAIN;
                            break;
                        case WHITESPACE:
                            /* ignore */
                            break;
                        }
                    break;
                    }
                    case SEEKING_START:
                    {
                    /* in blanks before field */
                    switch ( category )
                        {
                        case COMMENT_START:
                            // either bypass comment or arrange to deal with it later
                            offset = hideComments ? line.length() : i;
                            // handle pending empty field
                            state = SEEKING_FIELD;
                            fieldCount++;
                            return "";
                        case QUOTE:
                            state = IN_QUOTED;
                            break;
                        case SEPARATOR:
                            /* end of empty field */
                            offset = i + 1;
                            state = SEEKING_START;
                            fieldCount++;
                            return "";
                        case ORDINARY:
                            field.append( c );
                            state = IN_PLAIN;
                            break;
                        case WHITESPACE:
                            /* ignore */
                            break;
                        }
                    break;
                    }
                    case SKIPPING_TAIL:
                    {
                    /* in spaces after quoted field, seeking separatorChar */
                    switch ( category )
                        {
                        case COMMENT_START:
                            // handle pending field, deal with comment later.
                            offset = i;
                            state = SEEKING_FIELD;
                            fieldCount++;
                            return quotedField( field );
                        case ORDINARY:
                        case QUOTE:
                            throw new IOException(
                                    complaint( separatorChar, i )
                            );
                        case SEPARATOR:
                            offset = i + 1;
                            state = SEEKING_START;
                            fieldCount++;
                            return quotedField( field );
                        case WHITESPACE:
                            /* ignore trailing spaces up to separatorChar */
                            break;
                        }
                    break;
                    }
                    }// end switch(state)
                }
            // end charLoop over remaining chars in line.
            if ( DEBUGGING )
                {
                // for debugging
                out.println( "EOL state:"
                             + state
                             + " field:"
                             + field.length() );
                }
            // if not found a field yet, handle the end of line.
            switch ( state )
                {
                case AFTER_END_QUOTE:
                    /*
                     * In situation like this "xxx" which may turn out to be
                     * xxx""xxx" or "xxx", We find out here.
                    */
                    offset = line.length();/* carry on at eol */
                    state = SEEKING_FIELD;
                    fieldCount++;
                    return quotedField( field );
                case SEEKING_FIELD:
                    /* in blanks prior to start of a field  */
                    /* null to mark end of line */
                    // mark line as done, we need a new line of characters.
                    // carry on at start of next line.
                    line = null;
                    state = SEEKING_FIELD;
                    // return null for EOL
                    return null;
                case SEEKING_START:
                    /* in blanks after , */
                    /* null to mark end of line */
                    offset = line.length();/* carry on at eol */
                    // handle pending empty field
                    state = SEEKING_FIELD;
                    fieldCount++;
                    return "";
                case IN_PLAIN:
                    /* in middle of ordinary field */
                    offset = line.length(); /* push EOL back */
                    state = SEEKING_FIELD;
                    fieldCount++;
                    return unquotedField( field );
                case IN_QUOTED:
                    /* in middle of field surrounded in quotes */
                    if ( allowMultiLineFields )
                        {
                        field.append( lineSeparator );
                        // we are done with that line, but not with
                        // the
                        // field.
                        // We don't want to return a null
                        // to mark the end of the line., but we do want another line to process.
                        line = null;
                        // will read next line and seek the end of
                        // the quoted field with state = IN_QUOTED.
                        continue lineLoop;
                        }
                    else
                        {
                        // no multiline fields allowed
                        throw new IOException( complaint( quoteChar, line.length() - 1 ) );
                        }
                case SKIPPING_TAIL:
                    offset = line.length();/* carry on at eol */
                    state = SEEKING_FIELD;
                    fieldCount++;
                    return quotedField( field );
                } // end switch
            }
        // end lineLoop
        }// end get

    /**
     * Get all fields in the line. This reads only one line, not the whole file.
     * Skips to next line as a side effect, so don't need skipToNextLine.
     * Can find out if last field was a comment with wasComment();
     *
     * @return Array of strings, one for each field. Possibly empty, but never null.
     * @throws EOFException if run off the end of the file.
     * @throws IOException  if some problem reading the file.
     */
    public String[] getAllFieldsInLine() throws IOException
        {
        ArrayList<String> al = new ArrayList<String>( 100 );
        boolean lineHadComment = false;
        do
            {
            String field = get();
            if ( wasComment() )
                {
                lineHadComment = true;
                }
            if ( field == null )
                {
                break;
                }
            al.add( field );
            }
        while ( true );
        if ( lineHadComment )
            {
            wasComment = true;  // need to track specially since get null turns off last wasComment.
            final String comment = al.get( al.size() - 1 );
            if ( comment.length() > 0 && commentChars.indexOf( comment.charAt( 0 ) ) >= 0 )
                {
                wasLabelComment = true;
                }
            }
        return al.toArray( new String[ al.size() ] );
        }

    /**
     * Read one boolean field from the CSV file, e.g. (true, yes, 1, +) or (false, no, 0, -).
     *
     * @return boolean, empty field returns false, as does end of line.
     * @throws EOFException           at end of file after all the fields have been read.
     * @throws IOException            Some problem reading the file, possibly malformed data.
     * @throws NumberFormatException, if field does not contain a well-formed int.
     * @noinspection UnusedDeclaration
     */
    public boolean getBoolean() throws IOException
        {
        String s = get();
        // end of line returns 0
        if ( s == null || s.length() == 0 )
            {
            return false;
            }
        if ( s.equalsIgnoreCase( "true" ) || s.equalsIgnoreCase( "yes" ) || s.equals( "1" ) || s.equals( "+" ) )
            {
            return true;
            }
        if ( s.equalsIgnoreCase( "false" ) || s.equalsIgnoreCase( "no" ) || s.equals( "0" ) || s.equals( "-" ) )
            {
            return false;
            }
        throw new NumberFormatException(
                "Malformed boolean ["
                + s
                + "] near offset "
                + ( line.length() - 1 )
                + " after field "
                + fieldCount
                + " on line "
                + lineCount );
        }

    /**
     * Read one double field from the CSV file.
     *
     * @return double value, empty field returns 0, as does end of line.
     * @throws EOFException           at end of file after all the fields have been read.
     * @throws IOException            Some problem reading the file, possibly malformed data.
     * @throws NumberFormatException, if field does not contain a well-formed double.
     * @noinspection UnusedDeclaration
     */
    public double getDouble() throws IOException, NumberFormatException
        {
        final String s = get();
        if ( s == null || s.length() == 0 )
            {
            return 0;
            }
        try
            {
            return Double.parseDouble( s );
            }
        catch ( NumberFormatException e )
            {
            throw new NumberFormatException(
                    "Malformed double ["
                    + s
                    + "] near offset "
                    + ( line.length() - 1 )
                    + " after field "
                    + fieldCount
                    + " on line "
                    + lineCount );
            }
        }

    /**
     * Read one float field from the CSV file.
     *
     * @return float value, empty field returns 0, as does end of line.
     * @throws EOFException           at end of file after all the fields have been read.
     * @throws IOException            Some problem reading the file, possibly malformed data.
     * @throws NumberFormatException, if field does not contain a well-formed float.
     * @noinspection UnusedDeclaration
     */
    public float getFloat() throws IOException, NumberFormatException
        {
        String s = get();
        if ( s == null || s.length() == 0 )
            {
            return 0;
            }
        try
            {
            return Float.parseFloat( s );
            }
        catch ( NumberFormatException e )
            {
            throw new NumberFormatException(
                    "Malformed float ["
                    + s
                    + "] near offset "
                    + ( line.length() - 1 )
                    + " after field "
                    + fieldCount
                    + " on line "
                    + lineCount );
            }
        }

    /**
     * Read one hex-encoded integer field from the CSV file
     *
     * @return int value, empty field returns 0, as does end of line.
     * @throws EOFException           at end of file after all the fields have been read.
     * @throws IOException            Some problem reading the file, possibly malformed data.
     * @throws NumberFormatException, if field does not contain a well-formed int.
     * @noinspection UnusedDeclaration
     */
    public int getHexInt() throws IOException, NumberFormatException
        {
        String s = get();
        // end of line returns 0
        if ( s == null || s.length() == 0 )
            {
            return 0;
            }
        try
            {
            return Integer.parseInt( s, 16 );
            }
        catch ( NumberFormatException e )
            {
            throw new NumberFormatException(
                    "Malformed hex integer ["
                    + s
                    + "] near offset "
                    + ( line.length() - 1 )
                    + " after field "
                    + fieldCount
                    + " on line "
                    + lineCount );
            }
        }

    /**
     * Read one hex-encoded long field from the CSV file
     *
     * @return long value, empty field returns 0, as does end of line.
     * @throws EOFException           at end of file after all the fields have been read.
     * @throws IOException            Some problem reading the file, possibly malformed data.
     * @throws NumberFormatException, if field does not contain a well-formed long.
     * @noinspection UnusedDeclaration
     */
    public long getHexLong() throws IOException, NumberFormatException
        {
        String s = get();
        if ( s == null || s.length() == 0 )
            {
            return 0;
            }
        try
            {
            return Long.parseLong( s, 16 );
            }
        catch ( NumberFormatException e )
            {
            throw new NumberFormatException(
                    "Malformed hex long integer ["
                    + s
                    + "] near offset "
                    + ( line.length() - 1 )
                    + " after field "
                    + fieldCount
                    + " on line "
                    + lineCount );
            }
        }

    /**
     * Read one integer field from the CSV file
     *
     * @return int value, empty field returns 0, as does end of line.
     * @throws EOFException           at end of file after all the fields have been read.
     * @throws IOException            Some problem reading the file, possibly malformed data.
     * @throws NumberFormatException, if field does not contain a well-formed int.
     * @noinspection UnusedDeclaration
     */
    public int getInt() throws IOException, NumberFormatException
        {
        String s = get();
        // end of line returns 0
        if ( s == null || s.length() == 0 )
            {
            return 0;
            }
        try
            {
            return Integer.parseInt( s );
            }
        catch ( NumberFormatException e )
            {
            throw new NumberFormatException(
                    "Malformed integer ["
                    + s
                    + "] near offset "
                    + ( line.length() - 1 )
                    + " after field "
                    + fieldCount
                    + " on line "
                    + lineCount );
            }
        }

    /**
     * How many lines have been processed so far.
     *
     * @return count of how many lines have been read.
     */
    public int getLineCount()
        {
        return lineCount;
        }

    /**
     * Read one long field from the CSV file
     *
     * @return long value, empty field returns 0, as does end of line.
     * @throws EOFException           at end of file after all the fields have been read.
     * @throws IOException            Some problem reading the file, possibly malformed data.
     * @throws NumberFormatException, if field does not contain a well-formed long.
     * @noinspection UnusedDeclaration
     */
    public long getLong() throws IOException, NumberFormatException
        {
        String s = get();
        if ( s == null || s.length() == 0 )
            {
            return 0;
            }
        try
            {
            return Long.parseLong( s );
            }
        catch ( NumberFormatException e )
            {
            throw new NumberFormatException(
                    "Malformed long integer ["
                    + s
                    + "] near offset "
                    + ( line.length() - 1 )
                    + " after field "
                    + fieldCount
                    + " on line "
                    + lineCount );
            }
        }

    /**
     * Read one Date field from the CSV file, in ISO format yyyy-mm-dd
     *
     * @return yyyy-mm-dd date string, empty field returns "",  end of line. returns null.
     * @throws EOFException           at end of file after all the fields have been read.
     * @throws IOException            Some problem reading the file, possibly malformed data.
     * @throws NumberFormatException, if field does not contain a well-formed date.
     * @noinspection UnusedDeclaration
     */
    public String getYYYYMMDD() throws IOException
        {
        final String s = get();

        if ( s == null || s.length() == 0 )
            {
            return s;
            }
        if ( !BigDate.isValid(s) )
            {
            throw new NumberFormatException(
                    "Malformed ISO yyyy-mm-dd date ["
                    + s
                    + "] near offset "
                    + ( line.length() - 1 )
                    + " after field "
                    + fieldCount
                    + " on line "
                    + lineCount );
            }
        return s;
        }

    /**
     * Skip over fields you don't want to process.
     *
     * @param fields How many field you want to bypass reading. The newline counts as one field.
     *
     * @throws EOFException at end of file after all the fields have been read.
     * @throws IOException  Some problem reading the file, possibly malformed data.
     * @noinspection UnusedDeclaration
     */
    public void skip( int fields ) throws IOException
        {
        if ( fields <= 0 )
            {
            return;
            }
        for ( int i = 0; i < fields; i++ )
            {
            // throw results away
            get();
            }
        }

    /**
     * Skip over remaining fields on this line you don't want to process.
     *
     * @throws EOFException at end of file after all the fields have been read.
     * @throws IOException  Some problem reading the file, possibly malformed data.
     */
    public void skipToNextLine() throws IOException
        {
        if ( line == null )
            {
            getLineIfNeeded();
            }
        line = null;
        }

    /**
     * Was the last field returned via get a comment (including a label comment)?
     * Also works after getAllFieldsInLine to tell if there was a comment at the end of that line.
     *
     * @return true if last field returned via get was a comment.
     */
    public boolean wasComment()
        {
        return this.wasComment;
        }

    /**
     * Was the last field returned via get a label ## comment?  Also works after getAllFieldsInLine to tell if there was a comment at the end of that line.
     *
     * @return true if last field returned via get was a ## label  comment.
     */
    public boolean wasLabelComment()
        {
        return this.wasLabelComment;
        }

    // -------------------------- OTHER METHODS --------------------------

    /**
     * build table to for quick lookup of char category.
     */
    void buildLookup()
        {
        lookup = new CSVCharCategory[ 256 ];
        for ( int c = 0; c < 256; c++ )
            {
            lookup[ c ] = categorise( ( char ) c );
            }
        useLookup = true; // table now safe to use
        }

    /**
     * categorise a character for the finite state machine.
     *
     * @param c the character to categorise
     *
     * @return integer representing the character's category.
     */
    CSVCharCategory categorise( char c )
        {
        if ( useLookup && c < 256 )
            {
            return lookup[ c ];
            }
        switch ( c )
            {
            case ' ':
            case '\r':
            case '\n':
            case 0xff:
                return WHITESPACE;
            default:
                if ( c == separatorChar )
                    {
                    return SEPARATOR;
                    }
                else if ( c == quoteChar )
                    {
                    return QUOTE;
                    }
                if ( allowComments && commentChars.indexOf( c ) >= 0 )
                    {
                    return COMMENT_START;
                    }
                /* do our tests in crafted order, hoping for an early return */
                else if ( '!' <= c && c <= '~' )/* includes A-Z \\p{Lower} 0-9 common punctuation */
                    {
                    return ORDINARY;
                    }
                else if ( 0x00 <= c && c <= 0x20 || Character.isWhitespace( c ) )
                    {
                    return WHITESPACE;
                    }
                else
                    {
                    return ORDINARY;
                    }
            }
        }

    /**
     * compose an error message to describe what is wrong with the CSV file
     *
     * @param missing what expected char is missing.
     * @param near    offset near where the error is.
     *
     * @return String to describe the problem  to the user
     */
    private String complaint( char missing, int near )
        {
        FastCat sb = new FastCat( 8 );
        sb.append( "Malformed CSV stream. Missing [" );
        sb.append( missing );
        sb.append( "] near offset " );
        sb.append( near );
        sb.append( " after field " );
        sb.append( fieldCount );
        sb.append( " on line " );
        sb.append( lineCount );
        return sb.toString();
        }

    /**
     * Make sure a line is available for parsing. Does nothing if there already is one.
     *
     * @throws EOFException if hit the end of the file.
     */
    private void getLineIfNeeded() throws IOException
        {
        if ( line == null )
            {
            if ( r == null )
                {
                throw new IllegalArgumentException(
                        "attempt to use a closed CSVReader" );
                }
            line = r.readLine();/* this strips platform specific line ending */
            if ( line == null )/*
                                 * null means EOF, yet another inconsistent Java
                                 * convention.
                                 */
                {
                throw new EOFException();
                }
            else
                {
                state = (state == IN_QUOTED ? IN_QUOTED : SEEKING_FIELD);
                offset = 0;
                fieldCount = 0;
                lineCount++;
                }
            }
        }

    /**
     * Get field inside quotes.
     *
     * @param field accumulated chars for field.
     *
     * @return String, possibly trimmed.
     */
    private String quotedField( final StringBuilder field )
        {
        return trimQuoted ? field.toString().trim() : field.toString();
        }

    /**
     * Get field between commas.
     *
     * @param field accumulated chars for field.
     *
     * @return String, possibly trimmed.
     */
    private String unquotedField( final StringBuilder field )
        {
        return trimUnquoted ? field.toString().trim() : field.toString();
        }
    }// end CSVReader class.
