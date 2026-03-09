package kludwisz.fortressgen;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.seedfinding.mccore.rand.ChunkRand;
import com.seedfinding.mccore.util.block.BlockBox;
import com.seedfinding.mccore.util.block.BlockDirection;
import com.seedfinding.mccore.util.pos.BPos;
import com.seedfinding.mccore.version.MCVersion;
import com.seedfinding.mcseed.rand.JRand;

public class StaticFortressGenerator {
    // ------------------------------------------------------------
    // Fort generation - GENERAL
    // ------------------------------------------------------------

    public static void resetGenerator(FortressGenerator gen) {
        gen.isGenerated = false;
        gen.start = null;
        gen.pieces.clear();
    }

    public static void generateFortress(FortressGenerator gen, long structureSeed, int chunkX, int chunkZ, boolean skipHeight) {
        ChunkRand rand = gen.rand;
        resetGenerator(gen);
        gen.structseed = structureSeed;
        rand.setCarverSeed(structureSeed, chunkX, chunkZ, MCVersion.v1_16_1);

        gen.start = new StaticFortressGenerator.StartPiece(gen, (chunkX << 4) + 2, (chunkZ << 4) + 2);
        gen.pieces.add(gen.start);
        gen.start.addChildren(gen.start, gen.pieces, rand);
        List<Piece> pieceQueue = gen.start.pendingChildren;

        while (!pieceQueue.isEmpty()) {
            int var9 = rand.nextInt(pieceQueue.size());
            Piece var10 = pieceQueue.remove(var9);
            var10.addChildren(gen.start, gen.pieces, rand);
        }
        gen.isGenerated = true;

        if (skipHeight)
            return;

        calculateBoundingBox(gen);
        moveInsideHeights(gen, 48, 70);
    }

    private static void calculateBoundingBox(FortressGenerator gen) {
        gen.fortressBoundingBox = BlockBox.empty();
        Iterator<Piece> it = gen.pieces.iterator();
        while (it.hasNext()) {
            Piece var2 = it.next();
            gen.fortressBoundingBox.encompass(var2.boundingBox);
        }
    }

    private static void moveInsideHeights(FortressGenerator gen, int var2, int var3) {
        int var4 = var3 - var2 + 1 - gen.fortressBoundingBox.getYSpan();
        int var5;

        if (var4 > 1) {
            var5 = var2 + gen.rand.nextInt(var4);
        } else {
            var5 = var2;
        }

        int var6 = var5 - gen.fortressBoundingBox.minY;
        gen.fortressBoundingBox.move(0, var6, 0);
        Iterator<Piece> it = gen.pieces.iterator();

        while (it.hasNext()) {
            Piece var8 = it.next();
            var8.boundingBox.move(0, var6, 0);
        }
    }

    // ------------------------------------------------------------
    // Fort generation - FORTRESS PIECES
    // ------------------------------------------------------------

    // this section is just Mojang code that I cleaned up and modified to use SeedFinding lib classes

