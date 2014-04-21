package com.atlassian.jira.webtest.selenium.harness.util;

import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.selenium.framework.core.AbstractSeleniumPageObject;
import com.atlassian.jira.webtest.selenium.framework.dialogs.AttachFileDialog;
import com.atlassian.jira.webtest.selenium.framework.model.Mouse;
import com.atlassian.selenium.Conditions;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides utility methods for issue navigation.
 *
 * @since v4.2
 */
public class IssueNavigationImpl extends AbstractSeleniumPageObject implements IssueNavigation
{
    private static final String TMP_DIR_PATH = System.getProperty("java.io.tmpdir");
    private static final int WAIT = 5000;
    private static final int UPLOAD_TIMEOUT = 60000;

    private final Navigator navigator;
    private final AttachFileDialog attachFileDialog;

    public IssueNavigationImpl(SeleniumContext ctx, final Navigator navigator)
    {
        super(ctx);
        this.navigator = navigator;
        this.attachFileDialog = new AttachFileDialog(context);
    }

    public IssueNavigation viewIssue(final String issueKey)
    {
        navigator.gotoPage("browse/" + issueKey, true);
        return this;
    }

    public void attachFile(final String fieldName, final String fileName, final int size)
    {
        attachFileDialog.openFromViewIssue();
        attachFileDialog.assertReady();

        final File tempfile = getTempfile(fileName, size);
        final String conciseFilename = tempfile.getName();
        client.type(fieldName, tempfile.getAbsolutePath());

        assertThat.byTimeout(Conditions.isNotPresent("css=div.file.loading"), UPLOAD_TIMEOUT);
        assertThat.elementPresentByTimeout("jquery=label:contains(" + conciseFilename + ")", UPLOAD_TIMEOUT);
        attachFileDialog.submit();

        //assert the file was attached
        assertThat.elementPresent("jquery=.attachment-title:contains(" + conciseFilename + ")");
    }

    public void attachFileInExistingForm(final String fieldName, final String fileName, final int size)
    {
        final File tempfile = getTempfile(fileName, size);
        final String conciseFilename = tempfile.getName();
        client.type(fieldName, tempfile.getAbsolutePath());
        assertThat.byTimeout(Conditions.isNotPresent("css=div.file.loading"), UPLOAD_TIMEOUT);
        assertThat.elementPresentByTimeout("jquery=label:contains('" + conciseFilename + "')", UPLOAD_TIMEOUT);
    }

    @Override
    public List<String> attachFileWithErrors(String fieldName, String fileName, int size)
    {
        final File tempfile = getTempfile(fileName, size);
        client.type(fieldName, tempfile.getAbsolutePath());

        //Wait for the upload to finish.
        assertThat.byTimeout(Conditions.isNotPresent("css=div.file.loading"), UPLOAD_TIMEOUT);

        return getAttachmentErrors();
    }

    @Override
    public List<String> getAttachmentErrors()
    {
        final List<String> errors = new ArrayList<String>();
        for (int i = 0; i < 100; i++)
        {
            String locator = String.format("jQuery=.file-input-list div.error:eq(%d)", i);
            if (client.isElementPresent(locator))
            {
                errors.add(StringUtils.strip(client.getText(locator)));
            }
            else
            {
                break;
            }
        }
        return errors;
    }

    public void attachFileWithComment(final String fieldName, final String fileName, final int size, final String comment, final String commentLevel)
    {
        attachFileDialog.openFromViewIssue();
        attachFileDialog.assertReady();

        final File tempfile = getTempfile(fileName, size);
        final String conciseFilename = tempfile.getName();
        client.type(fieldName, tempfile.getAbsolutePath());
        assertThat.byTimeout(Conditions.isNotPresent("css=div.file.loading"), UPLOAD_TIMEOUT);
        assertThat.elementPresentByTimeout("jquery=label:contains(" + conciseFilename + ")", UPLOAD_TIMEOUT);
        client.type("jquery=.aui-dialog-open textarea#comment", comment);
        if (commentLevel != null)
        {
            client.click("jquery=#attach-file .select-menu a", false);
            assertThat.elementPresentByTimeout("css=.select-menu .aui-list", WAIT);
            clickSecurityLevel(commentLevel);
        }
        attachFileDialog.submit();
        client.waitForPageToLoad();
        //assert the file was attached
        assertThat.elementPresentByTimeout("css=header#stalker h1", WAIT);
        assertThat.elementPresent("jquery=.attachment-title:contains(" + conciseFilename + ")");
    }

    public void editIssue(final String issueKey)
    {
        viewIssue(issueKey);
        client.open(client.getAttribute("edit-issue@href"));
        client.waitForPageToLoad();
    }

    private File getTempfile(final String fileName, final int size)
    {
        File file = new File(fileName);
        //if the file doesn't already exist, create a bogus file!
        if (!file.exists())
        {
            //create a temp file first!
            byte[] data = new byte[size];
            for (int i = 0; i < size; i++)
            {
                data[i] = 'x';
            }
            try
            {
                file = new File(TMP_DIR_PATH + File.separator + fileName);
                FileUtils.writeByteArrayToFile(file, data);
                file.deleteOnExit();
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }

        }
        return file;
    }

    private void clickSecurityLevel(String levelName)
    {
        final String classLevelName = levelName.toLowerCase().replace(" ", "-");
        Mouse.mouseover(client, "css=li.aui-list-item-li-" + classLevelName);
        client.click("css=li.aui-list-item-li-" + classLevelName);
        try
        {
            Thread.sleep(500);
        }
        catch (InterruptedException e)
        {
            throw new RuntimeException(e);
        }
    }
}
