package mchorse.aperture.camera;

import com.google.common.base.Objects;
import com.google.gson.annotations.Expose;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;

/**
 * Position class
 *
 * This class represents a point in the space with specified angle of view.
 */
public class Position
{
    /**
     * Zero position. Not allowed for modification 
     */
    public static final Position ZERO = new Position(0, 0, 0, 0, 0);

    @Expose
    public Point point = new Point(0, 0, 0);
    @Expose
    public Angle angle = new Angle(0, 0);

    public static Position fromByteBuf(ByteBuf buffer)
    {
        return new Position(Point.fromByteBuf(buffer), Angle.fromByteBuf(buffer));
    }

    public Position(Point point, Angle angle)
    {
        this.point = point;
        this.angle = angle;
    }

    public Position(float x, float y, float z, float yaw, float pitch)
    {
        this.point.set(x, y, z);
        this.angle.set(yaw, pitch);
    }

    public Position(EntityPlayer player)
    {
        this.set(player);
    }

    public void set(EntityPlayer player)
    {
        this.point.set(player);
        this.angle.set(player);
    }

    public void copy(Position position)
    {
        this.point.set(position.point.x, position.point.y, position.point.z);
        this.angle.set(position.angle.yaw, position.angle.pitch, position.angle.roll, position.angle.fov);
    }

    public void apply(EntityPlayer player)
    {
        player.setPositionAndRotation(this.point.x, this.point.y, this.point.z, this.angle.yaw, this.angle.pitch);
        player.setLocationAndAngles(this.point.x, this.point.y, this.point.z, this.angle.yaw, this.angle.pitch);
        player.motionX = player.motionY = player.motionZ = 0;
    }

    public void toByteBuf(ByteBuf buffer)
    {
        this.point.toByteBuf(buffer);
        this.angle.toByteBuf(buffer);
    }

    @Override
    public String toString()
    {
        return Objects.toStringHelper(this).addValue(this.point).addValue(this.angle).toString();
    }
}