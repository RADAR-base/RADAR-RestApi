package org.radarcns.security.filter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.ws.rs.NameBinding;
import org.radarcns.auth.authorization.Permission;

/**
 * Indicates that a method needs an authenticated user that has a certain permission.
 */
@NameBinding
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface NeedsPermission {
    /**
     * Entity that the permission is needed on.
     */
    Permission.Entity entity();

    /**
     * Operation on given entity that the permission is needed for.
     */
    Permission.Operation operation();
}
