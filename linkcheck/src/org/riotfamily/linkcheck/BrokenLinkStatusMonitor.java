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
package org.riotfamily.linkcheck;

import org.riotfamily.core.screen.ScreenContext;
import org.riotfamily.core.status.I18nStatusMonitor;

public class BrokenLinkStatusMonitor extends I18nStatusMonitor {

	public BrokenLinkStatusMonitor() {
		setMessageKey("status.brokenLinks");
	}

	protected Object getArg(ScreenContext context) {
		return Link.countBrokenLinks();
	}

}