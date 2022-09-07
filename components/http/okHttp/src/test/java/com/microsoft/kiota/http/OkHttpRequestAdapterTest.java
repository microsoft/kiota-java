package com.microsoft.kiota.http;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.microsoft.kiota.authentication.AuthenticationProvider;
import com.microsoft.kiota.HttpMethod;
import com.microsoft.kiota.RequestInformation;
import java.util.concurrent.CompletableFuture;
import java.io.InputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import org.mockito.stubbing.Answer;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Dispatcher;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class OkHttpRequestAdapterTest {
	@ParameterizedTest
	@ValueSource(ints = {200, 201, 202, 203, 206})
	public void SendStreamReturnsUsableStream(int statusCode) throws Exception {
		final var authenticationProviderMock = mock(AuthenticationProvider.class);
		when(authenticationProviderMock.authenticateRequest(any(RequestInformation.class), any(Map.class))).thenReturn(CompletableFuture.completedFuture(null));
		final var client = getMockClient(new Response.Builder()
													.code(statusCode)
													.message("OK")
													.protocol(Protocol.HTTP_1_1)
													.request(new Request.Builder().url("http://localhost").build())
													.body(ResponseBody.create("".getBytes(), MediaType.parse("application/json")))
													.build());
		final var requestAdapter = new OkHttpRequestAdapter(authenticationProviderMock, null, null, client);
		final var requestInformation = new RequestInformation() {{
			setUri(new URI("https://localhost"));
			httpMethod = HttpMethod.GET;
		}};
		final var response = requestAdapter.sendPrimitiveAsync(requestInformation, InputStream.class, null, null).get();
		assertNotNull(response);
	}
	public static OkHttpClient getMockClient(final Response response) throws IOException {
        final OkHttpClient mockClient = mock(OkHttpClient.class);
        final Call remoteCall = mock(Call.class);
        final Dispatcher dispatcher = new Dispatcher();
        when(remoteCall.execute()).thenReturn(response);
        doAnswer((Answer<Void>) invocation -> {
            Callback callback = invocation.getArgument(0);
            callback.onResponse(null, response);
            return null;
        }).when(remoteCall)
            .enqueue(any(Callback.class));
        when(mockClient.dispatcher()).thenReturn(dispatcher);
        when(mockClient.newCall(any())).thenReturn(remoteCall);
        return mockClient;
    }
}
