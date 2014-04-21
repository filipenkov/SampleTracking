package com.atlassian.jira.config;

import com.atlassian.core.ofbiz.util.EntityUtils;
import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.cache.LazyLoadingCache;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.extension.Startable;
import com.atlassian.jira.issue.IssueConstant;
import com.atlassian.jira.issue.TextIssueConstant;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.issuetype.IssueTypeImpl;
import com.atlassian.jira.issue.priority.Priority;
import com.atlassian.jira.issue.priority.PriorityImpl;
import com.atlassian.jira.issue.resolution.Resolution;
import com.atlassian.jira.issue.resolution.ResolutionImpl;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.issue.status.StatusImpl;
import com.atlassian.jira.ofbiz.FieldMap;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.web.action.admin.translation.TranslationManager;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.EntityUtil;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

public class DefaultConstantsManager implements ConstantsManager, Startable
{
    private static final Logger log = Logger.getLogger(DefaultConstantsManager.class);
    private static final List<String> DEFAULT_CONSTANTS_SORT = Collections.singletonList("sequence ASC");
    private static final List<String> DEFAULT_ISSUE_TYPE_SORT = ImmutableList.of("style", "name");

    private final LazyLoadingCache<ConstantsCache<Priority>> priorityCache = new LazyLoadingCache<ConstantsCache<Priority>>(new PriorityCacheLoader());
    private final LazyLoadingCache<ConstantsCache<Resolution>> resolutionCache = new LazyLoadingCache<ConstantsCache<Resolution>>(new ResolutionCacheLoader());
    private final LazyLoadingCache<ConstantsCache<Status>> statusCache = new LazyLoadingCache<ConstantsCache<Status>>(new StatusCacheLoader());
    private final LazyLoadingCache<IssueTypeCache> issueTypeCache = new LazyLoadingCache<IssueTypeCache>(new IssueTypeCacheLoader());

    private final IssueConstant UNRESOLVED_RESOLUTION;

    private JiraAuthenticationContext authenticationContext;
    private OfBizDelegator ofBizDelegator;
    private final EventPublisher eventPublisher;
    private final TranslationManager translationManager;

    public DefaultConstantsManager(JiraAuthenticationContext authenticationContext, TranslationManager translationManager,
            OfBizDelegator ofBizDelegator, final EventPublisher eventPublisher)
    {
        this.translationManager = translationManager;
        this.authenticationContext = authenticationContext;
        this.ofBizDelegator = ofBizDelegator;
        this.eventPublisher = eventPublisher;
        UNRESOLVED_RESOLUTION = new TextIssueConstant("common.status.unresolved", "common.status.unresolved", null, authenticationContext);
    }

    public void start() throws Exception
    {
        eventPublisher.register(this);
    }

    @SuppressWarnings ({ "UnusedDeclaration" })
    @EventListener
    public void onClearCache(ClearCacheEvent ignored)
    {
        refresh();
    }

    public Collection<GenericValue> getStatuses()
    {
        return statusCache.getData().getGenericValues();
    }

    public Collection<Status> getStatusObjects()
    {
        return statusCache.getData().getObjects();
    }

    public GenericValue getStatus(String id)
    {
        return convertToGenericValue(getStatusObject(id));
    }

    public Status getStatusObject(String id)
    {
        return statusCache.getData().getObject(id);
    }

    public void refreshStatuses()
    {
        statusCache.reload();
    }

    public GenericValue getConstant(String constantType, String id)
    {
        if (PRIORITY_CONSTANT_TYPE.equalsIgnoreCase(constantType))
        {
            return getPriority(id);
        }
        else if (STATUS_CONSTANT_TYPE.equalsIgnoreCase(constantType))
        {
            return getStatus(id);
        }
        else if (RESOLUTION_CONSTANT_TYPE.equalsIgnoreCase(constantType))
        {
            return getResolution(id);
        }
        else if (ISSUE_TYPE_CONSTANT_TYPE.equalsIgnoreCase(constantType))
        {
            return getIssueType(id);
        }

        return null;
    }

