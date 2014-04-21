package com.atlassian.renderer.v2.components.list;

class ListType
{
    public final String openingTag;
    public final String closingTag;
    public final String bullet;

    public ListType(String bullet, String openingTag, String closingTag)
    {
        this.bullet = bullet;
        this.openingTag = openingTag;
        this.closingTag = closingTag;
    }
}
