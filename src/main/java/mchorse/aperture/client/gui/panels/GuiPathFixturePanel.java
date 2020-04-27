package mchorse.aperture.client.gui.panels;

import mchorse.aperture.camera.data.Position;
import mchorse.aperture.camera.fixtures.AbstractFixture;
import mchorse.aperture.camera.fixtures.KeyframeFixture;
import mchorse.aperture.camera.fixtures.KeyframeFixture.Easing;
import mchorse.aperture.camera.fixtures.KeyframeFixture.KeyframeInterpolation;
import mchorse.aperture.camera.fixtures.PathFixture;
import mchorse.aperture.camera.fixtures.PathFixture.DurablePosition;
import mchorse.aperture.client.gui.GuiCameraEditor;
import mchorse.aperture.client.gui.panels.modules.GuiAngleModule;
import mchorse.aperture.client.gui.panels.modules.GuiInterpModule;
import mchorse.aperture.client.gui.panels.modules.GuiPointModule;
import mchorse.aperture.client.gui.panels.modules.GuiPointsModule;
import mchorse.aperture.client.gui.panels.modules.GuiPointsModule.IPointPicker;
import mchorse.aperture.client.gui.utils.GuiFixtureKeyframesGraphEditor;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiButtonElement;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiToggleElement;
import mchorse.mclib.client.gui.framework.elements.utils.GuiContext;
import mchorse.mclib.client.gui.utils.keys.IKey;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;

/**
 * Path fixture panel
 *
 * This panel has the most modules used. It's responsible for editing path
 * fixture. It uses point and angle modules to edit a position which is picked
 * from the points module. Interpolation module is used to modify path fixture's
 * interpolation methods.
 */
public class GuiPathFixturePanel extends GuiAbstractFixturePanel<PathFixture> implements IPointPicker
{
    public GuiPointModule point;
    public GuiAngleModule angle;
    public GuiPointsModule points;
    public GuiInterpModule interp;
    public GuiToggleElement perPointDuration;
    public GuiToggleElement useSpeed;
    public GuiButtonElement toKeyframe;
    public GuiFixtureKeyframesGraphEditor<GuiPathFixturePanel> speed;

    public DurablePosition position;

    private long update;

    public GuiPathFixturePanel(Minecraft mc, GuiCameraEditor editor)
    {
        super(mc, editor);

        this.point = new GuiPointModule(mc, editor);
        this.angle = new GuiAngleModule(mc, editor);
        this.points = new GuiPointsModule(mc, editor, this);
        this.interp = new GuiInterpModule(mc, editor);
        this.perPointDuration = new GuiToggleElement(mc, IKey.lang("aperture.gui.panels.per_point"), false, (b) ->
        {
            this.fixture.perPointDuration = b.isToggled();
            this.editor.updateProfile();
        });
        this.useSpeed = new GuiToggleElement(mc, IKey.lang("aperture.gui.panels.use_speed"), false, (b) ->
        {
            this.fixture.useSpeed = b.isToggled();
            this.speed.setVisible(this.fixture.useSpeed);
            this.editor.updateProfile();

            if (this.fixture.useSpeed)
            {
                this.left.flex().hTo(this.speed.area);
                this.right.flex().hTo(this.speed.area);
            }
            else
            {
                this.left.flex().h(1F);
                this.right.flex().h(1F);
            }

            this.resize();

            if (this.fixture.useSpeed)
            {
                this.fixture.updateSpeedCache();
            }
        });
        this.toKeyframe = new GuiButtonElement(mc, IKey.lang("aperture.gui.panels.to_keyframe"), (b) -> this.toKeyframe());
        this.speed = new GuiFixtureKeyframesGraphEditor<GuiPathFixturePanel>(mc, this);
        this.speed.graph.setParent(this);
        this.speed.graph.setColor(0x0088ff);

        this.points.flex().relative(this).set(140, 0, 90, 20).y(1, -20).w(1, -280);
        this.speed.flex().relative(this).y(0.5F, 0).w(1F).h(0.5F, -30);
        this.left.flex().w(140);

        this.left.add(this.interp, this.perPointDuration, this.useSpeed, this.toKeyframe);
        this.left.markContainer();
        this.right.add(this.point, this.angle);

        this.prepend(this.speed);
        this.add(this.points);
    }

	@Override
	public void profileWasUpdated()
	{
		if (this.fixture.useSpeed)
		{
			this.update = System.currentTimeMillis() + 100;
		}
	}

