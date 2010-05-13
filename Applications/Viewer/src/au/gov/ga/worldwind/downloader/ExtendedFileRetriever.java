package au.gov.ga.worldwind.downloader;

import gov.nasa.worldwind.retrieve.RetrievalPostProcessor;

import java.io.File;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;

public class ExtendedFileRetriever extends FileRetriever implements ExtendedRetriever
{
	private Long ifModifiedSince;
	private Exception error;
	private boolean notModified = false;

	public ExtendedFileRetriever(URL url, Long ifModifiedSince, RetrievalPostProcessor postProcessor)
	{
		super(url, postProcessor);
		this.ifModifiedSince = ifModifiedSince;
	}

	@Override
	protected ByteBuffer doRead(URLConnection connection) throws Exception
	{
		try
		{
			if ("file".equalsIgnoreCase(connection.getURL().getProtocol()))
			{
				notModified = checkIfModified(connection.getURL());
				if (notModified)
					return null;
			}

			return super.doRead(connection);
		}
		catch (Exception e)
		{
			error = e;
			throw e;
		}
	}

	private boolean checkIfModified(URL url)
	{
		File file = null;
		try
		{
			file = new File(url.toURI());
		}
		catch (Exception e1)
		{
			try
			{
				file = new File(url.getPath());
			}
			catch (Exception e2)
			{
			}
		}
		return file != null && file.exists() && file.lastModified() <= ifModifiedSince;
	}

	@Override
	public Exception getError()
	{
		return error;
	}

	@Override
	public boolean isNotModified()
	{
		return notModified;
	}
}