    private static Piece findAndCreateBridgePieceFactory(PieceWeight var0, List<Piece> var1, JRand var2, int var3, int var4, int var5, BlockDirection var6, int var7) {
        Class<? extends Piece> var8 = var0.pieceClass;
        Object var9 = null;
        if (var8 == StaticFortressGenerator.BridgeStraight.class) {
            var9 = StaticFortressGenerator.BridgeStraight.createPiece(var1, var2, var3, var4, var5, var6, var7);
        } else if (var8 == StaticFortressGenerator.BridgeCrossing.class) {
            var9 = StaticFortressGenerator.BridgeCrossing.createPiece(var1, var3, var4, var5, var6, var7);
        } else if (var8 == StaticFortressGenerator.RoomCrossing.class) {
            var9 = StaticFortressGenerator.RoomCrossing.createPiece(var1, var3, var4, var5, var6, var7);
        } else if (var8 == StaticFortressGenerator.StairsRoom.class) {
            var9 = StaticFortressGenerator.StairsRoom.createPiece(var1, var3, var4, var5, var7, var6);
        } else if (var8 == StaticFortressGenerator.MonsterThrone.class) {
            var9 = StaticFortressGenerator.MonsterThrone.createPiece(var1, var3, var4, var5, var7, var6);
        } else if (var8 == StaticFortressGenerator.CastleEntrance.class) {
            var9 = StaticFortressGenerator.CastleEntrance.createPiece(var1, var2, var3, var4, var5, var6, var7);
        } else if (var8 == StaticFortressGenerator.CastleSmallCorridorPiece.class) {
            var9 = StaticFortressGenerator.CastleSmallCorridorPiece.createPiece(var1, var3, var4, var5, var6, var7);
        } else if (var8 == StaticFortressGenerator.CastleSmallCorridorRightTurnPiece.class) {
            var9 = StaticFortressGenerator.CastleSmallCorridorRightTurnPiece.createPiece(var1, var2, var3, var4, var5, var6, var7);
        } else if (var8 == StaticFortressGenerator.CastleSmallCorridorLeftTurnPiece.class) {
            var9 = StaticFortressGenerator.CastleSmallCorridorLeftTurnPiece.createPiece(var1, var2, var3, var4, var5, var6, var7);
        } else if (var8 == StaticFortressGenerator.CastleCorridorStairsPiece.class) {
            var9 = StaticFortressGenerator.CastleCorridorStairsPiece.createPiece(var1, var3, var4, var5, var6, var7);
        } else if (var8 == StaticFortressGenerator.CastleCorridorTBalconyPiece.class) {
            var9 = StaticFortressGenerator.CastleCorridorTBalconyPiece.createPiece(var1, var3, var4, var5, var6, var7);
        } else if (var8 == StaticFortressGenerator.CastleSmallCorridorCrossingPiece.class) {
            var9 = StaticFortressGenerator.CastleSmallCorridorCrossingPiece.createPiece(var1, var3, var4, var5, var6, var7);
        } else if (var8 == StaticFortressGenerator.CastleStalkRoom.class) {
            var9 = StaticFortressGenerator.CastleStalkRoom.createPiece(var1, var3, var4, var5, var6, var7);
        }

        return (Piece) var9;
    }

    public static class CastleCorridorTBalconyPiece extends Piece {
        public CastleCorridorTBalconyPiece(int var1, BlockBox var2, BlockDirection var3) {
            super(var1);
            this.orientation = var3;
            this.boundingBox = var2;
        }

        public void addChildren(Piece var1, List<Piece> var2, JRand var3) {
            byte var4 = 1;
            BlockDirection var5 = this.orientation;
            if (var5 == BlockDirection.WEST || var5 == BlockDirection.NORTH) {
                var4 = 5;
            }

            this.generateChildLeft((StaticFortressGenerator.StartPiece) var1, var2, var3, 0, var4, var3.nextInt(8) > 0);
            this.generateChildRight((StaticFortressGenerator.StartPiece) var1, var2, var3, 0, var4, var3.nextInt(8) > 0);
        }

        public static StaticFortressGenerator.CastleCorridorTBalconyPiece createPiece(List<Piece> var0, int var1, int var2, int var3, BlockDirection var4, int var5) {
            BlockBox var6 = BlockBox.rotated(var1, var2, var3, -3, 0, 0, 9, 7, 9, var4.getRotation());
            return isOkBox(var6) && Piece.intersectsNone(var0, var6) ? new StaticFortressGenerator.CastleCorridorTBalconyPiece(var5, var6, var4) : null;
        }
    }

    public static class CastleCorridorStairsPiece extends Piece {
        public CastleCorridorStairsPiece(int var1, BlockBox var2, BlockDirection var3) {
            super(var1);
            this.orientation = var3;
            this.boundingBox = var2;
        }

        public void addChildren(Piece var1, List<Piece> var2, JRand var3) {
            this.generateChildForward((StaticFortressGenerator.StartPiece) var1, var2, var3, 1, 0, true);
        }

