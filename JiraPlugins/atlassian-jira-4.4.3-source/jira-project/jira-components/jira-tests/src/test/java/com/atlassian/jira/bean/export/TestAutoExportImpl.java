package com.atlassian.jira.bean.export;

import com.atlassian.core.ofbiz.CoreFactory;
import com.atlassian.core.ofbiz.test.mock.MockActionDispatcher;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import org.apache.commons.io.FileUtils;
import webwork.action.Action;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class TestAutoExportImpl extends LegacyJiraMockTestCase
{
    AutoExportImpl autoExport;
    private File testTempDir;
    private FixedFilenameGenerator fixedFilenameGenerator;

    public TestAutoExportImpl(String s)
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        super.setUp();
        String fixtureName = this.getClass().getName() + "." + getName();
        String tempdir = System.getProperty("java.io.tmpdir") + File.separator + fixtureName;
        testTempDir = new File(tempdir);
        // tests sometimes get aborted, to a clean on start anyway
        if (testTempDir.exists())
        {
            FileUtils.deleteDirectory(testTempDir);
        }
        assertTrue(tempdir, testTempDir.mkdir());
        fixedFilenameGenerator = new FixedFilenameGenerator();
        autoExport = new AutoExportImpl(testTempDir.getAbsolutePath(), fixedFilenameGenerator);
    }

    protected void tearDown() throws Exception
    {
        FileUtils.deleteDirectory(testTempDir);
        testTempDir = null;
        autoExport = null;
        fixedFilenameGenerator = null;
        super.tearDown();
    }

    public void testIsValidDirectoryNullDirectory()
    {
        assertFalse(autoExport.isValidDirectory(null));
    }

    public void testIsValidDirectoryEmptyDirectory()
    {
        assertFalse(autoExport.isValidDirectory(""));
    }

    public void testIsValidDirectoryDirectoryDoesNotExist()
    {
        File tempFile = new File("doesNotExists123876");
        assertFalse(autoExport.isValidDirectory(tempFile.getAbsolutePath()));
    }

    public void testIsValidDirectoryNotDitectory() throws IOException
    {
        File tempFile = File.createTempFile("jira_autoexport_testfile", "");
        tempFile.createNewFile();
        tempFile.deleteOnExit();
        assertFalse(autoExport.isValidDirectory(tempFile.getAbsolutePath()));
    }

    public void testIsValidDirectory() throws IOException
    {
        String tempDir = System.getProperty("java.io.tmpdir");
        assertTrue(autoExport.isValidDirectory(tempDir));
    }

    public void testGetExportFilePathNoDirectory() throws Exception
    {
        try
        {
            autoExport = new AutoExportImpl("/default/directory/which/doesnt/exist");
            autoExport.getExportFilePath();
            fail("FileNotFoundException should have been thrown.");
        }
        catch (FileNotFoundException e)
        {
            assertEquals("Could not find suitable directory for export.", e.getMessage());
        }
    }

    public void testGetExportFilePathFileExists() throws IOException
    {
        File file = new File(fixedFilenameGenerator.generate(testTempDir.getAbsolutePath()).getAbsolutePath());
        file.createNewFile();

        try
        {
            autoExport.getExportFilePath();
            fail("AutoExport.FileExistsException should have been thrown.");
        }
        catch (FileExistsException e)
        {
            assertEquals("File with file name '" + file.getAbsolutePath() + "' already exists.", e.getMessage());
        }
        finally
        {
            file.delete();
        }
    }

    public void testGetExportFileTempDir() throws IOException, FileExistsException
    {
        String expectedPath = fixedFilenameGenerator.generate(testTempDir.getAbsolutePath()).getAbsolutePath();
        String path = autoExport.getExportFilePath();
        assertEquals(expectedPath, path);
    }

    public void testGetExportFileIndexDir() throws IOException, FileExistsException
    {
        ComponentAccessor.getIndexPathManager().setIndexRootPath(testTempDir.getAbsolutePath());

        String expectedPath = fixedFilenameGenerator.generate(testTempDir.getAbsolutePath()).getAbsolutePath();
        String path = autoExport.getExportFilePath();
        assertEquals(expectedPath, path);
    }

    public void testGetExportFileBackupDir() throws IOException, FileExistsException
    {
        ManagerFactory.getApplicationProperties().setString(APKeys.JIRA_PATH_BACKUP, testTempDir.getAbsolutePath());

        String expectedPath = fixedFilenameGenerator.generate(testTempDir.getAbsolutePath()).getAbsolutePath();
        String path = autoExport.getExportFilePath();
        assertEquals(expectedPath, path);
    }

    public void testGetExportFilePathServletTempDir() throws Exception
    {
        String expectedPath = fixedFilenameGenerator.generate(testTempDir.getAbsolutePath()).getAbsolutePath();
        String exportFilePath = autoExport.getExportFilePath();
        assertEquals(expectedPath, exportFilePath);
    }

    public void testExportData() throws Exception, FileExistsException, FileNotFoundException
    {
        String expectedFilePath = fixedFilenameGenerator.generate(testTempDir.getAbsolutePath()).getAbsolutePath();
        String filePath = autoExport.exportData();
        assertEquals(expectedFilePath, filePath);
    }


    private class FixedFilenameGenerator implements FilenameGenerator {
        public File generate(String basepath) throws IOException
        {
            return new File(basepath, AutoExport.BASE_FILE_NAME + "fixed-filename" + ".zip");
        }

    }

}


