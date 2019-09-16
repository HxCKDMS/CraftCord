package com.karelmikie3.craftcord.discord.Commands;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.stats.Stats;

public class CommandGetStat {
    public static String execute(MinecraftServer SERVER, String[] args) {
        //TODO Localize
        PlayerEntity p = SERVER.getPlayerList().getPlayerByUsername(args[args.length-1]);
        if (p == null) return "No Player With the name " + args[args.length-1] + " was found.";
        String type = args[0].toLowerCase();
        switch(type) {
            case "kills" :
                if (args.length < 3)
                    return "Command usage is kills <entityType> <playerName>";
                EntityType ent = getEntity(args[1].toLowerCase());
                int kills = SERVER.getPlayerList().getPlayerStats(p).getValue(Stats.ENTITY_KILLED.get(ent));
                return p.getDisplayName().getFormattedText() + " has killed [" + kills + "] " + ent.getName().getFormattedText();
            case "level":
                int level = p.experienceLevel;
                return p.getDisplayName().getFormattedText() + " is level [" + level + "] ";
            case "exp":
                int exp = p.experienceTotal;
                return p.getDisplayName().getFormattedText() + " has [" + exp + "] experience";
            case "stats":
                return p.getDisplayName().getFormattedText() + " has [" + p.getHealth() + "/" + p.getMaxHealth() + "] health [" + p.getFoodStats().getFoodLevel() + "] food [" + p.getFoodStats().getSaturationLevel() + "] saturation.";
            case "walked":
                int walked = (int) p.distanceWalkedModified;
                return p.getDisplayName().getFormattedText() + " has walked [" + walked + "] meters";
            default: return "Commands are kills/level/exp/stats/walked <playerName>";
        }
    }

    public static EntityType getEntity(String entity) {
        switch(entity) {
            case "skeleton": return EntityType.SKELETON;
            case "zombie": return EntityType.ZOMBIE;
            case "horse": return EntityType.HORSE;
            case "bat": return EntityType.BAT;
            case "blaze": return EntityType.BLAZE;
            case "spider": return EntityType.SPIDER;
            case "cavespider": return EntityType.CAVE_SPIDER;
            case "cat": return EntityType.CAT;
            case "wolf": return EntityType.WOLF;
            case "chicken": return EntityType.CHICKEN;
            case "cod": return EntityType.COD;
            case "cow": return EntityType.COW;
            case "pig": return EntityType.PIG;
            case "sheep": return EntityType.SHEEP;
            case "donkey": return EntityType.DONKEY;
            case "enderman": return EntityType.ENDERMAN;
            case "drowned": return EntityType.DROWNED;
            case "elder": return EntityType.ELDER_GUARDIAN;
            case "endermite": return EntityType.ENDERMITE;
            case "evoker": return EntityType.EVOKER;
            case "evokerfang": return EntityType.EVOKER_FANGS;
            case "crystal": return EntityType.END_CRYSTAL;
            case "fox": return EntityType.FOX;
            case "ghast": return EntityType.GHAST;
            case "giant": return EntityType.GIANT;
            case "guardian": return EntityType.GUARDIAN;
            case "husk": return EntityType.HUSK;
            case "illusioner": return EntityType.ILLUSIONER;
            case "llama": return EntityType.LLAMA;
            case "magmacube": return EntityType.MAGMA_CUBE;
            case "mule": return EntityType.MULE;
            case "mooshroom": return EntityType.MOOSHROOM;
            case "ocelot": return EntityType.OCELOT;
            case "panda": return EntityType.PANDA;
            case "parrot": return EntityType.PARROT;
            case "pufferfish": return EntityType.PUFFERFISH;
            case "pigman": return EntityType.ZOMBIE_PIGMAN;
            case "polarbear": return EntityType.POLAR_BEAR;
            case "rabbit": return EntityType.RABBIT;
            case "salmon": return EntityType.SALMON;
            case "shulker": return EntityType.SHULKER;
            case "silverfish": return EntityType.SILVERFISH;
            case "traderllama": return EntityType.TRADER_LLAMA;
            case "turtle": return EntityType.TURTLE;
            case "vex": return EntityType.VEX;
            case "villager": return EntityType.VILLAGER;
            case "irongolem": return EntityType.IRON_GOLEM;
            case "vindicator": return EntityType.VINDICATOR;
            case "wanteringtrader": return EntityType.WANDERING_TRADER;
            case "witch": return EntityType.WITCH;
            case "wither": return EntityType.WITHER;
            case "dragon":
            case "enderdragon": return EntityType.ENDER_DRAGON;
            case "pillager": return EntityType.PILLAGER;
            case "witherskeleton": return EntityType.WITHER_SKELETON;
            case "zombiehorse": return EntityType.ZOMBIE_HORSE;
            case "skeletonhorse": return EntityType.SKELETON_HORSE;
            case "phantom": return EntityType.PHANTOM;
            case "ravager": return EntityType.RAVAGER;
            case "slime" : return EntityType.SLIME;
            case "snowgolem": return EntityType.SNOW_GOLEM;
            default: return EntityType.PLAYER;
        }
    }
}
