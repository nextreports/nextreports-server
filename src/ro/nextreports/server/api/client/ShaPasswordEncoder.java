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
package ro.nextreports.server.api.client;

import org.apache.commons.codec.digest.DigestUtils;

/**
 * SHA implementation of PasswordEncoder.
 * If a <code>null</code> password is presented, it will be treated as an empty
 * <code>String</code> ("") password.
 * As SHA is a one-way hash.
 * 
 * @author: Decebal Suiu
 */
public class ShaPasswordEncoder implements PasswordEncoder {

    public String encode(String plainPassword) {
        return DigestUtils.shaHex("" + plainPassword);
    }

    public boolean compare(String encodedPassword, String plainPassword) {
        String pass1 = "" + encodedPassword;
        String pass2 = encode(plainPassword);
        
        return pass1.equals(pass2);
    }
    
    public static void main(String[] args) {
        String plainPassword = "1";
        ShaPasswordEncoder shaEncoder = new ShaPasswordEncoder();
        String encodedPassword = shaEncoder.encode(plainPassword);
        System.out.println("encodedPassword: '" + encodedPassword + "'");
    }
    
}
