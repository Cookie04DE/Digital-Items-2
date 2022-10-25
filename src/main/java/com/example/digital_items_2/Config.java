package com.example.digital_items_2;

import net.minecraftforge.common.ForgeConfigSpec;

public class Config {

    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final DECAY DECAY = new DECAY(BUILDER);
    public static final ENERGY ENERGY = new ENERGY(BUILDER);
    public static final ForgeConfigSpec spec = BUILDER.build();

    public static class DECAY {
        public final ForgeConfigSpec.ConfigValue<Boolean> enabled;
        public final ForgeConfigSpec.ConfigValue<Long> decayTicks;

        public DECAY(ForgeConfigSpec.Builder builder) {
            builder.comment("Item decay options");
            builder.push("decay");
            builder.comment("Do digital items decay over time? Default: true");
            enabled = builder.define("enabled", true);
            builder.comment("After how many ticks do digital items decay? Default: 120000 (5 in game days)");
            decayTicks = builder.defineInRange("decay_ticks", (long) 20 * 60 * 20 * 5, 0, Long.MAX_VALUE);
            builder.pop();
        }
    }

    public static class ENERGY {
        public final ForgeConfigSpec.ConfigValue<Boolean> enabled;
        public final ForgeConfigSpec.ConfigValue<Integer> maxEnergy;
        public final ForgeConfigSpec.ConfigValue<Integer> perTickDraw;
        public final ForgeConfigSpec.ConfigValue<Integer> digitizationCost;
        public final ForgeConfigSpec.ConfigValue<Integer> rematerializationCost;
        public final ForgeConfigSpec.ConfigValue<Integer> idInfo;
        public final ForgeConfigSpec.ConfigValue<Integer> refreshCost;

        private static final int defaultDigitizeCost = 240 * 20 * 5; // max output of mekanism generators advanced solar generator: 240 RF/t; 20 ticks per second; 5 seconds maximum output for one digitization
        public ENERGY(ForgeConfigSpec.Builder builder) {
            builder.comment("Energy options (all energy units in FE/RF)");
            builder.push("energy");

            builder.comment("Does the digitizer require power to operate? Default: true");
            enabled = builder.define("enabled", true);
            builder.comment("How much energy can the digitizer store? Default: 480000");
            maxEnergy = builder.defineInRange("max_energy", defaultDigitizeCost * 20, 0, Integer.MAX_VALUE); // enough energy to digitize 20 times without refill
            builder.comment("How much energy can the digitizer draw per tick? Default: 200");
            perTickDraw = builder.defineInRange("per_tick_draw", (defaultDigitizeCost * 20) / (20 * 2 * 60), 0, Integer.MAX_VALUE); // draws enough to refill entire digitizer within 2 minutes
            builder.comment("How much energy does a digitization require? Default: 24000");
            digitizationCost = builder.defineInRange("digitization_cost", defaultDigitizeCost, 0, Integer.MAX_VALUE);
            builder.comment("How much energy does a rematerialization cost? Default: 12000");
            rematerializationCost = builder.defineInRange("rematerialization_cost", defaultDigitizeCost / 2, 0, Integer.MAX_VALUE);
            builder.comment("How much energy does an ID based information request cost? Default: 4000");
            idInfo = builder.defineInRange("id_info", defaultDigitizeCost / 6, 0, Integer.MAX_VALUE);
            builder.comment("How much energy does an id refresh (which resets the decay of that id) cost? Default: 6000");
            refreshCost = builder.defineInRange("refresh_cost", defaultDigitizeCost / 4, 0, Integer.MAX_VALUE);

            builder.pop();
        }
    }
}
