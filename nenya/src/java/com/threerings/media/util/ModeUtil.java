//
// $Id: ModeUtil.java 868 2010-01-04 21:47:34Z dhoover $
//
// Nenya library - tools for developing networked games
// Copyright (C) 2002-2010 Three Rings Design, Inc., All Rights Reserved
// http://www.threerings.net/code/nenya/
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

package com.threerings.media.util;

import java.util.Comparator;
import java.util.TreeSet;

import java.awt.DisplayMode;
import java.awt.GraphicsDevice;

/**
 * Display mode related utilities.
 */
public class ModeUtil
{
    /**
     * Gets a display mode that matches the specified parameters. The
     * screen resolution must match the specified resolution exactly, the
     * specified desired depth will be used if it is available, and if
     * not, the highest depth greater than or equal to the specified
     * minimum depth is used. The highest refresh rate available for the
     * desired mode is also used.
     */
    public static DisplayMode getDisplayMode (
        GraphicsDevice gd, int width, int height, int desiredDepth, int minimumDepth)
    {
        DisplayMode[] modes = gd.getDisplayModes();
        final int ddepth = desiredDepth;

        // we sort modes in order of desirability
        Comparator<DisplayMode> mcomp = new Comparator<DisplayMode>() {
            public int compare (DisplayMode m1, DisplayMode m2) {
                int bd1 = m1.getBitDepth(), bd2 = m2.getBitDepth();
                int rr1 = m1.getRefreshRate(), rr2 = m2.getRefreshRate();

                // prefer the desired depth
                if (bd1 == ddepth && bd2 != ddepth) {
                    return -1;
                } else if (bd2 == ddepth && bd1 != ddepth) {
                    return 1;
                }

                // otherwise prefer higher depths
                if (bd1 != bd2) {
                    return bd2 - bd1;
                }

                // for same bitrates, prefer higher refresh rates
                return rr2 - rr1;
            }
        };

        // but we only add modes that meet our minimum requirements
        TreeSet<DisplayMode> mset = new TreeSet<DisplayMode>(mcomp);
        for (DisplayMode mode : modes) {
            if (mode.getWidth() == width &&
                mode.getHeight() == height &&
                mode.getBitDepth() >= minimumDepth &&
                mode.getRefreshRate() <= 75) {
                mset.add(mode);
            }
        }

        return (mset.size() > 0) ? mset.first() : null;
    }

    /**
     * Returns a string representation of the supplied display mode.
     */
    public static String toString (DisplayMode mode)
    {
        return "[width=" + mode.getWidth() +
            ", height=" + mode.getHeight() +
            ", depth=" + mode.getBitDepth() +
            ", refresh=" + mode.getRefreshRate() + "]";
    }
}
