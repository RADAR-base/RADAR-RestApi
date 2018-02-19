package org.radarcns.auth;

import static org.radarcns.webapp.resource.Parameter.PROJECT_NAME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.radarcns.auth.authorization.Permission;
import org.radarcns.webapp.resource.Parameter;

/**
 * Indicates that a method needs an authenticated user that has a certain permission on a project.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface NeedsPermissionOnProject {

    /**
     * Entity that the permission is needed on.
     */
    Permission.Entity entity();

    /**
     * Operation on given entity that the permission is needed for.
     */
    Permission.Operation operation();

    /**
     * Path parameter for the name of the project that the entity belongs to. In this API, this is
     * usually {@link Parameter#PROJECT_NAME}, so this is the default.
     */
    String projectParam() default PROJECT_NAME;
}
