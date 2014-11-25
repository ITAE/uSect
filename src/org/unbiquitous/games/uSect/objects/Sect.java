package org.unbiquitous.games.uSect.objects;

import static java.lang.String.format;

import java.io.File;
import java.io.FilenameFilter;
import java.util.UUID;

import javax.swing.text.Position;

import org.unbiquitous.driver.execution.executionUnity.ExecutionUnity;
import org.unbiquitous.games.uSect.environment.Environment;
import org.unbiquitous.games.uSect.environment.Environment.Stats;
import org.unbiquitous.games.uSect.environment.EnvironmentObject;
import org.unbiquitous.games.uSect.environment.Random;
import org.unbiquitous.games.uSect.objects.Something.Feeding;
import org.unbiquitous.games.uSect.objects.behavior.Artificial;
import org.unbiquitous.games.uSect.objects.behavior.Carnivore;
import org.unbiquitous.games.uSect.objects.behavior.Herbivore;
import org.unbiquitous.games.uSect.objects.behavior.Aggregate;
import org.unbiquitous.json.JSONException;
import org.unbiquitous.json.JSONObject;
import org.unbiquitous.uImpala.engine.asset.Animation;
import org.unbiquitous.uImpala.engine.asset.AssetManager;
import org.unbiquitous.uImpala.engine.asset.SimetricShape;
import org.unbiquitous.uImpala.engine.asset.Sprite;
import org.unbiquitous.uImpala.engine.asset.Text;
import org.unbiquitous.uImpala.engine.core.GameSingletons;
import org.unbiquitous.uImpala.engine.core.GameRenderers;
import org.unbiquitous.uImpala.engine.io.Screen;
import org.unbiquitous.uImpala.util.Color;
import org.unbiquitous.uImpala.util.Corner;
import org.unbiquitous.uImpala.util.math.Point;

public class Sect extends EnvironmentObject {
//	private static final Color HERBIVORE_COLOR = new Color(41, 128, 185,200);
//	private static final Color AGGREGATE_COLOR = new Color(50, 50, 50, 50);
//	private static final Color CARNIVOROUS_COLOR = new Color(211, 84, 0,200);
	private static final Color ATTACK_PAINT = new Color(192, 57, 43,128);
	
	private static final String HERBIVORE_IDLE_SPRITE	= "img/herbIdle.png";
	private static final String HERBIVORE_DAMAGE_SPRITE	= "img/herbDano.png";
	private static final String HERBIVORE_EAT_SPRITE	= "img/herbCome.png";
	private static final String HERBIVORE_WALK_SPRITE	= "img/herbAnda.png";
	
	private static final String CARNIVORE_WALKL_SPRITE 	= "img/carnAndaEsquerda.png";
	private static final String CARNIVORE_WALKR_SPRITE 	= "img/carnAndaDireita.png";
	private static final String CARNIVORE_EATL_SPRITE 	= "img/carnComeEsquerda.png";
	private static final String CARNIVORE_EATR_SPRITE 	= "img/carnComeDireita.png";
	
	private static final String MATING_SPRITE			= "img/love.png";
	
	private static final float HERBIVORE_SCALE_DIVISOR	= 30000.0f;
	private static final float CARNIVORE_SCALE_DIVISOR	= 90000.0f;
	
	private static final float MAX_SIZE	= 1.5f;
	private static final float MIN_SIZE	= 0.6f;
	
	private Animation herbivoreIdleSprite, herbivoreDamageSprite, herbivoreEatSprite, herbivoreWalkSprite;
	private Animation carnivoreWalklSprite, carnivoreWalkrSprite, carnivoreEatlSprite, carnivoreEatrSprite;
	private Animation matingSprite;
	
	private Behavior behavior;
	protected Point currentDir;
	
	private int radius = 30;
	private SimetricShape influence;
	protected Text text;
	private int influenceRadius = 50;
	
	public interface Behavior  extends Cloneable{
		public Something.Feeding feeding();
		public void init(Sect s);
		public void update();
		public void enteredViewRange(Something o);
		public void leftViewRange(Something o);
		public Behavior clone(); 
	}
	
	public Sect() {
		this(new Herbivore());
	}
	
	public Sect(Behavior behavior) {
		this(behavior, UUID.randomUUID());
	}
	
	public Sect(Behavior behavior, UUID id) {
		super(id);

		AssetManager assets = GameSingletons.get(AssetManager.class);
		
		herbivoreIdleSprite		= assets.newAnimation(HERBIVORE_IDLE_SPRITE, 4, 8);
		herbivoreDamageSprite	= assets.newAnimation(HERBIVORE_DAMAGE_SPRITE, 4, 8);
		herbivoreEatSprite		= assets.newAnimation(HERBIVORE_EAT_SPRITE, 4, 8);
		herbivoreWalkSprite		= assets.newAnimation(HERBIVORE_WALK_SPRITE, 4, 8);
		carnivoreWalklSprite	= assets.newAnimation(CARNIVORE_WALKL_SPRITE, 4, 8);
		carnivoreWalkrSprite	= assets.newAnimation(CARNIVORE_WALKR_SPRITE, 4, 8);
		carnivoreEatlSprite		= assets.newAnimation(CARNIVORE_EATL_SPRITE, 4, 8);
		carnivoreEatrSprite		= assets.newAnimation(CARNIVORE_EATR_SPRITE, 4, 8);

		influence = assets.newCircle(new Point(), ATTACK_PAINT, influenceRadius);
		matingSprite = assets.newAnimation(MATING_SPRITE, 8, 8);
				
		this.behavior = behavior;
		behavior.init(this);
	}
	
