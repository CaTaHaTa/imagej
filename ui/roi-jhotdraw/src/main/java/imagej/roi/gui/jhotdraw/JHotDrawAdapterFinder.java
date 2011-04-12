/* JHotDrawAdapterFinder.java
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
package imagej.roi.gui.jhotdraw;

import imagej.util.Log;
import imagej.roi.ImageJROI;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import net.java.sezpoz.Index;
import net.java.sezpoz.IndexItem;

/**
 * @author leek
 *The JHotDrawAdapterFinder finds all JHotDrawAdapters that can adapt
 *to a particular ROI.
 */
public class JHotDrawAdapterFinder {
	static private JHotDrawAdapterFinder theFinder;
	private JHotDrawAdapterFinder() {
		
	}
	private ArrayList<IJHotDrawROIAdapter> adapters;
	
	private void initialize() {
		Index<JHotDrawROIAdapter, IJHotDrawROIAdapter> index = Index.load(
				JHotDrawROIAdapter.class, IJHotDrawROIAdapter.class);
		adapters = new ArrayList<IJHotDrawROIAdapter>();
		for (IndexItem<JHotDrawROIAdapter, IJHotDrawROIAdapter> indexItem : index) {
			try {
				final IJHotDrawROIAdapter adapter = indexItem.instance();
				adapters.add(adapter);
				Log.debug("Found adapter: " + adapter);
			} catch (InstantiationException e) {
				Log.warn("Failed to load "+indexItem.className(), e);
			}
		}
	}
	
	/**
	 * Return all adapters capable of handling a given ROI
	 * @param roi - the ROI to be adapted to the JHotDraw GUI
	 * @return collection of valid adapters
	 */
	static public Collection<IJHotDrawROIAdapter> getAdaptersForROI(ImageJROI roi) {
		if (theFinder == null) {
			theFinder = new JHotDrawAdapterFinder();
			theFinder.initialize();
		}
		ArrayList<IJHotDrawROIAdapter> result = new ArrayList<IJHotDrawROIAdapter>();
		for (IJHotDrawROIAdapter adapter : theFinder.adapters) {
			if (adapter.supports(roi))
				result.add(adapter);
		}
		return result;
	}
	/**
	 * Get all of the discovered adapters.
	 * @return
	 */
	static public Collection<IJHotDrawROIAdapter> getAllAdapters() {
		if (theFinder == null) {
			theFinder = new JHotDrawAdapterFinder();
			theFinder.initialize();
		}
		return Collections.unmodifiableCollection(theFinder.adapters);
	}
}
