package com.atlassian.plugins.rest.common.expand;

import com.atlassian.plugins.rest.common.expand.parameter.ExpandParameter;
import com.atlassian.plugins.rest.common.expand.resolver.EntityExpanderResolver;
import com.atlassian.plugins.rest.common.util.ReflectionUtils;
import static com.atlassian.plugins.rest.common.util.ReflectionUtils.*;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import org.apache.commons.lang.StringUtils;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Collection;

/**
 * This allows for crawling the fields of any arbitrary object, looking for fields that should be expanded.
 */
public class EntityCrawler
{
    /**
     * Crawls an entity for fields that should be expanded and expands them.
     * @param entity the object to crawl, can be {@code null}.
     * @param expandParameter the parameters to match for expansion
     * @param expanderResolver the resolver to lookup {@link EntityExpander} for fields to be expanded.
     */
    public void crawl(Object entity, ExpandParameter expandParameter, EntityExpanderResolver expanderResolver)
    {
        if (entity == null)
        {
            return;
        }

        final Collection<Field> expandableFields = getExpandableFields(entity);
        setExpandParameter(expandableFields, entity);
        expandFields(expandableFields, entity, expandParameter, expanderResolver);
    }

    private void setExpandParameter(Collection<Field> expandableFields, Object entity)
    {
        final Field expand = getExpandField(entity);
        if (expand != null && !expandableFields.isEmpty())
        {
            final StringBuilder expandValue = new StringBuilder();
            for (Field field : expandableFields)
            {
                expandValue.append(getExpandable(field).value()).append(",");
            }
            expandValue.deleteCharAt(expandValue.length() - 1); // remove the last ","

            setFieldValue(expand, entity, expandValue.toString());
        }
    }

    private Field getExpandField(Object entity)
    {
        for (Field field : getDeclaredFields(entity.getClass()))
        {
            if (field.getType().equals(String.class))
            {
                final XmlAttribute annotation = field.getAnnotation(XmlAttribute.class);
                if (annotation != null && (field.getName().equals("expand") || "expand".equals(annotation.name())))
                {
                    return field;
                }
            }
        }
        return null;
    }

    private Collection<Field> getExpandableFields(final Object entity)
    {
        return Collections2.filter(getDeclaredFields(entity.getClass()), new Predicate<Field>()
        {
            public boolean apply(Field field)
            {
                return getExpandable(field) != null && ReflectionUtils.getFieldValue(field, entity) != null;
            }
        });
    }

    private void expandFields(Collection<Field> expandableFields, Object entity, ExpandParameter expandParameter, EntityExpanderResolver expanderResolver)
    {
        for (Field field : expandableFields)
        {
            final Expandable expandable = getExpandable(field);
            if (expandParameter.shouldExpand(expandable) && expanderResolver.hasExpander(field.getType()))
            {
                // we know the expander is not null, as per ExpanderResolver contract
                final EntityExpander<Object> entityExpander = expanderResolver.getExpander(field.getType());

                final ExpandContext<Object> context = new DefaultExpandContext<Object>(getFieldValue(field, entity), expandable, expandParameter);
                setFieldValue(field, entity, entityExpander.expand(context, expanderResolver, this));
            }
        }
    }

    /**
     * Returns the expandable annotation with the properly set value. The value is defined as the first valid point in the following list:
     * <ol>
     * <li>the value of the {@link Expandable} annotation if it is set</li>
     * <li>the name of an {@link XmlElement} if the annotation is present on the field and its name is not {@code ##default}</li>
     * <li>the name of the field</li>
     * <ol>
     * @param field the field to look up the Expandable for
     * @return {@code null} if the field is null, {@code null} if the field doesn't have an expandable annotation,
     *         an expandable annotation with a properly set value.
     */
    Expandable getExpandable(final Field field)
    {
        if (field == null)
        {
            return null;
        }

        final Expandable expandable = field.getAnnotation(Expandable.class);
        if (expandable == null)
        {
            return null;
        }

        if (StringUtils.isNotEmpty(expandable.value()))
        {
            return expandable;
        }

        final XmlElement xmlElement = field.getAnnotation(XmlElement.class);
        if (xmlElement != null && StringUtils.isNotEmpty(xmlElement.name()) && !StringUtils.equals("##default", xmlElement.name()))
        {
            return new ExpandableWithValue(xmlElement.name());
        }

        return new ExpandableWithValue(field.getName());
    }

    private static class ExpandableWithValue implements Expandable
    {
        private final String value;

        public ExpandableWithValue(String value)
        {
            this.value = value;
        }

        public String value()
        {
            return value;
        }

        public Class<? extends Annotation> annotationType()
        {
            return Expandable.class;
        }
    }
}
