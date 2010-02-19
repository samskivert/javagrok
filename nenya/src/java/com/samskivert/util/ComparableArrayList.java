//
// $Id$
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

import java.util.Arrays;
import java.util.Comparator;

/**
 * Provides a mechanism ({@link #sort}) for sorting the contents of the list
 * that doesn't involve creating two object arrays. Two copies of the elements
 * array are made if you called {@link java.util.Collections#sort}</code> (the
 * first is when {@link #toArray} is called on the collection and the second is
 * when {@link Arrays#sort} clones the supplied array so that it can do a merge
 * sort).
 */
public class ComparableArrayList<T extends Comparable<? super T>>
    extends SortableArrayList<T>
{
    /**
     * Sorts the elements in this list using the quick sort algorithm
     * (which does not involve any object allocation). The elements must
     * implement {@link Comparable} and all be mutually comparable.
     */
    public void sort ()
    {
        sort(_comp);
    }

    /**
     * Sorts the elements in this list using the quick sort algorithm
     * according to their reverse natural ordering. The elements must
     * implement {@link Comparable} and all be mutually comparable.
     */
    public void rsort ()
    {
        sort(new Comparator<T>() {
            public int compare (T o1, T o2) {
                return _comp.compare(o2, o1);
            }
        });
    }

    /**
     * Inserts the specified item into the list into a position that
     * preserves the sorting of the list. The list must be sorted prior to
     * the call to this method (an empty list built up entirely via calls
     * to {@link #insertSorted} will be properly sorted). The list must be
     * entirely comprised of elements that implement {@link Comparable}
     * and the element being added must implement {@link Comparable} as
     * well.
     *
     * @return the index at which the element was inserted.
     */
    public int insertSorted (T value)
    {
        return insertSorted(value, _comp);
    }

    protected transient Comparator<T> _comp = Comparators.comparable();

    /** Change this if the fields or inheritance hierarchy ever changes
     * (which is extremely unlikely). We override this because I'm tired
     * of serialized crap not working depending on whether I compiled with
     * jikes or javac. */
    private static final long serialVersionUID = 1;
}
