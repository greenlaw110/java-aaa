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
import org.osgl.exception.NotAppliedException;
import org.osgl.util.C;
import org.osgl.util.E;
import org.osgl.util.S;

import java.util.Collection;
import java.util.List;

/**
 * A simple and immutable {@link org.osgl.aaa.Principal} implementation.
 * <p>
 * This implementation use internal data structure to store the permissions and roles
 * granted to the principal. Sub class might choose to rely on
 * {@link org.osgl.aaa.AuthorizationService authorization service} to build up the acl
 * </p>
 */
public class SimplePrincipal extends AAAObjectBase implements Principal {

    public static final $.Func1<Permission, Iterable<Permission>> EXPAND_PERMISSION =
            new $.Func1<Permission, Iterable<Permission>>() {
        @Override
        public Iterable<Permission> apply(Permission permission) throws NotAppliedException, $.Break {
            return permission.implied();
        }
    };

    private Privilege privilege;
    private List<? extends Role> roles = C.list();
    private List<? extends Permission> perms = C.list();

    /**
     * This constructor is designed to be used by tools like ORM to deserialize the object from
     * a certain persistent storage
     */
    public SimplePrincipal() {}

    /**
     * Construct a principal by name, privilege, list of roles and list of permissions
     *
     * @param name the name of the principal
     * @param privilege the privilege
     * @param roles a collection of roles
     * @param perms a collection of permissions
     */
    public SimplePrincipal(String name, Privilege privilege, Collection<? extends Role> roles, Collection<? extends Permission> perms) {
        super(name);
        this.privilege = privilege;

        List<Role> emptyRoles = C.list();
        this.roles = null == roles ? emptyRoles : C.list(roles);

        List<Permission> emptyPerms = C.list();
        this.perms = null == perms ? emptyPerms : C.list(perms);
    }

    @Override
    public Privilege getPrivilege() {
        return privilege;
    }

    @Override
    public C.List<Role> getRoles() {
        return C.list(roles);
    }

    @Override
    public C.List<Permission> getPermissions() {
        return C.list(perms);
    }

    public static final Principal createSystemPrincipal(String name) {
        C.List<SimpleRole> roles = C.list();
        C.List<SimplePermission> perms = C.list();
        return new SimplePrincipal(name, null, roles, perms);
    }

    /**
     * The Builder can be used to build up a simple principal
     */
    public static class Builder {
        private String name;
        private Privilege privilege;
        private C.List<Role> roles = C.newList();
        private C.List<Permission> perms = C.newList();

        public Builder(Principal copy) {
            name = copy.getName();
            privilege = copy.getPrivilege();
            roles.addAll(copy.getRoles());
            perms.addAll(copy.getPermissions());
        }

        public Builder(String name) {
            E.illegalArgumentIf(S.blank(name));
            this.name = name;
        }

        public Builder grantPrivilege(Privilege p) {
            this.privilege = p;
            return this;
        }

        public Builder revokePrivilege() {
            this.privilege = null;
            return this;
        }

        public Builder grantRole(Role role) {
            roles.add(role);
            return this;
        }

        public Builder revokeRole(final String roleName) {
            roles = roles.remove(AAAObject.F.nameMatcher(roleName));
            return this;
        }

        public Builder revokeAllRoles() {
            roles.clear();
            return this;
        }

        public Builder grantPermission(Permission perm) {
            perms.add(perm);
            return this;
        }

        public Builder revokePermission(final String permName) {
            perms = perms.remove(AAAObject.F.nameMatcher(permName));
            return this;
        }

        public Builder revokeAllPermissions() {
            perms.clear();
            return this;
        }

        public SimplePrincipal toPrincipal() {
            return new SimplePrincipal(name, privilege, roles, perms);
        }
    }
}
