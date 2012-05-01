/*
 * Copyright (c) 2011 Automated Logic Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.alcshare.gothere.servlets;

import com.alcshare.gothere.data.LocPair;
import com.alcshare.gothere.data.LocationInfo;
import com.alcshare.gothere.search.LDSearch;
import com.alcshare.gothere.search.OldSearch;
import com.alcshare.gothere.search.SystemAccessSearchCache;
import com.controlj.green.addonsupport.access.DirectAccess;
import com.controlj.green.addonsupport.access.ReadAction;
import com.controlj.green.addonsupport.access.SystemAccess;
import com.controlj.green.addonsupport.access.SystemConnection;
import com.controlj.green.addonsupport.web.Link;
import com.controlj.green.addonsupport.web.LinkException;
import com.controlj.green.addonsupport.web.UITree;
import org.jetbrains.annotations.NotNull;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
//import java.util.Date;
import java.util.List;

public class SearchServlet extends HttpServlet
{
   private static final String KEY_DATA = "LU_DATA";
   private static final String PARAM_VALUE = "value";
   private static final String PARAM_REINIT = "reinit";
   private static final String PARAM_ALL = "all";

   @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
   {
      resp.setContentType("text/html");

      final String value = req.getParameter(PARAM_VALUE);
      final boolean all = Boolean.valueOf(req.getParameter(PARAM_ALL));
      if (req.getParameter(PARAM_REINIT) != null)
         req.getSession().removeAttribute(KEY_DATA);

      final PrintWriter writer = resp.getWriter();

      if (false)
      {
         OldSearch search = new OldSearch();
         List<LocPair> locData = search.getLookupData(req, KEY_DATA);
         //Date start = new Date();
         List<LocPair> result = search.search(locData, value);
         //Date end = new Date();
         //System.out.println("Searching for '" + value + "' took " + (end.getTime() - start.getTime()) + " mSec.");
         search.writeResults(result, writer, all);
      }
      else
      {
         LDSearch searcher = getSearch(req);
         //Date start = new Date();
         List<LocationInfo> result = searcher.search(value, 250);
         //Date end = new Date();
         //System.out.println("Searching for '" + value + "' took " + (end.getTime() - start.getTime()) + " mSec.");
         writeResults(result, writer, req, all);
      }
   }

   private void writeResults(final List<LocationInfo> locations, final PrintWriter writer, final HttpServletRequest req, final boolean all)
   {
      try
      {
         SystemConnection connection = DirectAccess.getDirectAccess().getUserSystemConnection(req);
         connection.runReadAction(new ReadAction()
         {
            @Override public void execute(@NotNull SystemAccess access) throws Exception
            {
               int count = 0;
               for (LocationInfo location : locations)
               {
                  String href;
                  try {
                      href = Link.getURL(req, UITree.GEO, location.location);
                  } catch (LinkException e) { href = ""; }

                  writer.println("<div><a target=\"webctrl\" href=\"" + href + "\">" + location.fullDisplayPath + "</a></div>");
                  if (!all && ++count > 1000)
                  {
                     writer.println("<div onclick=\"runSearch(false,true)\" style=\"padding-left:100px; font-weight:bold;text-decoration:underline;cursor:pointer;\">More ...</div>");
                     break;
                  }
               }
            }
         });
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }

   private LDSearch getSearch(final HttpServletRequest req)
   {
      HttpSession session = req.getSession();
      Object data = session.getAttribute(KEY_DATA);
      if (data == null)
      {
         try
         {
            final SystemAccessSearchCache cache = new SystemAccessSearchCache();
            data = new LDSearch(cache);
            session.setAttribute(KEY_DATA, data);

            SystemConnection connection = DirectAccess.getDirectAccess().getUserSystemConnection(req);
            //Date start = new Date();
            connection.runReadAction(new ReadAction()
            {
               @Override
               public void execute(@NotNull SystemAccess access) throws Exception
               {
                  cache.buildCache(access);
               }
            });
            //Date end = new Date();
            //System.out.println("JumpTo Add-On created search cache of " + cache.getCache().size() + " items in " +
            //      (end.getTime() - start.getTime()) + " mSec");
         }
         catch (Exception e)
         {
            System.err.println("Error in JumpTo Add-On:");
            e.printStackTrace();
         }
      }

      return (LDSearch) data;
   }
}