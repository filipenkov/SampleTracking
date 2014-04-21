package com.atlassian.gadgets.dashboard.internal.rest.representations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.atlassian.gadgets.dashboard.Layout;
import com.atlassian.gadgets.dashboard.internal.Dashboard;

import static com.atlassian.plugin.util.Assertions.notNull;

/**
 * Provides a JAXB representation of a {@code Dashboard}
 * <p/>
 * TODO : AG-428 We should add LINK elements to this representation, as the REST Guidleines recommend.
 */
@XmlRootElement
public final class DashboardRepresentation
{
    @XmlElement
    private final String id;
    @XmlElement
    private final String title;
    @XmlElement
    private final boolean writable;
    @XmlElement
    private final Layout layout;
    @XmlElement
    private final List<GadgetRepresentation> gadgets;

    public static class Builder
    {
        private final String id;
        private final String title;
        private final Layout layout;
        private List<GadgetRepresentation> gadgets = Collections.emptyList();
        private boolean writable = false;

        public Builder(final Dashboard dashboard)
        {
            id = dashboard.getId().toString();
            title = dashboard.getTitle();
            layout = dashboard.getLayout();
        }

        public Builder writable(boolean writable)
        {
            this.writable = writable;
            return this;
        }

        public Builder gadgets(List<GadgetRepresentation> gadgets)
        {
            notNull("gadgets", gadgets);

            this.gadgets = new ArrayList<GadgetRepresentation>(gadgets);
            return this;
        }

        public DashboardRepresentation build()
        {
            return new DashboardRepresentation(this);
        }
    }

    // Provided for JAXB.
    @SuppressWarnings("UnusedDeclaration")
    private DashboardRepresentation()
    {
        id = null;
        title = null;
        layout = null;
        gadgets = new ArrayList<GadgetRepresentation>();
        writable = false;
    }

    private DashboardRepresentation(Builder builder)
    {
        id = builder.id;
        title = builder.title;
        layout = builder.layout;
        gadgets = builder.gadgets;
        this.writable = builder.writable;
    }

    public String getId()
    {
        return id;
    }

    public String getTitle()
    {
        return title;
    }

    public Layout getLayout()
    {
        return layout;
    }

    public List<GadgetRepresentation> getGadgets()
    {
        return gadgets;
    }

    public boolean isWritable()
    {
        return writable;
    }
}
