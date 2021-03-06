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
package org.apache.juneau.urlencoding;

import static org.apache.juneau.serializer.SerializerContext.*;
import static org.apache.juneau.uon.UonSerializerContext.*;
import static org.apache.juneau.urlencoding.UrlEncodingContext.*;
import static org.apache.juneau.internal.StringUtils.*;

import java.io.*;
import java.net.*;

import org.apache.juneau.*;
import org.apache.juneau.serializer.*;
import org.apache.juneau.uon.*;

/**
 * Serializes POJO models to URL-encoded notation with UON-encoded values (a notation for URL-encoded query paramter values).
 *
 * <h5 class='section'>Media types:</h5>
 *
 * Handles <code>Accept</code> types: <code>application/x-www-form-urlencoded</code>
 * <p>
 * Produces <code>Content-Type</code> types: <code>application/x-www-form-urlencoded</code>
 *
 * <h5 class='section'>Description:</h5>
 *
 * This serializer provides several serialization options.  Typically, one of the predefined DEFAULT serializers will be sufficient.
 * However, custom serializers can be constructed to fine-tune behavior.
 *
 * <h5 class='section'>Configurable properties:</h5>
 *
 * This class has the following properties associated with it:
 * <ul>
 * 	<li>{@link UonSerializerContext}
 * 	<li>{@link BeanContext}
 * </ul>
 *
 * <p>
 * The following shows a sample object defined in Javascript:
 * <p class='bcode'>
 * 	{
 * 		id: 1,
 * 		name: <js>'John Smith'</js>,
 * 		uri: <js>'http://sample/addressBook/person/1'</js>,
 * 		addressBookUri: <js>'http://sample/addressBook'</js>,
 * 		birthDate: <js>'1946-08-12T00:00:00Z'</js>,
 * 		otherIds: <jk>null</jk>,
 * 		addresses: [
 * 			{
 * 				uri: <js>'http://sample/addressBook/address/1'</js>,
 * 				personUri: <js>'http://sample/addressBook/person/1'</js>,
 * 				id: 1,
 * 				street: <js>'100 Main Street'</js>,
 * 				city: <js>'Anywhereville'</js>,
 * 				state: <js>'NY'</js>,
 * 				zip: 12345,
 * 				isCurrent: <jk>true</jk>,
 * 			}
 * 		]
 * 	}
 * </p>
 *
 * <p>
 * Using the "strict" syntax defined in this document, the equivalent URL-encoded notation would be as follows:
 * <p class='bcode'>
 * 	<ua>id</ua>=<un>1</un>
 * 	&amp;<ua>name</ua>=<us>'John+Smith'</us>,
 * 	&amp;<ua>uri</ua>=<us>http://sample/addressBook/person/1</us>,
 * 	&amp;<ua>addressBookUri</ua>=<us>http://sample/addressBook</us>,
 * 	&amp;<ua>birthDate</ua>=<us>1946-08-12T00:00:00Z</us>,
 * 	&amp;<ua>otherIds</ua>=<uk>null</uk>,
 * 	&amp;<ua>addresses</ua>=@(
 * 		(
 * 			<ua>uri</ua>=<us>http://sample/addressBook/address/1</us>,
 * 			<ua>personUri</ua>=<us>http://sample/addressBook/person/1</us>,
 * 			<ua>id</ua>=<un>1</un>,
 * 			<ua>street</ua>=<us>'100+Main+Street'</us>,
 * 			<ua>city</ua>=<us>Anywhereville</us>,
 * 			<ua>state</ua>=<us>NY</us>,
 * 			<ua>zip</ua>=<un>12345</un>,
 * 			<ua>isCurrent</ua>=<uk>true</uk>
 * 		)
 * 	)
 * </p>
 *
 * <h5 class='section'>Example:</h5>
 * <p class='bcode'>
 * 	<jc>// Serialize a Map</jc>
 * 	Map m = <jk>new</jk> ObjectMap(<js>"{a:'b',c:1,d:false,e:['f',1,false],g:{h:'i'}}"</js>);
 *
 * 	<jc>// Serialize to value equivalent to JSON.</jc>
 * 	<jc>// Produces "a=b&amp;c=1&amp;d=false&amp;e=@(f,1,false)&amp;g=(h=i)"</jc>
 * 	String s = UrlEncodingSerializer.<jsf>DEFAULT</jsf>.serialize(s);
 *
 * 	<jc>// Serialize a bean</jc>
 * 	<jk>public class</jk> Person {
 * 		<jk>public</jk> Person(String s);
 * 		<jk>public</jk> String getName();
 * 		<jk>public int</jk> getAge();
 * 		<jk>public</jk> Address getAddress();
 * 		<jk>public boolean</jk> deceased;
 * 	}
 *
 * 	<jk>public class</jk> Address {
 * 		<jk>public</jk> String getStreet();
 * 		<jk>public</jk> String getCity();
 * 		<jk>public</jk> String getState();
 * 		<jk>public int</jk> getZip();
 * 	}
 *
 * 	Person p = <jk>new</jk> Person(<js>"John Doe"</js>, 23, <js>"123 Main St"</js>, <js>"Anywhere"</js>, <js>"NY"</js>, 12345, <jk>false</jk>);
 *
 * 	<jc>// Produces "name=John+Doe&amp;age=23&amp;address=(street='123+Main+St',city=Anywhere,state=NY,zip=12345)&amp;deceased=false"</jc>
 * 	String s = UrlEncodingSerializer.<jsf>DEFAULT</jsf>.serialize(s);
 * </p>
 */
