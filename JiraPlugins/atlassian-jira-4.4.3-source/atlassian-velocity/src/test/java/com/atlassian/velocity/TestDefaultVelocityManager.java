package com.atlassian.velocity;

import com.atlassian.core.util.map.EasyMap;
import junit.framework.TestCase;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;

import java.io.Writer;

public class TestDefaultVelocityManager extends TestCase
{
    public void testGetBodyReturnsHtmlEscapedError() throws Exception
    {
        final VelocityEngine ve = new VelocityEngine()
        {
            public boolean mergeTemplate(final String s, final Context context, final Writer writer) throws Exception
            {
                throw new Exception("<script>ATTACK</script>");
            }
        };

        DefaultVelocityManager manager = new DefaultVelocityManager()
        {
            protected synchronized VelocityEngine getVe()
            {
                return ve;
            }
        };

        String result = manager.getBody("", "", EasyMap.build());
        assertTrue(result.indexOf("&lt;script&gt;") >= 0);
        assertFalse(result.indexOf("<script>") >= 0);
    }
}
