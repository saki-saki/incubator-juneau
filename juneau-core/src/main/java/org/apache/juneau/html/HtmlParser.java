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
package org.apache.juneau.html;

import static javax.xml.stream.XMLStreamConstants.*;
import static org.apache.juneau.html.HtmlParser.Tag.*;
import static org.apache.juneau.internal.StringUtils.*;

import java.lang.reflect.*;
import java.util.*;

import javax.xml.namespace.*;
import javax.xml.stream.*;
import javax.xml.stream.events.*;

import org.apache.juneau.*;
import org.apache.juneau.annotation.*;
import org.apache.juneau.parser.*;
import org.apache.juneau.transform.*;

/**
 * Parses text generated by the {@link HtmlSerializer} class back into a POJO model.
 *
 *
 * <h6 class='topic'>Media types</h6>
 * <p>
 * 	Handles <code>Content-Type</code> types: <code>text/html</code>
 *
 *
 * <h6 class='topic'>Description</h6>
 * <p>
 * 	See the {@link HtmlSerializer} class for a description of the HTML generated.
 * <p>
 * 	This class is used primarily for automated testing of the {@link HtmlSerializer} class.
 *
 *
 * <h6 class='topic'>Configurable properties</h6>
 * <p>
 * 	This class has the following properties associated with it:
 * <ul>
 * 	<li>{@link HtmlSerializerContext}
 * </ul>
 *
 *
 * @author James Bognar (james.bognar@salesforce.com)
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
@Consumes({"text/html","text/html+stripped"})
public final class HtmlParser extends ReaderParser {

	/** Default parser, all default settings.*/
	public static final HtmlParser DEFAULT = new HtmlParser().lock();

	/*
	 * Reads anything starting at the current event.
	 * <p>
	 * 	Precondition:  Must be pointing at START_ELEMENT or CHARACTERS event.
	 * 	Postcondition:  Pointing at next event to be processed.
	 */
	private <T> T parseAnything(HtmlParserSession session, ClassMeta<T> eType, XMLEventReader r, Object outer, BeanPropertyMeta pMeta) throws Exception {

		BeanContext bc = session.getBeanContext();
		if (eType == null)
			eType = (ClassMeta<T>)object();
		PojoSwap<T,Object> transform = (PojoSwap<T,Object>)eType.getPojoSwap();
		ClassMeta<?> sType = eType.getSerializedClassMeta();
		session.setCurrentClass(sType);
		BeanDictionary bd = (pMeta == null ? bc.getBeanDictionary() : pMeta.getBeanDictionary());

		Object o = null;

		XMLEvent event = r.nextEvent();
		while (! (event.isStartElement() || (event.isCharacters() && ! event.asCharacters().isWhiteSpace()) || event.isEndDocument()))
			event = r.nextEvent();

		if (event.isEndDocument())
			throw new XMLStreamException("Unexpected end of stream in parseAnything for type '"+eType+"'", event.getLocation());

		if (event.isCharacters()) {
			String text = parseCharacters(event, r);
			if (sType.isObject())
				o = text;
			else if (sType.isCharSequence())
				o = text;
			else if (sType.isNumber())
				o = parseNumber(text, (Class<? extends Number>)eType.getInnerClass());
			else if (sType.isChar())
				o = text.charAt(0);
			else if (sType.isBoolean())
				o = Boolean.parseBoolean(text);
			else if (sType.canCreateNewInstanceFromString(outer))
				o = sType.newInstanceFromString(outer, text);
			else if (sType.canCreateNewInstanceFromNumber(outer))
				o = sType.newInstanceFromNumber(outer, parseNumber(text, sType.getNewInstanceFromNumberClass()));
			else
				throw new XMLStreamException("Unexpected characters '"+event.asCharacters().getData()+"' for type '"+eType+"'", event.getLocation());

		} else {
			Tag tag = Tag.forString(event.asStartElement().getName().getLocalPart(), false);

			// The _type attribute can be any of the following:
			// "object" - A map.
			// "array" - An array.
			// "X" - A bean type name as defined through @Bean.typeName().

			String typeName = "object";
			String text = "";

			if (tag.isOneOf(STRING, NUMBER, BOOLEAN, BR, FF, BS, TB))
				text = parseCharacters(event, r);

			if (tag == TABLE) {
				Map<String,String> attrs = getAttributes(event);
				typeName = attrs.get(bc.getBeanTypePropertyName());
				if (bd.hasName(typeName))
					sType = eType = (ClassMeta<T>)bd.getClassMeta(typeName);
				// Reset to "object" if it was a bean type name.
				if (! typeName.equals("array"))
					typeName = "object";
			}

			boolean isValid = true;

			if (tag == NULL)
				nextTag(r, xNULL);
			else if (tag == A)
				o = parseAnchor(session, event, r, eType);
			else if (sType.isObject()) {
				if (tag == STRING)
					o = text;
				else if (tag == NUMBER)
					o = parseNumber(text, null);
				else if (tag == BOOLEAN)
					o = Boolean.parseBoolean(text);
				else if (tag == TABLE) {
					if (typeName.equals("object")) {
						o = parseIntoMap(session, r, (Map)new ObjectMap(bc), sType.getKeyType(), sType.getValueType(), pMeta);
					} else if (typeName.equals("array")) {
						o = parseTableIntoCollection(session, r, (Collection)new ObjectList(bc), sType.getElementType(), pMeta);
					} else
						isValid = false;
				}
				else if (tag == UL)
					o = parseIntoCollection(session, r, new ObjectList(bc), null, pMeta);
			}
			else if (tag == STRING && sType.isCharSequence())
				o = text;
			else if (tag == STRING && sType.isChar())
				o = text.charAt(0);
			else if (tag == STRING && sType.canCreateNewInstanceFromString(outer))
				o = sType.newInstanceFromString(outer, text);
			else if (tag == NUMBER && sType.isNumber())
				o = parseNumber(text, (Class<? extends Number>)sType.getInnerClass());
			else if (tag == NUMBER && sType.canCreateNewInstanceFromNumber(outer))
				o = sType.newInstanceFromNumber(outer, parseNumber(text, sType.getNewInstanceFromNumberClass()));
			else if (tag == BOOLEAN && sType.isBoolean())
				o = Boolean.parseBoolean(text);
			else if (tag == TABLE) {
				if (typeName.equals("object")) {
					if (sType.isMap()) {
						o = parseIntoMap(session, r, (Map)(sType.canCreateNewInstance(outer) ? sType.newInstance(outer) : new ObjectMap(bc)), sType.getKeyType(), sType.getValueType(), pMeta);
					} else if (sType.canCreateNewInstanceFromObjectMap(outer)) {
						ObjectMap m = new ObjectMap(bc);
						parseIntoMap(session, r, m, string(), object(), pMeta);
						o = sType.newInstanceFromObjectMap(outer, m);
					} else if (sType.canCreateNewBean(outer)) {
						BeanMap m = bc.newBeanMap(outer, sType.getInnerClass());
						o = parseIntoBean(session, r, m).getBean();
					}
					else
						isValid = false;
				} else if (typeName.equals("array")) {
					if (sType.isCollection())
						o = parseTableIntoCollection(session, r, (Collection)(sType.canCreateNewInstance(outer) ? sType.newInstance(outer) : new ObjectList(bc)), sType.getElementType(), pMeta);
					else if (sType.isArray())
						o = bc.toArray(sType, parseTableIntoCollection(session, r, new ArrayList(), sType.getElementType(), pMeta));
					else
						isValid = false;
				} else
					isValid = false;
			} else if (tag == UL) {
				if (sType.isCollection())
					o = parseIntoCollection(session, r, (Collection)(sType.canCreateNewInstance(outer) ? sType.newInstance(outer) : new ObjectList(bc)), sType.getElementType(), pMeta);
				else if (sType.isArray())
					o = bc.toArray(sType, parseIntoCollection(session, r, new ArrayList(), sType.getElementType(), pMeta));
				else
					isValid = false;
			} else
				isValid = false;

			if (! isValid)
				throw new XMLStreamException("Unexpected tag '"+tag+"' for type '"+eType+"'", event.getLocation());
		}


		if (transform != null && o != null)
			o = transform.unswap(o, eType, bc);

		if (outer != null)
			setParent(eType, o, outer);

		return (T)o;
	}

	/*
	 * Reads an anchor tag and converts it into a bean.
	 */
	private <T> T parseAnchor(HtmlParserSession session, XMLEvent e, XMLEventReader r, ClassMeta<T> beanType) throws XMLStreamException {
		BeanContext bc = session.getBeanContext();
		String href = e.asStartElement().getAttributeByName(new QName("href")).getValue();
		String name = parseCharacters(e, r);
		Class<T> beanClass = beanType.getInnerClass();
		if (beanClass.isAnnotationPresent(HtmlLink.class)) {
			HtmlLink h = beanClass.getAnnotation(HtmlLink.class);
			BeanMap<T> m = bc.newBeanMap(beanClass);
			m.put(h.hrefProperty(), href);
			m.put(h.nameProperty(), name);
			return m.getBean();
		}
		return bc.convertToType(href, beanType);
	}

	private Map<String,String> getAttributes(XMLEvent e) {
		Map<String,String> m = new TreeMap<String,String>() ;
		for (Iterator i = e.asStartElement().getAttributes(); i.hasNext();) {
			Attribute a = (Attribute)i.next();
			m.put(a.getName().getLocalPart(), a.getValue());
		}
		return m;
	}

	/*
	 * Reads contents of <table> element.
	 * Precondition:  Must be pointing at <table> event.
	 * Postcondition:  Pointing at next START_ELEMENT or END_DOCUMENT event.
	 */
	private <K,V> Map<K,V> parseIntoMap(HtmlParserSession session, XMLEventReader r, Map<K,V> m, ClassMeta<K> keyType, ClassMeta<V> valueType, BeanPropertyMeta pMeta) throws Exception {
		Tag tag = nextTag(r, TR);

		// Skip over the column headers.
		nextTag(r, TH);
		parseElementText(r, xTH);
		nextTag(r, TH);
		parseElementText(r, xTH);
		nextTag(r, xTR);

		while (true) {
			tag = nextTag(r, TR, xTABLE);
			if (tag == xTABLE)
				break;
			nextTag(r, TD);
			K key = parseAnything(session, keyType, r, m, pMeta);
			nextTag(r, xTD);
			nextTag(r, TD);
			V value = parseAnything(session, valueType, r, m, pMeta);
			setName(valueType, value, key);
			m.put(key, value);
			nextTag(r, xTD);
			nextTag(r, xTR);
		}

		return m;
	}

	/*
	 * Reads contents of <ul> element.
	 * Precondition:  Must be pointing at event following <ul> event.
	 * Postcondition:  Pointing at next START_ELEMENT or END_DOCUMENT event.
	 */
	private <E> Collection<E> parseIntoCollection(HtmlParserSession session, XMLEventReader r, Collection<E> l, ClassMeta<E> elementType, BeanPropertyMeta pMeta) throws Exception {
		while (true) {
			Tag tag = nextTag(r, LI, xUL);
			if (tag == xUL)
				break;
			l.add(parseAnything(session, elementType, r, l, pMeta));
			nextTag(r, xLI);
		}
		return l;
	}

	/*
	 * Reads contents of <ul> element into an Object array.
	 * Precondition:  Must be pointing at event following <ul> event.
	 * Postcondition:  Pointing at next START_ELEMENT or END_DOCUMENT event.
	 */
	private Object[] parseArgs(HtmlParserSession session, XMLEventReader r, ClassMeta<?>[] argTypes) throws Exception {
		Object[] o = new Object[argTypes.length];
		int i = 0;
		while (true) {
			Tag tag = nextTag(r, LI, xUL);
			if (tag == xUL)
				break;
			o[i] = parseAnything(session, argTypes[i], r, session.getOuter(), null);
			i++;
			nextTag(r, xLI);
		}
		return o;
	}

	/*
	 * Reads contents of <ul> element.
	 * Precondition:  Must be pointing at event following <ul> event.
	 * Postcondition:  Pointing at next START_ELEMENT or END_DOCUMENT event.
	 */
	private <E> Collection<E> parseTableIntoCollection(HtmlParserSession session, XMLEventReader r, Collection<E> l, ClassMeta<E> elementType, BeanPropertyMeta pMeta) throws Exception {

		BeanContext bc = session.getBeanContext();
		if (elementType == null)
			elementType = (ClassMeta<E>)object();
		BeanDictionary bd = (pMeta == null ? bc.getBeanDictionary() : pMeta.getBeanDictionary());

		Tag tag = nextTag(r, TR);
		List<String> keys = new ArrayList<String>();
		while (true) {
			tag = nextTag(r, TH, xTR);
			if (tag == xTR)
				break;
			keys.add(parseElementText(r, xTH));
		}

		while (true) {
			XMLEvent event = r.nextTag();
			tag = Tag.forEvent(event);
			if (tag == xTABLE)
				break;
			if (elementType.canCreateNewBean(l)) {
				BeanMap m = bc.newBeanMap(l, elementType.getInnerClass());
				for (int i = 0; i < keys.size(); i++) {
					tag = nextTag(r, TD, NULL);
					if (tag == NULL) {
						m = null;
						nextTag(r, xNULL);
						break;
					}
					String key = keys.get(i);
					BeanMapEntry e = m.getProperty(key);
					if (e == null) {
						//onUnknownProperty(key, m, -1, -1);
						parseAnything(session, object(), r, l, null);
					} else {
						BeanPropertyMeta bpm = e.getMeta();
						ClassMeta<?> cm = bpm.getClassMeta();
						Object value = parseAnything(session, cm, r, m.getBean(false), bpm);
						setName(cm, value, key);
						bpm.set(m, value);
					}
					nextTag(r, xTD);
				}
				l.add(m == null ? null : (E)m.getBean());
			} else {
				String c = getAttributes(event).get(bc.getBeanTypePropertyName());
				Map m = (Map)(elementType.isMap() && elementType.canCreateNewInstance(l) ? elementType.newInstance(l) : new ObjectMap(bc));
				for (int i = 0; i < keys.size(); i++) {
					tag = nextTag(r, TD, NULL);
					if (tag == NULL) {
						m = null;
						nextTag(r, xNULL);
						break;
					}
					String key = keys.get(i);
					if (m != null) {
						ClassMeta<?> et = elementType.getElementType();
						Object value = parseAnything(session, et, r, l, pMeta);
						setName(et, value, key);
						m.put(key, value);
					}
					nextTag(r, xTD);
				}
				if (m != null && c != null) {
					ObjectMap m2 = (m instanceof ObjectMap ? (ObjectMap)m : new ObjectMap(m).setBeanContext(session.getBeanContext()));
					m2.put(bc.getBeanTypePropertyName(), c);
					l.add((E)bd.cast(m2));
				} else {
					l.add((E)m);
				}
			}
			nextTag(r, xTR);
		}
		return l;
	}

	/*
	 * Reads contents of <table> element.
	 * Precondition:  Must be pointing at event following <table> event.
	 * Postcondition:  Pointing at next START_ELEMENT or END_DOCUMENT event.
	 */
	private <T> BeanMap<T> parseIntoBean(HtmlParserSession session, XMLEventReader r, BeanMap<T> m) throws Exception {
		Tag tag = nextTag(r, TR);

		// Skip over the column headers.
		nextTag(r, TH);
		parseElementText(r, xTH);
		nextTag(r, TH);
		parseElementText(r, xTH);
		nextTag(r, xTR);

		while (true) {
			tag = nextTag(r, TR, xTABLE);
			if (tag == xTABLE)
				break;
			nextTag(r, TD);
			String key = parseElementText(r, xTD);
			nextTag(r, TD);
			BeanPropertyMeta pMeta = m.getPropertyMeta(key);
			if (pMeta == null) {
				if (m.getMeta().isSubTyped()) {
					Object value = parseAnything(session, object(), r, m.getBean(false), null);
					m.put(key, value);
				} else {
					onUnknownProperty(session, key, m, -1, -1);
					parseAnything(session, object(), r, null, null);
				}
			} else {
				ClassMeta<?> cm = pMeta.getClassMeta();
				Object value = parseAnything(session, cm, r, m.getBean(false), pMeta);
				setName(cm, value, key);
				pMeta.set(m, value);
			}
			nextTag(r, xTD);
			nextTag(r, xTR);
		}
		return m;
	}

	/*
	 * Parse until the next event is an end tag.
	 */
	private String parseCharacters(XMLEvent e, XMLEventReader r) throws XMLStreamException {

		List<String> strings = new LinkedList<String>();

		while (true) {
			int eventType = e.getEventType();
			if (eventType == CHARACTERS) {
				Characters c = e.asCharacters();
				if (! c.isWhiteSpace())
					strings.add(c.getData());
			}
			else if (eventType == START_ELEMENT) {
				Tag tag = Tag.forEvent(e);
				if (tag == BR)
					strings.add("\n");
				else if (tag == FF)
					strings.add("\f");
				else if (tag == BS)
					strings.add("\b");
				else if (tag == TB)
					strings.add("\t");
			}
			// Ignore all other elements.

			XMLEvent eNext = r.peek();

			if (eNext.isStartElement() || eNext.isEndElement()) {
				Tag tag = Tag.forEvent(eNext);
				if (! (tag.isOneOf(A, xA, BR, xBR, FF, xFF, BS, xBS, TB, xTB, STRING, xSTRING, NUMBER, xNUMBER, BOOLEAN, xBOOLEAN)))
					return trim(join(strings));
			} else if (eNext.isEndDocument()) {
				return trim(join(strings));
			}

			e = r.nextEvent();
		}
	}

	private String trim(String s) {
		int i2 = 0, i3;
		for (i2 = 0; i2 < s.length(); i2++) {
			char c = s.charAt(i2);
			if (c != ' ')
				break;
		}
		for (i3 = s.length(); i3 > i2; i3--) {
			char c = s.charAt(i3-1);
			if (c != ' ')
				break;
		}
		return s.substring(i2, i3);
	}

	/*
	 * Reads the element text of the current element, accounting for <a> and <br> tags. <br>
	 * Precondition:  Must be pointing at first event AFTER the start tag.
	 * Postcondition:  Pointing at next START_ELEMENT or END_DOCUMENT event.
	 */
	private String parseElementText(XMLEventReader r, Tag endTag) throws XMLStreamException {

		List<String> strings = new LinkedList<String>();

		XMLEvent e = r.nextEvent();
		Tag nTag = (e.isEndElement() ? Tag.forEvent(e) : null);

		while (nTag != endTag) {
			if (e.isCharacters())
				strings.add(parseCharacters(e, r));
			e = r.nextEvent();

			if (e.getEventType() == END_ELEMENT)
				nTag = Tag.forEvent(e);

			if (nTag == endTag)
				return join(strings);
		}

		return "";
	}

	enum Tag {

		TABLE(1,"<table>"),
		TR(2,"<tr>"),
		TH(3,"<th>"),
		TD(4,"<td>"),
		UL(5,"<ul>"),
		LI(6,"<li>"),
		STRING(7,"<string>"),
		NUMBER(8,"<number>"),
		BOOLEAN(9,"<boolean>"),
		NULL(10,"<null>"),
		A(11,"<a>"),
		BR(12,"<br>"),		// newline
		FF(13,"<ff>"),		// formfeed
		BS(14,"<bs>"),		// backspace
		TB(15,"<tb>"),		// tab
		xTABLE(-1,"</table>"),
		xTR(-2,"</tr>"),
		xTH(-3,"</th>"),
		xTD(-4,"</td>"),
		xUL(-5,"</ul>"),
		xLI(-6,"</li>"),
		xSTRING(-7,"</string>"),
		xNUMBER(-8,"</number>"),
		xBOOLEAN(-9,"</boolean>"),
		xNULL(-10,"</null>"),
		xA(-11,"</a>"),
		xBR(-12,"</br>"),
		xFF(-13,"</ff>"),
		xBS(-14,"</bs>"),
		xTB(-15,"</tb>");

		private Map<Integer,Tag> cache = new HashMap<Integer,Tag>();

		int id;
		String label;

		Tag(int id, String label) {
			this.id = id;
			this.label = label;
			cache.put(id, this);
		}

		static Tag forEvent(XMLEvent event) throws XMLStreamException {
			if (event.isStartElement())
				return forString(event.asStartElement().getName().getLocalPart(), false);
			else if (event.isEndElement())
				return forString(event.asEndElement().getName().getLocalPart(), true);
			throw new XMLStreamException("Invalid call to Tag.forEvent on event of type ["+event.getEventType()+"]");
		}

		private static Tag forString(String tag, boolean end) throws XMLStreamException {
			char c = tag.charAt(0);
			Tag t = null;
			if (c == 'u')
				t = (end ? xUL : UL);
			else if (c == 'l')
				t = (end ? xLI : LI);
			else if (c == 's')
				t = (end ? xSTRING : STRING);
			else if (c == 'b') {
				c = tag.charAt(1);
				if (c == 'o')
					t = (end ? xBOOLEAN : BOOLEAN);
				else if (c == 'r')
					t = (end ? xBR : BR);
				else if (c == 's')
					t = (end ? xBS : BS);
			}
			else if (c == 'a')
				t = (end ? xA : A);
			else if (c == 'n') {
				c = tag.charAt(2);
				if (c == 'm')
					t = (end ? xNUMBER : NUMBER);
				else if (c == 'l')
					t = (end ? xNULL : NULL);
			}
			else if (c == 't') {
				c = tag.charAt(1);
				if (c == 'a')
					t = (end ? xTABLE : TABLE);
				else if (c == 'r')
					t = (end ? xTR : TR);
				else if (c == 'h')
					t = (end ? xTH : TH);
				else if (c == 'd')
					t = (end ? xTD : TD);
				else if (c == 'b')
					t = (end ? xTB : TB);
			}
			else if (c == 'f')
				t = (end ? xFF : FF);
			if (t == null)
				throw new XMLStreamException("Unknown tag '"+tag+"' encountered");
			return t;
		}

		@Override /* Object */
		public String toString() {
			return label;
		}

		public boolean isOneOf(Tag...tags) {
			for (Tag tag : tags)
				if (tag == this)
					return true;
			return false;
		}
	}

	/*
	 * Reads the current tag.  Advances past anything that's not a start or end tag.  Throws an exception if
	 * 	it's not one of the expected tags.
	 * Precondition:  Must be pointing before the event we want to parse.
	 * Postcondition:  Pointing at the tag just parsed.
	 */
	private Tag nextTag(XMLEventReader r, Tag...expected) throws XMLStreamException {
		XMLEvent event = r.nextTag();
		Tag tag = Tag.forEvent(event);
		if (expected.length == 0)
			return tag;
		for (Tag t : expected)
			if (t == tag)
				return tag;
		throw new XMLStreamException("Unexpected tag: " + tag, event.getLocation());
	}

	private String join(List<String> s) {
		if (s.size() == 0)
			return "";
		if (s.size() == 1)
			return s.get(0);
		StringBuilder sb = new StringBuilder();
		for (String ss : s)
			sb.append(ss);
		return sb.toString();
	}

	//--------------------------------------------------------------------------------
	// Overridden methods
	//--------------------------------------------------------------------------------

	@Override /* Parser */
	public HtmlParserSession createSession(Object input, ObjectMap properties, Method javaMethod, Object outer) {
		return new HtmlParserSession(getContext(HtmlParserContext.class), getBeanContext(), input, properties, javaMethod, outer);
	}

	@Override /* Parser */
	protected <T> T doParse(ParserSession session, ClassMeta<T> type) throws Exception {
		type = session.getBeanContext().normalizeClassMeta(type);
		HtmlParserSession s = (HtmlParserSession)session;
		return parseAnything(s, type, s.getXmlEventReader(), session.getOuter(), null);
	}

	@Override /* ReaderParser */
	protected <K,V> Map<K,V> doParseIntoMap(ParserSession session, Map<K,V> m, Type keyType, Type valueType) throws Exception {
		HtmlParserSession s = (HtmlParserSession)session;
		return parseIntoMap(s, s.getXmlEventReader(), m, s.getBeanContext().getClassMeta(keyType), s.getBeanContext().getClassMeta(valueType), null);
	}

	@Override /* ReaderParser */
	protected <E> Collection<E> doParseIntoCollection(ParserSession session, Collection<E> c, Type elementType) throws Exception {
		HtmlParserSession s = (HtmlParserSession)session;
		return parseIntoCollection(s, s.getXmlEventReader(), c, s.getBeanContext().getClassMeta(elementType), null);
	}

	@Override /* ReaderParser */
	protected Object[] doParseArgs(ParserSession session, ClassMeta<?>[] argTypes) throws Exception {
		HtmlParserSession s = (HtmlParserSession)session;
		return parseArgs(s, s.getXmlEventReader(), argTypes);
	}

	@Override /* CoreApi */
	public HtmlParser setProperty(String property, Object value) throws LockedException {
		super.setProperty(property, value);
		return this;
	}

	@Override /* CoreApi */
	public HtmlParser setProperties(ObjectMap properties) throws LockedException {
		super.setProperties(properties);
		return this;
	}

	@Override /* CoreApi */
	public HtmlParser addNotBeanClasses(Class<?>...classes) throws LockedException {
		super.addNotBeanClasses(classes);
		return this;
	}

	@Override /* CoreApi */
	public HtmlParser addBeanFilters(Class<?>...classes) throws LockedException {
		super.addBeanFilters(classes);
		return this;
	}

	@Override /* CoreApi */
	public HtmlParser addPojoSwaps(Class<?>...classes) throws LockedException {
		super.addPojoSwaps(classes);
		return this;
	}

	@Override /* CoreApi */
	public HtmlParser addToDictionary(Class<?>...classes) throws LockedException {
		super.addToDictionary(classes);
		return this;
	}

	@Override /* CoreApi */
	public <T> HtmlParser addImplClass(Class<T> interfaceClass, Class<? extends T> implClass) throws LockedException {
		super.addImplClass(interfaceClass, implClass);
		return this;
	}

	@Override /* CoreApi */
	public HtmlParser setClassLoader(ClassLoader classLoader) throws LockedException {
		super.setClassLoader(classLoader);
		return this;
	}

	@Override /* Lockable */
	public HtmlParser lock() {
		super.lock();
		return this;
	}

	@Override /* Lockable */
	public HtmlParser clone() {
		try {
			return (HtmlParser)super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e); // Shouldn't happen
		}
	}
}
