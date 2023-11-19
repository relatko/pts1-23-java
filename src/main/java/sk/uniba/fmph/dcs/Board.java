package sk.uniba.fmph.dcs;

import java.util.ArrayList;
import java.util.Optional;

public class Board implements BoardInterface{
    public Points points;
    private PatternLineInterface[] patternLines;
    private WallLineInterface[] wallLines;
    private FloorInterface floor;

    public Board(PatternLineInterface[] patternLines, WallLineInterface[] wallLines, FloorInterface floor){
        points = new Points(0);
        this.patternLines = patternLines;
        this.wallLines = wallLines;
        this.floor = floor;
    }

    public Board(UsedTilesGiveInterface usedTiles,final ArrayList<Points> pointPattern){
        points = new Points(0);
        int countColours = Tile.values().length - 1;
        patternLines = new PatternLine[countColours];
        wallLines = new WallLine[countColours];
        floor = new Floor(usedTiles, pointPattern);

        for (int i = 0; i < countColours; i++){
            patternLines[i] = new PatternLine(i+1, usedTiles, floor, wallLines[i]);

            Tile[] tiles = new Tile[countColours];
            int j = i;
            for (Tile tile : Tile.values()){
                if (tile != Tile.STARTING_PLAYER){
                    tiles[j] = tile;
                    j++;
                    if (j >= countColours)
                        j = 0;
                }
            }
            WallLine lineUp = (i > 0 ? wallLines[i-1] : null);
            WallLine lineDown = (i < countColours - 1 ? wallLines[i+1] : null);
            wallLines[i] = new WallLine(tiles, lineUp, lineDown);
        }
    }

    private Optional<Tile>[][] getWall(){
        Optional<Tile>[][] wall = new Optional[wallLines.length][];
        for (int i = 0; i < wall.length; i++)
            wall[i] = wallLines[i].getTiles();
        return wall;
    }

    @Override
    public void put(int destinationIdx, Tile[] tiles) {
        patternLines[destinationIdx].put(tiles);
    }

    @Override
    public FinishRoundResult finishRound() {
        int newPoints = points.getValue();
        for (int i = 0; i < patternLines.length; i++)
            newPoints += patternLines[i].finishRound().getValue();
        newPoints -= floor.finishRound().getValue();
        points = new Points(newPoints);

        return GameFinished.gameFinished(getWall());
    }

    @Override
    public void endGame() {
        Points finalPoints = FinalPointsCalculation.getPoints(getWall());
        points = new Points(points.getValue() + finalPoints.getValue());
    }

    @Override
    public String state() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(points).append("\n");

        int length = patternLines[patternLines.length - 1].state().length();
        for (int i = 0; i < patternLines.length; i++){
            String alignedPatternLine = patternLines[i].state();
            int spaces = length - alignedPatternLine.length();
            alignedPatternLine = alignedPatternLine + " ".repeat(spaces);
            stringBuilder.append(alignedPatternLine).append(" | ").append(wallLines[i].state()).append("\n");
        }

        stringBuilder.append(floor.state());
        return stringBuilder.toString();
    }
}
