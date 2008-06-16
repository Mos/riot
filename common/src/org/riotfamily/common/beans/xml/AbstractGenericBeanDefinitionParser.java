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
 * Portions created by the Initial Developer are Copyright (C) 2007
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *   Felix Gnass [fgnass at neteye dot de]
 *   Carsten Woelk [cwoelk at neteye dot de]
 *
 * ***** END LICENSE BLOCK ***** */
package org.riotfamily.common.beans.xml;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * Class similar to Spring's {@link AbstractBeanDefinitionParser}.
 * Supports registering aliased beans and decoration of nested beans.
 *
 * @author Carsten Woelk [cwoelk at neteye dot de]
 * @author Felix Gnass [fgnass at neteye dot de]
 * @since 6.5
 */
public abstract class AbstractGenericBeanDefinitionParser implements BeanDefinitionParser {

	/** Constant for the id attribute */
	public static final String ID_ATTRIBUTE = "id";

	private static Log log = LogFactory.getLog(AbstractGenericBeanDefinitionParser.class);
	
	private Class<?> beanClass;

	private boolean decorate = true;
	
	private boolean enabled = true;

	public AbstractGenericBeanDefinitionParser(Class<?> beanClass) {
		Assert.notNull(beanClass, "The beanClass must not be null");
		this.beanClass = beanClass;
	}
	
	public AbstractGenericBeanDefinitionParser(String className) {
		try {
			beanClass = ClassUtils.forName(className);
		}
		catch (ClassNotFoundException e) {
			throw new FatalBeanException("Can't find the bean class", e);
		}
		catch (NoClassDefFoundError e) {
			enabled = false;
			log.warn("Turning of support for " + className 
					+ " elements due to a missing dependency: " 
					+ e.getMessage());
		}
	}

	/**
	 * Sets whether the bean should be decorated. Default is <code>true</code>.
	 */
	public void setDecorate(boolean decorate) {
		this.decorate = decorate;
	}

	public final BeanDefinition parse(Element element, ParserContext parserContext) {
		if (!enabled) {
			throw new FatalBeanException("Support for " + element.getTagName() +
					" has been disabled. Please add the required jar files " +
					"to your classpath.");
		}
		AbstractBeanDefinition definition = parseInternal(element, parserContext);
		if (!parserContext.isNested()) {
			try {
				String id = resolveId(element, definition, parserContext);
				if (!StringUtils.hasText(id)) {
					parserContext.getReaderContext().error(
							"Id is required for element '" + element.getLocalName() + "' when used as a top-level tag", element);
				}
				String[] aliases = resolveAliases(element, definition, parserContext);
				BeanDefinitionHolder holder = new BeanDefinitionHolder(definition, id, aliases);
				if (decorate) {
					parserContext.getDelegate().decorateBeanDefinitionIfRequired(element, holder);
				}
				registerBeanDefinition(holder, parserContext.getRegistry());
				if (shouldFireEvents()) {
					BeanComponentDefinition componentDefinition = new BeanComponentDefinition(holder);
					postProcessComponentDefinition(componentDefinition);
					parserContext.registerComponent(componentDefinition);
				}
			}
			catch (BeanDefinitionStoreException ex) {
				parserContext.getReaderContext().error(ex.getMessage(), element);
				return null;
			}
		}
		else if (decorate) {
			BeanDefinitionHolder holder = new BeanDefinitionHolder(definition, "");
			parserContext.getDelegate().decorateBeanDefinitionIfRequired(element, holder);
		}
		return definition;
	}

