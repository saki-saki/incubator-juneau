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

import org.apache.juneau.*;

/**
 * Properties associated with the {@link HtmlDocSerializer} class.
 *
 * <p>
 * These are typically specified via <ja>@RestResource.properties()</ja> and <ja>@RestMethod.properties()</ja>
 * annotations, although they can also be set programmatically via the <code>RestResponse.setProperty()</code> method.
 *
 * <h5 class='section'>Example:</h5>
 * <p class='bcode'>
 * 	<ja>@RestResource</ja>(
 * 		messages=<js>"nls/AddressBookResource"</js>,
 * 		properties={
 * 			<ja>@Property</ja>(name=HtmlDocSerializerContext.<jsf>HTMLDOC_title</jsf>, value=<js>"$L{title}"</js>),
 * 			<ja>@Property</ja>(name=HtmlDocSerializerContext.<jsf>HTMLDOC_description</jsf>, value=<js>"$L{description}"</js>),
 * 			<ja>@Property</ja>(name=HtmlDocSerializerContext.<jsf>HTMLDOC_links</jsf>, value=<js>"{options:'?method=OPTIONS',doc:'doc'}"</js>)
 * 		}
 * 	)
 * 	<jk>public class</jk> AddressBookResource <jk>extends</jk> RestServletJenaDefault {
 * </p>
 *
 * <p>
 * Note that shortcut annotations are also provided for these particular settings:
 * <p class='bcode'>
 * 	<ja>@RestResource</ja>(
 * 		messages=<js>"nls/AddressBookResource"</js>,
 * 		htmldoc=<ja>@HtmlDoc</ja>(
 * 			title=<js>"$L{title}"</js>,
 * 			description=<js>"$L{description}"</js>,
 * 			links={
 * 				<js>"options: ?method=OPTIONS"</js>,
 * 				<js>"doc: doc"</js>
 * 			}
 * 		)
 * 	)
 * </p>
 *
 * <p>
 * The <code>$L{...}</code> variable represent localized strings pulled from the resource bundle identified by the
 * <code>messages</code> annotation.
 * These variables are replaced at runtime based on the HTTP request locale.
 * Several built-in runtime variable types are defined, and the API can be extended to include user-defined variables.
 *
 * <h6 class='topic'>Inherited configurable properties</h6>
 * <ul class='doctree'>
 * 	<li class='jc'>
 * 		<a class="doclink" href="../BeanContext.html#ConfigProperties">BeanContext</a>
 * 		- Properties associated with handling beans on serializers and parsers.
 * 		<ul>
 * 			<li class='jc'>
 * 				<a class="doclink" href="../serializer/SerializerContext.html#ConfigProperties">SerializerContext</a>
 * 				- Configurable properties common to all serializers.
 * 				<ul>
 * 					<li class='jc'>
 * 						<a class="doclink" href="../html/HtmlSerializerContext.html#ConfigProperties">HtmlSerializerContext</a>
 * 						- Configurable properties on the HTML serializer.
 * 				</ul>
 * 			</li>
 * 		</ul>
 * 	</li>
 * </ul>
 */
public final class HtmlDocSerializerContext extends HtmlSerializerContext {

	/**
	 * <b>Configuration property:</b>  Header section contents.
	 *
	 * <ul>
	 * 	<li><b>Name:</b> <js>"HtmlDocSerializer.header"</js>
	 * 	<li><b>Data type:</b> <code>String</code>
	 * 	<li><b>Default:</b> <jk>null</jk>
	 * 	<li><b>Session-overridable:</b> <jk>true</jk>
	 * </ul>
	 *
	 * <p>
	 * Allows you to override the contents of the header section on the HTML page.
	 * The header section normally contains the title and description at the top of the page.
	 *
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode'>
	 * 	<ja>@RestResource</ja>(
	 * 		htmldoc=<ja>@HtmlDoc</ja>(
	 * 			header={
	 * 				<js>"&lt;h1&gt;My own header&lt;/h1&gt;"</js>
	 * 			}
	 * 		)
	 * 	)
	 * </p>
	 *
	 * <p>
	 * A value of <js>"NONE"</js> can be used to represent no value to differentiate it from an empty string.
	 */
	public static final String HTMLDOC_header = "HtmlDocSerializer.header";

