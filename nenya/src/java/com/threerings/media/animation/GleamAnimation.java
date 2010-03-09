//
// $Id: GleamAnimation.java 881 2010-02-10 20:32:41Z dhoover $
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

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Transparency;

import com.threerings.media.sprite.Sprite;
import com.threerings.media.sprite.SpriteManager;
import com.threerings.media.util.LinearTimeFunction;
import com.threerings.media.util.TimeFunction;

/**
 * Washes all non-transparent pixels in a sprite with a particular color (by compositing them with
 * the solid color with progressively higher alpha values) and then back again.
 */
public class GleamAnimation extends Animation
{

    /**
     * Creates a gleam animation with the supplied sprite. The sprite will be faded to the
     * specified color and then back again.
     *
     * @param fadeIn if true, the sprite itself will be faded in as we fade up to the gleam color
     * and the gleam color will fade out, leaving just the sprite imagery.
     */
    public GleamAnimation (Sprite sprite, Color color, int upmillis, int downmillis,
            boolean fadeIn)
    {
        this(null, sprite, color, upmillis, downmillis, fadeIn);
    }

    /**
     * Creates a gleam animation with the supplied sprite. The sprite will be faded to the
     * specified color and then back again. The sprite may be already added to the supplied sprite
     * manager or not, but when the animation is complete, it will have been added.
     *
     * @param fadeIn if true, the sprite itself will be faded in as we fade up to the gleam color
     * and the gleam color will fade out, leaving just the sprite imagery.
     */
    public GleamAnimation (SpriteManager spmgr, Sprite sprite, Color color, int upmillis,
            int downmillis, boolean fadeIn)
    {
        super(new Rectangle(sprite.getBounds()));
        _spmgr = spmgr;
        _sprite = sprite;
        _color = color;
        _upmillis = upmillis;
        _downmillis = downmillis;
        _fadeIn = fadeIn;
    }

    @Override
    public void tick (long timestamp)
    {
        if (timestamp - _lastUpdate < _millisBetweenUpdates) {
            return;
        }
        _lastUpdate = timestamp;
        int alpha;
        if (_upfunc != null) {
            if ((alpha = _upfunc.getValue(timestamp)) >= _maxAlpha) {
                _upfunc = null;
            }
        } else if (_downfunc != null) {
            if ((alpha = _downfunc.getValue(timestamp)) <= _minAlpha) {
                _downfunc = null;
            }
        } else {
            _finished = true;
            return;
        }

        // if the sprite is moved or changed size while we're gleaming it, track those changes
        if (!_bounds.equals(_sprite.getBounds())) {
            Rectangle obounds = new Rectangle(_bounds);
            _bounds.setBounds(_sprite.getBounds());
            invalidateAfterChange(obounds);
        }

        if (_alpha != alpha) {
            _alpha = alpha;
            invalidate();
        }
    }

    @Override
    public void fastForward (long timeDelta)
    {
        if (_upfunc != null) {
            _upfunc.fastForward(timeDelta);
        } else if (_downfunc != null) {
            _downfunc.fastForward(timeDelta);
        }
    }

    @Override
    public void paint (Graphics2D gfx)
    {
        // TODO: recreate our off image if the sprite bounds changed; we
        // also need to change the bounds of our animation which might
        // require some jockeying (especially if we shrink)
        if (_offimg == null) {
            _offimg = gfx.getDeviceConfiguration().createCompatibleImage(_bounds.width,
                _bounds.height, Transparency.TRANSLUCENT);
        }

        // create a mask image with our sprite and the appropriate color
        Graphics2D ogfx = (Graphics2D)_offimg.getGraphics();
        try {
            ogfx.setColor(_color);
            ogfx.fillRect(0, 0, _bounds.width, _bounds.height);
            ogfx.setComposite(AlphaComposite.DstAtop);
            ogfx.translate(-_sprite.getX(), -_sprite.getY());
            _sprite.paint(ogfx);
        } finally {
            ogfx.dispose();
        }

        Composite ocomp = null;
        Composite ncomp = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, _alpha / 1000f);

        // if we're fading the sprite in on the way up, set our alpha
        // composite before we render the sprite
        if (_fadeIn && _upfunc != null) {
            ocomp = gfx.getComposite();
            gfx.setComposite(ncomp);
        }

        // next render the sprite
        _sprite.paint(gfx);

        // if we're not fading in, we still need to alpha the white bits
        if (ocomp == null) {
            ocomp = gfx.getComposite();
            gfx.setComposite(ncomp);
        }

        // now alpha composite our mask atop the sprite
        gfx.drawImage(_offimg, _sprite.getX(), _sprite.getY(), null);
        gfx.setComposite(ocomp);
    }

    @Override
    protected void willStart (long tickStamp)
    {
        _upfunc = new LinearTimeFunction(_minAlpha, _maxAlpha, _upmillis);
        _downfunc = new LinearTimeFunction(_maxAlpha, _minAlpha, _downmillis);

        super.willStart(tickStamp);

        // remove the sprite we're fiddling with from the manager; we'll
        // add it back when we're done
        if (_spmgr != null && _spmgr.isManaged(_sprite)) {
            _spmgr.removeSprite(_sprite);
        }
    }

    @Override
    protected void shutdown ()
    {
        super.shutdown();
        if (_spmgr != null && !_spmgr.isManaged(_sprite)) {
            _spmgr.addSprite(_sprite);
        }
    }

    protected SpriteManager _spmgr;
    protected Sprite _sprite;
    protected Color _color;
    protected Image _offimg;
    protected boolean _fadeIn;

    protected TimeFunction _upfunc;
    protected TimeFunction _downfunc;
    protected int _upmillis;
    protected int _downmillis;
    protected int _maxAlpha = 750;
    protected int _minAlpha;
    protected int _alpha;
    protected long _lastUpdate;
    protected int _millisBetweenUpdates;
}
