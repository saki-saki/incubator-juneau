// ***************************************************************************************************************************
// * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements.  See the NOTICE file *
// * distributed with this work for additional information regarding copyright ownership.  The ASF licenses this file        *
// * to you under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance            *
// * with the License.  You may obtain a copy of the License at                                                              *
// *                                                                                                                         *
// *  http://www.apache.org/licenses/LICENSE-2.0                                                                             *
// *                                                                                                                         *
// * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an  *
// * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the License for the        *
// * specific language governing permissions and limitations under the License.                                              *
// ***************************************************************************************************************************
package org.apache.juneau.ini;

import java.io.*;

import org.apache.juneau.*;
import org.apache.juneau.http.*;

/**
 * Wraps a {@link ConfigFile} in a {@link Writable} to be rendered as plain text.
 */
class ConfigFileWritable implements Writable {

	private ConfigFileImpl cf;

	protected ConfigFileWritable(ConfigFileImpl cf) {
		this.cf = cf;
	}

	@Override /* Writable */
	public void writeTo(Writer out) throws IOException {
		cf.readLock();
		try {
			cf.serializeTo(out);
		} finally {
			cf.readUnlock();
		}
	}

	@Override /* Writable */
	public MediaType getMediaType() {
		return MediaType.PLAIN;
	}
}
