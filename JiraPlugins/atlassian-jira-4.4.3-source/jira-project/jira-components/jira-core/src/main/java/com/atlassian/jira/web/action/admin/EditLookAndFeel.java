package com.atlassian.jira.web.action.admin;

import com.atlassian.gadgets.dashboard.Color;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.bc.user.search.UserPickerSearchService;
import com.atlassian.jira.config.LocaleManager;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.config.properties.LookAndFeelBean;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.plugin.profile.UserFormatManager;
import com.atlassian.jira.plugin.userformat.UserFormatModuleDescriptor;
import com.atlassian.jira.plugin.userformat.configuration.UserFormatTypeConfiguration;
import com.atlassian.jira.plugin.userformat.descriptors.UserFormatModuleDescriptors;
import com.atlassian.jira.plugin.userformat.descriptors.UserFormatTypes;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.timezone.TimeZoneService;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.opensymphony.user.User;
import com.opensymphony.util.TextUtils;
import org.apache.commons.lang.StringUtils;
import webwork.action.ActionContext;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

@WebSudoRequired
public class EditLookAndFeel extends ViewApplicationProperties
{
    public static final String USER_FORMAT_PREFIX = "user_format_for_";
    public static final String GADGET_CHROME_COLOR_PREFIX = "gadgetChromeColor";

    private String logoUrl;
    private String logoWidth;
    private String logoHeight;

    private String topBgColour;
    private String topTextColour;
    private String topHighlightBgColour;
    private String topTextHighlightColour;
    private String topSeparatorColor;

    private String menuBgColour;
    private String menuTextColour;
    private String menuSeparatorColour;

    private String linkColour;
    private String linkAColour;
    private String headingColour;
    private String issuetype;
    private String summary;
    private String priority;
    private String components;
    private String versions;
    private String fixfor;
    private String assignee;
    private String environment;
    private String description;
    private String originaltimetrack;
    private String timetrack;
    private String formatTime;
    private String formatDay;
    private String formatComplete;
    private String formatDMY;
    private boolean preview = false;
    private boolean useISO8601Getter;
    private boolean useISO8601Setter;


    private static final long EXAMPLE_DATE = 1179892547906l; //Wed May 23 13:55:47 EST 2007
    private final UserFormatManager userFormatManager;
    private final UserFormatTypeConfiguration userFormatTypeConfiguration;
    private final UserFormatTypes userFormatTypes;
    private final UserFormatModuleDescriptors userFormatModuleDescriptors;
    private final BeanFactory i18nBeanFactory;

    public EditLookAndFeel(final UserPickerSearchService searchService, final UserFormatManager userFormatManager,
            final UserFormatTypeConfiguration userFormatTypeConfiguration, final UserFormatTypes userFormatTypes,
            final UserFormatModuleDescriptors userFormatModuleDescriptors, final BeanFactory i18nBeanFactory,
            final LocaleManager localeManager, final TimeZoneService timeZoneManager, final RendererManager rendererManager)
    {
        super(searchService, localeManager, timeZoneManager, rendererManager);
        this.userFormatManager = userFormatManager;
        this.userFormatTypeConfiguration = userFormatTypeConfiguration;
        this.userFormatTypes = userFormatTypes;
        this.userFormatModuleDescriptors = userFormatModuleDescriptors;
        this.i18nBeanFactory = i18nBeanFactory;
    }

