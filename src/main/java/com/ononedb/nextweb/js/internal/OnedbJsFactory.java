package com.ononedb.nextweb.js.internal;

import io.nextweb.Session;
import io.nextweb.promise.NextwebOperation;
import io.nextweb.promise.NextwebPromise;
import io.nextweb.promise.exceptions.NextwebExceptionManager;

import com.ononedb.nextweb.common.OnedbFactory;
import com.ononedb.nextweb.js.fn.JsResultImplementation;

import de.mxro.promise.Promise;

public class OnedbJsFactory extends OnedbFactory {

    @Override
    public <ResultType> NextwebPromise<ResultType> createPromise(final NextwebExceptionManager exceptionManager,
            final Session session, final NextwebOperation<ResultType> asyncResult) {
        return new JsResultImplementation<ResultType>(session, exceptionManager, asyncResult);
    }

    @Override
    public <ResultType> Promise<ResultType> createPromiseNew(final NextwebExceptionManager exceptionManager,
            final Session session, final NextwebOperation<ResultType> asyncResult) {
        throw new RuntimeException("Not supported yet.");
    }
}
