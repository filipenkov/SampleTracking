/*
 * Copyright (c) 2003 by Atlassian Software Systems Pty. Ltd.
 * All rights reserved.
 */
package com.atlassian.core.util;

import java.io.Serializable;


/**
 * A simple type to represent a pair of objects.
 */
public class PairType implements Serializable
{
    //~ Instance variables ---------------------------------------------------------------------------------------------

    private Serializable key;
    private Serializable  value;

    //~ Constructors ---------------------------------------------------------------------------------------------------

    public PairType()
    {
    }

    public PairType(Serializable key, Serializable  value)
    {
        this.key = key;
        this.value = value;
    }

    //~ Methods --------------------------------------------------------------------------------------------------------

    public Serializable  getKey()
    {
        return key;
    }

    public void setKey(Serializable key)
    {
        this.key = key;
    }

    public Serializable getValue()
    {
        return value;
    }

    public void setValue(Serializable value)
    {
        this.value = value;
    }

    public String toString()
    {
        return key+"/"+value;
    }

    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof PairType)) return false;

        final PairType pairType = (PairType) o;

        if (!key.equals(pairType.key)) return false;
        if (!value.equals(pairType.value)) return false;

        return true;
    }

    public int hashCode()
    {
        int result;
        result = key.hashCode();
        result = 29 * result + value.hashCode();
        return result;
    }
}
