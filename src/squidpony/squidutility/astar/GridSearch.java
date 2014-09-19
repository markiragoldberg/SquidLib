package squidpony.squidutility.astar;

import java.awt.Point;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Queue;
import squidpony.squidgrid.util.DirectionIntercardinal;

/**
 * Performs A* search.
 *
 * A* is a best-first search algorithm for pathfinding. It uses a heuristic value to reduce the total search space. If
 * the heuristic is too large then the optimal path is not guaranteed to be returned.
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
public class GridSearch {

    /**
     * The type of heuristic to use.
     */
    public enum SearchType {

        /**
         * The distance it takes when only the four primary directions can be moved in.
         */
        MANHATTAN,
        /**
         * The distance it takes when diagonal movement costs the same as cardinal movement.
         */
        CHEBYSHEV,
        /**
         * The distance it takes as the crow flies.
         */
        EUCLIDIAN,
        /**
         * Full space search. Least efficient but guaranteed to return a path if one exists.
         */
        DJIKSTRA
    };

    private final double[][] map;
    private final HashSet<Point> open = new HashSet<>();
    private final int width, height;
    private boolean[][] finished;
    private Point[][] parent;
    private Point start, target;
    private SearchType type;

    public GridSearch(double[][] map, SearchType type) {
        this.map = map;
        width = map.length;
        height = map[0].length;
        this.type = type;
    }

    /**
     * Finds an A* path to the target from the start. If no path is possible, returns null.
     *
     * @param startx the x coordinate of the start location
     * @param starty the y coordinate of the start location
     * @param targetx the x coordinate of the target location
     * @param targety the y coordinate of the target location
     * @return the shortest path, or null
     */
    public Queue<Point> path(int startx, int starty, int targetx, int targety) {
        start = new Point(startx, starty);
        target = new Point(targetx, targety);
        open.clear();
        finished = new boolean[width][height];
        parent = new Point[width][height];

        Point p = start;
        open.add(p);

        while (!p.equals(target)) {
            finished[p.x][p.y] = true;
            open.remove(p);
            for (DirectionIntercardinal dir : DirectionIntercardinal.OUTWARDS) {

                int x = p.x + dir.deltaX;
                if (x < 0 || x >= width) {
                    continue;//out of bounds so skip ahead
                }

                int y = p.y + dir.deltaY;
                if (y < 0 || y >= height) {
                    continue;//out of bounds so skip ahead
                }

                if (!finished[x][y]) {
                    Point test = new Point(x, y);
                    if (open.contains(test)) {
                        if (parent[x][y] == null || g(parent[x][y].x, parent[x][y].y) > g(p.x, p.y)) {
                            parent[x][y] = new Point(p);
                        }
                    } else {
                        open.add(test);
                        parent[x][y] = new Point(p);
                    }
                }
            }
            p = smallestF();
            if (p == null) {
                return null;//no path possible
            }
        }

        Deque<Point> deq = new ArrayDeque<>();
        while (!p.equals(start)) {
            deq.offerFirst(p);
            p = parent[p.x][p.y];
        }
        return deq;
    }

    /**
     * Finds the g value for the given location.
     *
     * If the given location is not valid or not attached to the pathfinding then Double.MAX_VALUE is returned.
     *
     * @param x coordinate
     * @param y coordinate
     */
    private double g(int x, int y) {
        if (x == start.x && y == start.y) {
            return 0;
        }
        if (x < 0 || y < 0 || x >= width || y >= height || parent[x][y] == null) {
            return Double.MAX_VALUE;
        }

        return map[x][y] + g(parent[x][y].x, parent[x][y].y) + 1;//follow path back to start
    }

    /**
     * Returns the distance to the goal location using the current calculation type.
     *
     * @param x coordinate
     * @param y coordinate
     * @return distance
     */
    private double h(int x, int y) {
        switch (type) {
            case MANHATTAN:
                return Math.abs(x - target.x) + Math.abs(y - target.y);//Manhattan
            case CHEBYSHEV:
                return Math.max(Math.abs(x - target.x), Math.abs(y - target.y));//Chebyshev
            case EUCLIDIAN:
                int xDist = Math.abs(x - target.x);
                xDist *= xDist;
                int yDist = Math.abs(y - target.y);
                yDist *= yDist;
                return Math.sqrt(xDist + yDist);
            case DJIKSTRA:
            default:
                return 0;

        }
    }

    private double f(int x, int y) {
        return h(x, y) + g(x, y);
    }

    /**
     * @return the current open point with the smallest F
     */
    private Point smallestF() {
        Point smallest = null;
        double smallF = Double.MAX_VALUE;
        for (Point p : open) {
            double f = f(p.x, p.y);
            if (smallest == null || f < smallF) {
                smallest = p;
                smallF = f(p.x, p.y);
            }
        }

        return smallest;
    }
}
