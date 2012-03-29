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

package imagej.io.plugins;

import imagej.data.Dataset;
import imagej.ext.display.Display;
import imagej.ext.menu.MenuConstants;
import imagej.ext.module.ui.WidgetStyle;
import imagej.ext.plugin.Menu;
import imagej.ext.plugin.Parameter;
import imagej.ext.plugin.Plugin;
import imagej.io.event.FileSavedEvent;
import imagej.ui.DialogPrompt;
import imagej.ui.DialogPrompt.Result;
import imagej.ui.UIService;
import imagej.util.Log;

import java.io.File;

import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.img.ImgPlus;
import ome.scifio.img.ImgIOException;
import ome.scifio.img.ImgSaver;

/**
 * Saves the current {@link Dataset} to disk using a user-specified file name.
 * 
 * @author Mark Hiner
 */
@Plugin(menu = {
  @Menu(label = MenuConstants.FILE_LABEL, weight = MenuConstants.FILE_WEIGHT,
    mnemonic = MenuConstants.FILE_MNEMONIC),
    @Menu(label = "Save As...", weight = 21) })
    public class SaveAsImage extends AbstractImageHandler {

  @Parameter(persist = false)
  private UIService uiService;

  @Parameter(label = "File to save", style = WidgetStyle.FILE_SAVE, initializer = "initOutputFile", persist = false)
  private File outputFile;

  @Parameter
  private Dataset dataset;

  @Parameter
  private Display<?> display;
  
  public void initOutputFile() {
    outputFile = new File(dataset.getImgPlus().getSource());
  }

  @Override
  public void run() {
    @SuppressWarnings("rawtypes")
    final ImgPlus img = dataset.getImgPlus();
    boolean overwrite = true;

    // TODO prompts the user if the file is dirty or being saved to a new
    // location. Could remove the isDirty check to always overwrite the current
    // file
    if (outputFile.exists() &&
      (dataset.isDirty() || !outputFile.getAbsolutePath().equals(
        img.getSource())))
    {
      final Result result =
        uiService.showDialog("\"" + outputFile.getName() +
          "\" already exists. Do you want to replace it?", "Save [IJ2]",
          DialogPrompt.MessageType.WARNING_MESSAGE,
          DialogPrompt.OptionType.YES_NO_OPTION);
      overwrite = result == DialogPrompt.Result.YES_OPTION;
    }

    if (overwrite) {
      final ImgSaver imageSaver = new ImgSaver();
      try {
        imageSaver.addStatusListener(this);
        imageSaver.saveImg(outputFile.getAbsolutePath(), img);
        eventService.publish(new FileSavedEvent(img.getSource()));
      }
      catch (final ImgIOException e) {
        Log.error(e);
        uiService.showDialog(e.getMessage(), "IJ2: Save Error", DialogPrompt.MessageType.ERROR_MESSAGE);
        return;
      }
      catch (final IncompatibleTypeException e) {
        Log.error(e);
        uiService.showDialog(e.getMessage(), "IJ2: Save Error", DialogPrompt.MessageType.ERROR_MESSAGE);
        return;
      }
      dataset.setName(outputFile.getName());
      dataset.setDirty(false);

      // TODO -- HACK -- setName() + update() currently doesn't work.
      // Pending #995
      display.getPanel().getWindow().setTitle(outputFile.getName());
      display.setName(outputFile.getName());
      display.update();
    }
  }
}
