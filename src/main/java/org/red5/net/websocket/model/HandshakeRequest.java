/*
 * RED5 Open Source Flash Server - https://github.com/red5
 * 
 * Copyright 2006-2015 by respective authors (see below). All rights reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.red5.net.websocket.model;

import org.apache.mina.core.buffer.IoBuffer;

/**
 * Wraps around a string that represents a websocket handshake request from the browser to the server.
 * 
 * @author Paul Gregoire
 */
public class HandshakeRequest {

    private final IoBuffer req;

    public HandshakeRequest(IoBuffer req) {
        this.req = req;
        req.flip();
    }

    public IoBuffer getRequest() {
        return req;
    }

}