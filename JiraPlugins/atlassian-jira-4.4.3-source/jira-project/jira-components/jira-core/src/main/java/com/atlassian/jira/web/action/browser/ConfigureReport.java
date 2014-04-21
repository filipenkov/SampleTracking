package com.atlassian.jira.web.action.browser;

import com.atlassian.configurable.ObjectConfiguration;
import com.atlassian.configurable.ObjectConfigurationException;
import com.atlassian.jira.plugin.report.Report;
import com.atlassian.jira.plugin.report.ReportModuleDescriptor;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.util.JiraUrlCodec;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.web.action.ProjectActionSupport;
import com.atlassian.plugin.PluginAccessor;
import org.apache.commons.lang.StringUtils;
import webwork.action.ActionContext;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;

public class ConfigureReport extends ProjectActionSupport
{
    private static final String EXCEL_VIEW = "excel";

    private final PluginAccessor pluginAccessor;
    private String reportKey;
    private ReportModuleDescriptor descriptor;
    private ObjectConfiguration oc;
    private String generatedReport;
    private Report report;

    public ConfigureReport(ProjectManager projectManager, PermissionManager permissionManager, PluginAccessor pluginAccessor)
    {
        super(projectManager, permissionManager);
        this.pluginAccessor = pluginAccessor;
    }

    public String getParamValue(final String key)
    {
        final Map<String, Object> inputParams = makeReportParams();
        String value = (String) inputParams.get(key);
        if (value == null)
        {
            try
            {
                value = getObjectConfiguration().getFieldDefault(key);
            }
            catch (ObjectConfigurationException objectConfigurationException)
            {
                if (log.isDebugEnabled())
                {
                    log.debug(
                            format("The configuration property with the key: %s could not be found for the "
                                    + "report module descriptor with the key: %s", key, reportKey),
                            objectConfigurationException
                    );
                }
            }
        }
        return value;
    }

    public List getParamValues(String key)
    {
        final Map<String, Object> inputParams = makeReportParams();
        Object values = inputParams.get(key);
        if (values == null)
        {
            try
            {
                values = getObjectConfiguration().getFieldDefault(key);
            }
            catch (ObjectConfigurationException objectConfigurationException)
            {
                if (log.isDebugEnabled())
                {
                    log.debug(
                            format("The configuration property with the key: %s could not be found for the "
                                    + "report module descriptor with the key: %s", key, reportKey),
                            objectConfigurationException
                    );
                }
                return Collections.emptyList();
            }
        }
        else if (values instanceof String[])
        {
            return Arrays.asList((String[])values);
        }

        return Arrays.asList(values.toString());
    }

    public String doDefault() throws Exception
    {
        //JRA-13939: Need to be null safe here, as some crawlers may be too stupid to submit a &amp; in a URL and will
        //submit an invalid reportKey param
        if (!validReportKey())
        {
            return "noreporterror";
        }
        return super.doDefault();
    }

    protected String doExecute() throws Exception
    {
        //JRA-13939: Need to be null safe here, as some crawlers may be too stupid to submit a &amp; in a URL and will
        //submit an invalid reportKey param
        if (!validReportKey())
        {
            return "noreporterror";
        }
        getReportModule().validate(this, makeReportParams());
        if (invalidInput())
        {
            return INPUT;
        }
        generatedReport = getReportModule().generateReportHtml(this, makeReportParams());
        return SUCCESS;
    }

    //JRA-13939: Need to be null safe here, as some crawlers may be too stupid to submit a &amp; in a URL and will
    //submit an invalid reportKey param
    private boolean validReportKey()
    {
        if (StringUtils.isEmpty(getReportKey()))
        {
            addErrorMessage(getText("report.configure.error.no.report.key"));
            return false;
        }
        return true;
    }

    public String doExcelView() throws Exception
    {
        generatedReport = getReportModule().generateReportExcel(this, makeReportParams());
        return EXCEL_VIEW;
    }

    /**
     * Makes report params from action params.
     *
     * @return a map of report parameters
     */
    private Map<String, Object> makeReportParams()
    {
        @SuppressWarnings ({ "unchecked" })
        Map<String, String[]> params = ActionContext.getParameters();
        Map<String, Object> reportParams = new LinkedHashMap<String, Object>(params.size());

        for (final Map.Entry entry : params.entrySet())
        {
            final String key = (String) entry.getKey();
            if (((String[]) entry.getValue()).length == 1)
            {
                reportParams.put(key, ((String[]) entry.getValue())[0]);
            }
            else
            {
                reportParams.put(key, entry.getValue());
            }
        }
        return reportParams;
    }

    public String getQueryString()
    {
        final Map<String, Object> params = makeReportParams();
        StringBuffer stringBuffer = new StringBuffer();
        boolean isFirstKey = true;

        for (final String key : params.keySet())
        {
            Object value = params.get(key);
            if (value instanceof String)
            {
                isFirstKey = appendUrlParameter(isFirstKey, key, (String) value, stringBuffer);
            }
            else if (value instanceof String[])
            {
                for (int i = 0; i < ((String[]) value).length; i++)
                {
                    String s = ((String[]) value)[i];
                    isFirstKey = appendUrlParameter(isFirstKey, key, s, stringBuffer);
                }
            }
        }

        return stringBuffer.toString();
    }

    private boolean appendUrlParameter(final boolean firstKey, String key, String value, StringBuffer stringBuffer)
    {
        if (firstKey)
        {
            stringBuffer.append(encode(key)).append("=").append(encode(value));
        }
        else
        {
            stringBuffer.append("&").append(encode(key)).append("=").append(encode(value));
        }
        return false;
    }

    private String encode(final String key)
    {
        return JiraUrlCodec.encode(key);
    }

    private Report getReportModule()
    {
        if (report == null)
        {
            report = getReport().getModule();
        }

        return report;
    }

    public String getGeneratedReport()
    {
        return generatedReport;
    }

    public String getReportKey()
    {
        return reportKey;
    }

    public void setReportKey(String reportKey)
    {
        this.reportKey = reportKey;
    }

    public ReportModuleDescriptor getReport()
    {
        if (descriptor == null)
        {
            descriptor = (ReportModuleDescriptor) pluginAccessor.getEnabledPluginModule(reportKey);
        }

        return descriptor;
    }

    public ObjectConfiguration getObjectConfiguration() throws ObjectConfigurationException
    {
        if (oc == null)
        {
            final Map objectConfigurationParameters =
                    MapBuilder.build
                            (
                                    "project", getSelectedProject(),
                                    "User", getRemoteUser()
                            );
            oc = getReport().getObjectConfiguration(objectConfigurationParameters);
        }

        return oc;
    }
}