//
// $Id: LogBuilder.java 2686 2010-01-06 00:53:25Z ray.j.greenwell $
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

/**
 * Formats a message and an array of alternating key value pairs like so:
 *
 * <pre>message [key=value, key=value, key=value]</pre>
 */
public class LogBuilder
{
    /**
     * Creates a log builder with no message and no initial key value pairs.
     */
    public LogBuilder ()
    {
        this("");
    }

    /**
     * Creates a log builder with the given message and key value pairs.
     */
    public LogBuilder (Object message, Object... args)
    {
        _log = new StringBuilder().append(message);
        append(args);
    }

    /**
     * Adds the given key value pairs to the log.
     */
    public LogBuilder append (Object... args)
    {
        if (args != null && args.length > 1) {
            for (int ii = 0, nn = args.length / 2; ii < nn; ii++) {
                if (_hasArgs) {
                    _log.append(", ");
                } else {
                    if (_log.length() > 0) {
                        // only need a space if we have a message
                        _log.append(' ');
                    }
                    _log.append('[');
                    _hasArgs = true;
                }
                _log.append(args[2 * ii]).append("=");
                try {
                    StringUtil.toString(_log, args[2 * ii + 1]);
                } catch (Throwable t) {
                    _log.append("<toString() failure: " + t + ">");
                }
            }
        }
        return this;
    }

    /**
     * Returns the formatted log message. Does not reset the buffer. You can continue to append
     * arguments and call {@link #toString} again.
     */
    @Override public String toString ()
    {
        String log = _log.toString();
        if (_hasArgs) {
            log += "]";
        }
        return log;
    }

    protected boolean _hasArgs;
    protected StringBuilder _log;
}
