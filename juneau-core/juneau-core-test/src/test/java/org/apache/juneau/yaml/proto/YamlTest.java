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

import org.junit.*;

@SuppressWarnings({"javadoc"})
public class YamlTest {

	//====================================================================================================
	// testBasic
	//====================================================================================================
	@Test
	public void testBasic() throws Exception {
//		Map<String,Object> m = new LinkedHashMap<String,Object>();
//		List<Object> l = new LinkedList<Object>();
//
//		WriterSerializer s1 = new YamlSerializerBuilder().simple().trimNullProperties(false).build();
//		WriterSerializer s2 = new YamlSerializerBuilder().simple().trimNullProperties(false).quoteChar('"').build();
//		String r;
//
//		// Null keys and values
//		m.clear();
//		m.put(null, null);
//		m.put("aaa", "bbb");
//		assertEquals("A1", "~: ~,\naaa: bbb\n", s1.serialize(m));
//
//		// Escapes.
//		// String = ["]
//		m.clear();
//		m.put("x", "[\"]");
//		assertEquals("{x:\"[\\\"]\"}", s2.serialize(m));
//		// String = [\"]
//		// JSON = {x:"\\\""}
//		m.clear();
//		m.put("x", "[\\\"]");
//		assertEquals("{x:\"[\\\\\\\"]\"}", s2.serialize(m));
//
//		// String = [\w[\w\-\.]{3,}\w]
//		// JSON = {x:"\\w[\\w\\-\\.]{3,}\\w"}
//		m.clear();
//		r = "\\w[\\w\\-\\.]{3,}\\w";
//		m.put("x", r);
//		assertEquals("{x:\"\\\\w[\\\\w\\\\-\\\\.]{3,}\\\\w\"}", s2.serialize(m));
//		assertEquals(r, new ObjectMap(s2.serialize(m)).getString("x"));
//
//		// String = [foo\bar]
//		// JSON = {x:"foo\\bar"}
//		m.clear();
//		m.put("x", "foo\\bar");
//		assertEquals("{x:\"foo\\\\bar\"}", s2.serialize(m));
//
//		m.clear();
//		m.put("null", null);
//		m.put("aaa", "bbb");
//		assertEquals("A2", "{'null':null,aaa:'bbb'}", s1.serialize(m));
//
//		m.clear();
//		m.put(null, "null");
//		m.put("aaa", "bbb");
//		assertEquals("A3", "{null:'null',aaa:'bbb'}", s1.serialize(m));
//
//		// Arrays
//		m.clear();
//		l.clear();
//		m.put("J", "f1");
//		m.put("B", "b");
//		m.put("C", "c");
//		l.add("1");
//		l.add("2");
//		l.add("3");
//		Object o = new Object[] { m, l };
//		Object o2 = new Object[] { o, "foo", "bar", new Integer(1), new Boolean(false), new Float(1.2), null };
//		assertEquals("K1", "[[{J:'f1',B:'b',C:'c'},['1','2','3']],'foo','bar',1,false,1.2,null]", s1.serialize(o2));
	}

//	@Test
//	public void testReservedKeywordAttributes() throws Exception {
//		Map<String,Object> m = new LinkedHashMap<String,Object>();
//
//		// Keys with reserved names.
//		for (String attr : new String[]{"","true","false","null","try","123","1x","-123",".123"}) {
//			m.clear();
//			m.put(attr,1);
//			assertObjectEquals("{'"+attr+"':1}", m);
//		}
//	}
//
//	//====================================================================================================
//	// Validate various backslashes in strings.
//	//====================================================================================================
//	@Test
//	public void testBackslashesInStrings() throws Exception {
//		YamlSerializer s = new YamlSerializerBuilder().simple().trimNullProperties(false).quoteChar('"').build();
//		String r, r2;
//
//		// [\\]
//		r = "\\";
//		r2 = s.serialize(r);
//		assertEquals(r2, "\"\\\\\"");
//		assertEquals(YamlParser.DEFAULT.parse(r2, Object.class), r);
//
//		// [\b\f\n\t]
//		r = "\b\f\n\t";
//		r2 = s.serialize(r);
//		assertEquals("\"\\b\\f\\n\\t\"", r2);
//		assertEquals(r, YamlParser.DEFAULT.parse(r2, Object.class));
//
//		// Special JSON case:  Forward slashes can OPTIONALLY be escaped.
//		// [\/]
//		assertEquals(YamlParser.DEFAULT.parse("\"\\/\"", Object.class), "/");
//
//		// Unicode
//		r = "\u1234\u1ABC\u1abc";
//		r2 = s.serialize(r);
//		assertEquals("\"\u1234\u1ABC\u1abc\"", r2);
//
//		assertEquals("\u1234", YamlParser.DEFAULT.parse("\"\\u1234\"", Object.class));
//	}
//
//	//====================================================================================================
//	// Indentation
//	//====================================================================================================
//	@Test
//	public void testIndentation() throws Exception {
//		ObjectMap m = new ObjectMap("{J:{B:['c',{D:'e'},['f',{G:'h'},1,false]]},I:'j'}");
//		String e = ""
//			+ "{"
//			+ "\n	J: {"
//			+ "\n		B: ["
//			+ "\n			'c',"
//			+ "\n			{"
//			+ "\n				D: 'e'"
//			+ "\n			},"
//			+ "\n			["
//			+ "\n				'f',"
//			+ "\n				{"
//			+ "\n					G: 'h'"
//			+ "\n				},"
//			+ "\n				1,"
//			+ "\n				false"
//			+ "\n			]"
//			+ "\n		]"
//			+ "\n	},"
//			+ "\n	I: 'j'"
//			+ "\n}";
//		assertEquals(e, YamlSerializer.DEFAULT.serialize(m));
//	}
//
//	//====================================================================================================
//	// Escaping double quotes
//	//====================================================================================================
//	@Test
//	public void testEscapingDoubleQuotes() throws Exception {
//		YamlSerializer s = YamlSerializer.DEFAULT;
//		String r = s.serialize(new ObjectMap().append("f1", "x'x\"x"));
//		assertEquals("{\"f1\":\"x'x\\\"x\"}", r);
//		YamlParser p = YamlParser.DEFAULT;
//		assertEquals("x'x\"x", p.parse(r, ObjectMap.class).getString("f1"));
//	}
//
//	//====================================================================================================
//	// Escaping single quotes
//	//====================================================================================================
//	@Test
//	public void testEscapingSingleQuotes() throws Exception {
//		YamlSerializer s = YamlSerializer.DEFAULT;
//		String r = s.serialize(new ObjectMap().append("f1", "x'x\"x"));
//		assertEquals("{f1:'x\\'x\"x'}", r);
//		YamlParser p = YamlParser.DEFAULT;
//		assertEquals("x'x\"x", p.parse(r, ObjectMap.class).getString("f1"));
//	}
//
//	//====================================================================================================
//	// testSubclassedList
//	//====================================================================================================
//	@Test
//	public void testSubclassedList() throws Exception {
//		YamlSerializer s = YamlSerializer.DEFAULT;
//		Map<String,Object> o = new HashMap<String,Object>();
//		o.put("c", new C());
//		assertEquals("{\"c\":[]}", s.serialize(o));
//	}
//
//	public static class C extends LinkedList<String> {
//	}
//
//	//====================================================================================================
//	// testEscapeSolidus
//	//====================================================================================================
//	@Test
//	public void testEscapeSolidus() throws Exception {
//		YamlSerializer s = new YamlSerializerBuilder().escapeSolidus(false).build();
//		String r = s.serialize("foo/bar");
//		assertEquals("\"foo/bar\"", r);
//		r = YamlParser.DEFAULT.parse(r, String.class);
//		assertEquals("foo/bar", r);
//
//		s = new YamlSerializerBuilder().escapeSolidus(true).build();
//		r = s.serialize("foo/bar");
//		assertEquals("\"foo\\/bar\"", r);
//		r = YamlParser.DEFAULT.parse(r, String.class);
//		assertEquals("foo/bar", r);
//	}
}