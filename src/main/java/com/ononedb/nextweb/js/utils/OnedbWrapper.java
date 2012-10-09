package com.ononedb.nextweb.js.utils;

import io.nextweb.js.utils.Wrapper;
import one.common.nodes.OneJSON;
import one.common.nodes.v01.OneJSONData;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;

public class OnedbWrapper {

	public final static Wrapper ONEJSON = new Wrapper() {

		@Override
		public Object wrap(final Object input) {
			return getJsObject(((OneJSON) input).getJSONString());
		}

		@Override
		public boolean canWrap(final Object input) {
			return input instanceof OneJSON;
		}

		@Override
		public boolean canUnwrap(final Object obj) {
			return obj instanceof JavaScriptObject || obj instanceof JSONValue;
		}

		@Override
		public Object unwrap(final Object obj) {
			final String jsonData = new JSONObject((JavaScriptObject) obj)
					.toString();
			return new OneJSONData(jsonData);
		}

	};

	public final native static JavaScriptObject getJsObject(final String json)/*-{
																				return eval("(" + json + ")");
																				}-*/;

}
