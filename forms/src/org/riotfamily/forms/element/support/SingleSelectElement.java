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
package org.riotfamily.forms.element.support;

import org.riotfamily.forms.base.UserInterface;
import org.riotfamily.forms.option.Reference;
import org.springframework.util.ObjectUtils;

public abstract class SingleSelectElement extends SelectElement {

	@Override
	protected Class<?> getRequiredType() {
		return Object.class;
	}
	
	public abstract class State extends SelectElement.State {

		@Override
		protected boolean isSelected(Object option, Object value) {
			return ObjectUtils.nullSafeEquals(option, value);
		}
		
		public void select(UserInterface ui, String value) {
			select(value);
		}
		
		protected void select(String value) {
			for (OptionState option : options) {
				option.setSelected(option.getValue().equals(value));
			}
		}
		
		protected void selectFirst() {
			select(options.get(0).getValue());
		}
		
		protected Reference getSelectedReference() {
			for (OptionState option : options) {
				if (option.isSelected()) {
					return option.getReference();
				}
			}
			return null;
		}

		@Override
		public Object getValue() {
			return getReferenceService().resolve(getSelectedReference());
		}
		
	}

}
