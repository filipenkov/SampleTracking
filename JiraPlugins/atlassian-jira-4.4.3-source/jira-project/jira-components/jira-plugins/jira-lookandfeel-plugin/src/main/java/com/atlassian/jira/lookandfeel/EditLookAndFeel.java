package com.atlassian.jira.lookandfeel;

import com.atlassian.jira.bc.user.search.UserPickerSearchService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.CoreFeatures;
import com.atlassian.jira.config.FeatureManager;
import com.atlassian.jira.config.LocaleManager;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.config.properties.LookAndFeelBean;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.lookandfeel.image.ImageDescriptor;
import com.atlassian.jira.lookandfeel.image.MultiPartImageDescriptor;
import com.atlassian.jira.lookandfeel.image.URLImageDescriptor;
import com.atlassian.jira.lookandfeel.upload.UploadService;
import com.atlassian.jira.plugin.profile.UserFormatManager;
import com.atlassian.jira.plugin.userformat.configuration.UserFormatTypeConfiguration;
import com.atlassian.jira.plugin.userformat.descriptors.UserFormatModuleDescriptors;
import com.atlassian.jira.plugin.userformat.descriptors.UserFormatTypes;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.timezone.TimeZoneService;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.web.ServletContextProvider;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import webwork.action.ActionContext;
import webwork.action.ServletActionContext;
import webwork.multipart.MultiPartRequestWrapper;

import java.io.IOException;


@WebSudoRequired
public class EditLookAndFeel extends com.atlassian.jira.web.action.admin.EditLookAndFeel
{

    //private final I18nResolver i18nResolver;
    //private final ApplicationProperties applicationProperties;


    private String logoOptionValue;

    private String faviconUrl;
    private String faviconOptionValue;


    private boolean customizeColors;
    private LogoChoice logoOptionChoice;
    private final JiraAuthenticationContext authenticationContext;

    private final LookAndFeelProperties lookAndFeelProperties;
    private final ApplicationProperties applicationProperties;
    private final VelocityRequestContextFactory requestContextFactory;
    private final FeatureManager featureManager;
    private LogoChoice faviconOptionChoice;
    private final I18nHelper i18nHelper;
    private final UploadService uploadService;
    private PluginSettings globalSettings;

    private final static String DEFAULT_LOGO_URL = "/images/logo.png";
    private final static String DEFAULT_FAVICON_URL = "/images/logo.png";


    public EditLookAndFeel(UserPickerSearchService searchService,
            UserFormatManager userFormatManager,
            BeanFactory i18nBeanFactory,
            LocaleManager localeManager,
            ApplicationProperties applicationProperties,
            JiraAuthenticationContext authenticationContext,
            TimeZoneService timeZoneService,
            RendererManager rendererManager,
            LookAndFeelProperties lookAndFeelProperties,
            PluginSettingsFactory pluginSettingsFactory,
            VelocityRequestContextFactory requestContextFactory,
            UploadService uploadService, FeatureManager featureManager)
    {
        this(searchService, userFormatManager, ComponentAccessor.getComponent(UserFormatTypeConfiguration.class),
                ComponentAccessor.getComponent(UserFormatTypes.class),
                ComponentAccessor.getComponent(UserFormatModuleDescriptors.class), i18nBeanFactory,
                localeManager, applicationProperties, authenticationContext, requestContextFactory, timeZoneService, rendererManager,
                lookAndFeelProperties, pluginSettingsFactory, uploadService, featureManager);

    }

    private EditLookAndFeel(final UserPickerSearchService searchService, final UserFormatManager userFormatManager,
            final UserFormatTypeConfiguration userFormatTypeConfiguration, final UserFormatTypes userFormatTypes,
            final UserFormatModuleDescriptors userFormatModuleDescriptors, final BeanFactory i18nBeanFactory,
            final LocaleManager localeManager, ApplicationProperties applicationProperties, final JiraAuthenticationContext authenticationContext,
            VelocityRequestContextFactory requestContextFactory, final TimeZoneService timeZoneService, final RendererManager rendererManager, LookAndFeelProperties lookAndFeelProperties,
            PluginSettingsFactory pluginSettingsFactory, UploadService uploadService, FeatureManager featureManager)
    {
        super(searchService, userFormatManager, userFormatTypeConfiguration, userFormatTypes, userFormatModuleDescriptors,
                i18nBeanFactory, localeManager, timeZoneService, rendererManager);
        this.authenticationContext = authenticationContext;
        this.uploadService = uploadService;
        this.requestContextFactory = requestContextFactory;
        this.lookAndFeelProperties = lookAndFeelProperties;
        this.applicationProperties = applicationProperties;
        this.featureManager = featureManager;
        this.logoOptionChoice = lookAndFeelProperties.getLogoChoice();
        this.faviconOptionChoice = lookAndFeelProperties.getFaviconChoice();
        this.customizeColors = lookAndFeelProperties.getCustomizeColors();
        this.globalSettings = pluginSettingsFactory.createGlobalSettings();
        i18nHelper = i18nBeanFactory.getInstance(authenticationContext.getLoggedInUser());
    }

