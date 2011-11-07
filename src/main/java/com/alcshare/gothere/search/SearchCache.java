/*=============================================================================
                    AUTOMATED LOGIC CORPORATION
            Copyright (c) 1999 - 2011 All Rights Reserved
     This document contains confidential/proprietary information.
===============================================================================

   @(#)ISearchCache

   Author(s) jmurph
   $Log: $    
=============================================================================*/
package com.alcshare.gothere.search;

import com.alcshare.gothere.data.LocationInfo;

import java.util.Collections;
import java.util.List;

public interface SearchCache
{
   List<CacheEntry> getCache();

   final class CacheEntry
   {
      public final char[] word;
      public final List<LocationInfo> locations;
      //private final int locationDepth;

      public CacheEntry(String word, List<LocationInfo> locations)
      {
         this.word = word.toCharArray();
         this.locations = Collections.unmodifiableList(locations);
      }
   }
}
