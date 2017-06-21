package mchorse.aperture.camera;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonWriter;

import mchorse.aperture.camera.fixtures.AbstractFixture;
import mchorse.aperture.camera.fixtures.CircularFixture;
import mchorse.aperture.camera.fixtures.FollowFixture;
import mchorse.aperture.camera.fixtures.IdleFixture;
import mchorse.aperture.camera.fixtures.LookFixture;
import mchorse.aperture.camera.fixtures.PathFixture;
import mchorse.aperture.camera.json.AbstractFixtureAdapter;
import mchorse.aperture.capabilities.camera.Camera;
import mchorse.aperture.capabilities.camera.ICamera;
import mchorse.aperture.network.Dispatcher;
import mchorse.aperture.network.common.PacketCameraProfile;
import mchorse.aperture.network.common.PacketCameraState;
import mchorse.aperture.utils.L10n;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.DimensionManager;

/**
 * Utilities for camera classes
 *
 * Includes methods for writing/reading camera profile, getting camera JSON
 * builder and stuff like sending camera profile to the client.
 */
public class CameraUtils
{
    /**
     * Get path to camera profile file (located in current world save's folder)
     */
    public static String cameraFile(String filename)
    {
        File file = new File(DimensionManager.getCurrentSaveRootDirectory() + "/aperture/cameras");

        if (!file.exists())
        {
            file.mkdirs();
        }

        return file.getAbsolutePath() + "/" + filename + ".json";
    }

    /**
     * Get a camera JSON builder. This will include custom serializers for some
     * of the camera fixture classes. Also custom serializers.
     */
    public static Gson cameraJSONBuilder(boolean pretty)
    {
        GsonBuilder builder = new GsonBuilder();

        if (pretty)
        {
            builder.setPrettyPrinting();
        }

        builder.excludeFieldsWithoutExposeAnnotation();

        /* Serializer and deserializer */
        AbstractFixtureAdapter fixtureAdapter = new AbstractFixtureAdapter();

        builder.registerTypeAdapter(AbstractFixture.class, fixtureAdapter);
        builder.registerTypeAdapter(IdleFixture.class, fixtureAdapter);
        builder.registerTypeAdapter(PathFixture.class, fixtureAdapter);
        builder.registerTypeAdapter(LookFixture.class, fixtureAdapter);
        builder.registerTypeAdapter(FollowFixture.class, fixtureAdapter);
        builder.registerTypeAdapter(CircularFixture.class, fixtureAdapter);

        return builder.create();
    }

    /**
     * Read CameraProfile instance from given file
     */
    public static String readCameraProfile(String filename) throws Exception
    {
        String path = cameraFile(filename);
        DataInputStream stream = new DataInputStream(new FileInputStream(path));
        Scanner scanner = new Scanner(stream, "UTF-8");
        String content = scanner.useDelimiter("\\A").next();

        scanner.close();

        return content;
    }

    /**
     * Write CameraProfile instance to given file
     */
    public static void writeCameraProfile(String filename, String profile) throws IOException
    {
        PrintWriter printer = new PrintWriter(cameraFile(filename));

        printer.print(profile);
        printer.close();
    }

    /* Commands */

    /**
     * Send a camera profile that was read from given file to player.
     *
     * This method also checks if player has same named camera profile, and if
     * it's expired (server has newer version), send him new one.
     */
    public static void sendProfileToPlayer(String filename, EntityPlayerMP player, boolean play, boolean force)
    {
        try
        {
            if (!force && playerHasProfile(player, filename, play))
            {
                return;
            }

            String profile = readCameraProfile(filename);
            ICamera recording = Camera.get(player);

            recording.setCurrentProfile(filename);
            recording.setCurrentProfileTimestamp(System.currentTimeMillis());

            Dispatcher.sendTo(new PacketCameraProfile(filename, profile, play), player);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            L10n.error(player, "profile.cant_load", filename);
        }
    }

    /**
     * Checks whether player has older camera profile
     */
    private static boolean playerHasProfile(EntityPlayerMP player, String filename, boolean play)
    {
        ICamera recording = Camera.get(player);
        File profile = new File(cameraFile(filename));

        boolean hasSame = recording.currentProfile().equals(filename);
        boolean isNewer = recording.currentProfileTimestamp() >= profile.lastModified();

        if (hasSame && isNewer)
        {
            if (play)
            {
                Dispatcher.sendTo(new PacketCameraState(true), player);
            }
            else
            {
                L10n.info(player, "profile.loaded", filename);
            }

            return true;
        }

        return false;
    }

    /**
     * Save given camera profile to file. Inform user about the problem, if the
     * camera profile couldn't be saved.
     */
    public static boolean saveCameraProfile(String filename, String profile, EntityPlayerMP player)
    {
        try
        {
            writeCameraProfile(filename, profile);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            L10n.error(player, "profile.cant_save", filename);

            return false;
        }

        return true;
    }

    /**
     * Gets all camera profiles names. This is used for playback GUI's tab 
     * completion thingy.
     */
    public static List<String> listProfiles()
    {
        File file = new File(DimensionManager.getCurrentSaveRootDirectory() + "/aperture/cameras");
        List<String> files = new ArrayList<String>();

        file.mkdirs();

        for (String filename : file.list())
        {
            if (filename.endsWith(".json"))
            {
                files.add(filename.substring(0, filename.lastIndexOf(".json")));
            }
        }

        return files;
    }

    /**
     * Turn a camera profile into JSON string
     * 
     * This method is also responsible for doing JSON prettifying, such as 
     * making sure there are 4 spaces for indentation. 
     */
    public static String toJSON(CameraProfile profile)
    {
        Gson gson = cameraJSONBuilder(true);

        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);

        /* Set 4 space indentation instead of shitty 2 space indentation */
        jsonWriter.setIndent("    ");
        gson.toJson(profile, CameraProfile.class, jsonWriter);

        return writer.toString();
    }
}