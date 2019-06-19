package org.xmpp.util;

import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;

import java.util.concurrent.atomic.AtomicLong;

public class JIDCache
{
    private final ConcurrentLinkedHashMap<String, ValueWrapper<String>> cache;

    private final String name;
    private final AtomicLong hits = new AtomicLong( 0 );
    private final AtomicLong misses = new AtomicLong( 0 );

    public JIDCache( String name, long capacity )
    {
        this.name = name;
        cache = new ConcurrentLinkedHashMap.Builder<String, ValueWrapper<String>>().maximumWeightedCapacity( capacity ).build();
    }

    public ValueWrapper<String> get( final String node )
    {
        final ValueWrapper<String> result = cache.get( node );
        if ( result == null ) {
            misses.incrementAndGet();
        } else {
            hits.incrementAndGet();
        }
        return result;
    }

    public ValueWrapper<String> put( final String answer, final ValueWrapper<String> value )
    {
        return cache.put( answer, value );
    }

    public void setCapacity( long capacity )
    {
        cache.setCapacity( capacity );
    }

    public long getCapacity()
    {
        return cache.capacity();
    }

    public long getSize()
    {
        return cache.weightedSize();
    }

    public void clear()
    {
        cache.clear();
    }

    public long getHits()
    {
        return hits.get();
    }

    public long getMisses()
    {
        return misses.get();
    }

    public String getName()
    {
        return name;
    }

    @Override
    public String toString()
    {
        return "JIDCache '" + name + "' " + getSize() + "/" + getCapacity();
    }
}