	/**
	 * <b>Configuration property:</b>  Page links.
	 *
	 * <ul>
	 * 	<li><b>Name:</b> <js>"HtmlDocSerializer.links.list"</js>
	 * 	<li><b>Data type:</b> <code>String[]</code>
	 * 	<li><b>Default:</b> empty array
	 * 	<li><b>Session-overridable:</b> <jk>true</jk>
	 * </ul>
	 *
	 * <p>
	 * Adds a list of hyperlinks immediately under the title and description but above the content of the page.
	 *
	 * <p>
	 * This can be used to provide convenient hyperlinks when viewing the REST interface from a browser.
	 *
	 * <p>
	 * The value is an array of strings with two possible values:
	 * <ul>
	 * 	<li>A key-value pair representing a hyperlink label and href:
	 * 		<br><js>"google: http://google.com"</js>
	 * 	<li>Arbitrary HTML.
	 * </ul>
	 *
	 * <p>
	 * Relative URLs are considered relative to the servlet path.
	 * For example, if the servlet path is <js>"http://localhost/myContext/myServlet"</js>, and the
	 * URL is <js>"foo"</js>, the link becomes <js>"http://localhost/myContext/myServlet/foo"</js>.
	 * Absolute (<js>"/myOtherContext/foo"</js>) and fully-qualified (<js>"http://localhost2/foo"</js>) URLs
	 * can also be used in addition to various other protocols specified by {@link UriResolver} such as
	 * <js>"servlet:/..."</js>.
	 *
	 * <h5 class='section'>Example:</h5>
	 * <p>
	 * The <code>AddressBookResource</code> sample class uses this property...
	 * <p class='bcode'>
	 * 	<ja>@RestResource</ja>(
	 * 		properties={
	 * 			<ja>@Property</ja>(name=HtmlDocSerializerContext.<jsf>HTMLDOC_links</jsf>,
	 * 				value=<js>"['options: ?method=OPTIONS', 'doc: doc']"</js>)
	 * 		}
	 * 	)
	 * 	<jk>public class</jk> AddressBookResource <jk>extends</jk> RestServletJenaDefault {
	 * </p>
	 *
	 * <p>
	 * 	...to produce this list of links on the HTML page...
	 * <img class='bordered' src='doc-files/HTML_LINKS.png'>
	 *
	 * <p>
	 * A shortcut on <ja>@RestResource</ja> is also provided for this setting:
	 * <p class='bcode'>
	 * 	<ja>@RestResource</ja>(
	 * 		htmldoc=@HtmlDoc(
	 * 			links={
	 * 				<js>"options: ?method=OPTIONS"</js>,
	 * 				<js>"doc: doc"</js>
	 * 			}
	 * 		)
	 * 	)
	 * 	<jk>public class</jk> AddressBookResource <jk>extends</jk> RestServletJenaDefault {
	 * </p>
	 */
	public static final String HTMLDOC_links = "HtmlDocSerializer.links.list";

	/**
	 * <b>Configuration property:</b>  Add to the {@link #HTMLDOC_links} property.
	 */
	public static final String HTMLDOC_links_add = "HtmlDocSerializer.links.list.add";

	/**
	 * <b>Configuration property:</b>  Nav section contents.
	 *
	 * <ul>
	 * 	<li><b>Name:</b> <js>"HtmlDocSerializer.nav"</js>
	 * 	<li><b>Data type:</b> <code>String</code>
	 * 	<li><b>Default:</b> <jk>null</jk>
	 * 	<li><b>Session-overridable:</b> <jk>true</jk>
	 * </ul>
	 *
	 * <p>
	 * Allows you to override the contents of the nav section on the HTML page.
	 * The nav section normally contains the page links at the top of the page.
	 *
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode'>
	 * 	<ja>@RestResource</ja>(
	 * 		htmldoc=<ja>@HtmlDoc</ja>(
	 * 			nav={
	 * 				<js>"&lt;p class='special-navigation'&gt;This is my special navigation content&lt;/p&gt;"</js>
	 * 			}
	 * 		)
	 * 	)
	 * </p>
	 *
	 * <p>
	 * When this property is specified, the {@link #HTMLDOC_links} property is ignored.
	 *
	 * <p>
	 * A value of <js>"NONE"</js> can be used to represent no value to differentiate it from an empty string.
	 */
	public static final String HTMLDOC_nav = "HtmlDocSerializer.nav";

