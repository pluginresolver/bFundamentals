package uk.thecodingbadgers.bFundamentals.support;

import net.minecraft.server.v1_7_R1.Enchantment;

public class DummyEnchantments {
	
	static {
        Enchantment.byId.getClass();
        org.bukkit.enchantments.Enchantment.stopAcceptingRegistrations();
    }

    public static void setup() {}
    
}
