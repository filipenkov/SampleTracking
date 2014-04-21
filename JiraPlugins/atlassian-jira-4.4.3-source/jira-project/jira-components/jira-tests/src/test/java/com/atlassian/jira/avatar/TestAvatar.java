package com.atlassian.jira.avatar;

import com.atlassian.jira.local.ListeningTestCase;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit test for {@link com.atlassian.jira.avatar.Avatar}.
 *
 * @since v4.0
 */
public class TestAvatar extends ListeningTestCase
{
    @Test
    public void testAvatarType()
    {
        assertNull(Avatar.Type.getByName(null));
    }
}
