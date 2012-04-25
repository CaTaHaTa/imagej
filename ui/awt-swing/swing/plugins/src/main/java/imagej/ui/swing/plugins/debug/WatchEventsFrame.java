/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2012 Board of Regents of the University of
 * Wisconsin-Madison, Broad Institute of MIT and Harvard, and Max Planck
 * Institute of Molecular Cell Biology and Genetics.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of any organization.
 * #L%
 */

package imagej.ui.swing.plugins.debug;

import imagej.event.ImageJEvent;

import java.awt.Font;
import java.util.Enumeration;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

/**
 * Swing-specific window for event watcher plugin.
 * 
 * @author Curtis Rueden
 */
public class WatchEventsFrame extends JFrame {

	private final DefaultTreeModel treeModel;
	private final DefaultMutableTreeNode root;
	private JTree tree;

	private final JTextArea textArea;

	// TODO: add tabular functionality

	// -- Constructor --

	public WatchEventsFrame() {
		super("Event Watcher");

		root = create(ImageJEvent.class);
		treeModel = new DefaultTreeModel(root);

		final JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		setContentPane(splitPane);

		textArea = createTextArea();
		splitPane.add(new JScrollPane(textArea));
		tree = createTree();
		splitPane.add(new JScrollPane(tree));

		pack();
	}

	private JTree createTree() {
		final JTree t = new JTree(treeModel);
		t.setShowsRootHandles(true);
		return t;
	}

	// -- WatchEventsFrame methods --

	public void append(final String text) {
		textArea.append(text);
		// make sure the last line is always visible
		textArea.setCaretPosition(textArea.getDocument().getLength());
	}

	public void clear() {
		textArea.setText("");
	}

	public void registerEventClass(final Class<?> eventClass) {
		findOrCreate(eventClass);
	}

	// -- Helper methods --

	private JTextArea createTextArea() {
		final JTextArea text = new JTextArea();
		text.setEditable(false);
		text.setRows(50);
		text.setColumns(84);
		final Font font = new Font(Font.MONOSPACED, Font.PLAIN, 12);
		text.setFont(font);
		return text;
	}

	private DefaultMutableTreeNode findOrCreate(final Class<?> eventClass) {
		if (eventClass == null) return null;
		System.out.println("==> findOrCreate: " + eventClass.getName());
		if (eventClass == ImageJEvent.class) return root;
		final DefaultMutableTreeNode parentNode =
			findOrCreate(eventClass.getSuperclass());

		@SuppressWarnings("unchecked")
		final Enumeration<DefaultMutableTreeNode> en = parentNode.children();
		while (en.hasMoreElements()) {
			final DefaultMutableTreeNode child = en.nextElement();
			final EventInfo eventInfo = (EventInfo) child.getUserObject();
			if (eventInfo.eventClass == eventClass) {
				System.out.println("===> found node: " + eventClass.getName());
				return child;
			}
			System.out.println("===> no match: " + eventInfo.eventClass.getName() + " vs. " + eventClass.getName());
		}
		System.out.println("===> no existing node found; creating: " + eventClass.getName());

		final DefaultMutableTreeNode node = create(eventClass);
		parentNode.add(node);
		treeModel.reload();
		tree.scrollPathToVisible(new TreePath(node.getPath()));
		return node;
	}

	private DefaultMutableTreeNode create(final Class<?> eventClass) {
		return new DefaultMutableTreeNode(new EventInfo(eventClass));
	}

	private class EventInfo {

		protected Class<?> eventClass;
		protected boolean enabled;

		public EventInfo(final Class<?> eventClass) {
			this.eventClass = eventClass;
			enabled = true;
		}

		@Override
		public String toString() {
			return eventClass.getName();
		}
	}

}
