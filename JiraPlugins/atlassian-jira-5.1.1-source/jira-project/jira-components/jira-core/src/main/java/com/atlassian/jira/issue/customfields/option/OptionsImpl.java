package com.atlassian.jira.issue.customfields.option;

import com.atlassian.jira.issue.comparator.BeanComparatorIgnoreCase;
import com.atlassian.jira.issue.customfields.manager.OptionsManager;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.util.CollectionReorderer;
import org.apache.commons.collections.MultiHashMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class OptionsImpl extends ArrayList<Option> implements Options
{
    static final long serialVersionUID = 1946632069203605222L;

    private Map<Long, Option> optionsLookup;

    private final FieldConfig relatedField;
    private final CollectionReorderer<Option> reorderer;
    private final OptionsManager optionsManager;

    public OptionsImpl(List<Option> options, FieldConfig relatedField, CollectionReorderer<Option> reorderer, OptionsManager optionsManager)
    {
        this.relatedField = relatedField;
        this.reorderer = reorderer;
        this.optionsManager = optionsManager;

        optionsLookup = new HashMap<Long, Option>();

        populateFromOptions(options, relatedField);
    }

    private void populateFromOptions(List options, FieldConfig relatedField)
    {
        Map parentChildMap = new MultiHashMap();
        if (options != null && !options.isEmpty())
        {
            for (final Object option1 : options)
            {
                Option option = (Option) option1;
                Option parentOption = option.getParentOption();
                parentChildMap.put(parentOption != null ? parentOption.getOptionId() : null, option);
                optionsLookup.put(option.getOptionId(), option);
            }

            // Get the base level
            Collection<Option> rootOptions = (Collection) parentChildMap.get(null);
            this.addAll(rootOptions);
        }
    }

    public List<Option> getRootOptions()
    {
        return this;
    }


    private Collection<Option> getPeerOptions(Option option)
    {
        final Option parentOption = option.getParentOption();
        if (parentOption != null)
        {
            return parentOption.getChildOptions();
        }
        else
        {
            return getRootOptions();
        }
    }

    public Option getOptionForValue(String value, Long parentOptionId)
    {

        Collection<Option> optionsForParent;
        if (parentOptionId != null)
        {
            optionsForParent = getOptionById(parentOptionId).getChildOptions();
        }
        else
        {
            optionsForParent = getRootOptions();
        }

        if (optionsForParent != null)
        {
            for (Option option : optionsForParent)
            {
                if (option != null && option.getValue() != null && option.getValue().equalsIgnoreCase(value))
                {
                    return option;
                }
            }
        }

        return null;
    }

    public void setValue(Option option, String value)
    {
        optionsManager.setValue(option, value);
    }

    public Option addOption(Option parent, String value)
    {
        Collection parentColl;
        Long parentOptionId;
        if (parent != null)
        {
            parentColl = parent.getChildOptions();
            parentOptionId = parent.getOptionId();
        }
        else
        {
            parentColl = getRootOptions();
            parentOptionId = null;
        }

        Long lastPosition = new Long(parentColl != null ? parentColl.size() : 0);
        return optionsManager.createOption(getRelatedFieldConfig(), parentOptionId, lastPosition, value);
    }

    public void removeOption(Option option)
    {
        optionsManager.deleteOptionAndChildren(option);

        // Renumber the list
        Collection<Option> peers = getPeerOptions(option);

        int i = 0;
        for (Iterator iterator = peers.iterator(); iterator.hasNext();)
        {
            Option currentOption = (Option) iterator.next();
            if (currentOption.equals(option))
            {
                iterator.remove();
            }
            else
            {
                currentOption.setSequence((long) i);
                i++;
            }
        }

        optionsManager.updateOptions(peers);
    }

    public void sortOptionsByValue(Option parentOption)
    {
        List<Option> options = new ArrayList<Option>(parentOption != null ? parentOption.getChildOptions() : getRootOptions());
        Collections.sort(options, new BeanComparatorIgnoreCase<Option>("value"));
        renumberOptions(options);

        optionsManager.updateOptions(options);
    }

    public void moveOptionToPosition(Map<Integer, Option> positionsToOptions)
    {
        if (positionsToOptions.isEmpty())
        { return; }

        // Assume that all options in the map are from the same option set
        Option option = positionsToOptions.values().iterator().next();
        List<Option> peerOptions = new ArrayList<Option>(getPeerOptions(option));
        reorderer.moveToPosition(peerOptions, positionsToOptions);
        renumberOptions(peerOptions);

        optionsManager.updateOptions(peerOptions);
    }

    public void moveToStartSequence(Option option)
    {
        List<Option> peerOptions = new ArrayList<Option>(getPeerOptions(option));
        reorderer.moveToStart(peerOptions, option);
        renumberOptions(peerOptions);

        optionsManager.updateOptions(peerOptions);
    }

    public void incrementSequence(Option option)
    {
        List<Option> peerOptions = new ArrayList<Option>(getPeerOptions(option));
        reorderer.decreasePosition(peerOptions, option);
        renumberOptions(peerOptions);

        optionsManager.updateOptions(peerOptions);
    }

    public void decrementSequence(Option option)
    {
        List<Option> peerOptions = new ArrayList<Option>(getPeerOptions(option));
        reorderer.increasePosition(peerOptions, option);
        renumberOptions(peerOptions);

        optionsManager.updateOptions(peerOptions);
    }

    public void moveToLastSequence(Option option)
    {
        List<Option> peerOptions = new ArrayList<Option>(getPeerOptions(option));
        reorderer.moveToEnd(peerOptions, option);
        renumberOptions(peerOptions);

        optionsManager.updateOptions(peerOptions);
    }

    public void disableOption(Option option)
    {
        optionsManager.disableOption(option);
    }

    public void enableOption(Option option)
    {
        optionsManager.enableOption(option);
    }

    public Option getOptionById(Long optionId)
    {
        return optionsLookup.get(optionId);
    }

    public FieldConfig getRelatedFieldConfig()
    {
        return relatedField;
    }


    private void renumberOptions(List options)
    {
        if (options != null)
        {
            for (int i = 0; i < options.size(); i++)
            {
                Option option = (Option) options.get(i);
                option.setSequence((long) i);
            }
        }
    }

}
