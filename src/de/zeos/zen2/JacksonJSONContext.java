/*
 * Copyright (c) 2011 the original author or authors.
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
 *
 *
 * NOTE: this class should be obsolete as soon as cometd supports jackson 2
 */

package de.zeos.zen2;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.text.ParseException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cometd.bayeux.server.ServerMessage;
import org.cometd.common.JSONContext;
import org.cometd.server.ServerMessageImpl;
import org.eclipse.jetty.util.IO;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class JacksonJSONContext implements JSONContext.Server {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final JavaType rootArrayType;

    private Log logger = LogFactory.getLog(getClass());

    public JacksonJSONContext() {
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        this.rootArrayType = this.objectMapper.constructType(ServerMessageImpl[].class);
    }

    public ObjectMapper getObjectMapper() {
        return this.objectMapper;
    }

    @Override
    public ServerMessage.Mutable[] parse(InputStream stream) throws ParseException {
        if (this.logger.isDebugEnabled()) {
            String input;
            try {
                input = IO.toString(stream);
            } catch (IOException x) {
                throw (ParseException) new ParseException("", -1).initCause(x);
            }
            return parse(input);
        }

        try {
            return getObjectMapper().readValue(stream, this.rootArrayType);
        } catch (IOException x) {
            throw (ParseException) new ParseException("", -1).initCause(x);
        }
    }

    @Override
    public ServerMessage.Mutable[] parse(Reader reader) throws ParseException {
        if (this.logger.isDebugEnabled()) {
            String input;
            try {
                input = IO.toString(reader);
            } catch (IOException x) {
                throw (ParseException) new ParseException("", -1).initCause(x);
            }
            return parse(input);
        }

        try {
            return getObjectMapper().readValue(reader, this.rootArrayType);
        } catch (IOException x) {
            throw (ParseException) new ParseException("", -1).initCause(x);
        }
    }

    @Override
    public ServerMessage.Mutable[] parse(String json) throws ParseException {
        this.logger.debug("Received: " + json);

        try {
            return getObjectMapper().readValue(json, this.rootArrayType);
        } catch (IOException x) {
            throw (ParseException) new ParseException(json, -1).initCause(x);
        }
    }

    @Override
    public String generate(ServerMessage.Mutable message) {
        String output;
        try {
            output = getObjectMapper().writeValueAsString(message);
        } catch (IOException x) {
            throw new RuntimeException(x);
        }

        this.logger.debug("Sending: " + output);
        return output;
    }

    @Override
    public String generate(ServerMessage.Mutable[] messages) {
        String output;
        try {
            output = getObjectMapper().writeValueAsString(messages);
        } catch (IOException x) {
            throw new RuntimeException(x);
        }
        this.logger.debug("Sending: " + output);
        return output;
    }
}
