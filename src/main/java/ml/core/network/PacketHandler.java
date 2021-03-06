package ml.core.network;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.logging.Level;

import ml.core.internal.CoreLogger;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;

/**
 * Thanks to MachineMuse for the idea on how to implement this
 * @author Matchlighter
 */
public class PacketHandler implements IPacketHandler {
		
	protected static BiMap<Integer, Class<? extends MLPacket>> PacketTypes = HashBiMap.create();
	
	public static void addHandler(Class<? extends MLPacket> pktClass){
		PacketTypes.put(PacketTypes.size(), pktClass);
	}
	
	public static void addHandlers(List<Class<? extends MLPacket>> pktClss) {
		for (Class<? extends MLPacket> pktCls : pktClss) {
			addHandler(pktCls);
		}
	}
	
	@Override
	public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player player) {
		EntityPlayer entPl = (EntityPlayer)player;
		
		MLPacket mlPkt = tryCastPacket(packet, entPl);
		if (mlPkt != null){
			try {
				mlPkt.handle(entPl, FMLCommonHandler.instance().getEffectiveSide());
			} catch (Exception e) {
				onError(e, mlPkt);
				e.printStackTrace();
			}
		} else {
			CoreLogger.severe("("+this.getClass().toString()+") received unknown packet");
		}
	}
	
	protected void onError(Throwable e, MLPacket mlPkt) {
		CoreLogger.log(Level.SEVERE, "Error handling packet in channel ("+mlPkt.channel+")", e);
	}

	private static MLPacket tryCastPacket(Packet250CustomPayload pkt, EntityPlayer pl){
		ByteArrayDataInput dat = ByteStreams.newDataInput(pkt.data);
		int pkId = dat.readInt();
		if (PacketTypes.get(pkId) != null){
			try {
				Constructor<? extends MLPacket> contructor = PacketTypes.get(pkId).getConstructor(EntityPlayer.class, ByteArrayDataInput.class);
				MLPacket nPkt = contructor.newInstance(pl, dat);
				nPkt.channel = pkt.channel;
				return nPkt;
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
		return null;
	}
	
	public static int findPacketId(Class<? extends MLPacket> pktClass){
		return PacketTypes.inverse().get(pktClass);
	}
}
