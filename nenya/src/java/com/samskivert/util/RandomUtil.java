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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;

import static com.samskivert.Log.log;

/**
 * Provides miscellaneous utility routines to simplify obtaining useful random number values and to
 * centralize seeding and proper care and feeding of the pseudo-random number generator.
 */
public class RandomUtil
{
    /** The random number generator used by the methods in this class. */
    public static final Random rand = new Random();

    /**
     * Returns a pseudorandom, uniformly distributed <code>int</code> value between 0 (inclusive)
     * and the specified value (exclusive).
     *
     * @param high the high value limiting the random number sought.
     */
    public static int getInt (int high)
    {
        return getInt(high, rand);
    }

    /**
     * Returns a pseudorandom, uniformly distributed <code>int</code> value between 0 (inclusive)
     * and the specified value (exclusive).
     *
     * @param high the high value limiting the random number sought.
     * @param r the random number generator to use
     */
    public static int getInt (int high, Random r)
    {
        return r.nextInt(high);
    }

    /**
     * Returns a pseudorandom, uniformly distributed <code>int</code> value between
     * <code>high</code> and <code>low</code>, exclusive of each.
     */
    public static int getInt (int high, int low)
    {
        if (high - low - 1 <= 0) {
            throw new IllegalArgumentException(
                "Invalid range [high=" + high + ", low=" + low + "]");
        }
        return low + 1 + rand.nextInt(high - low - 1);
    }

    /**
     * Returns a pseudorandom, uniformly distributed float value between 0.0 (inclusive) and the
     * specified value (exclusive).
     *
     * @param high the high value limiting the random number sought.
     */
    public static float getFloat (float high)
    {
        return getFloat(high, rand);
    }

    /**
     * Returns a pseudorandom, uniformly distributed float value between 0.0 (inclusive) and the
     * specified value (exclusive).
     *
     * @param high the high value limiting the random number sought.
     * @param r the random number generator to use
     */
    public static float getFloat (float high, Random r)
    {
        return r.nextFloat() * high;
    }

    /**
     * Returns true approximately one in n times.
     */
    public static boolean getChance (int n)
    {
        return getChance(n, rand);
    }

    /**
     * Returns true approximately one in n times.
     *
     * @param r the random number generator to use
     */
    public static boolean getChance (int n, Random r)
    {
        return getInt(n, r) == 0;
    }

     /**
     * Has a probability p of returning true.
     */
    public static boolean getProbability (float p)
    {
        return getProbability(p, rand);
    }

    /**
     * Has a probability p of returning true.
     *
     * @param r the random number generator to use
     */
    public static boolean getProbability (float p, Random r)
    {
        return r.nextFloat() < p;
    }

    /**
     * Returns a pseudorandom, uniformly distributed boolean.
     */
    public static boolean getBoolean ()
    {
        return getBoolean(rand);
    }

    /**
     * Returns a pseudorandom, uniformly distributed boolean.
     *
     * @param r the random number generator to use
     */
    public static boolean getBoolean (Random r)
    {
        return r.nextBoolean();
    }

    /**
     * Pick a random index from the array, weighted by the value of the corresponding array
     * element.
     *
     * @param weights an array of non-negative integers.
     *
     * @return an index into the array, or -1 if the sum of the weights is less than 1.  For
     * example, passing in {1, 0, 3, 4} will return:
     *
     * <table><tr><td>0</td><td>1/8th of the time</td></tr>
     * <tr><td>1</td><td>never</td></tr>
     * <tr><td>2</td><td>3/8th of the time</td></tr>
     * <tr><td>3</td><td>half of the time</td></tr></table>
     */
    public static int getWeightedIndex (int[] weights)
    {
        return getWeightedIndex(weights, rand);
    }

    /**
     * Pick a random index from the array, weighted by the value of the corresponding array
     * element.
     *
     * @param weights an array of non-negative integers.
     * @param r the random number generator to use
     *
     * @return an index into the array, or -1 if the sum of the weights is less than 1.  For
     * example, passing in {1, 0, 3, 4} will return:
     *
     * <table><tr><td>0</td><td>1/8th of the time</td></tr>
     * <tr><td>1</td><td>never</td></tr>
     * <tr><td>2</td><td>3/8th of the time</td></tr>
     * <tr><td>3</td><td>half of the time</td></tr></table>
     */
    public static int getWeightedIndex (int[] weights, Random r)
    {
        int sum = IntListUtil.sum(weights);
        if (sum < 1) {
            return -1;
        }
        int pick = getInt(sum, r);
        for (int ii = 0, nn = weights.length; ii < nn; ii++) {
            pick -= weights[ii];
            if (pick < 0) {
                return ii;
            }
        }
        log.warning("getWeightedIndex failed", new Throwable()); // Impossible!
        return 0;
    }

    /**
     * Pick a random index from the array, weighted by the value of the corresponding array
     * element.
     *
     * @param weights an array of non-negative floats.
     *
     * @return an index into the array, or -1 if the sum of the weights is less than or equal to
     * 0.0 or any individual element is negative.  For example, passing in {0.2, 0.0, 0.6, 0.8}
     * will return:
     *
     * <table><tr><td>0</td><td>1/8th of the time</td></tr>
     * <tr><td>1</td><td>never</td></tr>
     * <tr><td>2</td><td>3/8th of the time</td></tr>
     * <tr><td>3</td><td>half of the time</td></tr></table>
     */
    public static int getWeightedIndex (float[] weights)
    {
        return getWeightedIndex(weights, rand);
    }

