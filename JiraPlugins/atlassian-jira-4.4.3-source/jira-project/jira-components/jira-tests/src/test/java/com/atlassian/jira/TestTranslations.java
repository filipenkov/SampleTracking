package com.atlassian.jira;

import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.local.ListeningTestCase;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;

/**
 * This is an odd test which runs through the message bundles translations properties files checking if they meet certain criteria that translations
 * should.
 *
 * @since v3.13
 */
public class TestTranslations extends ListeningTestCase
{
    /**
     * Default root dir when none set with system property {@link #ROOT_DIR_SYSTEM_PROPERTY_KEY}.
     */
    private static final String DEFAULT_ROOT_DIR = TestTranslations.class.getResource("/com/atlassian/jira/web/action").getFile();

    /**
     * Property key for setting the root directory to find translations files in.
     */
    private static final String ROOT_DIR_SYSTEM_PROPERTY_KEY = "jira.translations.checker.root";

    private static final Logger log = Logger.getLogger(TestTranslations.class);

    /**
     * Tests that the Translations are all Hunky Dory.
     *
     * @throws IOException if there is a problem opening/reading/closing properties files under the root dir.
     */
    @Test
    public void testRunTranslationsChecker() throws IOException
    {
        String rootDirname = System.getProperty(ROOT_DIR_SYSTEM_PROPERTY_KEY);
        if (rootDirname == null) {
            rootDirname = DEFAULT_ROOT_DIR;
            log.info("Defaulting to root dir '" + DEFAULT_ROOT_DIR
                     + "' choose the root (relative or absolute) with System Property "
                     + ROOT_DIR_SYSTEM_PROPERTY_KEY);
        }


        TranslationsChecker tc = new TranslationsChecker();
        FileFinder finder = new FileFinder(tc);

        finder.checkDir(new File(rootDirname));
        if (!tc.success())
        {
            fail("There's trouble brewing in the translations files: \n" + tc.getProblemsDescription());
        }
    }

}
