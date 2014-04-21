package com.atlassian.jira.collector.plugin.components;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.json.JSONArray;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.jira.util.json.JSONObject;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class ErrorLogImpl implements ErrorLog
{
    private static final Logger log = Logger.getLogger(ErrorLogImpl.class);
    private static final String ERROR_KEY = "errors";

    private final PluginSettingsFactory pluginSettingsFactory;
    private final I18nHelper.BeanFactory beanFactory;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    public static final int MAX_LOGS = 30;

    public ErrorLogImpl(final PluginSettingsFactory pluginSettingsFactory, final I18nHelper.BeanFactory beanFactory)
    {
        this.pluginSettingsFactory = pluginSettingsFactory;
        this.beanFactory = beanFactory;
    }

    @Override
    public void logError(final Project project, final String collectorId, final String fullName, final String email, final String sourceUrl, final ErrorType type)
    {
        final PluginSettings settings = getPluginSettings(project);
        synchronized (this)
        {
            final String errorString = (String) settings.get(ERROR_KEY);
            try
            {
                final JSONArray errors = StringUtils.isBlank(errorString) ? new JSONArray() : new JSONArray(errorString);
                JSONArray splicedErrors = new JSONArray();
                if (errors.length() > MAX_LOGS)
                {
                    for (int i = 1; i < errors.length(); i++)
                    {
                        splicedErrors.put(errors.get(i));
                    }
                }
                else
                {
                    splicedErrors = errors;
                }

                final JSONObject json = new JSONObject();
                json.put("collectorId", collectorId).put("timestamp", System.currentTimeMillis()).
                        put("fullName", fullName).put("email", email).put("sourceUrl", sourceUrl).put("type", type.toString());
                splicedErrors.put(json);
                settings.put(ERROR_KEY, splicedErrors.toString());
            }
            catch (JSONException e)
            {
                log.error("Error storing error log for '" + collectorId + "'", e);
            }
        }
    }

    @Override
    public List<String> getFormattedErrors(final Project project, final User remoteUser)
    {
        final List<String> ret = new ArrayList<String>();
        final PluginSettings settings = getPluginSettings(project);
        final String errorString = (String) settings.get(ERROR_KEY);
        if (StringUtils.isBlank(errorString))
        {
            return ret;
        }

        try
        {
            final I18nHelper i18n = beanFactory.getInstance(remoteUser);
            final JSONArray errors = new JSONArray(errorString);
            for (int i = 0; i < errors.length(); i++)
            {
                final JSONObject error = errors.getJSONObject(i);
                ErrorType type = ErrorType.valueOf(error.getString("type"));
                String user = i18n.getText("common.words.anonymous");
                if (error.has("fullName") && StringUtils.isNotBlank(error.getString("fullName")))
                {
                    user = error.getString("fullName");
                    if (error.has("email"))
                    {
                        user += " (" + error.getString("email") + ")";
                    }
                }
                String sourceUrl = error.has("sourceUrl") ? error.getString("sourceUrl") : "unknown source";
                ret.add("[" + dateFormat.format(new Date(error.getLong("timestamp"))) + "] " +
                        i18n.getText("collector.plugin.error.log." + type.toString(), error.getString("collectorId"), sourceUrl, user));
            }
        }
        catch (JSONException e)
        {
            log.error("Error retrieving collector errors!", e);
        }

        //return most recent first!
        Collections.reverse(ret);
        return ret;
    }

    @Override
    public void clearErrors(final Project project)
    {
        final PluginSettings pluginSettings = getPluginSettings(project);
        synchronized (this)
        {
            pluginSettings.remove(ERROR_KEY);
        }
    }

    private PluginSettings getPluginSettings(final Project project)
    {
        return pluginSettingsFactory.createSettingsForKey(String.format("%s.%s",
                ErrorLogImpl.class.getSimpleName(), project == null ? "__GLOBAL__" : project.getKey()));
    }
}
