/*******************************************************************************
 * Copyright (c) 2005, 2011 IBM Corporation and others.
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
import java.util.Iterator;

import org.eclipse.wst.jsdt.core.ast.IASTNode;
import org.eclipse.wst.jsdt.core.ast.IAbstractFunctionDeclaration;
import org.eclipse.wst.jsdt.core.ast.IFunctionDeclaration;
import org.eclipse.wst.jsdt.core.compiler.CharOperation;
import org.eclipse.wst.jsdt.internal.compiler.ast.ASTNode;
import org.eclipse.wst.jsdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.wst.jsdt.internal.compiler.lookup.ArrayBinding;
import org.eclipse.wst.jsdt.internal.compiler.lookup.ClassScope;
import org.eclipse.wst.jsdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.wst.jsdt.internal.compiler.lookup.MultipleTypeBinding;
import org.eclipse.wst.jsdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.wst.jsdt.internal.compiler.lookup.Scope;
import org.eclipse.wst.jsdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.wst.jsdt.internal.compiler.lookup.TagBits;
import org.eclipse.wst.jsdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.wst.jsdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.wst.jsdt.internal.compiler.util.HashtableOfObject;


/**
 * The represenation of an inferred type. 
 * 
 * Provisional API: This class/interface is part of an interim API that is still under development and expected to 
 * change significantly before reaching stability. It is being made available at this early stage to solicit feedback 
 * from pioneering adopters on the understanding that any code that uses this API will almost certainly be broken 
 * (repeatedly) as the API evolves.
 */
public class InferredType extends ASTNode {

	char [] name;
	public ArrayList methods;
	public InferredAttribute[] attributes=new InferredAttribute[5];
	public int numberAttributes=0;
	HashtableOfObject attributesHash = new HashtableOfObject();
	public InferredType superClass;

	public InferredType referenceClass;

	public SourceTypeBinding binding;
	public boolean isDefinition;
	private TypeBinding resolvedType;
	public ClassScope scope;
	ReferenceBinding resolvedSuperType;

	public boolean isArray=false;
	public boolean isAnonymous=false;
	public boolean isObjectLiteral=false;

	private int nameStart = -1;
	
	public String inferenceProviderID;
	public String inferenceStyle;
	
	public ArrayList mixins;
	
	public final static char[] OBJECT_NAME=new char[]{'O','b','j','e','c','t'};
	public final static char[] OBJECT_LITERAL_NAME = new char[]{'{','}'};

	public final static char[] ARRAY_NAME=new char[]{'A','r','r','a','y'};
	public final static char[] FUNCTION_NAME=new char[]{'F','u','n','c','t','i','o','n'};
	public final static char[] GLOBAL_NAME=new char[]{'G','l','o','b','a','l'};

	public Object userData;
	
	boolean allStatic=false;
	
	/**
	 * Create a new inferred type
	 * 
	 * @param className inferred type name
	 */
	public InferredType(char [] className)
	{
		this.name=className;
		this.sourceStart=-1;
	}

	/**
	 * Gets the name of the inferred type
	 * 
	 * @return the inferred type name
	 */
	public char [] getName() {
		return name;
	}

	/**
	 * Get the superclass name of the inferred type
	 * 
	 * @return superclass name
	 */
	public char [] getSuperClassName()
	{
		return superClass!=null ? superClass.getName() : OBJECT_NAME;
	}
	
	/**
	 * Add a new inferred attribute to the inferred type
	 * 
	 * @param name the attribute name
	 * @param definer the ASTNode which this attribute is inferred from
	 * @param nameStart character position (in the source) of the attribute name
	 * @return a new InferredAttribute
	 */
	public InferredAttribute addAttribute(char [] name, IASTNode definer, int nameStart)
	{
		InferredAttribute attribute = findAttribute(name);
		if (attribute==null)
		{
			attribute=new InferredAttribute(name, this, definer);
			attribute.node=(ASTNode)definer;
			
			if (this.numberAttributes == this.attributes.length)

				System.arraycopy(
						this.attributes,
						0,
						this.attributes = new InferredAttribute[this.numberAttributes  * 2],
						0,
						this.numberAttributes );
						this.attributes [this.numberAttributes  ++] = attribute;


			attributesHash.put(name, attribute);

			if (!isAnonymous) {
				this.updatePositions(definer.sourceStart(), definer.sourceEnd());
			}
		}
		attribute.nameStart = nameStart;
		return attribute;
	}

