package com.atlassian.jira;

import com.atlassian.jira.util.dbc.Assertions;

import java.io.File;
import java.io.FileFilter;

/**
 * This Class walkes through a directory structure and based on a FileNameFilter provided by the FileChecker and if a
 * the file matches the FileNameFilter the checkFile() method is called for each file on the FileChecker (Visitor
 * pattern).
 *
 * @since v4.0
 */
public class FileFinder
{
    static final FileFilter IS_A_NON_DOT_DIR = new FileFilter()
    {
        public boolean accept(File pathname)
        {
            return pathname.isDirectory() && pathname.getName().charAt(0) != '.';
        }
    };

    private final FileChecker fileChecker;

    public FileFinder(final FileChecker fileChecker)
    {
        this.fileChecker = Assertions.notNull("fileChecker", fileChecker);
    }

    public void checkDir(File rootDir)
    {
        Assertions.stateTrue(rootDir.getAbsolutePath(), rootDir.exists() && rootDir.isDirectory());
        for (File propertiesFile : rootDir.listFiles(fileChecker.getFilenameFilter()))
        {
            fileChecker.checkFile(propertiesFile);
        }
        for (File dir : rootDir.listFiles(IS_A_NON_DOT_DIR))
        {
            checkDir(dir);
        }
    }
}
