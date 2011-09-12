//
// ScriptService.java
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

package imagej.ext.script;

import imagej.ImageJ;
import imagej.ext.plugin.PluginInfo;
import imagej.ext.plugin.PluginService;
import imagej.service.AbstractService;
import imagej.service.Service;
import imagej.util.FileUtils;
import imagej.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;

import net.java.sezpoz.Index;
import net.java.sezpoz.IndexItem;

/**
 * A service discovering all available script languages and convenience methods
 * to interact with them
 * 
 * @author Johannes Schindelin
 */
@Service
public class ScriptService extends AbstractService {

	private final PluginService pluginService;

	/** Index of registered script languages. */
	private final ScriptLanguageIndex scriptLanguageIndex =
		new ScriptLanguageIndex();

	public ScriptService() {
		// NB: Required by SezPoz.
		super(null);
		throw new UnsupportedOperationException();
	}

	public ScriptService(final ImageJ context, final PluginService pluginService)
	{
		super(context);
		this.pluginService = pluginService;

		reloadScriptLanguages();

		final ArrayList<PluginInfo<?>> plugins = new ArrayList<PluginInfo<?>>();
		new ScriptFinder().findPlugins(plugins);
		pluginService.addPlugins(plugins);

	}

	// -- ScriptService methods --

	public PluginService getPluginService() {
		return pluginService;
	}

	/** Gets the index of available script languages. */
	public ScriptLanguageIndex getIndex() {
		return scriptLanguageIndex;
	}

	public List<ScriptEngineFactory> getLanguages() {
		return new ArrayList<ScriptEngineFactory>(scriptLanguageIndex);
	}

	public ScriptEngineFactory getByFileExtension(final String fileExtension) {
		return scriptLanguageIndex.getByFileExtension(fileExtension);
	}

	public ScriptEngineFactory getByName(final String name) {
		return scriptLanguageIndex.getByName(name);
	}

	public Object eval(final File file) throws FileNotFoundException,
		ScriptException
	{
		final String fileExtension = FileUtils.getExtension(file);
		final ScriptEngineFactory language = getByFileExtension(fileExtension);
		if (language == null) {
			throw new UnsupportedOperationException(
				"Could not determine language for file extension " + fileExtension);
		}
		return language.getScriptEngine().eval(new FileReader(file));
	}

	public boolean isFileHandled(final File file) {
		return scriptLanguageIndex.isFileHandled(file);
	}

	public void reloadScriptLanguages() {
		scriptLanguageIndex.clear();
		for (final IndexItem<ScriptLanguage, ScriptEngineFactory> item : Index
			.load(ScriptLanguage.class, ScriptEngineFactory.class))
		{
			try {
				final ScriptEngineFactory language = item.instance();
				scriptLanguageIndex.add(language, false);
			}
			catch (final InstantiationException e) {
				Log.error("Invalid script language: " + item, e);
			}
		}
	}

}
