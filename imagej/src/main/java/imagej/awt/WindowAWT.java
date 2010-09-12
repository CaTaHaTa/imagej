package imagej.awt;

import ijx.gui.IjxWindow;
import java.awt.Frame;

/**
 *
 * @author GBH
 */
public class WindowAWT extends Frame implements IjxWindow{

  private boolean closed;
    @Override
    public boolean isClosed() {
      return closed;
    }

    @Override
    public boolean canClose() {
      return true;
    }

    @Override
    public boolean close() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
