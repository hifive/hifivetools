/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.wst.jsdt.internal.core;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.wst.jsdt.core.Flags;
import org.eclipse.wst.jsdt.core.IClassFile;
import org.eclipse.wst.jsdt.core.IField;
import org.eclipse.wst.jsdt.core.IFunction;
import org.eclipse.wst.jsdt.core.IJavaScriptElement;
import org.eclipse.wst.jsdt.core.IMember;
import org.eclipse.wst.jsdt.core.IPackageFragmentRoot;
import org.eclipse.wst.jsdt.core.ISourceRange;
import org.eclipse.wst.jsdt.core.ISourceReference;
import org.eclipse.wst.jsdt.core.IType;
import org.eclipse.wst.jsdt.core.JavaScriptModelException;
import org.eclipse.wst.jsdt.core.Signature;
import org.eclipse.wst.jsdt.core.compiler.CategorizedProblem;
import org.eclipse.wst.jsdt.core.compiler.CharOperation;
import org.eclipse.wst.jsdt.internal.compiler.IProblemFactory;
import org.eclipse.wst.jsdt.internal.compiler.ISourceElementRequestor;
import org.eclipse.wst.jsdt.internal.compiler.SourceElementParser;
import org.eclipse.wst.jsdt.internal.compiler.env.IBinaryType;
import org.eclipse.wst.jsdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.wst.jsdt.internal.compiler.problem.DefaultProblemFactory;
import org.eclipse.wst.jsdt.internal.compiler.util.SuffixConstants;
import org.eclipse.wst.jsdt.internal.compiler.util.Util;
import org.eclipse.wst.jsdt.internal.core.util.ReferenceInfoAdapter;

/**
 * A SourceMapper maps source code in a ZIP file to binary types in
 * a JAR. The SourceMapper uses the fuzzy parser to identify source
 * fragments in a .js file, and attempts to match the source code
 * with children in a binary type. A SourceMapper is associated
 * with a JarPackageFragment by an AttachSourceOperation.
 *
 * @see org.eclipse.wst.jsdt.internal.core.JarPackageFragment
 */
