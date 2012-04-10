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

package imagej.updater.ui;

import imagej.ext.plugin.ImageJPlugin;
import imagej.ext.plugin.Parameter;
import imagej.ext.plugin.Plugin;
import imagej.ext.plugin.PluginModuleInfo;
import imagej.ext.plugin.PluginService;
import imagej.updater.core.UpToDate;
import imagej.updater.core.UpdaterUIPlugin;
import imagej.util.Log;

import java.util.List;

/**
 * TODO
 * 
 * @author Johannes Schindelin
 */
@Plugin(label = "There are updates available")
public class UpdatesAvailable implements ImageJPlugin {

	private final static String YES = "Yes, please", NEVER = "Never",
			LATER = "Remind me later";

	@Parameter
	public PluginService pluginService;

	@Parameter(label = "Do you want to start the Updater now?", choices = { YES,
		NEVER, LATER })
	public String updateAction = YES;

	@Override
	public void run() {
		if (updateAction.equals(YES)) {
			final List<PluginModuleInfo<UpdaterUIPlugin>> updaters =
				pluginService.getRunnablePluginsOfType(UpdaterUIPlugin.class);
			if (updaters.size() > 0) {
				pluginService.run(updaters.get(0));
			}
			else {
				Log.error("No updater plugins found!");
			}
		}
		else if (updateAction.equals(NEVER)) UpToDate.setLatestNag(Long.MAX_VALUE);
		else if (updateAction.equals(LATER)) UpToDate.setLatestNag();
		else throw new RuntimeException("Unknown update action: " + updateAction);
	}

}
