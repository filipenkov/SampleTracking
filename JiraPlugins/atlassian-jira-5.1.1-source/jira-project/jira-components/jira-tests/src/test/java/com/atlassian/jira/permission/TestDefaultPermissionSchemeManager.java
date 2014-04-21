package com.atlassian.jira.permission;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.local.ListeningTestCase;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.scheme.SchemeType;
import com.atlassian.jira.user.MockUser;
import org.easymock.MockControl;
import org.easymock.classextension.MockClassControl;
import org.junit.Test;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests the DefaultPermissionSchemeManager class.
 *
 * @since v3.12
 */
public class TestDefaultPermissionSchemeManager extends ListeningTestCase
{

    @Test
    public void testHasSchemePermissionWithUser() throws GenericEntityException
    {

        MockControl schemeTypeMock = MockControl.createStrictControl(SchemeType.class);
        SchemeType schemeType = (SchemeType) schemeTypeMock.getMock();
        schemeType.isValidForPermission(666);
        schemeTypeMock.setReturnValue(false);
        schemeType.isValidForPermission(123);
        schemeTypeMock.setReturnValue(true);
        User user = new MockUser("John");
        schemeType.hasPermission(null, null, user, false);
        schemeTypeMock.setReturnValue(true);
        schemeTypeMock.replay();
        Map types = new HashMap();
        types.put("typename", schemeType);

        final MockControl mockPermissionTypeManagerControl = MockClassControl.createControl(PermissionTypeManager.class);
        final PermissionTypeManager mockPermissionTypeManager = (PermissionTypeManager) mockPermissionTypeManagerControl.getMock();
        mockPermissionTypeManager.getTypes();
        mockPermissionTypeManagerControl.setReturnValue(types, MockControl.ONE_OR_MORE);
        mockPermissionTypeManagerControl.replay();

        List permissions = EasyList.build(new MockGenericValue("permission", EasyMap.build("type", "typename", "parameter", null)));
        DefaultPermissionSchemeManager defaultPermissionSchemeManager = createDefaultPermissionSchemeManager(mockPermissionTypeManager, permissions);

        assertFalse(defaultPermissionSchemeManager.hasSchemePermission(new Long(666), null, null, user, false));
        assertTrue(defaultPermissionSchemeManager.hasSchemePermission(new Long(123), null, null, user, false));

        schemeTypeMock.verify();
        mockPermissionTypeManagerControl.verify();
    }

    @Test
    public void testHasSchemePermissionWithAnonymousUser() throws GenericEntityException
    {

        // type 1 does not give permission
        MockControl schemeType1Mock = MockControl.createStrictControl(SchemeType.class);
        SchemeType schemeType1 = (SchemeType) schemeType1Mock.getMock();
        schemeType1.hasPermission(null, null);
        schemeType1Mock.setReturnValue(false, 2);
        schemeType1Mock.replay();

        // type 2 does give  permission
        MockControl schemeType2Mock = MockControl.createStrictControl(SchemeType.class);
        SchemeType schemeType2 = (SchemeType) schemeType2Mock.getMock();
        schemeType2.hasPermission(null, null);
        schemeType2Mock.setReturnValue(true);
        schemeType2Mock.replay();

        Map types = new HashMap();
        types.put("type1", schemeType1);

        final MockControl mockPermissionTypeManagerControl = MockClassControl.createControl(PermissionTypeManager.class);
        final PermissionTypeManager mockPermissionTypeManager = (PermissionTypeManager) mockPermissionTypeManagerControl.getMock();
        mockPermissionTypeManager.getTypes();
        mockPermissionTypeManagerControl.setReturnValue(types, MockControl.ONE_OR_MORE);
        mockPermissionTypeManagerControl.replay();

        MockGenericValue permission1Gv = new MockGenericValue("permission", EasyMap.build("type", "type1", "parameter", null));
        List permissions = new ArrayList();
        permissions.add(permission1Gv);
        DefaultPermissionSchemeManager defaultPermissionSchemeManager = createDefaultPermissionSchemeManager(mockPermissionTypeManager, permissions);

        assertFalse(defaultPermissionSchemeManager.hasSchemePermission(new Long(123), null, null, null, false));
        MockGenericValue permission2Gv = new MockGenericValue("permission", EasyMap.build("type", "type2", "parameter", null));
        permissions.add(permission2Gv);
        types.put("type2", schemeType2);
        assertTrue(defaultPermissionSchemeManager.hasSchemePermission(new Long(123), null, null, null, false));

        schemeType2Mock.verify();
        mockPermissionTypeManagerControl.verify();
    }

    private DefaultPermissionSchemeManager createDefaultPermissionSchemeManager(PermissionTypeManager permissionTypeManager, final List permissions)
    {
        return new DefaultPermissionSchemeManager(null, permissionTypeManager, null, null, null, null, null, null)
        {
            public List getEntities(GenericValue scheme, Long permissionId)
            {
                return permissions;
            }
        };
    }
}
