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

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.StringSelection;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: mihai.panaitescu
 * Date: Feb 14, 2008
 * Time: 11:39:02 AM
 */
public class StringUtil {

    /**
     * Replace a string with another
     *
     * @param s       string to replace into
     * @param find    string to be replaced
     * @param replace new string
     * @return the string with replacements
     */
    public static ReplacedString replaceString(String s, String find, String replace) {
        if (replace == null) replace = "-";
        int index = -1;
        int l = find.length();
        boolean replaced = false;
        do {
            index = s.indexOf(find, index);
            if (index >= 0) {
                replaced = true;
                s = s.substring(0, index) + replace + s.substring(index + l);
            }
        } while (index >= 0);
        return new ReplacedString(s, replaced);
    }

    /**
     * Write a text to system clipboard
     *
     * @param text text to write
     */
    public static void writeToClipboard(String text) {
        Clipboard systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable transferableText = new StringSelection(text);
        systemClipboard.setContents(transferableText, null);
    }

    /** Get last characters from a string : if fewer characters, the string will start with ...
     *
     * @param s string
     * @param last last characters
     * @return a string with last characters
     */
    public static String getLastCharacters(String s, int last) {
        int index = s.length() - last + 1;
        if (index < 0) {
            index = 0;
        }
        String result = "";
        if (index > 0) {
            result = " ... ";
        }
        result = result +  s.substring(index);
        return result;
    }

}
