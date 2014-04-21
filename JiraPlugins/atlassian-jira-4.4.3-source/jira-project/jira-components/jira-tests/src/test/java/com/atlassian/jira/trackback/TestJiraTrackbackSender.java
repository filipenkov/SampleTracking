package com.atlassian.jira.trackback;

import com.atlassian.jira.local.ListeningTestCase;
import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.trackback.DefaultTrackbackSender;

import java.util.Map;

/**
 * @since v3.13
 */
public class TestJiraTrackbackSender extends ListeningTestCase
{
    private static final String ENCODING_BB_123 = "BB-123";
    private static final String CONTENT_TYPE = "Content-Type";

    @Test
    public void testHttpHeadersHasJiraEncoding() throws Exception
    {
        DefaultTrackbackSender sender = new JiraTrackbackSender(null) {
            String getJiraEncoding()
            {
                return ENCODING_BB_123;
            }
        };

        Map requestHeaderMap = sender.buildHttpHeaders("/somewhere/trackback", null);
        assertNotNull(requestHeaderMap);
        assertTrue(requestHeaderMap.containsKey(CONTENT_TYPE));
        final String expectedEncoding = "application/x-www-form-urlencoded; charset=" + ENCODING_BB_123;
        assertEquals(expectedEncoding,requestHeaderMap.get(CONTENT_TYPE));
    }
}
