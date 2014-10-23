package org.unbiquitous.games.uSect.environment;

import org.unbiquitous.games.uSect.environment.Environment.Stats;
import org.unbiquitous.games.uSect.objects.Sect;
import org.unbiquitous.games.uSect.objects.Something;
import org.unbiquitous.uImpala.engine.core.GameSingletons;
import org.unbiquitous.uImpala.engine.core.GameSettings;
import org.unbiquitous.uImpala.engine.io.Screen;
import org.unbiquitous.uImpala.util.math.Point;

class MovementManager {
	private Environment env;
	private int speed;
	private Screen screen;
	
	public MovementManager(Environment env) {
		this.env = env;
		GameSettings settings = GameSingletons.get(GameSettings.class);
		screen = GameSingletons.get(Screen.class);
		speed = settings.getInt("usect.speed.value",1);
	}

	public void moveTo(Sect sect, Point dir) {
		if(sect.behavior().feeding() == Something.Feeding.AGGREGATE && (sect.position().x == dir.x && sect.position().y == dir.y)){
			sect.setFrozen(true);
		} else {
			env.moveTo(sect.id(), determineFinalPosition(sect, adjustDirection(dir)));
			env.changeStats(sect, Stats.change().energy(-1));
		}	
	}

	private Point adjustDirection(Point dir) {
		//TODO: parametrize speed (and use CPU info for this)
		int base = Math.min(speed, dir.module());
		Point newDir = applySpeedProportionToVectorDirection(dir, base);
		addRandomRemainderToDirectionVector(dir, base, newDir);
		return newDir;
	}

	private Point applySpeedProportionToVectorDirection(Point dir, int speed) {
		Point newDir = new Point();
		newDir.x = (int) (speed * ((float)dir.x)/dir.module());
		newDir.y = (int) (speed * ((float)dir.y)/dir.module());
		return newDir;
	}
	
	private void addRandomRemainderToDirectionVector(Point dir, int speed, Point newDir) {
		if(newDir.module() < speed){
			if(Random.v() > 0.5 ){
				newDir.x += (int) (1 * Math.signum(dir.x));
			}else {
				newDir.y += (int) (1 * Math.signum(dir.y));
			}
		}
	}
	
	private Point determineFinalPosition(Sect sect, Point dir) {
		Point forwardPosition = new Point(sect.position().x + dir.x, sect.position().y + dir.y);
		checkBorders(forwardPosition);
		return checkColision(sect, dir, forwardPosition);
	}

	private void checkBorders(Point forwardPosition) {
		forwardPosition.x = Math.max(forwardPosition.x, 0);
		forwardPosition.x = Math.min(forwardPosition.x, screen.getWidth());
		forwardPosition.y = Math.max(forwardPosition.y, 0);
		forwardPosition.y = Math.min(forwardPosition.y, screen.getHeight());
	}

	private Point checkColision(Sect sect, Point dir, Point forwardPosition) {
		if(!hasColided(sect, forwardPosition)){
			return forwardPosition;
		}else if (Random.v() > 0.5){
			Point backwardsPosition = new Point(sect.position().x - dir.x, sect.position().y - dir.y);
			return backwardsPosition;
		}
		return sect.position();
	}

	private boolean hasColided(Sect sect, Point newPos) {
		boolean hasColided = false;
		for(Sect s: env.sects()){
			if(!sect.equals(s) && s.position().distanceTo(newPos) < sect.radius()){
				hasColided = true;
			}
		}
		return hasColided;
	}
}