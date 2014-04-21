package com.atlassian.jira.velocity;

import com.atlassian.jira.local.ListeningTestCase;
import org.junit.Test;
import com.atlassian.jira.FileFinder;

import java.io.File;
import java.io.IOException;

/**
 * Two tests related to velocity macros.
 *
 * @since v4.0
 */
public class TestVelocityMacros extends ListeningTestCase
{

    @Test
    public void testCheckForLocalVelocityMacros() throws Exception
    {
        VelocityTemplateChecker checker = new VelocityTemplateChecker();

        FileFinder finder = new FileFinder(checker);
        finder.checkDir(getDir());
    }

    @Test
    public void testForNameClashes() throws IOException
    {
        VelocityMacrosTemplatenamesChecker checker = new VelocityMacrosTemplatenamesChecker();

        FileFinder finder = new FileFinder(checker);
        finder.checkDir(getDir());
    }

    private File getDir()
    {
        final String classFileName = "/" + this.getClass().getName().replace('.', '/') + ".class"; // fully qualified
        final String pathToClassFile = this.getClass().getResource(classFileName).getFile();
        return new File(pathToClassFile.substring(0, pathToClassFile.length() - classFileName.length()));
    }

}
