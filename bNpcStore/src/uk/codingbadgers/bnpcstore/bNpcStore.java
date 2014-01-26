/**
 * bNpcStore 1.2-SNAPSHOT Copyright (C) 2013 CodingBadgers
 * <plugins@mcbadgercraft.com>
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.codingbadgers.bnpcstore;

import uk.codingbadgers.bnpcstore.database.DatabaseManager;
import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import static org.bukkit.Bukkit.getServer;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import uk.codingbadgers.bFundamentals.bFundamentals;
import uk.codingbadgers.bFundamentals.module.Module;
import uk.codingbadgers.bnpcstore.commands.NpcStoreCommand;
import uk.codingbadgers.bnpcstore.npc.NpcStoreListener;
import uk.codingbadgers.bnpcstore.gui.GuiInventory;
import uk.codingbadgers.bnpcstore.npc.StoreNPC;
import uk.thecodingbadgers.bDatabaseManager.Database.BukkitDatabase;
import uk.thecodingbadgers.bDatabaseManager.DatabaseTable.DatabaseTable;

public class bNpcStore extends Module {

    /** Static plugin module instance */
    static bNpcStore instance;
    
    /** A map of inventories and their names */
    private Map<String, GuiInventory> stores;
    
    /** A map of npcs and their stores */
    private Map<NPC, StoreNPC> npcStores;
    
    /** The vault economy item */
    private Economy economy; 
    
    /** The database manager **/
    private DatabaseManager databasemanager;
    
    /**
     * Called when the module is disabled.
     */
    @Override
    public void onDisable() {
        stores.clear();
        bNpcStore.instance = null;
    }

    /**
     * Called when the module is loaded.
     */
    @Override
    public void onEnable() {
        
        bNpcStore.instance = this;
        
        this.databasemanager = new DatabaseManager(this.m_plugin);
        
        // listeners
        register(new NpcStoreListener());
        
        // commands
        registerCommand(new NpcStoreCommand());
                
        // Load store guis
        loadStoreGuis();
        
        // Load store npcs
        loadStoreNPCs();       
        
    }
    
    /**
     * Output a message to someone
     * @param destination The thing to send the message to
     * @param message The message to send
     */
    public void output(CommandSender destination, String message) {
        Module.sendMessage(this.getName(), destination, message);
    }
    
    /**
     * Access to the module instance
     * @return 
     */
    public static bNpcStore getInstance() {
        return bNpcStore.instance;
    }
    
    /**
     * Access to the vault economy
     * @return The vault economy
     */
    public Economy getEconomy() {
        
        if (this.economy == null) {
            // Get the economy instance
            RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
            if (economyProvider != null) {
                this.economy = economyProvider.getProvider();
            }

            if (this.economy == null) {
                bFundamentals.log(Level.SEVERE, "Failed to find an economy plugin to use!");
            }
        }
        
        return this.economy;
    }

    /**
     * Load all store gui configs
     */
    private void loadStoreGuis() {
        
        this.stores = new HashMap<String, GuiInventory>();
        
        File storeFolder = new File(this.getDataFolder() + File.separator + "stores");
        if (!storeFolder.exists()) {
            storeFolder.mkdirs();
            createDefaultStore(storeFolder);
        }
        
        for (File folder : storeFolder.listFiles()) {
            
            if (!folder.isDirectory()) {
                continue;
            }
            
            FileConfiguration storeConfig = createConfigFile(folder + File.separator + "store.yml");
            FileConfiguration subMenuConfig = createConfigFile(folder + File.separator + "submenus.yml");
            FileConfiguration itemConfig = createConfigFile(folder + File.separator + "items.yml");
            
            GuiInventory inventory = new GuiInventory(m_plugin, storeConfig, subMenuConfig, itemConfig);
            this.stores.put(inventory.getTitle(), inventory);
            
            bFundamentals.log(Level.INFO, "Loaded store - " + inventory.getTitle());
        }
        
    }
    
    /**
     * 
     */
    private void loadStoreNPCs() {
        
        this.npcStores = new HashMap<NPC, StoreNPC>();
        DatabaseTable storeNpcTable = this.databasemanager.getNpcStoreTable();
        
        ResultSet result = this.databasemanager.getDatabase().queryResult("SELECT * FROM `bNpcStore-Npcs`");
		try {
			while(result.next()) {
                
                int ID = result.getInt("npcID");
                String storeName = result.getString("storeName");
                
                Bukkit.getLogger().log(Level.INFO, "Loading: " + ID + ", " + storeName);
                
                for (NPC npc : CitizensAPI.getNPCRegistry()) {
                    
                    if (npc.getId() == ID) {
                        
                        Bukkit.getLogger().log(Level.INFO, "Found NPC for " + ID);
                        
                        StoreNPC storeNPC = new StoreNPC(npc, false);
                        storeNPC.setStore(storeName);
                        this.registerNewNPC(storeNPC, npc);
                        break;
                    }
                    
                }
                
            }
            
			result.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

    }
    
    /**
     * 
     * @return 
     */
    public DatabaseManager getDatabaseManager() {
        return this.databasemanager;
    }

    /**
     * Create a default store as an example
     */
    private void createDefaultStore(File storeFolder) {
   
        storeFolder = new File(storeFolder + File.separator + "default");
 
        FileConfiguration storeConfig = createConfigFile(storeFolder + File.separator + "store.yml");
        FileConfiguration subMenuConfig = createConfigFile(storeFolder + File.separator + "submenus.yml");
        FileConfiguration itemConfig = createConfigFile(storeFolder + File.separator + "items.yml");
        
        // Setup the default store
        storeConfig.set("store.name", "Default Store");
        storeConfig.set("store.rows", 1);
        storeConfig.set("store.submenus", new String[] {"Example1", "Example2", "Example3"});
        storeConfig.set("store.items", new String[] {"TestItem"});
        
        // Setup the submenus
        for (int exampleIndex = 1; exampleIndex <= 3; ++exampleIndex) {
            String nodePath = "submenu.Example" + exampleIndex;
            subMenuConfig.set(nodePath + ".name", "Example " + exampleIndex);
            subMenuConfig.set(nodePath + ".icon", Material.GOLDEN_APPLE.name());
            subMenuConfig.set(nodePath + ".rows", exampleIndex + 1);
            subMenuConfig.set(nodePath + ".details", new String[] {"This is", "an example", "submenu"});
            subMenuConfig.set(nodePath + ".submenus", new String[] {});
            subMenuConfig.set(nodePath + ".items", new String[] {"Apple", "Potato", "Carrot"});
        }
        
        // Setup the items
        String[] items = new String[] {"Apple", "Potato", "Carrot", "TestItem"};
        Material[] icons = new Material[] {Material.APPLE, Material.POTATO_ITEM, Material.CARROT_ITEM, Material.DIAMOND};
        for (int exampleIndex = 0; exampleIndex < 4; ++exampleIndex) {
            String nodePath = "item." + items[exampleIndex];
            itemConfig.set(nodePath + ".name", items[exampleIndex]);
            itemConfig.set(nodePath + ".icon", icons[exampleIndex].name());
            itemConfig.set(nodePath + ".row", 0);
            itemConfig.set(nodePath + ".column", exampleIndex);
            itemConfig.set(nodePath + ".cost", exampleIndex * 17);
            itemConfig.set(nodePath + ".details", new String[] {"This is", "an example", "item"});
        }
        
        try {
            storeConfig.save(new File(storeFolder + File.separator + "store.yml"));
            subMenuConfig.save(new File(storeFolder + File.separator + "submenus.yml"));
            itemConfig.save(new File(storeFolder + File.separator + "items.yml"));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Create a configuration file at a given path
     * @param path The path to the config file.
     * @return A FileConfiguration for a file at the given path
     */
    private FileConfiguration createConfigFile(String path) {
        
        File file = new File(path);
        
        try  {
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
        } catch (IOException ex) {
            return null;
        }
        
        return YamlConfiguration.loadConfiguration(file);
    }

    /**
     * 
     * @param storeName
     * @return 
     */
    public GuiInventory getInventory(String storeName) {
        
        if (!this.stores.containsKey(storeName)) {
            Bukkit.getLogger().log(Level.WARNING, "Could not find a store with the name '" + storeName + "'");
            return null;
        }
        
        return this.stores.get(storeName);
    }
    
    /**
     * 
     * @return 
     */
    public List<String> getStoreNames() {
        
        List<String> names = new ArrayList<String>();
        
        for (String name : this.stores.keySet()) {
            names.add(name);
        }
        
        return names;
        
    }
    
    /**
     * 
     * @param npc
     * @return 
     */
    public StoreNPC findStoreNPC(NPC npc) {
        
        if (!this.npcStores.containsKey(npc)) {
            return null;
        }
        
        return this.npcStores.get(npc);
    }

    /**
     * 
     * @param storeNPC
     * @param npc 
     */
    public void registerNewNPC(StoreNPC storeNPC, NPC npc) {
        
        if (this.npcStores.containsKey(npc)) {
            return;
        }
        
        this.npcStores.put(npc, storeNPC);
        
    }

    /**
     * 
     * @param npc
     * @param storeName 
     */
    public void updateNpcStore(NPC npc, String storeName) {
        
        if (!this.npcStores.containsKey(npc)) {
            return;
        }
        
        StoreNPC storeNPC = this.npcStores.get(npc);
        this.npcStores.remove(npc);
        
        npc.setName(storeName);
        
        this.registerNewNPC(storeNPC, npc);
        
    }

    public void removeNPCStore(NPC npc) {
        
        if (!this.npcStores.containsKey(npc)) {
            return;
        }
        
        final String query = "DELETE FROM `bNpcStore-Npcs` WHERE `npcID`=" + npc.getId();
        this.databasemanager.getDatabase().query(query, true);
        
        this.npcStores.remove(npc);        
    }
    
}
