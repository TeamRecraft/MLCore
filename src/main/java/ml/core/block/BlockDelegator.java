package ml.core.block;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.IPlantable;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

/**
 * Partially inspired by OpenComputers.
 * 
 * @author Matchlighter
 */
public class BlockDelegator <DCls extends Delegate> extends Block {
	
	private BiMap<Integer, DCls> subBlocks = HashBiMap.create();
	
	protected DCls nullDelegate;

	public BlockDelegator(int blockId, Material xMaterial, DCls nullDelegate) {
		super(blockId, xMaterial);
		this.nullDelegate = nullDelegate;
		this.nullDelegate.parent = this;
	}
	
	/* ---------------------------- SubBlocks ---------------------------- */
	
	public boolean addSubBlock(int metaData, DCls sub) {
		if (subBlocks.containsKey(metaData) || subBlocks.containsValue(sub)) return false;
		subBlocks.put(metaData, sub);
		sub.parent = this;
		sub.metaId = metaData;
		return true;
	}
	
	public DCls subBlock(int metaData) {
		if (subBlocks.containsKey(metaData)) return subBlocks.get(metaData);
		return nullDelegate;
	}
	
	public DCls subBlock(ItemStack is) {
		if (is != null && is.getItem() instanceof ItemBlock) {
			ItemBlock ib = (ItemBlock)is.getItem();
			Block blk = Block.blocksList[ib.getBlockID()]; 
			if (blk == this) {
				return subBlock(is.getItemDamage());
			}
		}
		return nullDelegate;
	}
	
	public DCls subBlock(IBlockAccess world, int x, int y, int z) {
		Block blk = Block.blocksList[world.getBlockId(x, y, z)];
		if (blk == this) {
			return subBlock(world.getBlockMetadata(x, y, z));
		}
		return nullDelegate;
	}
	
	public static Delegate findSubBlock(ItemStack is) {
		if (is != null && is.getItem() instanceof ItemBlock) {
			ItemBlock ib = (ItemBlock)is.getItem();
			Block blk = Block.blocksList[ib.getBlockID()]; 
			if (blk instanceof BlockDelegator<?>) {
				return ((BlockDelegator<?>)blk).subBlock(is.getItemDamage());
			}
		}
		return null;
	}
	
	public static Delegate findSubBlock(IBlockAccess world, int x, int y, int z) {
		Block blk = Block.blocksList[world.getBlockId(x, y, z)];
		if (blk instanceof BlockDelegator<?>) {
			return ((BlockDelegator<?>)blk).subBlock(world.getBlockMetadata(x, y, z));
		}
		return null;
	}
	
	/* ---------------------------- ItemMethods ---------------------------- */
	
	public String getUnlocalizedName(ItemStack stack) {
		return subBlock(stack).getUnlocalizedName(stack);
	}
	
	public void addInformation(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, List par3List, boolean par4) {
		subBlock(par1ItemStack).addInformation(par1ItemStack, par2EntityPlayer, par3List, par4);
	}
	
	@Override
	public void getSubBlocks(int par1, CreativeTabs par2CreativeTabs, List par3List) {
		for (Integer i : subBlocks.keySet()) {
			par3List.add(new ItemStack(this, 1, i));
		}
	}
	
	/* ---------------------------- WorldMethods ---------------------------- */
	
	public void setBlockAt(DCls delegate, World world, int wx, int wy, int wz, int flags) {
		world.setBlock(wx, wy, wz, blockID, delegate.metaId, flags);
	}
	
	/* ---------------------------- BlockMethods ---------------------------- */
	
