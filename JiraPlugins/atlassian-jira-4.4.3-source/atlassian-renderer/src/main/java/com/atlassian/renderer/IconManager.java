package com.atlassian.renderer;

public interface IconManager
{
    Icon getLinkDecoration(String iconName);

    Icon getEmoticon(String symbol);

    String[] getEmoticonSymbols();
}
