package brightspark.nolives.item;

import brightspark.nolives.NoLives;
import brightspark.nolives.livesData.PlayerLivesWorldData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

public class ItemHeart extends Item
{
    public ItemHeart()
    {
        setUnlocalizedName("heart");
        setRegistryName("heart");
        setCreativeTab(NoLives.tab);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand)
    {
        ItemStack stack = player.getHeldItem(hand);
        if(!world.isRemote)
        {
            //Add life to player
            PlayerLivesWorldData data = PlayerLivesWorldData.get(world);
            if(data != null)
            {
                data.addLives(player.getUniqueID(), 1);
                stack.shrink(1);
                player.sendMessage(new TextComponentTranslation(NoLives.MOD_ID + ".addLife"));
            }
            else
                NoLives.logger.error("Unable to add life to player " + player.getDisplayNameString() + ". PlayerLivesWorldData was null!");
        }
        return new ActionResult<>(EnumActionResult.PASS, stack);
    }
}
