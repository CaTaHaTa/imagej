//
// AbstractModuleItem.java
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

package imagej.ext.module;

import imagej.ext.module.ui.WidgetStyle;
import imagej.util.ClassUtils;
import imagej.util.Log;
import imagej.util.Prefs;
import imagej.util.StringMaker;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Abstract superclass of {@link ModuleItem} implementations.
 * 
 * @author Curtis Rueden
 */
public abstract class AbstractModuleItem<T> implements ModuleItem<T> {

	private final ModuleInfo info;
	private Method callbackMethod;
	private boolean callbackInitialized = false;

	public AbstractModuleItem(final ModuleInfo info) {
		this.info = info;
	}

	// -- Object methods --

	@Override
	public String toString() {
		final StringMaker sm = new StringMaker();
		sm.append("label", getLabel());
		sm.append("description", getDescription());
		sm.append("visibility", getVisibility(), ItemVisibility.NORMAL);
		sm.append("required", isRequired());
		sm.append("persisted", isPersisted());
		sm.append("persistKey", getPersistKey());
		sm.append("callback", getCallback());
		sm.append("widgetStyle", getWidgetStyle(), WidgetStyle.DEFAULT);
		sm.append("min", getMinimumValue());
		sm.append("max", getMaximumValue());
		sm.append("stepSize", getStepSize(), ClassUtils.toNumber("1", getType()));
		sm.append("columnCount", getColumnCount(), 6);
		sm.append("choices", getChoices());
		return getName() + ": " + sm.toString();
	}

	// -- ModuleItem methods --

	@Override
	public ItemVisibility getVisibility() {
		return ItemVisibility.NORMAL;
	}

	@Override
	public boolean isRequired() {
		return false;
	}

	@Override
	public boolean isPersisted() {
		return true;
	}

	@Override
	public String getPersistKey() {
		return null;
	}

	@Override
	public T loadValue() {
		if (!isPersisted()) return null;

		final String sValue;
		final String persistKey = getPersistKey();
		if (persistKey == null || persistKey.isEmpty()) {
			final Class<?> prefClass = getDelegateClass();
			final String prefKey = getName();
			sValue = Prefs.get(prefClass, prefKey);
		}
		else sValue = Prefs.get(persistKey);

		return ClassUtils.convert(sValue, getType());
	}

	@Override
	public void saveValue(final T value) {
		if (!isPersisted()) return;

		final String sValue = value == null ? "" : value.toString();

		final String persistKey = getPersistKey();
		if (persistKey == null || persistKey.isEmpty()) {
			final Class<?> prefClass = getDelegateClass();
			final String prefKey = getName();
			Prefs.put(prefClass, prefKey, sValue);
		}
		else Prefs.put(persistKey, sValue);
	}

	@Override
	public String getCallback() {
		return null;
	}

	@Override
	public void callback(final Module module) {
		if (!callbackInitialized) callbackMethod = findCallbackMethod();
		if (callbackMethod == null) return;
		final String callback = getCallback();
		final Object obj = module.getDelegateObject();
		final String objClass = obj.getClass().getName();
		try {
			Log.debug(objClass + ": executing callback function: " + callback);
			callbackMethod.invoke(obj);
		}
		catch (final Exception e) {
			// NB: Several types of exceptions; simpler to handle them all the same.
			Log.warn(objClass + ": error executing callback method \"" + callback +
				"\" for module item " + getName(), e);
		}
	}

	@Override
	public WidgetStyle getWidgetStyle() {
		return WidgetStyle.DEFAULT;
	}

	@Override
	public T getMinimumValue() {
		return null;
	}

	@Override
	public T getMaximumValue() {
		return null;
	}

	@Override
	public Number getStepSize() {
		if (!ClassUtils.isNumber(getType())) return null;
		return ClassUtils.toNumber("1", getType());
	}

	@Override
	public int getColumnCount() {
		return 6;
	}

	@Override
	public List<T> getChoices() {
		return null;
	}

	@Override
	public T getValue(final Module module) {
		final Object result = module.getInput(getName());
		@SuppressWarnings("unchecked")
		final T value = (T) result;
		return value;
	}

	// -- BasicDetails methods --

	@Override
	public String getName() {
		return null;
	}

	@Override
	public String getLabel() {
		return null;
	}

	@Override
	public String getDescription() {
		return null;
	}

	@Override
	public void setName(final String name) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setLabel(final String description) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setDescription(final String description) {
		throw new UnsupportedOperationException();
	}

	// -- Helper methods --

	private Class<?> getDelegateClass() {
		return ClassUtils.loadClass(info.getDelegateClassName());
	}

	private Method findCallbackMethod() {
		callbackInitialized = true;
		final String callback = getCallback();
		if (callback == null || callback.isEmpty()) return null;

		final Class<?> c = getDelegateClass();
		try {
			// TODO - support inherited callback methods
			final Method m = c.getDeclaredMethod(callback);
			m.setAccessible(true);
			return m;
		}
		catch (final Exception e) {
			// NB: Multiple types of exceptions; simpler to handle them all the same.
			Log.warn("Cannot find callback method \"" + info.getDelegateClassName() +
				"#" + callback + "\" for module item " + getName(), e);
		}
		return null;
	}

}
