/*
 * Created by IntelliJ IDEA.
 * User: mike
 * Date: Jan 23, 2002
 * Time: 6:10:02 PM
 * To change template for new class use
 * Code Style | Class Templates options (Tools | IDE Options).
 */
package com.atlassian.core.util;

public class RandomGenerator
{
    /**
     * Generate a random password (length 6)
     */
    public static String randomPassword()
    {
        return randomString(6);
    }

    /**
     * Generate a random string of characters - including numbers
     */
    public static String randomString(int length)
    {
        return randomString(length, true);
    }

    /**
     * Generate a random string of characters
     */
    public static String randomString(int length, boolean includeNumbers)
    {
        StringBuffer b = new StringBuffer(length);

        for (int i = 0; i < length; i++)
        {
            if (includeNumbers)
                b.append(randomCharacter());
            else
                b.append(randomAlpha());
        }

        return b.toString();
    }

    /**
     * Generate a random alphanumeric character.
     *
     * 3/4rs of the time this will generate a letter, 1/4 a number
     */
    public static char randomCharacter()
    {
        int i = (int) (Math.random() * 3);
        if (i < 2)
            return randomAlpha();
        else
            return randomDigit();
    }

    /**
     * Generate a random character from the alphabet - either a-z or A-Z
     */
    public static char randomAlpha()
    {
        int i = (int) (Math.random() * 52);

        if (i > 25)
            return (char) (97 + i - 26);
        else
            return (char) (65 + i);
    }

    /**
     * Generate a random digit - from 0 - 9
     */
    public static char randomDigit()
    {
        int i = (int) (Math.random() * 10);
        return (char) (48 + i);
    }
}
