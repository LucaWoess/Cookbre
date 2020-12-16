import java.io.IOException;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

public class APIWrapper {
	public Response getRecipe(String URL) throws IOException {
		OkHttpClient client = new OkHttpClient();

		MediaType mediaType = MediaType.parse("application/xml");
		RequestBody body = RequestBody.create(mediaType, "URL");
		Request request = new Request.Builder()
			.url("https://mycookbook-io1.p.rapidapi.com/recipes/rapidapi")
			.post(body)
			.addHeader("content-type", "application/xml")
			.addHeader("x-rapidapi-key", "50dbb1f6fdmsh7d70313a69611f0p163dcejsne9eb67b73c45")
			.addHeader("x-rapidapi-host", "mycookbook-io1.p.rapidapi.com")
			.build();

		return client.newCall(request).execute();
	}
}
