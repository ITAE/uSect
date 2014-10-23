package org.unbiquitous.games.uSect;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.unbiquitous.driver.execution.executionUnity.ExecutionUnity;
import org.unbiquitous.games.uSect.environment.Environment;
import org.unbiquitous.games.uSect.environment.Environment.Stats;
import org.unbiquitous.games.uSect.environment.Random;
import org.unbiquitous.games.uSect.objects.Player;
import org.unbiquitous.games.uSect.objects.Sect;
import org.unbiquitous.games.uSect.objects.Something.Feeding;
import org.unbiquitous.games.uSect.objects.behavior.Aggregate;
import org.unbiquitous.games.uSect.objects.behavior.Artificial;
import org.unbiquitous.games.uSect.objects.behavior.Carnivore;
import org.unbiquitous.games.uSect.objects.behavior.Herbivore;
import org.unbiquitous.uImpala.engine.asset.AssetManager;
import org.unbiquitous.uImpala.engine.core.GameSingletons;
import org.unbiquitous.uImpala.engine.core.GameObjectTreeScene;
import org.unbiquitous.uImpala.engine.core.GameSettings;
import org.unbiquitous.uImpala.engine.io.Screen;
import org.unbiquitous.uImpala.engine.io.ScreenManager;
import org.unbiquitous.uImpala.engine.time.DeltaTime;
import org.unbiquitous.uImpala.util.math.Point;
import org.unbiquitous.uos.core.adaptabitilyEngine.Gateway;

public class StartScene extends GameObjectTreeScene {

	private Screen screen;
	private Environment env;

	public StartScene() {
		DeltaTime deltaTime = GameSingletons.get(DeltaTime.class);
		deltaTime.setUPS(30);
		
		screen = GameSingletons.get(ScreenManager.class).create();
		GameSettings settings = GameSingletons.get(GameSettings.class);
		
		if(settings.containsKey("usect.width") && settings.containsKey("usect.height")){
			screen.open("uSect", 
								settings.getInt("usect.width"), settings.getInt("usect.height"), 
								false, null);
		}else{
			screen.open();
		}
		
		GameSingletons.put(Screen.class, screen);
		GameSingletons.put(AssetManager.class,assets());
		setUpEnvironment(settings);
		
		@SuppressWarnings("unused")
		Gateway gateway = GameSingletons.get(Gateway.class);
		//gateway.addDriver(new USectDriver(settings,env));
	}

	private void setUpEnvironment(GameSettings settings) {
		env = new Environment((DeviceStats) settings.get("usect.devicestats"));
		populateSects(settings, env);
		populatePlayer(settings, env);
		add(env);
	}

	private void populateSects(GameSettings settings, Environment e) {
		populateHerbivores(settings, e);
		populateCarnivores(settings, e);
		populateArtificials(settings, e);
		populateAggregators(settings, e);
	}

	private void populateHerbivores(GameSettings settings, Environment e) {
		int multiplier = screen.getHeight()*screen.getWidth()/1000/100;
		int numberOfHerbivores = (int) (Random.v()*multiplier) + 10;
		for(int i = 0 ; i < numberOfHerbivores; i++){
			Sect sect = new Sect(new Herbivore());
			e.addSect(sect, randScreenPosition());
		}
	}
	
	public void populateAggregators(GameSettings settings, Environment e){
		int numberOfAggregators = 0;
		
		for(int i = 0; i < numberOfAggregators; i++){
			Sect sect = new Sect(new Aggregate());
			e.addSect(sect, randScreenPosition());
		}
	}
	
	private void populateCarnivores(GameSettings settings, Environment e) {
		int numberOfCarnivores = 1;
		for(int i = 0 ; i < numberOfCarnivores; i++){
			Sect sect = new Sect(new Carnivore());
			int startEnergy = settings.getInt("usect.initial.energy",30*60*10)*4;
			e.add(sect, new Stats(randScreenPosition(),startEnergy));
		}
	}

	private void populateArtificials(GameSettings settings, Environment e) {
		if(settings.containsKey("usect.artificials")){
			List<String> scripts = defineArtificialScripts(settings);
			for(String script: scripts){
				ExecutionUnity behavior = new ExecutionUnity(script);
				Feeding type = defineArtificialFeeding();
				Sect sect = new Sect(new Artificial(behavior, type));
				e.addSect(sect, randScreenPosition());
			}
		}
	}

	@SuppressWarnings("unchecked")
	private List<String> defineArtificialScripts(GameSettings settings) {
		Object value = settings.get("usect.artificials");
		if(value instanceof List){
			return (List<String>) value;
		}
		return Arrays.asList(value.toString());
	}

	private Feeding defineArtificialFeeding() {
		Feeding type = Feeding.CARNIVORE;
		if(Random.v() > 0.5){
			type = Feeding.HERBIVORE;
		}
		return type;
	}

	private Point randScreenPosition() {
		Point position = new Point(
				(int)(Random.v()*screen.getWidth()),
				(int)(Random.v()*screen.getHeight())
				);
		return position;
	}

	private void populatePlayer(GameSettings settings, Environment e) {
		if(settings.containsKey("usect.player.id")){
			UUID id = UUID.fromString(settings.getString("usect.player.id"));
			e.addPlayer(new Player(id));
		}
	}
	
	@Override
	public void update() {
		if (screen.isCloseRequested()) {
			GameSingletons.get(org.unbiquitous.uImpala.engine.core.Game.class).quit();
		}
		super.update();
	}
}
