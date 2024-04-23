package nota.player;

import net.minecraft.entity.player.PlayerEntity;
import nota.model.Playlist;
import nota.model.Song;

/**
 * SongPlayer playing only in specified distance
 */
public abstract class RangeSongPlayer extends SongPlayer {
	private int distance = 16;

	public RangeSongPlayer(Song song) {
		super(song);
	}

	public RangeSongPlayer(Playlist playlist) {
		super(playlist);
	}

	/**
	 * Sets distance in blocks where would be player able to hear sound.
	 *
	 * @param distance (Default 16 blocks)
	 */
	public void setDistance(int distance) {
		this.distance = distance;
	}

	public int getDistance() {
		return distance;
	}

	/**
	 * Returns true if the Player is able to hear the current RangeSongPlayer
	 *
	 * @param player in range
	 * @return ability to hear the current RangeSongPlayer
	 */
	public abstract boolean isInRange(PlayerEntity player);

}
