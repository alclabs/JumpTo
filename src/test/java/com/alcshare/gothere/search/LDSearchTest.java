/*=============================================================================
                    AUTOMATED LOGIC CORPORATION
            Copyright (c) 1999 - 2011 All Rights Reserved
     This document contains confidential/proprietary information.
===============================================================================

   @(#)LDSearchTest

   Author(s) jmurph
   $Log: $    
=============================================================================*/
package com.alcshare.gothere.search;

import com.alcshare.gothere.data.LocationInfo;
import com.controlj.green.addonsupport.access.Location;
import junit.framework.TestCase;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;

import java.util.*;

public class LDSearchTest extends TestCase
{
   final Mockery context = new JUnit4Mockery();
   LocationInfo basement;
   LocationInfo bvav11;
   LocationInfo bvav12;
   LocationInfo bvav21;
   LocationInfo bvav22;
   LocationInfo firstFloor;
   LocationInfo ffvav13;
   SearchCache searchCache;

   @Override protected void setUp() throws Exception
   {
      super.setUp();

      List<LocationInfo> locInfos = new ArrayList<LocationInfo>();
      locInfos.add(basement   = createLocInfo("Basement", "Main Building"));
      locInfos.add(bvav11     = createLocInfo("VAV 1-1", basement.fullDisplayPath));
      locInfos.add(bvav12     = createLocInfo("VAV 1-2", basement.fullDisplayPath));
      locInfos.add(bvav21     = createLocInfo("VAV 2-1", basement.fullDisplayPath));
      locInfos.add(bvav22     = createLocInfo("VAV 2-2", basement.fullDisplayPath));
      locInfos.add(firstFloor = createLocInfo("First Floor", "Main Building"));
      locInfos.add(ffvav13    = createLocInfo("VAV 1-3", firstFloor.fullDisplayPath));
      searchCache = createSearchCache(locInfos);
   }

   public void testSearch_EmptyString()
   {
      LDSearch search = new LDSearch(searchCache);
      List<LocationInfo> results = search.search("", 10);
      List<LocationInfo> expected = Arrays.asList();

      assertEquals(expected, results);
   }

   public void testSearch_VAV()
   {
      LDSearch search = new LDSearch(searchCache);
      List<LocationInfo> results = search.search("VAV", 10);
      List<LocationInfo> expected = Arrays.asList(bvav11, bvav12, bvav21, bvav22, ffvav13);

      assertEquals(expected, results);
   }

   public void testSearch_Bisment()
   {
      LDSearch search = new LDSearch(searchCache);
      List<LocationInfo> results = search.search("Bisment", 10);
      List<LocationInfo> expected = Arrays.asList(basement, bvav11, bvav12, bvav21, bvav22);

      assertEquals(expected, results);
   }

   public void testSearch_Fl()
   {
      LDSearch search = new LDSearch(searchCache);
      List<LocationInfo> results = search.search("Fl", 10);
      List<LocationInfo> expected = Arrays.asList(firstFloor, ffvav13);

      assertEquals(expected, results);
   }

   public void testSearch_Bisment_VAV()
   {
      LDSearch search = new LDSearch(searchCache);
      List<LocationInfo> results = search.search("Bisment VAV", 10);
      List<LocationInfo> expected = Arrays.asList(bvav11, bvav12, bvav21, bvav22);

      assertEquals(expected, results);
   }

   public void testSearch_ExtraSpaces()
   {
      LDSearch search = new LDSearch(searchCache);
      List<LocationInfo> results = search.search("  Bisment   VAV  ", 10);
      List<LocationInfo> expected = Arrays.asList(bvav11, bvav12, bvav21, bvav22);

      assertEquals(expected, results);
   }

   private LocationInfo createLocInfo(final String dispName, String dispPathStart)
   {
      final Location locationMock = context.mock(Location.class, dispName);
      context.checking(new Expectations() {{
         allowing(locationMock).getDisplayName();
         will(returnValue(dispName));
      }});

      String dispPath = dispPathStart + " / " + dispName;
      String[] words = dispPath.replaceAll(" / ", " ").split(" ");
      return new LocationInfo(locationMock, dispPath, words, 1);
   }

   private SearchCache createSearchCache(List<LocationInfo> locInfos)
   {
      Map<String, List<LocationInfo>> cache = new HashMap<String, List<LocationInfo>>();
      for (LocationInfo info : locInfos)
      {
         for (String word : info.displayWords)
         {
            List<LocationInfo> locs = cache.get(word);
            if (locs == null)
            {
               locs = new ArrayList<LocationInfo>();
               cache.put(word, locs);
            }
            locs.add(info);
         }
      }

      List<SearchCache.CacheEntry> entries = new ArrayList<SearchCache.CacheEntry>();
      for (Map.Entry<String, List<LocationInfo>> entry : cache.entrySet())
         entries.add(new SearchCache.CacheEntry(entry.getKey(), entry.getValue()));
      return toSearchCache(entries);
   }

   private SearchCache toSearchCache(final List<SearchCache.CacheEntry> cache)
   {
      return new SearchCache()
      {
         @Override public List<CacheEntry> getCache()
         {
            return Collections.unmodifiableList(cache);
         }
      };
   }
}

