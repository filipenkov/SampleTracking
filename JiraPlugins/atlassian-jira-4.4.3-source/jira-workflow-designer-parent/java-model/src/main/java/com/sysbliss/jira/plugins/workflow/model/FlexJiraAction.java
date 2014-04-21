/**
 * 
 */
package com.sysbliss.jira.plugins.workflow.model;

import java.util.List;

/**
 * @author jdoklovic
 * 
 */
public interface FlexJiraAction extends FlexWorkflowObject, FlexJiraMetadataContainer {

    /**
     * @param result
     */
    void setUnconditionalResult(FlexJiraResult result);

    FlexJiraResult getUnconditionalResult();

    boolean getIsCommon();

    void setIsCommon(boolean common);

    /**
     * @param view
     */
    void setView(String view);

    String getView();

    void setFieldScreenId(String id);

    String getFieldScreenId();

    /**
     * @param validators
     */
    void setValidators(List validators);

    List getValidators();

    /**
     * @param conditions
     */
    void setConditions(FlexJiraConditions conditions);

    FlexJiraConditions getConditions();

    String getLabel();
    void setLabel(String label);
}
