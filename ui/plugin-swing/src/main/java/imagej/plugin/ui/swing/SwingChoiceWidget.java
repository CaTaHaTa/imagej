//
// SwingChoiceWidget.java
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

package imagej.plugin.ui.swing;

import imagej.plugin.ui.ChoiceWidget;
import imagej.plugin.ui.ParamModel;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;

/**
 * Swing implementation of multiple choice selector widget.
 * 
 * @author Curtis Rueden
 */
public class SwingChoiceWidget extends SwingInputWidget
	implements ActionListener, ChoiceWidget
{

	private final JComboBox comboBox;

	public SwingChoiceWidget(final ParamModel model, final String[] items) {
		super(model);

		comboBox = new JComboBox(items);
		add(comboBox, BorderLayout.CENTER);
		comboBox.addActionListener(this);

		refresh();
	}

	// -- ActionListener methods --

	@Override
	public void actionPerformed(final ActionEvent e) {
		model.setValue(comboBox.getSelectedItem());
	}

	// -- ChoiceWidget methods --

	@Override
	public String getItem() {
		return comboBox.getSelectedItem().toString();
	}

	@Override
	public int getIndex() {
		return comboBox.getSelectedIndex();
	}

	// -- InputWidget methods --

	@Override
	public void refresh() {
		comboBox.setSelectedItem(getValidValue());
	}

	// -- Helper methods --

	private Object getValidValue() {
		final int itemCount = comboBox.getItemCount();
		if (itemCount == 0) return null; // no valid values exist

		final Object value = model.getValue();
		for (int i = 0; i < itemCount; i++) {
			final Object item = comboBox.getItemAt(i);
			if (value == item) return value;
		}

		// value was invalid; reset to first choice on the list
		final Object validValue = comboBox.getItemAt(0);
		model.setValue(validValue);
		return validValue;
	}

}
