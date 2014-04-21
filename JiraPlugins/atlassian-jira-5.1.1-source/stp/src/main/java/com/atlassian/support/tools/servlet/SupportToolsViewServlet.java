package com.atlassian.support.tools.servlet;

import java.io.IOException;
import java.net.URI;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.websudo.WebSudoManager;
import com.atlassian.sal.api.websudo.WebSudoSessionException;
import com.atlassian.support.tools.SimpleXsrfTokenGenerator;
import com.atlassian.support.tools.Stage;
import com.atlassian.support.tools.ValidationLog;
import com.atlassian.support.tools.action.SupportActionFactory;
import com.atlassian.support.tools.action.SupportToolsAction;
import com.atlassian.support.tools.action.impl.DefaultSupportActionFactory;
import com.atlassian.support.tools.action.impl.TabsAction;
import com.atlassian.support.tools.salext.SupportApplicationInfo;
import com.atlassian.support.tools.salext.mail.MailUtility;
import com.atlassian.support.tools.scheduler.SupportScheduledTaskControllerImpl;
import com.atlassian.templaterenderer.TemplateRenderer;


public class SupportToolsViewServlet extends HttpServlet
{
    static final String JIRA_SERAPH_SECURITY_ORIGINAL_URL = "os_security_originalurl";
    static final String CONF_SERAPH_SECURITY_ORIGINAL_URL = "seraph_originalurl";
    
	private static final Logger log = Logger.getLogger(SupportToolsViewServlet.class);
	private final TemplateRenderer renderer;
	private final SupportActionFactory factory;
	private final SupportApplicationInfo appInfo;
	private final UserManager userManager;
	private final LoginUriProvider loginUriProvider;
	private final WebSudoManager webSudoManager;

	private final SimpleXsrfTokenGenerator tokenGenerator;

	public SupportToolsViewServlet(UserManager userManager, LoginUriProvider loginUriProvider, TemplateRenderer renderer, ApplicationProperties applicationProperties, SupportApplicationInfo appInfo, MailUtility mailUtility, WebSudoManager webSudoManager, SupportScheduledTaskControllerImpl controller) throws GeneralSecurityException
	{
		this.userManager = userManager;
		this.loginUriProvider = loginUriProvider;
		this.renderer = renderer;
		this.appInfo = appInfo;
		this.webSudoManager = webSudoManager;
		
		this.tokenGenerator = new SimpleXsrfTokenGenerator();
		this.factory = new DefaultSupportActionFactory(appInfo, mailUtility, controller);
	}
	
	@Override
	public void init(ServletConfig config) throws ServletException
	{
		super.init(config);
		this.appInfo.initServletInfo(config);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		initializeHeader(resp);

		// Wrap this request in WebSudo to enforce new security constraints
        try 
        {
    		this.webSudoManager.willExecuteWebSudoRequest(req);

    		boolean isAdmin = performAdminChecks(req,resp);
    		
    		if (isAdmin ) 
    		{
    			// If we reach this point, we've successfully entered our credentials for WebSudo
    			Map<String,Object> context = prepareContext(req);
    			
    			SupportToolsAction action = (SupportToolsAction) context.get("action");
    			
    			// The token that's stored in the session
    			String sessionToken = this.tokenGenerator.generateToken(req);
    			String tokenName = this.tokenGenerator.getXsrfTokenName();
    			context.put("tokenName", tokenName);
    			String token = req.getParameter(tokenName);
    			context.put("token", sessionToken);
    			
    			if (this.tokenGenerator.validateToken(req, token)) 
    			{
    				displayResults(req, resp, context);
    			}
    			else 
    			{
    				// preserve form data for "retry"
    				context.put("existingParams", req.getParameterMap());
    				
    				if(action.getName().equals(TabsAction.ACTION_NAME))
    				{
    					this.renderer.render("/templates/xsrf-error.vm", context, resp.getWriter());
    				}
    				else
    				{
    					this.renderer.render("/templates/xsrf-error-body.vm", context, resp.getWriter());
    				}
    			}
    		}
        }
        catch(WebSudoSessionException wes)
        {
    		resp.sendError(HttpServletResponse.SC_FORBIDDEN);
        }
        finally {
        	resp.getWriter().flush();
        }
		
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		initializeHeader(resp);

		// Wrap this request in WebSudo to enforce new security constraints
        try {
    		this.webSudoManager.willExecuteWebSudoRequest(req);
 
            // If we reach this point, we've successfully entered our credentials for WebSudo
            Map<String,Object> context = prepareContext(req);

    		boolean isAdmin = performAdminChecks(req,resp);
    		
    		if (isAdmin ) {
    			// The token that's stored in the session
    			String sessionToken = this.tokenGenerator.generateToken(req);
    			String tokenName = this.tokenGenerator.getXsrfTokenName();
    			context.put("tokenName", tokenName);
    			context.put("token", sessionToken);
    			
    			displayResults(req, resp, context);
    		}
            
            resp.getWriter().close();
        }
        catch(WebSudoSessionException wes) 
        {
    		this.webSudoManager.enforceWebSudoProtection(req, resp);
        }
		
	}

