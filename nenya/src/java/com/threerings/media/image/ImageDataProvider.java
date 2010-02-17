//
// $Id: ImageDataProvider.java 868 2010-01-04 21:47:34Z dhoover $
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


import java.io.IOException;

import java.awt.image.BufferedImage;

/**
 * Provides access to image data for the image with the specified
 * path. Images loaded from different data providers (which are
 * differentiated by reference equality) will be considered distinct
 * images with respect to caching.
 */
public interface ImageDataProvider
{
    /**
     * Returns a string identifier for this image data provider which wil
     * be used to differentiate it from other providers and thus should be
     * unique.
     */
    public String getIdent ();

    /**
     * Returns the image at the specified path.
     */
    public BufferedImage loadImage (String path) throws IOException;
}