        public static StaticFortressGenerator.CastleCorridorStairsPiece createPiece(List<Piece> var0, int var1, int var2, int var3, BlockDirection var4, int var5) {
            BlockBox var6 = BlockBox.rotated(var1, var2, var3, -1, -7, 0, 5, 14, 10, var4.getRotation());
            return isOkBox(var6) && Piece.intersectsNone(var0, var6) ? new StaticFortressGenerator.CastleCorridorStairsPiece(var5, var6, var4) : null;
        }
    }

    public static class CastleSmallCorridorLeftTurnPiece extends Piece {
        private boolean isNeedingChest;

        public CastleSmallCorridorLeftTurnPiece(int var1, JRand var2, BlockBox var3, BlockDirection var4) {
            super(var1);
            this.orientation = var4;
            this.boundingBox = var3;
            this.isNeedingChest = var2.nextInt(3) == 0;
        }

        public void addChildren(Piece var1, List<Piece> var2, JRand var3) {
            this.generateChildLeft((StaticFortressGenerator.StartPiece) var1, var2, var3, 0, 1, true);
        }

        public static StaticFortressGenerator.CastleSmallCorridorLeftTurnPiece createPiece(List<Piece> var0, JRand var1, int var2, int var3, int var4, BlockDirection var5, int var6) {
            BlockBox var7 = BlockBox.rotated(var2, var3, var4, -1, 0, 0, 5, 7, 5, var5.getRotation());
            return isOkBox(var7) && Piece.intersectsNone(var0, var7) ? new StaticFortressGenerator.CastleSmallCorridorLeftTurnPiece(var6, var1, var7, var5) : null;
        }

        @Override
        public BPos getChestPos() {
            if (!this.isNeedingChest) return null;
            return new CoordinateTransformer(this.orientation, this.boundingBox).getWorldPos(3, 2, 3);
        }
    }

    public static class CastleSmallCorridorRightTurnPiece extends Piece {
        private boolean isNeedingChest;

        public CastleSmallCorridorRightTurnPiece(int var1, JRand var2, BlockBox var3, BlockDirection var4) {
            super(var1);
            this.orientation = var4;
            this.boundingBox = var3;
            this.isNeedingChest = var2.nextInt(3) == 0;
        }

        public void addChildren(Piece var1, List<Piece> var2, JRand var3) {
            this.generateChildRight((StaticFortressGenerator.StartPiece) var1, var2, var3, 0, 1, true);
        }

        public static StaticFortressGenerator.CastleSmallCorridorRightTurnPiece createPiece(List<Piece> var0, JRand var1, int var2, int var3, int var4, BlockDirection var5, int var6) {
            BlockBox var7 = BlockBox.rotated(var2, var3, var4, -1, 0, 0, 5, 7, 5, var5.getRotation());
            return isOkBox(var7) && Piece.intersectsNone(var0, var7) ? new StaticFortressGenerator.CastleSmallCorridorRightTurnPiece(var6, var1, var7, var5) : null;
        }

        @Override
        public BPos getChestPos() {
            if (!this.isNeedingChest) return null;
            return new CoordinateTransformer(this.orientation, this.boundingBox).getWorldPos(1, 2, 3);
        }
    }

    public static class CastleSmallCorridorCrossingPiece extends Piece {
        public CastleSmallCorridorCrossingPiece(int var1, BlockBox var2, BlockDirection var3) {
            super(var1);
            this.orientation = var3;
            this.boundingBox = var2;
        }

        public void addChildren(Piece var1, List<Piece> var2, JRand var3) {
            this.generateChildForward((StaticFortressGenerator.StartPiece) var1, var2, var3, 1, 0, true);
            this.generateChildLeft((StaticFortressGenerator.StartPiece) var1, var2, var3, 0, 1, true);
            this.generateChildRight((StaticFortressGenerator.StartPiece) var1, var2, var3, 0, 1, true);
        }

        public static StaticFortressGenerator.CastleSmallCorridorCrossingPiece createPiece(List<Piece> var0, int var1, int var2, int var3, BlockDirection var4, int var5) {
            BlockBox var6 = BlockBox.rotated(var1, var2, var3, -1, 0, 0, 5, 7, 5, var4.getRotation());
            return isOkBox(var6) && Piece.intersectsNone(var0, var6) ? new StaticFortressGenerator.CastleSmallCorridorCrossingPiece(var5, var6, var4) : null;
        }
    }

