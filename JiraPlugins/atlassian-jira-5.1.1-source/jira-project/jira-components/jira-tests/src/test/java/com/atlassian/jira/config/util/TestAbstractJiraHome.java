package com.atlassian.jira.config.util;

import com.atlassian.jira.local.ListeningTestCase;
import com.atlassian.jira.util.NotNull;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;

/**
 * Test for {@link AbstractJiraHome}.
 *
 * @since v4.1
 */
public class TestAbstractJiraHome extends ListeningTestCase
{
    @Test
    public void testGetLogDirectory() throws Exception
    {
        final File homeFile = new File("testFile");
        final FixedHome home = new FixedHome(homeFile);

        assertEquals(home.getLogDirectory(), new File(homeFile, "log"));
    }

    @Test
    public void testGetCachesDirectory() throws Exception
    {
        final File homeFile = new File("testFile");
        final FixedHome home = new FixedHome(homeFile);

        assertEquals(home.getCachesDirectory(), new File(homeFile, "caches"));
    }

    @Test
    public void testGetExportDirectory() throws Exception
    {
        final File homeFile = new File("testFile");
        final FixedHome home = new FixedHome(homeFile);

        assertEquals(home.getExportDirectory(), new File(homeFile, "export"));
    }

    @Test
    public void testGetPluginsDirectory() throws Exception
    {
        final File homeFile = new File("testFile");
        final FixedHome home = new FixedHome(homeFile);

        assertEquals(home.getPluginsDirectory(), new File(homeFile, "plugins"));
    }

    @Test
    public void testGetDataDirectory() throws Exception
    {
        final File homeFile = new File("testFile");
        final FixedHome home = new FixedHome(homeFile);

        assertEquals(home.getDataDirectory(), new File(homeFile, "data"));
    }

    public static class FixedHome extends AbstractJiraHome
    {
        private final File home;

        public FixedHome()
        {
            this(new File(System.getProperty("java.io.tmpdir")));
        }

        public FixedHome(final File home)
        {
            this.home = home;
        }

        @NotNull
        @Override
        public File getHome()
        {
            return home;
        }
    }
}
