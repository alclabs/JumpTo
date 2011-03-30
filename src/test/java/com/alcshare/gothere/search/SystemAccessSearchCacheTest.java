/*=============================================================================
                    AUTOMATED LOGIC CORPORATION
            Copyright (c) 1999 - 2011 All Rights Reserved
     This document contains confidential/proprietary information.
===============================================================================

   @(#)SystemAccessSearchCacheTest

   Author(s) jmurph
   $Log: $    
=============================================================================*/
package com.alcshare.gothere.search;

import com.alcshare.gothere.data.LocationInfo;
import com.controlj.green.addonsupport.access.Location;
import com.controlj.green.addonsupport.access.SystemAccess;
import com.controlj.green.addonsupport.access.Visitor;
import junit.framework.TestCase;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.api.Action;
import org.jmock.api.Invocation;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.action.CustomAction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SystemAccessSearchCacheTest extends TestCase
{
   final Mockery context = new JUnit4Mockery();

   public void testCache() throws Exception
   {
      final Location geoRootMock = createLocationMock("Geo Root", null);
      final Location systemMock = createLocationMock("System Node", null);
      final Location areaMock = createLocationMock("Area Node", null);
      final Location equipmentMock = createLocationMock("Equipment", null);

      final SystemAccess systemAccessMock = context.mock(SystemAccess.class);
      context.checking(new Expectations() {{
         allowing(systemAccessMock).getGeoRoot();
         will(returnValue(geoRootMock));
         allowing(systemAccessMock).visit(with(same(geoRootMock)), with(any(Visitor.class)));
         will(doVisit(systemMock, areaMock, equipmentMock));
      }});

      // test building the cache
      SystemAccessSearchCache cache = new SystemAccessSearchCache();
      cache.buildCache(systemAccessMock);

      assertNull(findWord("geo", cache.getCache()));
      assertNull(findWord("root", cache.getCache()));
      assertEquals(Arrays.asList(systemMock), findWord("system", cache.getCache()));
      assertEquals(Arrays.asList(areaMock), findWord("area", cache.getCache()));
      assertEquals(Arrays.asList(equipmentMock), findWord("equipment", cache.getCache()));
      assertEquals(Arrays.asList(systemMock, areaMock), findWord("node", cache.getCache()));
      assertEquals(4, cache.getCache().size());
   }

   private List<Location> findWord(String word, List<SearchCache.CacheEntry> cacheEntries)
   {
      for (SearchCache.CacheEntry entry : cacheEntries)
      {
         if (word.equals(new String(entry.word)))
         {
            List<Location> locs = new ArrayList<Location>(entry.locations.size());
            for (LocationInfo info : entry.locations)
               locs.add(info.location);
            return locs;
         }
      }
      return null;
   }
   private static Action doVisit(final Location systemLoc, final Location areaLoc, final Location eqLoc)
   {
      return new CustomAction("does visitor callbacks")
      {
         @Override public Object invoke(Invocation invocation) throws Throwable
         {
            Visitor visitor = (Visitor)invocation.getParameter(1);
            visitor.visitSystem(systemLoc);
            visitor.visitArea(areaLoc);
            visitor.visitEquipment(eqLoc);
            return null;
         }
      };
   }

   private Location createLocationMock(final String displayName, final Location parent) throws Exception
   {
      final Location mock = context.mock(Location.class, displayName);
      context.checking(new Expectations() {{
         allowing(mock).getDisplayName();
         will(returnValue(displayName));
         allowing(mock).hasParent();
         will(returnValue(parent != null));
         allowing(mock).getParent();
         will(returnValue(parent));
      }});
      return mock;
   }
}

