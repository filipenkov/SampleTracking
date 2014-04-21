package org.jcvi.jira.plugins.customfield.velocity;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.plugin.userformat.UserFormats;
import com.atlassian.jira.user.UserUtils;
import com.atlassian.core.util.DateUtils;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.util.build.BuildUtils;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.JiraKeyUtils;
import com.atlassian.jira.util.JiraUtils;
import com.atlassian.jira.util.JiraVelocityHelper;
import com.atlassian.jira.util.URLCodec;
import com.atlassian.jira.web.bean.PermissionCheckBean;
import com.atlassian.jira.web.util.ExternalLinkUtilImpl;
import com.atlassian.seraph.util.XMLUtils;
import com.opensymphony.util.TextUtils;

import java.util.*;
//AbstractUserProfileFragment.createVelocityParams(User, User)
//DefaultVelocityRequestContextFactory.DefaultVelocityRequestContextFactory(com.atlassian.jira.config.properties.ApplicationProperties)
//ComponentAccessor.getApplicationProperties

/**
 * Created by IntelliJ IDEA.
 * User: pedworth
 * Date: 10/3/11
 * An enumeration of the properties where each property has:
 * <ul>
 *     <li>The key used to add it to the context</li>
 *     <li>A lookup function to retrieve the value for the context</li>
 * </ul>
 * A static class is also included that acts as the environment for
 * the parameters to find their values from. The parameters, grouped by
 * SettingType, are:
 * <h3>ISSUE</h3>
 * <ul>
 *   <li><b>issue</b>
 *          <p>&lt;Issue&gt; the issue object passed in</p></li>
 *   <li><b>fields</b>
 *          <p>Map&lt;String,Object&gt; The fields and their values</p></li>
 * </ul>
 * <h3>CUSTOM_FIELD</h3>
 * <ul>
 *   <li><b>descriptor</b>
 *          <p>&lt;CustomFieldTypeModuleDescriptor&gt; information about rendering the field</p></li>
 *   <li><b>customField</b>
 *          <p>&lt;CustomField&gt; The instance of the CustomFieldType associated with the current field</p></li>
 * </ul>
 * <h3>UTILS</h3>
 * <p>These parameters are from JiraVelocityUtils, and the web-site</p>
 * <ul>
 *   <li>"xmlutils"</li>
 *   <li>"textutils"</li>
 *   <li>"outlookdate"</li>
 *   <li>"dateutils", new DateUtils(authenticationContext.getI18nHelper().getDefaultResourceBundle()));
 *   <li>"authcontext"</li>
 * </ul>
 * <p>These are in JiraVelocityUtils, but not listed on the web-site</p>
 * <ul>
 *   <li>"urlcodec", new JiraUrlCodec());
 *   <li>"currentMillis", System.currentTimeMillis());
 *   <li>"currentCalendar", Calendar.getInstance(authenticationContext.getLocale()));
 *   <li>"externalLinkUtil", ExternalLinkUtilImpl.getInstance());
 *   <li>"webResourceManager", ComponentManager.getInstance().getWebResourceManager());
 *   <li>"userformat", userFormatManager);
 *   <li>"map", new EasyMap());
 *   <li>"permissionCheck", new PermissionCheckBean(authenticationContext,
 * </ul>
 * <p>These were not in JiraVelocityUtils but were listed on the web</p>
 * <ul>
 *   <li>i18n</li>
 *   <li>buildutils</li>
 *   <li>velocityhelper</li>
 *   <li>jirakeyutils</li>
 *   <li>jirautils</li>
 *   <li>userutils</li>
 * </ul>
 * <p>Even more utility classes:</p>
 * <ul>
 *   <li>buildutils</li>
 *   <li>jirakeyutils</li>
 *   <li>jirautils</li>
 *   <li>userutils</li>
 * </ul>
 * <h3>COMPONENT_MANAGER</h3>
 * <ul>
 *   <li>applicationProperties</li>
 *   <li>constantsManager</li>
 *   <li>projectManager</li>
 * </ul>
 */
