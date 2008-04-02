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
package org.riotfamily.forms.element.core;

import java.net.URL;

import org.riotfamily.forms.element.DHTMLElement;
import org.riotfamily.forms.resource.FormResource;
import org.riotfamily.forms.resource.ResourceElement;
import org.riotfamily.forms.resource.ScriptResource;
import org.riotfamily.forms.support.TemplateUtils;


/**
 * A WYSIWYG richtext editor based on TinyMCE. 
 */
public class TinyMCE extends Textarea implements ResourceElement, DHTMLElement {

	public TinyMCE() {
	}
	
	public FormResource getResource() {
		return new ScriptResource("tiny_mce/strict_mode_fix.js", "tinyMCE.addControl",
				new ScriptResource("tiny_mce/tiny_mce_src.js", "tinyMCE"));
	}
	
	public String getLanguage() {
		String lang = getFormContext().getLocale().getLanguage().toLowerCase();
		URL languageScript = getClass().getResource(
				"/org/riotfamily/resources/tiny_mce/langs/"	+ lang + ".js");
		
		if (languageScript == null) {
			lang = "en";
		}
		return lang;
	}
	
	public String getInitScript() {
		return TemplateUtils.getInitScript(this);
	}

}