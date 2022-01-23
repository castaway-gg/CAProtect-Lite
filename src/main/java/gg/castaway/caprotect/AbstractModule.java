package gg.castaway.caprotect;

import java.util.logging.Logger;
import lombok.Getter;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

public abstract class AbstractModule implements Module, Listener {
	
	@Getter
	private final String name;
	
	protected final Logger logger;
	protected final Plugin plugin;
	
	protected AbstractModule(CAProtect plugin, String name) {
		this.plugin = plugin;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		this.name = name;
		this.logger = Logger.getLogger(plugin.getLogger().getName() + "-" + name);
		logger.info(() -> "Registered successfully");
	}
	
	@Override
	public void onEnable() {
		// Not implemented
	}
}
