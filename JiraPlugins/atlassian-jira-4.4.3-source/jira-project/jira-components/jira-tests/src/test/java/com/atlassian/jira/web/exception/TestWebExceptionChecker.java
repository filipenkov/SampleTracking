package com.atlassian.jira.web.exception;

import com.atlassian.jira.local.ListeningTestCase;
import org.junit.Test;
import static org.junit.Assert.*;

import java.net.SocketException;

@SuppressWarnings ({ "ThrowableInstanceNeverThrown" })
public class TestWebExceptionChecker extends ListeningTestCase
{
    @Test
    public void testCanBeSafelyIgnored() throws Exception
    {

        SocketException se1 = new SocketException("Connection reset");
        SocketException se2 = new SocketException("Connection okey dokey");
        SocketException se3 = new SocketException("Broken pipe");

        assertTrue(WebExceptionChecker.canBeSafelyIgnored(se1));
        assertFalse(WebExceptionChecker.canBeSafelyIgnored(se2));
        assertTrue(WebExceptionChecker.canBeSafelyIgnored(se3));
    }
}
