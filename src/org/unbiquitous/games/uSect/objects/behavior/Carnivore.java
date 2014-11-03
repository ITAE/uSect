package org.unbiquitous.games.uSect.objects.behavior;

import org.unbiquitous.games.uSect.objects.Sect.Behavior;
import org.unbiquitous.games.uSect.objects.Something;
import org.unbiquitous.games.uSect.objects.Something.Feeding;

public class Carnivore extends TargetFocused{

	protected long waitToMateAgain; 
	
	@Override
	public Feeding feeding() {
		return Feeding.CARNIVORE;
	}
	
	public void enteredViewRange(Something o){
		if(isSect(o) || isCorpse(o)){
			targetsInSight.add(o);
		}
		sortTargets();
	}

	private boolean isCorpse(Something o) {
		return o.type() == Something.Type.CORPSE;
	}

	private boolean isSect(Something o) {
		return o.type() == Something.Type.SECT;
	}
	
	@Override
	public void update() {
		if(sect.energy() < 5*initialEnergy){
			super.update();
		}
		Something target = target();
		if (target != null && insideInfluenceRadius(target)){
			if(itsMatingTime(target)){
				sect.mate();
				waitToMateAgain = 10*50;
			}else{
				if(sect.energy() < 5*initialEnergy){
					sect.attack();
				}
			}
		}
		waitToMateAgain --;
	}

	private boolean itsMatingTime(Something target) {
		return wantToMate(target) && hasMatingEnergy() && inTheMood();
	}

	private boolean inTheMood() {
		return waitToMateAgain <= 0;
	}
	
	@Override
	protected Something target() {
		for(Something s : targetsInSight){
			if(isCorpse(s)){
				return s;
			}
		}
		return super.target();
	}
	
	public Behavior clone() {
		return new Carnivore();
	}
}