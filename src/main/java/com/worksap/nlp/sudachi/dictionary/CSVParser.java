/*
 * Copyright (c) 2019 Works Applications Co., Ltd.
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

package com.worksap.nlp.sudachi.dictionary;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class CSVParser implements Closeable {

    static class Token {

        enum Type {
            COMMA, DQUOTE, NL, TEXTDATA, EOF
        }

        final Type type;
        final String content;

        Token(Type type, String content) {
            this.type = type;
            this.content = content;
        }
    }

    static final Token COMMA_TOKEN = new Token(Token.Type.COMMA, ",");
    static final Token DQUOTE_TOKEN = new Token(Token.Type.DQUOTE, "\"");
    static final Token NL_TOKEN = new Token(Token.Type.NL, "\n");
    static final Token EOF_TOKEN = new Token(Token.Type.EOF, null);

    static final Pattern TOKEN_PATTERN = Pattern.compile(",|\"|[^,\"]+");

    private BufferedReader reader;
    private Deque<Token> tokenBuffer = new ArrayDeque<>();
    private boolean hasNextField = false;

    CSVParser(Reader reader) {
        this.reader = new BufferedReader(reader);
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }

    List<String> getNextRecord() throws IOException {
        List<String> record = new ArrayList<>();
        hasNextField = false;
        String field;
        while ((field = getField()) != null) {
            if (field.equals("\n")) {
                return record;
            }
            record.add(field);
        }
        return null;
    }

    private String getField() throws IOException {
        if (hasNextField) {
            return getNextField();
        } else {
            return getFirstField();
        }
    }

    private String getFirstField() throws IOException {
        Token token = getToken();
        switch (token.type) {
        case COMMA:
            hasNextField = true;
            return "";
        case DQUOTE:
            return getEscapedField();
        case NL:
            return "\n";
        case TEXTDATA:
            return getUnescapedField(token);
        case EOF:
            return null;
        default:
            throw new IllegalArgumentException("Invalid format");
        }
    }

    private String getNextField() throws IOException {
        Token token = getToken();
        switch (token.type) {
        case COMMA:
            hasNextField = true;
            return "";
        case DQUOTE:
            return getEscapedField();
        case NL:
            hasNextField = false;
            ungetToken(NL_TOKEN);
            return "";
        case EOF:
            hasNextField = false;
            return "";
        case TEXTDATA:
            return getUnescapedField(token);
        default:
            throw new IllegalArgumentException("Invalid format");
        }
    }

    private String getEscapedField() throws IOException {
        hasNextField = false;
        boolean isClosed = false;
        StringBuilder content = new StringBuilder();
        while (true) {
            Token token = getToken();
            switch (token.type) {
            case NL:
                if (isClosed) {
                    ungetToken(token);
                    return content.toString();
                } else {
                    content.append('\n');
                }
                break;
            case COMMA:
                if (isClosed) {
                    hasNextField = true;
                    return content.toString();
                } else {
                    content.append(',');
                }
                break;
            case DQUOTE:
                if (isClosed) {
                    content.append('"');
                    isClosed = false;
                } else {
                    isClosed = true;
                }
                break;
            case TEXTDATA:
                if (isClosed) {
                    throw new IllegalArgumentException("Invalid format");
                } else {
                    content.append(token.content);
                }
                break;
            case EOF:
                if (!isClosed) {
                    throw new IllegalArgumentException("Invalid format");
                }
                return null;
            default:
                throw new IllegalArgumentException("Invalid format");
            }
        }
    }

    private String getUnescapedField(Token firstToken) throws IOException {
        hasNextField = false;
        StringBuilder content = new StringBuilder(firstToken.content);
        while (true) {
            Token token = getToken();
            switch (token.type) {
            case COMMA:
                hasNextField = true;
                return content.toString();
            case NL:
                ungetToken(token);
                return content.toString();
            case TEXTDATA:
                content.append(token.content);
                break;
            case EOF:
                return null;
            default:
                throw new IllegalArgumentException("Invalid format");
            }
        }
    }

    private Token getToken() throws IOException {
        if (!tokenBuffer.isEmpty()) {
            return tokenBuffer.removeLast();
        }
        String line = reader.readLine();
        if (line == null) {
            return EOF_TOKEN;
        }
        Matcher matcher = TOKEN_PATTERN.matcher(line);
        while (matcher.find()) {
            Token token;
            String content = matcher.group();
            if (content.equals(",")) {
                token = COMMA_TOKEN;
            } else if (content.equals("\"")) {
                token = DQUOTE_TOKEN;
            } else {
                token = new Token(Token.Type.TEXTDATA, content);
            }
            tokenBuffer.push(token);
        }
        tokenBuffer.push(NL_TOKEN);

        return tokenBuffer.removeLast();
    }

    private void ungetToken(Token token) {
        tokenBuffer.push(token);
    }
}