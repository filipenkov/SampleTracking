/*
 * [FastCat.java]
 *
 * Summary: Stripped down, fast replacement for StringBuilder and StringWriter to build concatenated Strings.
 *
 * Copyright: (c) 2009-2011 Roedy Green, Canadian Mind Products, http://mindprod.com
 *
 * Licence: This software may be copied and used freely for any purpose but military.
 *          http://mindprod.com/contact/nonmil.html
 *
 * Requires: JDK 1.5+
 *
 * Created with: JetBrains IntelliJ IDEA IDE http://www.jetbrains.com/idea/
 *
 * Version History:
 *  1.0 2009-09-29 initial release
 *  1.1 2009-10-09 return this from append
 *  1.2 2010-01-25 fix bug in rarely used clear method.
 *  1.3 2010-02-13 convert from JDK 1.6 to JDK 1.5
 */
package com.mindprod.fastcat;

import java.io.File;
import java.util.Arrays;

import static java.lang.System.out;

/**
 * Stripped down, fast replacement for StringBuilder and StringWriter to build concatenated Strings.
 * <p/>
 * For concatenating individual characters, StringBuilder will probably be faster.
 * It has an overhead of 4 bytes per piece appended, which would be 50% if the Strings were only 2-bytes long.
 * This algorithm works best with fairly long pieces. The main advantage is it is much easier to get a precise
 * estimate of how much space you will need.
 *
 * @author Roedy Green, Canadian Mind Products
 * @version 1.3 2010-02-13 convert from JDK 1.6 to JDK 1.5
 * @since 2009-09-29
 */
public class FastCat
    {
    // ------------------------------ CONSTANTS ------------------------------

    /**
     * undisplayed copyright notice
     */
    @SuppressWarnings( { "UnusedDeclaration" } )
    public static final String EMBEDDED_COPYRIGHT =
            "Copyright: (c) 2009-2011 Roedy Green, Canadian Mind Products, http://mindprod.com";

    /**
     * when package was released.
     *
     * @noinspection UnusedDeclaration
     */
    private static final String RELEASE_DATE = "2010-02-13";

    /**
     * version of package.
     *
     * @noinspection UnusedDeclaration
     */
    private static final String VERSION_STRING = "1.3";

    // ------------------------------ FIELDS ------------------------------

    /**
     * private collection of pieces we will later glue together once we know the size of the final String.
     * For RAM efficiency, we use an array, not an ArrayList
     */
    private final String[] pieces;

    /**
     * how many pieces we have collected pending so far
     */
    private int count = 0;

    /**
     * total length of all the  pieces we have collected pending so far
     */
    private int length = 0;

    // -------------------------- PUBLIC INSTANCE  METHODS --------------------------

    /**
     * no-arg constructor, defaults to 20 pieces as the estimate.
     */
    public FastCat()
        {
        this( 20 );
        }

    /**
     * constructor
     *
     * @param estNumberOfPieces estimated number of chunks you will concatenate. If the estimate is low, you will get ArrayIndexOutOfBoundsExceptions.
     */
    public FastCat( int estNumberOfPieces )
        {
        pieces = new String[ estNumberOfPieces ];
        }

    /**
     * append String
     *
     * @param s String to append
     *
     * @return this
     */
    public FastCat append( String s )
        {
        if ( count >= pieces.length )
            {
            overflow();
            }
        length += s.length();
        pieces[ count++ ] = s;
        return this;
        }

    /**
     * append arbitrary number of strings
     *
     * @param ss comma-separated list of Strings to append
     *
     * @return this
     */
    public FastCat append( String... ss )
        {
        for ( String s : ss )
            {
            if ( count >= pieces.length )
                {
                overflow();
                }
            length += s.length();
            pieces[ count++ ] = s;
            }
        return this;
        }

    /**
     * append int
     *
     * @param i int to append.
     *
     * @return this
     */
    public FastCat append( int i )
        {
        return append( Integer.toString( i ) );
        }

    /**
     * append char
     *
     * @param c char to append.
     *          If you use this method extensively, you will probably get better performance from StringBuilder.
     *
     * @return this
     */
    public FastCat append( char c )
        {
        if ( count >= pieces.length )
            {
            overflow();
            }
        final String s = String.valueOf( c );
        length += s.length();
        pieces[ count++ ] = s;
        return this;
        }

    /**
     * append Object
     *
     * @param o Object to append.  toString is called to acquire a String to concatenate.
     *
     * @return this
     */
    public FastCat append( Object o )
        {
        if ( count >= pieces.length )
            {
            overflow();
            }
        final String s = o.toString();
        length += s.length();
        pieces[ count++ ] = s;
        return this;
        }

    /**
     * append arbitrary number of Objects
     *
     * @param oo comma-separated list of Objects to to append. toString is called to acquire a String to concatenate.
     *
     * @return this
     */
    public FastCat append( Object... oo )
        {
        for ( Object o : oo )
            {
            if ( count >= pieces.length )
                {
                overflow();
                }
            final String s = o.toString();
            length += s.length();
            pieces[ count++ ] = s;
            }
        return this;
        }

    /**
     * empty the concatenated String being created
     */
    public void clear()
        {
        // clear out strings so they can be garbage collected. All would work without fill, but it would waste RAM.
        // works ok even if count is 0.
        // are we are doing is setting each of the pieces to a null, thus freeing the associated string for gc
        Arrays.fill( pieces, 0, count, null );
        count = 0;
        length = 0;
        }

    /**
     * current buffer length.
     *
     * @return current total of count of chars appended
     */
    public int length()
        {
        return length;
        }

    /**
     * Get the concatenation of all the strings appended so far
     */
    public String toString()
        {
        int offset = 0;
        final char[] buffer = new char[ length ];
        for ( int i = 0; i < count; i++ )
            {
            final String piece = pieces[ i ];
            // copy chars from String into our accumulating char[] buffer with System.arraycopy.
            piece.getChars( 0, piece.length(), buffer, offset );
            offset += piece.length();
            }
        return new String( buffer );  // Would like some way to just hand buffer over to String to avoid copy.
        }

    // -------------------------- OTHER METHODS --------------------------

    /**
     * We overflowed the array.  Give up.  Get user to improve estimate.  We could recover automatically,
     * but that would defeat the intent of fast code.
     */
    private void overflow()
        {
        // Sun JVM lets us figure out where the call came from.
        StackTraceElement e = new Throwable().getStackTrace()[ 2 ];
        throw new ArrayIndexOutOfBoundsException( "FastCat estimate "
                                                  + pieces.length
                                                  + " is too low.\n"
                                                  + e.getClassName()
                                                  + "."
                                                  + e.getMethodName()
                                                  + " line:"
                                                  + e.getLineNumber() );
        }

    // --------------------------- main() method ---------------------------

    /**
     * test harness
     *
     * @param args not used.
     */
    public static void main( String[] args )
        {
        FastCat sb = new FastCat( 7 );
        sb.append( "Hello" );
        sb.append( " " );
        sb.append( "World. " );
        sb.append( new File( "temp.txt" ) );
        sb.append( " ", "abc", "def" );
        out.println( sb.toString() );
        // prints Hello World. temp.txt abcdef
        }
    }
