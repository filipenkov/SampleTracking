package com.atlassian.core.spool;

import java.io.File;
import java.io.IOException;

/**
 * Simple strategy interface for creating new files
 */
public interface FileFactory
{
    /** Create a new file based on the strategy of the factory. Factories should ensure that the file returned exists.
     *
     * @return A newly created file
     * @throws IOException
     */
    File createNewFile() throws IOException;
}
