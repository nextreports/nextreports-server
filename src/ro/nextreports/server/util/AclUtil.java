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

import ro.nextreports.server.domain.AclEntry;

/**
 * @author Decebal Suiu
 */
public class AclUtil {

	private static final String DELIMITER = ":";

	public static String createRawAclEntry() {
		return DELIMITER.concat(DELIMITER);
	}

	public static byte getType(String rawAclEntry) {
		return Byte.parseByte(rawAclEntry.split(DELIMITER, 3)[0]);
	}

	public static String setType(String rawAclEntry, byte type) {
		String[] tokens = rawAclEntry.split(DELIMITER, 3);

		StringBuilder sb = new StringBuilder(50);
		sb.append(type);
		sb.append(DELIMITER);
		sb.append(tokens[1]);
		sb.append(DELIMITER);
		sb.append(tokens[2]);

		return sb.toString();
	}

	public static boolean isUserType(String rawAclEntry) {
		return getType(rawAclEntry) == AclEntry.USER_TYPE;
	}

	public static String setUserType(String rawAclEntry) {
		return setType(rawAclEntry, AclEntry.USER_TYPE);
	}

	public static boolean isGroupType(String rawAclEntry) {
		return !isUserType(rawAclEntry);
	}

	public static String setGroupType(String rawAclEntry) {
		return setType(rawAclEntry, AclEntry.GROUP_TYPE);
	}

	public static String getName(String rawAclEntry) {
		return rawAclEntry.split(DELIMITER, 3)[1];
	}

	public static String setName(String rawAclEntry, String name) {
		String[] tokens = rawAclEntry.split(DELIMITER, 3);

		StringBuilder sb = new StringBuilder(50);
		sb.append(tokens[0]);
		sb.append(DELIMITER);
		sb.append(name);
		sb.append(DELIMITER);
		sb.append(tokens[2]);

		return sb.toString();
	}

	public static int getPermissions(String rawAclEntry) {
		return Integer.parseInt(rawAclEntry.split(DELIMITER, 3)[2]);
	}

	public static String setPermissions(String rawAclEntry, int permissions) {
		String[] tokens = rawAclEntry.split(DELIMITER, 3);

		StringBuilder sb = new StringBuilder(50);
		sb.append(tokens[0]);
		sb.append(DELIMITER);
		sb.append(tokens[1]);
		sb.append(DELIMITER);
		sb.append(permissions);

		return sb.toString();
	}

	public static AclEntry decodeAclEntry(String rawAclEntry) {
		byte type = AclUtil.getType(rawAclEntry);
		String name = AclUtil.getName(rawAclEntry);
		int permissions = AclUtil.getPermissions(rawAclEntry);
		
		return new AclEntry(type, name, permissions);
	}

	public static String encodeAclEntry(AclEntry aclEntry) {
		String rawAclEntry = AclUtil.createRawAclEntry();

		rawAclEntry = AclUtil.setType(rawAclEntry, aclEntry.getType());
		rawAclEntry = AclUtil.setName(rawAclEntry, aclEntry.getName());
		rawAclEntry = AclUtil.setPermissions(rawAclEntry, aclEntry.getPermissions());

		return rawAclEntry;
	}

}
