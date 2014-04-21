package com.atlassian.jira.util.system.check;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import com.atlassian.jira.local.ListeningTestCase;

/**
 * @since v4.0
 */
public class TestJRA18659Check extends ListeningTestCase
{
    private JRA18659Check check;

    @Before
    public void setUp() throws Exception
    {
        check = new JRA18659Check();
    }

    @Test
    public void testJVM150_18() throws Exception
    {
        System.setProperty("java.vm.version", "1.5.0_18");
        System.setProperty("java.vm.vendor", "Sun Microsystems Inc.");

        final I18nMessage message = check.getWarningMessage();
        assertEquals("admin.warning.jra_18659", message.getKey());
    }

    @Test
    public void testJVM150_18_2() throws Exception
    {
        System.setProperty("java.vm.version", "1.5.0_18-b02");
        System.setProperty("java.vm.vendor", "Sun Microsystems Inc.");

        final I18nMessage message = check.getWarningMessage();
        assertEquals("admin.warning.jra_18659", message.getKey());
    }

    @Test
    public void testJVM150_15() throws Exception
    {
        System.setProperty("java.vm.version", "1.5.0_15");
        System.setProperty("java.vm.vendor", "Sun Microsystems Inc.");

        final I18nMessage message = check.getWarningMessage();
        assertEquals("admin.warning.jra_18659", message.getKey());
    }

    @Test
    public void testJVM150_15_2() throws Exception
    {
        System.setProperty("java.vm.version", "1.5.0_15-b02");
        System.setProperty("java.vm.vendor", "Sun Microsystems Inc.");

        final I18nMessage message = check.getWarningMessage();
        assertEquals("admin.warning.jra_18659", message.getKey());
    }

    @Test
    public void testJVM150_18_3() throws Exception
    {
        System.setProperty("java.vm.version", "1.5.0_18-b03");
        System.setProperty("java.vm.vendor", "Sun Microsystems Inc.");

        final I18nMessage message = check.getWarningMessage();
        assertNull(message);
    }

    @Test
    public void testJVM150_19() throws Exception
    {
        System.setProperty("java.vm.version", "1.5.0_19");
        System.setProperty("java.vm.vendor", "Sun Microsystems Inc.");

        final I18nMessage message = check.getWarningMessage();
        assertNull(message);
    }
    
    @Test
    public void testJVM160_5() throws Exception
    {
        System.setProperty("java.vm.version", "1.6.0_5");
        System.setProperty("java.vm.vendor", "Sun Microsystems Inc.");

        final I18nMessage message = check.getWarningMessage();
        assertNull(message);
    }
}