    /**
     * Pick a random index from the array, weighted by the value of the corresponding array
     * element.
     *
     * @param weights an array of non-negative floats.
     *
     * @return an index into the array, or -1 if the sum of the weights is less than or equal to
     * 0.0 or any individual element is negative.  For example, passing in {0.2, 0.0, 0.6, 0.8}
     * will return:
     *
     * <table><tr><td>0</td><td>1/8th of the time</td></tr>
     * <tr><td>1</td><td>never</td></tr>
     * <tr><td>2</td><td>3/8th of the time</td></tr>
     * <tr><td>3</td><td>half of the time</td></tr></table>
     */
    public static int getWeightedIndex (float[] weights, Random r)
    {
        float sum = 0.0f;
        for (float weight : weights) {
            if (weight < 0.0f) {
                return -1;
            }
            sum += weight;
        }

        if (sum <= 0.0) {
            return -1;
        }
        float pick = getFloat(sum, r);
        for (int ii = 0, nn = weights.length; ii < nn; ii++) {
            pick -= weights[ii];
            if (pick < 0.0) {
                return ii;
            }
        }

        log.warning("getWeightedIndex failed", new Throwable()); // Impossible!
        return 0;
    }

    /**
     * Picks a random object from the supplied array of values. Even weight is given to all
     * elements of the array.
     *
     * @return a randomly selected item or null if the array is null or of length zero.
     */
    public static <T> T pickRandom (T[] values)
    {
        return (values == null || values.length == 0) ? null : values[getInt(values.length)];
    }

    /**
     * Picks a random object from the supplied array of values, not including the specified skip
     * object as a possible selection (equality with the skipped object is referential rather than
     * via {@link Object#equals}). The element to be skipped must exist in the array exactly
     * once. Even weight is given to all elements of the array except the skipped element.
     *
     * @return a randomly selected item or null if the array is null, of length zero or contains
     * only the skip item.
     */
    public static <T> T pickRandom (T[] values, T skip)
    {
        if (values == null || values.length < 2) {
            return null;
        }
        int index = getInt(values.length-1);
        for (int ii = 0; ii <= index; ii++) {
            if (values[ii] == skip) {
                index++;
            }
        }
        return (index >= values.length) ? null : values[index];
    }

    /**
     * Picks a random object from the supplied {@link Collection}.
     */
    public static <T> T pickRandom (Collection<T> values)
    {
        return pickRandom(values, rand);
    }

    /**
     * Picks a random object from the supplied {@link Collection}.
     *
     * @param r the random number generator to use.
     */
    public static <T> T pickRandom (Collection<T> values, Random r)
    {
        return pickRandom(values.iterator(), values.size(), r);
    }

    /**
     * Picks a random object from the supplied List
     *
     * @return a randomly selected item.
     */
    public static <T> T pickRandom (List<T> values)
    {
        int size = values.size();
        if (size == 0) {
            throw new IllegalArgumentException(
                "Must have at least one element [size=" + size + "]");
        }
        return values.get(getInt(size));
    }

    /**
     * Picks a random object from the supplied List. The specified skip object will be skipped when
     * selecting a random value. The skipped object must exist exactly once in the List.
     *
     * @return a randomly selected item.
     */
    public static <T> T pickRandom (List<T> values, T skip)
    {
        return pickRandom(values, skip, rand);
    }

    /**
     * Picks a random object from the supplied List. The specified skip object will be skipped when
     * selecting a random value. The skipped object must exist exactly once in the List.
     *
     * @param r the random number generator to use.
     *
     * @return a randomly selected item.
     */
    public static <T> T pickRandom (List<T> values, T skip, Random r)
    {
        int size = values.size();
        if (size < 2) {
            throw new IllegalArgumentException(
                "Must have at least two elements [size=" + size + "]");
        }

        int pick = r.nextInt(size - 1);
        for (int ii = 0; ii < size; ii++) {
            T val = values.get(ii);
            if ((val != skip) && (pick-- == 0)) {
                return val;
            }
        }
        return null;
    }

    /**
     * Picks a random object from the supplied iterator (which must iterate over exactly
     * <code>count</code> objects.
     *
     * @return a randomly selected item.
     *
     * @exception NoSuchElementException thrown if the iterator provides fewer than
     * <code>count</code> elements.
     */
    public static <T> T pickRandom (Iterator<T> iter, int count)
    {
        return pickRandom(iter, count, rand);
    }

    /**
     * Picks a random object from the supplied iterator (which must iterate over exactly
     * <code>count</code> objects using the given Random.
     *
     * @param r the random number generator to use.
     *
     * @return a randomly selected item.
     *
     * @exception NoSuchElementException thrown if the iterator provides fewer than
     * <code>count</code> elements.
     */
    public static <T> T pickRandom (Iterator<T> iter, int count, Random r)
    {
        if (count < 1) {
            throw new IllegalArgumentException(
                "Must have at least one element [count=" + count + "]");
        }

        for (int ii = 0, ll = getInt(count, r); ii < ll; ii++) {
            iter.next();
        }
        return iter.next();
    }

    /**
     * Picks a random object from the supplied iterator (which must iterate over exactly
     * <code>count</code> objects. The specified skip object will be skipped when selecting a
     * random value. The skipped object must exist exactly once in the set of objects returned by
     * the iterator.
     *
     * @return a randomly selected item.
     *
     * @exception NoSuchElementException thrown if the iterator provides fewer than
     * <code>count</code> elements.
     */
    public static <T> T pickRandom (Iterator<T> iter, int count, T skip)
    {
        if (count < 2) {
            throw new IllegalArgumentException(
                "Must have at least two elements [count=" + count + "]");
        }

        int index = getInt(count-1);
        T value = null;
        do {
            value = iter.next();
            if (value == skip) {
                value = iter.next();
            }
        } while (index-- > 0);
        return value;
    }
}
