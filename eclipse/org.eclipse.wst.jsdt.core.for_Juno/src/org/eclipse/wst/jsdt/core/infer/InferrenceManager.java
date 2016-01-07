/*******************************************************************************
 * Copyright (c) 2005, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.wst.jsdt.core.infer;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.wst.jsdt.core.JavaScriptCore;
import org.eclipse.wst.jsdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.wst.jsdt.internal.core.util.Util;


/**
 * 
 * Internal
 * 
 * Provisional API: This class/interface is part of an interim API that is still under development and expected to 
 * change significantly before reaching stability. It is being made available at this early stage to solicit feedback 
 * from pioneering adopters on the understanding that any code that uses this API will almost certainly be broken 
 * (repeatedly) as the API evolves.
 */
public class InferrenceManager {

	public static final String EXTENSION_POINT= "inferrenceSupport"; //$NON-NLS-1$

	protected static final String TAG_INFERENCE_PROVIDER = "inferenceProvider"; //$NON-NLS-1$
	protected static final String ATTR_INFERENGINE_CLASS = "class"; //$NON-NLS-1$


	private static InferrenceManager instance = null;


	private  InferrenceSupportExtension [] extensions;

	public static InferrenceManager getInstance(){
		if( instance == null )
			instance = new InferrenceManager();

		return instance;
	}



	public InferrenceProvider [] getInferenceProviders()
	{

		if (extensions==null)
		{
			loadInferenceExtensions();
		}
		ArrayList extProviders=new ArrayList();
		extProviders.add(new DefaultInferrenceProvider());
		for (int i = 0; i < extensions.length; i++) {
			  if (extensions[i].inferProvider!=null)
				  extProviders.add(extensions[i].inferProvider);
			}
		return (InferrenceProvider [] )extProviders.toArray(new InferrenceProvider[extProviders.size()]);
	}


	public InferrenceProvider [] getInferenceProviders(IInferenceFile script)
	{
		InferrenceProvider[] inferenceProviders = getInferenceProviders();
		List extProviders=new ArrayList(inferenceProviders.length);
		for (int i = 0; i < inferenceProviders.length; i++) {
			    int applies = inferenceProviders[i].applysTo(script);
			    switch (applies) {
				case InferrenceProvider.MAYBE_THIS:
					  extProviders.add(inferenceProviders[i]);
					break;

				case InferrenceProvider.ONLY_THIS:
					InferrenceProvider [] thisProvider = {inferenceProviders[i]};
					return thisProvider;


				default:
					break;
				}
			}
		return (InferrenceProvider [] )extProviders.toArray(new InferrenceProvider[extProviders.size()]);
	}


	

	public IInferEngine [] getInferenceEngines(CompilationUnitDeclaration script)
	{
		InferrenceProvider[] inferenceProviders = getInferenceProviders();
		if (inferenceProviders.length==1)
			  return getSingleEngine(inferenceProviders[0]);
			
		List extEngines=new ArrayList();
		for (int i = 0; i < inferenceProviders.length; i++) {
			    if (script.compilationResult!=null && script.compilationResult.compilationUnit!=null)
			    {
			    	String inferenceID = script.compilationResult.compilationUnit.getInferenceID();
			    	if (inferenceProviders[i].getID().equals(inferenceID)) {
			    		return getSingleEngine(inferenceProviders[i]);
//			    		InferEngine eng=inferenceProviders[i].getInferEngine();
//						eng.appliesTo=InferrenceProvider.MAYBE_THIS;
//						eng.inferenceProvider=inferenceProviders[i];
//						extEngines.add(eng);
//						continue;
			    	}
			    }
			    int applies = InferrenceProvider.NOT_THIS;
			    try {
			    	applies = inferenceProviders[i].applysTo(script);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					Util.log(e, "exception in inference provider "+inferenceProviders[i].getID());
				}
			    switch (applies) {
				case InferrenceProvider.MAYBE_THIS:
					  IInferEngine eng=inferenceProviders[i].getInferEngine();
					  extEngines.add(eng);
					break;

				case InferrenceProvider.ONLY_THIS:
					  return getSingleEngine(inferenceProviders[i]);


				default:
					break;
				}
			}
		return (IInferEngine [] )extEngines.toArray(new IInferEngine[extEngines.size()]);
	}

	
	private IInferEngine [] getSingleEngine(InferrenceProvider provider)
	{
		  IInferEngine engine=provider.getInferEngine();
		  IInferEngine [] thisEngine = {engine};
		  return thisEngine;
	}
	
	
	protected void loadInferenceExtensions() {
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		ArrayList extList = new ArrayList();
		if (registry != null) {
			IExtensionPoint point = registry.getExtensionPoint(
					JavaScriptCore.PLUGIN_ID, EXTENSION_POINT);

			if (point != null) {
				IExtension[] extensions = point.getExtensions();
				for (int i = 0; i < extensions.length; i++) {
					IConfigurationElement[] elements = extensions[i]
							.getConfigurationElements();
					for (int j = 0; j < elements.length; j++) {
						try {
							InferrenceProvider inferProvider = null;
							if (elements[j].getName().equals(TAG_INFERENCE_PROVIDER)) {
								inferProvider = (InferrenceProvider) elements[j]
										.createExecutableExtension(ATTR_INFERENGINE_CLASS);
							}
							InferrenceSupportExtension inferenceSupport = new InferrenceSupportExtension();
							inferenceSupport.inferProvider = inferProvider;

							extList.add(inferenceSupport);
						} catch (CoreException e) {
							Util.log(e, "Error in loading inference extension");
						}
					}
				}
			}
		}

		this.extensions = (InferrenceSupportExtension[]) extList
				.toArray(new InferrenceSupportExtension[extList.size()]);
	}


}
