/*=============================================================================
                    AUTOMATED LOGIC CORPORATION
            Copyright (c) 1999 - 2011 All Rights Reserved
     This document contains confidential/proprietary information.
===============================================================================

   @(#)LevenshteinDistanceTest

   Author(s) jmurph
   $Log: $    
=============================================================================*/
package com.alcshare.gothere.search;

import spock.lang.Specification;
import static spock.util.matcher.HamcrestMatchers.closeTo

public class LevenshteinDistanceTest extends Specification
{
   def "test basic differences"()
   {
      expect:
         computeDistance(dataWord, searchWord) == dist

      where:
         dataWord | searchWord | dist
         "test"   | "tet"      | 1     // insert
         "test"   | "text"     | 1     // edit
         "test"   | "tesst"    | 1     // delete
   }

   def "test case sensitivity"()
   {
      expect:
         computeDistance("teSt", "Test", 10, false) == 2
         computeDistance("teSt", "Test", 10, true) == 0
   }

   def "test order independence"()
   {
      expect:
         computeDistance("damerau", "dammerau") == 1
         computeDistance("dammerau", "damerau") == 1
   }

   def "test banana"()
   {
      given:
         final String dataWord = "banana"

      expect:
         computeDistance(search, dataWord) == dist
         (computeNormalized(search, dataWord) as Number) closeTo(normDist, 0.005)

      where:
         search  | dist | normDist
         "baa"   | 1    | 0.33
         "bana"  | 0    | 0.67
         "nana"  | 1    | 0.5
         "banana"| 0    | 1.0
   }

   def "test finds suffixes"()
   {
      expect:
         computeDistance("abcd", "abcdefgh") == 0
         (computeNormalized("abcd", "abcdefgh") as Number) closeTo(0.5, 0.005)

      and:
         computeDistance("abcdefgh", "abcd") == 4
         (computeNormalized("abcdefgh", "abcd") as Number) closeTo(0.5, 0.005)
   }

   /**
    * The maxCompareLength is used for optimization.  Test that comparing longer strings results
    * in the extra characters being ignored, and not some error (like IndexOutOfBounds).
    */
   def "test max compare length"()
   {
      expect:
         computeDistance("testingwithalongstring", "thisisanotherlongstring", 30) == 9
         computeDistance("testingwithalongstring", "thisisanotherlongstring", 5) == 3
   }

   int computeDistance(String s, String t, int maxDistance = 30, boolean ignoreCase = true)
   {
      new LevenshteinDistance(maxDistance, ignoreCase).compute(s.toCharArray(), t.toCharArray())
   }

   double computeNormalized(String s, String t, int maxDistance = 30, boolean ignoreCase = true)
   {
      new LevenshteinDistance(maxDistance, ignoreCase).computeNormalized(s.toCharArray(), t.toCharArray())
   }
}