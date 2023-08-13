package kludwisz.fortressgen;

import java.util.List;

import com.seedfinding.mccore.rand.ChunkRand;
import com.seedfinding.mccore.util.data.Pair;
import com.seedfinding.mccore.util.pos.BPos;
import com.seedfinding.mccore.util.pos.CPos;
import com.seedfinding.mccore.version.MCVersion;
import com.seedfinding.mcfeature.loot.LootContext;
import com.seedfinding.mcfeature.loot.MCLootTables;
import com.seedfinding.mcfeature.loot.item.ItemStack;

public class Main {
	public static final ChunkRand rand = new ChunkRand();
	
	public static void main(String [] args) {
		test(12345L, new CPos(-68, -89));
	}
	
	// for in-game testing
	public static void test(long structseed, CPos fortPos) {
		if (!FortressGenerator.generateFortress(12345L, fortPos.getX(), fortPos.getZ(), rand, false))
			return;
		
		List<Pair<BPos, Long>> chests = FortressGenerator.getChestPositionsWithLootseeds(rand, MCVersion.v1_16_1);
		for (Pair<BPos, Long> p : chests) {
			System.out.println("/tp " + p.getFirst().getX() + " " + p.getFirst().getY() + " " + p.getFirst().getZ());
			LootContext ctx = new LootContext(p.getSecond());
			List<ItemStack> items = MCLootTables.NETHER_BRIDGE_CHEST.get().generate(ctx);
			for (ItemStack is : items) {
				System.out.println(is.getItem().getName() + " " + is.getCount());
			}
		}
		
		List<BPos> spawners = FortressGenerator.getSpawnerPositions();
		System.out.println("\n\nSpawners");
		for (BPos b : spawners) {
			System.out.println("/tp " + b.getX() + " " + b.getY() + " " + b.getZ());
		}
		
		// findDecoSalt();
		// DECORATION SALT: 70000 (index 0 step 7)
	}
	
	@SuppressWarnings("unused")
	private static void findDecoSalt() {
		CPos c = new CPos(-19, 3);
		
		for (int step=0; step<10; step++) for (int index=0; index<10; index++) {
			rand.setDecoratorSeed(12345L, c.getX()<<4, c.getZ()<<4, index, step, MCVersion.v1_16_1);
			LootContext ctx = new LootContext(rand.nextLong());
			
			List<ItemStack> items = MCLootTables.NETHER_BRIDGE_CHEST.get().generate(ctx);
			int fitness = 0;
			for (ItemStack is : items) {
				if (is.getItem().getName() == "golden_horse_armor") {
					if (is.getCount() != 1)
						break;
					fitness++;
				}
				else if (is.getItem().getName() == "saddle") {
					if (is.getCount() != 1)
						break;
					fitness++;
				} else break;
			}
			
			if (fitness == 2) System.out.println(step + " " + index);
		}
	}
}
