package main;

import java.util.LinkedList;
import java.util.List;

import config.ConfigFile;
import database.PlayerBaseUpdater;
import fx.buttons.XformScreenButton;
import fx.screens.ArtifactPopupPage;
import fx.screens.ClockPopupPage;
import fx.screens.ConfigPopupPage;
import fx.screens.LogPopupPage;
import fx.screens.PlayersPopupPage;
import fx.screens.PvPPopupPage;
import fx.screens.ScriptsPopupPage;
import fx.screens.XformPopupStage;
import gameinfo.Archetype;
import gameinfo.PlayerData;
import gameinfo.Race;
import loot.DicePopupPage;
import loot.LootPopupPage;

public class DisplayManager
{
    static List<PlayerData> activeTransforms   = new LinkedList<PlayerData>();
    static List<PlayerData> cooldownTransforms = new LinkedList<PlayerData>();

    static XformPopupStage   xformStage   = new XformPopupStage();
    static LogPopupPage      logsStage    = new LogPopupPage();
    static ConfigPopupPage   configStage  = new ConfigPopupPage();
    static PlayersPopupPage  playersStage = new PlayersPopupPage();
    static ScriptsPopupPage  scriptsStage = new ScriptsPopupPage();
    static ClockPopupPage    clockStage   = new ClockPopupPage();
    static PvPPopupPage      pvpStage     = new PvPPopupPage();
    static ArtifactPopupPage artiStage    = new ArtifactPopupPage();
    static DicePopupPage     diceStage    = new DicePopupPage();
    static LootPopupPage     lootStage    = new LootPopupPage();

    static XformScreenButton xformButton;

    public static void initialize()
    {
        // Check for any windows that should be open, and open them if needed.
        final String isDiceShowing = ConfigFile.getProperty(ConfigFile.IS_DICE_SHOWING);
        if (isDiceShowing != null && isDiceShowing.equals("true"))
        {
            DisplayManager.showDicePopup();
        }
        else
        {
            DisplayManager.hideDicePopup();
        }

        // Same for loot
        final String isLootShowing = ConfigFile.getProperty(ConfigFile.IS_LOOT_SHOWING);
        if (isLootShowing != null && isLootShowing.equals("true"))
        {
            DisplayManager.showLootPopup();
        }
        else
        {
            DisplayManager.hideLootPopup();
        }

    }

    public static synchronized void transformDetected(final PlayerData data)
    {
        if (!activeTransforms.contains(data) && noMatchingName(activeTransforms, data))
        {
            final PlayerData registered = PlayerBaseUpdater.findPlayerByNameAndServer(data.name, data.server);
            if (registered != null && registered.clazz != Archetype.Unknown)
            {
                data.clazz = registered.clazz;
            }
            activeTransforms.add(data);
            updateUIWithTransform();
            xformStage.addNewXform(data);
        }
    }

    /**
     * Avoid spawning xform link multiple times, in case multiple clients are open.
     */
    private static boolean noMatchingName(final List<PlayerData> list, final PlayerData data)
    {
        for (final PlayerData d : list)
        {
            if (data.getName().equals(d.getName()) && data.getRace().equals(d.getRace()) && data.getServer().equals(d.getServer()))
            {
                return false;
            }
        }

        return true;
    }

    public static void transitionFromActiveToCooldown(final PlayerData data)
    {
        if (activeTransforms.contains(data))
        {
            activeTransforms.remove(data);
            if (!cooldownTransforms.contains(data))
            {
                cooldownTransforms.add(data);
            }
        }

        System.out.println("Removing " + data);
        xformStage.transitionFromActiveToCooldown(data);
        updateUIWithTransform();
    }

    public static void checkPlayerDeath(final PlayerData data)
    {
        PlayerData flagRemoval = null;
        for (final PlayerData d : activeTransforms)
        {
            if (d.getName().equals(data.getName()) && d.getRace().equals(data.getRace()) && d.getServer().equals(data.getServer()))
            {
                // The transform was killed, we need to remove it from the
                // active list and shift it to the inactive list asap

                flagRemoval = d;
                break;
            }
        }

        if (flagRemoval != null)
        {
            transitionFromActiveToCooldown(flagRemoval);
        }
    }

