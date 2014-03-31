package de.macbury.botlogic.core.controller.actions;

import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;
import aurelienribon.tweenengine.equations.Elastic;
import de.macbury.botlogic.core.controller.GameAction;
import de.macbury.botlogic.core.tween.ModelEntityAccessor;

/**
 * Created by macbury on 31.03.14.
 */
public class MoveAction extends GameAction implements TweenCallback {
  @Override
  public void onStart() {
    Tween.to(level.robot, ModelEntityAccessor.POSITION_XZ, 1.0f).target(20, 30).setCallback(this).start(level.tweenManager);
  }

  @Override
  public void update(double delta) {

  }

  @Override
  public void onEnd() {

  }

  @Override
  public void onEvent(int i, BaseTween<?> baseTween) {
    finish();
  }
}
