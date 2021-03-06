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
package au.gov.ga.worldwind.animator.panels;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JSplitPane;

import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.application.ChangeOfAnimationListener;
import au.gov.ga.worldwind.common.ui.collapsiblesplit.CollapsibleSplitConstraints;
import au.gov.ga.worldwind.common.ui.collapsiblesplit.CollapsibleSplitPane;
import au.gov.ga.worldwind.common.ui.collapsiblesplit.l2fprod.CollapsibleGroup;

/**
 * The sidebar used in the Animator application.
 * <p/>
 * Essentially a wrapper around a {@link CollapsibleSplitPane} that controls
 * the visibility of it's container panes, and provides a listener that
 * performs tasks related to it's container panes.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 * @author Michael de Hoog (michael.deHoog@ga.gov.au)
 */
public class SideBar extends JPanel implements ChangeOfAnimationListener
{
	private static final long serialVersionUID = 20100906;

	/** The primary collapsible pane used in the sidebar */
	private CollapsibleSplitPane containerPane;
	
	/** The list groups attached to the collapsible pane */
	private List<CollapsibleGroup> groups = new ArrayList<CollapsibleGroup>();
	
	/** The panels backing the collapsible pane */
	private List<AnimatorCollapsiblePanel> panels = new ArrayList<AnimatorCollapsiblePanel>();
	
	/** 
	 * The parent of the side bar. 
	 * <p/>
	 * Used so the split pane can be disappeared if there are no groups open in the sidebar 
	 */
	private JSplitPane parent;
	
	/** The saved location of the parent divider bar. Used to re-inflate the parent split plane if needed. */
	private int savedDividerLocation;
	
	/** The saved location of the parent divider bar. Used to re-inflate the parent split plane if needed. */
	private int savedDividerSize;
	
	public SideBar(JSplitPane parent, List<AnimatorCollapsiblePanel> panels) 
	{
		super(new BorderLayout());
		
		this.parent = parent;
		savedDividerLocation = parent.getDividerLocation();
		savedDividerSize = parent.getDividerSize();

		containerPane = new CollapsibleSplitPane();
		containerPane.getLayout().setVertical(true);

		addPanels(panels);

		add(containerPane, BorderLayout.CENTER);
		
		refreshVisibility();
	}

	/**
	 * Add the provided panels to the collapsible pane
	 */
	private void addPanels(List<AnimatorCollapsiblePanel> panels)
	{
		for (AnimatorCollapsiblePanel panel : panels)
		{
			CollapsibleGroup group = new CollapsibleGroup();
			group.setIcon(panel.getIcon());
			group.setVisible(panel.isOn());
			group.setCollapsed(!panel.isExpanded());
			group.setScrollOnExpand(true);
			group.setLayout(new BorderLayout());
			group.setTitle(panel.getName());
			group.add(panel.getPanel(), BorderLayout.CENTER);

			CollapsibleSplitConstraints c = new CollapsibleSplitConstraints();
			c.collapsed = !panel.isExpanded();
			c.resizable = panel.isResizable();
			c.weight = panel.getWeight();

			containerPane.add(group, c);
			groups.add(group);
			
			this.panels.add(panel);
		}
	}

	/**
	 * Refreshes the visibility of the panels contained within the sidebar.
	 * <p/>
	 * Also updates the parent split plane to hide the split if no panels are being displayed in the side bar.
	 */
	public void refreshVisibility()
	{
		boolean anyVisible = false;
		for (CollapsibleGroup group : groups)
		{
			if (group.isVisible())
			{
				anyVisible = true;
				break;
			}
		}
		if (isVisible() != anyVisible)
		{
			setVisible(anyVisible);
			if (anyVisible)
			{
				parent.setDividerLocation(savedDividerLocation);
				parent.setDividerSize(savedDividerSize);
			}
			else
			{
				savedDividerLocation = parent.getDividerLocation();
				savedDividerSize = parent.getDividerSize();
				parent.setDividerSize(0);
			}
		}
		validate();
	}

	@Override
	public void updateAnimation(Animation newAnimation)
	{
		for (AnimatorCollapsiblePanel panel : panels)
		{
			panel.updateAnimation(newAnimation);
		}
	}
}
