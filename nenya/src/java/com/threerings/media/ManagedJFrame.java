//
// $Id: ManagedJFrame.java 868 2010-01-04 21:47:34Z dhoover $
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

package com.threerings.media;

import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Window;

import javax.swing.JFrame;

/**
 * When using the {@link FrameManager}, one must use this top-level frame
 * class.
 */
public class ManagedJFrame extends JFrame
    implements FrameManager.ManagedRoot
{
    /**
     * Constructs a managed frame with no title.
     */
    public ManagedJFrame ()
    {
        this("");
    }

    /**
     * Constructs a managed frame with the specified title.
     */
    public ManagedJFrame (String title)
    {
        super(title);
    }

    /**
     * Constructs a managed frame in the specified graphics configuration.
     */
    public ManagedJFrame (GraphicsConfiguration gc)
    {
        super(gc);
    }

    // from interface FrameManager.ManagedRoot
    public void init (FrameManager fmgr)
    {
        _fmgr = fmgr;
    }

    // from interface FrameManager.ManagedRoot
    public Window getWindow ()
    {
        return this;
    }

    /**
     * Returns the frame manager managing this frame.
     */
    public FrameManager getFrameManager ()
    {
        return _fmgr;
    }

    /**
     * We catch paint requests and forward them on to the repaint
     * infrastructure.
     */
    @Override
    public void paint (Graphics g)
    {
        update(g);
    }

    /**
     * We catch update requests and forward them on to the repaint
     * infrastructure.
     */
    @Override
    public void update (Graphics g)
    {
        Shape clip = g.getClip();
        Rectangle dirty;
        if (clip != null) {
            dirty = clip.getBounds();
        } else {
            dirty = getRootPane().getBounds();
            // account for our frame insets
            Insets insets = getInsets();
            dirty.x += insets.left;
            dirty.y += insets.top;
        }

        if (_fmgr != null) {
            _fmgr.restoreFromBack(dirty);
        }
    }

    protected FrameManager _fmgr;
}
