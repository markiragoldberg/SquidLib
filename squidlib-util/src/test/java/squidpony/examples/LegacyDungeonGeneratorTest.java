package squidpony.examples;
import squidpony.squidgrid.mapping.DungeonGenerator;
import squidpony.squidgrid.mapping.DungeonUtility;
import squidpony.squidgrid.mapping.styled.TilesetType;
import squidpony.squidmath.LightRNG;
import squidpony.squidmath.RNG;
import squidpony.squidmath.StatefulRNG;

import java.io.FileWriter;
import java.io.IOException;

/**
 * Sample output: {@code
 * ┌─────────────────┐     ┌─────────────┐   ┌─────┐ ┌───────┐   ┌─────┐
 * │ ~ ~ ~ ~ ~ ~ ~ ~ └─┐   │ . . . . . . └───┤ . . │ │ . . . │   │ . . │
 * │ ~ ~ ~ ~ ~ ~ ~ ~ ~ │   │ . . . . . . . . │ . . │ │ . . ──┴─┬─┘ . . │
 * │ ~ ~ ~ ~ ~ ~ ~ ~ ~ │ ┌─┘ . . . . . . . . │ . . │ │ . . . . │ . . . │
 * │ ~ ~ ~ ┌───┐ ~ ~ ~ │ │ . . . . . . . │ . │ . . └─┘ . . . . . . . . │
 * │ ~ ~ ──┤   │ ~ ~ ~ │ │ . . . . . . . │ . . . . . . . ^ . . . . . . │
 * │ ~ ~ ~ └───┘ ~ ~ ~ │ │ . . . . . . . │ . . . ^ . . . . . . . . . ┌─┘
 * │ ~ ~ ~ ~ ~ ~ ~ ~ ~ │ │ . . . . . . . ├───────────┐ . . . . ┌─────┘
 * │ ~ . ~ ~ ~ ~ ~ ~ ┌─┘ │ . . . . . . . │           │ . . ┌───┘
 * └─┐ ~ ~ ~ ~ ~ ┌───┘   │ . . ────────┬─┘     ┌─────┘ . . │
 *   │ ~ ~ ~ ~ ~ │       │ . . . . . . │       │ . . . . . │     ┌─────┐
 *   │ ~ ~ ~ ┌───┴───┬───┴─┐ . . . . . └─┐   ┌─┘ . . . . . ├─────┘ . . │
 * ┌─┴─┐ ~ ~ │ ~ ~ ~ │ ~ ~ └─┐ . . . . . └───┘ . . . . . ┌─┘ . . . . . │
 * │ ~ │ ~ ~ │ ~ ~ ~ │ ~ ~ ~ └─┐ . . . . ~ . . . . . . ┌─┘ . . . . . . │
 * │ ~ │ ~ ~ │ ~ ~ ~ │ ~ ~ ~ ~ └─┬─┐ . ~ ~ . . . . . ┌─┘ . . . ┌─┐ . . │
 * │ ~ │ ~ ~ │ ~ ~ ~ │ ~ ~ ~ ~ ~ └─┼───┐ ~ ~ ┌─────┬─┘ . . . . │ └─────┘
 * │ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ └───┘ ~ ~ ├─────┘ . . . . . │
 * │ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ + . . . . . . . ┌─┘   ┌───┐
 * │ ~ ~ ~ ~ ~ ~ ~ ~ ┌───┐ ~ ~ ~ ~ ~ ~ ~ ~ ~ │ . . . . . . ┌─┘     │ . │
 * └─────────────────┘   │ ~ ~ ~ ~ ~ ┌───────┤ . . ──┬─────┤       │ . │
 * ┌───────────────┐     └───┐ ~ ~ ┌─┘       │ . . . + . . └───────┤ . │
 * │ . . . . . . . │         │ ~ ~ ├───────┐ │ . . . │ . . . . . . │ . │
 * │ . . . . . . . ├───┐     │ ~ ~ │ . . . └─┴───────┘ . . . . . . ├───┘
 * │ . . ┌─┐ . . . │ ~ │     │ ~ ~ │ . . . . . . . . . . . . . . . │
 * │ . . │ │ . . ~ . ~ └─────┤ ~ ~ │ ^ . . . . . . . . . . ┌───────┴───┐
 * │ . . │ │ ~ ~ ~ ~ ~ ~ ~ ~ │ ~ ~ │ . . . . . . ──────────┤ . . . . . │
 * │ . . │ │ ~ ~ ~ ~ ~ ~ ~ ~ │ ~ ~ │ ^ . . . . . . . . . . │ . . . . . │
 * └───┬─┴─┤ ~ ~ ──┐ ~ │ ~ ~ │ ~ ~ │ . . . . . . . . . . . │ . . . . . │
 *     │ ~ │ . . ~ ├───┤ ~ ~ │ ~ . │ . . ┌───┐ . . . . . . │ . . . . . │
 *     │ ~ + ~ ~ ~ │   │ ~ ~ + ~ ~ ~ ~ . │   │ . . . . . ┌─┴─────┐ ^ . │
 * ┌───┴───┴─┐ ~ ~ │   │ ~ ~ │ ~ ~ ~ ~ ~ │ ┌─┘ . . . . . │     ┌─┘ . . │
 * │ . . . . │ . ~ └───┼─────┤ ~ . ┌─────┘ │ . . . . . . │     │ . . . │
 * │ . . . . . . ~ . . │ . . └─┬───┘ ┌─────┤ . . . . . . │   ┌─┘ . . . │
 * └─┐ . . . . . ~ ~ . + . . . └───┐ │ . . ├───────┐ . . │   │ . . . . │
 *   │ . . . . . ~ . . │ . . . . . └─┤ . . ├───────┤ . . │   │ . . . . │
 *   │ . . . . . . . . ├─┐ . . . . . │ . . │ . ~ ~ │ . . └───┤ . . . ──┤
 *   │ . . . . . . . . │ └─┐ . . . . . . . + . ~ ~ │ . . . . + . . . . │
 *   │ . . ┌───────────┘   └─┐ . . . . . . │ . . . │ . . . . │ . . . . │
 *   │ . . │                 └─┐ . . . . ┌─┤ . . . └─────. . ├─┐ . . . │
 *   │ . . │               ┌───┴───. . ┌─┘ │ . . . . . . . . │ └─┬─────┤
 *   │ . . ├─────────┬─────┤ . . . . ^ │   │ . . . . . . . . │   │ . . │
 *   │ . . │ . . . . │ ~ ~ │ . . . . . └───┴─────┬─┐ . . . . │   │ . . │
 *   │ . . │ . . . . │ ~ ~ │ . . . . . . . . . . │ │ . ^ . . ├───┤ . . │
 *   │ . . │ . . . . │ ~ ~ │ . . . . . . . . . . │ │ . . . . │ . │ . . │
 *   │ . . │ . . . . │ ~ ~ │ . . . . . . ┌─┬───/ ┤ └───┬─────┘ . └─/ ──┤
 * ┌─┘ . . └─────. . │ ~ ~ │ . . . . . . │ │ . . │     │ . . . . . . . │
 * │ . . . . . . . . │ ~ ~ └─────────. . │ │ . ^ └─────┘ . . . . . . . │
 * │ . . . . . . . . │ ~ ~ ~ ~ ~ ~ ~ ~ ~ │ │ . . . . . . . . │ . . . . │
 * └─┐ . . . . . . . │ ~ ~ ~ ~ ~ ~ ~ ~ ~ │ │ . . ^ . . . . . ├─────────┘
 *   │ . . . . . ┌───┴───┐ ~ ~ ──┬───────┘ └───┐ . . . . . ┌─┘   ┌─────┐
 *   └───┐ . . ┌─┘       ├───~ ~ │             │ . . . . . │     │ . . │
 *       │ . . │       ┌─┘ ~ ~ ~ ├───────────┬─┴───. . . . └─────┘ . . │
 * ┌─────┤ . . └─┐     │ . ~ ~ ~ │ . . . . . │ . . . . . . . . . . . . │
 * │ . . │ . . . │     │ . . ~ ┌─┤ . . . . . + . . . . . . . . . . . . │
 * │ . . + . . . │     │ . . ┌─┘ │ . . │ . . │ . . ┌─┐ . . ┌─────┐ . . │
 * │ . . ├─┐ . . └─────┘ . . └───┘ . . │ . . │ . . │ │ . ^ │     │ . . │
 * │ . . │ │ . . . . . . . . . . . . . ├─────┘ . . └─┘ . . │     │ . . │
 * │ . . │ │ . ^ . . . . ^ ^ . . . . . │ . . . . . . . . . │     │ ^ . │
 * └─────┘ └─────────────┐ . . . . . . + . . . . . . . . . │     │ . . │
 *                       └─────────────┴───────────────────┘     └─────┘
 * }
 * Created by Tommy Ettinger on 4/8/2015.
 * @author Tommy Ettinger - https://github.com/tommyettinger
 */
