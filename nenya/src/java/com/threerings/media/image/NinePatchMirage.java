// $Id$
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

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

/**
 * Wraps a {@link NinePatch} as a {@link Mirage} of specific dimensions.
 */
public class NinePatchMirage
    implements Mirage
{
    public NinePatchMirage (BufferedImage img, int width, int height)
    {
        this(new NinePatch(img), width, height);
    }

    public NinePatchMirage (NinePatch ninePatch, int width, int height)
    {
        if (width < 0 || height < 0) {
            throw new IllegalArgumentException("Illegal dimensions");
        }

        _ninePatch = ninePatch;
        _width = width;
        _height = height;
    }

    /**
     * Returns a new Mirage that's a NinePatch stretched and positioned to contain the given
     * Rectangle.
     */
    public static Mirage newNinePatchContaining(NinePatch ninePatch, Rectangle content)
    {
        Rectangle bounds = ninePatch.getBoundsSurrounding(content);

        Mirage mirage = new NinePatchMirage(ninePatch, bounds.width, bounds.height);
        return new TransformedMirage(mirage,
            AffineTransform.getTranslateInstance(bounds.x, bounds.y), false);
    }

    public long getEstimatedMemoryUsage ()
    {
        return ImageUtil.getEstimatedMemoryUsage(_ninePatch._img.getRaster());
    }

    public BufferedImage getSnapshot ()
    {
        BufferedImage img = new BufferedImage(_width, _height, BufferedImage.TYPE_INT_ARGB);

        _ninePatch.paint(img.createGraphics(), new Rectangle(0, 0, _width, _height));

        return img;
    }

    public int getWidth ()
    {
        return _width;
    }

    public int getHeight ()
    {
        return _height;
    }

    public boolean hitTest (int x, int y)
    {
        // This is stupidly expensive.
        return ImageUtil.hitTest(getSnapshot(), x, y);
    }

    public void paint (Graphics2D gfx, int x, int y)
    {
        _ninePatch.paint(gfx, new Rectangle(x, y, _width, _height));
    }

    protected final NinePatch _ninePatch;
    protected final int _width;
    protected final int _height;
}
