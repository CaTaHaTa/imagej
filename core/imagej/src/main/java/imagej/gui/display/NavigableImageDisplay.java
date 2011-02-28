package imagej.gui.display;

import imagej.model.Dataset;
import imagej.plugin.Plugin;
import imagej.plugin.display.Display;
import imagej.plugin.display.DisplayView;
import imagej.plugin.display.LayeredDisplay;

/**
 * TODO
 *
 * @author Curtis Rueden
 * @author Grant Harris
 */
@Plugin(type = Display.class)
public class NavigableImageDisplay extends AbstractSwingDisplay
	implements LayeredDisplay
{

	private NavigableImageFrame imageFrame;

	@Override
	public boolean canDisplay(Dataset dataset) {
		return true;
	}

	@Override
	public void display(Dataset dataset) {
		imageFrame = new NavigableImageFrame();

		// listen for user input
		imageFrame.addKeyListener(this);
		imageFrame.addMouseListener(this);
		imageFrame.addMouseMotionListener(this);
		imageFrame.addMouseWheelListener(this);

		// TODO - use DisplayView instead of Dataset directly
		imageFrame.setDataset(dataset);
		imageFrame.setVisible(true);
	}

	@Override
	public void pan(float x, float y) {
		imageFrame.getPanel().pan((int) x, (int) y);
	}

	@Override
	public void zoom(float factor) {
		// TODO
	}

	@Override
	public void addView(DisplayView view) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void removeView(DisplayView view) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void removeAllViews() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public DisplayView[] getViews() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public DisplayView getView(int n) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public DisplayView getActiveView() {
		return getView(0);
	}

}
