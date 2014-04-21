package com.atlassian.soy.impl;

import com.google.template.soy.data.SoyData;
import com.google.template.soy.data.SoyListData;
import com.google.template.soy.data.SoyMapData;
import com.google.template.soy.data.restricted.BooleanData;
import com.google.template.soy.data.restricted.FloatData;
import com.google.template.soy.data.restricted.IntegerData;
import com.google.template.soy.data.restricted.NullData;
import com.google.template.soy.data.restricted.StringData;
import org.hamcrest.core.Is;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public class SoyDataConverterTest
{

    public enum TestEnum
    {
        SINGLETON
    }

    @Test
    public void testConvertToSoyData() throws Exception
    {
        assertConvertsToType("", StringData.class);
        assertConvertsToType(TestEnum.SINGLETON, EnumData.class);

        assertConvertsToType(5, IntegerData.class);
        assertConvertsToType(Integer.valueOf(5).shortValue(), IntegerData.class);
        assertConvertsToType(Integer.valueOf(5).byteValue(), IntegerData.class);
        
        assertConvertsToType(Integer.valueOf(Integer.MAX_VALUE).longValue(), IntegerData.class);
        assertConvertsToType(Integer.valueOf(Integer.MIN_VALUE).longValue(), IntegerData.class);
        assertConvertsToType(Integer.valueOf(0).longValue(), IntegerData.class);
        
        assertConvertsToType(Long.MAX_VALUE, FloatData.class);

        assertConvertsToType(true, BooleanData.class);
        assertConvertsToType(false, BooleanData.class);
        assertConvertsToType((Object) null, NullData.class);
        
        assertConvertsToType(Collections.emptyList(), SoyListData.class);
        assertConvertsToType(Collections.emptySet(), SoyListData.class);
        assertConvertsToType(new Object[0], SoyListData.class);
        assertConvertsToType(new String[0], SoyListData.class);

        assertConvertsToType(Collections.emptyMap(), SoyMapData.class);
    }

    @Test
    public void testConvertFromSoyData() throws Exception
    {
        assertConvertsToType(StringData.forValue(""), String.class);
        assertConvertsToType(new EnumData(TestEnum.SINGLETON), TestEnum.class);

        assertConvertsToType(IntegerData.forValue(0), Integer.class);

        assertConvertsToType(FloatData.forValue(0), Double.class);

        assertConvertsToType(BooleanData.TRUE, Boolean.class);

        assertNull(SoyDataConverter.convertFromSoyData(NullData.INSTANCE));

        assertConvertsToType(new SoyListData(1, 2), List.class);

        assertConvertsToType(new SoyMapData(Collections.singletonMap("key", 1)), Map.class);
    }

    public void assertConvertsToType(SoyData value, Class<?> type)
    {
        final Object object = SoyDataConverter.convertFromSoyData(value);
        assertThat(object, Is.is(type));
    }
    
    public void assertConvertsToType(Object value, Class<? extends SoyData> type)
    {
        final SoyData soyData = SoyDataConverter.convertToSoyData(value);
        assertNotNull(soyData);
        assertThat(soyData, Is.is(type));
    }
}