    private LookAndFeelBean init()
    {
        final LookAndFeelBean lookAndFeelBean = LookAndFeelBean.getInstance(applicationProperties);
        if (super.getLogoUrl() == null)
        {
            super.setLogoUrl(lookAndFeelBean.getLogoUrl());
            super.setLogoWidth(lookAndFeelBean.getLogoWidth());
            super.setLogoHeight(lookAndFeelBean.getLogoHeight());
        }
        return lookAndFeelBean;
    }


    private LookAndFeelBean getLookAndFeelBean()
    {
        return LookAndFeelBean.getInstance(applicationProperties);
    }


    @Override
    public String doDefault() throws Exception
    {
        init();
        return super.doDefault();
    }


    @Override
    @RequiresXsrfCheck
    public String doExecute()
    {

        final LookAndFeelBean lookAndFeelBean = init();
        final String serverPath = ServletContextProvider.getServletContext().getRealPath("/");

        MultiPartRequestWrapper multiPartRequest = ServletActionContext.getMultiPartRequest();
        if (multiPartRequest == null)
        {
            return ERROR;
        }

        this.logoOptionChoice = LogoChoice.safeValueOf(logoOptionValue);
        this.faviconOptionChoice = LogoChoice.safeValueOf(faviconOptionValue);

        if (logoOptionChoice == null || faviconOptionChoice == null)
        {
            return ERROR;
        }

        try {
            boolean saveSuccess = (handleLogoChoice(logoOptionChoice, multiPartRequest, lookAndFeelBean, serverPath) &&
                    handleFaviconChoice(faviconOptionChoice, multiPartRequest, lookAndFeelBean, serverPath));

            if (!saveSuccess)
            {
                 return ERROR;
            }

            lookAndFeelProperties.setCustomizeColors(customizeColors);

            return super.doExecute();
        }
        catch (IOException e)
        {
            addErrorMessage(getText("jira.lookandfeel.upload.error", "",  e.getMessage()));
            return ERROR;
        }
        catch (Exception e)
        {
            return ERROR;
        }
    }

    private boolean handleLogoChoice(LogoChoice choice, MultiPartRequestWrapper multiPartRequest,
            LookAndFeelBean lookAndFeelBean, String serverPath) throws IOException
    {
        boolean success = false;
        ImageDescriptor imageDescriptor = null;
        String defaultLogoUrl = getLogoUrl();
        try
        {
            switch(choice) {
            case JIRA:
                success = true;
                setLogoUrl(null);
                if (!lookAndFeelProperties.getLogoChoice().equals(choice))
                {
                    setDefaultLogo(lookAndFeelBean);
                    globalSettings.put(LookAndFeelConstants.USING_CUSTOM_LOGO, BooleanUtils.toStringTrueFalse(false));
                    lookAndFeelProperties.resetDefaultLogoUrl();
                    lookAndFeelProperties.setLogoChoice(choice);
                }
                break;
            case UPLOAD:
                final String parameterName =  "logoFile";
                if (StringUtils.isNotBlank(multiPartRequest.getFilesystemName(parameterName)))
                {
                    imageDescriptor = new MultiPartImageDescriptor(parameterName, multiPartRequest, i18nHelper);
                    lookAndFeelProperties.resetDefaultLogoUrl();
                }
                else
                {
                    setLogoUrl(lookAndFeelBean.getLogoUrl());
                    success = true;
                }
                break;
            case URL:
                final String url = getLogoUrl();
                if (StringUtils.isNotBlank(url) && !url.equals(lookAndFeelBean.getLogoUrl()))
                {
                    imageDescriptor = new URLImageDescriptor(serverPath, getLogoUrl(), i18nHelper);
                }
                else
                {
                    setLogoUrl(lookAndFeelBean.getLogoUrl());
                    success = true;
                }
                break;
            }
            if (imageDescriptor != null) {
                addErrorMessages (uploadService.uploadLogo(imageDescriptor, lookAndFeelBean));
                if (!hasAnyErrors())
                {
                    lookAndFeelProperties.setLogoChoice(choice);
                    super.setLogoUrl(lookAndFeelBean.getLogoUrl());
                    super.setLogoWidth(lookAndFeelBean.getLogoWidth());
                    super.setLogoHeight(lookAndFeelBean.getLogoHeight());
                    success = true;
                }
            }
        }
        finally
        {
            if (imageDescriptor != null)
            {
                imageDescriptor.closeImageStreamQuietly();
            }
        }
        return success;
    }