    public static class CastleSmallCorridorPiece extends Piece {
        public CastleSmallCorridorPiece(int var1, BlockBox var2, BlockDirection var3) {
            super(var1);
            this.orientation = var3;
            this.boundingBox = var2;
        }

        public void addChildren(Piece var1, List<Piece> var2, JRand var3) {
            this.generateChildForward((StaticFortressGenerator.StartPiece) var1, var2, var3, 1, 0, true);
        }

        public static StaticFortressGenerator.CastleSmallCorridorPiece createPiece(List<Piece> var0, int var1, int var2, int var3, BlockDirection var4, int var5) {
            BlockBox var6 = BlockBox.rotated(var1, var2, var3, -1, 0, 0, 5, 7, 5, var4.getRotation());
            return isOkBox(var6) && Piece.intersectsNone(var0, var6) ? new StaticFortressGenerator.CastleSmallCorridorPiece(var5, var6, var4) : null;
        }
    }

    public static class CastleStalkRoom extends Piece {
        public CastleStalkRoom(int var1, BlockBox var2, BlockDirection var3) {
            super(var1);
            this.orientation = var3;
            this.boundingBox = var2;
        }

        public void addChildren(Piece var1, List<Piece> var2, JRand var3) {
            this.generateChildForward((StaticFortressGenerator.StartPiece) var1, var2, var3, 5, 3, true);
            this.generateChildForward((StaticFortressGenerator.StartPiece) var1, var2, var3, 5, 11, true);
        }

        public static StaticFortressGenerator.CastleStalkRoom createPiece(List<Piece> var0, int var1, int var2, int var3, BlockDirection var4, int var5) {
            BlockBox var6 = BlockBox.rotated(var1, var2, var3, -5, -3, 0, 13, 14, 13, var4.getRotation());
            return isOkBox(var6) && Piece.intersectsNone(var0, var6) ? new StaticFortressGenerator.CastleStalkRoom(var5, var6, var4) : null;
        }
    }

    public static class CastleEntrance extends Piece {
        public CastleEntrance(int var1, JRand var2, BlockBox var3, BlockDirection var4) {
            super(var1);
            this.orientation = var4;
            this.boundingBox = var3;
        }

        public void addChildren(Piece var1, List<Piece> var2, JRand var3) {
            this.generateChildForward((StaticFortressGenerator.StartPiece) var1, var2, var3, 5, 3, true);
        }

        public static StaticFortressGenerator.CastleEntrance createPiece(List<Piece> var0, JRand var1, int var2, int var3, int var4, BlockDirection var5, int var6) {
            BlockBox var7 = BlockBox.rotated(var2, var3, var4, -5, -3, 0, 13, 14, 13, var5.getRotation());
            return isOkBox(var7) && Piece.intersectsNone(var0, var7) ? new StaticFortressGenerator.CastleEntrance(var6, var1, var7, var5) : null;
        }
    }

    public static class MonsterThrone extends Piece {
        public MonsterThrone(int var1, BlockBox var2, BlockDirection var3) {
            super(var1);
            this.orientation = var3;
            this.boundingBox = var2;
        }

        public static StaticFortressGenerator.MonsterThrone createPiece(List<Piece> var0, int var1, int var2, int var3, int var4, BlockDirection var5) {
            BlockBox var6 = BlockBox.rotated(var1, var2, var3, -2, 0, 0, 7, 8, 9, var5.getRotation());
            return isOkBox(var6) && Piece.intersectsNone(var0, var6) ? new StaticFortressGenerator.MonsterThrone(var4, var6, var5) : null;
        }

        @Override
        public BPos getSpawnerPos() {
            return new CoordinateTransformer(this.orientation, this.boundingBox).getWorldPos(3, 5, 5);
        }

        public void addChildren(Piece var1, List<Piece> var2, JRand var3) {
        }
    }

