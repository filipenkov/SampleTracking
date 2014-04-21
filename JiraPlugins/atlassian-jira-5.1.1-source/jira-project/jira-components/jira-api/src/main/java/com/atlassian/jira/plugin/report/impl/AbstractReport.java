/*
 * Created by IntelliJ IDEA.
 * User: Mike
 * Date: Aug 3, 2004
 * Time: 9:36:14 AM
 */
package com.atlassian.jira.plugin.report.impl;

import com.atlassian.annotations.PublicSpi;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.IssueConstant;
import com.atlassian.jira.plugin.report.Report;
import com.atlassian.jira.plugin.report.ReportModuleDescriptor;
import com.atlassian.jira.web.action.ProjectActionSupport;
import org.ofbiz.core.entity.GenericValue;

import java.util.Map;

@PublicSpi
public abstract class AbstractReport implements Report
{
    protected ReportModuleDescriptor descriptor;

    public void init(ReportModuleDescriptor descriptor)
    {
        this.descriptor = descriptor;
    }

    public void validate(ProjectActionSupport action, Map params)
    {
    }

    /**
     * By default, reports do not have an Excel view.
     */
    public boolean isExcelViewSupported()
    {
        return false;
    }

    /**
     * By default, will throw an UnsuppportedOperationException as reports don't support Excel by default.
     */
    public String generateReportExcel(ProjectActionSupport action, Map params) throws Exception
    {
        throw new UnsupportedOperationException("This report does not support an Excel view.");
    }

    /**
     * Whether or not to show this report in the interface. The default is "true".
     */ 
    public boolean showReport()
    {
        return true;
    }

    protected ReportModuleDescriptor getDescriptor()
    {
        return descriptor;
    }

    public IssueConstant getIssueConstant(GenericValue issueConstantGV)
    {
        return ComponentAccessor.getConstantsManager().getIssueConstant(issueConstantGV);
    }
}