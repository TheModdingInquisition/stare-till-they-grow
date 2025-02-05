package dev.tmp.StareTillTheyGrow.Network.Message;

import dev.tmp.StareTillTheyGrow.Library.ActionType;
import dev.tmp.StareTillTheyGrow.Library.PlayerTargetDictionary;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class RegisterBlock {

    private final ActionType actionType;
    private final double x;
    private final double y;
    private final double z;

    // Initialize with given x,y,z positions
    public RegisterBlock(ActionType actionType, double x, double y, double z) {
        this.actionType = actionType;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    // Initialize with BlockPos
    public RegisterBlock(ActionType actionType, BlockPos blockPos) {
        this.actionType = actionType;
        this.x = blockPos.getX();
        this.y = blockPos.getY();
        this.z = blockPos.getZ();
    }

    public BlockPos getBlockPos() {
        return new BlockPos(this.x, this.y, this.z);
    }

    public ActionType getActionType() {
        return this.actionType;
    }

    // The encoding before sending
    public static void encode(RegisterBlock msg, FriendlyByteBuf buf) {
        buf.writeEnum(msg.actionType);
        buf.writeDouble(msg.x);
        buf.writeDouble(msg.y);
        buf.writeDouble(msg.z);
    }

    // The decoding after receiving.
    public static RegisterBlock decode(FriendlyByteBuf buf) {
        ActionType actionType = buf.readEnum(ActionType.class);
        double x = buf.readDouble();
        double y = buf.readDouble();
        double z = buf.readDouble();
        return new RegisterBlock(actionType, x, y, z);
    }

    // Handing the received data
    public static void handle(final RegisterBlock registerBlock, final Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        // Check if this is server sided
        if (context.getDirection().getReceptionSide().isServer()) {
            // Enqueue the task
            context.enqueueWork(() -> {
                // Get the player who send the command
                ServerPlayer player = ctx.get().getSender();
                if (null != player) {
                    ServerLevel world = player.server.overworld();
                    BlockPos blockPos = registerBlock.getBlockPos();
                    ActionType actionType = registerBlock.getActionType();
                    PlayerTargetDictionary.registerBlock(actionType, world, player, blockPos);
                }
            });
        }
        context.setPacketHandled(true);
    }

}
