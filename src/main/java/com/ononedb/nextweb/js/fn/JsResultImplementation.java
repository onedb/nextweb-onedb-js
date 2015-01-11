package com.ononedb.nextweb.js.fn;

import io.nextweb.Session;
import io.nextweb.operations.callbacks.CallbackFactory;
import io.nextweb.promise.NextwebOperation;
import io.nextweb.promise.Fn;
import io.nextweb.promise.NextwebPromise;
import io.nextweb.promise.callbacks.Callback;
import io.nextweb.promise.exceptions.ExceptionListener;
import io.nextweb.promise.exceptions.ExceptionManager;
import io.nextweb.promise.exceptions.ExceptionResult;
import io.nextweb.promise.exceptions.ImpossibleListener;
import io.nextweb.promise.exceptions.ImpossibleResult;
import io.nextweb.promise.exceptions.UnauthorizedListener;
import io.nextweb.promise.exceptions.UnauthorizedResult;
import io.nextweb.promise.exceptions.UndefinedListener;
import io.nextweb.promise.exceptions.UndefinedResult;

import java.util.LinkedList;
import java.util.List;

import one.core.domain.OneClient;
import one.core.dsl.callbacks.WhenCommitted;
import one.core.dsl.callbacks.results.WithCommittedResult;

import com.ononedb.nextweb.OnedbSession;

import de.mxro.fn.Closure;

public class JsResultImplementation<ResultType> implements NextwebPromise<ResultType> {

	private final NextwebOperation<ResultType> asyncResult;

	private ResultType resultCache;

	private final ExceptionManager exceptionManager;

	private boolean requestingResult;

	private final List<Callback<ResultType>> deferredCalls;

	private final Session session;

	private void requestResult(final Callback<ResultType> callback) {

		if (resultCache != null) {
			callback.onSuccess(resultCache);
			return;
		}

		if (requestingResult) {
			deferredCalls.add(callback);
			return;
		}

		requestingResult = true;

		asyncResult.apply(CallbackFactory
				.eagerCallback(session, exceptionManager,
						new Closure<ResultType>() {

							@Override
							public void apply(final ResultType result) {
								resultCache = result;
								requestingResult = false;
								callback.onSuccess(result);

								for (final Callback<ResultType> deferredCallback : deferredCalls) {
									deferredCallback.onSuccess(result);
								}
								deferredCalls.clear();
							}

						}).catchExceptions(new ExceptionListener() {

					@Override
					public void onFailure(final ExceptionResult r) {
						requestingResult = false;
						callback.onFailure(r);
						for (final Callback<ResultType> deferredCallback : deferredCalls) {
							deferredCallback.onFailure(r);
						}
						deferredCalls.clear();
					}
				}).catchUnauthorized(new UnauthorizedListener() {

					@Override
					public void onUnauthorized(final UnauthorizedResult r) {
						requestingResult = false;
						callback.onUnauthorized(r);
						for (final Callback<ResultType> deferredCallback : deferredCalls) {
							deferredCallback.onUnauthorized(r);
						}
						deferredCalls.clear();
					}
				}).catchUndefined(new UndefinedListener() {

					@Override
					public void onUndefined(final UndefinedResult r) {
						requestingResult = false;
						callback.onUndefined(r);
						for (final Callback<ResultType> deferredCallback : deferredCalls) {
							deferredCallback.onUndefined(r);
						}
						deferredCalls.clear();
					}
				}).catchImpossible(new ImpossibleListener() {

					@Override
					public void onImpossible(final ImpossibleResult ir) {
						requestingResult = false;
						callback.onImpossible(ir);
						for (final Callback<ResultType> deferredCallback : deferredCalls) {
							deferredCallback.onImpossible(ir);
						}
						deferredCalls.clear();

					}
				}));

		if (session != null) {

			final OneClient client = ((OnedbSession) session).getClient();

			client.one().commit(client).and(new WhenCommitted() {

				@Override
				public void thenDo(final WithCommittedResult r) {

				}

				@Override
				public void onFailure(final Throwable t) {
					exceptionManager.onFailure(Fn.exception(this, t));
				}

			});
		}

	}

	@Override
	public ExceptionManager getExceptionManager() {
		return exceptionManager;
	}

	@Override
	public void apply(final Callback<ResultType> callback) {
		requestResult(callback);
	}

	/**
	 * Will trigger a request
	 */
	@Override
	public ResultType get() {

		requestResult(CallbackFactory.lazyCallback(session, exceptionManager,
				new Closure<ResultType>() {

					@Override
					public void apply(final ResultType o) {
						// nothing
					}
				}));

		return this.resultCache;
	}

	public JsResultImplementation(final Session session,
			final ExceptionManager fallbackExceptionManager,
			final NextwebOperation<ResultType> asyncResult) {
		super();
		assert asyncResult != null;
		this.session = session;
		this.asyncResult = asyncResult;
		this.resultCache = null;
		this.exceptionManager = new ExceptionManager(fallbackExceptionManager);
		this.requestingResult = false;
		this.deferredCalls = new LinkedList<Callback<ResultType>>();
	}

	@Override
	public void get(final Closure<ResultType> callback) {
		apply(CallbackFactory.lazyCallback(session, exceptionManager,
				new Closure<ResultType>() {

					@Override
					public void apply(final ResultType o) {
						callback.apply(o);
					}
				}));

	}

	@Override
	public NextwebPromise<ResultType> catchImpossible(final ImpossibleListener listener) {
		this.exceptionManager.catchImpossible(listener);
		return this;
	}

	@Override
	public NextwebPromise<ResultType> catchUndefined(final UndefinedListener listener) {
		this.exceptionManager.catchUndefined(listener);
		return this;
	}

	@Override
	public NextwebPromise<ResultType> catchUnauthorized(
			final UnauthorizedListener listener) {
		this.exceptionManager.catchUnauthorized(listener);
		return this;
	}

	@Override
	public NextwebPromise<ResultType> catchExceptions(final ExceptionListener listener) {
		this.exceptionManager.catchExceptions(listener);
		return this;
	}

	@Override
	public NextwebOperation<ResultType> getDecoratedResult() {

		return this.asyncResult;
	}

}
