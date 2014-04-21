package com.atlassian.modzdetector;

import java.util.ArrayList;
import java.util.List;

/**
 * A data class to hold the modifications and removals summary.
 */
public final class Modifications
{
    public final List<String> modifiedFiles;
    public final List<String> removedFiles;

    public Modifications()
    {
        modifiedFiles = new ArrayList<String>();
        removedFiles = new ArrayList<String>();
    }

    public Modifications append(Modifications another) {
        Modifications both = new Modifications();
        both.modifiedFiles.addAll(this.modifiedFiles);
        both.modifiedFiles.addAll(another.modifiedFiles);
        both.removedFiles.addAll(this.removedFiles);
        both.removedFiles.addAll(another.removedFiles);
        return both;
    }
}