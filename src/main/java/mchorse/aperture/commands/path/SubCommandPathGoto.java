package mchorse.aperture.commands.path;

import mchorse.aperture.camera.Angle;
import mchorse.aperture.camera.Position;
import mchorse.aperture.camera.fixtures.AbstractFixture;
import mchorse.aperture.camera.fixtures.PathFixture;
import mchorse.aperture.commands.CommandCamera;
import mchorse.aperture.utils.L10n;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;

/**
 * Path's sub-command /camera path goto
 *
 * This sub-command is responsible for teleporting given player to a point at
 * given index in a path fixture. Very useful for readjusting the point in a
 * path fixture.
 */
public class SubCommandPathGoto extends CommandBase
{
    @Override
    public String getCommandName()
    {
        return "goto";
    }

    @Override
    public String getCommandUsage(ICommandSender sender)
    {
        return "aperture.commands.camera.path.goto";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        if (args.length < 2)
        {
            throw new WrongUsageException(this.getCommandUsage(sender));
        }

        EntityPlayer player = (EntityPlayer) sender;

        int index = CommandBase.parseInt(args[0], 0);
        int point = CommandBase.parseInt(args[1]);

        if (!CommandCamera.getProfile().has(index))
        {
            L10n.error(sender, "profile.not_exists", args[0]);
            return;
        }

        AbstractFixture fixture = CommandCamera.getProfile().get(index);

        if (!(fixture instanceof PathFixture))
        {
            L10n.error(sender, "profile.not_path", index);
            return;
        }

        PathFixture path = (PathFixture) fixture;

        if (!path.hasPoint(point))
        {
            L10n.error(sender, "profile.no_path_point", index, point);
            return;
        }

        Position position = path.getPoint(point);
        Angle angle = position.angle;

        position.apply(player);
        CommandCamera.getControl().setRollAndFOV(angle.roll, angle.fov);
    }
}