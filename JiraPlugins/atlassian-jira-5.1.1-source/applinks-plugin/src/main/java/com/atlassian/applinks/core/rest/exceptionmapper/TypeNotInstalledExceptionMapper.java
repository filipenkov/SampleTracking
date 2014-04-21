package com.atlassian.applinks.core.rest.exceptionmapper;

import com.atlassian.applinks.api.TypeNotInstalledException;
import com.atlassian.sal.api.message.I18nResolver;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import static com.atlassian.applinks.core.rest.util.RestUtil.badRequest;

@Provider
public class TypeNotInstalledExceptionMapper implements ExceptionMapper<TypeNotInstalledException>
{
    private I18nResolver i18nResolver;

    public TypeNotInstalledExceptionMapper(@Context I18nResolver i18nResolver)
    {
        this.i18nResolver = i18nResolver;
    }

    public Response toResponse(final TypeNotInstalledException e)
    {
        return badRequest(i18nResolver.getText("applinks.type.not.installed", e.getType()));
    }
}
