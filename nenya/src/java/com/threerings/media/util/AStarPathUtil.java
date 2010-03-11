//
// $Id: AStarPathUtil.java 868 2010-01-04 21:47:34Z dhoover $
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

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import java.awt.Point;

import com.google.common.collect.Lists;

import com.samskivert.util.HashIntMap;

/**
 * The <code>AStarPathUtil</code> class provides a facility for finding a reasonable path
 * between two points in a scene using the A* search algorithm.
 *
 * <p> See the path-finding article on <a
 * href="http://www.gamasutra.com/features/19990212/sm_01.htm">Gamasutra</a> for more detailed
 * information.
 */
public class AStarPathUtil
{
    /**
     * Provides traversibility information when computing paths.
     */
    public static interface TraversalPred
    {
        /**
         * Requests to know if the specified traverser (which was provided in the call to
         * {@link #getPath(TraversalPred,Object,int,int,int,int,int,boolean)}) can traverse the
         * specified tile coordinate.
         */
        public boolean canTraverse (Object traverser, int x, int y);
    }

    /**
     * Provides extended traversibility information when computing paths.
     */
    public static interface ExtendedTraversalPred extends TraversalPred
    {
        /**
         * Requests to know if the specific traverser (which was provided in the call to
         * {@link #getPath(TraversalPred,Object,int,int,int,int,int,boolean)}) can traverse from
         * the specified source tile coordinate to the specified destination tile coordinate.
         */
        public boolean canTraverse (Object traverser, int sx, int sy, int dx, int dy);
    }

    /**
     * Considers all the possible steps the piece in question can take.
     */
    public static class Stepper
    {
        public void init (Info info, Node n)
        {
            _info = info;
            _node = n;
        }

        /**
         * Should call {@link #considerStep} in turn on all possible steps from the specified
         * coordinates. No checking must be done as to whether the step is legal, that will be
         * handled later. Just enumerate all possible steps.
         */
        public void considerSteps (int x, int y)
        {
            considerStep(x - 1, y - 1, DIAGONAL_COST);
            considerStep(x, y - 1, ADJACENT_COST);
            considerStep(x + 1, y - 1, DIAGONAL_COST);
            considerStep(x - 1, y, ADJACENT_COST);
            considerStep(x + 1, y, ADJACENT_COST);
            considerStep(x - 1, y + 1, DIAGONAL_COST);
            considerStep(x, y + 1, ADJACENT_COST);
            considerStep(x + 1, y + 1, DIAGONAL_COST);
        }

        protected void considerStep (int x, int y, int cost)
        {
            AStarPathUtil.considerStep(_info, _node, x, y, cost);
        }

        protected Info _info;
        protected Node _node;
    }

    /** The standard cost to move between nodes. */
    public static final int ADJACENT_COST = 10;

    /** The cost to move diagonally. */
    public static final int DIAGONAL_COST = (int)Math.sqrt(
        (ADJACENT_COST * ADJACENT_COST) * 2);

