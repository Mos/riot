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
 * Portions created by the Initial Developer are Copyright (C) 2008
 * the Initial Developer. All Rights Reserved.
 * 
 * Contributor(s):
 *   Jan-Frederic Linde [jfl at neteye dot de]
 * 
 * ***** END LICENSE BLOCK ***** */
package org.riotfamily.media.setup;

import java.io.IOException;

import org.riotfamily.media.model.RiotFile;
import org.riotfamily.media.model.RiotVideo;
import org.springframework.core.io.Resource;

/**
 * @author Jan-Frederic Linde [jfl at neteye dot de]
 * @since 7.0
 */
public class RiotVideoFactoryBean extends AbstractRiotFileFactoryBean {
	
	public Class<?> getObjectType() {
		return RiotVideo.class;
	}
	
	protected RiotFile createRiotFile(Resource resource) throws IOException {
		return new RiotVideo(resource.getFile());
	}

}
