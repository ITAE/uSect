package org.unbiquitous.games.uSect.environment;

import java.util.HashSet;
import java.util.Set;

import org.unbiquitous.games.uSect.objects.Sect;
import org.unbiquitous.uImpala.engine.core.GameSettings;
import org.unbiquitous.uImpala.engine.core.GameSingletons;
import org.unbiquitous.uImpala.util.math.Point;

public class AggregateManager {
	private Set<Sect> aggregating = new HashSet<Sect>();
	private Environment env;
	@SuppressWarnings("unused")
	private int aggregated = 0;
	GameSettings settings = new GameSettings();
	
	public AggregateManager(Environment env){
		this.env = env;		
		GameSettings settings = GameSingletons.get(GameSettings.class);
		aggregated = settings.getInt("usect.aggregated", 0);
	}
	
	public void add(Sect aggregator){
		if(!aggregating.contains(aggregator)){
			aggregating.add(aggregator);	
		}
	}
	
	public void update(){
		processAggregation();
		
	}
	
	public void processAggregation(){
		for(Sect aggregate1 : aggregating){
			for(Sect aggregate2 : env.sects()){
				checkAggregation(aggregate1, aggregate2);
			}
		}
		aggregating.clear();
	}
	
	public void checkAggregation(Sect aggregate1, Sect aggregate2){
		if(aggregate1.id() != aggregate2.id()){
			env.changeStats(aggregate1, Environment.Stats.change().aggregated());
			env.changeStats(aggregate2, Environment.Stats.change().aggregated());
			if(aggregate1.behavior().feeding().equals(aggregate2.behavior().feeding()) 
					&& aggregate1.position().distanceTo(aggregate2.position()) <= aggregate1.influenceRadius() ){
				//aggregate1.moveTo(aggregate2.position());
				aggregate1.moveTo(aggregate2.position());
			//	aggregate2.moveTo(aggregate1.position());
				System.out.println("x: " + aggregate1.position().x + " y:" +aggregate1.position().y);
			}
		}
	}
}
