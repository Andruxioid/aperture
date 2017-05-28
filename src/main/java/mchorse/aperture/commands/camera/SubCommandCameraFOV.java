package mchorse.aperture.commands.camera;

import mchorse.aperture.commands.CommandCamera;
import mchorse.aperture.utils.L10n;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

/**
 * Camera's sub-command /camera fov
 *
 * This command is responsible for setting and getting fov of this client.
 */
public class SubCommandCameraFOV extends CommandBase
{
    @Override
    public String getCommandName()
    {
        return "fov";
    }

    @Override
    public String getCommandUsage(ICommandSender sender)
    {
        return "aperture.commands.camera.fov";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        Minecraft mc = Minecraft.getMinecraft();

        if (args.length == 0)
        {
            L10n.info(sender, "camera.fov", mc.gameSettings.fovSetting);
        }
        else
        {
            CommandCamera.getControl().setFOV((float) CommandBase.parseDouble(args[0]));
        }
    }
}