	private boolean performAdminChecks(HttpServletRequest req, HttpServletResponse resp) throws IOException
	{
		String pathInfo = req.getPathInfo();
		String[] tokens = StringUtils.split(pathInfo, '/');
		String actionName = (tokens == null || tokens.length == 0) ? null : tokens[0];
		SupportToolsAction action = this.factory.getAction(actionName);
		
		String username = this.userManager.getRemoteUsername(req);
		if(username == null)
		{
			if(action.getName().equals(TabsAction.ACTION_NAME))
			{
				redirectToLogin(req, resp);
			}
			else
			{
				this.renderer.render("templates/ajax-not-logged-in.vm", prepareContext(req), resp.getWriter());
			}
			return false;
		}
		
		if(this.userManager.isSystemAdmin(username))
		{
			return true;
		}
		else
		{
			if(action.getName().equals(TabsAction.ACTION_NAME))
			{
				redirectToLogin(req, resp);
			}
			else
			{
				this.renderer.render("/templates/ajax-no-permission.vm", prepareContext(req), resp.getWriter());
			}
		}
		
		return false;
	}

	private Map<String, Object> prepareContext(HttpServletRequest req)
	{
		Map<String,Object> context = new HashMap<String,Object>();

		String pathInfo = req.getPathInfo();
		String[] tokens = StringUtils.split(pathInfo, '/');
		Stage stage = (tokens == null || tokens.length < 2) ? Stage.START : Stage.lookup(tokens[1]);
		context.put("stage", stage);

		String actionName = (tokens == null || tokens.length == 0) ? null : tokens[0];
		SupportToolsAction action = this.factory.getAction(actionName);
		context.put("action", action);

		if (action.getName().equals(TabsAction.ACTION_NAME)) {
			context.put("factory", this.factory);
		}
		
		// In case the Base URL is misconfigured, start with the request URI and
		// work backward
		String baseURL = this.appInfo.getBaseURL(req);
		context.put("servletHomePath", baseURL + req.getServletPath());
		context.put("info", this.appInfo);
		context.put("baseURL", this.appInfo.getBaseURL(req));

		return context;
	}

	protected void displayResults(HttpServletRequest req, HttpServletResponse resp, Map<String, Object> context) throws IOException
	{
		SupportToolsAction action = (SupportToolsAction) context.get("action");
		Stage stage = (Stage) context.get("stage");

		ValidationLog validationLog = new ValidationLog(this.appInfo);
		context.put("validationLog", validationLog);
		
		SafeHttpServletRequest safeReq = new SafeHttpServletRequestImpl(req);
		
		action.prepare(context, safeReq, validationLog);
		
		if(stage == Stage.EXECUTE)
		{
			action.validate(context, safeReq, validationLog);
			if(validationLog.hasErrors())
			{
				this.renderer.render(action.getErrorTemplatePath(), context, resp.getWriter());
			}
			else
			{
				try
				{
					action.execute(context, safeReq, validationLog);
					if(validationLog.hasErrors())
					{
						this.renderer.render(action.getErrorTemplatePath(), context, resp.getWriter());
					}
					else
					{
						this.renderer.render(action.getSuccessTemplatePath(), context, resp.getWriter());
					}
				}
				catch(Exception e)
				{
					log.error(e.getMessage(), e);
					validationLog.addError("Error rendering the page, check your logs for more details.");
					this.renderer.render(action.getErrorTemplatePath(), context, resp.getWriter());
				}
			}
		}
		else
			// shall we check for an unknown stage?
		{
			this.renderer.render(action.getStartTemplatePath(), context, resp.getWriter());
		}
	}

	protected void initializeHeader(HttpServletResponse resp)
	{
		resp.setHeader("Cache-Control", "no-cache"); // HTTP 1.1
		resp.setHeader("Pragma", "no-cache"); // HTTP 1.0
		resp.setDateHeader("Expires", 0); // prevents proxy server caching
		resp.setContentType("text/html;charset=utf-8");
	}

	/**
	 * Copied from our <a href=
	 * "http://confluence.atlassian.com/display/DEVNET/Plugin+tutorial+-+Writing+an+Admin+Configuration+Screen"
	 * >cross-product admin example code</a>.
	 * 
	 * */
	private void redirectToLogin(HttpServletRequest request, HttpServletResponse response) throws IOException
	{
        final URI uri = getUri(request);
        addSessionAttributes(request, uri.toASCIIString());
		response.sendRedirect(this.loginUriProvider.getLoginUri(getUri(request)).toASCIIString());
	}

	/**
	 * Copied from our <a href=
	 * "http://confluence.atlassian.com/display/DEVNET/Plugin+tutorial+-+Writing+an+Admin+Configuration+Screen"
	 * >cross-product admin example code</a>.
	 * 
	 * */
	private URI getUri(HttpServletRequest request)
	{
		StringBuffer builder = request.getRequestURL();
		if(request.getQueryString() != null)
		{
			builder.append("?");
			builder.append(request.getQueryString());
		}
		return URI.create(builder.toString());
	}
	
    private void addSessionAttributes(final HttpServletRequest request, final String uriString)
    {
        // UPM-637 - Seraph tries to be clever and if your currently logged in user is trying to access a
        // URL that does not have a Seraph role restriction then it will redirect to os_destination. In the case
        // of the UPM we do not want this behavior since we do have an elevated Seraph role but have not way to
        // programatically tell Seraph about it.
        // UPM-637 - this is the JIRA specific string to let Seraph know that it should re-show the login page
        request.getSession().setAttribute(JIRA_SERAPH_SECURITY_ORIGINAL_URL, uriString);
        // UPM-637 - this is the Confluence specific string to let Seraph know that it should re-show the login page
        request.getSession().setAttribute(CONF_SERAPH_SECURITY_ORIGINAL_URL, uriString);
    }

}
