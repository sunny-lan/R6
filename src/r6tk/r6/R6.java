package r6tk.r6;

import java.util.ArrayList;
import java.util.List;

import r6tk.r6.geom.R6Error;

public class R6 {
	List<LightRay> rays;
	List<ICollideable> transforms;
	List<IR6Listener> listeners;
	List<Object> objects;

	public R6() {
		transforms = new ArrayList<>();
		rays = new ArrayList<>();
		objects = new ArrayList<>();
	}

	public void add(LightRay ray) throws R6Exception {
		rays.add(ray);
		objects.add(ray);
		update();
	}

	public void add(ICollideable transform) throws R6Exception {
		transforms.add(transform);
		objects.add(transform);
		update();
	}

	public void add(IR6Listener listener) {
		listeners.add(listener);
	}

	public void remove(LightRay ray) throws R6Exception {
		rays.remove(ray);
		objects.remove(ray);
		update();
	}

	public void remove(ICollideable transform) throws R6Exception {
		transforms.remove(transform);
		objects.remove(transform);
		update();
	}

	public void remove(IR6Listener listener) {
		listeners.remove(listener);
	}

	public LightRay findReflection(LightRay ray) throws R6Exception {
		ICollideable min = null;
		double mindist = -1;
		for (ICollideable object : transforms) {
			double xint;
			double yint;
			try {
				xint = object.getXInt(ray.head);
				yint = object.getYInt(ray.head);
			} catch (R6Exception e) {
				if (e.e == R6Error.no_solutions)
					continue;
				else
					throw new R6Exception(R6Error.friendship_is_magic);
			}
			double dist = Math
					.sqrt(Math.pow(Math.abs(ray.head.x1() - xint), 2) + Math.pow(Math.abs(ray.head.y1() - yint), 2));
			if (min == null) {
				min = object;
				mindist = dist;
			} else {
				if (dist < mindist) {
					min = object;
					mindist = dist;
				}
			}
		}
		if (mindist == -1)
			throw new R6Exception(R6Error.no_collision);
		return new LightRay(min.applyTransformation(ray.head));
	}

	public void trace(LightRay ray) throws R6Exception {
		LightRay currentRay = ray;
		while (true) {
			LightRay nextRay;
			try {
				nextRay = findReflection(currentRay);
			} catch (R6Exception e) {
				if (e.e == R6Error.no_collision)
					break;
				else
					throw new R6Exception(R6Error.friendship_is_magic);
			}
			currentRay.bounce = nextRay;
			currentRay = nextRay;
		}
	}

	public void update() throws R6Exception {
		for (LightRay ray : rays)
			trace(ray);

		for (IR6Listener listener : listeners)
			listener.update();
	}
}