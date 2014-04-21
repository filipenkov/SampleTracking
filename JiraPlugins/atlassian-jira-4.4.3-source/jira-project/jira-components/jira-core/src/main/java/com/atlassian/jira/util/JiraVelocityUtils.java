package com.atlassian.jira.util;

import com.atlassian.core.util.XMLUtils;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.datetime.DateTimeFormatterFactory;
import com.atlassian.jira.datetime.DateTimeStyle;
import com.atlassian.jira.mail.JiraMailQueueUtils;
import com.atlassian.jira.plugin.keyboardshortcut.KeyboardShortcutManager;
import com.atlassian.jira.plugin.profile.UserFormatManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.xsrf.XsrfTokenGenerator;
import com.atlassian.jira.util.collect.CompositeMap;
import com.atlassian.jira.util.collect.MemoizingMap.Master;
import com.atlassian.jira.util.collect.MemoizingMap.Master.Builder;
import com.atlassian.jira.util.velocity.DefaultVelocityRequestContextFactory;
import com.atlassian.jira.util.velocity.VelocityRequestContext;
import com.atlassian.jira.web.ExecutingHttpRequest;
import com.atlassian.jira.web.bean.PermissionCheckBean;
import com.atlassian.jira.web.component.IssueConstantWebComponent;
import com.atlassian.jira.web.util.ExternalLinkUtil;
import com.atlassian.jira.web.util.ExternalLinkUtilImpl;
import com.atlassian.plugin.webresource.UrlMode;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.util.concurrent.LazyReference;
import com.opensymphony.util.TextUtils;
import org.apache.log4j.Logger;
import org.apache.velocity.app.FieldMethodizer;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/** Helper class that contains a number of utility methods for velocity templates. */
public class JiraVelocityUtils
{
    public static final Logger log = Logger.getLogger(JiraVelocityUtils.class);

    private static final Master<String, Object> MASTER;
    static
    {
        final Builder<String, Object> builder = Master.builder();
        builder.add("xmlutils", new XMLUtils());
        builder.add("textutils", new TextUtils());
        builder.add("urlcodec", new JiraUrlCodec());
        builder.add("urlModeAbsolute", UrlMode.ABSOLUTE);
        builder.add("dateTimeStyle", new FieldMethodizer(DateTimeStyle.class.getName()));

        // lazily created items
        builder.addLazy("currentMillis", new Supplier<Long>()
        {
            public Long get()
            {
                return System.currentTimeMillis();
            }
        });
        builder.addLazy("externalLinkUtil", new Supplier<ExternalLinkUtil>()
        {
            public ExternalLinkUtil get()
            {
                return ExternalLinkUtilImpl.getInstance();
            }
        });

        final Supplier<VelocityRequestContext> requestContext = new Supplier<VelocityRequestContext>()
        {
            public VelocityRequestContext get()
            {
                // a request context object should be used instead of using the 'req' object, or 'baseurl' object below. (JRA-11038)
                return new DefaultVelocityRequestContextFactory(ComponentAccessor.getApplicationProperties()).getJiraVelocityRequestContext();
            }
        };

        builder.addLazy("requestContext", requestContext);
        builder.addLazy("baseurl", new Supplier<String>()
        {
            public String get()
            {
                return requestContext.get().getBaseUrl();
            }
        });

        builder.addLazy("issueConstantWebComponent",  new Supplier<IssueConstantWebComponent>() {

            public IssueConstantWebComponent get()
            {
                return new IssueConstantWebComponent(requestContext.get().getBaseUrl());
            }
        });

        builder.addLazy("webResourceManager", new Supplier<WebResourceManager>()
        {
            public WebResourceManager get()
            {
                return ComponentManager.getInstance().getWebResourceManager();
            }
        });
        builder.addLazy("userformat", new Supplier<UserFormatManager>()
        {
            public UserFormatManager get()
            {
                return ComponentManager.getComponentInstanceOfType(UserFormatManager.class);
            }
        });
        builder.addLazy("map", new Supplier<EasyMap>()
        {
            public EasyMap get()
            {
                return new EasyMap();
            }
        });
        builder.addLazy("atl_token", new Supplier<String>()
        {
            public String get()
            {
                return getXsrfToken();
            }
        });
        builder.addLazy("keyboardShortcutManager",new Supplier<KeyboardShortcutManager>()
        {
            public KeyboardShortcutManager get()
            {
                return ComponentManager.getComponentInstanceOfType(KeyboardShortcutManager.class);
            }
        });

        MASTER = builder.master();
    }

