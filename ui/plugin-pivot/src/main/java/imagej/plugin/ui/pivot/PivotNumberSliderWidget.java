//
// PivotNumberSliderWidget.java
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

import imagej.plugin.ui.ParamDetails;
import imagej.util.ClassUtils;

import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.Slider;
import org.apache.pivot.wtk.SliderValueListener;

/**
 * Pivot implementation of number chooser widget, using a slider.
 * 
 * @author Curtis Rueden
 */
public class PivotNumberSliderWidget extends PivotNumberWidget
	implements SliderValueListener
{

	private final Slider slider;
	private final Label label;

	public PivotNumberSliderWidget(final ParamDetails details,
		final Number min, final Number max)
	{
		super(details);

		slider = new Slider();
		slider.setRange(min.intValue(), max.intValue());
		add(slider);
		slider.getSliderValueListeners().add(this);

		label = new Label();
		add(label);

		refresh();
	}

	// -- NumberWidget methods --

	@Override
	public Number getValue() {
		final String value = "" + slider.getValue();
		return ClassUtils.toNumber(value, details.getType());
	}

	// -- InputWidget methods --

	@Override
	public void refresh() {
		final Number value = (Number) details.getValue();
		slider.setValue(value.intValue());
		label.setText(value.toString());
	}

	// -- SliderValueListener methods --

	@Override
	public void valueChanged(final Slider s, final int previousValue) {
		label.setText("" + s.getValue());
	}

}
