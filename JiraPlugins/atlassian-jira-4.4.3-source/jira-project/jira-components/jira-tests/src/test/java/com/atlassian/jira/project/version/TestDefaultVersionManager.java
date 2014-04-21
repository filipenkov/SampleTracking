package com.atlassian.jira.project.version;

import com.atlassian.jira.local.ListeningTestCase;
import com.atlassian.jira.mock.project.MockVersion;
import org.junit.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Quick test for {@link DefaultVersionManager}. The legacy and slower tests are available in
 * {@link TestDefaultVersionManagerLegacy}
 *
 * @since v4.4
 */
public class TestDefaultVersionManager extends ListeningTestCase
{
    @Test
    public void testIsVersionOverDue() throws Exception
    {
        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.add(Calendar.MONTH, -1);
        Date releaseDate = calendar.getTime();

        DefaultVersionManager manager = new DefaultVersionManager(null, null, null, null, null, null);
        MockVersion version = new MockVersion(574438, "My Test Version");

        assertFalse("Version without released date should not be overdue.", manager.isVersionOverDue(version));

        version.setReleaseDate(releaseDate);
        assertTrue("Version should be overdue.", manager.isVersionOverDue(version));

        version.setReleased(true);
        assertFalse("Released version should never be overdue.", manager.isVersionOverDue(version));

        version.setReleased(false);
        version.setArchived(true);
        assertFalse("Archieved version should never be overdue.", manager.isVersionOverDue(version));

        version = new MockVersion(28829292, "My new Version");

        calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 0);

        version.setReleaseDate(calendar.getTime());
        assertFalse("Version due midnight today should not be overdue.", manager.isVersionOverDue(version));
        calendar.add(Calendar.SECOND, 1);
        version.setReleaseDate(calendar.getTime());
        assertFalse("Version due today should not be overdue.", manager.isVersionOverDue(version));
        calendar.add(Calendar.SECOND, -2);
        version.setReleaseDate(calendar.getTime());
        assertTrue("Version due yesterday should be overdue.", manager.isVersionOverDue(version));
    }
}
