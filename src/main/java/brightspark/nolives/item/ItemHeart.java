package brightspark.nolives.item;

import brightspark.nolives.NLConfig;
import brightspark.nolives.NoLives;
import brightspark.nolives.event.LifeChangeEvent;
import brightspark.nolives.livesData.PlayerLivesWorldData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

public class ItemHeart extends Item {
	public ItemHeart() {
		setTranslationKey("heart");
		setRegistryName("heart");
		setCreativeTab(NoLives.tab);
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
		ItemStack stack = player.getHeldItem(hand);
		if (!world.isRemote) {
			LifeChangeEvent.LifeGainEvent event = new LifeChangeEvent.LifeGainEvent((EntityPlayerMP) player, NLConfig.livesFromHeartItem, LifeChangeEvent.LifeGainEvent.GainType.ITEM);
			if (!MinecraftForge.EVENT_BUS.post(event) && event.getLivesToGain() > 0)
				//Add life to player
				if (PlayerLivesWorldData.addLives(world, player, event.getLivesToGain()))
					stack.shrink(1);
		}
		return new ActionResult<>(EnumActionResult.PASS, stack);
	}
}
