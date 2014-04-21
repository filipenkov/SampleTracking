package com.atlassian.jira.plugins.workflow;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.issue.IssueConstant;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.priority.Priority;
import com.atlassian.jira.issue.resolution.Resolution;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.opensymphony.module.propertyset.PropertySet;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * Created by IntelliJ IDEA.
 * User: jdoklovic
 * Date: 3/11/11
 * Time: 11:12 AM
 * To change this template use File | Settings | File Templates.
 */
public class MockStatusConstantsManager implements ConstantsManager {

    private HashMap<String, Status> statusMap;

    public MockStatusConstantsManager() {
        this.statusMap = new HashMap();

        statusMap.put("1", new MockStatus("1","Open","/images/icons/status_open.gif"));
        statusMap.put("3", new MockStatus("3","In Progress","/images/icons/status_inprogress.gif"));
        statusMap.put("4", new MockStatus("4","Reopened","/images/icons/status_reopened.gif"));
        statusMap.put("5", new MockStatus("5","Resolved","/images/icons/status_resolved.gif"));
        statusMap.put("6", new MockStatus("6","Closed","/images/icons/status_closed.gif"));
    }

    public Collection<GenericValue> getPriorities() {
        throw new UnsupportedOperationException();
    }

    public Collection<Priority> getPriorityObjects() {
        throw new UnsupportedOperationException();
    }

    public GenericValue getPriority(String id) {
        throw new UnsupportedOperationException();
    }

    public Priority getPriorityObject(String id) {
        throw new UnsupportedOperationException();
    }

    public String getPriorityName(String id) {
        throw new UnsupportedOperationException();
    }

    public GenericValue getDefaultPriority() {
        throw new UnsupportedOperationException();
    }

    public Priority getDefaultPriorityObject() {
        throw new UnsupportedOperationException();
    }

    public void refreshPriorities() {
        throw new UnsupportedOperationException();
    }

    public Collection<GenericValue> getResolutions() {
        throw new UnsupportedOperationException();
    }

    public Collection<Resolution> getResolutionObjects() {
        throw new UnsupportedOperationException();
    }

    public GenericValue getResolution(String id) {
        throw new UnsupportedOperationException();
    }

    public Resolution getResolutionObject(String id) {
        throw new UnsupportedOperationException();
    }

    public void refreshResolutions() {
        throw new UnsupportedOperationException();
    }

    public GenericValue getIssueType(String id) {
        throw new UnsupportedOperationException();
    }

    public IssueType getIssueTypeObject(String id) {
        throw new UnsupportedOperationException();
    }

    public Collection<GenericValue> getIssueTypes() {
        throw new UnsupportedOperationException();
    }

    public Collection<IssueType> getRegularIssueTypeObjects() {
        throw new UnsupportedOperationException();
    }

    public Collection<IssueType> getAllIssueTypeObjects() {
        throw new UnsupportedOperationException();
    }

    public List<GenericValue> getAllIssueTypes() {
        throw new UnsupportedOperationException();
    }

    public List<String> getAllIssueTypeIds() {
        throw new UnsupportedOperationException();
    }

    public Collection<GenericValue> getSubTaskIssueTypes() {
        throw new UnsupportedOperationException();
    }

    public Collection<IssueType> getSubTaskIssueTypeObjects() {
        throw new UnsupportedOperationException();
    }

    public List<GenericValue> getEditableSubTaskIssueTypes() {
        throw new UnsupportedOperationException();
    }

    public List<String> expandIssueTypeIds(Collection<String> issueTypeIds) {
        throw new UnsupportedOperationException();
    }

    public void refreshIssueTypes() {
        throw new UnsupportedOperationException();
    }

    public GenericValue createIssueType(String name, Long sequence, String style, String description, String iconurl) throws CreateException {
        throw new UnsupportedOperationException();
    }

    public void validateCreateIssueType(String name, String style, String description, String iconurl, ErrorCollection errors, String nameFieldName) {
        throw new UnsupportedOperationException();
    }

