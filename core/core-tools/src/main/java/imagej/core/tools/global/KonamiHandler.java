//
// KonamiHandler.java
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

package imagej.core.tools.global;

import imagej.ext.display.event.key.KyPressedEvent;
import imagej.ext.plugin.PluginService;
import imagej.tool.AbstractTool;
import imagej.tool.Tool;

import java.awt.event.KeyEvent;

/**
 * Mysterious!
 * 
 * @author Curtis Rueden
 */
@Tool(name = "Konami", global = true)
public class KonamiHandler extends AbstractTool {

	private static final int[] CODE = { KeyEvent.VK_UP, KeyEvent.VK_UP,
		KeyEvent.VK_DOWN, KeyEvent.VK_DOWN, KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT,
		KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT, KeyEvent.VK_B, KeyEvent.VK_A };

	private static final String PLUGIN = "imagej.core.plugins.app.EasterEgg";

	private int index = 0;

	@Override
	public void onKeyDown(final KyPressedEvent evt) {
		if (evt.getCode() == CODE[index]) {
			index++;
			if (index == CODE.length) {
				index = 0;
				final PluginService pluginService =
					evt.getContext().getService(PluginService.class);
				pluginService.run(PLUGIN);
			}
		}
		else index = 0;
	}

}
