package com.atlassian.modzdetector;

import junit.framework.TestCase;

import java.io.ByteArrayInputStream;

/**
 * Unit test for the {@link com.atlassian.modzdetector.MD5HashAlgorithm}
 *
 * @since 0.8
 */
public class TestMd5HashAlgorithm extends TestCase
{
    public void testSimple()
    {
        MD5HashAlgorithm algo = new MD5HashAlgorithm();
        ByteArrayInputStream bais = new ByteArrayInputStream(new byte[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13 });
        final String hash = algo.getHash(bais);
        assertEquals("aa541e601b7b9ddd0504d19866350d4e", hash); // obtained independently via md5 command from openssl
    }

}
