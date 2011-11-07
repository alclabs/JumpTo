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

import junit.framework.TestCase;

public class LevenshteinDistanceTest extends TestCase
{
   // the maxCompareLength for any test that doesn't explicitly provide it
   private static final int DEF_DIST = 30;

   // the ignoreCase value for any test that doesn't explicitly provide it
   private static final boolean DEF_CASE = true;

   public void testInsert()
   {
      final String dataWord = "test";
      final String searchWord = "tet";

      assertEquals(1, computeDistance(dataWord, searchWord));
   }

   public void testEdit()
   {
      final String dataWord = "test";
      final String searchWord = "text";

      assertEquals(1, computeDistance(dataWord, searchWord));
   }

   public void testDelete()
   {
      final String dataWord = "test";
      final String searchWord = "tesst";

      assertEquals(1, computeDistance(dataWord, searchWord));
   }

   public void testCaseSensitivity()
   {
      final String s1 = "teSt";
      final String s2 = "Test";

      assertEquals(2, computeDistance(s1, s2, false));
      assertEquals(0, computeDistance(s1, s2, true));
   }

   public void testOrderIndependant()
   {
      final String s1 = "damerau";
      final String s2 = "dammerau";

      assertEquals(1, computeDistance(s1, s2));
      assertEquals(1, computeDistance(s2, s1));
   }

   public void testBanana()
   {
      final String dataWord = "banana";
      assertEquals(1, computeDistance("baa", dataWord));
      assertEquals(0.33d, computeNormalized("baa", dataWord), 0.005d);

      assertEquals(0, computeDistance("bana", dataWord));
      assertEquals(0.67d, computeNormalized("bana", dataWord), 0.005d);

      assertEquals(1, computeDistance("nana", dataWord));
      assertEquals(0.5d, computeNormalized("nana", dataWord), 0.005d);

      assertEquals(0, computeDistance(dataWord, dataWord));
      assertEquals(1.0d, computeNormalized(dataWord, dataWord), 0.005d);
   }

   public void testFindsSuffixes()
   {
      final String searchWord = "abcd";
      final String dataWord = "abcdefgh";

      assertEquals(0, computeDistance(searchWord, dataWord));
      assertEquals(0.5d, computeNormalized(searchWord, dataWord), 0.005d);
      assertEquals(4, computeDistance(dataWord, searchWord));
      assertEquals(0.5d, computeNormalized(dataWord, searchWord), 0.005d);
   }

   /**
    * The maxCompareLength is used for optimization.  Test that comparing longer strings results
    * in the extra characters being ignored, and not some error (like IndexOutOfBounds).
    */
   public void testMaxCompareLength()
   {
      final String s1 = "testingwithalongstring";
      final String s2 = "thisisanotherlongstring";

      assertEquals(9, computeDistance(s1, s2, 30));
      assertEquals(3, computeDistance(s1, s2, 5));
   }

   private int computeDistance(String s, String t) { return computeDistance(s, t, DEF_DIST, DEF_CASE); }
   private int computeDistance(String s, String t, boolean ignoreCase) { return computeDistance(s, t, DEF_DIST, ignoreCase); }
   private int computeDistance(String s, String t, int maxDistance) { return computeDistance(s, t, maxDistance, DEF_CASE); }
   private int computeDistance(String s, String t, int maxDistance, boolean ignoreCase)
   {
      return new LevenshteinDistance(maxDistance, ignoreCase).compute(s.toCharArray(), t.toCharArray());
   }

   private double computeNormalized(String s, String t) { return computeNormalized(s, t, DEF_DIST, DEF_CASE); }
   private double computeNormalized(String s, String t, boolean ignoreCase) { return computeNormalized(s, t, DEF_DIST, ignoreCase); }
   private double computeNormalized(String s, String t, int maxDistance) { return computeNormalized(s, t, maxDistance, DEF_CASE); }
   private double computeNormalized(String s, String t, int maxDistance, boolean ignoreCase)
   {
      return new LevenshteinDistance(maxDistance, ignoreCase).computeNormalized(s.toCharArray(), t.toCharArray());
   }
}

