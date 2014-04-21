package com.atlassian.jira.web.action.admin;

import com.atlassian.gadgets.dashboard.Color;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.config.properties.LookAndFeelBean;
import com.atlassian.jira.plugin.profile.UserFormatManager;
import com.atlassian.jira.plugin.userformat.UserFormatModuleDescriptor;
import com.atlassian.jira.plugin.userformat.configuration.UserFormatTypeConfiguration;
import com.atlassian.jira.plugin.userformat.descriptors.UserFormatModuleDescriptors;
import com.atlassian.jira.plugin.userformat.descriptors.UserFormatTypes;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.action.ProjectActionSupport;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import org.apache.commons.lang.StringUtils;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.atlassian.jira.user.util.Users.isAnonymous;

@WebSudoRequired
public class ViewLookAndFeel extends ProjectActionSupport
{
    LookAndFeelBean lookAndFeelBean = null;
    private final UserFormatManager userFormatManager;
    private final UserFormatTypeConfiguration userFormatTypeConfiguration;
    private final BeanFactory i18nBeanFactory;
    private final UserFormatModuleDescriptors userFormatModuleDescriptors;
    private final UserFormatTypes userFormatTypes;

    public ViewLookAndFeel(final UserFormatManager userFormatManager, final UserFormatTypeConfiguration userFormatTypeConfiguration,
            final BeanFactory i18nBeanFactory, final UserFormatModuleDescriptors userFormatModuleDescriptors,
            final UserFormatTypes userFormatTypes)
    {
        this.userFormatManager = userFormatManager;
        this.userFormatTypeConfiguration = userFormatTypeConfiguration;
        this.i18nBeanFactory = i18nBeanFactory;
        this.userFormatModuleDescriptors = userFormatModuleDescriptors;
        this.userFormatTypes = userFormatTypes;
    }

    public Timestamp getCurrentTimestamp()
    {
        return new Timestamp(System.currentTimeMillis());
    }

    public LookAndFeelBean getLookAndFeelBean()
    {
        if (lookAndFeelBean == null)
        {
            lookAndFeelBean = LookAndFeelBean.getInstance(getApplicationProperties());
        }
        return lookAndFeelBean;
    }

    public boolean isShowInvisibleWarningForTopText()
    {
        final LookAndFeelBean lookAndFeelBean = getLookAndFeelBean();
        String topBgColour = lookAndFeelBean.getTopBackgroundColour();
        String topTextColour = lookAndFeelBean.getTopTxtColour();
        if (topBgColour == null && topTextColour == null)
        {
            return false;
        }

        return (topBgColour != null && topBgColour.equalsIgnoreCase(topTextColour));

    }

    public String getColor(String landFKeyName)
    {
        final ApplicationProperties ap = getApplicationProperties();
        String colorValue = ap.getString(landFKeyName);
        final String defaultColorValue = ap.getDefaultString(landFKeyName);
        String colorStr = colorValue;
        if (colorValue == null || StringUtils.equals(colorValue, defaultColorValue))
        {
            // its not backed and is the default
            colorStr = "<" + getText("common.words.default") + ">";
            colorValue = ap.getDefaultBackedString(landFKeyName);
        }
        StringBuilder sb = new StringBuilder();
        sb.append("<table cellpadding=\"0\" cellspacing=\"0\"><tr>")
                .append("<td><table cellspacing=\"1\" cellpadding=\"0\" border=\"1\"><tr><td><div style=\"width:18px;height:12px;font-size:0px;background-color:" + htmlEncode(colorValue) + ";\"></div></td></tr></table></td>")
                .append("<td>&nbsp;")
                .append(htmlEncode(colorStr))
                .append("</td>")
                .append("</tr></table>");

        return sb.toString();
    }

    public List<Color> getGadgetColors()
    {
        final List<Color> colors = new ArrayList<Color>();
        for (Color color : Color.values())
        {
            //color 8 is special.  It's the chromeless mode.
            if(!color.equals(Color.color8))
            {
                colors.add(color);
            }
        }
        return colors;
    }

