package com.atlassian.velocity;

import com.atlassian.core.util.map.EasyMap;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.junit.Test;

import java.io.Writer;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestDefaultVelocityManager
{
    @Test
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
        assertTrue(result.contains("&lt;script&gt;"));
        assertFalse(result.contains("<script>"));
    }
}
