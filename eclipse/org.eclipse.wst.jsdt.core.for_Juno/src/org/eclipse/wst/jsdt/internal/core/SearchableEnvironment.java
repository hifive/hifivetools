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
package org.eclipse.wst.jsdt.internal.core;

import java.util.ArrayList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.wst.jsdt.core.IJavaScriptElement;
import org.eclipse.wst.jsdt.core.IOpenable;
import org.eclipse.wst.jsdt.core.IPackageFragment;
import org.eclipse.wst.jsdt.core.IType;
import org.eclipse.wst.jsdt.core.JavaScriptCore;
import org.eclipse.wst.jsdt.core.JavaScriptModelException;
import org.eclipse.wst.jsdt.core.UnimplementedException;
import org.eclipse.wst.jsdt.core.WorkingCopyOwner;
import org.eclipse.wst.jsdt.core.compiler.CharOperation;
import org.eclipse.wst.jsdt.core.infer.IInferenceFile;
import org.eclipse.wst.jsdt.core.search.IJavaScriptSearchConstants;
import org.eclipse.wst.jsdt.core.search.IJavaScriptSearchScope;
import org.eclipse.wst.jsdt.core.search.SearchPattern;
import org.eclipse.wst.jsdt.internal.codeassist.ISearchRequestor;
import org.eclipse.wst.jsdt.internal.compiler.env.AccessRestriction;
import org.eclipse.wst.jsdt.internal.compiler.env.IBinaryType;
import org.eclipse.wst.jsdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.wst.jsdt.internal.compiler.env.INameEnvironment;
import org.eclipse.wst.jsdt.internal.compiler.env.ISourceType;
import org.eclipse.wst.jsdt.internal.compiler.env.NameEnvironmentAnswer;
import org.eclipse.wst.jsdt.internal.compiler.impl.ITypeRequestor;
import org.eclipse.wst.jsdt.internal.core.search.BasicSearchEngine;
import org.eclipse.wst.jsdt.internal.core.search.IConstructorRequestor;
import org.eclipse.wst.jsdt.internal.core.search.IRestrictedAccessBindingRequestor;
import org.eclipse.wst.jsdt.internal.core.search.IRestrictedAccessTypeRequestor;

/**
 * This class provides a <code>SearchableBuilderEnvironment</code> for code
 * assist which uses the Java model as a search tool.
 */
