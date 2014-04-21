/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.csv;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.IssueConstant;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.plugins.importer.external.ExternalUtils;
import com.atlassian.jira.plugins.importer.external.beans.SetMultiHashMap;
import com.atlassian.jira.plugins.importer.imports.csv.mappers.DefaultExternalIssueMapper;
import com.atlassian.jira.plugins.importer.imports.importer.ImportLogger;
import com.atlassian.jira.plugins.importer.imports.importer.impl.AbstractConfigBean;
import com.atlassian.jira.plugins.importer.imports.importer.impl.ConsoleImportLogger;
import com.atlassian.jira.util.I18nHelper;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CsvConfigBean extends AbstractConfigBean implements CsvDateParser {
	public static final Character DEFAULT_DELIMITER = ',';

	public static final String DEFAULT_ENCODING = "UTF-8";

	private static final Logger log = Logger.getLogger(CsvConfigBean.class);

	public static final String DEFAULT_DATE_FORMAT = "yyyyMMddHHmmss";

	private static final String NULL_VALUE = "<<blank>>";

	private static final String DATE_FIELDS = "date.fields";

	public static final String EXTRA_USER_FIELDS = "user.extra.fields";

	private static final String[] DEFAULT_DATE_FIELDS = {"created", "duedate", "updated", "resolutiondate"};

	private static final String FIELD_MAPPING_PREFIX = "field.";
	private static final String CF_PREFIX = "customfield_";
	private static final String TYPE_SEPERATOR = ":";
	public static final String EXISTING_CUSTOM_FIELD = "existingCustomField";
	public static final String NEW_CUSTOM_FIELD = "newCustomField";
	public static final String MAP_VALUES_FIELD_NAME = "mapValuesForFields";

	public static final String NEW_CUSTOM_FIELD_TYPE = NEW_CUSTOM_FIELD + "type";

	private static final String VALUE_MAPPING_PREFIX = "value.";

	public static final String READ_FROM_CSV = "mapfromcsv";

	public static final String USER_EMAIL_SUFFIX = "user.email.suffix";

	public static final String DATE_IMPORT_FORMAT = "date.import.format";

	private static final String PROJECT_KEY = "project.key";

	private static final String PROJECT_NAME = "project.name";

	private static final String PROJECT_LEAD = "project.lead";

	private static final String PROJECT_URL = "project.url";
	private static final String PROJECT_DESCRIPTION = "project.description";

	private final Predicate VALID_FIELD = new Predicate() {
        private final String[] VALID_FIELDS = {
			DATE_IMPORT_FORMAT,
			USER_EMAIL_SUFFIX,
			DATE_FIELDS,
			EXTRA_USER_FIELDS
        };

        private final String[] VALID_PREFIX = {FIELD_MAPPING_PREFIX,
            VALUE_MAPPING_PREFIX,
            "duplicate.",
            "settings.advanced.mapper."
        };

		public boolean evaluate(Object object) {
			String s = (String) object;
			boolean valid = false;

			if (s != null) {
				if (StringUtils.indexOfAny(s, VALID_PREFIX) == 0) {
					valid = true;
				} else if (ArrayUtils.contains(VALID_FIELDS, s)) {
					valid = true;
				}
			}

			return valid;
		}
	};

	protected File importLocation;
	protected final Character delimiter;
	private final ExternalUtils utils;
	private final String encoding;

	private final Map<String, Object> config = Maps.newLinkedHashMap();
	private Map<String, Object> unmappedFields;

	private final Map<String, Boolean> headerRow = Maps.newHashMap();
	private final Map<String, String> sampleData = Maps.newHashMap();

	private CsvProvider csvImportFile;
	private SetMultiHashMap<String, String> currentValuesCache = new SetMultiHashMap<String, String>(
			HashMultimap.<String, String>create());
	private BiMap<String, String> valueMappingKeyCache = HashBiMap.create();

	private final I18nHelper i18nHelper;
	private boolean hasEmptyHeaders = false;
//	private Collection<FieldMapping> model = Lists.newArrayList();

	public CsvConfigBean(File csvImportFile, String encoding, Character delimiter, ExternalUtils utils)
			throws FileNotFoundException, ImportException {
		super(utils.getAuthenticationContext());

		this.delimiter = delimiter;
		this.encoding = StringUtils.defaultIfEmpty(encoding, DEFAULT_ENCODING);
		this.utils = utils;
		this.i18nHelper = utils.getAuthenticationContext().getI18nHelper();
		this.csvImportFile = new MindProdCsvProvider(csvImportFile, encoding, new HeaderRowCsvMapper(), delimiter);
		this.importLocation = csvImportFile;
		this.csvImportFile.startSession();

		final Collection<String> headerLine = this.csvImportFile.getHeaderLine();
		for(String headerName : headerLine) {
			headerRow.put(headerName, headerRow.containsKey(headerName));
			if (StringUtils.isEmpty(headerName)) {
				hasEmptyHeaders = true;
			}
		}

		final ListMultimap<String, String> line = this.csvImportFile.getNextLine();
		if (line == null) {
			throw new ImportException(
					i18nHelper.getText("jira-importer-plugin.csv.could.not.parse.second.line"));
		} else if (line.size() == 1 && "".equals(line.values().iterator().next())) {
			throw new ImportException(
					i18nHelper.getText("jira-importer-plugin.csv.second.line.empty"));
		}

		for(Map.Entry<String, String> field : line.entries()) {
			if (StringUtils.isNotEmpty(field.getValue())
					&& !sampleData.containsKey(field.getKey())) {
				sampleData.put(field.getKey(), field.getValue());
			}
		}

		this.csvImportFile.stopSession();

	}

	public boolean hasEmptyHeaders() {
		return hasEmptyHeaders;
	}

	public boolean isUsingMultipleColumns(String field) throws ImportException {
		return headerRow.get(field);
	}

    @Override
	public void copyFromProperties(final InputStream is) throws IOException {
		final ObjectMapper mapper = new ObjectMapper();
		final Map<String, Object> configuration = mapper.readValue(is, new TypeReference<Map<String, Object>>(){});
		copyFromProperties(configuration);
	}

	protected CustomFieldManager getCustomFieldManager() {
		return ComponentAccessor.getCustomFieldManager();
	}

	@Nullable
	private CustomField getCustomField(String customfieldId, ImportLogger log) {
		CustomField customFieldObject = null;
		try {
			customFieldObject = getCustomFieldManager().getCustomFieldObject(customfieldId);
		} catch (final NumberFormatException e) {
			// Don't do anything, expected for new stuff
		} catch (final Exception e) {
			log.warn(e, "Can't get custom field %s", customfieldId);
		}
		return customFieldObject;
	}

	protected void copyFromProperties(final Map<String, Object> configuration) {
		final Set<String> mapValues = Sets.newHashSet();
		for(Map.Entry<String, Object> entry : configuration.entrySet()) {
			final String key = entry.getKey();
			if (!(entry.getValue() instanceof String)) {
				continue;
			}

			final String value = (String) entry.getValue();

			if (isFieldMapping(key) && value.startsWith(CF_PREFIX)) {
				// Deal with custom field
				CustomField cf = getCustomField(value, ConsoleImportLogger.INSTANCE);
				if (cf != null) {
					config.put(key, EXISTING_CUSTOM_FIELD);
					config.put(key + EXISTING_CUSTOM_FIELD, cf.getId());
				} else {
					config.put(key, NEW_CUSTOM_FIELD);
					config.put(key + NEW_CUSTOM_FIELD, extractCustomFieldId(value));
					config.put(key + NEW_CUSTOM_FIELD_TYPE, extractCustomFieldType(value));
				}
			} else if (isValueMapping(key) && StringUtils.isEmpty(value)) {
				config.put(key, NULL_VALUE);
			} else {
				config.put(key, value);
			}

			// Add to value list
			if (isValueMapping(key)) {
				String fieldName = StringUtils.substringBetween(key, ".");
				if (StringUtils.isNotEmpty(fieldName)) {
					mapValues.add(fieldName);
				}
			}
		}

		// Save the mapping required
		String[] mappingRequired = new String[mapValues.size()];
		Iterator iterator = mapValues.iterator();
		for (int i = 0; i < mappingRequired.length; i++) {
			mappingRequired[i] = (String) iterator.next();
		}
		config.put(MAP_VALUES_FIELD_NAME, mappingRequired);

		initiailiseUnmappedFields();//filter out unmapped fields and populate the {@link #unmappedFields}
	}

	@Override
	public void copyToNewProperties(Map<String, Object> newconfig) {
		// Copy all properties to the config file
		Set entries = config.entrySet();
		for (Iterator iterator = entries.iterator(); iterator.hasNext();) {
			Map.Entry entry = (Map.Entry) iterator.next();
			String key = (String) entry.getKey();
			Object value = entry.getValue();

			if (value instanceof String) {
				String sValue = (String) value;
				if (StringUtils.isNotEmpty(sValue)) {
					// Deal with the custom field stuff
					if (sValue.equals(NEW_CUSTOM_FIELD)) {
						String newValue = translateNewCfMapping(key);

						newconfig.put(key, newValue);
					} else if (sValue.equals(EXISTING_CUSTOM_FIELD)) {
						newconfig.put(key, getExistingCfNameValue(key));
					} else if (!key.endsWith(EXISTING_CUSTOM_FIELD) &&
							!key.endsWith(NEW_CUSTOM_FIELD) &&
							!key.endsWith(NEW_CUSTOM_FIELD_TYPE)) {
						// Is normal value
						if (isValueMapping(key) && NULL_VALUE.equals(sValue)) {
							newconfig.put(key, "");
						} else if (isFieldMapping(key) && NULL_VALUE.equals(sValue)) {
							// Do nudda
						} else {
							newconfig.put(key, value);
						}
					}
				}
			} else if (value != null) {
				log.debug("Value is of invalid type!");
				log.debug("key: " + key);
				log.debug("value: " + value);
				log.debug("value.class: " + value.getClass());
			}
		}

		// Deal with date fields
		Collection dateFields = getDateFields();
		if (dateFields != null) {
			newconfig.remove(DATE_FIELDS);
			for (Iterator iterator = dateFields.iterator(); iterator.hasNext();) {
				Object o = iterator.next();
				newconfig.put(DATE_FIELDS, o);
			}
		}

		// Deal with user fields
		Collection<String> userFields = getExtraUserFields();
		if (userFields != null) {
			newconfig.remove(EXTRA_USER_FIELDS);
			for (String o : userFields) {
				newconfig.put(EXTRA_USER_FIELDS, o);
			}
		}
	}

	public String getProjectKey() {
		return getStringValue(PROJECT_KEY);
	}


	@Override
	public String getProjectKey(String projectName) {
		return getProjectKey();
	}

	@Override
	public String getProjectName(String projectName) {
		return StringUtils.defaultIfEmpty(getStringValue(PROJECT_NAME), projectName);
	}

	@Override
	public String getProjectLead(String projectName) {
		return getStringValue(PROJECT_LEAD);
	}

	public String getProjectDescription() {
		return getStringValue(PROJECT_DESCRIPTION);
	}

	public String getProjectUrl() {
		return getStringValue(PROJECT_URL);
	}


	@Nullable
    String translateNewCfMapping(String key) {
		final String value = (String) config.get(getNewCfName(key));
		if (value == null) {
			return null;
		}
		String newValue = CF_PREFIX + value;

		if (StringUtils.isNotEmpty((String) config.get(getNewCfType(key)))) {
			newValue = newValue + ":" + config.get(getNewCfType(key));
		}
		return newValue;
	}

	public void populateConfigBean(Map actionParams) {
		for (Map.Entry entry : (Set<Map.Entry>) actionParams.entrySet()) {
			String key = (String) entry.getKey();

			if (!VALID_FIELD.evaluate(key)) {
				continue;
			}

			if (key.endsWith(NEW_CUSTOM_FIELD)
					&& !NEW_CUSTOM_FIELD.equals(
					rationaliseActionParams(actionParams.get(StringUtils.removeEnd(key, NEW_CUSTOM_FIELD))))) {
				continue;
			}

			if (key.endsWith(EXISTING_CUSTOM_FIELD)
					&& !EXISTING_CUSTOM_FIELD.equals(
					rationaliseActionParams(actionParams.get(StringUtils.removeEnd(key, EXISTING_CUSTOM_FIELD))))) {
				continue;
			}

			final Object value = rationaliseActionParams(entry.getValue());

			// If is a value mapping, then translate the key mapping
			if (isValueMapping(key)) {
				key = valueMappingKeyCache.get(key);
			}

			config.put(key, value);
		}
	}

	public void populateFieldMappings(Map<String, String> actionParams) {

		Iterator<Map.Entry<String, Object>> configEntries = config.entrySet().iterator();
		while(configEntries.hasNext()) {
			if (configEntries.next().getKey().startsWith(FIELD_MAPPING_PREFIX)) {
				configEntries.remove();
			}
		}

		for (Map.Entry entry : actionParams.entrySet()) {
			String key = (String) entry.getKey();

			if (!VALID_FIELD.evaluate(key)) {
				continue;
			}

			if (key.endsWith(NEW_CUSTOM_FIELD)
					&& !NEW_CUSTOM_FIELD.equals(
					rationaliseActionParams(actionParams.get(StringUtils.removeEnd(key, NEW_CUSTOM_FIELD))))) {
				continue;
			}

			if (key.endsWith(EXISTING_CUSTOM_FIELD)
					&& !EXISTING_CUSTOM_FIELD.equals(
					rationaliseActionParams(actionParams.get(StringUtils.removeEnd(key, EXISTING_CUSTOM_FIELD))))) {
				continue;
			}

			final Object value = rationaliseActionParams(entry.getValue());

			// If is a value mapping, then translate the key mapping
			if (isValueMapping(key)) {
				key = valueMappingKeyCache.get(key);
			}

			if (!"false".equals(value)) {
				config.put(key, value);
			} else {
				config.remove(key);
			}
		}
	}

	/**
	 * Gets the unique field values for this given field
	 *
	 * @param fieldName
	 * @return current values for the field
	 */
	public Collection<String> getCurrentValues(String fieldName) {
		return currentValuesCache.get(fieldName);
	}

	/*
	 * Populates the Unique field values of the
	 */
	public void populateUniqueCsvFieldValues() throws ImportException {
		final List<String> newColumnsToCache = Lists.newArrayList();

		for (String fieldName : getMapValues()) {
			// Check if not already in cache
			if (!currentValuesCache.containsKey(fieldName)) {
				newColumnsToCache.add(fieldName);
			}
		}

		// Reads the Csv File
		csvImportFile.startSession();
		SetMultiHashMap<String, String> newCache = csvImportFile.readUniqueValues(newColumnsToCache);
		csvImportFile.stopSession();

		// Update the new columns
		Collection<Map.Entry<String, String>> entries = newCache.entries();
		for (Map.Entry<String, String> entry : entries) {
			List<String> value = Lists.newArrayList(entry.getValue());
			Collections.sort(value);
			currentValuesCache.putAll(entry.getKey(), value);
		}
	}

	@Nullable
	public Collection<String> getDateFields() {
		try {
			Collection<String> dateFields = Lists.newArrayList();

			for (Map.Entry<String, ?> entry : config.entrySet()) {
				String key = entry.getKey();
				if (isFieldMapping(key) && entry.getValue() instanceof String) {
					String value = (String) entry.getValue();

					if (ArrayUtils.contains(DEFAULT_DATE_FIELDS, value)) {
						dateFields.add(extractFieldName(key));
					} else if (isCustomFieldMapping(value)) {
						String type = getCustomFieldMappingType(key, value);
						if (StringUtils.contains(type, "date")) {
							dateFields.add(extractFieldName(key));
						}
					}
				}
			}
			return dateFields;
		} catch (Exception e) {
			log.warn("Unable to find date fields. Null being returned. ", e);
			return null;
		}
	}

	@Nullable
	private Collection<String> getExtraUserFields() {
		try {
			Collection<String> userFields = Lists.newArrayList();

			for (Map.Entry<String, ?> entry : config.entrySet()) {
				String key = entry.getKey();
				if (isFieldMapping(key) && entry.getValue() instanceof String) {
					String value = (String) entry.getValue();

					if (isCustomFieldMapping(value)) {
						String type = getCustomFieldMappingType(key, value);
						if (StringUtils.contains(type, "user")) {
							if (value.equals(NEW_CUSTOM_FIELD)) {
								userFields.add(translateNewCfMapping(key));
							} else if (value.equals(EXISTING_CUSTOM_FIELD)) {
								userFields.add(getExistingCfNameValue(key));
							}
						}
					}
				}
			}

			return userFields;
		} catch (Exception e) {
			log.warn("Unable to find user fields. Null being returned. ", e);
			return null;
		}
	}

	public boolean isInMapValues(String s) {
		return getMapValues().contains(s);
	}

	public boolean isSelectedValue(String csvHeaderName, String valueToTest) {
		if (StringUtils.isNotEmpty(csvHeaderName) && StringUtils.isNotEmpty(valueToTest)) {
			Object currentValue = getValue(getFieldName(csvHeaderName));

			if (currentValue instanceof List) {
				List currentList = (List) currentValue;
				return currentList.contains(valueToTest);
			} else {
				return valueToTest.equals(currentValue);
			}
		} else {
			return false;
		}
	}

	private boolean isFieldMapping(String key) {
		return key != null && key.startsWith(FIELD_MAPPING_PREFIX);
	}

	private boolean isCustomFieldMapping(String value) {
		return EXISTING_CUSTOM_FIELD.equals(value) || NEW_CUSTOM_FIELD.equals(value);
	}

	private String extractFieldName(String fullKey) {
		return StringUtils.substringAfter(fullKey, ".");
	}

	/**
	 * Returns the Type suffix of the custom field.
	 * Returns null if not a custom field value or CF is not a system custom field
	 *
	 * @param key
	 * @param value
	 * @return mapping type
	 */
	@Nullable
	private String getCustomFieldMappingType(String key, String value) {
		String returnType = null;
		if (EXISTING_CUSTOM_FIELD.equals(value)) {
			// Need to do lookups... hhmm..
			String customFieldId = getExistingCfNameValue(key);
			CustomField customField = getCustomFieldManager().getCustomFieldObject(customFieldId);
			if (customField != null && customField.getCustomFieldType().getKey()
					.startsWith(CustomFieldManager.PLUGIN_KEY)) {
				returnType = StringUtils.substringAfterLast(customField.getCustomFieldType().getKey(), ":");
			}
		} else if (NEW_CUSTOM_FIELD.equals(value)) {
			returnType = getNewCfTypeValue(key);
		}

		return returnType;
	}

	@Nullable
	public String getExistingCfNameValue(String key) {
		return (String) config.get(getExistingCfName(key));
	}

	public String getNewCfTypeValue(String key) {
		return (String) config.get(getNewCfType(key));
	}

	public String getNewCfNameValue(String key) {
		return (String) config.get(getNewCfName(key));
	}

	private boolean isValueMapping(String key) {
		return key != null && key.startsWith(VALUE_MAPPING_PREFIX);
	}


	public String getFieldName(String fieldName) {
		if (isFieldMapping(fieldName)) {
			return fieldName;
		} else {
			return FIELD_MAPPING_PREFIX + fieldName;
		}
	}


	public boolean isFieldMapped(String fieldName) {
		return getValue(getFieldName(fieldName)) != null;
	}

	@Nullable
	public String getFieldMapping(String fieldName) {
		return getStringValue(getFieldName(fieldName));
	}

	public String getValueFieldName(String fieldName) {
		return VALUE_MAPPING_PREFIX + fieldName;
	}

	public String getValueMappingName(String fieldName, String value) {
		return getValueFieldName(fieldName) + "." + value;
	}

	public String getConvertedValueMappingName(String fieldName, String value) {
		String uncoded = getValueMappingName(fieldName, value);
		if (!valueMappingKeyCache.containsValue(uncoded)) {
			valueMappingKeyCache.put(VALUE_MAPPING_PREFIX + String.valueOf(valueMappingKeyCache.size()), uncoded);
		}

		return valueMappingKeyCache.inverse().get(uncoded);
	}

	@Nullable
	public String getExistingCfName(String fieldName) {
		return getFieldName(fieldName) + EXISTING_CUSTOM_FIELD;
	}

	@Nullable
	public String getNewCfName(String fieldName) {
		return getFieldName(fieldName) + NEW_CUSTOM_FIELD;
	}

	@Nullable
	public String getNewCfType(String fieldName) {
		return getFieldName(fieldName) + NEW_CUSTOM_FIELD_TYPE;
	}

	public boolean isCfMapping(String key) {
		return isFieldMapping(key) && (key.endsWith(EXISTING_CUSTOM_FIELD) || key.endsWith(NEW_CUSTOM_FIELD) || key
				.endsWith(NEW_CUSTOM_FIELD_TYPE));
	}

	/**
	 * Initialise the {@link #unmappedFields} with the field mappings from the {@link #config} file which does not
	 * correspond to a header name in the CSV file. The mappings of the {@link #unmappedFields} before this method call
	 * will be lost. Note: custom field mappings are never added to the {@link #unmappedFields} (JRA-12124).
	 */
	private void initiailiseUnmappedFields() {
		unmappedFields = Maps.newLinkedHashMap();
		for (Iterator iterator = config.keySet().iterator(); iterator.hasNext();) {
			String key = (String) iterator.next();
			//custom field mappings should never be removed - JRA-12124
			if (isFieldMapping(key) && !isCfMapping(key)) {
				String fieldName = extractFieldName(key);
				if (!headerRow.keySet().contains(fieldName)) {
					unmappedFields.put(fieldName, config.get(key));
				}
			}
		}

		//remove the unmapped fields from the config as they are to be ignored
		for (Iterator iterator = unmappedFields.keySet().iterator(); iterator.hasNext();) {
			String fieldName = (String) iterator.next();
			config.remove(getFieldName(fieldName));
		}
	}

	public Map<String, Object> getUnmappedFields() {
		return unmappedFields;
	}

	public Set<String> getHeaderRow() {
		return headerRow.keySet();
	}

	public Map<String, String> getSampleData() {
		return sampleData;
	}

	public Set<String> getMapValues() {
		final String[] mapValues = (String[]) getValue(MAP_VALUES_FIELD_NAME);
		final Set<String> map = mapValues == null ? Sets.<String>newHashSet() : Sets.newHashSet(mapValues);
		final String statusField = getFieldWithValue("status");
		if (statusField != null) {
			map.add(statusField);
		}
		return map;
	}

	public void setMapValues(String[] mapValues) {
		Arrays.sort(mapValues);
		setValue(MAP_VALUES_FIELD_NAME, mapValues);
	}

	public boolean isReadingProjectsFromCsv() {
		Object readFromCsv = getValue(READ_FROM_CSV);
		return readFromCsv != null && Boolean.parseBoolean(readFromCsv.toString());
	}

	public void populateProjectMapping(boolean readingFromCsv, @Nullable String projectName,
			@Nullable String projectKey, @Nullable String projectLead) {
		config.put(READ_FROM_CSV, Boolean.toString(readingFromCsv));
		if (readingFromCsv) {
			config.remove(PROJECT_KEY);
			config.remove(PROJECT_NAME);
			config.remove(PROJECT_LEAD);
		} else {
			config.put(PROJECT_KEY, projectKey);
			config.put(PROJECT_NAME, projectName);
			config.put(PROJECT_LEAD, projectLead);
		}
	}

	public boolean isIssueConstantMappingSelected(IssueConstant ic, String mappingName, @Nullable String currentValue) {
		return currentValue == null ? false
				: isIssueConstantMappingSelected0(ic, getValue(getValueMappingName(mappingName, currentValue)),
						currentValue);
	}

	public boolean isMappedAsBlank(String mappingName, @Nullable String currentValue) {
		return NULL_VALUE.equals(getValue(getValueMappingName(mappingName, currentValue)));
	}

	protected boolean isIssueConstantMappingSelected0(IssueConstant ic, @Nullable Object value, @Nonnull String currentValue) {
		if (value != null) {
			if (value.equals(ic.getId())) {
				return true;
			}
		} else if (StringUtils.equalsIgnoreCase(ic.getNameTranslation(), currentValue)) {
			return true;
		}
		return false;
	}

	@Nullable
	public Object getValue(String key) {
		Object o = config.get(key);

		if (o instanceof List && ((List) o).size() == 1) {
			return ((List) o).get(0);
		}

		return o;
	}

	@Nullable
	public String getStringValue(String key) {
		Object o = getValue(key);
		return o instanceof String ? (String) o : null;
	}

	/**
	 * Get the field mappings options
	 *
	 * @return field mappings
	 */
	public Map<String, Map<String, String>> getFieldMappings(String field) {
		final Map<String, Map<String, String>> fieldMappings = Maps.newLinkedHashMap();

		boolean fieldHasManyColumns = headerRow.get(field);

		if (isReadingProjectsFromCsv() && !fieldHasManyColumns) {
			fieldMappings.put(getText("admin.csv.import.mappings.project.fields.header"), getProjectFields());
		}

		fieldMappings.put(getText("admin.csv.import.mappings.version.comp.header"), getVersionFields());

		final Map<String, String> issueFields = getIssueFields(fieldHasManyColumns);
		// Only include the timetracking fields if time tracking is enabled
		if (isTimetrackingEnabled()) {
			if (!fieldHasManyColumns) {
				issueFields.putAll(getTimeTrackingFields());
			}
			issueFields.put(IssueFieldConstants.WORKLOG, getText("jira-importer-plugin.csv.worklog.desc"));
		}
		fieldMappings.put(getText("admin.csv.import.mappings.issue.fields.header"), issueFields);

		if (utils.areSubtasksEnabled() && !fieldHasManyColumns) {
			final LinkedHashMap<String, String> entries = Maps.newLinkedHashMap();
			entries.put(DefaultExternalIssueMapper.SUBTASK_PARENT_ID, getText("jira-importer-plugin.csv.mappings.subtasks.parentid"));
			entries.put(DefaultExternalIssueMapper.ISSUE_ID, getText("jira-importer-plugin.csv.mappings.subtasks.issueid"));
			fieldMappings.put(getText("jira-importer-plugin.csv.mappings.subtasks"), entries);
		}


		fieldMappings.put(getCustomFieldsOptgroupTitle(), Collections.<String, String>emptyMap());
		fieldMappings.put("", Collections.singletonMap(NEW_CUSTOM_FIELD, getText("admin.csv.import.mappings.custom.fields.new")));

		return fieldMappings;
	}

	public File getImportLocation() {
		return importLocation;
	}

	public void setValue(String key, Object value) {
		config.put(key, value);
	}

	// -------------------------------------------------------------------------------- MIsc

	private boolean isTimetrackingEnabled() {
		return  utils.getApplicationProperties().getOption(APKeys.JIRA_OPTION_TIMETRACKING);
	}

	@Nullable
	private Object rationaliseActionParams(Object value) {
		if (value instanceof Object[]) {
			Object[] a = (Object[]) value;
			if (a.length == 0) {
				value = null;
			} else if (a.length == 1) {
				value = a[0];
			/*} else {
				value = Arrays.asList(a);*/
			}
		}
		return value;
	}

	/**
	 * Checks that there is a field (and not a custom field) mapped to the given value.
	 *
	 * @param value value to lookup
	 * @return true if there is a field mapped to the value. false otherwise or when value is null
	 */
	public boolean containsFieldWithValue(String value) {
		return getFieldWithValue(value) != null;
	}

	@Nullable
	public String getFieldWithValue(@Nullable String value) {
		//for simpler null guards, ignore any null values
		if (value == null) {
			return null;
		}

		for (Iterator iterator = config.keySet().iterator(); iterator.hasNext();) {
			final String key = (String) iterator.next();
			if (isFieldMapping(key) && !isCfMapping(key) && (value.equals(config.get(key)))) {
				return StringUtils.replaceOnce(key, FIELD_MAPPING_PREFIX, "");
			}
		}
		return null;
	}

	public String extractCustomFieldType(final String customfieldId) {
		return StringUtils.substringAfter(customfieldId, TYPE_SEPERATOR);
	}

	public String extractCustomFieldId(final String customfieldId) {
		String fieldId;
		if (StringUtils.contains(customfieldId, TYPE_SEPERATOR)) {
			fieldId = StringUtils.substringBetween(customfieldId, CF_PREFIX, TYPE_SEPERATOR);
		} else {
			fieldId = StringUtils.substringAfter(customfieldId, CF_PREFIX);
		}
		return fieldId;
	}

	protected Map<String, String> getProjectFields() {
		Map<String, String> PROJECT_FIELDS = Maps.newLinkedHashMap();
		PROJECT_FIELDS.put("project.name", getText("jira-importer-plugin.csv.project.name"));
		PROJECT_FIELDS.put("project.key", getText("jira-importer-plugin.csv.project.key"));
		PROJECT_FIELDS.put("project.lead", getText("jira-importer-plugin.csv.project.lead"));
		PROJECT_FIELDS.put("project.description", getText("jira-importer-plugin.csv.project.description"));
		PROJECT_FIELDS.put("project.url", getText("jira-importer-plugin.csv.project.url"));
		return PROJECT_FIELDS;
	}

	protected Map<String, String> getIssueFields(boolean multipleColumns) {
		final Map<String, String> res = Maps.newLinkedHashMap();
		if (!multipleColumns) {
			res.put(IssueFieldConstants.ASSIGNEE, getText("issue.field.assignee"));
		}
		res.put(IssueFieldConstants.ATTACHMENT, getText("issue.field.attachment"));
		res.put(IssueFieldConstants.COMMENT, getText("issue.field.comment.body"));
        res.put(IssueFieldConstants.LABELS, getText("issue.field.labels"));
		res.put(IssueFieldConstants.ISSUE_KEY, getText("jira-importer-plugin.csv.issue.key"));
		if (!multipleColumns) {
			res.put(IssueFieldConstants.CREATED, getText("issue.field.date.created"));
			res.put(IssueFieldConstants.UPDATED, getText("issue.field.date.modified"));
			res.put(IssueFieldConstants.DESCRIPTION, getText("issue.field.description"));
			res.put(IssueFieldConstants.DUE_DATE, getText("issue.field.duedate"));
			res.put(IssueFieldConstants.ENVIRONMENT, getText("issue.field.environment"));
			res.put(IssueFieldConstants.ISSUE_TYPE, getText("issue.field.issuetype"));
			res.put(IssueFieldConstants.PRIORITY, getText("issue.field.priority"));
			res.put(IssueFieldConstants.REPORTER, getText("issue.field.reporter"));
			res.put(IssueFieldConstants.RESOLUTION, getText("issue.field.resolution"));
			res.put(IssueFieldConstants.RESOLUTION_DATE, getText("issue.field.date.resolved"));
			res.put(IssueFieldConstants.STATUS, getText("issue.field.status"));
			res.put(IssueFieldConstants.SUMMARY, getText("issue.field.summary"));
			res.put(IssueFieldConstants.VOTES, getText("issue.field.vote"));
		}
		return res;
	}

	protected Map<String, String> getTimeTrackingFields() {
		//Map<String, String> res = getIssueFields(false);
		// Setup the map with the timetracking fields
		Map<String, String> res = Maps.newLinkedHashMap();
		res.put("timeoriginalestimate", getText("jira-importer-plugin.csv.original.estimate.desc"));
		res.put("timeestimate", getText("jira-importer-plugin.csv.remaining.estimate.desc"));
		res.put("timespent", getText("jira-importer-plugin.csv.time.spent.desc"));
		return res;
	}

	protected Map<String, String> getVersionFields() {
		Map<String, String> VERSION_FIELDS = Maps.newLinkedHashMap();
		VERSION_FIELDS.put(IssueFieldConstants.COMPONENTS, getText("issue.field.component"));
		VERSION_FIELDS.put(IssueFieldConstants.AFFECTED_VERSIONS, getText("issue.field.version"));
		VERSION_FIELDS.put(IssueFieldConstants.FIX_FOR_VERSIONS, getText("issue.field.fixversion"));
		return VERSION_FIELDS;
	}

	public String getCustomFieldsOptgroupTitle() {
		return getText("admin.csv.import.mappings.custom.fields.header");
	}

	private String getText(String s) {
		return i18nHelper.getText(s);
	}

	public Map<String,Object> getConfig() {
		final Map<String, Object> map = Maps.newHashMap();
		for(Map.Entry<String, Object> entry : config.entrySet()) {
			if (entry.getKey() != null && entry.getValue() != null) {
				map.put(entry.getKey(), entry.getValue());
			}
		}
		return map;
	}

	@Override
	public Date parseDate(String translatedValue) throws ParseException {
		final String dateFormat = StringUtils.defaultIfEmpty(getStringValue("date.import.format"), DEFAULT_DATE_FORMAT);
		return parseDate(translatedValue, dateFormat);
	}

	public static Date parseDate(String dateStr, String dateFormat) throws ParseException {
		final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat);
		return simpleDateFormat.parse(dateStr);
	}

	public String formatDate(Date date) {
		return utils.getAuthenticationContext().getOutlookDate().formatDateTimePicker(date);
	}

	public Character getDelimiter() {
		return delimiter;
	}

	public String getEncoding() {
		return encoding;
	}

}