    public IssueConstant getConstantObject(String constantType, String id)
    {
        if (PRIORITY_CONSTANT_TYPE.equalsIgnoreCase(constantType))
        {
            return getPriorityObject(id);
        }
        else if (STATUS_CONSTANT_TYPE.equalsIgnoreCase(constantType))
        {
            return getStatusObject(id);
        }
        else if (RESOLUTION_CONSTANT_TYPE.equalsIgnoreCase(constantType))
        {
            return getResolutionObject(id);
        }
        else if (ISSUE_TYPE_CONSTANT_TYPE.equalsIgnoreCase(constantType))
        {
            return getIssueTypeObject(id);
        }

        return null;
    }

    public Collection getConstantObjects(String constantType)
    {
        if (PRIORITY_CONSTANT_TYPE.equalsIgnoreCase(constantType))
        {
            return getPriorityObjects();
        }
        else if (STATUS_CONSTANT_TYPE.equalsIgnoreCase(constantType))
        {
            return getStatusObjects();
        }
        else if (RESOLUTION_CONSTANT_TYPE.equalsIgnoreCase(constantType))
        {
            return getResolutionObjects();
        }
        else if (ISSUE_TYPE_CONSTANT_TYPE.equalsIgnoreCase(constantType))
        {
            return getAllIssueTypeObjects();
        }

        return null;
    }

    public List<IssueConstant> convertToConstantObjects(String constantType, Collection ids)
    {
        if (ids != null && !ids.isEmpty() && constantType != null)
        {
            List<IssueConstant> list = new ArrayList<IssueConstant>(ids.size());
            for (Object o : ids)
            {
                String id;
                if (o instanceof GenericValue)
                {
                    id = ((GenericValue) o).getString("id");
                }
                else
                {
                    id = (String) o;
                }

                IssueConstant constant = null;
                if (PRIORITY_CONSTANT_TYPE.equalsIgnoreCase(constantType))
                {
                    constant = getPriorityObject(id);
                }
                else if (STATUS_CONSTANT_TYPE.equalsIgnoreCase(constantType))
                {
                    constant = getStatusObject(id);
                }
                else if (RESOLUTION_CONSTANT_TYPE.equalsIgnoreCase(constantType))
                {
                    if ("-1".equals(id))
                    {
                        constant = UNRESOLVED_RESOLUTION;
                    }
                    else
                    {
                        constant = getResolutionObject(id);
                    }
                }
                else if (ISSUE_TYPE_CONSTANT_TYPE.equalsIgnoreCase(constantType))
                {
                    constant = getIssueTypeObject(id);
                }

                if (constant != null)
                {
                    list.add(constant);
                }
                else
                {
                    log.warn(id + " returned a null constant of type " + constantType);
                }
            }
            return list;
        }
        else
        {
            return null;
        }
    }

    public boolean constantExists(String constantType, String name)
    {
        return (getIssueConstantByName(constantType, name) != null);
    }

    public IssueConstant getIssueConstantByName(String constantType, String name)
    {
        ConstantsCache<? extends IssueConstant> constantsCache = getConstantsCache(constantType);

        for (IssueConstant issueConstant : constantsCache.getObjects())
        {
            // TODO: Do we really allow null names?
            if (name == null)
            {
                if (issueConstant.getName() == null)
                    return issueConstant;
            }
            else
            {
                if (name.equals(issueConstant.getName()))
                {
                    return issueConstant;
                }
            }
        }
        // Not Found
        return null;
    }

    public GenericValue getConstantByName(String constantType, String name)
    {
        return convertToGenericValue(getIssueConstantByName(constantType, name));
    }

    private GenericValue convertToGenericValue(final IssueConstant issueConstant)
    {
        if (issueConstant == null)
        {
            return null;
        }
        else
        {
            return issueConstant.getGenericValue();
        }
    }