@SuppressWarnings( "deprecation" )
public class LegacyDungeonGeneratorTest {
    public static int width = 80, height = 80, depth = 16;
    public static void main( String[] args )
    {
        //seed is, in base 36, the number SQUIDLIB
        //new LightRNG(2252637788195L)
        StatefulRNG rng = new StatefulRNG();
        DungeonGenerator dungeonGenerator = new squidpony.squidgrid.mapping.LegacyDungeonGenerator(width, height, rng);

        /*
        String[][] tiles = new DungeonBoneGen().getTiles(TilesetType.DEFAULT_DUNGEON);
        for(String[] tile : tiles)
        {
            System.out.println("{");
            for(String row : tile)
            {
                System.out.println("\"" + row + "\",");
            }
            System.out.println("},");
        }
        */

        dungeonGenerator.addDoors(15, true);
        dungeonGenerator.addWater(15);
        dungeonGenerator.addGrass(10);
        dungeonGenerator.addBoulders(5);
        dungeonGenerator.addTraps(2);
        //MixedGenerator mix = new MixedGenerator(width, height, rng);
        //mix.putCaveCarvers(3);
        //mix.putWalledBoxRoomCarvers(2);
        //mix.putWalledRoundRoomCarvers(2);
        //dungeonGenerator.generate(mix.generate());
        dungeonGenerator.generate(TilesetType.DEFAULT_DUNGEON);
        char[][] dungeon = dungeonGenerator.getDungeon();
        int walls = DungeonUtility.countCells(dungeon, '#'), floors = dungeon.length * dungeon[0].length - walls,
                water = DungeonUtility.countCells(dungeon, '~') + DungeonUtility.countCells(dungeon, ','),
                grass = DungeonUtility.countCells(dungeon, '"');
        dungeon[dungeonGenerator.stairsUp.x][dungeonGenerator.stairsUp.y] = '<';
        dungeon[dungeonGenerator.stairsDown.x][dungeonGenerator.stairsDown.y] = '>';

        dungeonGenerator.setDungeon(DungeonUtility.doubleWidth(
                DungeonUtility.hashesToLines(dungeon, true)));
        System.out.println(dungeonGenerator);
        System.out.println("Water/Non-Wall: " + water + "/" + floors + ", or " + (water * 1.0 / floors));
        System.out.println("Grass/Non-Wall: " + grass + "/" + floors + ", or " + (grass * 1.0 / floors));

/*
        dungeonGenerator = new DungeonGenerator(width, height, rng);
        //dungeonGenerator.addDoors(15, false);
        //dungeonGenerator.addWater(20);
        //dungeonGenerator.addGrass(10);
        rng.setState(2252637788195L);
        SerpentMapGenerator serpent = new SerpentMapGenerator(width, height, rng, 0.2, true);
        serpent.putWalledBoxRoomCarvers(5);
        serpent.putWalledRoundRoomCarvers(4);
        serpent.putCaveCarvers(6);
        char[][] map = serpent.generate();
        dungeonGenerator.generate(map);

        char[][] sdungeon = dungeonGenerator.getDungeon();
        //sdungeon[dungeonGenerator.stairsUp.x][dungeonGenerator.stairsUp.y] = '<';
        //sdungeon[dungeonGenerator.stairsDown.x][dungeonGenerator.stairsDown.y] = '>';

        dungeonGenerator.setDungeon(DungeonUtility.doubleWidth(
                DungeonUtility.hashesToLines(sdungeon)));
        System.out.println(dungeonGenerator);
        */
        /*
        dungeonGenerator = new DungeonGenerator(width, height, rng);
        //dungeonGenerator.addDoors(15, false);
        //dungeonGenerator.addWater(20);
        //dungeonGenerator.addGrass(10);
        rng.setState(0xf00dd00dL);
        serpent = new SerpentMapGenerator(width, height, rng, 0.4, true);
        serpent.putWalledBoxRoomCarvers(2);
        serpent.putWalledRoundRoomCarvers(2);
        serpent.putCaveCarvers(3);
        map = serpent.generate();
        dungeonGenerator.generate(map);

        sdungeon = dungeonGenerator.getDungeon();
        sdungeon[dungeonGenerator.stairsUp.x][dungeonGenerator.stairsUp.y] = '<';
        sdungeon[dungeonGenerator.stairsDown.x][dungeonGenerator.stairsDown.y] = '>';

        dungeonGenerator.setDungeon(DungeonUtility.doubleWidth(
                DungeonUtility.hashesToLines(sdungeon)));
        System.out.println(dungeonGenerator);
        */
        /*
        dungeonGenerator = new LegacyDungeonGenerator(width, height, rng);
        //dungeonGenerator.addDoors(15, false);
        //dungeonGenerator.addWater(20);
        //dungeonGenerator.addGrass(10);
        rng.setState(0xf00dd00dL);
        LanesMapGenerator lanes = new LanesMapGenerator(width, height, rng, 3);
        lanes.putBoxRoomCarvers(1);
        dungeon = lanes.generate();
        char[][] sdungeon = dungeonGenerator.generate(dungeon);

        //sdungeon[dungeonGenerator.stairsUp.x][dungeonGenerator.stairsUp.y] = '<';
        //sdungeon[dungeonGenerator.stairsDown.x][dungeonGenerator.stairsDown.y] = '>';

        dungeonGenerator.setDungeon(DungeonUtility.doubleWidth(
                DungeonUtility.hashesToLines(sdungeon)));
        System.out.println(dungeonGenerator);
        */
/*
        System.out.println("------------------------------------------------------------");

        rng.setState(2252637788195L);
        SerpentDeepMapGenerator deepSerpent = new SerpentDeepMapGenerator(width, height, depth, rng, 0.15);
        deepSerpent.putWalledBoxRoomCarvers(2);
        deepSerpent.putWalledRoundRoomCarvers(2);
        deepSerpent.putCaveCarvers(3);
        char[][][] map3D = deepSerpent.generate();
        DungeonGenerator[] gens = new DungeonGenerator[depth];
        for (int i = 0; i < depth; i++) {
            gens[i] = new DungeonGenerator(width, height, rng);
            gens[i].addWater(rng.nextInt(25));
            gens[i].addGrass(rng.nextInt(15));
            gens[i].addBoulders(rng.nextInt(30));
            gens[i].addDoors(rng.between(4, 10), false);
            gens[i].generateRespectingStairs(map3D[i]);
            gens[i].setDungeon(DungeonUtility.doubleWidth(
                    DungeonUtility.hashesToLines(gens[i].getDungeon(), true)));
            System.out.println(gens[i]);
            System.out.print  ("------------------------------------------------------------");
            System.out.print  ("------------------------------------------------------------");
            System.out.print  ("------------------------------------------------------------");
            System.out.println("------------------------------------------------------------");

        }
*/
    }
    public static void mainAlt( String[] args ) {
        LightRNG rng = new LightRNG(2252637788195L);
        DungeonGenerator dungeonGenerator = new DungeonGenerator(120, 120, new RNG((rng)));
        dungeonGenerator.addDoors(15, true);
        dungeonGenerator.addWater(15);
        dungeonGenerator.addGrass(15);
        dungeonGenerator.addBoulders(8);
        dungeonGenerator.generate();

        dungeonGenerator.setDungeon(
                DungeonUtility.hashesToLines(dungeonGenerator.getDungeon(), true));
        char[][] iso = dungeonGenerator.getDungeon();
        int[][] water = new int[120][120];
        for (int i = 0; i < 120; i++)
        {
            for (int j = 0; j < 120; j++)
            {
                water[i][j] = -1;
            }
        }
        boolean even = true;
        StringBuilder sb = new StringBuilder(64000);
        for(int row = 0, sx = -59, sy = 59; row < 240; ++row, even = (row % 2 == 0), sx += (even) ? 1 : 0, sy += (!even) ? 1 : 0)
        {
            sb.append("<div class=\"row\">\n");
            for(int col = 0; col < 120; col++)
            {
                int x = sx + col;
                int y = sy - col;
                if(x < 0 || y < 0 || x > 119 || y > 119)
                {
                    sb.append("<div class=\"isotile\"></div>\n");
                }
                else
                {
                    switch (iso[x][y])
                    {
                        case '.':
                            sb.append("<div class=\"isotile\"><img src=\"dungeon/palette48_Floor_Huge_face" + rng.nextInt(4) + "_0.png\" /></div>\n");
                            break;
                        case '"':
                            sb.append("<div class=\"isotile\"><img src=\"dungeon/palette47_Grass_Huge_face" + rng.nextInt(4) + "_0.png\" /></div>\n");
                            break;
                        case '#':
                            sb.append("<div class=\"isotile\"><img src=\"dungeon/palette48_Boulder_Huge_face" + rng.nextInt(4) + "_0.png\" /></div>\n");
                            break;
                        case '~':
                        case ',':
                            water[x][y] = rng.nextInt(16);
                            if(water[x][y-1] > -1) water[x][y] = (water[x][y] & 14) | ((water[x][y-1] & 4) >> 2);
                            if(water[x][y+1] > -1) water[x][y] = (water[x][y] & 11) | ((water[x][y+1] & 1) * 4);
                            if(water[x-1][y] > -1) water[x][y] = (water[x][y] & 7)  | ((water[x-1][y] & 2) * 4);
                            if(water[x+1][y] > -1) water[x][y] = (water[x][y] & 13) | ((water[x+1][y] & 8) >> 2);

                            sb.append("<div class=\"isotile\"><img src=\"dungeon/palette0_Water_Huge_face0_" + (Integer.toHexString(water[x][y])) + ".png\" /></div>\n");
                            break;
                        case '│':
                            sb.append("<div class=\"isotile\"><img src=\"dungeon/palette48_Wall_Straight_Huge_face" + (rng.nextInt(2) * 2) + "_0.png\" /></div>\n");
                            break;
                        case '─':
                            sb.append("<div class=\"isotile\"><img src=\"dungeon/palette48_Wall_Straight_Huge_face" + (rng.nextInt(2) * 2 + 1) + "_0.png\" /></div>\n");
                            break;

                        case '┌':
                            sb.append("<div class=\"isotile\"><img src=\"dungeon/palette48_Wall_Corner_Huge_face2_0.png\" /></div>\n");
                            break;
                        case '┐':
                            sb.append("<div class=\"isotile\"><img src=\"dungeon/palette48_Wall_Corner_Huge_face3_0.png\" /></div>\n");
                            break;
                        case '└':
                            sb.append("<div class=\"isotile\"><img src=\"dungeon/palette48_Wall_Corner_Huge_face1_0.png\" /></div>\n");
                            break;
                        case '┘':
                            sb.append("<div class=\"isotile\"><img src=\"dungeon/palette48_Wall_Corner_Huge_face0_0.png\" /></div>\n");
                            break;

                        case '┤':
                            sb.append("<div class=\"isotile\"><img src=\"dungeon/palette48_Wall_Tee_Huge_face0_0.png\" /></div>\n");
                            break;
                        case '┴':
                            sb.append("<div class=\"isotile\"><img src=\"dungeon/palette48_Wall_Tee_Huge_face1_0.png\" /></div>\n");
                            break;
                        case '├':
                            sb.append("<div class=\"isotile\"><img src=\"dungeon/palette48_Wall_Tee_Huge_face2_0.png\" /></div>\n");
                            break;
                        case '┬':
                            sb.append("<div class=\"isotile\"><img src=\"dungeon/palette48_Wall_Tee_Huge_face3_0.png\" /></div>\n");
                            break;

                        case '┼':
                            sb.append("<div class=\"isotile\"><img src=\"dungeon/palette48_Wall_Cross_Huge_face" + rng.nextInt(4) + "_0.png\" /></div>\n");
                            break;

                        case '+':
                            sb.append("<div class=\"isotile\"><img src=\"dungeon/palette48_Door_Closed_Huge_face" + (rng.nextInt(2) * 2) + "_0.png\" /></div>\n");
                            break;
                        case '/':
                            sb.append("<div class=\"isotile\"><img src=\"dungeon/palette48_Door_Closed_Huge_face" + (rng.nextInt(2) * 2 + 1) + "_0.png\" /></div>\n");
                            break;

                        case ' ':
                            sb.append("<div class=\"isotile\"></div>\n");
                            break;
                    }
                }
            }

            sb.append("</div>\n");
        }
        try {
            FileWriter fw = new FileWriter("data.html");
            fw.write(sb.toString());
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
