//
// AbstractAWTDisplay.java
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

package imagej.gui.swing.display;

import imagej.display.Display;
import imagej.display.event.key.KyPressedEvent;
import imagej.display.event.key.KyReleasedEvent;
import imagej.display.event.key.KyTypedEvent;
import imagej.display.event.mouse.MsButtonEvent;
import imagej.display.event.mouse.MsClickedEvent;
import imagej.display.event.mouse.MsDraggedEvent;
import imagej.display.event.mouse.MsEnteredEvent;
import imagej.display.event.mouse.MsExitedEvent;
import imagej.display.event.mouse.MsMovedEvent;
import imagej.display.event.mouse.MsPressedEvent;
import imagej.display.event.mouse.MsReleasedEvent;
import imagej.display.event.mouse.MsWheelEvent;
import imagej.display.event.window.WinActivatedEvent;
import imagej.display.event.window.WinClosedEvent;
import imagej.display.event.window.WinClosingEvent;
import imagej.display.event.window.WinDeactivatedEvent;
import imagej.display.event.window.WinDeiconifiedEvent;
import imagej.display.event.window.WinIconifiedEvent;
import imagej.display.event.window.WinOpenedEvent;
import imagej.event.Events;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

/**
 * TODO
 *
 * @author Curtis Rueden
 */
public abstract class AbstractAWTDisplay implements Display, KeyListener,
	MouseListener, MouseMotionListener, MouseWheelListener, WindowListener
{

	// -- KeyListener methods --

	@Override
	public void keyTyped(KeyEvent e) {
		Events.publish(new KyTypedEvent(this,
			e.getKeyChar(), e.getKeyCode(), e.getModifiers()));
	}

	@Override
	public void keyPressed(KeyEvent e) {
		Events.publish(new KyPressedEvent(this,
			e.getKeyChar(), e.getKeyCode(), e.getModifiers()));
	}

	@Override
	public void keyReleased(KeyEvent e) {
		Events.publish(new KyReleasedEvent(this,
			e.getKeyChar(), e.getKeyCode(), e.getModifiers()));
	}

	// -- MouseListener methods --

	@Override
	public void mouseClicked(MouseEvent e) {
		Events.publish(new MsClickedEvent(this, e.getX(), e.getY(),
			mouseButton(e), e.getClickCount(), e.isPopupTrigger()));
	}

	@Override
	public void mousePressed(MouseEvent e) {
		Events.publish(new MsPressedEvent(this, e.getX(), e.getY(),
			mouseButton(e), e.getClickCount(), e.isPopupTrigger()));
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		Events.publish(new MsReleasedEvent(this, e.getX(), e.getY(),
			mouseButton(e), e.getClickCount(), e.isPopupTrigger()));
	}

	// -- MouseMotionListener methods --

	@Override
	public void mouseEntered(MouseEvent e) {
		Events.publish(new MsEnteredEvent(this, e.getX(), e.getY()));
	}

	@Override
	public void mouseExited(MouseEvent e) {
		Events.publish(new MsExitedEvent(this, e.getX(), e.getY()));
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		Events.publish(new MsDraggedEvent(this, e.getX(), e.getY(),
			mouseButton(e), e.getClickCount(), e.isPopupTrigger()));
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		Events.publish(new MsMovedEvent(this, e.getX(), e.getY()));
	}

	// -- MouseWheelListener methods --

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		Events.publish(new MsWheelEvent(this,
			e.getX(), e.getY(), e.getWheelRotation()));
	}

	// -- WindowListener methods --

	@Override
	public void windowActivated(WindowEvent e) {
		Events.publish(new WinActivatedEvent(this));
	}

	@Override
	public void windowClosed(WindowEvent e) {
		Events.publish(new WinClosedEvent(this));
	}

	@Override
	public void windowClosing(WindowEvent e) {
		Events.publish(new WinClosingEvent(this));
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
		Events.publish(new WinDeactivatedEvent(this));
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
		Events.publish(new WinDeiconifiedEvent(this));
	}

	@Override
	public void windowIconified(WindowEvent e) {
		Events.publish(new WinIconifiedEvent(this));
	}

	@Override
	public void windowOpened(WindowEvent e) {
		Events.publish(new WinOpenedEvent(this));
	}

	private int mouseButton(final MouseEvent e) {
		switch (e.getButton()) {
			case MouseEvent.BUTTON1:
				return MsButtonEvent.LEFT_BUTTON;
			case MouseEvent.BUTTON2:
				return MsButtonEvent.RIGHT_BUTTON;
			case MouseEvent.BUTTON3:
				return MsButtonEvent.MIDDLE_BUTTON;
			default:
				return -1;
		}
	}

}
