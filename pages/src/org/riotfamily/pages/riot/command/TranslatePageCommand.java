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
package org.riotfamily.pages.riot.command;

import org.riotfamily.core.screen.list.command.CommandContext;
import org.riotfamily.core.screen.list.command.CommandResult;
import org.riotfamily.core.screen.list.command.impl.support.AbstractBatchCommand;
import org.riotfamily.core.screen.list.command.result.RefreshListResult;
import org.riotfamily.pages.model.Page;
import org.riotfamily.pages.model.Site;

public class TranslatePageCommand extends AbstractBatchCommand<Page> {

	@Override
	public String getAction(CommandContext context) {
		Site site = (Site) context.getParent();
		if (site == null || site.getMasterSite() == null) {
			return null;
		}
		return super.getAction(context);
	}

	@Override
	protected String getIcon(String action) {
		return "page_add";
	}
	
	@Override
	protected CommandResult execute(CommandContext context, Page page,
			int index, int selectionSize) {

		Page parent = (Page) context.getParent();
		parent.addPage(new Page(page));
		return new RefreshListResult();
	}

}