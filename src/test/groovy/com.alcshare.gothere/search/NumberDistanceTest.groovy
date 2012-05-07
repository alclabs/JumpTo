/*=============================================================================
                    AUTOMATED LOGIC CORPORATION
            Copyright (c) 1999 - 2012 All Rights Reserved
     This document contains confidential/proprietary information.
===============================================================================

   @(#)NumberDistanceTest

   Author(s) jmurph
   $Log: $    
=============================================================================*/
package com.alcshare.gothere.search;

import spock.lang.Specification;

public class NumberDistanceTest extends Specification
{
   def "test basic"()
   {
      given:
         final String testStr = "T-12-34Fred"

      expect:
         computeDistance(searchStr, testStr) == dist

      where:
         searchStr | dist
         "12"      | 0
         "1"       | 1
         "2"       | 1
         "3"       | 1
         "4"       | 1
         "34"      | 0
         "5"       | Integer.MAX_VALUE
         "21"      | Integer.MAX_VALUE
         "43"      | Integer.MAX_VALUE
   }

   def "test repeating number sequence"()
   {
      given:
         final String testStr = "313145"

      expect:
         computeDistance(searchStr, testStr) == dist

      where:
         searchStr | dist
         "3"       | 5
         "1"       | 5
         "31"      | 4
         "313"     | 3
         "314"     | 3
         "3131"    | 2
         "3145"    | 2
         "31314"   | 1
         "313145"  | 0
   }

   def "test partial match at end of test string"()
   {
      expect:
         computeDistance("12", "181") == Integer.MAX_VALUE
   }

   def "test with target stores"()
   {
      given:
         final String testStr = "T-2375"

      expect:
         computeDistance(searchStr, testStr) == dist

      where:
         searchStr | dist
         "1"       | Integer.MAX_VALUE
         "2"       | 3
         "3"       | 3
         "23"      | 2
         "235"     | Integer.MAX_VALUE
         "237"     | 1
         "2373"    | Integer.MAX_VALUE
         "2375"    | 0
   }

   int computeDistance(String s, String t) { new NumberDistance().compute(s.toCharArray(), t.toCharArray()) }
}