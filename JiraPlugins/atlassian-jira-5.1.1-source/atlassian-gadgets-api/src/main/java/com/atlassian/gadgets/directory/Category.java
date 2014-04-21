package com.atlassian.gadgets.directory;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum Category
{

    // Product Names
    JIRA("JIRA"),
    CONFLUENCE("Confluence"),
//    FISHEYE("Fisheye"),
//    CRUCIBLE("Crucible"),
    FE_CRU("Fisheye/Crucible"),
    CROWD("Crowd"),
    CLOVER("Clover"),
    BAMBOO("Bamboo"),

    // Other Categories
    ADMIN("Admin"),
//    PROJECTS("Projects","Project"),
    CHARTS("Charts", "Chart"),
//    ISSUES("Issues", "Issue"),
//    FILTERS("Filters", "Filter"),
//    CONTENT("Content"),
//    EXTERNAL_CONTENT("External Content"),
//    USERS("Users", "User"),
//    ACTIVITY("Activity"),
//    TIME_TRACKING("Time Tracking"),
//    AGILE("Agile"),
//    BUILDS("Builds", "Build"),
//    SOURCE("Source"),
//    SOURCE_CODE("Source code"),
//    VERSION_CONTROL("Version Control"),
//    REVIEWS("Reviews", "Review"),
//    WIKI("Wiki", "Wikis"),
//    MACROS("Macros", "Macro"),
//    METRICS("Metrics", "Metric"),
//    QUALITY("Quality"),
//    REPORTS("Reports", "Report"),

    WALLBOARD("Wallboard", "WallBoard"),
    // Default Category
    OTHER("Other");

    private final String name;
    private final List<String> aliases;

    private Category(String name, String... aliases)
    {
        this.name = name;
        this.aliases = Collections.unmodifiableList(Arrays.asList(aliases));
    }

    // add the category name and alias strings to a map so we can easily retrieve the correct Category given a string
    private static final Map<String, Category> categoryNameMap;

    static
    {
        Map<String, Category> map = new HashMap<String, Category>();
        for (Category category : Category.values())
        {
            map.put(category.getName().toLowerCase(), category);
            for (String alias : category.aliases)
            {
                map.put(alias.toLowerCase(), category);
            }
        }

        categoryNameMap = Collections.unmodifiableMap(map);
    }

    public static Category named(String categoryName)
    {
        Category category = categoryNameMap.get(categoryName.toLowerCase());
        return category != null ? category : OTHER;
    }

    public String getName()
    {
        return name;
    }
}
