package com.github.bea4dev.vanilla_source.command;

import com.github.bea4dev.vanilla_source.api.VanillaSourceAPI;
import com.github.bea4dev.vanilla_source.api.player.EnginePlayer;
import com.github.bea4dev.vanilla_source.api.world.parallel.ParallelUniverse;
import com.github.bea4dev.vanilla_source.gui.UniverseGUI;
import com.github.bea4dev.vanilla_source.structure.StructureData;
import com.github.bea4dev.vanilla_source.util.RegionBlocks;
import com.github.bea4dev.vanilla_source.world_edit.WorldEditUtil;
import com.github.bea4dev.vanilla_source.structure.ParallelStructure;
import com.github.bea4dev.vanilla_source.structure.ImplStructureData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ParallelCommandExecutor implements CommandExecutor, TabExecutor {
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (args == null) return false;
        if (args.length == 0) return false;
    
        if(args[0].equals("structure")){
            if(args.length < 3){
                return false;
            }
        
            //parallel structure set-data [structure-name] [data-name] [player]
            if(args[1].equals("set-data")) {
                if(args.length < 5){
                    return false;
                }
            
                ParallelStructure parallelStructure = ParallelStructure.getParallelStructure(args[2]);
                if (parallelStructure == null) {
                    sender.sendMessage(ChatColor.RED + "指定された名前の構造体は存在しません。");
                    return true;
                }
            
                ImplStructureData implStructureData = (ImplStructureData) ImplStructureData.getStructureData(args[3]);
                if (implStructureData == null) {
                    sender.sendMessage(ChatColor.RED + "指定された名前の構造データは存在しません。");
                    return true;
                }
            
                Player player = Bukkit.getPlayer(args[4]);
                if(player == null){
                    sender.sendMessage(ChatColor.RED + "指定されたプレイヤーが見つかりませんでした。");
                    return true;
                }
            
                parallelStructure.setStructureData(player, implStructureData);
                sender.sendMessage(ChatColor.GREEN + "適用しました。");
                return true;
            }
        
            //parallel structure remove-data [structure-name] [player]
            if(args[1].equals("remove-data")) {
                if (args.length < 4) {
                    return false;
                }
            
                ParallelStructure parallelStructure = ParallelStructure.getParallelStructure(args[2]);
                if (parallelStructure == null) {
                    sender.sendMessage(ChatColor.RED + "指定された名前の構造体は存在しません。");
                    return true;
                }
            
                Player player = Bukkit.getPlayer(args[3]);
                if(player == null){
                    sender.sendMessage(ChatColor.RED + "指定されたプレイヤーが見つかりませんでした。");
                    return true;
                }
            
                parallelStructure.clearStructureData(player, true);
                sender.sendMessage(ChatColor.GREEN + "適用しました。");
                return true;
            }
        }
    
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "このコマンドはコンソールから実行できません。");
            return true;
        }

        if(args[0].equals("menu")){
            Player player = (Player) sender;
            UniverseGUI.openUniverseGUI(player);

            return true;
        }
    
        if(args[0].equals("join-universe")){
            if(args.length != 2) return false;
            
            String universeName = args[1];
            ParallelUniverse universe = VanillaSourceAPI.getInstance().getUniverse(universeName);
            
            if(universe == null){
                sender.sendMessage("§aThe universe is not found.");
                return true;
            }
    
            EnginePlayer enginePlayer = EnginePlayer.getEnginePlayer((Player) sender);
            if(enginePlayer == null) return false;
            
            enginePlayer.setUniverse(universe);
            enginePlayer.getBukkitPlayer().sendMessage("§7Switched to §r" + universe.getName());
        }
    
        if(args[0].equals("leave-universe")){
            EnginePlayer enginePlayer = EnginePlayer.getEnginePlayer((Player) sender);
            if(enginePlayer == null) return false;
        
            enginePlayer.setUniverse(VanillaSourceAPI.getInstance().getDefaultUniverse());
        }
        
        //parallel structure-data create [name]
        if(args[0].equals("structure-data")){
            if(args.length < 3){
                return false;
            }
            if(args[1].equals("create")) {
                Player player = (Player) sender;
                RegionBlocks regionBlocks = WorldEditUtil.getSelectedRegion(player);
                if (regionBlocks == null) {
                    return true;
                }
    
                ImplStructureData implStructureData = (ImplStructureData) StructureData.getStructureData(args[2]);
                if (implStructureData != null) {
                    sender.sendMessage(ChatColor.RED + "指定された名前の構造データは既に存在しています。");
                    return true;
                }
                implStructureData = new ImplStructureData(args[2]);
    
                implStructureData.setBlockData(regionBlocks.getMinimum().toLocation(player.getWorld()), regionBlocks.getBlocks());
                sender.sendMessage(ChatColor.GREEN + "作成しました。");
                return true;
            }
    
            if(args[1].equals("save")) {
                ImplStructureData implStructureData = (ImplStructureData) ImplStructureData.getStructureData(args[2]);
                if (implStructureData == null) {
                    sender.sendMessage(ChatColor.RED + "指定された名前の構造データは存在しません。");
                    return true;
                }
                
                implStructureData.saveData();
                sender.sendMessage(ChatColor.GREEN + "保存しました。");
                return true;
            }
        }
    
        
        if(args[0].equals("structure")){
            if(args.length < 3){
                return false;
            }
            //parallel structure create [name]
            if(args[1].equals("create")) {
                Player player = (Player) sender;
                RegionBlocks regionBlocks = WorldEditUtil.getSelectedRegion(player);
                if (regionBlocks == null) {
                    return true;
                }
            
                ParallelStructure parallelStructure = ParallelStructure.getParallelStructure(args[2]);
                if (parallelStructure != null) {
                    sender.sendMessage(ChatColor.RED + "指定された名前の構造体は既に存在しています。");
                    return true;
                }
                parallelStructure = new ParallelStructure(args[2]);
                parallelStructure.setBaseLocation(regionBlocks.getMinimum().toLocation(player.getWorld()));
                
                sender.sendMessage(ChatColor.GREEN + "作成しました。");
                return true;
            }
    
            //parallel structure save [name]
            if(args[1].equals("save")) {
                ParallelStructure parallelStructure = ParallelStructure.getParallelStructure(args[2]);
                if (parallelStructure == null) {
                    sender.sendMessage(ChatColor.RED + "指定された名前の構造体は存在しません。");
                    return true;
                }
    
                parallelStructure.saveData();
                sender.sendMessage(ChatColor.GREEN + "保存しました。");
                return true;
            }
        }
        
        return true;
    }
    
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
    
        List<String> list = new ArrayList<>();
    
        if (args.length == 1) {
            list.add("structure-data");
            list.add("structure");
            list.add("menu");
            list.add("join-universe");
            list.add("leave-universe");
        
            return list;
        }
    
        if (args.length == 2) {
            if(args[0].equals("structure")){
                list.add("set-data");
                list.add("remove-data");
                list.add("create");
                list.add("save");
            }
            
            if(args[0].equals("structure-data")){
                list.add("create");
                list.add("save");
            }
            
            if(args[0].equals("join-universe")){
                list.addAll(VanillaSourceAPI.getInstance().getAllUniverseName());
            }
        
            return list;
        }
    
        if (args.length == 3) {
            if(args[0].equals("structure")){
                if(args[1].equals("create")) {
                    list.add("[structure-name]");
                }else{
                    list = new ArrayList<>(ParallelStructure.getStructureMap().keySet());
                }
            }else{
                if(args[1].equals("create")) {
                    list.add("[data-name]");
                }else{
                    list = new ArrayList<>(ImplStructureData.getStructureDataMap().keySet());
                }
            }
        
            return list;
        }
    
        if (args.length == 4) {
            if(args[1].equals("set-data")){
                list = new ArrayList<>(ImplStructureData.getStructureDataMap().keySet());
                return list;
            }
        }
        
        return null;
    }
    
}
