package org.radarcns.webapp.filter;

import java.lang.reflect.Method;
import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.Provider;
import org.radarcns.auth.NeedsPermission;
import org.radarcns.auth.NeedsPermissionOnProject;
import org.radarcns.auth.NeedsPermissionOnSubject;
import org.radarcns.auth.PermissionFilter;
import org.radarcns.auth.PermissionOnProjectFilter;
import org.radarcns.auth.PermissionOnSubjectFilter;

/** Authorization for different auth tags. */
@Provider
public class AuthorizationFeature implements DynamicFeature {
    @Override
    public void configure(ResourceInfo resourceInfo, FeatureContext context) {
        Method method = resourceInfo.getResourceMethod();
        if (method.isAnnotationPresent(NeedsPermission.class)) {
            context.register(PermissionFilter.class, 2000);
        }
        if (method.isAnnotationPresent(NeedsPermissionOnProject.class)) {
            context.register(PermissionOnProjectFilter.class, 2050);
        }
        if (method.isAnnotationPresent(NeedsPermissionOnSubject.class)) {
            context.register(PermissionOnSubjectFilter.class, 2100);
        }
    }
}
