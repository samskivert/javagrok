//
// $Id: FormatterUtil.java 2750 2010-02-19 18:56:42Z samskivert $
//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2010 Michael Bayne, et al.
// 
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package com.samskivert.util;

import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Helper bits for {@link OneLineLogFormatter} and {@link TerseLogFormatter}.
 */
public class FormatterUtil
{
    /** The line separator to use between log messages. */
    public static String LINE_SEPARATOR = "\n";
    static {
        try {
            LINE_SEPARATOR = System.getProperty("line.separator");
        } catch (Exception e) {
        }
    }

    /**
     * Configures the default logging handler to use an instance of the specified formatter when
     * formatting messages.
     */
    public static void configureDefaultHandler (Formatter formatter)
    {
        Logger logger = LogManager.getLogManager().getLogger("");
        for (Handler handler : logger.getHandlers()) {
            handler.setFormatter(formatter);
        }
    }
}
