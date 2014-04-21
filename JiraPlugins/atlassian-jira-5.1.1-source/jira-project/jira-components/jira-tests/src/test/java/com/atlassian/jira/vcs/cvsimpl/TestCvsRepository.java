/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.vcs.cvsimpl;

import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.jira.util.LockException;
import com.atlassian.jira.vcs.viewcvs.ViewCvsBrowser;
import com.mockobjects.constraint.Constraint;
import com.mockobjects.constraint.IsAnything;
import com.mockobjects.constraint.IsEqual;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.module.propertyset.map.MapPropertySet;
import net.sf.statcvs.input.LogSyntaxException;
import net.sf.statcvs.model.CvsContent;
import org.netbeans.lib.cvsclient.CVSRoot;
import org.netbeans.lib.cvsclient.command.CommandException;
import org.netbeans.lib.cvsclient.connection.AuthenticationException;

import java.io.File;
import java.io.IOException;

public class TestCvsRepository extends LegacyJiraMockTestCase
{
    String name = "test-cvs-name";
    String password = "test-cvs-password";
    String cvsLogFilePath = System.getProperty("java.io.tempdir") + File.separator + "cvs.log";
    String moduleName = "testcvsmodule";
    String cvsRoot = ":pserver:test@testserver:/root/test";
    long cvsTimeout = 1000;
    // The repository path part o fthe CVS root
    String repositoryPath = "/root/test";
    String baseURL = "http://test-cvs-baseURL";
    String badBaseUrl = "badBaseUrl";
    String description = "test-cvs-description";
    String repositoryBrowserURL = "repositoryBrowserURL";
    String repositoryBroswerType = "VIEW_CVS";
    String type = "cvs";
    boolean fetchLog = true;
    private String viewCvsRootParameter = "root";

    public TestCvsRepository(String s)
    {
        super(s);
    }

    public void testSettersGetters()
    {
        Mock mockPropertySet = new Mock(PropertySet.class);
        mockPropertySet.expectAndReturn("getString", P.args(new IsAnything()), null);

        CvsRepository repository = new CvsRepository((PropertySet) mockPropertySet.proxy(), null);

        repository.setDescription(description);
        assertEquals(description, repository.getDescription());

        repository.setName(name);
        assertEquals(name, repository.getName());

        repository.setCvsLogFilePath(cvsLogFilePath);
        assertEquals(cvsLogFilePath, repository.getCvsLogFilePath());

        repository.setDescription(description);
        assertEquals(description, repository.getDescription());

        //test other setters and getters`
        repository.setPassword(password);
        assertEquals(password, repository.getPassword());

        repository.setModuleName(moduleName);
        assertEquals(moduleName, repository.getModuleName());

        repository.setCvsRoot(cvsRoot);
        assertEquals(cvsRoot, repository.getCvsRoot());

        repository.setCvsTimeout(cvsTimeout);
        assertEquals(cvsTimeout, repository.getCvsTimeout());
    }


    public void testCvsRepositoryPropertySetConstructor()
    {
        //this is because mocks doesn't allow for different return values based on method arguments.....yet.
        PropertySet ps = setupPropertySet();

        //todo - fix this when new mocks version comes out
        //        Mock propertySetMock = new Mock(PropertySet.class);
        //        propertySetMock.expectAndReturn("getString", P.args(new IsEqual(CvsRepository.KEY_PASSWORD)), "test-cvs-password");
        //        propertySetMock.expectAndReturn("getString", P.args(new IsEqual(CvsRepository.KEY_BASEDIR)), "test-base-dir");
        //        propertySetMock.expectAndReturn("getString", P.args(new IsAnything()), "test-base-dir");
        //        propertySetMock.expectAndReturn("getString", P.args(new IsAnything()), "test-cvs-password");
        //        propertySetMock.expectVoid();
        //        propertySetMock.verify();

        CvsRepository repository = new CvsRepository(ps, null);
        assertEquals(name, repository.getName());
        assertEquals(description, repository.getDescription());
        assertEquals(password, repository.getPassword());
        assertEquals(cvsLogFilePath, repository.getCvsLogFilePath());
        assertEquals(cvsRoot, repository.getCvsRoot());
        assertEquals(moduleName, repository.getModuleName());
        assertEquals(fetchLog, repository.fetchLog());
    }

