/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.vcs;

import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.jira.vcs.Repository;
import com.atlassian.jira.vcs.RepositoryManager;
import com.atlassian.jira.vcs.cvsimpl.CvsRepository;
import com.atlassian.jira.vcs.cvsimpl.CvsRepositoryUtil;
import com.atlassian.jira.vcs.viewcvs.ViewCvsBrowser;
import com.mockobjects.constraint.Constraint;
import com.mockobjects.constraint.IsAnything;
import com.mockobjects.constraint.IsEqual;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.module.propertyset.map.MapPropertySet;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.netbeans.lib.cvsclient.CVSRoot;
import org.ofbiz.core.entity.GenericEntityException;
import webwork.action.Action;

import java.io.File;
import java.io.IOException;

public class TestRepositoryTest extends LegacyJiraMockTestCase
{
    private RepositoryTest rt;
    private Long id = new Long(10000);
    private String name = "Test CVS Repository";
    private String description = "Test-cvs-description for the repository";
    private String cvsLogFilePath = System.getProperty("java.io.tempdir") + File.separator + "cvs.log";
    private String moduleName = "testcvsmodule";
    private String cvsRoot = ":pserver:test@testserver:/root/test";
    private String password = "test-cvs-password";
    private boolean fetchLog = true;
    private String baseURL = "http://test-cvs-baseURL";
    private String repositoryBroswerType = "VIEW_CVS";
    private String viewCvsRootParameter = "mymoduleroot";
    private String timeout = "10";
    private Long timeoutLong = new Long(timeout);

    public TestRepositoryTest(String s)
    {
        super(s);
    }

    public void testDoValidationNoRepositoryId() throws Exception
    {
        rt = new RepositoryTest(null, null);

        final String result = rt.execute();
        assertEquals(Action.SUCCESS, result);
        assertEquals("Please specifiy a Repository to test.", rt.getMessage());
    }

    public void testDoValidationNoRepository() throws Exception
    {
        Mock mockRepositoryManager = new Mock(RepositoryManager.class);
        mockRepositoryManager.setStrict(true);

        final Long id = new Long(1);
        mockRepositoryManager.expectAndReturn("getRepository", P.args(new IsEqual(id)), null);

        rt = new RepositoryTest((RepositoryManager) mockRepositoryManager.proxy(), null);
        rt.setId(id);

        final String result = rt.execute();
        assertEquals(Action.SUCCESS, result);

        assertEquals("Could not retrieve repository with id '" + id + "'.", rt.getMessage());
        mockRepositoryManager.verify();
    }


    public void testDoValidationInvalidRepository() throws Exception
    {
        Mock mockRepositoryManager = new Mock(RepositoryManager.class);
        mockRepositoryManager.setStrict(true);

        final Long id = new Long(1);
        mockRepositoryManager.expectAndThrow("getRepository", P.args(new IsEqual(id)), new GenericEntityException());
        rt = new RepositoryTest((RepositoryManager) mockRepositoryManager.proxy(), null);
        rt.setId(new Long(1));

        final String result = rt.execute();
        assertEquals(Action.SUCCESS, result);
        assertEquals("Error occurred while retrieving repository with id '" + id + "'.", rt.getMessage());
        mockRepositoryManager.verify();
    }

    public void testDoValidationInvalidRepositoryType() throws Exception
    {
        Mock mockRepositoryManager = new Mock(RepositoryManager.class);
        mockRepositoryManager.setStrict(true);

        Mock mockRepository = new Mock(Repository.class);
        mockRepository.setStrict(true);
        mockRepository.expectAndReturn("getType", "Some type");

        final Long id = new Long(1);
        mockRepositoryManager.expectAndReturn("getRepository", P.args(new IsEqual(id)), mockRepository.proxy());
        rt = new RepositoryTest((RepositoryManager) mockRepositoryManager.proxy(), null);
        rt.setId(new Long(1));

        final String result = rt.execute();
        assertEquals(Action.SUCCESS, result);
        assertEquals("Unknown repository type.", rt.getMessage());
        mockRepositoryManager.verify();
        mockRepository.verify();
    }

