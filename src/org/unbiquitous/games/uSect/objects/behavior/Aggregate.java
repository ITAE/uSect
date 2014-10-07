package org.unbiquitous.games.uSect.objects.behavior;

import org.unbiquitous.games.uSect.objects.Sect.Behavior;
import org.unbiquitous.games.uSect.objects.Something;
import org.unbiquitous.games.uSect.objects.Something.Feeding;

public class Aggregate extends TargetFocused {

	@Override
	public Feeding feeding() {
		return Feeding.AGGREGATE;
	}

	@Override
	public void enteredViewRange(Something o) {
		if(isAggregate(o)){
			targetsInSight.add(o);
		}
		sortTargets();
	}
	
	private boolean isAggregate(Something o) {
		return o.feeding() == Something.Feeding.AGGREGATE;
	}
	
	@Override
	public void update() {
		//if(sect.aggregateValue() < 5){
			super.update();
		//}
		Something target = target();
		if (target != null && insideInfluenceRadius(target)){
			sect.aggregate();			
		}
	}

	@Override
	public Behavior clone() {
		// TODO Auto-generated method stub
		return null;
	}

	
	
}
