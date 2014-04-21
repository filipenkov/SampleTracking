package com.atlassian.sisyphus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import junit.framework.TestCase;

import com.atlassian.sisyphus.SisyphusPatternSourceDecorator.IterableSequencer;


public class TestIteratorSequencer extends TestCase
{
	public void testEmpty() throws Exception 
	{
		Iterator<SisyphusPattern> iter = new IterableSequencer(new ArrayList<Iterable<SisyphusPattern>>());
		assertFalse(iter.hasNext());
		try
		{
			iter.next();
			fail("Expected a NoSuchElementException here");
		}
		catch(NoSuchElementException e)
		{
			// expected
		}
	}
	
	public void testSequence() throws Exception 
	{
		List<Iterable<SisyphusPattern>> list = new ArrayList<Iterable<SisyphusPattern>>();
		list.add(Arrays.asList(new SisyphusPattern("A")));
		list.add(Arrays.asList(new SisyphusPattern("b")));
		list.add(Arrays.asList(new SisyphusPattern("C")));
		list.add(Arrays.asList(new SisyphusPattern("d")));
		list.add(Arrays.asList(new SisyphusPattern("E")));
		list.add(Arrays.asList(new SisyphusPattern("f")));
		
		Iterator<SisyphusPattern> iter = new IterableSequencer(list);
		assertEquals("A", iter.next().getId());
		assertEquals("b", iter.next().getId());
		assertEquals("C", iter.next().getId());
		assertEquals("d", iter.next().getId());
		assertEquals("E", iter.next().getId());
		assertEquals("f", iter.next().getId());
		assertFalse(iter.hasNext());
	}
	
	public void testMultiSourceDelegator() throws Exception 
	{
		SisyphusPatternSourceDecorator decorator = new SisyphusPatternSourceDecorator();
		assertEquals(0, decorator.size());
		assertNull(decorator.getPattern("A"));
		
		decorator.add(new TestSource(new SisyphusPattern("A")));
		decorator.add(new TestSource(new SisyphusPattern("b")));
		decorator.add(new TestSource(new SisyphusPattern("C")));
		decorator.add(new TestSource(new SisyphusPattern("d")));
		decorator.add(new TestSource(new SisyphusPattern("E")));
		decorator.add(new TestSource(new SisyphusPattern("f")));

		assertEquals(6, decorator.size());
		assertEquals("A", decorator.getPattern("A").getId());
		assertEquals("b", decorator.getPattern("b").getId());
		assertEquals("C", decorator.getPattern("C").getId());
		assertEquals("d", decorator.getPattern("d").getId());
		assertEquals("E", decorator.getPattern("E").getId());
		assertEquals("f", decorator.getPattern("f").getId());

		Iterator<SisyphusPattern> iter = decorator.iterator();
		assertEquals("A", iter.next().getId());
		assertEquals("b", iter.next().getId());
		assertEquals("C", iter.next().getId());
		assertEquals("d", iter.next().getId());
		assertEquals("E", iter.next().getId());
		assertEquals("f", iter.next().getId());
		assertFalse(iter.hasNext());
	}
	
	private static class TestSource extends MappedSisyphusPatternSource
	{
		public TestSource(SisyphusPattern pattern) 
		{
			super.regexMap.put(pattern.getId(), pattern);
		}
	}
}
