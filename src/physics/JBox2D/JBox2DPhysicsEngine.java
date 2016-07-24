package physics.JBox2D;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.World;
import physics.*;

public class JBox2DPhysicsEngine implements PhysicsEngine {

	private World world;

	private int velocityIterations = 8;
	private int positionIterations = 3;

	public JBox2DPhysicsEngine(float[] gravity) {

		world = new World(new Vec2(gravity[0],  gravity[1]));
	}

	@Override
	public void update(float delta) {

		world.step(delta, velocityIterations, positionIterations);
//		System.out.println(bodies.values().iterator().next().getPosition());
	}

	@Override
	public PhysicsObject createPhysicsObject(PhysicsObjectDef physicsObjectDef) {

		Body body = ((JBox2DPhysicsObjectDef) physicsObjectDef).applyToWorld(world);
		return new JBox2DPhysicsObject(body);
	}

	@Override
	public boolean removePhysicsObject(PhysicsObject physicsObject) {

		world.destroyBody(((JBox2DPhysicsObject) physicsObject).getBody());

		return true;
	}

	public World getWorld() {

		return world;
	}

	@Override
	public String[] settingsNames() {
		
		return new String[]{"Position Iterations", "Velocity Iterations"};
	}

	@Override
	public String settingOptions(int setting) {
		
		final String[] OPTIONS = {"integer > 0", "integer > 0"};
		
		return OPTIONS[setting];
	}

	@Override
	public boolean setSetting(int setting, Object option) {
		
		switch (setting) {
		
		case(0): positionIterations = (int) option;
			break;
		case(1): velocityIterations = (int) option;
			break;
		default:
			return false;
		}
		
		return true;
	}

	@Override
	public PhysicsObjectDef newPhysicsObjectDef(PhysicsObject.Type physicsType) {

		return new JBox2DPhysicsObjectDef(physicsType);
	}

	@Override
	public physics.Body newBody() {

		return new JBox2DFixture(false);
	}

	@Override
	public Sensor newSensor() {

		return new JBox2DFixture(true);
	}
}