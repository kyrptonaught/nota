package nota;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import nota.player.SongPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Main class, contains methods for playing and adjusting songs for players.
 */
@SuppressWarnings("unused")
public class Nota implements ModInitializer {
	public static final String MOD_ID = "nota";
	public static final Logger LOGGER = LoggerFactory.getLogger("Nota");

	private static Nota instance;
	public MinecraftServer server;

	Map<UUID, ArrayList<SongPlayer>> playingSongs = new ConcurrentHashMap<>();
	Map<UUID, Byte> playerVolume = new ConcurrentHashMap<>();

	private boolean disabling = false;

	/**
	 * Returns true if a Player is currently receiving a song
	 *
	 * @param player player entity
	 * @return is receiving a song
	 */
	public static boolean isReceivingSong(PlayerEntity player) {
		return isReceivingSong(player.getUuid());
	}

	/**
	 * Returns true if a Player with specified UUID is currently receiving a song
	 *
	 * @param playerUuid player's uuid
	 * @return is receiving a song
	 */
	public static boolean isReceivingSong(UUID playerUuid) {
		ArrayList<SongPlayer> songs = instance.playingSongs.get(playerUuid);
		return (songs != null && !songs.isEmpty());
	}

	/**
	 * Stops the song for a Player
	 *
	 * @param player player entity
	 */
	public static void stopPlaying(PlayerEntity player) {
		stopPlaying(player.getUuid());
	}

	/**
	 * Stops the song for a Player
	 *
	 * @param playerUuid player's uuid
	 */
	public static void stopPlaying(UUID playerUuid) {
		ArrayList<SongPlayer> songs = instance.playingSongs.get(playerUuid);
		if (songs == null) {
			return;
		}
		for (SongPlayer songPlayer : songs) {
			songPlayer.removePlayer(playerUuid);
		}
	}

	/**
	 * Sets the volume for a given Player
	 *
	 * @param player player entity
	 * @param volume volume
	 */
	public static void setPlayerVolume(PlayerEntity player, byte volume) {
		setPlayerVolume(player.getUuid(), volume);
	}

	/**
	 * Sets the volume for a given Player
	 *
	 * @param playerUuid player's uuid
	 * @param volume     volume
	 */
	public static void setPlayerVolume(UUID playerUuid, byte volume) {
		instance.playerVolume.put(playerUuid, volume);
	}

	/**
	 * Gets the volume for a given Player
	 *
	 * @param player player entity
	 * @return volume (byte)
	 */
	public static byte getPlayerVolume(PlayerEntity player) {
		return getPlayerVolume(player.getUuid());
	}

	/**
	 * Gets the volume for a given Player
	 *
	 * @param playerUuid player's uuid
	 * @return volume (byte)
	 */
	public static byte getPlayerVolume(UUID playerUuid) {
		if (instance.playerVolume.containsKey(playerUuid)) {
			return instance.playerVolume.get(playerUuid);
		} else {
			instance.playerVolume.put(playerUuid, (byte) 100);
			return 100;
		}
	}

	public static ArrayList<SongPlayer> getSongPlayersByPlayer(PlayerEntity player) {
		return getSongPlayersByPlayer(player.getUuid());
	}

	public static ArrayList<SongPlayer> getSongPlayersByPlayer(UUID playerUuid) {
		return instance.playingSongs.get(playerUuid);
	}

	public static void setSongPlayersByPlayer(PlayerEntity player, ArrayList<SongPlayer> songs) {
		setSongPlayersByPlayer(player.getUuid(), songs);
	}

	public static void setSongPlayersByPlayer(UUID playerUuid, ArrayList<SongPlayer> songs) {
		instance.playingSongs.put(playerUuid, songs);
	}

	public boolean isDisabling() {
		return this.disabling;
	}

	public static Nota getAPI() {
		return Nota.instance;
	}

	public MinecraftServer getServer() {
		return this.server;
	}

	@Override
	public void onInitialize() {
		Nota.instance = this;
		ServerLifecycleEvents.SERVER_STARTED.register(server -> Nota.getAPI().server = server);
		ServerLifecycleEvents.SERVER_STOPPING.register(server -> Nota.getAPI().disabling = true);

		NotaCommands.onInitialize();
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> NotaCommands.registerCommands(dispatcher));
	}
}