    public static class StairsRoom extends Piece {
        public StairsRoom(int var1, BlockBox var2, BlockDirection var3) {
            super(var1);
            this.orientation = var3;
            this.boundingBox = var2;
        }

        public void addChildren(Piece var1, List<Piece> var2, JRand var3) {
            this.generateChildRight((StaticFortressGenerator.StartPiece) var1, var2, var3, 6, 2, false);
        }

        public static StaticFortressGenerator.StairsRoom createPiece(List<Piece> var0, int var1, int var2, int var3, int var4, BlockDirection var5) {
            BlockBox var6 = BlockBox.rotated(var1, var2, var3, -2, 0, 0, 7, 11, 7, var5.getRotation());
            return isOkBox(var6) && Piece.intersectsNone(var0, var6) ? new StaticFortressGenerator.StairsRoom(var4, var6, var5) : null;
        }
    }

    public static class RoomCrossing extends Piece {
        public RoomCrossing(int var1, BlockBox var2, BlockDirection var3) {
            super(var1);
            this.orientation = var3;
            this.boundingBox = var2;
        }

        public void addChildren(Piece var1, List<Piece> var2, JRand var3) {
            this.generateChildForward((StaticFortressGenerator.StartPiece) var1, var2, var3, 2, 0, false);
            this.generateChildLeft((StaticFortressGenerator.StartPiece) var1, var2, var3, 0, 2, false);
            this.generateChildRight((StaticFortressGenerator.StartPiece) var1, var2, var3, 0, 2, false);
        }

        public static StaticFortressGenerator.RoomCrossing createPiece(List<Piece> var0, int var1, int var2, int var3, BlockDirection var4, int var5) {
            BlockBox var6 = BlockBox.rotated(var1, var2, var3, -2, 0, 0, 7, 9, 7, var4.getRotation());
            return isOkBox(var6) && Piece.intersectsNone(var0, var6) ? new StaticFortressGenerator.RoomCrossing(var5, var6, var4) : null;
        }
    }

    public static class BridgeCrossing extends Piece {
        public BridgeCrossing(int var1, BlockBox var2, BlockDirection var3) {
            super(var1);
            this.orientation = var3;
            this.boundingBox = var2;
        }

        protected BridgeCrossing(JRand var1, int var2, int var3) {
            super(0);
            this.orientation = BlockDirection.randomHorizontal(var1);

            if (this.orientation.getAxis() == BlockDirection.Axis.Z) {
                this.boundingBox = new BlockBox(var2, 64, var3, var2 + 19 - 1, 73, var3 + 19 - 1);
            } else {
                this.boundingBox = new BlockBox(var2, 64, var3, var2 + 19 - 1, 73, var3 + 19 - 1);
            }
        }

        public void addChildren(Piece var1, List<Piece> var2, JRand var3) {
            this.generateChildForward((StaticFortressGenerator.StartPiece) var1, var2, var3, 8, 3, false);
            this.generateChildLeft((StaticFortressGenerator.StartPiece) var1, var2, var3, 3, 8, false);
            this.generateChildRight((StaticFortressGenerator.StartPiece) var1, var2, var3, 3, 8, false);
        }

        public static StaticFortressGenerator.BridgeCrossing createPiece(List<Piece> var0, int var1, int var2, int var3, BlockDirection var4, int var5) {
            BlockBox var6 = BlockBox.rotated(var1, var2, var3, -8, -3, 0, 19, 10, 19, var4.getRotation());
            return isOkBox(var6) && Piece.intersectsNone(var0, var6) ? new StaticFortressGenerator.BridgeCrossing(var5, var6, var4) : null;
        }
    }

    public static class BridgeEndFiller extends Piece {
        public BridgeEndFiller(int var1, JRand var2, BlockBox var3, BlockDirection var4) {
            super(var1);
            this.orientation = var4;
            this.boundingBox = var3;
            var2.nextInt();
        }

