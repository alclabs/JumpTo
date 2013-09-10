/*=============================================================================
                    AUTOMATED LOGIC CORPORATION
            Copyright (c) 1999 - 2011 All Rights Reserved
     This document contains confidential/proprietary information.
===============================================================================

   @(#)SearchCacheBuilder

   Author(s) jmurph
   $Log: $    
=============================================================================*/
package com.alcshare.gothere.search;

import com.alcshare.gothere.data.LocationInfo;
import com.controlj.green.addonsupport.access.*;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public final class SystemAccessSearchCache implements SearchCache
{
   private Map<String, List<LocationInfo>> cache;
   private List<CacheEntry> cacheEntries;

   public void buildCache(SystemAccess access)
   {
      cache = new HashMap<>();
      cacheEntries = Collections.emptyList();
      
      Location root = access.getGeoRoot();
      access.visit(root, new GeoVisitor()
      {
         @Override public void handleLocation(Location location)
         {
            if (location.getType() != LocationType.System) {
               LocationInfo locInfo = new LocationInfo(location);
               for (String word : locInfo.displayWords)
               {
                  List<LocationInfo> list = cache.get(word);
                  if (list == null)
                  {
                     list = new ArrayList<>();
                     cache.put(word, list);
                  }

                  list.add(locInfo);
               }
            }
         }
      });

      cacheEntries = new ArrayList<>(cache.size());
      optimizeCache();
   }

   @Override public List<CacheEntry> getCache() { return Collections.unmodifiableList(cacheEntries); }
   
   private void optimizeCache()
   {
      for (Map.Entry<String, List<LocationInfo>> entry : cache.entrySet())
      {
         ((ArrayList)entry.getValue()).trimToSize();
         cacheEntries.add(new CacheEntry(entry.getKey(), entry.getValue()));
      }
   }

   public static String getFullDisplayPath(Location loc) {
       if (!loc.hasParent()) {
           return "";
       } else {
           try {
               return getFullDisplayPath(loc.getParent()) + ' ' + loc.getDisplayName();
           } catch (UnresolvableException e) {
               throw new RuntimeException("can't resolve parent even though hasParent is true", e);
           }
       }
   }

   private abstract static class GeoVisitor extends Visitor
   {
      @Override public void visitSystem(@NotNull Location system) { handleLocation(system); }
      @Override public void visitArea(@NotNull Location area) { handleLocation(area); }
      @Override public void visitEquipment(@NotNull Location eq) { handleLocation(eq); }

      abstract void handleLocation(Location location);
   }
}