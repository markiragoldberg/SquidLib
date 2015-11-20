package squidpony.squidgrid.mapping;

import squidpony.squidmath.Coord;
import squidpony.squidmath.PoissonDisk;
import squidpony.squidmath.RNG;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A variant on {@link MixedGenerator} that creates bi-radially symmetric maps (basically a yin-yang shape). Useful for
 * strategy games and possibly competitive multi-player games. The Coords passed to constructors as room positions do
 * not necessarily need to be
 * Created by Tommy Ettinger on 11/20/2015.
 */
public class SymmetryDungeonGenerator extends MixedGenerator {

    public static LinkedHashMap<Coord, List<Coord>> removeSomeOverlap(int width, int height, List<Coord> sequence)
    {
        List<Coord> s2 = new ArrayList<Coord>(sequence.size());
        for(Coord c : sequence)
        {
            if(c.x * 1.0 / width + c.y * 1.0 / height <= 1.0)
                s2.add(c);
        }
        return listToMap(s2);
    }
    public static LinkedHashMap<Coord, List<Coord>> removeSomeOverlap(int width, int height, LinkedHashMap<Coord, List<Coord>> connections)
    {
        LinkedHashMap<Coord, List<Coord>> lhm2 = new LinkedHashMap<Coord, List<Coord>>(connections.size());
        for(Coord c : connections.keySet()) {
            if (c.x * 1.0 / width + c.y * 1.0 / height <= 1.0) {
                List<Coord> cs = new ArrayList<Coord>(4);
                for (Coord c2 : connections.get(c))
                {
                    if (c2.x * 1.0 / width + c2.y * 1.0 / height <= 1.0) {
                        cs.add(c2);
                    }
                }
                lhm2.put(c, cs);
            }
        }
        return lhm2;
    }
    /**
     * This prepares a map generator that will generate a map with the given width and height, using the given RNG.
     * This version of the constructor uses Poisson Disk sampling to generate the points it will draw caves and
     * corridors between, ensuring a minimum distance between points, but it does not ensure that paths between points
     * will avoid overlapping with rooms or other paths. You call the different carver-adding methods to affect what the
     * dungeon will look like, putCaveCarvers(), putBoxRoomCarvers(), and putRoundRoomCarvers(), defaulting to only
     * caves if none are called. You call generate() after adding carvers, which returns a char[][] for a map.
     *
     * @param width  the width of the final map in cells
     * @param height the height of the final map in cells
     * @param rng    an RNG object to use for random choices; this make a lot of random choices.
     * @see PoissonDisk used to ensure spacing for the points.
     */
    public SymmetryDungeonGenerator(int width, int height, RNG rng) {
        this(width, height, rng, basicPoints(width, height, rng));
    }

    /**
     * This prepares a map generator that will generate a map with the given width and height, using the given RNG.
     * This version of the constructor uses a List of Coord points from some other source to determine the path to add
     * rooms or caves to and then connect. You call the different carver-adding methods to affect what the
     * dungeon will look like, putCaveCarvers(), putBoxRoomCarvers(), and putRoundRoomCarvers(), defaulting to only
     * caves if none are called. You call generate() after adding carvers, which returns a char[][] for a map.
     *
     * @param width    the width of the final map in cells
     * @param height   the height of the final map in cells
     * @param rng      an RNG object to use for random choices; this make a lot of random choices.
     * @param sequence a List of Coord to connect in order; index 0 is the start, index size() - 1 is the end.
     * @see SerpentMapGenerator a class that uses this technique
     */
    public SymmetryDungeonGenerator(int width, int height, RNG rng, List<Coord> sequence) {
        this(width, height, rng, listToMap((sequence)), 1f);
    }

    /**
     * This prepares a map generator that will generate a map with the given width and height, using the given RNG.
     * This version of the constructor uses a LinkedHashMap with Coord keys and Coord array values to determine a
     * branching path for the dungeon to take; each key will connect once to each of the Coords in its value, and you
     * usually don't want to connect in both directions. You call the different carver-adding methods to affect what the
     * dungeon will look like, putCaveCarvers(), putBoxRoomCarvers(), and putRoundRoomCarvers(), defaulting to only
     * caves if none are called. You call generate() after adding carvers, which returns a char[][] for a map.
     *
     * @param width       the width of the final map in cells
     * @param height      the height of the final map in cells
     * @param rng         an RNG object to use for random choices; this make a lot of random choices.
     * @param connections a Map of Coord keys to arrays of Coord to connect to next; shouldn't connect both ways
     * @see SerpentMapGenerator a class that uses this technique
     */
    public SymmetryDungeonGenerator(int width, int height, RNG rng, LinkedHashMap<Coord, List<Coord>> connections) {
        this(width, height, rng, connections, 0.8f);
    }