        public static StaticFortressGenerator.BridgeEndFiller createPiece(List<Piece> var0, JRand var1, int var2, int var3, int var4, BlockDirection var5, int var6) {
            BlockBox var7 = BlockBox.rotated(var2, var3, var4, -1, -3, 0, 5, 10, 8, var5.getRotation());
            return isOkBox(var7) && Piece.intersectsNone(var0, var7) ? new StaticFortressGenerator.BridgeEndFiller(var6, var1, var7, var5) : null;
        }

        public void addChildren(Piece var1, List<Piece> var2, JRand var3) {
        }
    }

    public static class BridgeStraight extends Piece {
        public BridgeStraight(int var1, JRand var2, BlockBox var3, BlockDirection var4) {
            super(var1);
            this.orientation = var4;
            this.boundingBox = var3;
        }

        public void addChildren(Piece var1, List<Piece> var2, JRand var3) {
            this.generateChildForward((StaticFortressGenerator.StartPiece) var1, var2, var3, 1, 3, false);
        }

        public static StaticFortressGenerator.BridgeStraight createPiece(List<Piece> var0, JRand var1, int var2, int var3, int var4, BlockDirection var5, int var6) {
            BlockBox var7 = BlockBox.rotated(var2, var3, var4, -1, -3, 0, 5, 10, 19, var5.getRotation());
            return isOkBox(var7) && Piece.intersectsNone(var0, var7) ? new StaticFortressGenerator.BridgeStraight(var6, var1, var7, var5) : null;
        }
    }


    public static class StartPiece extends StaticFortressGenerator.BridgeCrossing {
        public PieceWeight previousPiece;
        public List<PieceWeight> availableBridgePieces;
        public List<PieceWeight> availableCastlePieces;
        public final List<Piece> pendingChildren = new ArrayList<>();

        public StartPiece(FortressGenerator gen, int var2, int var3) {
            super(gen.rand, var2, var3);
            this.availableBridgePieces = new ArrayList<>();
            PieceWeight[] var4 = gen.BRIDGE_PIECE_WEIGHTS;
            int var5 = var4.length;

            int var6;
            PieceWeight var7;
            for (var6 = 0; var6 < var5; ++var6) {
                var7 = var4[var6];
                var7.placeCount = 0;
                this.availableBridgePieces.add(var7);
            }

            this.availableCastlePieces = new ArrayList<>();
            var4 = gen.CASTLE_PIECE_WEIGHTS;
            var5 = var4.length;

            for (var6 = 0; var6 < var5; ++var6) {
                var7 = var4[var6];
                var7.placeCount = 0;
                this.availableCastlePieces.add(var7);
            }
        }
    }

    public abstract static class Piece {
        public BlockBox boundingBox;
        public BlockDirection orientation;
        public int genDepth;

        protected Piece(int var2) {
            this.genDepth = var2;
        }

        public abstract void addChildren(Piece var1, List<Piece> var2, JRand var3);

        private int updatePieceWeight(List<PieceWeight> var1) {
            boolean var2 = false;
            int var3 = 0;

            PieceWeight var5;
            for (Iterator<PieceWeight> var4 = var1.iterator(); var4.hasNext(); var3 += var5.weight) {
                var5 = (PieceWeight) var4.next();
                if (var5.maxPlaceCount > 0 && var5.placeCount < var5.maxPlaceCount) {
                    var2 = true;
                }
            }

            return var2 ? var3 : -1;
        }

        private Piece generatePiece(StaticFortressGenerator.StartPiece var1, List<PieceWeight> var2, List<Piece> var3, JRand var4, int var5, int var6, int var7, BlockDirection var8, int var9) {
            int var10 = this.updatePieceWeight(var2);
            boolean var11 = var10 > 0 && var9 <= 30;
            int var12 = 0;

            while (var12 < 5 && var11) {
                ++var12;
                int var13 = var4.nextInt(var10);
                Iterator<PieceWeight> var14 = var2.iterator();

                while (var14.hasNext()) {
                    PieceWeight var15 = (PieceWeight) var14.next();
                    var13 -= var15.weight;
                    if (var13 < 0) {
                        if (!var15.doPlace(var9) || var15 == var1.previousPiece && !var15.allowInRow) {
                            break;
                        }

                        Piece var16 = StaticFortressGenerator.findAndCreateBridgePieceFactory(var15, var3, var4, var5, var6, var7, var8, var9);
                        if (var16 != null) {
                            ++var15.placeCount;
                            var1.previousPiece = var15;
                            if (!var15.isValid()) {
                                var2.remove(var15);
                            }

                            //pieces.add(var16);
                            return var16;
                        }
                    }
                }
            }

            return StaticFortressGenerator.BridgeEndFiller.createPiece(var3, var4, var5, var6, var7, var8, var9);
        }