    /**
     * Return a list of <code>Point</code> objects representing a path from coordinates
     * <code>(ax, by)</code> to <code>(bx, by)</code>, inclusive, determined by performing an
     * A* search in the given scene's base tile layer. Assumes the starting and destination nodes
     * are traversable by the specified traverser.
     *
     * @param tpred lets us know what tiles are traversible.
     * @param stepper enumerates the possible steps.
     * @param trav the traverser to follow the path.
     * @param longest the longest allowable path in tile traversals.
     * @param ax the starting x-position in tile coordinates.
     * @param ay the starting y-position in tile coordinates.
     * @param bx the ending x-position in tile coordinates.
     * @param by the ending y-position in tile coordinates.
     * @param partial if true, a partial path will be returned that gets us as close as we can to
     * the goal in the event that a complete path cannot be located.
     *
     * @return the list of points in the path.
     */
    public static List<Point> getPath (
            TraversalPred tpred, Stepper stepper, Object trav, int longest,
            int ax, int ay, int bx, int by, boolean partial)
    {
        Info info = new Info(tpred, trav, longest, bx, by);

        // set up the starting node
        Node s = info.getNode(ax, ay);
        s.g = 0;
        s.h = getDistanceEstimate(ax, ay, bx, by);
        s.f = s.g + s.h;

        // push starting node on the open list
        info.open.add(s);
        _considered = 1;

        // track the best path
        float bestdist = Float.MAX_VALUE;
        Node bestpath = null;

        // while there are more nodes on the open list
        while (info.open.size() > 0) {

            // pop the best node so far from open
            Node n = info.open.first();
            info.open.remove(n);

            // if node is a goal node
            if (n.x == bx && n.y == by) {
                // construct and return the acceptable path
                return getNodePath(n);

            } else if (partial) {
                float pathdist = MathUtil.distance(n.x, n.y, bx, by);
                if (pathdist < bestdist) {
                    bestdist = pathdist;
                    bestpath = n;
                }
            }

            // consider each successor of the node
            stepper.init(info, n);
            stepper.considerSteps(n.x, n.y);

            // push the node on the closed list
            info.closed.add(n);
        }

        // return the best path we could find if we were asked to do so
        if (bestpath != null) {
            return getNodePath(bestpath);
        }

            // no path found
            return null;
    }

    /**
     * Gets a path with the default stepper which assumes the piece can move one in any of the
     * eight cardinal directions.
     */
    public static List<Point> getPath (
            TraversalPred tpred, Object trav, int longest,
            int ax, int ay, int bx, int by, boolean partial)
    {
        return getPath(
            tpred, new Stepper(), trav, longest, ax, ay, bx, by, partial);
    }

    /**
     * Returns the number of nodes considered in computing the most recent path.
     */
    public static int getConsidered ()
    {
        return _considered;
    }

    /**
     * Consider the step <code>(n.x, n.y)</code> to <code>(x, y)</code> for possible inclusion
     * in the path.
     *
     * @param info the info object.
     * @param n the originating node for the step.
     * @param x the x-coordinate for the destination step.
     * @param y the y-coordinate for the destination step.
     */
    protected static void considerStep (Info info, Node n, int x, int y, int cost)
    {
        // skip node if it's outside the map bounds or otherwise impassable
        if (!info.isStepValid(n.x, n.y, x, y)) {
            return;
        }

        // calculate the new cost for this node
        int newg = n.g + cost;

            // make sure the cost is reasonable
            if (newg > info.maxcost) {
    //            Log.info("Rejected costly step.");
                return;
            }

        // retrieve the node corresponding to this location
        Node np = info.getNode(x, y);

        // skip if it's already in the open or closed list or if its
        // actual cost is less than the just-calculated cost
        if ((info.open.contains(np) || info.closed.contains(np)) &&
            np.g <= newg) {
            return;
        }

        // remove the node from the open list since we're about to
        // modify its score which determines its placement in the list
        info.open.remove(np);

        // update the node's information
        np.parent = n;
        np.g = newg;
        np.h = getDistanceEstimate(np.x, np.y, info.destx, info.desty);
        np.f = np.g + np.h;

        // remove it from the closed list if it's present
        info.closed.remove(np);

        // add it to the open list for further consideration
        info.open.add(np);
        _considered++;
    }

    /**
     * Return a list of <code>Point</code> objects detailing the path from the first node (the
     * given node's ultimate parent) to the ending node (the given node itself.)
     *
     * @param n the ending node in the path.
     *
     * @return the list detailing the path.
     */
    protected static List<Point> getNodePath (Node n)
    {
        Node cur = n;
        ArrayList<Point> path = Lists.newArrayList();

        while (cur != null) {
            // add to the head of the list since we're traversing from
            // the end to the beginning
            path.add(0, new Point(cur.x, cur.y));

            // advance to the next node in the path
            cur = cur.parent;
        }

        return path;
    }

    /**
     * Return a heuristic estimate of the cost to get from <code>(ax, ay)</code> to
     * <code>(bx, by)</code>.
     */
    protected static int getDistanceEstimate (int ax, int ay, int bx, int by)
    {
        // we're doing all of our cost calculations based on geometric distance times ten
        int xsq = bx - ax;
        int ysq = by - ay;
        return (int) (ADJACENT_COST * Math.sqrt(xsq * xsq + ysq * ysq));
    }