	public int radius() {
		return radius;
	}
	
	public int influenceRadius() {
		return influenceRadius;
	}
	
	public Behavior behavior() {
		return behavior;
	}
	
	public void update() {
		behavior.update();
	}
	
	public void enteredSight(Something o){
		behavior.enteredViewRange(o);
	}
	
	public void leftSight(Something o) {
		behavior.leftViewRange(o);
	}

	public void moveTo(Point dir) {
		currentDir = dir;
		env.moveTo(this,dir);
	}

	public void attack() {
		env.attack(this);
	}
	
	public void mate() {
		env.mate(this);
	}
	
	public void aggregate(){
		
		env.aggregate(this);
	}
	
	public Point positionOf(UUID id){
		return env.stats(id).position;
	}
	
	public void render(GameRenderers renderers) {
		if(behavior instanceof Carnivore){
			float tamanhoSect = (float) (env.stats(id()).energy)/CARNIVORE_SCALE_DIVISOR;
			
			if(tamanhoSect > MAX_SIZE)
				tamanhoSect = MAX_SIZE;
			else if(tamanhoSect < MIN_SIZE)
				tamanhoSect = MIN_SIZE;
			
			if(env.stats(id()).attackCoolDown > 0)		
				carnivoreEatrSprite.render(GameSingletons.get(Screen.class), (float)position().x, (float)position().y, Corner.CENTER, 1f, 0f, tamanhoSect, tamanhoSect);
			else
				carnivoreWalkrSprite.render(GameSingletons.get(Screen.class), (float)position().x, (float)position().y, Corner.CENTER, 1f, 0f, tamanhoSect, tamanhoSect);
		}else{
			float tamanhoSect = (float) (env.stats(id()).energy)/HERBIVORE_SCALE_DIVISOR;
			
			if(tamanhoSect > MAX_SIZE)
				tamanhoSect = MAX_SIZE;
			else if(tamanhoSect < MIN_SIZE)
				tamanhoSect = MIN_SIZE;
			
			herbivoreWalkSprite.render(GameSingletons.get(Screen.class), (float)position().x, (float)position().y, Corner.CENTER, 1f, 0f, tamanhoSect, tamanhoSect);
		}
		
		if(env.stats(id()).busyCoolDown > 0){
			matingSprite.render(GameSingletons.get(Screen.class), position().x, position().y);
		}
	}
	
	public String toString() {
		return format("Sect:%s@%s[e=%s]",behavior.feeding(), position(),energy());
	}

	public static Sect fromJSON(Environment e, JSONObject json) {
		return fromJSON(e, json, null);
		
	}
	
	public static Sect fromJSON(Environment e, JSONObject json, Point position) {
		UUID id = UUID.fromString(json.optString("id"));
		Sect s = new Sect(deserializeBehavior(json), id);
		if(position == null){
			Screen screen = GameSingletons.get(Screen.class);
			int x = (int) (Math.random() * screen.getWidth());
			int y = (int) (Math.random() * screen.getHeight());
			position = new Point(x,y);
		}
		Stats stats = new Stats(position, json.optLong("energy"));
		return (Sect) e.add(s, stats);
	}

	private static Behavior deserializeBehavior(JSONObject json) {
		Behavior behavior;
		if("Carnivore".equalsIgnoreCase(json.optString("behavior"))){
			behavior = new Carnivore();
		}else if("Aggregate".equalsIgnoreCase(json.optString("behavior"))){
			behavior = new Aggregate();			
		}else if("Artificial".equalsIgnoreCase(json.optString("behavior"))){
			behavior = deserializeArtificialBehavior(json);
		}else{
			behavior = new Herbivore();
		}
		return behavior;
	}

	private static Behavior deserializeArtificialBehavior(JSONObject json) {
		Behavior behavior;
		Feeding feeding = Feeding.valueOf(json.optString("feeding"));
		ExecutionUnity unity = ExecutionUnity.fromJSON(json.optJSONObject("execution"));
		behavior = new Artificial(unity,feeding);
		return behavior;
	}

	public JSONObject toJSON() {
		try {
			JSONObject json = new JSONObject();
			json.put("id", id().toString());
			json.put("behavior", behavior().getClass().getSimpleName());
			if(behavior instanceof Artificial){
				json.put("feeding", behavior().feeding());
				json.put("execution", ((Artificial)behavior()).unity().toJSON());
			}
			json.put("energy", energy());
			return json;
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
	};
}