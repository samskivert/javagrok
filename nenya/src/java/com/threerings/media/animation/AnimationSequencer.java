//
// $Id: AnimationSequencer.java 877 2010-02-08 23:43:45Z dhoover $
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

import java.util.ArrayList;

import java.awt.Graphics2D;
import java.awt.Rectangle;

import com.google.common.collect.Lists;

import com.samskivert.util.StringUtil;

import static com.threerings.media.Log.log;

/**
 * An animation that provides facilities for adding a sequence of animations that are fired after
 * a fixed time interval has elapsed or after previous animations in the sequence have completed.
 * Facilities are also provided for running code upon the completion of animations in the
 * sequence.
 */
public class AnimationSequencer extends Animation
{
    /**
     * Constructs an animation sequencer with the expectation that animations will be added via
     * subsequent calls to {@link #addAnimation}.
     *
     * @param animmgr the animation manager to which to add our animations when they are ready to
     * start.
     */
    public AnimationSequencer (AnimationManager animmgr)
    {
        super(new Rectangle());
        _animmgr = animmgr;
    }

    /**
     * Adds the supplied animation to the sequence with the given parameters. Note that care
     * should be taken if this is called after the animation sequence has begun firing animations.
     * Do not add new animations after the final animation in the sequence has been started or you
     * run the risk of attempting to add an animation to the sequence after it thinks that it has
     * finished (in which case this method will fail).
     *
     * @param anim the animation to be sequenced, or null if the completion action should be run
     * immediately when this "animation" is ready to fired.
     * @param delta the number of milliseconds following the <em>start</em> of the previous
     * animation in the queue that this animation should be started; 0 if it should be started
     * simultaneously with its predecessor int the queue; -1 if it should be started when its
     * predecessor has completed.
     * @param completionAction a runnable to be executed when this animation completes.
     */
    public void addAnimation (Animation anim, long delta, Runnable completionAction)
    {
        // sanity check
        if (_finished) {
            throw new IllegalStateException("Animation added to finished sequencer");
        }

        // if this guy is triggering on a previous animation, grab that
        // good fellow here
        AnimRecord trigger = null;
        if (delta == -1) {
            if (_queued.size() > 0) {
                // if there are queued animations we want the most
                // recently queued animation
                trigger = _queued.get(_queued.size()-1);
            } else if (_running.size() > 0) {
                // otherwise, if there are running animations, we want the
                // last one in that list
                trigger = _running.get(_running.size()-1);
            }
            // otherwise we have no trigger, we'll just start ASAP
        }

        AnimRecord arec = new AnimRecord(anim, delta, trigger, completionAction);
//         Log.info("Queued " + arec + ".");
        _queued.add(arec);
    }

    /**
     * Clears out the animations being managed by this sequencer.
     */
    public void clear ()
    {
        _queued.clear();
        _lastStamp = 0;
    }

    @Override
    public void tick (long tickStamp)
    {
        if (_lastStamp == 0) {
            _lastStamp = tickStamp;
        }

        // add all animations whose time has come
        while (_queued.size() > 0) {
            AnimRecord arec = _queued.get(0);
            if (!arec.readyToFire(tickStamp, _lastStamp)) {
                // if it's not time to add this animation, all subsequent
                // animations must surely wait as well
                break;
            }

            // remove it from queued and put it on the running list
            _queued.remove(0);
            _running.add(arec);

            // note that we've advanced to the next animation
            _lastStamp = tickStamp;

            // fire in the hole!
            arec.fire(tickStamp);
        }

        // we're done when both lists are empty
//         boolean finished = _finished;
        _finished = ((_queued.size() + _running.size()) == 0);
//         if (!finished && _finished) {
//             Log.info("Finishing sequence at " + (tickStamp%10000) + ".");
//         }
    }

    @Override
    public void paint (Graphics2D gfx)
    {
        // don't care
    }

    @Override
    public void fastForward (long timeDelta)
    {
        _lastStamp += timeDelta;
    }

    @Override
    public void viewLocationDidChange (int dx, int dy)
    {
        super.viewLocationDidChange(dx, dy);

        // track cumulative view location changes so that we can adjust our
        // animations before we start them
        _vdx += dx;
        _vdy += dy;
    }

    /**
     * Called when the time comes to start an animation. Derived classes may override this method
     * and pass the animation on to their animation manager and do whatever else they need to do
     * with operating animations. The default implementation simply adds them to the media panel
     * supplied when we were constructed.
     *
     * @param anim the animation to be displayed.
     * @param tickStamp the timestamp at which this animation was fired.
     */
    protected void startAnimation (Animation anim, long tickStamp)
    {
        // account for any view scrolling that happened before this animation
        // was actually added to the view
        if (_vdx != 0 || _vdy != 0) {
            anim.viewLocationDidChange(_vdx, _vdy);
        }

        _animmgr.registerAnimation(anim);
    }

    protected class AnimRecord extends AnimationAdapter
    {
        public AnimRecord (
            Animation anim, long delta, AnimRecord trigger, Runnable completionAction) {
            _anim = anim;
            _delta = delta;
            _trigger = trigger;
            _completionAction = completionAction;
        }

        public boolean readyToFire (long now, long lastStamp) {
            if (_delta == -1) {
                // if we have no trigger, that means we should start immediately, otherwise we wait
                // until our trigger is no longer running (they are guaranteed not to be still
                // queued at this point because readyToFire is only called on an animation after
                // all animations previous in the queue have been started)
                return (_trigger == null) ? true : !_running.contains(_trigger);

            } else {
                return (lastStamp + _delta <= now);
            }
        }

        public void fire (long when) {
//             Log.info("Firing " + this + " at " + (when%10000) + ".");

            // if we have an animation, start it up and await its completion
            if (_anim != null) {
                startAnimation(_anim, when);
                _anim.addAnimationObserver(this);

            } else {
                // since there's no animation, we need to fire our
                // completion routine immediately
                fireCompletion(when);
            }
        }

        public void fireCompletion (long when) {
//             Log.info("Completing " + this + " at " + (when%10000) + ".");

            // call the completion action, if there is one
            if (_completionAction != null) {
                try {
                    _completionAction.run();
                } catch (Throwable t) {
                    log.warning(t);
                }
            }

            // make a note that this animation is complete
            _running.remove(this);

            // kids, don't try this at home; we call tick() immediately so that any animations
            // triggered on the completion of previous animations can trigger on the completion of
            // this animation rather than having to wait until the next tick to do so
            tick(when);
        }

        @Override
        public void animationCompleted (Animation anim, long when) {
            fireCompletion(when);
        }

        @Override
        public String toString () {
            return "[anim=" + StringUtil.shortClassName(_anim) +
                ((_anim == null) ? "" : ("/" + _anim.hashCode())) +
                ", action=" + _completionAction +
                ", delta=" + _delta + ", trig=" + (_trigger != null) + "]";
        }

        protected Animation _anim;
        protected Runnable _completionAction;
        protected long _delta;
        protected AnimRecord _trigger;
    }

    /** The animation manager in which we run animations. */
    protected AnimationManager _animmgr;

    /** Animations that have not been fired. */
    protected ArrayList<AnimRecord> _queued = Lists.newArrayList();

    /** Animations that are currently running. */
    protected ArrayList<AnimRecord> _running = Lists.newArrayList();

    /** The timestamp at which we fired the last animation. */
    protected long _lastStamp;

    /** Used to track view scrolling while animations are in limbo. */
    protected int _vdx, _vdy;
}
