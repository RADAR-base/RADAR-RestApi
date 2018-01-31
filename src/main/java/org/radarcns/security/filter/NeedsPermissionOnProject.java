package org.radarcns.security.filter;

import static org.radarcns.webapp.util.Parameter.PROJECT_NAME;
import static org.radarcns.webapp.util.Parameter.PROJECT_NAME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.radarcns.auth.authorization.Permission;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface NeedsPermissionOnProject {
    Permission.Entity entity();
    Permission.Operation operation();
    String projectParam() default PROJECT_NAME;
}
