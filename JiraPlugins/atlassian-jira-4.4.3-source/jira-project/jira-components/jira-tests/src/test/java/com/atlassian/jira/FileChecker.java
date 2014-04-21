package com.atlassian.jira;

import java.io.File;
import java.io.FilenameFilter;

/**
 * TODO: Document this class / interface here
 *
 * @since v4.0
 */
public interface FileChecker
{
    void checkFile(File file);

    FilenameFilter getFilenameFilter();

    void testFinished();
}
