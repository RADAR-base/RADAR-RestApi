package org.radarcns.webapp.filter;

import java.lang.reflect.Method;
import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.Provider;
import org.radarcns.security.filter.NeedsPermission;
import org.radarcns.security.filter.NeedsPermissionOnProject;
import org.radarcns.security.filter.NeedsPermissionOnSubject;
import org.radarcns.security.filter.PermissionFilter;
import org.radarcns.security.filter.PermissionOnProjectFilter;
import org.radarcns.security.filter.PermissionOnSubjectFilter;

@Provider
public class AuthorizationDynamicFeature implements DynamicFeature {

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
