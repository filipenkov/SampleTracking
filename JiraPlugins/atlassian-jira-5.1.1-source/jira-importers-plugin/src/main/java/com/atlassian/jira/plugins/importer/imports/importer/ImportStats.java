/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.importer;

import com.atlassian.core.util.DateUtils;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.collect.MapBuilder;
import com.google.common.collect.Sets;
import org.apache.commons.lang.time.StopWatch;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.util.LinkedHashSet;
import java.util.Map;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ImportStats {

	private final LinkedHashSet<String> failures = Sets.newLinkedHashSet();

	private final LinkedHashSet<String> warnings = Sets.newLinkedHashSet();

	public enum Stage { VALIDATE, USERS, CUSTOM_FIELDS, PROJECTS, ISSUES, LINKS };

	public enum State { RUNNING, SUCCESS, ERROR };

	boolean running;
	boolean aborted;

	@XmlTransient
    private final StopWatch timer = new StopWatch();

	@XmlTransient
	private Stage currentStep;

	private final Map<Stage, ImportStageStats> stages;

	public ImportStats(final DateUtils dateUtils, final I18nHelper i18nHelper) {
		stages = MapBuilder.<Stage, ImportStageStats>newBuilder()
				.add(Stage.VALIDATE, new ImportStageStats(dateUtils, i18nHelper))
				.add(Stage.USERS, new ImportStageStats(dateUtils, i18nHelper))
				.add(Stage.CUSTOM_FIELDS, new ImportStageStats(dateUtils, i18nHelper))
				.add(Stage.PROJECTS, new ImportStageStats(dateUtils, i18nHelper))
				.add(Stage.ISSUES, new ImportStageStats(dateUtils, i18nHelper))
				.add(Stage.LINKS, new ImportStageStats(dateUtils, i18nHelper))
				.toMap();

		currentStep = Stage.USERS;
    }

	public void incrementFailures(String format) {
		stages.get(currentStep).incrementFailures();
		failures.add(format);
	}

	public void incrementWarnings(String format) {
		stages.get(currentStep).incrementWarnings();
		warnings.add(format);
	}

	public LinkedHashSet<String> getFailures() {
		return failures;
	}

	public LinkedHashSet<String> getWarnings() {
		return warnings;
	}

	public void setTotalItems(long total) {
		stages.get(currentStep).setItemsToBeImported(total);
	}

	public long getTotalItems() {
		return stages.get(currentStep).getItemsToBeImported();
	}

	public int incrementProgress() {
		return stages.get(currentStep).incrementProgress();
	}

	public int incrementCreated() {
		return stages.get(currentStep).incrementCreated();
	}

    public void start() {
        timer.reset();
        timer.start();
    }

    public void stop() {
        timer.stop();
    }

	public void setRunning(boolean running) {
		this.running = running;
	}

	public void setAborted(boolean aborted) {
		this.aborted = aborted;
	}

	public void beginStep(Stage step) {
		currentStep = step;
		if (currentStep != null) {
			stages.get(currentStep).start();
		}
	}

	public void endStep() {
		if (currentStep != null) {
			stages.get(currentStep).stop();
		}
	}

	public ImportStageStats getStage(Stage stage) {
		return stages.get(stage);
	}

	public ImportStageStats getProjectsStage() {
		return getStage(Stage.PROJECTS);
	}

	public ImportStageStats getIssuesStage() {
		return getStage(Stage.ISSUES);
	}
}