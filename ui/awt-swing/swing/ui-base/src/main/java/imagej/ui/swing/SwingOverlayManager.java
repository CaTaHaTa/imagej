//
// SwingOverlayManager.java
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

package imagej.ui.swing;

import imagej.ImageJ;
import imagej.data.display.DisplayService;
import imagej.data.display.DisplayView;
import imagej.data.display.ImageDisplay;
import imagej.data.display.OverlayService;
import imagej.data.display.event.DisplayViewSelectionEvent;
import imagej.data.event.OverlayCreatedEvent;
import imagej.data.event.OverlayDeletedEvent;
import imagej.data.roi.AbstractOverlay;
import imagej.data.roi.Overlay;
import imagej.event.EventSubscriber;
import imagej.event.Events;
import imagej.ext.display.event.DisplayActivatedEvent;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * Overlay Manager Swing UI
 * 
 * @author Adam Fraser
 */
public class SwingOverlayManager extends JFrame implements ActionListener {

	private static final long serialVersionUID = -6498169032123522303L;
	private JList olist = null;
	private boolean selecting = false; //flag to prevent event feedback loops 
	/** Maintains the list of event subscribers, to avoid garbage collection. */
	private List<EventSubscriber<?>> subscribers;

	private void subscribeToEvents() {

		subscribers = new ArrayList<EventSubscriber<?>>();

		final EventSubscriber<OverlayCreatedEvent> creationSubscriber =
				new EventSubscriber<OverlayCreatedEvent>() {

					@Override
					public void onEvent(OverlayCreatedEvent event) {
						System.out.println("\tCREATED: " + event.toString());
						olist.updateUI();
					}

				};
		subscribers.add(creationSubscriber);
		Events.subscribe(OverlayCreatedEvent.class, creationSubscriber);
		//
		final EventSubscriber<OverlayDeletedEvent> deletionSubscriber =
				new EventSubscriber<OverlayDeletedEvent>() {

					@Override
					public void onEvent(OverlayDeletedEvent event) {
						System.out.println("\tDELETED: " + event.toString());
						olist.updateUI();
					}

				};
		subscribers.add(deletionSubscriber);
		Events.subscribe(OverlayDeletedEvent.class, deletionSubscriber);
		//
		// No need to update unless thumbnail will be redrawn.
//		final EventSubscriber<OverlayRestructuredEvent> restructureSubscriber =
//				new EventSubscriber<OverlayRestructuredEvent>() {
//
//					@Override
//					public void onEvent(OverlayRestructuredEvent event) {
//						System.out.println("\tRESTRUCTURED: " + event.toString());
//						olist.updateUI();
//					}
//
//				};
//		subscribers.add(restructureSubscriber);
//		Events.subscribe(OverlayRestructuredEvent.class, restructureSubscriber);
		//
		/*
		 * Update when a display is activated 
		 */
		final EventSubscriber<DisplayActivatedEvent> displayActivatedSubscriber =
				new EventSubscriber<DisplayActivatedEvent>() {

					@Override
					public void onEvent(final DisplayActivatedEvent event) {
						olist.updateUI();
					}

				};
		subscribers.add(displayActivatedSubscriber);
		Events.subscribe(DisplayActivatedEvent.class, displayActivatedSubscriber);
		//
		/*
		 * Update the selection when an overlay displayView selection changes 
		 */
		final EventSubscriber<DisplayViewSelectionEvent> displayViewSelectedSubscriber =
				new EventSubscriber<DisplayViewSelectionEvent>() {

					@Override
					public void onEvent(final DisplayViewSelectionEvent event) {
						if (selecting == true) {
							return;
						}
						selecting = true;
						// Select or deselect the corresponding overlay in the list
						Object overlay = event.getDisplayView().getDataObject();
						if (event.isSelected()) {
							int[] current_sel = olist.getSelectedIndices();
							olist.setSelectedValue(overlay, true);
							int[] new_sel = olist.getSelectedIndices();
							int[] sel = Arrays.copyOf(current_sel, current_sel.length + new_sel.length);
							System.arraycopy(new_sel, 0, sel, current_sel.length, new_sel.length);
							olist.setSelectedIndices(sel);
						} else {
							for (int i : olist.getSelectedIndices()) {
								if (olist.getModel().getElementAt(i) == overlay) {
									olist.removeSelectionInterval(i, i);
								}
							}
						}
						selecting = false;
					}
				};
		subscribers.add(displayViewSelectedSubscriber);
		Events.subscribe(DisplayViewSelectionEvent.class, displayViewSelectedSubscriber);
		//
	}
	/*
	 * Constructor. Create a JList to list the overlays. 
	 */

