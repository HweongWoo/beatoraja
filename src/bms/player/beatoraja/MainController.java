package bms.player.beatoraja;

import static bms.player.beatoraja.skin.SkinProperty.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.stream.Stream;

import bms.player.beatoraja.config.SkinConfiguration;
import org.lwjgl.input.Mouse;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter.OutputType;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.StringBuilder;

import bms.player.beatoraja.PlayerResource.PlayMode;
import bms.player.beatoraja.audio.AudioDriver;
import bms.player.beatoraja.audio.GdxAudioDeviceDriver;
import bms.player.beatoraja.audio.GdxSoundDriver;
import bms.player.beatoraja.audio.PortAudioDriver;
import bms.player.beatoraja.config.KeyConfiguration;
import bms.player.beatoraja.decide.MusicDecide;
import bms.player.beatoraja.input.BMSPlayerInputProcessor;
import bms.player.beatoraja.input.mouseData;
import bms.player.beatoraja.ir.IRConnection;
import bms.player.beatoraja.ir.IRResponse;
import bms.player.beatoraja.play.BMSPlayer;
import bms.player.beatoraja.play.TargetProperty;
import bms.player.beatoraja.result.CourseResult;
import bms.player.beatoraja.result.MusicResult;
import bms.player.beatoraja.select.MusicSelector;
import bms.player.beatoraja.select.bar.TableBar;
import bms.player.beatoraja.skin.SkinLoader;
import bms.player.beatoraja.skin.SkinObject.SkinOffset;
import bms.player.beatoraja.skin.SkinProperty;
import bms.player.beatoraja.song.SQLiteSongDatabaseAccessor;
import bms.player.beatoraja.song.SongDatabaseAccessor;
import bms.player.beatoraja.song.SongInformationAccessor;
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.UploadedMedia;
import twitter4j.conf.ConfigurationBuilder;

/**
 * �뜝�럡�뀘�뜝�럡臾뤷뜝�럡�땼�뜝�럡�뀴�뜝�럡�떖�뜝�럡�븙�뜝�럡萸у뜝�럡�떋�뜝�럡荑곩뜝�럡�땽�뜝�럡�떖�뜝�럡�돮�뜝�럡�뀯�뜝�럡占썲뜝�럡�븣
 *
 * @author exch
 */
public class MainController extends ApplicationAdapter {

	public static final String VERSION = "beatoraja 0.5.5";
	
	private static final boolean debug = true;

	/**
	 *
	 */
	private final long boottime = System.currentTimeMillis();
	private final Calendar cl = Calendar.getInstance();
	private long mouseMovedTime;

	private BMSPlayer bmsplayer;
	private MusicDecide decide;
	private MusicSelector selector;
	private MusicResult result;
	private CourseResult gresult;
	private KeyConfiguration keyconfig;
	private SkinConfiguration skinconfig;

	private AudioDriver audio;

	private PlayerResource resource;

	private FreeTypeFontGenerator generator;
	private BitmapFont systemfont;
	private BitmapFont updatefont;

	private MainState current;
	private static MainState currentState;
	/**
	 * �뜝�럥諭싧뜝�럥占쎈쵓�삕野껋���삕�젆占쏙옙�꽱占쎈엡占쎈걢�뜝�럥�렮
	 */
	private long starttime;
	private long nowmicrotime;

	private Config config;
	private PlayerConfig player;
	private PlayMode auto;
	private boolean songUpdated;

	private SongDatabaseAccessor songdb;
	private SongInformationAccessor infodb;

	private IRConnection ir;

	private SpriteBatch sprite;
	/**
	 * 1�뜝�럩�윧�뜝�럡臾뤷뜝�럡�땿�뜝�럡�뀞�뜝�럡苡멨뜝�럥�끉�븨�똻�뼅椰꾬옙�뜝�럡援퀯MS�뜝�럡臾꾢뜝�럡�뀖�뜝�럡�뀞�뜝�럡�땽
	 */
	private Path bmsfile;

	private BMSPlayerInputProcessor input;
	/**
	 * FPS�뜝�럡�럩�뜝�럥吏귛뜝�럥�룙�뜝�럡援됧뜝�럡�뜲�뜝�럡愿띶뜝�럡苡삣뜝�럡肄ⓨ뜝�럡愿�
	 */
	private boolean showfps;
	/**
	 * �뜝�럡臾뤷뜝�럡�땿�뜝�럡�뀞�뜝�럡�돭�뜝�럡�떖�뜝�럡�븶�뜝�럡�뀘�뜝�럡�뀯�뜝�럡�븧�뜝�럡�븕
	 */
	private PlayDataAccessor playdata;

	static final Path configpath = Paths.get("config.json");
	private static final Path songdbpath = Paths.get("songdata.db");
	private static final Path infodbpath = Paths.get("songinfo.db");

	private SystemSoundManager sound;

	private ScreenShotThread screenshot;

	private TwitterUploadThread twitterUpload;

