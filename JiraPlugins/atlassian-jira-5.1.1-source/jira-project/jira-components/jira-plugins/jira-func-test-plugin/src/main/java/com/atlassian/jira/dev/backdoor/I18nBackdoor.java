package com.atlassian.jira.dev.backdoor;

import com.atlassian.jira.util.I18nHelper;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.util.Locale;

/**
 * Use this backdoor to gain access to the translation for an i18n key.
 * @since v5.0
 */
@Path("i18n")
public class I18nBackdoor
{
    private final I18nHelper.BeanFactory i18nHelper;

    public I18nBackdoor(final I18nHelper.BeanFactory i18nHelperFactory)
    {
        this.i18nHelper = i18nHelperFactory;
    }

    @GET
    @AnonymousAllowed
    public Response getText(@QueryParam ("key") String key, @QueryParam("locale") String locale)
    {
        final String language = locale.split("_")[0];
        final String country = locale.split("_")[1];
        return Response.ok(i18nHelper.getInstance(new Locale(language, country)).getText(key)).build();
    }
}
