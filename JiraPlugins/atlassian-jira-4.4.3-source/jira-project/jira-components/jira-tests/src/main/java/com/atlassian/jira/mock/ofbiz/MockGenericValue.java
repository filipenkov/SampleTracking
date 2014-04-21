package com.atlassian.jira.mock.ofbiz;

import com.atlassian.core.ofbiz.CoreFactory;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.ofbiz.core.entity.GenericDelegator;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericPK;
import org.ofbiz.core.entity.GenericValue;
import org.ofbiz.core.entity.model.ModelEntity;
import org.ofbiz.core.entity.model.ModelField;
import org.ofbiz.core.util.UtilMisc;
import org.ofbiz.core.util.UtilValidate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This was taken from atlassian-ofbiz and placed into its now rightful home of JIRA.
 *
 * @since 4.3
 */
public class MockGenericValue extends GenericValue {
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="MF_CLASS_MASKS_FIELD", justification="It is a mock, so we are okay with this.")    
    Map<String,Object> fields;
    protected boolean created = false;
    protected boolean stored = false;
    protected boolean refreshed = false;
    protected boolean removed = false;
    Map<String,List<GenericValue>> related = new HashMap();

    GenericDelegator gd;

    public MockGenericValue(GenericValue value) {
        this(value.getEntityName());
        this.fields = value.getFields(value.getAllKeys());
    }

    public MockGenericValue(String entityName) {
        super(new ModelEntity(), null);
        this.entityName = entityName;
        this.fields = new HashMap();
    }

    public MockGenericValue(String entityName, Map fields) {
        this(entityName);

        if (fields != null)
        {
            this.fields = Maps.newHashMap(fields);
        }
    }

    public MockGenericValue(String entityName, Long id) {
        this(entityName, ImmutableMap.<String,Object>of("id", id));
    }

    public Object get(String name) {
        return fields.get(name);
    }

    public void set(String name, Object value) {
        fields.put(name, value);
    }

    public Collection<String> getAllKeys() {
        return fields.keySet();
    }

    public Map<String,Object> getFields(Collection collection)
    {
        Map selectedFields = new HashMap();
        for (Iterator iterator = collection.iterator(); iterator.hasNext();)
        {
            String key = (String) iterator.next();
            selectedFields.put(key, fields.get(key));
        }
        return selectedFields;
    }

    public Map<String,Object> getAllFields()
    {
        return fields;
    }

    public List<GenericValue> getRelated(String s) throws GenericEntityException {
        final List<GenericValue> related = this.related.get(s);
        return related != null ? related : Collections.<GenericValue>emptyList();
    }

    public List<GenericValue> getRelated(String s, Map map, List order) throws GenericEntityException {
        return CoreFactory.getGenericDelegator().getRelated(s, map, order, this);
    }

    public void setRelated(String s, List relatedGVs) {
        related.put(s, relatedGVs);
    }

    public GenericValue create() throws GenericEntityException {
        created = true;
        return this;
    }

    public boolean isCreated() {
        return created;
    }

    public boolean isStored() {
        return stored;
    }

    public boolean isRemoved() {
        return removed;
    }

    public boolean isRefreshed() {
        return refreshed;
    }

    public ModelEntity getModelEntity() {
        return new MockModelEntity(this);
    }

    public boolean matchesFields(Map keyValuePairs) {
        if (fields == null) return true;
        if (keyValuePairs == null || keyValuePairs.size() == 0) return true;
        Iterator entries = keyValuePairs.entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry anEntry = (Map.Entry) entries.next();
            if (!UtilValidate.areEqual(anEntry.getValue(), this.fields.get(anEntry.getKey()))) {
                return false;
            }
        }
        return true;
    }

    public GenericPK getPrimaryKey() {
        return new GenericPK(getModelEntity(), UtilMisc.toMap("id", fields.get("id")));
    }


    public void setDelegator(GenericDelegator internalDelegator) {
        this.gd = internalDelegator;
    }

    public GenericDelegator getDelegator() {
        return gd;
    }

    public void store() throws GenericEntityException {
        stored = true;
        CoreFactory.getGenericDelegator().store(this);
    }

    public void remove() throws GenericEntityException {
        removed = true;
        CoreFactory.getGenericDelegator().removeValue(this);
    }

    public void removeRelated(String relationName) throws GenericEntityException {
        related.remove(relationName);
    }

    public void refresh() throws GenericEntityException {
        refreshed = true;
        CoreFactory.getGenericDelegator().refresh(this);
    }

    public String toString() {
        StringBuffer theString = new StringBuffer();
        theString.append("[GenericEntity:");
        theString.append(getEntityName());
        theString.append(']');

        Iterator entries = fields.entrySet().iterator();
        Map.Entry anEntry = null;
        while (entries.hasNext()) {
            anEntry = (Map.Entry) entries.next();
            theString.append('[');
            theString.append(anEntry.getKey());
            theString.append(',');
            theString.append(anEntry.getValue());
            theString.append(']');
        }
        return theString.toString();
    }

    public Object dangerousGetNoCheckButFast(ModelField modelField) {
        if (modelField == null) throw new IllegalArgumentException("Cannot get field with a null modelField");
        return fields.get(modelField.getName());
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MockGenericValue)) return false;
        if (!super.equals(o)) return false;

        final MockGenericValue mockGenericValue = (MockGenericValue) o;

        if (fields != null ? !fields.equals(mockGenericValue.fields) : mockGenericValue.fields != null) return false;

        return true;
    }

    public int hashCode() {
        int result = super.hashCode();
        result = 29 * result + (fields != null ? fields.hashCode() : 0);
        result = 29 * result + (created ? 1 : 0);
        return result;
    }

    public void setString(String s, String s1) {
        this.set(s, s1);
    }

    public List getRelatedOrderBy(String relationName, List orderBy) throws GenericEntityException {
        return CoreFactory.getGenericDelegator().getRelatedOrderBy(relationName, orderBy, this);
    }

    public List getRelatedByAnd(String relationName, Map fields) throws GenericEntityException {
        return CoreFactory.getGenericDelegator().getRelatedByAnd(relationName, fields, this);
    }

    public class MockModelEntity extends ModelEntity {
        GenericValue value;

        public MockModelEntity() {

        }

        public MockModelEntity(GenericValue value) {
            this.value = value;
            this.setEntityName(value.getEntityName());
        }

        public List getAllFieldNames() {
            List fieldnames = new ArrayList();

            for (Iterator iterator = value.getAllKeys().iterator(); iterator.hasNext();) {
                String key = (String) iterator.next();
                fieldnames.add(key);
            }

            return fieldnames;
        }

        public ModelField getField(String fieldName) {
            ModelField field = null;

            if (value.getAllKeys().contains(fieldName)) {
                field = new ModelField();
                field.setName(fieldName);
            }

            return field;
        }
    }

    public Set entrySet()
    {
        return fields.entrySet();
    }

    public Set keySet()
    {
        return fields.keySet();
    }

    public int size()
    {
        return fields.size();
    }

    public boolean isEmpty()
    {
        return fields.isEmpty();
    }

    public Collection values()
    {
        return fields.values();
    }

    public Object clone()
    {
        return new MockGenericValue(entityName, new HashMap(fields));
    }
}
