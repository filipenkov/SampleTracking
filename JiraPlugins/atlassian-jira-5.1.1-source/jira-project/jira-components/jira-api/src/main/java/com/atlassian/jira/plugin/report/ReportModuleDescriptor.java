/*
 * Created by IntelliJ IDEA.
 * User: Mike
 * Date: Jul 28, 2004
 * Time: 11:01:00 AM
 */
package com.atlassian.jira.plugin.report;

import com.atlassian.jira.plugin.ConfigurableModuleDescriptor;
import com.atlassian.jira.plugin.JiraResourcedModuleDescriptor;

/**
 * The report plugin allows end users to write pluggable reports for JIRA.
 *
 * @see Report
 */
//@RequiresRestart
public interface ReportModuleDescriptor extends JiraResourcedModuleDescriptor<Report>, ConfigurableModuleDescriptor
{
    public Report getModule();

    public String getLabel();

    public String getLabelKey();
}
