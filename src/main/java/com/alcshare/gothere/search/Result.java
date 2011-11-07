/*=============================================================================
                    AUTOMATED LOGIC CORPORATION
            Copyright (c) 1999 - 2011 All Rights Reserved
     This document contains confidential/proprietary information.
===============================================================================

   @(#)Result

   Author(s) jmurph
   $Log: $    
=============================================================================*/
package com.alcshare.gothere.search;

import com.alcshare.gothere.data.LocationInfo;

public class Result
{
   public LocationInfo info;
   public int mismatches;

   public Result(LocationInfo info, int mismatches)
   {
      this.info = info;
      this.mismatches = mismatches;
   }
}

