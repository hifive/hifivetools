/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Erling Ellingsen -  patch for bug 125570
 *     NS Solutions Corporation - patch lineno432-437,669-674
 *******************************************************************************/
package org.eclipse.wst.jsdt.internal.compiler.lookup;

import java.util.ArrayList;
import java.util.HashSet;

import org.eclipse.wst.jsdt.core.LibrarySuperType;
import org.eclipse.wst.jsdt.core.compiler.CharOperation;
import org.eclipse.wst.jsdt.core.compiler.libraries.SystemLibraryLocation;
import org.eclipse.wst.jsdt.core.infer.InferredType;
import org.eclipse.wst.jsdt.core.infer.InferrenceManager;
import org.eclipse.wst.jsdt.core.infer.InferrenceProvider;
import org.eclipse.wst.jsdt.internal.compiler.ASTVisitor;
import org.eclipse.wst.jsdt.internal.compiler.ast.ASTNode;
import org.eclipse.wst.jsdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.wst.jsdt.internal.compiler.ast.FunctionExpression;
import org.eclipse.wst.jsdt.internal.compiler.ast.ImportReference;
import org.eclipse.wst.jsdt.internal.compiler.ast.LocalDeclaration;
import org.eclipse.wst.jsdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.wst.jsdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.wst.jsdt.internal.compiler.env.AccessRestriction;
import org.eclipse.wst.jsdt.internal.compiler.problem.ProblemReporter;
import org.eclipse.wst.jsdt.internal.compiler.util.CompoundNameVector;
import org.eclipse.wst.jsdt.internal.compiler.util.HashtableOfObject;
import org.eclipse.wst.jsdt.internal.compiler.util.HashtableOfType;
import org.eclipse.wst.jsdt.internal.compiler.util.ObjectVector;
import org.eclipse.wst.jsdt.internal.compiler.util.SimpleNameVector;
import org.eclipse.wst.jsdt.internal.compiler.util.SimpleSetOfCharArray;
import org.eclipse.wst.jsdt.internal.core.util.Util;



public class CompilationUnitScope extends BlockScope {

public LookupEnvironment environment;
public CompilationUnitDeclaration referenceContext;
public char[][] currentPackageName;
public PackageBinding fPackage;
public ImportBinding[] imports;
public HashtableOfObject typeOrPackageCache; // used in Scope.getTypeOrPackage()

public SourceTypeBinding[] topLevelTypes;

private CompoundNameVector qualifiedReferences;
private SimpleNameVector simpleNameReferences;
private ObjectVector referencedTypes;
private ObjectVector referencedSuperTypes;

HashtableOfType constantPoolNameUsage;
public int analysisIndex;
private int captureID = 1;

/* Allows a compilation unit to inherit fields from a superType */
public ReferenceBinding superBinding;
private MethodScope methodScope;
private ClassScope classScope;

public int temporaryAnalysisIndex;


public HashSet externalCompilationUnits=new HashSet();

public static final char FILENAME_DOT_SUBSTITUTION='#';


class DeclarationVisitor extends ASTVisitor
{
	ArrayList methods=new ArrayList();
	public boolean visit(LocalDeclaration localDeclaration, BlockScope scope) {
		if(localDeclaration.initialization instanceof FunctionExpression) {
			this.visit(((FunctionExpression)localDeclaration.initialization).getMethodDeclaration(), scope);
		} else {
			TypeBinding type=localDeclaration.resolveVarType(scope);
			LocalVariableBinding binding = new LocalVariableBinding(localDeclaration, type, 0, false);
			localDeclaration.binding=binding;
			addLocalVariable(binding);
		}
		return false;
	}

