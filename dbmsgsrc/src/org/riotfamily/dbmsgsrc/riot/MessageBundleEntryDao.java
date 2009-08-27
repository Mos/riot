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
package org.riotfamily.dbmsgsrc.riot;

import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.riotfamily.core.dao.ListParams;
import org.riotfamily.dbmsgsrc.model.MessageBundleEntry;
import org.riotfamily.dbmsgsrc.support.DbMessageSource;
import org.riotfamily.riot.hibernate.dao.AbstractHqlDao;
import org.springframework.dao.DataAccessException;

public class MessageBundleEntryDao extends AbstractHqlDao {

	private String bundle = DbMessageSource.DEFAULT_BUNDLE;

	public MessageBundleEntryDao(SessionFactory sessionFactory) {
		super(sessionFactory);
	}
	
	public void setBundle(String bundle) {
		this.bundle = bundle;
	}
	
	public String getBundle() {
		return bundle;
	}

	public Class<?> getEntityClass() {
		return MessageBundleEntry.class;
	}
	
	@Override
	protected String getWhere() {
		return "this.bundle = :bundle";
	}
	
	@Override
	protected void setQueryParameters(Query query, Object parent, ListParams params) {
		super.setQueryParameters(query, parent, params);
		query.setParameter("bundle", bundle);
	}

	@Override
	public void save(Object entity, Object parent) throws DataAccessException {
		MessageBundleEntry entry = (MessageBundleEntry) entity;
		entry.setBundle(bundle);
		super.save(entity, parent);
	}
}