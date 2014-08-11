package com.ononedb.nextweb.js.internal;

import io.nextweb.Session;
import io.nextweb.promise.Deferred;
import io.nextweb.promise.Result;
import io.nextweb.promise.exceptions.ExceptionManager;

import com.ononedb.nextweb.common.OnedbFactory;
import com.ononedb.nextweb.js.fn.JsResultImplementation;

public class OnedbJsFactory extends OnedbFactory {

	@Override
	public <ResultType> Result<ResultType> createResult(
			final ExceptionManager exceptionManager, final Session session,
			final Deferred<ResultType> asyncResult) {
		return new JsResultImplementation<ResultType>(session,
				exceptionManager, asyncResult);
	}

}
