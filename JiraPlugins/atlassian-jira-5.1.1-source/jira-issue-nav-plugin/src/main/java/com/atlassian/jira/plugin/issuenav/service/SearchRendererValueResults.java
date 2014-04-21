package com.atlassian.jira.plugin.issuenav.service;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.HashMap;

/**
* Maps of values returned by search renderer, indexed by searcher id
 * @since v5.1
*/
@XmlRootElement
public class SearchRendererValueResults extends HashMap<String, SearchRendererValue>
{
}
