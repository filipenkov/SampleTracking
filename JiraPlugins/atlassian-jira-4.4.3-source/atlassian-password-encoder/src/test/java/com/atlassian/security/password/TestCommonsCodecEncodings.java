package com.atlassian.security.password;

import junit.framework.TestCase;
import org.apache.commons.codec.binary.Base64;

import java.util.Arrays;

/**
 * Tests some known values with our encoding libraries to make sure future upgrades don't change formats.
 * We're trying to pick up problems like this: https://issues.apache.org/jira/browse/CODEC-89
 */
public final class TestCommonsCodecEncodings extends TestCase
{
    private static final byte[] SHORT_DATA = new byte[] { 0, 1, 2, 3, 4 };
    private static final byte[] SHORT_DATA_BASE64 = { 65, 65, 69, 67, 65, 119, 81, 61 };
    private static final String SHORT_DATA_BASE64_UTF8 = "AAECAwQ=";

    // some Japanese text from here: http://ja.wikipedia.org/wiki/Australia, converted with native2ascii
    private static final byte[] LONG_DATA = StringUtils.getBytesUtf8(
        "\u65e5\u672c\u8a9e\u306e\u8868\u8a18\u306f\u3001\u30aa\u30fc\u30b9\u30c8\u30e9\u30ea\u30a2\u3002" +
        "Commonwealth \u306b\u5bfe\u5fdc\u3059\u308b\u8a9e\u3068\u3057\u3066\u300c\u9023\u90a6\u300d\u3092\u4ed8" +
        "\u52a0\u3057\u3001\u30aa\u30fc\u30b9\u30c8\u30e9\u30ea\u30a2\u9023\u90a6\u3068\u3055\u308c\u308b\u4e8b" +
        "\u3082\u3042\u308b\u304c\u3001\u3053\u306e\u5834\u5408\u306e Commonwealth \u306f\u300c\u9023\u90a6\u300d" +
        "\u3068\u3044\u3046\u610f\u5473\u3067\u306f\u306a\u3044\u306e\u3067\u3001\u3053\u308c\u3092\u6b63\u5f0f" +
        "\u540d\u79f0\u8a33\u3068\u3059\u308b\u306e\u306f\u4e0d\u9069\u5207\u304b\u3082\u77e5\u308c\u306a\u3044" +
        "\u3002");
    private static final byte[] LONG_DATA_BASE64 = { 53, 112, 101, 108, 53, 112, 121, 115, 54, 75, 113, 101, 52, 52, 71, 117, 54, 75,
        71, 111, 54, 75, 105, 89, 52, 52, 71, 118, 52, 52, 67, 66, 52, 52, 75, 113, 52, 52, 79, 56, 52, 52, 75, 53, 52, 52, 79, 73, 52, 52, 79,
        112, 52, 52, 79, 113, 52, 52, 75, 105, 52, 52, 67, 67, 81, 50, 57, 116, 98, 87, 57, 117, 100, 50, 86, 104, 98, 72, 82, 111, 73, 79,
        79, 66, 113, 43, 87, 118, 118, 117, 87, 47, 110, 79, 79, 66, 109, 101, 79, 67, 105, 43, 105, 113, 110, 117, 79, 66, 113, 79, 79, 66,
        108, 43, 79, 66, 112, 117, 79, 65, 106, 79, 109, 65, 111, 43, 109, 67, 112, 117, 79, 65, 106, 101, 79, 67, 107, 117, 83, 55, 109,
        79, 87, 75, 111, 79, 79, 66, 108, 43, 79, 65, 103, 101, 79, 67, 113, 117, 79, 68, 118, 79, 79, 67, 117, 101, 79, 68, 105, 79, 79, 68,
        113, 101, 79, 68, 113, 117, 79, 67, 111, 117, 109, 65, 111, 43, 109, 67, 112, 117, 79, 66, 113, 79, 79, 66, 108, 101, 79, 67, 106,
        79, 79, 67, 105, 43, 83, 54, 105, 43, 79, 67, 103, 117, 79, 66, 103, 117, 79, 67, 105, 43, 79, 66, 106, 79, 79, 65, 103, 101, 79, 66,
        107, 43, 79, 66, 114, 117, 87, 103, 116, 79, 87, 81, 105, 79, 79, 66, 114, 105, 66, 68, 98, 50, 49, 116, 98, 50, 53, 51, 90, 87, 70,
        115, 100, 71, 103, 103, 52, 52, 71, 118, 52, 52, 67, 77, 54, 89, 67, 106, 54, 89, 75, 109, 52, 52, 67, 78, 52, 52, 71, 111, 52, 52,
        71, 69, 52, 52, 71, 71, 53, 111, 83, 80, 53, 90, 71, 122, 52, 52, 71, 110, 52, 52, 71, 118, 52, 52, 71, 113, 52, 52, 71, 69, 52, 52, 71,
        117, 52, 52, 71, 110, 52, 52, 67, 66, 52, 52, 71, 84, 52, 52, 75, 77, 52, 52, 75, 83, 53, 113, 50, 106, 53, 98, 121, 80, 53, 90, 67, 78,
        53, 54, 101, 119, 54, 75, 105, 122, 52, 52, 71, 111, 52, 52, 71, 90, 52, 52, 75, 76, 52, 52, 71, 117, 52, 52, 71, 118, 53, 76, 105, 78,
        54, 89, 71, 112, 53, 89, 105, 72, 52, 52, 71, 76, 52, 52, 75, 67, 53, 53, 43, 108, 52, 52, 75, 77, 52, 52, 71, 113, 52, 52, 71, 69, 52,
        52, 67, 67 };
    private static final String LONG_DATA_BASE64_UTF8 = "5pel5pys6Kqe44Gu6KGo6KiY44Gv44CB44Kq44O844K544OI44Op44Oq44" +
        "Ki44CCQ29tbW9ud2VhbHRoIOOBq+WvvuW/nOOBmeOCi+iqnuOBqOOBl+OB" +
        "puOAjOmAo+mCpuOAjeOCkuS7mOWKoOOBl+OAgeOCquODvOOCueODiOOD" +
        "qeODquOCoumAo+mCpuOBqOOBleOCjOOCi+S6i+OCguOBguOCi+OBjOOAgeO" +
        "Bk+OBruWgtOWQiOOBriBDb21tb253ZWFsdGgg44Gv44CM6YCj6YKm44CN4" +
        "4Go44GE44GG5oSP5ZGz44Gn44Gv44Gq44GE44Gu44Gn44CB44GT44KM44K" +
        "S5q2j5byP5ZCN56ew6Kiz44Go44GZ44KL44Gu44Gv5LiN6YGp5YiH44GL44KC" +
        "55+l44KM44Gq44GE44CC";