    public String getGadgetColor(Color colorId)
    {
        return getColor(APKeys.JIRA_LF_GADGET_COLOR_PREFIX + colorId);
    }

    public String getUserFormatTypeDesc(String type)
    {
        if (StringUtils.isNotBlank(type))
        {
            final ModuleDescriptor descriptor = userFormatModuleDescriptors.withKey(userFormatTypeConfiguration.getUserFormatKeyForType(type));
            if(descriptor != null)
            {
                final String descriptionKey = descriptor.getDescriptionKey();
                if(StringUtils.isNotEmpty(descriptionKey))
                {
                    return getI18n().getText(descriptionKey);
                }
                return descriptor.getDescription();
            }
        }
        return null;
    }

    public String getUserFormatTypeName(String type)
    {
        final UserFormatModuleDescriptor descriptor = userFormatModuleDescriptors.withKey(userFormatTypeConfiguration.getUserFormatKeyForType(type));
        if(descriptor != null)
        {
            final String typeKey = descriptor.getTypeI18nKey();
            if(StringUtils.isNotEmpty(typeKey))
            {
                return getI18n().getText(typeKey);
            }
        }
        return type;
    }

    public String getUserFormatName(String type)
    {
        final ModuleDescriptor descriptor = userFormatModuleDescriptors.withKey(userFormatTypeConfiguration.getUserFormatKeyForType(type));
        if(descriptor != null)
        {
            if(StringUtils.isNotEmpty(descriptor.getI18nNameKey()))
            {
                return getI18n().getText(descriptor.getI18nNameKey());
            }
            return descriptor.getName();
        }
        return "";
    }

    public Set<String> getUserFormatTypes()
    {
        return ImmutableSet.copyOf(userFormatTypes.get());
    }

    public String getSampleUserFormat(String type)
    {
        final com.atlassian.crowd.embedded.api.User remoteUser = getLoggedInUser();
        //remote user should really never be null as we are in the admin section, but some loonies may allow 
        //anonymous access to the admin section.
        return userFormatManager.formatUser(isAnonymous(remoteUser) ? null : remoteUser.getName(), type, "look_and_feel");
    }

    public boolean hasUserFormatsToEdit()
    {
        final Set<String> types = getUserFormatTypes();
        for (String type : types)
        {
            if (Iterables.size(userFormatModuleDescriptors.forType(type)) > 1)
            {
                return true;
            }
        }
        return false;
    }

    private I18nHelper getI18n()
    {
        return i18nBeanFactory.getInstance(getLoggedInUser());
    }

    public boolean getUseISO8601() {
        return getApplicationProperties().getOption(APKeys.JIRA_DATE_TIME_PICKER_USE_ISO8601);
    }

    public boolean isRefreshResourcesPerformed() {
        return request.getParameter("refreshResourcesPerformed") != null;
    }

    public String getLogoUrlWithContext()
    {
        return addContextToUrl(getLookAndFeelBean().getLogoUrl());
    }

    public String getFaviconHiResUrlWithContext()
    {
        return addContextToUrl(getLookAndFeelBean().getFaviconHiResUrl());
    }

    private String addContextToUrl(String url)
    {
        if (url != null &&  !url.startsWith("http://"))
        {
            String context = request.getContextPath();
            if ("/".equals(context))
            {
                context="";
            }
            url = context+url;
        }
        return url;
    }

    public String getLogoWidth()
    {
        return getLookAndFeelBean().getLogoWidth();
    }

    public String getLogoHeight()
    {
        return getLookAndFeelBean().getLogoHeight();
    }

    public String getFaviconHiResWidth()
    {
        return getLookAndFeelBean().getFaviconHiResWidth();
    }

    public String getFaviconHiResHeight()
    {
        return getLookAndFeelBean().getFaviconHiResHeight();
    }
}

