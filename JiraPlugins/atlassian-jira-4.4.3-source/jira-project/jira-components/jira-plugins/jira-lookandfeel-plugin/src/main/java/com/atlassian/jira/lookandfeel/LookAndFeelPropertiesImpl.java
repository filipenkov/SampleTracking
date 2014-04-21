package com.atlassian.jira.lookandfeel;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.config.properties.LookAndFeelBean;
import com.atlassian.jira.lookandfeel.upload.UploadService;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.google.common.base.Preconditions;
import org.apache.commons.lang.BooleanUtils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Map;

/**
 *
 */
public class LookAndFeelPropertiesImpl implements LookAndFeelProperties
{

    private static final String CUSTOMIZE_COLORS = "com.atlassian.jira.lookandfeel.customizeColors";
    private static final String LOGO_CHOICE = "com.atlassian.jira.lookandfeel.logoChoice";
    private static final String FAVICON_CHOICE = "com.atlassian.jira.lookandfeel.faviconChoice";
    private static final String DEFAULT_LOGO_URL = "com.atlassian.jira.lookandfeel.logo.default.url";
    private static final String DEFAULT_FAVICON_URL = "com.atlassian.jira.lookandfeel.favicon.default.url";


    private static final boolean DEFAULT_WHITE_ARROW = false;

    private final PluginSettings globalSettings;
    private final LookAndFeelBean lookAndFeelBean;
    private final UploadService uploadService;


    public LookAndFeelPropertiesImpl(PluginSettingsFactory pluginSettingsFactory,
            ApplicationProperties applicationProperties,
            UploadService uploadService)
    {
        globalSettings = pluginSettingsFactory.createGlobalSettings();
        lookAndFeelBean = LookAndFeelBean.getInstance(applicationProperties);
        this.uploadService = uploadService;
    }

    @Override
    public void setCustomizeColors(boolean value)
    {
        globalSettings.put(CUSTOMIZE_COLORS, String.valueOf(value));
    }


    @Override
    public boolean getCustomizeColors()
    {
        String customizeColors = (String) globalSettings.get(CUSTOMIZE_COLORS);
        if (customizeColors != null) {
            return Boolean.parseBoolean(customizeColors);
        }
        else
        {
            return false;
        }
    }

    @Override
    public void setLogoChoice(LogoChoice choice)
    {
        Preconditions.checkNotNull(choice, "Choice cannot be null");
        globalSettings.put(LOGO_CHOICE, choice.toString());
    }


    @Override
    public LogoChoice getLogoChoice()
    {
        LogoChoice logoOptionChoice = null;

        String logoChoice = (String) globalSettings.get(LOGO_CHOICE);
        if (logoChoice != null)
        {
            logoOptionChoice = LogoChoice.safeValueOf(logoChoice);
        }

        return logoOptionChoice != null ? logoOptionChoice : LogoChoice.JIRA;
    }


    @Override
    public void setFaviconChoice(LogoChoice choice)
    {
        Preconditions.checkNotNull(choice, "Choice cannot be null");
        globalSettings.put(FAVICON_CHOICE, choice.toString());
    }


    @Override
    public LogoChoice getFaviconChoice()
    {
        LogoChoice faviconOptionChoice = null;
        String faviconChoice = (String) globalSettings.get(FAVICON_CHOICE);
        if (faviconChoice != null)
        {
            faviconOptionChoice = LogoChoice.safeValueOf(faviconChoice);
        }

        return faviconOptionChoice != null ? faviconOptionChoice : LogoChoice.JIRA;
    }


    @Override
    public void reset()
    {
        setCustomizeColors(false);
        setLogoChoice(LogoChoice.JIRA);
        setFaviconChoice(LogoChoice.JIRA);
        resetDefaultFaviconUrl();
        resetDefaultLogoUrl();
    }

    @Override
    public void uploadDefaultLogo(BufferedImage image)
    {
        Preconditions.checkNotNull(image, "image cannot be null");
        Map<String, String> imageInfo = uploadService.uploadDefaultLogo(image);
        if (imageInfo != null)
        {
            setLogoDefaults(imageInfo.get("path"), imageInfo.get("width"), imageInfo.get("height"));
        }
    }

    @Override
    public void setDefaultLogo(String url, String width, String height)
    {
        Preconditions.checkNotNull(url, "URL cannot be null");
        globalSettings.put(LookAndFeelConstants.USING_CUSTOM_DEFAULT_LOGO, BooleanUtils.toStringTrueFalse(true));
        setLogoDefaults(url, width, height);
    }

    @Override
    public void resetDefaultLogo()
    {
        File file = new File(uploadService.getLogoDirectory(), LookAndFeelConstants.JIRA_SCALED_DEFAULT_LOGO_FILENAME);
        if (file.exists())
        {
            file.delete();
            globalSettings.put(LookAndFeelConstants.USING_CUSTOM_DEFAULT_FAVICON, BooleanUtils.toStringTrueFalse(false));
        }
    }

