package com.atlassian.jira.timezone;

import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.junit.rules.InitMockitoMocks;
import com.atlassian.jira.plugin.language.TranslationTransform;
import com.atlassian.jira.plugin.util.InvolvedPluginsTracker;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.user.preferences.UserPreferencesManager;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.jira.web.bean.BackingI18n;
import com.atlassian.jira.web.bean.MockI18nTranslationMode;
import com.atlassian.plugin.PluginAccessor;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.joda.time.DateTimeZone;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;

import static org.junit.Assert.fail;

public class TestTimeZoneServiceI18n
{
    @Mock ApplicationProperties applicationProperties;
    @Mock PermissionManager permissionManager;
    @Mock UserPreferencesManager userPreferencesManager;

    @Rule public InitMockitoMocks intiMocks = new InitMockitoMocks(this);

    I18nHelper i18nHelper;

    private List<TranslationTransform> translationTransforms = Collections.emptyList();

    @Before
    public void setUp() throws Exception
    {
        i18nHelper = createI18nBean(new Locale("en-AU"));
    }

    @Test
    public void allSupportedTimeZoneCitiesShouldHaveTranslations() throws Exception
    {
        TimeZoneServiceImpl tzs = new TimeZoneServiceImpl(applicationProperties, permissionManager, userPreferencesManager, new JodaCanonicalIdsProvider());

        StringBuilder sb = new StringBuilder();
        List<TimeZoneInfo> untranslated = Lists.newArrayList();

        ErrorCollection errors = new SimpleErrorCollection();
        List<TimeZoneInfo> infos = tzs.getTimeZoneInfos(new JiraServiceContextImpl(new MockUser("tester"), errors, i18nHelper));
        for (TimeZoneInfo info : infos)
        {
            if (!info.getCity().startsWith("timezone."))
            {
                continue;
            }

            untranslated.add(info);

            String suggested = info.getTimeZoneId();
            int lastSlash = info.getTimeZoneId().lastIndexOf("/");
            if (lastSlash != -1)
            {
                suggested = info.getTimeZoneId().substring(lastSlash + 1).replace('_', ' ');
            }

            // build up the suggested property value
            sb.append("\n").append(info.getCity()).append("=").append(suggested);
        }

        if (!untranslated.isEmpty())
        {
            fail(String.format("Missing translations for %d cities. Suggested property values : %s", untranslated.size(), sb.toString()));
        }
    }

    private I18nHelper createI18nBean(Locale locale)
    {
        return new BackingI18n(locale, new MockI18nTranslationMode(), new InvolvedPluginsTracker(), translationTransforms)
        {
            @Override
            public ResourceBundle getDefaultResourceBundle()
            {
                return ResourceBundle.getBundle(JiraWebActionSupport.class.getName());
            }

            @Override
            protected PluginAccessor getPluginAccessor()
            {
                return null;
            }
        };
    }

    private static class JodaCanonicalIdsProvider extends TimeZoneIds
    {
        @Override
        public Set<String> getCanonicalIds()
        {
            return Sets.filter(DateTimeZone.getAvailableIDs(), new Predicate<String>()
            {
                @Override
                public boolean apply(String id)
                {
                    // only return canonical id's
                    return id.equals(DateTimeZone.forID(id).getID());
                }
            });
        }
    }
}