    public String doDefault() throws Exception
    {
        LookAndFeelBean lookAndFeelBean = LookAndFeelBean.getInstance(getApplicationProperties());
        logoUrl = lookAndFeelBean.getLogoUrl();
        logoWidth = lookAndFeelBean.getLogoWidth();
        logoHeight = lookAndFeelBean.getLogoHeight();

        topBgColour = lookAndFeelBean.getTopBackgroundColour();
        topTextColour = lookAndFeelBean.getTopTxtColour();
        topHighlightBgColour = lookAndFeelBean.getTopHighlightColor();
        topTextHighlightColour = lookAndFeelBean.getTopTextHighlightColor();
        topSeparatorColor = lookAndFeelBean.getTopSeparatorBackgroundColor();

        menuBgColour = lookAndFeelBean.getMenuBackgroundColour();
        menuSeparatorColour = lookAndFeelBean.getMenuSeparatorColour();
        menuTextColour = lookAndFeelBean.getMenuTxtColour();

        linkColour = lookAndFeelBean.getTextLinkColour();
        linkAColour = lookAndFeelBean.getTextActiveLinkColour();

        headingColour = lookAndFeelBean.getTextHeadingColour();

        issuetype = getApplicationProperties().getString(APKeys.JIRA_ISSUE_DESC_ISSUETYPE);
        summary = getApplicationProperties().getString(APKeys.JIRA_ISSUE_DESC_SUMMARY);
        priority = getApplicationProperties().getString(APKeys.JIRA_ISSUE_DESC_PRIORITY);
        components = getApplicationProperties().getString(APKeys.JIRA_ISSUE_DESC_COMPONENTS);
        versions = getApplicationProperties().getString(APKeys.JIRA_ISSUE_DESC_VERSIONS);
        fixfor = getApplicationProperties().getString(APKeys.JIRA_ISSUE_DESC_FIXFOR);
        assignee = getApplicationProperties().getString(APKeys.JIRA_ISSUE_DESC_ASSIGNEE);
        environment = getApplicationProperties().getString(APKeys.JIRA_ISSUE_DESC_ENVIRONMENT);
        description = getApplicationProperties().getString(APKeys.JIRA_ISSUE_DESC_DESCRIPTION);
        originaltimetrack = getApplicationProperties().getString(APKeys.JIRA_ISSUE_DESC_ORIGINAL_TIMETRACK);
        timetrack = getApplicationProperties().getString(APKeys.JIRA_ISSUE_DESC_TIMETRACK);

        formatTime = getApplicationProperties().getString(APKeys.JIRA_LF_DATE_TIME);
        formatDay = getApplicationProperties().getString(APKeys.JIRA_LF_DATE_DAY);
        formatComplete = getApplicationProperties().getString(APKeys.JIRA_LF_DATE_COMPLETE);
        formatDMY = getApplicationProperties().getString(APKeys.JIRA_LF_DATE_DMY);

        useISO8601Getter = getApplicationProperties().getOption(APKeys.JIRA_DATE_TIME_PICKER_USE_ISO8061);

        return super.doDefault();
    }

    protected void doValidation()
    {
        final boolean logoSet = TextUtils.stringSet(logoUrl);
        if (!TextUtils.stringSet(logoWidth))
        {
            if (logoSet)
            {
                addError("logoWidth", getI18n().getText("admin.errors.must.specify.logo.width"));
            }
        }
        else
        {
            try
            {
                final int width = Integer.parseInt(logoWidth);
                if (width < 0)
                {
                    addError("logoWidth", getI18n().getText("admin.errors.logo.width.must.be.number"));
                }
            }
            catch (NumberFormatException e)
            {
                addError("logoWidth", getI18n().getText("admin.errors.logo.width.must.be.number"));
            }
        }

        if (!TextUtils.stringSet(logoHeight))
        {
            if (logoSet)
            {
                addError("logoHeight", getI18n().getText("admin.errors.must.specify.logo.height"));
            }
        }
        else
        {
            try
            {
                final int height = Integer.parseInt(logoHeight);
                if (height < 0)
                {
                    addError("logoHeight", getI18n().getText("admin.errors.logo.height.must.be.number"));
                }
            }
            catch (NumberFormatException e)
            {
                addError("logoHeight", getI18n().getText("admin.errors.logo.height.must.be.number"));
            }
        }

        if (!validateDateFormat(formatTime))
        {
            addError("formatTime", getI18n().getText("admin.errors.must.specify.a.valid.time.format"));
        }

        if (!validateDateFormat(formatDay))
        {
            addError("formatDay", getI18n().getText("admin.errors.must.specify.a.valid.day.format"));
        }

        if (!validateDateFormat(formatComplete))
        {
            addError("formatComplete", getI18n().getText("admin.errors.must.specify.a.complete.date.time.format"));
        }

        if (!validateDateFormat(formatDMY))
        {
            addError("formatDMY", getI18n().getText("admin.errors.must.specify.a.valid.date.format"));
        }

        super.doValidation();
    }

    private boolean validateDateFormat(String format)
    {
        if (TextUtils.stringSet(format))
        {
            try
            {
                new SimpleDateFormat(format);
            }
            catch (IllegalArgumentException e)
            {
                return false;
            }
        }

        return true;
    }

