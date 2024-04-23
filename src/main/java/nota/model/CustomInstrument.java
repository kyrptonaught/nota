package nota.model;

import net.minecraft.sound.SoundEvent;

/**
 * Create custom instruments from a sound file
 */
public class CustomInstrument {

	private final byte index;
	private final String name;
	private final String soundFileName;
	private SoundEvent sound;

	/**
	 * Creates a CustomInstrument
	 *
	 * @param index
	 * @param name
	 * @param soundFileName
	 */
	public CustomInstrument(byte index, String name, String soundFileName) {
		this.index = index;
		this.name = name;
		this.soundFileName = soundFileName.replaceAll(".ogg", "");
		if (this.soundFileName.equalsIgnoreCase("pling")) {
			this.sound = Sound.NOTE_PLING.bukkitSound();
		}
	}

	/**
	 * Gets index of CustomInstrument
	 *
	 * @return index
	 */
	public byte getIndex() {
		return index;
	}

	/**
	 * Gets name of CustomInstrument
	 *
	 * @return name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets file name of the sound
	 *
	 * @return file name
	 */
	public String getSoundFileName() {
		return soundFileName;
	}

	/**
	 * Gets the org.bukkit.Sound enum for this CustomInstrument
	 *
	 * @return org.bukkit.Sound enum
	 */
	public SoundEvent getSound() {
		return sound;
	}
}