    public void testDoValidationParseLog() throws Exception
    {
        final String message = "Something bad happened";
        final Exception exception = new IOException(message);

        Mock mockCvsRepositoryUtil = new Mock(CvsRepositoryUtil.class);
        mockCvsRepositoryUtil.setStrict(true);

        mockCvsRepositoryUtil.expectVoid("checkLogFilePath", P.args(new IsAnything(), new IsEqual(Boolean.valueOf(fetchLog))));
        mockCvsRepositoryUtil.expectVoid("checkCvsRoot", P.args(new IsEqual(cvsRoot)));
        mockCvsRepositoryUtil.expectVoid("updateCvs", new Constraint[]{new IsAnything(), new IsEqual(cvsRoot), new IsEqual(moduleName), new IsEqual(password), new IsEqual(timeoutLong)});
        CVSRoot root = CVSRoot.parse(cvsRoot);
        mockCvsRepositoryUtil.expectAndReturn("parseCvsRoot", P.args(new IsEqual(cvsRoot)), root);
        mockCvsRepositoryUtil.expectAndThrow("parseCvsLogs", new Constraint[]{new IsAnything(), new IsEqual(moduleName), new IsEqual(root.getRepository()), new IsEqual(name)}, exception);

        CvsRepository repository = new CvsRepository(setupPropertySet(), (CvsRepositoryUtil) mockCvsRepositoryUtil.proxy());

        Mock mockRepositoryManager = new Mock(RepositoryManager.class);
        mockRepositoryManager.setStrict(true);
        mockRepositoryManager.expectAndReturn("getRepository", P.args(new IsEqual(id)), repository);

        rt = new RepositoryTest((RepositoryManager) mockRepositoryManager.proxy(), (CvsRepositoryUtil) mockCvsRepositoryUtil.proxy());
        rt.setId(id);

        final String result = rt.execute();
        assertEquals(Action.SUCCESS, result);
        assertEquals(message + "\n" + ExceptionUtils.getStackTrace(exception), rt.getMessage());

        mockCvsRepositoryUtil.verify();
        mockRepositoryManager.verify();
    }

    public void testDoExecute() throws Exception
    {
        Mock mockCvsRepositoryUtil = new Mock(CvsRepositoryUtil.class);
        mockCvsRepositoryUtil.setStrict(true);

        mockCvsRepositoryUtil.expectVoid("checkLogFilePath", P.args(new IsAnything(), new IsEqual(Boolean.valueOf(fetchLog))));
        mockCvsRepositoryUtil.expectVoid("checkCvsRoot", P.args(new IsEqual(cvsRoot)));
        mockCvsRepositoryUtil.expectVoid("updateCvs", new Constraint[]{new IsAnything(), new IsEqual(cvsRoot), new IsEqual(moduleName), new IsEqual(password), new IsEqual(timeoutLong)});
        CVSRoot root = CVSRoot.parse(cvsRoot);
        mockCvsRepositoryUtil.expectAndReturn("parseCvsRoot", P.args(new IsEqual(cvsRoot)), root);
        mockCvsRepositoryUtil.expectAndReturn("parseCvsLogs", new Constraint[]{new IsAnything(), new IsEqual(moduleName), new IsEqual(root.getRepository()), new IsEqual(name)}, null);

        CvsRepository repository = new CvsRepository(setupPropertySet(), (CvsRepositoryUtil) mockCvsRepositoryUtil.proxy());

        Mock mockRepositoryManager = new Mock(RepositoryManager.class);
        mockRepositoryManager.setStrict(true);
        mockRepositoryManager.expectAndReturn("getRepository", P.args(new IsEqual(id)), repository);

        rt = new RepositoryTest((RepositoryManager) mockRepositoryManager.proxy(), (CvsRepositoryUtil) mockCvsRepositoryUtil.proxy());
        rt.setId(id);

        final String result = rt.execute();
        assertEquals(Action.SUCCESS, result);
        assertNull(rt.getMessage());

        mockCvsRepositoryUtil.verify();
        mockRepositoryManager.verify();
    }

    private PropertySet setupPropertySet()
    {
        PropertySet ps = new MapPropertySet()
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
                    return timeout;
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
        return ps;
    }
}
