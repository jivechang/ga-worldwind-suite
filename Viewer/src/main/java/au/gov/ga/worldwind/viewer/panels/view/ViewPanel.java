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
package au.gov.ga.worldwind.viewer.panels.view;

import gov.nasa.worldwind.Disposable;
import gov.nasa.worldwind.View;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVKey;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import au.gov.ga.worldwind.common.util.Icons;
import au.gov.ga.worldwind.common.view.hmd.oculus.OculusView;
import au.gov.ga.worldwind.common.view.stereo.StereoFlyView;
import au.gov.ga.worldwind.common.view.stereo.StereoFreeView;
import au.gov.ga.worldwind.common.view.stereo.StereoOrbitView;
import au.gov.ga.worldwind.common.view.stereo.StereoSubSurfaceOrbitView;
import au.gov.ga.worldwind.viewer.theme.AbstractThemePanel;
import au.gov.ga.worldwind.viewer.theme.Theme;
import au.gov.ga.worldwind.viewer.theme.ThemePanel;

/**
 * {@link ThemePanel} used to switch between different view types.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class ViewPanel extends AbstractThemePanel
{
	private WorldWindow wwd;

	private JRadioButton orbitRadio;
	private JRadioButton subSurfaceRadio;
	private JRadioButton flyRadio;
	private JRadioButton freeRadio;
	private JRadioButton oculusRadio;

	public ViewPanel()
	{
		super(new GridBagLayout());
		setResizable(false);
		GridBagConstraints c;
		JPanel panel;

		panel = new JPanel(new GridBagLayout());
		c = new GridBagConstraints();
		c.weightx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		add(panel, c);

		ButtonGroup bg = new ButtonGroup();
		ActionListener al = new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				setupView();
			}
		};

		orbitRadio = new JRadioButton("Orbit");
		bg.add(orbitRadio);
		orbitRadio.addActionListener(al);
		c = new GridBagConstraints();
		c.gridx = 0;
		c.weightx = 1d / 5d;
		panel.add(orbitRadio, c);

		subSurfaceRadio = new JRadioButton("Sub-surface");
		bg.add(subSurfaceRadio);
		subSurfaceRadio.addActionListener(al);
		c = new GridBagConstraints();
		c.gridx = 1;
		c.weightx = 1d / 5d;
		panel.add(subSurfaceRadio, c);

		flyRadio = new JRadioButton("Fly");
		bg.add(flyRadio);
		flyRadio.addActionListener(al);
		c = new GridBagConstraints();
		c.gridx = 2;
		c.weightx = 1d / 5d;
		panel.add(flyRadio, c);

		freeRadio = new JRadioButton("Free");
		bg.add(freeRadio);
		freeRadio.addActionListener(al);
		c = new GridBagConstraints();
		c.gridx = 3;
		c.weightx = 1d / 5d;
		panel.add(freeRadio, c);

		oculusRadio = new JRadioButton("Oculus");
		bg.add(oculusRadio);
		oculusRadio.addActionListener(al);
		c = new GridBagConstraints();
		c.gridx = 4;
		c.weightx = 1d / 5d;
		panel.add(oculusRadio, c);
	}

	@Override
	public Icon getIcon()
	{
		return Icons.view.getIcon();
	}

	@Override
	public void setup(Theme theme)
	{
		wwd = theme.getWwd();
		wwd.getSceneController().addPropertyChangeListener(AVKey.VIEW, new PropertyChangeListener()
		{
			@Override
			public void propertyChange(PropertyChangeEvent evt)
			{
				updateRadioButtons();
			}
		});
		updateRadioButtons();
	}

	@Override
	public void dispose()
	{
	}

	protected void updateRadioButtons()
	{
		View view = wwd.getView();
		if (view instanceof StereoSubSurfaceOrbitView)
		{
			subSurfaceRadio.setSelected(true);
		}
		else if (view instanceof StereoOrbitView)
		{
			orbitRadio.setSelected(true);
		}
		else if (view instanceof StereoFlyView)
		{
			flyRadio.setSelected(true);
		}
		else if (view instanceof StereoFreeView)
		{
			freeRadio.setSelected(true);
		}
		else if (view instanceof OculusView)
		{
			oculusRadio.setSelected(true);
		}
	}

	protected void setupView()
	{
		if (wwd == null)
			return;

		View oldView = wwd.getView();
		View view = null;

		if (orbitRadio.isSelected() && !(oldView instanceof StereoOrbitView))
		{
			view = new StereoOrbitView();
		}
		else if (subSurfaceRadio.isSelected() && !(oldView instanceof StereoSubSurfaceOrbitView))
		{
			view = new StereoSubSurfaceOrbitView();
		}
		else if (flyRadio.isSelected() && !(oldView instanceof StereoFlyView))
		{
			view = new StereoFlyView();
		}
		else if (freeRadio.isSelected() && !(oldView instanceof StereoFreeView))
		{
			view = new StereoFreeView();
		}
		else if (oculusRadio.isSelected() && !(oldView instanceof OculusView))
		{
			view = new OculusView();
		}

		if (view == null)
			return;

		view.copyViewState(oldView);
		wwd.setView(view);
		wwd.redraw();

		if (oldView instanceof Disposable)
		{
			((Disposable) oldView).dispose();
		}
	}
}