    public IssueConstant getConstantByNameIgnoreCase(final String constantType, final String name)
    {
        ConstantsCache<? extends IssueConstant> constantsCache = getConstantsCache(constantType);

        for (IssueConstant issueConstant : constantsCache.getObjects())
        {
            // TODO: Do we really allow null names?
            if (name == null)
            {
                if (issueConstant.getName() == null)
                    return issueConstant;
            }
            else
            {
                if (name.equalsIgnoreCase(issueConstant.getName()))
                {
                    return issueConstant;
                }
            }
        }
        // Not Found
        return null;
    }

    private ConstantsCache<? extends IssueConstant> getConstantsCache(final String constantType)
    {
        if (PRIORITY_CONSTANT_TYPE.equalsIgnoreCase(constantType))
        {
            return priorityCache.getData();
        }
        else if (STATUS_CONSTANT_TYPE.equalsIgnoreCase(constantType))
        {
            return statusCache.getData();
        }
        else if (RESOLUTION_CONSTANT_TYPE.equalsIgnoreCase(constantType))
        {
            return resolutionCache.getData();
        }
        else if (ISSUE_TYPE_CONSTANT_TYPE.equalsIgnoreCase(constantType))
        {
            return issueTypeCache.getData();
        }

        throw new IllegalArgumentException("Unknown constant type '" + constantType + "'.");
    }

    public GenericValue createIssueType(String name, Long sequence, String style, String description, String iconurl) throws CreateException
    {
        GenericValue createdIssueType;
        try
        {
            Map<String, Object> fields = MapBuilder.<String, Object>newBuilder()
                    .add("id", EntityUtils.getNextStringId(ISSUE_TYPE_CONSTANT_TYPE))
                    .add("name", name)
                    .add("sequence", sequence)
                    .add("style", StringUtils.trimToNull(style))
                    .add("description", description)
                    .add("iconurl", iconurl).toMap();
            createdIssueType = EntityUtils.createValue(ISSUE_TYPE_CONSTANT_TYPE, fields);
        }
        catch (GenericEntityException e)
        {
            throw new CreateException("Error occurred while creating issue type with name='" + name + "' sequence='" + sequence +
                    "' style='" + style + "' description='" + description + "' iconurl='" + iconurl + "'.", e);
        }
        refreshIssueTypes();
        return createdIssueType;
    }

    public void validateCreateIssueType(String name, String style, String description, String iconurl, ErrorCollection errors, String nameFieldName)
    {
        if (StringUtils.isBlank(name))
        {
            errors.addError(nameFieldName, authenticationContext.getI18nHelper().getText("admin.errors.must.specify.name"));
        }
        else
        {
            for (final GenericValue issueType : getIssueTypes())
            {
                if (name.trim().equalsIgnoreCase(issueType.getString("name")))
                {
                    errors.addError(nameFieldName, authenticationContext.getI18nHelper().getText("admin.errors.issue.type.with.this.name.already.exists"));
                    break;
                }
            }
        }

        if (StringUtils.isBlank(iconurl))
        {
            errors.addError("iconurl", authenticationContext.getI18nHelper().getText("admin.errors.must.specify.url.for.issue.type"));
        }
    }

    public void updateIssueType(String id, String name, Long sequence, String style, String description, String iconurl) throws DataAccessException
    {
        if (id == null)
        {
            throw new IllegalArgumentException("Id cannot be null.");
        }

        try
        {
            // Get the value from the database so that we do not modify the cached value directly
            final GenericValue issueTypeGV = EntityUtil.getOnly(ofBizDelegator.findByAnd(ISSUE_TYPE_CONSTANT_TYPE, new FieldMap("id", id)));

            if (issueTypeGV == null)
            {
                throw new IllegalArgumentException("Issue Type with id '" + id + "' does not exist.");
            }

            issueTypeGV.set("name", name);
            issueTypeGV.set("sequence", sequence);
            issueTypeGV.set("style", style);
            issueTypeGV.set("description", description);
            issueTypeGV.set("iconurl", iconurl);
            issueTypeGV.store();
        }
        catch (GenericEntityException e)
        {
            throw new DataAccessException("Error occurred while updating issue type with id '" + id + "'.");
        }
        refreshIssueTypes();
    }