    @RequiresXsrfCheck
    public String doReset() throws Exception
    {
        LookAndFeelBean lookAndFeelBean = LookAndFeelBean.getInstance(getApplicationProperties());
        lookAndFeelBean.setLogoUrl(null);
        lookAndFeelBean.setLogoWidth(null);
        lookAndFeelBean.setLogoHeight(null);
        lookAndFeelBean.setFaviconHiResUrl(null);
        lookAndFeelBean.setFaviconUrl(null);

        lookAndFeelBean.setTopBackgroundColour(null);
        lookAndFeelBean.setTopTxtColour(null);
        lookAndFeelBean.setTopHighlightColor(null);
        lookAndFeelBean.setTopTextHighlightColor(null);
        lookAndFeelBean.setTopSeparatorBackgroundColor(null);

        lookAndFeelBean.setMenuBackgroundColour(null);
        lookAndFeelBean.setMenuTxtColour(null);
        lookAndFeelBean.setMenuSeparatorColour(null);

        lookAndFeelBean.setTextLinkColour(null);
        lookAndFeelBean.setTextActiveLinkColour(null);

        lookAndFeelBean.setTextHeadingColour(null);

        //todo - determine if we really want to reset the descriptions
        getApplicationProperties().setString(APKeys.JIRA_ISSUE_DESC_ISSUETYPE, null);
        getApplicationProperties().setString(APKeys.JIRA_ISSUE_DESC_SUMMARY, null);
        getApplicationProperties().setString(APKeys.JIRA_ISSUE_DESC_PRIORITY, null);
        getApplicationProperties().setString(APKeys.JIRA_ISSUE_DESC_COMPONENTS, null);
        getApplicationProperties().setString(APKeys.JIRA_ISSUE_DESC_VERSIONS, null);
        getApplicationProperties().setString(APKeys.JIRA_ISSUE_DESC_FIXFOR, null);
        getApplicationProperties().setString(APKeys.JIRA_ISSUE_DESC_ASSIGNEE, null);
        getApplicationProperties().setString(APKeys.JIRA_ISSUE_DESC_ENVIRONMENT, null);
        getApplicationProperties().setString(APKeys.JIRA_ISSUE_DESC_DESCRIPTION, null);
        getApplicationProperties().setString(APKeys.JIRA_ISSUE_DESC_ORIGINAL_TIMETRACK, null);
        getApplicationProperties().setString(APKeys.JIRA_ISSUE_DESC_TIMETRACK, null);

        getApplicationProperties().setString(APKeys.JIRA_LF_DATE_TIME, null);
        getApplicationProperties().setString(APKeys.JIRA_LF_DATE_DAY, null);
        getApplicationProperties().setString(APKeys.JIRA_LF_DATE_COMPLETE, null);
        getApplicationProperties().setString(APKeys.JIRA_LF_DATE_DMY, null);
        getApplicationProperties().setOption(APKeys.JIRA_DATE_TIME_PICKER_USE_ISO8061, false);

        //reset the gadget chrome colours.
        for (Color color : Color.values())
        {
            lookAndFeelBean.setGadgetChromeColor(color.toString(), null);
        }

        // Flushes the dateformat objects to use the new ones.
        ManagerFactory.getOutlookDateManager().refresh();

        return getRedirect("ViewLookAndFeel.jspa");
    }