	/**
	 * Add an InferredAttribute to this inferred type.
	 * 
	 * @param newAttribute the attribute to add.
	 * @return 
	 */
	public InferredAttribute addAttribute(InferredAttribute newAttribute)
	{
		IASTNode definer=newAttribute.node;
		InferredAttribute attribute = findAttribute(newAttribute.name);
		if (attribute==null)
		{

			if (this.numberAttributes == this.attributes.length)

				System.arraycopy(
						this.attributes,
						0,
						this.attributes = new InferredAttribute[this.numberAttributes  * 2],
						0,
						this.numberAttributes );
						this.attributes [this.numberAttributes  ++] = newAttribute;


			attributesHash.put(newAttribute.name, newAttribute);

			if (!isAnonymous) {
				if (definer != null) {
					this.updatePositions(definer.sourceStart(), definer.sourceEnd());
				}
				else {
					this.updatePositions(newAttribute.sourceStart(), newAttribute.sourceEnd());
				}
			}
		}
		return newAttribute;
	}
	/**
	 * Find the inferred attribute with the given name
	 * 
	 * @param name name of the attribute to find
	 * @return the found InferredAttribute, or null if not found
	 */
	public InferredAttribute findAttribute(char [] name)
	{
		return (InferredAttribute)attributesHash.get(name);
//		if (attributes!=null)
//		for (Iterator attrIterator = attributes.iterator(); attrIterator.hasNext();) {
//			InferredAttribute attribute = (InferredAttribute) attrIterator.next();
//			if (CharOperation.equals(name,attribute.name))
//				return attribute;
//		}
//		return null;
	}


	/**
	 * Add a new constructor method to the inferred type
	 * 
	 * @param methodName name of the method to add
	 * @param functionDeclaration the AST Node containing the method bode
	 * @param nameStart character position (in the source) of the method name
	 * @return a new inferred method
	 */
	public InferredMethod addConstructorMethod(char [] methodName, IFunctionDeclaration functionDeclaration, int nameStart) {
		InferredMethod method = this.addMethod(methodName, functionDeclaration, nameStart, true);
		method.isConstructor = true;
		this.setNameStart(nameStart);
		method.getFunctionDeclaration().setInferredType(this);
		return method;
	}
	
	/**
	 * Add a new method to the inferred type
	 * 
	 * @param methodName name of the method to add
	 * @param functionDeclaration the AST Node containing the method bode
	 * @param nameStart character position (in the source) of the method name
	 * @return a new inferred method
	 */
	public InferredMethod addMethod(char [] methodName, IFunctionDeclaration functionDeclaration, int nameStart) {
		return this.addMethod(methodName, functionDeclaration, nameStart, false);
	}
	
	/**
	 * Add a new method to the inferred type
	 * 
	 * @param methodName name of the method to add
	 * @param functionDeclaration the AST Node containing the method bode
	 * @param isConstructor true if it is a constructor
	 * @return a new inferred method
	 */
	private InferredMethod addMethod(char [] methodName, IFunctionDeclaration functionDeclaration, int nameStart, boolean isConstructor) {
		MethodDeclaration methodDeclaration = (MethodDeclaration)functionDeclaration;
		InferredMethod method = findMethod(methodName, methodDeclaration);
		if (method==null) {
			method=new InferredMethod(methodName,methodDeclaration,this);
			if (methodDeclaration.inferredMethod==null) 
				methodDeclaration.inferredMethod = method;
			else
			{
				if (isConstructor)
				{
					methodDeclaration.inferredMethod.inType=this;
					method.isStatic=methodDeclaration.inferredMethod.isStatic;
					method.bits=methodDeclaration.inferredMethod.bits;
					methodDeclaration.inferredMethod = method;
				} else if (methodDeclaration.inferredMethod.isConstructor)
					method.inType=methodDeclaration.inferredMethod.inType;
				
			}
			if (methods==null)
				methods=new ArrayList();
			methods.add(method);

			if( !isAnonymous )
				this.updatePositions(methodDeclaration.sourceStart, methodDeclaration.sourceEnd);
			method.isConstructor=isConstructor;
			method.nameStart = nameStart;
		} else {
			if (methodDeclaration.inferredMethod==null) {
				methodDeclaration.inferredMethod=method;
			}
		}
		
		return method;
	}

	/**
	 * Find an inferred method
	 * 
	 * @param methodName name of the method to find
	 * @param methodDeclaration not used
	 * @return the found method, or null
	 */
	public InferredMethod findMethod(char [] methodName, IFunctionDeclaration methodDeclaration) {
		boolean isConstructor= methodName==TypeConstants.INIT;
		if (methods!=null)
			for (Iterator methodIterator = methods.iterator(); methodIterator.hasNext();) {
				InferredMethod method = (InferredMethod) methodIterator.next();
				if (CharOperation.equals(methodName,method.name))
					return method;
				if (isConstructor && method.isConstructor)
					return method;
			}
			return null;

	}