    public void removeIssueType(String id) throws RemoveException
    {
        final GenericValue issueTypeGV = getIssueType(id);
        if (issueTypeGV != null)
        {
            try
            {
                issueTypeGV.remove();
                refreshIssueTypes();
            }
            catch (GenericEntityException e)
            {
                throw new RemoveException("Error occurred while removing issute type with id '" + id + "'.", e);
            }
        }
        else
        {
            throw new RemoveException("Issue type with id '" + id + "' does not exist.");
        }

    }

    public void storeIssueTypes(List<GenericValue> issueTypes) throws DataAccessException
    {
        try
        {
            ofBizDelegator.storeAll(issueTypes);
        }
        catch (DataAccessException e)
        {
            throw new DataAccessException("Error occurred while storing issue types to the database.", e);
        }

        refreshIssueTypes();
    }

    public void refresh()
    {
        // Do not load the defaults set all caches to uninitialised
        priorityCache.reset();
        resolutionCache.reset();
        issueTypeCache.reset();
        statusCache.reset();
    }

    public List<String> expandIssueTypeIds(Collection<String> issueTypeIds)
    {
        if (issueTypeIds == null)
        {
            return Collections.emptyList();
        }

        for (final String issueTypeId : issueTypeIds)
        {
            if (ALL_STANDARD_ISSUE_TYPES.equals(issueTypeId))
            {
                return getIds(getIssueTypes());
            }
            else if (ALL_SUB_TASK_ISSUE_TYPES.equals(issueTypeId))
            {
                return getIds(getSubTaskIssueTypes());
            }
            else if (ALL_ISSUE_TYPES.equals(issueTypeId))
            {
                return getAllIssueTypeIds();
            }
        }

        return new ArrayList<String>(issueTypeIds);
    }

    public List<String> getAllIssueTypeIds()
    {
        return issueTypeCache.getData().getCachedIds();
    }

    public IssueConstant getIssueConstant(GenericValue issueConstantGV)
    {
        if (issueConstantGV == null)
        {
            return null;
        }

        if (ISSUE_TYPE_CONSTANT_TYPE.equals(issueConstantGV.getEntityName()))
        {
            return getIssueTypeObject(issueConstantGV.getString("id"));
        }
        else if (STATUS_CONSTANT_TYPE.equals(issueConstantGV.getEntityName()))
        {
            return getStatusObject(issueConstantGV.getString("id"));
        }
        else if (PRIORITY_CONSTANT_TYPE.equals(issueConstantGV.getEntityName()))
        {
            return getPriorityObject(issueConstantGV.getString("id"));
        }
        else if (RESOLUTION_CONSTANT_TYPE.equals(issueConstantGV.getEntityName()))
        {
            return getResolutionObject(issueConstantGV.getString("id"));
        }

        throw new IllegalArgumentException("Unknown constant entity name '" + issueConstantGV.getEntityName() + "'.");
    }

    private static List<String> getIds(Collection<GenericValue> issueTypes)
    {
        List<String> ids = new ArrayList<String>(issueTypes.size());
        for (final GenericValue issueTypeGV : issueTypes)
        {
            ids.add(issueTypeGV.getString("id"));
        }
        return ids;
    }

    public Collection<GenericValue> getPriorities()
    {
        return priorityCache.getData().getGenericValues();
    }

    public Collection<Priority> getPriorityObjects()
    {
        return priorityCache.getData().getObjects();
    }

    public Priority getPriorityObject(String id)
    {
        return priorityCache.getData().getObject(id);
    }

