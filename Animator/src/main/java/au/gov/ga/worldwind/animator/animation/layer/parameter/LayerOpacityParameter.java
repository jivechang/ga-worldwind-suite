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
package au.gov.ga.worldwind.animator.animation.layer.parameter;

import static au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants.getOpacityParameterNameKey;
import static au.gov.ga.worldwind.common.util.message.MessageSourceAccessor.getMessage;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.layers.AbstractLayer;
import gov.nasa.worldwind.layers.Layer;

import org.w3c.dom.Element;

import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.animation.annotation.EditableParameter;
import au.gov.ga.worldwind.animator.animation.io.AnimationFileVersion;
import au.gov.ga.worldwind.animator.animation.io.AnimationIOConstants;
import au.gov.ga.worldwind.animator.animation.parameter.ParameterBase;
import au.gov.ga.worldwind.animator.animation.parameter.ParameterValue;
import au.gov.ga.worldwind.animator.animation.parameter.ParameterValueFactory;
import au.gov.ga.worldwind.common.util.Validate;

/**
 * A {@link LayerParameter} that controls the opacity of an
 * {@link AbstractLayer}.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
@EditableParameter(bound = true, minValue = 0.0, maxValue = 1.0)
public class LayerOpacityParameter extends LayerParameterBase
{
	private static final long serialVersionUID = 20100907L;

	public LayerOpacityParameter(Animation animation, Layer layer)
	{
		this(null, animation, layer);
	}

	/**
	 * Constructor.
	 */
	public LayerOpacityParameter(String name, Animation animation, Layer layer)
	{
		super(name, animation, layer);
		setDefaultValue(1.0);
	}

	@SuppressWarnings("unused")
	private LayerOpacityParameter()
	{
	}
	
	@Override
	protected String getDefaultName()
	{
		return getMessage(getOpacityParameterNameKey());
	}

	@Override
	public Type getType()
	{
		return Type.OPACITY;
	}

	@Override
	public void doApplyValue(double value)
	{
		getLayer().setOpacity(value);
	}

	@Override
	public ParameterValue getCurrentValue()
	{
		return ParameterValueFactory.createParameterValue(this, getLayer().getOpacity(), animation.getCurrentFrame());
	}

	@Override
	protected ParameterBase createParameterFromXml(String name, Animation animation, Element element,
			Element parameterElement, AnimationFileVersion version, AVList context)
	{
		AnimationIOConstants constants = version.getConstants();
		Layer parameterLayer = (Layer) context.getValue(constants.getCurrentLayerKey());
		Validate.notNull(parameterLayer,
				"No layer found in the context. Expected one under the key '" + constants.getCurrentLayerKey() + "'.");

		return new LayerOpacityParameter(name, animation, parameterLayer);
	}
}
