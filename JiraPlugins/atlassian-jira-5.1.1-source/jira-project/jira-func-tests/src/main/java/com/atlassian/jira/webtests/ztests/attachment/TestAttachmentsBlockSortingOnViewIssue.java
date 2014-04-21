package com.atlassian.jira.webtests.ztests.attachment;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.navigation.issue.AttachmentsBlock;
import com.atlassian.jira.functest.framework.navigation.issue.FileAttachmentsList;
import com.atlassian.jira.functest.framework.navigation.issue.ImageAttachmentsGallery;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.util.collect.CollectionBuilder;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Defines the test cases to be implemented to verify the sorting functionality of the attachments block on the view
 * issue page.
 *
 * @since v4.2
 */
@SuppressWarnings ({"unused"})
public interface TestAttachmentsBlockSortingOnViewIssue
{
    void testAttachmentsDefaultToSortingByNameInDescendingOrder() throws Exception;

    void testCanSortAttachmentsByFileNameInAscendingOrder() throws Exception;

    void testCanSortAttachmentsByFileNameInDescendingOrder() throws Exception;

    void testCanSortAttachmentsByDateInAscendingOrder() throws Exception;

    void testCanSortAttachmentsByDateInDescendingOrder() throws Exception;

    abstract class AbstractTestAttachmentsBlockSortingOnViewIssue extends FuncTestCase
    {
        @Override
        protected void setUpTest()
        {
            super.setUpTest();
            administration.restoreData("TestAttachmentsBlockSortingOnViewIssue.xml");
            removeAttachmentFilesFromJiraHome(); // Clean up any left-overs from previous tests
            copyAttachmentFilesToJiraHome();

            // Attachment Sorting by Name is locale sensitive, so we set this locale before running these tests.
            administration.generalConfiguration().setJiraLocale("English (Australia)");
        }

        @Override
        protected void tearDownTest()
        {
            removeAttachmentFilesFromJiraHome();
            super.tearDownTest();
        }