    public void testEncodeBase64Short() throws Exception
    {
        byte[] encodedBytes = Base64.encodeBase64(SHORT_DATA);
        assertEquals(SHORT_DATA_BASE64, encodedBytes);
    }

    public void testEncodeBase64Long() throws Exception
    {
        byte[] encodedBytes = Base64.encodeBase64(LONG_DATA);
        assertEquals(LONG_DATA_BASE64, encodedBytes);
    }

    public void testNewStringUTF8Short() throws Exception
    {
        byte[] encodedBytes = Base64.encodeBase64(SHORT_DATA);
        String encodedString = StringUtils.newStringUtf8(encodedBytes);
        assertEquals(SHORT_DATA_BASE64_UTF8, encodedString);
    }

    public void testNewStringUTF8Long() throws Exception
    {
        byte[] encodedBytes = Base64.encodeBase64(LONG_DATA);
        String encodedString = StringUtils.newStringUtf8(encodedBytes);
        assertEquals(LONG_DATA_BASE64_UTF8, encodedString);
    }

    public void testGetBytesUTF8Short() throws Exception
    {
        assertEquals(new byte[]{ (byte) 0xE6, (byte) 0x97, (byte) 0xA5, (byte) 0xE6, (byte) 0x9C, (byte) 0xAC,  },
            StringUtils.getBytesUtf8("\u65e5\u672c"));
    }

    private static void assertEquals(byte[] expected, byte[] actual)
    {
        assertTrue("expected: " + Arrays.toString(expected) + ", but was: " + Arrays.toString(actual),
            Arrays.equals(expected, actual));
    }
}
