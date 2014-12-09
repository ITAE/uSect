package org.unbiquitous.games.uSect.environment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.unbiquitous.games.uSect.environment.Environment.Stats;
import org.unbiquitous.games.uSect.objects.Sect;
import org.unbiquitous.games.uSect.objects.Something;
import org.unbiquitous.games.uSect.objects.Something.Type;
import org.unbiquitous.uos.core.messageEngine.messages.Response;

class SectManager implements EnvironemtObjectManager{
	private Environment env;
	private List<Sect> sects;
	private List<Sect> sectsAddedThisTurn;
	private List<Sect> sectsThatDiedThisTurn;

	public SectManager(Environment env) {
		this.env = env;
		this.sects = new ArrayList<Sect>();
		this.sectsAddedThisTurn = new ArrayList<Sect>();
		this.sectsThatDiedThisTurn = new ArrayList<Sect>();
	}

	public EnvironmentObject add(EnvironmentObject o) {
		if(o instanceof Sect){
			return add((Sect) o);
		}
		return null;
	}
	
	Sect add(Sect s){
		sects.add(s);
		sectsAddedThisTurn.add(s);
		return s;
	}
	
	@Override
	public void remove(EnvironmentObject o) {
		sects.remove(o);
	}

	@Override
	public void removeAll(Collection<EnvironmentObject> c) {
		sects.removeAll(c);
	}

	List<Sect> sects(){
		return sects;
	}
	
	public void update() {
		for(Sect s : sects){
			updateSect(s);
		}
		//TODO: clear stats
		sects.removeAll(sectsThatDiedThisTurn);
		for(Sect dead: sectsThatDiedThisTurn){
			env.addCorpse(dead.position());
			for(Sect s : sects()){
				s.leftSight(new Something(dead.id(), env, Type.SECT, s.behavior().feeding()));
			}
		}
		sectsThatDiedThisTurn.clear(); //TODO: remove from other places
		sectsAddedThisTurn.clear(); //TODO: remove from other places
	}

	private void updateSect(Sect s) {
		checkForNewSects(s);
		s.update();
		env.changeStats(s, Stats.change().energy(-1));
		if(env.stats(s.id()).energy <= 0){
			sectsThatDiedThisTurn.add(s);
		}
	}

	private void checkForNewSects(Sect s) {
		for(Sect newSect : sectsAddedThisTurn){
			if(!s.equals(newSect)){
				s.enteredSight(new Something(newSect.id(), env, Something.Type.SECT, newSect.behavior().feeding()));
			}
		}
	}
}