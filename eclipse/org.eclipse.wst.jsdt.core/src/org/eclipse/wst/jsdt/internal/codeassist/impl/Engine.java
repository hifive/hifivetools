/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.wst.jsdt.internal.codeassist.impl;

import java.util.Map;

import org.eclipse.wst.jsdt.core.compiler.CharOperation;
import org.eclipse.wst.jsdt.internal.compiler.CompilationResult;
import org.eclipse.wst.jsdt.internal.compiler.ast.ASTNode;
import org.eclipse.wst.jsdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.wst.jsdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.wst.jsdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.wst.jsdt.internal.compiler.ast.Initializer;
import org.eclipse.wst.jsdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.wst.jsdt.internal.compiler.env.AccessRestriction;
import org.eclipse.wst.jsdt.internal.compiler.env.IBinaryType;
import org.eclipse.wst.jsdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.wst.jsdt.internal.compiler.env.ISourceType;
import org.eclipse.wst.jsdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.wst.jsdt.internal.compiler.impl.ITypeRequestor;
import org.eclipse.wst.jsdt.internal.compiler.impl.ITypeRequestor2;
import org.eclipse.wst.jsdt.internal.compiler.lookup.Binding;
import org.eclipse.wst.jsdt.internal.compiler.lookup.CompilationUnitScope;
import org.eclipse.wst.jsdt.internal.compiler.lookup.ImportBinding;
import org.eclipse.wst.jsdt.internal.compiler.lookup.ImportConflictBinding;
import org.eclipse.wst.jsdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.wst.jsdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.wst.jsdt.internal.compiler.lookup.PackageBinding;
import org.eclipse.wst.jsdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.wst.jsdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.wst.jsdt.internal.compiler.parser.SourceTypeConverter;
import org.eclipse.wst.jsdt.internal.compiler.problem.ProblemSeverities;
import org.eclipse.wst.jsdt.internal.core.NameLookup;
import org.eclipse.wst.jsdt.internal.core.SearchableEnvironment;
import org.eclipse.wst.jsdt.internal.oaametadata.LibraryAPIs;

public abstract class Engine implements ITypeRequestor, ITypeRequestor2 {

	public LookupEnvironment lookupEnvironment;

	protected CompilationUnitScope unitScope;
	public SearchableEnvironment nameEnvironment;

	public AssistOptions options;
	public CompilerOptions compilerOptions;
	public boolean forbiddenReferenceIsError;
	public boolean discouragedReferenceIsError;

	public boolean importCachesInitialized = false;
	public char[][][] importsCache;
	public ImportBinding[] onDemandImportsCache;
	public int importCacheCount = 0;
	public int onDemandImportCacheCount = 0;
	public char[] currentPackageName = null;

	public Engine(Map settings){
		this.options = new AssistOptions(settings);
		this.compilerOptions = new CompilerOptions(settings);
		this.forbiddenReferenceIsError =
			(this.compilerOptions.getSeverity(CompilerOptions.ForbiddenReference) & ProblemSeverities.Error) != 0;
		this.discouragedReferenceIsError =
			(this.compilerOptions.getSeverity(CompilerOptions.DiscouragedReference) & ProblemSeverities.Error) != 0;
	}

	/**
	 * Add an additional binary type
	 */
	public void accept(IBinaryType binaryType, PackageBinding packageBinding, AccessRestriction accessRestriction) {
//		lookupEnvironment.createBinaryTypeFrom(binaryType, packageBinding, accessRestriction);
	}

	public abstract CompilationUnitDeclaration doParse(ICompilationUnit unit, AccessRestriction accessRestriction) ;
	/**
	 * Add an additional compilation unit.
	 */
	public void accept(ICompilationUnit sourceUnit, AccessRestriction accessRestriction) {
		CompilationUnitDeclaration parsedUnit =  doParse(sourceUnit,accessRestriction);

		lookupEnvironment.buildTypeBindings(parsedUnit, accessRestriction);
		lookupEnvironment.completeTypeBindings(parsedUnit, true);
	}
	public void accept(ICompilationUnit unit, char[][] typeNames, AccessRestriction accessRestriction) {
		CompilationUnitDeclaration parsedUnit =  doParse(unit,accessRestriction);

		lookupEnvironment.buildTypeBindings(parsedUnit, typeNames, accessRestriction);
		lookupEnvironment.completeTypeBindings(parsedUnit, typeNames, true);		
	}