    public Priority getDefaultPriorityObject()
    {
        final String defaultPriorityId = (ComponentAccessor.getApplicationProperties()).getString(APKeys.JIRA_CONSTANT_DEFAULT_PRIORITY);
        if (defaultPriorityId == null)
        {
            final Collection<Priority> priorities = getPriorityObjects();
            Priority defaultPriority = null;
            final Iterator<Priority> priorityIt = priorities.iterator();

            int times = (int) Math.ceil((double) priorities.size() / 2d);
            for (int i = 0; i < times; i++)
            {
                defaultPriority = priorityIt.next();
            }

            return defaultPriority;
        }
        else
        {
            return getPriorityObject(defaultPriorityId);
        }

    }
    public GenericValue getDefaultPriority()
    {
        final Priority defaultPriority = getDefaultPriorityObject();
        return defaultPriority == null ? null : defaultPriority.getGenericValue();
    }

    public GenericValue getPriority(String id)
    {
        return getConstant((List<GenericValue>)getPriorities(), id);
    }

    /**
     * @param id the id of a priority
     * @return the name of the priority with the given id, or an i18n'd String indicating that
     * no priority is set (e.g. "None") if the id is null.
     */
    public String getPriorityName(String id)
    {
        if ("-1".equals(id))
        {
            return authenticationContext.getI18nHelper().getText("constants.manager.no.priority");
        }

        return getPriority(id).getString("name");
    }

    public Resolution getResolutionObject(String id)
    {
        return resolutionCache.getData().getObject(id);
    }

    public void refreshPriorities()
    {
        priorityCache.reload();
    }

    public Collection<GenericValue> getResolutions()
    {
        return resolutionCache.getData().getGenericValues();
    }

    public Collection<Resolution> getResolutionObjects()
    {
        return resolutionCache.getData().getObjects();
    }

    public GenericValue getResolution(String id)
    {
        return convertToGenericValue(getResolutionObject(id));
    }

    public void refreshResolutions()
    {
        resolutionCache.reload();
    }

    public Collection<GenericValue> getIssueTypes()
    {
        return issueTypeCache.getData().getRegularIssueTypes();
    }

    public Collection<IssueType> getAllIssueTypeObjects()
    {
        return issueTypeCache.getData().getObjects();
    }

    public Collection<IssueType> getRegularIssueTypeObjects()
    {
        return issueTypeCache.getData().getRegularIssueTypeObjects();
    }

    public Collection<IssueType> getSubTaskIssueTypeObjects()
    {
        return issueTypeCache.getData().getSubTaskIssueTypeObjects();
    }

    public Status getStatusByName(final String name)
    {
        try
        {
            return Iterables.find(getStatusObjects(), new Predicate<Status>()
            {
                @Override
                public boolean apply(@Nullable Status statusObject)
                {
                    return statusObject.getName().equals(name);
                }
            });
        }
        catch (NoSuchElementException notFound)
        {
            return null;
        }
    }

    /**
     * Retrieve subtask issue types.
     * @return A collection of IssueType {@link GenericValue}s
     */
    public Collection<GenericValue> getSubTaskIssueTypes()
    {
        return issueTypeCache.getData().getSubTaskIssueTypes();
    }

    public List<GenericValue> getEditableSubTaskIssueTypes()
    {
        return ofBizDelegator.findByField(ISSUE_TYPE_CONSTANT_TYPE, "style", SubTaskManager.SUB_TASK_ISSUE_TYPE_STYLE, "name");
    }

    public List<GenericValue> getAllIssueTypes()
    {
        return issueTypeCache.getData().getGenericValues();
    }

    public GenericValue getIssueType(String id)
    {
        return convertToGenericValue(getIssueTypeObject(id));
    }

    public IssueType getIssueTypeObject(String id)
    {
        return issueTypeCache.getData().getObject(id);
    }

    public void refreshIssueTypes()
    {
        issueTypeCache.reload();
    }

