/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package it.com.atlassian.jira.webtest.selenium.admin.imports.fogbugz;

import com.atlassian.jira.rest.client.JiraRestClient;
import com.atlassian.jira.rest.client.NullProgressMonitor;
import com.atlassian.jira.rest.client.domain.BasicComponent;
import com.atlassian.jira.rest.client.domain.BasicUser;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.junit.Assert;
import org.junit.Ignore;

@Ignore
public class FogbugzITUtil {

	public static void verifyComponentLeadImported(JiraRestClient restClient) {
		final Iterable<BasicComponent> components = restClient.getProjectClient().getProject("SAM", new NullProgressMonitor())
				.getComponents();
		final ImmutableMap<String, BasicComponent> componentIndex = Maps
				.uniqueIndex(components, new Function<BasicComponent, String>() {
					@Override
					public String apply(BasicComponent input) {
						return input.getName();
					}
				});
		final BasicComponent codeComponent = componentIndex.get("Code");
		final BasicUser codeLead = restClient.getComponentClient().getComponent(codeComponent.getSelf(), new NullProgressMonitor()).getLead();
		Assert.assertNotNull(codeLead);
		Assert.assertEquals("Wojtek Seliga", codeLead.getDisplayName());
	}
}
