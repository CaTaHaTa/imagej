//
// DebugPostprocessor.java
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

package imagej.ext.plugin.debug;

import imagej.ext.Priority;
import imagej.ext.module.Module;
import imagej.ext.plugin.Plugin;
import imagej.ext.plugin.process.PostprocessorPlugin;
import imagej.util.Log;

import java.util.Map;

/**
 * A postprocessor plugin that dumps parameter values to the log.
 * 
 * @author Curtis Rueden
 */
@Plugin(type = PostprocessorPlugin.class, priority = Priority.FIRST_PRIORITY)
public class DebugPostprocessor implements PostprocessorPlugin {

	@Override
	public void process(final Module module) {
		// dump input values to log
		Log.debug("INPUTS:");
		final Map<String, Object> inputs = module.getInputs();
		for (final String key : inputs.keySet()) {
			Log.debug("\t" + key + " = " + inputs.get(key));
		}

		// dump output values to log
		Log.debug("OUTPUTS:");
		final Map<String, Object> outputs = module.getOutputs();
		for (final String key : outputs.keySet()) {
			Log.debug("\t" + key + " = " + outputs.get(key));
		}
	}

}
