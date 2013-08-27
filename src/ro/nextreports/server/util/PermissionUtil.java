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
package ro.nextreports.server.util;

/**
 * @author Decebal Suiu
 */
public class PermissionUtil {

	public static final char READ_SYMBOL = 'r';
	public static final char EXECUTE_SYMBOL = 'x';
	public static final char WRITE_SYMBOL = 'w';
	public static final char DELETE_SYMBOL = 'd';
	public static final char SECURITY_SYMBOL = 's';
	public static final char NO_SYMBOL = '-';
	
	public static boolean hasRead(int permissions) {
		return BitUtil.isSet(permissions, 0);
	}

	public static boolean hasExecute(int permissions) {
		return BitUtil.isSet(permissions, 1);
	}

	public static boolean hasWrite(int permissions) {
		return BitUtil.isSet(permissions, 2);
	}

	public static boolean hasDelete(int permissions) {
		return BitUtil.isSet(permissions, 3);
	}

	public static boolean hasSecurity(int permissions) {
		return BitUtil.isSet(permissions, 4);
	}

	public static int setRead(int permissions) {
		return setRead(permissions, true);
	}

	public static int setRead(int permissions, boolean b) {
		if (b) {
			return BitUtil.setBit(permissions, 0);
		} else {
			return BitUtil.clearBit(permissions, 0);
		}
	}

	public static int setExecute(int permissions) {
		return setExecute(permissions, true);
	}

	public static int setExecute(int permissions, boolean b) {
		if (b) {
			return BitUtil.setBit(permissions, 1);
		} else {
			return BitUtil.clearBit(permissions, 1);
		}
	}
	
	public static int setWrite(int permissions) {
		return setWrite(permissions, true);
	}

	public static int setWrite(int permissions, boolean b) {
		if (b) {
			return BitUtil.setBit(permissions, 2);
		} else {
			return BitUtil.clearBit(permissions, 2);
		}
	}

	public static int setDelete(int permissions) {
		return setDelete(permissions, true);
	}

	public static int setDelete(int permissions, boolean b) {
		if (b) {
			return BitUtil.setBit(permissions, 3);
		} else {
			return BitUtil.clearBit(permissions, 3);
		}
	}
	
	public static int setSecurity(int permissions) {
		return setSecurity(permissions, true);
	}

	public static int setSecurity(int permissions, boolean b) {
		if (b) {
			return BitUtil.setBit(permissions, 4);
		} else {
			return BitUtil.clearBit(permissions, 4);
		}
	}

    public static int getRead() {
        return setRead(0);
	}

	public static int getExecute() {
        return setExecute(0);
	}

	public static int getWrite() {
        return setWrite(0);
	}

	public static int getDelete() {
        return setDelete(0);
	}

	public static int getSecurity() {
        return setSecurity(0);
	}

    public static int getFullPermissions() {
        int permissions = 0;
        
        permissions = setRead(permissions);
        permissions = setWrite(permissions);
        permissions = setExecute(permissions);
        permissions = setDelete(permissions);
        permissions = setSecurity(permissions);
        
        return permissions;
    }

    public static String toString(int permissions) {
    	StringBuilder sb  = new StringBuilder();
    	
    	// check read
    	if (hasRead(permissions)) {
    		sb.append(READ_SYMBOL);
    	} else {
    		sb.append(NO_SYMBOL);
    	}

    	// check execute
    	if (hasExecute(permissions)) {
    		sb.append(EXECUTE_SYMBOL);
    	} else {
    		sb.append(NO_SYMBOL);
    	}

    	// check write
    	if (hasWrite(permissions)) {
    		sb.append(WRITE_SYMBOL);
    	} else {
    		sb.append(NO_SYMBOL);
    	}

    	// check delete
    	if (hasDelete(permissions)) {
    		sb.append(DELETE_SYMBOL);
    	} else {
    		sb.append(NO_SYMBOL);
    	}

    	// check security
    	if (hasDelete(permissions)) {
    		sb.append(SECURITY_SYMBOL);
    	} else {
    		sb.append(NO_SYMBOL);
    	}

    	return sb.toString();
    }
    
}
