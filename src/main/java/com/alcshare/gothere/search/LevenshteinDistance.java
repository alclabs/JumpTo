/*=============================================================================
                    AUTOMATED LOGIC CORPORATION
            Copyright (c) 1999 - 2011 All Rights Reserved
     This document contains confidential/proprietary information.
===============================================================================

   @(#)LevenshteinDistance

   Author(s) jmurph
   $Log: $    
=============================================================================*/
package com.alcshare.gothere.search;

/**<!=========================================================================>
   <i>Adapted from material put into the public domain by
   <a href="http://www.merriampark.com/ld.htm">Michael Gilleland</a>.</i>
   This class computes the Levenshtein distance between two words.  The Levenshtein
   distance is also sometimes called the edit distance, because it is the number
   of additions, subtractions, or changes that is need to turn one of the words
   into the other word.
   <p/>
   This class has two public functions, {@link #compute} and {@link #computeNormalized}.
   These functions return the raw edit distance, or a normalized edit distance
   based on the length of the words.  The normalized distance is likely to be
   more useful in most use cases.
   <p/>
   <em>Implementation Note: in order to reduce memory "thrashing", this class
   reuses work buffers.  Because of this reuse, the work is synchronized to
   prevent wrong results.  Therefore, if multiple threads need to compute
   Levenshtein distances, each thread should allocate an instance of this class.
   It is also recommended that when doing lots of distance calculations, you
   reuse a single class instance for better memory behavior.</em>
   @author jmurph
<!==========================================================================>*/
public class LevenshteinDistance
{
   private final int   maxCompareLength;
   private final int[] workArray1;
   private final int[] workArray2;
   private final boolean ignoreCase;

   public LevenshteinDistance(int maxCompareLength, boolean ignoreCase)
   {
      this.maxCompareLength = maxCompareLength;
      workArray1 = new int[maxCompareLength + 1];
      workArray2 = new int[maxCompareLength + 1];
      this.ignoreCase = ignoreCase;
   }

   /**<!====== compute =======================================================>
      Returns the raw edit distance.  Note that if s.length is less than t.length,
      the words are not compared path s.length.  In other words, any characters
      in t past s.length are ignored.  So, if s == "free" and t == "freedom", this
      method will return an edit distance of 0.
      <!      Name       Description>
      @param  search     the "search string"
      @param  target     the "target string"
      @return the edit distance between the given words.
      @author jmurph
   <!=======================================================================>*/
   public int compute(final char[] search, final char[] target)
   {
      final int sL = Math.min(search.length, maxCompareLength);
      final int tL = Math.min(target.length, maxCompareLength);
      return computeInternal(search, sL, target, tL);
   }

   /**<!====== computeNormalized =============================================>
      Returns the normalized edit distance between the given words.  The distance
      is normalized using the following equation:
      <pre>
          (shortestLen - distance ) / (double)longestLen
      </pre>
      where shortestLen is the length of the shortest given word and longestLen
      is the length of the longest given word.  The result of this computation
      is between 0.0 and 1.0, where 0.0 means the words are completely unrelated,
      and 1.0 means that the words are an exact match.
      <!      Name       Description>
      @param  word1      a word to check
      @param  word2      a word to check
      @return the normalized edit distance between the given words.
      @author jmurph
   <!=======================================================================>*/
   public double computeNormalized(char[] word1, char[] word2)
   {
      final int w1L = Math.min(word1.length, maxCompareLength);
      final int w2L = Math.min(word2.length, maxCompareLength);
      if (w1L <= w2L)
      {
         int distance = computeInternal(word1, w1L, word2, w2L);
         return (w1L - distance) / (double)w2L;
      }
      else
      {
         int distance = computeInternal(word2, w2L, word1, w1L);
         return (w2L - distance) / (double)w1L;
      }
   }

   private synchronized int computeInternal(final char[] s, final int sL, final char[] t, final int tL)
   {
      if (sL == 0) return tL;
      if (tL == 0) return sL;

      int[] prvArray = workArray1;
      int[] curArray = workArray2;
      int[] swpArray; // just a holder for use when swapping arrays

      for (int i = 0; i <= sL; i++)
         prvArray[i] = i;

      for (int j = 1; j <= tL; j++)
      {
         char tJ = t[j-1];
         curArray[0] = j;

         for (int i = 0; i < sL; i++)
         {
            int cost = eq(s[i], tJ) ? 0 : 1;
            curArray[i+1] = minimum(curArray[i] + 1, prvArray[i + 1] + 1, prvArray[i] + cost);
         }

         // This if handles s being a prefix of t.  Extra "t" characters are ignored after "s" ends.
         if (j > sL)
            return Math.min(curArray[sL], prvArray[sL]);

         // copy current distance counts to 'previous row' distance counts
         swpArray = prvArray;
         prvArray = curArray;
         curArray = swpArray;
      }

      // our last action in the above loop was to switch cur and prv arrays,
      // so now prvArray actually has the most recent cost counts
      return prvArray[sL];
   }

   private static int minimum(int a, int b, int c) { return Math.min(a, Math.min(b, c)); }

   private boolean eq(char a, char b)
   {
      // if not ignoring case, just check a == b.  If ignoring case, then we also need to check
      // if the toUpperCase || toLowerCase match.  See java.lang.String.java#equalsIgnoreCase for
      // the reason (basically, some alphabets have weird case conversion rules).
      return a == b || ignoreCase && (Character.toUpperCase(a) == Character.toUpperCase(b) ||
                                      Character.toLowerCase(a) == Character.toLowerCase(b));
   }
}