	/**
	 * Add additional source types (the first one is the requested type, the rest is formed by the
	 * secondary types defined in the same compilation unit).
	 */
	public void accept(ISourceType[] sourceTypes, PackageBinding packageBinding, AccessRestriction accessRestriction) {
		CompilationResult result =
			new CompilationResult(sourceTypes[0].getFileName(), sourceTypes[0].getPackageName(), 1, 1, this.compilerOptions.maxProblemsPerUnit);
		CompilationUnitDeclaration unit =
			SourceTypeConverter.buildCompilationUnit(
				sourceTypes,//sourceTypes[0] is always toplevel here
				SourceTypeConverter.FIELD_AND_METHOD // need field and methods
				| SourceTypeConverter.MEMBER_TYPE, // need member types
				// no need for field initialization
				lookupEnvironment.problemReporter,
				result);

		if (unit != null) {
			lookupEnvironment.buildTypeBindings(unit, accessRestriction);
			lookupEnvironment.completeTypeBindings(unit, true);
		}
	}

	public void accept(LibraryAPIs libraryMetaData) {
		lookupEnvironment.buildTypeBindings(libraryMetaData);
	}

	public abstract AssistParser getParser();

	public void initializeImportCaches() {
		ImportBinding[] importBindings = this.unitScope.imports;
		int length = importBindings == null ? 0 : importBindings.length;

		this.currentPackageName = CharOperation.concatWith(new char[][] {}, '.');

		for (int i = 0; i < length; i++) {
			ImportBinding importBinding = importBindings[i];
			if(importBinding.onDemand) {
				if(this.onDemandImportsCache == null) {
					this.onDemandImportsCache = new ImportBinding[length - i];
				}
				this.onDemandImportsCache[this.onDemandImportCacheCount++] =
					importBinding;
			} else {
				if(!(importBinding.resolvedImport instanceof MethodBinding) ||
						importBinding instanceof ImportConflictBinding) {
					if(this.importsCache == null) {
						this.importsCache = new char[length - i][][];
					}
					this.importsCache[this.importCacheCount++] = new char[][]{
							importBinding.compoundName[importBinding.compoundName.length - 1],
							CharOperation.concatWith(importBinding.compoundName, '.')
						};
				}
			}
		}

		this.importCachesInitialized = true;
	}

	protected boolean mustQualifyType(
		char[] packageName,
		char[] typeName,
		char[] enclosingTypeNames,
		int modifiers) {

		// If there are no types defined into the current CU yet.
		if (unitScope == null)
			return true;

		if(!this.importCachesInitialized) {
			this.initializeImportCaches();
		}


		for (int i = 0; i < this.importCacheCount; i++) {
			char[][] importName = this.importsCache[i];
			if(CharOperation.equals(typeName, importName[0])) {
				char[] fullyQualifiedTypeName =
					enclosingTypeNames == null || enclosingTypeNames.length == 0
							? CharOperation.concat(
									packageName,
									typeName,
									'.')
							: CharOperation.concat(
									CharOperation.concat(
										packageName,
										enclosingTypeNames,
										'.'),
									typeName,
									'.');
				return !CharOperation.equals(fullyQualifiedTypeName, importName[1]);
			}
		}

		if ((enclosingTypeNames == null || enclosingTypeNames.length == 0 ) && CharOperation.equals(this.currentPackageName, packageName))
			return false;

		char[] fullyQualifiedEnclosingTypeName = null;

		for (int i = 0; i < this.onDemandImportCacheCount; i++) {
			ImportBinding importBinding = this.onDemandImportsCache[i];
			Binding resolvedImport = importBinding.resolvedImport;

			char[][] importName = importBinding.compoundName;
			char[] importFlatName = CharOperation.concatWith(importName, '.');

			boolean isFound = false;
			// resolvedImport is a ReferenceBindng or a PackageBinding
			if(resolvedImport instanceof ReferenceBinding) {
				if(enclosingTypeNames != null && enclosingTypeNames.length != 0) {
					if(fullyQualifiedEnclosingTypeName == null) {
						fullyQualifiedEnclosingTypeName =
							CharOperation.concat(
									packageName,
									enclosingTypeNames,
									'.');
					}
					if(CharOperation.equals(fullyQualifiedEnclosingTypeName, importFlatName)) {
						isFound = true;
					}
				}
			} else {
				if(enclosingTypeNames == null || enclosingTypeNames.length == 0) {
					if(CharOperation.equals(packageName, importFlatName)) {
						isFound = true;
					}
				}
			}

			// find potential conflict with another import
			if(isFound) {
				for (int j = 0; j < this.onDemandImportCacheCount; j++) {
					if(i != j) {
						ImportBinding conflictingImportBinding = this.onDemandImportsCache[j];
						if(conflictingImportBinding.resolvedImport instanceof ReferenceBinding) {
							ReferenceBinding refBinding =
								(ReferenceBinding) conflictingImportBinding.resolvedImport;
							if (refBinding.getMemberType(typeName) != null) {
								return true;
							}
						} else {
							char[] conflictingImportName =
								CharOperation.concatWith(conflictingImportBinding.compoundName, '.');

							if (this.nameEnvironment.nameLookup.findType(
									String.valueOf(typeName),
									String.valueOf(conflictingImportName),
									false,
									NameLookup.ACCEPT_ALL,
									false/*don't check restrictions*/) != null) {
								return true;
							}
						}
					}
				}
				return false;
			}
		}
		return true;
	}

