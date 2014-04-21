package com.atlassian.config.wizard;

import com.atlassian.config.ConfigurationException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: ROSS
 * Date: 15/03/2004
 * Time: 13:50:52
 * To change this template use File | Settings | File Templates.
 */
public class SetupWizard
{
    private List steps = new ArrayList();

    private SetupStep currentStep = null;

    private SaveStrategy saveStrategy = null;

    public void addStep(SetupStep step)
    {
        addStep(step, steps.size());
    }

    public void addStep(SetupStep step, int index)
    {
        step.setIndex(index);
        steps.add(index, step);
    }

    public SetupStep getCurrentStep()
    {
        if (currentStep == null && steps.size() > 0)
        {
            currentStep = getStep(0);
        }
        return currentStep;
    }

    protected void setCurrentStep(SetupStep step)
    {
        currentStep = step;
    }

    public String getStepNameByIndex(int index)
    {
        if (index >= steps.size())
        {
            return null;
        }
        else
        {
            return ((SetupStep) steps.get(index)).getName();
        }
    }

    public int getStepIndexByName(String name)
    {
        SetupStep setupStep;
        for (Iterator iterator = steps.iterator(); iterator.hasNext();)
        {
            setupStep = (SetupStep) iterator.next();
            if (setupStep.getName().equals(name))
            {
                return setupStep.getIndex();
            }
        }
        return -1;
    }

    /**
     * @return true if a given step is finished
     */
    public boolean isSetupStepFinished(String stepName) throws StepNotFoundException
    {
        return isStepBeforeCurrentStep(stepName);
    }


    private boolean isStepBeforeCurrentStep(String stepName) throws StepNotFoundException
    {
        int pos = getStepIndexByName(stepName);
        if (pos == -1)
            throw new StepNotFoundException(stepName);
        return pos < getCurrentStep().getIndex();
    }


    public boolean isSetupComplete()
    {
        return (steps.size() - 1) == getCurrentStep().getIndex();
    }

    public void previous() throws ConfigurationException
    {
        if (getCurrentStep().getIndex() > 0)
        {
            int index = getCurrentStep().getIndex() - 1;
            setCurrentStep((SetupStep) steps.get(index));
            getCurrentStep().onStart();
            save();
        }
    }

    public void next() throws ConfigurationException
    {
        if (!isSetupComplete())
        {
            int nextIndex = getCurrentStep().getIndex() + 1;
            getCurrentStep().onNext();
            setCurrentStep((SetupStep) steps.get(nextIndex));
            getCurrentStep().onStart();
            save();
        }
    }

    public void next(String stepName) throws StepNotFoundException, ConfigurationException
    {
        int step = getStepIndexByName(stepName);
        if (step == -1)
        {
            throw new StepNotFoundException(stepName);
        }
        if (getCurrentStep() != null) getCurrentStep().onNext();
        setCurrentStep((SetupStep) steps.get(step));
        getCurrentStep().onStart();
        save();
    }

    public void start() throws StepNotFoundException, ConfigurationException
    {
        if (steps.size() == 0)
        {
            throw new StepNotFoundException("There are no steps associated with this wizard");
        }
        else
        {
            currentStep = (SetupStep) steps.get(0);
            currentStep.onStart();
            save();
        }

    }

    public void finish() throws ConfigurationException
    {
        try
        {
            String current = getStepNameByIndex(steps.size() - 1);
            if (!current.equals(getCurrentStep().getName()))
            {
                next(current);
            }
            else
            {
                getCurrentStep().onNext();
                save();
            }
        }
        catch (StepNotFoundException e)
        {  /*ignore*/  }
    }

    protected synchronized void save() throws ConfigurationException
    {
        if (saveStrategy != null)
        {
            saveStrategy.save(this);
        }
    }

    public SaveStrategy getSaveStrategy()
    {
        return saveStrategy;
    }

    public void setSaveStrategy(SaveStrategy saveStrategy)
    {
        this.saveStrategy = saveStrategy;
    }

    public SetupStep getStep(String name)
    {
        int index = getStepIndexByName(name);
        return getStep(index);
    }

    public SetupStep getStep(int index)
    {
        if (index == -1)
        {
            return null;
        }
        return (SetupStep) steps.get(index);
    }

}
