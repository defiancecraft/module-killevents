package com.defiancecraft.modules.killevents.tokens;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Denotes a TokenParser function
 */
@Retention(RetentionPolicy.CLASS)
@Documented
@Target(ElementType.METHOD)
public @interface TokenParserFunction {	
}
