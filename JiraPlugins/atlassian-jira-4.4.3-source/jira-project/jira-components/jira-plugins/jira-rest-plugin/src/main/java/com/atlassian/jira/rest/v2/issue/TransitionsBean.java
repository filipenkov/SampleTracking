package com.atlassian.jira.rest.v2.issue;

import com.atlassian.jira.util.collect.CollectionBuilder;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
* @since v4.2
*/
@SuppressWarnings ({ "FieldCanBeLocal", "UnusedDeclaration" })
@XmlRootElement
public class TransitionsBean
{
    @XmlElement
    private String name;

    @XmlElement
    private Collection<TransitionFieldBean> fields;

    @XmlElement
    private String transitionDestination;

    @SuppressWarnings ({ "UnusedDeclaration" })
    TransitionsBean() {}
    public TransitionsBean(final String name, final Collection<TransitionFieldBean> fields, final String transitionDestination)
    {
        this.name = name;
        this.fields = fields;
        this.transitionDestination = transitionDestination;
    }

    static final TransitionsBean DOC_EXAMPLE;
    static final Map<Integer, TransitionsBean> DOC_MAP_EXAMPLE = new SerializableHashMap<Integer, TransitionsBean>();
    static
    {
        DOC_EXAMPLE = new TransitionsBean("Close Issue", CollectionBuilder.list(TransitionFieldBean.DOC_EXAMPLE), "6");
        DOC_MAP_EXAMPLE.put(731, DOC_EXAMPLE);
    }

    /**
     * This subclass is needed to keep JAXB from freaking out.
     */
    @XmlRootElement (name = "transitions")
    static class SerializableHashMap<K, V> extends HashMap<K, V>
    {
        // empty
    }
}
