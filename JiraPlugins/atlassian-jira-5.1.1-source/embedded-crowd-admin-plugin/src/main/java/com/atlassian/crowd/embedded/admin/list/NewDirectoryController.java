package com.atlassian.crowd.embedded.admin.list;

import com.atlassian.plugin.web.springmvc.xsrf.XsrfTokenGenerator;
import com.atlassian.sal.api.ApplicationProperties;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractCommandController;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Handles the submission of the "new" directory form, redirecting to the appropriate form controller.
 */
public final class NewDirectoryController extends AbstractCommandController
{
    private String redirectPath = "";

    public void setRedirectPath(String redirectPath)
    {
        this.redirectPath = redirectPath;
    }

    protected ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors) throws Exception
    {
        NewDirectoryCommand newDirectoryCommand = (NewDirectoryCommand) command;
        NewDirectoryType directoryType = newDirectoryCommand.getNewDirectoryType();

        // This will be null if the session timed out. We need to handle that instead of just generating a null pointer exception
        if (directoryType == null)
        {
            return new ModelAndView(new RedirectView(request.getServletPath() + redirectPath, true));
        }
        return new ModelAndView(new RedirectView(request.getServletPath() + directoryType.getFormUrl(), true));
    }
}
