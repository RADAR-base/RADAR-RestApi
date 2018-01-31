package org.radarcns.security.filter;

import static org.radarcns.webapp.util.Parameter.PROJECT_NAME;
import static org.radarcns.webapp.util.Parameter.SUBJECT_ID;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.radarcns.auth.authorization.Permission;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface NeedsPermissionOnSubject {
    String NO_PROJECT = "none";

    Permission.Entity entity();
    Permission.Operation operation();
    String projectParam() default NO_PROJECT;
    String subjectParam() default SUBJECT_ID;
}
