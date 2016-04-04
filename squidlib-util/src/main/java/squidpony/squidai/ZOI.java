package squidpony.squidai;

import squidpony.squidgrid.Direction;
import squidpony.squidgrid.Radius;
import squidpony.squidmath.Coord;
import squidpony.squidmath.CoordPacker;
import squidpony.squidmath.ShortVLA;

import java.util.*;

/**
 * Calculates the Zone of Influence, also known as Zone of Control, for different points on a map.
 * Uses CoordPacker for more efficient storage and manipulation of zones; it's recommended if you use this class to be
 * somewhat familiar with the methods for manipulating packed data in that class.
 * Created by Tommy Ettinger on 10/27/2015.
 */
public class ZOI {
    private char[][] map;
    private DijkstraMap dijkstra;
    private Coord[][] influences;
    private short[][] packedGroups;
    private boolean completed = false;
    private Radius radius;
    /**
     * Constructs a Zone of Influence map. Takes a (quite possibly jagged) array of arrays of Coord influences, where
     * the elements of the outer array represent different groups of influencing "factions" or groups that exert control
     * over nearby areas, and the Coord elements of the inner array represent individual spots that are part of those
     * groups and share influence with all Coord in the same inner array. Also takes a char[][] for a map, which can be
     * the simplified map with only '#' for walls and '.' for floors, or the final map (with chars like '~' for deep
     * water as well as walls and floors), and a Radius enum that will be used to determine how distance is calculated.
     * <br>
     * Call calculate() when you want information out of this.
     * @param influences an outer array containing influencing groups, each an array containing Coords that influence
     * @param map a char[][] that is used as a map; should be bounded
     * @param measurement a Radius enum that corresponds to how distance should be measured
     */
    public ZOI(Coord[][] influences, char[][] map, Radius measurement) {
        this.influences = influences;
        packedGroups = new short[influences.length][];
        this.map = map;
        radius = measurement;
        dijkstra = new DijkstraMap(map, DijkstraMap.findMeasurement(measurement));
    }
    /**
     * Constructs a Zone of Influence map. Takes an arrays of Coord influences, where each Coord is treated as both a
     * one-element group of influencing "factions" or groups that exert control over nearby areas, and the individual
     * spot that makes up one of those groups and spreads influence. Also takes a char[][] for a map, which can be the
     * simplified map with only '#' for walls and '.' for floors, or the final map (with chars like '~' for deep water
     * as well as walls and floors), and a Radius enum that will be used to determine how distance is calculated.
     * <br>
     * Essentially, this is the same as constructing a ZOI with a Coord[][] where each inner array has only one element.
     * <br>
     * Call calculate() when you want information out of this.
     * @param influences an array containing Coords that each have their own independent influence
     * @param map a char[][] that is used as a map; should be bounded
     * @param measurement a Radius enum that corresponds to how distance should be measured
     * @see squidpony.squidmath.PoissonDisk for a good way to generate evenly spaced Coords that can be used here
     */
    public ZOI(Coord[] influences, char[][] map, Radius measurement) {
        this.influences = new Coord[influences.length][];
        for (int i = 0; i < influences.length; i++) {
            this.influences[i] = new Coord[] { influences[i] };
        }
        packedGroups = new short[influences.length][];
        this.map = map;
        radius = measurement;
        dijkstra = new DijkstraMap(map, DijkstraMap.findMeasurement(measurement));
    }

