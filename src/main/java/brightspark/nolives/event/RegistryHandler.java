package brightspark.nolives.event;

import brightspark.nolives.NoLives;
import brightspark.nolives.block.TESRHeart;
import brightspark.nolives.block.TileHeart;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SuppressWarnings("ConstantConditions")
@Mod.EventBusSubscriber
public class RegistryHandler {
	@SubscribeEvent
	public static void registerItems(RegistryEvent.Register<Item> event) {
		event.getRegistry().register(NoLives.itemHeart);
		event.getRegistry().register(new ItemBlock(NoLives.blockHeart).setRegistryName(NoLives.blockHeart.getRegistryName()));
	}

	@SubscribeEvent
	public static void registerBlocks(RegistryEvent.Register<Block> event) {
		event.getRegistry().register(NoLives.blockHeart);
		GameRegistry.registerTileEntity(TileHeart.class, NoLives.blockHeart.getRegistryName());
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public static void registerModels(ModelRegistryEvent event) {
		regModel(NoLives.itemHeart);
		regModel(NoLives.blockHeart);

		ClientRegistry.bindTileEntitySpecialRenderer(TileHeart.class, new TESRHeart());
	}

	@SideOnly(Side.CLIENT)
	private static void regModel(Block block) {
		regModel(Item.getItemFromBlock(block));
	}

	@SideOnly(Side.CLIENT)
	private static void regModel(Item item) {
		ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
	}
}