	public SwingOverlayManager() {
		olist = new JList(new OverlayListModel());
		olist.setCellRenderer(new OverlayRenderer());

		// Populate the list with the current overlays
		OverlayService om = ImageJ.get(OverlayService.class);
		for (Overlay overlay : om.getOverlays()) {
			olist.add(olist.getCellRenderer().getListCellRendererComponent(olist, overlay, -1, false, false));
		}

		JScrollPane listScroller = new JScrollPane(olist);
		listScroller.setPreferredSize(new Dimension(250, 80));
		listScroller.setAlignmentX(LEFT_ALIGNMENT);
		JPanel listPane = new JPanel();
		listPane.setLayout(new BoxLayout(listPane, BoxLayout.PAGE_AXIS));
		listPane.add(listScroller);
		listPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		JButton delbutton = new JButton("delete selected");
		delbutton.setMnemonic(KeyEvent.VK_DELETE);
		delbutton.setActionCommand("delete");
		delbutton.addActionListener(this);
		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
		buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
		buttonPane.add(Box.createHorizontalGlue());
		buttonPane.add(delbutton);

		setSize(300, 300);

		Container cp = this.getContentPane();
		cp.add(listPane, BorderLayout.CENTER);
		cp.add(buttonPane, BorderLayout.PAGE_END);

		//
		// Listen to list selections
		//
		ListSelectionListener listSelectionListener = new ListSelectionListener() {

			public void valueChanged(ListSelectionEvent listSelectionEvent) {
				if (selecting == true) {
					return;
				}
				selecting = true;
				final DisplayService displayService = ImageJ.get(DisplayService.class);
				final ImageDisplay display = (ImageDisplay) displayService.getActiveDisplay();
				JList list = (JList) listSelectionEvent.getSource();
				Object selectionValues[] = list.getSelectedValues();
				for (final DisplayView overlayView : display.getViews()) {
					overlayView.setSelected(false);
					for (Object overlay : selectionValues) {
						if (overlay == overlayView.getDataObject()) {
							overlayView.setSelected(true);
							break;
						}
					}
				}
				selecting = false;
			}

		};
		olist.addListSelectionListener(listSelectionListener);
		subscribeToEvents();
//		Events.subscribe(OverlayCreatedEvent.class, creationSubscriber);
//		Events.subscribe(OverlayDeletedEvent.class, deletionSubscriber);
//		// No need to update unless thumbnail will be redrawn.
////		Events.subscribe(OverlayRestructuredEvent.class, restructureSubscriber);
//		Events.subscribe(DisplayActivatedEvent.class, displayActivatedSubscriber);
//		Events.subscribe(DisplayViewSelectionEvent.class, displayViewSelectedSubscriber);

	}

	public void actionPerformed(ActionEvent e) {
		if ("delete".equals(e.getActionCommand())) {
			AbstractOverlay overlay = (AbstractOverlay) olist.getSelectedValue();
			overlay.delete();
			System.out.println("\tDelete overlay " + overlay.getRegionOfInterest().toString());
		}
	}

	/**
	 * JList synchronized with the overlays in the OverlayService.
	 */
	public class OverlayListModel extends AbstractListModel {

		private static final long serialVersionUID = 7941252533859436640L;
		private OverlayService om = ImageJ.get(OverlayService.class);
		private DisplayService dm = ImageJ.get(DisplayService.class);

		public Object getElementAt(int index) {
			ImageDisplay display = (ImageDisplay) dm.getActiveDisplay();
			return om.getOverlays(display).get(index);
		}

		public int getSize() {
			ImageDisplay display = (ImageDisplay) dm.getActiveDisplay();
			return om.getOverlays(display).size();
		}

	}

	/**
	 *
	 */
	public class OverlayRenderer extends DefaultListCellRenderer {

		private static final long serialVersionUID = 2468086636364454253L;
		private Hashtable iconTable = new Hashtable();

		public Component getListCellRendererComponent(JList list,
				Object value, int index, boolean isSelected, boolean hasFocus) {
			JLabel label = (JLabel) super.getListCellRendererComponent(
					list, value, index, isSelected, hasFocus);
			if (value instanceof Overlay) {
				Overlay overlay = (Overlay) value;
				//TODO: create overlay thumbnail from overlay 
				ImageIcon icon = (ImageIcon) iconTable.get(value);
//				if (icon == null) {
//					icon = new ImageIcon(...);
//					iconTable.put(value, icon);
//				}
				label.setIcon(icon);
			} else {
				// Clear old icon; needed in 1st release of JDK 1.2
				label.setIcon(null);
			}
			return label;
		}

	}
}
