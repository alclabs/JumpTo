/*=============================================================================
                    AUTOMATED LOGIC CORPORATION
            Copyright (c) 1999 - 2011 All Rights Reserved
     This document contains confidential/proprietary information.
===============================================================================

   @(#)LDSearch

   Author(s) jmurph
   $Log: $    
=============================================================================*/
package com.alcshare.gothere.search;

import com.alcshare.gothere.data.LocationInfo;

import java.util.*;

/**<!=========================================================================>
   Allows searching over a given search-cache, using a single LevenshteinDistance
   object.  This means that the search is done using a single thread (see
   {@link LevenshteinDistance} for an explanation of why).  Just call {@link #search}
   to get a list of results back from the search-cache.
   @author jmurph
<!==========================================================================>*/
public class LDSearch
{
   private SearchCache cache;
   private LevenshteinDistance ld;

   /**<!====== LDSearch ======================================================>
      Creates a new searcher over the given search-cache.
      @author jmurph
   <!=======================================================================>*/
   public LDSearch(SearchCache cache)
   {
      this.cache = cache;
      ld = new LevenshteinDistance(30, true);
   }

   /**<!====== search ========================================================>
      Returns up to <code>maxResults</code> results for the given pattern string.
      Basically, the pattern string is broken into words, and these words are
      matched against the search-cache.  Words too far (in edit distance) are
      just discarded, while remaining words are ordered by edit distance.  If
      the pattern consists of multiple words, the results of each word are and-ed
      (set intersect operation) together.
      <!      Name       Description>
      @param  pattern    The search pattern.
      @param  maxResults The maximum number of results to return.
      @return The locations that match the search pattern.
      @author jmurph
   <!=======================================================================>*/
   public List<LocationInfo> search(String pattern, int maxResults)
   {
      String[] words = pattern.toLowerCase().split("\\s");

      List<Map<LocationInfo, Integer>> allResults = new ArrayList<Map<LocationInfo, Integer>>();
      for (String word : words)
      {
         if (!word.isEmpty())
         {
            Map<LocationInfo, Integer> results = doSearch(word, determineMaxDistance(word));
            allResults.add(results);
         }
      }

      // special case - handle empty list to prevent an error from occurring in orderResults (plus, this is much faster)
      if (allResults.isEmpty())
         return Collections.emptyList();

      Map<LocationInfo, Integer> result;
      if (allResults.size() == 1)
         result = allResults.get(0);
      else
         result = mergeResults(allResults);

      return orderResults(result, maxResults);
   }

   private List<LocationInfo> orderResults(Map<LocationInfo, Integer> result, int numResults)
   {
      PriorityQueue<Result> resultQueue = new PriorityQueue<Result>(numResults, new Comparator<Result>()
      {
         @Override public int compare(Result o1, Result o2)
         {
            if (o1.mismatches != o2.mismatches)
               return o1.mismatches < o2.mismatches ? -1 : 1;
            return o1.info.fullDisplayPath.compareTo(o2.info.fullDisplayPath);
         }
      });

      for (Map.Entry<LocationInfo, Integer> entry : result.entrySet())
         resultQueue.add(new Result(entry.getKey(), entry.getValue()));

      int num = Math.min(result.size(), numResults);
      List<LocationInfo> resultList = new ArrayList<LocationInfo>(num);
      for (int i = 0; i < num; i++)
         resultList.add(resultQueue.remove().info);

      return resultList;
   }

   private Map<LocationInfo, Integer> mergeResults(List<Map<LocationInfo, Integer>> allResults)
   {
      // find the intersecting keys
      Set<LocationInfo> intersectingKeys = null;
      for (Map<LocationInfo, Integer> result : allResults)
      {
         if (intersectingKeys == null)
            intersectingKeys = new HashSet<LocationInfo>(result.keySet());
         else
            intersectingKeys.retainAll(result.keySet());
      }

      // for all intersecting keys, add all the ranks together
      Map<LocationInfo, Integer> mergedResults = new HashMap<LocationInfo, Integer>();
      for (LocationInfo key : intersectingKeys)
      {
         int mergedRank = 0;
         for (Map<LocationInfo, Integer> result : allResults)
            mergedRank += result.get(key);
         mergedResults.put(key, mergedRank);
      }

      return mergedResults;
   }

   private int determineMaxDistance(String pattern)
   {
      int len = pattern.length();
      if (len <= 2) return 0;
      else if (len <= 5) return 1;
      else if (len <= 9) return 2;
      else return 3;
   }

   private Map<LocationInfo, Integer> doSearch(String searchWord, int maxDistance)
   {
      final char[] patternChars = searchWord.toCharArray();
      final Map<LocationInfo, Integer> results = new HashMap<LocationInfo, Integer>();

      for (SystemAccessSearchCache.CacheEntry entry : cache.getCache())
      {
         int distance = ld.compute(patternChars, entry.word);
         if (distance <= maxDistance)
         {
            for (LocationInfo locationInfo : entry.locations)
               results.put(locationInfo, distance);
         }
      }

      return results;
   }
}