	/**
	 * Creates a {@link BeanDefinitionBuilder} instance for the
	 * {@link #getBeanClass bean Class} and passes it to the
	 * {@link #doParse} strategy method.
	 * @param element the element that is to be parsed into a single BeanDefinition
	 * @param parserContext the object encapsulating the current state of the parsing process
	 * @return the BeanDefinition resulting from the parsing of the supplied {@link Element}
	 * @throws IllegalStateException if the bean {@link Class} returned from
	 * {@link #getBeanClass(org.w3c.dom.Element)} is <code>null</code>
	 * @see #doParse
	 */
	protected final AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
		BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(beanClass);
		builder.setSource(parserContext.extractSource(element));
		if (parserContext.isNested()) {
			// Inner bean definition must receive same singleton status as containing bean.
			builder.setSingleton(parserContext.getContainingBeanDefinition().isSingleton());
		}
		if (parserContext.isDefaultLazyInit()) {
			// Default-lazy-init applies to custom bean definitions as well.
			builder.setLazyInit(true);
		}
		doParse(element, parserContext, builder);
		return builder.getBeanDefinition();
	}

	/**
	 * Resolve the ID for the supplied {@link BeanDefinition}.
	 * <p>When using {@link #shouldGenerateId generation}, a name is generated automatically.
	 * Otherwise, the ID is extracted from the "id" attribute, potentially with a
	 * {@link #shouldGenerateIdAsFallback() fallback} to a generated id.
	 * @param element the element that the bean definition has been built from
	 * @param definition the bean definition to be registered
	 * @param parserContext the object encapsulating the current state of the parsing process;
	 * provides access to a {@link org.springframework.beans.factory.support.BeanDefinitionRegistry}
	 * @return the resolved id
	 * @throws BeanDefinitionStoreException if no unique name could be generated
	 * for the given bean definition
	 */
	protected String resolveId(Element element, AbstractBeanDefinition definition, ParserContext parserContext)
			throws BeanDefinitionStoreException {

		if (shouldGenerateId()) {
			return parserContext.getReaderContext().generateBeanName(definition);
		}
		else {
			String id = element.getAttribute(ID_ATTRIBUTE);
			if (!StringUtils.hasText(id) && shouldGenerateIdAsFallback()) {
				id = parserContext.getReaderContext().generateBeanName(definition);
			}
			return id;
		}
	}

	/**
	 * Resolve the aliases for the supplied {@link BeanDefinition}. The default
	 * implementation delegates the call to
	 * {@link #resolveAlias(Element, AbstractBeanDefinition, ParserContext)}
	 * and tokenizes the returned String.
	 */
	protected String[] resolveAliases(Element element,
			AbstractBeanDefinition definition, ParserContext parserContext) {

		String alias = resolveAlias(element, definition, parserContext);
		return StringUtils.tokenizeToStringArray(alias, ",; ");
	}

	/**
	 * Resolve the alias for the supplied {@link BeanDefinition}. The returned
	 * String may contain multiple bean-names separated by commas, semicolons
	 * or spaces. The default implementation returns <code>null</code>
	 */
	protected String resolveAlias(Element element,
			AbstractBeanDefinition definition, ParserContext parserContext) {

		return null;
	}

	/**
	 * Register the supplied {@link BeanDefinitionHolder bean} with the supplied
	 * {@link BeanDefinitionRegistry registry}.
	 * <p>Subclasses can override this method to control whether or not the supplied
	 * {@link BeanDefinitionHolder bean} is actually even registered, or to
	 * register even more beans.
	 * <p>The default implementation registers the supplied {@link BeanDefinitionHolder bean}
	 * with the supplied {@link BeanDefinitionRegistry registry} only if the <code>isNested</code>
	 * parameter is <code>false</code>, because one typically does not want inner beans
	 * to be registered as top level beans.
	 * @param definition the bean definition to be registered
	 * @param registry the registry that the bean is to be registered with
	 * @see BeanDefinitionReaderUtils#registerBeanDefinition(BeanDefinitionHolder, BeanDefinitionRegistry)
	 */
	protected void registerBeanDefinition(BeanDefinitionHolder definition, BeanDefinitionRegistry registry) {
		BeanDefinitionReaderUtils.registerBeanDefinition(definition, registry);
	}

	/**
	 * Should an ID be generated instead of read from the passed in {@link Element}?
	 * <p>Disabled by default; subclasses can override this to enable ID generation.
	 * Note that this flag is about <i>always</i> generating an ID; the parser
	 * won't even check for an "id" attribute in this case.
	 * @return whether the parser should always generate an id
	 */
	protected boolean shouldGenerateId() {
		return false;
	}

	/**
	 * Should an ID be generated instead if the passed in {@link Element} does not
	 * specify an "id" attribute explicitly?
	 * <p>Disabled by default; subclasses can override this to enable ID generation
	 * as fallback: The parser will first check for an "id" attribute in this case,
	 * only falling back to a generated ID if no value was specified.
	 * @return whether the parser should generate an id if no id was specified
	 */
	protected boolean shouldGenerateIdAsFallback() {
		return true;
	}

	/**
	 * Controls whether this parser is supposed to fire a
	 * {@link org.springframework.beans.factory.parsing.BeanComponentDefinition}
	 * event after parsing the bean definition.
	 * <p>This implementation returns <code>true</code> by default; that is,
	 * an event will be fired when a bean definition has been completely parsed.
	 * Override this to return <code>false</code> in order to suppress the event.
	 * @return <code>true</code> in order to fire a component registration event
	 * after parsing the bean definition; <code>false</code> to suppress the event
	 * @see #postProcessComponentDefinition
	 * @see org.springframework.beans.factory.parsing.ReaderContext#fireComponentRegistered
	 */
	protected boolean shouldFireEvents() {
		return true;
	}

	/**
	 * Hook method called after the primary parsing of a
	 * {@link BeanComponentDefinition} but before the
	 * {@link BeanComponentDefinition} has been registered with a
	 * {@link org.springframework.beans.factory.support.BeanDefinitionRegistry}.
	 * <p>Derived classes can override this method to supply any custom logic that
	 * is to be executed after all the parsing is finished.
	 * <p>The default implementation is a no-op.
	 * @param componentDefinition the {@link BeanComponentDefinition} that is to be processed
	 */
	protected void postProcessComponentDefinition(BeanComponentDefinition componentDefinition) {
	}

	/**
	 * Parse the supplied {@link Element} and populate the supplied
	 * {@link BeanDefinitionBuilder} as required.
	 * <p>The default implementation delegates to the <code>doParse</code>
	 * version without ParserContext argument.
	 * @param element the XML element being parsed
	 * @param parserContext the object encapsulating the current state of the parsing process
	 * @param builder used to define the <code>BeanDefinition</code>
	 * @see #doParse(Element, BeanDefinitionBuilder)
	 */
	protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
		doParse(element, builder);
	}

	/**
	 * Parse the supplied {@link Element} and populate the supplied
	 * {@link BeanDefinitionBuilder} as required.
	 * <p>The default implementation does nothing.
	 * @param element the XML element being parsed
	 * @param builder used to define the <code>BeanDefinition</code>
	 */
	protected void doParse(Element element, BeanDefinitionBuilder builder) {
	}

}
