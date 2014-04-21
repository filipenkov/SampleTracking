package com.atlassian.jira.issue.customfields.option;

import com.atlassian.jira.issue.fields.config.FieldConfig;

import java.util.AbstractList;
import java.util.List;
import java.util.Map;

public class GenericImmutableOptions extends AbstractList implements Options
{

    FieldConfig fieldConfig;
    List originalList;

    public GenericImmutableOptions(List originalList, FieldConfig fieldConfig)
    {
        this.originalList = originalList;
        this.fieldConfig = fieldConfig;
    }


    public Object get(int index)
    {
        return originalList.get(index);
    }

    public int size()
    {
        return originalList.size();
    }

    public List getRootOptions()
    {
        return originalList;
    }


    public Option addOption(Option parent, String value)
    {
        throw new UnsupportedOperationException();
    }

    public void removeOption(Option option)
    {
        throw new UnsupportedOperationException();
    }

    public void setValue(Option option, String value)
    {
        throw new UnsupportedOperationException();
    }

    public void disableOption(Option option)
    {
        throw new UnsupportedOperationException();
    }

    public void enableOption(Option option)
    {
        throw new UnsupportedOperationException();
    }

    public void moveToStartSequence(Option option)
    {
        throw new UnsupportedOperationException();
    }

    public void incrementSequence(Option option)
    {
        throw new UnsupportedOperationException();
    }

    public void decrementSequence(Option option)
    {
        throw new UnsupportedOperationException();
    }

    public void moveToLastSequence(Option option)
    {
        throw new UnsupportedOperationException();
    }

    public Option getOptionById(Long optionId)
    {
        throw new UnsupportedOperationException();
    }

    public Option getOptionForValue(String value, Long parentOptionId)
    {
        throw new UnsupportedOperationException();
    }

    public FieldConfig getRelatedFieldConfig()
    {
        return fieldConfig;
    }

    public void sortOptionsByValue(Option parentOption)
    {
        throw new UnsupportedOperationException();
    }

    public void moveOptionToPosition(Map positionsToOptions)
    {
        throw new UnsupportedOperationException();
    }

}