	/**
	 * <b>Configuration property:</b>  Aside section contents.
	 *
	 * <ul>
	 * 	<li><b>Name:</b> <js>"HtmlDocSerializer.aside"</js>
	 * 	<li><b>Data type:</b> <code>String</code>
	 * 	<li><b>Default:</b> <jk>null</jk>
	 * 	<li><b>Session-overridable:</b> <jk>true</jk>
	 * </ul>
	 *
	 * <p>
	 * Allows you to specify the contents of the aside section on the HTML page.
	 * The aside section floats on the right of the page for providing content supporting the serialized content of
	 * the page.
	 *
	 * <p>
	 * By default, the aside section is empty.
	 *
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode'>
	 * 	<ja>@RestResource</ja>(
	 * 		htmldoc=<ja>@HtmlDoc</ja>(
	 * 			aside={
	 * 				<js>"&lt;ul&gt;"</js>,
	 * 				<js>"	&lt;li&gt;Item 1"</js>,
	 * 				<js>"	&lt;li&gt;Item 2"</js>,
	 * 				<js>"	&lt;li&gt;Item 3"</js>,
	 * 				<js>"&lt;/ul&gt;"</js>
	 * 			}
	 * 		)
	 * 	)
	 * </p>
	 *
	 * <p>
	 * A value of <js>"NONE"</js> can be used to represent no value to differentiate it from an empty string.
	 */
	public static final String HTMLDOC_aside = "HtmlDocSerializer.aside";

	/**
	 * <b>Configuration property:</b>  Footer section contents.
	 *
	 * <ul>
	 * 	<li><b>Name:</b> <js>"HtmlDocSerializer.footer"</js>
	 * 	<li><b>Data type:</b> <code>String</code>
	 * 	<li><b>Default:</b> <jk>null</jk>
	 * 	<li><b>Session-overridable:</b> <jk>true</jk>
	 * </ul>
	 *
	 * <p>
	 * Allows you to specify the contents of the footer section on the HTML page.
	 *
	 * <p>
	 * By default, the footer section is empty.
	 *
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode'>
	 * 	<ja>@RestResource</ja>(
	 * 		htmldoc=<ja>@HtmlDoc</ja>(
	 * 			footer={
	 * 				<js>"&lt;b&gt;This interface is great!&lt;/b&gt;"</js>
	 * 			}
	 * 		)
	 * 	)
	 * </p>
	 *
	 * <p>
	 * A value of <js>"NONE"</js> can be used to represent no value to differentiate it from an empty string.
	 */
	public static final String HTMLDOC_footer = "HtmlDocSerializer.footer";

	/**
	 * <b>Configuration property:</b>  No-results message.
	 *
	 * <ul>
	 * 	<li><b>Name:</b> <js>"HtmlDocSerializer.noResultsMessage"</js>
	 * 	<li><b>Data type:</b> <code>String</code>
	 * 	<li><b>Default:</b> <js>"&lt;p&gt;no results&lt;/p&gt;"</js>
	 * 	<li><b>Session-overridable:</b> <jk>true</jk>
	 * </ul>
	 *
	 * <p>
	 * Allows you to specify the string message used when trying to serialize an empty array or empty list.
	 *
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode'>
	 * 	<ja>@RestResource</ja>(
	 * 		htmldoc=<ja>@HtmlDoc</ja>(
	 * 			noResultsMessage=<js>"&lt;b&gt;This interface is great!&lt;/b&gt;"</js>
	 * 		)
	 * 	)
	 * </p>
	 *
	 * <p>
	 * A value of <js>"NONE"</js> can be used to represent no value to differentiate it from an empty string.
	 */
	public static final String HTMLDOC_noResultsMessage = "HtmlDocSerializer.noResultsMessage";

