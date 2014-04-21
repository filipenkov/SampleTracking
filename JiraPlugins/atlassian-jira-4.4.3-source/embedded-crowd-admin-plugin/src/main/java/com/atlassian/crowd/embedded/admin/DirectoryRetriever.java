package com.atlassian.crowd.embedded.admin;

import com.atlassian.crowd.embedded.api.CrowdDirectoryService;
import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.exception.DirectoryNotFoundException;

import javax.servlet.http.HttpServletRequest;

/**
 * Retrieves {@link Directory} objects from Crowd, given a request with the "directoryId" form parameter.
 */
public final class DirectoryRetriever
{
    public static final String PARAMETER_NAME = "directoryId";
    private CrowdDirectoryService crowdDirectoryService;

    public boolean hasDirectoryId(HttpServletRequest request)
    {
        return request.getParameter(PARAMETER_NAME) != null && !request.getParameter(PARAMETER_NAME).equals("0");
    }

    public Directory getDirectory(HttpServletRequest request) throws DirectoryNotFoundException
    {
        try
        {
            long directoryId = Long.parseLong(request.getParameter(PARAMETER_NAME));
            Directory directory = crowdDirectoryService.findDirectoryById(directoryId);
            if (directory == null)
            {
                throw new DirectoryNotFoundException(directoryId);
            }
            return directory;
        }
        catch (NumberFormatException e)
        {
            throw new DirectoryNotFoundException(e);
        }
    }

    public void setCrowdDirectoryService(CrowdDirectoryService crowdDirectoryService)
    {
        this.crowdDirectoryService = crowdDirectoryService;
    }
}
