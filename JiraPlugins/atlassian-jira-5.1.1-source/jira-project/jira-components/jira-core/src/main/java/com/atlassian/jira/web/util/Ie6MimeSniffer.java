package com.atlassian.jira.web.util;

import java.io.UnsupportedEncodingException;

/**
 * Emulates Internet Explorer MIME type sniffing behaviour to predict the MIME override that IE will do to files.
 * Of interest are those files which will be detected as HTML and then loaded as such (regardless of MIME type
 * headers sent by the server) resulting in a potential XSS attack vector in the case of files originating from
 * other end users (like attachments). See http://jira.atlassian.com/browse/JRA-10862
 * <p>
 * Note that this implementation is based on apparent IE behaviour and research but as there is no spec,
 * is an approximation only. Strict security measures should not rely on this emulation since IE will reliably
 * force users to decide when files are accompanied with a "Content-Disposition" header of "attachment".
 *
 * @since v3.13
 */
public class Ie6MimeSniffer
{
    private final int maximumBytesToCheck;

    /**
     * IE only sniffs the first 256 bytes
     */
    public static final int MAX_BYTES_TO_SNIFF = 256;

    /**
     * According to Anton's hazy recollection, IE uses ASCII first then UTF-8 but it doesn't matter
     * for this since they encode smelly bytes the same.
     */
    private static final String IE_ENCODING = "UTF-8";

    /**
     * Using a testing harness, the following tags were found to cause the content type to be overridden.
     */
    static final byte[][] SMELLY_BYTES = encodeBytes(
        new String[] { "<html", "<head", "<body", "<script", "<table", "<img", "<plaintext", "<pre", "<title" }, IE_ENCODING);

    private static final byte ASCII_UPPERCASE_Z = 0x5a;
    private static final byte ASCII_UPPERCASE_A = 0x41;
    private static final byte ASCII_LOWERCASE_A = 0x61;

    /** Upper case group has a lower value than the lower case group and the offset alows a lowercase translation. */
    private static final int CASE_GROUP_OFFSET = ASCII_LOWERCASE_A - ASCII_UPPERCASE_A;

    /**
     * Creates a MIME sniffer which replicates behaviour of Internet Explorer 6 and above. <em>Note that this
     * constructor will cause the sniffer to check the first MAX_BYTES_TO_SNIFF bytes only which seems to be what IE
     * does. </em>
     */
    public Ie6MimeSniffer()
    {
        this(MAX_BYTES_TO_SNIFF);
    }

    /**
     * Creates a MIME sniffer which replicates behaviour of Internet Explorer 6 and above. Note that specifying all
     * bytes as a maximum to check will temporarily require a duplicate of the bytes in memory when performing the
     * check.
     * @param maximumBytesToCheck the number of bytes to sniff use -1 to indicate all bytes.
     */
    public Ie6MimeSniffer(final int maximumBytesToCheck)
    {
        if (maximumBytesToCheck < -1)
        {
            throw new IllegalArgumentException("looking for -1 or above for max bytes to check.");
        }
        this.maximumBytesToCheck = maximumBytesToCheck;
    }

    /**
     * Returns true if any known version of Internet Explorer will, when given a file which begins with the given
     * bytes, detect an HTML mime type based on its contents. Note that later versions are smarter and will not
     * detect certain files as being html but in IE 7 this appears to depend on the given file extension and so cannot
     * be trusted.
     *
     * @param fileContents the bytes of the file to sniff, only the configured number of bytes is sniffed.
     * @return true only if any of the configured versions of IE would sniff the MIME type as text/html.
     */
    public boolean smellsLikeHtml(final byte[] fileContents)
    {
        int length = fileContents.length;
        if ((fileContents.length > maximumBytesToCheck) && (maximumBytesToCheck != -1))
        {
            length = maximumBytesToCheck;
        }
        final byte[] copy = new byte[length];
        System.arraycopy(fileContents, 0, copy, 0, length);
        toLowerCaseAscii(copy);
        for (int i = 0; i < SMELLY_BYTES.length; i++)
        {
            if (containsSubarray(copy, SMELLY_BYTES[i]))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Destructively shifts all english ASCII alphabetic character bytes to lowercase.
     *
     * @param lowerCaseMe the bytes to be lowercased.
     */
    static void toLowerCaseAscii(final byte[] lowerCaseMe)
    {
        for (int i = 0; i < lowerCaseMe.length; i++)
        {
            if ((lowerCaseMe[i] <= ASCII_UPPERCASE_Z) && (lowerCaseMe[i] >= ASCII_UPPERCASE_A))
            {
                lowerCaseMe[i] = (byte) (lowerCaseMe[i] + CASE_GROUP_OFFSET);
            }
        }
    }

    /**
     * Returns true only if the subArray is wholly contained in the superArray.
     * Candidate for ripping out into a utility class.
     * @param superArray the array to search.
     * @param subArray the array you're looking for.
     * @return true only if subArray was found within superArray.
     */
    static boolean containsSubarray(final byte[] superArray, final byte[] subArray)
    {
        if (superArray.length < subArray.length)
        {
            return false;
        }
        final int lastMatchableSuperIndex = superArray.length - subArray.length;
        for (int i = 0; i <= lastMatchableSuperIndex; i++)
        {
            // candidate match within superArray is the range from i to i+subArray.length-1
            for (int j = 0; j < subArray.length; j++)
            {
                if (superArray[i + j] != subArray[j])
                {
                    // the subarray has failed to match its position j with super at i+j
                    break;
                }
                if (j == subArray.length - 1)
                {
                    // we have tested the last subarray element without a mismatch
                    return true;
                }
            }
        }
        // we never reached the last subarray element
        return false;
    }

    /**
     * Helper to have ready byte arrays that correspond to the given tag strings.
     *
     * @param smellyTags strings that smell.
     * @param encoding   the encoding to use.
     * @return the bytes of each given String in the given encoding.
     */
    private static byte[][] encodeBytes(final String[] smellyTags, final String encoding)
    {
        try
        {
            final byte[][] smellys = new byte[smellyTags.length][];
            for (int i = 0; i < smellyTags.length; i++)
            {
                smellys[i] = smellyTags[i].getBytes(encoding);
            }
            return smellys;
        }
        catch (final UnsupportedEncodingException e)
        {
            throw new Error(IE_ENCODING + " is required", e);
        }
    }
}