	private void toKeyframe()
    {
        int c = this.fixture.getCount();

        if (c <= 1)
        {
            return;
        }

        long duration = this.fixture.getDuration();
        KeyframeFixture fixture = new KeyframeFixture(duration);
        AbstractFixture.copyModifiers(this.fixture, fixture);
        KeyframeInterpolation pos = this.fixture.interpolationPos.interp;
        KeyframeInterpolation angle = this.fixture.interpolationAngle.interp;
        Easing posEasing = this.fixture.interpolationPos.easing;
        Easing angleEasing = this.fixture.interpolationAngle.easing;

        long x = 0;
        int i = 0;

        for (DurablePosition point : this.fixture.getPoints())
        {
            if (!this.fixture.perPointDuration)
            {
                x = (int) (i / (c - 1F) * duration);
            }

            int index = fixture.x.insert(x, (float) point.point.x);
            fixture.y.insert(x, (float) point.point.y);
            fixture.z.insert(x, (float) point.point.z);
            fixture.yaw.insert(x, point.angle.yaw);
            fixture.pitch.insert(x, point.angle.pitch);
            fixture.roll.insert(x, point.angle.roll);
            fixture.fov.insert(x, point.angle.fov);

            fixture.x.get(index).setInterpolation(pos, posEasing);
            fixture.y.get(index).setInterpolation(pos, posEasing);
            fixture.z.get(index).setInterpolation(pos, posEasing);
            fixture.yaw.get(index).setInterpolation(angle, angleEasing);
            fixture.pitch.get(index).setInterpolation(angle, angleEasing);
            fixture.roll.get(index).setInterpolation(angle, angleEasing);
            fixture.fov.get(index).setInterpolation(angle, angleEasing);

            if (this.fixture.perPointDuration)
            {
                x += point.getDuration();
            }

            i ++;
        }

        this.editor.createFixture(fixture);
    }

    @Override
    public void pickPoint(GuiPointsModule module, int index)
    {
        this.position = this.fixture.getPoint(index);

        this.point.fill(this.position.point);
        this.angle.fill(this.position.angle);

        if (this.fixture.perPointDuration)
        {
            this.duration.setValue(this.position.getDuration());
        }
        else
        {
            this.duration.setValue(this.fixture.getDuration());
        }

        if (this.editor.isSyncing())
        {
            this.editor.timeline.setValueFromScrub((int) this.currentOffset());
        }
    }

    @Override
    public void select(PathFixture fixture, long duration)
    {
        boolean same = this.fixture == fixture;

        super.select(fixture, duration);

        int index = this.points.index;

        if (duration != -1)
        {
            index = (int) ((duration / (float) fixture.getDuration()) * fixture.getCount());

            DurablePosition pos = fixture.getPoint(index);

            this.position = pos;
            this.points.index = index;
        }

        this.point.fill(this.position.point);
        this.angle.fill(this.position.angle);
        this.points.fill(fixture);
        this.interp.fill(fixture);
        this.perPointDuration.toggled(fixture.perPointDuration);
        this.useSpeed.toggled(fixture.useSpeed);

        if (this.fixture.useSpeed)
        {
            this.left.flex().hTo(this.speed.area);
            this.right.flex().hTo(this.speed.area);
        }
        else
        {
            this.left.flex().h(1F);
            this.right.flex().h(1F);
        }

        this.resize();

        if (!same)
        {
            this.speed.graph.setDuration(fixture.getDuration());
            this.speed.setChannel(fixture.speed);
            this.speed.setVisible(this.fixture.useSpeed);
        }

        if (fixture.perPointDuration && this.position != null)
        {
            this.duration.setValue(this.position.getDuration());
        }

        this.points.index = index;
    }

    @Override
    public long currentOffset()
    {
        long point = this.fixture.getTickForPoint(this.points.index);

        if (point == this.fixture.getDuration())
        {
            point--;
        }

        return super.currentOffset() + point;
    }

    @Override
    public void editFixture(Position position)
    {
        if (this.position != null)
        {
            this.position.set(position);

            super.editFixture(position);
        }
    }

    @Override
    protected void updateDuration(long value)
    {
        if (this.fixture.perPointDuration)
        {
            this.position.setDuration(value);
        }
        else
        {
            this.fixture.setDuration(value);
        }

        this.speed.graph.setDuration((int) value);
        this.editor.updateValues();
    }

    @Override
    public void draw(GuiContext context)
    {
    	if (this.fixture.useSpeed && this.update > 0 && System.currentTimeMillis() >= this.update)
	    {
	    	this.fixture.updateSpeedCache();
	    	this.update = 0;
	    }

        super.draw(context);
    }
}