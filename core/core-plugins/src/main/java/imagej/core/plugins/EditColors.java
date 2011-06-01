//
// EditColors.java
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

package imagej.core.plugins;

import imagej.ImageJ;
import imagej.display.ColorTables;
import imagej.display.DatasetView;
import imagej.display.Display;
import imagej.display.DisplayManager;
import imagej.display.DisplayView;
import imagej.plugin.ImageJPlugin;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;
import imagej.plugin.PreviewPlugin;

import java.util.List;

import net.imglib2.display.ColorTable8;
import net.imglib2.display.CompositeXYProjector;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.RealType;

/**
 * Plugin that allows toggling between different color modes.
 * 
 * @author Curtis Rueden
 */
@Plugin(menuPath = "Image>Edit Colors")
public class EditColors implements ImageJPlugin, PreviewPlugin {

	public static final String GRAYSCALE = "Grayscale";
	public static final String COLOR = "Color";
	public static final String COMPOSITE = "Composite";

	@Parameter(label = "Color mode", persist = false, choices = {
		EditColors.GRAYSCALE, EditColors.COLOR, EditColors.COMPOSITE })
	private String colorMode = EditColors.GRAYSCALE;

	public EditColors() {
		final DatasetView view = getActiveDisplayView();
		colorMode = getColorMode(view);
	}

	@Override
	public void run() {
		final DatasetView view = getActiveDisplayView();
		final CompositeXYProjector<? extends RealType<?>, ARGBType> proj =
			view.getProjector();
		view.resetColorTables(colorMode.equals(EditColors.GRAYSCALE));
		proj.setComposite(colorMode.equals(EditColors.COMPOSITE));
		proj.map();
		view.update();
	}

	@Override
	public void preview() {
		run();
	}

	public String getColorMode() {
		return colorMode;
	}

	public void setColorMode(final String colorMode) {
		this.colorMode = colorMode;
	}

	private DatasetView getActiveDisplayView() {
		final DisplayManager manager = ImageJ.get(DisplayManager.class);
		final Display display = manager.getActiveDisplay();
		if (display == null) {
			return null; // headless UI or no open images
		}
		final DisplayView activeView = display.getActiveView();
		return activeView instanceof DatasetView ? (DatasetView) activeView : null;
	}

	private String getColorMode(final DatasetView view) {
		final CompositeXYProjector<? extends RealType<?>, ARGBType> proj =
			view.getProjector();
		final boolean composite = proj.isComposite();
		if (composite) return EditColors.COMPOSITE;

		final List<ColorTable8> colorTables = view.getColorTables();
		for (final ColorTable8 colorTable : colorTables) {
			if (colorTable != ColorTables.GRAYS) return EditColors.COLOR;
		}
		return EditColors.GRAYSCALE;
	}

}
