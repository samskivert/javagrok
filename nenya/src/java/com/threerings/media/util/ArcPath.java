//
// $Id: ArcPath.java 886 2010-02-12 19:36:44Z dhoover $
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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;

import com.samskivert.util.StringUtil;

import com.threerings.util.DirectionUtil;

/**
 * The line path is used to cause a pathable to go from point A to point B
 * along the arc of an ellipse in a certain number of milliseconds.
 */
public class ArcPath extends TimedPath
{
    /** An orientation constant indicating that the normal (eight)
     * directions should be used to orient the pathable being made to
     * follow this path. */
    public static final int NORMAL = 0;

    /** An orientation constant indicating that the fine (sixteen)
     * directions should be used to orient the pathable being made to
     * follow this path. */
    public static final int FINE = 1;

    /** An orientation indicating that the pathable should not be oriented
     * as it moves along the path.
     */
    public static final int NONE = 2;

    /**
     * Creates an arc path that will animate a pathable from the specified
     * starting position along an ellipse defined by the supplied
     * parameters. The pathable will travel the specified number of
     * radians along the arc of that ellipse. A positive number of radians
     * indicates counter-clockwise travel along the circle, a negative
     * number, clockwise rotation.
     *
     * @param start the starting point for the pathable.
     * @param xradius the length of the x radius.
     * @param yradius the length of the y radius.
     * @param sangle the starting angle.
     * @param delta the angle through which the pathable should be moved.
     * @param duration the number of milliseconds during which to effect
     * the animation.
     * @param orient an orientation code indicating how the pathable
     * should be oriented when following the path, either {@link #NORMAL},
     * or {@link #FINE}.
     */
    public ArcPath (Point start, double xradius, double yradius,
                    double sangle, double delta, long duration,
                    int orient)
    {
        super(duration);
        _xradius = xradius;
        _yradius = yradius;
        _sangle = sangle;
        _delta = delta;
        _orient = orient;

        // compute the center of the ellipse
        _center = new Point(
            (int)(start.x - Math.round(Math.cos(sangle) * xradius)),
            (int)(start.y - Math.round(Math.sin(sangle) * yradius)));
    }

    /**
     * Return a copy of the path, translated by the specified amounts.
     */
    public Path getTranslatedInstance (int x, int y)
    {
        int startx = (int)(_center.x + Math.round(Math.cos(_sangle) * _xradius));
        int starty = (int)(_center.y + Math.round(Math.sin(_sangle) * _yradius));

        return new ArcPath(new Point(startx + x, starty + y),
            _xradius, _yradius, _sangle, _delta, _duration, _orient);
    }

    /**
     * Sets the offset that is applied to the pathable whenever it is oriented. This offset is in
     * clockwise units whose granularity is specified by the {@link #NORMAL} or {@link #FINE}
     * setting supplied to the path at construct time. The intent here is to allow arc paths to be
     * applied that don't orient the pathable in the direction they are traveling but instead in
     * some fixed offset from that direction.
     */
    public void setOrientOffset (int offset)
    {
        _orientOffset = offset;
    }

    /**
     * Returns the position of the end of the path.
     */
    public Point getEndPos ()
    {
        return new Point(
            (int)(_center.x + Math.round(Math.cos(_sangle + _delta) * _xradius)),
            (int)(_center.y + Math.round(Math.sin(_sangle + _delta) * _yradius)));
    }

    // documentation inherited
    public boolean tick (Pathable pable, long timestamp)
    {
        double angle;
        boolean modified = false;

        // if we've blown past our arrival time...
        if (timestamp >= _startStamp + _duration) {
            // ...force the angle to the destination angle
            angle = _sangle + _delta;

        } else {
            // otherwise, compute the angle at which we should place the pathable based on the
            // elapsed time
            long elapsed = timestamp - _startStamp;
            angle = _sangle + _delta * elapsed / _duration;
        }

        // determine where we should be along the path
        computePosition(_center, _xradius, _yradius, angle, _tpos);

        // Skip this if we are not reorienting as we follow the path.
        if (_orient != NONE) {
            // compute the pathable's new orientation
            double theta = angle + ((_delta > 0) ? Math.PI/2 : -Math.PI/2);
            int orient;
            switch (_orient) {
            default:
            case NORMAL:
                orient = DirectionUtil.getDirection(theta);
                // adjust it appropriately
                orient = DirectionUtil.rotateCW(orient, 2*_orientOffset);
                break;

            case FINE:
                orient = DirectionUtil.getFineDirection(theta);
                // adjust it appropriately
                orient = DirectionUtil.rotateCW(orient, _orientOffset);
                break;
            }

            // update the pathable's orientation if it changed
            if (pable.getOrientation() != orient) {
                pable.setOrientation(orient);
                modified = true;
            }
        }

        // update the pathable's location if it moved
        if (pable.getX() != _tpos.x || pable.getY() != _tpos.y) {
            pable.setLocation(_tpos.x, _tpos.y);
            modified = true;
        }

        // if we completed our path, let the sprite know
        if (angle == _sangle + _delta) {
            pable.pathCompleted(timestamp);
        }

        return modified;
    }

    // documentation inherited
    public void paint (Graphics2D gfx)
    {
        int x = (int)(_center.x - _xradius), y = (int)(_center.y - _yradius);
        int width = (int)(2*_xradius), height = (int)(2*_yradius);
        int sangle = (int)(Math.round(180 * _sangle / Math.PI)),
            delta = (int)(Math.round(180 * _delta / Math.PI));

        gfx.setColor(Color.blue);
        gfx.drawRect(x, y, width-1, height-1);

        gfx.setColor(Color.yellow);
        gfx.drawArc(x, y, width-1, height-1, 0, 360);

        gfx.setColor(Color.red);
        gfx.drawArc(x, y, width-1, height-1, 360-sangle, -delta);
    }

    @Override
    protected void toString (StringBuilder buf)
    {
        super.toString(buf);
        buf.append(", center=").append(StringUtil.toString(_center));
        buf.append(", sangle=").append(_sangle);
        buf.append(", delta=").append(_delta);
        buf.append(", radii=").append(_xradius).append("/").append(_yradius);
    }

    /**
     * Computes the position of an entity along the path defined by the supplied parameters
     * assuming that it must finish the path in the specified duration (in millis) and has been
     * traveling the path for the specified number of elapsed milliseconds.
     */
    public static void computePosition (
        Point center, double xradius, double yradius, double angle, Point pos)
    {
        pos.x = (int)Math.round(center.x + xradius * Math.cos(angle));
        pos.y = (int)Math.round(center.y + yradius * Math.sin(angle));
    }

    /** The center of our ellipse. */
    protected Point _center;

    /** Our ellipse radii. */
    protected double _xradius, _yradius;

    /** Our starting and delta angles. */
    protected double _sangle, _delta;

    /** The method to be used to orient the pathable. */
    protected int _orient;

    /** An orientation offset used when orienting our pathable. */
    protected int _orientOffset = 0;

    /** A temporary point used when computing our position along the path. */
    protected Point _tpos = new Point();
}