    /**
     * This prepares a map generator that will generate a map with the given width and height, using the given RNG.
     * This version of the constructor uses a LinkedHashMap with Coord keys and Coord array values to determine a
     * branching path for the dungeon to take; each key will connect once to each of the Coords in its value, and you
     * usually don't want to connect in both directions. You call the different carver-adding methods to affect what the
     * dungeon will look like, putCaveCarvers(), putBoxRoomCarvers(), and putRoundRoomCarvers(), defaulting to only
     * caves if none are called. You call generate() after adding carvers, which returns a char[][] for a map.
     *
     * @param width              the width of the final map in cells
     * @param height             the height of the final map in cells
     * @param rng                an RNG object to use for random choices; this make a lot of random choices.
     * @param connections        a Map of Coord keys to arrays of Coord to connect to next; shouldn't connect both ways
     * @param roomSizeMultiplier a float multiplier that will be applied to each room's width and height
     * @see SerpentMapGenerator a class that uses this technique
     */
    public SymmetryDungeonGenerator(int width, int height, RNG rng, LinkedHashMap<Coord, List<Coord>> connections, float roomSizeMultiplier) {
        super(width, height, rng, crossConnect(width, height, connections), roomSizeMultiplier);
    }

    protected static LinkedHashMap<Coord, List<Coord>> listToMap(List<Coord> sequence)
    {
        LinkedHashMap<Coord, List<Coord>> conns = new LinkedHashMap<Coord, List<Coord>>(sequence.size() - 1);
        for (int i = 0; i < sequence.size() - 1; i++) {
            Coord c1 = sequence.get(i), c2 = sequence.get(i+1);
            List<Coord> cs = new ArrayList<Coord>(1);
            cs.add(c2);
            conns.put(c1, cs);
        }
        return conns;
    }

    protected static LinkedHashMap<Coord, List<Coord>> crossConnect(int width, int height, LinkedHashMap<Coord, List<Coord>> connections)
    {
        LinkedHashMap<Coord, List<Coord>> conns = new LinkedHashMap<Coord, List<Coord>>(connections.size());
        for(Map.Entry<Coord, List<Coord>> entry : connections.entrySet())
        {
            conns.put(entry.getKey(), new ArrayList<Coord>(entry.getValue()));
        }
        double lowest1 = 9999, lowest2 = 9999, lowest3 = 9999, lowest4 = 9999;
        Coord l1 = null, l2 = null, l3 = null, l4 = null, r1 = null, r2 = null, r3 = null, r4 = null;
        for(List<Coord> left : connections.values())
        {
            for(List<Coord> right : connections.values())
            {
                for(Coord lc : left)
                {
                    for(Coord rc : right)
                    {
                        Coord rc2 = Coord.get(width - 1 - rc.x, height - 1 - rc.y);
                        double dist = lc.distance(rc2);
                        if(dist < 0.001)
                            continue;
                        if(dist < lowest1)
                        {
                            lowest1 = dist;
                            l1 = lc;
                            r1 = rc2;
                        }
                        else if(dist < lowest2)
                        {
                            lowest2 = dist;
                            l2 = lc;
                            r2 = rc2;
                        }
                        else if(dist < lowest3)
                        {
                            lowest3 = dist;
                            l3 = lc;
                            r3 = rc2;
                        }
                        else if(dist < lowest4)
                        {
                            lowest4 = dist;
                            l4 = lc;
                            r4 = rc2;
                        }
                    }
                }
            }
        }
        if(l1 != null && r1 != null)
        {
            if(conns.containsKey(l1))
            {
                conns.get(l1).add(r1);
            }
            else if(conns.containsKey(r1))
            {
                conns.get(r1).add(l1);
            }
        }
        if(l2 != null && r2 != null)
        {
            if(conns.containsKey(l2))
            {
                conns.get(l2).add(r2);
            }
            else if(conns.containsKey(r2))
            {
                conns.get(r2).add(l2);
            }
        }
        if(l3 != null && r3 != null)
        {
            if(conns.containsKey(l3))
            {
                conns.get(l3).add(r3);
            }
            else if(conns.containsKey(r3))
            {
                conns.get(r3).add(l3);
            }
        }
        if(l4 != null && r4 != null)
        {
            if(conns.containsKey(l4))
            {
                conns.get(l4).add(r4);
            }
            else if(conns.containsKey(r4))
            {
                conns.get(r4).add(l4);
            }
        }
        return conns;
    }
    /**
     * Internal use. Marks a point to be made into floor.
     *
     * @param x x position to mark
     * @param y y position to mark
     * @return false if everything is normal, true if and only if this failed to mark because the position is walled
     */
    @Override
    protected boolean mark(int x, int y) {
        return super.mark(x, y) || super.mark(width - 1 - x, height - 1 - y);
    }

    /**
     * Internal use. Marks a point to be made into floor.
     *
     * @param x x position to mark
     * @param y y position to mark
     */
    @Override
    protected void markPiercing(int x, int y) {
        super.markPiercing(x, y);
        super.markPiercing(width - 1 - x, height - 1 - y);
    }

    /**
     * Internal use. Marks a point to be made into floor.
     *
     * @param x x position to mark
     * @param y y position to mark
     */
    @Override
    protected void wallOff(int x, int y) {
        super.wallOff(x, y);
        super.wallOff(width - 1 - x, height - 1 - y);
    }
}