    /**
     * A holding class to contain the wealth of information referenced
     * while performing an A* search for a path through a tile array.
     */
    protected static class Info
    {
        /** Knows whether or not tiles are traversable. */
        public TraversalPred tpred;

        /** The tile array dimensions. */
        public int tilewid, tilehei;

        /** The traverser moving along the path. */
        public Object trav;

        /** The set of open nodes being searched. */
        public SortedSet<Node> open;

        /** The set of closed nodes being searched. */
        public ArrayList<Node> closed;

        /** The destination coordinates in the tile array. */
        public int destx, desty;

        /** The maximum cost of any path that we'll consider. */
        public int maxcost;

        public Info (TraversalPred tpred, Object trav,
                     int longest, int destx, int desty)
        {
            // save off references
            this.tpred = tpred;
            this.trav = trav;
            this.destx = destx;
            this.desty = desty;

            // compute our maximum path cost
            this.maxcost = longest * ADJACENT_COST;

            // construct the open and closed lists
            open = new TreeSet<Node>();
            closed = Lists.newArrayList();
        }

        /**
         * Returns whether moving from the given source to destination coordinates is a valid
         * move.
         */
        protected boolean isStepValid (int sx, int sy, int dx, int dy)
        {
            // not traversable if the destination itself fails test
            if (tpred instanceof ExtendedTraversalPred) {
                if (!((ExtendedTraversalPred)tpred).canTraverse(
                        trav, sx, sy, dx, dy)) {
                    return false;
                }
            } else {
                if (!isTraversable(dx, dy)) {
                    return false;
                }
            }

            // if the step is diagonal, make sure the corners don't impede our progress
            if ((Math.abs(dx - sx) == 1) && (Math.abs(dy - sy) == 1)) {
                return isTraversable(dx, sy) && isTraversable(sx, dy);
            }

            // non-diagonals are always traversable
            return true;
        }

        /**
         * Returns whether the given coordinate is valid and traversable.
         */
        protected boolean isTraversable (int x, int y)
        {
            return tpred.canTraverse(trav, x, y);
        }

        /**
         * Get or create the node for the specified point.
         */
        public Node getNode (int x, int y)
        {
            // note: this _could_ break for unusual values of x and y.
            // perhaps use a IntTuple as a key? Bleah.
            int key = (x << 16) | (y & 0xffff);
            Node node = _nodes.get(key);
            if (node == null) {
                node = new Node(x, y);
                _nodes.put(key, node);
            }
            return node;
        }

        /** The nodes being considered in the path. */
        protected HashIntMap<Node> _nodes = new HashIntMap<Node>();
    }

    /**
     * A class that represents a single traversable node in the tile array
     * along with its current A*-specific search information.
     */
    protected static class Node implements Comparable<Node>
    {
        /** The node coordinates. */
        public int x, y;

        /** The actual cheapest cost of arriving here from the start. */
        public int g;

        /** The heuristic estimate of the cost to the goal from here. */
        public int h;

        /** The score assigned to this node. */
        public int f;

        /** The node from which we reached this node. */
        public Node parent;

        /** The node's monotonically-increasing unique identifier. */
        public int id;

        public Node (int x, int y)
        {
            this.x = x;
            this.y = y;
            id = _nextid++;
        }

        public int compareTo (Node o)
        {
            int bf = o.f;

            // since the set contract is fulfilled using the equality results returned here, and
            // we'd like to allow multiple nodes with equivalent scores in our set, we explicitly
            // define object equivalence as the result of object.equals(), else we use the unique
            // node id since it will return a consistent ordering for the objects.
            if (f == bf) {
                return (this == o) ? 0 : (id - o.id);
            }

            return f - bf;
        }

        /** The next unique node id. */
        protected static int _nextid = 0;
    }

    /** The number of nodes considered in computing our path. */
    protected static int _considered = 0;
}
