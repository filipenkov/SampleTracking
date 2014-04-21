package com.atlassian.applinks.core.property;

import com.atlassian.sal.api.pluginsettings.PluginSettings;
import junit.framework.TestCase;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 *
 * @since v3.3
 */
public class HashingLongPropertyKeysPluginSettingsTest extends TestCase
{
    private PluginSettings pluginSettings;
    private HashingLongPropertyKeysPluginSettings hashingPropertySettings;

    @Override
    protected void setUp() throws Exception
    {
        pluginSettings = mock(PluginSettings.class);
        hashingPropertySettings = new HashingLongPropertyKeysPluginSettings(pluginSettings);
    }

    @Test
    public void testGetKeyLessThan100Characters() throws Exception
    {
        String key = StringUtils.repeat("a", 100);
        String value = "Test";
        when(pluginSettings.get(key)).thenReturn(value);
        assertEquals(value, hashingPropertySettings.get(key));
        verify(pluginSettings, never()).remove(key);
        verify(pluginSettings, never()).put(hashedKey(key), value);
    }

    

    @Test
    public void testGetKeyMoreThan100Characters() throws Exception
    {
        String key = StringUtils.repeat("a", 101);
        String value = "Test";
        when(pluginSettings.get(key)).thenReturn(value);
        when(pluginSettings.get(hashedKey(key))).thenReturn(value);
        assertEquals(value, hashingPropertySettings.get(key));
        verify(pluginSettings).remove(key);
        verify(pluginSettings).put(hashedKey(key), value);
    }

    @Test
    public void testGetKeyMoreThan100CharactersNoMigration() throws Exception
    {
        String key = StringUtils.repeat("a", 101);
        String value = "Test";
        when(pluginSettings.get(key)).thenReturn(null);
        when(pluginSettings.get(hashedKey(key))).thenReturn(value);
        assertEquals(value, hashingPropertySettings.get(key));
        verify(pluginSettings, never()).remove(key);
        verify(pluginSettings, never()).put(hashedKey(key), value);
    }

    @Test
    public void testPutKeyMoreThan100Characters() throws Exception
    {
        String key = StringUtils.repeat("a", 101);
        String oldValue = "Test";
        final String newValue = new String("New");
        when(pluginSettings.get(key)).thenReturn(oldValue);
        when(pluginSettings.put(hashedKey(key), newValue)).thenReturn(oldValue);
        assertEquals(oldValue, hashingPropertySettings.put(key, newValue));
        verify(pluginSettings).get(key);
        verify(pluginSettings).remove(key);
        verify(pluginSettings).put(hashedKey(key), oldValue);
    }

    @Test
    public void testPutKeyMoreThan100CharactersNoMigration() throws Exception
    {
        String key = StringUtils.repeat("a", 101);
        String oldValue = "Test";
        String newValue = "New";
        when(pluginSettings.get(key)).thenReturn(null);
        when(pluginSettings.put(hashedKey(key), newValue)).thenReturn(oldValue);
        assertEquals(oldValue, hashingPropertySettings.put(key, newValue));
        verify(pluginSettings).get(key);
        verify(pluginSettings, never()).remove(key);
        verify(pluginSettings, never()).put(hashedKey(key), oldValue);
    }

    @Test
    public void testPutKeyLessThan100Characters() throws Exception
    {
        String key = StringUtils.repeat("a", 100);
        String value = "Test";
        when(pluginSettings.put(key, value)).thenReturn(null);
        assertEquals(null, hashingPropertySettings.put(key, value));
        verify(pluginSettings, times(1)).put(key, value);
        verify(pluginSettings, never()).remove(key);
        verify(pluginSettings, never()).put(hashedKey(key), value);
    }

     @Test
    public void testRemoveKeyMoreThan100Characters() throws Exception
    {
        String key = StringUtils.repeat("a", 101);
        String oldValue = "Test";
        when(pluginSettings.get(key)).thenReturn(oldValue);
        when(pluginSettings.remove(hashedKey(key))).thenReturn(oldValue);
        assertEquals(oldValue, hashingPropertySettings.remove(key));
        verify(pluginSettings).get(key);
        verify(pluginSettings).remove(key);
        verify(pluginSettings).put(hashedKey(key), oldValue);
    }

