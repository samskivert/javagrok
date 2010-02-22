//
// $Id: BlankAnimation.java 877 2010-02-08 23:43:45Z dhoover $
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

package com.threerings.media.animation;

import java.awt.Graphics2D;
import java.awt.Rectangle;

/**
 * Displays nothing, but does so for a specified amount of time. Useful when you want to get an
 * animation completed event in some period of time but don't actually need to display anything.
 */
public class BlankAnimation extends Animation
{
    public BlankAnimation (long duration)
    {
        super(new Rectangle(0, 0, 0, 0));
        _duration = duration;
    }

    @Override
    public void tick (long timestamp)
    {
        if (_start == 0) {
            // initialize our starting time
            _start = timestamp;
        }

        // check whether we're done
        _finished = (timestamp - _start >= _duration);
    }

    @Override
    public void fastForward (long timeDelta)
    {
        if (_start > 0) {
            _start += timeDelta;
        }
    }

    @Override
    public void paint (Graphics2D gfx)
    {
        // nothing doing
    }

    protected long _duration, _start;
}
