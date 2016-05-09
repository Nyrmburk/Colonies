package activity;

import java.awt.*;
import java.util.Stack;

import graphics.RenderContext;
import gui.*;

public abstract class Activity {
	
	private static Stack<Activity> stack = new Stack<Activity>();
	private static boolean killCurrentActivity = false;
	
	private View view;
	private RenderContext renderContext;
	
	protected abstract void onCreate();
	protected abstract void onStart();
	protected abstract void onResume();
	protected abstract void onPause();
	protected abstract void onStop();
	protected abstract void onDestroy();
	
	public abstract void onUpdate(int delta);
	
	public static void update(int delta) {

		if (killCurrentActivity) {
			killCurrentActivity();
			return;
		}

		Activity currentActivity = Activity.currentActivity();

		if (currentActivity != null) {

			currentActivity.onUpdate(delta);
			currentActivity.getView().revalidate();
		}
	}
	
	public void setView(View view) {

		this.view = view;
		view.revalidate();
	}

	public View getView() {

		return view;
	}

	public void setRenderContext(RenderContext renderContext) {

		this.renderContext = renderContext;
	}

	public RenderContext getRenderContext() {

		return renderContext;
	}
	
	public static void createActivity(Activity activity) {

		if (killCurrentActivity)
			killCurrentActivity();

		if (!stack.isEmpty()) {
			
			Activity previousActivity = stack.peek();
			
			stopActivity(previousActivity);
		}
		
		stack.push(activity);

		activity.onCreate();
		startActivity(activity);

		if (activity.getView() == null) {

			stack.remove(activity);
			stopActivity(activity);
			try {
				throw new Exception("A view must be created");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (activity.getRenderContext() == null) {

			activity.setRenderContext(new RenderContext());
		}
	}
	
	public static void stopCurrentActivity() {

		killCurrentActivity = true;
	}

	private static void killCurrentActivity() {

		killCurrentActivity = false;
		Activity currentActivity = stack.pop();
		stopActivity(currentActivity);

		if (!stack.isEmpty()) {

			startActivity(stack.peek());
		}
	}
	
	public static Activity currentActivity() {

		Activity currentActivity;

		if (stack.isEmpty()) {

			currentActivity = null;
		} else {

			currentActivity = stack.peek();
		}

		return currentActivity;
	}
	
	private static void startActivity(Activity activity) {

		activity.onStart();
		activity.onResume();
	}
	
	private static void stopActivity(Activity activity) {
		
		activity.onPause();
		activity.onStop();
	}

	public void setSize(Dimension size) {

		this.view.setBounds(0, 0, size.width, size.height);
	}
}
