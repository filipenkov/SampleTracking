/*
 * Copyright (c) 2011. Atlassian
 * All rights reserved
 */

package com.atlassian.jira.plugins.importer.web.wizard;

import com.atlassian.jira.plugin.webfragment.SimpleLinkFactory;
import com.atlassian.jira.plugin.webfragment.descriptors.SimpleLinkFactoryModuleDescriptor;
import com.atlassian.jira.plugin.webfragment.model.SimpleLink;
import com.atlassian.jira.plugin.webfragment.model.SimpleLinkImpl;
import com.atlassian.jira.plugins.importer.web.ImporterController;
import com.atlassian.jira.plugins.importer.web.ImporterControllerFactory;
import com.atlassian.jira.plugins.importer.web.ImporterLogsPage;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.I18nHelper;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.opensymphony.user.User;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class TabsSimpleLinkFactory implements SimpleLinkFactory{
	private final static String STEP_I18N_PREFIX = "jira-importer-plugin.wizard.step.";
	public final static String STEP_I18N_FINAL = ImporterLogsPage.class.getSimpleName();

	private static Logger log = Logger.getLogger(TabsSimpleLinkFactory.class);
	private final ImporterControllerFactory importerControllerFactory;
	private final JiraAuthenticationContext jiraAuthenticationContext;

	public TabsSimpleLinkFactory(ImporterControllerFactory importerControllerFactory,
			JiraAuthenticationContext jiraAuthenticationContext) {
		this.importerControllerFactory = importerControllerFactory;
		this.jiraAuthenticationContext = jiraAuthenticationContext;
	}

	@Override
	public void init(SimpleLinkFactoryModuleDescriptor descriptor) {
	}

	@Override
	public List<SimpleLink> getLinks(User user, Map<String, Object> params) {
		final ImporterController controller = importerControllerFactory.getController(((HttpServletRequest) params.get("request"))
				.getParameter("externalSystem"));
		final List<String> steps = Lists.newArrayList(controller.getSteps());
		steps.add(STEP_I18N_FINAL);
		final AtomicInteger stepNo = new AtomicInteger(1);
		return Lists.transform(steps, new Function<String, SimpleLink>() {
			@Override
			public SimpleLink apply(String step) {
				return new SimpleLinkImpl(step,
					String.valueOf(stepNo.getAndIncrement()) + ". " + getI18nHelper().getText(STEP_I18N_PREFIX + step),
					getI18nHelper().getText(STEP_I18N_PREFIX + step + ".title"),
					null,
					null,
					"#",
					null);
			}
		});
	}

	public I18nHelper getI18nHelper() {
		return jiraAuthenticationContext.getI18nHelper();
	}
}