        private Piece generateAndAddPiece(StaticFortressGenerator.StartPiece var1, List<Piece> var2, JRand var3, int var4, int var5, int var6, BlockDirection var7, int var8, boolean var9) {
            if (Math.abs(var4 - var1.boundingBox.minX) <= 112 && Math.abs(var6 - var1.boundingBox.minZ) <= 112) {
                List<PieceWeight> var10 = var1.availableBridgePieces;
                if (var9) {
                    var10 = var1.availableCastlePieces;
                }

                Piece var11 = this.generatePiece(var1, var10, var2, var3, var4, var5, var6, var7, var8 + 1);
                if (var11 != null) {
                    var2.add(var11);
                    var1.pendingChildren.add(var11);
                }

                return var11;
            } else {
                return StaticFortressGenerator.BridgeEndFiller.createPiece(var2, var3, var4, var5, var6, var7, var8);
            }
        }


        protected Piece generateChildForward(StaticFortressGenerator.StartPiece var1, List<Piece> var2, JRand var3, int var4, int var5, boolean var6) {
            BlockDirection var7 = this.orientation;
            if (var7 != null) {
                switch (var7) {
                    case NORTH:
                        return this.generateAndAddPiece(var1, var2, var3, this.boundingBox.minX + var4, this.boundingBox.minY + var5, this.boundingBox.minZ - 1, var7, this.genDepth, var6);
                    case SOUTH:
                        return this.generateAndAddPiece(var1, var2, var3, this.boundingBox.minX + var4, this.boundingBox.minY + var5, this.boundingBox.maxZ + 1, var7, this.genDepth, var6);
                    case WEST:
                        return this.generateAndAddPiece(var1, var2, var3, this.boundingBox.minX - 1, this.boundingBox.minY + var5, this.boundingBox.minZ + var4, var7, this.genDepth, var6);
                    case EAST:
                        return this.generateAndAddPiece(var1, var2, var3, this.boundingBox.maxX + 1, this.boundingBox.minY + var5, this.boundingBox.minZ + var4, var7, this.genDepth, var6);
                    default:
                        return null;
                }
            }

            return null;
        }

        protected Piece generateChildLeft(StaticFortressGenerator.StartPiece var1, List<Piece> var2, JRand var3, int var4, int var5, boolean var6) {
            BlockDirection var7 = this.orientation;
            if (var7 != null) {
                switch (var7) {
                    case NORTH:
                        return this.generateAndAddPiece(var1, var2, var3, this.boundingBox.minX - 1, this.boundingBox.minY + var4, this.boundingBox.minZ + var5, BlockDirection.WEST, this.genDepth, var6);
                    case SOUTH:
                        return this.generateAndAddPiece(var1, var2, var3, this.boundingBox.minX - 1, this.boundingBox.minY + var4, this.boundingBox.minZ + var5, BlockDirection.WEST, this.genDepth, var6);
                    case WEST:
                        return this.generateAndAddPiece(var1, var2, var3, this.boundingBox.minX + var5, this.boundingBox.minY + var4, this.boundingBox.minZ - 1, BlockDirection.NORTH, this.genDepth, var6);
                    case EAST:
                        return this.generateAndAddPiece(var1, var2, var3, this.boundingBox.minX + var5, this.boundingBox.minY + var4, this.boundingBox.minZ - 1, BlockDirection.NORTH, this.genDepth, var6);
                    default:
                        return null;
                }
            }

            return null;
        }

