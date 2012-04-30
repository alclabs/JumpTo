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

import com.controlj.green.addonsupport.access.Location;
import com.controlj.green.addonsupport.access.SystemAccess;
import spock.lang.Specification
import com.controlj.green.addonsupport.access.Visitor;

public class SystemAccessSearchCacheTest extends Specification
{
   def mockLocation(String dispName, Location parent = null)
   {
      Location location = Mock()
      location.getDisplayName() >> dispName
      location.hasParent() >> { parent != null }
      location.getParent() >> parent
      return location
   }

   def "test cache"()
   {
      given:
         def geoRoot = mockLocation("Geo Root", null);
         def system = mockLocation("System Node", null);
         def area = mockLocation("Area Node", null);
         def equipment = mockLocation("Equipment", null);

         SystemAccess sysAccess = Mock()
         sysAccess.getGeoRoot() >> geoRoot
         sysAccess.visit(geoRoot, _) >> { loc, visitor ->
            visitor.visitSystem(system);
            visitor.visitArea(area);
            visitor.visitEquipment(equipment);
         }

      when:
         SystemAccessSearchCache cache = new SystemAccessSearchCache();
         cache.buildCache(sysAccess);
      then:
         findWord("geo", cache) == null
         findWord("root", cache) == null
         findWord("system", cache) == [system]
         findWord("area", cache) == [area]
         findWord("equipment", cache) == [equipment]
         findWord("node", cache) == [system, area]
         cache.getCache().size() == 4
   }

   private List<Location> findWord(String word, SystemAccessSearchCache cache)
   {
      for (SearchCache.CacheEntry entry : cache.getCache())
      {
         if (word.equals(new String(entry.word)))
            return entry.locations.collect { it.location }
      }
      return null;
   }
}