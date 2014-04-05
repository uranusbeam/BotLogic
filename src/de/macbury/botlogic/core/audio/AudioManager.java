package de.macbury.botlogic.core.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.Disposable;

/**
 * Created by macbury on 31.03.14.
 */
public class AudioManager implements Disposable {
  public Sound click;
  public Sound move;
  public Music music;
  public Sound rotation;

  public AudioManager() {
    this.music              = Gdx.audio.newMusic(Gdx.files.internal("audio/design.mp3"));
    music.setLooping(true);
    music.setVolume(0.1f);
    this.rotation           = Gdx.audio.newSound(Gdx.files.internal("audio/rotate_effect.mp3"));
    this.move               = Gdx.audio.newSound(Gdx.files.internal("audio/move_effect.mp3"));
    this.click              = Gdx.audio.newSound(Gdx.files.internal("audio/effects/click.ogg"));
    //rotation.setLooping(1, true);
  }

  @Override
  public void dispose() {
    music.dispose();
    rotation.dispose();
  }
}