	public boolean visit(MethodDeclaration methodDeclaration, Scope parentScope) {

		char[] selector = null;

		if(methodDeclaration.selector != null) {
			selector = methodDeclaration.selector;
		} else if(methodDeclaration.inferredMethod != null && methodDeclaration.inferredMethod.isConstructor) {
			//this is that inferred constructors get added to the methods list
			selector = methodDeclaration.inferredMethod.name;
		}

		if (selector!=null)
		{
			MethodScope scope = new MethodScope(parentScope,methodDeclaration, false);
			MethodBinding methodBinding = scope.createMethod(methodDeclaration,selector,referenceContext.compilationUnitBinding,false,false);
			if (methodBinding != null && methodBinding.selector!=null) // is null if binding could not be created
				methods.add(methodBinding);
			if (methodBinding.selector!=null)
			{
				environment.defaultPackage.addBinding(methodBinding, methodBinding.selector,Binding.METHOD);
				fPackage.addBinding(methodBinding, methodBinding.selector,Binding.METHOD);
			}
			methodDeclaration.binding=methodBinding;
			methodDeclaration.bindArguments();
		}
		return false;
	}
}



public CompilationUnitScope(CompilationUnitDeclaration unit, LookupEnvironment environment) {
	super(COMPILATION_UNIT_SCOPE, null);

	this.environment = environment;
	this.referenceContext = unit;
	unit.scope = this;

	/* bc - start bug 218398 - NPE when doing source->cleanup */

	char [][]pkgName= unit.currentPackage == null ?
			(unit.compilationResult!=null? unit.compilationResult.getPackageName():null)
			: unit.currentPackage.tokens;
	this.currentPackageName = pkgName == null ? CharOperation.NO_CHAR_CHAR : pkgName;
//	this.currentPackageName = CharOperation.NO_CHAR_CHAR;
	/* bc - end bug 218398 - NPE when doing source->cleanup */


	this.referencedTypes = new ObjectVector();
	if (compilerOptions().produceReferenceInfo) {
		this.qualifiedReferences = new CompoundNameVector();
		this.simpleNameReferences = new SimpleNameVector();
		this.referencedSuperTypes = new ObjectVector();
	} else {
		this.qualifiedReferences = null; // used to test if dependencies should be recorded
		this.simpleNameReferences = null;
//		this.referencedTypes = null;
		this.referencedSuperTypes = null;
	}

}

protected CompilationUnitScope(LookupEnvironment environment)
{
	super(COMPILATION_UNIT_SCOPE, null);
	this.environment = environment;

	this.referencedTypes = new ObjectVector();
	if (compilerOptions().produceReferenceInfo) {
		this.qualifiedReferences = new CompoundNameVector();
		this.simpleNameReferences = new SimpleNameVector();
		this.referencedSuperTypes = new ObjectVector();
	} else {
		this.qualifiedReferences = null; // used to test if dependencies should be recorded
		this.simpleNameReferences = null;
//		this.referencedTypes = null;
		this.referencedSuperTypes = null;
	}
}

//public MethodScope methodScope() {
//	if(superBinding!=null && methodScope==null) {
//		methodScope = new MethodScope(classScope,referenceContext(),false);
//	}
//
//	return methodScope;
//}

public ClassScope classScope() {
	if (this.classScope!=null) return this.classScope;
	return super.classScope();
}

void buildFieldsAndMethods() {
	for (int i = 0, length = topLevelTypes.length; i < length; i++)
		topLevelTypes[i].buildFieldsAndMethods();
}

void buildTypeBindings(AccessRestriction accessRestriction) {
	buildTypeBindings(new char[0][0], accessRestriction);
}

void buildTypeBindings(char[][] restrictToNames, AccessRestriction accessRestriction) {
	topLevelTypes = new SourceTypeBinding[0]; // want it initialized if the package cannot be resolved
	if (referenceContext.compilationResult.compilationUnit != null) {
		char[][] expectedPackageName = referenceContext.compilationResult.compilationUnit.getPackageName();
		if (expectedPackageName != null
				&& !CharOperation.equals(currentPackageName, expectedPackageName)) {
			currentPackageName = expectedPackageName.length == 0 ? CharOperation.NO_CHAR_CHAR : expectedPackageName;
		}
	}
	if (currentPackageName == CharOperation.NO_CHAR_CHAR) {
		fPackage = environment.defaultPackage;
	} else {
		if ((fPackage = environment.createPackage(currentPackageName)) == null) {
//			problemReporter().packageCollidesWithType(referenceContext);
//			return;
//		} else if (referenceContext.isPackageInfo()) {
//			// resolve package annotations now if this is "package-info.js".
//			if (referenceContext.types == null || referenceContext.types.length == 0) {
//				referenceContext.types = new TypeDeclaration[1];
//				TypeDeclaration declaration = new TypeDeclaration(referenceContext.compilationResult);
//				referenceContext.types[0] = declaration;
//				declaration.name = TypeConstants.PACKAGE_INFO_NAME;
//				declaration.modifiers = ClassFileConstants.AccDefault | ClassFileConstants.AccInterface;
//				firstIsSynthetic = true;
//			}
		}
//		recordQualifiedReference(currentPackageName); // always dependent on your own package
	}

//	// Skip typeDeclarations which know of previously reported errors
//	TypeDeclaration[] types = referenceContext.types;
//	int typeLength = (types == null) ? 0 : types.length;
//	topLevelTypes = new SourceTypeBinding[typeLength];
//	int count = 0;
//	nextType: for (int i = 0; i < typeLength; i++) {
//		TypeDeclaration typeDecl = types[i];
//		ReferenceBinding typeBinding = fPackage.getType0(typeDecl.name);
//		recordSimpleReference(typeDecl.name); // needed to detect collision cases
//		if (typeBinding != null && !(typeBinding instanceof UnresolvedReferenceBinding)) {
//			// if a type exists, it must be a valid type - cannot be a NotFound problem type
//			// unless its an unresolved type which is now being defined
//			problemReporter().duplicateTypes(referenceContext, typeDecl);
//			continue nextType;
//		}
//		if (fPackage != environment.defaultPackage && fPackage.getPackage(typeDecl.name) != null) {
//			// if a package exists, it must be a valid package - cannot be a NotFound problem package
//			problemReporter().typeCollidesWithPackage(referenceContext, typeDecl);
//			continue nextType;
//		}
//
//		if ((typeDecl.modifiers & ClassFileConstants.AccPublic) != 0) {
//			char[] mainTypeName;
//			if ((mainTypeName = referenceContext.getMainTypeName()) != null // mainTypeName == null means that implementor of IJavaScriptUnit decided to return null
//					&& !CharOperation.equals(mainTypeName, typeDecl.name)) {
//				problemReporter().publicClassMustMatchFileName(referenceContext, typeDecl);
//				// tolerate faulty main type name (91091), allow to proceed into type construction
//			}
//		}
//
//		ClassScope child = new ClassScope(this, typeDecl);
//		SourceTypeBinding type = child.buildType(null, fPackage, accessRestriction);
//		if (firstIsSynthetic && i == 0)
//			type.modifiers |= ClassFileConstants.AccSynthetic;
//		if (type != null)
//			topLevelTypes[count++] = type;
//	}
//
//	// shrink topLevelTypes... only happens if an error was reported
//	if (count != topLevelTypes.length)
//		System.arraycopy(topLevelTypes, 0, topLevelTypes = new SourceTypeBinding[count], 0, count);
//

	this.faultInImports();

	// Skip typeDeclarations which know of previously reported errors
	int typeLength = referenceContext.numberInferredTypes;

	/* Include super type whild building */
//	if(superTypeName!=null) {
//		superType = environment.askForType(new char[][] {superTypeName});
//	}

//			//((SourceTypeBinding)superType).classScope.buildInferredType(null, environment.defaultPackage,accessRestriction);
//			//((SourceTypeBinding)superType).classScope.connectTypeHierarchy();
//			//FieldBinding[] fields = superType.fields();
//			//addSubscope(((SourceTypeBinding)superType).classScope);
//
//
//		//	this.parent = ((SourceTypeBinding)superType).classScope;
//
//
//		}
//
//
//	}

	/* may need to get the actual binding here */
//	if(libSuperType!=null) {
//		//JsGlobalScopeContainerInitializer cinit = libSuperType.getContainerInitializer();
//		//IIncludePathEntry[] entries = libSuperType.getClasspathEntries();
//		IPackageFragment[] fragments = libSuperType.getPackageFragments();
//		for(int i = 0;i<fragments.length;i++) {
//			String packageName = fragments[i].getElementName();
//			PackageBinding binding = environment.getPackage0(packageName.toCharArray());
//			superBinding  = binding.getType(libSuperType.getSuperTypeName().toCharArray());
//			if(superBinding!=null) break;
//
//		}
//
//	}else


	topLevelTypes = new SourceTypeBinding[typeLength];

	int count = 0;


	SimpleSetOfCharArray addTypes=new SimpleSetOfCharArray(10);
//	nextType:
	String fileName=new String(this.referenceContext.getFileName());
	nextType: for (int i = 0; i < typeLength; i++) {
		InferredType typeDecl =  referenceContext.inferredTypes[i];

		if (typeDecl.isDefinition && !typeDecl.isEmptyGlobal()) {
			if(restrictToNames.length > 0) {
					boolean continueBuilding = false;
					for (int j = 0; !continueBuilding
							&& j < restrictToNames.length; j++) {
						if (CharOperation.equals(typeDecl.getName(),
								restrictToNames[j]))
							continueBuilding = true;
					}
					if (!continueBuilding)
						continue nextType;

			}
			ReferenceBinding typeBinding = environment.defaultPackage
					.getType0(typeDecl.getName());
			recordSimpleReference(typeDecl.getName()); // needed to detect collision cases
			SourceTypeBinding existingBinding=null;
			if (typeBinding != null
					&& !(typeBinding instanceof UnresolvedReferenceBinding)) {
				// if a type exists, it must be a valid type - cannot be a NotFound problem type
				// unless its an unresolved type which is now being defined
//				problemReporter().duplicateTypes(referenceContext, typeDecl);
//				continue nextType;
				if (typeBinding instanceof SourceTypeBinding)
					existingBinding=(SourceTypeBinding)typeBinding;
			}
			ClassScope child = new ClassScope(this, typeDecl);
			SourceTypeBinding type = child.buildInferredType(null, environment.defaultPackage,
					accessRestriction);
			//		SourceTypeBinding type = buildType(typeDecl,null, fPackage, accessRestriction);
			if (type != null)
			{
				if (existingBinding!=null && typeDecl.isNamed()  )
				{
					if (existingBinding.nextType!=null)
					{
						existingBinding.addNextType(type);
					}
					else
					{
						if (!CharOperation.equals(type.fileName, existingBinding.fileName))
							existingBinding.addNextType(type);
					}
					environment.defaultPackage.addType(existingBinding);
					fPackage.addType(existingBinding);
				}
				else
					if (typeDecl.isNamed() )
						addTypes.add(typeDecl.getName());
//					  environment.addUnitsContainingBinding(null, typeDecl.getName(), Binding.TYPE,fileName);
				topLevelTypes[count++] = type;
			}
		}
	}


	char [][] typeNames= new  char [addTypes.elementSize] [];
	addTypes.asArray(typeNames);
	environment.addUnitsContainingBindings(typeNames, Binding.TYPE, fileName);


	// shrink topLevelTypes... only happens if an error was reported
	if (count != topLevelTypes.length)
		System.arraycopy(topLevelTypes, 0, topLevelTypes = new SourceTypeBinding[count], 0, count);


	buildSuperType();


	char [] path=CharOperation.concatWith(this.currentPackageName, '/');
	referenceContext.compilationUnitBinding=new CompilationUnitBinding(this,environment.defaultPackage,path, superBinding);

	if (fPackage!=environment.defaultPackage)
		fPackage.addBinding(referenceContext.compilationUnitBinding, referenceContext.getMainTypeName(), Binding.COMPILATION_UNIT);

	DeclarationVisitor visitor = new DeclarationVisitor();
	this.referenceContext.traverse(visitor, this);
	MethodBinding[] methods = (MethodBinding[])visitor.methods.toArray(new MethodBinding[visitor.methods.size()]);
	referenceContext.compilationUnitBinding.setMethods(methods);
}

public void buildSuperType() {

	char[] superTypeName = null;
	LibrarySuperType libSuperType = null;
	if(this.referenceContext.compilationResult!=null && this.referenceContext.compilationResult.compilationUnit!=null) {
		libSuperType = this.referenceContext.compilationResult.compilationUnit.getCommonSuperType();
		if(libSuperType==null) {
			superTypeName = null;
			return;
		}else
			superTypeName = libSuperType.getSuperTypeName().toCharArray();
	}
	if (superTypeName==null)
		return;

//	superBinding  =  environment.askForType(new char[][] {superTypeName});
	superBinding  =  findType(superTypeName, environment.defaultPackage, environment.defaultPackage);

		if(superBinding==null || !superBinding.isValidBinding()) {
			superTypeName = null;
			return ;
		}


		/* If super type is combined source type, search through SourceTypes for the specific instance */
		if( (superBinding instanceof SourceTypeBinding) && ((SourceTypeBinding)superBinding).nextType!=null) {


				classScope = ((SourceTypeBinding)superBinding).classScope;

				SourceTypeBinding sourceType = null;

				if(superBinding instanceof SourceTypeBinding) {
					sourceType = (SourceTypeBinding)superBinding;
				}
				// start 2012/08/07
				// classScope.buildInferredType(sourceType, environment.defaultPackage, null);
				if(classScope != null){
					classScope.buildInferredType(sourceType, environment.defaultPackage, null);
				}
				// end 2012/08/07

				recordTypeReference(superBinding);
				recordSuperTypeReference(superBinding);
				environment().setAccessRestriction(superBinding, null);
		}else if(superBinding!=null) {
			InferredType te = superBinding.getInferredType();
			classScope = new ClassScope(this, te);

			SourceTypeBinding sourceType = null;

			if(superBinding instanceof SourceTypeBinding) {
				sourceType = (SourceTypeBinding)superBinding;
			}
			classScope.buildInferredType(sourceType, environment.defaultPackage, null);


			recordTypeReference(superBinding);
			recordSuperTypeReference(superBinding);
			environment().setAccessRestriction(superBinding, null);
		}







	if(superTypeName!=null && superTypeName.length==0) {
		superTypeName=null;
	}
}

SourceTypeBinding buildType(InferredType inferredType, SourceTypeBinding enclosingType, PackageBinding packageBinding, AccessRestriction accessRestriction) {
	// provide the typeDeclaration with needed scopes

	if (enclosingType == null) {
		char[][] className = CharOperation.arrayConcat(packageBinding.compoundName, inferredType.getName());
		inferredType.binding = new SourceTypeBinding(className, packageBinding, this);

		//@GINO: Anonymous set bits
		if( inferredType.isAnonymous )
			inferredType.binding.tagBits |= TagBits.AnonymousTypeMask;

	} else {
//		char[][] className = CharOperation.deepCopy(enclosingType.compoundName);
//		className[className.length - 1] =
//			CharOperation.concat(className[className.length - 1], inferredType.getName(), '$');
//		inferredType.binding = new MemberTypeBinding(className, this, enclosingType);
	}

	SourceTypeBinding sourceType = inferredType.binding;
	environment().setAccessRestriction(sourceType, accessRestriction);
	environment().defaultPackage.addType(sourceType);
	sourceType.fPackage.addType(sourceType);
	return sourceType;
}


public PackageBinding getDefaultPackage() {
		return environment.defaultPackage;
}

public  void addLocalVariable(LocalVariableBinding binding) {
	super.addLocalVariable(binding);
	environment.defaultPackage.addBinding(binding, binding.name, Binding.VARIABLE);
	fPackage.addBinding(binding, binding.name, Binding.VARIABLE);
}

void checkAndSetImports() {
	if (referenceContext.imports == null) {
		imports = getDefaultImports();
		return;
	}

	// allocate the import array, add java.lang.* by default
	int numberOfStatements = referenceContext.imports.length;
	int numberOfImports = numberOfStatements + 1;
	for (int i = 0; i < numberOfStatements; i++) {
		ImportReference importReference = referenceContext.imports[i];
		if (((importReference.bits & ASTNode.OnDemand) != 0) && CharOperation.equals(JAVA_LANG, importReference.tokens)) {
			numberOfImports--;
			break;
		}
	}
	ImportBinding[] resolvedImports = new ImportBinding[numberOfImports];
	resolvedImports[0] = getDefaultImports()[0];
	int index = 1;

	nextImport : for (int i = 0; i < numberOfStatements; i++) {
		ImportReference importReference = referenceContext.imports[i];
		char[][] compoundName = importReference.tokens;

		// skip duplicates or imports of the current package
		for (int j = 0; j < index; j++) {
			ImportBinding resolved = resolvedImports[j];
			if (resolved.onDemand == ((importReference.bits & ASTNode.OnDemand) != 0))
				if (CharOperation.equals(compoundName, resolvedImports[j].compoundName))
					continue nextImport;
		}

		if ((importReference.bits & ASTNode.OnDemand) != 0) {
			if (CharOperation.equals(compoundName, currentPackageName))
				continue nextImport;

			Binding importBinding = findImport(compoundName, compoundName.length);
			if (!importBinding.isValidBinding())
				continue nextImport;	// we report all problems in faultInImports()
			resolvedImports[index++] = new ImportBinding(compoundName, true, importBinding, importReference);
		} else {
			// resolve single imports only when the last name matches
			resolvedImports[index++] = new ImportBinding(compoundName, false, null, importReference);
		}
	}

	// shrink resolvedImports... only happens if an error was reported
	if (resolvedImports.length > index)
		System.arraycopy(resolvedImports, 0, resolvedImports = new ImportBinding[index], 0, index);
	imports = resolvedImports;
}

/*
 * INTERNAL USE-ONLY
 * Innerclasses get their name computed as they are generated, since some may not
 * be actually outputed if sitting inside unreachable code.
 */
public char[] computeConstantPoolName(LocalTypeBinding localType) {
	if (localType.constantPoolName() != null) {
		return localType.constantPoolName();
	}
	// delegates to the outermost enclosing classfile, since it is the only one with a global vision of its innertypes.

	if (constantPoolNameUsage == null)
		constantPoolNameUsage = new HashtableOfType();

	ReferenceBinding outerMostEnclosingType = localType.scope.outerMostClassScope().enclosingSourceType();

	// ensure there is not already such a local type name defined by the user
	int index = 0;
	char[] candidateName;
	boolean isCompliant15 = compilerOptions().complianceLevel >= ClassFileConstants.JDK1_5;
	while(true) {
		if (localType.isMemberType()){
			if (index == 0){
				candidateName = CharOperation.concat(
					localType.enclosingType().constantPoolName(),
					localType.sourceName,
					'$');
			} else {
				// in case of collision, then member name gets extra $1 inserted
				// e.g. class X { { class L{} new X(){ class L{} } } }
				candidateName = CharOperation.concat(
					localType.enclosingType().constantPoolName(),
					'$',
					String.valueOf(index).toCharArray(),
					'$',
					localType.sourceName);
			}
		} else if (localType.isAnonymousType()){
			if (isCompliant15) {
				// from 1.5 on, use immediately enclosing type name
				candidateName = CharOperation.concat(
					localType.enclosingType.constantPoolName(),
					String.valueOf(index+1).toCharArray(),
					'$');
			} else {
				candidateName = CharOperation.concat(
					outerMostEnclosingType.constantPoolName(),
					String.valueOf(index+1).toCharArray(),
					'$');
			}
		} else {
			// local type
			if (isCompliant15) {
				candidateName = CharOperation.concat(
					CharOperation.concat(
						localType.enclosingType().constantPoolName(),
						String.valueOf(index+1).toCharArray(),
						'$'),
					localType.sourceName);
			} else {
				candidateName = CharOperation.concat(
					outerMostEnclosingType.constantPoolName(),
					'$',
					String.valueOf(index+1).toCharArray(),
					'$',
					localType.sourceName);
			}
		}
		if (constantPoolNameUsage.get(candidateName) != null) {
			index ++;
		} else {
			constantPoolNameUsage.put(candidateName, localType);
			break;
		}
	}
	return candidateName;
}

void connectTypeHierarchy(char[][] typeNames) {
		// if(superType!=null) {
		// if(superType instanceof SourceTypeBinding) {
		// ((SourceTypeBinding)superType).classScope.buildFieldsAndMethods();
		// ((SourceTypeBinding)superType).classScope.connectTypeHierarchy();
		//
		// }
		// ReferenceBinding[] memberTypes = superType.memberTypes();
		// ReferenceBinding[] memberFields = superType.typeVariables();
		// FunctionBinding[] memberMethods = superType.availableMethods();
		// for(int i=0;i<memberTypes.length;i++) {
		// recordReference(memberTypes[i], memberTypes[i].sourceName);
		// }
		// }

		// if(superTypeName!=null) {
		// ReferenceBinding binding = environment.askForType(new char[][]
		// {superTypeName});
		// this.recordSuperTypeReference(binding);
		// }
		if (classScope != null)
			classScope.connectTypeHierarchy();
		nextType: for (int i = 0; i < referenceContext.numberInferredTypes; i++) {
			InferredType inferredType = referenceContext.inferredTypes[i];
			if(typeNames.length > 0) {
				boolean continueBuilding = false;
				for (int j = 0; !continueBuilding
						&& j < typeNames.length; j++) {
					if (CharOperation.equals(inferredType.getName(),
							typeNames[j]))
						continueBuilding = true;
				}
				if (!continueBuilding)
					continue nextType;

		}

			// start 2012/08/07
			// if (inferredType.binding != null )
			// inferredType.binding.classScope.connectTypeHierarchy();
			if (inferredType.binding != null && inferredType.binding.classScope != null)
				inferredType.binding.classScope.connectTypeHierarchy();
			// end 2012/08/07
		}
}
void connectTypeHierarchy() {
	connectTypeHierarchy(new char[0][0]);
}
void faultInImports() {
	if (this.typeOrPackageCache != null)
		return; // can be called when a field constant is resolved before static imports
	if (referenceContext.imports == null) {
		this.typeOrPackageCache = new HashtableOfObject(1);
		return;
	}

	// collect the top level type names if a single type import exists
	int numberOfStatements = referenceContext.imports.length;
	HashtableOfType typesBySimpleNames = null;
	for (int i = 0; i < numberOfStatements; i++) {
		if ((referenceContext.imports[i].bits & ASTNode.OnDemand) == 0) {
			typesBySimpleNames = new HashtableOfType(topLevelTypes.length + numberOfStatements);
			for (int j = 0, length = topLevelTypes.length; j < length; j++)
				typesBySimpleNames.put(topLevelTypes[j].sourceName, topLevelTypes[j]);
			break;
		}
	}

	// allocate the import array, add java.lang.* by default
	ImportBinding[] defaultImports = getDefaultImports();
	int numberOfImports = numberOfStatements + defaultImports.length;
	for (int i = 0; i < numberOfStatements; i++) {
		ImportReference importReference = referenceContext.imports[i];
		if (((importReference.bits & ASTNode.OnDemand) != 0) && CharOperation.equals(JAVA_LANG, importReference.tokens)) {
			numberOfImports--;
			break;
		}
	}
	ImportBinding[] resolvedImports = new ImportBinding[numberOfImports];
	System.arraycopy(defaultImports, 0, resolvedImports, 0, defaultImports.length);
	int index = defaultImports.length;

	// keep static imports with normal imports until there is a reason to split them up
	// on demand imports continue to be packages & types. need to check on demand type imports for fields/methods
	// single imports change from being just types to types or fields
	nextImport : for (int i = 0; i < numberOfStatements; i++) {
		ImportReference importReference = referenceContext.imports[i];
		char[][] compoundName = importReference.tokens;

		// skip duplicates or imports of the current package
		for (int j = 0; j < index; j++) {
			ImportBinding resolved = resolvedImports[j];
			if (resolved.onDemand == ((importReference.bits & ASTNode.OnDemand) != 0)) {
				if (CharOperation.equals(compoundName, resolved.compoundName)) {
					continue nextImport;
				}
			}
		}
		if ((importReference.bits & ASTNode.OnDemand) != 0) {
			if (CharOperation.equals(compoundName, currentPackageName)) {
				continue nextImport;
			}

			Binding importBinding = findImport(compoundName, compoundName.length);
			if (!importBinding.isValidBinding()) {
				continue nextImport;
			}
			resolvedImports[index++] = new ImportBinding(compoundName, true, importBinding, importReference);
		} else {
			Binding importBinding = findSingleImport(compoundName);
			if (!importBinding.isValidBinding()) {
				continue nextImport;
			}
			ReferenceBinding conflictingType = null;
			if (importBinding instanceof MethodBinding) {
				conflictingType = (ReferenceBinding) getType(compoundName, compoundName.length);
				if (!conflictingType.isValidBinding())
					conflictingType = null;
			}
			// collisions between an imported static field & a type should be checked according to spec... but currently not by javac
			if (importBinding instanceof ReferenceBinding || conflictingType != null) {
				ReferenceBinding referenceBinding = conflictingType == null ? (ReferenceBinding) importBinding : conflictingType;
				if (importReference.isTypeUseDeprecated(referenceBinding, this))
					problemReporter().deprecatedType(referenceBinding, importReference);

				ReferenceBinding existingType = typesBySimpleNames.get(compoundName[compoundName.length - 1]);
				if (existingType != null) {
					continue nextImport;
				}
				typesBySimpleNames.put(compoundName[compoundName.length - 1], referenceBinding);
			}
			resolvedImports[index++] = conflictingType == null
				? new ImportBinding(compoundName, false, importBinding, importReference)
				: new ImportConflictBinding(compoundName, importBinding, conflictingType, importReference);
		}
	}

	// shrink resolvedImports... only happens if an error was reported
	if (resolvedImports.length > index)
		System.arraycopy(resolvedImports, 0, resolvedImports = new ImportBinding[index], 0, index);
	imports = resolvedImports;

	int length = imports.length;
	this.typeOrPackageCache = new HashtableOfObject(length);
	for (int i = 0; i < length; i++) {
		ImportBinding binding = imports[i];
		if (!binding.onDemand && binding.resolvedImport instanceof ReferenceBinding || binding instanceof ImportConflictBinding)
			this.typeOrPackageCache.put(binding.compoundName[binding.compoundName.length - 1], binding);
	}
}
public void faultInTypes() {
	faultInImports();

	this.referenceContext.compilationUnitBinding.faultInTypesForFieldsAndMethods();
	for (int i = 0, length = topLevelTypes.length; i < length; i++)
		topLevelTypes[i].faultInTypesForFieldsAndMethods();
}

//this API is for code assist purpose
public Binding findImport(char[][] compoundName, boolean onDemand) {
	if(onDemand) {
		return findImport(compoundName, compoundName.length);
	} else {
		return findSingleImport(compoundName);
	}
}

private Binding findImport(char[][] compoundName, int length) {
	recordQualifiedReference(compoundName);

	Binding binding = environment.getTopLevelPackage(compoundName[0]);
	int i = 1;
	foundNothingOrType: if (binding != null) {
		PackageBinding packageBinding = (PackageBinding) binding;
		while (i < length) {
			int type = (i+1==length)?Binding.COMPILATION_UNIT: Binding.PACKAGE;
			binding = packageBinding.getTypeOrPackage(compoundName[i++], type);
			if (binding == null || !binding.isValidBinding()) {
				binding = null;
				break foundNothingOrType;
			}
			if (i==length && (binding instanceof CompilationUnitBinding))
				return binding;
			if (!(binding instanceof PackageBinding))
		 		break foundNothingOrType;

			packageBinding = (PackageBinding) binding;
		}
		return packageBinding;
	}

	ReferenceBinding type;
	if (binding == null) {
		if (environment.defaultPackage == null || compilerOptions().complianceLevel >= ClassFileConstants.JDK1_4)
			return new ProblemReferenceBinding(CharOperation.subarray(compoundName, 0, i), null, ProblemReasons.NotFound);
		type = findType(compoundName[0], environment.defaultPackage, environment.defaultPackage);
		if (type == null || !type.isValidBinding())
			return new ProblemReferenceBinding(CharOperation.subarray(compoundName, 0, i), null, ProblemReasons.NotFound);
		i = 1; // reset to look for member types inside the default package type
	} else {
		type = (ReferenceBinding) binding;
	}

	while (i < length) {
		if (!type.canBeSeenBy(environment.defaultPackage))
			return new ProblemReferenceBinding(CharOperation.subarray(compoundName, 0, i), type, ProblemReasons.NotVisible);

		char[] name = compoundName[i++];
		// does not look for inherited member types on purpose, only immediate members
		type = type.getMemberType(name);
		if (type == null)
			return new ProblemReferenceBinding(CharOperation.subarray(compoundName, 0, i), null, ProblemReasons.NotFound);
	}
	if (!type.canBeSeenBy(environment.defaultPackage))
		return new ProblemReferenceBinding(compoundName, type, ProblemReasons.NotVisible);
	return type;
}
private Binding findSingleImport(char[][] compoundName) {
	if (compoundName.length == 1) {
		// findType records the reference
		// the name cannot be a package
		if (environment.defaultPackage == null || compilerOptions().complianceLevel >= ClassFileConstants.JDK1_4)
			return new ProblemReferenceBinding(compoundName, null, ProblemReasons.NotFound);
		ReferenceBinding typeBinding = findType(compoundName[0], environment.defaultPackage, environment.defaultPackage);
		if (typeBinding == null)
			return new ProblemReferenceBinding(compoundName, null, ProblemReasons.NotFound);
		return typeBinding;
	}

	return findImport(compoundName, compoundName.length);
}

MethodBinding findStaticMethod(ReferenceBinding currentType, char[] selector) {
	if (!currentType.canBeSeenBy(this))
		return null;

	do {
		MethodBinding[] methods = currentType.getMethods(selector);
		if (methods != Binding.NO_METHODS) {
			for (int i = methods.length; --i >= 0;) {
				MethodBinding method = methods[i];
				if (method.isStatic() && method.canBeSeenBy(environment.defaultPackage))
					return method;
			}
		}

		((SourceTypeBinding) currentType).classScope.connectTypeHierarchy();
	} while ((currentType = currentType.superclass()) != null);
	return null;
}
ImportBinding[] getDefaultImports() {
	// initialize the default imports if necessary... share the default java.lang.* import
	Binding importBinding = environment.defaultPackage;
//	if (importBinding != null)
//		importBinding = ((PackageBinding) importBinding).getTypeOrPackage(JAVA_LANG[1]);

	// abort if java.lang cannot be found...
	if (importBinding == null || !importBinding.isValidBinding()) {
	// create a proxy for the missing BinaryType
		MissingBinaryTypeBinding missingObject = environment.cacheMissingBinaryType(JAVA_LANG_OBJECT, this.referenceContext);
		importBinding = missingObject.fPackage;
	}
	ImportBinding systemJSBinding = null;
	if (environment.defaultImports != null)
	{
		systemJSBinding=environment.defaultImports[0];
	}
	else
	{
		systemJSBinding=new ImportBinding(new char[][] {SystemLibraryLocation.SYSTEM_LIBARAY_NAME}, true, importBinding, (ImportReference)null);
		environment.defaultImports=new ImportBinding[]{systemJSBinding};
	}



	ImportBinding[] defaultImports=null;
	String[] contextIncludes=null;
	InferrenceProvider[] inferenceProviders = InferrenceManager.getInstance().getInferenceProviders(this.referenceContext);
    if (inferenceProviders!=null &&inferenceProviders.length>0)
    {
    	for(int i = 0; i < inferenceProviders.length; i++) {
    		if(contextIncludes == null) {
    			contextIncludes = inferenceProviders[i].getResolutionConfiguration().getContextIncludes();
    		} else {
    			String[] contextIncludesTemp = inferenceProviders[0].getResolutionConfiguration().getContextIncludes();
    			if(contextIncludesTemp != null) {
	    			String[] contextIncludesOld = contextIncludes;
	    			contextIncludes = new String[contextIncludesTemp.length + contextIncludesOld.length];
	    			System.arraycopy(contextIncludesOld, 0, contextIncludes, 0, contextIncludesOld.length);
	    			System.arraycopy(contextIncludesTemp, 0, contextIncludes, contextIncludesOld.length - 1, contextIncludesTemp.length);
    			}
    		}
    	}

    }
    if (contextIncludes!=null && contextIncludes.length>0)
    {
      ArrayList list = new ArrayList();
      list.add(systemJSBinding);
      for (int i = 0; i < contextIncludes.length; i++) {
		String include=contextIncludes[i];
		if (include!=null)
		{
			int index=Util.indexOfJavaLikeExtension(include);
			if (index>=0)
				include=include.substring(0,index);
			include=include.replace('.', FILENAME_DOT_SUBSTITUTION);
			char [][] qualifiedName=CharOperation.splitOn('/', include.toCharArray());
			Binding binding=findImport(qualifiedName, qualifiedName.length);
			if (binding.isValidBinding())
			{
				list.add(new ImportBinding(qualifiedName, true, binding, null));
			}
		}
	  }
      defaultImports = ( ImportBinding[])list.toArray( new ImportBinding[list.size()]);
    }
    else
    	defaultImports = new ImportBinding[] {systemJSBinding};
	return defaultImports ;
}
// NOT Public API
public final Binding getImport(char[][] compoundName, boolean onDemand) {
	if (onDemand)
		return findImport(compoundName, compoundName.length);
	return findSingleImport(compoundName);
}

public int nextCaptureID() {
	return this.captureID++;
}

/* Answer the problem reporter to use for raising new problems.
*
* Note that as a side-effect, this updates the current reference context
* (unit, type or method) in case the problem handler decides it is necessary
* to abort.
*/

public ProblemReporter problemReporter() {
	ProblemReporter problemReporter = referenceContext.problemReporter;
	problemReporter.referenceContext = referenceContext;
	return problemReporter;
}

/*
What do we hold onto:

1. when we resolve 'a.b.c', say we keep only 'a.b.c'
 & when we fail to resolve 'c' in 'a.b', lets keep 'a.b.c'
THEN when we come across a new/changed/removed item named 'a.b.c',
 we would find all references to 'a.b.c'
-> This approach fails because every type is resolved in every onDemand import to
 detect collision cases... so the references could be 10 times bigger than necessary.

2. when we resolve 'a.b.c', lets keep 'a.b' & 'c'
 & when we fail to resolve 'c' in 'a.b', lets keep 'a.b' & 'c'
THEN when we come across a new/changed/removed item named 'a.b.c',
 we would find all references to 'a.b' & 'c'
-> This approach does not have a space problem but fails to handle collision cases.
 What happens if a type is added named 'a.b'? We would search for 'a' & 'b' but
 would not find a match.

3. when we resolve 'a.b.c', lets keep 'a', 'a.b' & 'a', 'b', 'c'
 & when we fail to resolve 'c' in 'a.b', lets keep 'a', 'a.b' & 'a', 'b', 'c'
THEN when we come across a new/changed/removed item named 'a.b.c',
 we would find all references to 'a.b' & 'c'
OR 'a.b' -> 'a' & 'b'
OR 'a' -> '' & 'a'
-> As long as each single char[] is interned, we should not have a space problem
 and can handle collision cases.

4. when we resolve 'a.b.c', lets keep 'a.b' & 'a', 'b', 'c'
 & when we fail to resolve 'c' in 'a.b', lets keep 'a.b' & 'a', 'b', 'c'
THEN when we come across a new/changed/removed item named 'a.b.c',
 we would find all references to 'a.b' & 'c'
OR 'a.b' -> 'a' & 'b' in the simple name collection
OR 'a' -> 'a' in the simple name collection
-> As long as each single char[] is interned, we should not have a space problem
 and can handle collision cases.
*/
void recordQualifiedReference(char[][] qualifiedName) {
	if (qualifiedReferences == null) return; // not recording dependencies

	int length = qualifiedName.length;
	if (length > 1) {
		while (!qualifiedReferences.contains(qualifiedName)) {
			qualifiedReferences.add(qualifiedName);
			if (length == 2) {
				recordSimpleReference(qualifiedName[0]);
				recordSimpleReference(qualifiedName[1]);
				return;
			}
			length--;
			recordSimpleReference(qualifiedName[length]);
			System.arraycopy(qualifiedName, 0, qualifiedName = new char[length][], 0, length);
		}
	} else if (length == 1) {
		recordSimpleReference(qualifiedName[0]);
	}
}
void recordReference(char[][] qualifiedEnclosingName, char[] simpleName) {
	recordQualifiedReference(qualifiedEnclosingName);
	recordSimpleReference(simpleName);
}
void recordReference(ReferenceBinding type, char[] simpleName) {
	ReferenceBinding actualType = typeToRecord(type);
	if (actualType != null)
		recordReference(actualType.compoundName, simpleName);
}
void recordSimpleReference(char[] simpleName) {
	if (simpleNameReferences == null) return; // not recording dependencies

	if (!simpleNameReferences.contains(simpleName))
		simpleNameReferences.add(simpleName);
}
void recordSuperTypeReference(TypeBinding type) {
	if (referencedSuperTypes == null) return; // not recording dependencies

	ReferenceBinding actualType = typeToRecord(type);
	if (actualType != null && !referencedSuperTypes.containsIdentical(actualType))
		referencedSuperTypes.add(actualType);
}
public void recordTypeConversion(TypeBinding superType, TypeBinding subType) {
	recordSuperTypeReference(subType); // must record the hierarchy of the subType that is converted to the superType
}
void recordTypeReference(TypeBinding type) {
	if (referencedTypes == null) return; // not recording dependencies

	ReferenceBinding actualType = typeToRecord(type);
	if (actualType != null && !referencedTypes.containsIdentical(actualType))
		referencedTypes.add(actualType);
}
void recordTypeReferences(TypeBinding[] types) {
	if (referencedTypes == null) return; // not recording dependencies
	if (types == null || types.length == 0) return;

	for (int i = 0, max = types.length; i < max; i++) {
		// No need to record supertypes of method arguments & thrown exceptions, just the compoundName
		// If a field/method is retrieved from such a type then a separate call does the job
		ReferenceBinding actualType = typeToRecord(types[i]);
		if (actualType != null && !referencedTypes.containsIdentical(actualType))
			referencedTypes.add(actualType);
	}
}
Binding resolveSingleImport(ImportBinding importBinding) {
	if (importBinding.resolvedImport == null) {
		importBinding.resolvedImport = findSingleImport(importBinding.compoundName);
		if (!importBinding.resolvedImport.isValidBinding() || importBinding.resolvedImport instanceof PackageBinding) {
			if (this.imports != null) {
				ImportBinding[] newImports = new ImportBinding[imports.length - 1];
				for (int i = 0, n = 0, max = this.imports.length; i < max; i++)
					if (this.imports[i] != importBinding)
						newImports[n++] = this.imports[i];
				this.imports = newImports;
			}
			return null;
		}
	}
	return importBinding.resolvedImport;
}
public void storeDependencyInfo() {
	// add the type hierarchy of each referenced supertype
	// cannot do early since the hierarchy may not be fully resolved
	for (int i = 0; i < referencedSuperTypes.size; i++) { // grows as more types are added
		ReferenceBinding type = (ReferenceBinding) referencedSuperTypes.elementAt(i);
		if (!referencedTypes.containsIdentical(type))
			referencedTypes.add(type);

		if (!type.isLocalType()) {
			ReferenceBinding enclosing = type.enclosingType();
			if (enclosing != null)
				recordSuperTypeReference(enclosing);
		}
		ReferenceBinding superclass = type.superclass();
		if (superclass != null)
			recordSuperTypeReference(superclass);
	}

	for (int i = 0, l = referencedTypes.size; i < l; i++) {
		ReferenceBinding type = (ReferenceBinding) referencedTypes.elementAt(i);
		if (type instanceof MultipleTypeBinding)
		{
			ReferenceBinding[] types = ((MultipleTypeBinding)type).types;
			for (int j = 0; j < types.length; j++) {
				if (!types[j].isLocalType())
					recordQualifiedReference(types[j].isMemberType()
						? CharOperation.splitOn('.', types[j].readableName())
						: types[j].compoundName);

			}
		}
		else
		if (!type.isLocalType())
			recordQualifiedReference(type.isMemberType()
				? CharOperation.splitOn('.', type.readableName())
				: type.compoundName);
	}

	int size = qualifiedReferences.size;
	char[][][] qualifiedRefs = new char[size][][];
	for (int i = 0; i < size; i++)
		qualifiedRefs[i] = qualifiedReferences.elementAt(i);
	referenceContext.compilationResult.qualifiedReferences = qualifiedRefs;

	size = simpleNameReferences.size;
	char[][] simpleRefs = new char[size][];
	for (int i = 0; i < size; i++)
		simpleRefs[i] = simpleNameReferences.elementAt(i);
	referenceContext.compilationResult.simpleNameReferences = simpleRefs;
}
public String toString() {
	return "--- JavaScriptUnit Scope : " + new String(referenceContext.getFileName()); //$NON-NLS-1$
}
private ReferenceBinding typeToRecord(TypeBinding type) {
	while (type.isArrayType())
		type = ((ArrayBinding) type).leafComponentType;

	switch (type.kind()) {
		case Binding.BASE_TYPE :
			return null;
	}
	if (type instanceof CompilationUnitBinding)
		return null;
	ReferenceBinding refType = (ReferenceBinding) type;
	if (refType.isLocalType()) return null;
	return refType;
}
public void verifyMethods(MethodVerifier verifier) {
	for (int i = 0, length = topLevelTypes.length; i < length; i++)
		topLevelTypes[i].verifyMethods(verifier);
 }

public void cleanup()
{

	if (this.referencedTypes!=null)
	  for (int i = 0, l = referencedTypes.size; i < l; i++) {
		Object obj=referencedTypes.elementAt(i);
		if (obj instanceof SourceTypeBinding)
		{
			SourceTypeBinding type = (SourceTypeBinding) obj;
			type.cleanup();
		}
	}
}

public void addExternalVar(LocalVariableBinding binding) {
  externalCompilationUnits.add(binding.declaringScope.compilationUnitScope());
}
}
