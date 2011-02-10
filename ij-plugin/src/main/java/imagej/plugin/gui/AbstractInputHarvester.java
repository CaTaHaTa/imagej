package imagej.plugin.gui;

import imagej.Log;
import imagej.dataset.Dataset;
import imagej.plugin.Parameter;
import imagej.plugin.PluginHandler;
import imagej.plugin.spi.PluginPreprocessor;
import imagej.util.ClassUtils;

import java.io.File;
import java.lang.reflect.Field;

/**
 * InputHarvester is a plugin preprocessor that obtains the input parameters.
 *
 * It first assigns values from the passed-in input map. Any remaining
 * parameters are collected using an {@link InputPanel} dialog box.
 */
public abstract class AbstractInputHarvester
	implements PluginPreprocessor, InputHarvester
{

	@Override
	public void process(PluginHandler pluginHandler) {
		final Iterable<Field> inputs = pluginHandler.getInputFields();
		if (!inputs.iterator().hasNext()) return; // no inputs to harvest

		final InputPanel inputPanel = createInputPanel();
		buildPanel(inputPanel, pluginHandler);
		final boolean ok = showDialog(inputPanel, pluginHandler);
		if (!ok) return;
		harvestResults(inputPanel, pluginHandler);
	}

	@Override
	public abstract InputPanel createInputPanel();

	@Override
	public void buildPanel(InputPanel inputPanel, PluginHandler pluginHandler) {
		final Iterable<Field> inputs = pluginHandler.getInputFields();

		for (final Field field : inputs) {
			final String name = field.getName();
			final Class<?> type = field.getType();
			final Parameter param = pluginHandler.get(field);

			final String label = makeLabel(name, param.label());
			final boolean required = param.required();
			final String persist = param.persist();

			Object value = "";
			if (!persist.isEmpty()) {
				// TODO - retrieve initial value from persistent storage
			}
			else if (!required) {
				value = pluginHandler.getValue(field);
			}

			if (ClassUtils.isNumber(type)) {
				Number min = ClassUtils.toNumber(param.min(), type);
				if (min == null) min = ClassUtils.getMinimumNumber(type);
				Number max = ClassUtils.toNumber(param.max(), type);
				if (max == null) max = ClassUtils.getMaximumNumber(type);
				Number stepSize = ClassUtils.toNumber(param.stepSize(), type);
				if (stepSize == null) stepSize = ClassUtils.toNumber("1", type);
				inputPanel.addNumber(name, label, (Number) value, min, max, stepSize);
			}
			else if (ClassUtils.isText(type)) {
				final String[] choices = param.choices();
				if (choices.length > 0) {
					inputPanel.addChoice(name, label, value.toString(), choices);
				}
				else {
					final int columns = param.columns();
					inputPanel.addTextField(name, label, value.toString(), columns);
				}
			}
			else if (ClassUtils.isBoolean(type)) {
				inputPanel.addToggle(name, label, (Boolean) value);
			}
			else if (File.class.isAssignableFrom(type)) {
				inputPanel.addFile(name, label, (File) value);
			}
			else if (Dataset.class.isAssignableFrom(type)) {
				inputPanel.addDataset(name, label, (Dataset) value);
			}
			else {
				// NB: unsupported field type
				Log.warn("Unsupported field type: " + type.getName());
			}
		}
	}

	@Override
	public abstract boolean showDialog(InputPanel inputPanel,
		PluginHandler pluginHandler);

	@Override
	public void harvestResults(InputPanel inputPanel,
		PluginHandler pluginHandler)
	{
		// TODO: harvest inputPanel values and assign to plugin input parameters
		final Iterable<Field> inputs = pluginHandler.getInputFields();

		for (final Field field : inputs) {
			final String name = field.getName();
			final Class<?> type = field.getType();
			final Parameter param = pluginHandler.get(field);

			final Object value;
			if (ClassUtils.isNumber(type)) {
				value = inputPanel.getNumber(name);
			}
			else if (ClassUtils.isText(type)) {
				final String[] choices = param.choices();
				if (choices.length > 0) value = inputPanel.getChoice(name);
				else value = inputPanel.getTextField(name);
			}
			else if (ClassUtils.isBoolean(type)) {
				value = inputPanel.getToggle(name);
			}
			else if (File.class.isAssignableFrom(type)) {
				value = inputPanel.getFile(name);
			}
			else if (Dataset.class.isAssignableFrom(type)) {
				value = inputPanel.getDataset(name);
			}
			else value = null;
			if (value != null) pluginHandler.setValue(name, value);
		}
	}

	private String makeLabel(String name, String label) {
		if (label == null || label.isEmpty()) {
			return name.substring(0, 1).toUpperCase() + name.substring(1);
		}
		return label;
	}

}