        protected Piece generateChildRight(StaticFortressGenerator.StartPiece var1, List<Piece> var2, JRand var3, int var4, int var5, boolean var6) {
            BlockDirection var7 = this.orientation;
            if (var7 != null) {
                switch (var7) {
                    case NORTH:
                        return this.generateAndAddPiece(var1, var2, var3, this.boundingBox.maxX + 1, this.boundingBox.minY + var4, this.boundingBox.minZ + var5, BlockDirection.EAST, this.genDepth, var6);
                    case SOUTH:
                        return this.generateAndAddPiece(var1, var2, var3, this.boundingBox.maxX + 1, this.boundingBox.minY + var4, this.boundingBox.minZ + var5, BlockDirection.EAST, this.genDepth, var6);
                    case WEST:
                        return this.generateAndAddPiece(var1, var2, var3, this.boundingBox.minX + var5, this.boundingBox.minY + var4, this.boundingBox.maxZ + 1, BlockDirection.SOUTH, this.genDepth, var6);
                    case EAST:
                        return this.generateAndAddPiece(var1, var2, var3, this.boundingBox.minX + var5, this.boundingBox.minY + var4, this.boundingBox.maxZ + 1, BlockDirection.SOUTH, this.genDepth, var6);
                    default:
                        return null;
                }
            }

            return null;
        }

        public BPos getChestPos() {
            return null;
        }

        public BPos getSpawnerPos() {
            return null;
        }

        protected static boolean isOkBox(BlockBox var0) {
            return var0 != null && var0.minY > 10;
        }

        protected static boolean intersectsNone(List<Piece> pieceList, BlockBox bb2) {
            for (Piece piece : pieceList) {
                BlockBox bb1 = piece.boundingBox;
                if (bb1.minX <= bb2.maxX && bb1.maxX >= bb2.minX && bb1.minZ <= bb2.maxZ && bb1.maxZ >= bb2.minZ && bb1.minY <= bb2.maxY && bb1.maxY >= bb2.minY)
                    return false;
            }
            return true;
        }
    }

    static class PieceWeight {
        public final Class<? extends Piece> pieceClass;
        public final int weight;
        public int placeCount;
        public final int maxPlaceCount;
        public final boolean allowInRow;

        public PieceWeight(Class<? extends Piece> var1, int var2, int var3, boolean var4) {
            super();
            this.pieceClass = var1;
            this.weight = var2;
            this.maxPlaceCount = var3;
            this.allowInRow = var4;
        }

        public PieceWeight(Class<? extends Piece> var1, int var2, int var3) {
            this(var1, var2, var3, false);
        }

        public boolean doPlace(int var1) {
            return this.maxPlaceCount == 0 || this.placeCount < this.maxPlaceCount;
        }

        public boolean isValid() {
            return this.maxPlaceCount == 0 || this.placeCount < this.maxPlaceCount;
        }
    }

    // i didnt change the piece names cause they look very funny
    enum PieceType {
        NETHER_FORTRESS_START,
        NETHER_FORTRESS_CASTLE_CORRIDOR_T_BALCONY,
        NETHER_FORTRESS_CASTLE_CORRIDOR_STAIRS,
        NETHER_FORTRESS_CASTLE_SMALL_CORRIDOR_LEFT_TURN,
        NETHER_FORTRESS_CASTLE_SMALL_CORRIDOR_RIGHT_TURN,
        NETHER_FORTRESS_CASTLE_SMALL_CORRIDOR_CROSSING,
        NETHER_FORTRESS_CASTLE_SMALL_CORRIDOR,
        NETHER_FORTRESS_CASTLE_STALK_ROOM,
        NETHER_FORTRESS_CASTLE_ENTRANCE,
        NETHER_FORTRESS_MONSTER_THRONE,
        NETHER_FORTRESS_STAIRS_ROOM,
        NETHER_FORTRESS_ROOM_CROSSING,
        NETHER_FORTRESS_BRIDGE_CROSSING,
        NETHER_FORTRESS_BRIDGE_STRAIGHT,
        NETHER_FORTRESS_BRIDGE_END_FILLER
    }
}

