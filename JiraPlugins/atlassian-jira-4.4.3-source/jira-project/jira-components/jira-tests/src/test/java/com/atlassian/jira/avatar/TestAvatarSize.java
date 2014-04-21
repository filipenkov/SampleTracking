package com.atlassian.jira.avatar;

import com.atlassian.jira.local.ListeningTestCase;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit test for {@link com.atlassian.jira.avatar.AvatarSize}.
 *
 * @since v4.0
 */
public class TestAvatarSize extends ListeningTestCase
{
    @Test
    public void testGetters() {
        AvatarSize s = new AvatarSize(1234, "boing");
        assertEquals(1234, s.getPixels());
        assertEquals("boing", s.getFilenameFlag());
        final Selection expected = new Selection(0, 0, 1234, 1234);
        assertEquals(expected.getBottomRightX(), s.originSelection().getBottomRightX());
        assertEquals(expected.getBottomRightY(), s.originSelection().getBottomRightY());
        assertEquals(expected.getTopLeftX(), s.originSelection().getTopLeftX());
        assertEquals(expected.getTopLeftY(), s.originSelection().getTopLeftY());
        assertEquals(expected.getHeight(), s.originSelection().getHeight());
        assertEquals(expected.getWidth(), s.originSelection().getWidth());

    }
}
