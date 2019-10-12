package brightspark.nolives.command;

import brightspark.nolives.NoLives;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

public abstract class NLCommandBase extends CommandBase {
	@Override
	public int getRequiredPermissionLevel() {
		return 0;
	}

	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
		return NoLives.checkCommandPermission(this, server, server);
	}
}