        protected final void copyAttachmentFilesToJiraHome()
        {
            final File jiraAttachmentsPath = new File(administration.getCurrentAttachmentPath());
            final File testAttachmentsPath = new File(environmentData.getXMLDataLocation(), "TestAttachmentsBlockSortingOnViewIssue/attachments");

            try
            {
                FileUtils.copyDirectory(testAttachmentsPath, jiraAttachmentsPath);
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }

        protected final void removeAttachmentFilesFromJiraHome()
        {
            final File jiraAttachmentsPath = new File(administration.getCurrentAttachmentPath());
            try
            {
                FileUtils.cleanDirectory(jiraAttachmentsPath);
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Responsible for holding tests that verify that the attachments list on the view issue page
     * can be sorted using a key (i.e. name, date ...) in ascending or descending order.
     *
     * @since v4.2
     */
    @WebTest ({ Category.FUNC_TEST, Category.BROWSING })
    class TestAttachmentsListSorting extends AbstractTestAttachmentsBlockSortingOnViewIssue implements TestAttachmentsBlockSortingOnViewIssue
    {
        public void testAttachmentsDefaultToSortingByNameInDescendingOrder() throws Exception
        {
            final List<FileAttachmentsList.FileAttachmentItem> expectedFileAttachmentsList =
                    CollectionBuilder.newBuilder(
                            FileAttachmentsList.Items.file("_fil\u00E5e", "0.0 kB", ADMIN_FULLNAME, "06/May/10 12:13 PM"),
                            FileAttachmentsList.Items.file("[#JRA-18780] Test Issue 123 - Atlassian.pdf", "193 kB",
                                    ADMIN_FULLNAME, "06/May/10 11:27 AM"),
                            FileAttachmentsList.Items.file("[#JRA-18780] Test Issue 123 - Printable.pdf", "98 kB",
                                    ADMIN_FULLNAME, "06/May/10 12:02 PM"),
                            FileAttachmentsList.Items.file("a", "0.0 kB", ADMIN_FULLNAME, "06/May/10 12:12 PM"),
                            FileAttachmentsList.Items.file("a1k4BJwT.jpg.part", "22 kB", ADMIN_FULLNAME, "06/May/10 12:01 PM"),
                            FileAttachmentsList.Items.file("\u00E1 file", "0.0 kB", ADMIN_FULLNAME, "06/May/10 12:14 PM"),
                            FileAttachmentsList.Items.file("build.xml", "1 kB", ADMIN_FULLNAME, "06/May/10 12:00 PM"),
                            FileAttachmentsList.Items.file("catalina.sh", "12 kB", ADMIN_FULLNAME, "06/May/10 12:15 PM"),
                            FileAttachmentsList.Items.file("license.txt", "1 kB", ADMIN_FULLNAME, "06/May/10 11:26 AM"),
                            FileAttachmentsList.Items.file("license.txt", "1 kB", ADMIN_FULLNAME, "06/May/10 11:24 AM"),
                            FileAttachmentsList.Items.file("pom.xml", "5 kB", ADMIN_FULLNAME, "06/May/10 11:29 AM"),
                            FileAttachmentsList.Items.file("pom.xml", "2 kB", ADMIN_FULLNAME, "06/May/10 11:25 AM"),
                            FileAttachmentsList.Items.file("Tickspot", "0.1 kB", ADMIN_FULLNAME, "06/May/10 12:03 PM")
                    ).asList();

            final List<FileAttachmentsList.FileAttachmentItem> actualFileAttachmentsList =
                    navigation.issue().attachments("HSP-1").list().get();

            assertEquals(expectedFileAttachmentsList, actualFileAttachmentsList);
        }

        public void testCanSortAttachmentsByFileNameInAscendingOrder() throws Exception
        {
            final List<FileAttachmentsList.FileAttachmentItem> expectedFileAttachmentsList =
                    CollectionBuilder.newBuilder(
                            FileAttachmentsList.Items.file("_fil\u00E5e", "0.0 kB", ADMIN_FULLNAME, "06/May/10 12:13 PM"),
                            FileAttachmentsList.Items.file("[#JRA-18780] Test Issue 123 - Atlassian.pdf", "193 kB",
                                    ADMIN_FULLNAME, "06/May/10 11:27 AM"),
                            FileAttachmentsList.Items.file("[#JRA-18780] Test Issue 123 - Printable.pdf", "98 kB",
                                    ADMIN_FULLNAME, "06/May/10 12:02 PM"),
                            FileAttachmentsList.Items.file("a", "0.0 kB", ADMIN_FULLNAME, "06/May/10 12:12 PM"),
                            FileAttachmentsList.Items.file("a1k4BJwT.jpg.part", "22 kB", ADMIN_FULLNAME, "06/May/10 12:01 PM"),
                            FileAttachmentsList.Items.file("\u00E1 file", "0.0 kB", ADMIN_FULLNAME, "06/May/10 12:14 PM"),
                            FileAttachmentsList.Items.file("build.xml", "1 kB", ADMIN_FULLNAME, "06/May/10 12:00 PM"),
                            FileAttachmentsList.Items.file("catalina.sh", "12 kB", ADMIN_FULLNAME, "06/May/10 12:15 PM"),
                            FileAttachmentsList.Items.file("license.txt", "1 kB", ADMIN_FULLNAME, "06/May/10 11:26 AM"),
                            FileAttachmentsList.Items.file("license.txt", "1 kB", ADMIN_FULLNAME, "06/May/10 11:24 AM"),
                            FileAttachmentsList.Items.file("pom.xml", "5 kB", ADMIN_FULLNAME, "06/May/10 11:29 AM"),
                            FileAttachmentsList.Items.file("pom.xml", "2 kB", ADMIN_FULLNAME, "06/May/10 11:25 AM"),
                            FileAttachmentsList.Items.file("Tickspot", "0.1 kB", ADMIN_FULLNAME, "06/May/10 12:03 PM")
                    ).asList();

            final AttachmentsBlock attachmentsBlock = navigation.issue().attachments("HSP-1");
            attachmentsBlock.sort(AttachmentsBlock.Sort.Key.NAME, AttachmentsBlock.Sort.Direction.ASCENDING);

            final List<FileAttachmentsList.FileAttachmentItem> actualFileAttachmentsList = attachmentsBlock.list().get();
            assertEquals(expectedFileAttachmentsList, actualFileAttachmentsList);

            verifySortingSettingIsStickyDuringTheSession(expectedFileAttachmentsList);
        }

        public void testCanSortAttachmentsByFileNameInDescendingOrder() throws Exception
        {
            final List<FileAttachmentsList.FileAttachmentItem> expectedFileAttachmentsList =
                    CollectionBuilder.newBuilder(
                            FileAttachmentsList.Items.file("Tickspot", "0.1 kB", ADMIN_FULLNAME, "06/May/10 12:03 PM"),
                            FileAttachmentsList.Items.file("pom.xml", "2 kB", ADMIN_FULLNAME, "06/May/10 11:25 AM"),
                            FileAttachmentsList.Items.file("pom.xml", "5 kB", ADMIN_FULLNAME, "06/May/10 11:29 AM"),
                            FileAttachmentsList.Items.file("license.txt", "1 kB", ADMIN_FULLNAME, "06/May/10 11:24 AM"),
                            FileAttachmentsList.Items.file("license.txt", "1 kB", ADMIN_FULLNAME, "06/May/10 11:26 AM"),
                            FileAttachmentsList.Items.file("catalina.sh", "12 kB", ADMIN_FULLNAME, "06/May/10 12:15 PM"),
                            FileAttachmentsList.Items.file("build.xml", "1 kB", ADMIN_FULLNAME, "06/May/10 12:00 PM"),
                            FileAttachmentsList.Items.file("\u00E1 file", "0.0 kB", ADMIN_FULLNAME, "06/May/10 12:14 PM"),
                            FileAttachmentsList.Items.file("a1k4BJwT.jpg.part", "22 kB", ADMIN_FULLNAME, "06/May/10 12:01 PM"),
                            FileAttachmentsList.Items.file("a", "0.0 kB", ADMIN_FULLNAME, "06/May/10 12:12 PM"),
                            FileAttachmentsList.Items.file("[#JRA-18780] Test Issue 123 - Printable.pdf", "98 kB",
                                    ADMIN_FULLNAME, "06/May/10 12:02 PM"),
                            FileAttachmentsList.Items.file("[#JRA-18780] Test Issue 123 - Atlassian.pdf", "193 kB",
                                    ADMIN_FULLNAME, "06/May/10 11:27 AM"),
                            FileAttachmentsList.Items.file("_fil\u00E5e", "0.0 kB", ADMIN_FULLNAME, "06/May/10 12:13 PM")
                    ).asList();

            final AttachmentsBlock attachmentsBlock = navigation.issue().attachments("HSP-1");
            attachmentsBlock.sort(AttachmentsBlock.Sort.Key.NAME, AttachmentsBlock.Sort.Direction.DESCENDING);

            final List<FileAttachmentsList.FileAttachmentItem> actualFileAttachmentsList = attachmentsBlock.list().get();
            assertEquals(expectedFileAttachmentsList, actualFileAttachmentsList);

            verifySortingSettingIsStickyDuringTheSession(expectedFileAttachmentsList);
        }

        public void testCanSortAttachmentsByDateInAscendingOrder() throws Exception
        {
            final List<FileAttachmentsList.FileAttachmentItem> expectedFileAttachmentsList =
                    CollectionBuilder.newBuilder(
                            FileAttachmentsList.Items.file("license.txt", "1 kB", ADMIN_FULLNAME, "06/May/10 11:24 AM"),
                            FileAttachmentsList.Items.file("pom.xml", "2 kB", ADMIN_FULLNAME, "06/May/10 11:25 AM"),
                            FileAttachmentsList.Items.file("license.txt", "1 kB", ADMIN_FULLNAME, "06/May/10 11:26 AM"),
                            FileAttachmentsList.Items.file("[#JRA-18780] Test Issue 123 - Atlassian.pdf", "193 kB",
                                    ADMIN_FULLNAME, "06/May/10 11:27 AM"),
                            FileAttachmentsList.Items.file("pom.xml", "5 kB", ADMIN_FULLNAME, "06/May/10 11:29 AM"),
                            FileAttachmentsList.Items.file("build.xml", "1 kB", ADMIN_FULLNAME, "06/May/10 12:00 PM"),
                            FileAttachmentsList.Items.file("a1k4BJwT.jpg.part", "22 kB", ADMIN_FULLNAME, "06/May/10 12:01 PM"),
                                                    FileAttachmentsList.Items.file("[#JRA-18780] Test Issue 123 - Printable.pdf", "98 kB",
                                                            ADMIN_FULLNAME, "06/May/10 12:02 PM"),
                            FileAttachmentsList.Items.file("Tickspot", "0.1 kB", ADMIN_FULLNAME, "06/May/10 12:03 PM"),
                            FileAttachmentsList.Items.file("a", "0.0 kB", ADMIN_FULLNAME, "06/May/10 12:12 PM"),
                            FileAttachmentsList.Items.file("_fil\u00E5e", "0.0 kB", ADMIN_FULLNAME, "06/May/10 12:13 PM"),
                            FileAttachmentsList.Items.file("\u00E1 file", "0.0 kB", ADMIN_FULLNAME, "06/May/10 12:14 PM"),
                            FileAttachmentsList.Items.file("catalina.sh", "12 kB", ADMIN_FULLNAME, "06/May/10 12:15 PM")
                    ).asList();

            final AttachmentsBlock attachmentsBlock = navigation.issue().attachments("HSP-1");
            attachmentsBlock.sort(AttachmentsBlock.Sort.Key.DATE, AttachmentsBlock.Sort.Direction.ASCENDING);

            final List<FileAttachmentsList.FileAttachmentItem> actualFileAttachmentsList = attachmentsBlock.list().get();
            assertEquals(expectedFileAttachmentsList, actualFileAttachmentsList);

            verifySortingSettingIsStickyDuringTheSession(expectedFileAttachmentsList);
        }

        public void testCanSortAttachmentsByDateInDescendingOrder() throws Exception
        {
            final List<FileAttachmentsList.FileAttachmentItem> expectedFileAttachmentsList =
                    CollectionBuilder.newBuilder(
                            FileAttachmentsList.Items.file("catalina.sh", "12 kB", ADMIN_FULLNAME, "06/May/10 12:15 PM"),
                            FileAttachmentsList.Items.file("\u00E1 file", "0.0 kB", ADMIN_FULLNAME, "06/May/10 12:14 PM"),
                            FileAttachmentsList.Items.file("_fil\u00E5e", "0.0 kB", ADMIN_FULLNAME, "06/May/10 12:13 PM"),
                            FileAttachmentsList.Items.file("a", "0.0 kB", ADMIN_FULLNAME, "06/May/10 12:12 PM"),
                            FileAttachmentsList.Items.file("Tickspot", "0.1 kB", ADMIN_FULLNAME, "06/May/10 12:03 PM"),
                            FileAttachmentsList.Items.file("[#JRA-18780] Test Issue 123 - Printable.pdf", "98 kB",
                                    ADMIN_FULLNAME, "06/May/10 12:02 PM"),
                            FileAttachmentsList.Items.file("a1k4BJwT.jpg.part", "22 kB", ADMIN_FULLNAME, "06/May/10 12:01 PM"),
                            FileAttachmentsList.Items.file("build.xml", "1 kB", ADMIN_FULLNAME, "06/May/10 12:00 PM"),
                            FileAttachmentsList.Items.file("pom.xml", "5 kB", ADMIN_FULLNAME, "06/May/10 11:29 AM"),
                            FileAttachmentsList.Items.file("[#JRA-18780] Test Issue 123 - Atlassian.pdf", "193 kB",
                                    ADMIN_FULLNAME, "06/May/10 11:27 AM"),
                            FileAttachmentsList.Items.file("license.txt", "1 kB", ADMIN_FULLNAME, "06/May/10 11:26 AM"),
                            FileAttachmentsList.Items.file("pom.xml", "2 kB", ADMIN_FULLNAME, "06/May/10 11:25 AM"),
                            FileAttachmentsList.Items.file("license.txt", "1 kB", ADMIN_FULLNAME, "06/May/10 11:24 AM")
                    ).asList();

            final AttachmentsBlock attachmentsBlock = navigation.issue().attachments("HSP-1");
            attachmentsBlock.sort(AttachmentsBlock.Sort.Key.DATE, AttachmentsBlock.Sort.Direction.DESCENDING);

            final List<FileAttachmentsList.FileAttachmentItem> actualFileAttachmentsList = attachmentsBlock.list().get();
            assertEquals(expectedFileAttachmentsList, actualFileAttachmentsList);
            verifySortingSettingIsStickyDuringTheSession(expectedFileAttachmentsList);
        }

        private void verifySortingSettingIsStickyDuringTheSession
                (final List<FileAttachmentsList.FileAttachmentItem> expectedFileAttachmentsList)
        {
            navigation.gotoDashboard();

            final AttachmentsBlock attachmentsBlock = navigation.issue().attachments("HSP-1");
            final List<FileAttachmentsList.FileAttachmentItem> actualAttachmentsList = attachmentsBlock.list().get();

            assertEquals(expectedFileAttachmentsList, actualAttachmentsList);
        }
    }

    /**
     * Responsible for holding tests that verify that the image attachments shown in the image gallery on the view issue
     * page can be sorted using a key (i.e. name, date ...) in ascending or descending order.
     *
     * @since v4.2
     */
    @WebTest ({ Category.FUNC_TEST, Category.BROWSING })
    class TestImageAttachmentsGallerySorting extends AbstractTestAttachmentsBlockSortingOnViewIssue implements TestAttachmentsBlockSortingOnViewIssue
    {
        public void testAttachmentsDefaultToSortingByNameInDescendingOrder() throws Exception
        {
            final List<ImageAttachmentsGallery.ImageAttachmentItem> expectedImageAttachments =
                    CollectionBuilder.newBuilder(
                            new ImageAttachmentsGallery.ImageAttachmentItem("200px-FCB.svg.png","16 kB"),
                            new ImageAttachmentsGallery.ImageAttachmentItem("235px-Floppy_disk_2009_G1.jpg","8 kB"),
                            new ImageAttachmentsGallery.ImageAttachmentItem("235px-Floppy_disk_2009_G1.jpg","8 kB"),
                            new ImageAttachmentsGallery.ImageAttachmentItem("tropical-desktop-wallpaper-1280x1024.jpg", "115 kB")
                    ).asList();

            final List<ImageAttachmentsGallery.ImageAttachmentItem> actualImageAttachments =
                    navigation.issue().attachments("HSP-1").gallery().get();

            assertEquals(expectedImageAttachments, actualImageAttachments);
        }

        public void testCanSortAttachmentsByFileNameInAscendingOrder() throws Exception
        {
            final List<ImageAttachmentsGallery.ImageAttachmentItem> expectedImageAttachments =
                    CollectionBuilder.newBuilder(
                            new ImageAttachmentsGallery.ImageAttachmentItem("200px-FCB.svg.png","16 kB"),
                            new ImageAttachmentsGallery.ImageAttachmentItem("235px-Floppy_disk_2009_G1.jpg","8 kB"),
                            new ImageAttachmentsGallery.ImageAttachmentItem("235px-Floppy_disk_2009_G1.jpg","8 kB"),
                            new ImageAttachmentsGallery.ImageAttachmentItem("tropical-desktop-wallpaper-1280x1024.jpg", "115 kB")
                    ).asList();

            final AttachmentsBlock attachmentsBlock = navigation.issue().attachments("HSP-1");
            attachmentsBlock.sort(AttachmentsBlock.Sort.Key.NAME, AttachmentsBlock.Sort.Direction.ASCENDING);

            final List<ImageAttachmentsGallery.ImageAttachmentItem> actualImageAttachments =
                    attachmentsBlock.gallery().get();

            assertEquals(expectedImageAttachments, actualImageAttachments);
            verifySortingSettingIsStickyDuringTheSession(expectedImageAttachments);
        }

        public void testCanSortAttachmentsByFileNameInDescendingOrder() throws Exception
        {
            final List<ImageAttachmentsGallery.ImageAttachmentItem> expectedImageAttachments =
                    CollectionBuilder.newBuilder(
                            new ImageAttachmentsGallery.ImageAttachmentItem("tropical-desktop-wallpaper-1280x1024.jpg", "115 kB"),
                            new ImageAttachmentsGallery.ImageAttachmentItem("235px-Floppy_disk_2009_G1.jpg","8 kB"),
                            new ImageAttachmentsGallery.ImageAttachmentItem("235px-Floppy_disk_2009_G1.jpg","8 kB"),
                            new ImageAttachmentsGallery.ImageAttachmentItem("200px-FCB.svg.png","16 kB")
                    ).asList();

            final AttachmentsBlock attachmentsBlock = navigation.issue().attachments("HSP-1");
            attachmentsBlock.sort(AttachmentsBlock.Sort.Key.NAME, AttachmentsBlock.Sort.Direction.DESCENDING);

            final List<ImageAttachmentsGallery.ImageAttachmentItem> actualImageAttachments =
                    attachmentsBlock.gallery().get();

            assertEquals(expectedImageAttachments, actualImageAttachments);
            verifySortingSettingIsStickyDuringTheSession(expectedImageAttachments);
        }

        public void testCanSortAttachmentsByDateInAscendingOrder() throws Exception
        {
            final List<ImageAttachmentsGallery.ImageAttachmentItem> expectedImageAttachments =
                    CollectionBuilder.newBuilder(
                            new ImageAttachmentsGallery.ImageAttachmentItem("235px-Floppy_disk_2009_G1.jpg","8 kB"),
                            new ImageAttachmentsGallery.ImageAttachmentItem("200px-FCB.svg.png","16 kB"),
                            new ImageAttachmentsGallery.ImageAttachmentItem("tropical-desktop-wallpaper-1280x1024.jpg", "115 kB"),
                            new ImageAttachmentsGallery.ImageAttachmentItem("235px-Floppy_disk_2009_G1.jpg","8 kB")
                    ).asList();

            final AttachmentsBlock attachmentsBlock = navigation.issue().attachments("HSP-1");
            attachmentsBlock.sort(AttachmentsBlock.Sort.Key.DATE, AttachmentsBlock.Sort.Direction.ASCENDING);

            final List<ImageAttachmentsGallery.ImageAttachmentItem> actualImageAttachments =
                    attachmentsBlock.gallery().get();

            assertEquals(expectedImageAttachments, actualImageAttachments);
            verifySortingSettingIsStickyDuringTheSession(expectedImageAttachments);
        }

        public void testCanSortAttachmentsByDateInDescendingOrder() throws Exception
        {
            final List<ImageAttachmentsGallery.ImageAttachmentItem> expectedImageAttachments =
                    CollectionBuilder.newBuilder(
                            new ImageAttachmentsGallery.ImageAttachmentItem("235px-Floppy_disk_2009_G1.jpg","8 kB"),
                            new ImageAttachmentsGallery.ImageAttachmentItem("tropical-desktop-wallpaper-1280x1024.jpg", "115 kB"),
                            new ImageAttachmentsGallery.ImageAttachmentItem("200px-FCB.svg.png","16 kB"),
                            new ImageAttachmentsGallery.ImageAttachmentItem("235px-Floppy_disk_2009_G1.jpg","8 kB")
                    ).asList();

            final AttachmentsBlock attachmentsBlock = navigation.issue().attachments("HSP-1");
            attachmentsBlock.sort(AttachmentsBlock.Sort.Key.DATE, AttachmentsBlock.Sort.Direction.DESCENDING);

            final List<ImageAttachmentsGallery.ImageAttachmentItem> actualImageAttachments =
                    attachmentsBlock.gallery().get();

            assertEquals(expectedImageAttachments, actualImageAttachments);
            verifySortingSettingIsStickyDuringTheSession(expectedImageAttachments);
        }

        private void verifySortingSettingIsStickyDuringTheSession
                (final List<ImageAttachmentsGallery.ImageAttachmentItem> expectedFileAttachmentsList)
        {
            navigation.gotoDashboard();

            final AttachmentsBlock attachmentsBlock = navigation.issue().attachments("HSP-1");
            final List<ImageAttachmentsGallery.ImageAttachmentItem> actualImageAttachments =
                    attachmentsBlock.gallery().get();

            assertEquals(expectedFileAttachmentsList, actualImageAttachments);
        }
    }
}