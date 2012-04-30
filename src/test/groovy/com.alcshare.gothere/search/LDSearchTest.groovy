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
import spock.lang.Specification

public class LDSearchTest extends Specification
{
   LocationInfo basement;
   LocationInfo bvav11;
   LocationInfo bvav12;
   LocationInfo bvav21;
   LocationInfo bvav22;
   LocationInfo firstFloor;
   LocationInfo ffvav13;
   SearchCache searchCache;

   def setup()
   {
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

   def "search empty string"()
   {
      given:
         LDSearch search = new LDSearch(searchCache)
      expect:
         search.search("", 10) == []
   }

   def "search VAV"()
   {
      given:
         LDSearch search = new LDSearch(searchCache)
      expect:
         search.search("VAV", 10) == [bvav11, bvav12, bvav21, bvav22, ffvav13]
   }

   def "search Bisment"()
   {
      given:
         LDSearch search = new LDSearch(searchCache)
      expect:
         search.search("Bisment", 10) == [basement, bvav11, bvav12, bvav21, bvav22]
   }

   def "search Fl"()
   {
      given:
         LDSearch search = new LDSearch(searchCache)
      expect:
         search.search("Fl", 10) == [firstFloor, ffvav13]
   }

   def "search Bisment VAV"()
   {
      given:
         LDSearch search = new LDSearch(searchCache)
      expect:
         search.search("Bisment VAV", 10) == [bvav11, bvav12, bvav21, bvav22]
   }

   def "search with extra spaces"()
   {
      given:
         LDSearch search = new LDSearch(searchCache)
      expect:
         search.search("  Bisment   VAV  ", 10) == [bvav11, bvav12, bvav21, bvav22]
   }

   def createLocInfo(String dispName, String dispPathStart)
   {
      Location location = Mock()
      location.getDisplayName() >> dispName
      String dispPath = dispPathStart + " / " + dispName
      String[] words = dispPath.replaceAll(" / ", " ").split(" ")
      return new LocationInfo(location, dispPath, words, 1)
   }

   def createSearchCache(List<LocationInfo> locInfos)
   {
      Map<String, List<LocationInfo>> cache = [:]
      locInfos.each { info ->
         info.displayWords.each { word ->
            if (cache[word] == null)
               cache[word] = []

            cache[word].add(info)
         }
      }

      def entries = []
      cache.each { key, value -> entries.add(new SearchCache.CacheEntry(key, value)) }

      SearchCache searchCache = Mock()
      searchCache.getCache() >> entries
      return searchCache
   }
}