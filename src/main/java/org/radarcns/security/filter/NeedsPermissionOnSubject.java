package org.radarcns.security.filter;

import static org.radarcns.webapp.util.Parameter.SUBJECT_ID;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.radarcns.auth.authorization.Permission;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface NeedsPermissionOnSubject {

    /**
     * Token value for {@link #projectParam()} to indicate that the project name will be deduced
     * from the subject in {@link #subjectParam()}.
     */
    String PROJECT_OF_SUBJECT = "__projectOfSubject";

    /**
     * Entity that the permission is needed on.
     */
    Permission.Entity entity();

    /**
     * Operation on given entity that the permission is needed for.
     */
    Permission.Operation operation();

    /**
     * Path parameter for the name of the project that the entity belongs to.
     * If set to {@link #PROJECT_OF_SUBJECT}, it will be deduced by taking the project where given
     * subject is a participant. If {@link org.radarcns.webapp.util.Parameter#PROJECT_NAME} is a
     * path parameter, then that is preferred.
     */
    String projectParam() default PROJECT_OF_SUBJECT;

    /**
     * Path parameter for the ID of the subject that the entity belongs to.
     * In this API, {@link org.radarcns.webapp.util.Parameter#SUBJECT_ID} is usually used for path
     * parameters, so that is the default.
     */
    String subjectParam() default SUBJECT_ID;
}