    public void updateIssueType(String id, String name, Long sequence, String style, String description, String iconurl) throws DataAccessException {
        throw new UnsupportedOperationException();
    }

    public void removeIssueType(String id) throws RemoveException {
        throw new UnsupportedOperationException();
    }

    public void storeIssueTypes(List<GenericValue> issueTypes) throws DataAccessException {
       throw new UnsupportedOperationException();
    }

    public GenericValue getStatus(String id) {
        throw new UnsupportedOperationException();
    }

    public Status getStatusObject(String id) {
        return statusMap.get(id);
    }

    public Collection<GenericValue> getStatuses() {
        throw new UnsupportedOperationException();
    }

    public Collection<Status> getStatusObjects() {
        return statusMap.values();
    }

    public void refreshStatuses() {
        throw new UnsupportedOperationException();
    }

    public Status getStatusByName(String name) {
        throw new UnsupportedOperationException();
    }

    public Status getStatusByTranslatedName(final String s)
    {
        throw new UnsupportedOperationException();
    }

    public GenericValue getConstant(String constantType, String id) {
        throw new UnsupportedOperationException();
    }

    public IssueConstant getConstantObject(String constantType, String id) {
        throw new UnsupportedOperationException();
    }

    public Collection getConstantObjects(String constantType) {
        throw new UnsupportedOperationException();
    }

    public List convertToConstantObjects(String constantType, Collection ids) {
        throw new UnsupportedOperationException();
    }

    public boolean constantExists(String constantType, String name) {
        throw new UnsupportedOperationException();
    }

    public GenericValue getConstantByName(String constantType, String name) {
        throw new UnsupportedOperationException();
    }

    public IssueConstant getIssueConstantByName(String constantType, String name) {
        throw new UnsupportedOperationException();
    }

    public IssueConstant getConstantByNameIgnoreCase(String constantType, String name) {
        throw new UnsupportedOperationException();
    }

    public IssueConstant getIssueConstant(GenericValue issueConstantGV) {
        throw new UnsupportedOperationException();
    }

    public void refresh() {
        throw new UnsupportedOperationException();
    }

    public IssueType insertIssueType(String s, Long aLong, String s1, String s2, String s3) throws CreateException
    {
        throw new UnsupportedOperationException();
    }

    private class MockStatus implements Status {

        private String id;
        private String name;
        private String iconUrl;

        private MockStatus(String id, String name, String iconUrl) {
            this.id = id;
            this.name = name;
            this.iconUrl = iconUrl;
        }

        public GenericValue getGenericValue() {
            throw new UnsupportedOperationException();
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            throw new UnsupportedOperationException();
        }

        public void setDescription(String description) {
           throw new UnsupportedOperationException();
        }

        public Long getSequence() {
            throw new UnsupportedOperationException();
        }

        public void setSequence(Long sequence) {
            throw new UnsupportedOperationException();
        }

        public String getIconUrl() {
            return iconUrl;
        }

        public String getIconUrlHtml() {
            throw new UnsupportedOperationException();
        }

        public void setIconUrl(String iconURL) {
            this.iconUrl = iconUrl;
        }

        public String getNameTranslation() {
            throw new UnsupportedOperationException();
        }

        public String getDescTranslation() {
            throw new UnsupportedOperationException();
        }

        public String getNameTranslation(String locale) {
            throw new UnsupportedOperationException();
        }

        public String getDescTranslation(String locale) {
            throw new UnsupportedOperationException();
        }

        public String getNameTranslation(I18nHelper i18n) {
            throw new UnsupportedOperationException();
        }

        public String getDescTranslation(I18nHelper i18n) {
            throw new UnsupportedOperationException();
        }

        public void setTranslation(String translatedName, String translatedDesc, String issueConstantPrefix, Locale locale) {
            throw new UnsupportedOperationException();
        }

        public void deleteTranslation(String issueConstantPrefix, Locale locale) {
            throw new UnsupportedOperationException();
        }

        public PropertySet getPropertySet() {
            throw new UnsupportedOperationException();
        }

        public int compareTo(Object o) {
            throw new UnsupportedOperationException();
        }
    }

}
