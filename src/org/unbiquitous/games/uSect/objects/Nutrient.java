package org.unbiquitous.games.uSect.objects;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.unbiquitous.games.uSect.environment.EnvironmentObject;
import org.unbiquitous.games.uSect.objects.Something.Feeding;
import org.unbiquitous.games.uSect.objects.Something.Type;
import org.unbiquitous.uImpala.engine.asset.Animation;
import org.unbiquitous.uImpala.engine.asset.AssetManager;
import org.unbiquitous.uImpala.engine.asset.SimetricShape;
import org.unbiquitous.uImpala.engine.core.GameSingletons;
import org.unbiquitous.uImpala.engine.core.GameRenderers;
import org.unbiquitous.uImpala.engine.io.Screen;
import org.unbiquitous.uImpala.util.Color;
import org.unbiquitous.uImpala.util.math.Point;

public class Nutrient extends EnvironmentObject{
	private Set<Sect> targetOf;
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected Map<Sect, Integer> absortionTable = new HashMap();
	
	protected int radius = 10;
	protected SimetricShape shape;
	protected Animation shapeAnimated;
	protected Something.Type type = Type.NUTRIENT;
	
	private Sect hasBeenConsumedBy;

	public Nutrient() {
		AssetManager assets = GameSingletons.get(AssetManager.class);
		shape = assets.newCircle(new Point(), Color.GREEN.darker(), radius);
		shapeAnimated = assets.newAnimation("img/fruta.png", 4, 8);
		targetOf = new HashSet<Sect>();
	}

	public Sect hasBeenConsumedBy() {
		return hasBeenConsumedBy;
	}
	
	public void inContactWith(Sect s) {
		absortionTable.put(s, 1+absortionTable.get(s));
		if(absortionTable.get(s) >= 5){
			notifyAbsortionToAll();
			hasBeenConsumedBy = s;
		}
	}

	private void notifyAbsortionToAll() {
		for(Sect s: targetOf){
			s.leftSight(new Something(id(), env, type, Feeding.NONE));
		}
	}

	public void insightOf(Sect s) {
		if(! targetOf.contains(s)){
			s.enteredSight(new Something(id(), env, type, Feeding.NONE)); 
			targetOf.add(s);
			absortionTable.put(s, 0);
		};
	}

	public void render(GameRenderers renderers) {
		if(type == type.NUTRIENT){
			shapeAnimated.render(GameSingletons.get(Screen.class), (float)position().x, (float)position().y);
		} else if (type == type.CORPSE){
			shape.center(position());
			shape.render();
		}
		
		//shapeAnimated.render(GameSingletons.get(Screen.class), (float)position().x, (float)position().y);
	}
}