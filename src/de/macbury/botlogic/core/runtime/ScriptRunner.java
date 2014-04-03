package de.macbury.botlogic.core.runtime;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import de.macbury.botlogic.core.controller.GameController;
import de.macbury.botlogic.core.controller.api.MathLib;
import org.mozilla.javascript.*;

import java.util.ArrayList;

/**
 * Created by macbury on 28.03.14.
 */
public class ScriptRunner implements Disposable {
  private static final String TAG = "ScriptRunner";
  private static final String SCRIPT_SETUP_FUNCTION = "setup();";
  private static final String SCRIPT_LOOP_FUNCTION = "loop();";
  private GameController gameController;

  private ArrayList<ScriptRuntimeListener> listeners;
  private ScriptRunnable currentRunnable;
  public ScriptRunner(GameController gameController) {
    this.gameController = gameController;
    Gdx.app.log(TAG, "Initializing script runner");
    this.listeners = new ArrayList<ScriptRuntimeListener>();
  }

  public void execute(String source) {
    if (currentRunnable != null) {
      if (currentRunnable.isRunning()){
        throw new GdxRuntimeException("Already running!");
      } else {
        currentRunnable.dispose();
      }
    }

    currentRunnable = new ScriptRunnable(source);
    Thread thread = new Thread(currentRunnable);
    thread.start();
  }

  public boolean isRunning() {
    return (currentRunnable != null && currentRunnable.isRunning());
  }

  public void finish() {
    if (currentRunnable != null) {
      currentRunnable.stop();
    }
  }

  public void addListener(ScriptRuntimeListener listener) {
    if (listeners.indexOf(listener) == -1) {
      this.listeners.add(listener);
    }
  }

  public void removeListener(ScriptRuntimeListener listener) {
    if (listeners.indexOf(listener) != -1) {
      this.listeners.remove(listener);
    }
  }

  @Override
  public void dispose() {
    listeners.clear();
    Gdx.app.log(TAG, "Disposing");
    if (currentRunnable != null) {
      currentRunnable.dispose();
    }
  }

  private class ScriptRunnable implements Runnable, Disposable {
    private String SCRIPT_CONTEXT_NAME = "";
    private ScriptContextFactory factory;
    private String source;
    private final Object[] tempArgs;
    private ScriptableObject objectScope;
    private ScriptContext context;
    private boolean running;

    public ScriptRunnable(String source) {
      this.source = source;

      this.factory = (ScriptContextFactory)ScriptContextFactory.getGlobal();
      tempArgs = new Object[1];

      SCRIPT_CONTEXT_NAME = "Script";
    }

    @Override
    public void run() {
      this.context   = (ScriptContext)factory.enterContext();
      context.setOptimizationLevel(-1);

      running = true;

      Gdx.app.log(TAG, "Reseting scope");
      this.objectScope = context.initStandardObjects();
      context.setInterrputFlag(false);

      synchronized (gameController) {
        objectScope.put("robot", objectScope, gameController.getRobotController());
        objectScope.put("math", objectScope, new MathLib());
        context.evaluateString(objectScope, Gdx.files.internal("sketches/rdk/helpers.js").readString(), SCRIPT_CONTEXT_NAME, 0, null);
      }

      Gdx.app.log(TAG, "Running code");

      for(ScriptRuntimeListener listener : listeners) {
        listener.onScriptStart(ScriptRunner.this);
      }

      try {
        context.evaluateString(objectScope, source, SCRIPT_CONTEXT_NAME, 0, null);
        context.evaluateString(objectScope, SCRIPT_SETUP_FUNCTION, SCRIPT_CONTEXT_NAME, 0, null);
        running = true;
        while(running) {
          context.evaluateString(objectScope, SCRIPT_LOOP_FUNCTION, SCRIPT_CONTEXT_NAME, 0, null);
          try {
            Thread.sleep(10);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
      } catch (ScriptInterputException e) {
        running = false;
        for(ScriptRuntimeListener listener : listeners) {
          listener.onScriptInterput(ScriptRunner.this);
        }
      } catch (RhinoException e) {
        running = false;
        for(ScriptRuntimeListener listener : listeners) {
          listener.onScriptError(ScriptRunner.this, e);
        }
      } finally {
        for(ScriptRuntimeListener listener : listeners) {
          running = false;
          listener.onScriptFinish(ScriptRunner.this);
        }
        Gdx.app.log(TAG, "Exiting context");
        Context.exit();
      }
    }

    public boolean isRunning() {
      return running;
    }

    public void stop() {
      if (context != null) {
        synchronized (context) {
          context.interrputScript();
        }
        running = false;
      }
    }

    @Override
    public void dispose() {
      running = false;
      if (context != null) {
        context.interrputScript();
      }
    }
  }
}