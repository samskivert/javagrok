//
// $Id: AnimationObserver.java 868 2010-01-04 21:47:34Z dhoover $
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

/**
 * An interface to be implemented by classes that would like to observe an
 * {@link Animation} and be notified of interesting events relating to it.
 */
public interface AnimationObserver
{
    /**
     * Called the first time this animation is ticked.
     */
    public void animationStarted (Animation anim, long when);

    /**
     * Called when the observed animation has completed.
     */
    public void animationCompleted (Animation anim, long when);
}