	public TypeBinding resolveType(Scope scope, ASTNode node) {
		// handle the error here
		if (this.resolvedType != null) // is a shared type reference which was already resolved
			return this.resolvedType.isValidBinding() ? this.resolvedType : null; // already reported error


		if (isArray())
		{
			TypeBinding memberType = (referenceClass!=null)?referenceClass.resolveType(scope,node):null;
			if (memberType==null)
				memberType=TypeBinding.UNKNOWN;
			this.resolvedType=new ArrayBinding(memberType, 1, scope.compilationUnitScope().environment) ;

		}
		else {
			if (CharOperation.indexOf('|', name)>0)
			{
				char[][] names = CharOperation.splitAndTrimOn('|', name);
				this.resolvedType=new MultipleTypeBinding(scope,names);
			}
			else
			  this.resolvedType = scope.getType(name);
			/* the inferred type isn't valid, so don't assign it to the variable */
			if(!this.resolvedType.isValidBinding()) this.resolvedType = null;
		}


		if (this.resolvedType == null)
			return null; // detected cycle while resolving hierarchy
		if (node!=null && !this.resolvedType.isValidBinding()) {
			scope.problemReporter().invalidType(node, this.resolvedType);
			return null;
		}
		if (node!=null && node.isTypeUseDeprecated(this.resolvedType, scope))
			scope.problemReporter().deprecatedType(this.resolvedType, node);

		if( isAnonymous )
			this.resolvedType.tagBits |= TagBits.AnonymousTypeMask;

		return this.resolvedType ;
	}



	public void dumpReference(StringBuffer sb)
	{
		sb.append(name);
		if (referenceClass!=null)
		{
			sb.append('(');
			referenceClass.dumpReference(sb);
			sb.append(')');
		}
	}

	public boolean containsMethod(IAbstractFunctionDeclaration inMethod) {
		if (methods!=null)
			for (Iterator iter = methods.iterator(); iter.hasNext();) {
				InferredMethod method = (InferredMethod) iter.next();
				if (method.getFunctionDeclaration()==inMethod)
					return true;
			}
		return false;
	}



	public ReferenceBinding resolveSuperType(ClassScope classScope) {
		if (this.resolvedSuperType != null)
			return this.resolvedSuperType;

		if(superClass != null)
			this.resolvedSuperType = (ReferenceBinding)classScope.getType(superClass.getName());

		return this.resolvedSuperType;
	}

	public boolean isArray()
	{
		return CharOperation.equals(ARRAY_NAME, name);
	}
	
	public boolean isFunction()
	{
		return CharOperation.equals(FUNCTION_NAME, name);
	}

	public StringBuffer print(int indent, StringBuffer output) {
		printIndent(indent, output);
		char[] superName= getSuperClassName();
		output.append("class ").append(name).append(" extends ").append(superName).append("{\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		for (int i=0;i<this.numberAttributes;i++) {
				this.attributes[i].print(indent+1,output);
				output.append(";\n"); //$NON-NLS-1$
			}
		if (methods!=null)
			for (Iterator methodIterator = methods.iterator(); methodIterator.hasNext();) {
				InferredMethod method = (InferredMethod) methodIterator.next();
				method.print(indent+1,output);
				output.append("\n"); //$NON-NLS-1$
			}
		output.append("}"); //$NON-NLS-1$
		return output;
	}

	public boolean isInferred()
	{
		return true;
	}

	public void updatePositions(int start, int end)
	{
		if (this.sourceStart==-1 ||(start>=0 && start<this.sourceStart))
			this.sourceStart=start;
		if (end>0&&end>this.sourceEnd)
			this.sourceEnd=end;
	}

	public IAbstractFunctionDeclaration declarationOf(MethodBinding methodBinding) {
		if (methodBinding != null && this.methods != null) {
			for (int i = 0, max = this.methods.size(); i < max; i++) {
				InferredMethod method=(InferredMethod) this.methods.get(i);

				if (method.methodBinding==methodBinding)
					return method.getFunctionDeclaration();
			}
		}
		return null;
	}
	
	public boolean isNamed()
	{
		return !isAnonymous || !CharOperation.prefixEquals(IInferEngine.ANONYMOUS_PREFIX, this.name);
	}
	
	/**
	 * Set the charactor position (in the source) of the type name
	 * 
	 * @param start type name position
	 */
	public void setNameStart(int start)
	{
		this.nameStart=start;
	}
	
	public int getNameStart()
	{
		return this.nameStart!= -1 ? this.nameStart : this.sourceStart;
	}
	
	public boolean isEmptyGlobal()
	{
		return (CharOperation.equals(GLOBAL_NAME, this.name) &&
				this.numberAttributes==0 && 
				(this.methods==null || this.methods.isEmpty()));
	}


	/**
	 * <p>Adds the name of a type to mix into this type once all of the types have
	 * been inferred</p>
	 * 
	 * @param mixinTypeName the name of the type to mix into this type
	 */
	public void addMixin(char[] mixinTypeName)
	{
		if (mixins==null)
			mixins=new ArrayList();
		mixins.add(mixinTypeName);
	}

}