    private PropertySet setupPropertySet()
    {
        return new MapPropertySet()
        {
            public String getString(String key)
            {
                if (CvsRepository.KEY_PASSWORD.equals(key))
                {
                    return password;
                }
                else if (CvsRepository.KEY_LOG_FILE_PATH.equals(key))
                {
                    return cvsLogFilePath;
                }
                else if (CvsRepository.KEY_CVS_ROOT.equals(key))
                {
                    return cvsRoot;
                }
                else if (CvsRepository.KEY_DESCRIPTION.equals(key))
                {
                    return description;
                }
                else if (CvsRepository.KEY_NAME.equals(key))
                {
                    return name;
                }
                else if (CvsRepository.KEY_MODULE_NAME.equals(key))
                {
                    return moduleName;
                }
                else if (CvsRepository.KEY_FETCH_LOG.equals(key))
                {
                    return String.valueOf(fetchLog);
                }
                else if (CvsRepository.KEY_CVS_TIMEOUT.equals(key))
                {
                    return String.valueOf(cvsTimeout);
                }
                else if (CvsRepository.KEY_REPOSITTORY_BROWSER_TYPE.equals(key))
                {
                    return repositoryBroswerType;
                }
                else if (ViewCvsBrowser.KEY_BASE_URL.equals(key))
                {
                    return baseURL;
                }
                else if (ViewCvsBrowser.ROOT_PARAMETER.equals(key))
                {
                    return viewCvsRootParameter;
                }

                throw new IllegalArgumentException("Invalid property " + key);
            }
        };
    }

    /*
    // TODO Test this when we can mock stuff out - as otherwise need to create whole sort of stuff
    public void testGetCommitsForIssue() throws RepositoryException
    {
        // Setup Mock CvsRepositoryUtil
        Mock mockCvsRepositoryUtil = new Mock(CvsRepositoryUtil.class);
        mockCvsRepositoryUtil.setStrict(true);

        mockCvsRepositoryUtil.expectVoid("updateCvs", new Constraint[]{new IsAnything(), new IsEqual(cvsRoot), new IsEqual(moduleName), new IsEqual(password)});
        final CVSRoot testCVSRoot = CVSRoot.parse(cvsRoot);
        mockCvsRepositoryUtil.expectAndReturn("parseCvsRoot", P.args(new IsEqual(cvsRoot)), testCVSRoot);

        CvsContent testCvsContent = new CvsContent();
        Commit testCommitA = new Commit(null);
        Commit testCommitB = new Commit(null);
        Commit testCommitC = new Commit(null);
        testCvsContent.setCommits(EasyList.build(testCommitA, testCommitB, testCommitC));
        mockCvsRepositoryUtil.expectAndReturn("parseCvsLogs", new Constraint[]{new IsAnything(), new IsEqual(moduleName), new IsEqual(repositoryPath), new IsEqual(name)}, testCvsContent);

        PropertySet ps = setupPropertySet();
        CvsRepository repository = new CvsRepository(ps, (CvsRepositoryUtil) mockCvsRepositoryUtil.proxy());
        List commitsForIssue = repository.getCommitsForIssue("TST-1");
        assertEquals(3, commitsForIssue.size());

        mockCvsRepositoryUtil.verify();
    }
    */

