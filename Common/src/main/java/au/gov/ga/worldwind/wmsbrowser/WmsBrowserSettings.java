/*******************************************************************************
 * Copyright 2012 Geoscience Australia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package au.gov.ga.worldwind.wmsbrowser;

import static au.gov.ga.worldwind.common.util.Util.isBlank;
import gov.nasa.worldwind.util.WWXML;

import java.awt.Dimension;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.xpath.XPath;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import au.gov.ga.worldwind.common.util.Util;
import au.gov.ga.worldwind.common.util.XMLUtil;
import au.gov.ga.worldwind.wmsbrowser.wmsserver.WmsServerIdentifier;
import au.gov.ga.worldwind.wmsbrowser.wmsserver.WmsServerIdentifierImpl;


/**
 * A class used to retrieve and persist WMS browser settings between
 * invocations.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class WmsBrowserSettings
{
	/** The settings file name to use */
	private static final String SETTINGS_FILE_NAME = "wmsBrowser.xml";
	
	/** The Singleton Settings instance */
	private static WmsBrowserSettings instance;
	
	public static WmsBrowserSettings get()
	{
		if (instance == null)
		{
			loadSettings();
		}
		return instance;
	}

	public static void save()
	{
		if (instance == null)
		{
			return;
		}
		
		try
		{
			Document document = WWXML.createDocumentBuilder(false).newDocument();
		
			Element rootElement = document.createElement("wmsBrowserSettings");
			document.appendChild(rootElement);
			
			saveSplitLocation(rootElement);
			saveWindowSize(rootElement);
			saveWmsServerLocations(rootElement);
			saveCswCatalogueServers(rootElement);
			saveMaxNumberCswSearchResultsPerService(rootElement);
			
			XMLUtil.saveDocumentToFormattedStream(document, new FileOutputStream(new File(Util.getUserGAWorldWindDirectory(), SETTINGS_FILE_NAME)));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private static void saveSplitLocation(Element rootElement)
	{
		Element splitLocationElement = WWXML.appendElement(rootElement, "splitLocation");
		WWXML.setIntegerAttribute(splitLocationElement, "value", instance.getSplitLocation());
	}

	private static void saveWindowSize(Element rootElement)
	{
		Element windowSizeElement = WWXML.appendElement(rootElement, "windowSize");
		WWXML.setIntegerAttribute(windowSizeElement, "width", instance.getWindowDimension().width);
		WWXML.setIntegerAttribute(windowSizeElement, "height", instance.getWindowDimension().height);
	}
	
	private static void saveWmsServerLocations(Element rootElement)
	{
		Element serverLocationsContainer = WWXML.appendElement(rootElement, "serverLocations");
		List<WmsServerIdentifier> servers = instance.getWmsServers();
		for (int i = 0; i < servers.size(); i++)
		{
			Element layerElement = WWXML.appendElement(serverLocationsContainer, "server");
			WWXML.setIntegerAttribute(layerElement, "index", i);
			WWXML.setTextAttribute(layerElement, "name", servers.get(i).getName());
			WWXML.setTextAttribute(layerElement, "url", servers.get(i).getCapabilitiesUrl().toExternalForm());
		}
	}
	
	private static void saveCswCatalogueServers(Element rootElement)
	{
		Element cswCataloguesContainer = WWXML.appendElement(rootElement, "cswCatalogues");
		List<URL> servers = instance.getCswCatalogueServers();
		for (int i = 0; i < servers.size(); i++)
		{
			Element layerElement = WWXML.appendElement(cswCataloguesContainer, "server");
			WWXML.setIntegerAttribute(layerElement, "index", i);
			WWXML.setTextAttribute(layerElement, "url", servers.get(i).toExternalForm());
		}
	}
	
	private static void saveMaxNumberCswSearchResultsPerService(Element rootElement)
	{
		Element splitLocationElement = WWXML.appendElement(rootElement, "maxNumberWmsSearchResultsPerService");
		WWXML.setIntegerAttribute(splitLocationElement, "value", instance.getMaxNumberWmsSearchResultsPerService());
	}
	
	private static void loadSettings()
	{
		instance = new WmsBrowserSettings();
		
		// If no file is detected, continue with the vanilla instance
		File settingsFile = new File(Util.getUserGAWorldWindDirectory(), SETTINGS_FILE_NAME);
		if (!settingsFile.exists())
		{
			return;
		}
		
		// Otherwise load the settings from the file
		Document xmlDocument = WWXML.openDocument(settingsFile);
		Element rootElement = xmlDocument.getDocumentElement();
		XPath xpath = WWXML.makeXPath();
		
		loadSplitLocation(rootElement, xpath);
		loadWindowSize(rootElement, xpath);
		loadWmsServerLocations(rootElement, xpath);
		loadCswCatalogueServers(rootElement, xpath);
		loadMaxNumberCswSearchResultsPerService(rootElement, xpath);
	}
	
	private static void loadSplitLocation(Element rootElement, XPath xpath)
	{
		Integer splitLocation = WWXML.getInteger(rootElement, "//splitLocation/@value", xpath);
		if (splitLocation != null)
		{
			instance.setSplitLocation(splitLocation);
		}
	}
	
	private static void loadWindowSize(Element rootElement, XPath xpath)
	{
		Integer width = WWXML.getInteger(rootElement, "//windowSize/@width", xpath);
		Integer height = WWXML.getInteger(rootElement, "//windowSize/@height", xpath);
		if (width != null && height != null)
		{
			instance.setWindowDimension(new Dimension(width, height));
		}
	}
	
	private static void loadWmsServerLocations(Element rootElement, XPath xpath)
	{
		List<WmsServerIdentifier> servers = new ArrayList<WmsServerIdentifier>();
		Integer serverCount = WWXML.getInteger(rootElement, "count(//serverLocations/server)", null);
		for (int i = 0; i < serverCount; i++)
		{
			String serverName = WWXML.getText(rootElement, "//serverLocations/server[@index='" + i + "']/@name");
			String serverLocation = WWXML.getText(rootElement, "//serverLocations/server[@index='" + i + "']/@url");
			if (!isBlank(serverLocation))
			{
				URL url = toUrl(serverLocation);
				if (url != null)
				{
					servers.add(new WmsServerIdentifierImpl(serverName, url));
				}
			}
		}
		if (!servers.isEmpty())
		{
			instance.setWmsServers(servers);
		}
	}

	private static void loadCswCatalogueServers(Element rootElement, XPath xpath)
	{
		List<URL> servers = new ArrayList<URL>();
		Integer serverCount = WWXML.getInteger(rootElement, "count(//cswCatalogues/server)", null);
		for (int i = 0; i < serverCount; i++)
		{
			String serverLocation = WWXML.getText(rootElement, "//cswCatalogues/server[@index='" + i + "']/@url");
			if (!isBlank(serverLocation))
			{
				URL url = toUrl(serverLocation);
				if (url != null)
				{
					servers.add(url);
				}
			}
		}
		if (!servers.isEmpty())
		{
			instance.setCswCatalogueServers(servers);
		}
	}
	
	private static void loadMaxNumberCswSearchResultsPerService(Element rootElement, XPath xpath)
	{
		Integer maxNumberSearchResults = WWXML.getInteger(rootElement, "//maxNumberWmsSearchResultsPerService/@value", xpath);
		if (maxNumberSearchResults != null)
		{
			instance.setMaxNumberWmsSearchResultsPerService(maxNumberSearchResults);
		}
	}
	
	private static URL toUrl(String serverLocation)
	{
		try
		{
			return new URL(serverLocation);
		}
		catch (Exception e)
		{
			return null;
		}
	}

	// ----------------------------------
	// Instance members
	// ----------------------------------

	/** The location of the split pane split bar */
	private int splitLocation = 300;
	
	private Dimension windowDimension = new Dimension(768, 640);
	
	private List<WmsServerIdentifier> wmsServers;
	
	private List<URL> cswCatalogueServers;
	
	private int maxNumberWmsSearchResultsPerService = 50;
	
	/**
	 * Private constructor. Obtain a {@link WmsBrowserSettings} instance using the static {@link #get()} method.
	 */
	private WmsBrowserSettings()
	{
		try
		{
			wmsServers = new ArrayList<WmsServerIdentifier>(Arrays.asList(new WmsServerIdentifier[]{
					//new WmsServerIdentifierImpl("Geoscience Australia national geoscience datasets", new URL("http://www.ga.gov.au/wms/getmap?dataset=national&request=getCapabilities")),
					//new WmsServerIdentifierImpl("Geoscience Australia Map Connect (1:250k)", new URL("http://mapconnect.ga.gov.au/wmsconnector/com.esri.wms.Esrimap?Version=1.1.1&Request=getcapabilities&Service=WMS&Servicename=GDA94_MapConnect_SDE_250kmap_WMS&")),
					new WmsServerIdentifierImpl("NASA Earth Observations", new URL("http://neowms.sci.gsfc.nasa.gov/wms/wms")),
			}));
			
			cswCatalogueServers = new ArrayList<URL>(Arrays.asList(new URL[]{
				new URL("http://catalog.geodata.gov/geoportal/csw/discovery?"),	
			}));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public int getSplitLocation()
	{
		return splitLocation;
	}
	
	public void setSplitLocation(int splitLocation)
	{
		this.splitLocation = splitLocation;
	}

	public List<WmsServerIdentifier> getWmsServers()
	{
		return wmsServers;
	}
	
	public void addWmsServer(WmsServerIdentifier identifier)
	{
		if (identifier == null || wmsServers.contains(identifier))
		{
			return;
		}
		wmsServers.add(identifier);
	}
	
	public void setWmsServers(List<WmsServerIdentifier> identifiers)
	{
		this.wmsServers = identifiers;
	}
	
	public List<URL> getCswCatalogueServers()
	{
		return cswCatalogueServers;
	}
	
	public void addCswCatalogueServer(URL serverUrl)
	{
		if (serverUrl == null || cswCatalogueServers.contains(serverUrl))
		{
			return;
		}
		cswCatalogueServers.add(serverUrl);
	}
	
	public void setCswCatalogueServers(List<URL> cswCatalogueServers)
	{
		this.cswCatalogueServers = cswCatalogueServers;
	}
	
	public Dimension getWindowDimension()
	{
		return windowDimension;
	}

	public void setWindowDimension(Dimension windowDimension)
	{
		this.windowDimension = windowDimension;
	}
	
	public int getMaxNumberWmsSearchResultsPerService()
	{
		return maxNumberWmsSearchResultsPerService;
	}
	
	public void setMaxNumberWmsSearchResultsPerService(int maxNumberCswSearchResultsPerService)
	{
		this.maxNumberWmsSearchResultsPerService = maxNumberCswSearchResultsPerService;
	}
	
}
