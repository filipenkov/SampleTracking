package com.atlassian.jira.webtest.selenium.harness.util;

import java.util.List;

/**
 * Provide abstractions for dealing with Issues via Selenium
 *
 * @since v4.2
 */
public interface IssueNavigation
{
    /**
     * View an issue by key and return a handle to this object
     * @param issueKey the key of the issue to view
     * @return this
     */
    IssueNavigation viewIssue(final String issueKey);

    /**
     * Attach a file given the input field name, fileName and file size.  The file name
     * can be an absolute path to an existing file, in which case the size will be ignored and the file
     * found at that path will simply be attached.  If no file exists yet, a file containing 'x' will
     * be generated of the correct size.
     *
     * @param fieldName The input field name
     * @param fileName The fileName or absolute pat to attach
     * @param size the size of the file to generate
     */
    void attachFile(final String fieldName, final String fileName, final int size);

    /**
     * Attach a file given the input field name, fileName and file size.  The file name
     * can be an absolute path to an existing file, in which case the size will be ignored and the file
     * found at that path will simply be attached.  If no file exists yet, a file containing 'x' will
     * be generated of the correct size.  This will not try to pop open the attach file dialog, but simply
     * expect that we're already on a form with the attach file field.
     *
     * @param fieldName The input field name
     * @param fileName The fileName or absolute pat to attach
     * @param size the size of the file to generate
     */
    void attachFileInExistingForm(final String fieldName, final String fileName, final int size);

    /**
     * Attach a file given the input field name, fileName and file size.  Rather than successfully attaching
     * a file, this method should be used to assert errors when uploading a file.
     *
     * @param fieldName The input field name
     * @param fileName The fileName or absolute pat to attach
     * @param size the size of the file to generate
     *
     * @return the errors in the attachment.
     */
    List<String> attachFileWithErrors(final String fieldName, final String fileName, final int size);

    /**
     * Attach a file given the input field name, fileName and file size.  The file name
     * can be an absolute path to an existing file, in which case the size will be ignored and the file
     * found at that path will simply be attached.  If no file exists yet, a file containing 'x' will
     * be generated of the correct size. This method will also add a comment & commentLevel.
     *
     * @param fieldName The input field name
     * @param fileName The fileName or absolute pat to attach
     * @param size the size of the file to generate
     * @param comment the comment body
     * @param commentLevel the security level for the comment.
     */
    void attachFileWithComment(String fieldName, String fileName, int size, String comment, String commentLevel);

    /**
     * Return a list of the current attachment errors. Empty list will be returned if no errors.
     *
     * @return a list of the current attachment errors. Empty list will be returned if no errors.
     */
    List<String> getAttachmentErrors();

    /**
     * Go to the edit screen of the issue specified
     * @param issueKey the issue to edit
     */
    void editIssue(String issueKey);
}
