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
package org.apache.juneau.rest.widget;

import java.io.*;

import org.apache.juneau.html.*;
import org.apache.juneau.internal.*;
import org.apache.juneau.rest.*;
import org.apache.juneau.serializer.*;

/**
 * A subclass of widgets for rendering menu items with drop-down windows.
 *
 * <p>
 * Defines some simple CSS and Javascript for enabling drop-down menus in the nav section of the page (although
 * nothing keeps you from using it in an arbitrary location in the page).
 *
 * <p>
 * The script specifies a <js>"menuClick(element)"</js> function that toggles the visibility of the next sibling of the
 * element.
 *
 * <p>
 * Subclasses should implement the following two methods:
 * <ul>
 * 	<li>{@link #getLabel(RestRequest)} - The menu item label.
 * 	<li>{@link #getContent(RestRequest)} - The menu item content.
 *
 * <p>
 * For example, to render a link that brings up a simple dialog in a div tag:
 * <p class='bcode'>
 * 	<ja>@Override</ja>
 * 	<jk>public</jk> String getLabel() {
 * 		<jk>return</jk> <js>"my-menu-item"</js>;
 * 	};
 *
 * 	<ja>@Override</ja>
 * 	<jk>public</jk> Div getLabel() {
 * 		<jk>return</jk> Html5Builder.<jsm>div</jsm>(<js>"Surprise!"</js>).style(<js>"color:red"</js>);
 * 	};
 * </p>
 *
 * <p>
 * The HTML content returned by the {@link #getHtml(RestRequest)} method is added where the <js>"$W{...}"</js> is
 * referenced in the page.
 */
public abstract class MenuItemWidget extends Widget {

	/**
	 * Returns the Javascript needed for the show and hide actions of the menu item.
	 */
	@Override /* Widget */
	public String getScript(RestRequest req) throws Exception {
		return loadScript("MenuItemWidget.js");
	}

	/**
	 * Defines a <js>"menu-item"</js> class that needs to be used on the outer element of the HTML returned by the
	 * {@link #getHtml(RestRequest)} method.
	 */
	@Override /* Widget */
	public String getStyle(RestRequest req) throws Exception {
		return loadStyle("MenuItemWidget.css");
	}

	@Override /* Widget */
	public String getHtml(RestRequest req) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append(""
			+ "<div class='menu-item'>"
			+ "\n\t<a class='link' onclick='menuClick(this)'>"+getLabel(req)+"</a>"
			+ "\n\t<div class='popup-content'>\n"
		);
		Object o = getContent(req);
		if (o instanceof Reader)
			IOUtils.pipe((Reader)o, new StringBuilderWriter(sb));
		else if (o instanceof CharSequence)
			sb.append((CharSequence)o);
		else {
			SerializerSessionArgs args = new SerializerSessionArgs(req.getProperties(), null, req.getLocale(), null, null, req.getUriContext());
			WriterSerializerSession session = HtmlSerializer.DEFAULT.createSession(args);
			try {
				session.indent = 2;
				session.serialize(sb, o);
			} finally {
				session.close();
			}
		}
		sb.append(""
			+ "\n\t</div>"
			+ "\n</div>"
		);
		return sb.toString();
	}

	/**
	 * The label for the menu item as it's rendered in the menu bar.
	 *
	 * @param req The HTTP request object.
	 * @return The menu item label.
	 * @throws Exception
	 */
	public abstract String getLabel(RestRequest req) throws Exception;

	/**
	 * The content of the popup.
	 *
	 * @param req The HTTP request object.
	 * @return
	 * 	The content of the popup.
	 * 	<br>Can be any of the following types:
	 * 	<ul>
	 * 		<li>{@link Reader} - Serialized directly to the output.
	 * 		<li>{@link CharSequence} - Serialized directly to the output.
	 * 		<li>Other - Serialized as HTML using {@link HtmlSerializer#DEFAULT}.
	 * 			<br>Note that this includes any of the {@link org.apache.juneau.dto.html5} beans.
	 * 	</ul>
	 * @throws Exception
	 */
	public abstract Object getContent(RestRequest req) throws Exception;
}
