/*=============================================================================
                    AUTOMATED LOGIC CORPORATION
            Copyright (c) 1999 - 2011 All Rights Reserved
     This document contains confidential/proprietary information.
===============================================================================

   @(#)LocationInfo

   Author(s) jmurph
   $Log: $    
=============================================================================*/
package com.alcshare.gothere.data;

import com.controlj.green.addonsupport.access.Location;
import com.controlj.green.addonsupport.access.UnresolvableException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**<!=========================================================================>
   Holds information about the location.  This way the information can be used
   without having to create new ReadActions all the time (which is inefficient
   as well as ugly).
   @author jmurph
<!==========================================================================>*/
public class LocationInfo
{
   public final Location location;
   public final String fullDisplayPath;
   public final String[] displayWords;
   public final int depth;

   /**<!====== LocationInfo ==================================================>
      Extracts all information from the location.  This method should be called
      from within a ReadAction.
      @author jmurph
   <!=======================================================================>*/
   public LocationInfo(Location location)
   {
      this.location = location;

      Location tmpLocation = location;
      int tmpDepth = 0;
      String tmpDisplayPath = tmpLocation.getDisplayName();
      List<String> tmpDisplayWords = new ArrayList<String>(getSearchWords(tmpDisplayPath));
      while (tmpLocation.hasParent())
      {
         try { tmpLocation = tmpLocation.getParent(); } catch (UnresolvableException e) { break; }

         ++tmpDepth;
         tmpDisplayPath = tmpLocation.getDisplayName() + (tmpDisplayPath.length() == 0 ? "" : " / " + tmpDisplayPath);
         tmpDisplayWords.addAll(getSearchWords(tmpLocation.getDisplayName()));
      }
      tmpDisplayWords.remove(""); // get rid of empty strings from the words list

      fullDisplayPath = tmpDisplayPath;
      displayWords = tmpDisplayWords.toArray(new String[tmpDisplayWords.size()]);
      depth = tmpDepth;
   }

   /**<!====== LocationInfo ==================================================>
      for unit tests
      @author jmurph
   <!=======================================================================>*/
   public LocationInfo(Location location, String fullDisplayPath, String[] displayWords, int depth)
   {
      this.location = location;
      this.fullDisplayPath = fullDisplayPath;
      this.displayWords = displayWords;
      this.depth = depth;
   }

   private static List<String> getSearchWords(String displayName)
   {
      return Arrays.asList(displayName.toLowerCase().split("\\s"));
   }

   @Override public String toString()
   {
      return fullDisplayPath;
   }
}