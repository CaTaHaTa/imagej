package imagej.util.diag.inspect;

/*
 * Copyright (C) 2000 Sean Bridges
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */


import java.util.Iterator;
import java.util.Map;

/**
 * A map node is an object node that displays its values a little more intelligently. 
 */

public class MapNode extends ObjectNode {

  public MapNode(Value value, String name, InspectorNode parent)
  {
    super(value, name, parent);
  }

  public String getValueString()
  {
    Object instance = getValueReference().getValue();
    return instance.getClass().getName() + "\n" + asString(instance);
  }


  private String asString(Object value)
  {
    if(value == null)
      return "<null>";

    Map map = (Map) value;


    Iterator iter = map.keySet().iterator();

    StringBuffer buf = new StringBuffer();
    while(iter.hasNext())
    {
      Object key = iter.next();
      buf.append("  ");
      buf.append(key);
      buf.append("->");
      buf.append(map.get(key));
      if(iter.hasNext())
        buf.append("\n");
    }

    return buf.toString();

  }
}
