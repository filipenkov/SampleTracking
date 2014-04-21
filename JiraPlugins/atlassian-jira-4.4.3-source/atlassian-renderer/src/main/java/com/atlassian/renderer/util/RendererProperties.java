package com.atlassian.renderer.util;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.log4j.Category;
import com.atlassian.core.util.ClassLoaderUtils;

/**
 * A class of contant values that can be overridden by a properties file.
 */
public class RendererProperties
{
    // Public properties that can be overridden by a properties file
    public static String EMOTICONS_PATH = "icons/emoticons/";
    public static String ICONS_PATH = "icons/";

    //all the i18n keys that need to be in the properties file
    public static String URL_LINK_TITLE = "renderer.external.link.title";
    public static String SITE_RELATIVE_LINK_TITLE = "renderer.site.relative.link.title";
    public static String RELATIVE_LINK_TITLE = "renderer.relative.link.title";
    public static String SEND_MAIL_TO = "renderer.send.mail.to";
    public static String TITLE_WITH_ANCHOR = "renderer.title.with.anchor";
    public static String ATTACHED_TO = "renderer.attached.to";
    public static String NEWS_ITEMS_FOR = "renderer.news.items.for";
    public static String NEWS_ITEMS_FOR_SPACEKEY = "renderer.news.items.for.spacekey";
    public static String CREATE_PAGE = "renderer.create.page";
    public static String CREATE_PAGE_IN_SPACE = "renderer.create.page.in.space";
    public static String EXTERNAL_SHORTCUT_LINK = "renderer.external.shortcut.link";
    public static String VIEW_SPACE = "renderer.view.space";
    public static String VIEW_PROFILE = "renderer.view.profile";


    // Internal variables
    private static final String propertiesFileName = "atlassian-renderer.properties";
    private static final Category log = Category.getInstance(RendererProperties.class);

    static
    {
        // read the properties file if present
        java.util.Properties props = new java.util.Properties();
        java.io.InputStream propsStream = null;

        try {
            // See if it is in the classpath
            propsStream = ClassLoaderUtils.getResourceAsStream(propertiesFileName, RendererProperties.class);
            props.load(propsStream);
        } catch (Throwable t) {
            log.info("The atlassian-renderer was unable to find the atlassian-renderer.properties on the classpath, using default property values.");
        }

        // update field values as appropriate from the properties file
        java.lang.reflect.Field[] fields = RendererProperties.class.getFields();
        for (int i = 0; i < fields.length; i++) {
            String name = fields[i].getName();
            String value = props.getProperty(name);
            if (value != null) {
                value = value.trim();
                Object oVal = ConvertUtils.convert(value, fields[i].getType());
                try
                {
                    fields[i].set(null, oVal);
                }
                catch (IllegalAccessException e)
                {
                    log.warn("The properties object of atlassian-renderer was unable to set the field: " + fields[i].getName());
                }
            }
        }
    }
}
