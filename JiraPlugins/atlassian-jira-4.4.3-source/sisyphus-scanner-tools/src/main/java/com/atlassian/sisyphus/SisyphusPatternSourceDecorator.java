package com.atlassian.sisyphus;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class SisyphusPatternSourceDecorator implements SisyphusPatternSource
{
	private final List<SisyphusPatternSource> delegateSources = new ArrayList<SisyphusPatternSource>();
	
	public void add(SisyphusPatternSource source)
	{
		delegateSources.add(source);
	}
	
	public SisyphusPattern getPattern(String patternID) 
	{
		for (SisyphusPatternSource src : delegateSources) 
		{
			SisyphusPattern pattern = src.getPattern(patternID);
			if(pattern != null)
				return pattern;
		}
		return null;
	}

	public int size() 
	{
		int size = 0;
		for (SisyphusPatternSource src : delegateSources) 
			size += src.size();
		return size;
	}

	public Iterator<SisyphusPattern> iterator() 
	{
		return new IterableSequencer(delegateSources);
	}
	
	static class IterableSequencer implements Iterator<SisyphusPattern>
	{
		private final Iterator<? extends Iterable<SisyphusPattern>> sources;
		private Iterator<SisyphusPattern> currentIterator;

		public IterableSequencer(Iterable<? extends Iterable<SisyphusPattern>> sources) 
		{
			this.sources = sources.iterator();
		}

		public boolean hasNext() 
		{
			if(currentIterator == null)
			{
				// most likely to be the initializer call
				if(sources.hasNext())
				{
					currentIterator = sources.next().iterator();
					return hasNext();
				}
				else
				{
					// the sources is empty
					return false;
				}
			}
			else
			{
				if(currentIterator.hasNext())
				{
					return true;
				}
				else if(sources.hasNext())
				{
					currentIterator = sources.next().iterator();
					return hasNext();
				}
				else
				{
					return false;
				}
			}
		}

		public SisyphusPattern next() 
		{
			if(hasNext())
				return currentIterator.next();
			else
				throw new NoSuchElementException();
		}

		public void remove() 
		{
			if(currentIterator != null)
				currentIterator.remove();
		}
	}
}