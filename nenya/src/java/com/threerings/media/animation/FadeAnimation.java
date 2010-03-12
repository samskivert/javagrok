//
// $Id: FadeAnimation.java 877 2010-02-08 23:43:45Z dhoover $
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

import com.threerings.media.effects.FadeEffect;

/**
 * An animation that displays an image fading from one alpha level to another in specified
 * increments over time. The animation is finished when the specified target alpha is reached.
 */
public abstract class FadeAnimation extends Animation
{
    /**
     * Constructs a fade animation.
     *
     * @param bounds our bounds.
     * @param alpha the starting alpha.
     * @param step the alpha amount to step by each millisecond.
     * @param target the target alpha level.
     */
    protected FadeAnimation (Rectangle bounds, float alpha, float step, float target)
    {
        super(bounds);
        _effect = new FadeEffect(alpha, step, target);
    }

    @Override
    public void tick (long timestamp)
    {
        if (_effect.tick(timestamp)) {
            _finished = _effect.finished();
            invalidate();
        }
    }

    @Override
    public void paint (Graphics2D gfx)
    {
        _effect.beforePaint(gfx);
        paintAnimation(gfx);
        _effect.afterPaint(gfx);
    }

    @Override
    protected void willStart (long tickStamp)
    {
        super.willStart(tickStamp);
        _effect.init(tickStamp);
    }

    /**
     * Here is where derived animations actually render their image.
     */
    protected abstract void paintAnimation (Graphics2D gfx);

    /** This handles the main work of fading. */
    protected FadeEffect _effect;
}
