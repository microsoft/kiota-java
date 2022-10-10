package com.microsoft.kiota;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ResponseHandlerOption implements RequestOption {
	private ResponseHandler responseHandler;

	@Nullable
	public ResponseHandler getResponseHandler() {
		return responseHandler;
	}
	public void setResponseHandler(@Nullable final ResponseHandler value) {
		responseHandler = value;
	}

	@Override
	@Nonnull
	public <T extends RequestOption> Class<T> getType() {
		return (Class<T>) ResponseHandlerOption.class;
	}
	
}
