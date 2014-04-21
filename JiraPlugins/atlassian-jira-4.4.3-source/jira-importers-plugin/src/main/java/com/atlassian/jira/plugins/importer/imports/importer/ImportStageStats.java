/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.importer;

import com.atlassian.core.util.DateUtils;
import com.atlassian.jira.util.I18nHelper;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.time.StopWatch;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ImportStageStats {

	@XmlTransient
	private final DateUtils dateUtils;
	@XmlTransient
	private final I18nHelper i18nHelper;
	@XmlTransient
    private final StopWatch timer = new StopWatch();

	private long itemsToBeImported;
	private int warnings = 0;
	private int failures = 0;
	private Integer itemsImported = 0;
	private int itemsCreated = 0;

	@SuppressWarnings("unused")
	private ImportStats.State state;

	public ImportStageStats(final DateUtils dateUtils, final I18nHelper i18nHelper) {
		this.dateUtils = dateUtils;
		this.i18nHelper = i18nHelper;
    }

	public StopWatch getTimer() {
		return timer;
	}

	@SuppressWarnings("unused")
	@XmlElement
	public String getImportRate() {
		final int v = (int) (getIssuesImportedPerMilliSeconds() * 60000);
		return v + " " + i18nHelper.getText("jira-importer-plugin.external.logs.issuesperminute.abbrv");
	}

	private double getIssuesImportedPerMilliSeconds() {
		return getTotalImported() / getTimer().getTime();
	}

	@SuppressWarnings("unused")
	@XmlElement
	public String getEstimateRemaining() {
		if (getTotalImported() > 10) {
			final long issuesRemaining = itemsToBeImported - itemsImported;
			double secondsRemaining = issuesRemaining / getIssuesImportedPerMilliSeconds() / 1000;

			// add some overhead
			secondsRemaining = secondsRemaining * 1.4;
			final String formattedDate = getFormattedDate((long) secondsRemaining);
			return i18nHelper.getText("jira-importer-plugin.external.logs.remaining", formattedDate);
		} else {
			return "";
		}
	}

	private double getTotalImported() {
		return itemsImported;
	}

	private String getFormattedDate(final long secs) {
		if (secs < 60) {
			return i18nHelper.getText("core.dateutils.minute.less.than");
		} else {
			return dateUtils.formatDurationPretty(secs);
		}
	}

	public int incrementFailures() {
		return failures++;
	}

	public int incrementWarnings() {
		return warnings++;
	}

	public int incrementProgress() {
		return itemsImported++;
	}

	public int incrementCreated() {
		return itemsCreated++;
	}

    public void start() {
        timer.reset();
        timer.start();
		state = ImportStats.State.RUNNING;
    }

    public void stop() {
        timer.stop();
		state = warnings > 0 || failures > 0 ? ImportStats.State.ERROR : ImportStats.State.SUCCESS;
    }

	public void setItemsToBeImported(long toBeImported) {
		this.itemsToBeImported = toBeImported;
	}

	public long getItemsToBeImported() {
		return itemsToBeImported;
	}

	public int getItemsCreated() {
		return itemsCreated;
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	public int getItemsImported() {
		return itemsImported;
	}
}