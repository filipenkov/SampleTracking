package com.atlassian.velocity;

import sun.security.action.GetPropertyAction;

import java.io.*;
import java.security.AccessController;
import java.util.BitSet;

/**
 * A simple class store methods we want to expose to velocity templates
 */
public class VelocityHelper
{
    private static BitSet dontNeedEncoding;
    static final int caseDiff = ('a' - 'A');
    static String dfltEncName = null;

    static
    {
        dontNeedEncoding = new BitSet(256);
        int i;
        for (i = 'a'; i <= 'z'; i++)
        {
            dontNeedEncoding.set(i);
        }
        for (i = 'A'; i <= 'Z'; i++)
        {
            dontNeedEncoding.set(i);
        }
        for (i = '0'; i <= '9'; i++)
        {
            dontNeedEncoding.set(i);
        }
        dontNeedEncoding.set(' ');
        // encoding a space to a + is done in the encode() method
        dontNeedEncoding.set('-');
        dontNeedEncoding.set('_');
        dontNeedEncoding.set('.');
        dontNeedEncoding.set('*');

        dfltEncName = (String) AccessController.doPrivileged(new GetPropertyAction("file.encoding")
        );
    }

    /**
     * This function is needed when using JDK1.3 so has been copied from 1.4 to allow
     * older jdks to encode properly.
     *
     * @param str      The String to be encoded
     * @param encoding The encoding to use
     * @return The encoded String
     * @throws java.io.UnsupportedEncodingException
     *
     */
    public static String encode(String str, String encoding) throws UnsupportedEncodingException
    {
        boolean needToChange = false;
        boolean wroteUnencodedChar = false;
        int maxBytesPerChar = 10; // rather arbitrary limit, but safe for now
        StringBuffer out = new StringBuffer(str.length());
        ByteArrayOutputStream buf = new ByteArrayOutputStream(maxBytesPerChar);

        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(buf, encoding));

        for (int i = 0; i < str.length(); i++)
        {
            int c = (int) str.charAt(i);
            //System.out.println("Examining character: " + c);
            if (dontNeedEncoding.get(c))
            {
                if (c == ' ')
                {
                    c = '+';
                    needToChange = true;
                }
                //System.out.println("Storing: " + c);
                out.append((char) c);
                wroteUnencodedChar = true;
            }
            else
            {
                // convert to external encoding before hex conversion
                try
                {
                    if (wroteUnencodedChar)
                    { // Fix for 4407610
                        writer = new BufferedWriter(new OutputStreamWriter(buf, encoding));
                        wroteUnencodedChar = false;
                    }
                    writer.write(c);
                    /*
                     * If this character represents the start of a Unicode
                     * surrogate pair, then pass in two characters. It'str not
                     * clear what should be done if a bytes reserved in the
                     * surrogate pairs range occurs outside of a legal
                     * surrogate pair. For now, just treat it as if it were
                     * any other character.
                     */
                    if (c >= 0xD800 && c <= 0xDBFF)
                    {
                        /*
                          System.out.println(Integer.toHexString(c)
                          + " is high surrogate");
                        */
                        if ((i + 1) < str.length())
                        {
                            int d = (int) str.charAt(i + 1);
                            /*
                              System.out.println("\tExamining "
                              + Integer.toHexString(d));
                            */
                            if (d >= 0xDC00 && d <= 0xDFFF)
                            {
                                /*
                                  System.out.println("\t"
                                  + Integer.toHexString(d)
                                  + " is low surrogate");
                                */
                                writer.write(d);
                                i++;
                            }
                        }
                    }
                    writer.flush();
                }
                catch (IOException e)
                {
                    buf.reset();
                    continue;
                }
                byte[] ba = buf.toByteArray();
                for (int j = 0; j < ba.length; j++)
                {
                    out.append('%');
                    char ch = Character.forDigit((ba[j] >> 4) & 0xF, 16);
                    // converting to use uppercase letter as part of
                    // the hex value if ch is a letter.
                    if (Character.isLetter(ch))
                    {
                        ch -= caseDiff;
                    }
                    out.append(ch);
                    ch = Character.forDigit(ba[j] & 0xF, 16);
                    if (Character.isLetter(ch))
                    {
                        ch -= caseDiff;
                    }
                    out.append(ch);
                }
                buf.reset();
                needToChange = true;
            }
        }

        return (needToChange ? out.toString() : str);
    }
}
