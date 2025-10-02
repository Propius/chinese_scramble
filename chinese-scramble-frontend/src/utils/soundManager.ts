/**
 * Sound Manager - Handles all game audio using Web Audio API
 *
 * Features:
 * - Simple frequency-based sounds (no external audio files needed)
 * - Volume control
 * - Sound on/off toggle
 * - Lightweight and performant
 *
 * Usage:
 * import { soundManager } from './utils/soundManager';
 * soundManager.playDrop();
 * soundManager.playWin();
 */

class SoundManager {
  private audioContext: AudioContext | null = null;
  private enabled: boolean = true;
  private volume: number = 0.3;

  constructor() {
    if (typeof window !== 'undefined') {
      // Lazy initialization of AudioContext
      this.initAudioContext();
    }
  }

  private initAudioContext() {
    try {
      // @ts-ignore
      const AudioContextClass = window.AudioContext || window.webkitAudioContext;
      this.audioContext = new AudioContextClass();
    } catch (e) {
      console.warn('Web Audio API not supported:', e);
      this.enabled = false;
    }
  }

  /**
   * Play a simple tone
   */
  private playTone(frequency: number, duration: number, type: OscillatorType = 'sine') {
    if (!this.enabled || !this.audioContext) return;

    try {
      const oscillator = this.audioContext.createOscillator();
      const gainNode = this.audioContext.createGain();

      oscillator.connect(gainNode);
      gainNode.connect(this.audioContext.destination);

      oscillator.frequency.value = frequency;
      oscillator.type = type;

      gainNode.gain.setValueAtTime(this.volume, this.audioContext.currentTime);
      gainNode.gain.exponentialRampToValueAtTime(
        0.01,
        this.audioContext.currentTime + duration
      );

      oscillator.start(this.audioContext.currentTime);
      oscillator.stop(this.audioContext.currentTime + duration);
    } catch (e) {
      console.warn('Error playing sound:', e);
    }
  }

  /**
   * Play multiple tones in sequence (melody)
   */
  private playMelody(notes: Array<{ frequency: number; duration: number; type?: OscillatorType }>) {
    if (!this.enabled || !this.audioContext) return;

    let currentTime = this.audioContext.currentTime;

    notes.forEach(note => {
      try {
        const oscillator = this.audioContext!.createOscillator();
        const gainNode = this.audioContext!.createGain();

        oscillator.connect(gainNode);
        gainNode.connect(this.audioContext!.destination);

        oscillator.frequency.value = note.frequency;
        oscillator.type = note.type || 'sine';

        gainNode.gain.setValueAtTime(this.volume, currentTime);
        gainNode.gain.exponentialRampToValueAtTime(
          0.01,
          currentTime + note.duration
        );

        oscillator.start(currentTime);
        oscillator.stop(currentTime + note.duration);

        currentTime += note.duration;
      } catch (e) {
        console.warn('Error in melody:', e);
      }
    });
  }

  /**
   * Sound: Tile drop/placement
   */
  playDrop() {
    this.playTone(440, 0.1, 'triangle');
  }

  /**
   * Sound: Tile removed
   */
  playRemove() {
    this.playTone(220, 0.15, 'square');
  }

  /**
   * Sound: Answer submitted
   */
  playSubmit() {
    this.playMelody([
      { frequency: 523, duration: 0.1 }, // C
      { frequency: 659, duration: 0.1 }, // E
      { frequency: 784, duration: 0.15 } // G
    ]);
  }

  /**
   * Sound: Correct answer (win)
   */
  playWin() {
    this.playMelody([
      { frequency: 523, duration: 0.15 },  // C
      { frequency: 659, duration: 0.15 },  // E
      { frequency: 784, duration: 0.15 },  // G
      { frequency: 1047, duration: 0.3 }   // C (high)
    ]);
  }

  /**
   * Sound: Wrong answer (lose)
   */
  playLose() {
    this.playMelody([
      { frequency: 392, duration: 0.2 },   // G
      { frequency: 349, duration: 0.2 },   // F
      { frequency: 294, duration: 0.3 }    // D
    ]);
  }

  /**
   * Sound: Hint requested
   */
  playHint() {
    this.playMelody([
      { frequency: 880, duration: 0.1 },   // A
      { frequency: 1047, duration: 0.15 }  // C
    ]);
  }

  /**
   * Sound: Button click
   */
  playClick() {
    this.playTone(660, 0.05, 'square');
  }

  /**
   * Sound: Timer warning (when time is running out)
   */
  playWarning() {
    this.playTone(880, 0.2, 'sawtooth');
  }

  /**
   * Sound: Reset/Clear board
   */
  playReset() {
    this.playMelody([
      { frequency: 440, duration: 0.08, type: 'square' },
      { frequency: 330, duration: 0.08, type: 'square' },
      { frequency: 220, duration: 0.12, type: 'square' }
    ]);
  }

  /**
   * Sound: Timeout (time's up)
   */
  playTimeout() {
    this.playMelody([
      { frequency: 494, duration: 0.15, type: 'sawtooth' },  // B
      { frequency: 466, duration: 0.15, type: 'sawtooth' },  // A#
      { frequency: 440, duration: 0.15, type: 'sawtooth' },  // A
      { frequency: 392, duration: 0.3, type: 'sawtooth' }    // G
    ]);
  }

  /**
   * Enable/disable all sounds
   */
  setEnabled(enabled: boolean) {
    this.enabled = enabled;
    localStorage.setItem('soundEnabled', enabled.toString());
  }

  /**
   * Set volume (0.0 to 1.0)
   */
  setVolume(volume: number) {
    this.volume = Math.max(0, Math.min(1, volume));
    localStorage.setItem('soundVolume', this.volume.toString());
  }

  /**
   * Get current enabled status
   */
  isEnabled(): boolean {
    return this.enabled;
  }

  /**
   * Get current volume
   */
  getVolume(): number {
    return this.volume;
  }

  /**
   * Initialize from localStorage
   */
  loadSettings() {
    const savedEnabled = localStorage.getItem('soundEnabled');
    const savedVolume = localStorage.getItem('soundVolume');

    if (savedEnabled !== null) {
      this.enabled = savedEnabled === 'true';
    }

    if (savedVolume !== null) {
      this.volume = parseFloat(savedVolume);
    }
  }
}

// Export singleton instance
export const soundManager = new SoundManager();

// Load saved settings on initialization
if (typeof window !== 'undefined') {
  soundManager.loadSettings();
}
