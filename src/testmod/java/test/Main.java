package test;

import nota.Nota;
import nota.event.SongEndEvent;
import nota.event.SongStartEvent;
import nota.model.Playlist;
import nota.model.RepeatMode;
import nota.model.Song;
import nota.player.PositionSongPlayer;
import nota.utils.NBSDecoder;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.UUID;

public class Main implements ModInitializer {
	public static final String MOD_ID = "nota-test";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static Song vitality = null;
	public static Song home = null;
	public static Song bad_apple = null;
	public static Song rush_e = null;
	public static Song tetris_b_theme = null;
	public static Song merry_go_round_of_life = null;

	public static PositionSongPlayer songPlayer = null;

	@Override
	public void onInitialize() {
		try {
			vitality = NBSDecoder.parse(new File("songs/vitality.nbs"));
			home = NBSDecoder.parse(new File("songs/home.nbs"));

			bad_apple = NBSDecoder.parse(new File("songs/bad_apple.nbs"));
			rush_e = NBSDecoder.parse(new File("songs/rush_e.nbs"));

			tetris_b_theme = NBSDecoder.parse(new File("songs/tetris_b_theme.nbs"));
			merry_go_round_of_life = NBSDecoder.parse(new File("songs/merry_go_round_of_life.nbs"));
			LOGGER.info("Songs successfully decoded!");
		}
		catch(Exception e) {
			LOGGER.warn("Failed to decode songs due to an exception: " + e);
		}

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(CommandManager.literal("start-song").executes(context -> {
				var player = context.getSource().getPlayer();
				if(player != null && songPlayer == null) {
					var world = player.world;
					if(!world.isClient) {
						//songPlayer = new PositionSongPlayer(new Playlist(vitality, home, bad_apple, rush_e, tetris_b_theme, merry_go_round_of_life), world);
						//Song mega = NBSDecoder.parse(new File("songs/octavetest-sweep.nbs"));
						Song mega = NBSDecoder.parse(new File("songs/test2.nbs"));
						songPlayer = new PositionSongPlayer(mega, world);
						songPlayer.setId(new Identifier("test:position"));
						songPlayer.setBlockPos(player.getBlockPos());
						songPlayer.addPlayer(player);
						songPlayer.setRepeatMode(RepeatMode.NONE);
						//songPlayer.setEnable10Octave(true);
						songPlayer.setPlaying(true);
					}
				}
				return 1;
			}));
			dispatcher.register(CommandManager.literal("toggle-song").executes(context -> {
				if(songPlayer != null) {
					songPlayer.destroy();
					songPlayer = null;
				}
				return 1;
			}));
		});

		SongStartEvent.EVENT.register(sp -> {
			if(sp.getId().equals(new Identifier("test:position"))) {
				for(UUID uuid : sp.getPlayerUUIDs()) {
					PlayerEntity player = Nota.getAPI().getServer().getPlayerManager().getPlayer(uuid);
					if(player != null) {
						player.sendMessage(Text.of("Song Started Playing: " + sp.getSong().getTitle()));
					}
				}
			}

		});

		SongEndEvent.EVENT.register(sp -> {
			if(sp.getId().equals(new Identifier("test:position"))) {
				for(UUID uuid : sp.getPlayerUUIDs()) {
					PlayerEntity player = Nota.getAPI().getServer().getPlayerManager().getPlayer(uuid);
					if(player != null) {
						player.sendMessage(Text.of("Song Ended Playing: " + sp.getSong().getTitle()));
					}
				}
			}
		});

		//Example code, you can remove it
		FabricLoader.getInstance().getModContainer(MOD_ID).ifPresent(mod -> {
			ModMetadata meta = mod.getMetadata();
			LOGGER.info(meta.getName() + " " + meta.getVersion().getFriendlyString() + " successfully initialized!");
		});
	}
}