@SuppressWarnings("hiding")
public class UrlEncodingSerializer extends UonSerializer implements PartSerializer {

	/** Reusable instance of {@link UrlEncodingSerializer}, all default settings. */
	public static final UrlEncodingSerializer DEFAULT = new UrlEncodingSerializer(PropertyStore.create());

	/** Reusable instance of {@link UrlEncodingSerializer.PlainText}. */
	public static final UrlEncodingSerializer DEFAULT_PLAINTEXT = new PlainText(PropertyStore.create());

	/** Reusable instance of {@link UrlEncodingSerializer.Expanded}. */
	public static final UrlEncodingSerializer DEFAULT_EXPANDED = new Expanded(PropertyStore.create());

	/** Reusable instance of {@link UrlEncodingSerializer.Readable}. */
	public static final UrlEncodingSerializer DEFAULT_READABLE = new Readable(PropertyStore.create());

	/**
	 * Equivalent to <code><jk>new</jk> UrlEncodingSerializerBuilder().expandedParams(<jk>true</jk>).build();</code>.
	 */
	public static class Expanded extends UrlEncodingSerializer {

		/**
		 * Constructor.
		 *
		 * @param propertyStore The property store containing all the settings for this object.
		 */
		public Expanded(PropertyStore propertyStore) {
			super(propertyStore.copy().append(URLENC_expandedParams, true));
		}
	}

	/**
	 * Equivalent to <code><jk>new</jk> UrlEncodingSerializerBuilder().useWhitespace(<jk>true</jk>).build();</code>.
	 */
	public static class Readable extends UrlEncodingSerializer {

		/**
		 * Constructor.
		 *
		 * @param propertyStore The property store containing all the settings for this object.
		 */
		public Readable(PropertyStore propertyStore) {
			super(propertyStore.copy().append(SERIALIZER_useWhitespace, true));
		}
	}

	/**
	 * Equivalent to <code><jk>new</jk> UrlEncodingSerializerBuilder().plainTextParts().build();</code>.
	 */
	public static class PlainText extends UrlEncodingSerializer {

		/**
		 * Constructor.
		 *
		 * @param propertyStore The property store containing all the settings for this object.
		 */
		public PlainText(PropertyStore propertyStore) {
			super(propertyStore.copy().append(UON_paramFormat, "PLAINTEXT"));
		}
	}

	private final UrlEncodingSerializerContext ctx;

