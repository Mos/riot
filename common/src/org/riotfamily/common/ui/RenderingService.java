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
package org.riotfamily.common.ui;

import java.util.Arrays;
import java.util.List;

import org.riotfamily.common.util.SpringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;

public class RenderingService implements InitializingBean {

	private List<Renderer> renderers;

	@Autowired
	private ApplicationContext applicationContext;
	
	public RenderingService() {
	}
	
	public RenderingService(Renderer... renderers) {
		this.renderers = Arrays.asList(renderers);
	}
	
	public static RenderingService newInstance(ConversionService conversionService) {
		return new RenderingService(new SpringConversionRenderer(conversionService));
	}
	
	public void setRenderers(List<Renderer> renderers) {
		this.renderers = renderers;
	}
	
	public void afterPropertiesSet() throws Exception {
		if (renderers == null) {
			renderers = SpringUtils.orderedBeans(applicationContext, Renderer.class);
		}
	}
	
	public String render(Object object) {
		if (object == null) {
			return "";
		}
		return render(object, TypeDescriptor.forObject(object));
	}
	
	public String render(Object object, TypeDescriptor typeDescriptor) {
		for (Renderer renderer : renderers) {
			if (renderer.supports(typeDescriptor)) {
				return renderer.render(object, typeDescriptor);
			}
		}
		throw new IllegalStateException("No renderer for " + typeDescriptor);
	}
	
}
