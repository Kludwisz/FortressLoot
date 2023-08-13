package kludwisz.fortressgen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.seedfinding.mccore.rand.ChunkRand;
import com.seedfinding.mccore.util.block.BlockBox;
import com.seedfinding.mccore.util.block.BlockDirection;
import com.seedfinding.mccore.util.data.Pair;
import com.seedfinding.mccore.util.pos.BPos;
import com.seedfinding.mccore.util.pos.CPos;
import com.seedfinding.mccore.version.MCVersion;
import com.seedfinding.mcseed.rand.JRand;

public class FortressGenerator {
   private static final PieceWeight[] BRIDGE_PIECE_WEIGHTS = new PieceWeight[]{new PieceWeight(FortressGenerator.BridgeStraight.class, 30, 0, true), new PieceWeight(FortressGenerator.BridgeCrossing.class, 10, 4), new PieceWeight(FortressGenerator.RoomCrossing.class, 10, 4), new PieceWeight(FortressGenerator.StairsRoom.class, 10, 3), new PieceWeight(FortressGenerator.MonsterThrone.class, 5, 2), new PieceWeight(FortressGenerator.CastleEntrance.class, 5, 1)};
   private static final PieceWeight[] CASTLE_PIECE_WEIGHTS = new PieceWeight[]{new PieceWeight(FortressGenerator.CastleSmallCorridorPiece.class, 25, 0, true), new PieceWeight(FortressGenerator.CastleSmallCorridorCrossingPiece.class, 15, 5), new PieceWeight(FortressGenerator.CastleSmallCorridorRightTurnPiece.class, 5, 10), new PieceWeight(FortressGenerator.CastleSmallCorridorLeftTurnPiece.class, 5, 10), new PieceWeight(FortressGenerator.CastleCorridorStairsPiece.class, 10, 3, true), new PieceWeight(FortressGenerator.CastleCorridorTBalconyPiece.class, 7, 2), new PieceWeight(FortressGenerator.CastleStalkRoom.class, 5, 2)};
   public static ArrayList<Piece> pieces = new ArrayList<>();
   public static FortressGenerator.StartPiece start;
   public static BlockBox fortressBoundingBox;
   private static boolean isGenerated = false;
   private static long structseed;
   
   
   // ------------------------------------------------------------
   // Fort generation - GENERAL
   // ------------------------------------------------------------
   
   public static void resetGenerator() {
	   isGenerated = false;
	   start = null;
	   pieces.clear();
   }
   
   public static boolean generateFortress(long structureSeed, int chunkX, int chunkZ, ChunkRand rand, boolean skipHeight) {
	   	resetGenerator();
	   	structseed = structureSeed;
	   	rand.setCarverSeed(structureSeed, chunkX, chunkZ, MCVersion.v1_16_1);
	   	
	   	start = new FortressGenerator.StartPiece((JRand)rand, (chunkX << 4) + 2, (chunkZ << 4) + 2);
       	pieces.add(start);
       	start.addChildren(start, pieces, rand);
       	List<Piece> pieceQueue = start.pendingChildren;

       	while(!pieceQueue.isEmpty()) {
       		int var9 = rand.nextInt(pieceQueue.size());
       		Piece var10 = (Piece)pieceQueue.remove(var9);
       		var10.addChildren(start, pieces, rand);
       	}
       	isGenerated = true;
       	
       	if (skipHeight)
       		return true;
       		
       	calculateBoundingBox();
       	moveInsideHeights(rand, 48, 70);
       	return true;
   }
   
   private static void calculateBoundingBox() {
	   fortressBoundingBox = BlockBox.empty();
	   Iterator<Piece> it = pieces.iterator();
	   while(it.hasNext()) {
		   Piece var2 = (Piece)it.next();
		   fortressBoundingBox.encompass(var2.boundingBox);
	   }
   }
   
   private static void moveInsideHeights(JRand var1, int var2, int var3) {
	   int var4 = var3 - var2 + 1 - fortressBoundingBox.getYSpan();
	   int var5;
	   
	   if (var4 > 1) {
		   var5 = var2 + var1.nextInt(var4);
	   } else {
		   var5 = var2;
	   }

	   int var6 = var5 - fortressBoundingBox.minY;
	   fortressBoundingBox.move(0, var6, 0);
	   Iterator<Piece> it = pieces.iterator();

	   while(it.hasNext()) {
		   Piece var8 = (Piece)it.next();
		   var8.boundingBox.move(0, var6, 0);
	   }
   }
   
   
   // ------------------------------------------------------------
   // SEEDFINDING UTILITIES (loot, spawners)
   // ------------------------------------------------------------
   
