/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.importer.external;

import com.atlassian.jira.web.action.admin.customfields.CreateCustomField;

public class CustomFieldConstants {

	private static final String SEARCHER = "searcher";
	
	public static final String TEXT_FIELD_TYPE = CreateCustomField.FIELD_TYPE_PREFIX + "textfield";

	public static final String URL_FIELD_TYPE = CreateCustomField.FIELD_TYPE_PREFIX + "url";

	public static final String TEXT_FIELD_SEARCHER = CreateCustomField.FIELD_TYPE_PREFIX + "textsearcher";

	public static final String EXACT_TEXT_SEARCHER = CreateCustomField.FIELD_TYPE_PREFIX + "exacttextsearcher";

	public static final String FREE_TEXT_FIELD_TYPE = CreateCustomField.FIELD_TYPE_PREFIX + "textarea";
	
	public static final String DATE_FIELD_TYPE = CreateCustomField.FIELD_TYPE_PREFIX + "date";

	public static final String DATETIME_FIELD_TYPE = CreateCustomField.FIELD_TYPE_PREFIX + "datetime";

	public static final String DATETIME_FIELD_SEARCHER = CreateCustomField.FIELD_TYPE_PREFIX + "datetimerange";

	public static final String DATE_PICKER_FIELD_TYPE = CreateCustomField.FIELD_TYPE_PREFIX + "datepicker";

	public static final String DATE_FIELD_SEARCHER = CreateCustomField.FIELD_TYPE_PREFIX + "daterange";

	public static final String SELECT_FIELD_TYPE = CreateCustomField.FIELD_TYPE_PREFIX + "select";

	public static final String SELECT_FIELD_SEARCHER = SELECT_FIELD_TYPE + SEARCHER;

	public static final String USER_PICKER_FIELD_TYPE = CreateCustomField.FIELD_TYPE_PREFIX + "userpicker";

	public static final String USER_PICKER_SEARCHER = USER_PICKER_FIELD_TYPE + SEARCHER;

	public static final String MULTISELECT_FIELD_TYPE = CreateCustomField.FIELD_TYPE_PREFIX + "multiselect";

	public static final String MULTISELECT_FIELD_SEARCHER = MULTISELECT_FIELD_TYPE + SEARCHER;

	public static final String NUMBER_FIELD_TYPE = CreateCustomField.FIELD_TYPE_PREFIX + "float";

	public static final String NUMBER_FIELD_SEARCHER = CreateCustomField.FIELD_TYPE_PREFIX + "exactnumber";

	public static final String NUMBER_RANGE_FIELD_SEARCHER = CreateCustomField.FIELD_TYPE_PREFIX + "numberrange";

	public static final String MULTICHECKBOXES_FIELD_TYPE = CreateCustomField.FIELD_TYPE_PREFIX + "multicheckboxes";

	public static final String MULTICHECKBOXES_FIELD_SEARCHER = CreateCustomField.FIELD_TYPE_PREFIX + "checkboxsearcher";

	public static final String RADIO_FIELD_TYPE = CreateCustomField.FIELD_TYPE_PREFIX + "radiobuttons";

	public static final String RADIO_FIELD_SEARCHER = CreateCustomField.FIELD_TYPE_PREFIX + "radiosearcher";

	public static final String PLUGIN_KEY_PREFIX = "com.atlassian.jira.plugins.jira-importers-plugin:";

	public static final String BUGZILLA_ID_TYPE = PLUGIN_KEY_PREFIX + "bug-importid";

	public static final String BUGZILLA_ID_SEARCHER = CreateCustomField.FIELD_TYPE_PREFIX + "exactnumber";

	public static final String SINGLE_VERSION_PICKER_TYPE = CreateCustomField.FIELD_TYPE_PREFIX + "version";

	public static final String VERSION_PICKER_TYPE = CreateCustomField.FIELD_TYPE_PREFIX + "multiversion";

	public static final String GH_RANKING_FIELD_TYPE = "com.pyxis.greenhopper.jira:greenhopper-ranking";
}
