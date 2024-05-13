package nota.model.playmode;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
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

		SoundEvent sound = InstrumentUtils.getInstrument(note, song.getFirstCustomInstrumentIndex(), song.getCustomInstruments(), doTranspose);
		((ServerPlayerEntity) player).networkHandler.sendPacket(new PlaySoundS2CPacket(Registries.SOUND_EVENT.getEntry(sound), SoundCategory.RECORDS, pos.getX(), pos.getY(), pos.getZ(), volume, pitch, player.getRandom().nextLong()));
	}
}
