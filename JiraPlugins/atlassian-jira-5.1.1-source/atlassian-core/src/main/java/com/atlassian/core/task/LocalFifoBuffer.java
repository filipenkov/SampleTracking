package com.atlassian.core.task;

import org.apache.commons.collections.Buffer;
import org.apache.commons.collections.BufferUtils;
import org.apache.commons.collections.buffer.UnboundedFifoBuffer;

import java.util.Collection;

public class LocalFifoBuffer implements FifoBuffer
{
    private Buffer buffer = BufferUtils.synchronizedBuffer(new UnboundedFifoBuffer());

    public synchronized Object remove()
    {
        if (!buffer.isEmpty())
            return buffer.remove();
        else
            return null;
    }

    public void add(Object o)
    {
        buffer.add(o);
    }

    public int size()
    {
        return buffer.size();
    }

    public Collection getItems()
    {
        return buffer;
    }

    public void clear()
    {
        buffer.clear();
    }
}