//
// PivotChoiceWidget.java
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

package imagej.plugin.ui.pivot;

import imagej.ext.module.ui.ChoiceWidget;
import imagej.ext.module.ui.WidgetModel;

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.wtk.ListButton;

/**
 * Pivot implementation of multiple choice selector widget.
 * 
 * @author Curtis Rueden
 */
public class PivotChoiceWidget extends PivotInputWidget
	implements ChoiceWidget
{

	private final ListButton listButton;

	public PivotChoiceWidget(final WidgetModel model, final String[] items) {
		super(model);

		listButton = new ListButton();
		listButton.setListData(new ArrayList<String>(items));
		add(listButton);

		refreshWidget();
	}

	// -- ChoiceWidget methods --

	@Override
	public String getValue() {
		return listButton.getSelectedItem().toString();
	}

	@Override
	public int getIndex() {
		return listButton.getSelectedIndex();
	}

	// -- InputWidget methods --

	@Override
	public void refreshWidget() {
		final Object value = getModel().getValue();
		if (value.equals(listButton.getSelectedItem())) return; // no change
		listButton.setSelectedItem(value);
	}

}
