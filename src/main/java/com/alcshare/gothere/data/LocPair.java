package com.alcshare.gothere.data;

import com.controlj.green.addonsupport.access.Location;
import com.controlj.green.addonsupport.access.UnresolvableException;
import com.controlj.green.addonsupport.web.Link;
import com.controlj.green.addonsupport.web.LinkException;
import com.controlj.green.addonsupport.web.UITree;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;

/**
 *
 */
public class LocPair implements Serializable, Comparable {
    public String displayPath;
    public String href;

    public LocPair(HttpServletRequest req, Location loc, String overrideName) {
        this(req, loc);
        displayPath = overrideName;
    }

    public LocPair(HttpServletRequest req, Location loc) {
        displayPath = getFullDisplayPath(loc);
        try {
            href = Link.getURL(req, UITree.GEO, loc);
        } catch (LinkException e) { href = ""; }
    }

    private String getFullDisplayPath(Location loc) {
        if (!loc.hasParent()) {
            return "";
        } else {
            try {
                return getFullDisplayPath(loc.getParent()) + " / " + loc.getDisplayName();
            } catch (UnresolvableException e) {
                throw new RuntimeException("can't resolve parent even though hasParent is true", e);
            }
        }
    }

    @Override
    public int compareTo(Object o) {
        return displayPath.compareTo(((LocPair)o).displayPath);
    }
}
