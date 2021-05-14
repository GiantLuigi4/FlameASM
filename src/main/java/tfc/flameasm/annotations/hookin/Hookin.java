package tfc.flameasm.annotations.hookin;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Hookin {
	String target();
	String mappings() default "SAME_AS_JAR";
}
