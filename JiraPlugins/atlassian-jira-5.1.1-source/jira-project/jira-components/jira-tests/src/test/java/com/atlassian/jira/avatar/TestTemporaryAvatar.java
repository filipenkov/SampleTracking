package com.atlassian.jira.avatar;

import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.local.ListeningTestCase;

import java.io.File;

/**
 * Unit test for {@link com.atlassian.jira.avatar.TemporaryAvatar}.
 *
 * @since v4.0
 */
public class TestTemporaryAvatar extends ListeningTestCase
{
    @Test
    public void testConstructor()
    {

        try
        {
            new TemporaryAvatar(null, null, null, null);
            fail("expected Exception");
        }
        catch (RuntimeException yay)
        {

        }

        try
        {
            new TemporaryAvatar("content/type", null, null, null);
            fail("expected Exception");
        }
        catch (RuntimeException yay)
        {

        }

        try
        {
            new TemporaryAvatar("content/type", "originalFilename", null, null);
            fail("expected Exception");
        }
        catch (RuntimeException yay)
        {

        }

    }

    /** Super basic */
    @Test
    public void testGetters() {
        final File file = new File("/");
        TemporaryAvatar ta = new TemporaryAvatar("content/type", "originalFilename", file, null);
        assertEquals("content/type", ta.getContentType());
        assertEquals("originalFilename", ta.getOriginalFilename());
        assertEquals(file, ta.getFile());
    }

}
