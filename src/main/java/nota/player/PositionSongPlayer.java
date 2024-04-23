package nota.player;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import nota.Nota;
import nota.model.Layer;
import nota.model.Note;
import nota.model.Playlist;
import nota.model.Song;

/**
 * SongPlayer created at a specified BlockPos
 */
public class PositionSongPlayer extends RangeSongPlayer {
	private BlockPos pos;
	World world;

	public PositionSongPlayer(Song song, World world) {
		super(song);
		this.world = world;
	}

	public PositionSongPlayer(Playlist playlist, World world) {
		super(playlist);
		this.world = world;
	}

	/**
	 * Gets location on which is the PositionSongPlayer playing
	 *
	 * @return {@link BlockPos}
	 */
	public BlockPos getBlockPos() {
		return this.pos;
	}

	/**
	 * Sets location on which is the PositionSongPlayer playing
	 */
	public void setBlockPos(BlockPos pos) {
		this.pos = pos;
	}

	@Override
	public void playTick(PlayerEntity player, int tick) {
		if (!player.getWorld().getRegistryKey().equals(world.getRegistryKey())) {
			return; // not in same world
		}

		byte playerVolume = Nota.getPlayerVolume(player);

		for (Layer layer : song.getLayerHashMap().values()) {
			Note note = layer.getNote(tick);
			if (note == null) continue;

			float volume = ((layer.getVolume() * (int) this.volume * (int) playerVolume * note.getVelocity()) / 100_00_00_00F)
				* ((1F / 16F) * getDistance());

			if (isInRange(player)) {
				this.playerList.put(player.getUuid(), true);
				this.channelMode.play(player, pos, song, layer, note, volume, !enable10Octave);
			} else {
				this.playerList.put(player.getUuid(), false);
			}
		}
	}

	/**
	 * Returns true if the Player is able to hear the current PositionSongPlayer
	 *
	 * @param player in range
	 * @return ability to hear the current PositionSongPlayer
	 */
	@Override
	public boolean isInRange(PlayerEntity player) {
		return player.getBlockPos().isWithinDistance(pos, getDistance());
	}
}