    private static void updateUIWithTransform()
    {
        int numActiveElyXforms = 0;
        int numActiveAsmoXforms = 0;

        for (final PlayerData d : activeTransforms)
        {
            if (d.race.equals(Race.Elyos))
            {
                numActiveElyXforms++;
            }
            else if (d.race.equals(Race.Asmodian))
            {
                numActiveAsmoXforms++;
            }
        }

        xformButton.updateXformNumbers(numActiveElyXforms, numActiveAsmoXforms);
    }

    public static void removeCooldown(final PlayerData data)
    {
        if (cooldownTransforms.contains(data))
        {
            cooldownTransforms.remove(data);
        }

        updateUIWithTransform();
        xformStage.removeCooldown(data);
    }

    public static void hideAllPopups()
    {
        // xformStage.hide();
        logsStage.hide();
        playersStage.hide();
        scriptsStage.hide();
    }

    public static void toggleLogsPopup()
    {
        if (logsStage.isShowing())
        {
            logsStage.close();
        }
        else
        {
            hideAllPopups();
            logsStage.show();
        }

    }

    public static void toggleConfigPopup()
    {
        if (configStage.isShowing())
        {
            configStage.close();
        }
        else
        {
            hideAllPopups();
            configStage.show();
        }
    }

    public static void openLoggingSettings()
    {
        if (!configStage.isShowing())
        {
            configStage.show();
            configStage.showLoggingSettings();
        }
    }

    public static void togglePlayersPopup()
    {
        if (playersStage.isShowing())
        {
            playersStage.close();
        }
        else
        {
            hideAllPopups();
            playersStage.show();
        }
    }

    public static void toggleScriptsPopup()
    {
        if (scriptsStage.isShowing())
        {
            scriptsStage.close();
        }
        else
        {
            hideAllPopups();
            scriptsStage.show();
        }
    }

    public static void toggleArtifactPopup()
    {
        if (artiStage.isShowing())
        {
            artiStage.close();
        }
        else
        {
            artiStage.show();
        }
    }

    public static void toggleDicePopup()
    {
        if (diceStage.isShowing())
        {
            diceStage.close();
            ConfigFile.setProperty(ConfigFile.IS_DICE_SHOWING, "false");

        }
        else
        {
            diceStage.show();
            diceStage.showCursorForASecond();

            DicePopupPage.showPlayerRoll("<current roll>", 23);
            DicePopupPage.updateWinsRoll("<wins roll>", 99);
            DicePopupPage.updateLastToRoll("<last to roll>");
            ConfigFile.setProperty(ConfigFile.IS_DICE_SHOWING, "true");
        }
    }

    private static void hideDicePopup()
    {
        if (diceStage.isShowing())
        {
            diceStage.close();
        }
    }

    private static void showDicePopup()
    {
        diceStage.show();
        diceStage.showCursorForASecond();

        DicePopupPage.showPlayerRoll("<current roll>", 23);
        DicePopupPage.updateWinsRoll("<wins roll>", 99);
        DicePopupPage.updateLastToRoll("<last to roll>");
    }

    public static void toggleLootPopup()
    {
        if (lootStage.isShowing())
        {
            lootStage.close();
            ConfigFile.setProperty(ConfigFile.IS_LOOT_SHOWING, "false");

        }
        else
        {
            lootStage.show();
            lootStage.showCursorForASecond();
            ConfigFile.setProperty(ConfigFile.IS_LOOT_SHOWING, "true");
        }
    }

    private static void hideLootPopup()
    {
        if (lootStage.isShowing())
        {
            lootStage.close();
        }
    }

    private static void showLootPopup()
    {
        lootStage.show();
        lootStage.showCursorForASecond();
    }

    public static void showTransformPopup()
    {
        xformStage.show();
    }

    public static void hideTransformPopup()
    {
        xformStage.close();
    }

    public static void showClockPopup()
    {
        if (clockStage != null)
        {
            clockStage.show();
        }
    }

    public static void hideClockPopup()
    {
        if (clockStage != null)
        {
            clockStage.hide();
        }
    }

    public static boolean isClockShowing()
    {
        return clockStage.isShowing();
    }

    public static boolean isTransformShowing()
    {
        return xformStage.isShowing();
    }

    public static void setXformButton(final XformScreenButton xformButton)
    {
        DisplayManager.xformButton = xformButton;
    }

    public static boolean isPvPShowing()
    {
        return pvpStage.isShowing();
    }

    public static void showPvPPopup()
    {
        if (pvpStage != null)
        {
            pvpStage.show();
        }
    }

    public static void hidePvPPopup()
    {
        if (pvpStage != null)
        {
            pvpStage.hide();
        }
    }

}
