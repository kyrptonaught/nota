package nota;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import nota.event.SongEndEvent;
import nota.model.RepeatMode;
import nota.model.Song;
import nota.player.EntitySongPlayer;
import nota.player.PositionSongPlayer;
import nota.player.RadioSongPlayer;
import nota.player.SongPlayer;
import nota.utils.NBSDecoder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class NotaCommands {
	private static final HashMap<String, SongPlayer> songPlayers = new HashMap<>();

	public static void onInitialize() {
		SongEndEvent.EVENT.register(songPlayer -> {
			String song = songPlayer.getId().getPath();
			songPlayers.remove(song).destroy();
		});

		createDir(FabricLoader.getInstance().getGameDir().resolve("nbs"));
	}

	public static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
		dispatcher.register(CommandManager.literal("playnbs")
			.requires((source) -> source.hasPermissionLevel(2))
			.then(CommandManager.argument("song", StringArgumentType.word())
				.suggests(NotaCommands::getAvailableNBSFiles)
				.then(CommandManager.argument("looping", BoolArgumentType.bool())
					.then(CommandManager.argument("listeners", EntityArgumentType.players())
						.then(CommandManager.literal("GLOBAL")
							.executes(context -> {
								Song song = getSong(context);
								RadioSongPlayer songPlayer = new RadioSongPlayer(song);

								primeSongPlayer(songPlayer, context);
								songPlayer.setPlaying(true);
								return 1;
							}))
						.then(CommandManager.literal("BLOCKPOS")
							.then(CommandManager.argument("blockpos", BlockPosArgumentType.blockPos())
								.then(CommandManager.argument("distance", IntegerArgumentType.integer(1))
									.then(CommandManager.argument("fade", BoolArgumentType.bool())
										.executes(context -> {
											BlockPos blockPos = BlockPosArgumentType.getBlockPos(context, "blockpos");
											int distance = IntegerArgumentType.getInteger(context, "distance");
											boolean fade = BoolArgumentType.getBool(context, "fade");

											Song song = getSong(context);
											PositionSongPlayer songPlayer = new PositionSongPlayer(song, context.getSource().getWorld());
											songPlayer.setBlockPos(blockPos);
											songPlayer.setDistance(distance);
											songPlayer.setFade(fade);

											primeSongPlayer(songPlayer, context);
											songPlayer.setPlaying(true);
											return 1;
										})))))
						.then(CommandManager.literal("ENTITY")
							.then(CommandManager.argument("entity", EntityArgumentType.entity())
								.then(CommandManager.argument("distance", IntegerArgumentType.integer(1))
									.then(CommandManager.argument("fade", BoolArgumentType.bool())
										.executes(context -> {
											Entity entity = EntityArgumentType.getEntity(context, "entity");
											int distance = IntegerArgumentType.getInteger(context, "distance");
											boolean fade = BoolArgumentType.getBool(context, "fade");

											Song song = getSong(context);
											EntitySongPlayer songPlayer = new EntitySongPlayer(song);
											songPlayer.setEntity(entity);
											songPlayer.setDistance(distance);
											songPlayer.setFade(fade);

											primeSongPlayer(songPlayer, context);
											songPlayer.setPlaying(true);
											return 1;
										})))))))));
		dispatcher.register(CommandManager.literal("stopnbs")
			.requires((source) -> source.hasPermissionLevel(2))
			.then(CommandManager.literal("ALL")
				.executes(context -> {
					songPlayers.values().forEach(SongPlayer::destroy);
					songPlayers.clear();
					return 1;
				}))
			.then(CommandManager.argument("song", StringArgumentType.word())
				.suggests((context, builder) -> {
					songPlayers.keySet().forEach(builder::suggest);
					return builder.buildFuture();
				})
				.executes(context -> {
					String songFile = StringArgumentType.getString(context, "song");
					songPlayers.remove(songFile).destroy();
					return 1;
				})));
	}

	private static Song getSong(CommandContext<ServerCommandSource> context) {
		String songFile = StringArgumentType.getString(context, "song");
		Path input = FabricLoader.getInstance().getGameDir().resolve("nbs");
		Path path = input.resolve(songFile + ".nbs");

		return NBSDecoder.parse(path.toFile());
	}

	private static void primeSongPlayer(SongPlayer songPlayer, CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "listeners");
		boolean looping = BoolArgumentType.getBool(context, "looping");
		String songFile = StringArgumentType.getString(context, "song").toLowerCase().replaceAll("[^a-z0-9_.-]", "");

		songPlayer.setEnable10Octave(true);
		songPlayer.setAutoDestroy(true);
		songPlayer.setRepeatMode(looping ? RepeatMode.ALL : RepeatMode.NONE);
		songPlayer.setId(new Identifier("nota", songFile));
		players.forEach(songPlayer::addPlayer);

		if (songPlayers.containsKey(songFile)) songPlayers.remove(songFile).destroy();
		songPlayers.put(songFile, songPlayer);
	}

	private static CompletableFuture<Suggestions> getAvailableNBSFiles(CommandContext<ServerCommandSource> context, final SuggestionsBuilder builder) {
		Path input = FabricLoader.getInstance().getGameDir().resolve("nbs");

		try (Stream<Path> files = Files.walk(input)) {
			files.forEach(path1 -> {
				if (path1.getFileName().toString().endsWith(".nbs"))
					builder.suggest(path1.getFileName().toString().replace(".nbs", ""));
			});
		} catch (Exception ignored) {
		}
		return builder.buildFuture();
	}

	public static boolean createDir(Path directory) {
		try {
			Files.createDirectories(directory);
			return true;
		} catch (IOException exception) {
			System.out.println("Failed to create directory: " + directory);
			exception.printStackTrace();
		}
		return false;
	}
}
