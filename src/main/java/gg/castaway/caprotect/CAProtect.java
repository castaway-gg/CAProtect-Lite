package gg.castaway.caprotect;

import gg.castaway.caprotect.module.AntiEntityCollision;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.plugin.java.JavaPlugin;

public class CAProtect extends JavaPlugin {
	
	@Override
	public void onEnable() {
		List<Module> modules = new ArrayList<>();
		modules.add(new AntiEntityCollision(this));
		
		modules.forEach(Module::onEnable);
	}
}