    @RequiresXsrfCheck
    public String doRefreshResources() throws Exception
    {
        final LookAndFeelBean lookAndFeelBean = LookAndFeelBean.getInstance(getApplicationProperties());
        // this causes the underlying counter to be bumped
        lookAndFeelBean.setMenuBackgroundColour(lookAndFeelBean.getMenuBackgroundColour());
        return getRedirect("ViewLookAndFeel.jspa?refreshResourcesPerformed=true");
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        //this is so that we can reload the page & get the colours, without actually changing any properties.
        if (preview)
        {
            preview = false;
            return INPUT;
        }
        LookAndFeelBean lookAndFeelBean = LookAndFeelBean.getInstance(getApplicationProperties());

        String newValue = TextUtils.stringSet(logoUrl) ? logoUrl : null;
        if (hasChanged(lookAndFeelBean.getLogoUrl(), newValue))
        {
            lookAndFeelBean.setLogoUrl(newValue);
        }
        newValue = TextUtils.stringSet(logoWidth) ? logoWidth : null;
        if (hasChanged(lookAndFeelBean.getLogoWidth(), newValue))
        {
            lookAndFeelBean.setLogoWidth(newValue);
        }
        newValue = TextUtils.stringSet(logoHeight) ? logoHeight : null;
        if (hasChanged(lookAndFeelBean.getLogoHeight(), newValue))
        {
            lookAndFeelBean.setLogoHeight(newValue);
        }
        newValue = formatColorCode(topBgColour);
        if (hasChanged(lookAndFeelBean.getTopBackgroundColour(), newValue))
        {
            lookAndFeelBean.setTopBackgroundColour(newValue);
        }
        newValue = formatColorCode(topTextColour);
        if (hasChanged(lookAndFeelBean.getTopTxtColour(), newValue))
        {
            lookAndFeelBean.setTopTxtColour(newValue);
        }
        newValue = formatColorCode(topHighlightBgColour);
        if (hasChanged(lookAndFeelBean.getTopHighlightColor(), newValue))
        {
            lookAndFeelBean.setTopHighlightColor(newValue);
        }
        newValue = formatColorCode(topTextHighlightColour);
        if (hasChanged(lookAndFeelBean.getTopTextHighlightColor(), newValue))
        {
            lookAndFeelBean.setTopTextHighlightColor(newValue);
        }
        newValue = formatColorCode(topSeparatorColor);
        if (hasChanged(lookAndFeelBean.getTopSeparatorBackgroundColor(), newValue))
        {
            lookAndFeelBean.setTopSeparatorBackgroundColor(newValue);
        }
        newValue = formatColorCode(menuBgColour);
        if (hasChanged(lookAndFeelBean.getMenuBackgroundColour(), newValue))
        {
            lookAndFeelBean.setMenuBackgroundColour(newValue);
        }
        newValue = formatColorCode(menuSeparatorColour);
        if (hasChanged(lookAndFeelBean.getMenuSeparatorColour(), newValue))
        {
            lookAndFeelBean.setMenuSeparatorColour(newValue);
        }
        newValue = formatColorCode(menuTextColour);
        if (hasChanged(lookAndFeelBean.getMenuTxtColour(), newValue))
        {
            lookAndFeelBean.setMenuTxtColour(newValue);
        }

        newValue = formatColorCode(linkColour);
        if (hasChanged(lookAndFeelBean.getTextLinkColour(), newValue))
        {
            lookAndFeelBean.setTextLinkColour(newValue);
        }
        newValue = formatColorCode(linkAColour);
        if (hasChanged(lookAndFeelBean.getTextActiveLinkColour(), newValue))
        {
            lookAndFeelBean.setTextActiveLinkColour(newValue);
        }

        newValue = formatColorCode(headingColour);
        if (hasChanged(lookAndFeelBean.getTextHeadingColour(), newValue))
        {
            lookAndFeelBean.setTextHeadingColour(newValue);
        }


        final ApplicationProperties ap = getApplicationProperties();
        ap.setString(APKeys.JIRA_ISSUE_DESC_ISSUETYPE, TextUtils.stringSet(issuetype) ? issuetype : null);
        ap.setString(APKeys.JIRA_ISSUE_DESC_SUMMARY, TextUtils.stringSet(summary) ? summary : null);
        ap.setString(APKeys.JIRA_ISSUE_DESC_PRIORITY, TextUtils.stringSet(priority) ? priority : null);
        ap.setString(APKeys.JIRA_ISSUE_DESC_COMPONENTS, TextUtils.stringSet(components) ? components : null);
        ap.setString(APKeys.JIRA_ISSUE_DESC_VERSIONS, TextUtils.stringSet(versions) ? versions : null);
        ap.setString(APKeys.JIRA_ISSUE_DESC_FIXFOR, TextUtils.stringSet(fixfor) ? fixfor : null);
        ap.setString(APKeys.JIRA_ISSUE_DESC_ASSIGNEE, TextUtils.stringSet(assignee) ? assignee : null);
        ap.setString(APKeys.JIRA_ISSUE_DESC_ENVIRONMENT, TextUtils.stringSet(environment) ? environment : null);
        ap.setString(APKeys.JIRA_ISSUE_DESC_DESCRIPTION, TextUtils.stringSet(description) ? description : null);
        ap.setString(APKeys.JIRA_ISSUE_DESC_ORIGINAL_TIMETRACK, TextUtils.stringSet(originaltimetrack) ? originaltimetrack : null);
        ap.setString(APKeys.JIRA_ISSUE_DESC_TIMETRACK, TextUtils.stringSet(timetrack) ? timetrack : null);

        ap.setString(APKeys.JIRA_LF_DATE_TIME, TextUtils.stringSet(formatTime) ? formatTime : null);
        ap.setString(APKeys.JIRA_LF_DATE_DAY, TextUtils.stringSet(formatDay) ? formatDay : null);
        ap.setString(APKeys.JIRA_LF_DATE_COMPLETE, TextUtils.stringSet(formatComplete) ? formatComplete : null);
        ap.setString(APKeys.JIRA_LF_DATE_DMY, TextUtils.stringSet(formatDMY) ? formatDMY : null);
        ap.setOption(APKeys.JIRA_DATE_TIME_PICKER_USE_ISO8061, useISO8601Setter);
        
        setUserFormat(ActionContext.getParameters());
        setGadgetChromeColors(ActionContext.getParameters());

        // Flushes the dateformat objects to use the new ones.
        ManagerFactory.getOutlookDateManager().refresh();

        return getRedirect("ViewLookAndFeel.jspa");
    }