    private void setDefaultLogo(LookAndFeelBean lookAndFeelBean)
    {
        if ("true".equals(globalSettings.get(LookAndFeelConstants.USING_CUSTOM_DEFAULT_LOGO)))
        {
            //use uploaded default logo
            setLogoUrl((String)globalSettings.get(LookAndFeelConstants.CUSTOM_DEFAULT_LOGO_URL));
            setLogoHeight((String)globalSettings.get(LookAndFeelConstants.DEFAULT_LOGO_HEIGHT));
            setLogoWidth((String)globalSettings.get(LookAndFeelConstants.DEFAULT_LOGO_WIDTH));
            lookAndFeelBean.setLogoUrl(getLogoUrl());
            lookAndFeelBean.setLogoHeight(getLogoHeight());
            lookAndFeelBean.setLogoWidth(getLogoWidth());
            lookAndFeelProperties.resetDefaultLogoUrl();
        }
        else //reset to JIRA URL
        {
            setLogoUrl(null);
            setLogoHeight(null);
            setLogoWidth(null);
            lookAndFeelProperties.resetDefaultLogoUrl();
        }
    }

    private void setDefaultFavicon(LookAndFeelBean lookAndFeelBean)
    {
        lookAndFeelBean.setFaviconUrl(null);
        lookAndFeelBean.setFaviconHiResUrl(null);
        lookAndFeelProperties.resetDefaultFaviconUrl();
    }

    private boolean handleFaviconChoice(LogoChoice choice, MultiPartRequestWrapper multiPartRequest,
                     LookAndFeelBean lookAndFeelBean, String serverPath) throws IOException {

        boolean success = false;
        String faviconUrl = null;
        String faviconHiResUrl = null;
        ImageDescriptor imageDescriptor = null;

        //only do something if choice has changed
        try
        {
            switch(choice) {
            case JIRA:
                success = true;
                if (!lookAndFeelProperties.getFaviconChoice().equals(choice))
                {
                    setDefaultFavicon(lookAndFeelBean);
                    globalSettings.put(LookAndFeelConstants.USING_CUSTOM_FAVICON, BooleanUtils.toStringTrueFalse(false));
                    lookAndFeelProperties.resetDefaultFaviconUrl();
                    lookAndFeelProperties.setFaviconChoice(choice);
                }
                break;
            case UPLOAD:
                final String parameterName = "faviconFile";
                if (StringUtils.isNotBlank(multiPartRequest.getFilesystemName(parameterName)))
                {
                    imageDescriptor = new MultiPartImageDescriptor(parameterName, multiPartRequest, i18nHelper);
                    lookAndFeelProperties.resetDefaultFaviconUrl();
                }
                else
                {
                    success = true;
                }
                break;
            case URL:
                String url = getFaviconUrl();
                if (StringUtils.isNotBlank(url) && !url.equals(lookAndFeelBean.getLogoUrl()))
                {
                    imageDescriptor =  new URLImageDescriptor(serverPath, url, i18nHelper);
                }
                else
                {
                    success = true;
                }
                break;
            }
           if (imageDescriptor != null) {
                addErrorMessages (uploadService.uploadFavicon(lookAndFeelBean, imageDescriptor));
                if (!hasAnyErrors())
                {
                    lookAndFeelProperties.setFaviconChoice(choice);
                    success = true;
                }
            }
        }
        finally
        {
            if (imageDescriptor != null)
            {
                imageDescriptor.closeImageStreamQuietly();
            }
        }
        return success;
    }


    private String ensureUrlCorrect(String url)
    {
        // url must start with 'http://', 'http://', or else add the leading '/'
        if (StringUtils.isNotBlank(url) && !url.startsWith("http") && !url.startsWith("/"))
        {
            url = "/" + url;
        }
        return url;
    }



