package brightspark.nolives;

import brightspark.nolives.livesData.PlayerLivesWorldData;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MessageGetLives implements IMessage
{
    public Map<UUID, Integer> lives;

    public MessageGetLives() {}

    public MessageGetLives(Map<UUID, Integer> lives)
    {
        this.lives = lives;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        int size = buf.readInt();
        lives = new HashMap<>(size);
        for(int i = 0; i < size; i++)
        {
            long leastSig = buf.readLong();
            long mostSig = buf.readLong();
            lives.put(new UUID(mostSig, leastSig), buf.readInt());
        }
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeInt(lives.size());
        for(Map.Entry<UUID, Integer> entry : lives.entrySet())
        {
            UUID uuid = entry.getKey();
            buf.writeLong(uuid.getLeastSignificantBits());
            buf.writeLong(uuid.getMostSignificantBits());
            buf.writeInt(entry.getValue());
        }
    }

    public static class Handler implements IMessageHandler<MessageGetLives, IMessage>
    {
        @Override
        public IMessage onMessage(MessageGetLives message, MessageContext ctx)
        {
            Minecraft.getMinecraft().addScheduledTask(() ->
            {
                EntityPlayer player = Minecraft.getMinecraft().player;
                UUID playerUuid = player.getUniqueID();
                for(Map.Entry<UUID, Integer> entry : message.lives.entrySet())
                {
                    if(entry.getKey().equals(playerUuid))
                    {
                        PlayerLivesWorldData data = PlayerLivesWorldData.get(player.world);
                        if(data != null) data.setLives(playerUuid, entry.getValue());
                    }
                }
            });
            return null;
        }
    }
}
