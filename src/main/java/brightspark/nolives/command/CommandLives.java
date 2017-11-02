package brightspark.nolives.command;

import brightspark.nolives.livesData.PlayerLivesWorldData;
import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerProfileCache;
import net.minecraft.util.text.TextComponentString;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CommandLives extends CommandBase
{
    @Override
    public String getName()
    {
        return "lives";
    }

    @Override
    public String getUsage(ICommandSender sender)
    {
        return "TODO";
    }

    @Override
    public int getRequiredPermissionLevel()
    {
        return 0;
    }

    private boolean canSenderUseCommand(ICommandSender sender, String commandVariant)
    {
        return sender.canUseCommand(2, getName()) || commandVariant.equalsIgnoreCase("list");
    }

    private String genWhitespace(int length)
    {
        StringBuilder sb = new StringBuilder(length);
        for(int i = 0; i < length; i++)
            sb.append(" ");
        return sb.toString();
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        if(!(sender instanceof EntityPlayer)) return;
        EntityPlayer player = (EntityPlayer) sender;
        PlayerLivesWorldData livesData = PlayerLivesWorldData.get(player.world);
        if(livesData == null) throw new CommandException("Failed to get Player Lives data from the world");

        if(args.length == 0)
        {
            //Show user how many lives they have left
            sender.sendMessage(new TextComponentString("You have " + livesData.getLives(player.getUniqueID()) + " lives left"));
        }
        else if(canSenderUseCommand(sender, args[0]))
        {
            if(args[0].equalsIgnoreCase("list"))
            {
                TextComponentString text = new TextComponentString("Player lives:");
                PlayerProfileCache cache = server.getPlayerProfileCache();
                List<String> playerNames = Lists.newArrayList(cache.getUsernames());
                int longestName = 0;
                for(String name : playerNames)
                    if(name.length() > longestName)
                        longestName = name.length();
                longestName += 5;
                Map<UUID, Integer> allLives = livesData.getAllLives();
                for(Map.Entry<UUID, Integer> entry : allLives.entrySet())
                {
                    UUID uuid = entry.getKey();
                    GameProfile profile = cache.getProfileByUUID(uuid);
                    if(profile == null) continue;
                    String name = profile.getName();
                    String entryLives = allLives.get(uuid).toString();
                    String whitespace = genWhitespace(longestName - entryLives.length());
                    text.appendText("\n").appendText(name).appendText(whitespace).appendText(entryLives);
                }
                sender.sendMessage(text);
            }
            else if(args.length > 1)
            {
                switch(args[0])
                {
                    case "add": //TODO Add lives

                        break;
                    case "sub": //TODO Sub lives

                        break;
                    case "set": //TODO Set lives

                        break;
                }
            }
        }
    }
}