    @Override
    public String doReset() throws Exception
    {
        final LookAndFeelBean lookAndFeelBean = init();

        String defaultLogoUrl = null;
        String defaultLogoWidth = null;
        String defaultLogoHeight = null;
        String defaultFaviconUrl = null;
        String defaultHiResFaviconUrl = null;
        if (lookAndFeelProperties.isUsingCustomDefaultLogo())
        {
            defaultLogoUrl =  (String)globalSettings.get(LookAndFeelConstants.CUSTOM_DEFAULT_LOGO_URL);
            defaultLogoWidth = (String)globalSettings.get(LookAndFeelConstants.DEFAULT_LOGO_WIDTH);
            defaultLogoHeight = (String)globalSettings.get(LookAndFeelConstants.DEFAULT_LOGO_HEIGHT);
        }
        if (lookAndFeelProperties.isUsingCustomDefaultFavicon())
        {
            defaultFaviconUrl = (String)globalSettings.get(LookAndFeelConstants.CUSTOM_DEFAULT_FAVICON_URL);
            defaultHiResFaviconUrl = (String)globalSettings.get(LookAndFeelConstants.CUSTOM_DEFAULT_FAVICON_URL);
        }
        String result = super.doReset();
        lookAndFeelBean.setFaviconHiResUrl(defaultHiResFaviconUrl);
        lookAndFeelBean.setFaviconUrl(defaultFaviconUrl);
        lookAndFeelBean.setLogoUrl(defaultLogoUrl);
        lookAndFeelBean.setLogoWidth(defaultLogoWidth);
        lookAndFeelBean.setLogoHeight(defaultLogoHeight);
        lookAndFeelProperties.reset();

        return result;
    }

    public String getContextPath()
    {
        return ActionContext.getRequest().getContextPath();
    }


    @Override
    public String getText(final String aTextName)
    {
        return i18nHelper.getText(aTextName);
    }

    public String getLogoOption()
    {
        return logoOptionValue;
    }

    public void setLogoOption(String logoOption)
    {
        this.logoOptionValue = logoOption;
    }

    public String getFaviconOption()
    {
        return faviconOptionValue;
    }

    public void setFaviconOption(String faviconOptionValue) {
        this.faviconOptionValue = faviconOptionValue;
    }

    public String getFaviconUrl()
    {
        return faviconUrl != null ? faviconUrl : "";
    }

    public void setFaviconUrl(String faviconUrl) {

        this.faviconUrl = ensureUrlCorrect(faviconUrl);
    }

    public String getDbBackedDefaultLogoUrl()
    {
        String defaultUrl;
        if ("true".equals(globalSettings.get(LookAndFeelConstants.USING_CUSTOM_DEFAULT_LOGO)))
        {
            defaultUrl =  ensureURLContext((String)globalSettings.get(LookAndFeelConstants.CUSTOM_DEFAULT_LOGO_URL));
         }
        else
        {
            defaultUrl =  null;
        }
        return defaultUrl;
    }

    private String ensureURLContext(String defaultUrl) {
        if (defaultUrl.startsWith("http"))
        {
            return defaultUrl;
        }
        else
        {
            return getBaseUrl() + defaultUrl;
        }
    }

    public void setLogoUrl(String logoUrl)
    {
        if (StringUtils.isBlank(logoUrl))
        {
            if("true".equals((String) globalSettings.get(LookAndFeelConstants.USING_CUSTOM_DEFAULT_LOGO)))
            {
                logoUrl = (String)globalSettings.get(LookAndFeelConstants.CUSTOM_DEFAULT_LOGO_URL);
            }
            else
            {
                LookAndFeelBean lookAndFeelBean = init();
                logoUrl=lookAndFeelBean.getLogoUrl();
            }
        }
        super.setLogoUrl(logoUrl);
    }

    public void setCustomizeColors(boolean customizeColors) {
        this.customizeColors = customizeColors;
    }

    public boolean isCustomizeColors() {
        return customizeColors;
    }

    public boolean isDefaultLogoOption() {
        return this.logoOptionChoice.equals(LogoChoice.JIRA);
    }

    public boolean isUploadLogoOption() {
        return this.logoOptionChoice.equals(LogoChoice.UPLOAD);
    }

    public boolean isUrlLogoOption() {
        return this.logoOptionChoice.equals(LogoChoice.URL);
    }

    public boolean isDefaultFaviconOption() {
        return this.faviconOptionChoice.equals(LogoChoice.JIRA);
    }

    public boolean isUploadFaviconOption() {
        return this.faviconOptionChoice.equals(LogoChoice.UPLOAD);
    }

    public boolean isUrlFaviconOption() {
        return this.faviconOptionChoice.equals(LogoChoice.URL);
    }

    private String getBaseUrl()
    {
        return requestContextFactory.getJiraVelocityRequestContext().getBaseUrl();
    }

    public boolean isStudioEnabled()
    {
        return featureManager.isEnabled(CoreFeatures.ON_DEMAND);
    }

    public boolean isWhiteArrow()
    {
        return !isBlackArrow();
    }

    public boolean isBlackArrow()
    {
        return lookAndFeelProperties.isBlackArrow();
    }

    public void setBlackArrow(final boolean isBlackArrow)
    {
        lookAndFeelProperties.setBlackArrow(isBlackArrow);
    }
}
