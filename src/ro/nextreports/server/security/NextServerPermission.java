/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ro.nextreports.server.security;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.springframework.security.acls.domain.AclFormattingUtils;
import org.springframework.security.acls.domain.CumulativePermission;
import org.springframework.security.acls.model.Permission;

/**
 * @author Decebal Suiu
 */
public final class NextServerPermission implements Permission {

    public static final Permission READ = new NextServerPermission(1 << 0, 'R'); // 1
    public static final Permission EXECUTE = new NextServerPermission(1 << 1, 'E'); // 2
    public static final Permission WRITE = new NextServerPermission(1 << 2, 'W'); // 4
    public static final Permission DELETE = new NextServerPermission(1 << 3, 'D'); // 8
    public static final Permission SECURITY = new NextServerPermission(1 << 4, 'C'); // 16

    private static Map<Integer, NextServerPermission> permissionsByInteger;
    private static Map<String, NextServerPermission> permissionsByName;

    private char code;
    private int mask;

    private NextServerPermission(int mask, char code) {
        this.mask = mask;
        this.code = code;
    }

    static {
    	permissionsByInteger = new HashMap<Integer, NextServerPermission>();
    	permissionsByName = new HashMap<String, NextServerPermission>();

        Field[] fields = NextServerPermission.class.getDeclaredFields();
        for (Field field : fields) {
            try {
                Object fieldValue = field.get(null);
                if (NextServerPermission.class.isAssignableFrom(fieldValue.getClass())) {
                    NextServerPermission permission = (NextServerPermission) fieldValue;
                    permissionsByInteger.put(permission.getMask(), permission);
                    permissionsByName.put(field.getName(), permission);
                }
            } catch (Exception e) {
            	// ignore
            }
        }
    }

    public static Permission buildFromMask(int mask) {
        if (permissionsByInteger.containsKey(mask)) {
            return permissionsByInteger.get(mask);
        }

        // to get this far, we have to use a CumulativePermission
        CumulativePermission cumulativePermission = new CumulativePermission();
        for (int i = 0; i < 32; i++) {
            int permissionToCheck = 1 << i;
            if ((mask & permissionToCheck) == permissionToCheck) {
                Permission permission = permissionsByInteger.get(permissionToCheck);
                if (permission == null) {
                	System.out.println("Mask " + permissionToCheck + " does not have a corresponding static NextServerPermission");
                	continue;
                }
                cumulativePermission.set(permission);
            }
        }

        return cumulativePermission;
    }

    public static Permission[] buildFromMask(int[] masks) {
        if ((masks == null) || (masks.length == 0)) {
            return new Permission[0];
        }

        List<Permission> list = new Vector<Permission>();
        for (int mask : masks) {
            list.add(NextServerPermission.buildFromMask(mask));
        }

        Permission[] permissions = new Permission[list.size()];
        permissions = list.toArray(permissions);

        return permissions;
    }

    public static Permission buildFromName(String name) {
    	if (!permissionsByName.containsKey(name)) {
    		System.out.println("Unknown permission '" + name + "'");
    		return null;
    	}

        return permissionsByName.get(name);
    }

    public static Permission[] buildFromName(String[] names) {
        if ((names == null) || (names.length == 0)) {
            return new Permission[0];
        }

        List<Permission> list = new Vector<Permission>();
        for (String name : names) {
            list.add(NextServerPermission.buildFromName(name));
        }

        Permission[] permissions = new Permission[list.size()];
        permissions = list.toArray(permissions);

        return permissions;
    }

    public int getMask() {
        return mask;
    }

    public String getPattern() {
        return AclFormattingUtils.printBinary(mask, code);
    }

    @Override
    public String toString() {
        return "NextServerPermission[" + getPattern() + "=" + mask + "]";
    }

    @Override
    public boolean equals(Object object) {
        if (object == null) {
            return false;
        }

        if (!(object instanceof Permission)) {
            return false;
        }

        Permission permission = (Permission) object;

        return (mask == permission.getMask());
    }

    @Override
	public int hashCode() {
		return mask;
	}

}
