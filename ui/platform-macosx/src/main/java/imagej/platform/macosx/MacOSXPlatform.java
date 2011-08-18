//
// MacOSXPlatform.java
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

package imagej.platform.macosx;

import com.apple.eawt.Application;

import imagej.platform.Platform;
import imagej.platform.PlatformHandler;
import imagej.platform.PlatformService;

/**
 * A platform implementation for handling Mac OS X platform issues:
 * <ul>
 * <li>Application events are rebroadcast as ImageJ events.</li>
 * <li>Mac OS X screen menu bar is enabled.</li>
 * <li>Special screen menu bar menu items are handled.</li>
 * </ul>
 * 
 * @author Curtis Rueden
 */
@Platform(osName = "Mac OS X")
public class MacOSXPlatform implements PlatformHandler {

	private MacOSXAppListener appListener;

	// -- MacOSXPlatform methods --

	public MacOSXAppListener getAppListener() {
		return appListener;
	}

	// -- PlatformHandler methods --

	@Override
	public void configure(final PlatformService platformService) {
		// use Mac OS X screen menu bar
		System.setProperty("apple.laf.useScreenMenuBar", "true");

		// translate Mac OS X application events into ImageJ events
		final Application app = Application.getApplication();
		appListener = new MacOSXAppListener(app);

		// duplicate menu bar across all window frames
		platformService.setMenuBarDuplicated(true);
	}

}
