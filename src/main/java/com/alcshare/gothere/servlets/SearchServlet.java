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
import com.controlj.green.addonsupport.AddOnInfo;
import com.controlj.green.addonsupport.access.*;
import org.jetbrains.annotations.NotNull;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SearchServlet extends HttpServlet {
    private static final String KEY_DATA = "LU_DATA";
    private static final String PARAM_VALUE = "value";
    private static final String PARAM_REINIT = "reinit";
    private static final String PARAM_ALL = "all";

    @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html");

        final String riString = req.getParameter(PARAM_REINIT);
        final String allString = req.getParameter(PARAM_ALL);
        boolean all = false;
        if (allString != null && allString.equalsIgnoreCase("true")) {
            all = true;
        }
        if (riString != null) {
            req.getSession().removeAttribute(KEY_DATA);
        }
        final String value = req.getParameter(PARAM_VALUE);

        final PrintWriter writer = resp.getWriter();

        List<LocPair> locData = getLookupData(req);
        List<LocPair> result = search(locData, value);
        writeResults(result, writer, all);
    }

    private void writeResults(List<LocPair> data, PrintWriter writer, boolean all) {
        int count =0;
        for (LocPair pair : data) {
            writer.println("<div><a target=\"webctrl\" href=\""+pair.href+"\">"+pair.displayPath+"</a></div>");
            if (!all && (count++ >= 1000)) {
                writer.println("<div onclick=\"runSearch(false,true)\" style=\"padding-left:100px; font-weight:bold;text-decoration:underline;cursor:pointer;\">More ...</div>");
                break;
            }
        }
    }

    
    private List<LocPair> search(List<LocPair>data, String value) {
        List<LocPair> result = new ArrayList<LocPair>();
        String upperValue = value.toUpperCase();
        String[] searchStrings = upperValue.split("\\s");
        //Date start = new Date();
        if (value == null || value.length() == 0) {
            result.add(data.get(0));
        } else {
            for (LocPair pair : data) {
                boolean missing = false;
                for (String searchString : searchStrings) {
                    if (!pair.searchPath.contains(searchString)) {
                        missing = true;
                        break;
                    }
                }
                if (!missing) {
                    result.add(pair);
                }
            }
        }
        //Date end = new Date();
        //System.out.println("Searching for '"+value+"' took "+(end.getTime() - start.getTime())+" mSec.");
        return result;
    }

    private List<LocPair> getLookupData(final HttpServletRequest req) {
        HttpSession session = req.getSession();
        Object data = session.getAttribute(KEY_DATA);
        if (data == null)  {
            try {
                final List<LocPair> locList = new ArrayList<LocPair>();
                data = locList;
                session.setAttribute(KEY_DATA, data);
                SystemConnection connection = AddOnInfo.getAddOnInfo().getUserSystemConnection(req);
                //Date start = new Date();

                connection.runReadAction(new ReadAction() {
                    @Override
                    public void execute(@NotNull SystemAccess access) throws Exception {
                        Location root = access.getGeoRoot();
                        access.visit(root, new ListBuilderVisitor(locList, req));
                        Collections.sort(locList);
                    }
                });
                //Date end = new Date();
                // System.out.println("JumpTo Add-On created search list of "+locList.size()+" items in "+(end.getTime() - start.getTime())+ " mSec");

            } catch (Exception e) {
                System.err.println("Error in JumpTo Add-On:");
                e.printStackTrace();
            }

        }
        return (List<LocPair>) data;
    }

    public class ListBuilderVisitor extends Visitor {
        private List<LocPair> locList;
        HttpServletRequest req;

        public ListBuilderVisitor(List<LocPair> locList, HttpServletRequest req) {
            super(false);
            this.locList = locList;
            this.req = req;
        }

        @Override
        public void visitSystem(@NotNull Location system) {
            locList.add(new LocPair(req, system, " / "));
        }

        @Override
        public void visitArea(@NotNull Location area) {
            locList.add(new LocPair(req, area));
        }

        @Override
        public void visitEquipment(@NotNull Location eq) {
            locList.add(new LocPair(req, eq));
        }
    }
    
}
