package com.atlassian.streams.spi;

public enum ServletPath
{
    COMMENTS("/plugins/servlet/streamscomments");
    
    private final String path;
    
    private ServletPath(String path)
    {
        this.path = path;
    }
    
    public String getPath()
    {
        return path;
    }
    
    @Override
    public String toString()
    {
        return path;
    }
}
