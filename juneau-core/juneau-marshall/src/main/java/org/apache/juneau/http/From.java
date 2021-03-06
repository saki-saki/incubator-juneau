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
package org.apache.juneau.http;

/**
 * Represents a parsed <l>From</l> HTTP request header.
 *
 * <p>
 * The email address of the user making the request.
 *
 * <h6 class='figure'>Example</h6>
 * <p class='bcode'>
 * 	From: user@example.com
 * </p>
 *
 * <h6 class='topic'>RFC2616 Specification</h6>
 *
 * The From request-header field, if given, SHOULD contain an Internet e-mail address for the human user who controls
 * the requesting user agent.
 * The address SHOULD be machine-usable, as defined by "mailbox" in RFC 822 [9] as updated by RFC 1123 [8]:
 *
 * <p class='bcode'>
 * 	From   = "From" ":" mailbox
 * </p>
 *
 * <p>
 * An example is:
 * <p class='bcode'>
 * 	From: webmaster@w3.org
 * </p>
 *
 * <p>
 * This header field MAY be used for logging purposes and as a means for identifying the source of invalid or unwanted
 * requests.
 * It SHOULD NOT be used as an insecure form of access protection.
 * The interpretation of this field is that the request is being performed on behalf of the person given, who accepts
 * responsibility for the method performed.
 * In particular, robot agents SHOULD include this header so that the person responsible for running the robot can be
 * contacted if problems occur on the receiving end.
 *
 * <p>
 * The Internet e-mail address in this field MAY be separate from the Internet host which issued the request.
 * For example, when a request is passed through a proxy the original issuer's address SHOULD be used.
 *
 * <p>
 * The client SHOULD NOT send the From header field without the user's approval, as it might conflict with the user's
 * privacy interests or their site's security policy.
 * It is strongly recommended that the user be able to disable, enable, and modify the value of this field at any time
 * prior to a request.
 *
 * <h6 class='topic'>Additional Information</h6>
 * <ul class='doctree'>
 * 	<li class='jp'>
 * 		<a class='doclink' href='package-summary.html#TOC'>org.apache.juneau.http</a>
 * 	<li class='extlink'>
 * 		<a class='doclink' href='https://www.w3.org/Protocols/rfc2616/rfc2616.html'>
 * 		Hypertext Transfer Protocol -- HTTP/1.1</a>
 * </ul>
 */
public final class From extends HeaderString {

	/**
	 * Returns a parsed <code>From</code> header.
	 *
	 * @param value The <code>From</code> header string.
	 * @return The parsed <code>From</code> header, or <jk>null</jk> if the string was null.
	 */
	public static From forString(String value) {
		if (value == null)
			return null;
		return new From(value);
	}

	private From(String value) {
		super(value);
	}
}
