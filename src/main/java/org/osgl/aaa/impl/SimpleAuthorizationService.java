package org.osgl.aaa.impl;

/*-
 * #%L
 * Java AAA Service
 * %%
 * Copyright (C) 2017 OSGL (Open Source General Library)
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.osgl.$;
import org.osgl.aaa.*;
import org.osgl.logging.LogManager;
import org.osgl.logging.Logger;
import org.osgl.util.C;

import java.util.Collection;
import java.util.Set;

/**
 * A simple authorization service implementation
 */
public class SimpleAuthorizationService implements AuthorizationService {

    private static Logger logger = LogManager.get(SimpleAuthorizationService.class);

    @Override
    public Privilege getPrivilege(Principal principal, AAAContext context) {
        return principal.getPrivilege();
    }

    @Override
    public Collection<Role> getRoles(Principal principal, AAAContext context) {
        return principal.getRoles();
    }

    @Override
    public Collection<Permission> getPermissions(Role role, AAAContext context) {
        return role.getPermissions();
    }

    @Override
    public Collection<Permission> getPermissions(Principal principal, AAAContext context) {
        return principal.getPermissions();
    }

    @Override
    public Collection<Permission> getAllPermissions(Principal principal, AAAContext context) {
        C.List<Permission> perms = C.newList(getPermissions(principal, context)).lazy();
        C.list(getRoles(principal, context)).accept($.visitor(Role.F.PERMISSION_GETTER.andThen(C.F.addAllTo(perms))));
        Set<Permission> retVal = C.newSet();
        for (Permission p : perms) {
            if (null == p) {
                logger.warn(new RuntimeException(), "Null permission found on principal %s", principal.getName());
                continue;
            }
            collectPermission(retVal, p);
        }
        return retVal;
    }

    private void collectPermission(Set<Permission> set, Permission p) {
        Set<Permission> implied = p.implied();
        if (null == implied) {
            logger.warn(new RuntimeException(""), "Null implied found on permission: %s", p.getName());
        }
        for (Permission p0 : p.implied()) {
            if (p0 != p) {
                collectPermission(set, p0);
            }
        }
        set.add(p);
    }
}
