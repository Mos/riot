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
package org.riotfamily.riot.list.command.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.riotfamily.common.util.FormatUtils;
import org.riotfamily.riot.list.command.Command;
import org.riotfamily.riot.list.command.CommandContext;
import org.riotfamily.riot.list.command.CommandState;
import org.riotfamily.riot.runtime.RiotRuntime;
import org.riotfamily.riot.runtime.RiotRuntimeAware;
import org.springframework.beans.factory.BeanNameAware;

/**
 * Abstract base class for commands.
 */
public abstract class AbstractCommand implements Command, BeanNameAware, 
		RiotRuntimeAware {

	private static final String COMMAND_NAME_SUFFIX = "Command";

	private final String COMMAND_MESSAGE_PREFIX = "command.";

	protected Log log = LogFactory.getLog(getClass());

	private String id;
	
	private String beanName;
	
	private String styleClass;

	private boolean showOnForm;
	
	private boolean targetRequired = true;
	
	private RiotRuntime runtime;

	public String getId() {
		if (id == null) {
			if (beanName != null) {
				id = beanName;
			}
			else {
				id = getClass().getName();
			}
			int i = id.lastIndexOf('.');
			if (i >= 0) {
				id = id.substring(i + 1);
			}
			if (id.endsWith(COMMAND_NAME_SUFFIX)) {
				id = id.substring(0, id.length() - COMMAND_NAME_SUFFIX.length());
			}
		}
		return id;
	}

	/**
	 * Sets the commandId. If no value is set the bean name will be used
	 * by default.
	 *
	 * @see #setBeanName(String)
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * Implementation of the
	 * {@link org.springframework.beans.factory.BeanNameAware BeanNameAware}
	 * interface. If no command id is explicitly set, the bean name will be
	 * used instead. Note that if the name ends with the suffix "Command"
	 * it will be removed from the id.
	 */
	public void setBeanName(String beanName) {
		this.beanName = beanName;
	}
	
	/**
	 * Implementation of the {@link RiotRuntimeAware} interface. Allows 
	 * subclasses to call {@link #getRiotServletPrefix()}.
	 */
	public void setRiotRuntime(RiotRuntime runtime) {
		this.runtime = runtime;
	}
	
	protected RiotRuntime getRuntime() {
		return runtime;
	}

	/**
	 * Always returns <code>null</code>. Sublasses may override this method
	 * in order to display a confirmation message before the command is
	 * executed.
	 */
	public String getConfirmationMessage(CommandContext context) {
		return null;
	}
	
	protected Object[] getDefaultMessageArgs(CommandContext context) {
		Class<?> clazz = context.getListDefinition().getBeanClass();
		Object item = context.getBean();
		String type = context.getMessageResolver().getClassLabel(null, clazz);
		String label = FormatUtils.xmlEscape(context.getListDefinition()
				.getLabel(item, context.getMessageResolver()));
		
		return new Object[] {label, type, context.getObjectId()};
	}

	public boolean isShowOnForm() {
		return this.showOnForm;
	}

	public void setShowOnForm(boolean showOnForm) {
		this.showOnForm = showOnForm;
	}
	
	/**
	 * Returns whether the command requires a target for the execution.
	 * This flag only applies to trees. It prevents target selection, which is
	 * particularly useful when you have a command that applies to the whole tree structure.
	 */
	protected boolean isTargetRequired() {
		return targetRequired;
	}

	protected void setTargetRequired(boolean targetRequired) {
		this.targetRequired = targetRequired;
	}

	/**
	 *
	 */
	public CommandState getState(CommandContext context) {
		String action = getAction(context);
		CommandState state = getState(context, action);
		state.setEnabled(isEnabled(context, action));
		return state;
	}
	
	protected CommandState getState(CommandContext context, String action) {
		CommandState state = new CommandState();
		state.setId(getId());
		state.setAction(action);
		state.setLabel(getLabel(context, action));
		state.setStyleClass(getStyleClass(context, action));
		state.setItemStyleClass(getItemStyleClass(context, action));
		state.setTargetRequired(isTargetRequired());
		return state;
	}

	/**
	 * Returns the command's id. Subclasses may override this method if the
	 * action depends on the context.
	 */
	protected String getAction(CommandContext context) {
		return getId();
	}

	/**
	 * Returns a label by resolving the message-key
	 * <code>command.<i>labelKeySuffix</i></code>, where <i>labelKeySuffix</i>
	 * is the String returned by {@link #getLabelKeySuffix(CommandContext, String)}.
	 */
	protected String getLabel(CommandContext context, String action) {
		String key = getLabelKeySuffix(context, action);
		return context.getMessageResolver().getMessage(
				COMMAND_MESSAGE_PREFIX + key, null,
				FormatUtils.camelToTitleCase(key));
	}

	/**
	 * Returns the command's action. Subclasses may override this method if the
	 * label depends on the context.
	 *
	 * @see #getLabel(CommandContext, String)
	 */
	protected String getLabelKeySuffix(CommandContext context, String action) {
		return action;
	}

	public void setStyleClass(String styleClass) {
		this.styleClass = styleClass;
	}
	
	/**
	 * Returns the CSS class that is assigned to command's HTML element and
	 * therefore defines which icon is displayed. If no class is set, the 
	 * default implementation will return the action instead.
	 */
	protected String getStyleClass(CommandContext context, String action) {
		if (styleClass == null) {
			return action;
		}
		return styleClass;
	}

	/**
	 * Returns a CSS class that is added to the list of class names of the
	 * whole item/row. The default implementation always returns
	 * <code>null</code>. Subclasses may override this method to highlight
	 * a list item depending on the context.
	 */
	protected String getItemStyleClass(CommandContext context, String action) {
		return null;
	}

	/**
	 * Subclasses may inspect the given context to decide whether the
	 * command should be enabled. Commands don't need to check the
	 * {@link org.riotfamily.riot.security.policy.AuthorizationPolicy policy} since
	 * commands will be automatically disabled if the action returned by
	 * {@link #getAction(CommandContext) getAction()} is denied.
	 * The default implementation always returns <code>true</code>.
	 */
	protected boolean isEnabled(CommandContext context, String action) {
		return true;
	}

}