    @Test
    public void testRemoveKeyMoreThan100CharactersNoMigration() throws Exception
    {
        String key = StringUtils.repeat("a", 101);
        String oldValue = "Test";
        when(pluginSettings.get(key)).thenReturn(null);
        when(pluginSettings.remove(hashedKey(key))).thenReturn(oldValue);
        assertEquals(oldValue, hashingPropertySettings.remove(key));
        verify(pluginSettings).get(key);
        verify(pluginSettings, never()).remove(key);
        verify(pluginSettings, never()).put(hashedKey(key), oldValue);
    }

    @Test
    public void testRemoveKeyLessThan100Characters() throws Exception
    {
        String key = StringUtils.repeat("a", 100);
        String value = "Test";
        when(pluginSettings.get(key)).thenReturn(null);
        assertEquals(null, hashingPropertySettings.remove(key));
        verify(pluginSettings, times(1)).remove(key);
        verify(pluginSettings, never()).get(key);
        verify(pluginSettings, never()).put(hashedKey(key), value);
    }

    @Test
    public void testAssertDifferentHashes1() throws Exception
    {
        StringBuffer keySB = new StringBuffer();
        keySB.append(StringUtils.repeat("applinks30", 11));
        String key = keySB.toString();
        when(pluginSettings.get(key)).thenReturn(null);
        assertEquals(null, hashingPropertySettings.get(key));
        verify(pluginSettings, never()).remove(key);
        verify(pluginSettings, times(1)).get("applinks30applinks30applinks30applinks30applinks30applinks30applinks3ec9e11270f5fdcc35f2a33943f14710");
        verify(pluginSettings, never()).put("applinks30applinks30applinks30applinks30applinks30applinks30applinks3ec9e11270f5fdcc35f2a33943f14710", null);
    }

    @Test
    public void testAssertDifferentHashes2() throws Exception
    {
        StringBuffer keySB = new StringBuffer();
        keySB.append(StringUtils.repeat("u", 33));
        keySB.append(StringUtils.repeat("a", 33));
        keySB.append(StringUtils.repeat("l", 35));

        String key = keySB.toString();
        when(pluginSettings.get(key)).thenReturn(null);
        assertEquals(null, hashingPropertySettings.get(key));
        verify(pluginSettings, never()).remove(key);
        verify(pluginSettings, times(1)).get("uuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaallebe34760ee1633c7d3528b15ed6da67c");
        verify(pluginSettings,  never()).put("uuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaallebe34760ee1633c7d3528b15ed6da67c", null);
    }

    @Test
    public void testAssertDifferentHashes3() throws Exception
    {
        StringBuffer keySB = new StringBuffer();
        keySB.append(StringUtils.repeat("JIRA", 30));

        String key = keySB.toString();
        when(pluginSettings.get(key)).thenReturn(null);
        assertEquals(null, hashingPropertySettings.get(key));
        verify(pluginSettings, never()).remove(key);
        verify(pluginSettings, times(1)).get("JIRAJIRAJIRAJIRAJIRAJIRAJIRAJIRAJIRAJIRAJIRAJIRAJIRAJIRAJIRAJIRAJIRAd0d1f1f8d0c83f3f998a2a8e7487a805");
        verify(pluginSettings, never()).put("JIRAJIRAJIRAJIRAJIRAJIRAJIRAJIRAJIRAJIRAJIRAJIRAJIRAJIRAJIRAJIRAJIRAd0d1f1f8d0c83f3f998a2a8e7487a805", null);
    }

    /**
     * Confirm that {@link DigestUtils#md5Hex(String)} uses utf-8 implicitly.
     */
    @Test
    public void hashUsesUtf8Encoding() throws Exception
    {
        String key = StringUtils.repeat("\u00A3", 100);
        hashingPropertySettings.put(key, "");
        
        String expectedHashed = StringUtils.repeat("\u00A3", 80) + "e2bb2f9dd43f75508e5aab61b76c0b4c";
        verify(pluginSettings).put(expectedHashed, "");
    }
    
    private static String hashedKey(String key)
    {
        final String keyHash = DigestUtils.md5Hex(key);
        final String keptOriginalKey = key.substring(0, 100 - keyHash.length());
        final String hashedKey = keptOriginalKey + keyHash;
        return hashedKey;
    }
}
