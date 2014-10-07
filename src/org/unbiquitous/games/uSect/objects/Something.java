package org.unbiquitous.games.uSect.objects;

import java.util.UUID;

import org.unbiquitous.games.uSect.environment.Environment;
import org.unbiquitous.uImpala.util.math.Point;

public class Something {
	public enum Type {NUTRIENT, SECT, CORPSE}
	public enum Feeding{NONE, HERBIVORE, CARNIVORE, AGGREGATE}
	
	private UUID id; 
	private Type type;
	private Environment env;
	private Feeding feeding;
	
	//TODO: must be created inside the EnvironmentObject
	public Something(UUID id, Environment env, Type type, Feeding feeding) {
		super();
		this.id = id;
		this.env = env;
		this.type = type;
		this.feeding = feeding;
	}

	public UUID id() {		return id;}
	public Point position() {	return env.stats(id).position;}
	public Type type() {	return type;}
	public Feeding feeding() {	return feeding;}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Something){
			return ((Something)obj).id.equals(this.id) ;
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return id.hashCode();
	}
}