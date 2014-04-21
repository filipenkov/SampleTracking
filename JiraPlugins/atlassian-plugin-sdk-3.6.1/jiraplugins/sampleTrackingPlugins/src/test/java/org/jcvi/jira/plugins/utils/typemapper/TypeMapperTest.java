package org.jcvi.jira.plugins.utils.typemapper;

import org.junit.Test;
import static org.junit.Assert.*;
import static org.junit.Assert.assertArrayEquals;

import java.util.*;

/**
 * User: pedworth
 * Date: 11/2/11
 * Check that the util class behaves
 * ClassName must end in Test to be run by surefire, under Atlassians config.
 */
public class TypeMapperTest {
    private static final String[] emptyList = {};
    private static final String[] oneItem = {"one"};
    private static final String[] multiple = {"aaa","second","cccc",};
//Collections can't generally contain null
//    private static final String[] withNullStart = {null,"two","three"};
//    private static final String[] withNullMid = {"one",null,"three"};
//    private static final String[] withNullEnd = {"one","two",null};

    private static final String[][] testContent = {emptyList,
                                                   oneItem,
                                                   multiple};

    @Test(expected = NullPointerException.class)
    public void nullMapperNullInputTest() {
        //create an instance, with first null mapper
        TypeMapperUtils.mapList(null, null);
    }
    @Test(expected = NullPointerException.class)
    public void nullMapperNotNullInputTest() {
        List<Object> input = new ArrayList<Object>();
        //create an instance, with first null mapper
        TypeMapperUtils.mapList(null, input);
    }
    @Test
    public void notNullMapperNullInputTest() {
        TypeMapper<Object,Object> mapper
                = new TypeMapperUtils.NopMapper<Object>();
        Collection<Object> result = TypeMapperUtils.
                mapList(mapper, null);
        assertNull("Null in should result in Null out",result);
    }
    @Test
    public void nopMapperTest() {
        TypeMapper<String,String> mapper
                = new TypeMapperUtils.NopMapper<String>();
        for (String[] content : testContent) {
            assertMapper(content,mapper,content);
        }

    }
    @Test
    public void stringMapperTest() {
        TypeMapper<Number,String> mapper = new TypeMapperUtils.StringMapper<Number>();
        Number[] numbers = { 1, 2, 4 };
        String[] strings = { "1", "2", "4" };
        assertMapper(numbers,mapper,strings);
    }

    private <T> void assertMapper(T[] input, TypeMapper<T,String> mapper, String[] output) {
        List<T> in = Arrays.asList(input);
        List<String> result =
                TypeMapperUtils.mapList(mapper, in);
        assertArrayEquals(output,result.toArray());

    }
}
