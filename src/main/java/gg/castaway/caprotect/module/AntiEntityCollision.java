package gg.castaway.caprotect.module;

import com.google.common.collect.Sets;
import gg.castaway.caprotect.AbstractModule;
import gg.castaway.caprotect.CAProtect;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Minecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.inventory.ItemStack;

public class AntiEntityCollision extends AbstractModule {
	
	private static final int MINECARTS_PER_BLOCK = 10;
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
					if (!chunk.isLoaded()) continue;
					Entity[] entities = chunk.getEntities();
					if (entities.length <= MINECARTS_PER_BLOCK) {
						continue;
					}
					
					for (Entity entity : entities) {
						if (isValidMinecart(entity)) {
							int nearbyMinecartCount = getNearbyMinecartCount(entities, entity.getLocation());
							if (nearbyMinecartCount > MINECARTS_PER_BLOCK) {
								entity.remove();
								destroyed++;
							}
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
	
	private int getNearbyMinecartCount(Entity[] entities, Location location) {
		int foundMinecarts = 0;
		for (Entity entity : entities) {
			if (!isValidMinecart(entity)) {
				continue;
			}
			
			double dx = location.getX() - entity.getLocation().getX();
			double dy = location.getY() - entity.getLocation().getY();
			double dz = location.getZ() - entity.getLocation().getZ();
			double ds = dx * dx + dy * dy + dz * dz;
			if (ds > 1) {
				continue;
			}
			
			// It's a minecart and it's in range
			foundMinecarts++;
			if (foundMinecarts > MINECARTS_PER_BLOCK) {
				return foundMinecarts; // We don't really care at this point, there are already too many so no need to check the rest
			}
		}
		
		return foundMinecarts;
	}
	
	private boolean isValidMinecart(Entity e) {
		return e instanceof Minecart;
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
