/*=============================================================================
                    AUTOMATED LOGIC CORPORATION
            Copyright (c) 1999 - 2013 All Rights Reserved
     This document contains confidential/proprietary information.
===============================================================================

   @(#)Menu

   Author(s) jmurph
   $Log: $    
=============================================================================*/
package com.alcshare.gothere.ui;

import com.controlj.green.addonsupport.AddOnInfo;
import com.controlj.green.addonsupport.access.Operator;
import com.controlj.green.addonsupport.web.menus.*;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class FindMenu implements SystemMenuProvider {
    private static final String ENTRY_KEY = FindMenu.class.getPackage().getName()+".Find";
    private final String menuAction;

    public FindMenu() {
        menuAction = loadMenuActionScript();
    }

    @Override public void updateMenu(@NotNull Operator operator, @NotNull com.controlj.green.addonsupport.web.menus.Menu menu) {
        String actionJS = menuAction.replaceAll("\\$\\{addon-name\\}", AddOnInfo.getAddOnInfo().getName());
        MenuEntryFactory entryFactory = MenuEntryFactory.newEntry(ENTRY_KEY)
                                                        .display("Find")
                                                        .accelerator(Accelerators.ctrl('f'))
                                                        .action(new MenuAction(actionJS));
        menu.addMenuEntry(entryFactory.create());
    }

    private String loadMenuActionScript() {
        try {
            InputStream inputStream = getClass().getResourceAsStream("menuaction.js");
            return loadMenuActionScript(inputStream);
        } catch (IOException e) {
            return "alert('error loading menu action');";
        }
    }

    private String loadMenuActionScript(InputStream is) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null)
                sb.append(line).append('\n');
            return sb.toString();
        }
    }

    private static class MenuAction implements Action {
        private final String menuAction;
        public MenuAction(String menuAction) { this.menuAction = menuAction; }
        @NotNull @Override public String getJavaScript() { return menuAction; }
    }
}