package org.radarcns.security.filter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.ws.rs.NameBinding;
import org.radarcns.auth.authorization.Permission;

@NameBinding
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface NeedsPermission {
    Permission.Entity entity();
    Permission.Operation operation();
}
