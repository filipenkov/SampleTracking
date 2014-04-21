/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.setup;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.dataimport.DataImportParams;
import com.atlassian.jira.bc.dataimport.DataImportService;
import com.atlassian.jira.bc.license.JiraLicenseService;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.util.IndexPathManager;
import com.atlassian.jira.license.LicenseDetails;
import com.atlassian.jira.license.LicenseJohnsonEventRaiser;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.jira.task.TaskProgressSink;
import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.web.util.ExternalLinkUtil;
import com.atlassian.johnson.JohnsonEventContainer;
import com.mockobjects.constraint.IsAnything;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;
import org.easymock.classextension.EasyMock;
import webwork.action.Action;
import webwork.action.ServletActionContext;

import javax.servlet.ServletContext;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import java.io.File;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.verify;

public class TestSetupImport extends LegacyJiraMockTestCase
{
    public static final String FILENAME = "filename";
    public static final String PATH_NAME = "/etc/monkey/";

    private SetupImport si;
    private ExternalLinkUtil externalLinkUtil;
    private BuildUtilsInfo buildUtilsInfo;
    private IndexPathManager indexPathManager;
    private DataImportService dataImportService;

    public TestSetupImport(final String s)
    {
        super(s);
    }

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        indexPathManager = createMock(IndexPathManager.class);
        externalLinkUtil = createMock(ExternalLinkUtil.class);
        buildUtilsInfo = createMock(BuildUtilsInfo.class);
        dataImportService = createMock(DataImportService.class);

        si = new SetupImport(this.indexPathManager, externalLinkUtil, buildUtilsInfo, fileFactory, dataImportService, null, null);
    }

    public void testGetSets() throws Exception
    {
        assertNull(si.getFilename());

        si.setFilename(FILENAME);
        assertEquals(FILENAME, si.getFilename());
    }

    public void testDoDefault() throws Exception
    {
        si.getApplicationProperties().setString(APKeys.JIRA_SETUP, "true");
        assertEquals("setupalready", si.doDefault());

        si.getApplicationProperties().setString(APKeys.JIRA_SETUP, null);
        assertEquals(Action.INPUT, si.doDefault());
    }

    public void testDoValidation() throws Exception
    {
        si.setFilename("");
        final ErrorCollection errors = new SimpleErrorCollection();
        errors.addError(FILENAME, "Could not find file at this location.");
        expect(dataImportService.validateImport(EasyMock.<User>anyObject(), EasyMock.<DataImportParams>anyObject())).andReturn(
                new DataImportService.ImportValidationResult(errors, null));

        replay(dataImportService);
        assertEquals(Action.INPUT, si.execute());
        assertEquals("Could not find file at this location.", si.getErrors().get(FILENAME));

        verify(dataImportService);
    }

    public void testExecuteSetupAlready() throws Exception
    {
        setUpServletContext();
        setAllValidData(si);
        si.getApplicationProperties().setString(APKeys.JIRA_SETUP, "true");
        assertEquals("setupalready", si.execute());
    }

    private void setUpServletContext()
    {
        final Mock mockServletContext = new Mock(ServletContext.class);
        final ServletContext servletContext = (ServletContext) mockServletContext.proxy();
        ServletActionContext.setServletContext(servletContext);

        final JohnsonEventContainer appEventContainer = new JohnsonEventContainer();
        mockServletContext.expectAndReturn("getAttribute", P.args(new IsAnything()), appEventContainer);
    }

    private void setAllValidData(final SetupImport si)
    {
        setAllValidData(si, true);
    }

    private void setAllValidData(final SetupImport si, boolean fileExists)
    {
        File file = createMock(File.class);
        expect(file.exists()).andReturn(fileExists);
        replay(file);

        File path = createMock(File.class);
        expect(path.exists()).andReturn(true);
        expect(path.isDirectory()).andReturn(true);
        expect(path.isAbsolute()).andReturn(true);
        expect(path.canWrite()).andReturn(true);
        expect(path.mkdirs()).andReturn(true);
        replay(path);

        expect(fileFactory.getFile(PATH_NAME)).andReturn(path);
        expect(fileFactory.getFile(FILENAME)).andReturn(file);
        replay(fileFactory);

        si.setFilename(FILENAME);
    }
}