@SuppressWarnings({"UnusedDeclaration"}) //too much noise from un-referenced but still used enum values
public enum VelocityContextProperties {
    VALUE("value", SettingType.NOT_AUTO_ADDED) {
        @Override
        public Object doGetContextValue(Settings settings) {
            return null;
        }
    },
    VCONFIG("vconfig", SettingType.CONFIG) {
        @Override
        public Object doGetContextValue(Settings settings) {
            return settings.getConfig();
        }
    },
    FIELDS("fields",SettingType.CURRENT_ISSUE) {
        @Override
        public Object doGetContextValue(Settings settings) {
            return getAllFields(settings.getCurrentIssue());
        }
    },
    ISSUE("issue",SettingType.CURRENT_ISSUE) {
        @Override
        public Object doGetContextValue(Settings settings) {
            return settings.getCurrentIssue();
        }
    },
    DESCRIPTOR("descriptor",SettingType.CUSTOM_FIELD) {
        @Override
        public Object doGetContextValue(Settings settings) {
            return settings.getCustomField().getCustomFieldType().getDescriptor();
        }
    },
    CUSTOM_FIELD("customField",SettingType.CUSTOM_FIELD) {
        @Override
        public Object doGetContextValue(Settings settings) {
            return settings.getCustomField();
        }
    },
    OUTLOOKDATE("outlookdate",SettingType.AUTHENTICATION_CONTEXT) {
        @Override
        public Object doGetContextValue(Settings settings) {
            return settings.getAuthenticationContext().getOutlookDate();
        }
    },
    CURRENT_CALENDAR("currentCalendar",SettingType.AUTHENTICATION_CONTEXT) {
        public Object doGetContextValue(Settings settings) {
            return Calendar.getInstance(settings.getAuthenticationContext().getLocale());
        }
    },
    AUTHCONTEXT("authcontext",SettingType.AUTHENTICATION_CONTEXT) {
        @Override
        public Object doGetContextValue(Settings settings) {
            return settings.getAuthenticationContext();
        }
    },
    DATEUTILS("dateutils",SettingType.AUTHENTICATION_CONTEXT) {
        @Override
        public Object doGetContextValue(Settings settings) {
            return new DateUtils(settings.getAuthenticationContext().getI18nHelper().getDefaultResourceBundle());
        }
    },
    PERMISSION_CHECK("permissionCheck",SettingType.AUTHENTICATION_CONTEXT) {
        @Override
        public Object doGetContextValue(Settings settings) {
            return new PermissionCheckBean(settings.getAuthenticationContext(), ComponentManager.getInstance().getPermissionManager());
        }
    },
    I_18_N("i18n", SettingType.AUTHENTICATION_CONTEXT) {
        @Override
        public Object doGetContextValue(Settings settings) {
            return settings.getAuthenticationContext().getI18nHelper();
        }
    },
    APPLICATION_PROPERTIES("applicationProperties", SettingType.COMPONENT_MANAGER) {
        @Override
        public Object doGetContextValue(Settings settings) {
            return settings.getComponentManager().getApplicationProperties();
        }
    },
    CONSTANTS_MANAGER("constantsManager", SettingType.COMPONENT_MANAGER) {
        @Override
        public Object doGetContextValue(Settings settings) {
            return settings.getComponentManager().getConstantsManager();
        }
    },
    PROJECT_MANAGER("projectManager", SettingType.COMPONENT_MANAGER) {
        @Override
        public Object doGetContextValue(Settings settings) {
            return settings.getComponentManager().getProjectManager();
        }
    },
    VELOCITYHELPER("velocityhelper", SettingType.COMPONENT_MANAGER) {
        @Override
        public Object doGetContextValue(Settings settings) {
            //todo: see when they replace this with something
            //ComponentManager.getFieldManager() is deprecated but
            //the suggested replacement ComponentAccessor.getFieldAccessor()
            //isn't compatible
            //noinspection deprecation
            return new JiraVelocityHelper(settings.getComponentManager().getFieldManager());
        }
    },
    BUILDUTILS("buildutils", SettingType.UTILITIES) {
        @Override
        public Object doGetContextValue(Settings settings) {
            return new BuildUtils();
        }
    },
    JIRAKEYUTILS("jirakeyutils", SettingType.UTILITIES) {
        @Override
        public Object doGetContextValue(Settings settings) {
            return new JiraKeyUtils();
        }
    },
    JIRAUTILS("jirautils", SettingType.UTILITIES) {
        @Override
        public Object doGetContextValue(Settings settings) {
            return new JiraUtils();
        }
    },
    USERUTILS("userutils", SettingType.UTILITIES) {
        @Override
        public Object doGetContextValue(Settings settings) {
            return new UserUtils();
        }
    },
    XMLUTILS("xmlutils", SettingType.UTILITIES) {
        @Override
        public Object doGetContextValue(Settings settings) {
            return new XMLUtils();
        }
    },
    TEXTUTILS("textutils", SettingType.UTILITIES) {
        @Override
        public Object doGetContextValue(Settings settings) {
            return new TextUtils();
        }
    },
    URLCODEC("urlcodec", SettingType.UTILITIES) {
        @Override
        public Object doGetContextValue(Settings settings) {
            return new URLCodec();
        }
    },
    //an annoying case, a utility value that shouldn't be cached
    CURRENT_MILLIS("currentMillis", SettingType.UTILITIES) {
        @Override
        public Object doGetContextValue(Settings settings) {
            if (settings.isCacheing()) {
                return null; //don't cache this value
            }
            return System.currentTimeMillis();
        }
    },
    EXTERNAL_LINK_UTIL("externalLinkUtil", SettingType.UTILITIES) {
        @Override
        public Object doGetContextValue(Settings settings) {
            return ExternalLinkUtilImpl.getInstance();
        }
    },
    USERFORMAT("userformat", SettingType.UTILITIES) {
        @Override
        public Object doGetContextValue(Settings settings) {
            //todo: check this works
            return ComponentManager.getComponentInstanceOfType(UserFormats.class);
        }
    },
    MAP("map", SettingType.UTILITIES) {
        @Override
        public Object doGetContextValue(Settings settings) {
            return new EasyMap();
        }
    };

