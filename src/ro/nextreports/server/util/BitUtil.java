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
public class BitUtil {

	/**
	 * @return `rawValue' with bit `bitIndex' set to 1
	 */
	public static int setBit(int value, int bitIndex) {
		assert(bitIndex >= 0);
		assert(bitIndex <= 31);

		return value | pow2(bitIndex);
	}

	/**
	 * @return true, if bit `bitIndex' in `rawValue' is set
	 */
	public static boolean isSet(int value, int bitIndex) {
		assert(bitIndex >= 0);
		assert(bitIndex <= 31);

		return (value & pow2(bitIndex)) != 0;
	}

	/**
	 * @return `rawValue' with bit `bitIndex' set to 0
	 */
	public static int clearBit(int value, int bitIndex) {
		assert(bitIndex >= 0);
		assert(bitIndex <= 31);

		int bit = pow2(bitIndex);
		return (value & bit) == 0 ? value : value ^ bit;
	}

	private static int pow2(int n) {
		return 1 << n;
	}

}
