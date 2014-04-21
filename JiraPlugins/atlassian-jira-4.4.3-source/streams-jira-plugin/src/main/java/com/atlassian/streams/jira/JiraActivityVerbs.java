package com.atlassian.streams.jira;

import com.atlassian.streams.api.ActivityVerb;
import com.atlassian.streams.api.ActivityVerbs.VerbFactory;

import static com.atlassian.streams.api.ActivityVerbs.ATLASSIAN_IRI_BASE;
import static com.atlassian.streams.api.ActivityVerbs.like;
import static com.atlassian.streams.api.ActivityVerbs.newVerbFactory;
import static com.atlassian.streams.api.ActivityVerbs.update;

public final class JiraActivityVerbs
{
    private static final String JIRA_IRI_BASE = ATLASSIAN_IRI_BASE + "jira/";
    private static final VerbFactory jiraVerbs = newVerbFactory(JIRA_IRI_BASE);

    public static ActivityVerb transition()
    {
        return jiraVerbs.newVerb("transition", update());
    }
    
    public static ActivityVerb reopen()
    {
        return jiraVerbs.newVerb("reopen", transition());
    }

    public static ActivityVerb close()
    {
        return jiraVerbs.newVerb("close", transition());
    }

    public static ActivityVerb open()
    {
        return jiraVerbs.newVerb("open", transition());
    }

    public static ActivityVerb resolve()
    {
        return jiraVerbs.newVerb("resolve", transition());
    }
 
    public static ActivityVerb start()
    {
        return jiraVerbs.newVerb("start", transition());
    }
    
    public static ActivityVerb stop()
    {
        return jiraVerbs.newVerb("stop", transition());
    }
    
    public static ActivityVerb vote()
    {
        return jiraVerbs.newVerb("vote", like());
    }

    public static ActivityVerb link()
    {
        return jiraVerbs.newVerb("link", update());
    }
}