    private final String contextName;
    private final SettingType dependency;
    private final Object cachedValue;
    private VelocityContextProperties(String key, SettingType requires) {
        this.contextName = key;
        this.dependency  = requires;
        dependency.addProperty(this);
        if (requires.isCacheByDefault()) {
            //if it is cacheable then it shouldn't
            //depend on the values of the settings
            //and so a blank settings object should be fine
            Settings nullSettings = new Settings();
            //note it can still avoid being cached by returning null
            //getContextValue is called in case there is some odd
            //reason why a SettingType has a dependency despite
            //being set as cacheable
            cachedValue = getContextValue(nullSettings);
        } else {
            cachedValue = null;
        }
    }
    public String getContextName() {
        return contextName;
    }
    public Object getContextValue(Settings settings) {
        if (cachedValue != null) {
            return cachedValue;
        }
        if (settings != null &&
          dependency.hasSetting(settings)) {
            return doGetContextValue(settings);
        }
        return null;
    }
    abstract Object  doGetContextValue(Settings settings);

    public static enum SettingType {
        CONFIG(false) {
            @Override
            public boolean hasSetting(Settings settings) {
                return settings.getConfig() != null;
            }
        },
        CURRENT_ISSUE(false) {
            @Override
            public boolean hasSetting(Settings settings) {
                return settings.getCurrentIssue() != null;
            }
        },
        CUSTOM_FIELD(false) {
            @Override
            public boolean hasSetting(Settings settings) {
                return settings.getCustomField() != null;
            }
        },
        AUTHENTICATION_CONTEXT(false) {
            @Override
            public boolean hasSetting(Settings settings) {
                return settings.getAuthenticationContext() != null;
            }
        },
        COMPONENT_MANAGER(true) {
            @Override
            public boolean hasSetting(Settings settings) {
                return true;
            }
        },
        UTILITIES(true) {
            @Override
            public boolean hasSetting(Settings settings) {
                return true;
            }
        },
        NOT_AUTO_ADDED(false) {
            @Override
            public boolean hasSetting(Settings settings) {
                return false;
            }
        };
        private final List<VelocityContextProperties> props;
        private final boolean cacheByDefault;
        SettingType(boolean cache) {
            props = new ArrayList<VelocityContextProperties>();
            this.cacheByDefault = cache;
        }
        public abstract boolean hasSetting(Settings settings);
        void addProperty(VelocityContextProperties prop) {
            props.add(prop);
        }
        public VelocityContextProperties[] getAssociatedProperties() {
            return props.toArray(new VelocityContextProperties[props.size()]);
        }
        public boolean isCacheByDefault() {
            return cacheByDefault;
        }
    }

