/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jssdb.core.proxy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface IndexField {
    //
}