    /**
     * Static method to construct a map with a number of common parameters used by velocity templates.
     *
     * @param authenticationContext JiraAuthenticationContext
     * @return a Map with common velocity parameters
     */
    public static Map<String, Object> getDefaultVelocityParams(final JiraAuthenticationContext authenticationContext)
    {
        return getDefaultVelocityParams(new HashMap<String, Object>(), authenticationContext);
    }

    /**
     * Static method to construct a map with a number of common parameters used by velocity templates.
     *
     * @param startingParams        Map of parameters that may be used to override any of the parameters set here.
     * @param authenticationContext JiraAuthenticationContext
     * @return a Map with common velocity parameters
     */
    public static Map<String, Object> getDefaultVelocityParams(Map<String, Object> startingParams, final JiraAuthenticationContext authenticationContext)
    {
        //JRADEV-990: startingParams needs to be a mutable map.
        startingParams = (startingParams == null) ? new HashMap<String, Object>() : startingParams;
        return CompositeMap.of(startingParams, createVelocityParams(authenticationContext));
    }

    public static Map<String, Object> createVelocityParams(final JiraAuthenticationContext authenticationContext)
    {
        // lazy master builder for the parameters
        final Map<String, Object> localParams = new HashMap<String, Object>();

        localParams.put("currentCalendar", new LazyCalendar(authenticationContext.getLocale(),
                ComponentManager.getComponentInstanceOfType(ApplicationProperties.class)));
        localParams.put("authcontext", authenticationContext);
        localParams.put("outlookdate", authenticationContext.getOutlookDate());
        localParams.put("dateFormatter", dateTimeFormatterFactory().formatter().withStyle(DateTimeStyle.COMPLETE).forLoggedInUser());
        localParams.put("systemDateFormatter", dateTimeFormatterFactory().formatter().withSystemZone().withStyle(DateTimeStyle.RELATIVE_WITHOUT_TIME));
        localParams.put("rssDateTimeFormatter", dateTimeFormatterFactory().formatter().withStyle(DateTimeStyle.RSS_RFC822_DATE_TIME).forLoggedInUser());
        localParams.put("rssDateFormatter", dateTimeFormatterFactory().formatter().withStyle(DateTimeStyle.RSS_RFC822_DATE_TIME).withSystemZone());
        localParams.put("dateutils", new DateUtils(authenticationContext));
        // an bean to help with concise permission checks (done as part of JRA-13469) but needed in general
        localParams.put("permissionCheck", new PermissionCheckBean(authenticationContext, ComponentAccessor.getPermissionManager()));

        if (ExecutingHttpRequest.get() != null)
        {
            localParams.put("req", ExecutingHttpRequest.get());
        }

        return MASTER.combine(JiraMailQueueUtils.getContextParamsMaster()).toMap(localParams);
    }

    private static DateTimeFormatterFactory dateTimeFormatterFactory()
    {
        return ComponentManager.getComponentInstanceOfType(DateTimeFormatterFactory.class);
    }

    private static String getXsrfToken()
    {
        return getXsrfTokenGenerator().generateToken();
    }

    private static XsrfTokenGenerator getXsrfTokenGenerator()
    {
        return ComponentManager.getComponentInstanceOfType(XsrfTokenGenerator.class);
    }

    public static class LazyCalendar
    {
        private final LazyReference<Calendar> reference;
        private final ApplicationProperties applicationProperties;

        LazyCalendar(final Locale locale, final ApplicationProperties applicationProperties)
        {
            this.applicationProperties = applicationProperties;
            reference = new LazyReference<Calendar>()
            {
                @Override
                protected Calendar create() throws Exception
                {
                    return Calendar.getInstance(locale);
                }
            };
        }

        public int getFirstDayOfWeek()
        {
            return reference.get().getFirstDayOfWeek();
        }

        public boolean isUseISO8061()
        {
            return applicationProperties.getOption(APKeys.JIRA_DATE_TIME_PICKER_USE_ISO8061);
        }
    }

    /**
     * Date utils class.
     */
    static class DateUtils extends com.atlassian.core.util.DateUtils
    {
        public DateUtils(JiraAuthenticationContext authenticationContext)
        {
            super(authenticationContext.getI18nHelper().getDefaultResourceBundle());
        }

        /**
         * This method is used to instantiate a Date in a Velocity template.
         *
         * @param currentMillis a long
         * @return a new Date
         */
        public Date date(long currentMillis)
        {
            return new Date(currentMillis);
        }
    }
}
