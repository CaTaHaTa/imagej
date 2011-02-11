package imagej.gui;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.java.sezpoz.Indexable;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
@Indexable(type=IDisplayPlugin.class)
public @interface DisplayPlugin { }
