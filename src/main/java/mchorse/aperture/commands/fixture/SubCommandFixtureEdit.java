package mchorse.aperture.commands.fixture;

import mchorse.aperture.ClientProxy;
import mchorse.aperture.camera.CameraProfile;
import mchorse.aperture.commands.SubCommandBase;
import mchorse.aperture.utils.L10n;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;

/**
 * Camera's sub-command /camera edit
 *
 * This command is responsible for editing camera fixture's values passed from
 * this command in string array or directly from player's properties such as
 * position and rotation.
 *
 * It also outputs fixture's values if the values aren't specified.
 */
public class SubCommandFixtureEdit extends CommandBase
{
    @Override
    public String getCommandName()
    {
        return "edit";
    }

    @Override
    public String getCommandUsage(ICommandSender sender)
    {
        return "aperture.commands.camera.fixture.edit";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        if (args.length < 1)
        {
            throw new WrongUsageException(this.getCommandUsage(sender));
        }

        CameraProfile profile = ClientProxy.control.currentProfile;
        int index = CommandBase.parseInt(args[0]);

        if (!profile.has(index))
        {
            L10n.error(sender, "profile.not_exists", index);
            return;
        }

        profile.get(index).edit(SubCommandBase.dropFirstArgument(args), (EntityPlayer) sender);
        profile.dirty();
    }
}
