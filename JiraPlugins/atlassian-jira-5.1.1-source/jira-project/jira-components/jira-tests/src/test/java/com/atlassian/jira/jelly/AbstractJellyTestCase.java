/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.jelly;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.dao.application.ApplicationDAO;
import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.api.DirectoryType;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.OperationType;
import com.atlassian.crowd.embedded.api.PasswordCredential;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.embedded.spi.DirectoryDao;
import com.atlassian.crowd.exception.InvalidCredentialException;
import com.atlassian.crowd.exception.InvalidUserException;
import com.atlassian.crowd.exception.OperationNotPermittedException;
import com.atlassian.crowd.exception.embedded.InvalidGroupException;
import com.atlassian.crowd.model.application.Application;
import com.atlassian.crowd.model.application.ApplicationImpl;
import com.atlassian.crowd.model.application.ApplicationType;
import com.atlassian.crowd.model.directory.DirectoryImpl;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.SystemPropertyKeys;
import com.atlassian.jira.jelly.service.EmbededJellyContext;
import com.atlassian.jira.jelly.service.JellyServiceException;
import com.atlassian.jira.jelly.tag.JellyTagConstants;
import com.atlassian.jira.local.AbstractUsersIndexingTestCase;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.MockGroup;
import com.atlassian.jira.user.MockUser;
import electric.xml.Document;
import electric.xml.Element;
import electric.xml.ParseException;
import org.apache.commons.jelly.JellyException;
import org.apache.commons.jelly.XMLOutput;

import java.io.File;
import java.io.StringWriter;

public abstract class AbstractJellyTestCase extends AbstractUsersIndexingTestCase
{
    protected static final String FS = System.getProperty("file.separator");
    protected String scriptFilename;

    public AbstractJellyTestCase(String s)
    {
        super(s);
    }

    protected AbstractJellyTestCase(String s, String scriptFilename)
    {
        super(s);
        this.scriptFilename = scriptFilename;
    }

    protected void setUp() throws Exception
    {
        super.setUp();
        UtilsForTests.getTestEntity("PermissionScheme", EasyMap.build("name", "Default Permission Scheme", "description", "Permission scheme", "id", new Long(0)));
        System.setProperty(SystemPropertyKeys.JELLY_SYSTEM_PROPERTY, "true");
    }

    protected void tearDown() throws Exception
    {
        System.setProperty(SystemPropertyKeys.JELLY_SYSTEM_PROPERTY, "false");
        super.tearDown();
    }

    protected String getPath()
    {
        final String userDirPath = new File(System.getProperty("user.dir")).getPath();
        final String absoluteDirPath = new File(this.getClass().getResource("/" + this.getClass().getName().replace('.', '/') + ".class").getFile()).getParent() + "/";

        return absoluteDirPath.substring(userDirPath.length() + 1); // relative to the user dir
    }

    protected abstract String getRelativePath();

    /**
     * Runs the given string as a Jelly Script.
     *
     * @param scriptFilename the filename of the Script in the configured path.
     * @return The resulting Document after running.
     */
    protected Document runScript(final String scriptFilename) throws JellyServiceException
    {
        return runScript(new ScriptRunner()
        {
            void runScript(EmbededJellyContext context, XMLOutput xmlOutput) throws JellyServiceException
            {
                try
                {
                    final String s = getPath() + scriptFilename;

                    context.runScript(s, xmlOutput);
                }
                catch (JellyException e)
                {
                    throw new JellyServiceException(e);
                }
            }
        });
    }

    /**
     * Runs the given string as a Jelly Script.
     *
     * @param scriptAsString the script code itself.
     * @return The resulting Document after running.
     */
    protected Document runScriptAsString(final String scriptAsString) throws JellyServiceException
    {
        return runScript(new ScriptRunner()
        {
            void runScript(final EmbededJellyContext context, final XMLOutput xmlOutput) throws JellyServiceException
            {
                context.runScriptAsString(scriptAsString, xmlOutput);
            }
        });

    }

    /**
     * Sets up the Jelly context and runs the script on it, returning the resulting Document. Template Method pattern.
     *
     * @param runner an implementation that causes Jelly to run a script.
     */
    private Document runScript(ScriptRunner runner) throws JellyServiceException
    {
        StringWriter stringWriter = new StringWriter();
        EmbededJellyContext embededJellyContext = new EmbededJellyContext();

        //Try to log the users into the jelly context.
        User loggedInUser = ComponentManager.getComponent(JiraAuthenticationContext.class).getLoggedInUser();
        if (loggedInUser != null)
        {
            embededJellyContext.setVariable(JellyTagConstants.USERNAME, loggedInUser.getName());
        }

        XMLOutput xmlOutput = XMLOutput.createXMLOutput(stringWriter, false);
        runner.runScript(embededJellyContext, xmlOutput);
        try
        {
            return new Document(stringWriter.toString());
        }
        catch (ParseException e)
        {
            throw new JellyServiceException(e);
        }
    }

    /**
     * Runs the tags in the given string as a jelly script, puts the xml and root node etc around the script.
     *
     * @param scriptBody the meat of the script to run
     * @return the resulting Document.
     * @throws JellyServiceException if something goes wrong during script execution
     */
    protected Document runScriptBody(String scriptBody) throws JellyServiceException
    {
        String wholeScript = new StringBuffer()
                .append("<?xml version=\"1.0\"?>\n")
                .append("<JiraJelly xmlns:jira=\"jelly:com.atlassian.jira.jelly.JiraTagLib\" xmlns:core=\"jelly:core\" xmlns:log=\"jelly:log\">\n")
                .append(scriptBody)
                .append("</JiraJelly>")
                .toString();

        //runScriptAsString(wholeScript);
        return runScriptAsString(wholeScript);
    }

    protected void runScriptAndAssertTextResultEquals(String expectedOutput)
            throws Exception
    {
        runScriptAndAssertTextResultEquals(expectedOutput, scriptFilename);
    }

    protected void runScriptAndAssertTextResultEquals(String expectedOutput, String scriptFileName)
            throws Exception
    {
        Document document = runScript(scriptFileName);

        assertRootTextEquals(document, expectedOutput);
    }

    /**
     * Assert that the given document contains a text node that consists only of the expectedOutput. Use expectedOutput
     * of null to assert there is no text node.
     *
     * @param resultDoc the document to be checked.
     * @param expectedOutput the text to assert exists or null to assert none.
     */
    protected void assertRootTextEquals(Document resultDoc, String expectedOutput)
    {
        Element root = resultDoc.getRoot();
        assertEquals("Unexpected root elements found while looking for text only", 0, root.getElements().size());

        // Assert the the project id of 1 was outputted in the returned document
        String textString = root.getTextString();

        assertEquals(expectedOutput, textString);
    }

    public void setScriptFilename(String scriptFilename)
    {
        this.scriptFilename = scriptFilename;
    }

    private abstract static class ScriptRunner
    {
        abstract void runScript(EmbededJellyContext context, XMLOutput xmlOutput) throws JellyServiceException;
    }
}