    @Override
    public void uploadDefaultFavicon(BufferedImage image)
    {
        Preconditions.checkNotNull(image, "image cannot be null");
        Map<String, String> imageInfo = uploadService.uploadDefaultFavicon(image);
        if (imageInfo != null)
        {
            setFaviconDefaults(imageInfo.get("path"), imageInfo.get("width"), imageInfo.get("height"));
        }
    }

    @Override
    public void setDefaultFavicon(String url, String width, String height)
    {
        Preconditions.checkNotNull(url, "url cannot be null");
        globalSettings.put(LookAndFeelConstants.USING_CUSTOM_DEFAULT_FAVICON, BooleanUtils.toStringTrueFalse(true));
        setFaviconDefaults(url, width, height);
    }

    @Override
    public void resetDefaultFavicon()
    {
        File file = new File(uploadService.getLogoDirectory(), LookAndFeelConstants.JIRA_SCALED_DEFAULT_FAVICON_FILENAME);
        if (file.exists())
        {
            file.delete();
            globalSettings.put(LookAndFeelConstants.USING_CUSTOM_DEFAULT_LOGO, BooleanUtils.toStringTrueFalse(false));
        }
    }

    @Override
    public boolean isUsingCustomDefaultLogo()
    {
        return ("true".equals(globalSettings.get(LookAndFeelConstants.USING_CUSTOM_DEFAULT_LOGO)));
    }

    @Override
    public boolean isUsingCustomDefaultFavicon()
    {
        return ("true".equals(globalSettings.get(LookAndFeelConstants.USING_CUSTOM_DEFAULT_FAVICON)));
    }

    @Override
    public String getDefaultCssLogoUrl()
    {
        if (isUsingCustomDefaultFavicon())
        {
            return (String)globalSettings.get(LookAndFeelConstants.CUSTOM_DEFAULT_LOGO_URL);
        }
        else
        {
            return LookAndFeelConstants.CSS_DEFAULTLOGO_URL;
        }
    }

    @Override
    public void resetDefaultFaviconUrl()
    {
        if (!"true".equals(globalSettings.get(LookAndFeelConstants.USING_CUSTOM_DEFAULT_FAVICON)))
        {
            globalSettings.remove(DEFAULT_FAVICON_URL);
        }
        else
        {
            globalSettings.put(DEFAULT_FAVICON_URL, LookAndFeelConstants.CUSTOM_DEFAULT_FAVICON_URL);
        }
    }

    @Override
    public void resetDefaultLogoUrl()
    {
        if (!"true".equals(globalSettings.get(LookAndFeelConstants.USING_CUSTOM_DEFAULT_LOGO)))
        {
            globalSettings.remove(DEFAULT_LOGO_URL);
        }
        else
        {
            globalSettings.put(DEFAULT_LOGO_URL, LookAndFeelConstants.CUSTOM_DEFAULT_LOGO_URL);
        }
    }

    private void setFaviconDefaults(String path, String width, String height)
    {
        globalSettings.put(LookAndFeelConstants.CUSTOM_DEFAULT_FAVICON_URL, path);
        globalSettings.put(LookAndFeelConstants.DEFAULT_FAVICON_WIDTH, width);
        globalSettings.put(LookAndFeelConstants.DEFAULT_FAVICON_HEIGHT, height);
        if (!"true".equals(globalSettings.get(LookAndFeelConstants.USING_CUSTOM_FAVICON)))
        {
            lookAndFeelBean.setFaviconUrl(path);
            lookAndFeelBean.setFaviconHiResUrl(path);
        }
    }

    private void setLogoDefaults(String path, String width, String height)
    {
        globalSettings.put(LookAndFeelConstants.CUSTOM_DEFAULT_LOGO_URL, path);
        globalSettings.put(LookAndFeelConstants.DEFAULT_LOGO_WIDTH, width);
        globalSettings.put(LookAndFeelConstants.DEFAULT_LOGO_HEIGHT, height);
        if (!"true".equals(globalSettings.get(LookAndFeelConstants.USING_CUSTOM_LOGO)))
        {
            lookAndFeelBean.setLogoUrl(path);
            lookAndFeelBean.setLogoHeight(height);
            lookAndFeelBean.setLogoWidth(width);
        }
    }


    @Override
    public void setBlackArrow(final boolean blackArrow)
    {
        globalSettings.put(LookAndFeelConstants.USING_BLACK_ARROW, String.valueOf(blackArrow));
    }

    @Override
    public boolean isBlackArrow()
    {
        Object o = globalSettings.get(LookAndFeelConstants.USING_BLACK_ARROW);
        if (o == null)
        {
            return DEFAULT_WHITE_ARROW;
        }
        return Boolean.parseBoolean(o.toString());
    }


}
