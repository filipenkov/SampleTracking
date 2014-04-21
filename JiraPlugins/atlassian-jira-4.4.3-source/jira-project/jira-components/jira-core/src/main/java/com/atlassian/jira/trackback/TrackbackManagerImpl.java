package com.atlassian.jira.trackback;

import com.atlassian.core.ofbiz.util.EntityUtils;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.trackback.Trackback;
import com.atlassian.trackback.TrackbackException;
import org.apache.commons.lang.exception.NestableRuntimeException;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import org.ofbiz.core.util.UtilDateTime;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TrackbackManagerImpl implements TrackbackManager
{
    private static final String ENTITY_NAME = "TrackbackPing";

    private final OfBizDelegator ofBizDelegator;

    public TrackbackManagerImpl(OfBizDelegator ofBizDelegator)
    {
        this.ofBizDelegator = ofBizDelegator;
    }

    public void storeTrackback(Trackback trackback, GenericValue issue) throws TrackbackException
    {
        if (issue == null)
        {
            throw new IllegalArgumentException("Cannot create trackback for null issue");
        }
        if (trackback == null)
        {
            return;
        }
        if (getTrackbacksForIssue(issue).contains(trackback))
        {
            return;
        }

        try
        {
            Map<String, Object> fields = new HashMap<String, Object>();
            fields.put("url", trackback.getUrl());
            fields.put("title", trackback.getTitle());
            fields.put("blogname", trackback.getBlogName());
            fields.put("excerpt", trackback.getExcerpt());
            fields.put("issue", issue.getLong("id"));
            fields.put("created", UtilDateTime.nowTimestamp()); //date is stored, but not retrieved (yet)
            EntityUtils.createValue(ENTITY_NAME, fields);
        }
        catch (GenericEntityException e)
        {
            throw new TrackbackException(trackback, e);
        }
    }

    /**
     * Return a collection of {@link Trackback}s
     */
    public Collection<Trackback> getTrackbacksForIssue(GenericValue issue)
    {
        if (issue == null)
        {
            return Collections.emptyList();
        }

        try
        {
            List<GenericValue> related = issue.getRelated("ChildTrackbackPing");
            Collection<Trackback> trackbacks = new ArrayList<Trackback>(related.size());
            for (GenericValue trackbackGV : related)
            {
                trackbacks.add(createTrackBack(trackbackGV));
            }
            return trackbacks;
        }
        catch (GenericEntityException e)
        {
            throw new NestableRuntimeException("Exception whilst getting trackbacks for issue " + issue, e);
        }
    }

    private Trackback createTrackBack(GenericValue trackbackGV)
    {
        Trackback trackback = new Trackback();
        trackback.setId(trackbackGV.getLong("id"));
        trackback.setUrl(trackbackGV.getString("url"));
        trackback.setTitle(trackbackGV.getString("title"));
        trackback.setBlogName(trackbackGV.getString("blogname"));
        trackback.setExcerpt(trackbackGV.getString("excerpt"));

        return trackback;
    }

    public Trackback getTrackback(Long trackbackId)
    {
        GenericValue trackbackGV = ofBizDelegator.findByPrimaryKey(ENTITY_NAME, MapBuilder.build("id", trackbackId));
        return trackbackGV == null ? null : createTrackBack(trackbackGV);
    }

    public void deleteTrackback(Long id)
    {
        ofBizDelegator.removeByAnd(ENTITY_NAME, MapBuilder.build("id", id));
    }
}
