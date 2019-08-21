package brightspark.nolives.block;

import brightspark.nolives.NLConfig;
import brightspark.nolives.NoLives;
import brightspark.nolives.event.LifeChangeEvent;
import brightspark.nolives.livesData.PlayerLivesWorldData;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

import javax.annotation.Nullable;

public class BlockHeart extends BlockContainer {
	public BlockHeart() {
		super(Material.ROCK, MapColor.RED);
		setRegistryName("heart_block");
		setTranslationKey("heart_block");
		setCreativeTab(NoLives.tab);
		setHardness(2f);
		setResistance(12f);
		setSoundType(SoundType.STONE);
		translucent = true;
	}

	@Nullable
	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileHeart();
	}

	@Override
	public BlockRenderLayer getRenderLayer() {
		return BlockRenderLayer.TRANSLUCENT;
	}

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.MODEL;
	}

	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean canSilkHarvest(World world, BlockPos pos, IBlockState state, EntityPlayer player) {
		return NLConfig.canSilkHarvestBlock;
	}

	@Override
	public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
		if (NLConfig.dropItemsFromBlock)
			drops.add(new ItemStack(NoLives.itemHeart, NLConfig.livesFromHeartBlock));
	}

	@Override
	public void onBlockHarvested(World world, BlockPos pos, IBlockState state, EntityPlayer player) {
		if (!NLConfig.dropItemsFromBlock && !world.isRemote && !player.isCreative()) {
			LifeChangeEvent.LifeGainEvent event = new LifeChangeEvent.LifeGainEvent((EntityPlayerMP) player, NLConfig.livesFromHeartBlock, LifeChangeEvent.LifeGainEvent.GainType.BLOCK);
			if (!MinecraftForge.EVENT_BUS.post(event) && event.getLivesToGain() > 0) {
				if (!PlayerLivesWorldData.addLives(world, player, event.getLivesToGain()))
					NoLives.sendMessageText(player, "max", NLConfig.maxLives);
			}
		}
		super.onBlockHarvested(world, pos, state, player);
	}
}
