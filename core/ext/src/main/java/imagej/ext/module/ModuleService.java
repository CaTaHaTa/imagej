//
// ModuleService.java
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

import imagej.IService;
import imagej.Service;
import imagej.event.EventSubscriber;
import imagej.event.Events;
import imagej.ext.module.event.ModulesCreatedEvent;
import imagej.ext.module.event.ModulesDeletedEvent;
import imagej.ext.module.process.ModulePostprocessor;
import imagej.ext.module.process.ModulePreprocessor;
import imagej.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Service for keeping track of and executing available modules.
 * 
 * @author Curtis Rueden
 * @see Module
 * @see ModuleInfo
 */
@Service(priority = Service.HIGH_PRIORITY)
public class ModuleService implements IService {

	/** Index of registered modules. */
	private final ModuleIndex moduleIndex = new ModuleIndex();

	/** Maintains the list of event subscribers, to avoid garbage collection. */
	private List<EventSubscriber<?>> subscribers;

	// -- ModuleService methods --

	/** Gets the index of available modules. */
	public ModuleIndex getIndex() {
		return moduleIndex;
	}

	/** Manually registers a module with the module service. */
	public void addModule(final ModuleInfo module) {
		moduleIndex.add(module);
	}

	/** Manually unregisters a module with the module service. */
	public void removeModule(final ModuleInfo module) {
		moduleIndex.remove(module);
	}

	/** Manually registers a list of modules with the module service. */
	public void addModules(final Collection<? extends ModuleInfo> modules) {
		moduleIndex.addAll(modules);
	}

	/** Manually unregisters a list of modules with the module service. */
	public void removeModules(final Collection<? extends ModuleInfo> modules) {
		moduleIndex.removeAll(modules);
	}

	/** Gets the list of available modules. */
	public List<ModuleInfo> getModules() {
		return moduleIndex.getAll();
	}

	/**
	 * Executes the given module, without any pre- or postprocessing.
	 * 
	 * @param info The module to instantiate and run.
	 * @param separateThread Whether to execute the module in a new thread.
	 */
	public void run(final ModuleInfo info, final boolean separateThread) {
		run(info, null, null, separateThread);
	}

	/**
	 * Executes the given module.
	 * 
	 * @param info The module to instantiate and run.
	 * @param pre List of preprocessing steps to perform.
	 * @param post List of postprocessing steps to perform.
	 * @param separateThread Whether to execute the module in a new thread.
	 */
	public void run(final ModuleInfo info,
		final List<? extends ModulePreprocessor> pre,
		final List<? extends ModulePostprocessor> post,
		final boolean separateThread)
	{
		try {
			run(info.createModule(), pre, post, separateThread);
		}
		catch (final ModuleException e) {
			Log.error("Could not execute module: " + info, e);
		}
	}

	/**
	 * Executes the given module, without any pre- or postprocessing.
	 * 
	 * @param module The module to run.
	 * @param separateThread Whether to execute the module in a new thread.
	 */
	public void run(final Module module, final boolean separateThread) {
		run(module, null, null, separateThread);
	}

	/**
	 * Executes the given module.
	 * 
	 * @param module The module to run.
	 * @param pre List of preprocessing steps to perform.
	 * @param post List of postprocessing steps to perform.
	 * @param separateThread Whether to execute the module in a new thread.
	 */
	public void run(final Module module,
		final List<? extends ModulePreprocessor> pre,
		final List<? extends ModulePostprocessor> post,
		final boolean separateThread)
	{
		// TODO - Implement a better threading mechanism for launching modules.
		// Perhaps a ThreadService so that the UI can query currently
		// running modules and so forth?
		if (separateThread) {
			final String className = module.getInfo().getDelegateClassName();
			final String threadName = "ModuleRunner-" + className;
			new Thread(new Runnable() {

				@Override
				public void run() {
					new ModuleRunner(module, pre, post).run();
				}
			}, threadName).start();
		}
		else module.run();
	}

	// -- IService methods --

	@Override
	public void initialize() {
		subscribeToEvents();
	}

	// -- Helper methods --

	private void subscribeToEvents() {
		subscribers = new ArrayList<EventSubscriber<?>>();

		final EventSubscriber<ModulesCreatedEvent> modulesAddedSubscriber =
			new EventSubscriber<ModulesCreatedEvent>() {

				@Override
				public void onEvent(final ModulesCreatedEvent event) {
					addModules(event.getModuleInfos());
				}
			};
		subscribers.add(modulesAddedSubscriber);
		Events.subscribe(ModulesCreatedEvent.class, modulesAddedSubscriber);

		final EventSubscriber<ModulesDeletedEvent> modulesDeletedSubscriber =
			new EventSubscriber<ModulesDeletedEvent>() {

				@Override
				public void onEvent(final ModulesDeletedEvent event) {
					removeModules(event.getModuleInfos());
				}
			};
		subscribers.add(modulesDeletedSubscriber);
		Events.subscribe(ModulesDeletedEvent.class, modulesDeletedSubscriber);
	}

}
