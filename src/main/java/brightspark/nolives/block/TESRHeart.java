package brightspark.nolives.block;

import brightspark.nolives.NoLives;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;

public class TESRHeart extends TileEntitySpecialRenderer<TileHeart> {
	private static final ItemStack heartStack = new ItemStack(NoLives.itemHeart);

	@Override
	public void render(TileHeart te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
		GlStateManager.pushMatrix();
		GlStateManager.translate(x + 0.5d, y + 0.4d, z + 0.5d);
		Vec3d playerPos = Minecraft.getMinecraft().player.getPositionVector();
		te.updateRotation(playerPos);
		double rotation = te.getRotation(partialTicks);
		GlStateManager.rotate((float) rotation, 0f, 1f, 0f);
		Minecraft.getMinecraft().getRenderItem().renderItem(heartStack, ItemCameraTransforms.TransformType.GROUND);
		GlStateManager.popMatrix();
	}
}
