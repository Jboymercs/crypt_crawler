package com.unseen.cc;

import com.unseen.cc.handlers.ModSoundHandler;
import com.unseen.cc.init.ModDimensions;
import com.unseen.cc.init.ModEntities;
import com.unseen.cc.init.ModStructures;
import com.unseen.cc.proxy.CommonProxy;
import com.unseen.cc.util.ModReference;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = ModReference.MOD_ID, name = ModReference.NAME, version = ModReference.VERSION)
public class Main {

    @SidedProxy(clientSide = ModReference.CLIENT_PROXY_CLASS, serverSide = ModReference.COMMON_PROXY_CLASS)
    public static CommonProxy proxy;
    public static Logger LOGGER = LogManager.getLogger(ModReference.MOD_ID);
    public static SimpleNetworkWrapper network;
    @Mod.Instance
    public static Main instance;

    public Main() {

    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        //Handles Entities
        ModEntities.registerEntities();
        ModEntities.registerEntitySpawns();
        //Proxy Init
        proxy.init();
        //Registers Dimensions
        ModDimensions.registerDimensions();
    }

    @EventHandler
    public void init(FMLInitializationEvent e) {
        //Handles Structure Registration
        ModStructures.handleStructureRegistries();
        //Handles Sounds Registration
        ModSoundHandler.registerSounds();
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent e) {

    }


    public static <MSG extends IMessage> void sendMSGToAll(MSG message) {

        //  for(EntityPlayerMP playerMP : Minecraft.getMinecraft().) {
        //  sendNonLocal(message, playerMP);
        //  }
        //network.sendToAll(message);
    }


    public static <MSG extends IMessage> void sendNonLocal(MSG message, EntityPlayerMP playerMP) {
        network.sendTo(message, playerMP);
    }
}
