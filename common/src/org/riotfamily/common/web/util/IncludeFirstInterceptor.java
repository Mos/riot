/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 * 
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 * 
 * The Original Code is Riot.
 * 
 * The Initial Developer of the Original Code is
 * Neteye GmbH.
 * Portions created by the Initial Developer are Copyright (C) 2006
 * the Initial Developer. All Rights Reserved.
 * 
 * Contributor(s):
 *   Felix Gnass [fgnass at neteye dot de]
 * 
 * ***** END LICENSE BLOCK ***** */
package org.riotfamily.common.web.util;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

/**
 * HandlerInterceptor that allows an included controller to handle the request
 * before any other controller is executed. This is useful if an included 
 * controller needs to send a redirect, which would not work under normal 
 * circumstances, since the response would have already been committed.
 * <p>
 * The interceptor looks for a special request parameter which contains the URI
 * of the controller that should handle the request in the fist place. If such
 * a parameter is present, the interceptor will use a RequestDispatcher to 
 * forward the request to that URI. For this forward the response is replaced
 * by a wrapper that captures the output and checks whether a redirect or error 
 * has been sent. If yes, the interceptor prevents the execution of the actual 
 * handler. Otherwise the execution continues and the captured output is 
 * rendered when the controller is requested for the second time.
 * </p> 
 */
public class IncludeFirstInterceptor extends HandlerInterceptorAdapter {

	public static final String INCLUDE_URI_PARAM = "__includeUri";
	
	private static final String HANDLER_ATTRIBUTE = 
			IncludeFirstInterceptor.class.getName() + ".handler";
	
	private static final String RESPONSE_WRAPPER_ATTRIBUTE = 
		IncludeFirstInterceptor.class.getName() + ".responseWrapper";
	
	public boolean preHandle(HttpServletRequest request, 
			HttpServletResponse response, Object handler) throws Exception {
		
		DeferredRenderingResponseWrapper responseWrapper = (DeferredRenderingResponseWrapper)
				request.getAttribute(RESPONSE_WRAPPER_ATTRIBUTE);
		
		Object firstHandler = request.getAttribute(HANDLER_ATTRIBUTE);
		
		if (firstHandler == null) {
			if (responseWrapper == null) {
				return handleUnknown(request, response);
			}
			else {
				return handleFirstInclude(handler, request);
			}
		}
		else {
			return handleSubsequentInclude(handler, firstHandler, 
							responseWrapper, response);
		}
	}
		
	protected boolean handleUnknown(HttpServletRequest request, 
			HttpServletResponse response) throws Exception {
		
		String includeUri = request.getParameter(INCLUDE_URI_PARAM);
		if (includeUri != null) {
			DeferredRenderingResponseWrapper responseWrapper = 
					new DeferredRenderingResponseWrapper(response);
			
			request.setAttribute(RESPONSE_WRAPPER_ATTRIBUTE, responseWrapper);			
			if (includeUri.startsWith(request.getContextPath())) {
				includeUri = includeUri.substring(
							request.getContextPath().length());
			}
			request.getRequestDispatcher(includeUri).forward(
					request, responseWrapper);
			
			if (responseWrapper.isRedirectSent()) {
				return false;
			}
		}
		return true;
	}
	
	protected boolean handleFirstInclude(Object handler, HttpServletRequest request) {
		request.setAttribute(HANDLER_ATTRIBUTE, handler);
		return true;
	}
	
	protected boolean handleSubsequentInclude(Object handler, 
			Object firstHandler, 
			DeferredRenderingResponseWrapper responseWrapper, 
			HttpServletResponse response) 
			throws IOException {

		//TODO Check if getRequestURI() matches the includeUri, to support
		//multiple includes of the same handler under different URLs.
		if (handler.equals(firstHandler)) {
			//Second time we come across the handler ...
			responseWrapper.renderResponse(response);
			return false;
		}
		return true;
	}
}