    private boolean hasChanged(String value1, String value2)
    {
        return value1 == null ? value2 != null : !value1.equals(value2);
    }

    private String formatColorCode(String color)
    {
        if (TextUtils.stringSet(color))
        {
            return makeValidColor(color);
        }
        return null;
    }

    /**
     * Makes a valid CSS color.
     *
     * @param color the color to make valid
     * @return a valid CSS color
     */
    private String makeValidColor(String color)
    {
        color = color.trim();
        color = color.startsWith("#") ? color : "#" + color;
        // check lengths of values
        final int len = color.length();
        if (len > 7)
        {
            color = color.substring(0, 7);
        }
        else if (len == 4)
        {
            char[] chars = color.toCharArray();
            color = "#" + chars[1] + chars[1] + chars[2] + chars[2] + chars[3] + chars[3];
        }
        else if (len < 7) {
            StringBuffer sb = new StringBuffer(color);
            for (int i = len; i < 7; i++)
            {
                sb.append("0");
            }
            color = sb.toString();
        }
        return color;
    }

    // ---------------------------------------------------->
    // Getters and Setters
    public void setPreview(boolean preview)
    {
        this.preview = preview;
    }

    public String getLogoUrl()
    {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl)
    {
        //logoUrl must start with 'http://', 'http://', or else add the leading '/'
        if (TextUtils.stringSet(logoUrl) && !logoUrl.startsWith("http") && !logoUrl.startsWith("/"))
        {
            logoUrl = "/" + logoUrl;
        }

        this.logoUrl = logoUrl;
    }

    public String getLogoWidth()
    {
        return logoWidth;
    }

    public void setLogoWidth(String logoWidth)
    {
        this.logoWidth = logoWidth;
    }

    public String getLogoHeight()
    {
        return logoHeight;
    }

    public void setLogoHeight(String logoHeight)
    {
        this.logoHeight = logoHeight;
    }

    public String getTopBgColour()
    {
        return topBgColour;
    }

    public void setTopBgColour(String topBgColour)
    {
        this.topBgColour = topBgColour;
    }

    public String getMenuBgColour()
    {
        return menuBgColour;
    }

    public void setMenuBgColour(String menuBgColour)
    {
        this.menuBgColour = menuBgColour;
    }

    public String getMenuSeparatorColour()
    {
        return menuSeparatorColour;
    }

    public void setMenuSeparatorColour(String menuSeparatorColour)
    {
        this.menuSeparatorColour = menuSeparatorColour;
    }

    public String getMenuTextColour()
    {
        return menuTextColour;
    }

    public void setMenuTextColour(String menuTextColour)
    {
        this.menuTextColour = menuTextColour;
    }

    public String getTopTextColour()
    {
        return topTextColour;
    }

    public void setTopTextColour(String topTextColour)
    {
        this.topTextColour = topTextColour;
    }

    public String getIssuetype()
    {
        return issuetype;
    }

