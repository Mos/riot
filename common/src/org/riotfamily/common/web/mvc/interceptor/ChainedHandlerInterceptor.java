/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.riotfamily.common.web.mvc.interceptor;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.context.request.WebRequestInterceptor;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.WebRequestHandlerInterceptorAdapter;

/**
 * HandlerInterceptor that delegates calls to a list of interceptors.
 * Supported interceptor types are HandlerInterceptor and WebRequestInterceptor.
 * 
 * @see #setInterceptors(List)
 * @author Felix Gnass [fgnass at neteye dot de]
 * @since 6.5
 */
public class ChainedHandlerInterceptor implements HandlerInterceptor, 
		InitializingBean {

	private List<HandlerInterceptor> interceptors;
	
	private HandlerInterceptor[] adaptedInterceptors;
	
	/**
	 * Set the interceptors to apply in a chain.
	 * <p>Supported interceptor types are HandlerInterceptor and WebRequestInterceptor.
	 * @param interceptors array of handler interceptors, or <code>null</code> if none
	 * @see org.springframework.web.servlet.HandlerInterceptor
	 * @see org.springframework.web.context.request.WebRequestInterceptor
	 */
	public void setInterceptors(List<HandlerInterceptor> interceptors) {
		this.interceptors = interceptors;
	}
	
	/**
	 * Calls the {@link #initInterceptors()} method.
	 */
	public void afterPropertiesSet() throws Exception {
		initInterceptors();
	}
	
	/**
	 * Initialize the specified interceptors, adapting them where necessary.
	 * @see #setInterceptors
	 * @see #adaptInterceptor
	 */
	protected void initInterceptors() {
		if (interceptors != null) {
			adaptedInterceptors = new HandlerInterceptor[interceptors.size()];
			for (int i = 0; i < adaptedInterceptors.length; i++) {
				Object interceptor = interceptors.get(i);
				if (interceptor == null) {
					throw new IllegalArgumentException("Entry number " + i + " in interceptors list is null");
				}
				adaptedInterceptors[i] = adaptInterceptor(interceptor);
			}
		}
		else {
			adaptedInterceptors = new HandlerInterceptor[0];
		}
	}
	
	/**
	 * Adapt the given interceptor object to the HandlerInterceptor interface.
	 * <p>Supported interceptor types are HandlerInterceptor and WebRequestInterceptor.
	 * Each given WebRequestInterceptor will be wrapped in a WebRequestHandlerInterceptorAdapter.
	 * Can be overridden in subclasses.
	 * @param interceptor the specified interceptor object
	 * @return the interceptor wrapped as HandlerInterceptor
	 * @see org.springframework.web.servlet.HandlerInterceptor
	 * @see org.springframework.web.context.request.WebRequestInterceptor
	 * @see WebRequestHandlerInterceptorAdapter
	 */
	protected HandlerInterceptor adaptInterceptor(Object interceptor) {
		if (interceptor instanceof HandlerInterceptor) {
			return (HandlerInterceptor) interceptor;
		}
		else if (interceptor instanceof WebRequestInterceptor) {
			return new WebRequestHandlerInterceptorAdapter((WebRequestInterceptor) interceptor);
		}
		else {
			throw new IllegalArgumentException("Interceptor type not supported: " + interceptor.getClass().getName());
		}
	}
	
	/**
	 * Delegates the call to all configured interceptors. If an interceptor 
	 * returns <code>false</code> the chain is not further processes and the 
	 * method returns <code>false</code> itself. Otherwise <code>true</code>
	 * is returned after all interceptors have been called. 
	 */
	public boolean preHandle(HttpServletRequest request, 
			HttpServletResponse response, Object handler) throws Exception {

		for (int i = 0; i < adaptedInterceptors.length; i++) {
			HandlerInterceptor interceptor = adaptedInterceptors[i];
			if (!interceptor.preHandle(request, response, handler)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Delegates the call to all configured interceptors.
	 */
	public void postHandle(HttpServletRequest request, 
			HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
		
		for (int i = 0; i < adaptedInterceptors.length; i++) {
			HandlerInterceptor interceptor = adaptedInterceptors[i];
			interceptor.postHandle(request, response, handler, modelAndView);
		}
	}
	
	/**
	 * Delegates the call to all configured interceptors.
	 */
	public void afterCompletion(HttpServletRequest request, 
			HttpServletResponse response, Object handler, Exception ex)
			throws Exception {
		
		for (int i = 0; i < adaptedInterceptors.length; i++) {
			HandlerInterceptor interceptor = adaptedInterceptors[i];
			interceptor.afterCompletion(request, response, handler, ex);
		}
	}

}
