package gui.event;

import activity.Activity;
import gui.ActionListener;
import gui.GUIElement;
import input.*;
import input.Button;
import input.InputContext;

import java.awt.*;
import java.util.List;
import java.util.LinkedList;

/**
 * Created by Nyrmburk on 4/30/2016.
 * <p>
 * For listeners: First, addActionListener is caled on the GUIElement in question. The ActionListener itself manages
 * the events, not the component. After it detects an event, it calls the actionPerformed method with the action in
 * question. The new ActionEvent tells the class it is in (to make sure it is the right type of event) and has an object
 * specific to the event. In the case of a mouse listener, the object would return info on the button pressed, location
 * of mouse, etc. This approach is extensible and easy to implement. It also works on all components.
 * <p>
 * Steps to implement
 * remove implementation from Activity
 * refactor PointerListener into a separate object that handles the listening itself.
 * refactor Component so that it no longer accepts ActionListener
 * refactor Component so that it no longer delegates ActionEvents
 */
public abstract class PointerListener extends ActionListener<PointerListener.PointerEvent> {

	private static final String ACTIVITY_INPUT = "pointer_input";

	private static GUIElement previousElement;
	private static GUIElement currentElement;

	private static GUIElement dragElement;

	private static Point previousPointerLocation;
	private static Point pointerLocation;

	public enum State {
		ENTER,
		EXIT,
		PRESS,
		HOLD,
		RELEASE,
		CLICK,
		MOVE,
		DRAG,
	}

	private static MultiInput pointerInput = new MultiInput(ACTIVITY_INPUT) {

		{
			this.setInputs(new SimpleInput("x_axis"), new SimpleInput("y_axis"));
		}

		@Override
		public void onUpdate(Input[] inputs, float delta) {

			int x = (int) inputs[0].getValue();
			int y = (int) inputs[1].getValue();

			previousPointerLocation = pointerLocation;
			pointerLocation = new Point(x, y);

			Activity currentActivity = Activity.currentActivity();
			currentElement = currentActivity.getView().getPointOver(pointerLocation);

			if (previousElement != null && previousElement != currentElement) {
				for (PointerListener listener : previousElement.getPointerListeners()) {
					listener.actionPerformed(new PointerEvent(previousElement, pointerLocation, State.EXIT));
				}
				previousElement = null;
				currentElement = null;
			}

			if (currentElement != null) {

				if (previousElement != currentElement) {

					for (PointerListener listener : currentElement.getPointerListeners()) {
						listener.actionPerformed(newEvent(State.ENTER));
					}
					previousElement = currentElement;
				}

				if (!previousPointerLocation.equals(pointerLocation)) {

					for (PointerListener listener : currentElement.getPointerListeners()) {
						listener.actionPerformed(newEvent(State.MOVE));
					}

					if (dragElement != null) {

						for (PointerListener listener : dragElement.getPointerListeners()) {
							listener.actionPerformed(newEvent(State.DRAG));
						}
					}
				}
			}
		}
	};

	private static Button pointerButton = new Button("primary") {

		private GUIElement pressElement;

		{
//			Engine.pointer.addInput(this, );
			Binding.delegate(this);
		}

		@Override
		public void onPress() {
			if (currentElement != null) {

				dragElement = currentElement;

				for (PointerListener listener : currentElement.getPointerListeners()) {
					listener.actionPerformed(newEvent(State.PRESS));
				}
			}
			pressElement = currentElement;
		}

		@Override
		public void onRelease() {

			if (currentElement != null) {

				for (PointerListener listener : currentElement.getPointerListeners()) {
					listener.actionPerformed(newEvent(State.RELEASE));
				}

				if (pressElement == currentElement) {
					for (PointerListener listener : currentElement.getPointerListeners()) {
						listener.actionPerformed(newEvent(State.CLICK));
					}
				}
			}

			dragElement = null;
		}

		@Override
		public void onHold(float delta) {
			if (currentElement != null) {
				for (PointerListener listener : currentElement.getPointerListeners()) {
					listener.actionPerformed(newEvent(State.HOLD));
				}
			}
		}
	};

	static {
		InputContext inputContext = new InputContext();
		inputContext.setAsCurrentContext();
		inputContext.inputs.add(pointerInput);
		inputContext.inputs.add(pointerButton);
		System.out.println("Initialized");
	}

	@Override
	public void update(int delta) {
	}

	public Point getPointerLocation() {

		return pointerLocation;
	}

	private static PointerEvent newEvent(State state) {

		return new PointerEvent(currentElement, pointerLocation, state);
	}

	public static class PointerEvent extends Event {

		public Point pointer;
		public State state;

		public PointerEvent(GUIElement source, Point pointer, State state) {
			super(source);
			this.pointer = pointer;
			this.state = state;
		}
	}
}
