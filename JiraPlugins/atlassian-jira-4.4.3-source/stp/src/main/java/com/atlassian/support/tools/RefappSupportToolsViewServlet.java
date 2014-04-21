package com.atlassian.support.tools;

import java.io.IOException;
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

import com.atlassian.support.tools.action.SupportActionFactory;
import com.atlassian.support.tools.action.SupportToolsAction;
import com.atlassian.support.tools.action.impl.DefaultSupportActionFactory;
import com.atlassian.support.tools.action.impl.HomeAction;
import com.atlassian.support.tools.salext.SupportApplicationInfo;
import com.atlassian.support.tools.salext.mail.MailUtility;
import com.atlassian.templaterenderer.TemplateRenderer;


public class RefappSupportToolsViewServlet extends HttpServlet
{
    static final String JIRA_SERAPH_SECURITY_ORIGINAL_URL = "os_security_originalurl";
    static final String CONF_SERAPH_SECURITY_ORIGINAL_URL = "seraph_originalurl";
    
	private static final Logger log = Logger.getLogger(RefappSupportToolsViewServlet.class);
	private final TemplateRenderer renderer;
	private final SupportActionFactory factory;
	private final SupportApplicationInfo appInfo;

	private final SimpleXsrfTokenGenerator tokenGenerator;

	public RefappSupportToolsViewServlet(TemplateRenderer renderer, SupportApplicationInfo appInfo, MailUtility mailUtility) throws GeneralSecurityException
	{
		this.renderer = renderer;
		this.appInfo = appInfo;
		
		this.tokenGenerator = new SimpleXsrfTokenGenerator();
		this.factory = new DefaultSupportActionFactory(appInfo, mailUtility);
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

		// If we reach this point, we've successfully entered our credentials for WebSudo
		Map<String,Object> context = prepareContext(req);
		
		SupportToolsAction action = (SupportToolsAction) context.get("action");
		
		// The token that's stored in the session
		String sessionToken = this.tokenGenerator.generateToken(req);
		String tokenName = this.tokenGenerator.getXsrfTokenName();
		context.put("tokenName", tokenName);
		String token = req.getParameter(tokenName);
		context.put("token", sessionToken);
		
		if (this.tokenGenerator.validateToken(req, token)) {
			displayResults(req, resp, context);
		}
		else {
			// preserve form data for "retry"
			context.put("existingParams", req.getParameterMap());
			
			if(action.getName().equals(HomeAction.ACTION_NAME))
			{
				this.renderer.render("/templates/xsrf-error.vm", context, resp.getWriter());
			}
			else
			{
				this.renderer.render("/templates/xsrf-error-body.vm", context, resp.getWriter());
			}
		}
		
		resp.getWriter().close();
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		initializeHeader(resp);
		

		// If we reach this point, we've successfully entered our credentials for WebSudo
		Map<String,Object> context = prepareContext(req);
		
		// The token that's stored in the session
		String sessionToken = this.tokenGenerator.generateToken(req);
		String tokenName = this.tokenGenerator.getXsrfTokenName();
		context.put("tokenName", tokenName);
		context.put("token", sessionToken);
		
		displayResults(req, resp, context);
		
		resp.getWriter().close();		
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

		if (action.getName().equals(HomeAction.ACTION_NAME)) {
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
		
		if(stage == Stage.EXECUTE)
		{
			action.prepare(context, req, validationLog);
			action.validate(context, req, validationLog);
			if(validationLog.hasErrors())
			{
				this.renderer.render(action.getErrorTemplatePath(), context, resp.getWriter());
			}
			else
			{
				try
				{
					action.execute(context, req, validationLog);
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
					log.error(e.getMessage(),e);
					validationLog.addError("Error rendering template, check your logs for details.");
					this.renderer.render(action.getErrorTemplatePath(), context, resp.getWriter());
				}
			}
		}
		else
			// shall we check for an unknown stage?
		{
			action.prepare(context, req, validationLog);
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
}
