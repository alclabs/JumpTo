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

import com.alcshare.gothere.data.LocationInfo;
import com.alcshare.gothere.search.LDSearch;
import com.alcshare.gothere.search.SystemAccessSearchCache;
import com.controlj.green.addonsupport.AddOnInfo;
import com.controlj.green.addonsupport.FileLogger;
import com.controlj.green.addonsupport.InvalidConnectionRequestException;
import com.controlj.green.addonsupport.access.*;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONWriter;

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

   private static final FileLogger LOGGER = AddOnInfo.getAddOnInfo().getDateStampLogger();

   @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
   {
      resp.setContentType("application/json");
      final String value = req.getParameter(PARAM_VALUE);
      if (req.getParameter(PARAM_REINIT) != null)
         req.getSession().removeAttribute(KEY_DATA);

      final PrintWriter writer = resp.getWriter();

      try
      {
         LDSearch searcher = getSearch(req);
         //Date start = new Date();
         List<LocationInfo> result = searcher.search(value, 250);
         //Date end = new Date();
         //System.out.println("Searching for '" + value + "' took " + (end.getTime() - start.getTime()) + " mSec.");
         writeResults(result, writer);
      }
      catch (Exception e)
      {
         LOGGER.println(e);
      }
   }

   private void writeResults(final List<LocationInfo> locations, final PrintWriter writer)
   {
      try
      {
         JSONWriter jsonWriter = new JSONWriter(writer);
         jsonWriter.array();
         int count = 0;
         for (LocationInfo location : locations)
         {
            jsonWriter.object();
            jsonWriter.key("disp").value(location.fullDisplayPath);
            jsonWriter.key("gql").value(location.gqlPath);
            jsonWriter.endObject();
            if (++count > 1000)
               break;
         }
         jsonWriter.endArray();
      } catch (JSONException e) {
         LOGGER.println(e);
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
         } catch (ActionExecutionException | InvalidConnectionRequestException | SystemException e) {
            LOGGER.println(e);
         }
      }

      return (LDSearch) data;
   }
}