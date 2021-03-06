package ml.core.gui.core;

import ml.core.gui.event.EventDataPacketReceived;
import ml.core.gui.event.EventGuiClosing;
import ml.core.internal.PacketContainerData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet250CustomPayload;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.relauncher.Side;

public class MLContainer extends Container {

	protected TopParentGuiElement priElemement;

	public MLContainer(TopParentGuiElement elm) {
		priElemement = elm;
	}

	public TopParentGuiElement getPrimaryElement() {
		return priElemement;
	}
	
	@Override
	public boolean canInteractWith(EntityPlayer entityplayer) {
		return priElemement.canInteractWith(entityplayer);
	}

	@Override
	public void onContainerClosed(EntityPlayer par1EntityPlayer) {
		priElemement.injectEvent(new EventGuiClosing(priElemement));
		super.onContainerClosed(par1EntityPlayer);
	}
	
	/**
	 * For data transmission when 2 shorts isn't enough
	 */
	public void handleDataPacket(NBTTagCompound pload, Side side) {
		priElemement.injectEvent(new EventDataPacketReceived(priElemement, pload, side)); // TODO Test this stuff
	}

	public void sendPacket(NBTTagCompound payload, Side sendTo) {
		Packet250CustomPayload pkt = new PacketContainerData(windowId, payload).convertToPkt250();
		if (sendTo == Side.SERVER) {
			PacketDispatcher.sendPacketToServer(pkt);
		} else {
			PacketDispatcher.sendPacketToPlayer(pkt, (Player)priElemement.player);
		}
	}
	
	@Override
	public ItemStack transferStackInSlot(EntityPlayer par1EntityPlayer, int par2) {
		Slot slt = getSlot(par2);
		return priElemement.transferStackFromSlot(par1EntityPlayer, slt);
	}

	/**
	 * <u>DO NOT</u> call dynamically. Call once per slot on init. Not again. Must be synced between Client and server. <br/>
	 * <b>Note:</b> ControlSlot automatically handles this on instantiation.
	 */
	public Slot addSlotToContainer(Slot par1Slot) {
		return super.addSlotToContainer(par1Slot);
	}
}
