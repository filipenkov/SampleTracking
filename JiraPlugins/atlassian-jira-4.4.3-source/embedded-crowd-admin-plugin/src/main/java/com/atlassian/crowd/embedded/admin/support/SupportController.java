package com.atlassian.crowd.embedded.admin.support;

import com.atlassian.crowd.embedded.admin.util.HtmlEncoder;
import com.atlassian.crowd.embedded.api.CrowdDirectoryService;
import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.sal.api.user.UserManager;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class SupportController
{
    private final CrowdService crowdService;
    private final CrowdDirectoryService crowdDirectoryService;
    private final UserManager userManager;
    private final HtmlEncoder htmlEncoder;

    public SupportController(CrowdService crowdService, CrowdDirectoryService crowdDirectoryService, UserManager userManager, HtmlEncoder htmlEncoder)
    {
        this.crowdService = crowdService;
        this.crowdDirectoryService = crowdDirectoryService;
        this.userManager = userManager;
        this.htmlEncoder = htmlEncoder;
    }

    public ModelAndView directories(HttpServletRequest request, HttpServletResponse response) throws Exception
    {
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("supportInformation", getSupportInformation(request));
        // Add a reference to the context to itself and also add the request.  Needed to render web item links.
        model.put("context", model);
        model.put("req", request);
        model.put("htmlEncoder", htmlEncoder);
        return new ModelAndView("support-directories", model);
    }

    public void download(HttpServletRequest request, HttpServletResponse response) throws Exception
    {
        response.setContentType("text/plain; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=atlassian-directory-configuration.txt");
        response.getWriter().println(getSupportInformation(request));
        response.getWriter().flush();
    }

    private String getSupportInformation(HttpServletRequest request)
    {
        User currentUser = crowdService.getUser(userManager.getRemoteUsername(request));
        List<Directory> directories = crowdDirectoryService.findAllDirectories();
        return getSupportInformation(directories, currentUser);
    }

    private String getSupportInformation(List<Directory> directories, User currentUser)
    {
        SupportInformationBuilder builder = new SupportInformationBuilder();
        if (currentUser != null)
        {
            builder.addHeading("Current user");
            builder.addField("Directory ID", currentUser.getDirectoryId());
            builder.addField("Username", currentUser.getName());
            builder.addField("Display name", currentUser.getDisplayName());
            builder.addField("Email address", currentUser.getEmailAddress());
            builder.newLine();
        }
        if (directories != null)
        {
            builder.addHeading("Directories configured");
            for (Directory directory : directories)
            {
                builder.addField("Directory ID", directory.getId());
                builder.addField("Name", directory.getName());
                builder.addField("Active", directory.isActive());
                builder.addField("Type", directory.getType());
                builder.addField("Created date", directory.getCreatedDate());
                builder.addField("Updated date", directory.getUpdatedDate());
                builder.addField("Allowed operations", directory.getAllowedOperations());
                builder.addField("Implementation class", directory.getImplementationClass());
                builder.addField("Encryption type", directory.getEncryptionType());
                builder.addAttributes("Attributes", directory.getAttributes());
                builder.newLine();
            }
        }
        return builder.build();
    }
}
