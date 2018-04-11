/**
 * Copyright (C) 2010-2018 Structr GmbH
 *
 * This file is part of Structr <http://structr.org>.
 *
 * Structr is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Structr is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Structr.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.structr.flow.engine;

import java.util.LinkedHashMap;
import java.util.Map;
import org.structr.core.GraphObject;

/**
 *
 */
public class Context {

	private Map<String, Object> data = new LinkedHashMap<>();
	private GraphObject thisObject   = null;
	private Object result            = null;
	private FlowError error          = null;

	public Context() {
		this(null);
	}

	public Context(final GraphObject thisObject) {
		this.thisObject = thisObject;
	}

	public Context(final GraphObject thisObject, final Map<String, Object> data) {
		this.thisObject = thisObject;
		this.data.putAll(data);
	}

	public GraphObject getThisObject() {
		return thisObject;
	}

	public void error(final FlowError error) {
		this.error = error;
	}

	public void setResult(final Object result) {
		this.result = result;
	}

	public Object getResult() {
		return result;
	}

	public FlowError getError() {
		return error;
	}

	public boolean hasResult() {
		return result != null;
	}

	public boolean hasError() {
		return error != null;
	}

	public void setData(final String key, final Object value) {
		data.put(key, value);
	}

	public Object getData(final String key) {
		return data.get(key);
	}
}
