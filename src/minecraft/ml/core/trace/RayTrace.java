package ml.core.trace;

import ml.core.geo.BlockVec3;
import ml.core.geo.Cuboid6;
import ml.core.geo.Vector3;
import ml.core.math.MathHelper;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumMovingObjectType;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import com.google.common.collect.BiMap;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Influenced by ChickenBones' implementation (thanks CB)
 * @author Matchlighter
 *
 */
public class RayTrace {

	private double f_dist;
	private int f_face;
	private Vector3 traceFace(int face, Vector3 strt, Vector3 end, Cuboid6 cuboid){
		Vector3 hit = null;
		switch(face){
		case 0:
			hit = strt.getInterceptOfXZ(end, cuboid.min.y);
			break;
		case 1:
			hit = strt.getInterceptOfXZ(end, cuboid.max.y);
			break;

		case 2:
			hit = strt.getInterceptOfXZ(end, cuboid.min.z);
			break;
		case 3:
			hit = strt.getInterceptOfXZ(end, cuboid.max.z);
			break;

		case 4:
			hit = strt.getInterceptOfXZ(end, cuboid.min.x);
			break;
		case 5:
			hit = strt.getInterceptOfXZ(end, cuboid.max.x);
			break;
		}

		if (hit==null)
			return null;

		switch (face) {
		case 0:
		case 1:
			if (!MathHelper.between(cuboid.min.x, hit.x, cuboid.max.x) || !MathHelper.between(cuboid.min.z, hit.z, cuboid.max.z))
				return null;
			break;
		case 2:
		case 3:
			if (!MathHelper.between(cuboid.min.x, hit.x, cuboid.max.x) || !MathHelper.between(cuboid.min.y, hit.y, cuboid.max.y))
				return null;
			break;
		case 4:
		case 5:
			if (!MathHelper.between(cuboid.min.y, hit.y, cuboid.max.y) || !MathHelper.between(cuboid.min.z, hit.z, cuboid.max.z))
				return null;
			break;
		}

		double t_dist = strt.minus(hit).magSqr();
		if (t_dist < f_dist){
			f_dist = t_dist;
			f_face = face;
		}

		return hit;
	}

	public MovingObjectPosition traceCuboid(Vector3 strt, Vector3 end, Cuboid6 cuboid){
		f_dist = Double.MAX_VALUE;
		f_face = -1;
		Vector3 hit = null;

		for (int i=0; i<6; i++){
			hit = traceFace(i, strt, end, cuboid);
		}

		if (f_face<0) return null;

		MovingObjectPosition mop = new MovingObjectPosition(0, 0, 0, f_face, hit.toVec3());
		mop.typeOfHit = null;
		return mop;
	}

	private Cuboid6 c_cuboid;
	public MovingObjectPosition traceCuboids(Vector3 strt, Vector3 end, BiMap<Integer, Cuboid6> cuboids){
		double l_dist = Double.MAX_VALUE;
		MovingObjectPosition mop = null;

		for (int idex : cuboids.keySet()){
			MovingObjectPosition tmop = traceCuboid(strt, end, cuboids.get(idex));
			if (tmop != null && f_dist < l_dist){
				l_dist = f_dist;
				mop = tmop;
				mop.subHit = idex;
				c_cuboid = cuboids.get(idex);
			}
		}

		return mop;
	}

	public MovingObjectPosition traceCuboids(Vector3 strt, Vector3 end, BiMap<Integer, Cuboid6> cuboids, BlockVec3 blv3, Block blk){
		MovingObjectPosition mop = traceCuboids(strt, end, cuboids);
		if (mop != null){
			mop.blockX = blv3.x;
			mop.blockY = blv3.y;
			mop.blockZ = blv3.z;
			mop.typeOfHit = EnumMovingObjectType.TILE;
			if (blk != null)
				c_cuboid.translate(new Vector3(blv3.x, blv3.y, blv3.z)).setToBlockBounds(blk);
		}
		return mop;
	}

	public static MovingObjectPosition retraceBlock(World world, EntityPlayer player, int x, int y, int z){
		Vec3 strt = Vec3.createVectorHelper(player.posX, (player.posY + 1.62) - (double)player.yOffset, player.posZ);
		Vec3 look = player.getLook(1.0F);
		double reach = world.isRemote ? getReachDist_client() : getReachDist_server((EntityPlayerMP) player);
		Vec3 end = strt.addVector(look.xCoord * reach, look.yCoord * reach, look.zCoord * reach);
		return Block.blocksList[world.getBlockId(x, y, z)].collisionRayTrace(world, x, y, z, strt, end);
	}

	public static double getReachDist_server(EntityPlayerMP player){
		return player.theItemInWorldManager.getBlockReachDistance();
	}

	@SideOnly(Side.CLIENT)
	public static double getReachDist_client(){
		return Minecraft.getMinecraft().playerController.getBlockReachDistance();
	}
}
