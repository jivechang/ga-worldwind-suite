package au.gov.ga.worldwind.common.util.message;

/**
 * A static accessor to get access to the message source from anywhere in the
 * application
 * 
 * @author James Navin (james.navin@ga.gov.au)
 * 
 */
public class MessageSourceAccessor
{
	static
	{
		set(new ResourceBundleMessageSource());
		addBundle("au.gov.ga.worldwind.common.data.messages.commonMessages");
	}

	private static MessageSource messageSource;

	/**
	 * @return The message source to use
	 */
	public static MessageSource get()
	{
		if (messageSource == null)
		{
			throw new IllegalStateException(
					"Message source not set. Call set() first to set the message source.");
		}

		return messageSource;
	}

	/**
	 * Set the message source to use in the application.
	 * 
	 * @param source
	 *            The message source to set.
	 */
	public static void set(MessageSource source)
	{
		messageSource = source;
	}

	/**
	 * Helper function that calls addBundle() on the {@link MessageSource}
	 * stored in this accessor.
	 * 
	 * @param name
	 *            The base name of the collection of messages to add
	 */
	public static void addBundle(String baseName)
	{
		messageSource.addBundle(baseName);
	}

	/**
	 * Helper function that calls getMessage() on the {@link MessageSource}
	 * stored in this accessor.
	 * 
	 * @param key
	 *            The key for the message to return
	 * 
	 * @return The message with the provided key, or <code>null</code> if a
	 *         message with the provided key cannot be found
	 */
	public static String getMessage(String key)
	{
		return get().getMessage(key);
	}
}
