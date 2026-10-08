package com.tokyoghoul.rpg.client;

import com.tokyoghoul.rpg.network.SyncDataPacket;
import com.tokyoghoul.rpg.capability.GhoulData;

public final class ClientGhoulData {
    private static int race,points,level,rc,strength,speed,kagune,regen,stealth;
    private static int investigation,quinque,tactics,armor,discipline,quinqueMastery,rage,rageTicks; private static boolean quinqueBleed,quinquePoison,quinqueBlind,quinqueSlow;
    private static boolean rageUnlocked,kaguneActive;
    private static int kaguneType, hunger, bleedingTicks;

    public static void set(SyncDataPacket p){
        race=p.race(); points=p.points(); level=p.level(); rc=p.rc();
        strength=p.strength(); speed=p.speed(); kagune=p.kagune(); regen=p.regen(); stealth=p.stealth();
        investigation=p.investigation(); quinque=p.quinque(); tactics=p.tactics(); armor=p.armor(); discipline=p.discipline(); quinqueMastery=p.quinqueMastery(); quinqueBleed=p.quinqueBleed(); quinquePoison=p.quinquePoison(); quinqueBlind=p.quinqueBlind(); quinqueSlow=p.quinqueSlow();
        rage=p.rage(); rageTicks=p.rageTicks(); rageUnlocked=p.rageUnlocked(); kaguneActive=p.kaguneActive();
        kaguneType=p.kaguneType(); hunger=p.hunger(); bleedingTicks=p.bleedingTicks();
    }

    public static GhoulData.Race race(){
        GhoulData.Race[] values=GhoulData.Race.values();
        return race>=0 && race<values.length ? values[race] : GhoulData.Race.HUMAN;
    }
    public static int points(){return points;}
    public static int level(){return level;}
    public static int rc(){return rc;}
    public static int rage(){return rage;}
    public static int rageTicks(){return rageTicks;}
    public static boolean rageUnlocked(){return rageUnlocked;}
    public static boolean rageActive(){return rageTicks>0;}
    public static boolean kaguneActive(){return kaguneActive;}
    public static int kaguneType(){return kaguneType;}
    public static int hunger(){return hunger;}
    public static int bleedingTicks(){return bleedingTicks;}
    public static int strength(){return strength;}
    public static int speed(){return speed;}
    public static int kagune(){return kagune;}
    public static int regen(){return regen;}
    public static int stealth(){return stealth;}
    public static int investigation(){return investigation;}
    public static int quinque(){return quinque;}
    public static int tactics(){return tactics;}
    public static int armor(){return armor;}
    public static int discipline(){return discipline;}
    public static int quinqueMastery(){return quinqueMastery;} public static boolean quinqueBleed(){return quinqueBleed;} public static boolean quinquePoison(){return quinquePoison;} public static boolean quinqueBlind(){return quinqueBlind;} public static boolean quinqueSlow(){return quinqueSlow;}

    private ClientGhoulData(){}
}