	public static final int timerCount = SkinProperty.TIMER_MAX + 1;
	private final long[] timer = new long[timerCount];
	public static final int offsetCount = SkinProperty.OFFSET_MAX + 1;
	private final SkinOffset[] offset = new SkinOffset[offsetCount];

	protected TextureRegion black;
	protected TextureRegion white;

	public MainController(Path f, Config config, PlayerConfig player, PlayMode auto, boolean songUpdated) {
		this.auto = auto;
		this.config = config;
		this.songUpdated = songUpdated;

		for(int i = 0;i < offset.length;i++) {
			offset[i] = new SkinOffset();
		}

		if(player == null) {
			player = PlayerConfig.readPlayerConfig(config.getPlayername());
		}
		this.player = player;

		this.bmsfile = f;

		try {
			Class.forName("org.sqlite.JDBC");
			songdb = new SQLiteSongDatabaseAccessor(songdbpath.toString(), config.getBmsroot());
			if(config.isUseSongInfo()) {
				infodb = new SongInformationAccessor(infodbpath.toString());
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		playdata = new PlayDataAccessor(config.getPlayername());

		ir = IRConnection.getIRConnection(player.getIrname());
		if(ir != null) {
			if(player.getUserid().length() == 0 || player.getPassword().length() == 0) {
				ir = null;
			} else {
				IRResponse response = ir.login(player.getUserid(), player.getPassword());
				if(!response.isSuccessed()) {
					Logger.getGlobal().warning("IR�뜝�럡爰끻뜝�럡荑곩뜝�럡�떃�뜝�럡�뀰�뜝�럡�뀞�뜝�럡�떋�땱�떚留뚪뇡占� : " + response.getMessage());
					ir = null;
				}
			}
		}

		switch(config.getAudioDriver()) {
		case Config.AUDIODRIVER_PORTAUDIO:
			try {
				audio = new PortAudioDriver(config);
			} catch(Throwable e) {
				e.printStackTrace();
				config.setAudioDriver(Config.AUDIODRIVER_SOUND);
			}
			break;
		}

		sound = new SystemSoundManager(config);
	}

	public SkinOffset getOffset(int index) {
		return offset[index];
	}

	public SongDatabaseAccessor getSongDatabase() {
		return songdb;
	}

	public SongInformationAccessor getInfoDatabase() {
		return infodb;
	}

	public PlayDataAccessor getPlayDataAccessor() {
		return playdata;
	}

	public SpriteBatch getSpriteBatch() {
		return sprite;
	}

	public PlayerResource getPlayerResource() {
		return resource;
	}

	public Config getConfig() {
		return config;
	}

	public PlayerConfig getPlayerConfig() {
		return player;
	}

	public static final int STATE_SELECTMUSIC = 0;
	public static final int STATE_DECIDE = 1;
	public static final int STATE_PLAYBMS = 2;
	public static final int STATE_RESULT = 3;
	public static final int STATE_GRADE_RESULT = 4;
	public static final int STATE_CONFIG = 5;
	public static final int STATE_SKIN_SELECT = 6;

	public void changeState(int state) {
		MainState newState = null;
		switch (state) {
		case STATE_SELECTMUSIC:
			if (this.bmsfile != null) {
				exit();
			} else {
				newState = selector;
			}
			break;
		case STATE_DECIDE:
			newState = decide;
			break;
		case STATE_PLAYBMS:
			if (bmsplayer != null) {
				bmsplayer.dispose();
			}
			bmsplayer = new BMSPlayer(this, resource);
			newState = bmsplayer;
			break;
		case STATE_RESULT:
			newState = result;
			break;
		case STATE_GRADE_RESULT:
			newState = gresult;
			break;
		case STATE_CONFIG:
			newState = keyconfig;
			break;
		case STATE_SKIN_SELECT:
			newState = skinconfig;
			break;
		}

		if (newState != null && current != newState) {
			Arrays.fill(timer, Long.MIN_VALUE);
			if(current != null) {
				current.setSkin(null);
			}
			newState.create();
			newState.getSkin().prepare(newState);
			current = newState;
			currentState = newState;
			starttime = System.nanoTime();
		}
		if (current.getStage() != null) {
			Gdx.input.setInputProcessor(new InputMultiplexer(current.getStage(), input.getKeyBoardInputProcesseor()));
		} else {
			Gdx.input.setInputProcessor(input.getKeyBoardInputProcesseor());
		}
	}

	public void setPlayMode(PlayMode auto) {
		this.auto = auto;

	}

	@Override
	public void create() {
		final long t = System.currentTimeMillis();
		sprite = new SpriteBatch();
		SkinLoader.initPixmapResourcePool(config.getSkinPixmapGen());

		generator = new FreeTypeFontGenerator(Gdx.files.internal("skin/default/VL-Gothic-Regular.ttf"));
		FreeTypeFontParameter parameter = new FreeTypeFontParameter();
		parameter.size = 24;
		systemfont = generator.generateFont(parameter);

		input = new BMSPlayerInputProcessor(config, player);
		switch(config.getAudioDriver()) {
		case Config.AUDIODRIVER_SOUND:
			audio = new GdxSoundDriver(config);
			break;
		case Config.AUDIODRIVER_AUDIODEVICE:
			audio = new GdxAudioDeviceDriver(config);
			break;
		}

		resource = new PlayerResource(audio, config, player);
		selector = new MusicSelector(this, songUpdated);
		decide = new MusicDecide(this);
		result = new MusicResult(this);
		gresult = new CourseResult(this);
		keyconfig = new KeyConfiguration(this);
		skinconfig = new SkinConfiguration(this);
		if (bmsfile != null) {
			if(resource.setBMSFile(bmsfile, auto)) {
				changeState(STATE_PLAYBMS);
			} else {
				// �뜝�룞�삕�뜝�럡臾썲뜝�럡�떖�뜝�럡�븣�뜝�럡�돬�뜝�럡�떖�뜝�럡�돮�뜝�럡苡쏙옙�뇲占쎄텣雅뚭퉵�삕椰꾬옙�뜝�럡苡룟뜝�럡援됧뜝�럡愿쟢xit�뜝�럡援됧뜝�럡�뜲
				changeState(STATE_CONFIG);
				exit();
			}
		} else {
			changeState(STATE_SELECTMUSIC);
		}

		Logger.getGlobal().info("�뜝�럥�뻻�뜝�럩留얍뜝�럥�넁�뜝�럩�걢�뜝�럥�렮(ms) : " + (System.currentTimeMillis() - t));

		Thread polling = new Thread(() -> {
			long time = 0;
			for (;;) {
				final long now = System.nanoTime() / 1000000;
				if (time != now) {
					time = now;
					input.poll();
				} else {
					try {
						Thread.sleep(0, 500000);
					} catch (InterruptedException e) {
					}
				}
			}
		});
		polling.start();

		if(player.getTarget() >= TargetProperty.getAllTargetProperties().length) {
			player.setTarget(0);
		}

		Pixmap plainPixmap = new Pixmap(2,1, Pixmap.Format.RGBA8888);
		plainPixmap.drawPixel(0,0, Color.toIntBits(255,0,0,0));
		plainPixmap.drawPixel(1,0, Color.toIntBits(255,255,255,255));
		Texture plainTexture = new Texture(plainPixmap);
		black = new TextureRegion(plainTexture,0,0,1,1);
		white = new TextureRegion(plainTexture,1,0,1,1);
		plainPixmap.dispose();

		Gdx.gl.glClearColor(0, 0, 0, 1);
	}

	private long prevtime;

	private final StringBuilder message = new StringBuilder();

	@Override
	public void render() {
//		input.poll();
		nowmicrotime = ((System.nanoTime() - starttime) / 1000);

		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		current.render();
		sprite.begin();
		if (current.getSkin() != null) {
			current.getSkin().drawAllObjects(sprite, current);
		}
		sprite.end();

		final Stage stage = current.getStage();
		if (stage != null) {
			stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
			stage.draw();
		}

		// show fps
		if (showfps) {
			sprite.begin();
			systemfont.setColor(Color.PURPLE);
			message.setLength(0);
			systemfont.draw(sprite, message.append("FPS ").append(Gdx.graphics.getFramesPerSecond()), 10,
					config.getResolution().height - 2);
			if(debug) {
				message.setLength(0);
				systemfont.draw(sprite, message.append("Skin Pixmap Images ").append(SkinLoader.getResource().size()), 10,
						config.getResolution().height - 26);
				message.setLength(0);
				systemfont.draw(sprite, message.append("Total Memory Used(MB) ").append(Runtime.getRuntime().totalMemory() / (1024 * 1024)), 10,
						config.getResolution().height - 50);
				message.setLength(0);
				systemfont.draw(sprite, message.append("Total Free Memory(MB) ").append(Runtime.getRuntime().freeMemory() / (1024 * 1024)), 10,
						config.getResolution().height - 74);
				message.setLength(0);
				systemfont.draw(sprite, message.append("Max Sprite In Batch ").append(sprite.maxSpritesInBatch), 10,
						config.getResolution().height - 98);
			}

			sprite.end();
		}
		// show screenshot status
		if (screenshot != null && screenshot.savetime + 2000 > System.currentTimeMillis()) {
			sprite.begin();
			systemfont.setColor(Color.GOLD);
			systemfont.draw(sprite, "Screen shot saved : " + screenshot.path, 100,
					config.getResolution().height - 2);
			sprite.end();
		} else if (twitterUpload != null && twitterUpload.savetime + 2000 > System.currentTimeMillis()) {
			sprite.begin();
			systemfont.setColor(Color.GOLD);
			systemfont.draw(sprite, "Twitter Upload : " + twitterUpload.text, 100,
					config.getResolution().height - 2);
			sprite.end();
		} else if(updateSong != null && updateSong.isAlive()) {
			if(currentState instanceof MusicSelector) {
				sprite.begin();
				updatefont.setColor(0,1,1,0.5f + (System.currentTimeMillis() % 750) / 1000.0f);
				updatefont.draw(sprite, updateSong.message, 100, config.getResolution().height - 2);
				sprite.end();
			}
		}

		final long time = System.currentTimeMillis();
		if(time > prevtime) {
		    prevtime = time;
            //current.input();
            // event - move pressed
            if (mouseData.isMousePressed()) {
            	mouseData.setMousePressed(false);
                current.getSkin().mousePressed(current, mouseData.getMouseButton(), mouseData.getMouseX(), mouseData.getMouseY());
            }
            // event - move dragged
            if (mouseData.isMouseDragged()) {
            	mouseData.setMouseDragged(false);
                current.getSkin().mouseDragged(current, mouseData.getMouseButton(), mouseData.getMouseX(), mouseData.getMouseY());
            }

            // �뜝�럡臾삣뜝�럡�뀡�뜝�럡�븣�뜝�럡�뀪�뜝�럡�떖�뜝�럡�븳�뜝�럡�땽占쎄퉿占쎈ず占쎈�뤷뜝�럥�뼋�븨�뙋�삕
            if(mouseData.isMouseMoved()) {
            	mouseData.setMouseMoved(false);
            	mouseMovedTime = time;
			}
			Mouse.setGrabbed(current == bmsplayer && time > mouseMovedTime + 5000 && Mouse.isInsideWindow());

//			// FPS占쎄퉿占쎈ず占쎈�뤷뜝�럥�뱥�뜝�럩�읂
//            if (input.checkIfFunctionPressed(0)) {
//                showfps = !showfps;
//                input.resetFunctionTime(0);
//            }
            // fullscrees - windowed
//            if (input.checkIfFunctionPressed(3)) {
//                boolean fullscreen = Gdx.graphics.isFullscreen();
//                Graphics.DisplayMode currentMode = Gdx.graphics.getDisplayMode();
//                if (fullscreen) {
//                    Gdx.graphics.setWindowedMode(currentMode.width, currentMode.height);
//                } else {
//                    Gdx.graphics.setFullscreenMode(currentMode);
//                }
//                config.setDisplaymode(fullscreen ? Config.DisplayMode.WINDOW : Config.DisplayMode.FULLSCREEN);
//                input.resetFunctionTime(3);
//            }

            // if (input.getFunctionstate()[4] && input.getFunctiontime()[4] != 0) {
            // int resolution = config.getResolution();
            // resolution = (resolution + 1) % RESOLUTION.length;
            // if (config.isFullscreen()) {
            // Gdx.graphics.setWindowedMode((int) RESOLUTION[resolution].width,
            // (int) RESOLUTION[resolution].height);
            // Graphics.DisplayMode currentMode = Gdx.graphics.getDisplayMode();
            // Gdx.graphics.setFullscreenMode(currentMode);
            // }
            // else {
            // Gdx.graphics.setWindowedMode((int) RESOLUTION[resolution].width,
            // (int) RESOLUTION[resolution].height);
            // }
            // config.setResolution(resolution);
            // input.getFunctiontime()[4] = 0;
            // }

            // screen shot
//            if (input.checkIfFunctionPressed(5)) {
//                if (screenshot == null || screenshot.savetime != 0) {
//                    screenshot = new ScreenShotThread(ScreenUtils.getFrameBufferPixels(0, 0, Gdx.graphics.getBackBufferWidth(),
//                            Gdx.graphics.getBackBufferHeight(), true));
//                    screenshot.start();
//                }
//                input.resetFunctionTime(5);
//            }
//
//            if (input.checkIfFunctionPressed(6)) {
//                if (twitterUpload == null || twitterUpload.savetime != 0) {
//                	twitterUpload = new TwitterUploadThread(ScreenUtils.getFrameBufferPixels(0, 0, Gdx.graphics.getBackBufferWidth(),
//                            Gdx.graphics.getBackBufferHeight(), false), player);
//                	twitterUpload.start();
//                }
//                input.resetFunctionTime(6);
//            }
        }
	}

	@Override
	public void dispose() {
		if (bmsplayer != null) {
			bmsplayer.dispose();
		}
		if (selector != null) {
			selector.dispose();
		}
		if (decide != null) {
			decide.dispose();
		}
		if (result != null) {
			result.dispose();
		}
		if (gresult != null) {
			gresult.dispose();
		}
		if (keyconfig != null) {
			keyconfig.dispose();
		}
		if (skinconfig != null) {
			skinconfig.dispose();
		}
		resource.dispose();
//		input.dispose();
		SkinLoader.getResource().dispose();
		ShaderManager.dispose();
	}

	@Override
	public void pause() {
		current.pause();
	}

	@Override
	public void resize(int width, int height) {
		current.resize(width, height);
	}

	@Override
	public void resume() {
		current.resume();
	}

	public void saveConfig(){
		Config.write(config);
		PlayerConfig.write(player);
		Logger.getGlobal().info("佯몃돍�삕�븨�똻�뼊繹먭쒀�삕占쎌죸�뜝�럡�럩畑띕끃鍮녺빊占�");
	}

	public void exit() {
		saveConfig();

		dispose();
		Gdx.app.exit();
	}

	public BMSPlayerInputProcessor getInputProcessor() {
		return input;
	}

	public AudioDriver getAudioProcessor() {
		return audio;
	}

	public IRConnection getIRConnection() {
		return ir;
	}

	public SystemSoundManager getSoundManager() {
		return sound;
	}

	public long getPlayTime() {
		return System.currentTimeMillis() - boottime;
	}

	public Calendar getCurrnetTime() {
		cl.setTimeInMillis(System.currentTimeMillis());
		return cl;
	}

	public long getStartTime() {
		return starttime / 1000000;
	}

	public long getStartMicroTime() {
		return starttime / 1000;
	}

	public long getNowTime() {
		return nowmicrotime / 1000;
	}

	public long getNowTime(int id) {
		if(isTimerOn(id)) {
			return (nowmicrotime - timer[id]) / 1000;
		}
		return 0;
	}

	public long getNowMicroTime() {
		return nowmicrotime;
	}

	public long getNowMicroTime(int id) {
		if(isTimerOn(id)) {
			return nowmicrotime - timer[id];
		}
		return 0;
	}

	public long getTimer(int id) {
		return timer[id] / 1000;
	}

	public long getMicroTimer(int id) {
		return timer[id];
	}

	public boolean isTimerOn(int id) {
		return timer[id] != Long.MIN_VALUE;
	}

	public void setTimerOn(int id) {
		timer[id] = nowmicrotime;
	}

	public void setTimerOff(int id) {
		timer[id] = Long.MIN_VALUE;
	}

	public void setMicroTimer(int id, long microtime) {
		timer[id] = microtime;
	}

	public void switchTimer(int id, boolean on) {
		if(on) {
			if(timer[id] == Long.MIN_VALUE) {
				timer[id] = nowmicrotime;
			}
		} else {
			timer[id] = Long.MIN_VALUE;
		}
	}

	public static String getClearTypeName() {
		String[] clearTypeName = { "NO PLAY", "FAILED", "ASSIST EASY CLEAR", "LIGHT ASSIST EASY CLEAR", "EASY CLEAR",
				"CLEAR", "HARD CLEAR", "EXHARD CLEAR", "FULL COMBO", "PERFECT", "MAX" };

		if(currentState.getNumberValue(NUMBER_CLEAR) >= 0 && currentState.getNumberValue(NUMBER_CLEAR) < clearTypeName.length) {
			return clearTypeName[currentState.getNumberValue(NUMBER_CLEAR)];
		}

		return "";
	}

	public static String getRankTypeName() {
		String rankTypeName = "";
		if(currentState.getBooleanValue(OPTION_RESULT_AAA_1P)) rankTypeName += "AAA";
		else if(currentState.getBooleanValue(OPTION_RESULT_AA_1P)) rankTypeName += "AA";
		else if(currentState.getBooleanValue(OPTION_RESULT_A_1P)) rankTypeName += "A";
		else if(currentState.getBooleanValue(OPTION_RESULT_B_1P)) rankTypeName += "B";
		else if(currentState.getBooleanValue(OPTION_RESULT_C_1P)) rankTypeName += "C";
		else if(currentState.getBooleanValue(OPTION_RESULT_D_1P)) rankTypeName += "D";
		else if(currentState.getBooleanValue(OPTION_RESULT_E_1P)) rankTypeName += "E";
		else if(currentState.getBooleanValue(OPTION_RESULT_F_1P)) rankTypeName += "F";
		return rankTypeName;
	}


	/**
	 * �뜝�럡�븣�뜝�럡�뀯�뜝�럡�땼�뜝�럡�떖�뜝�럡�떋�뜝�럡�븙�뜝�럡萸у뜝�럡留쇿뜝�럡�돮�뜝�럥�듋�뜝�럥�뵍�뜝�럥�럞�뜝�럡�븣�뜝�럡�땿�뜝�럡留쇿뜝�럡�돰
	 *
	 * @author exch
	 */
	static class ScreenShotThread extends Thread {

		/**
		 * �뜝�럥�듋�뜝�럥�뵍�뜝�럡愿뤹븨���뒙占쎈뻸�뜝�럡愿쇔뜝�럡援닷뜝�럩�걢�뜝�럥�렮
		 */
		private long savetime;
		/**
		 * �뜝�럡�븣�뜝�럡�뀯�뜝�럡�땼�뜝�럡�떖�뜝�럡�떋�뜝�럡�븙�뜝�럡萸у뜝�럡留쇿뜝�럡�돮畑띕끃鍮녺빊�눦�삕占쏙옙
		 */
		private final String path;
		/**
		 * �뜝�럡�븣�뜝�럡�뀯�뜝�럡�땼�뜝�럡�떖�뜝�럡�떋�뜝�럡�븙�뜝�럡萸у뜝�럡留쇿뜝�럡�돮�뜝�럡荑걈ixel�뜝�럡�돭�뜝�럡�떖�뜝�럡�븶
		 */
		private final byte[] pixels;

		public ScreenShotThread(byte[] pixels) {
			this.pixels = pixels;
			final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
			String stateName = "";
			if(currentState instanceof MusicSelector) {
				stateName = "_Music_Select";
			} else if(currentState instanceof MusicDecide) {
				stateName = "_Decide";
			} if(currentState instanceof BMSPlayer) {
				if(currentState.getTextValue(STRING_TABLE_LEVEL).length() > 0){
					stateName = "_Play_" + currentState.getTextValue(STRING_TABLE_LEVEL);
				}else{
					stateName = "_Play_LEVEL" + currentState.getNumberValue(NUMBER_PLAYLEVEL);
				}
				if(currentState.getTextValue(STRING_FULLTITLE).length() > 0) stateName += " " + currentState.getTextValue(STRING_FULLTITLE);
			} else if(currentState instanceof MusicResult || currentState instanceof CourseResult) {
				if(currentState instanceof MusicResult){
					if(currentState.getTextValue(STRING_TABLE_LEVEL).length() > 0){
						stateName += "_" + currentState.getTextValue(STRING_TABLE_LEVEL);
					}else{
						stateName += "_LEVEL" + currentState.getNumberValue(NUMBER_PLAYLEVEL);
					}
				}else{
					stateName += "_";
				}
				if(currentState.getTextValue(STRING_FULLTITLE).length() > 0) stateName += currentState.getTextValue(STRING_FULLTITLE);
				stateName += " " + getClearTypeName();
				stateName += " " + getRankTypeName();
			} else if(currentState instanceof KeyConfiguration) {
				stateName = "_Config";
			} else if(currentState instanceof SkinConfiguration) {
				stateName = "_Skin_Select";
			}
			stateName = stateName.replace("\\", "占쎈쐻�뜝占�").replace("/", "櫻뗫뵃�삕").replace(":", "櫻뗫뵃�삕").replace("*", "櫻뗫뵃�삕").replace("?", "櫻뗫뵃�삕").replace("\"", "�뜝�룞�삕").replace("<", "櫻뗫뵃�삕").replace(">", "櫻뗫뵃�삕").replace("|", "壤깍옙�뜝占�").replace("\t", " ");

			path = "screenshot/" + sdf.format(Calendar.getInstance().getTime()) + stateName +".png";
		}

		@Override
		public void run() {
			// �뜝�럥占쎈�먯삕繹먯쉻�삕亦낆떣�삕亦끸뫜�삕�댆洹⑥삕野껋���삕亦낅뿰�삕�댆洹⑥삕繹먲옙�뜝�럡�뀖�뜝�뜫�썸뤃占�255�뜝�럡苡썲뜝�럡援됧뜝�럡�뜲(=�뜝�럥�뀍占쎄턃�뜝�럥猷얍뜝�럡�럩�뜝�럡�맗�뜝�럡愿쒎뜝�럡援�)
			for(int i = 3;i < pixels.length;i+=4) {
				pixels[i] = (byte) 0xff;
			}
			Pixmap pixmap = new Pixmap(Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight(),
					Pixmap.Format.RGBA8888);
			BufferUtils.copy(pixels, 0, pixmap.getPixels(), pixels.length);
			PixmapIO.writePNG(new FileHandle(path), pixmap);
			pixmap.dispose();
			Logger.getGlobal().info("�뜝�럡�븣�뜝�럡�뀯�뜝�럡�땼�뜝�럡�떖�뜝�럡�떋�뜝�럡�븙�뜝�럡萸у뜝�럡留쇿뜝�럡�돮畑띕끃鍮녺빊占�:" + path);
			savetime = System.currentTimeMillis();
		}
	}

	/**
	 * Twitter�뜝�럥裕딉옙紐쏉옙�뒏�얇굩�삕亦끹룇�삕�댆猿볦삕疫뀁슱�삕繹먲옙
	 */
	static class TwitterUploadThread extends Thread {

		/**
		 * �뜝�럥�듋�뜝�럥�뵍�뜝�럡愿뤹븨���뒙占쎈뻸�뜝�럡愿쇔뜝�럡援닷뜝�럩�걢�뜝�럥�렮
		 */
		private long savetime;

		/**
		 * �뜝�럥�듋�뜝�럥�뵍�뜝�럡愿뤹븨���뒙占쎈뻸�뜝�럡愿쇔뜝�럡援닷뜝�럩�걢�뜝�럥�렮
		 */
		private String text = "";

		private final PlayerConfig player;

		/**
		 * �뜝�럡�븣�뜝�럡�뀯�뜝�럡�땼�뜝�럡�떖�뜝�럡�떋�뜝�럡�븙�뜝�럡萸у뜝�럡留쇿뜝�럡�돮�뜝�럡荑걈ixel�뜝�럡�돭�뜝�럡�떖�뜝�럡�븶
		 */
		private final byte[] pixels;

		public TwitterUploadThread(byte[] pixels, PlayerConfig player) {
			this.pixels = pixels;
			this.player = player;
			java.lang.StringBuilder builder = new java.lang.StringBuilder();
			if(currentState instanceof MusicSelector) {
				// empty
			} else if(currentState instanceof MusicDecide) {
				// empty
			} if(currentState instanceof BMSPlayer) {
				if(currentState.getTextValue(STRING_TABLE_NAME).length() > 0){
					builder.append(currentState.getTextValue(STRING_TABLE_LEVEL));
				}else{
					builder.append("LEVEL");
					builder.append(currentState.getNumberValue(NUMBER_PLAYLEVEL));
				}
				if(currentState.getTextValue(STRING_FULLTITLE).length() > 0) {
					builder.append(" ");
					builder.append(currentState.getTextValue(STRING_FULLTITLE));
				}
			} else if(currentState instanceof MusicResult || currentState instanceof CourseResult) {
				if(currentState instanceof MusicResult) {
					if(currentState.getTextValue(STRING_TABLE_NAME).length() > 0){
						builder.append(currentState.getTextValue(STRING_TABLE_LEVEL));
					}else{
						builder.append("LEVEL");
						builder.append(currentState.getNumberValue(NUMBER_PLAYLEVEL));
					}
				}
				if(currentState.getTextValue(STRING_FULLTITLE).length() > 0) {
					builder.append(" ");
					builder.append(currentState.getTextValue(STRING_FULLTITLE));
				}
				builder.append(" ");
				builder.append(getClearTypeName());
				builder.append(" ");
				builder.append(getRankTypeName());
			} else if(currentState instanceof KeyConfiguration) {
				// empty
			} else if(currentState instanceof SkinConfiguration) {
				// empty
			}
			text = builder.toString();
			text = text.replace("\\", "占쎈쐻�뜝占�").replace("/", "櫻뗫뵃�삕").replace(":", "櫻뗫뵃�삕").replace("*", "櫻뗫뵃�삕").replace("?", "櫻뗫뵃�삕").replace("\"", "�뜝�룞�삕").replace("<", "櫻뗫뵃�삕").replace(">", "櫻뗫뵃�삕").replace("|", "壤깍옙�뜝占�").replace("\t", " ");
		}

		@Override
		public void run() {
			ConfigurationBuilder cb = new ConfigurationBuilder();
			cb.setOAuthConsumerKey(player.getTwitterConsumerKey())
			  .setOAuthConsumerSecret(player.getTwitterConsumerSecret())
			  .setOAuthAccessToken(player.getTwitterAccessToken())
			  .setOAuthAccessTokenSecret(player.getTwitterAccessTokenSecret());
			TwitterFactory twitterFactory = new TwitterFactory(cb.build());
			Twitter twitter = twitterFactory.getInstance();

			Pixmap pixmap = null;
	        try {
				// �뜝�럥占쎈�먯삕繹먯쉻�삕亦낆떣�삕亦끸뫜�삕�댆洹⑥삕野껋���삕亦낅뿰�삕�댆洹⑥삕繹먲옙�뜝�럡�뀖�뜝�뜫�썸뤃占�255�뜝�럡苡썲뜝�럡援됧뜝�럡�뜲(=�뜝�럥�뀍占쎄턃�뜝�럥猷얍뜝�럡�럩�뜝�럡�맗�뜝�럡愿쒎뜝�럡援�)
				for(int i = 3;i < pixels.length;i+=4) {
					pixels[i] = (byte) 0xff;
				}

				// create png byte stream
				pixmap = new Pixmap(Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight(),
						Pixmap.Format.RGBA8888);
				BufferUtils.copy(pixels, 0, pixmap.getPixels(), pixels.length);
				ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();
				PixmapIO.PNG png = new PixmapIO.PNG((int)(pixmap.getWidth() * pixmap.getHeight() * 1.5f));
				png.write(byteArrayOutputStream, pixmap);
				byte[] imageBytes=byteArrayOutputStream.toByteArray();
				ByteArrayInputStream byteArrayInputStream=new ByteArrayInputStream(imageBytes);

				// Upload Media and Post
				UploadedMedia mediastatus = twitter.uploadMedia("from beatoraja", byteArrayInputStream);
				Logger.getGlobal().info("Twitter Media Upload:" + mediastatus.toString());
				StatusUpdate update = new StatusUpdate(text);
				update.setMediaIds(new long[]{mediastatus.getMediaId()});
				Status status = twitter.updateStatus(update);
				Logger.getGlobal().info("Twitter Post:" + status.toString());
				savetime = System.currentTimeMillis();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if(pixmap != null) pixmap.dispose();
			}
		}
	}

	private UpdateThread updateSong;

	public void updateSong(String path) {
		if (updateSong == null || !updateSong.isAlive()) {
			updateSong = new SongUpdateThread(path);
			updateSong.start();
		} else {
			Logger.getGlobal().warning("占쎈�ワ옙�쑞占쎈윧�뜝�럩�윫�뜝�럥�꽌庸뉗빢�삕�뜝�럡荑곩뜝�럡援닷뜝�럡�꺗�뜝�럡�굺占쎈윫�뜝�럥�꽌亦껁끆�굺�굜�냵�삕野껋꼻�삕�뙴�떣�삕�뤃�끀�굦占쏙옙椰꾊띿삕�뤃占썲뜝�럡猿섇뜝�럡愿쇔뜝�럡援�");
		}
	}

	public void updateTable(TableBar reader) {
		if (updateSong == null || !updateSong.isAlive()) {
			updateSong = new TableUpdateThread(reader);
			updateSong.start();
		} else {
			Logger.getGlobal().warning("占쎈�ワ옙�쑞占쎈윧�뜝�럩�윫�뜝�럥�꽌庸뉗빢�삕�뜝�럡荑곩뜝�럡援닷뜝�럡�꺗�뜝�럡�굺占쎈윫�뜝�럥�꽌亦껁끆�굺�굜�냵�삕野껋꼻�삕�뙴�떣�삕�뤃�끀�굦占쏙옙椰꾊띿삕�뤃占썲뜝�럡猿섇뜝�럡愿쇔뜝�럡援�");
		}
	}

	abstract class UpdateThread extends Thread {

		private String message;

		public UpdateThread(String message) {
			this.message = message;
			FreeTypeFontParameter parameter = new FreeTypeFontParameter();
			parameter.size = 24;
			parameter.characters += message;
			if(updatefont != null) {
				updatefont.dispose();
			}
			updatefont = generator.generateFont(parameter);

		}
	}

	/**
	 * 占쎈�ワ옙�쑞占쎈윧�뜝�럡�돭�뜝�럡�떖�뜝�럡�븶�뜝�럡臾쒎뜝�럡�떖�뜝�럡�븣�뜝�럩�윫�뜝�럥�꽌�뜝�럥�럞�뜝�럡�븣�뜝�럡�땿�뜝�럡留쇿뜝�럡�돰
	 *
	 * @author exch
	 */
	class SongUpdateThread extends UpdateThread {

		private final String path;

		public SongUpdateThread(String path) {
			super("updating folder : " + (path == null ? "ALL" : path));
			this.path = path;
		}

		public void run() {
			getSongDatabase().updateSongDatas(path, false, getInfoDatabase());
		}
	}

	/**
	 * �뜝�럩�쑘�뜝�럩援�俑앾옙�뜝�뜾源쀯옙踰껓옙�윫�뜝�럥�꽌�뜝�럥�럞�뜝�럡�븣�뜝�럡�땿�뜝�럡留쇿뜝�럡�돰
	 *
	 * @author exch
	 */
	class TableUpdateThread extends UpdateThread {

		private final TableBar accessor;

		public TableUpdateThread(TableBar bar) {
			super("updating table : " + bar.getAccessor().name);
			accessor = bar;
		}

		public void run() {
			TableData td = accessor.getAccessor().read();
			if (td != null) {
				accessor.getAccessor().write(td);
				accessor.setTableData(td);
			}
		}
	}

	public static class SystemSoundManager {
		private Array<Path> bgms = new Array();
		private Path currentBGMPath;
		private Array<Path> sounds = new Array();
		private Path currentSoundPath;

		public SystemSoundManager(Config config) {
			scan(Paths.get(config.getBgmpath()), bgms, "select.");
			scan(Paths.get(config.getSoundpath()), sounds, "clear.");
			Logger.getGlobal().info("�솾袁⑸�울옙�떑�뜝�럡愿드뜝�럡�뜳�뜝�럡援퀯GM Set : " + bgms.size + " Sound Set : " + sounds.size);
		}

		public void shuffle() {
			if(bgms.size > 0) {
				currentBGMPath = bgms.get((int) (Math.random() * bgms.size));
			}
			if(sounds.size > 0) {
				currentSoundPath = sounds.get((int) (Math.random() * sounds.size));
			}
			Logger.getGlobal().info("BGM Set : " + currentBGMPath + " Sound Set : " + currentSoundPath);
		}

		public Path getBGMPath() {
			return currentBGMPath;
		}

		public Path getSoundPath() {
			return currentSoundPath;
		}

		private void scan(Path p, Array<Path> paths, String name) {
			if (Files.isDirectory(p)) {
				try (Stream<Path> sub = Files.list(p)) {
					sub.forEach(new Consumer<Path>() {
						@Override
						public void accept(Path t) {
							scan(t, paths, name);
						}
					});
				} catch (IOException e) {
				}
			} else if (p.getFileName().toString().toLowerCase().equals(name + "wav") ||
					p.getFileName().toString().toLowerCase().equals(name + "ogg")) {
				paths.add(p.getParent());
			}

		}
	}
}
