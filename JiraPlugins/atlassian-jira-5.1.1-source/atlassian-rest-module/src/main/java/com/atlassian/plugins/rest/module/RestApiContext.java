package com.atlassian.plugins.rest.module;

import com.google.common.base.Preconditions;
import org.apache.commons.lang.StringUtils;

import java.util.Set;

public class RestApiContext
{
    public static final String SLASH = "/";

    public final static String LATEST = SLASH + "latest";

    public final static String ANY_PATH_PATTERN = "/*";

    private final String restContext;
    private final String apiPath;
    private final ApiVersion version;
    private final Set<String> packages;

    public RestApiContext(String restContext, String apiContext, ApiVersion version, Set<String> packages)
    {
        this.restContext = prependSlash(Preconditions.checkNotNull(restContext));
        this.apiPath = prependSlash(Preconditions.checkNotNull(apiContext));
        this.version = Preconditions.checkNotNull(version);
        this.packages = Preconditions.checkNotNull(packages);
    }

    /**
     * @return the REST context, always starts with "/".
     */
    public String getRestContext()
    {
        return restContext;
    }

    /**
     * @return the API path, always starts with "/"
     */
    public String getApiPath()
    {
        return apiPath;
    }

    /**
     * @return the API version
     */
    public ApiVersion getVersion()
    {
        return version;
    }

    public String getPathToVersion()
    {
        return getPathToVersion(SLASH + version);
    }

    public String getPathToLatest()
    {
        return getPathToVersion(LATEST);
    }

    public String getPathToVersion(String version)
    {
        return restContext + apiPath + version;
    }

    private String prependSlash(String path)
    {
        return StringUtils.startsWith(path, SLASH) ? path : SLASH + path;
    }

    public Set<String> getPackages()
    {
        return packages;
    }
}
