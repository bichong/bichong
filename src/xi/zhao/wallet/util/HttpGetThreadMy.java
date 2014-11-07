package xi.zhao.wallet.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.annotation.Nonnull;
import javax.net.ssl.HttpsURLConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xi.zhao.wallet.Constants;

public abstract class HttpGetThreadMy extends Thread{

	private final String url;

	private static final Logger log = LoggerFactory.getLogger(HttpGetThread.class);

	public HttpGetThreadMy( @Nonnull final String url)
	{
		this.url = url;
	}

	@Override
	public void run()
	{
		HttpURLConnection connection = null;

		log.debug("querying \"" + url + "\"...");

		try
		{
			connection = (HttpURLConnection) new URL(url).openConnection();
			connection.setConnectTimeout(Constants.HTTP_TIMEOUT_MS);
			connection.setReadTimeout(Constants.HTTP_TIMEOUT_MS);
			connection.setRequestProperty("Accept-Charset", "utf-8");
			connection.connect();

			if (connection.getResponseCode() == HttpURLConnection.HTTP_OK)
			{
				final long serverTime = connection.getDate();
				// TODO parse connection.getContentType() for charset

				final BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), Constants.UTF_8), 64);
				final String line = reader.readLine().trim();
				reader.close();

				handleLine(line, serverTime);
			}
		}
		catch (final Exception x)
		{
			handleException(x);
		}
		finally
		{
			if (connection != null)
				connection.disconnect();
		}
	}

	protected abstract void handleLine(@Nonnull String line, long serverTime);

	protected abstract void handleException(@Nonnull Exception x);

}
