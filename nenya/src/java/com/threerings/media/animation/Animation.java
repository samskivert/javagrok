//
// $Id: Animation.java 877 2010-02-08 23:43:45Z dhoover $
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

import java.awt.Rectangle;

import com.samskivert.util.ObserverList;

import com.threerings.media.AbstractMedia;

/**
 * The animation class is an abstract class that should be extended to provide animation
 * functionality. It is generally used in conjunction with an {@link AnimationManager}.
 */
public abstract class Animation extends AbstractMedia
{
    /**
     * Constructs an animation.
     *
     * @param bounds the animation rendering bounds.
     */
    public Animation (Rectangle bounds)
    {
        super(bounds);
    }

    /**
     * Returns true if the animation has finished all of its business, false if not.
     */
    public boolean isFinished ()
    {
        return _finished;
    }

    /**
     * If this animation has run to completion, it can be reset to prepare it for another go.
     */
    public void reset ()
    {
        _finished = false;
    }

    @Override
    public void setLocation (int x, int y)
    {
        if (_bounds.x == x && _bounds.y == y) {
            return; // no-op
        }

        // make a note of our current bounds
        Rectangle obounds = new Rectangle(_bounds);

        // move ourselves
        super.setLocation(x, y);

        // this method will invalidate our old and new bounds efficiently
        invalidateAfterChange(obounds);
    }

    @Override
    protected void willStart (long tickStamp)
    {
        super.willStart(tickStamp);
        queueNotification(new AnimStartedOp(this, tickStamp));
    }

    /**
     * Called when the animation is finished and the animation manager is about to remove it from
     * service.
     */
    protected void willFinish (long tickStamp)
    {
        queueNotification(new AnimCompletedOp(this, tickStamp));
    }

    /**
     * Called when the animation is finished and the animation manager has removed it from
     * service.
     */
    protected void didFinish (long tickStamp)
    {
    }

    /**
     * Adds an animation observer to this animation's list of observers.
     */
    public void addAnimationObserver (AnimationObserver obs)
    {
        addObserver(obs);
    }

    /**
     * Removes an animation observer from this animation's list of observers.
     */
    public void removeAnimationObserver (AnimationObserver obs)
    {
        removeObserver(obs);
    }

    /** Whether the animation is finished. */
    protected boolean _finished = false;

    /** Used to dispatch {@link AnimationObserver#animationStarted}. */
    protected static class AnimStartedOp implements ObserverList.ObserverOp<Object>
    {
        public AnimStartedOp (Animation anim, long when) {
            _anim = anim;
            _when = when;
        }

        public boolean apply (Object observer) {
            ((AnimationObserver)observer).animationStarted(_anim, _when);
            return true;
        }

        protected Animation _anim;
        protected long _when;
    }

    /** Used to dispatch {@link AnimationObserver#animationCompleted}. */
    protected static class AnimCompletedOp implements ObserverList.ObserverOp<Object>
    {
        public AnimCompletedOp (Animation anim, long when) {
            _anim = anim;
            _when = when;
        }

        public boolean apply (Object observer) {
            ((AnimationObserver)observer).animationCompleted(_anim, _when);
            return true;
        }

        protected Animation _anim;
        protected long _when;
    }
}