   public static List<BPos> getChestPositions() {
	   if (!isGenerated)
		   return null;
	   
	   ArrayList <BPos> chestPositions = new ArrayList<>();
	   
	   for (Piece piece : pieces) {
		   BPos chestPos = piece.getChestPos();
		   if (chestPos != null)
			   chestPositions.add(chestPos);
	   }
	   
	   return chestPositions;
   }
   
   public static List<Pair<BPos, Long>> getChestPositionsWithLootseeds(ChunkRand rand, MCVersion version) {
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
		   rand.setDecoratorSeed(structseed, c.getX()<<4, c.getZ()<<4, 0, 7, version);
		   
		   for (BPos chest : chestChunks.get(c)) {
			   result.add(new Pair<BPos,Long>(chest, rand.nextLong()));
		   }
	   }
	   
	   return result;
   }
   
   public static List<BPos> getSpawnerPositions() {
	   ArrayList<BPos> result = new ArrayList<>();
	   
	   int spawners = 0;
	   for (Piece piece : pieces) {
		   if (spawners >= 2) break; // optimization
		   
		   BPos spawner = piece.getSpawnerPos();
		   if (spawner != null) {
			   spawners++;
			   result.add(spawner);
		   }
	   }
	   
	   return result;
   }
   
   // ------------------------------------------------------------
   // Fort generation - FORTRESS PIECES
   // ------------------------------------------------------------
   
   // this section is just Mojang code that I cleaned up and modified to use SeedFinding lib classes
   
   private static Piece findAndCreateBridgePieceFactory(PieceWeight var0, List<Piece> var1, JRand var2, int var3, int var4, int var5, BlockDirection var6, int var7) {
      Class<? extends Piece> var8 = var0.pieceClass;
      Object var9 = null;
      if (var8 == FortressGenerator.BridgeStraight.class) {
         var9 = FortressGenerator.BridgeStraight.createPiece(var1, var2, var3, var4, var5, var6, var7);
      } else if (var8 == FortressGenerator.BridgeCrossing.class) {
         var9 = FortressGenerator.BridgeCrossing.createPiece(var1, var3, var4, var5, var6, var7);
      } else if (var8 == FortressGenerator.RoomCrossing.class) {
         var9 = FortressGenerator.RoomCrossing.createPiece(var1, var3, var4, var5, var6, var7);
      } else if (var8 == FortressGenerator.StairsRoom.class) {
         var9 = FortressGenerator.StairsRoom.createPiece(var1, var3, var4, var5, var7, var6);
      } else if (var8 == FortressGenerator.MonsterThrone.class) {
         var9 = FortressGenerator.MonsterThrone.createPiece(var1, var3, var4, var5, var7, var6);
      } else if (var8 == FortressGenerator.CastleEntrance.class) {
         var9 = FortressGenerator.CastleEntrance.createPiece(var1, var2, var3, var4, var5, var6, var7);
      } else if (var8 == FortressGenerator.CastleSmallCorridorPiece.class) {
         var9 = FortressGenerator.CastleSmallCorridorPiece.createPiece(var1, var3, var4, var5, var6, var7);
      } else if (var8 == FortressGenerator.CastleSmallCorridorRightTurnPiece.class) {
         var9 = FortressGenerator.CastleSmallCorridorRightTurnPiece.createPiece(var1, var2, var3, var4, var5, var6, var7);
      } else if (var8 == FortressGenerator.CastleSmallCorridorLeftTurnPiece.class) {
         var9 = FortressGenerator.CastleSmallCorridorLeftTurnPiece.createPiece(var1, var2, var3, var4, var5, var6, var7);
      } else if (var8 == FortressGenerator.CastleCorridorStairsPiece.class) {
         var9 = FortressGenerator.CastleCorridorStairsPiece.createPiece(var1, var3, var4, var5, var6, var7);
      } else if (var8 == FortressGenerator.CastleCorridorTBalconyPiece.class) {
         var9 = FortressGenerator.CastleCorridorTBalconyPiece.createPiece(var1, var3, var4, var5, var6, var7);
      } else if (var8 == FortressGenerator.CastleSmallCorridorCrossingPiece.class) {
         var9 = FortressGenerator.CastleSmallCorridorCrossingPiece.createPiece(var1, var3, var4, var5, var6, var7);
      } else if (var8 == FortressGenerator.CastleStalkRoom.class) {
         var9 = FortressGenerator.CastleStalkRoom.createPiece(var1, var3, var4, var5, var6, var7);
      }

      return (Piece)var9;
   }

   public static class CastleCorridorTBalconyPiece extends Piece {
      public CastleCorridorTBalconyPiece(int var1, BlockBox var2, BlockDirection var3) {
         super(PieceType.NETHER_FORTRESS_CASTLE_CORRIDOR_T_BALCONY, var1);
         this.orientation = var3;
         this.boundingBox = var2;
      }

      public void addChildren(Piece var1, List<Piece> var2, JRand var3) {
         byte var4 = 1;
         BlockDirection var5 = this.orientation;
         if (var5 == BlockDirection.WEST || var5 == BlockDirection.NORTH) {
            var4 = 5;
         }

         this.generateChildLeft((FortressGenerator.StartPiece)var1, var2, var3, 0, var4, var3.nextInt(8) > 0);
         this.generateChildRight((FortressGenerator.StartPiece)var1, var2, var3, 0, var4, var3.nextInt(8) > 0);
      }

      public static FortressGenerator.CastleCorridorTBalconyPiece createPiece(List<Piece> var0, int var1, int var2, int var3, BlockDirection var4, int var5) {
         BlockBox var6 = BlockBox.rotated(var1, var2, var3, -3, 0, 0, 9, 7, 9, var4.getRotation());
         return isOkBox(var6) && Piece.intersectsNone(var0, var6) ? new FortressGenerator.CastleCorridorTBalconyPiece(var5, var6, var4) : null;
      }
   }

   public static class CastleCorridorStairsPiece extends Piece {
      public CastleCorridorStairsPiece(int var1, BlockBox var2, BlockDirection var3) {
         super(PieceType.NETHER_FORTRESS_CASTLE_CORRIDOR_STAIRS, var1);
         this.orientation = var3;
         this.boundingBox = var2;
      }

      public void addChildren(Piece var1, List<Piece> var2, JRand var3) {
         this.generateChildForward((FortressGenerator.StartPiece)var1, var2, var3, 1, 0, true);
      }

      public static FortressGenerator.CastleCorridorStairsPiece createPiece(List<Piece> var0, int var1, int var2, int var3, BlockDirection var4, int var5) {
         BlockBox var6 = BlockBox.rotated(var1, var2, var3, -1, -7, 0, 5, 14, 10, var4.getRotation());
         return isOkBox(var6) && Piece.intersectsNone(var0, var6) ? new FortressGenerator.CastleCorridorStairsPiece(var5, var6, var4) : null;
      }
   }

   public static class CastleSmallCorridorLeftTurnPiece extends Piece {
      private boolean isNeedingChest;

      public CastleSmallCorridorLeftTurnPiece(int var1, JRand var2, BlockBox var3, BlockDirection var4) {
         super(PieceType.NETHER_FORTRESS_CASTLE_SMALL_CORRIDOR_LEFT_TURN, var1);
         this.orientation = var4;
         this.boundingBox = var3;
         this.isNeedingChest = var2.nextInt(3) == 0;
      }

      public void addChildren(Piece var1, List<Piece> var2, JRand var3) {
         this.generateChildLeft((FortressGenerator.StartPiece)var1, var2, var3, 0, 1, true);
      }

      public static FortressGenerator.CastleSmallCorridorLeftTurnPiece createPiece(List<Piece> var0, JRand var1, int var2, int var3, int var4, BlockDirection var5, int var6) {
         BlockBox var7 = BlockBox.rotated(var2, var3, var4, -1, 0, 0, 5, 7, 5, var5.getRotation());
         return isOkBox(var7) && Piece.intersectsNone(var0, var7) ? new FortressGenerator.CastleSmallCorridorLeftTurnPiece(var6, var1, var7, var5) : null;
      }

      @Override
      public BPos getChestPos() {
    	  if (!this.isNeedingChest) return null;
    	  
          CoordinateTransformer.setParams(this.orientation, this.boundingBox);
          BPos chestPos = CoordinateTransformer.getWorldPos(3, 2, 3);
          
          return chestPos;
      }
   }

   public static class CastleSmallCorridorRightTurnPiece extends Piece {
      private boolean isNeedingChest;

      public CastleSmallCorridorRightTurnPiece(int var1, JRand var2, BlockBox var3, BlockDirection var4) {
         super(PieceType.NETHER_FORTRESS_CASTLE_SMALL_CORRIDOR_RIGHT_TURN, var1);
         this.orientation = var4;
         this.boundingBox = var3;
         this.isNeedingChest = var2.nextInt(3) == 0;
      }

      public void addChildren(Piece var1, List<Piece> var2, JRand var3) {
         this.generateChildRight((FortressGenerator.StartPiece)var1, var2, var3, 0, 1, true);
      }

      public static FortressGenerator.CastleSmallCorridorRightTurnPiece createPiece(List<Piece> var0, JRand var1, int var2, int var3, int var4, BlockDirection var5, int var6) {
         BlockBox var7 = BlockBox.rotated(var2, var3, var4, -1, 0, 0, 5, 7, 5, var5.getRotation());
         return isOkBox(var7) && Piece.intersectsNone(var0, var7) ? new FortressGenerator.CastleSmallCorridorRightTurnPiece(var6, var1, var7, var5) : null;
      }

      @Override
      public BPos getChestPos() {
         if (!this.isNeedingChest) return null;
         
         CoordinateTransformer.setParams(this.orientation, this.boundingBox);
       	 BPos chestPos = CoordinateTransformer.getWorldPos(1, 2, 3);

         return chestPos;
      }
   }

   public static class CastleSmallCorridorCrossingPiece extends Piece {
      public CastleSmallCorridorCrossingPiece(int var1, BlockBox var2, BlockDirection var3) {
         super(PieceType.NETHER_FORTRESS_CASTLE_SMALL_CORRIDOR_CROSSING, var1);
         this.orientation = var3;
         this.boundingBox = var2;
      }

      public void addChildren(Piece var1, List<Piece> var2, JRand var3) {
         this.generateChildForward((FortressGenerator.StartPiece)var1, var2, var3, 1, 0, true);
         this.generateChildLeft((FortressGenerator.StartPiece)var1, var2, var3, 0, 1, true);
         this.generateChildRight((FortressGenerator.StartPiece)var1, var2, var3, 0, 1, true);
      }

      public static FortressGenerator.CastleSmallCorridorCrossingPiece createPiece(List<Piece> var0, int var1, int var2, int var3, BlockDirection var4, int var5) {
         BlockBox var6 = BlockBox.rotated(var1, var2, var3, -1, 0, 0, 5, 7, 5, var4.getRotation());
         return isOkBox(var6) && Piece.intersectsNone(var0, var6) ? new FortressGenerator.CastleSmallCorridorCrossingPiece(var5, var6, var4) : null;
      }
   }

   public static class CastleSmallCorridorPiece extends Piece {
      public CastleSmallCorridorPiece(int var1, BlockBox var2, BlockDirection var3) {
         super(PieceType.NETHER_FORTRESS_CASTLE_SMALL_CORRIDOR, var1);
         this.orientation = var3;
         this.boundingBox = var2;
      }

      public void addChildren(Piece var1, List<Piece> var2, JRand var3) {
         this.generateChildForward((FortressGenerator.StartPiece)var1, var2, var3, 1, 0, true);
      }

      public static FortressGenerator.CastleSmallCorridorPiece createPiece(List<Piece> var0, int var1, int var2, int var3, BlockDirection var4, int var5) {
         BlockBox var6 = BlockBox.rotated(var1, var2, var3, -1, 0, 0, 5, 7, 5, var4.getRotation());
         return isOkBox(var6) && Piece.intersectsNone(var0, var6) ? new FortressGenerator.CastleSmallCorridorPiece(var5, var6, var4) : null;
      }
   }

   public static class CastleStalkRoom extends Piece {
      public CastleStalkRoom(int var1, BlockBox var2, BlockDirection var3) {
         super(PieceType.NETHER_FORTRESS_CASTLE_STALK_ROOM, var1);
         this.orientation = var3;
         this.boundingBox = var2;
      }

      public void addChildren(Piece var1, List<Piece> var2, JRand var3) {
         this.generateChildForward((FortressGenerator.StartPiece)var1, var2, var3, 5, 3, true);
         this.generateChildForward((FortressGenerator.StartPiece)var1, var2, var3, 5, 11, true);
      }

      public static FortressGenerator.CastleStalkRoom createPiece(List<Piece> var0, int var1, int var2, int var3, BlockDirection var4, int var5) {
         BlockBox var6 = BlockBox.rotated(var1, var2, var3, -5, -3, 0, 13, 14, 13, var4.getRotation());
         return isOkBox(var6) && Piece.intersectsNone(var0, var6) ? new FortressGenerator.CastleStalkRoom(var5, var6, var4) : null;
      }
   }

   public static class CastleEntrance extends Piece {
      public CastleEntrance(int var1, JRand var2, BlockBox var3, BlockDirection var4) {
         super(PieceType.NETHER_FORTRESS_CASTLE_ENTRANCE, var1);
         this.orientation = var4;
         this.boundingBox = var3;
      }

      public void addChildren(Piece var1, List<Piece> var2, JRand var3) {
         this.generateChildForward((FortressGenerator.StartPiece)var1, var2, var3, 5, 3, true);
      }

      public static FortressGenerator.CastleEntrance createPiece(List<Piece> var0, JRand var1, int var2, int var3, int var4, BlockDirection var5, int var6) {
         BlockBox var7 = BlockBox.rotated(var2, var3, var4, -5, -3, 0, 13, 14, 13, var5.getRotation());
         return isOkBox(var7) && Piece.intersectsNone(var0, var7) ? new FortressGenerator.CastleEntrance(var6, var1, var7, var5) : null;
      }
   }

   public static class MonsterThrone extends Piece {
      public MonsterThrone(int var1, BlockBox var2, BlockDirection var3) {
         super(PieceType.NETHER_FORTRESS_MONSTER_THRONE, var1);
         this.orientation = var3;
         this.boundingBox = var2;
      }

      public static FortressGenerator.MonsterThrone createPiece(List<Piece> var0, int var1, int var2, int var3, int var4, BlockDirection var5) {
         BlockBox var6 = BlockBox.rotated(var1, var2, var3, -2, 0, 0, 7, 8, 9, var5.getRotation());
         return isOkBox(var6) && Piece.intersectsNone(var0, var6) ? new FortressGenerator.MonsterThrone(var4, var6, var5) : null;
      }
      
      @Override
      public BPos getSpawnerPos() {
    	  CoordinateTransformer.setParams(this.orientation, this.boundingBox);
          BPos spawnerPos = CoordinateTransformer.getWorldPos(3, 5, 5);
          return spawnerPos;
      }
      
      public void addChildren(Piece var1, List<Piece> var2, JRand var3) {}
   }

   public static class StairsRoom extends Piece {
      public StairsRoom(int var1, BlockBox var2, BlockDirection var3) {
         super(PieceType.NETHER_FORTRESS_STAIRS_ROOM, var1);
         this.orientation = var3;
         this.boundingBox = var2;
      }

      public void addChildren(Piece var1, List<Piece> var2, JRand var3) {
         this.generateChildRight((FortressGenerator.StartPiece)var1, var2, var3, 6, 2, false);
      }

      public static FortressGenerator.StairsRoom createPiece(List<Piece> var0, int var1, int var2, int var3, int var4, BlockDirection var5) {
         BlockBox var6 = BlockBox.rotated(var1, var2, var3, -2, 0, 0, 7, 11, 7, var5.getRotation());
         return isOkBox(var6) && Piece.intersectsNone(var0, var6) ? new FortressGenerator.StairsRoom(var4, var6, var5) : null;
      }
   }

   public static class RoomCrossing extends Piece {
      public RoomCrossing(int var1, BlockBox var2, BlockDirection var3) {
         super(PieceType.NETHER_FORTRESS_ROOM_CROSSING, var1);
         this.orientation = var3;
         this.boundingBox = var2;
      }

      public void addChildren(Piece var1, List<Piece> var2, JRand var3) {
         this.generateChildForward((FortressGenerator.StartPiece)var1, var2, var3, 2, 0, false);
         this.generateChildLeft((FortressGenerator.StartPiece)var1, var2, var3, 0, 2, false);
         this.generateChildRight((FortressGenerator.StartPiece)var1, var2, var3, 0, 2, false);
      }

      public static FortressGenerator.RoomCrossing createPiece(List<Piece> var0, int var1, int var2, int var3, BlockDirection var4, int var5) {
         BlockBox var6 = BlockBox.rotated(var1, var2, var3, -2, 0, 0, 7, 9, 7, var4.getRotation());
         return isOkBox(var6) && Piece.intersectsNone(var0, var6) ? new FortressGenerator.RoomCrossing(var5, var6, var4) : null;
      }
   }

   public static class BridgeCrossing extends Piece {
      public BridgeCrossing(int var1, BlockBox var2, BlockDirection var3) {
         super(PieceType.NETHER_FORTRESS_BRIDGE_CROSSING, var1);
         this.orientation = var3;
         this.boundingBox = var2;
      }

      protected BridgeCrossing(JRand var1, int var2, int var3) {
         super(PieceType.NETHER_FORTRESS_BRIDGE_CROSSING, 0);
         this.orientation = BlockDirection.randomHorizontal(var1);
         
         if (this.orientation.getAxis() == BlockDirection.Axis.Z) {
            this.boundingBox = new BlockBox(var2, 64, var3, var2 + 19 - 1, 73, var3 + 19 - 1);
         } else {
            this.boundingBox = new BlockBox(var2, 64, var3, var2 + 19 - 1, 73, var3 + 19 - 1);
         }
      }

      public void addChildren(Piece var1, List<Piece> var2, JRand var3) {
         this.generateChildForward((FortressGenerator.StartPiece)var1, var2, var3, 8, 3, false);
         this.generateChildLeft((FortressGenerator.StartPiece)var1, var2, var3, 3, 8, false);
         this.generateChildRight((FortressGenerator.StartPiece)var1, var2, var3, 3, 8, false);
      }

      public static FortressGenerator.BridgeCrossing createPiece(List<Piece> var0, int var1, int var2, int var3, BlockDirection var4, int var5) {
         BlockBox var6 = BlockBox.rotated(var1, var2, var3, -8, -3, 0, 19, 10, 19, var4.getRotation());
         return isOkBox(var6) && Piece.intersectsNone(var0, var6) ? new FortressGenerator.BridgeCrossing(var5, var6, var4) : null;
      }
   }

   public static class BridgeEndFiller extends Piece {
      public BridgeEndFiller(int var1, JRand var2, BlockBox var3, BlockDirection var4) {
         super(PieceType.NETHER_FORTRESS_BRIDGE_END_FILLER, var1);
         this.orientation = var4;
         this.boundingBox = var3;
         var2.nextInt();
      }

      public static FortressGenerator.BridgeEndFiller createPiece(List<Piece> var0, JRand var1, int var2, int var3, int var4, BlockDirection var5, int var6) {
         BlockBox var7 = BlockBox.rotated(var2, var3, var4, -1, -3, 0, 5, 10, 8, var5.getRotation());
         return isOkBox(var7) && Piece.intersectsNone(var0, var7) ? new FortressGenerator.BridgeEndFiller(var6, var1, var7, var5) : null;
      }
      
      public void addChildren(Piece var1, List<Piece> var2, JRand var3) {}
   }

   public static class BridgeStraight extends Piece {
      public BridgeStraight(int var1, JRand var2, BlockBox var3, BlockDirection var4) {
         super(PieceType.NETHER_FORTRESS_BRIDGE_STRAIGHT, var1);
         this.orientation = var4;
         this.boundingBox = var3;
      }

      public void addChildren(Piece var1, List<Piece> var2, JRand var3) {
         this.generateChildForward((FortressGenerator.StartPiece)var1, var2, var3, 1, 3, false);
      }

      public static FortressGenerator.BridgeStraight createPiece(List<Piece> var0, JRand var1, int var2, int var3, int var4, BlockDirection var5, int var6) {
         BlockBox var7 = BlockBox.rotated(var2, var3, var4, -1, -3, 0, 5, 10, 19, var5.getRotation());
         return isOkBox(var7) && Piece.intersectsNone(var0, var7) ? new FortressGenerator.BridgeStraight(var6, var1, var7, var5) : null;
      }
   }

   
   public static class StartPiece extends FortressGenerator.BridgeCrossing {
      public PieceWeight previousPiece;
      public List<PieceWeight> availableBridgePieces;
      public List<PieceWeight> availableCastlePieces;
      public final List<Piece> pendingChildren = new ArrayList<>();

      public StartPiece(JRand var1, int var2, int var3) {
         super(var1, var2, var3);
         this.availableBridgePieces = new ArrayList<>();
         PieceWeight[] var4 = FortressGenerator.BRIDGE_PIECE_WEIGHTS;
         int var5 = var4.length;

         int var6;
         PieceWeight var7;
         for(var6 = 0; var6 < var5; ++var6) {
            var7 = var4[var6];
            var7.placeCount = 0;
            this.availableBridgePieces.add(var7);
         }

         this.availableCastlePieces = new ArrayList<>();
         var4 = FortressGenerator.CASTLE_PIECE_WEIGHTS;
         var5 = var4.length;

         for(var6 = 0; var6 < var5; ++var6) {
            var7 = var4[var6];
            var7.placeCount = 0;
            this.availableCastlePieces.add(var7);
         }

      }
   }

   abstract static class Piece {
	   public BlockBox boundingBox;
	   public BlockDirection orientation;
	   public int genDepth;
	   
      protected Piece(PieceType var1, int var2) {
    	  this.genDepth = var2;
      }

      public abstract void addChildren(Piece var1, List<Piece> var2, JRand var3);
      
      private int updatePieceWeight(List<PieceWeight> var1) {
         boolean var2 = false;
         int var3 = 0;

         PieceWeight var5;
         for(Iterator<PieceWeight> var4 = var1.iterator(); var4.hasNext(); var3 += var5.weight) {
            var5 = (PieceWeight)var4.next();
            if (var5.maxPlaceCount > 0 && var5.placeCount < var5.maxPlaceCount) {
               var2 = true;
            }
         }

         return var2 ? var3 : -1;
      }

      private Piece generatePiece(FortressGenerator.StartPiece var1, List<PieceWeight> var2, List<Piece> var3, JRand var4, int var5, int var6, int var7, BlockDirection var8, int var9) {
         int var10 = this.updatePieceWeight(var2);
         boolean var11 = var10 > 0 && var9 <= 30;
         int var12 = 0;

         while(var12 < 5 && var11) {
            ++var12;
            int var13 = var4.nextInt(var10);
            Iterator<PieceWeight> var14 = var2.iterator();

            while(var14.hasNext()) {
               PieceWeight var15 = (PieceWeight)var14.next();
               var13 -= var15.weight;
               if (var13 < 0) {
                  if (!var15.doPlace(var9) || var15 == var1.previousPiece && !var15.allowInRow) {
                     break;
                  }

                  Piece var16 = FortressGenerator.findAndCreateBridgePieceFactory(var15, var3, var4, var5, var6, var7, var8, var9);
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

         return FortressGenerator.BridgeEndFiller.createPiece(var3, var4, var5, var6, var7, var8, var9);
      }

      private Piece generateAndAddPiece(FortressGenerator.StartPiece var1, List<Piece> var2, JRand var3, int var4, int var5, int var6, BlockDirection var7, int var8, boolean var9) {
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
            return FortressGenerator.BridgeEndFiller.createPiece(var2, var3, var4, var5, var6, var7, var8);
         }
      }

      
      protected Piece generateChildForward(FortressGenerator.StartPiece var1, List<Piece> var2, JRand var3, int var4, int var5, boolean var6) {
         BlockDirection var7 = this.orientation;
         if (var7 != null) {
            switch(var7) {
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

      protected Piece generateChildLeft(FortressGenerator.StartPiece var1, List<Piece> var2, JRand var3, int var4, int var5, boolean var6) {
         BlockDirection var7 = this.orientation;
         if (var7 != null) {
            switch(var7) {
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

      protected Piece generateChildRight(FortressGenerator.StartPiece var1, List<Piece> var2, JRand var3, int var4, int var5, boolean var6) {
         BlockDirection var7 = this.orientation;
         if (var7 != null) {
            switch(var7) {
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
    	  for(Piece piece : pieceList) {
    		  BlockBox bb1 = piece.boundingBox;
    		  if(bb1.minX <= bb2.maxX && bb1.maxX >= bb2.minX && bb1.minZ <= bb2.maxZ && bb1.maxZ >= bb2.minZ && bb1.minY <= bb2.maxY && bb1.maxY >= bb2.minY)
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
   static enum PieceType {
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

