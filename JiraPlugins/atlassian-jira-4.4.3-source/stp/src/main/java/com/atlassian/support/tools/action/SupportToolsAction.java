package com.atlassian.support.tools.action;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.atlassian.support.tools.ValidationLog;
import com.atlassian.templaterenderer.RenderingException;

public interface SupportToolsAction extends Validateable
{
	/**
	 * @return The distinct name for this module, used in constructing the module ID.  Must be unique.
	 */
	String getName();

	/**
	 * @return The category (tab group) this action belongs to.
	 */
	String getCategory();
	
	/**
	 * @return The template to use if the "execute" phase completes successfully.
	 */
	String getSuccessTemplatePath();

	/**
	 * @return The template to use if there are errors during the "execute" phase.
	 */
	String getErrorTemplatePath();

	/**
	 * @return The template to use during the "view" phase
	 */
	String getStartTemplatePath();

	/**
	 * @return the title (or i18n key for the title) of this module
	 */
	String getTitle();

	/**
	 * @return a new instance of this SupportToolsAction.
	 */
	SupportToolsAction newInstance();

	/**
	 * A setup method that's run before both the "view" and "execute" phases.
	 * @param context The context that will be exposed to the velocity templates.
	 * @param request The incoming HttpServletRequest object.
	 * @param validationLog The ValidationLog object used to collect errors during either phase.
	 */
	void prepare(Map<String, Object> context, HttpServletRequest request, ValidationLog validationLog);

	/**
	 * @param context The context that will be exposed to the velocity templates.
	 * @param req The incoming HttpServletRequest object.
	 * @param validationLog The ValidationLog object used to collect errors during either phase.
	 * @throws RenderingException
	 * @throws IOException
	 * @throws Exception
	 */
	void execute(Map<String, Object> context, HttpServletRequest req, ValidationLog validationLog) throws RenderingException, IOException, Exception;
}
