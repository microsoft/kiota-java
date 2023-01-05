package com.microsoft.kiota.http.middleware;

import java.io.IOException;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.microsoft.kiota.http.middleware.options.UserAgentHandlerOption;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/** Adds the current library version as a product to the user agent header */
public class UserAgentHandler implements Interceptor {
	@Nonnull
	private final UserAgentHandlerOption _userAgentOption;
	/**
	 * Creates a new instance of the user agent handler with the default options
	 */
	public UserAgentHandler() {
		this(null);
	}
	/**
	 * Creates a new instance of the user agent handler with the provided options
	 * @param userAgentHandlerOption the options to use
	 */
	public UserAgentHandler(@Nullable UserAgentHandlerOption userAgentHandlerOption) {
		if (userAgentHandlerOption == null) {
			_userAgentOption = new UserAgentHandlerOption();
		} else {
			_userAgentOption = userAgentHandlerOption;
		}
	}
	private final static String USER_AGENT_HEADER_NAME = "User-Agent";
	/* @inheritdoc */
	@Override
	public Response intercept(Chain chain) throws IOException {
		Objects.requireNonNull(chain, "parameter chain cannot be null");
        final Request request = chain.request();

		UserAgentHandlerOption userAgentHandlerOption = request.tag(UserAgentHandlerOption.class);
        if(userAgentHandlerOption == null) { userAgentHandlerOption = this._userAgentOption; }

		if (!userAgentHandlerOption.getEnabled()) return chain.proceed(request);

		String currentValue = request.headers().get(USER_AGENT_HEADER_NAME);
		final String valueToAppend = String.format("%s/%s", userAgentHandlerOption.getProductName(), userAgentHandlerOption.getProductVersion());
		final Request.Builder builder = request.newBuilder();
		if (currentValue == null || currentValue.isEmpty()) {
			builder.header(USER_AGENT_HEADER_NAME, valueToAppend);
		} else if (!currentValue.contains(valueToAppend)) {
			builder.header(USER_AGENT_HEADER_NAME, String.format("%s %s", currentValue, valueToAppend));
		}
		return chain.proceed(builder.build());
	}
	
}
