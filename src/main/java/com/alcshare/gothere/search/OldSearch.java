/*=============================================================================
                    AUTOMATED LOGIC CORPORATION
            Copyright (c) 1999 - 2011 All Rights Reserved
     This document contains confidential/proprietary information.
===============================================================================

   @(#)OldSearch

   Author(s) jmurph
   $Log: $    
=============================================================================*/
package com.alcshare.gothere.search;

import com.alcshare.gothere.data.LocPair;
import com.controlj.green.addonsupport.access.*;
import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
//import java.util.Date;
import java.util.List;

// the original search routine written by Steve
public class OldSearch
{
   public List<LocPair> search(List<LocPair> data, String value)
   {
      List<LocPair> result = new ArrayList<LocPair>();
      String upperValue = value.toUpperCase();
      String[] searchStrings = upperValue.split("\\s");
      if (value == null || value.length() == 0)
      {
         result.add(data.get(0));
      }
      else
      {
         for (LocPair pair : data)
         {
            boolean missing = false;
            for (String searchString : searchStrings)
            {
               if (!pair.searchPath.contains(searchString))
               {
                  missing = true;
                  break;
               }
            }
            if (!missing)
            {
               result.add(pair);
            }
         }
      }
      return result;
   }

   public void writeResults(List<LocPair> data, PrintWriter writer, boolean all)
   {
      int count = 0;
      for (LocPair pair : data)
      {
         writer.println("<div><a target=\"webctrl\" href=\"" + pair.href + "\">" + pair.displayPath + "</a></div>");
         if (!all && (count++ >= 1000))
         {
            writer.println("<div onclick=\"runSearch(false,true)\" style=\"padding-left:100px; font-weight:bold;text-decoration:underline;cursor:pointer;\">More ...</div>");
            break;
         }
      }
   }

   public List<LocPair> getLookupData(final HttpServletRequest req, String KEY_DATA)
   {
      HttpSession session = req.getSession();
      Object data = session.getAttribute(KEY_DATA);
      if (data == null)
      {
         try
         {
            final List<LocPair> locList = new ArrayList<LocPair>();
            data = locList;
            session.setAttribute(KEY_DATA, data);
            SystemConnection connection = DirectAccess.getDirectAccess().getUserSystemConnection(req);
            //Date start = new Date();

            connection.runReadAction(new ReadAction()
            {
               @Override
               public void execute(@NotNull SystemAccess access) throws Exception
               {
                  Location root = access.getGeoRoot();
                  access.visit(root, new ListBuilderVisitor(locList, req));
                  Collections.sort(locList);
               }
            });
            //Date end = new Date();
            //System.out.println("JumpTo Add-On created search list of " + locList.size() + " items in " +
            //      (end.getTime() - start.getTime()) + " mSec");

         }
         catch (Exception e)
         {
            System.err.println("Error in JumpTo Add-On:");
            e.printStackTrace();
         }

      }
      return (List<LocPair>) data;
   }

   public class ListBuilderVisitor extends Visitor
   {
      private List<LocPair> locList;
      HttpServletRequest req;

      public ListBuilderVisitor(List<LocPair> locList, HttpServletRequest req)
      {
         super(false);
         this.locList = locList;
         this.req = req;
      }

      @Override
      public void visitSystem(@NotNull Location system)
      {
         locList.add(new LocPair(req, system, " / "));
      }

      @Override
      public void visitArea(@NotNull Location area)
      {
         locList.add(new LocPair(req, area));
      }

      @Override
      public void visitEquipment(@NotNull Location eq)
      {
         locList.add(new LocPair(req, eq));
      }
   }
}

