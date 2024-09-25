package com.github.bea4dev.vanilla_source.config;

import com.github.bea4dev.vanilla_source.VanillaSource;
import com.github.bea4dev.vanilla_source.api.setting.VSSettings;
import com.github.bea4dev.vanilla_source.lang.SystemLanguage;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class ImplVSSettings extends VSSettings {

    public static void load(){
        VanillaSource.getPlugin().getLogger().info("Loading config file.");
        
        File file = new File("plugins/VanillaSource", "config.yml");
        file.getParentFile().mkdirs();
    
        if(!file.exists()){
            VanillaSource.getPlugin().saveResource("config.yml", false);
        }
    
        //ロードと値の保持
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);
        
        if (yml.contains("use-cached-chunk-packet")) useCachedChunkPacket = yml.getBoolean("use-cached-chunk-packet");
        if (yml.contains("rewrite-light-packet")) rewriteLightPacket = yml.getBoolean("rewrite-light-packet");
        if (yml.contains("entity-threads")) entityThreads = yml.getInt("entity-threads");
        if (yml.contains("chiyogami-parallel-bridge")) useChiyogamiParallelBridge = yml.getBoolean("chiyogami-parallel-bridge");
        if (yml.contains("system-language")) SystemLanguage.setLang(yml.getString("system-language"));
    }
    
}
