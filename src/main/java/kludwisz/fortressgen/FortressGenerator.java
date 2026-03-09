package kludwisz.fortressgen;

import com.seedfinding.mccore.rand.ChunkRand;
import com.seedfinding.mccore.util.block.BlockBox;
import com.seedfinding.mccore.util.data.Pair;
import com.seedfinding.mccore.util.pos.BPos;
import com.seedfinding.mccore.util.pos.CPos;
import com.seedfinding.mccore.version.MCVersion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FortressGenerator {
    final StaticFortressGenerator.PieceWeight[] BRIDGE_PIECE_WEIGHTS = new StaticFortressGenerator.PieceWeight[]{new StaticFortressGenerator.PieceWeight(StaticFortressGenerator.BridgeStraight.class, 30, 0, true), new StaticFortressGenerator.PieceWeight(StaticFortressGenerator.BridgeCrossing.class, 10, 4), new StaticFortressGenerator.PieceWeight(StaticFortressGenerator.RoomCrossing.class, 10, 4), new StaticFortressGenerator.PieceWeight(StaticFortressGenerator.StairsRoom.class, 10, 3), new StaticFortressGenerator.PieceWeight(StaticFortressGenerator.MonsterThrone.class, 5, 2), new StaticFortressGenerator.PieceWeight(StaticFortressGenerator.CastleEntrance.class, 5, 1)};
    final StaticFortressGenerator.PieceWeight[] CASTLE_PIECE_WEIGHTS = new StaticFortressGenerator.PieceWeight[]{new StaticFortressGenerator.PieceWeight(StaticFortressGenerator.CastleSmallCorridorPiece.class, 25, 0, true), new StaticFortressGenerator.PieceWeight(StaticFortressGenerator.CastleSmallCorridorCrossingPiece.class, 15, 5), new StaticFortressGenerator.PieceWeight(StaticFortressGenerator.CastleSmallCorridorRightTurnPiece.class, 5, 10), new StaticFortressGenerator.PieceWeight(StaticFortressGenerator.CastleSmallCorridorLeftTurnPiece.class, 5, 10), new StaticFortressGenerator.PieceWeight(StaticFortressGenerator.CastleCorridorStairsPiece.class, 10, 3, true), new StaticFortressGenerator.PieceWeight(StaticFortressGenerator.CastleCorridorTBalconyPiece.class, 7, 2), new StaticFortressGenerator.PieceWeight(StaticFortressGenerator.CastleStalkRoom.class, 5, 2)};

    public final ArrayList<StaticFortressGenerator.Piece> pieces;
    public StaticFortressGenerator.StartPiece start;
    public BlockBox fortressBoundingBox;

    final ChunkRand rand;
    boolean isGenerated;
    long structseed;

    public FortressGenerator() {
        pieces = new ArrayList<>();
        rand = new ChunkRand();
        isGenerated = false;
    }

    public void generate(long structseed, int chunkX, int chunkZ, boolean skipHeight) {
        StaticFortressGenerator.generateFortress(this, structseed, chunkX, chunkZ, skipHeight);
    }

    // ------------------------------------------------------------
    // SEEDFINDING UTILITIES (loot, spawners)
    // ------------------------------------------------------------

    public List<BPos> getChestPositions() {
        if (!isGenerated)
            return null;

        ArrayList<BPos> chestPositions = new ArrayList<>();

        for (StaticFortressGenerator.Piece piece : pieces) {
            BPos chestPos = piece.getChestPos();
            if (chestPos != null)
                chestPositions.add(chestPos);
        }

        return chestPositions;
    }

    public List<Pair<BPos, Long>> getChestPositionsWithLootseeds(ChunkRand rand, MCVersion version) {
        if (!isGenerated)
            return null;

        List<BPos> positions = getChestPositions();
        HashMap<CPos, List<BPos>> chestChunks = new HashMap<>();

        for (BPos chest : positions) {
            CPos chunkPos = chest.toChunkPos();
            if (!chestChunks.containsKey(chunkPos)) {
                chestChunks.put(chunkPos, new ArrayList<BPos>());
            }
            chestChunks.get(chunkPos).add(chest);
        }

        // now proccessing each chunk separately to gather lootseeds
        ArrayList<Pair<BPos, Long>> result = new ArrayList<>();
        for (CPos c : chestChunks.keySet()) {
            rand.setDecoratorSeed(structseed, c.getX() << 4, c.getZ() << 4, 0, 7, version);

            for (BPos chest : chestChunks.get(c)) {
                result.add(new Pair<BPos, Long>(chest, rand.nextLong()));
            }
        }

        return result;
    }

    public List<BPos> getSpawnerPositions() {
        ArrayList<BPos> result = new ArrayList<>();

        int spawners = 0;
        for (StaticFortressGenerator.Piece piece : pieces) {
            if (spawners >= 2) break; // optimization

            BPos spawner = piece.getSpawnerPos();
            if (spawner != null) {
                spawners++;
                result.add(spawner);
            }
        }

        return result;
    }
}
