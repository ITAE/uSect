package org.unbiquitous.games.uSect.environment;

import java.util.UUID;

import org.unbiquitous.uImpala.engine.core.GameObject;
import org.unbiquitous.uImpala.util.math.Point;

public abstract class EnvironmentObject extends GameObject {
	private UUID id;
	protected Environment env;

	public EnvironmentObject() {
		this(UUID.randomUUID());
	}
	
	public EnvironmentObject(UUID id) {
		this.id = id;
	}
	
	protected void update() {
	}

	protected void wakeup(Object... args) {
	}

	protected void destroy() {
	}

	public UUID id(){ return id;}
	
	public void setEnv(Environment env) {
		this.env = env;
	}
	
	public Point position() {
		return env.stats(id).position;
	}
	
	public Long energy() {
		return env.stats(id).energy;
	}
	
	public int aggregateValue(){
		return env.stats(id).aggregated;
	}
	
	public boolean equals(Object obj) {
		if (obj instanceof EnvironmentObject) {
			return ((EnvironmentObject) obj).id.equals(this.id);
		}
		return false;
	}

	public int hashCode() {
		return id.hashCode();
	}
}