    public static class Settings {
        private final Map<String,String> config;
        private final Issue currentIssue;
        private final CustomField customField;
        private final JiraAuthenticationContext authenticationContext;
        private final boolean cacheing;
        //internal constructor used to create the null settings
        //object used when getting values to cache
        private Settings() {
            //noinspection AssignmentToCollectionOrArrayFieldFromParameter
            this.config                = null;
            this.currentIssue          = null;
            this.customField           = null;
            this.authenticationContext = null;
            cacheing = true;
        }

        public Settings(Map<String,String> conf,
                        Issue issue,
                        CustomField cf,
                        JiraAuthenticationContext auth) {
            //noinspection AssignmentToCollectionOrArrayFieldFromParameter
            this.config                = conf;
            this.currentIssue          = issue;
            this.customField           = cf;
            this.authenticationContext = auth;
            cacheing = false;
        }
        public Map<String,String> getConfig() {
            return config;
        }
        public Issue getCurrentIssue() {
            return currentIssue;
        }
        public CustomField getCustomField() {
            return customField;
        }
        public JiraAuthenticationContext getAuthenticationContext() {
            return authenticationContext;
        }
        public ComponentManager getComponentManager() {
            return ComponentManager.getInstance();
        }
        boolean isCacheing() {
            return cacheing;
        }
    }

    /**
     * <p>Map&lt;String,Object&gt; of field.name -> value and
     * of field.id -> value. The value is whatever type the field uses as a
     * transport object</p>
     *
     * <p>Note: the values are gathered at the time the method is called.
     * They will not reflect any changes made, especially in the
     * presentation layer</p>
     * @param issue Must not be null
     * @return A map containing the above variables
     */
    private static Map<String, Object> getAllFields(Issue issue) {
        //The issue has to have been created or
        //none of the fields will have values , tested by:
	    if (issue == null || !issue.isCreated()) {
            //as the map is empty its type doesn't really matter
            //noinspection unchecked
            return Collections.EMPTY_MAP;//don't add anything
        }

        Map<String,Object> values = new HashMap<String,Object>();
        CustomFieldManager customFieldManager = ComponentAccessor.getCustomFieldManager();

        //Use CustomFieldManager to get the CustomField wanted .
        List<CustomField> fieldsForIssue = customFieldManager.getCustomFieldObjects(issue);
        for (CustomField field : fieldsForIssue) {
            //The CustomField can be accessed by visible name or id number:
            String name = field.getName();
            String id   = field.getId();
            //The value can be accessed through the Issue object.
            Object value = issue.getCustomFieldValue(field);
            if (value != null) {
                if (name != null) {
                    values.put(name,value);
                }
                //I don't think id can be null
                values.put(id,value);
            }
        }
        return values;
    }
}
