package nota.model.playmode;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import nota.model.Layer;
import nota.model.Note;
import nota.model.Song;

/**
 * Decides how is {@link Note} played to {@link PlayerEntity}
 */
public abstract class ChannelMode {

	public abstract void play(PlayerEntity player, BlockPos pos, Song song, Layer layer, Note note, float volume, boolean doTranspose);
}
