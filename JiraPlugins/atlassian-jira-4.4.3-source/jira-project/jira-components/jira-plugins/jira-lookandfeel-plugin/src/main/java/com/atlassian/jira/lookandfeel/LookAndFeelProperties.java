package com.atlassian.jira.lookandfeel;

import java.awt.image.BufferedImage;
import java.net.URL;

/**
 *
 *
 * @since v4.4
 */
public interface LookAndFeelProperties
{
    void setCustomizeColors(boolean value);

    boolean getCustomizeColors();

    void setLogoChoice(LogoChoice choice);

    LogoChoice getLogoChoice();

    void setFaviconChoice(LogoChoice choice);

    LogoChoice getFaviconChoice();

    void uploadDefaultLogo(BufferedImage image);

    void setDefaultLogo(String url, String width, String height);

    void resetDefaultLogo();

    void uploadDefaultFavicon(BufferedImage image);

    void setDefaultFavicon(String url, String width, String height);

    void resetDefaultFavicon();

    void reset();

    boolean isBlackArrow();

   void setBlackArrow(final boolean blackArrow);

    boolean isUsingCustomDefaultLogo();

    boolean isUsingCustomDefaultFavicon();

    String getDefaultCssLogoUrl();

    void resetDefaultFaviconUrl();

    void resetDefaultLogoUrl();
}
