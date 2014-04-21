/*
 * Created by IntelliJ IDEA.
 * User: owen
 * Date: Nov 27, 2002
 * Time: 2:14:35 PM
 * CVS Revision: $Revision: 1.1 $
 * Last CVS Commit: $Date: 2002/12/02 05:26:32 $
 * Author of last CVS Commit: $Author: mike $
 * To change this template use Options | File Templates.
 */
package com.atlassian.core.util;

import java.util.ArrayList;
import java.util.List;

public class HTMLUtils
{
    private int currentIndex;

    public static String stripTags(String html)
    {
        StringBuffer detagged = new StringBuffer();
        boolean intag = false;
        for (int count = 0; count < html.length(); count++)
        {
            char current = html.charAt(count);

            if (current == '>')
                intag = false;
            else if (current == '<')
                intag = true;
            else if (!intag)
                detagged.append(current);
        }
        return detagged.toString();
    }

    public static String stripOuterHtmlTags(String html)
    {
        ArrayList tags = new ArrayList();
        tags.add(new String[]{"html", "true", "0"});
        tags.add(new String[]{"head", "false", "0"});
        tags.add(new String[]{"body", "true", "0"});
        String result = stripOuterTags(html, tags, 0);

        return result.trim();
    }

    private static String stripOuterTags(String html, String tag, boolean inclusive)
    {
        ArrayList tags = new ArrayList();
        tags.add(new String[]{tag, new Boolean(inclusive).toString(), "0"});
        return stripOuterTags(html, tags, 0);
    }

    private static String stripOuterTags(String html, List tagIncs, int listValue)
    {
        String[] tagInc = (String[]) tagIncs.get(listValue);
        String tag = tagInc[0];
        boolean inclusive = new Boolean(tagInc[1]).booleanValue();
        int initialCount = new Integer(tagInc[2]).intValue();

        String[] previousInc = null;
        if (listValue != 0)
            previousInc = (String[]) tagIncs.get(listValue - 1);

        String[] nextInc = null;
        if (listValue < tagIncs.size() - 1)
            nextInc = (String[]) tagIncs.get(listValue + 1);

        StringBuffer detagged = new StringBuffer();
        boolean tagValue = false;
        for (int count = initialCount; count < html.length(); count++)
        {
            char current = html.charAt(count);

            if (tagValue)
            {
                if (current == '<')
                {
                    int newCounter = foundTag(html, count, tag, false);
                    if (newCounter != -1)
                    {
                        tagValue = false;
                        count = newCounter;
                        if (previousInc != null)
                        {
                            previousInc[2] = new Integer(newCounter).toString();
                            tagIncs.add(listValue - 1, previousInc);
                            tagIncs.remove(listValue);
                        }
                        if (inclusive)
                        {
                            return detagged.toString();
                        }
                        else
                        {
                            listValue++;
                            tagInc = (String[]) tagIncs.get(listValue);
                            tag = tagInc[0];
                            inclusive = new Boolean(tagInc[1]).booleanValue();
                            if (listValue < tagIncs.size() - 1)
                                nextInc = (String[]) tagIncs.get(listValue + 1);
                            else
                                nextInc = null;
                        }
                    }
                    else
                    {
                        if (inclusive)
                            detagged.append(current);
                    }
                }
                else
                {
                    if (inclusive)
                        detagged.append(current);
                }
            }
            else
            {
                if (current == '<')
                {
                    int newCounter = foundTag(html, count, tag, true);
                    if (newCounter != -1)
                    {
                        tagValue = true;
                        count = newCounter;
                        //move tags to trim
                        if (nextInc != null)
                        {
                            if (inclusive)
                            {
                                nextInc[2] = new Integer(count).toString();
                                tagIncs.remove(listValue + 1);
                                tagIncs.add(listValue + 1, nextInc);
                                detagged.append(stripOuterTags(html, tagIncs, listValue + 1));
                                String[] tempTagIncs = (String[]) tagIncs.get(listValue);
                                count = new Integer(tempTagIncs[2]).intValue();
                            }
                        }
                    }
                    else
                    {
                        detagged.append(current);
                    }
                }
                else
                {
                    detagged.append(current);
                }
            }
        }
        return detagged.toString();
    }

    private static int foundTag(String html, int count, String tag, boolean opening)
    {
        String tagToFind = tag;
        if (!opening)
            tagToFind = "/" + tagToFind;
        int htmlCounter = count;
        int htmlFound = 0;
        boolean inQuotes = false;
        for (htmlCounter = count; htmlCounter < html.length(); htmlCounter++)
        {
            char current2 = html.charAt(htmlCounter);
            if (current2 == '\"')
            {
                inQuotes = !inQuotes;
                htmlFound = 0;
            }
            else if (!inQuotes)
            {
                if (current2 == '>')
                    break;
                else if (htmlFound != tagToFind.length())
                {
                    if (tagToFind.toLowerCase().charAt(htmlFound) == current2 || tagToFind.toUpperCase().charAt(htmlFound) == current2)
                        htmlFound++;
                    else
                        htmlFound = 0;
                }
            }
        }
        if (htmlFound == tagToFind.length())
        {
            return htmlCounter + 1;
        }
        else
        {
            return -1;
        }
    }
}
