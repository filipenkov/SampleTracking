package com.atlassian.gadgets.directory.internal;

import java.io.IOException;
import java.net.URI;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.templaterenderer.TemplateRenderer;

import static com.google.common.base.Preconditions.checkNotNull;

public class DirectoryConfigServlet extends HttpServlet
{
    private final TemplateRenderer renderer;
    private final UserManager userManager;
    private final LoginUriProvider loginUriProvider;

    public DirectoryConfigServlet(TemplateRenderer renderer, UserManager userManager, LoginUriProvider loginUriProvider)
    {
        this.renderer = checkNotNull(renderer, "renderer");
        this.userManager = checkNotNull(userManager, "userManager");
        this.loginUriProvider = checkNotNull(loginUriProvider, "loginUriProvider");
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        if (!isAdmin(request))
        {
            URI returnUri = URI.create(request.getServletPath() + (request.getQueryString() != null ? "?" + request.getQueryString() : ""));
            response.sendRedirect(loginUriProvider.getLoginUri(returnUri).toString());
            return;
        }
        response.setContentType("text/html;charset=utf-8");
        renderer.render("directory-config.vm", response.getWriter());
    }

    private boolean isAdmin(HttpServletRequest req)
    {
        String user = userManager.getRemoteUsername(req);
        return user != null && userManager.isSystemAdmin(user);
    }
}