    public void setIssuetype(String issuetype)
    {
        this.issuetype = issuetype;
    }

    public String getSummary()
    {
        return summary;
    }

    public void setSummary(String summary)
    {
        this.summary = summary;
    }

    public String getPriority()
    {
        return priority;
    }

    public void setPriority(String priority)
    {
        this.priority = priority;
    }

    public String getComponents()
    {
        return components;
    }

    public void setComponents(String components)
    {
        this.components = components;
    }

    public String getVersions()
    {
        return versions;
    }

    public void setVersions(String versions)
    {
        this.versions = versions;
    }

    public String getFixfor()
    {
        return fixfor;
    }

    public void setFixfor(String fixfor)
    {
        this.fixfor = fixfor;
    }

    public String getAssignee()
    {
        return assignee;
    }

    public void setAssignee(String assignee)
    {
        this.assignee = assignee;
    }

    public String getEnvironment()
    {
        return environment;
    }

    public void setEnvironment(String environment)
    {
        this.environment = environment;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getOriginaltimetrack()
    {
        return originaltimetrack;
    }

    public void setOriginaltimetrack(String originaltimetrack)
    {
        this.originaltimetrack = originaltimetrack;
    }

    public String getTimetrack()
    {
        return timetrack;
    }

    public void setTimetrack(String timetrack)
    {
        this.timetrack = timetrack;
    }

    public String getLinkColour()
    {
        return linkColour;
    }

    public void setLinkColour(String linkColour)
    {
        this.linkColour = linkColour;
    }

    public String getLinkAColour()
    {
        return linkAColour;
    }

    public void setLinkAColour(String linkAColour)
    {
        this.linkAColour = linkAColour;
    }

    public String getHeadingColour()
    {
        return headingColour;
    }

    public void setHeadingColour(String headingColour)
    {
        this.headingColour = headingColour;
    }

    public String getFormatTime()
    {
        return formatTime;
    }

    public void setFormatTime(String formatTime)
    {
        this.formatTime = formatTime;
    }

    public String getFormatDay()
    {
        return formatDay;
    }

    public void setFormatDay(String formatDay)
    {
        this.formatDay = formatDay;
    }

    public String getFormatComplete()
    {
        return formatComplete;
    }

    public void setFormatComplete(String formatComplete)
    {
        this.formatComplete = formatComplete;
    }

    public String getFormatDMY()
    {
        return formatDMY;
    }

    public void setFormatDMY(String formatDMY)
    {
        this.formatDMY = formatDMY;
    }

    public static Date getExampleDate()
    {
        return new Date(EXAMPLE_DATE);
    }

    public String getTopHighlightBgColour()
    {
        return topHighlightBgColour;
    }

    public void setTopHighlightBgColour(final String topHighlightBgColour)
    {
        this.topHighlightBgColour = topHighlightBgColour;
    }

    public String getTopTextHighlightColour()
    {
        return topTextHighlightColour;
    }

    public void setTopTextHighlightColour(final String newValue)
    {
        this.topTextHighlightColour = newValue;
    }

    public String getTopSeparatorColor()
    {
        return topSeparatorColor;
    }

    public void setTopSeparatorColor(final String topSeparatorColor)
    {
        this.topSeparatorColor = topSeparatorColor;
    }

    public String getUserFormatTypeName(String type)
    {
        final UserFormatModuleDescriptor descriptor = userFormatModuleDescriptors.withKey(userFormatTypeConfiguration.getUserFormatKeyForType(type));
        if(descriptor != null)
        {
            final String typeI18nKey = descriptor.getTypeI18nKey();
            if(StringUtils.isNotEmpty(typeI18nKey))
            {
                return getI18n().getText(typeI18nKey);
            }
        }
        return type;
    }

    public String getUserFormatName(String type)
    {
        final UserFormatModuleDescriptor descriptor = userFormatModuleDescriptors.withKey(userFormatTypeConfiguration.getUserFormatKeyForType(type));
        if(descriptor != null)
        {
            final String nameKey = descriptor.getI18nNameKey();
            if(StringUtils.isNotEmpty(nameKey))
            {
                return getI18n().getText(nameKey);
            }
            return descriptor.getName();
        }
        return type;
    }

    public String getUserFormatKey(String type)
    {
        final UserFormatModuleDescriptor descriptor = userFormatModuleDescriptors.withKey(userFormatTypeConfiguration.getUserFormatKeyForType(type));
        if(descriptor != null)
        {
            return descriptor.getCompleteKey();
        }
        return type;
    }

    public Collection getUserFormatModuleDescriptorsForType(String type)
    {
        return ImmutableList.copyOf(userFormatModuleDescriptors.forType(type));
    }

    public String getCurrentUserFormatForType(String type)
    {
        return userFormatTypeConfiguration.getUserFormatKeyForType(type);
    }

    public Set<String> getUserFormatTypes()
    {
        return ImmutableSet.copyOf(userFormatTypes.get());
    }

    public String getSampleUserFormat(String type)
    {
        final User remoteUser = getRemoteUser();
        //remote user should really never be null as we are in the admin section, but some loonies may allow
        //anonymous access to the admin section.
        return userFormatManager.formatUser(remoteUser == null ? null : remoteUser.getName(), type, "look_and_feel");
    }

    public boolean hasUserFormatsToEdit()
    {
        Set types = getUserFormatTypes();
        for (Object type1 : types)
        {
            String type = (String) type1;
            if (getUserFormatModuleDescriptorsForType(type).size() > 1)
            {
                return true;
            }
        }
        return false;
    }

    public List<Color> getGadgetColors()
    {
        final List<Color> colors = new ArrayList<Color>();
        for (Color color : Color.values())
        {
            //color 8 is special.  It's the chromeless mode.
            if (!color.equals(Color.color8))
            {
                colors.add(color);
            }
        }
        return colors;
    }

    public String getGadgetColor(Color colorId)
    {
        final LookAndFeelBean lookAndFeelBean = LookAndFeelBean.getInstance(getApplicationProperties());
        String gadgetColor = lookAndFeelBean.getGadgetChromeColor(colorId.toString());
        if(StringUtils.isBlank(gadgetColor))
        {
            gadgetColor = "";
        }

        return gadgetColor;
    }

    public String getLookAndFeelVersionNumber()
    {
        LookAndFeelBean lookAndFeelBean = LookAndFeelBean.getInstance(getApplicationProperties());
        return lookAndFeelBean.getVersion() + "";
    }


    private void setUserFormat(Map parameters)
    {
        final Set set = parameters.entrySet();
        for (Iterator iterator = set.iterator(); iterator.hasNext();)
        {
            final Map.Entry entry = (Map.Entry) iterator.next();
            final String key = (String) entry.getKey();
            if(key.startsWith(USER_FORMAT_PREFIX))
            {
                String type = key.substring(USER_FORMAT_PREFIX.length());
                try
                {
                    userFormatTypeConfiguration.setUserFormatKeyForType(type, ((String[]) entry.getValue())[0]);
                }
                catch (IllegalArgumentException e)
                {
                    addError(USER_FORMAT_PREFIX + type, getI18n().getText("admin.globalsettings.lookandfeel.error.invalid.user.format"));
                }
            }
        }
    }

    private void setGadgetChromeColors(Map parameters)
    {
        final Set set = parameters.entrySet();
        final LookAndFeelBean lookAndFeelBean = LookAndFeelBean.getInstance(getApplicationProperties());
        for (Iterator iterator = set.iterator(); iterator.hasNext();)
        {
            final Map.Entry entry = (Map.Entry) iterator.next();
            final String key = (String) entry.getKey();
            if(key.startsWith(GADGET_CHROME_COLOR_PREFIX))
            {
                final String colorId = key.substring(GADGET_CHROME_COLOR_PREFIX.length());
                final String[] values = (String[]) entry.getValue();
                final String value = values != null && values.length > 0 ? values[0] : null;
                final String newValue = StringUtils.isNotBlank(value) ? value : null;
                if (hasChanged(lookAndFeelBean.getGadgetChromeColor(colorId), newValue))
                {
                    lookAndFeelBean.setGadgetChromeColor(colorId, value);
                }
            }
        }
    }

    private I18nHelper getI18n()
    {
        return i18nBeanFactory.getInstance(getRemoteUser());
    }

    public boolean getUseISO8601()
    {
        return useISO8601Getter;
    }

    public void setUseISO8601(final boolean useISO8061)
    {
        this.useISO8601Setter = useISO8061;
    }
}
