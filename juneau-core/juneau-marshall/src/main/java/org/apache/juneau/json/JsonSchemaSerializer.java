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
package org.apache.juneau.json;

import static org.apache.juneau.serializer.SerializerContext.*;

import org.apache.juneau.*;
import org.apache.juneau.serializer.*;

/**
 * Serializes POJO metadata to HTTP responses as JSON.
 *
 * <h5 class='section'>Media types:</h5>
 *
 * Handles <code>Accept</code> types: <code>application/json+schema, text/json+schema</code>
 * <p>
 * Produces <code>Content-Type</code> types: <code>application/json</code>
 *
 * <h5 class='section'>Description:</h5>
 *
 * Produces the JSON-schema for the JSON produced by the {@link JsonSerializer} class with the same properties.
 */
public final class JsonSchemaSerializer extends JsonSerializer {

	/**
	 * Constructor.
	 *
	 * @param propertyStore Initialize with the specified config property store.
	 */
	public JsonSchemaSerializer(PropertyStore propertyStore) {
		super(
			propertyStore.copy()
				.append(SERIALIZER_detectRecursions, true)
				.append(SERIALIZER_ignoreRecursions, true),
			"application/json",
			"application/json+schema", "text/json+schema"
		);
	}

	@Override /* Serializer */
	public WriterSerializerSession createSession(SerializerSessionArgs args) {
		return new JsonSchemaSerializerSession(ctx, args);
	}
}