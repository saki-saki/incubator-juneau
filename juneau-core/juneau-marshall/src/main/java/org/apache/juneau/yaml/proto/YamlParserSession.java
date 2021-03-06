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
package org.apache.juneau.yaml.proto;

import static org.apache.juneau.internal.StringUtils.*;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;

import org.apache.juneau.*;
import org.apache.juneau.internal.*;
import org.apache.juneau.parser.*;
import org.apache.juneau.transform.*;

/**
 * Session object that lives for the duration of a single use of {@link YamlParser}.
 *
 * <p>
 * This class is NOT thread safe.
 * It is typically discarded after one-time use although it can be reused against multiple inputs.
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public final class YamlParserSession extends ReaderParserSession {

	private static final AsciiSet decChars = new AsciiSet("0123456789");

	/**
	 * Create a new session using properties specified in the context.
	 *
	 * @param ctx
	 * 	The context creating this session object.
	 * 	The context contains all the configuration settings for this object.
	 * @param args
	 * 	Runtime session arguments.
	 */
	protected YamlParserSession(YamlParserContext ctx, ParserSessionArgs args) {
		super(ctx, args);
	}

	/**
	 * Returns <jk>true</jk> if the specified character is whitespace.
	 *
	 * <p>
	 * The definition of whitespace is different for strict vs lax mode.
	 * Strict mode only interprets 0x20 (space), 0x09 (tab), 0x0A (line feed) and 0x0D (carriage return) as whitespace.
	 * Lax mode uses {@link Character#isWhitespace(int)} to make the determination.
	 *
	 * @param cp The codepoint.
	 * @return <jk>true</jk> if the specified character is whitespace.
	 */
	protected final boolean isWhitespace(int cp) {
		if (isStrict())
				return cp <= 0x20 && (cp == 0x09 || cp == 0x0A || cp == 0x0D || cp == 0x20);
		return Character.isWhitespace(cp);
	}

	/**
	 * Returns <jk>true</jk> if the specified character is whitespace or '/'.
	 *
	 * @param cp The codepoint.
	 * @return <jk>true</jk> if the specified character is whitespace or '/'.
	 */
	protected final boolean isCommentOrWhitespace(int cp) {
		if (cp == '/')
			return true;
		if (isStrict())
			return cp <= 0x20 && (cp == 0x09 || cp == 0x0A || cp == 0x0D || cp == 0x20);
		return Character.isWhitespace(cp);
	}

	@Override /* ParserSession */
	protected <T> T doParse(ParserPipe pipe, ClassMeta<T> type) throws Exception {
		ParserReader r = pipe.getParserReader();
		if (r == null)
			return null;
		T o = parseAnything(type, r, getOuter(), null);
		validateEnd(r);
		return o;
	}

	@Override /* ReaderParserSession */
	protected <K,V> Map<K,V> doParseIntoMap(ParserPipe pipe, Map<K,V> m, Type keyType, Type valueType) throws Exception {
		ParserReader r = pipe.getParserReader();
		m = parseIntoMap2(r, m, (ClassMeta<K>)getClassMeta(keyType), (ClassMeta<V>)getClassMeta(valueType), null);
		validateEnd(r);
		return m;
	}

	@Override /* ReaderParserSession */
	protected <E> Collection<E> doParseIntoCollection(ParserPipe pipe, Collection<E> c, Type elementType) throws Exception {
		ParserReader r = pipe.getParserReader();
		c = parseIntoCollection2(r, c, getClassMeta(elementType), null);
		validateEnd(r);
		return c;
	}

	private <T> T parseAnything(ClassMeta<T> eType, ParserReader r, Object outer, BeanPropertyMeta pMeta) throws Exception {

		if (eType == null)
			eType = (ClassMeta<T>)object();
		PojoSwap<T,Object> swap = (PojoSwap<T,Object>)eType.getPojoSwap(this);
		ClassMeta<?> sType = swap == null ? eType : swap.getSwapClassMeta(this);
		setCurrentClass(sType);

		Object o = null;

		skipCommentsAndSpace(r);
		int c = r.peek();
		if (c == -1) {
			if (isStrict())
				throw new ParseException(r.getLocation(this), "Empty input.");
			// Let o be null.
		} else if ((c == ',' || c == '}' || c == ']')) {
			if (isStrict())
				throw new ParseException(r.getLocation(this), "Missing value detected.");
			// Handle bug in Cognos 10.2.1 that can product non-existent values.
			// Let o be null;
		} else if (c == 'n') {
			parseKeyword("null", r);
		} else if (sType.isObject()) {
			if (c == '{') {
				ObjectMap m2 = new ObjectMap(this);
				parseIntoMap2(r, m2, string(), object(), pMeta);
				o = cast(m2, pMeta, eType);
			} else if (c == '[') {
				o = parseIntoCollection2(r, new ObjectList(this), object(), pMeta);
			} else if (c == '\'' || c == '"') {
				o = parseString(r);
				if (sType.isChar())
					o = o.toString().charAt(0);
			} else if (c >= '0' && c <= '9' || c == '-' || c == '.') {
				o = parseNumber(r, null);
			} else if (c == 't') {
				parseKeyword("true", r);
				o = Boolean.TRUE;
			} else {
				parseKeyword("false", r);
				o = Boolean.FALSE;
			}
		} else if (sType.isBoolean()) {
			o = parseBoolean(r);
		} else if (sType.isCharSequence()) {
			o = parseString(r);
		} else if (sType.isChar()) {
			o = parseString(r).charAt(0);
		} else if (sType.isNumber()) {
			o = parseNumber(r, (Class<? extends Number>)sType.getInnerClass());
		} else if (sType.isMap()) {
			Map m = (sType.canCreateNewInstance(outer) ? (Map)sType.newInstance(outer) : new ObjectMap(this));
			o = parseIntoMap2(r, m, sType.getKeyType(), sType.getValueType(), pMeta);
		} else if (sType.isCollection()) {
			if (c == '{') {
				ObjectMap m = new ObjectMap(this);
				parseIntoMap2(r, m, string(), object(), pMeta);
				o = cast(m, pMeta, eType);
			} else {
				Collection l = (sType.canCreateNewInstance(outer) ? (Collection)sType.newInstance() : new ObjectList(this));
				o = parseIntoCollection2(r, l, sType, pMeta);
			}
		} else if (sType.canCreateNewBean(outer)) {
			BeanMap m = newBeanMap(outer, sType.getInnerClass());
			o = parseIntoBeanMap2(r, m).getBean();
		} else if (sType.canCreateNewInstanceFromString(outer) && (c == '\'' || c == '"')) {
			o = sType.newInstanceFromString(outer, parseString(r));
		} else if (sType.canCreateNewInstanceFromNumber(outer) && isFirstNumberChar((char)c)) {
			o = sType.newInstanceFromNumber(this, outer, parseNumber(r, sType.getNewInstanceFromNumberClass()));
		} else if (sType.isArray() || sType.isArgs()) {
			if (c == '{') {
				ObjectMap m = new ObjectMap(this);
				parseIntoMap2(r, m, string(), object(), pMeta);
				o = cast(m, pMeta, eType);
			} else {
				ArrayList l = (ArrayList)parseIntoCollection2(r, new ArrayList(), sType, pMeta);
				o = toArray(sType, l);
			}
		} else if (c == '{') {
			Map m = new ObjectMap(this);
			parseIntoMap2(r, m, sType.getKeyType(), sType.getValueType(), pMeta);
			if (m.containsKey(getBeanTypePropertyName(eType)))
				o = cast((ObjectMap)m, pMeta, eType);
			else
				throw new ParseException(r.getLocation(this), "Class ''{0}'' could not be instantiated.  Reason: ''{1}''",
						sType.getInnerClass().getName(), sType.getNotABeanReason());
		} else if (sType.canCreateNewInstanceFromString(outer) && ! isStrict()) {
			o = sType.newInstanceFromString(outer, parseString(r));
		} else {
			throw new ParseException(r.getLocation(this), "Unrecognized syntax for class type ''{0}'', starting character ''{1}''",
				sType, (char)c);
		}

		if (swap != null && o != null)
			o = swap.unswap(this, o, eType);

		if (outer != null)
			setParent(eType, o, outer);

		return (T)o;
	}

	private Number parseNumber(ParserReader r, Class<? extends Number> type) throws Exception {
		int c = r.peek();
		if (c == '\'' || c == '"')
			return parseNumber(r, parseString(r), type);
		return parseNumber(r, parseNumberString(r), type);
	}

	private Number parseNumber(ParserReader r, String s, Class<? extends Number> type) throws Exception {

		// JSON has slightly different number rules from Java.
		// Strict mode enforces these different rules, lax does not.
		if (isStrict()) {

			// Lax allows blank strings to represent 0.
			// Strict does not allow blank strings.
			if (s.length() == 0)
				throw new ParseException(r.getLocation(this), "Invalid JSON number: ''{0}''", s);

			// Need to weed out octal and hexadecimal formats:  0123,-0123,0x123,-0x123.
			// Don't weed out 0 or -0.
			boolean isNegative = false;
			char c = s.charAt(0);
			if (c == '-') {
				isNegative = true;
				c = (s.length() == 1 ? 'x' : s.charAt(1));
			}

			// JSON doesn't allow '.123' and '-.123'.
			if (c == '.')
				throw new ParseException(loc(r), "Invalid JSON number: ''{0}''", s);

			// '01' is not a valid number, but '0.1', '0e1', '0e+1' are valid.
			if (c == '0' && s.length() > (isNegative ? 2 : 1)) {
				char c2 = s.charAt((isNegative ? 2 : 1));
				if (c2 != '.' && c2 != 'e' && c2 != 'E')
					throw new ParseException(loc(r), "Invalid JSON number: ''{0}''", s);
			}

			// JSON doesn't allow '1.' or '0.e1'.
			int i = s.indexOf('.');
			if (i != -1 && (s.length() == (i+1) || ! decChars.contains(s.charAt(i+1))))
				throw new ParseException(loc(r), "Invalid JSON number: ''{0}''", s);

		}
		return StringUtils.parseNumber(s, type);
	}

	private Boolean parseBoolean(ParserReader r) throws Exception {
		int c = r.peek();
		if (c == '\'' || c == '"')
			return Boolean.valueOf(parseString(r));
		if (c == 't') {
			parseKeyword("true", r);
			return Boolean.TRUE;
		}
		parseKeyword("false", r);
		return Boolean.FALSE;
	}


	private <K,V> Map<K,V> parseIntoMap2(ParserReader r, Map<K,V> m, ClassMeta<K> keyType,
			ClassMeta<V> valueType, BeanPropertyMeta pMeta) throws Exception {

		if (keyType == null)
			keyType = (ClassMeta<K>)string();

		int S0=0; // Looking for outer {
		int S1=1; // Looking for attrName start.
		int S3=3; // Found attrName end, looking for :.
		int S4=4; // Found :, looking for valStart: { [ " ' LITERAL.
		int S5=5; // Looking for , or }
		int S6=6; // Found , looking for attr start.

		int state = S0;
		String currAttr = null;
		int c = 0;
		while (c != -1) {
			c = r.read();
			if (state == S0) {
				if (c == '{')
					state = S1;
			} else if (state == S1) {
				if (c == '}') {
					return m;
				} else if (isCommentOrWhitespace(c)) {
					skipCommentsAndSpace(r.unread());
				} else {
					currAttr = parseFieldName(r.unread());
					state = S3;
				}
			} else if (state == S3) {
				if (c == ':')
					state = S4;
			} else if (state == S4) {
				if (isCommentOrWhitespace(c)) {
					skipCommentsAndSpace(r.unread());
				} else {
					K key = convertAttrToType(m, currAttr, keyType);
					V value = parseAnything(valueType, r.unread(), m, pMeta);
					setName(valueType, value, key);
					m.put(key, value);
					state = S5;
				}
			} else if (state == S5) {
				if (c == ',')
					state = S6;
				else if (isCommentOrWhitespace(c))
					skipCommentsAndSpace(r.unread());
				else if (c == '}') {
					return m;
				} else {
					break;
				}
			} else if (state == S6) {
				if (c == '}') {
					break;
				} else if (isCommentOrWhitespace(c)) {
					skipCommentsAndSpace(r.unread());
				} else {
					currAttr = parseFieldName(r.unread());
					state = S3;
				}
			}
		}
		if (state == S0)
			throw new ParseException(loc(r), "Expected '{' at beginning of JSON object.");
		if (state == S1)
			throw new ParseException(loc(r), "Could not find attribute name on JSON object.");
		if (state == S3)
			throw new ParseException(loc(r), "Could not find ':' following attribute name on JSON object.");
		if (state == S4)
			throw new ParseException(loc(r), "Expected one of the following characters: {,[,',\",LITERAL.");
		if (state == S5)
			throw new ParseException(loc(r), "Could not find '}' marking end of JSON object.");
		if (state == S6)
			throw new ParseException(loc(r), "Unexpected '}' found in JSON object.");

		return null; // Unreachable.
	}

	/*
	 * Parse a JSON attribute from the character array at the specified position, then
	 * set the position marker to the last character in the field name.
	 */
	private String parseFieldName(ParserReader r) throws Exception {
		int c = r.peek();
		if (c == '\'' || c == '"')
			return parseString(r);
		if (isStrict())
			throw new ParseException(loc(r), "Unquoted attribute detected.");
		r.mark();
		// Look for whitespace.
		while (c != -1) {
			c = r.read();
			if (c == ':' || isWhitespace(c) || c == '/') {
				r.unread();
				String s = r.getMarked().intern();
				return s.equals("null") ? null : s;
			}
		}
		throw new ParseException(loc(r), "Could not find the end of the field name.");
	}

	private <E> Collection<E> parseIntoCollection2(ParserReader r, Collection<E> l,
			ClassMeta<?> type, BeanPropertyMeta pMeta) throws Exception {

		int S0=0; // Looking for outermost [
		int S1=1; // Looking for starting [ or { or " or ' or LITERAL or ]
		int S2=2; // Looking for , or ]
		int S3=3; // Looking for starting [ or { or " or ' or LITERAL

		int argIndex = 0;

		int state = S0;
		int c = 0;
		while (c != -1) {
			c = r.read();
			if (state == S0) {
				if (c == '[')
					state = S1;
			} else if (state == S1) {
				if (c == ']') {
					return l;
				} else if (isCommentOrWhitespace(c)) {
					skipCommentsAndSpace(r.unread());
				} else if (c != -1) {
					l.add((E)parseAnything(type.isArgs() ? type.getArg(argIndex++) : type.getElementType(), r.unread(), l, pMeta));
					state = S2;
				}
			} else if (state == S2) {
				if (c == ',') {
					state = S3;
				} else if (isCommentOrWhitespace(c)) {
					skipCommentsAndSpace(r.unread());
				} else if (c == ']') {
					return l;
				} else {
					break;  // Invalid character found.
				}
			} else if (state == S3) {
				if (isCommentOrWhitespace(c)) {
					skipCommentsAndSpace(r.unread());
				} else if (c == ']') {
					break;
				} else if (c != -1) {
					l.add((E)parseAnything(type.isArgs() ? type.getArg(argIndex++) : type.getElementType(), r.unread(), l, pMeta));
					state = S2;
				}
			}
		}
		if (state == S0)
			throw new ParseException(loc(r), "Expected '[' at beginning of JSON array.");
		if (state == S1)
			throw new ParseException(loc(r), "Expected one of the following characters: {,[,',\",LITERAL.");
		if (state == S2)
			throw new ParseException(loc(r), "Expected ',' or ']'.");
		if (state == S3)
			throw new ParseException(loc(r), "Unexpected trailing comma in array.");

		return null;  // Unreachable.
	}

	private <T> BeanMap<T> parseIntoBeanMap2(ParserReader r, BeanMap<T> m) throws Exception {

		int S0=0; // Looking for outer {
		int S1=1; // Looking for attrName start.
		int S3=3; // Found attrName end, looking for :.
		int S4=4; // Found :, looking for valStart: { [ " ' LITERAL.
		int S5=5; // Looking for , or }

		int state = S0;
		String currAttr = "";
		int c = 0;
		int currAttrLine = -1, currAttrCol = -1;
		while (c != -1) {
			c = r.read();
			if (state == S0) {
				if (c == '{')
					state = S1;
			} else if (state == S1) {
				if (c == '}') {
					return m;
				} else if (isCommentOrWhitespace(c)) {
					skipCommentsAndSpace(r.unread());
				} else {
					r.unread();
					currAttrLine= r.getLine();
					currAttrCol = r.getColumn();
					currAttr = parseFieldName(r);
					state = S3;
				}
			} else if (state == S3) {
				if (c == ':')
					state = S4;
			} else if (state == S4) {
				if (isCommentOrWhitespace(c)) {
					skipCommentsAndSpace(r.unread());
				} else {
					if (! currAttr.equals(getBeanTypePropertyName(m.getClassMeta()))) {
						BeanPropertyMeta pMeta = m.getPropertyMeta(currAttr);
						setCurrentProperty(pMeta);
						if (pMeta == null) {
							onUnknownProperty(r.getPipe(), currAttr, m, currAttrLine, currAttrCol);
							parseAnything(object(), r.unread(), m.getBean(false), null); // Read content anyway to ignore it
						} else {
							ClassMeta<?> cm = pMeta.getClassMeta();
							Object value = parseAnything(cm, r.unread(), m.getBean(false), pMeta);
							setName(cm, value, currAttr);
							pMeta.set(m, currAttr, value);
						}
						setCurrentProperty(null);
					}
					state = S5;
				}
			} else if (state == S5) {
				if (c == ',')
					state = S1;
				else if (isCommentOrWhitespace(c))
					skipCommentsAndSpace(r.unread());
				else if (c == '}') {
					return m;
				}
			}
		}
		if (state == S0)
			throw new ParseException(loc(r), "Expected '{' at beginning of JSON object.");
		if (state == S1)
			throw new ParseException(loc(r), "Could not find attribute name on JSON object.");
		if (state == S3)
			throw new ParseException(loc(r), "Could not find ':' following attribute name on JSON object.");
		if (state == S4)
			throw new ParseException(loc(r), "Expected one of the following characters: {,[,',\",LITERAL.");
		if (state == S5)
			throw new ParseException(loc(r), "Could not find '}' marking end of JSON object.");

		return null; // Unreachable.
	}

	/*
	 * Starting from the specified position in the character array, returns the
	 * position of the character " or '.
	 * If the string consists of a concatenation of strings (e.g. 'AAA' + "BBB"), this method
	 * will automatically concatenate the strings and return the result.
	 */
	private String parseString(ParserReader r) throws Exception  {
		r.mark();
		int qc = r.read();		// The quote character being used (" or ')
		if (qc != '"' && isStrict()) {
			String msg = (
				qc == '\''
				? "Invalid quote character \"{0}\" being used."
				: "Did not find quote character marking beginning of string.  Character=\"{0}\""
			);
			throw new ParseException(loc(r), msg, (char)qc);
		}
		final boolean isQuoted = (qc == '\'' || qc == '"');
		String s = null;
		boolean isInEscape = false;
		int c = 0;
		while (c != -1) {
			c = r.read();
			// Strict syntax requires that all control characters be escaped.
			if (isStrict() && c <= 0x1F)
				throw new ParseException("Unescaped control character encountered: ''0x{0}''", String.format("%04X", c));
			if (isInEscape) {
				switch (c) {
					case 'n': r.replace('\n'); break;
					case 'r': r.replace('\r'); break;
					case 't': r.replace('\t'); break;
					case 'f': r.replace('\f'); break;
					case 'b': r.replace('\b'); break;
					case '\\': r.replace('\\'); break;
					case '/': r.replace('/'); break;
					case '\'': r.replace('\''); break;
					case '"': r.replace('"'); break;
					case 'u': {
						String n = r.read(4);
						try {
							r.replace(Integer.parseInt(n, 16), 6);
						} catch (NumberFormatException e) {
							throw new ParseException(loc(r), "Invalid Unicode escape sequence in string.");
						}
						break;
					}
					default:
						throw new ParseException(loc(r), "Invalid escape sequence in string.");
				}
				isInEscape = false;
			} else {
				if (c == '\\') {
					isInEscape = true;
					r.delete();
				} else if (isQuoted) {
					if (c == qc) {
						s = r.getMarked(1, -1);
						break;
					}
				} else {
					if (c == ',' || c == '}' || c == ']' || isWhitespace(c)) {
						s = r.getMarked(0, -1);
						r.unread();
						break;
					} else if (c == -1) {
						s = r.getMarked(0, 0);
						break;
					}
				}
			}
		}
		if (s == null)
			throw new ParseException(loc(r), "Could not find expected end character ''{0}''.", (char)qc);

		// Look for concatenated string (i.e. whitespace followed by +).
		skipCommentsAndSpace(r);
		if (r.peek() == '+') {
			if (isStrict())
				throw new ParseException(loc(r), "String concatenation detected.");
			r.read();	// Skip past '+'
			skipCommentsAndSpace(r);
			s += parseString(r);
		}
		return trim(s); // End of input reached.
	}

	/*
	 * Looks for the keywords true, false, or null.
	 * Throws an exception if any of these keywords are not found at the specified position.
	 */
	private void parseKeyword(String keyword, ParserReader r) throws Exception {
		try {
			String s = r.read(keyword.length());
			if (s.equals(keyword))
				return;
			throw new ParseException(loc(r), "Unrecognized syntax.");
		} catch (IndexOutOfBoundsException e) {
			throw new ParseException(loc(r), "Unrecognized syntax.");
		}
	}

	/*
	 * Doesn't actually parse anything, but moves the position beyond any whitespace or comments.
	 * If positionOnNext is 'true', then the cursor will be set to the point immediately after
	 * the comments and whitespace.  Otherwise, the cursor will be set to the last position of
	 * the comments and whitespace.
	 */
	private void skipCommentsAndSpace(ParserReader r) throws Exception {
		int c = 0;
		while ((c = r.read()) != -1) {
			if (! isWhitespace(c)) {
				if (c == '/') {
					if (isStrict())
						throw new ParseException(loc(r), "Javascript comment detected.");
					skipComments(r);
				} else {
					r.unread();
					return;
				}
			}
		}
	}

	/*
	 * Doesn't actually parse anything, but when positioned at the beginning of comment,
	 * it will move the pointer to the last character in the comment.
	 */
	private void skipComments(ParserReader r) throws ParseException, IOException {
		int c = r.read();
		//  "/* */" style comments
		if (c == '*') {
			while (c != -1)
				if ((c = r.read()) == '*')
					if ((c = r.read()) == '/')
						return;
		//  "//" style comments
		} else if (c == '/') {
			while (c != -1) {
				c = r.read();
				if (c == -1 || c == '\n')
					return;
			}
		}
		throw new ParseException(loc(r), "Open ended comment.");
	}

	/*
	 * Call this method after you've finished a parsing a string to make sure that if there's any
	 * remainder in the input, that it consists only of whitespace and comments.
	 */
	private void validateEnd(ParserReader r) throws Exception {
		skipCommentsAndSpace(r);
		int c = r.read();
		if (c != -1 && c != ';')  // var x = {...}; expressions can end with a semicolon.
			throw new ParseException(loc(r), "Remainder after parse: ''{0}''.", (char)c);
	}

	private ObjectMap loc(ParserReader r) {
		return getLastLocation().append("line", r.getLine()).append("column", r.getColumn());
	}
}