	@Override
	public float getExplosionResistance(Entity par1Entity, World world, int x, int y, int z, double explosionX, double explosionY, double explosionZ) {
		return subBlock(world, x, y, z).getExplosionResistance(par1Entity, world, x, y, z, explosionX, explosionY, explosionZ);
	}
	
	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
		subBlock(world, x, y, z).setBlockBoundsBasedOnState(world, x, y, z);
	}
	
	@Override
	public MovingObjectPosition collisionRayTrace(World world, int x, int y, int z, Vec3 par5Vec3, Vec3 par6Vec3) {
		return subBlock(world, x, y, z).collisionRayTrace(world, x, y, z, par5Vec3, par6Vec3);
	}
	
	public MovingObjectPosition superCollisionRayTrace(World world, int x, int y, int z, Vec3 par5Vec3, Vec3 par6Vec3) {
		return super.collisionRayTrace(world, x, y, z, par5Vec3, par6Vec3);
	}
	
	@Override
	public int getLightValue(IBlockAccess world, int x, int y, int z) {
		return subBlock(world, x, y, z).getLightValue(world, x, y, z);
	}
	
	@Override
	public int getLightOpacity(World world, int x, int y, int z) {
		return subBlock(world, x, y, z).getLightOpacity(world, x, y, z);
	}
	
	@Override
	public boolean canBeReplacedByLeaves(World world, int x, int y, int z) {
		return subBlock(world, x, y, z).canBeReplacedByLeaves(world, x, y, z);
	}
	
	@Override
	public boolean canCreatureSpawn(EnumCreatureType type, World world, int x, int y, int z) {
		return subBlock(world, x, y, z).canCreatureSpawn(type, world, x, y, z);
	}
	
	@Override
	public String getUnlocalizedName() {
		// TODO Auto-generated method stub
		return super.getUnlocalizedName();
	}
	
	@Override
	public boolean canSilkHarvest(World world, EntityPlayer player, int x, int y, int z, int metadata) {
		return subBlock(world, x, y, z).canSilkHarvest(world, player, x, y, z, metadata);
	}
	
	@Override
	public boolean canSustainLeaves(World world, int x, int y, int z) {
		return subBlock(world, x, y, z).canSustainLeaves(world, x, y, z);
	}
	
	@Override
	public boolean canSustainPlant(World world, int x, int y, int z, ForgeDirection direction, IPlantable plant) {
		return subBlock(world, x, y, z).canSustainPlant(world, x, y, z, direction, plant);
	}
	
	@Override
	public void velocityToAddToEntity(World world, int x, int y, int z, Entity par5Entity, Vec3 par6Vec3) {
		subBlock(world, x, y, z).velocityToAddToEntity(world, x, y, z, par5Entity, par6Vec3);
	}
	
	@Override
	public boolean isLadder(World world, int x, int y, int z, EntityLivingBase entity) {
		return subBlock(world, x, y, z).isLadder(world, x, y, z, entity);
	}
	
	@Override
	public boolean isFertile(World world, int x, int y, int z) {
		return subBlock(world, x, y, z).isFertile(world, x, y, z);
	}
	
	@Override
	public boolean isAirBlock(World world, int x, int y, int z) {
		return subBlock(world, x, y, z).isAirBlock(world, x, y, z);
	}
	
	@Override
	public boolean isFlammable(IBlockAccess world, int x, int y, int z, int metadata, ForgeDirection face) {
		return subBlock(world, x, y, z).isFlammable(world, x, y, z, metadata, face);
	}
	
	@Override
	public boolean isBlockBurning(World world, int x, int y, int z) {
		return subBlock(world, x, y, z).isBlockBurning(world, x, y, z);
	}
	
	@Override
	public boolean isBlockSolidOnSide(World world, int x, int y, int z, ForgeDirection side) {
		return subBlock(world, x, y, z).isBlockSolidOnSide(world, x, y, z, side);
	}
	
	@Override
	public boolean canBlockStay(World world, int x, int y, int z) {
		return subBlock(world, x, y, z).canBlockStay(world, x, y, z);
	}
	
	@Override
	public void fillWithRain(World world, int x, int y, int z) {
		subBlock(world, x, y, z).fillWithRain(world, x, y, z);
	}
	
	@Override
	public boolean canPlaceTorchOnTop(World world, int x, int y, int z) {
		return subBlock(world, x, y, z).canPlaceTorchOnTop(world, x, y, z);
	}
	
	/* ---------------------------- Events ---------------------------- */
	
	@Override
	public void updateTick(World world, int x, int y, int z, Random par5Random) {
		subBlock(world, x, y, z).updateTick(world, x, y, z, par5Random);
	}
	
	@Override
	public void onBlockAdded(World world, int x, int y, int z) {
		subBlock(world, x, y, z).onBlockAdded(world, x, y, z);
	}
	
	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase par5EntityLivingBase, ItemStack par6ItemStack) {
		subBlock(world, x, y, z).onBlockPlacedBy(world, x, y, z, par5EntityLivingBase, par6ItemStack);
	}
	
	@Override
	public void onBlockPreDestroy(World world, int x, int y, int z, int par5) {
		subBlock(world, x, y, z).onBlockPreDestroy(world, x, y, z, par5);
	}
	
	@Override
	public void breakBlock(World world, int x, int y, int z, int par5, int par6) {
		subBlock(world, x, y, z).breakBlock(world, x, y, z, par5, par6);
	}
	
	@Override
	public boolean removeBlockByPlayer(World world, EntityPlayer player, int x, int y, int z) {
		return subBlock(world, x, y, z).removeBlockByPlayer(world, player, x, y, z);
	}
	
	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, int par5) {
		subBlock(world, x, y, z).onNeighborBlockChange(world, x, y, z, par5);
	}
	
	@Override
	public void onBlockClicked(World world, int x, int y, int z, EntityPlayer par5EntityPlayer) {
		subBlock(world, x, y, z).onBlockClicked(world, x, y, z, par5EntityPlayer);
	}
	
	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer par5EntityPlayer, int par6, float par7, float par8, float par9) {
		return subBlock(world, x, y, z).onBlockActivated(world, x, y, z, par5EntityPlayer, par6, par7, par8, par9);
	}
	
	@Override
	public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity par5Entity) {
		subBlock(world, x, y, z).onEntityCollidedWithBlock(world, x, y, z, par5Entity);
	}
	
	@Override
	public void onEntityWalking(World world, int x, int y, int z, Entity par5Entity) {
		subBlock(world, x, y, z).onEntityWalking(world, x, y, z, par5Entity);
	}
	
	/* ---------------------------- Redstone ---------------------------- */
	
	@Override
	public boolean canConnectRedstone(IBlockAccess world, int x, int y, int z, int side) {
		return subBlock(world, x, y, z).canConnectRedstone(world, x, y, z, side);
	}
	
	@Override
	public int isProvidingStrongPower(IBlockAccess world, int x, int y, int z, int par5) {
		return subBlock(world, x, y, z).isProvidingStrongPower(world, x, y, z, par5);
	}
	
	@Override
	public int isProvidingWeakPower(IBlockAccess world, int x, int y, int z, int par5) {
		return subBlock(world, x, y, z).isProvidingWeakPower(world, x, y, z, par5);
	}
	
	/* ---------------------------- TileEntities ---------------------------- */
	
	@Override
	public boolean hasTileEntity(int metadata) {
		return subBlock(metadata).hasTileEntity(metadata);
	}
	
	@Override
	public TileEntity createTileEntity(World world, int metadata) {
		return subBlock(metadata).createTileEntity(world, metadata);
	}
	
	/* ---------------------------- Mining ---------------------------- */
	
	@Override
	public boolean canEntityDestroy(World world, int x, int y, int z, Entity entity) {
		return subBlock(world, x, y, z).canEntityDestroy(world, x, y, z, entity);
	}
	
	@Override
	public boolean canHarvestBlock(EntityPlayer player, int meta) {
		return subBlock(meta).canHarvestBlock(player, meta);
	}
	
	@Override
	public boolean canPlaceBlockOnSide(World world, int x, int y, int z, int par5, ItemStack par6ItemStack) {
		return subBlock(world, x, y, z).canPlaceBlockOnSide(world, x, y, z, par5, par6ItemStack);
	}
	
	@Override
	public ArrayList<ItemStack> getBlockDropped(World world, int x, int y, int z, int metadata, int fortune) {
		return subBlock(world, x, y, z).getBlockDropped(world, x, y, z, metadata, fortune);
	}
	
	@Override
	public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z) {
		return subBlock(world, x, y, z).getPickBlock(target, world, x, y, z);
	}
	
	@Override
	public int getExpDrop(World world, int data, int enchantmentLevel) {
		return subBlock(data).getExpDrop(world, data, enchantmentLevel);
	}
	
	@Override
	public float getBlockHardness(World world, int x, int y, int z) {
		return subBlock(world, x, y, z).getBlockHardness(world, x, y, z);
	}
	
	/* ---------------------------- ClientSide ---------------------------- */
	
	@Override
	public float getBlockBrightness(IBlockAccess world, int x, int y, int z) {
		return subBlock(world, x, y, z).getBlockBrightness(world, x, y, z);
	}
	
	@Override
	public int getMixedBrightnessForBlock(IBlockAccess world, int x, int y, int z) {
		return subBlock(world, x, y, z).getMixedBrightnessForBlock(world, x, y, z);
	}
	
	@Override
	public boolean shouldSideBeRendered(IBlockAccess world, int x, int y, int z, int par5) {
		Boolean renderSide = subBlock(world, x, y, z).shouldSideBeRendered(world, x, y, z, par5);
		return renderSide != null ? renderSide : super.shouldSideBeRendered(world, x, y, z, par5);
	}
	
	@Override
	public Icon getIcon(int side, int meta) {
		return subBlock(meta).getIcon(side, meta);
	}
	
	@Override
	public Icon getBlockTexture(IBlockAccess world, int x, int y, int z, int par5) {
		return subBlock(world, x, y, z).getBlockTexture(world, x, y, z, par5);
	}
	
	@Override
	public int getRenderColor(int meta) {
		return subBlock(meta).getRenderColor(meta);
	}
	
	@Override
	public void registerIcons(IconRegister par1IconRegister) {
		for (DCls sub : subBlocks.values()) sub.registerIcons(par1IconRegister);
	}

	@Override
	public boolean addBlockDestroyEffects(World world, int x, int y, int z, int meta, EffectRenderer effectRenderer) {
		return subBlock(world, x, y, z).addBlockDestroyEffects(world, x, y, z, meta, effectRenderer);
	}
	
	@Override
	public boolean addBlockHitEffects(World world, MovingObjectPosition target, EffectRenderer effectRenderer) {
		return subBlock(world, target.blockX, target.blockY, target.blockZ).addBlockHitEffects(world, target, effectRenderer);
	}
	
	@Override
	public void randomDisplayTick(World world, int x, int y, int z, Random par5Random) {
		for (DCls sub : subBlocks.values()) sub.randomDisplayTick(world, x, y, z, par5Random);
	}
	
}
