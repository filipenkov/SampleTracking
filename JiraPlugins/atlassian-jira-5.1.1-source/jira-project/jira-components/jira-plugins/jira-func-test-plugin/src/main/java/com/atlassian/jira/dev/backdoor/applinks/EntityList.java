package com.atlassian.jira.dev.backdoor.applinks;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;

/**
 * JAXB-enabled entity list.
 *
 * @since v4.3
 */
@XmlRootElement
public class EntityList
{
    public final ArrayList<EntityRefBean> entities;

    public EntityList(ArrayList<EntityRefBean> entities)
    {
        this.entities = entities;
    }
}
