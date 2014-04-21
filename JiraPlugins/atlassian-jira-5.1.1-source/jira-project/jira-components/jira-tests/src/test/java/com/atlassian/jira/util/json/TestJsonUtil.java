package com.atlassian.jira.util.json;

import com.atlassian.jira.local.ListeningTestCase;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.LinkedHashMap;

import static org.junit.Assert.assertEquals;

public class TestJsonUtil extends ListeningTestCase
{
    private static Object newAbc(String a, float b) {
        LinkedHashMap<String, Object> o = new LinkedHashMap<String, Object>();
        o.put("a", a);
        o.put("b", b);
        return o;
    }

    @Test
    public void testToJsonString() {
        assertEquals("null", JsonUtil.toJsonString(null));
        assertEquals("123", JsonUtil.toJsonString(123));
        assertEquals("\"abc\"", JsonUtil.toJsonString("abc"));
        assertEquals("\"\\\"abc\\\"\"", JsonUtil.toJsonString("\"abc\""));
        assertEquals("[]", JsonUtil.toJsonString(new String[] {}));
        assertEquals("[\"a\",\"b\",\"cc\"]", JsonUtil.toJsonString(new String[] {"a", "b", "cc"}));
        assertEquals("[\"a\",\"b\",\"cc\"]", JsonUtil.toJsonString(ImmutableList.of("a", "b", "cc")));
        assertEquals("{\"a\":\"abc\",\"b\":127.0}", JsonUtil.toJsonString(newAbc("abc", 127)));
        assertEquals("\"<\\/script>\"", JsonUtil.toJsonString("</script>"));
        assertEquals("\"<script>\"", JsonUtil.toJsonString("<script>"));
        assertEquals("\"fasd<\\/b>\"", JsonUtil.toJsonString("fasd</b>"));
    }
}