    private GenericValue getConstant(List<GenericValue> constants, String id)
    {
        if (id == null)
        {
            return null;
        }

        return EntityUtil.getOnly(EntityUtil.filterByAnd(constants, ImmutableMap.of("id", id)));
    }

    private List<GenericValue> getConstants(String type)
    {
        return getConstantsWithSort(type, DEFAULT_CONSTANTS_SORT);
    }

    private List<GenericValue> getConstantsWithSort(String type, List<String> sortList)
    {
        try
        {
            //We do not return an immutable list here because the caches take care of this by themselves.
            return ofBizDelegator.findAll(type, sortList);
        }
        catch (Exception e)
        {
            log.error("Error getting constants of type: " + type + " : " + e, e);
        }

        return null;
    }

    private static class ConstantsCache<T extends IssueConstant>
    {
        private final List<GenericValue> gvList;
        private final Map<String, T> idObjectMap;
        private final List<String> idList;

        /**
         * Constructs a ConstantsCache instance.
         *
         * <p> The GenericValue List is assumed to be immutable. This is currently taken care of by #getConstantsWithSort().
         *
         * @param gvList A list of GenericValues.
         * @param idObjectMap An ID to Constant Object Map.
         */
        public ConstantsCache(final List<GenericValue> gvList, final Map<String, T> idObjectMap)
        {
            this.gvList = Collections.unmodifiableList(gvList);
            this.idObjectMap = Collections.unmodifiableMap(idObjectMap);
            // Extract and store the ids.
            // There is a big performance hit to rebuild these in complex (many projects/issuetypes/customefields) installations.
            // Only because they can get retrieved > 200,000 times per issue query.
            List<String> ids = getIds(gvList);
            this.idList = Collections.unmodifiableList(ids);
        }

        List<GenericValue> getGenericValues()
        {
            return gvList;
        }

        Collection<T> getObjects()
        {
            return idObjectMap.values();
        }

        T getObject(final String id)
        {
            return idObjectMap.get(id);
        }

        List<String> getCachedIds()
        {
            return idList;
        }
    }

    private static class IssueTypeCache extends ConstantsCache<IssueType>
    {
        private final List<IssueType> regularIssueTypeObjects;
        private final List<GenericValue> regularIssueTypes;
        private final List<IssueType> subTaskIssueTypeObjects;
        private final List<GenericValue> subTaskIssueTypes;

        /**
         * Constructs a CacheData instance.
         *
         * <p> The GenericValue List is assumed to be immutable. This is currently taken care of by getConstantsWithSort().
         *
         * @param gvList      An immutable list of GenericValues.
         * @param idObjectMap An ID to Constant Object Map.
         * @param regularIssueTypeObjects List of regular IssueType Objects ordered by name
         * @param regularIssueTypes List of regular IssueTypes ordered by name
         * @param subTaskIssueTypeObjects List of subTask IssueTypes ordered by name
         * @param subTaskIssueTypes List of subTask IssueTypes ordered by name
         */
        public IssueTypeCache(List<GenericValue> gvList, Map<String, IssueType> idObjectMap, List<IssueType> regularIssueTypeObjects,
                List<GenericValue> regularIssueTypes, List<IssueType> subTaskIssueTypeObjects, List<GenericValue> subTaskIssueTypes)
        {
            super(gvList, idObjectMap);
            this.regularIssueTypeObjects = Collections.unmodifiableList(regularIssueTypeObjects);
            this.regularIssueTypes = Collections.unmodifiableList(regularIssueTypes);
            this.subTaskIssueTypeObjects = Collections.unmodifiableList(subTaskIssueTypeObjects);
            this.subTaskIssueTypes = Collections.unmodifiableList(subTaskIssueTypes);
        }

        public List<IssueType> getRegularIssueTypeObjects()
        {
            return regularIssueTypeObjects;
        }

        public List<GenericValue> getRegularIssueTypes()
        {
            return regularIssueTypes;
        }