    public void testParseCvsLogs() throws IOException, LogSyntaxException, LockException, CommandException, AuthenticationException
    {
        // Setup Mock CvsRepositoryUtil
        Mock mockCvsRepositoryUtil = new Mock(CvsRepositoryUtil.class);
        mockCvsRepositoryUtil.setStrict(true);

        final CVSRoot testCVSRoot = CVSRoot.parse(cvsRoot);
        mockCvsRepositoryUtil.expectVoid("updateCvs", new Constraint[]{new IsAnything(), new IsEqual(cvsRoot), new IsEqual(moduleName), new IsEqual(password), new IsEqual(cvsTimeout)});
        mockCvsRepositoryUtil.expectAndReturn("parseCvsRoot", P.args(new IsEqual(cvsRoot)), testCVSRoot);
        CvsContent testCvsContent = new CvsContent();
        mockCvsRepositoryUtil.expectAndReturn("parseCvsLogs", new Constraint[]{new IsAnything(), new IsEqual(moduleName), new IsEqual(repositoryPath), new IsEqual(name)}, testCvsContent);

        PropertySet ps = setupPropertySet();

        CvsRepository repository = new CvsRepository(ps, (CvsRepositoryUtil) mockCvsRepositoryUtil.proxy());
        repository.updateRepository();

        // Check Results
        mockCvsRepositoryUtil.verify();
    }

    public void testCopyContent()
            throws LockException, IOException, LogSyntaxException, CommandException, AuthenticationException
    {
        final CVSRoot testCVSRoot1 = CVSRoot.parse(cvsRoot);
        final CvsContent testCvsContent1 = new CvsContent();
        Mock mockCvsRepositoryUtil1 = new Mock(CvsRepositoryUtil.class);
        mockCvsRepositoryUtil1.setStrict(true);
        mockCvsRepositoryUtil1.expectVoid("updateCvs", new Constraint[]{new IsAnything(), new IsEqual(cvsRoot), new IsEqual(moduleName), new IsEqual(password), new IsEqual(cvsTimeout)});
        mockCvsRepositoryUtil1.expectAndReturn("parseCvsRoot", P.args(new IsEqual(cvsRoot)), testCVSRoot1);
        mockCvsRepositoryUtil1.expectAndReturn("parseCvsLogs", new Constraint[]{new IsAnything(), new IsEqual(moduleName), new IsEqual(repositoryPath), new IsEqual(name)}, testCvsContent1);
        final CvsRepository repository1 = new CvsRepository(setupPropertySet(), (CvsRepositoryUtil) mockCvsRepositoryUtil1.proxy());
        repository1.updateRepository();

        final CVSRoot testCVSRoot2 = CVSRoot.parse(cvsRoot);
        final CvsContent testCvsContent2 = new CvsContent();
        Mock mockCvsRepositoryUtil2 = new Mock(CvsRepositoryUtil.class);
        mockCvsRepositoryUtil2.setStrict(true);
        mockCvsRepositoryUtil2.expectVoid("updateCvs", new Constraint[]{new IsAnything(), new IsEqual(cvsRoot), new IsEqual(moduleName), new IsEqual(password), new IsEqual(cvsTimeout)});
        mockCvsRepositoryUtil2.expectAndReturn("parseCvsRoot", P.args(new IsEqual(cvsRoot)), testCVSRoot2);
        mockCvsRepositoryUtil2.expectAndReturn("parseCvsLogs", new Constraint[]{new IsAnything(), new IsEqual(moduleName), new IsEqual(repositoryPath), new IsEqual(name)}, testCvsContent2);
        final CvsRepository repository2 = new CvsRepository(setupPropertySet(), (CvsRepositoryUtil) mockCvsRepositoryUtil2.proxy());
        repository2.updateRepository();

        final CvsRepository repository3 = new CvsRepository(setupPropertySet(), null);

        final CvsContent cvsContent1 = repository1.getCvsContent();
        CvsContent cvsContent2 = repository2.getCvsContent();
        final CvsContent cvsContent3 = repository3.getCvsContent();

        assertFalse(cvsContent1 == cvsContent2);
        assertFalse(cvsContent2 == cvsContent3);
        assertFalse(cvsContent3 == cvsContent1);

        repository2.copyContent(repository1);
        cvsContent2 = repository2.getCvsContent();
        assertTrue(cvsContent1 == cvsContent2);

        repository2.copyContent(repository3);
        cvsContent2 = repository2.getCvsContent();
        assertTrue(cvsContent2 == cvsContent3);
    }

}
