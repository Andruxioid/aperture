package mchorse.aperture.camera.fixtures;

import com.google.gson.annotations.Expose;

import io.netty.buffer.ByteBuf;
import mchorse.aperture.camera.Point;
import mchorse.aperture.camera.Position;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.MathHelper;

/**
 * Circular camera fixture
 *
 * This camera fixture is responsible for rotating the camera so it would go
 * round in circles relatively given point in space.
 *
 * You know, like one of these rotating thingies on car expos that rotate cars
 * round and round and round...
 */
public class CircularFixture extends AbstractFixture
{
    /**
     * Center point of circular fixture
     */
    @Expose
    public Point start = new Point(0, 0, 0);

    /**
     * Start angle offset (in degrees)
     */
    @Expose
    public float offset = 0;

    /**
     * Distance (in blocks units) from center point
     */
    @Expose
    public float distance = 5;

    /**
     * How much degrees to perform during running
     */
    @Expose
    public float circles = 360;

    /**
     * Pitch of the
     */
    @Expose
    public float pitch = 0;

    public CircularFixture(long duration)
    {
        super(duration);
    }

    @Override
    public void edit(String[] args, EntityPlayer player) throws CommandException
    {
        if (args.length > 0)
        {
            this.distance = (float) CommandBase.parseDouble(args[0]);
        }

        if (args.length > 1)
        {
            this.circles = (float) CommandBase.parseDouble(args[1]);
        }

        if (args.length > 2)
        {
            this.pitch = (float) CommandBase.parseDouble(args[2]);
        }

        this.offset = player.rotationYaw < 0 ? 360 + player.rotationYaw : player.rotationYaw;
        this.offset = (this.offset + 90) % 360;

        this.pitch = player.rotationPitch;

        float cos = (float) Math.cos(Math.toRadians(this.offset));
        float sin = (float) Math.sin(Math.toRadians(this.offset));

        this.start.x = (float) player.posX + cos * this.distance;
        this.start.y = (float) player.posY;
        this.start.z = (float) player.posZ + sin * this.distance;
    }

    @Override
    public void applyFixture(long ticks, float partialTicks, Position pos)
    {
        float progress = (ticks / (float) this.duration) + (1.0F / this.duration * partialTicks);
        float angle = (float) (Math.toRadians(this.offset) + progress * Math.toRadians(this.circles));

        float cos = this.distance * (float) Math.cos(angle);
        float sin = this.distance * (float) Math.sin(angle);

        /* +0.5, because player's position isn't in the entity's center */
        float x = this.start.x + 0.5F + cos;
        float y = this.start.y;
        float z = this.start.z + 0.5F + sin;

        float yaw = (float) (MathHelper.atan2(sin, cos) * (180D / Math.PI)) - 90.0F;

        pos.point.set(x - 0.5F, y, z - 0.5F);
        pos.angle.set(MathHelper.wrapDegrees(yaw - 180.0F), this.pitch);
    }

    @Override
    public void fromByteBuf(ByteBuf buffer)
    {
        super.fromByteBuf(buffer);

        this.start = Point.fromByteBuf(buffer);
        this.offset = buffer.readFloat();
        this.distance = buffer.readFloat();
        this.circles = buffer.readFloat();
        this.pitch = buffer.readFloat();
    }

    @Override
    public void toByteBuf(ByteBuf buffer)
    {
        super.toByteBuf(buffer);

        this.start.toByteBuf(buffer);
        buffer.writeFloat(this.offset);
        buffer.writeFloat(this.distance);
        buffer.writeFloat(this.circles);
        buffer.writeFloat(this.pitch);
    }

    @Override
    public byte getType()
    {
        return AbstractFixture.CIRCULAR;
    }
}