public class SourceMapper
	extends ReferenceInfoAdapter
	implements ISourceElementRequestor, SuffixConstants {

	public static boolean VERBOSE = false;
	/**
	 * Specifies the file name filter use to compute the root paths.
	 */
	private static final FilenameFilter FILENAME_FILTER = new FilenameFilter() {
		public boolean accept(File dir, String name) {
			return org.eclipse.wst.jsdt.internal.core.util.Util.isJavaLikeFileName(name);
		}
	};
	/**
	 * Specifies the location of the package fragment roots within
	 * the zip (empty specifies the default root). <code>null</code> is
	 * not a valid root path.
	 */
	protected ArrayList rootPaths;

	/**
	 * The binary type source is being mapped for
	 */
	protected BinaryType binaryType;

	/**
	 * The location of the zip file containing source.
	 */
	protected IPath sourcePath;
	/**
	 * Specifies the location of the package fragment root within
	 * the zip (empty specifies the default root). <code>null</code> is
	 * not a valid root path.
	 */
	protected String rootPath = ""; //$NON-NLS-1$

	/**
	 * Table that maps a binary method to its parameter names.
	 * Keys are the method handles, entries are <code>char[][]</code>.
	 */
	protected HashMap parameterNames;

	/**
	 * Table that maps a binary element to its <code>SourceRange</code>s.
	 * Keys are the element handles, entries are <code>SourceRange[]</code> which
	 * is a two element array; the first being source range, the second
	 * being name range.
	 */
	protected HashMap sourceRanges;

	/*
	 * A map from IJavaScriptElement to String[]
	 */
	protected HashMap categories;


	/**
	 * The unknown source range {-1, 0}
	 */
	public static final SourceRange UNKNOWN_RANGE = new SourceRange(-1, 0);

	/**
	 * The position within the source of the start of the
	 * current member element, or -1 if we are outside a member.
	 */
	protected int[] memberDeclarationStart;
	/**
	 * The <code>SourceRange</code> of the name of the current member element.
	 */
	protected SourceRange[] memberNameRange;
	/**
	 * The name of the current member element.
	 */
	protected String[] memberName;

	/**
	 * The parameter names for the current member method element.
	 */
	protected char[][][] methodParameterNames;

	/**
	 * The parameter types for the current member method element.
	 */
	protected char[][][] methodParameterTypes;


	/**
	 * The element searched for
	 */
	protected IJavaScriptElement searchedElement;

	/**
	 * imports references
	 */
	private HashMap importsTable;
	private HashMap importsCounterTable;

	/**
	 * Enclosing type information
	 */
	IType[] types;
	int[] typeDeclarationStarts;
	SourceRange[] typeNameRanges;
	int[] typeModifiers;
	int typeDepth;

	/**
	 *  Anonymous counter in case we want to map the source of an anonymous class.
	 */
	int anonymousCounter;
	int anonymousClassName;

	/**
	 *Options to be used
	 */
	String encoding;
	Map options;

	/**
	 * Use to handle root paths inference
	 */
	private boolean areRootPathsComputed;

	public SourceMapper() {
		this.areRootPathsComputed = false;
	}

	/**
	 * Creates a <code>SourceMapper</code> that locates source in the zip file
	 * at the given location in the specified package fragment root.
	 */
	public SourceMapper(IPath sourcePath, String rootPath, Map options) {
		this.areRootPathsComputed = false;
		this.options = options;
		try {
			this.encoding = ResourcesPlugin.getWorkspace().getRoot().getDefaultCharset();
		} catch (CoreException e) {
			// use no encoding
		}
		if (rootPath != null) {
			this.rootPaths = new ArrayList();
			this.rootPaths.add(rootPath);
		}
		this.sourcePath = sourcePath;
		this.sourceRanges = new HashMap();
		this.parameterNames = new HashMap();
		this.importsTable = new HashMap();
		this.importsCounterTable = new HashMap();
	}

	/**
	 * @see ISourceElementRequestor
	 */
	public void acceptImport(
			int declarationStart,
			int declarationEnd,
			char[][] tokens,
			boolean onDemand) {
		char[][] imports = (char[][]) this.importsTable.get(this.binaryType);
		int importsCounter;
		if (imports == null) {
			imports = new char[5][];
			importsCounter = 0;
		} else {
			importsCounter = ((Integer) this.importsCounterTable.get(this.binaryType)).intValue();
		}
		if (imports.length == importsCounter) {
			System.arraycopy(
				imports,
				0,
				(imports = new char[importsCounter * 2][]),
				0,
				importsCounter);
		}
		char[] name = CharOperation.concatWith(tokens, '.');
		if (onDemand) {
			int nameLength = name.length;
			System.arraycopy(name, 0, (name = new char[nameLength + 2]), 0, nameLength);
			name[nameLength] = '.';
			name[nameLength + 1] = '*';
		}
		imports[importsCounter++] = name;
		this.importsTable.put(this.binaryType, imports);
		this.importsCounterTable.put(this.binaryType, new Integer(importsCounter));
	}

	/**
	 * @see ISourceElementRequestor
	 */
	public void acceptLineSeparatorPositions(int[] positions) {
		//do nothing
	}

	/**
	 * @see ISourceElementRequestor
	 */
	public void acceptPackage(
		int declarationStart,
		int declarationEnd,
		char[] name) {
		//do nothing
	}

	/**
	 * @see ISourceElementRequestor
	 */
	public void acceptProblem(CategorizedProblem problem) {
		//do nothing
	}

	private void addCategories(IJavaScriptElement element, char[][] elementCategories) {
		if (elementCategories == null) return;
		if (this.categories == null)
			this.categories = new HashMap();
		this.categories.put(element, CharOperation.toStrings(elementCategories));
	}

	/**
	 * Closes this <code>SourceMapper</code>'s zip file. Once this is done, this
	 * <code>SourceMapper</code> cannot be used again.
	 */
	public void close() {
		this.sourceRanges = null;
		this.parameterNames = null;
	}

	/**
	 * Converts these type names to unqualified signatures. This needs to be done in order to be consistent
	 * with the way the source range is retrieved.
	 * @see SourceMapper#getUnqualifiedMethodHandle
	 * @see Signature
	 */
	private String[] convertTypeNamesToSigs(char[][] typeNames) {
		if (typeNames == null)
			return CharOperation.NO_STRINGS;
		int n = typeNames.length;
		if (n == 0)
			return CharOperation.NO_STRINGS;
		String[] typeSigs = new String[n];
		for (int i = 0; i < n; ++i) {
			char[] typeSig = Signature.createCharArrayTypeSignature(typeNames[i], false);

			// transforms signatures that contains a qualification into unqualified signatures
			// e.g. "QX<+QMap.Entry;>;" becomes "QX<+QEntry;>;"
			StringBuffer simpleTypeSig = null;
			int start = 0;
			int dot = -1;
			int length = typeSig.length;
			for (int j = 0; j < length; j++) {
				switch (typeSig[j]) {
					case Signature.C_UNRESOLVED:
						if (simpleTypeSig != null)
							simpleTypeSig.append(typeSig, start, j-start);
						start = j;
						break;
					case Signature.C_DOT:
						dot = j;
						break;
					case Signature.C_NAME_END:
						if (dot > start) {
							if (simpleTypeSig == null)
								simpleTypeSig = new StringBuffer().append(typeSig, 0, start);
							simpleTypeSig.append(Signature.C_UNRESOLVED);
							simpleTypeSig.append(typeSig, dot+1, j-dot-1);
							start = j;
						}
						break;
				}
			}
			if (simpleTypeSig == null) {
				typeSigs[i] = new String(typeSig);
			} else {
				simpleTypeSig.append(typeSig, start, length-start);
				typeSigs[i] = simpleTypeSig.toString();
			}
		}
		return typeSigs;
	}

	private synchronized void computeAllRootPaths(IType type) {
		if (this.areRootPathsComputed) {
			return;
		}
		IPackageFragmentRoot root = (IPackageFragmentRoot) type.getPackageFragment().getParent();
		final HashSet tempRoots = new HashSet();
		long time = 0;
		if (VERBOSE) {
			System.out.println("compute all root paths for " + root.getElementName()); //$NON-NLS-1$
			time = System.currentTimeMillis();
		}
		final HashSet firstLevelPackageNames = new HashSet();
		boolean containsADefaultPackage = false;

//		if (root.isArchive()) {
//			JarPackageFragmentRoot jarPackageFragmentRoot = (JarPackageFragmentRoot) root;
//			IJavaScriptProject project = jarPackageFragmentRoot.getJavaScriptProject();
//			String sourceLevel = null;
//			String complianceLevel = null;
//			JavaModelManager manager = JavaModelManager.getJavaModelManager();
//			ZipFile zip = null;
//			try {
//				zip = manager.getZipFile(jarPackageFragmentRoot.getPath());
//				for (Enumeration entries = zip.entries(); entries.hasMoreElements(); ) {
//					ZipEntry entry = (ZipEntry) entries.nextElement();
//					String entryName = entry.getName();
//					if (!entry.isDirectory()) {
//						int index = entryName.indexOf('/');
//						if (index != -1 && Util.isClassFileName(entryName)) {
//							String firstLevelPackageName = entryName.substring(0, index);
//							if (!firstLevelPackageNames.contains(firstLevelPackageName)) {
//								if (sourceLevel == null) {
//									sourceLevel = project.getOption(JavaScriptCore.COMPILER_SOURCE, true);
//									complianceLevel = project.getOption(JavaScriptCore.COMPILER_COMPLIANCE, true);
//								}
//								IStatus status = JavaScriptConventions.validatePackageName(firstLevelPackageName, sourceLevel, complianceLevel);
//								if (status.isOK() || status.getSeverity() == IStatus.WARNING) {
//									firstLevelPackageNames.add(firstLevelPackageName);
//								}
//							}
//						} else if (Util.isClassFileName(entryName)) {
//							containsADefaultPackage = true;
//						}
//					}
//				}
//			} catch (CoreException e) {
//				// ignore
//			} finally {
//				manager.closeZipFile(zip); // handle null case
//			}
//		} else
		{
			Object target = JavaModel.getTarget(ResourcesPlugin.getWorkspace().getRoot(), root.getPath(), true);
			if (target instanceof IResource) {
				IResource resource = (IResource) target;
				if (resource instanceof IContainer) {
					try {
						IResource[] members = ((IContainer) resource).members();
						for (int i = 0, max = members.length; i < max; i++) {
							IResource member = members[i];
							if (member.getType() == IResource.FOLDER) {
								firstLevelPackageNames.add(member.getName());
							} else if (Util.isClassFileName(member.getName())) {
								containsADefaultPackage = true;
							}
						}
					} catch (CoreException e) {
						// ignore
					}
				}
			} else if (target instanceof File) {
				File file = (File)target;
				if (file.isDirectory()) {
					File[] files = file.listFiles();
					for (int i = 0, max = files.length; i < max; i++) {
						File currentFile = files[i];
						if (currentFile.isDirectory()) {
							firstLevelPackageNames.add(currentFile.getName());
						} else if (Util.isClassFileName(currentFile.getName())) {
							containsADefaultPackage = true;
						}
					}
				}
			}
		}

		if (Util.isArchiveFileName(this.sourcePath.lastSegment())) {
			JavaModelManager manager = JavaModelManager.getJavaModelManager();
			ZipFile zip = null;
			try {
				zip = manager.getZipFile(this.sourcePath);
				for (Enumeration entries = zip.entries(); entries.hasMoreElements(); ) {
					ZipEntry entry = (ZipEntry) entries.nextElement();
					String entryName;
					if (!entry.isDirectory() && org.eclipse.wst.jsdt.internal.core.util.Util.isJavaLikeFileName(entryName = entry.getName())) {
						IPath path = new Path(entryName);
						int segmentCount = path.segmentCount();
						if (segmentCount > 1) {
							for (int i = 0, max = path.segmentCount() - 1; i < max; i++) {
								if (firstLevelPackageNames.contains(path.segment(i))) {
									tempRoots.add(path.uptoSegment(i));
									// don't break here as this path could contain other first level package names (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=74014)
								}
								if (i == max - 1 && containsADefaultPackage) {
									tempRoots.add(path.uptoSegment(max));
								}
							}
						} else if (containsADefaultPackage) {
							tempRoots.add(new Path("")); //$NON-NLS-1$
						}
					}
				}
			} catch (CoreException e) {
				// ignore
			} finally {
				manager.closeZipFile(zip); // handle null case
			}
		} else {
			Object target = JavaModel.getTarget(ResourcesPlugin.getWorkspace().getRoot(), this.sourcePath, true);
			if (target instanceof IResource) {
				if (target instanceof IContainer) {
					computeRootPath((IContainer)target, firstLevelPackageNames, containsADefaultPackage, tempRoots);
				}
			} else if (target instanceof File) {
				File file = (File)target;
				if (file.isDirectory()) {
					computeRootPath(file, firstLevelPackageNames, containsADefaultPackage, tempRoots);
				}
			}
		}
		int size = tempRoots.size();
		if (this.rootPaths != null) {
			for (Iterator iterator = this.rootPaths.iterator(); iterator.hasNext(); ) {
				tempRoots.add(new Path((String) iterator.next()));
			}
			this.rootPaths.clear();
		} else {
			this.rootPaths = new ArrayList(size);
		}
		size = tempRoots.size();
		if (size > 0) {
			ArrayList sortedRoots = new ArrayList(tempRoots);
			if (size > 1) {
				Collections.sort(sortedRoots, new Comparator() {
					public int compare(Object o1, Object o2) {
						IPath path1 = (IPath) o1;
						IPath path2 = (IPath) o2;
						return path1.segmentCount() - path2.segmentCount();
					}
				});
			}
			for (Iterator iter = sortedRoots.iterator(); iter.hasNext();) {
				IPath path = (IPath) iter.next();
				this.rootPaths.add(path.toString());
			}
		}
		this.areRootPathsComputed = true;
		if (VERBOSE) {
			System.out.println("Spent " + (System.currentTimeMillis() - time) + "ms"); //$NON-NLS-1$ //$NON-NLS-2$
			System.out.println("Found " + size + " root paths");	//$NON-NLS-1$ //$NON-NLS-2$
			int i = 0;
			for (Iterator iterator = this.rootPaths.iterator(); iterator.hasNext();) {
				System.out.println("root[" + i + "]=" + ((String) iterator.next()));//$NON-NLS-1$ //$NON-NLS-2$
				i++;
			}
		}
	}

	private void computeRootPath(File directory, HashSet firstLevelPackageNames, boolean hasDefaultPackage, Set set) {
		File[] files = directory.listFiles();
		boolean hasSubDirectories = false;
		loop: for (int i = 0, max = files.length; i < max; i++) {
			File file = files[i];
			if (file.isDirectory()) {
				hasSubDirectories = true;
				if (firstLevelPackageNames.contains(file.getName())) {
					IPath fullPath = new Path(file.getParentFile().getPath());
					IPath rootPathEntry = fullPath.removeFirstSegments(this.sourcePath.segmentCount()).setDevice(null);
					set.add(rootPathEntry);
					break loop;
				} else {
					computeRootPath(file, firstLevelPackageNames, hasDefaultPackage, set);
				}
			} else if (i == max - 1 && !hasSubDirectories && hasDefaultPackage) {
				File parentDir = file.getParentFile();
				if (parentDir.list(FILENAME_FILTER).length != 0) {
					IPath fullPath = new Path(parentDir.getPath());
					IPath rootPathEntry = fullPath.removeFirstSegments(this.sourcePath.segmentCount()).setDevice(null);
					set.add(rootPathEntry);
				}
			}
		}
	}

	private void computeRootPath(IContainer container, HashSet firstLevelPackageNames, boolean hasDefaultPackage, Set set) {
		try {
			IResource[] resources = container.members();
			boolean hasSubDirectories = false;
			loop: for (int i = 0, max = resources.length; i < max; i++) {
				IResource resource = resources[i];
				if (resource.getType() == IResource.FOLDER) {
					hasSubDirectories = true;
					if (firstLevelPackageNames.contains(resource.getName())) {
						IPath fullPath = container.getFullPath();
						IPath rootPathEntry = fullPath.removeFirstSegments(this.sourcePath.segmentCount()).setDevice(null);
						set.add(rootPathEntry);
						break loop;
					} else {
						computeRootPath((IFolder) resource, firstLevelPackageNames, hasDefaultPackage, set);
					}
				}
				if (i == max - 1 && !hasSubDirectories && hasDefaultPackage) {
					// check if one member is a .js file
					boolean hasJavaSourceFile = false;
					for (int j = 0; j < max; j++) {
						if (org.eclipse.wst.jsdt.internal.core.util.Util.isJavaLikeFileName(resources[i].getName())) {
							hasJavaSourceFile = true;
							break;
						}
					}
					if (hasJavaSourceFile) {
						IPath fullPath = container.getFullPath();
						IPath rootPathEntry = fullPath.removeFirstSegments(this.sourcePath.segmentCount()).setDevice(null);
						set.add(rootPathEntry);
					}
				}
			}
		} catch (CoreException e) {
			// ignore
		}
	}

	/**
	 * @see ISourceElementRequestor
	 */
	public void enterType(TypeInfo typeInfo) {

		this.typeDepth++;
		if (this.typeDepth == this.types.length) { // need to grow
			System.arraycopy(
				this.types,
				0,
				this.types = new IType[this.typeDepth * 2],
				0,
				this.typeDepth);
			System.arraycopy(
				this.typeNameRanges,
				0,
				this.typeNameRanges = new SourceRange[this.typeDepth * 2],
				0,
				this.typeDepth);
			System.arraycopy(
				this.typeDeclarationStarts,
				0,
				this.typeDeclarationStarts = new int[this.typeDepth * 2],
				0,
				this.typeDepth);
			System.arraycopy(
				this.memberName,
				0,
				this.memberName = new String[this.typeDepth * 2],
				0,
				this.typeDepth);
			System.arraycopy(
				this.memberDeclarationStart,
				0,
				this.memberDeclarationStart = new int[this.typeDepth * 2],
				0,
				this.typeDepth);
			System.arraycopy(
				this.memberNameRange,
				0,
				this.memberNameRange = new SourceRange[this.typeDepth * 2],
				0,
				this.typeDepth);
			System.arraycopy(
				this.methodParameterTypes,
				0,
				this.methodParameterTypes = new char[this.typeDepth * 2][][],
				0,
				this.typeDepth);
			System.arraycopy(
				this.methodParameterNames,
				0,
				this.methodParameterNames = new char[this.typeDepth * 2][][],
				0,
				this.typeDepth);
			System.arraycopy(
				this.typeModifiers,
				0,
				this.typeModifiers = new int[this.typeDepth * 2],
				0,
				this.typeDepth);
		}
		if (typeInfo.name.length == 0) {
			this.anonymousCounter++;
			if (this.anonymousCounter == this.anonymousClassName) {
				this.types[typeDepth] = this.getType(this.binaryType.getElementName());
			} else {
				this.types[typeDepth] = this.getType(new String(typeInfo.name));
			}
		} else {
			this.types[typeDepth] = this.getType(new String(typeInfo.name));
		}
		this.typeNameRanges[typeDepth] =
			new SourceRange(typeInfo.nameSourceStart, typeInfo.nameSourceEnd - typeInfo.nameSourceStart + 1);
		this.typeDeclarationStarts[typeDepth] = typeInfo.declarationStart;

		IType currentType = this.types[typeDepth];

		// type modifiers
		this.typeModifiers[typeDepth] = typeInfo.modifiers;

		// categories
		addCategories(currentType, typeInfo.categories);
	}

	/**
	 * @see ISourceElementRequestor
	 */
	public void enterCompilationUnit() {
		// do nothing
	}

	/**
	 * @see ISourceElementRequestor
	 */
	public void enterConstructor(MethodInfo methodInfo) {
		enterAbstractMethod(methodInfo);
	}

	/**
	 * @see ISourceElementRequestor
	 */
	public void enterField(FieldInfo fieldInfo) {
		if (typeDepth >= 0) {
			this.memberDeclarationStart[typeDepth] = fieldInfo.declarationStart;
			this.memberNameRange[typeDepth] =
				new SourceRange(fieldInfo.nameSourceStart, fieldInfo.nameSourceEnd - fieldInfo.nameSourceStart + 1);
			String fieldName = new String(fieldInfo.name);
			this.memberName[typeDepth] = fieldName;

			// categories
			IType currentType = this.types[typeDepth];
			IField field = currentType.getField(fieldName);
			addCategories(field, fieldInfo.categories);
		}
	}

	/**
	 * @see ISourceElementRequestor
	 */
	public void enterInitializer(
		int declarationSourceStart,
		int modifiers) {
		//do nothing
	}

	/**
	 * @see ISourceElementRequestor
	 */
	public void enterMethod(MethodInfo methodInfo) {
		enterAbstractMethod(methodInfo);
	}
	private void enterAbstractMethod(MethodInfo methodInfo) {
		if (typeDepth >= 0) {
			this.memberName[typeDepth] = new String(methodInfo.name);
			this.memberNameRange[typeDepth] =
				new SourceRange(methodInfo.nameSourceStart, methodInfo.nameSourceEnd - methodInfo.nameSourceStart + 1);
			this.memberDeclarationStart[typeDepth] = methodInfo.declarationStart;
			IType currentType = this.types[typeDepth];
			int currenTypeModifiers = this.typeModifiers[typeDepth];
			char[][] parameterTypes = methodInfo.parameterTypes;
			if (parameterTypes != null && methodInfo.isConstructor && currentType.getDeclaringType() != null && !Flags.isStatic(currenTypeModifiers)) {
				IType declaringType = currentType.getDeclaringType();
				String declaringTypeName = declaringType.getElementName();
				if (declaringTypeName.length() == 0) {
					IClassFile classFile = declaringType.getClassFile();
					int length = parameterTypes.length;
					char[][] newParameterTypes = new char[length+1][];
					declaringTypeName = classFile.getElementName();
					declaringTypeName = declaringTypeName.substring(0, declaringTypeName.indexOf('.'));
					newParameterTypes[0] = declaringTypeName.toCharArray();
					System.arraycopy(parameterTypes, 0, newParameterTypes, 1, length);
					this.methodParameterTypes[typeDepth] = newParameterTypes;
				} else {
					int length = parameterTypes.length;
					char[][] newParameterTypes = new char[length+1][];
					newParameterTypes[0] = declaringTypeName.toCharArray();
					System.arraycopy(parameterTypes, 0, newParameterTypes, 1, length);
					this.methodParameterTypes[typeDepth] = newParameterTypes;
				}
			} else {
				this.methodParameterTypes[typeDepth] = parameterTypes;
			}
			this.methodParameterNames[typeDepth] = methodInfo.parameterNames;

			IFunction method = currentType.getFunction(
					this.memberName[typeDepth],
					convertTypeNamesToSigs(this.methodParameterTypes[typeDepth]));

			// categories
			addCategories(method, methodInfo.categories);
		}
	}

	/**
	 * @see ISourceElementRequestor
	 */
	public void exitType(int declarationEnd) {
		if (typeDepth >= 0) {
			IType currentType = this.types[typeDepth];
			setSourceRange(
				currentType,
				new SourceRange(
					this.typeDeclarationStarts[typeDepth],
					declarationEnd - this.typeDeclarationStarts[typeDepth] + 1),
				this.typeNameRanges[typeDepth]);
			this.typeDepth--;
		}
	}

	/**
	 * @see ISourceElementRequestor
	 */
	public void exitCompilationUnit(int declarationEnd) {
		//do nothing
	}

	/**
	 * @see ISourceElementRequestor
	 */
	public void exitConstructor(int declarationEnd) {
		exitAbstractMethod(declarationEnd);
	}

	/**
	 * @see ISourceElementRequestor
	 */
	public void exitField(int initializationStart, int declarationEnd, int declarationSourceEnd) {
		if (typeDepth >= 0) {
			IType currentType = this.types[typeDepth];
			setSourceRange(
				currentType.getField(this.memberName[typeDepth]),
				new SourceRange(
					this.memberDeclarationStart[typeDepth],
					declarationEnd - this.memberDeclarationStart[typeDepth] + 1),
				this.memberNameRange[typeDepth]);
		}
	}

	/**
	 * @see ISourceElementRequestor
	 */
	public void exitInitializer(int declarationEnd) {
		// implements abstract method
	}

	/**
	 * @see ISourceElementRequestor
	 */
	public void exitMethod(int declarationEnd, int defaultValueStart, int defaultValueEnd) {
		exitAbstractMethod(declarationEnd);
	}
	private void exitAbstractMethod(int declarationEnd) {
		if (typeDepth >= 0) {
			IType currentType = this.types[typeDepth];
			SourceRange sourceRange =
				new SourceRange(
					this.memberDeclarationStart[typeDepth],
					declarationEnd - this.memberDeclarationStart[typeDepth] + 1);
			IFunction method = currentType.getFunction(
					this.memberName[typeDepth],
					convertTypeNamesToSigs(this.methodParameterTypes[typeDepth]));
			setSourceRange(
				method,
				sourceRange,
				this.memberNameRange[typeDepth]);
			setMethodParameterNames(
				method,
				this.methodParameterNames[typeDepth]);
		}
	}

	/**
	 * Locates and returns source code for the given (binary) type, in this
	 * SourceMapper's ZIP file, or returns <code>null</code> if source
	 * code cannot be found.
	 */
	public char[] findSource(IType type, IBinaryType info) {
		if (!type.isBinary()) {
			return null;
		}
		String simpleSourceFileName = ((BinaryType) type).getSourceFileName(info);
		if (simpleSourceFileName == null) {
			return null;
		}
		return findSource(type, simpleSourceFileName);
	}

	/**
	 * Locates and returns source code for the given (binary) type, in this
	 * SourceMapper's ZIP file, or returns <code>null</code> if source
	 * code cannot be found.
	 * The given simpleSourceFileName is the .js file name (without the enclosing
	 * folder) used to create the given type (e.g. "A.js" for x/y/A$Inner.class)
	 */
	public char[] findSource(IType type, String simpleSourceFileName) {
		long time = 0;
		if (VERBOSE) {
			time = System.currentTimeMillis();
		}
		PackageFragment pkgFrag = (PackageFragment) type.getPackageFragment();
		String name = org.eclipse.wst.jsdt.internal.core.util.Util.concatWith(pkgFrag.names, simpleSourceFileName, '/');

		char[] source = null;

		if (this.rootPath != null) {
			source = getSourceForRootPath(this.rootPath, name);
		}

		if (source == null) {
			computeAllRootPaths(type);
			if (this.rootPaths != null) {
				loop: for (Iterator iterator = this.rootPaths.iterator(); iterator.hasNext(); ) {
					String currentRootPath = (String) iterator.next();
					if (!currentRootPath.equals(this.rootPath)) {
						source = getSourceForRootPath(currentRootPath, name);
						if (source != null) {
							// remember right root path
							this.rootPath = currentRootPath;
							break loop;
						}
					}
				}
			}
		}
		if (VERBOSE) {
			System.out.println("spent " + (System.currentTimeMillis() - time) + "ms for " + type.getElementName()); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return source;
	}

	private char[] getSourceForRootPath(String currentRootPath, String name) {
		String newFullName;
		if (!currentRootPath.equals(IPackageFragmentRoot.DEFAULT_PACKAGEROOT_PATH)) {
			if (currentRootPath.endsWith("/")) { //$NON-NLS-1$
				newFullName = currentRootPath + name;
			} else {
				newFullName = currentRootPath + '/' + name;
			}
		} else {
			newFullName = name;
		}
		return this.findSource(newFullName);
	}

	public char[] findSource(String fullName) {
		char[] source = null;
		if (Util.isArchiveFileName(this.sourcePath.lastSegment())) {
			// try to get the entry
			ZipEntry entry = null;
			ZipFile zip = null;
			JavaModelManager manager = JavaModelManager.getJavaModelManager();
			try {
				zip = manager.getZipFile(this.sourcePath);
				entry = zip.getEntry(fullName);
				if (entry != null) {
					// now read the source code
					source = readSource(entry, zip);
				}
			} catch (CoreException e) {
				return null;
			} finally {
				manager.closeZipFile(zip); // handle null case
			}
		} else {
			Object target = JavaModel.getTarget(ResourcesPlugin.getWorkspace().getRoot(), this.sourcePath, true);
			if (target instanceof IResource) {
				if (target instanceof IContainer) {
					IResource res = ((IContainer)target).findMember(fullName);
					if (res instanceof IFile) {
						try {
							source = org.eclipse.wst.jsdt.internal.core.util.Util.getResourceContentsAsCharArray((IFile)res);
						} catch (JavaScriptModelException e) {
							// ignore
						}
					}
				}
			} else if (target instanceof File) {
				File file = (File)target;
				if (file.isDirectory()) {
					File sourceFile = new File(file, fullName);
					if (sourceFile.isFile()) {
						try {
							source = Util.getFileCharContent(sourceFile, this.encoding);
						} catch (IOException e) {
							// ignore
						}
					}
				}
			}
		}
		return source;
	}



	/**
	 * Returns the SourceRange for the name of the given element, or
	 * {-1, -1} if no source range is known for the name of the element.
	 */
	public SourceRange getNameRange(IJavaScriptElement element) {
		switch(element.getElementType()) {
			case IJavaScriptElement.METHOD :
				if (((IMember) element).isBinary()) {
					IJavaScriptElement[] el = getUnqualifiedMethodHandle((IFunction) element, false);
					if(el[1] != null && this.sourceRanges.get(el[0]) == null) {
						element = getUnqualifiedMethodHandle((IFunction) element, true)[0];
					} else {
						element = el[0];
					}
				}
				break;
		}
		SourceRange[] ranges = (SourceRange[]) this.sourceRanges.get(element);
		if (ranges == null) {
			return UNKNOWN_RANGE;
		} else {
			return ranges[1];
		}
	}

	/**
	 * Returns parameters names for the given method, or
	 * null if no parameter names are known for the method.
	 */
	public char[][] getMethodParameterNames(IFunction method) {
		if (method.isBinary()) {
			IJavaScriptElement[] el = getUnqualifiedMethodHandle(method, false);
			if(el[1] != null && this.parameterNames.get(el[0]) == null) {
				method = (IFunction) getUnqualifiedMethodHandle(method, true)[0];
			} else {
				method = (IFunction) el[0];
			}
		}
		char[][] parameters = (char[][]) this.parameterNames.get(method);
		if (parameters == null) {
			return null;
		} else {
			return parameters;
		}
	}

	/**
	 * Returns the <code>SourceRange</code> for the given element, or
	 * {-1, -1} if no source range is known for the element.
	 */
	public SourceRange getSourceRange(IJavaScriptElement element) {
		if (!this.areRootPathsComputed && element instanceof ISourceReference)
			try {
				return (SourceRange)((ISourceReference)element).getSourceRange();
			} catch (JavaScriptModelException e) {
				org.eclipse.wst.jsdt.internal.core.util.Util.log(e, "error getting source range"); //$NON-NLS-1$
				return UNKNOWN_RANGE;
			}
		switch(element.getElementType()) {
			case IJavaScriptElement.METHOD :
				if (((IMember) element).isBinary()) {
					IJavaScriptElement[] el = getUnqualifiedMethodHandle((IFunction) element, false);
					if(el[1] != null && this.sourceRanges.get(el[0]) == null) {
						element = getUnqualifiedMethodHandle((IFunction) element, true)[0];
					} else {
						element = el[0];
					}
				}
				break;
		}
		SourceRange[] ranges = (SourceRange[]) this.sourceRanges.get(element);
		if (ranges == null) {
			return UNKNOWN_RANGE;
		} else {
			return ranges[0];
		}
	}

	/**
	 * Returns the type with the given <code>typeName</code>.  Returns inner classes
	 * as well.
	 */
	protected IType getType(String typeName) {
		if (typeName.length() == 0) {
			IJavaScriptElement classFile = this.binaryType.getParent();
			String classFileName = classFile.getElementName();
			StringBuffer newClassFileName = new StringBuffer();
			int lastDollar = classFileName.lastIndexOf('$');
			for (int i = 0; i <= lastDollar; i++)
				newClassFileName.append(classFileName.charAt(i));
			newClassFileName.append(Integer.toString(this.anonymousCounter));
			PackageFragment pkg = (PackageFragment) classFile.getParent();
			return new BinaryType(new ClassFile(pkg, newClassFileName.toString()), typeName);
		} else if (this.binaryType.getElementName().equals(typeName))
			return this.binaryType;
		else
			return this.binaryType.getType(typeName);
	}

	/**
	 * Creates a handle that has parameter types that are not
	 * fully qualified so that the correct source is found.
	 */
	protected IJavaScriptElement[] getUnqualifiedMethodHandle(IFunction method, boolean noDollar) {
		boolean hasDollar = false;
		String[] qualifiedParameterTypes = method.getParameterTypes();
		String[] unqualifiedParameterTypes = new String[qualifiedParameterTypes.length];
		for (int i = 0; i < qualifiedParameterTypes.length; i++) {
			StringBuffer unqualifiedTypeSig = new StringBuffer();
			getUnqualifiedTypeSignature(qualifiedParameterTypes[i], 0/*start*/, qualifiedParameterTypes[i].length(), unqualifiedTypeSig, noDollar);
			unqualifiedParameterTypes[i] = unqualifiedTypeSig.toString();
			hasDollar |= unqualifiedParameterTypes[i].lastIndexOf('$') != -1;
		}

		IJavaScriptElement[] result = new IJavaScriptElement[2];
		result[0] = ((IType) method.getParent()).getFunction(
			method.getElementName(),
			unqualifiedParameterTypes);
		if(hasDollar) {
			result[1] = result[0];
		}
		return result;
	}

	private int getUnqualifiedTypeSignature(String qualifiedTypeSig, int start, int length, StringBuffer unqualifiedTypeSig, boolean noDollar) {
		char firstChar = qualifiedTypeSig.charAt(start);
		int end = start + 1;
		boolean sigStart = false;
		firstPass: for (int i = start; i < length; i++) {
			char current = qualifiedTypeSig.charAt(i);
			switch (current) {
				case Signature.C_ARRAY :
					unqualifiedTypeSig.append(current);
					start = i + 1;
					end = start + 1;
					firstChar = qualifiedTypeSig.charAt(start);
					break;
				case Signature.C_RESOLVED :
				case Signature.C_UNRESOLVED :
					if (!sigStart) {
						start = ++i;
						sigStart = true;
					}
					break;
				case Signature.C_NAME_END:
					end = i;
					break firstPass;
				case Signature.C_DOT:
					start = ++i;
					break;
			}
		}
		switch (firstChar) {
			case Signature.C_RESOLVED :
			case Signature.C_UNRESOLVED :
				unqualifiedTypeSig.append(Signature.C_UNRESOLVED);
				if (noDollar) {
					int lastDollar = qualifiedTypeSig.lastIndexOf('$', end);
					if (lastDollar > start)
						start = lastDollar + 1;
				}
				for (int i = start; i < length; i++) {
					char current = qualifiedTypeSig.charAt(i);
					switch (current) {
						case Signature.C_NAME_END:
							unqualifiedTypeSig.append(current);
							return i + 1;
						default:
							unqualifiedTypeSig.append(current);
							break;
					}
				}
				return length;
			default :
				// primitive type or wildcard
				unqualifiedTypeSig.append(qualifiedTypeSig.substring(start, end));
				return end;
		}
	}

	/**
	 * Maps the given source code to the given binary type and its children.
	 */
	public void mapSource(IType type, char[] contents, IBinaryType info) {
		this.mapSource(type, contents, info, null);
	}

	/**
	 * Maps the given source code to the given binary type and its children.
	 * If a non-null java element is passed, finds the name range for the
	 * given java element without storing it.
	 */
	public synchronized ISourceRange mapSource(
		IType type,
		char[] contents,
		IBinaryType info,
		IJavaScriptElement elementToFind) {

		this.binaryType = (BinaryType) type;

		// check whether it is already mapped
		if (this.sourceRanges.get(type) != null) return (elementToFind != null) ? getNameRange(elementToFind) : null;

		this.importsTable.remove(this.binaryType);
		this.importsCounterTable.remove(this.binaryType);
		this.searchedElement = elementToFind;
		this.types = new IType[1];
		this.typeDeclarationStarts = new int[1];
		this.typeNameRanges = new SourceRange[1];
		this.typeModifiers = new int[1];
		this.typeDepth = -1;
		this.memberDeclarationStart = new int[1];
		this.memberName = new String[1];
		this.memberNameRange = new SourceRange[1];
		this.methodParameterTypes = new char[1][][];
		this.methodParameterNames = new char[1][][];
		this.anonymousCounter = 0;

		HashMap oldSourceRanges = (HashMap) this.sourceRanges.clone();
		try {
			IProblemFactory factory = new DefaultProblemFactory();
			SourceElementParser parser = null;
			this.anonymousClassName = 0;
			if (info == null) {
				try {
					info = (IBinaryType) this.binaryType.getElementInfo();
				} catch(JavaScriptModelException e) {
					return null;
				}
			}
			boolean isAnonymousClass = info.isAnonymous();
			char[] fullName = info.getName();
			if (isAnonymousClass) {
				String eltName = this.binaryType.getParent().getElementName();
				eltName = eltName.substring(eltName.lastIndexOf('$') + 1, eltName.length());
				try {
					this.anonymousClassName = Integer.parseInt(eltName);
				} catch(NumberFormatException e) {
					// ignore
				}
			}
			boolean doFullParse = hasToRetrieveSourceRangesForLocalClass(fullName);
			parser = new SourceElementParser(this, factory, new CompilerOptions(this.options), doFullParse, true/*optimize string literals*/);
			parser.javadocParser.checkDocComment = false; // disable javadoc parsing
			IJavaScriptElement javaElement = this.binaryType.getJavaScriptUnit();
			if (javaElement == null) javaElement = this.binaryType.getParent();
			parser.parseCompilationUnit(
				new BasicCompilationUnit(contents, null, this.binaryType.sourceFileName(info), javaElement),
				doFullParse);
			if (elementToFind != null) {
				ISourceRange range = this.getNameRange(elementToFind);
				return range;
			} else {
				return null;
			}
		} finally {
			if (elementToFind != null) {
				this.sourceRanges = oldSourceRanges;
			}
			this.binaryType = null;
			this.searchedElement = null;
			this.types = null;
			this.typeDeclarationStarts = null;
			this.typeNameRanges = null;
			this.typeDepth = -1;
		}
	}
	private char[] readSource(ZipEntry entry, ZipFile zip) {
		try {
			byte[] bytes = Util.getZipEntryByteContent(entry, zip);
			if (bytes != null) {
				return Util.bytesToChar(bytes, this.encoding);
			}
		} catch (IOException e) {
			// ignore
		}
		return null;
	}

	/**
	 * Sets the mapping for this method to its parameter names.
	 *
	 * @see #parameterNames
	 */
	protected void setMethodParameterNames(
		IFunction method,
		char[][] parameterNames) {
		if (parameterNames == null) {
			parameterNames = CharOperation.NO_CHAR_CHAR;
		}
		this.parameterNames.put(method, parameterNames);
	}

	/**
	 * Sets the mapping for this element to its source ranges for its source range
	 * and name range.
	 *
	 * @see #sourceRanges
	 */
	protected void setSourceRange(
		IJavaScriptElement element,
		SourceRange sourceRange,
		SourceRange nameRange) {
		this.sourceRanges.put(element, new SourceRange[] { sourceRange, nameRange });
	}

	/**
	 * Return a char[][] array containing the imports of the attached source for the binary type
	 */
	public char[][] getImports(BinaryType type) {
		char[][] imports = (char[][]) this.importsTable.get(type);
		if (imports != null) {
			int importsCounter = ((Integer) this.importsCounterTable.get(type)).intValue();
			if (imports.length != importsCounter) {
				System.arraycopy(
					imports,
					0,
					(imports = new char[importsCounter][]),
					0,
					importsCounter);
			}
			this.importsTable.put(type, imports);
		}
		return imports;
	}

	private boolean hasToRetrieveSourceRangesForLocalClass(char[] eltName) {
		/*
		 * A$1$B$2 : true
		 * A$B$B$2 : true
		 * A$C$B$D : false
		 * A$F$B$D$1$F : true
		 * A$F$B$D$1F : true
		 * A$1 : true
		 * A$B : false
		 */
		if (eltName == null) return false;
		int length = eltName.length;
		int dollarIndex = CharOperation.indexOf('$', eltName, 0);
		while (dollarIndex != -1) {
			int nameStart = dollarIndex+1;
			if (nameStart == length) return false;
			if (Character.isDigit(eltName[nameStart]))
				return true;
			dollarIndex = CharOperation.indexOf('$', eltName, nameStart);
		}
		return false;
	}

}
