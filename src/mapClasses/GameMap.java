package mapClasses;

import Network.Network;
import mainGame.House;
import mainGame.Main;
import mainGame.PlayerStats;
import mainGame.Road;
import org.lwjgl.input.Mouse;
import org.newdawn.slick.*;
import org.newdawn.slick.geom.*;

import java.util.*;

public class GameMap
{
    private static Layout mapLayout;
    private ArrayList<Tile> map;

    private Circle[] housePlots;
    private ArrayList<House> houses;

    private int houseRadius;

    private Line[] roadPlots;
    private ArrayList<Road> roads;

    public static int[] serializedHouse = new int[2];
    public static int[] deSerializedHouse = new int[]{0, 0};
    public static boolean serverInputInReceived = false;

    public GameMap()
    {
    }

    public void GameMapGameMap()
    {
        int mapTileSize = 55;
        houseRadius = 10;
        buildMap(mapTileSize, Layout.pointy);
        findHousePlots();

        findRoadPlots();

        houses = new ArrayList<>();
        roads = new ArrayList<>();
    }

    private void findHousePlots() //should be 54
    {
        housePlots = new Circle[54];
        int counter = 1;

        for (Tile tile : map)
        {
            if (!(Math.abs(tile.q) == 3 || Math.abs(tile.r) == 3 || Math.abs(tile.s) == 3))
            {
                ArrayList<Point> centerPoints = Layout.polygonCorners(mapLayout, tile);

                for (Point point : centerPoints)
                {
                    Circle tmpCircle = new Circle(point.getX(), point.getY(), houseRadius);
                    if (housePlots[0] == null)
                    {
                        housePlots[0] = tmpCircle;
                    } else
                    {
                        boolean canAdd = true;
                        for (int i = 0; i < housePlots.length; i++)
                        {
                            if (housePlots[i] != null && housePlots[i].contains(tmpCircle.getCenterX(), tmpCircle.getCenterY()))
                                canAdd = false;
                        }
                        if (canAdd)
                        {
                            housePlots[counter] = tmpCircle;
                            counter++;
                        }
                    }
                }
            }
        }
    }

    public void addHouse(int indexPos, int playerID, boolean wasReceivedFromServer)
    {
        House tmpHouse = new House(housePlots[indexPos], playerID);
        houses.add(tmpHouse);
        if (!wasReceivedFromServer)
            serializeHouse(indexPos, PlayerStats.ID);

        //removeHouseNeighborPlots(indexPos);
        removeHousePlot(indexPos);
    }

    private void findRoadPlots() //should be 72
    {
        roadPlots = new Line[72];
        int counter = 0;

        for (Tile tile : map)
        {
            if (!(Math.abs(tile.q) == 3 || Math.abs(tile.r) == 3 || Math.abs(tile.s) == 3))
            {
                ArrayList<Point> tmpPlots = Layout.polygonCorners(mapLayout, tile);
                for (int i = 0; i < tmpPlots.size(); i++)
                {
                    int iPlusOne = (i == tmpPlots.size() - 1) ? (-5) : 1;

                    Line l = new Line(tmpPlots.get(i).getX(), tmpPlots.get(i).getY(),
                            tmpPlots.get(i + iPlusOne).getX(), tmpPlots.get(i + iPlusOne).getY());

                    if (roadPlots[0] == null)
                    {
                        roadPlots[0] = l;
                    } else
                    {
                        boolean canAdd = true;
                        for (int j = 0; j < roadPlots.length; j++)
                        {
                            if (roadPlots[j] != null && roadPlots[j].intersects
                                    (new Circle(l.getCenterX(), l.getCenterY(), 3)))
                            {
                                canAdd = false;
                            }
                        }
                        if (canAdd)
                        {
                            roadPlots[counter] = l;
                            counter++;
                        }
                    }
                }
            }
        }
    }

    public void addRoad(Line roadLine, int playerID)
    {
        roads.add(new Road(roadLine, playerID));
    }

    private void buildMap(int tileSize, Orientation orientation)
    {
        mapLayout = new Layout(orientation,
                new Point(tileSize, tileSize),
                new Point(Main.ScreenWidth / 2, Main.ScreenHeight / 2));

        map = new ArrayList<>();
        for (int q = -3; q <= 3; q++)
        {
            int r1 = Math.max(-3, -q - 3);
            int r2 = Math.min(3, -q + 3);
            for (int r = r1; r <= r2; r++)
            {
                map.add(new Tile(q, r, -q - r));
            }
        }
        if (!Network.isConnected)
        {
            assignTileVariablesFromLocal();
        } else
        {
            assignTileVariablesFromServer();
        }
    }

    private void assignTileVariablesFromLocal()
    {
        Integer[] yieldNumbers = {2, 3, 3, 4, 4, 5, 5, 6, 6, 8, 8, 9, 9, 10, 10, 11, 11, 12};
        ArrayList<Integer> listOfYieldNumbers = new ArrayList<>(Arrays.asList(yieldNumbers));
        Collections.shuffle(listOfYieldNumbers);

        ArrayList<String> listOfTileTypes = new ArrayList<>(
                Arrays.<String>asList(
                        "Grain", "Grain", "Grain", "Grain",
                        "Lumber", "Lumber", "Lumber", "Lumber",
                        "Wool", "Wool", "Wool", "Wool",
                        "Ore", "Ore", "Ore",
                        "Brick", "Brick", "Brick",
                        "Desert"));
        Collections.shuffle(listOfTileTypes);

        int counter = 0;
        for (Tile tile : map)
        {
            if (Math.abs(tile.q) == 3 || Math.abs(tile.r) == 3 || Math.abs(tile.s) == 3)
            {
                tile.setTileType("Water");


                counter++;
            } else
            {
                String type = tile.getTileType();

                if (tile.getTileType().equalsIgnoreCase("Default"))
                {
                    if (!listOfTileTypes.isEmpty())
                    {
                        type = listOfTileTypes.remove(listOfTileTypes.size() - 1);
                        tile.setTileType(type);
                    }
                }

                if (!listOfYieldNumbers.isEmpty())
                {
                    if (!tile.getTileType().equalsIgnoreCase("desert"))
                    {
                        int yieldNum = listOfYieldNumbers.remove(listOfYieldNumbers.size() - 1);
                        tile.setYieldNumber(yieldNum);
                    }
                }
            }
        }
    }