public class SearchableEnvironment implements INameEnvironment,
		IJavaScriptSearchConstants {

	public NameLookup nameLookup;

	protected ICompilationUnit unitToSkip;

	protected org.eclipse.wst.jsdt.core.IJavaScriptUnit[] workingCopies;

	protected JavaProject javaProject;

	protected IJavaScriptSearchScope searchScope;

	protected boolean checkAccessRestrictions;

	/**
	 * Creates a SearchableEnvironment on the given project
	 */
	public SearchableEnvironment(JavaProject project,
			IRestrictedAccessBindingRequestor resolutionScope,
			org.eclipse.wst.jsdt.core.IJavaScriptUnit[] workingCopies)
			throws JavaScriptModelException {

		this.javaProject = project;
		this.checkAccessRestrictions = !JavaScriptCore.IGNORE.equals(project
				.getOption(JavaScriptCore.COMPILER_PB_FORBIDDEN_REFERENCE, true))
				|| !JavaScriptCore.IGNORE.equals(project.getOption(
						JavaScriptCore.COMPILER_PB_DISCOURAGED_REFERENCE, true));
		this.workingCopies = workingCopies;
		this.nameLookup = javaProject.newNameLookup(workingCopies);
		this.nameLookup.setRestrictedAccessRequestor(resolutionScope);
		// Create search scope with visible entry on the project's classpath
		if (false){//this.checkAccessRestrictions) {
			this.searchScope = BasicSearchEngine
					.createJavaSearchScope(this.nameLookup.packageFragmentRoots );
				} else {
			this.searchScope = BasicSearchEngine
					.createJavaSearchScope(this.nameLookup.packageFragmentRoots);
		}
		this.nameLookup.searchScope=this.searchScope;
	}
	public SearchableEnvironment(JavaProject project,

			org.eclipse.wst.jsdt.core.IJavaScriptUnit[] workingCopies)
			throws JavaScriptModelException {
		this(project,null,workingCopies);
	}
	/**
	 * Creates a SearchableEnvironment on the given project
	 */
	public SearchableEnvironment(JavaProject project, WorkingCopyOwner owner)
			throws JavaScriptModelException {
		this(project, owner == null ? null : JavaModelManager
				.getJavaModelManager()
				.getWorkingCopies(owner, true/* add primary WCs */));
	}

	public SearchableEnvironment(JavaProject project, IRestrictedAccessBindingRequestor resolutionScope,WorkingCopyOwner owner)
	throws JavaScriptModelException {
		this(project, resolutionScope, owner == null ? null : JavaModelManager
		.getJavaModelManager()
		.getWorkingCopies(owner, true/* add primary WCs */));
}
	private static int convertSearchFilterToModelFilter(int searchFilter) {
		switch (searchFilter) {
			case IJavaScriptSearchConstants.CLASS:
				return NameLookup.ACCEPT_CLASSES;
			default:
				return NameLookup.ACCEPT_ALL;
		}
	}

	/**
	 * Returns the given type in the the given package if it exists, otherwise
	 * <code>null</code>.
	 */
	protected NameEnvironmentAnswer find(String typeName, String packageName) {
		if (packageName == null)
			packageName = IPackageFragment.DEFAULT_PACKAGE_NAME;
		NameLookup.Answer answer = this.nameLookup.findType(typeName,
				packageName, false/* exact match */, NameLookup.ACCEPT_ALL,
				this.checkAccessRestrictions);
		if (answer != null) {
			// construct name env answer
			if (answer.type instanceof BinaryType) { // BinaryType
				try {
					return new NameEnvironmentAnswer(
							(IBinaryType) ((BinaryType) answer.type)
									.getElementInfo(), answer.restriction);
				} catch (JavaScriptModelException npe) {
					return null;
				}
			} else { // SourceType
				try {
					// retrieve the requested type
					SourceTypeElementInfo sourceType = (SourceTypeElementInfo) ((SourceType) answer.type)
							.getElementInfo();
					if (answer.type.isBinary())
					{
						ICompilationUnit compUnit=(ICompilationUnit)answer.type.getClassFile();
						return new NameEnvironmentAnswer(compUnit,answer.restriction);
					}
					ISourceType topLevelType = sourceType;
					while (topLevelType.getEnclosingType() != null) {
						topLevelType = topLevelType.getEnclosingType();
					}
					IType[] types = null;
					org.eclipse.wst.jsdt.core.IJavaScriptUnit compilationUnit = sourceType.getHandle().getJavaScriptUnit();
					// find all siblings (other types declared in same unit,
					// since may be used for name resolution)
					if (compilationUnit!=null)
					 types = compilationUnit.getTypes();
					else if (sourceType.getHandle().getClassFile()!=null)
						 types = sourceType.getHandle().getClassFile().getTypes();

					ISourceType[] sourceTypes = new ISourceType[types.length];

					// in the resulting collection, ensure the requested type is
					// the first one
					sourceTypes[0] = sourceType;
					int length = types.length;
					for (int i = 0, index = 1; i < length; i++) {
						ISourceType otherType = (ISourceType) ((JavaElement) types[i])
								.getElementInfo();
						if (!otherType.equals(topLevelType) && index < length) // check
																				// that
																				// the
																				// index
																				// is
																				// in
																				// bounds
																				// (see
																				// https://bugs.eclipse.org/bugs/show_bug.cgi?id=62861)
							sourceTypes[index++] = otherType;
					}
					return new NameEnvironmentAnswer(sourceTypes,
							answer.restriction);
				} catch (JavaScriptModelException npe) {
					return null;
				}
			}
		}
		return null;
	}

	/**
	 * Returns the given type in the the given package if it exists, otherwise
	 * <code>null</code>.
	 */
	protected NameEnvironmentAnswer findBinding(String typeName, String packageName,
			int type, boolean returnMultiple, String excludePath) {
		if (packageName == null)
			packageName = IPackageFragment.DEFAULT_PACKAGE_NAME;
		NameLookup.Answer answer =
			this.nameLookup.findBinding(
				typeName,
				packageName,
				type,
				false/* exact match */,
				NameLookup.ACCEPT_ALL,
				this.checkAccessRestrictions,  returnMultiple,  excludePath);
		if (answer != null && answer.element!=null) {
			if (answer.element instanceof IJavaScriptElement)
			{
				IOpenable openable = ((IJavaScriptElement)answer.element).getOpenable();

				ICompilationUnit compilationUnit=	null;
				if (openable instanceof ClassFile) {
					ClassFile classFile = (ClassFile) openable;
					compilationUnit=classFile;
				}
				else if (openable instanceof MetadataFile) {
					return new NameEnvironmentAnswer(((MetadataFile)openable).getAPIs());
				}
				else if (openable instanceof ICompilationUnit) {
					compilationUnit=(ICompilationUnit)openable;
				}
				return new NameEnvironmentAnswer(compilationUnit,answer.restriction);

			}
			else if (answer.element!=null && answer.element.getClass().isArray())
			{
				Object [] elements=(Object [])answer.element;
				ICompilationUnit[] units = new ICompilationUnit[elements.length];
				System.arraycopy(elements, 0, units, 0, elements.length);
				return new NameEnvironmentAnswer(units,answer.restriction);
			}
				// return new NameEnvironmentAnswer((IBinaryType) ((BinaryType)
				// answer.type).getElementInfo(), answer.restriction);
		}
		return null;
	}

	/**
	 * Find the packages that start with the given prefix. A valid prefix is a
	 * qualified name separated by periods (ex. java.util). The packages found
	 * are passed to: ISearchRequestor.acceptPackage(char[][] packageName)
	 */
	public void findPackages(char[] prefix, ISearchRequestor requestor) {
		this.nameLookup.seekPackageFragments(new String(prefix), true,
				new SearchableEnvironmentRequestor(requestor));
	}

	/**
	 * Find the top-level types that are defined
	 * in the current environment and whose simple name matches the given name.
	 *
	 * The types found are passed to one of the following methods (if additional
	 * information is known about the types):
	 *    ISearchRequestor.acceptType(char[][] packageName, char[] typeName)
	 *    ISearchRequestor.acceptClass(char[][] packageName, char[] typeName, int modifiers)
	 *    ISearchRequestor.acceptInterface(char[][] packageName, char[] typeName, int modifiers)
	 *
	 * This method can not be used to find member types... member
	 * types are found relative to their enclosing type.
	 */
	public void findExactTypes(char[] name, final boolean findMembers, int searchFor, final ISearchRequestor storage) {

		try {
			final String excludePath;
			if (this.unitToSkip != null) {
				if (!(this.unitToSkip instanceof IJavaScriptElement)) {
					// revert to model investigation
					findExactTypes(
						new String(name),
						storage,
						convertSearchFilterToModelFilter(searchFor));
					return;
				}
				excludePath = ((IJavaScriptElement) this.unitToSkip).getPath().toString();
			} else {
				excludePath = null;
			}

			IProgressMonitor progressMonitor = new IProgressMonitor() {
				boolean isCanceled = false;
				public void beginTask(String n, int totalWork) {
					// implements interface method
				}
				public void done() {
					// implements interface method
				}
				public void internalWorked(double work) {
					// implements interface method
				}
				public boolean isCanceled() {
					return isCanceled;
				}
				public void setCanceled(boolean value) {
					isCanceled = value;
				}
				public void setTaskName(String n) {
					// implements interface method
				}
				public void subTask(String n) {
					// implements interface method
				}
				public void worked(int work) {
					// implements interface method
				}
			};
			IRestrictedAccessTypeRequestor typeRequestor = new IRestrictedAccessTypeRequestor() {
				public void acceptType(int modifiers, char[] packageName, char[] simpleTypeName, char[][] enclosingTypeNames, String path, AccessRestriction access) {
					if (excludePath != null && excludePath.equals(path))
						return;
					if (!findMembers && enclosingTypeNames != null && enclosingTypeNames.length > 0)
						return; // accept only top level types
					storage.acceptType(packageName, path.toCharArray(),simpleTypeName, enclosingTypeNames, modifiers, access);
				}
			};
			try {
				new BasicSearchEngine(this.workingCopies).searchAllTypeNames(
					null,
					SearchPattern.R_EXACT_MATCH,
					name,
					SearchPattern.R_EXACT_MATCH,
					searchFor,
					this.searchScope,
					typeRequestor,
					CANCEL_IF_NOT_READY_TO_SEARCH,
					progressMonitor);
			} catch (OperationCanceledException e) {
				findExactTypes(
					new String(name),
					storage,
					convertSearchFilterToModelFilter(searchFor));
			}
		} catch (JavaScriptModelException e) {
			findExactTypes(
				new String(name),
				storage,
				convertSearchFilterToModelFilter(searchFor));
		}
	}

	/**
	 * Returns all types whose simple name matches with the given <code>name</code>.
	 */
	private void findExactTypes(String name, ISearchRequestor storage, int type) {
		SearchableEnvironmentRequestor requestor =
			new SearchableEnvironmentRequestor(storage, this.unitToSkip, this.javaProject, this.nameLookup);
		this.nameLookup.seekTypes(name, null, false, type, requestor);
	}


	/**
	 * @see org.eclipse.wst.jsdt.internal.compiler.env.INameEnvironment#findType(char[][])
	 */
	public NameEnvironmentAnswer findType(char[][] compoundTypeName,
			ITypeRequestor requestor) {
		if (compoundTypeName == null)
			return null;

		int length = compoundTypeName.length;
		if (length <= 1) {
			if (length == 0)
				return null;
			return find(new String(compoundTypeName[0]), null);
		}

		int lengthM1 = length - 1;
		char[][] packageName = new char[lengthM1][];
		System.arraycopy(compoundTypeName, 0, packageName, 0, lengthM1);

		return find(new String(compoundTypeName[lengthM1]), CharOperation
				.toString(packageName));
	}

	public NameEnvironmentAnswer findBinding(char[] typeName,
			char[][] packageName, int type, ITypeRequestor requestor, boolean returnMultiple, String excludePath) {
		if (typeName == null)
			return null;

		return findBinding(new String(typeName), packageName == null
				|| packageName.length == 0 ? null : CharOperation
				.toString(packageName), type,  returnMultiple,excludePath );
	}

	/**
	 * @see org.eclipse.wst.jsdt.internal.compiler.env.INameEnvironment#findType(char[],
	 *      char[][])
	 */
	public NameEnvironmentAnswer findType(char[] name, char[][] packageName,
			ITypeRequestor requestor) {
		if (name == null)
			return null;

		return find(new String(name), packageName == null
				|| packageName.length == 0 ? null : CharOperation
				.toString(packageName));
	}

	/**
	 * Find the top-level types that are defined in the
	 * current environment and whose name starts with the given prefix. The
	 * prefix is a qualified name separated by periods or a simple name (ex.
	 * java.util.V or V).
	 *
	 * The types found are passed to one of the following methods (if additional
	 * information is known about the types):
	 * ISearchRequestor.acceptType(char[][] packageName, char[] typeName)
	 * ISearchRequestor.acceptClass(char[][] packageName, char[] typeName, int
	 * modifiers) ISearchRequestor.acceptInterface(char[][] packageName, char[]
	 * typeName, int modifiers)
	 *
	 * This method can not be used to find member types... member types are
	 * found relative to their enclosing type.
	 */
	public void findTypes(char[] prefix, final boolean findMembers,
			boolean camelCaseMatch, int searchFor, final ISearchRequestor storage) {

		/*
		 * if (true){ findTypes(new String(prefix), storage,
		 * NameLookup.ACCEPT_CLASSES | NameLookup.ACCEPT_INTERFACES); return; }
		 */
		try {
			final String excludePath;
			if (this.unitToSkip != null) {
				if (!(this.unitToSkip instanceof IJavaScriptElement)) {
					// revert to model investigation
					findTypes(new String(prefix), storage,
							convertSearchFilterToModelFilter(searchFor));
					return;
				}
				excludePath = ((IJavaScriptElement) this.unitToSkip).getPath()
						.toString();
			} else {
				excludePath = null;
			}
			
			//int lastDotIndex = CharOperation.lastIndexOf('.', prefix);
			// just use the simple name field to hold the full type name
			char[] qualification, simpleName;
			qualification = null;
			if (camelCaseMatch) {
				simpleName = prefix;
			} else {
				simpleName = CharOperation.toLowerCase(prefix);
			}
			
//			if (lastDotIndex < 0 || true) {
//				qualification = null;
//				if (camelCaseMatch) {
//					simpleName = prefix;
//				} else {
//					simpleName = CharOperation.toLowerCase(prefix);
//				}
//			} else {
//				qualification = CharOperation.subarray(prefix, 0, lastDotIndex);
//				if (camelCaseMatch) {
//					simpleName = CharOperation.subarray(prefix,
//							lastDotIndex + 1, prefix.length);
//				} else {
//					simpleName = CharOperation.toLowerCase(CharOperation
//							.subarray(prefix, lastDotIndex + 1, prefix.length));
//				}
//			}

			IProgressMonitor progressMonitor = new IProgressMonitor() {
				boolean isCanceled = false;

				public void beginTask(String name, int totalWork) {
					// implements interface method
				}

				public void done() {
					// implements interface method
				}

				public void internalWorked(double work) {
					// implements interface method
				}

				public boolean isCanceled() {
					return isCanceled;
				}

				public void setCanceled(boolean value) {
					isCanceled = value;
				}

				public void setTaskName(String name) {
					// implements interface method
				}

				public void subTask(String name) {
					// implements interface method
				}

				public void worked(int work) {
					// implements interface method
				}
			};
			IRestrictedAccessTypeRequestor typeRequestor = new IRestrictedAccessTypeRequestor() {
				public void acceptType(int modifiers, char[] packageName, char[] simpleTypeName, char[][] enclosingTypeNames, String path, AccessRestriction access) {
					if (excludePath != null && excludePath.equals(path))
						return;
					if (!findMembers && enclosingTypeNames != null && enclosingTypeNames.length > 0)
						return; // accept only top level types
					storage.acceptType(packageName, path.toCharArray(),simpleTypeName, enclosingTypeNames, modifiers, access);
				}
			};
			try {
				int matchRule = SearchPattern.R_PREFIX_MATCH;
				if (camelCaseMatch)
					matchRule |= SearchPattern.R_CAMELCASE_MATCH;
				new BasicSearchEngine(this.workingCopies).searchAllTypeNames(
						qualification,
						SearchPattern.R_EXACT_MATCH,
						simpleName,
						matchRule, // not case sensitive
						searchFor,
						this.searchScope,
						typeRequestor, CANCEL_IF_NOT_READY_TO_SEARCH,
						progressMonitor);
			} catch (OperationCanceledException e) {
				findTypes(new String(prefix), storage, convertSearchFilterToModelFilter(searchFor));
			}
		} catch (JavaScriptModelException e) {
			findTypes(new String(prefix), storage, NameLookup.ACCEPT_ALL);
		}
	}



	/**
	 * Find the top-level types (classes and interfaces) that are defined in the
	 * current environment and whose name starts with the given prefix. The
	 * prefix is a qualified name separated by periods or a simple name (ex.
	 * java.util.V or V).
	 *
	 * The types found are passed to one of the following methods (if additional
	 * information is known about the types):
	 * ISearchRequestor.acceptType(char[][] packageName, char[] typeName)
	 * ISearchRequestor.acceptClass(char[][] packageName, char[] typeName, int
	 * modifiers) ISearchRequestor.acceptInterface(char[][] packageName, char[]
	 * typeName, int modifiers)
	 *
	 * This method can not be used to find member types... member types are
	 * found relative to their enclosing type.
	 */
	public void findBindings(char[] prefix, final int bindingType,
			boolean camelCaseMatch, final ISearchRequestor storage) {

		/*
		 * if (true){ findTypes(new String(prefix), storage,
		 * NameLookup.ACCEPT_CLASSES | NameLookup.ACCEPT_INTERFACES); return; }
		 */
		try {
			final String excludePath;
			if (this.unitToSkip != null) {
				if (!(this.unitToSkip instanceof IJavaScriptElement)) {
					// revert to model investigation
					findBindings(new String(prefix),bindingType, storage,
							NameLookup.ACCEPT_ALL);
					return;
				}
				excludePath = ((IJavaScriptElement) this.unitToSkip).getPath()
						.toString();
			} else {
				excludePath = null;
			}
			int lastDotIndex = CharOperation.lastIndexOf('.', prefix);
			char[] qualification, simpleName;
			if (lastDotIndex < 0) {
				qualification = null;
				if (camelCaseMatch) {
					simpleName = prefix;
				} else {
					simpleName = CharOperation.toLowerCase(prefix);
				}
			} else {
				qualification = CharOperation.subarray(prefix, 0, lastDotIndex);
				if (camelCaseMatch) {
					simpleName = CharOperation.subarray(prefix,
							lastDotIndex + 1, prefix.length);
				} else {
					simpleName = CharOperation.toLowerCase(CharOperation
							.subarray(prefix, lastDotIndex + 1, prefix.length));
				}
			}

			IProgressMonitor progressMonitor = new IProgressMonitor() {
				boolean isCanceled = false;

				public void beginTask(String name, int totalWork) {
					// implements interface method
				}

				public void done() {
					// implements interface method
				}

				public void internalWorked(double work) {
					// implements interface method
				}

				public boolean isCanceled() {
					return isCanceled;
				}

				public void setCanceled(boolean value) {
					isCanceled = value;
				}

				public void setTaskName(String name) {
					// implements interface method
				}

				public void subTask(String name) {
					// implements interface method
				}

				public void worked(int work) {
					// implements interface method
				}
			};
			IRestrictedAccessBindingRequestor bindingRequestor = new IRestrictedAccessBindingRequestor() {
				String exclude;
				public boolean acceptBinding(int type,int modifiers, char[] packageName,
						char[] simpleTypeName,
						String path, AccessRestriction access) {
					if (exclude!=null && exclude.equals(path))
						return false;
					if (excludePath != null && excludePath.equals(path))
						return false;
					storage.acceptBinding(packageName, path.toCharArray(),simpleTypeName,type,
							  modifiers, access);
					return true;
				}


				public String getFoundPath() {

					return null;
				}

				public void reset() {}


				public ArrayList getFoundPaths() {
					return null;
				}


				public void setExcludePath(String excludePath) {
					this.exclude=excludePath;

				}
			};
			try {
				int matchRule = SearchPattern.R_PREFIX_MATCH;
				if (camelCaseMatch)
					matchRule |= SearchPattern.R_CAMELCASE_MATCH;
				new BasicSearchEngine(this.workingCopies).searchAllBindingNames(
						qualification,
						simpleName,
						bindingType,
						matchRule, // not case sensitive
						/*IJavaScriptSearchConstants.TYPE,*/ this.searchScope,
						bindingRequestor, CANCEL_IF_NOT_READY_TO_SEARCH,
						true,
						progressMonitor);
			} catch (OperationCanceledException e) {
				findBindings(new String(prefix),bindingType, storage, NameLookup.ACCEPT_ALL);
			}
		} catch (JavaScriptModelException e) {
			findTypes(new String(prefix), storage, NameLookup.ACCEPT_ALL);
		}
	}

	/**
	 * <p>The progress monitor is used to be able to cancel completion operations</p>
	 * 
	 * <p>Find constructor declarations that are defined
	 * in the current environment and whose name starts with the
	 * given prefix. The prefix is a qualified name separated by periods
	 * or a simple name (ex. foo.bar.V or V).</p>
	 *
	 * <p>The constructors found are passed to
	 * {@link ISearchRequestor#acceptConstructor(int, char[], int, char[][], char[][], String, AccessRestriction)}</p>
	 * 
	 * @param prefix to use in the search
	 * @param storage to report constructor declarations matching the given prefix to
	 */
	public void findConstructorDeclarations(char[] prefix, final ISearchRequestor storage) {
		final String excludePath;
		if (this.unitToSkip != null && this.unitToSkip instanceof IJavaScriptElement) {
			excludePath = ((IJavaScriptElement) this.unitToSkip).getPath().toString();
		} else {
			excludePath = null;
		}

		IProgressMonitor progressMonitor = new IProgressMonitor() {
			boolean isCanceled = false;
			public void beginTask(String name, int totalWork) {
				// implements interface method
			}
			public void done() {
				// implements interface method
			}
			public void internalWorked(double work) {
				// implements interface method
			}
			public boolean isCanceled() {
				return this.isCanceled;
			}
			public void setCanceled(boolean value) {
				this.isCanceled = value;
			}
			public void setTaskName(String name) {
				// implements interface method
			}
			public void subTask(String name) {
				// implements interface method
			}
			public void worked(int work) {
				// implements interface method
			}
		};
		
		IConstructorRequestor constructorRequestor = new IConstructorRequestor() {
			/**
			 * @see org.eclipse.wst.jsdt.internal.core.search.IConstructorRequestor#acceptConstructor(
			 * 		int, char[], int, char[][], char[][], java.lang.String, org.eclipse.wst.jsdt.internal.compiler.env.AccessRestriction)
			 */
			public void acceptConstructor(
					int modifiers,
					char[] typeName,
					int parameterCount,
					char[][] parameterTypes,
					char[][] parameterNames,
					String path,
					AccessRestriction access) {
				
				if (excludePath != null && excludePath.equals(path))
					return;
				
				storage.acceptConstructor(
						modifiers,
						typeName,
						parameterCount,
						parameterTypes,
						parameterNames, 
						path,
						access);
			}
		};
		
		/* when matching on "Test|" will match on
		 * Test
		 * TestBar
		 * foo.Test
		 * foo.TestBar
		 * test.foo.Bar
		 */
		int matchRule = SearchPattern.R_REGEXP_MATCH;
		String escapedPrefix = new String(prefix);
		escapedPrefix.replaceAll("\\.", "\\."); //replace all "." with "\."
		String regex = "(.*\\." + escapedPrefix + "[^\\.]*)|(" + escapedPrefix + ".*)";
		
		try {
			new BasicSearchEngine(this.workingCopies).searchAllConstructorDeclarations(
					regex.toCharArray(),
					matchRule,
					this.searchScope,
					constructorRequestor,
					CANCEL_IF_NOT_READY_TO_SEARCH,
					progressMonitor);
		} catch (OperationCanceledException e) {
			Logger.logException("Constructor search operation canceled.", e);
		}
	}

	/**
	 * Returns all types whose name starts with the given (qualified)
	 * <code>prefix</code>.
	 *
	 * If the <code>prefix</code> is unqualified, all types whose simple name
	 * matches the <code>prefix</code> are returned.
	 */
	private void findTypes(String prefix, ISearchRequestor storage, int type) {
		// TODO (david) should add camel case support
		SearchableEnvironmentRequestor requestor = new SearchableEnvironmentRequestor(
				storage, this.unitToSkip, this.javaProject, this.nameLookup);
		int index = prefix.lastIndexOf('.');
		if (index == -1) {
			this.nameLookup.seekTypes(prefix, null, true, type, requestor);
		} else {
			String packageName = prefix.substring(0, index);
			JavaElementRequestor elementRequestor = new JavaElementRequestor();
			this.nameLookup.seekPackageFragments(packageName, false,
					elementRequestor);
			IPackageFragment[] fragments = elementRequestor
					.getPackageFragments();
			if (fragments != null) {
				String className = prefix.substring(index + 1);
				for (int i = 0, length = fragments.length; i < length; i++)
					if (fragments[i] != null)
						this.nameLookup.seekTypes(className, fragments[i],
								true, type, requestor);
			}
		}
	}

	private void findBindings(String prefix, int bindingType, ISearchRequestor storage, int type) {
		SearchableEnvironmentRequestor requestor = new SearchableEnvironmentRequestor(
				storage, this.unitToSkip, this.javaProject, this.nameLookup);
		int index = prefix.lastIndexOf('.');
		if (index == -1) {
			this.nameLookup.seekBindings(prefix, bindingType,null, true, type, requestor);
		} else {
			throw new UnimplementedException("shouldnt get here"); //$NON-NLS-1$
//			String packageName = prefix.substring(0, index);
//			JavaElementRequestor elementRequestor = new JavaElementRequestor();
//			this.nameLookup.seekPackageFragments(packageName, false,
//					elementRequestor);
//			IPackageFragment[] fragments = elementRequestor
//					.getPackageFragments();
//			if (fragments != null) {
//				String className = prefix.substring(index + 1);
//				for (int i = 0, length = fragments.length; i < length; i++)
//					if (fragments[i] != null)
//						this.nameLookup.seekTypes(className, fragments[i],
//								true, type, requestor);
//			}
		}
	}
	/**
	 * @see org.eclipse.wst.jsdt.internal.compiler.env.INameEnvironment#isPackage(char[][],
	 *      char[])
	 */
	public boolean isPackage(char[][] parentPackageName, char[] subPackageName) {
		String[] pkgName;
		if (parentPackageName == null)
			pkgName = new String[] { new String(subPackageName) };
		else {
			int length = parentPackageName.length;
			pkgName = new String[length + 1];
			for (int i = 0; i < length; i++)
				pkgName[i] = new String(parentPackageName[i]);
			pkgName[length] = new String(subPackageName);
		}
		return this.nameLookup.isPackage(pkgName);
	}

	/**
	 * Returns a printable string for the array.
	 */
	protected String toStringChar(char[] name) {
		return "[" //$NON-NLS-1$
				+ new String(name) + "]"; //$NON-NLS-1$
	}

	/**
	 * Returns a printable string for the array.
	 */
	protected String toStringCharChar(char[][] names) {
		StringBuffer result = new StringBuffer();
		for (int i = 0; i < names.length; i++) {
			result.append(toStringChar(names[i]));
		}
		return result.toString();
	}

	public void cleanup() {
		// nothing to do
	}
	
	public void setCompilationUnit(IInferenceFile file)
	{
		nameLookup.setScriptFile(file);
	}
}
