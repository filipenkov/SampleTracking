package com.atlassian.jira.config.properties;

public interface SystemPropertyKeys
{
    public static final String JELLY_SYSTEM_PROPERTY = "jira.jelly.on";
    public static final String UPGRADE_SYSTEM_PROPERTY = "non.admin.upgrade";
    public static final String TRACKBACK_SYSTEM_PROPERTY = "jira.trackback.senddisabled";
    public static final String DISABLE_VCS_POLLING_SYSTEM_PROPERTY = "jira.vcs.polldisabled";
    public static final String JIRA_DEV_MODE = "jira.dev.mode";
    public static final String ATLASSIAN_DEV_MODE = "atlassian.dev.mode";
    public static final String DISABLE_BUNDLED_PLUGINS = "jira.plugins.bundled.disable";
    public static final String MAIL_DECODE_PARAMETERS = "mail.mime.decodeparameters";
    public static final String MAIL_DECODE_FILENAME = "mail.mime.decodefilename";
    public static final String XSRF_DETECTION_CHECK = "xsrf.detection.check";
    public static final String JIRA_I18N_RELOADBUNDLES = "jira.i18n.reloadbundles";
    public static final String SUPER_BATCH_DISABLED = "jira.superbatching.disabled";
    public static final String WEBSUDO_IS_DISABLED = "jira.websudo.is.disabled";
    public static final String SHOW_PERF_MONITOR = "jira.show.perf.monitor";
}
