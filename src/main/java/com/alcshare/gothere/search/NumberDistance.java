/*=============================================================================
                    AUTOMATED LOGIC CORPORATION
            Copyright (c) 1999 - 2012 All Rights Reserved
     This document contains confidential/proprietary information.
===============================================================================

   @(#)NumberDistance

   Author(s) jmurph
   $Log: $    
=============================================================================*/
package com.alcshare.gothere.search;

public class NumberDistance
{
   public int compute(final char[] search, final char[] target)
   {
      int distance = Integer.MAX_VALUE;

      int idx = 0;
      while (true)
      {
         int startIdx = scanForDigit(target, idx, true);
         if (startIdx == target.length)
            break;

         int endIdx = scanForDigit(target, startIdx, false);
         int dist = findNumberDistance(search, target, startIdx, endIdx);
         distance = Math.min(distance, dist);

         idx = endIdx;
      }

      return distance;
   }

   private int scanForDigit(char[] target, int startIdx, boolean forDigit)
   {
      for (int idx = startIdx; idx < target.length; ++idx)
         if (Character.isDigit(target[idx]) == forDigit)
            return idx;
      return target.length;
   }

   private int findNumberDistance(char[] search, char[] target, int startIdx, int endIdx)
   {
      int minDistance = Integer.MAX_VALUE;
      int idx = startIdx;

      while (idx < endIdx)
      {
         int distance = idx - startIdx;
         while (idx < endIdx)
         {
            if (search[0] == target[idx])
               break;
            ++distance;
            ++idx;
         }

         // if we never matched the first digit, then bail
         if (idx == endIdx)
            break;

         int searchIdx = 1;
         while (searchIdx < search.length && idx < endIdx-1 && search[searchIdx] == target[++idx])
            ++searchIdx;

         if (searchIdx < search.length)
         {
            if (idx == endIdx-1)
               break; // reached end of target string, we're done
            else
               continue; // there was a mismatch at some point, so keep looking
         }
         else
            minDistance = Math.min(minDistance, distance + endIdx - idx - 1);

         ++idx;
      }
      return minDistance;
   }
}

