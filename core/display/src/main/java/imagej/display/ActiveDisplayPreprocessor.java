//
// ActiveDisplayPreprocessor.java
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

package imagej.display;

import imagej.ImageJ;
import imagej.data.Dataset;
import imagej.ext.module.Module;
import imagej.ext.module.ModuleItem;
import imagej.ext.plugin.Plugin;
import imagej.ext.plugin.process.PreprocessorPlugin;

/**
 * Assigns the active {@link Display} when there is one single unresolved
 * {@link Display} parameter. Hence, rather than a dialog prompting the user to
 * choose a {@link Display}, the active {@link Display} is used automatically.
 * <p>
 * In the case of more than one {@link Display} parameter, the active
 * {@link Display} is not used and instead the user must select. This behavior
 * is consistent with ImageJ v1.x.
 * </p>
 * <p>
 * The same process is applied for {@link DisplayView} and {@link Dataset}
 * parameters, using the active {@link Display}'s active {@link DisplayView} and
 * {@link Dataset}, respectively.
 * </p>
 * 
 * @author Curtis Rueden
 */
@Plugin(type = PreprocessorPlugin.class, priority = Plugin.HIGH_PRIORITY)
public class ActiveDisplayPreprocessor implements PreprocessorPlugin {

	// -- PluginPreprocessor methods --

	@Override
	public boolean canceled() {
		return false;
	}

	@Override
	public String getMessage() {
		return null;
	}

	// -- ModuleProcessor methods --

	@Override
	public void process(final Module module) {
		final DisplayService displayService = ImageJ.get(DisplayService.class);

		// assign active display to single Display input
		final String displayInput = getSingleInput(module, Display.class);
		final Display activeDisplay = displayService.getActiveDisplay();
		if (displayInput != null && activeDisplay != null) {
			module.setInput(displayInput, activeDisplay);
			module.setResolved(displayInput, true);
		}

		// assign active dataset view to single DatasetView input
		final String datasetViewInput = getSingleInput(module, DatasetView.class);
		final DatasetView activeDatasetView = displayService.getActiveDatasetView();
		if (datasetViewInput != null && activeDatasetView != null) {
			module.setInput(datasetViewInput, activeDatasetView);
			module.setResolved(datasetViewInput, true);
		}

		// assign active display view to single DisplayView input
		final String displayViewInput = getSingleInput(module, DisplayView.class);
		final DisplayView activeDisplayView =
			activeDisplay == null ? null : activeDisplay.getActiveView();
		if (displayViewInput != null && activeDisplayView != null) {
			module.setInput(displayViewInput, activeDisplayView);
			module.setResolved(displayViewInput, true);
		}

		// assign active dataset to single Dataset input
		final String datasetInput = getSingleInput(module, Dataset.class);
		final Dataset activeDataset = displayService.getActiveDataset();
		if (datasetInput != null && activeDataset != null) {
			module.setInput(datasetInput, activeDataset);
			module.setResolved(datasetInput, true);
		}
	}

	// -- Helper methods --

	private String getSingleInput(final Module module, final Class<?> type) {
		final Iterable<ModuleItem<?>> inputs = module.getInfo().inputs();
		String result = null;
		for (final ModuleItem<?> item : inputs) {
			final String name = item.getName();
			final boolean resolved = module.isResolved(name);
			if (resolved) continue; // skip resolved inputs
			if (!type.isAssignableFrom(item.getType())) continue;
			if (result != null) return null; // there are multiple matching inputs
			result = name;
		}
		return result;
	}

}
