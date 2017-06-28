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
package org.apache.juneau;

import java.beans.*;

/**
 * Default property namer.
 *
 * <h5 class='section'>Examples:</h5>
 * <ul>
 * 	<li><js>"fooBar"</js> -&gt; <js>"fooBar"</js>
 * 	<li><js>"fooBarURL"</js> -&gt; <js>"fooBarURL"</js>
 * 	<li><js>"FooBarURL"</js> -&gt; <js>"fooBarURL"</js>
 * 	<li><js>"URL"</js> -&gt; <js>"URL"</js>
 * </ul>
 *
 * <p>
 * See {@link Introspector#decapitalize(String)} for exact rules.
 */
public final class PropertyNamerDefault implements PropertyNamer {

	@Override /* PropertyNamer */
	public String getPropertyName(String name) {
		return Introspector.decapitalize(name);
	}

}
