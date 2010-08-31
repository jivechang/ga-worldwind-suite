package au.gov.ga.worldwind.common.downloader;

import gov.nasa.worldwind.retrieve.HTTPRetriever;
import gov.nasa.worldwind.retrieve.RetrievalPostProcessor;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;

/**
 * Extension of {@link HTTPRetriever} which implements {@link ExtendedRetriever}
 * .
 * 
 * @author Michael de Hoog
 */
public class ExtendedHTTPRetriever extends HTTPRetriever implements ExtendedRetriever
{
	private Long ifModifiedSince;
	private Exception error;

	public ExtendedHTTPRetriever(URL url, Long ifModifiedSince, RetrievalPostProcessor postProcessor)
	{
		super(url, postProcessor);
		this.ifModifiedSince = ifModifiedSince;
	}

	@Override
	protected ByteBuffer doRead(URLConnection connection) throws Exception
	{
		//overridden to catch exceptions and set the modification date in the URLConnection

		if (ifModifiedSince != null)
			connection.setIfModifiedSince(ifModifiedSince.longValue());
		try
		{
			ByteBuffer buffer = super.doRead(connection);
			if (buffer == null && !isOk() && !isNotModified())
			{
				throw new HttpException(getResponseCode() + ": " + getResponseMessage(),
						getResponseCode());
			}
			return buffer;
		}
		catch (Exception e)
		{
			error = e;
			throw e;
		}
	}

	/**
	 * Was the response status code from the server a HTTP_OK (200)?
	 * 
	 * @return True if the http server returned status 200
	 */
	public boolean isOk()
	{
		return getResponseCode() == HttpURLConnection.HTTP_OK;
	}

	@Override
	public boolean isNotModified()
	{
		return getResponseCode() == HttpURLConnection.HTTP_NOT_MODIFIED;
	}

	@Override
	public Exception getError()
	{
		return error;
	}
}