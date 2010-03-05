//
// $Id: PerfTimer.java 868 2010-01-04 21:47:34Z dhoover $
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

package com.threerings.media.timer;

import sun.misc.Perf;

/**
 * A timer that uses the performance clock exposed by Sun in JDK 1.4.2.
 */
public class PerfTimer extends CalibratingTimer
{
    public PerfTimer ()
    {
        _timer = Perf.getPerf();
        init(_timer.highResFrequency() / 1000, _timer.highResFrequency() / 1000000);
    }

    @Override
    public long current ()
    {
        return _timer.highResCounter();
    }

    /** A performance timer object. */
    protected Perf _timer;
}
