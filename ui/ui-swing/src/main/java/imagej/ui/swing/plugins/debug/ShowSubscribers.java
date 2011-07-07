package imagej.ui.swing.plugins.debug;

import imagej.display.event.DisplayActivatedEvent;
import imagej.event.EventSubscriber;
import imagej.event.Events;
import imagej.object.event.ObjectCreatedEvent;
import imagej.object.event.ObjectDeletedEvent;
import imagej.object.event.ObjectsUpdatedEvent;
import imagej.plugin.ImageJPlugin;
import imagej.plugin.Plugin;
import imagej.ui.swing.StaticSwingUtils;
import imagej.ui.swing.SwingOutputWindow;
import java.util.List;

/**
 * For EventBus diagnostics...
 * Shows what is subscribed to a event types...  
 * 
 * Change/Add event types as necessary...
 * 
 * @author GBH
 */
@Plugin(menuPath = "Plugins>Debug>Subscribers")
public class ShowSubscribers implements ImageJPlugin {

	private static SwingOutputWindow window;
	private List<EventSubscriber<?>> subscribers;

	@Override
	public void run() {
		window = new SwingOutputWindow("Subscribers");
		StaticSwingUtils.locateLowerRight(window);
		listSubs(ObjectsUpdatedEvent.class);
		listSubs(ObjectCreatedEvent.class);
		listSubs(ObjectDeletedEvent.class);
		listSubs(DisplayActivatedEvent.class);
		
	}

	private void listSubs(Class clazz) {
		subscribers = Events.getSubscribers(clazz);
		window.append(clazz.getSimpleName() + "...\n");
		for (EventSubscriber<?> subscriber : subscribers) {
			window.append(subscriber.toString() + "\n");
		}
	}
}