        public List<IssueType> getSubTaskIssueTypeObjects()
        {
            return subTaskIssueTypeObjects;
        }

        public List<GenericValue> getSubTaskIssueTypes()
        {
            return subTaskIssueTypes;
        }
    }

    private class PriorityCacheLoader implements LazyLoadingCache.CacheLoader<ConstantsCache<Priority>>
    {
        public ConstantsCache<Priority> loadData()
        {
            List<GenericValue> priorities = getConstants(PRIORITY_CONSTANT_TYPE);
            Map<String, Priority> priorityObjectsMap = new LinkedHashMap<String, Priority>();
            for (final GenericValue priorityGV : priorities)
            {
                final PriorityImpl priority = new PriorityImpl(priorityGV, translationManager, authenticationContext);
                priorityObjectsMap.put(priorityGV.getString("id"), priority);
            }

            return new ConstantsCache<Priority>(priorities, priorityObjectsMap);
        }
    }
    private class ResolutionCacheLoader implements LazyLoadingCache.CacheLoader<ConstantsCache<Resolution>>
    {
        public ConstantsCache<Resolution> loadData()
        {
            List<GenericValue> resolutions = getConstants(RESOLUTION_CONSTANT_TYPE);
            Map<String, Resolution> resolutionObjectsMap = new LinkedHashMap<String, Resolution>();
            for (final GenericValue resolutionGV : resolutions)
            {
                ResolutionImpl resolution = new ResolutionImpl(resolutionGV, translationManager, authenticationContext);
                resolutionObjectsMap.put(resolutionGV.getString("id"), resolution);
            }

            return new ConstantsCache<Resolution>(resolutions, resolutionObjectsMap);
        }
    }
    private class StatusCacheLoader implements LazyLoadingCache.CacheLoader<ConstantsCache<Status>>
    {
        public ConstantsCache<Status> loadData()
        {
            List<GenericValue> statuses = getConstants(STATUS_CONSTANT_TYPE);
            Map<String, Status> statusObjectsMap = new LinkedHashMap<String, Status>();
            for (GenericValue statusGV : statuses)
            {
                statusObjectsMap.put(statusGV.getString("id"), new StatusImpl(statusGV, translationManager, authenticationContext));
            }
            return new ConstantsCache<Status>(statuses, statusObjectsMap);
        }
    }
    private class IssueTypeCacheLoader implements LazyLoadingCache.CacheLoader<IssueTypeCache>
    {
        public IssueTypeCache loadData()
        {
            // Get all issue types from DB
            List<GenericValue> allIssueTypeGVs = getConstantsWithSort(ISSUE_TYPE_CONSTANT_TYPE, DEFAULT_ISSUE_TYPE_SORT);

            Map<String, IssueType> idObjectMap = new LinkedHashMap<String, IssueType>();
            final List<IssueType> regularIssueTypeObjects = new ArrayList<IssueType>();
            final List<GenericValue> regularIssueTypes = new ArrayList<GenericValue>();
            final List<IssueType> subTaskIssueTypeObjects = new ArrayList<IssueType>();
            final List<GenericValue> subTaskIssueTypes = new ArrayList<GenericValue>();

            for (final GenericValue issueTypeGV : allIssueTypeGVs)
            {
                IssueType issueType = new IssueTypeImpl(issueTypeGV, translationManager, authenticationContext);
                if (issueType.isSubTask())
                {
                    subTaskIssueTypeObjects.add(issueType);
                    subTaskIssueTypes.add(issueTypeGV);
                }
                else
                {
                    regularIssueTypeObjects.add(issueType);
                    regularIssueTypes.add(issueTypeGV);
                }

                idObjectMap.put(issueType.getId(), issueType);
            }
            return new IssueTypeCache(allIssueTypeGVs, idObjectMap, regularIssueTypeObjects, regularIssueTypes, subTaskIssueTypeObjects, subTaskIssueTypes);
        }
    }
}
