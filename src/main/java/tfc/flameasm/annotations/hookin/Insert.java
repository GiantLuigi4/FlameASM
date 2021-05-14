package tfc.flameasm.annotations.hookin;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Insert {
	String target();
	
	/**
	 * Valid points are as follows:
	 * TOP, which inserts a call to your method at the top of the target class's code for the method
	 * BOTTOM, same as top, but puts it before return statements
	 */
	String point();
	String mappings() default "SAME_AS_JAR";
}
