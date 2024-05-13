package nota.player;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import nota.Nota;
import nota.model.Layer;
import nota.model.Note;
import nota.model.Playlist;
import nota.model.Song;

@SuppressWarnings("unused")
public class EntitySongPlayer extends RangeSongPlayer {

	private Entity entity;

	public EntitySongPlayer(Song song) {
		super(song);
	}

	public EntitySongPlayer(Playlist playlist) {
		super(playlist);
	}

	/**
	 * Returns true if the Player is able to hear the current {@link EntitySongPlayer}
	 *
	 * @param player in range
	 * @return ability to hear the current {@link EntitySongPlayer}
	 */
	@Override
	public boolean isInRange(PlayerEntity player) {
		return player.getBlockPos().isWithinDistance(entity.getBlockPos(), getDistance());
	}

	/**
	 * Set entity associated with this {@link EntitySongPlayer}
	 *
	 * @param entity entity
	 */
	public void setEntity(Entity entity) {
		this.entity = entity;
	}

	/**
	 * Get {@link Entity} associated with this {@link EntitySongPlayer}
	 *
	 * @return entity
	 */
	public Entity getEntity() {
		return entity;
	}

	@Override
	public void playTick(PlayerEntity player, int tick) {
		if (!entity.isAlive()) {
			if (autoDestroy) {
				destroy();
			} else {
				setPlaying(false);
			}
		}
		if (!player.getWorld().getRegistryKey().equals(entity.getWorld().getRegistryKey())) {
			return; // not in same world
		}

		byte playerVolume = Nota.getPlayerVolume(player);

		for (Layer layer : song.getLayerHashMap().values()) {
			Note note = layer.getNote(tick);
			if (note == null) continue;

			float volume = ((layer.getVolume() * (int) this.volume * (int) playerVolume * note.getVelocity()) / 100_00_00_00F);

			if (isInRange(player)) {
				playerList.put(player.getUuid(), true);
				channelMode.play(player, getFade() ? entity.getBlockPos() : player.getBlockPos(), song, layer, note, volume, !enable10Octave);
			} else {
				playerList.put(player.getUuid(), false);
			}
		}
	}
}
