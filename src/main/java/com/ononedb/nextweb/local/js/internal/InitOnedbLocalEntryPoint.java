package com.ononedb.nextweb.local.js.internal;

import org.timepedia.exporter.client.ExporterUtil;

import com.google.gwt.core.client.GWT;
import com.ononedb.nextweb.js.OnedbNextwebJsEngineImpl;
import com.ononedb.nextweb.local.js.OnedbLocal;

public class InitOnedbLocalEntryPoint {

	public void onModuleLoad() {
		ExporterUtil.exportAll();
		final OnedbNextwebJsEngineImpl engineImpl = OnedbNextwebJsEngineImpl
				.init();

		GWT.create(OnedbLocal.class);

		OnedbLocal.injectStartServerCapability(engineImpl);

		onLoadImpl();

		// ExporterUtil.exportAll();
	}

	/**
	 * Check if a callback method has been specified and call upon successful
	 * load.
	 */
	private native void onLoadImpl() /*-{
										var callOnLoad = function() {
										if ($wnd.onNextweb && typeof $wnd.onNextweb == 'function') {
										$wnd.onNextweb();
										return true;
										}
										return false;
										};

										if (callOnLoad()) {
										return;
										}

										var myint;
										myint = setInterval(function() {
										if (callOnLoad()) {
										clearInterval(myint);
										}
										}, 20);

										}-*/;

}
