/*
 * Copyright (c) 2018 Works Applications Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.worksap.nlp.sudachi;

import java.util.HashMap;
import java.util.Map;

class NumericParser {

    static class StringNumber {
        StringBuilder significand = new StringBuilder();
        int scale = 0;
        int point = -1;

        void clear() {
            significand.setLength(0);
            scale = 0;
            point = -1;
        }

        void append(int i) {
            if (i == 0 && isZero()) {
                return;
            }
            significand.append(intToChar(i));
        }

        void scale(int i) {
            if (isZero()) {
                significand.append('1');
            }
            scale += i;
        }

        boolean add(StringNumber number) {
            if (number.isZero()) {
                return true;
            }

            if (isZero()) {
                significand.append(number.significand);
                scale = number.scale;
                point = number.point;
                return true;
            }

            normalizeScale();
            int l = number.intLength();
            if (scale >= l) {
                fillZero(scale - l);
                if (number.point >= 0) {
                    point = significand.length() + number.point;
                }
                significand.append(number.significand.toString());
                scale = number.scale;
                return true;
            }

            return false;
        }

        boolean setPoint() {
            if (scale == 0 && point < 0) {
                point = significand.length();
                return true;
            }
            return false;
        }

        int intLength() {
            normalizeScale();
            if (point >= 0) {
                return point;
            }
            return significand.length() + scale;
        }

        boolean isZero() {
            return significand.length() == 0;
        }

        @Override
        public String toString() {
            if (isZero()) {
                return "0";
            }

            normalizeScale();
            if (scale > 0) {
                fillZero(scale);
            } else if (point >= 0) {
                significand.insert(point, '.');
                if (point == 0) {
                    significand.insert(0, '0');
                }
                int i = significand.length() - 1;
                while (i >= 0 && significand.charAt(i) == '0') {
                    i--;
                }
                significand.delete(i + 1, significand.length());
            }

            int l = (point >= 0) ? point - 1 : significand.length();
            int i = 0;
            while (i < l && significand.charAt(i) == '0') {
                i++;
            }
            significand.delete(0, i);

            return significand.toString();
        }

        private void normalizeScale() {
            if (point >= 0) {
                int nScale = significand.length() - point;
                if (nScale > scale) {
                    point += scale;
                    scale = 0;
                } else {
                    scale -= nScale;
                    point = -1;
                }
            }
        }

        private void fillZero(int length) {
            for (int i = 0; i < length; i++) {
                significand.append('0');
            }
        }

        private static char intToChar(int i) {
            return (char)('0' + i);
        }
    }

    private static final Map<Character, Integer> CHAR_TO_NUM;
    static {
        Map<Character, Integer> map = new HashMap<>();
        for (int i = 0; i < 10; i++) {
            map.put((char)('0' + i), i);
        }
        map.put('〇', 0);
        map.put('一', 1);
        map.put('二', 2);
        map.put('三', 3);
        map.put('四', 4);
        map.put('五', 5);
        map.put('六', 6);
        map.put('七', 7);
        map.put('八', 8);
        map.put('九', 9);
        map.put('十', -1);
        map.put('百', -2);
        map.put('千', -3);
        map.put('万', -4);
        map.put('億', -8);
        map.put('兆', -12);
        CHAR_TO_NUM = map;
    }

    int parsedLength = 0;
    int previousComma = -1;
    StringNumber total = new StringNumber();
    StringNumber subtotal = new StringNumber();
    StringNumber tmp = new StringNumber();

    void clear() {
        parsedLength = 0;
        previousComma = -1;
        total.clear();
        subtotal.clear();
        tmp.clear();
    }

    boolean append(char c) {
        parsedLength++;

        if (c == '.') {
            if (parsedLength == 1) {
                return false;
            }
            return tmp.setPoint();
        } else if (c == ',') {
            return checkComma();
        }

        Integer n = CHAR_TO_NUM.get(c);
        if (n == null) {
            return false;
        }
        if (isSmallUnit(n)) {
            tmp.scale(-n);
            if (!subtotal.add(tmp)) {
                return false;
            }
            tmp.clear();
        } else if (isLargeUnit(n)) {
            if (!subtotal.add(tmp)) {
                return false;
            }
            subtotal.scale(-n);
            if (!total.add(subtotal)) {
                return false;
            }
            subtotal.clear();
            tmp.clear();
        } else {
            tmp.append(n);
        }

        return true;
    }

    boolean done() {
        return subtotal.add(tmp) && total.add(subtotal);
    }

    String getNormalized() {
        return total.toString();
    }

    private boolean checkComma() {
        boolean ret;
        if (parsedLength == 1) {
            return false;
        } else if (previousComma < 0) {
            ret = (parsedLength <= 4 && !tmp.isZero());
        } else {
            ret = (parsedLength - previousComma == 4);
        }
        previousComma = parsedLength;
        return ret;
    }

    private boolean isSmallUnit(int n) {
        return n < 0 && n >= -3;
    }

    private boolean isLargeUnit(int n) {
        return n <= -4;
    }

}
