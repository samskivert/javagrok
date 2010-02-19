//
// $Id: CachedVolatileMirage.java 868 2010-01-04 21:47:34Z dhoover $
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

package com.threerings.media.image;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Transparency;
import java.awt.image.BufferedImage;

import static com.threerings.media.Log.log;

/**
 * A mirage implementation which allows the image to be maintained in
 * video memory and refetched from the image manager in the event that our
 * target screen resolution changes or we are flushed from video memory
 * for some other reason.
 *
 * <p> These objects are never created directly, but always obtained from
 * the {@link ImageManager}.
 */
public class CachedVolatileMirage extends VolatileMirage
{
    /**
     * Creates a mirage with the supplied regeneration informoation and
     * prepared image.
     */
    protected CachedVolatileMirage (
        ImageManager imgr, ImageManager.ImageKey source,
        Rectangle bounds, Colorization[] zations)
    {
        super(imgr, bounds);

        _source = source;
        _zations = zations;

        // create our volatile image for the first time
        createVolatileImage();
    }

    @Override
    protected int getTransparency ()
    {
        BufferedImage source = _imgr.getImage(_source, _zations);
        return (source == null) ? Transparency.OPAQUE :
            source.getColorModel().getTransparency();
    }

    @Override
    protected void refreshVolatileImage ()
    {
        Graphics gfx = null;
        try {
            BufferedImage source = _imgr.getImage(_source, _zations);
            if (source != null) {
                gfx = _image.getGraphics();
                // We grab a subimage before drawing because otherwise bufferedimage does all its
                //  computations across the entire image, including those parts outside the bounds.
                BufferedImage subImg =
                    source.getSubimage(_bounds.x, _bounds.y, _bounds.width, _bounds.height);
                gfx.drawImage(subImg, 0, 0, null);
            }

        } catch (Exception e) {
            log.warning("Failure refreshing mirage " + this + ".", e);

        } finally {
            if (gfx != null) {
                gfx.dispose();
            }
        }
    }

    @Override
    protected void toString (StringBuilder buf)
    {
        super.toString(buf);
        buf.append(", key=").append(_source);
        buf.append(", zations=").append(_zations);
    }

    /** The key that identifies the image data used to create our volatile
     * image. */
    protected ImageManager.ImageKey _source;

    /** Optional colorizations that are applied to our source image when
     * creating our mirage. */
    protected Colorization[] _zations;
}
