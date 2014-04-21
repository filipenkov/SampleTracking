package com.atlassian.security.password;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

/**
 * Tests the implementation returned by {@link DefaultPasswordEncoder#getDefaultInstance()}.
 */
public final class TestDefaultPasswordEncoderWithPKCS5S2 extends TestCase
{
    private static final PasswordEncoder ENCODER = new TimingPasswordEncoder(DefaultPasswordEncoder.getDefaultInstance());

    private static final List<String> PASSWORDS = asList(
        "secret",
        "d76#$;<_@",
        "\u263a\u2600",

        // some Japanese text from here: http://ja.wikipedia.org/wiki/Australia, converted with native2ascii
        "\u65e5\u672c\u8a9e\u306e\u8868\u8a18\u306f\u3001\u30aa\u30fc\u30b9\u30c8\u30e9\u30ea\u30a2\u3002" +
            "Commonwealth \u306b\u5bfe\u5fdc\u3059\u308b\u8a9e\u3068\u3057\u3066\u300c\u9023\u90a6\u300d\u3092\u4ed8" +
            "\u52a0\u3057\u3001\u30aa\u30fc\u30b9\u30c8\u30e9\u30ea\u30a2\u9023\u90a6\u3068\u3055\u308c\u308b\u4e8b" +
            "\u3082\u3042\u308b\u304c\u3001\u3053\u306e\u5834\u5408\u306e Commonwealth \u306f\u300c\u9023\u90a6\u300d" +
            "\u3068\u3044\u3046\u610f\u5473\u3067\u306f\u306a\u3044\u306e\u3067\u3001\u3053\u308c\u3092\u6b63\u5f0f" +
            "\u540d\u79f0\u8a33\u3068\u3059\u308b\u306e\u306f\u4e0d\u9069\u5207\u304b\u3082\u77e5\u308c\u306a\u3044" +
            "\u3002"
    );
    private static final List<String> PREVIOUSLY_ENCODED = asList(
        "{PKCS5S2}TzwIctDE2RJIQfT+O473W5+qhSAyof1Kz+hfe2kvAcnDe2uVzdp/ymw5V7mHH+Zv",
        "{PKCS5S2}tCXb6Rh25PwM1f12uVF4sPO3kxPtgCW5U0s/Hbxnyh8o6xCQfvWcjttAMtuu1ubp",
        "{PKCS5S2}5Gu3Qq4GW8RnBpIR6b1Vn6V8ihb/QlawC7kJXQxbr2+HgXA468K0F0hHqFchh0py",
        "{PKCS5S2}VekFbW/Bwc9PJg4ptYfZtIzb1K8QI6llgxSy04vRRypp0kICI/92KG8viKLyWywT"
    );

    private static final List<String> ENCODED = new ArrayList<String>(PASSWORDS.size());

    static
    {
        // encode passwords just once per build, otherwise the random salt changes
        for (String password : PASSWORDS)
        {
            ENCODED.add(ENCODER.encodePassword(password));
        }
    }

    public void testStartsWithKnownPrefix() throws Exception
    {
        for (String encoded : ENCODED)
        {
            assertTrue("encoded password should start with prefix: " + encoded, encoded.startsWith("{PKCS5S2}"));
        }
    }

    public void testCanDecodeEncodedPassword() throws Exception
    {
        for (String encoded : ENCODED)
        {
            assertTrue("canDecodePassword() should return true for: " + encoded, ENCODER.canDecodePassword(encoded));
        }
    }

    public void testKnownPasswordIsValid() throws Exception
    {
        for (int i=0; i<PASSWORDS.size(); i++)
        {
            String password = PASSWORDS.get(i);
            String encoded = ENCODED.get(i);
            assertTrue("password should be valid: " + password, ENCODER.isValidPassword(password, encoded));
        }
    }

    public void testPreviouslyEncodedPasswordIsValid() throws Exception
    {
        for (int i=0; i<PASSWORDS.size(); i++)
        {
            String password = PASSWORDS.get(i);
            String encoded = PREVIOUSLY_ENCODED.get(i);
            assertTrue("previously encoded value for password '" + password + "' is not valid: " + encoded, ENCODER.isValidPassword(password, encoded));
        }
    }

    public void testNewlyEncodedPasswordIsNotSameAsPreviouslyEncoded() throws Exception
    {
        // this basically tests that the salt is random
        for (int i=0; i<PASSWORDS.size(); i++)
        {
            String password = PASSWORDS.get(i);
            String previouslyEncoded = PREVIOUSLY_ENCODED.get(i);
            String newlyEncoded = ENCODER.encodePassword(password);
            assertFalse("newly encoded value for password '" + password + "' should not equal previously encoded value: " + previouslyEncoded,
                previouslyEncoded.equals(newlyEncoded));
        }
    }

    public void testIncorrectPasswordIsNotValid() throws Exception
    {
        for (int i=0; i<PASSWORDS.size(); i++)
        {
            String password = PASSWORDS.get(i) + "1";  // append some garbage
            String encoded = ENCODED.get(i);
            assertFalse("invalid password should not be valid: " + password, ENCODER.isValidPassword(password, encoded));
        }
    }

    public void testPasswordWithExtraWhitespaceIsNotValid() throws Exception
    {
        for (int i=0; i<PASSWORDS.size(); i++)
        {
            String password = " " + PASSWORDS.get(i); // prepend some whitespace
            String encoded = ENCODED.get(i);
            assertFalse("invalid password should be not valid: " + password, ENCODER.isValidPassword(password, encoded));
        }
    }
}
