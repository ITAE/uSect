package org.unbiquitous.games.uSect;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.UUID;

import org.fest.assertions.core.Condition;
import org.junit.Before;
import org.junit.Test;
import org.unbiquitous.games.uSect.environment.Environment;
import org.unbiquitous.games.uSect.objects.Sect;
import org.unbiquitous.games.uSect.objects.behavior.Artificial;
import org.unbiquitous.uImpala.engine.asset.AssetManager;
import org.unbiquitous.uImpala.engine.core.GameSingletons;
import org.unbiquitous.uImpala.engine.core.GameSettings;
import org.unbiquitous.uImpala.engine.io.MouseManager;
import org.unbiquitous.uImpala.engine.io.Screen;
import org.unbiquitous.uImpala.engine.io.ScreenManager;
import org.unbiquitous.uImpala.engine.time.DeltaTime;
import org.unbiquitous.uos.core.adaptabitilyEngine.Gateway;

public class SetUpGameTest {
	private DeltaTime timer;
	private ScreenManager screens;
	private Screen screen;
	private GameSettings settings;

	@Before public void setUp(){
		TestUtils.setUpEnvironment();
		
		timer = mock(DeltaTime.class);
		GameSingletons.put(DeltaTime.class, timer);
		
		screens = mock(ScreenManager.class);
		screen = mock(Screen.class);
		when(screens.create()).thenReturn(screen);
		GameSingletons.put(ScreenManager.class, screens);
		
		settings = GameSingletons.get(GameSettings.class);
	}
	
	@Test public void deltaTimeIs30(){
		new StartScene();
		verify(timer).setUPS(30);
	}
	
	@Test public void setUpOpensAScreen(){
		new StartScene();
		verify(screen).open();
		assertThat(GameSingletons.get(Screen.class)).isEqualTo(screen);
		assertThat(GameSingletons.get(AssetManager.class)).isNotNull();
	}
	
	@Test public void setUpOpensAScreenWithSpecifiedWidthIfRequested(){
		settings.put("usect.width", 800);
		settings.put("usect.height", 600);
		new StartScene();
		verify(screen).open("uSect",800,600,false,null);
	}
	
	@Test public void populatesAnEnvironmentWithSects(){
		StartScene scene = new StartScene();
		scene.update();
		assertThat(scene.getChildren()).hasSize(1);
		Environment e = (Environment) scene.getChildren().get(0);
		assertThat(e.sects()).isNotEmpty();
	}
	
	@Test public void noPlayerMustBeSetByDefault(){
		StartScene scene = new StartScene();
		scene.update();
		Environment e = (Environment) scene.getChildren().get(0);
		assertThat(e.players()).isEmpty();
	}
	
	@Test public void setPlayerIfInformed(){
//		settings.put("usect.player.name","John");
		UUID id = UUID.randomUUID();
		settings.put("usect.player.id",id.toString());
		GameSingletons.put(MouseManager.class, new MouseManager());
		StartScene scene = new StartScene();
		scene.update();
		Environment e = (Environment) scene.getChildren().get(0);
		assertThat(e.players()).hasSize(1);
		assertThat(e.players().get(0).id()).isEqualTo(id);
	}
	
	@Test public void loadsArtificialSects(){
		StringBuilder script = new StringBuilder()
		.append("function update()\n")
		.append("end\n")
		;
		settings.put("usect.artificials",script.toString());
		StartScene scene = new StartScene();
		scene.update();
		Environment e = (Environment) scene.getChildren().get(0);
		assertThat(e.sects()).haveExactly(1, new Condition<Sect>("an Artificial") {
			public boolean matches(Sect value) {
				return value.behavior() instanceof Artificial;
			}
		});
	}
	
	@Test public void loadsMultipleArtificialSects(){
		StringBuilder script = new StringBuilder()
		.append("function update()\n")
		.append("end\n")
		;
		settings.put("usect.artificials",Arrays.asList(script.toString(),script.toString()));
		StartScene scene = new StartScene();
		scene.update();
		Environment e = (Environment) scene.getChildren().get(0);
		assertThat(e.sects()).haveExactly(2, new Condition<Sect>("an Artificial") {
			public boolean matches(Sect value) {
				return value.behavior() instanceof Artificial;
			}
		});
	}
	
	
	@Test public void deploysASectDriverToManageInteractions(){
		Gateway gateway = mock(Gateway.class);
		GameSingletons.put(Gateway.class, gateway);
		new StartScene();
	//	verify(gateway).addDriver(any(USectDriver.class));
	}
}