    private void assignTileVariablesFromServer()
    {
        ArrayList<String> listOfTileTypes = Network.serverListOfTypes;
        ArrayList<Integer> listOfYieldNumbers = Network.serverListOfYieldNumbers;

        for (Tile tile : map)
        {
            if (Math.abs(tile.q) == 3 || Math.abs(tile.r) == 3 || Math.abs(tile.s) == 3)
            {
                tile.setTileType("Water");
            } else
            {
                String type = tile.getTileType();

                if (tile.getTileType().equalsIgnoreCase("Default"))
                {
                    if (!listOfTileTypes.isEmpty())
                    {
                        type = listOfTileTypes.remove(listOfTileTypes.size() - 1);
                        tile.setTileType(type);
                    }
                }

                if (!listOfYieldNumbers.isEmpty())
                {
                    if (!tile.getTileType().equalsIgnoreCase("desert"))
                    {
                        int yieldNum = listOfYieldNumbers.remove(listOfYieldNumbers.size() - 1);
                        tile.setYieldNumber(yieldNum);
                    }
                }
            }
        }
    }

    public ArrayList tilesYieldingResource(int diceRoll)
    {
        ArrayList<Tile> resourceYieldingTiles = new ArrayList<>();
        for (Tile tile : map)
        {
            if (tile.getYieldNumber() == diceRoll)
                resourceYieldingTiles.add(tile);
        }
        return resourceYieldingTiles;
    }

    private void removeHousePlot(int indexPos)
    {
        if (housePlots[indexPos] != null)
            housePlots[indexPos] = null;
    }

    private void removeHouseNeighborPlots(int indexPos)
    {
        for (int i = 0; i < housePlots.length; i++)
        {
            if (housePlots[i] != null)
            {
                int distance = (int) new Vector2f(housePlots[indexPos].getCenterX(), housePlots[indexPos].getCenterY()).
                        distance(new Vector2f(housePlots[i].getCenterX(), housePlots[i].getCenterY()));
                if (distance < 65)
                {
                    housePlots[i] = null;
                }
            }
        }
    }

    public void update()
    {
        if (serverInputInReceived)
            deSerializeHouse();


        for (int i = 0; i < housePlots.length; i++)
        {
            if (checkMouseOverHousePlot(i) && Mouse.isButtonDown(0))
                addHouse(i, PlayerStats.ID, false);
        }
    }

    public void render(Graphics g) throws SlickException
    {
        for (Tile tile : map)
        {
            Polygon tmpPoly = new Polygon();
            ArrayList<Point> tmpPoints = Layout.polygonCorners(mapLayout, tile);
            for (Point point : tmpPoints)
                tmpPoly.addPoint(point.getX(), point.getY());

            Image tmpTexture = tile.returnTextureByType();
            g.texture(tmpPoly, tmpTexture, true);
            tmpTexture.setFilter(Image.FILTER_LINEAR);
            if (tile.getYieldNumber() != 0)
            {
                g.drawString(String.valueOf(tile.getYieldNumber()),
                        Layout.hexToPixel(mapLayout, tile).getX() - 6,
                        Layout.hexToPixel(mapLayout, tile).getY() - 8
                );
            }
        }

        for (int i = 0; i < housePlots.length; i++)
        {
            if (housePlots[i] != null && checkMouseOverHousePlot(i))
                g.fill(housePlots[i]);
            else if (housePlots[i] != null)
                g.draw(housePlots[i]);

        }

        for (int i = 0; i < roadPlots.length; i++)
        {
            if (roadPlots[i] != null)
            {
                Circle mouseC = new Circle(Mouse.getX(), Main.ScreenHeight - Mouse.getY(), 5);
                if (roadPlots[i].intersects(mouseC))
                {
                    g.setLineWidth(8);
                    g.draw(roadPlots[i]);
                } else
                {
                    g.setLineWidth(3);
                    g.draw(roadPlots[i]);
                }
            }
        }

        if (!roads.isEmpty())

        {
            for (Road road : roads)
            {
                road.render(g);
            }
        }

        if (!houses.isEmpty())

        {
            for (House house : houses)
            {
                house.render(g);
            }
        }

    }

    private void serializeHouse(int housePlotPos, int playerID)
    {
        serializedHouse[0] = housePlotPos;
        serializedHouse[1] = playerID;
    }

    private void deSerializeHouse()
    {
        addHouse(deSerializedHouse[0], deSerializedHouse[1], true);
        serverInputInReceived = false;
    }

    private boolean checkMouseOverHousePlot(int indexPos)
    {
        return housePlots[indexPos] != null &&
                housePlots[indexPos].contains(Mouse.getX(), Main.ScreenHeight - Mouse.getY());
    }

    private void debugTileInfo()
    {
        for (Tile tile : map)
        {
            if (!(Math.abs(tile.q) == 3 || Math.abs(tile.r) == 3 || Math.abs(tile.s) == 3))
                System.out.println("Tile: " + tile.q + ", " + tile.r + ", " + tile.s +
                        " : Got number: " + tile.getYieldNumber() +
                        " : and Type: " + tile.getTileType());
        }
    }
}