	/**
	 * Constructor.
	 *
	 * @param propertyStore
	 * 	The property store containing all the settings for this object.
	 */
	public UrlEncodingSerializer(PropertyStore propertyStore) {
		this(propertyStore, "application/x-www-form-urlencoded");
	}

	/**
	 * Constructor.
	 *
	 * @param propertyStore
	 * 	The property store containing all the settings for this object.
	 * @param produces
	 * 	The media type that this serializer produces.
	 * @param accept
	 * 	The accept media types that the serializer can handle.
	 * 	<p>
	 * 	Can contain meta-characters per the <code>media-type</code> specification of
	 * 	<a class="doclink" href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.1">RFC2616/14.1</a>
	 * 	<p>
	 * 	If empty, then assumes the only media type supported is <code>produces</code>.
	 * 	<p>
	 * 	For example, if this serializer produces <js>"application/json"</js> but should handle media types of
	 * 	<js>"application/json"</js> and <js>"text/json"</js>, then the arguments should be:
	 * 	<br><code><jk>super</jk>(propertyStore, <js>"application/json"</js>, <js>"application/json"</js>, <js>"text/json"</js>);</code>
	 * 	<br>...or...
	 * 	<br><code><jk>super</jk>(propertyStore, <js>"application/json"</js>, <js>"*&#8203;/json"</js>);</code>
	 */
	public UrlEncodingSerializer(PropertyStore propertyStore, String produces, String...accept) {
		super(propertyStore.copy().append(UON_encodeChars, true), produces, accept);
		this.ctx = createContext(UrlEncodingSerializerContext.class);
	}


	@Override /* CoreObject */
	public UrlEncodingSerializerBuilder builder() {
		return new UrlEncodingSerializerBuilder(propertyStore);
	}


	//--------------------------------------------------------------------------------
	// Methods for constructing individual parameter values.
	//--------------------------------------------------------------------------------

	/**
	 * Converts the specified object to a string using this serializers {@link BeanSession#convertToType(Object, Class)}
	 * method and runs {@link URLEncoder#encode(String,String)} against the results.
	 *
	 * <p>
	 * Useful for constructing URL parts.
	 *
	 * @param o The object to serialize.
	 * @param urlEncode
	 * 	URL-encode the string if necessary.
	 * 	If <jk>null</jk>, then uses the value of the {@link UonSerializerContext#UON_encodeChars} setting.
	 * @param plainTextParams
	 * 	Whether we're using plain-text params.
	 * 	If <jk>null</jk>, then uses the value from the {@link UrlEncodingSerializerContext#URLENC_paramFormat} setting.
	 * @return The serialized object.
	 */
	private String serializePart(Object o, Boolean urlEncode, Boolean plainTextParams) {
		try {
			// Shortcut for simple types.
			ClassMeta<?> cm = getBeanContext().getClassMetaForObject(o);
			if (cm != null) {
				if (cm.isNumber() || cm.isBoolean())
					return o.toString();
				if (cm.isCharSequence()) {
					String s = o.toString();
					boolean ptt = (plainTextParams != null ? plainTextParams : ctx.plainTextParams());
					if (ptt || s.isEmpty() || ! UonUtils.needsQuotes(s))
						return (urlEncode ? urlEncode(s) : s);
				}
			}

			StringWriter w = new StringWriter();
			UonSerializerSession s = new UonSerializerSession(ctx, urlEncode, createDefaultSessionArgs());
			s.serialize(w, o);
			return w.toString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}


	//--------------------------------------------------------------------------------
	// Entry point methods
	//--------------------------------------------------------------------------------

	@Override /* Serializer */
	public WriterSerializerSession createSession(SerializerSessionArgs args) {
		return new UrlEncodingSerializerSession(ctx, null, args);
	}

	@Override /* PartSerializer */
	public String serialize(PartType type, Object value) {
		switch(type) {
			case HEADER: return serializePart(value, false, true);
			case FORM_DATA: return serializePart(value, false, null);
			case PATH: return serializePart(value, false, null);
			case QUERY: return serializePart(value, false, null);
			default: return toString(value);
		}
	}
}
