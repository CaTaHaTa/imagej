//
// NavigableImageFrame.java
//

/*
ImageJ software for multidimensional image processing and analysis.

Copyright (c) 2010, ImageJDev.org.
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright
notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright
notice, this list of conditions and the following disclaimer in the
documentation and/or other materials provided with the distribution.
 * Neither the names of the ImageJDev.org developers nor the
names of its contributors may be used to endorse or promote products
derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
 */

package imagej.gui.swing.display;

import imagej.display.DisplayController;
import imagej.display.EventDispatcher;
import imagej.display.ImageCanvas;
import imagej.display.ImageDisplayWindow;
import imagej.model.AxisLabel;

import java.awt.Adjustable;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import net.miginfocom.swing.MigLayout;

/**
 * TODO
 * 
 * @author Curtis Rueden
 * @author Grant Harris
 */
public class NavigableImageFrame extends JFrame implements ImageDisplayWindow {

	// TODO - Rework this class to be a JPanel, not a JFrame.

	private final JLabel imageLabel;
	private final NavigableImagePanel imgCanvas;
	private JPanel sliders;

	protected DisplayController controller;

	public NavigableImageFrame(final NavigableImagePanel imgCanvas) {
		this.imgCanvas = imgCanvas;
		imgCanvas.setBorder(new LineBorder(Color.black));

		imageLabel = new JLabel(" ");
		final int prefHeight = imageLabel.getPreferredSize().height;
		imageLabel.setPreferredSize(new Dimension(0, prefHeight));

		sliders = new JPanel();

		final JPanel pane = new JPanel();
		pane.setLayout(new BorderLayout());
		pane.setBorder(new EmptyBorder(3, 3, 3, 3));
		setContentPane(pane);

		pane.add(imageLabel, BorderLayout.NORTH);
		pane.add(imgCanvas, BorderLayout.CENTER);
		pane.add(sliders, BorderLayout.SOUTH);

		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	}

	@Override
	public void setDisplayController(final DisplayController controller) {
		this.controller = controller;
		final int[] dims = controller.getDims();
		final AxisLabel[] dimLabels = controller.getDimLabels();
		if (dims.length > 2)
			createSliders(dims, dimLabels);
	}

	@Override
	public void setLabel(final String s) {
		imageLabel.setText(s);
	}

	public ImageCanvas getPanel() {
		return imgCanvas;
	}

	private void createSliders(final int[] dims, final AxisLabel[] dimLabels) {
		sliders.setLayout(new MigLayout("fillx, wrap 2", "[right|fill,grow]"));
		sliders.removeAll();
		for (int i = 0, p = -1; i < dims.length; i++) {
			if (AxisLabel.isXY(dimLabels[i])) continue;
			p++;
			if (dims[i] == 1) continue;

			final JLabel label = new JLabel(dimLabels[i].toString());
			label.setHorizontalAlignment(SwingConstants.RIGHT);
			final JScrollBar slider =
				new JScrollBar(Adjustable.HORIZONTAL, 1, 1, 1, dims[i] + 1);
			final int posIndex = p;
			slider.addAdjustmentListener(new AdjustmentListener() {
				@Override
				public void adjustmentValueChanged(final AdjustmentEvent e) {
					controller.updatePosition(posIndex, slider.getValue() - 1);
				}
			});
			sliders.add(label);
			sliders.add(slider);
		}
	}

	@Override
	public void setImageCanvas(final ImageCanvas canvas) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public ImageCanvas getImageCanvas() {
		return imgCanvas;
	}

	@Override
	public void updateImage() {
		imgCanvas.updateImage();
	}

	@Override
	public void addEventDispatcher(final EventDispatcher dispatcher) {
		addWindowListener((AWTEventDispatcher) dispatcher);

	}

}
