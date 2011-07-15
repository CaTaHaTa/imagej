//
// DefaultPluginModuleFactory.java
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

package imagej.plugin;

import imagej.module.Module;
import imagej.module.ModuleException;

/**
 * The default implementation of {@link PluginModuleFactory}, using a
 * {@link PluginModule}.
 * 
 * @author Curtis Rueden
 */
public class DefaultPluginModuleFactory implements PluginModuleFactory {

	@Override
	public <R extends RunnablePlugin> Module createModule(
		final PluginModuleInfo<R> info) throws ModuleException
	{
		// if the plugin implements Module, return a new instance directly
		try {
			final Class<R> pluginClass = info.loadClass();
			if (Module.class.isAssignableFrom(pluginClass)) {
				return (Module) pluginClass.newInstance();
			}
		}
		catch (InstantiableException e) {
			throw new ModuleException(e);
		}
		catch (InstantiationException e) {
			throw new ModuleException(e);
		}
		catch (IllegalAccessException e) {
			throw new ModuleException(e);
		}

		// plugin does not implement Module; wrap it in a PluginModule instance
		return new PluginModule<R>(info);
	}

}
