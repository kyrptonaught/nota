package nota.model.playmode;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.BlockPos;
import nota.model.Layer;
import nota.model.Note;
import nota.model.Song;
import nota.utils.InstrumentUtils;
import nota.utils.NoteUtils;

/**
 * {@link Note} is played inside of {@link PlayerEntity}'s head.
 */
public class MonoMode extends ChannelMode {

	@Override
	public void play(PlayerEntity player, BlockPos pos, Song song, Layer layer, Note note, float volume, boolean doTranspose) {
		float pitch;
		if (doTranspose) {
			pitch = NoteUtils.getPitchTransposed(note);
		} else {
			pitch = NoteUtils.getPitchInOctave(note);
		}
		player.playSoundToPlayer(InstrumentUtils.getInstrument(note, song.getFirstCustomInstrumentIndex(), song.getCustomInstruments(), doTranspose), SoundCategory.RECORDS, volume, pitch);
	}
}
