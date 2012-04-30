/*=============================================================================
                    AUTOMATED LOGIC CORPORATION
            Copyright (c) 1999 - 2011 All Rights Reserved
     This document contains confidential/proprietary information.
===============================================================================

   @(#)LocationInfoTest

   Author(s) jmurph
   $Log: $
=============================================================================*/
package com.alcshare.gothere.data;

import com.controlj.green.addonsupport.access.Location
import spock.lang.Specification;

public class LocationInfoTest extends Specification
{
   def mockLocation(String dispName, Location parent = null)
   {
      Location location = Mock()
      location.getDisplayName() >> dispName
      location.hasParent() >> { parent != null }
      location.getParent() >> parent
      return location
   }

   def "test root location"()
   {
      given:
         Location location = mockLocation("Root Location")
         def info = new LocationInfo(location);

      expect:
         info.location.is location
         info.fullDisplayPath == "Root Location"
         info.displayWords == ["root", "location"]
         info.depth == 0
   }

   def "test second level location"()
   {
      given:
         Location root = mockLocation("Root Location")
         Location basement = mockLocation("Basement", root)
         def info = new LocationInfo(basement);

      expect:
         info.location.is basement
         info.fullDisplayPath == "Root Location / Basement"
         info.displayWords == ["basement", "root", "location"]
         info.depth == 1
   }
}