/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.importer.imports.mantis;

import com.google.common.collect.Maps;

import java.util.HashMap;

public class MantisFieldConstants {
	private static final HashMap<String, String> priorities;
	private static final HashMap<String, String> severities;
	private static final HashMap<String, String> resolutions;
	private static final HashMap<String, String> statuses;

    private static final String CUSTOM = "custom_";
	/*
	$s_reproducibility_enum_string = '10:always,30:sometimes,50:random,70:have not tried,90:unable to reproduce,100:N/A';
	$s_projection_enum_string = '10:none,30:tweak,50:minor fix,70:major rework,90:redesign';
	$s_eta_enum_string = '10:none,20:< 1 day,30:2-3 days,40:< 1 week,50:< 1 month,60:> 1 month';
	$s_sponsorship_enum_string = '0:Unpaid,1:Requested,2:Paid';
	*/

	public static final String PRIORITY_NONE = "none";
	public static final String PRIORITY_LOW = "low";
	public static final String PRIORITY_NORMAL = "normal";
	public static final String PRIORITY_HIGH = "high";
	public static final String PRIORITY_URGENT = "urgent";
	public static final String PRIORITY_IMMEDIATE = "immediate";

	public static final String SEVERITY_FEATURE = "feature";
	public static final String SEVERITY_TRIVIAL = "trivial";
	public static final String SEVERITY_TEXT = "text";
	public static final String SEVERITY_TWEAK = "tweak";
	public static final String SEVERITY_MINOR = "minor";
	public static final String SEVERITY_MAJOR = "major";
	public static final String SEVERITY_CRASH = "crash";
	public static final String SEVERITY_BLOCK = "block";

	public static final String RESOLUTION_OPEN_ID = "10";

	public static final String RESOLUTION_REOPENED_ID = "30";

	static {
		priorities = Maps.newHashMap();
		priorities.put(RESOLUTION_OPEN_ID, PRIORITY_NONE);
		priorities.put("20", PRIORITY_LOW);
		priorities.put(RESOLUTION_REOPENED_ID, PRIORITY_NORMAL);
		priorities.put("40", PRIORITY_HIGH);
		priorities.put("50", PRIORITY_URGENT);
		priorities.put("60", PRIORITY_IMMEDIATE);
	}

	static {
		severities = Maps.newHashMap();
		severities.put(RESOLUTION_OPEN_ID, SEVERITY_FEATURE);
		severities.put("20", SEVERITY_TRIVIAL);
		severities.put(RESOLUTION_REOPENED_ID, SEVERITY_TEXT);
		severities.put("40", SEVERITY_TWEAK);
		severities.put("50", SEVERITY_MINOR);
		severities.put("60", SEVERITY_MAJOR);
		severities.put("70", SEVERITY_CRASH);
		severities.put("80", SEVERITY_BLOCK);
	}

	public static final String RESOLUTION_OPEN = "open";

	public static final String RESOLUTION_FIXED = "fixed";

	public static final String RESOLUTION_REOPENED = "reopened";

	public static final String RESOLUTION_UNABLE_TO_REPRODUCE = "unable to reproduce";

	public static final String RESOLUTION_NOT_FIXABLE = "not fixable";

	public static final String RESOLUTION_DUPLICATE = "duplicate";

	public static final String RESOLUTION_NO_CHANGE_REQUIRED = "no change required";

	public static final String RESOLUTION_SUSPENDED = "suspended";

	public static final String RESOLUTION_WONT_FIX = "won\'t fix";

	static {
		resolutions = Maps.newHashMap();
		resolutions.put(RESOLUTION_OPEN_ID, RESOLUTION_OPEN);
		resolutions.put("20", RESOLUTION_FIXED);
		resolutions.put(RESOLUTION_REOPENED_ID, RESOLUTION_REOPENED);
		resolutions.put("40", RESOLUTION_UNABLE_TO_REPRODUCE);
		resolutions.put("50", RESOLUTION_NOT_FIXABLE);
		resolutions.put("60", RESOLUTION_DUPLICATE);
		resolutions.put("70", RESOLUTION_NO_CHANGE_REQUIRED);
		resolutions.put("80", RESOLUTION_SUSPENDED);
		resolutions.put("90", RESOLUTION_WONT_FIX);
	}

	public static final String STATUS_NEW = "new";

	public static final String STATUS_FEEDBACK = "feedback";

	public static final String STATUS_ACKNOWLEDGED = "acknowledged";

	public static final String STATUS_CONFIRMED = "confirmed";

	public static final String STATUS_ASSIGNED = "assigned";

	public static final String STATUS_RESOLVED = "resolved";

	public static final String STATUS_CLOSED = "closed";

	static {
		statuses = Maps.newHashMap();
		statuses.put(RESOLUTION_OPEN_ID, STATUS_NEW);
		statuses.put("20", STATUS_FEEDBACK);
		statuses.put(RESOLUTION_REOPENED_ID, STATUS_ACKNOWLEDGED);
		statuses.put("40", STATUS_CONFIRMED);
		statuses.put("50", STATUS_ASSIGNED);
		statuses.put("80", STATUS_RESOLVED);
		statuses.put("90", STATUS_CLOSED);
	}

	public static String getPriorityName(String id) {
		return  priorities.containsKey(id) ? priorities.get(id) : (CUSTOM + id);
	}

	public static String getSeverityName(String id) {
		return severities.containsKey(id) ? severities.get(id) : (CUSTOM + id);
	}

	public static String getStatusName(String id) {
		return statuses.containsKey(id) ? statuses.get(id) : (CUSTOM + id);
	}

	public static String getResolutionName(String id) {
		return resolutions.containsKey(id) ? resolutions.get(id) : (CUSTOM + id);
	}
}
