package org.radarcns.auth;

import static org.radarcns.webapp.resource.Parameter.PROJECT_NAME;
import static org.radarcns.webapp.resource.Parameter.SUBJECT_ID;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.radarcns.auth.authorization.Permission;
import org.radarcns.webapp.resource.Parameter;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface NeedsPermissionOnSubject {

    /**
     * Entity that the permission is needed on.
     */
    Permission.Entity entity();

    /**
     * Operation on given entity that the permission is needed for.
     */
    Permission.Operation operation();

    /**
     * Path parameter for the name of the project that the entity belongs to. In this API, {@link
     * Parameter#PROJECT_NAME} is usually used for path parameters, so that is the default.
     */
    String projectParam() default PROJECT_NAME;

    /**
     * Path parameter for the ID of the subject that the entity belongs to. In this API, {@link
     * Parameter#SUBJECT_ID} is usually used for path parameters, so that is the default.
     */
    String subjectParam() default SUBJECT_ID;
}
