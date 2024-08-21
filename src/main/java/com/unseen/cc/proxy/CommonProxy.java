package com.unseen.cc.proxy;

import com.unseen.cc.Main;
import com.unseen.cc.client.animation.AnimationMessage;
import com.unseen.cc.util.ModReference;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;

public class CommonProxy {


    public EntityPlayer getClientPlayer() {
        return null;
    }

    public World getClientWorld() {
        return getClientPlayer().world;
    }

    public void init() {
        int packetId = 0;
        Main.network = NetworkRegistry.INSTANCE.newSimpleChannel(ModReference.CHANNEL_NETWORK_NAME);
        Main.network.registerMessage(AnimationMessage.Handler.class, AnimationMessage.class, packetId++, Side.SERVER);
    }

    public void handleAnimationPacket(int entityId, int index) {

    }
}
