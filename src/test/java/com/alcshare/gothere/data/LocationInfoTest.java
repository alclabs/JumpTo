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

import com.controlj.green.addonsupport.access.Location;
import junit.framework.TestCase;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;

import java.util.Arrays;

public class LocationInfoTest extends TestCase
{
   final Mockery context = new JUnit4Mockery();

   public void testRootLocation() throws Exception
   {
      Location locationMock = createLocationMock("Root Location", null);

      LocationInfo info = new LocationInfo(locationMock);
      assertEquals(locationMock, info.location);
      assertEquals("Root Location", info.fullDisplayPath);
      assertEquals(Arrays.asList("root", "location"), Arrays.asList(info.displayWords));
      assertEquals(0, info.depth);
   }

   public void testSecondLevelLocation() throws Exception
   {
      Location rootMock = createLocationMock("Root Location", null);
      Location basementMock = createLocationMock("Basement", rootMock);

      LocationInfo info = new LocationInfo(basementMock);
      assertEquals(basementMock, info.location);
      assertEquals("Root Location / Basement", info.fullDisplayPath);
      assertEquals(Arrays.asList("basement", "root", "location"), Arrays.asList(info.displayWords));
      assertEquals(1, info.depth);
   }

   private Location createLocationMock(final String displayName, final Location parent) throws Exception
   {
      final Location mock = context.mock(Location.class, displayName);
      context.checking(new Expectations() {{
         allowing(mock).getDisplayName();
         will(returnValue(displayName));
         allowing(mock).hasParent();
         will(returnValue(parent != null));
         allowing(mock).getParent();
         will(returnValue(parent));
      }});
      return mock;
   }
}

