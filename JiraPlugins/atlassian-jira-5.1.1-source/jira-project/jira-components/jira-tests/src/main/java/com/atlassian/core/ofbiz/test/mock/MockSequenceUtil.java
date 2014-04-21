package com.atlassian.core.ofbiz.test.mock;

import org.ofbiz.core.entity.SequenceUtil;
import org.ofbiz.core.entity.model.ModelEntity;

import java.util.HashMap;
import java.util.Map;

/**
 * This was taken from atlassian-ofbiz and placed into its now rightful home of JIRA.
 *
 * @since 4.3
 */
public class MockSequenceUtil extends SequenceUtil {
    Map sequences = new HashMap();

    public MockSequenceUtil(String helperName, ModelEntity seqEntity, String nameFieldName, String idFieldName) {
        super(helperName, seqEntity, nameFieldName, idFieldName);
    }

    public Long getNextSeqId(String seqName) {
        Long l = null;

        synchronized (sequences) {
            l = (Long) sequences.get(seqName);

            if (l == null) {
                l = new Long(0);
            }

            l = new Long(l.longValue() + 1);

            sequences.put(seqName, l);
        }

        return new Long(l.longValue());
    }
}
