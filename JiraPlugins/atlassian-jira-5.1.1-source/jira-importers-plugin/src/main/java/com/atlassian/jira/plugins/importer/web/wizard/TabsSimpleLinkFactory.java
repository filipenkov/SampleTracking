/*
 * Copyright (c) 2011. Atlassian
 * All rights reserved
 */

package com.atlassian.jira.plugins.importer.web.wizard;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.plugin.webfragment.SimpleLinkFactory;
import com.atlassian.jira.plugin.webfragment.descriptors.SimpleLinkFactoryModuleDescriptor;
import com.atlassian.jira.plugin.webfragment.model.SimpleLink;
import com.atlassian.jira.plugin.webfragment.model.SimpleLinkImpl;
import com.atlassian.jira.plugins.importer.extensions.ExternalSystemImporterModuleDescriptor;
import com.atlassian.jira.plugins.importer.extensions.ImporterController;
import com.atlassian.jira.plugins.importer.web.ImporterLogsPage;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.plugin.PluginAccessor;
import com.google.common.base.Function;
import com.google.common.collect.Lists;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class TabsSimpleLinkFactory implements SimpleLinkFactory{
	private static final String STEP_I18N_PREFIX = "jira-importer-plugin.wizard.step.";
	public static final String STEP_I18N_FINAL = ImporterLogsPage.class.getSimpleName();

	private final JiraAuthenticationContext jiraAuthenticationContext;
	private final PluginAccessor pluginAccessor;

	public TabsSimpleLinkFactory(JiraAuthenticationContext jiraAuthenticationContext, PluginAccessor pluginAccessor) {
		this.jiraAuthenticationContext = jiraAuthenticationContext;
		this.pluginAccessor = pluginAccessor;
	}

	@Override
	public void init(SimpleLinkFactoryModuleDescriptor descriptor) {
	}

	@Override
	public List<SimpleLink> getLinks(User user, Map<String, Object> params) {
        final HttpServletRequest request = ((HttpServletRequest) params.get("request"));
        if (request == null) {
            return Collections.emptyList();
        }
		final String externalSystemKey = request.getParameter("externalSystem");
		final ExternalSystemImporterModuleDescriptor moduleDescriptor;
		try {
			moduleDescriptor = (ExternalSystemImporterModuleDescriptor) pluginAccessor.getEnabledPluginModule(externalSystemKey);
			if (moduleDescriptor == null) {
				return Collections.emptyList();
			}
		} catch (IllegalArgumentException e) {
			return Collections.emptyList();
		}

		final ImporterController controller = moduleDescriptor.getModule();
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
                    Collections.<String, String>emptyMap(),
					"#",
					null);
			}
		});
	}

	public I18nHelper getI18nHelper() {
		return jiraAuthenticationContext.getI18nHelper();
	}
}