	/**
	 * <b>Configuration property:</b>  Prevent word wrap on page.
	 *
	 * <ul>
	 * 	<li><b>Name:</b> <js>"HtmlDocSerializer.nowrap"</js>
	 * 	<li><b>Data type:</b> <code>Boolean</code>
	 * 	<li><b>Default:</b> <jk>false</jk>
	 * 	<li><b>Session-overridable:</b> <jk>true</jk>
	 * </ul>
	 *
	 * <p>
	 * Adds <js>"* {white-space:nowrap}"</js> to the CSS instructions on the page to prevent word wrapping.
	 */
	public static final String HTMLDOC_nowrap = "HtmlDocSerializer.nowrap";

	/**
	 * <b>Configuration property:</b>  Stylesheet import URLs.
	 *
	 * <ul>
	 * 	<li><b>Name:</b> <js>"HtmlDocSerializer.stylesheet"</js>
	 * 	<li><b>Data type:</b> <code>List&lt;String&gt;</code>
	 * 	<li><b>Default:</b> empty list
	 * 	<li><b>Session-overridable:</b> <jk>true</jk>
	 * </ul>
	 *
	 * <p>
	 * Adds a link to the specified stylesheet URL.
	 *
	 * <p>
	 * Note that this stylesheet is controlled by the <code><ja>@RestResource</ja>.stylesheet()</code> annotation.
	 *
	 * <p>
	 * A value of <js>"NONE"</js> can be used to represent no value to differentiate it from an empty string.
	 */
	public static final String HTMLDOC_stylesheet = "HtmlDocSerializer.stylesheet";

	/**
	 * <b>Configuration property:</b>  Add to the {@link #HTMLDOC_stylesheet} property.
	 */
	public static final String HTMLDOC_stylesheet_add = "HtmlDocSerializer.stylesheet.list.add";

	/**
	 * <b>Configuration property:</b>  CSS style code.
	 *
	 * <ul>
	 * 	<li><b>Name:</b> <js>"HtmlDocSerializer.style.list"</js>
	 * 	<li><b>Data type:</b> <code>List&lt;String&gt;</code>
	 * 	<li><b>Default:</b> empty list
	 * 	<li><b>Session-overridable:</b> <jk>true</jk>
	 * </ul>
	 *
	 * <p>
	 * Adds the specified CSS instructions to the HTML page.
	 *
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode'>
	 * 	<ja>@RestResource</ja>(
	 * 		properties={
	 * 			<ja>@Property</ja>(name=HtmlDocSerializerContext.<jsf>HTMLDOC_style</jsf>,
	 * 				value=<js>"h3 { color: red; }\nh5 { font-weight: bold; }"</js>)
	 * 		}
	 * 	)
	 * </p>
	 *
	 * <p>
	 * A shortcut on <ja>@RestResource</ja> is also provided for this setting:
	 * <p class='bcode'>
	 * 	<ja>@RestResource</ja>(
	 * 		htmldoc=@HtmlDoc(
	 * 			style={
	 * 				<js>"h3 { color: red; }"</js>,
	 * 				<js>"h5 { font-weight: bold; }"</js>
	 * 			}
	 * 		)
	 * 	)
	 * </p>
	 */
	public static final String HTMLDOC_style = "HtmlDocSerializer.style.list";

	/**
	 * <b>Configuration property:</b>  Add to the {@link #HTMLDOC_style} property.
	 */
	public static final String HTMLDOC_style_add = "HtmlDocSerializer.style.list.add";