    /**
     * Finds the zones of influence for each of the influences (inner arrays of Coord) this was constructed with, and
     * returns them as packed data (using CoordPacker, which can also be used to unpack the data, merge zones, get
     * shared borders, and all sorts of other tricks). This has each zone of influence overlap with its neighbors; this
     * is useful to find borders using CoordPacker.intersectPacked(), and borders are typically between 1 and 2 cells
     * wide. You can get a different region as packed data if you want region A without the overlapping areas it shares
     * with region B by using {@code short[] different = CoordPacker.differencePacked(A, B)}. Merging two zones A and B
     * can be done with {@code short[] merged = CoordPacker.unionPacked(A, B)} . You can unpack the data
     * into a boolean[][] easily with CoordPacker.unpack(), where true is contained in the zone and false is not.
     * The CoordPacker methods fringe(), expand(), singleRandom(), randomSample(), and randomPortion() are also
     * potentially useful for this sort of data. You should save the short[][] for later use if you want to call
     * nearestInfluences() in this class.
     * <br>
     * The first short[] in the returned short[][] will correspond to the area influenced by the first Coord[] in the
     * nested array passed to the constructor (or the first Coord if a non-nested array was passed); the second will
     * correspond to the second, and so on. The length of the short[][] this returns will equal the number of influence
     * groups.
     * @return an array of short[] storing the zones' areas; each can be used as packed data with CoordPacker
     */
    public short[][] calculate()
    {
        for (int i = 0; i < influences.length; i++) {
            for (int j = 0; j < influences[i].length; j++) {
                dijkstra.setGoal(influences[i][j]);
            }
        }
        double[][] scannedAll = dijkstra.scan(null);

        for (int i = 0; i < influences.length; i++) {

            /*
            dijkstra.clearGoals();
            dijkstra.resetMap();
            for (int j = 0; j < influences[i].length; j++) {
                dijkstra.setGoal(influences[i][j]);
            }
            double[][] factionScanned = dijkstra.scan(null);
            for (int y = 0; y < map[0].length; y++) {
                for (int x = 0; x < map.length; x++) {
                    influenced[x][y] = (scannedAll[x][y] < DijkstraMap.FLOOR) &&
                            (factionScanned[x][y] - scannedAll[x][y] <= 1);
                }
            }*/
            packedGroups[i] = CoordPacker.pack(increasing(scannedAll, influences[i]));
        }
        completed = true;
        return packedGroups;
    }
    protected boolean[][] increasing(double[][] dm, Coord[] inf) {
        LinkedHashSet<Coord> open = new LinkedHashSet<>(64), fresh = new LinkedHashSet<>(64);
        Collections.addAll(open, inf);
        Direction[] dirs = (radius.equals2D(Radius.DIAMOND)) ? Direction.CARDINALS : Direction.OUTWARDS;
        boolean[][] influenced = new boolean[map.length][map[0].length];

        int numAssigned = open.size();
        double diff, h;
        while (numAssigned > 0) {
            numAssigned = 0;
            for (Coord cell : open) {
                influenced[cell.x][cell.y] = true;
                for (int d = 0; d < dirs.length; d++) {
                    Coord adj = cell.translate(dirs[d].deltaX, dirs[d].deltaY);
                    if (!open.contains(adj) && dm[adj.x][adj.y] < DijkstraMap.FLOOR && !influenced[adj.x][adj.y]) {
                        //h = heuristic(dirs[d]);
                        diff = dm[adj.x][adj.y] - dm[cell.x][cell.y];
                        if (diff <= 1.0 && diff >= 0) {
                            fresh.add(adj);
                            influenced[adj.x][adj.y] = true;
                            ++numAssigned;
                        }
                    }
                }
            }

            open = new LinkedHashSet<>(fresh);
            fresh.clear();
        }

        return influenced;
    }
    private static final double root2 = Math.sqrt(2.0);

    private double heuristic(Direction target) {
        switch (radius) {
            case CIRCLE:
            case SPHERE:
                switch (target) {
                    case DOWN_LEFT:
                    case DOWN_RIGHT:
                    case UP_LEFT:
                    case UP_RIGHT:
                        return root2;
                    default:
                        return 1.0;
                }
            default: return 1.0;
        }
    }

    /**
     * Given the zones resulting from this class' calculate method and a Coord to check, finds the indices of all
     * influencing groups in zones that have the Coord in their area, and returns all such indices as an int array.
     * @param zones a short[][] returned by calculate; not a multi-packed short[][] from CoordPacker !
     * @param point the Coord to test
     * @return an int[] where each element is the index of an influencing group in zones
     */
    public int[] nearestInfluences(short[][] zones, Coord point)
    {
        ShortVLA found = new ShortVLA(4);
        for (short i = 0; i < zones.length; i++) {
            if(CoordPacker.queryPacked(zones[i], point.x, point.y))
                found.add(i);
        }
        return found.asInts();
    }
    /**
     * This can be given a Coord to check in the results of the latest calculate() call. Finds the indices of all
     * influencing groups in zones that have the Coord in their area, and returns all such indices as an int array.
     * @param point the Coord to test
     * @return an int[] where each element is the index of an influencing group in zones
     */
    public int[] nearestInfluences(Coord point)
    {
        if(!completed)
            return new int[0];
        ShortVLA found = new ShortVLA(4);
        for (short i = 0; i < packedGroups.length; i++) {
            if(CoordPacker.queryPacked(packedGroups[i], point.x, point.y))
                found.add(i);
        }
        return found.asInts();
    }

    /**
     * Gets the influencing groups; ideally the result should not be changed without setting it back with setInfluences.
     * @return influences a jagged array of Coord arrays, where the inner arrays are groups of influences
     */
    public Coord[][] getInfluences() {
        return influences;
    }

    /**
     * Changes the influencing groups. This also invalidates the last calculation for the purposes of nearestInfluences,
     * at least for the overload that takes only a Coord.
     * @param influences a jagged array of Coord arrays, where the inner arrays are groups of influences
     */
    public void setInfluences(Coord[][] influences) {
        this.influences = influences;
        packedGroups = new short[influences.length][];
        completed = false;
    }
}