	/*
	 * Find the node (a field, a method or an initializer) at the given position
	 * and parse its block statements if it is a method or an initializer.
	 * Returns the node or null if not found
	 */
	protected ASTNode parseBlockStatements(CompilationUnitDeclaration unit, int position) {
		int length = unit.types.length;
		for (int i = 0; i < length; i++) {
			TypeDeclaration type = unit.types[i];
			if (type.declarationSourceStart < position
				&& type.declarationSourceEnd >= position) {
				getParser().scanner.setSource(unit.compilationResult);
				return parseBlockStatements(type, unit, position);
			}
		}
		return null;
	}

	private ASTNode parseBlockStatements(
		TypeDeclaration type,
		CompilationUnitDeclaration unit,
		int position) {
		//members
		TypeDeclaration[] memberTypes = type.memberTypes;
		if (memberTypes != null) {
			int length = memberTypes.length;
			for (int i = 0; i < length; i++) {
				TypeDeclaration memberType = memberTypes[i];
				if (memberType.bodyStart > position)
					continue;
				if (memberType.declarationSourceEnd >= position) {
					return parseBlockStatements(memberType, unit, position);
				}
			}
		}
		//methods
		AbstractMethodDeclaration[] methods = type.methods;
		if (methods != null) {
			int length = methods.length;
			for (int i = 0; i < length; i++) {
				AbstractMethodDeclaration method = methods[i];
				if (method.bodyStart > position)
					continue;

				if(method.isDefaultConstructor())
					continue;

				if (method.declarationSourceEnd >= position) {

					getParser().parseBlockStatements(method, unit);
					return method;
				}
			}
		}
		//initializers
		FieldDeclaration[] fields = type.fields;
		if (fields != null) {
			int length = fields.length;
			for (int i = 0; i < length; i++) {
				FieldDeclaration field = fields[i];
				if (field.sourceStart > position)
					continue;
				if (field.declarationSourceEnd >= position) {
					if (field instanceof Initializer) {
						getParser().parseBlockStatements((Initializer)field, type, unit);
					}
					return field;
				}
			}
		}
		return null;
	}

	protected void reset() {
		lookupEnvironment.reset();
	}

	public static char[] getTypeSignature(TypeBinding typeBinding) {
		return typeBinding.signature();
	}
	public static char[] getSignature(Binding binding) {
		char[] result = null;
		if ((binding.kind() & Binding.TYPE) != 0 || (binding.kind() & Binding.COMPILATION_UNIT) != 0) {
			TypeBinding typeBinding = (TypeBinding)binding;
			result = typeBinding.signature();
		} else if ((binding.kind() & Binding.METHOD) != 0) {
			MethodBinding methodBinding = (MethodBinding)binding;
			int oldMod = methodBinding.modifiers;
			result = methodBinding.signature();
			methodBinding.modifiers = oldMod;
		}
		if (result != null) {
			if ( (binding.kind() & Binding.TYPE) != 0 )
			result = CharOperation.replaceOnCopy(result, '/', '.');
		}
		return result;
	}

	public static char[][] getSignatures(Binding[] bindings) {
		int length = bindings == null ? 0 : bindings.length;
		char[][] signatures = new char[length][];
		for (int i = 0; i < length; i++) {
			signatures[i] = getSignature(bindings[i]);
		}
		return signatures;
	}
}
