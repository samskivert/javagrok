//
// $Id: KeyDispatcher.java 868 2010-01-04 21:47:34Z dhoover $
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

package com.threerings.util;

import java.util.ArrayList;
import java.util.LinkedList;

import java.awt.Component;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;

import javax.swing.JComboBox;
import javax.swing.JRootPane;
import javax.swing.JTable;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.text.JTextComponent;

import com.google.common.collect.Lists;

import com.samskivert.util.HashIntMap;

/**
 * Handles dispatching special global key pressed and released events to
 * those that care to monitor and process such things.
 *
 * <p> A {@link JTextComponent} may registered as a "chat grabber" via
 * {@link #pushChatGrabber} so as to capture typed chat characters
 * regardless of which component currently has the focus.
 *
 * <p> Components may also register as global {@link KeyListener}s via
 * {@link #addGlobalKeyListener} to always be notified of key press and
 * release events for all keys.
 */
public class KeyDispatcher
    implements KeyEventDispatcher, AncestorListener, WindowFocusListener
{
    /**
     * Constructs a key dispatcher.
     */
    public KeyDispatcher (Window window)
    {
        // save things off
        _window = window;

        // listen to window events on our main window so that we can release
        // keys when the mouse leaves the window
        _window.addWindowFocusListener(this);

        // monitor key events from the central dispatch mechanism
        KeyboardFocusManager.getCurrentKeyboardFocusManager().
            addKeyEventDispatcher(this);
    }

    /**
     * Shuts down the key dispatcher.
     */
    public void shutdown ()
    {
        // cease monitoring key events
        KeyboardFocusManager.getCurrentKeyboardFocusManager().
            removeKeyEventDispatcher(this);

        // cease observing our window
        _window.removeWindowFocusListener(this);
    }

    /**
     * Makes the specified component the new grabber of key typed events
     * that look like they're chat-related per {@link #isChatCharacter}.
     */
    public void pushChatGrabber (JTextComponent comp)
    {
        // note this component as the new chat grabber
        _curChatGrabber = comp;

        // add the component to the list of grabbers
        _chatGrabbers.addLast(comp);
        comp.addAncestorListener(this);

        // and request to give the new component the focus since that's a
        // sensible thing to do as it's aiming to nab all key events
        // henceforth
        comp.requestFocusInWindow();
    }

    /**
     * Removes the specified component from the list of chat grabbers so
     * that it will no longer be notified of chat-like key events.
     */
    public void removeChatGrabber (JTextComponent comp)
    {
        // remove the component from the list of grabbers
        comp.removeAncestorListener(this);
        _chatGrabbers.remove(comp);

        // update the current chat grabbing component
        _curChatGrabber = _chatGrabbers.isEmpty() ? null :
            _chatGrabbers.getLast();
    }

    /**
     * Adds the key listener to receive all key events at all times.
     */
    public void addGlobalKeyListener (KeyListener listener)
    {
        _listeners.add(listener);
    }

    /**
     * Removes the specified global key listener.
     */
    public void removeGlobalKeyListener (KeyListener listener)
    {
        _listeners.remove(listener);
    }

    // documentation inherited from interface KeyEventDispatcher
    public boolean dispatchKeyEvent (KeyEvent e)
    {
        int lsize = _listeners.size();

        switch (e.getID()) {
        case KeyEvent.KEY_TYPED:
            // dispatch to all the global listeners
            for (int ii = 0; ii < lsize; ii++) {
                _listeners.get(ii).keyTyped(e);
            }

            // see if a chat grabber needs to grab it
            if (_curChatGrabber != null)  {
                Component target = e.getComponent();
                // if the key was typed on a non-text component or one
                // that wasn't editable...
                if (isChatCharacter(e.getKeyChar()) &&
                    !isTypeableTarget(target)) {
                    // focus our grabby component, and redirect this
                    // key event there
                    _curChatGrabber.requestFocusInWindow();
                    KeyboardFocusManager.getCurrentKeyboardFocusManager().
                        redispatchEvent(_curChatGrabber, e);
                    return true;
                }
            }
            break;

        case KeyEvent.KEY_PRESSED:
            if (lsize > 0) {
                for (int ii = 0; ii < lsize; ii++) {
                    _listeners.get(ii).keyPressed(e);
                }
                // remember the key event..
                _downKeys.put(e.getKeyCode(), e);
            }
            break;

        case KeyEvent.KEY_RELEASED:
            if (lsize > 0) {
                for (int ii = 0; ii < lsize; ii++) {
                    _listeners.get(ii).keyReleased(e);
                }
                // forget the key event
                _downKeys.remove(e.getKeyCode());
            }
            break;
        }

        return false;
    }

    /**
     * Returns true if the specified target component supports being typed into, and thus we
     * shouldn't steal focus away from it if the user starts typing.
     */
    protected boolean isTypeableTarget (Component target)
    {
        return target.isShowing() &&
            (((target instanceof JTextComponent) && ((JTextComponent) target).isEditable()) ||
             (target instanceof JComboBox) ||
             (target instanceof JTable) ||
             (target instanceof JRootPane));
    }

    /**
     * Returns whether the specified character is a chat character.
     */
    protected boolean isChatCharacter (char c)
    {
        return (Character.isLetterOrDigit(c) || ('/' == c));
    }

    // documentation inherited from interface WindowFocusListener
    public void windowGainedFocus (WindowEvent e)
    {
        // nothing
    }

    // documentation inherited from interface WindowFocusListener
    public void windowLostFocus (WindowEvent e)
    {
        // un-press any keys that were left down
        if (!_downKeys.isEmpty()) {
            long now = System.currentTimeMillis();
            for (KeyEvent down : _downKeys.values()) {
                KeyEvent up = new KeyEvent(
                    down.getComponent(), KeyEvent.KEY_RELEASED, now,
                    down.getModifiers(), down.getKeyCode(), down.getKeyChar(),
                    down.getKeyLocation());
                for (int ii = 0, nn = _listeners.size(); ii < nn; ii++) {
                    _listeners.get(ii).keyReleased(up);
                }
            }
            _downKeys.clear();
        }
    }

    // documentation inherited from interface AncestorListener
    public void ancestorAdded (AncestorEvent ae)
    {
        // nothing
    }

    // documentation inherited from interface AncestorListener
    public void ancestorMoved (AncestorEvent ae)
    {
        // nothing
    }

    // documentation inherited from interface AncestorListener
    public void ancestorRemoved (AncestorEvent ae)
    {
        removeChatGrabber((JTextComponent) ae.getComponent());
    }

    /** The main window for which we're observing key events. */
    protected Window _window;

    /** The current most-recently pushed component that wants to grab
     * alphanumeric key presses. */
    protected JTextComponent _curChatGrabber;

    /** The stack of grabbers. */
    protected LinkedList<JTextComponent> _chatGrabbers =
        new LinkedList<JTextComponent>();

    /** Global key listeners. */
    protected ArrayList<KeyListener> _listeners = Lists.newArrayList();

    /** Keys that are currently held down. */
    protected HashIntMap<KeyEvent> _downKeys = new HashIntMap<KeyEvent>();
}