	/**
	 * <b>Configuration property:</b>  Javascript code.
	 *
	 * <ul>
	 * 	<li><b>Name:</b> <js>"HtmlDocSerializer.script.list"</js>
	 * 	<li><b>Data type:</b> <code>List&lt;String&gt;</code>
	 * 	<li><b>Default:</b> empty list
	 * 	<li><b>Session-overridable:</b> <jk>true</jk>
	 * </ul>
	 *
	 * <p>
	 * Adds the specified Javascript code to the HTML page.
	 *
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode'>
	 * 	<ja>@RestResource</ja>(
	 * 		properties={
	 * 			<ja>@Property</ja>(name=HtmlDocSerializerContext.<jsf>HTMLDOC_script</jsf>,
	 * 				value=<js>"alert('hello!');"</js>)
	 * 		}
	 * 	)
	 * </p>
	 *
	 * <p>
	 * A shortcut on <ja>@RestResource</ja> is also provided for this setting:
	 * <p class='bcode'>
	 * 	<ja>@RestResource</ja>(
	 * 		htmldoc=@HtmlDoc(
	 * 			script={
	 * 				<js>"alert('hello!');"</js>
	 * 			}
	 * 		)
	 * 	)
	 * </p>
	 */
	public static final String HTMLDOC_script = "HtmlDocSerializer.script.list";

	/**
	 * <b>Configuration property:</b>  Add to the {@link #HTMLDOC_script} property.
	 */
	public static final String HTMLDOC_script_add = "HtmlDocSerializer.script.list.add";

	/**
	 * <b>Configuration property:</b>  HTML document template.
	 *
	 * <ul>
	 * 	<li><b>Name:</b> <js>"HtmlDocSerializer.template"</js>
	 * 	<li><b>Data type:</b> <code>Class&lt;? <jk>extends</jk> HtmlDocTemplate&gt;</code> or {@link HtmlDocTemplate}
	 * 	<li><b>Default:</b> <code>HtmlDocTemplateBasic.<jk>class</jk></code>
	 * 	<li><b>Session-overridable:</b> <jk>true</jk>
	 * </ul>
	 *
	 * <p>
	 * Specifies the template to use for serializing the page.
	 *
	 * <p>
	 * By default, the {@link HtmlDocTemplateBasic} class is used to construct the contents of the HTML page, but
	 * can be overridden with your own custom implementation class.
	 *
	 * <h5 class='section'>Example:</h5>
	 * <p class='bcode'>
	 * 	<ja>@RestResource</ja>(
	 * 		htmldoc=@HtmlDoc(
	 * 			template=MySpecialDocTemplate.<jk>class</jk>
	 * 		)
	 * 	)
	 * </p>
	 */
	public static final String HTMLDOC_template = "HtmlDocSerializer.template";


	final String[] style, stylesheet, script, links;
	final String header, nav, aside, footer, noResultsMessage;
	final boolean nowrap;
	final HtmlDocTemplate template;

	/**
	 * Constructor.
	 *
	 * <p>
	 * Typically only called from {@link PropertyStore#getContext(Class)}.
	 *
	 * @param ps The property store that created this context.
	 */
	public HtmlDocSerializerContext(PropertyStore ps) {
		super(ps);
		style = ps.getProperty(HTMLDOC_style, String[].class, new String[0]);
		stylesheet = ps.getProperty(HTMLDOC_stylesheet, String[].class, new String[0]);
		script = ps.getProperty(HTMLDOC_script, String[].class, new String[0]);
		header = ps.getProperty(HTMLDOC_header, String.class, null);
		nav = ps.getProperty(HTMLDOC_nav, String.class, null);
		aside = ps.getProperty(HTMLDOC_aside, String.class, null);
		footer = ps.getProperty(HTMLDOC_footer, String.class, null);
		nowrap = ps.getProperty(HTMLDOC_nowrap, boolean.class, false);
		links = ps.getProperty(HTMLDOC_links, String[].class, new String[0]);
		noResultsMessage = ps.getProperty(HTMLDOC_noResultsMessage, String.class, "<p>no results</p>");
		template = ps.getTypedProperty(HTMLDOC_template, HtmlDocTemplate.class, HtmlDocTemplateBasic.class);
	}

	@Override /* Context */
	public ObjectMap asMap() {
		return super.asMap()
			.append("HtmlDocSerializerContext", new ObjectMap()
				.append("header", header)
				.append("nav", nav)
				.append("links", links)
				.append("aside", aside)
				.append("footer", footer)
				.append("style", style)
				.append("stylesheet", stylesheet)
				.append("nowrap", nowrap)
				.append("template", template)
				.append("noResultsMessage", noResultsMessage)
			);
	}
}
