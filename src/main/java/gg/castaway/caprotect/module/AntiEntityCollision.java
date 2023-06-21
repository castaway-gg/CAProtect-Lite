package gg.castaway.caprotect.module;

import com.google.common.collect.Sets;
import gg.castaway.caprotect.AbstractModule;
import gg.castaway.caprotect.CAProtect;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class AntiEntityCollision extends AbstractModule {

	private static final int ENTITIES_PER_BLOCK = 10;
	private final Set<World> worlds = Sets.newConcurrentHashSet();

	// Prevents the player from cramming a ton of entities into one space in order to lag with collisions
	public AntiEntityCollision(CAProtect plugin) {
		super(plugin, "AntiEntityCollision");
	}

	@Override
	public void onEnable() {
		worlds.clear();
		worlds.addAll(plugin.getServer().getWorlds());
		plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, this::process, 100, 100);
	}

	private void process() {
		for (World world : worlds) {
			Chunk[] chunks = world.getLoadedChunks();
			plugin.getServer().getScheduler().runTask(plugin, () -> {
				int destroyed = 0;
				for (Chunk chunk : chunks) {
					if (!world.isChunkLoaded(chunk)) continue;
					List<Entity> entities = getEntities(chunk);
					if (entities.size() <= ENTITIES_PER_BLOCK) {
						continue;
					}
					for (Entity entity : entities) {
						if (entities.size() > ENTITIES_PER_BLOCK) {
							entity.remove();
							destroyed++;
						}
					}
				}
				if (destroyed > 0) {
					final int d = destroyed;
					plugin.getLogger().log(Level.INFO,
							() -> "Destroyed " + d + " minecarts in " + world.getName() + " to prevent lag machine");
				}
			});
		}
	}
	/***
	 Returns all entities which are an instance of a minecart or boat in a chunk
	 @param chunk minecraft world chunk
	 @return Entity list of minecarts and boats in that chunk
	 */
	List<Entity> getEntities(final Chunk chunk) {
		return Arrays.stream(chunk.getEntities()).filter(e -> e instanceof Minecart || e instanceof Boat).collect(Collectors.toList());
	}
	@EventHandler
	public void onWorldLoad(WorldLoadEvent e) {
		worlds.add(e.getWorld());
	}

	@EventHandler
	public void onWorldUnload(WorldUnloadEvent e) {
		worlds.remove(e.getWorld());
	}

}
