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

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.AssertionFailedException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.wst.jsdt.core.IIncludePathEntry;
import org.eclipse.wst.jsdt.core.IJavaScriptElement;
import org.eclipse.wst.jsdt.core.IJavaScriptModelMarker;
import org.eclipse.wst.jsdt.core.IJavaScriptModelStatus;
import org.eclipse.wst.jsdt.core.IJavaScriptModelStatusConstants;
import org.eclipse.wst.jsdt.core.IJavaScriptProject;
import org.eclipse.wst.jsdt.core.IJavaScriptUnit;
import org.eclipse.wst.jsdt.core.IJsGlobalScopeContainer;
import org.eclipse.wst.jsdt.core.IPackageFragment;
import org.eclipse.wst.jsdt.core.IPackageFragmentRoot;
import org.eclipse.wst.jsdt.core.IRegion;
import org.eclipse.wst.jsdt.core.IType;
import org.eclipse.wst.jsdt.core.ITypeHierarchy;
import org.eclipse.wst.jsdt.core.ITypeRoot;
import org.eclipse.wst.jsdt.core.JavaScriptCore;
import org.eclipse.wst.jsdt.core.JavaScriptModelException;
import org.eclipse.wst.jsdt.core.LibrarySuperType;
import org.eclipse.wst.jsdt.core.WorkingCopyOwner;
import org.eclipse.wst.jsdt.core.compiler.CategorizedProblem;
import org.eclipse.wst.jsdt.core.compiler.CharOperation;
import org.eclipse.wst.jsdt.core.eval.IEvaluationContext;
import org.eclipse.wst.jsdt.internal.compiler.util.ObjectVector;
import org.eclipse.wst.jsdt.internal.compiler.util.SuffixConstants;
import org.eclipse.wst.jsdt.internal.core.JavaModelManager.PerProjectInfo;
import org.eclipse.wst.jsdt.internal.core.JavaProjectElementInfo.LookupCache;
import org.eclipse.wst.jsdt.internal.core.builder.JavaBuilder;
import org.eclipse.wst.jsdt.internal.core.util.HandleFactory;
import org.eclipse.wst.jsdt.internal.core.util.MementoTokenizer;
import org.eclipse.wst.jsdt.internal.core.util.Messages;
import org.eclipse.wst.jsdt.internal.core.util.Util;
import org.eclipse.wst.jsdt.launching.JavaRuntime;
import org.osgi.service.prefs.BackingStoreException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Handle for a Java Project.
 *
 * <p>A Java Project internally maintains a devpath that corresponds
 * to the project's classpath. The classpath may include source folders
 * from the current project; jars in the current project, other projects,
 * and the local file system; and binary folders (output location) of other
 * projects. The Java Model presents source elements corresponding to output
 * .class files in other projects, and thus uses the devpath rather than
 * the classpath (which is really a compilation path). The devpath mimics
 * the classpath, except has source folder entries in place of output
 * locations in external projects.
 *
 * <p>Each JavaProject has a NameLookup facility that locates elements
 * on by name, based on the devpath.
 *
 * @see IJavaScriptProject
 */
public class JavaProject
	extends Openable
	implements IJavaScriptProject, IProjectNature, SuffixConstants {

	/**
	 * Name of file containing project classpath
	 */
	//public static final String CLASSPATH_FILENAME = ".jsdtscope";  //$NON-NLS-1$

	/**
	 * Value of the project's raw classpath if the .classpath file contains invalid entries.
	 */
	public static final IIncludePathEntry[] INVALID_CLASSPATH = new IIncludePathEntry[0];

	/* default CLASSPATH for projects without a classpath set.
	 * 
	 */
	
	/**
	 * Whether the underlying file system is case sensitive.
	 */
	protected static final boolean IS_CASE_SENSITIVE = !new File("Temp").equals(new File("temp")); //$NON-NLS-1$ //$NON-NLS-2$

	/**
	 * An empty array of strings indicating that a project doesn't have any prerequesite projects.
	 */
	protected static final String[] NO_PREREQUISITES = CharOperation.NO_STRINGS;

	/**
	 * Name of file containing custom project preferences
	 * @see <a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=59258">bug 59258</a>
	 */
	private static final String PREF_FILENAME = ".jprefs";  //$NON-NLS-1$

	/**
	 * Name of directory containing preferences file
	 */
	public static final String DEFAULT_PREFERENCES_DIRNAME = ".settings"; //$NON-NLS-1$

	/**
	 * Extension for file containing custom project preferences
	 */
	public static final String JAVA_CORE_PREFS_FILE = JavaScriptCore.PLUGIN_ID+".prefs"; //$NON-NLS-1$

	/*
	 * Value of project's resolved classpath while it is being resolved
	 */
	private static final IIncludePathEntry[] RESOLUTION_IN_PROGRESS = new IIncludePathEntry[0];

	public static final String SHARED_PROPERTIES_DIRECTORY = ".settings"; //$NON-NLS-1$
	
	/**
	 * Name of file containing project classpath
	 * appended settings directory per bug: 210831
	 */
	
	public static final String CLASSPATH_FILENAME = ".jsdtscope";  //$NON-NLS-1$
	/**
	 * The platform project this <code>IJavaScriptProject</code> is based on
	 */
	protected IProject project;

	/**
	 * Constructor needed for <code>IProject.getNature()</code> and <code>IProject.addNature()</code>.
	 *
	 * @see #setProject(IProject)
	 */
	public JavaProject() {
		super(null);
	}

	public IFile getJSDTScopeFile() {
		return getJSDTScopeFile(false);
	}
		
	public IFile getJSDTScopeFile(boolean forceCreate) {

		// Return the projects .jsdtscope file
		IFolder rscPath = this.project.getFolder(JavaProject.SHARED_PROPERTIES_DIRECTORY);
		if(!rscPath.exists()&&forceCreate)
			try {
				rscPath.create(true, true, new NullProgressMonitor());
			}
			catch (CoreException e) {}

		
		return rscPath.getFile(CLASSPATH_FILENAME);	
		
		
		
	}
	

	public JavaProject(IProject project, JavaElement parent) {
		super(parent);
		this.project = project;
	}

	public static boolean areClasspathsEqual(
			IIncludePathEntry[] firstClasspath, IIncludePathEntry[] secondClasspath,
			IPath firstOutputLocation, IPath secondOutputLocation) {
		int length = firstClasspath.length;
		if (length != secondClasspath.length) return false;
		for (int i = 0; i < length; i++) {
			if (!firstClasspath[i].equals(secondClasspath[i]))
				return false;
		}
		if (firstOutputLocation == null)
			return secondOutputLocation == null;
		return firstOutputLocation.equals(secondOutputLocation);
	}

	/**
	 * Compare current classpath with given one to see if any different.
	 * Note that the argument classpath contains its binary output.
	 * @param newClasspath IIncludePathEntry[]
	 * @param newOutputLocation IPath
	 * @param otherClasspathWithOutput IIncludePathEntry[]
	 * @return boolean
	 */
	private static boolean areClasspathsEqual(IIncludePathEntry[] newClasspath, IPath newOutputLocation, IIncludePathEntry[] otherClasspathWithOutput) {

		if (otherClasspathWithOutput == null || otherClasspathWithOutput.length == 0)
			return false;

		int length = otherClasspathWithOutput.length;
		if (length != newClasspath.length + 1)
				// output is amongst file entries (last one)
				return false;


		// compare classpath entries
		for (int i = 0; i < length - 1; i++) {
			if (!otherClasspathWithOutput[i].equals(newClasspath[i]))
				return false;
		}
		// compare binary outputs
		IIncludePathEntry output = otherClasspathWithOutput[length - 1];
		if (output.getContentKind() != ClasspathEntry.K_OUTPUT
				|| !output.getPath().equals(newOutputLocation))
			return false;
		return true;
	}

	/**
	 * Returns a canonicalized path from the given external path.
	 * Note that the return path contains the same number of segments
	 * and it contains a device only if the given path contained one.
	 * @param externalPath IPath
	 * @see java.io.File for the definition of a canonicalized path
	 * @return IPath
	 */
	public static IPath canonicalizedPath(IPath externalPath) {

		if (externalPath == null)
			return null;

//		if (JavaModelManager.VERBOSE) {
//			System.out.println("JAVA MODEL - Canonicalizing " + externalPath.toString());
//		}

		if (IS_CASE_SENSITIVE) {
//			if (JavaModelManager.VERBOSE) {
//				System.out.println("JAVA MODEL - Canonical path is original path (file system is case sensitive)");
//			}
			return new Path(new File(externalPath.toOSString()).getPath());
		}

		// if not external path, return original path
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		if (workspace == null) return externalPath; // protection during shutdown (30487)
		if (workspace.getRoot().findMember(externalPath) != null) {
//			if (JavaModelManager.VERBOSE) {
//				System.out.println("JAVA MODEL - Canonical path is original path (member of workspace)");
//			}
			return externalPath;
		}

		IPath canonicalPath = null;
		try {
			canonicalPath =
				new Path(new File(externalPath.toOSString()).getCanonicalPath());
		} catch (IOException e) {
			// default to original path
//			if (JavaModelManager.VERBOSE) {
//				System.out.println("JAVA MODEL - Canonical path is original path (IOException)");
//			}
			return externalPath;
		}

		IPath result;
		int canonicalLength = canonicalPath.segmentCount();
		if (canonicalLength == 0) {
			// the java.io.File canonicalization failed
//			if (JavaModelManager.VERBOSE) {
//				System.out.println("JAVA MODEL - Canonical path is original path (canonical path is empty)");
//			}
			return externalPath;
		} else if (externalPath.isAbsolute()) {
			result = canonicalPath;
		} else {
			// if path is relative, remove the first segments that were added by the java.io.File canonicalization
			// e.g. 'lib/classes.zip' was converted to 'd:/myfolder/lib/classes.zip'
			int externalLength = externalPath.segmentCount();
			if (canonicalLength >= externalLength) {
				result = canonicalPath.removeFirstSegments(canonicalLength - externalLength);
			} else {
//				if (JavaModelManager.VERBOSE) {
//					System.out.println("JAVA MODEL - Canonical path is original path (canonical path is " + canonicalPath.toString() + ")");
//				}
				return externalPath;
			}
		}

		// keep device only if it was specified (this is because File.getCanonicalPath() converts '/lib/classed.zip' to 'd:/lib/classes/zip')
		if (externalPath.getDevice() == null) {
			result = result.setDevice(null);
		}
//		if (JavaModelManager.VERBOSE) {
//			System.out.println("JAVA MODEL - Canonical path is " + result.toString());
//		}
		return result;
	}

	/**
	 * Returns true if the given project is accessible and it has
	 * a java nature, otherwise false.
	 * @param project IProject
	 * @return boolean
	 */
	public static boolean hasJavaNature(IProject project) {
		try {
			return project.hasNature(JavaScriptCore.NATURE_ID);
		} catch (CoreException e) {
			if (ExternalJavaProject.EXTERNAL_PROJECT_NAME.equals(project.getName()))
				return true;
			// project does not exist or is not open
		}
		return false;
	}

	public static boolean hasJSDTScope(IProject project) {

		
		IFile file = project.getFile(CLASSPATH_FILENAME);
		if (file!=null && file.exists())
			return true;

		IFolder rscPath =  project.getFolder(JavaProject.SHARED_PROPERTIES_DIRECTORY);
		if(rscPath.exists())
		{
			file = rscPath.getFile(CLASSPATH_FILENAME);
			if (file!=null && file.exists())
				return true;

		}

		return false;
	}


	
	/*
	 * Detect cycles in the classpath of the workspace's projects
	 * and create markers if necessary.
	 * @param preferredClasspaths Map
	 * @throws JavaScriptModelException
	 */
	public static void validateCycles(Map preferredClasspaths) throws JavaScriptModelException {

		//long start = System.currentTimeMillis();

		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		IProject[] rscProjects = workspaceRoot.getProjects();
		int length = rscProjects.length;
		JavaProject[] projects = new JavaProject[length];

		HashSet cycleParticipants = new HashSet();
		HashSet traversed = new HashSet();

		// compute cycle participants
		ArrayList prereqChain = new ArrayList();
		for (int i = 0; i < length; i++){
			if (hasJavaNature(rscProjects[i])) {
				JavaProject project = (projects[i] = (JavaProject)JavaScriptCore.create(rscProjects[i]));
				if (!traversed.contains(project.getPath())){
					prereqChain.clear();
					project.updateCycleParticipants(prereqChain, cycleParticipants, workspaceRoot, traversed, preferredClasspaths);
				}
			}
		}
		//System.out.println("updateAllCycleMarkers: " + (System.currentTimeMillis() - start) + " ms");

		for (int i = 0; i < length; i++){
			JavaProject project = projects[i];
			if (project != null) {
				if (cycleParticipants.contains(project.getPath())){
					IMarker cycleMarker = project.getCycleMarker();
					String circularCPOption = project.getOption(JavaScriptCore.CORE_CIRCULAR_CLASSPATH, true);
					int circularCPSeverity = JavaScriptCore.ERROR.equals(circularCPOption) ? IMarker.SEVERITY_ERROR : IMarker.SEVERITY_WARNING;
					if (cycleMarker != null) {
						// update existing cycle marker if needed
						try {
							int existingSeverity = ((Integer)cycleMarker.getAttribute(IMarker.SEVERITY)).intValue();
							if (existingSeverity != circularCPSeverity) {
								cycleMarker.setAttribute(IMarker.SEVERITY, circularCPSeverity);
							}
						} catch (CoreException e) {
							throw new JavaScriptModelException(e);
						}
					} else {
						// create new marker
						project.createClasspathProblemMarker(
							new JavaModelStatus(IJavaScriptModelStatusConstants.INCLUDEPATH_CYCLE, project));
					}
				} else {
					project.flushClasspathProblemMarkers(true, false);
				}
			}
		}
	}

	/**
	 * Adds a builder to the build spec for the given project.
	 */
	public void addToBuildSpec(String builderID) throws CoreException {

		IProjectDescription description = this.project.getDescription();
		int javaCommandIndex = getJavaCommandIndex(description.getBuildSpec(), builderID);

		if (javaCommandIndex == -1) {

			// Add a Java command to the build spec
			ICommand command = description.newCommand();
			command.setBuilderName(builderID);
			setJavaCommand(description, command);
		}
	}
	/**
	 * @see Openable
	 */
	protected boolean buildStructure(OpenableElementInfo info, IProgressMonitor pm, Map newElements, IResource underlyingResource) throws JavaScriptModelException {

		IIncludePathEntry[] resolvedClasspath =null;
		// check whether the java project can be opened
		if (!hasJavaNature((IProject) underlyingResource)) {
//			throw newNotPresentException();
//			 resolvedClasspath = getDefaultClasspath();
		}
		// cannot refresh cp markers on opening (emulate cp check on startup) since can create deadlocks (see bug 37274)
			resolvedClasspath = getResolvedClasspath();

		// compute the pkg fragment roots
		info.setChildren(computePackageFragmentRoots(resolvedClasspath, false, null /*no reverse map*/));

		// remember the timestamps of external libraries the first time they are looked up
		try {
			getPerProjectInfo().rememberExternalLibTimestamps();
		} catch (JavaScriptModelException e) {
			if (e.getStatus().getCode()!=IJavaScriptModelStatusConstants.ELEMENT_DOES_NOT_EXIST)
				throw e;
		}

		return true;
	}

	private IIncludePathEntry[] getDefaultClasspath() {
		IIncludePathEntry[] defaultClasspath = {JavaScriptCore.newContainerEntry(new Path(JavaRuntime.JRE_CONTAINER)),
						                      JavaScriptCore.newContainerEntry(new Path(JavaRuntime.BASE_BROWSER_LIB)),
						                      JavaScriptCore.newSourceEntry(getProject().getFullPath())};

		
		return defaultClasspath;
	}

	/**
	 * Computes the collection of package fragment roots (local ones) and set it on the given info.
	 * Need to check *all* package fragment roots in order to reset NameLookup
	 * @param info JavaProjectElementInfo
	 * @throws JavaScriptModelException
	 */
	public void computeChildren(JavaProjectElementInfo info) throws JavaScriptModelException {
		IIncludePathEntry[] classpath = getResolvedClasspath();
		JavaProjectElementInfo.LookupCache projectCache = info.projectCache;
		if (projectCache != null) {
			IPackageFragmentRoot[] newRoots = computePackageFragmentRoots(classpath, true, null /*no reverse map*/);
			checkIdentical: { // compare all pkg fragment root lists
				IPackageFragmentRoot[] oldRoots = projectCache.allPkgFragmentRootsCache;
				if (oldRoots.length == newRoots.length){
					for (int i = 0, length = oldRoots.length; i < length; i++){
						if (!oldRoots[i].equals(newRoots[i])){
							break checkIdentical;
						}
					}
					return; // no need to update
				}
			}
		}
		info.setNonJavaResources(null);
		info.setChildren(
			computePackageFragmentRoots(classpath, false, null /*no reverse map*/));
	}

	/**
	 * Internal computation of an expanded classpath. It will eliminate duplicates, and produce copies
	 * of exported or restricted classpath entries to avoid possible side-effects ever after.
	 */
	private void computeExpandedClasspath(
		ClasspathEntry referringEntry,
		HashSet rootIDs,
		ObjectVector accumulatedEntries) throws JavaScriptModelException {

		String projectRootId = this.rootID();
		if (rootIDs.contains(projectRootId)){
			return; // break cycles if any
		}
		rootIDs.add(projectRootId);

		IIncludePathEntry[] resolvedClasspath = getResolvedClasspath();

		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		boolean isInitialProject = referringEntry == null;
		for (int i = 0, length = resolvedClasspath.length; i < length; i++){
			ClasspathEntry entry = (ClasspathEntry) resolvedClasspath[i];
			if (isInitialProject || entry.isExported()){
				String rootID = entry.rootID();
				if (rootIDs.contains(rootID)) {
					continue;
				}
				// combine restrictions along the project chain
				ClasspathEntry combinedEntry = entry.combineWith(referringEntry);
				accumulatedEntries.add(combinedEntry);

				// recurse in project to get all its indirect exports (only consider exported entries from there on)
				if (entry.getEntryKind() == IIncludePathEntry.CPE_PROJECT) {
					IResource member = workspaceRoot.findMember(entry.getPath());
					if (member != null && member.getType() == IResource.PROJECT){ // double check if bound to project (23977)
						IProject projRsc = (IProject) member;
						if (JavaProject.hasJavaNature(projRsc)) {
							JavaProject javaProject = (JavaProject) JavaScriptCore.create(projRsc);
							javaProject.computeExpandedClasspath(
								combinedEntry,
								rootIDs,
								accumulatedEntries);
						}
					}
				} else {
					rootIDs.add(rootID);
				}
			}
		}
	}

	/**
	 * Computes the package fragment roots identified by the given entry.
	 * Only works with resolved entry
	 * @param resolvedEntry IIncludePathEntry
	 * @return IPackageFragmentRoot[]
	 */
	public IPackageFragmentRoot[] computePackageFragmentRoots(IIncludePathEntry resolvedEntry) {
		try {
			return
				computePackageFragmentRoots(
					new IIncludePathEntry[]{ resolvedEntry },
					false, // don't retrieve exported roots
					null /* no reverse map */
				);
		} catch (JavaScriptModelException e) {
			return new IPackageFragmentRoot[] {};
		}
	}

	/**
	 * Returns the package fragment roots identified by the given entry. In case it refers to
	 * a project, it will follow its classpath so as to find exported roots as well.
	 * Only works with resolved entry
	 * @param resolvedEntry IIncludePathEntry
	 * @param accumulatedRoots ObjectVector
	 * @param rootIDs HashSet
	 * @param referringEntry the CP entry (project) referring to this entry, or null if initial project
	 * @param checkExistency boolean
	 * @param retrieveExportedRoots boolean
	 * @throws JavaScriptModelException
	 */
	public void computePackageFragmentRoots(
		IIncludePathEntry resolvedEntry,
		ObjectVector accumulatedRoots,
		HashSet rootIDs,
		IIncludePathEntry referringEntry,
		boolean checkExistency,
		boolean retrieveExportedRoots,
		Map rootToResolvedEntries) throws JavaScriptModelException {

		String rootID = ((ClasspathEntry)resolvedEntry).rootID();
		if (rootIDs.contains(rootID)) return;

		IPath projectPath = this.project.getFullPath();
		IPath entryPath = resolvedEntry.getPath();
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		IPackageFragmentRoot root = null;

		switch(resolvedEntry.getEntryKind()){

			// source folder
			case IIncludePathEntry.CPE_SOURCE :

				if (projectPath.isPrefixOf(entryPath)){
					if (checkExistency) {
						Object target = JavaModel.getTarget(workspaceRoot, entryPath, checkExistency);
						if (target == null) return;

						if (target instanceof IFolder || target instanceof IProject){
							root = getPackageFragmentRoot((IResource)target);
						}
					} else {
						root = getFolderPackageFragmentRoot(entryPath);
					}
				}
				break;

			// internal/external JAR or folder
			case IIncludePathEntry.CPE_LIBRARY :

				if (referringEntry != null  && !resolvedEntry.isExported()) return;

				if (checkExistency) {
					Object target = JavaModel.getTarget(workspaceRoot, entryPath, checkExistency);
					if (target == null) return;

					if (target instanceof IResource){
						// internal target
						root = getPackageFragmentRoot((IResource) target);
					} else {
						// external target - only JARs allowed
						if (JavaModel.isFile(target))
						{
							 if (true) {//org.eclipse.wst.jsdt.internal.compiler.util.Util.isJavaFileName(entryPath.lastSegment())) {
								 root = new LibraryFragmentRoot(entryPath, this);

							 }
							 else
							 {
//								 root = new JarPackageFragmentRoot(entryPath, this);

							 }
						}
					}
				} else {
					root = getPackageFragmentRoot(entryPath);
				}
				break;

			// recurse into required project
			case IIncludePathEntry.CPE_PROJECT :

				if (!retrieveExportedRoots) return;
				if (referringEntry != null && !resolvedEntry.isExported()) return;

				IResource member = workspaceRoot.findMember(entryPath);
				if (member != null && member.getType() == IResource.PROJECT){// double check if bound to project (23977)
					IProject requiredProjectRsc = (IProject) member;
					if (JavaProject.hasJavaNature(requiredProjectRsc)){ // special builder binary output
						rootIDs.add(rootID);
						JavaProject requiredProject = (JavaProject)JavaScriptCore.create(requiredProjectRsc);
						requiredProject.computePackageFragmentRoots(
							requiredProject.getResolvedClasspath(),
							accumulatedRoots,
							rootIDs,
							rootToResolvedEntries == null ? resolvedEntry : ((ClasspathEntry)resolvedEntry).combineWith((ClasspathEntry) referringEntry), // only combine if need to build the reverse map
							checkExistency,
							retrieveExportedRoots,
							rootToResolvedEntries);
					}
				break;
			}
		}
		if (root != null) {
			accumulatedRoots.add(root);
			rootIDs.add(rootID);
			if (rootToResolvedEntries != null) rootToResolvedEntries.put(root, ((ClasspathEntry)resolvedEntry).combineWith((ClasspathEntry) referringEntry));
		}
	}

	/**
	 * Returns (local/all) the package fragment roots identified by the given project's classpath.
	 * Note: this follows project classpath references to find required project contributions,
	 * eliminating duplicates silently.
	 * Only works with resolved entries
	 * @param resolvedClasspath IIncludePathEntry[]
	 * @param retrieveExportedRoots boolean
	 * @return IPackageFragmentRoot[]
	 * @throws JavaScriptModelException
	 */
	public IPackageFragmentRoot[] computePackageFragmentRoots(
					IIncludePathEntry[] resolvedClasspath,
					boolean retrieveExportedRoots,
					Map rootToResolvedEntries) throws JavaScriptModelException {

		ObjectVector accumulatedRoots = new ObjectVector();
		computePackageFragmentRoots(
			resolvedClasspath,
			accumulatedRoots,
			new HashSet(5), // rootIDs
			null, // inside original project
			true, // check existency
			retrieveExportedRoots,
			rootToResolvedEntries);
		IPackageFragmentRoot[] rootArray = new IPackageFragmentRoot[accumulatedRoots.size()];
		accumulatedRoots.copyInto(rootArray);
		return rootArray;
	}

	/**
	 * Returns (local/all) the package fragment roots identified by the given project's classpath.
	 * Note: this follows project classpath references to find required project contributions,
	 * eliminating duplicates silently.
	 * Only works with resolved entries
	 * @param resolvedClasspath IIncludePathEntry[]
	 * @param accumulatedRoots ObjectVector
	 * @param rootIDs HashSet
	 * @param referringEntry project entry referring to this CP or null if initial project
	 * @param checkExistency boolean
	 * @param retrieveExportedRoots boolean
	 * @throws JavaScriptModelException
	 */
	public void computePackageFragmentRoots(
		IIncludePathEntry[] resolvedClasspath,
		ObjectVector accumulatedRoots,
		HashSet rootIDs,
		IIncludePathEntry referringEntry,
		boolean checkExistency,
		boolean retrieveExportedRoots,
		Map rootToResolvedEntries) throws JavaScriptModelException {

		if (referringEntry == null){
			rootIDs.add(rootID());
		}
		for (int i = 0, length = resolvedClasspath.length; i < length; i++){
			computePackageFragmentRoots(
				resolvedClasspath[i],
				accumulatedRoots,
				rootIDs,
				referringEntry,
				checkExistency,
				retrieveExportedRoots,
				rootToResolvedEntries);
		}
	}
	/**
	 * Compute the file name to use for a given shared property
	 * @param qName QualifiedName
	 * @return String
	 */
	public String computeSharedPropertyFileName(QualifiedName qName) {

		return '.' + qName.getLocalName();
	}

	/**
	 * Configure the project with Java nature.
	 */
	public void configure() throws CoreException {

		// register Java builder
		addToBuildSpec(JavaScriptCore.BUILDER_ID);
	}

	/*
	 * Returns whether the given resource is accessible through the children or the non-Java resources of this project.
	 * Returns true if the resource is not in the project.
	 * Assumes that the resource is a folder or a file.
	 */
	public boolean contains(IResource resource) {

		IIncludePathEntry[] classpath;
		IPath output;
		try {
			classpath = getResolvedClasspath();
			output = getOutputLocation();
		} catch (JavaScriptModelException e) {
			return false;
		}

		IPath fullPath = resource.getFullPath();
		IPath innerMostOutput = output.isPrefixOf(fullPath) ? output : null;
		IIncludePathEntry innerMostEntry = null;
		for (int j = 0, cpLength = classpath.length; j < cpLength; j++) {
			IIncludePathEntry entry = classpath[j];

			IPath entryPath = entry.getPath();
			if ((innerMostEntry == null || innerMostEntry.getPath().isPrefixOf(entryPath))
					&& entryPath.isPrefixOf(fullPath)) {
				innerMostEntry = entry;
			}
		}
		if (innerMostEntry != null) {
			// special case prj==src and nested output location
			if (innerMostOutput != null && innerMostOutput.segmentCount() > 1 // output isn't project
					&& innerMostEntry.getPath().segmentCount() == 1) { // 1 segment must be project name
				return false;
			}
			if  (resource instanceof IFolder) {
				 // folders are always included in src/lib entries
				 return true;
			}
			return true;
//			switch (innerMostEntry.getEntryKind()) {
//				case IIncludePathEntry.CPE_SOURCE:
//					// .class files are not visible in source folders
//					return !org.eclipse.wst.jsdt.internal.compiler.util.Util.isClassFileName(fullPath.lastSegment());
//				case IIncludePathEntry.CPE_LIBRARY:
//					// .js files are not visible in library folders
//					return !org.eclipse.wst.jsdt.internal.core.util.Util.isJavaLikeFileName(fullPath.lastSegment());
//			}
		}
		if (innerMostOutput != null) {
			return false;
		}
		return true;
	}

	/**
	 * Record a new marker denoting a classpath problem
	 */
	public void createClasspathProblemMarker(IJavaScriptModelStatus status) {

		IMarker marker = null;
		int severity;
		String[] arguments = CharOperation.NO_STRINGS;
		boolean isCycleProblem = false, isClasspathFileFormatProblem = false;
		switch (status.getCode()) {

			case  IJavaScriptModelStatusConstants.INCLUDEPATH_CYCLE :
				isCycleProblem = true;
				if (JavaScriptCore.ERROR.equals(getOption(JavaScriptCore.CORE_CIRCULAR_CLASSPATH, true))) {
					severity = IMarker.SEVERITY_ERROR;
				} else {
					severity = IMarker.SEVERITY_WARNING;
				}
				break;

			case  IJavaScriptModelStatusConstants.INVALID_INCLUDEPATH_FILE_FORMAT :
				isClasspathFileFormatProblem = true;
				severity = IMarker.SEVERITY_ERROR;
				break;

//			case  IJavaScriptModelStatusConstants.INCOMPATIBLE_JDK_LEVEL :
//				String setting = getOption(JavaScriptCore.CORE_INCOMPATIBLE_JDK_LEVEL, true);
//				if (JavaScriptCore.ERROR.equals(setting)) {
//					severity = IMarker.SEVERITY_ERROR;
//				} else if (JavaScriptCore.WARNING.equals(setting)) {
//					severity = IMarker.SEVERITY_WARNING;
//				} else {
//					return; // setting == IGNORE
//				}
//				break;

			default:
				IPath path = status.getPath();
				if (path != null) arguments = new String[] { path.toString() };
				if (JavaScriptCore.ERROR.equals(getOption(JavaScriptCore.CORE_INCOMPLETE_CLASSPATH, true)) &&
					status.getSeverity() != IStatus.WARNING) {
					severity = IMarker.SEVERITY_ERROR;
				} else {
					severity = IMarker.SEVERITY_WARNING;
				}
				break;
		}

		try {
			marker = this.project.createMarker(IJavaScriptModelMarker.BUILDPATH_PROBLEM_MARKER);
			marker.setAttributes(
				new String[] {
					IMarker.MESSAGE,
					IMarker.SEVERITY,
					IMarker.LOCATION,
					IJavaScriptModelMarker.CYCLE_DETECTED,
					IJavaScriptModelMarker.INCLUDEPATH_FILE_FORMAT,
					IJavaScriptModelMarker.ID,
					IJavaScriptModelMarker.ARGUMENTS ,
					IJavaScriptModelMarker.CATEGORY_ID,
					IMarker.SOURCE_ID,
				},
				new Object[] {
					status.getMessage(),
					new Integer(severity),
					Messages.classpath_buildPath,
					isCycleProblem ? "true" : "false",//$NON-NLS-1$ //$NON-NLS-2$
					isClasspathFileFormatProblem ? "true" : "false",//$NON-NLS-1$ //$NON-NLS-2$
					new Integer(status.getCode()),
					Util.getProblemArgumentsForMarker(arguments) ,
					new Integer(CategorizedProblem.CAT_BUILDPATH),
					JavaBuilder.SOURCE_ID,
				}
			);
		} catch (CoreException e) {
			// could not create marker: cannot do much
			if (JavaModelManager.VERBOSE) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Returns a new element info for this element.
	 */
	protected Object createElementInfo() {
		return new JavaProjectElementInfo();
	}

	/*
	 * Reads and decode an XML classpath string
	 */
	public IIncludePathEntry[] decodeClasspath(String xmlClasspath, Map unknownElements) throws IOException, AssertionFailedException {

		ArrayList paths = new ArrayList();
		IIncludePathEntry defaultOutput = null;
		StringReader reader = new StringReader(xmlClasspath);
		Element cpElement;
		try {
			DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			cpElement = parser.parse(new InputSource(reader)).getDocumentElement();
		} catch (SAXException e) {
			throw new IOException(Messages.file_badFormat);
		} catch (ParserConfigurationException e) {
			throw new IOException(Messages.file_badFormat);
		} finally {
			reader.close();
		}

		if (!cpElement.getNodeName().equalsIgnoreCase("classpath")) { //$NON-NLS-1$
			throw new IOException(Messages.file_badFormat);
		}
		NodeList list = cpElement.getElementsByTagName("classpathentry"); //$NON-NLS-1$
		int length = list.getLength();

		for (int i = 0; i < length; ++i) {
			Node node = list.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				IIncludePathEntry entry = ClasspathEntry.elementDecode((Element)node, this, unknownElements);
				if (entry != null){
					if (entry.getContentKind() == ClasspathEntry.K_OUTPUT) {
						defaultOutput = entry; // separate output
					} else {
						paths.add(entry);
			}
		}
			}
		}
		// return a new empty classpath is it size is 0, to differenciate from an INVALID_INCLUDEPATH
		int pathSize = paths.size();
		IIncludePathEntry[] entries = new IIncludePathEntry[pathSize + (defaultOutput == null ? 0 : 1)];
		paths.toArray(entries);
		if (defaultOutput != null) entries[pathSize] = defaultOutput; // ensure output is last item
		return entries;
	}

	public IIncludePathEntry decodeIncludepathEntry(String encodedEntry) {

		try {
			if (encodedEntry == null) return null;
			StringReader reader = new StringReader(encodedEntry);
			Element node;

			try {
				DocumentBuilder parser =
					DocumentBuilderFactory.newInstance().newDocumentBuilder();
				node = parser.parse(new InputSource(reader)).getDocumentElement();
			} catch (SAXException e) {
				return null;
			} catch (ParserConfigurationException e) {
				return null;
			} finally {
				reader.close();
			}

			if (!node.getNodeName().equalsIgnoreCase("classpathentry") //$NON-NLS-1$
					|| node.getNodeType() != Node.ELEMENT_NODE) {
				return null;
			}
			return ClasspathEntry.elementDecode(node, this, null/*not interested in unknown elements*/);
		} catch (IOException e) {
			// bad format
			return null;
		}
	}

	/**
	/**
	 * Removes the Java nature from the project.
	 */
	public void deconfigure() throws CoreException {

		// deregister Java builder
		removeFromBuildSpec(JavaScriptCore.BUILDER_ID);

		// remove .classpath file
//		getProject().getFile(ClasspathHelper.CLASSPATH_FILENAME).delete(false, null);
	}

	/**
	 * Returns a default class path.
	 * This is the root of the project
	 */
	protected IIncludePathEntry[] defaultClasspath() {
		return getDefaultClasspath();
//		return new IIncludePathEntry[] {
//			 JavaScriptCore.newSourceEntry(this.project.getFullPath())};
	}

	/**
	 * Returns a default output location.
	 * This is the project bin folder
	 */
	protected IPath defaultOutputLocation() {
		// https://bugs.eclipse.org/bugs/show_bug.cgi?id=272495
		return this.project.getFullPath(); //$NON-NLS-1$
	}

	/**
	 * Returns the XML String encoding of the class path.
	 */
	protected String encodeClasspath(IIncludePathEntry[] classpath, IPath outputLocation, boolean indent, Map unknownElements) throws JavaScriptModelException {
		try {
			ByteArrayOutputStream s = new ByteArrayOutputStream();
			OutputStreamWriter writer = new OutputStreamWriter(s, "UTF8"); //$NON-NLS-1$
			XMLWriter xmlWriter = new XMLWriter(writer, this, true/*print XML version*/);

			xmlWriter.startTag(ClasspathEntry.TAG_CLASSPATH, indent);
			for (int i = 0; i < classpath.length; ++i) {
				((ClasspathEntry)classpath[i]).elementEncode(xmlWriter, this.project.getFullPath(), indent, true, unknownElements);
			}

			if (outputLocation != null) {
				outputLocation = outputLocation.removeFirstSegments(1);
				outputLocation = outputLocation.makeRelative();
				HashMap parameters = new HashMap();
				parameters.put(ClasspathEntry.TAG_KIND, ClasspathEntry.kindToString(ClasspathEntry.K_OUTPUT));
				parameters.put(ClasspathEntry.TAG_PATH, String.valueOf(outputLocation));
				xmlWriter.printTag(ClasspathEntry.TAG_CLASSPATHENTRY, parameters, indent, true, true);
			}

			xmlWriter.endTag(ClasspathEntry.TAG_CLASSPATH, indent, true/*insert new line*/);
			writer.flush();
			writer.close();
			return s.toString("UTF8");//$NON-NLS-1$
		} catch (IOException e) {
			throw new JavaScriptModelException(e, IJavaScriptModelStatusConstants.IO_EXCEPTION);
		}
	}

	public String encodeIncludepathEntry(IIncludePathEntry classpathEntry) {
		try {
			ByteArrayOutputStream s = new ByteArrayOutputStream();
			OutputStreamWriter writer = new OutputStreamWriter(s, "UTF8"); //$NON-NLS-1$
			XMLWriter xmlWriter = new XMLWriter(writer, this, false/*don't print XML version*/);

			((ClasspathEntry)classpathEntry).elementEncode(xmlWriter, this.project.getFullPath(), true/*indent*/, true/*insert new line*/, null/*not interested in unknown elements*/);

			writer.flush();
			writer.close();
			return s.toString("UTF8");//$NON-NLS-1$
		} catch (IOException e) {
			return null; // never happens since all is done in memory
		}
	}

	/**
	 * Returns true if this handle represents the same Java project
	 * as the given handle. Two handles represent the same
	 * project if they are identical or if they represent a project with
	 * the same underlying resource and occurrence counts.
	 *
	 * @see JavaElement#equals(Object)
	 */
	public boolean equals(Object o) {

		if (this == o)
			return true;

		if (!(o instanceof JavaProject))
			return false;

		JavaProject other = (JavaProject) o;
		return this.project.equals(other.getProject());
	}

	public boolean exists() {
		try {
			return this.project.hasNature(JavaScriptCore.NATURE_ID);
		} catch (CoreException e) {
			// project does not exist or is not open
		}
		return false;
	}

	/**
	 * @see IJavaScriptProject#findElement(IPath)
	 */
	public IJavaScriptElement findElement(IPath path) throws JavaScriptModelException {
		return findElement(path, DefaultWorkingCopyOwner.PRIMARY);
	}

	/**
	 * @see IJavaScriptProject#findElement(IPath, WorkingCopyOwner)
	 */
	public IJavaScriptElement findElement(IPath path, WorkingCopyOwner owner) throws JavaScriptModelException {

		if (path == null || path.isAbsolute()) {
			throw new JavaScriptModelException(
				new JavaModelStatus(IJavaScriptModelStatusConstants.INVALID_PATH, path));
		}
		try {

			String extension = path.getFileExtension();
			if (extension == null) {
				String packageName = path.toString().replace(IPath.SEPARATOR, '.');

				NameLookup lookup = newNameLookup((WorkingCopyOwner)null/*no need to look at working copies for pkgs*/);
				IPackageFragment[] pkgFragments = lookup.findPackageFragments(packageName, false);
				if (pkgFragments == null) {
					return null;

				} else {
					// try to return one that is a child of this project
					for (int i = 0, length = pkgFragments.length; i < length; i++) {

						IPackageFragment pkgFragment = pkgFragments[i];
						if (this.equals(pkgFragment.getParent().getParent())) {
							return pkgFragment;
						}
					}
					// default to the first one
					return pkgFragments[0];
				}
			} else if (Util.isJavaLikeFileName(path.lastSegment())) {
				IPath packagePath = path.removeLastSegments(1);
				String packageName = packagePath.toString().replace(IPath.SEPARATOR, '.');
				String typeName = path.lastSegment();
				typeName = typeName.substring(0, typeName.length() - extension.length() - 1);
				String qualifiedName = null;
				if (packageName.length() > 0) {
					qualifiedName = packageName + "." + typeName; //$NON-NLS-1$
				} else {
					qualifiedName = typeName;
				}
				// lookup cu
				NameLookup lookup = newNameLookup(owner);
				ITypeRoot cu = lookup.findCompilationUnit(qualifiedName);
				if(cu!=null)	return cu;
				// lookup type
				
				
				NameLookup.Answer answer =
					lookup.findType(
					qualifiedName,
					false,
					NameLookup.ACCEPT_ALL,
					true/* consider secondary types */,
					false/* do NOT wait for indexes */,
					false/*don't check restrictions*/,
					null);

				if (answer != null) {
					return answer.type.getParent();
				} else {
					return null;
				}
			} else {
				// unsupported extension
				return null;
			}
		} catch (JavaScriptModelException e) {
			if (e.getStatus().getCode()
				== IJavaScriptModelStatusConstants.ELEMENT_DOES_NOT_EXIST) {
				return null;
			} else {
				throw e;
			}
		}
	}

	/**
	 * @see IJavaScriptProject
	 */
	public IPackageFragment findPackageFragment(IPath path)
		throws JavaScriptModelException {

		return findPackageFragment0(JavaProject.canonicalizedPath(path));
	}
	/*
	 * non path canonicalizing version
	 */
	private IPackageFragment findPackageFragment0(IPath path)
		throws JavaScriptModelException {

		NameLookup lookup = newNameLookup((WorkingCopyOwner)null/*no need to look at working copies for pkgs*/);
		return lookup.findPackageFragment(path);
	}

	/**
	 * @see IJavaScriptProject
	 */
	public IPackageFragmentRoot findPackageFragmentRoot(IPath path)
		throws JavaScriptModelException {

		return findPackageFragmentRoot0(JavaProject.canonicalizedPath(path));
	}
	/*
	 * no path canonicalization
	 */
	public IPackageFragmentRoot findPackageFragmentRoot0(IPath path)
		throws JavaScriptModelException {

		IPackageFragmentRoot[] allRoots = this.getAllPackageFragmentRoots();
		if (!path.isAbsolute()) {
			throw new IllegalArgumentException(Messages.path_mustBeAbsolute);
		}
		for (int i= 0; i < allRoots.length; i++) {
			IPackageFragmentRoot classpathRoot= allRoots[i];
			if (classpathRoot.getPath().equals(path)) {
				return classpathRoot;
			}
		}
		return null;
	}
	/**
	 * @see IJavaScriptProject
	 */
	public IPackageFragmentRoot[] findPackageFragmentRoots(IIncludePathEntry entry) {
		try {
			IIncludePathEntry[] classpath = this.getRawIncludepath();
			for (int i = 0, length = classpath.length; i < length; i++) {
				if (classpath[i].equals(entry)) { // entry may need to be resolved
					return
						computePackageFragmentRoots(
							resolveClasspath(new IIncludePathEntry[] {entry}),
							false, // don't retrieve exported roots
							null); /*no reverse map*/
				}
			}
		} catch (JavaScriptModelException e) {
			// project doesn't exist: return an empty array
		}
		return new IPackageFragmentRoot[] {};
	}
	/**
	 * @see IJavaScriptProject#findType(String)
	 */
	public IType findType(String fullyQualifiedName) throws JavaScriptModelException {
		return findType(fullyQualifiedName, DefaultWorkingCopyOwner.PRIMARY);
	}
	
	/**
	 * @see IJavaScriptProject#findTypes(String)
	 */
	public IType[] findTypes(String fullyQualifiedName) throws JavaScriptModelException {
		return findTypes(fullyQualifiedName, DefaultWorkingCopyOwner.PRIMARY);
	}
	/**
	 * @see IJavaScriptProject#findType(String, IProgressMonitor)
	 */
	public IType findType(String fullyQualifiedName, IProgressMonitor progressMonitor) throws JavaScriptModelException {
		return findType(fullyQualifiedName, DefaultWorkingCopyOwner.PRIMARY, progressMonitor);
	}

	/*
	 * Internal findType with instanciated name lookup
	 */
	IType findType(String fullyQualifiedName, NameLookup lookup, boolean considerSecondaryTypes, IProgressMonitor progressMonitor) throws JavaScriptModelException {
		NameLookup.Answer answer = lookup.findType(
			fullyQualifiedName,
			false,
			NameLookup.ACCEPT_ALL,
			considerSecondaryTypes,
			true, /* wait for indexes (only if consider secondary types)*/
			false/*don't check restrictions*/,
			progressMonitor);
		if (answer == null) {
			// try to find enclosing type
			int lastDot = fullyQualifiedName.lastIndexOf('.');
			if (lastDot == -1) return null;
			IType type = findType(fullyQualifiedName.substring(0, lastDot), lookup, considerSecondaryTypes, progressMonitor);
			if (type != null) {
				type = type.getType(fullyQualifiedName.substring(lastDot+1));
				if (!type.exists()) {
					return null;
				}
			}
			return type;
		}
		if (answer.type==null && answer.element instanceof ITypeRoot)
		{
			answer.type=((ITypeRoot)answer.element).getType(fullyQualifiedName);
		}
		else if (answer.type==null && answer.element!=null && answer.element.getClass().isArray())
		{
			Object [] elements=(Object [])answer.element;
			if(elements[0] instanceof ITypeRoot)
				answer.type=((ITypeRoot)elements[0]).getType(fullyQualifiedName);
		}
		return answer.type;
	}
	
	/*
	 * Internal findType with instantiated name lookup - can return multiple IType's
	 */
	IType[] findTypes(String fullyQualifiedName, NameLookup lookup, boolean considerSecondaryTypes, IProgressMonitor progressMonitor) throws JavaScriptModelException {
		NameLookup.Answer answer = lookup.findType(
			fullyQualifiedName,
			false,
			NameLookup.ACCEPT_ALL,
			considerSecondaryTypes,
			true, /* wait for indexes (only if consider secondary types)*/
			false/*don't check restrictions*/,
			progressMonitor);
		if (answer == null) {
			// try to find enclosing type
			int lastDot = fullyQualifiedName.lastIndexOf('.');
			if (lastDot == -1) return null;
			IType type = findType(fullyQualifiedName.substring(0, lastDot), lookup, considerSecondaryTypes, progressMonitor);
			if (type != null) {
				type = type.getType(fullyQualifiedName.substring(lastDot+1));
				if (type != null && type.exists()) {
					return new IType[]{type};
				}
			}
			return new IType[0];
		}
		if (answer.type==null && answer.element instanceof ITypeRoot)
		{
			answer.type=((ITypeRoot)answer.element).getType(fullyQualifiedName);
		}
		else if (answer.type==null && answer.element!=null && answer.element.getClass().isArray())
		{
			Object[] elements = (Object[]) answer.element;
			List iTypes = new ArrayList(elements.length);
			for (int i = 0; i < elements.length; i++) {
				// could be a binding instead
				if (elements[i] instanceof ITypeRoot) {
					IType rootType = ((ITypeRoot) elements[i]).getType(fullyQualifiedName);
					if (rootType != null && rootType.exists()) {
						iTypes.add(rootType);
					}
				}
			}
			return (IType[]) iTypes.toArray(new IType[iTypes.size()]);
		}
		if (answer.type != null && answer.type.exists()) {
			return new IType[]{answer.type};
		}
		return new IType[0];
	}
	/**
	 * @see IJavaScriptProject#findType(String, String)
	 */
	public IType findType(String packageName, String typeQualifiedName) throws JavaScriptModelException {
		return findType(packageName, typeQualifiedName, DefaultWorkingCopyOwner.PRIMARY);
	}
	/**
	 * @see IJavaScriptProject#findType(String, String, IProgressMonitor)
	 */
	public IType findType(String packageName, String typeQualifiedName, IProgressMonitor progressMonitor) throws JavaScriptModelException {
		return findType(packageName, typeQualifiedName, DefaultWorkingCopyOwner.PRIMARY, progressMonitor);
	}
	/*
	 * Internal findType with instanciated name lookup
	 */
	IType findType(String packageName, String typeQualifiedName, NameLookup lookup, boolean considerSecondaryTypes, IProgressMonitor progressMonitor) throws JavaScriptModelException {
		NameLookup.Answer answer = lookup.findType(
			typeQualifiedName,
			packageName,
			false,
			NameLookup.ACCEPT_ALL,
			considerSecondaryTypes,
			true, // wait for indexes (in case we need to consider secondary types)
			false/*don't check restrictions*/,
			progressMonitor);
		return answer == null ? null : answer.type;
	}
	/**
	 * @see IJavaScriptProject#findType(String, String, WorkingCopyOwner)
	 */
	public IType findType(String packageName, String typeQualifiedName, WorkingCopyOwner owner) throws JavaScriptModelException {
		NameLookup lookup = newNameLookup(owner);
		return findType(
			packageName,
			typeQualifiedName,
			lookup,
			false, // do not consider secondary types
			null);
	}

	/**
	 * @see IJavaScriptProject#findType(String, String, WorkingCopyOwner, IProgressMonitor)
	 */
	public IType findType(String packageName, String typeQualifiedName, WorkingCopyOwner owner, IProgressMonitor progressMonitor) throws JavaScriptModelException {
		NameLookup lookup = newNameLookup(owner);
		return findType(
			packageName,
			typeQualifiedName,
			lookup,
			true, // consider secondary types
			progressMonitor);
	}

	/**
	 * @see IJavaScriptProject#findType(String, WorkingCopyOwner)
	 */
	public IType findType(String fullyQualifiedName, WorkingCopyOwner owner) throws JavaScriptModelException {
		NameLookup lookup = newNameLookup(owner);
		return findType(fullyQualifiedName, lookup, false, null);
	}
	
	/**
	 * @see IJavaScriptProject#findTypes(String, WorkingCopyOwner)
	 */
	public IType[] findTypes(String fullyQualifiedName, WorkingCopyOwner owner) throws JavaScriptModelException {
		NameLookup lookup = newNameLookup(owner);
		return findTypes(fullyQualifiedName, lookup, false, null);
	}

	/**
	 * @see IJavaScriptProject#findType(String, WorkingCopyOwner, IProgressMonitor)
	 */
	public IType findType(String fullyQualifiedName, WorkingCopyOwner owner, IProgressMonitor progressMonitor) throws JavaScriptModelException {
		NameLookup lookup = newNameLookup(owner);
		return findType(fullyQualifiedName, lookup, true, progressMonitor);
	}

	/**
	 * Remove all markers denoting classpath problems
	 */ //TODO (philippe) should improve to use a bitmask instead of booleans (CYCLE, FORMAT, VALID)
	protected void flushClasspathProblemMarkers(boolean flushCycleMarkers, boolean flushClasspathFormatMarkers) {
		try {
			if (this.project.isAccessible()) {
				IMarker[] markers = this.project.findMarkers(IJavaScriptModelMarker.BUILDPATH_PROBLEM_MARKER, false, IResource.DEPTH_ZERO);
				for (int i = 0, length = markers.length; i < length; i++) {
					IMarker marker = markers[i];
					if (flushCycleMarkers && flushClasspathFormatMarkers) {
						marker.delete();
					} else {
						String cycleAttr = (String)marker.getAttribute(IJavaScriptModelMarker.CYCLE_DETECTED);
						String classpathFileFormatAttr =  (String)marker.getAttribute(IJavaScriptModelMarker.INCLUDEPATH_FILE_FORMAT);
						if ((flushCycleMarkers == (cycleAttr != null && cycleAttr.equals("true"))) //$NON-NLS-1$
							&& (flushClasspathFormatMarkers == (classpathFileFormatAttr != null && classpathFileFormatAttr.equals("true")))){ //$NON-NLS-1$
							marker.delete();
						}
					}
				}
			}
		} catch (CoreException e) {
			// could not flush markers: not much we can do
			if (JavaModelManager.VERBOSE) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Returns the set of patterns corresponding to this project visibility given rules
	 * @return an array of IPath or null if none
	 */
	public IPath[] getAccessRestrictions(String optionName) {
		String sequence = getOption(optionName, true); // inherit from workspace
		if (sequence == null || sequence.length() == 0) return null;
		IPath[] rules = null;
		char[][] patterns = CharOperation.splitOn('|', sequence.toCharArray());
		int patternCount;
		if ((patternCount  = patterns.length) > 0) {
			rules = new IPath[patternCount];
			for (int j = 0; j < patterns.length; j++){
				rules[j] = new Path(new String(patterns[j]));
			}
		}
		return rules;
	}

	/**
	 * @see IJavaScriptProject
	 */
	public IPackageFragmentRoot[] getAllPackageFragmentRoots()
		throws JavaScriptModelException {

		return getAllPackageFragmentRoots(null /*no reverse map*/);
	}

	public IPackageFragmentRoot[] getAllPackageFragmentRoots(Map rootToResolvedEntries) throws JavaScriptModelException {

		return computePackageFragmentRoots(getResolvedClasspath(), true/*retrieveExportedRoots*/, rootToResolvedEntries);
	}

	/**
	 * Returns the classpath entry that refers to the given path
	 * or <code>null</code> if there is no reference to the path.
	 * @param path IPath
	 * @return IIncludePathEntry
	 * @throws JavaScriptModelException
	 */
	public IIncludePathEntry getClasspathEntryFor(IPath path) throws JavaScriptModelException {
		getResolvedClasspath(); // force resolution
		PerProjectInfo perProjectInfo = getPerProjectInfo();
		if (perProjectInfo == null)
			return null;
		Map rootPathToResolvedEntries = perProjectInfo.rootPathToResolvedEntries;
		if (rootPathToResolvedEntries == null)
			return null;
		return (IIncludePathEntry) rootPathToResolvedEntries.get(path);
	}

	/*
	 * Returns the cycle marker associated with this project or null if none.
	 */
	public IMarker getCycleMarker(){
		try {
			if (this.project.isAccessible()) {
				IMarker[] markers = this.project.findMarkers(IJavaScriptModelMarker.BUILDPATH_PROBLEM_MARKER, false, IResource.DEPTH_ZERO);
				for (int i = 0, length = markers.length; i < length; i++) {
					IMarker marker = markers[i];
					String cycleAttr = (String)marker.getAttribute(IJavaScriptModelMarker.CYCLE_DETECTED);
					if (cycleAttr != null && cycleAttr.equals("true")){ //$NON-NLS-1$
						return marker;
					}
				}
			}
		} catch (CoreException e) {
			// could not get markers: return null
		}
		return null;
	}

		/**
    	 * Returns the project custom preference pool.
    	 * Project preferences may include custom encoding.
    	 * @return IEclipsePreferences
    	 */
    	public IEclipsePreferences getEclipsePreferences(){
    		if (!JavaProject.hasJavaNature(this.project)) return null;
    		// Get cached preferences if exist
    		JavaModelManager.PerProjectInfo perProjectInfo = JavaModelManager.getJavaModelManager().getPerProjectInfo(this.project, true);
    		if (perProjectInfo.preferences != null) return perProjectInfo.preferences;
    		// Init project preferences
    		IScopeContext context = new ProjectScope(getProject());
    		final IEclipsePreferences eclipsePreferences = context.getNode(JavaScriptCore.PLUGIN_ID);
    		updatePreferences(eclipsePreferences);
    		perProjectInfo.preferences = eclipsePreferences;

    		// Listen to node removal from parent in order to reset cache (see bug 68993)
    		IEclipsePreferences.INodeChangeListener nodeListener = new IEclipsePreferences.INodeChangeListener() {
    			public void added(IEclipsePreferences.NodeChangeEvent event) {
    				// do nothing
    			}
    			public void removed(IEclipsePreferences.NodeChangeEvent event) {
    				if (event.getChild() == eclipsePreferences) {
    					JavaModelManager.getJavaModelManager().resetProjectPreferences(JavaProject.this);
    				}
    			}
    		};
    		((IEclipsePreferences) eclipsePreferences.parent()).addNodeChangeListener(nodeListener);

    		// Listen to preference changes
    		IEclipsePreferences.IPreferenceChangeListener preferenceListener = new IEclipsePreferences.IPreferenceChangeListener() {
    			public void preferenceChange(IEclipsePreferences.PreferenceChangeEvent event) {
    				String propertyName = event.getKey();
					JavaModelManager manager = JavaModelManager.getJavaModelManager();
					if (propertyName.startsWith(JavaScriptCore.PLUGIN_ID)) {
						if (propertyName.equals(JavaScriptCore.CORE_JAVA_BUILD_CLEAN_OUTPUT_FOLDER) ||
							propertyName.equals(JavaScriptCore.CORE_JAVA_BUILD_RESOURCE_COPY_FILTER) ||
							propertyName.equals(JavaScriptCore.CORE_JAVA_BUILD_DUPLICATE_RESOURCE) ||
							propertyName.equals(JavaScriptCore.CORE_JAVA_BUILD_RECREATE_MODIFIED_CLASS_FILES_IN_OUTPUT_FOLDER) ||
							propertyName.equals(JavaScriptCore.CORE_JAVA_BUILD_INVALID_CLASSPATH) ||
							propertyName.equals(JavaScriptCore.CORE_ENABLE_CLASSPATH_EXCLUSION_PATTERNS) ||
							propertyName.equals(JavaScriptCore.CORE_ENABLE_CLASSPATH_MULTIPLE_OUTPUT_LOCATIONS) ||
							propertyName.equals(JavaScriptCore.CORE_INCOMPLETE_CLASSPATH) ||
							propertyName.equals(JavaScriptCore.CORE_CIRCULAR_CLASSPATH) ||
							propertyName.equals(JavaScriptCore.CORE_INCOMPATIBLE_JDK_LEVEL))
						{
							manager.deltaState.addClasspathValidation(JavaProject.this);
						}
						manager.resetProjectOptions(JavaProject.this);
    				}
    			}
    		};
    		eclipsePreferences.addPreferenceChangeListener(preferenceListener);
    		return eclipsePreferences;
    	}

	public String getElementName() {
		return this.project.getName();
	}

	/**
	 * @see IJavaScriptElement
	 */
	public int getElementType() {
		return JAVASCRIPT_PROJECT;
	}

	/**
	 * This is a helper method returning the expanded classpath for the project, as a list of classpath entries,
	 * where all classpath variable entries have been resolved and substituted with their final target entries.
	 * All project exports have been appended to project entries.
	 * @return IIncludePathEntry[]
	 * @throws JavaScriptModelException
	 */
	public IIncludePathEntry[] getExpandedClasspath()	throws JavaScriptModelException {

			ObjectVector accumulatedEntries = new ObjectVector();
			computeExpandedClasspath(null, new HashSet(5), accumulatedEntries);

			IIncludePathEntry[] expandedPath = new IIncludePathEntry[accumulatedEntries.size()];
			accumulatedEntries.copyInto(expandedPath);

			return expandedPath;
	}

	/**
	 * The path is known to match a source/library folder entry.
	 * @param path IPath
	 * @return IPackageFragmentRoot
	 */
	public IPackageFragmentRoot getFolderPackageFragmentRoot(IPath path) {
		if (path.segmentCount() == 1) { // default project root
			return getPackageFragmentRoot(this.project);
		}
		return getPackageFragmentRoot(this.project.getWorkspace().getRoot().getFolder(path));
	}

	/*
	 * @see JavaElement
	 */
	public IJavaScriptElement getHandleFromMemento(String token, MementoTokenizer memento, WorkingCopyOwner owner) {
		switch (token.charAt(0)) {
			case JEM_PACKAGEFRAGMENTROOT:
				String rootPath = IPackageFragmentRoot.DEFAULT_PACKAGEROOT_PATH;
				token = null;
				while (memento.hasMoreTokens()) {
					token = memento.nextToken();
					char firstChar = token.charAt(0);
					if (firstChar != JEM_PACKAGEFRAGMENT && firstChar != JEM_COUNT) {
						rootPath += token;
					} else {
						break;
					}
				}
				JavaElement root = (JavaElement)getPackageFragmentRoot(new Path(rootPath));
				if (token != null && token.charAt(0) == JEM_PACKAGEFRAGMENT) {
					return root.getHandleFromMemento(token, memento, owner);
				} else {
					return root.getHandleFromMemento(memento, owner);
				}
		}
		return null;
	}

	/**
	 * Returns the <code>char</code> that marks the start of this handles
	 * contribution to a memento.
	 */
	protected char getHandleMementoDelimiter() {

		return JEM_JAVAPROJECT;
	}

	/**
	 * Find the specific Java command amongst the given build spec
	 * and return its index or -1 if not found.
	 */
	private int getJavaCommandIndex(ICommand[] buildSpec, String builder_ID) {

		for (int i = 0; i < buildSpec.length; ++i) {
			if (buildSpec[i].getBuilderName().equals(builder_ID)) {
				return i;
			}
		}
		return -1;
	}
//	private int getJavaCommandIndex(ICommand[] buildSpec) {
//		return getJavaCommandIndex(buildSpec, JavaScriptCore.BUILDER_ID);
//
//	}
	/**
	 * Convenience method that returns the specific type of info for a Java project.
	 */
	protected JavaProjectElementInfo getJavaProjectElementInfo()
		throws JavaScriptModelException {

		return (JavaProjectElementInfo) getElementInfo();
	}

	/**
	 * Returns an array of non-java resources contained in the receiver.
	 */
	public Object[] getNonJavaScriptResources() throws JavaScriptModelException {

		return ((JavaProjectElementInfo) getElementInfo()).getNonJavaResources(this);
	}

	/**
	 * @see org.eclipse.wst.jsdt.core.IJavaScriptProject#getOption(String, boolean)
	 */
	public String getOption(String optionName, boolean inheritJavaCoreOptions) {

		String propertyName = optionName;
		if (JavaModelManager.getJavaModelManager().optionNames.contains(propertyName)){
			IEclipsePreferences projectPreferences = getEclipsePreferences();
			String javaCoreDefault = inheritJavaCoreOptions ? JavaScriptCore.getOption(propertyName) : null;
			if (projectPreferences == null) return javaCoreDefault;
			String value = projectPreferences.get(propertyName, javaCoreDefault);
			return value == null ? null : value.trim();
		}
		return null;
	}

	/**
	 * @see org.eclipse.wst.jsdt.core.IJavaScriptProject#getOptions(boolean)
	 */
	public Map getOptions(boolean inheritJavaCoreOptions) {

		// initialize to the defaults from JavaScriptCore options pool
		Map options = inheritJavaCoreOptions ? JavaScriptCore.getOptions() : new Hashtable(5);

		// Get project specific options
		JavaModelManager.PerProjectInfo perProjectInfo = null;
		Hashtable projectOptions = null;
		HashSet optionNames = JavaModelManager.getJavaModelManager().optionNames;
		try {
			perProjectInfo = getPerProjectInfo();
			projectOptions = perProjectInfo.options;
			if (projectOptions == null) {
				// get eclipse preferences
				IEclipsePreferences projectPreferences= getEclipsePreferences();
				if (projectPreferences == null) return options; // cannot do better (non-Java project)
				// create project options
				String[] propertyNames = projectPreferences.keys();
				projectOptions = new Hashtable(propertyNames.length);
				for (int i = 0; i < propertyNames.length; i++){
					String propertyName = propertyNames[i];
					String value = projectPreferences.get(propertyName, null);
					if (value != null && optionNames.contains(propertyName)){
						projectOptions.put(propertyName, value.trim());
					}
				}
				// cache project options
				perProjectInfo.options = projectOptions;
			}
		} catch (JavaScriptModelException jme) {
			projectOptions = new Hashtable();
		} catch (BackingStoreException e) {
			projectOptions = new Hashtable();
		}

		// Inherit from JavaScriptCore options if specified
		if (inheritJavaCoreOptions) {
			Iterator propertyNames = projectOptions.entrySet().iterator();
			while (propertyNames.hasNext()) {
				Map.Entry entry = (Map.Entry) propertyNames.next();
				String propertyName = (String) entry.getKey();
				String propertyValue = (String) entry.getValue();
				if (propertyValue != null && optionNames.contains(propertyName)){
					options.put(propertyName, propertyValue.trim());
				}
			}
			return options;
		}
		return projectOptions;
	}

	/**
	 * @see IJavaScriptProject
	 */
	public IPath getOutputLocation() throws JavaScriptModelException {
		// Do not create marker while getting output location
		JavaModelManager.PerProjectInfo perProjectInfo = this.getPerProjectInfo();
		IPath outputLocation = perProjectInfo.outputLocation;
		if (outputLocation != null) return outputLocation;

		// force to read classpath - will position output location as well
		getRawIncludepath();

		outputLocation = perProjectInfo.outputLocation;
		if (outputLocation == null) {
			return this.defaultOutputLocation();
		}
		return outputLocation;
	}

	/**
	 * @param path IPath
	 * @return A handle to the package fragment root identified by the given path.
	 * This method is handle-only and the element may or may not exist. Returns
	 * <code>null</code> if unable to generate a handle from the path (for example,
	 * an absolute path that has less than 1 segment. The path may be relative or
	 * absolute.
	 */
	public IPackageFragmentRoot getPackageFragmentRoot(IPath path) {
		if (!path.isAbsolute()) {
			path = getPath().append(path);
		}
		int segmentCount = path.segmentCount();
		String lastSegment = path.lastSegment();
		switch (segmentCount) {
			case 0:
				return null;
			case 1:
				if (path.equals(getPath())) { // see https://bugs.eclipse.org/bugs/show_bug.cgi?id=75814
					// default root
					return getPackageFragmentRoot(this.project);
				}
			default:
				// a path ending with .jar/.zip is still ambiguous and could still resolve to a source/lib folder
				// thus will try to guess based on existing resource
				if (org.eclipse.wst.jsdt.internal.compiler.util.Util.isArchiveFileName(lastSegment)
						|| org.eclipse.wst.jsdt.internal.compiler.util.Util.isJavaFileName(lastSegment)) {
					IResource resource = this.project.getWorkspace().getRoot().findMember(path);
					if (resource != null && resource.getType() == IResource.FOLDER){
						return getPackageFragmentRoot(resource);
					}
					return getPackageFragmentRoot0(path);
				} else if (segmentCount == 1) {
					// lib being another project
					return getPackageFragmentRoot(this.project.getWorkspace().getRoot().getProject(lastSegment));
				} else {
					// lib being a folder
					IFolder folder = this.project.getWorkspace().getRoot().getFolder(path);
					if (folder!=null && folder.exists())
						return getPackageFragmentRoot(folder);
					return getPackageFragmentRoot0(path);
				}
		}
	}

	/**
	 * @see IJavaScriptProject
	 */
	public IPackageFragmentRoot getPackageFragmentRoot(IResource resource) {

		switch (resource.getType()) {
			case IResource.FILE:
				if (org.eclipse.wst.jsdt.internal.compiler.util.Util.isArchiveFileName(resource.getName())) {
//					return new JarPackageFragmentRoot(resource, this);
				} else {
					return null;
				}
			case IResource.FOLDER:
				return new PackageFragmentRoot(resource, this);
			case IResource.PROJECT:
				return new PackageFragmentRoot(resource, this);
			default:
				return null;
		}
	}

	/**
	 * @see IJavaScriptProject
	 */
	public IPackageFragmentRoot getPackageFragmentRoot(String jarPath) {

		return getPackageFragmentRoot0(JavaProject.canonicalizedPath(new Path(jarPath)));
	}

	/*
	 * no path canonicalization
	 */
	public IPackageFragmentRoot getPackageFragmentRoot0(IPath jarPath) {

		return new LibraryFragmentRoot(jarPath, this);
	}

	/**
	 * @see IJavaScriptProject
	 */
	public IPackageFragmentRoot[] getPackageFragmentRoots()
		throws JavaScriptModelException {

		Object[] children;
		int length;
		IPackageFragmentRoot[] roots;

		System.arraycopy(
			children = getChildren(),
			0,
			roots = new IPackageFragmentRoot[length = children.length],
			0,
			length);

		return roots;
	}

	/**
	 * @see IJavaScriptProject
	 */
	public IPackageFragment[] getPackageFragments() throws JavaScriptModelException {

		IPackageFragmentRoot[] roots = getPackageFragmentRoots();
		return getPackageFragmentsInRoots(roots);
	}

	/**
	 * Returns all the package fragments found in the specified
	 * package fragment roots.
	 * @param roots IPackageFragmentRoot[]
	 * @return IPackageFragment[]
	 */
	public IPackageFragment[] getPackageFragmentsInRoots(IPackageFragmentRoot[] roots) {

		ArrayList frags = new ArrayList();
		for (int i = 0; i < roots.length; i++) {
			IPackageFragmentRoot root = roots[i];
			try {
				IJavaScriptElement[] rootFragments = root.getChildren();
				for (int j = 0; j < rootFragments.length; j++) {
					frags.add(rootFragments[j]);
				}
			} catch (JavaScriptModelException e) {
				// do nothing
			}
		}
		IPackageFragment[] fragments = new IPackageFragment[frags.size()];
		frags.toArray(fragments);
		return fragments;
	}

	/**
	 * @see IJavaScriptElement
	 */
	public IPath getPath() {
		return this.project.getFullPath();
	}

	public JavaModelManager.PerProjectInfo getPerProjectInfo() throws JavaScriptModelException {
		return JavaModelManager.getJavaModelManager().getPerProjectInfoCheckExistence(this.project);
	}

	private IPath getPluginWorkingLocation() {
		return this.project.getWorkingLocation(JavaScriptCore.PLUGIN_ID);
	}

	/**
	 * Returns the project custom preference pool.
	 * Project preferences may include custom encoding.
	 * @return Preferences
	 * @deprecated WARNING:  this method do nothing from now and will be removed soon!
	 * 	If you use it, switch as soon as possible to new preferences API by using
	 * 	{@link #getEclipsePreferences()} to avoid future compilation error...
	 * @see <a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=59258">bug 59258</a>
	 * TODO (frederic) remove for 3.1...
	 */
	public Preferences getPreferences(){
		/*
		if (!JavaProject.hasJavaNature(this.project)) return null;
		JavaModelManager.PerProjectInfo perProjectInfo = JavaModelManager.getJavaModelManager().getPerProjectInfo(this.project, true);
		Preferences preferences =  perProjectInfo.preferences;
		if (preferences != null) return preferences;
		preferences = loadPreferences();
		if (preferences == null) preferences = new Preferences();
		perProjectInfo.preferences = preferences;
		return preferences;
		*/
		return new Preferences();
	}

	/**
	 * @see IJavaScriptProject#getProject()
	 */
	public IProject getProject() {
		return this.project;
	}

	public LookupCache getProjectCache() throws JavaScriptModelException {
		return ((JavaProjectElementInfo) getElementInfo()).getProjectCache(this);
	}

	/**
	 * @see IJavaScriptProject
	 */
	public IIncludePathEntry[] getRawIncludepath() throws JavaScriptModelException {
		JavaModelManager.PerProjectInfo perProjectInfo = getPerProjectInfo();
		IIncludePathEntry[] classpath = perProjectInfo.rawClasspath;
		if (classpath != null) return classpath;

		classpath = perProjectInfo.readAndCacheClasspath(this);

		if (classpath == JavaProject.INVALID_CLASSPATH)
			return defaultClasspath();

		return classpath;
	}

	/**
	 * @see IJavaScriptProject#getRequiredProjectNames()
	 */
	public String[] getRequiredProjectNames() throws JavaScriptModelException {

		return this.projectPrerequisites(getResolvedClasspath());
	}

	/*
	 * Returns the cached resolved classpath, or compute it ignoring unresolved entries and cache it.
	 */
	public IIncludePathEntry[] getResolvedClasspath() throws JavaScriptModelException {
		try {
			PerProjectInfo perProjectInfo = getPerProjectInfo();
			if (perProjectInfo.resolvedClasspath == null)
				resolveClasspath(perProjectInfo);
			return perProjectInfo.resolvedClasspath;
		} catch (JavaScriptModelException e) {
			if (e.getStatus().getCode()==IJavaScriptModelStatusConstants.ELEMENT_DOES_NOT_EXIST)
			{
				IIncludePathEntry[] defaultClasspath = defaultClasspath();
				return resolveClasspath(defaultClasspath);
			}
			else
				throw e;
		}
	}

	/**
	 * @see IJavaScriptProject
	 */
	public IIncludePathEntry[] getResolvedIncludepath(boolean ignoreUnresolvedEntry) throws JavaScriptModelException {
		if  (JavaModelManager.getJavaModelManager().isClasspathBeingResolved(this)) {
			if (JavaModelManager.CP_RESOLVE_VERBOSE_ADVANCED)
				verbose_reentering_classpath_resolution();
		    return RESOLUTION_IN_PROGRESS;
		}
		PerProjectInfo perProjectInfo = getPerProjectInfo();

		// use synchronized block to ensure consistency
		IIncludePathEntry[] resolvedClasspath;
		IJavaScriptModelStatus unresolvedEntryStatus;
		synchronized (perProjectInfo) {
			resolvedClasspath = perProjectInfo.resolvedClasspath;
			unresolvedEntryStatus = perProjectInfo.unresolvedEntryStatus;
		}

		if (resolvedClasspath == null
				|| (unresolvedEntryStatus != null && !unresolvedEntryStatus.isOK())) { // force resolution to ensure initializers are run again
			resolveClasspath(perProjectInfo);
			synchronized (perProjectInfo) {
				resolvedClasspath = perProjectInfo.resolvedClasspath;
				unresolvedEntryStatus = perProjectInfo.unresolvedEntryStatus;
			}
		}
		if (!ignoreUnresolvedEntry && unresolvedEntryStatus != null && !unresolvedEntryStatus.isOK())
			throw new JavaScriptModelException(unresolvedEntryStatus);
		return resolvedClasspath;
	}

	private void verbose_reentering_classpath_resolution() {
		Util.verbose(
			"CPResolution: reentering raw classpath resolution, will use empty classpath instead" + //$NON-NLS-1$
			"	project: " + getElementName() + '\n' + //$NON-NLS-1$
			"	invocation stack trace:"); //$NON-NLS-1$
		new Exception("<Fake exception>").printStackTrace(System.out); //$NON-NLS-1$
	}

	/**
	 * @see IJavaScriptElement
	 */
	public IResource getResource() {
		return this.project;
	}

	/**
	 * Retrieve a shared property on a project. If the property is not defined, answers null.
	 * Note that it is orthogonal to IResource persistent properties, and client code has to decide
	 * which form of storage to use appropriately. Shared properties produce real resource files which
	 * can be shared through a VCM onto a server. Persistent properties are not shareable.
	 *
	 * @param key String
	 * @see JavaProject#setSharedProperty(String, String)
	 * @return String
	 * @throws CoreException
	 */
	public String getSharedProperty(String key) throws CoreException {

		IFile rscFile = this.project.getFile(key);
		String property = null;
		//IFile rscFile = this.project.getFile(key);
		if (rscFile.exists()) {
			byte[] bytes = Util.getResourceContentsAsByteArray(rscFile);
			try {
				property = new String(bytes, org.eclipse.wst.jsdt.internal.compiler.util.Util.UTF_8); // .classpath always encoded with UTF-8
			} catch (UnsupportedEncodingException e) {
				Util.log(e, "Could not read .jsdtscope with UTF-8 encoding"); //$NON-NLS-1$
				// fallback to default
				property = new String(bytes);
			}
		} else {
			// when a project is imported, we get a first delta for the addition of the .project, but the .classpath is not accessible
			// so default to using java.io.File
			// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=96258
			URI location = rscFile.getLocationURI();
			if (location != null) {
				File file = Util.toLocalFile(location, null/*no progress monitor available*/);
				if (file != null && file.exists()) {
					byte[] bytes;
					try {
						bytes = org.eclipse.wst.jsdt.internal.compiler.util.Util.getFileByteContent(file);
					} catch (IOException e) {
						return null;
					}
					try {
						property = new String(bytes, org.eclipse.wst.jsdt.internal.compiler.util.Util.UTF_8); // .classpath always encoded with UTF-8
					} catch (UnsupportedEncodingException e) {
						Util.log(e, "Could not read .jsdtscope with UTF-8 encoding"); //$NON-NLS-1$
						// fallback to default
						property = new String(bytes);
					}
				}
			}
		}
		return property;
	}

	/**
	 * @see JavaElement
	 */
	public SourceMapper getSourceMapper() {

		return null;
	}

	/**
	 * @see IJavaScriptElement
	 */
	public IResource getUnderlyingResource() throws JavaScriptModelException {
		if (!exists()) throw newNotPresentException();
		return this.project;
	}

	/**
	 * @see IJavaScriptProject
	 */
	public boolean hasBuildState() {

		return JavaModelManager.getJavaModelManager().getLastBuiltState(this.project, null) != null;
	}

	/**
	 * @see IJavaScriptProject
	 */
	public boolean hasIncludepathCycle(IIncludePathEntry[] preferredClasspath) {
		HashSet cycleParticipants = new HashSet();
		HashMap preferredClasspaths = new HashMap(1);
		preferredClasspaths.put(this, preferredClasspath);
		updateCycleParticipants(new ArrayList(2), cycleParticipants, ResourcesPlugin.getWorkspace().getRoot(), new HashSet(2), preferredClasspaths);
		return !cycleParticipants.isEmpty();
	}

	public boolean hasCycleMarker(){
		return this.getCycleMarker() != null;
	}

	public int hashCode() {
		return this.project.hashCode();
	}

	/**
	 * Answers true if the project potentially contains any source. A project which has no source is immutable.
	 * @return boolean
	 */
	public boolean hasSource() {

		// look if any source folder on the classpath
		// no need for resolved path given source folder cannot be abstracted
		IIncludePathEntry[] entries;
		try {
			entries = this.getRawIncludepath();
		} catch (JavaScriptModelException e) {
			return true; // unsure
		}
		for (int i = 0, max = entries.length; i < max; i++) {
			if (entries[i].getEntryKind() == IIncludePathEntry.CPE_SOURCE) {
				return true;
			}
		}
		return false;
	}



	/*
	 * @see IJavaScriptProject
	 */
	public boolean isOnIncludepath(IJavaScriptElement element) {
		IIncludePathEntry[] rawClasspath;
		try {
			rawClasspath = getRawIncludepath();
		} catch(JavaScriptModelException e){
			return false; // not a Java project
		}
		int elementType = element.getElementType();
		boolean isPackageFragmentRoot = false;
		boolean isFolderPath = false;
		boolean isSource = false;
		switch (elementType) {
			case IJavaScriptElement.JAVASCRIPT_MODEL:
				return false;
			case IJavaScriptElement.JAVASCRIPT_PROJECT:
				break;
			case IJavaScriptElement.PACKAGE_FRAGMENT_ROOT:
				isPackageFragmentRoot = true;
				break;
			case IJavaScriptElement.PACKAGE_FRAGMENT:
				isFolderPath = !((IPackageFragmentRoot)element.getParent()).isArchive();
				break;
			case IJavaScriptElement.JAVASCRIPT_UNIT:
				isSource = true;
				break;
			default:
				isSource = element.getAncestor(IJavaScriptElement.JAVASCRIPT_UNIT) != null;
				break;
		}
		IPath elementPath = element.getPath();

		// first look at unresolved entries
		int length = rawClasspath.length;
		for (int i = 0; i < length; i++) {
			IIncludePathEntry entry = rawClasspath[i];
			switch (entry.getEntryKind()) {
				case IIncludePathEntry.CPE_LIBRARY:
				case IIncludePathEntry.CPE_PROJECT:
				case IIncludePathEntry.CPE_SOURCE:
					if (isOnClasspathEntry(elementPath, isFolderPath, isPackageFragmentRoot, entry))
						return true;
					break;
			}
		}

		// no need to go further for compilation units and elements inside a compilation unit
		// it can only be in a source folder, thus on the raw classpath
		if (isSource)
			return false;

		// then look at resolved entries
		for (int i = 0; i < length; i++) {
			IIncludePathEntry rawEntry = rawClasspath[i];
			switch (rawEntry.getEntryKind()) {
				case IIncludePathEntry.CPE_CONTAINER:
					IJsGlobalScopeContainer container;
					try {
						container = JavaScriptCore.getJsGlobalScopeContainer(rawEntry.getPath(), this);
					} catch (JavaScriptModelException e) {
						break;
					}
					if (container == null)
						break;
					IIncludePathEntry[] containerEntries = container.getIncludepathEntries();
					if (containerEntries == null)
						break;
					// container was bound
					for (int j = 0, containerLength = containerEntries.length; j < containerLength; j++){
						IIncludePathEntry resolvedEntry = containerEntries[j];
						if (resolvedEntry == null) {
							if (JavaModelManager.CP_RESOLVE_VERBOSE) {
								JavaModelManager.getJavaModelManager().verbose_missbehaving_container(this, rawEntry.getPath(), containerEntries);
							}
							return false;
						}
						if (isOnClasspathEntry(elementPath, isFolderPath, isPackageFragmentRoot, resolvedEntry))
							return true;
					}
					break;
				case IIncludePathEntry.CPE_VARIABLE:
					IIncludePathEntry resolvedEntry = JavaScriptCore.getResolvedIncludepathEntry(rawEntry);
					if (resolvedEntry == null)
						break;
					if (isOnClasspathEntry(elementPath, isFolderPath, isPackageFragmentRoot, resolvedEntry))
						return true;
					break;
			}
		}

		return false;
	}

	/*
	 * @see IJavaScriptProject
	 */
	public boolean isOnIncludepath(IResource resource) {
		IPath exactPath = resource.getLocation();
		IPath path = resource.getFullPath();

		// ensure that folders are only excluded if all of their children are excluded
		int resourceType = resource.getType();
		boolean isFolderPath = resourceType == IResource.FOLDER || resourceType == IResource.PROJECT;

		IIncludePathEntry[] classpath;
		try {
			classpath = this.getResolvedClasspath();
		} catch(JavaScriptModelException e){
			return false; // not a Java project
		}
		for (int i = 0; i < classpath.length; i++) {
			IIncludePathEntry entry = classpath[i];
			IPath entryPath = entry.getPath();
			if (entryPath.equals(exactPath) || entryPath.equals(exactPath)) { // package fragment roots must match exactly entry pathes (no exclusion there)
				return true;
			}
			if (entryPath.isPrefixOf(path)
					&& !Util.isExcluded(path, ((ClasspathEntry)entry).fullInclusionPatternChars(), ((ClasspathEntry)entry).fullExclusionPatternChars(), isFolderPath)) {
				return true;
			}
		}
		return false;
	}

	private boolean isOnClasspathEntry(IPath elementPath, boolean isFolderPath, boolean isPackageFragmentRoot, IIncludePathEntry entry) {
		IPath entryPath = entry.getPath();
		if (isPackageFragmentRoot) {
			// package fragment roots must match exactly entry pathes (no exclusion there)
			if (entryPath.equals(elementPath))
				return true;
		} else {
			if (entryPath.isPrefixOf(elementPath)
					&& !Util.isExcluded(elementPath, ((ClasspathEntry)entry).fullInclusionPatternChars(), ((ClasspathEntry)entry).fullExclusionPatternChars(), isFolderPath))
				return true;
		}
		return false;
	}

	/**
	 * load preferences from a shareable format (VCM-wise)
	 */
	 public Preferences loadPreferences() {

	 	Preferences preferences = new Preferences();
	 	IPath projectMetaLocation = getPluginWorkingLocation();
		if (projectMetaLocation != null) {
			File prefFile = projectMetaLocation.append(PREF_FILENAME).toFile();
			if (prefFile.exists()) { // load preferences from file
				InputStream in = null;
				try {
					in = new BufferedInputStream(new FileInputStream(prefFile));
					preferences.load(in);
				} catch (IOException e) { // problems loading preference store - quietly ignore
				} finally {
					if (in != null) {
						try {
							in.close();
						} catch (IOException e) { // ignore problems with close
						}
					}
				}
				// one shot read, delete old preferences
				prefFile.delete();
				return preferences;
			}
		}
		return null;
	 }

	/**
	 * @see IJavaScriptProject#newEvaluationContext()
	 */
	public IEvaluationContext newEvaluationContext() {
//		EvaluationContext context = new EvaluationContext();
//		context.setLineSeparator(Util.getLineSeparator(null/*no existing source*/, this));
//		return new EvaluationContextWrapper(context, this);
		return null;
	}

	/*
	 * Returns a new name lookup. This name lookup first looks in the given working copies.
	 */
	public NameLookup newNameLookup(IJavaScriptUnit[] workingCopies) throws JavaScriptModelException {
		return getJavaProjectElementInfo().newNameLookup(this, workingCopies);
	}

	/*
	 * Returns a new name lookup. This name lookup first looks in the working copies of the given owner.
	 */
	public NameLookup newNameLookup(WorkingCopyOwner owner) throws JavaScriptModelException {

		JavaModelManager manager = JavaModelManager.getJavaModelManager();
		IJavaScriptUnit[] workingCopies = owner == null ? null : manager.getWorkingCopies(owner, true/*add primary WCs*/);
		return newNameLookup(workingCopies);
	}

	/*
	 * Returns a new search name environment for this project. This name environment first looks in the given working copies.
	 */
	public SearchableEnvironment newSearchableNameEnvironment(IJavaScriptUnit[] workingCopies) throws JavaScriptModelException {
		return new SearchableEnvironment(this, workingCopies);
	}

	/*
	 * Returns a new search name environment for this project. This name environment first looks in the working copies
	 * of the given owner.
	 */
	public SearchableEnvironment newSearchableNameEnvironment(WorkingCopyOwner owner) throws JavaScriptModelException {
		return new SearchableEnvironment(this, owner);
	}

	/**
	 * @see IJavaScriptProject
	 */
	public ITypeHierarchy newTypeHierarchy(
		IRegion region,
		IProgressMonitor monitor)
		throws JavaScriptModelException {

		return newTypeHierarchy(region, DefaultWorkingCopyOwner.PRIMARY, monitor);
	}

	/**
	 * @see IJavaScriptProject
	 */
	public ITypeHierarchy newTypeHierarchy(
		IRegion region,
		WorkingCopyOwner owner,
		IProgressMonitor monitor)
		throws JavaScriptModelException {

		if (region == null) {
			throw new IllegalArgumentException(Messages.hierarchy_nullRegion);
		}
		IJavaScriptUnit[] workingCopies = JavaModelManager.getJavaModelManager().getWorkingCopies(owner, true/*add primary working copies*/);
		CreateTypeHierarchyOperation op =
			new CreateTypeHierarchyOperation(region, workingCopies, null, true);
		op.runOperation(monitor);
		return op.getResult();
	}

	/**
	 * @see IJavaScriptProject
	 */
	public ITypeHierarchy newTypeHierarchy(
		IType type,
		IRegion region,
		IProgressMonitor monitor)
		throws JavaScriptModelException {

		return newTypeHierarchy(type, region, DefaultWorkingCopyOwner.PRIMARY, monitor);
	}

	/**
	 * @see IJavaScriptProject
	 */
	public ITypeHierarchy newTypeHierarchy(
		IType type,
		IRegion region,
		WorkingCopyOwner owner,
		IProgressMonitor monitor)
		throws JavaScriptModelException {

		if (type == null) {
			throw new IllegalArgumentException(Messages.hierarchy_nullFocusType);
		}
		if (region == null) {
			throw new IllegalArgumentException(Messages.hierarchy_nullRegion);
		}
		IJavaScriptUnit[] workingCopies = JavaModelManager.getJavaModelManager().getWorkingCopies(owner, true/*add primary working copies*/);
		CreateTypeHierarchyOperation op =
			new CreateTypeHierarchyOperation(region, workingCopies, type, true/*compute subtypes*/);
		op.runOperation(monitor);
		return op.getResult();
	}
	public String[] projectPrerequisites(IIncludePathEntry[] entries)
		throws JavaScriptModelException {

		ArrayList prerequisites = new ArrayList();
		// need resolution
		entries = resolveClasspath(entries);
		for (int i = 0, length = entries.length; i < length; i++) {
			IIncludePathEntry entry = entries[i];
			if (entry.getEntryKind() == IIncludePathEntry.CPE_PROJECT) {
				prerequisites.add(entry.getPath().lastSegment());
			}
		}
		int size = prerequisites.size();
		if (size == 0) {
			return NO_PREREQUISITES;
		} else {
			String[] result = new String[size];
			prerequisites.toArray(result);
			return result;
		}
	}

	/*
	 * Reads the classpath file entries of this project's .classpath file.
	 * This includes the output entry.
	 * As a side effect, unknown elements are stored in the given map (if not null)
	 * Throws exceptions if the file cannot be accessed or is malformed.
	 */
	public IIncludePathEntry[] readFileEntriesWithException(Map unknownElements) throws CoreException, IOException, AssertionFailedException {
		String xmlClasspath;
		IFile rscFile = getJSDTScopeFile();
		if (rscFile.exists()) {
			byte[] bytes = Util.getResourceContentsAsByteArray(rscFile);
			try {
				xmlClasspath = new String(bytes, org.eclipse.wst.jsdt.internal.compiler.util.Util.UTF_8); // .classpath always encoded with UTF-8
			} catch (UnsupportedEncodingException e) {
				Util.log(e, "Could not read .jsdtscope with UTF-8 encoding"); //$NON-NLS-1$
				// fallback to default
				xmlClasspath = new String(bytes);
			}
		} else {
			// when a project is imported, we get a first delta for the addition of the .project, but the .classpath is not accessible
			// so default to using java.io.File
			// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=96258
			URI location = rscFile.getLocationURI();
			if (location == null)
				throw new IOException("Cannot obtain a location URI for " + rscFile); //$NON-NLS-1$
			File file = Util.toLocalFile(location, null/*no progress monitor available*/);
			if (file == null)
				throw new IOException("Unable to fetch file from " + location); //$NON-NLS-1$
			byte[] bytes;
			try {
				bytes = org.eclipse.wst.jsdt.internal.compiler.util.Util.getFileByteContent(file);
			} catch (IOException e) {
				if (!file.exists())
					return defaultClasspath();
				throw e;
			}
			try {
				xmlClasspath = new String(bytes, org.eclipse.wst.jsdt.internal.compiler.util.Util.UTF_8); // .classpath always encoded with UTF-8
			} catch (UnsupportedEncodingException e) {
				Util.log(e, "Could not read .jsdtscope with UTF-8 encoding"); //$NON-NLS-1$
				// fallback to default
				xmlClasspath = new String(bytes);
			}
		}
		return decodeClasspath(xmlClasspath, unknownElements);
	}

	/*
	 * Reads the classpath file entries of this project's .classpath file.
	 * This includes the output entry.
	 * As a side effect, unknown elements are stored in the given map (if not null)
	 */
	private IIncludePathEntry[] readFileEntries(Map unkwownElements) {
		try {
			return readFileEntriesWithException(unkwownElements);
		} catch (CoreException e) {
			Util.log(e, "Exception while reading " + getPath().append(JavaProject.CLASSPATH_FILENAME)); //$NON-NLS-1$
			return JavaProject.INVALID_CLASSPATH;
		} catch (IOException e) {
			Util.log(e, "Exception while reading " + getPath().append(JavaProject.CLASSPATH_FILENAME)); //$NON-NLS-1$
			return JavaProject.INVALID_CLASSPATH;
		} catch (AssertionFailedException e) {
			Util.log(e, "Exception while reading " + getPath().append(JavaProject.CLASSPATH_FILENAME)); //$NON-NLS-1$
			return JavaProject.INVALID_CLASSPATH;
		}
	}

	/**
	 * @see IJavaScriptProject
	 */
	public IPath readOutputLocation() {
		// Read classpath file without creating markers nor logging problems
		IIncludePathEntry[] classpath = readFileEntries(null/*not interested in unknown elements*/);
		if (classpath == JavaProject.INVALID_CLASSPATH)
			return defaultOutputLocation();

		// extract the output location
		IPath outputLocation = null;
		if (classpath.length > 0) {
			IIncludePathEntry entry = classpath[classpath.length - 1];
			if (entry.getContentKind() == ClasspathEntry.K_OUTPUT) {
				outputLocation = entry.getPath();
			}
		}
		return outputLocation;
	}

	/**
	 * @see IJavaScriptProject
	 */
	public IIncludePathEntry[] readRawIncludepath() {
		// Read classpath file without creating markers nor logging problems
		IIncludePathEntry[] classpath = readFileEntries(null/*not interested in unknown elements*/);
		if (classpath == JavaProject.INVALID_CLASSPATH)
			return defaultClasspath();

		// discard the output location
		if (classpath.length > 0) {
			IIncludePathEntry entry = classpath[classpath.length - 1];
			if (entry.getContentKind() == ClasspathEntry.K_OUTPUT) {
				IIncludePathEntry[] copy = new IIncludePathEntry[classpath.length - 1];
				System.arraycopy(classpath, 0, copy, 0, copy.length);
				classpath = copy;
			}
		}
		return classpath;
	}

	/**
	 * Removes the given builder from the build spec for the given project.
	 */
	public void removeFromBuildSpec(String builderID) throws CoreException {

		IProjectDescription description = this.project.getDescription();
		ICommand[] commands = description.getBuildSpec();
		for (int i = 0; i < commands.length; ++i) {
			if (commands[i].getBuilderName().equals(builderID)) {
				ICommand[] newCommands = new ICommand[commands.length - 1];
				System.arraycopy(commands, 0, newCommands, 0, i);
				System.arraycopy(commands, i + 1, newCommands, i, commands.length - i - 1);
				description.setBuildSpec(newCommands);
				this.project.setDescription(description, null);
				return;
			}
		}
	}

	/*
	 * Resets this project's caches
	 */
	public void resetCaches() {
		JavaProjectElementInfo info = (JavaProjectElementInfo) JavaModelManager.getJavaModelManager().peekAtInfo(this);
		if (info != null){
			info.resetCaches();
		}
	}

	/*
	 * Resolve the given raw classpath.
	 */
	public IIncludePathEntry[] resolveClasspath(IIncludePathEntry[] rawClasspath) throws JavaScriptModelException {
		ArrayList resolvedEntries = new ArrayList();
		for (int i = 0, length = rawClasspath.length; i < length; i++) {
			IIncludePathEntry rawEntry = rawClasspath[i];
			switch (rawEntry.getEntryKind()){
				case IIncludePathEntry.CPE_VARIABLE:
					IIncludePathEntry resolvedEntry = null;
					try {
						resolvedEntry = JavaScriptCore.getResolvedIncludepathEntry(rawEntry);
					} catch (AssertionFailedException e) {
						// Catch the assertion failure
						// see bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=55992
						break;
					}
					if (resolvedEntry != null)
						resolvedEntries.add(resolvedEntry);
					break;
				case IIncludePathEntry.CPE_CONTAINER:
					IJsGlobalScopeContainer container = JavaScriptCore.getJsGlobalScopeContainer(rawEntry.getPath(), this);
					if (container == null)
						break;
					IIncludePathEntry[] containerEntries = container.getIncludepathEntries();
					if (containerEntries == null)
						break;

					// container was bound
					for (int j = 0, containerLength = containerEntries.length; j < containerLength; j++){
						ClasspathEntry cEntry = (ClasspathEntry) containerEntries[j];
						if (cEntry == null) {
							if (JavaModelManager.CP_RESOLVE_VERBOSE) {
								JavaModelManager.getJavaModelManager().verbose_missbehaving_container(this, rawEntry.getPath(), containerEntries);
							}
							break;
						}
						// if container is exported or restricted, then its nested entries must in turn be exported  (21749) and/or propagate restrictions
						cEntry = cEntry.combineWith((ClasspathEntry) rawEntry);
						resolvedEntries.add(cEntry);
					}
					break;
				default:
					resolvedEntries.add(rawEntry);
			}
		}
		IIncludePathEntry[] result = new IIncludePathEntry[resolvedEntries.size()];
		resolvedEntries.toArray(result);
		return result;
	}

	/*
	 * Resolve the given perProjectInfo's raw classpath and store the resolved classpath in the perProjectInfo.
	 */
	public void resolveClasspath(PerProjectInfo perProjectInfo) throws JavaScriptModelException {
		JavaModelManager manager = JavaModelManager.getJavaModelManager();
		try {
			manager.setClasspathBeingResolved(this, true);

			// get raw info inside a synchronized block to ensure that it is consistent
			IIncludePathEntry[] rawClasspath;
			IPath outputLocation;
			IJavaScriptModelStatus rawClasspathStatus;
			synchronized (perProjectInfo) {
				rawClasspath= perProjectInfo.rawClasspath;
				if (rawClasspath == null)
					rawClasspath = perProjectInfo.readAndCacheClasspath(this);
				outputLocation = perProjectInfo.outputLocation;
				rawClasspathStatus = perProjectInfo.rawClasspathStatus;
			}

			IJavaScriptModelStatus unresolvedEntryStatus = JavaModelStatus.VERIFIED_OK;
			HashMap rawReverseMap = new HashMap();
			Map rootPathToResolvedEntries = new HashMap();

			ArrayList resolvedEntries = new ArrayList();
			int length = rawClasspath.length;
			for (int i = 0; i < length; i++) {

				IIncludePathEntry rawEntry = rawClasspath[i];
				IPath resolvedPath;

				switch (rawEntry.getEntryKind()){

					case IIncludePathEntry.CPE_VARIABLE :
						IIncludePathEntry resolvedEntry = null;
						try {
							resolvedEntry = JavaScriptCore.getResolvedIncludepathEntry(rawEntry);
						} catch (AssertionFailedException e) {
							// Catch the assertion failure and set ststus instead
							// see bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=55992
							unresolvedEntryStatus = new JavaModelStatus(IJavaScriptModelStatusConstants.INVALID_PATH, e.getMessage());
							break;
						}
						if (resolvedEntry == null) {
							unresolvedEntryStatus = new JavaModelStatus(IJavaScriptModelStatusConstants.CP_VARIABLE_PATH_UNBOUND, this, rawEntry.getPath());
						} else {
							if (rawReverseMap.get(resolvedPath = resolvedEntry.getPath()) == null) {
								rawReverseMap.put(resolvedPath , rawEntry);
								rootPathToResolvedEntries.put(resolvedPath, resolvedEntry);
							}
							resolvedEntries.add(resolvedEntry);
						}
						break;

					case IIncludePathEntry.CPE_CONTAINER :
						IJsGlobalScopeContainer container = JavaScriptCore.getJsGlobalScopeContainer(rawEntry.getPath(), this);
						if (container == null){
							unresolvedEntryStatus = new JavaModelStatus(IJavaScriptModelStatusConstants.CP_CONTAINER_PATH_UNBOUND, this, rawEntry.getPath());
							break;
						}

						IIncludePathEntry[] containerEntries = container.getIncludepathEntries();
						if (containerEntries == null) break;

						// container was bound
						for (int j = 0, containerLength = containerEntries.length; j < containerLength; j++){
							ClasspathEntry cEntry = (ClasspathEntry) containerEntries[j];
							if (cEntry == null) {
								if (JavaModelManager.CP_RESOLVE_VERBOSE) {
									JavaModelManager.getJavaModelManager().verbose_missbehaving_container(this, rawEntry.getPath(), containerEntries);
								}
								break;
							}
							// if container is exported or restricted, then its nested entries must in turn be exported  (21749) and/or propagate restrictions
							cEntry = cEntry.combineWith((ClasspathEntry) rawEntry);
							if (rawReverseMap.get(resolvedPath = cEntry.getPath()) == null) {
								rawReverseMap.put(resolvedPath , rawEntry);
								rootPathToResolvedEntries.put(resolvedPath, cEntry);
							}
							resolvedEntries.add(cEntry);
						}
						break;

					default :
						if (rawReverseMap.get(resolvedPath = rawEntry.getPath()) == null) {
							rawReverseMap.put(resolvedPath , rawEntry);
							rootPathToResolvedEntries.put(resolvedPath, rawEntry);
						}
						resolvedEntries.add(rawEntry);

				}
			}

			// store resolved info along with the raw info to ensure consistency
			IIncludePathEntry[] resolvedClasspath = new IIncludePathEntry[resolvedEntries.size()];
			resolvedEntries.toArray(resolvedClasspath);
			perProjectInfo.setClasspath(rawClasspath, outputLocation, rawClasspathStatus, resolvedClasspath, rawReverseMap, rootPathToResolvedEntries, unresolvedEntryStatus);
		} finally {
			manager.setClasspathBeingResolved(this, false);
		}
	}

	/**
	 * Answers an ID which is used to distinguish project/entries during package
	 * fragment root computations
	 * @return String
	 */
	public String rootID(){
		return "[PRJ]"+this.project.getFullPath(); //$NON-NLS-1$
	}

	/**
	 * Saves the classpath in a shareable format (VCM-wise) only when necessary, that is, if  it is semantically different
	 * from the existing one in file. Will never write an identical one.
	 *
	 * @param newClasspath IIncludePathEntry[]
	 * @param newOutputLocation IPath
	 * @return boolean Return whether the .classpath file was modified.
	 * @throws JavaScriptModelException
	 */
	public boolean saveClasspath(IIncludePathEntry[] newClasspath, IPath newOutputLocation) throws JavaScriptModelException {

		if (!this.project.isAccessible()) return false;

		Map unknownElements = new HashMap();
		IIncludePathEntry[] fileEntries = readFileEntries(unknownElements);
		if (fileEntries != JavaProject.INVALID_CLASSPATH && areClasspathsEqual(newClasspath, newOutputLocation, fileEntries)) {
			// no need to save it, it is the same
			return false;
		}

		// actual file saving
		try {
			setSharedProperty(getJSDTScopeFile(true).getProjectRelativePath().toString(), encodeClasspath(newClasspath, newOutputLocation, true, unknownElements));
			return true;
		} catch (CoreException e) {
			throw new JavaScriptModelException(e);
		}
	}

	/**
	 * Update the Java command in the build spec (replace existing one if present,
	 * add one first if none).
	 */
	private void setJavaCommand(
		IProjectDescription description,
		ICommand newCommand)
		throws CoreException {

		ICommand[] oldBuildSpec = description.getBuildSpec();
		int oldJavaCommandIndex = getJavaCommandIndex(oldBuildSpec, newCommand.getBuilderName());
		ICommand[] newCommands;

		if (oldJavaCommandIndex == -1) {
			// Add a Java build spec before other builders (1FWJK7I)
			newCommands = new ICommand[oldBuildSpec.length + 1];
			System.arraycopy(oldBuildSpec, 0, newCommands, 1, oldBuildSpec.length);
			newCommands[0] = newCommand;
		} else {
		    oldBuildSpec[oldJavaCommandIndex] = newCommand;
			newCommands = oldBuildSpec;
		}

		// Commit the spec change into the project
		description.setBuildSpec(newCommands);
		this.project.setDescription(description, null);
	}

	/**
	 * @see org.eclipse.wst.jsdt.core.IJavaScriptProject#setOption(java.lang.String, java.lang.String)
	 */
	public void setOption(String optionName, String optionValue) {
		if (!JavaModelManager.getJavaModelManager().optionNames.contains(optionName)) return; // unrecognized option
		if (optionValue == null) return; // invalid value
		IEclipsePreferences projectPreferences = getEclipsePreferences();
		String defaultValue = JavaScriptCore.getOption(optionName);
		if (optionValue.equals(defaultValue)) {
			// set default value => remove preference
			projectPreferences.remove(optionName);
		} else {
			projectPreferences.put(optionName, optionValue);
		}

		// Dump changes
		try {
			projectPreferences.flush();
		} catch (BackingStoreException e) {
			// problem with pref store - quietly ignore
		}
	}

	/**
	 * @see org.eclipse.wst.jsdt.core.IJavaScriptProject#setOptions(Map)
	 */
	public void setOptions(Map newOptions) {

		IEclipsePreferences projectPreferences = getEclipsePreferences();
		try {
			if (newOptions == null){
				projectPreferences.clear();
			} else {
				Iterator entries = newOptions.entrySet().iterator();
				while (entries.hasNext()){
					Map.Entry entry = (Map.Entry) entries.next();
					String key = (String) entry.getKey();
					if (!JavaModelManager.getJavaModelManager().optionNames.contains(key)) continue; // unrecognized option
					// no filtering for encoding (custom encoding for project is allowed)
					projectPreferences.put(key, (String) entry.getValue());
				}

				// reset to default all options not in new map
				// @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=26255
				// @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=49691
				String[] pNames = projectPreferences.keys();
				int ln = pNames.length;
				for (int i=0; i<ln; i++) {
					String key = pNames[i];
					if (!newOptions.containsKey(key)) {
						projectPreferences.remove(key); // old preferences => remove from preferences table
					}
				}
			}

			// persist options
			projectPreferences.flush();

			// flush cache immediately
			try {
				getPerProjectInfo().options = null;
			} catch (JavaScriptModelException e) {
				// do nothing
			}
		} catch (BackingStoreException e) {
			// problem with pref store - quietly ignore
		}
	}
	/**
	 * @see IJavaScriptProject
	 */
	public void setOutputLocation(IPath path, IProgressMonitor monitor) throws JavaScriptModelException {
		if (path == null) {
			throw new IllegalArgumentException(Messages.path_nullPath);
		}
		if (path.equals(getOutputLocation())) {
			return;
		}
		setRawIncludepath(getRawIncludepath(), path, monitor);
	}

	/**
	 * Sets the underlying kernel project of this Java project,
	 * and fills in its parent and name.
	 * Called by IProject.getNature().
	 *
	 * @see IProjectNature#setProject(IProject)
	 */
	public void setProject(IProject project) {

		this.project = project;
		this.parent = JavaModelManager.getJavaModelManager().getJavaModel();
		/* Make sure the scope file is properly set. */
		getJSDTScopeFile(true);
	}

	/**
	 * @see IJavaScriptProject#setRawIncludepath(IIncludePathEntry[],boolean,IProgressMonitor)
	 */
	public void setRawIncludepath(
		IIncludePathEntry[] entries,
		boolean canModifyResources,
		IProgressMonitor monitor)
		throws JavaScriptModelException {

		setRawIncludepath(
			entries,
			getOutputLocation()/*don't change output*/,
			canModifyResources,
			monitor);
	}

	/**
	 * @see IJavaScriptProject#setRawIncludepath(IIncludePathEntry[],IPath,boolean,IProgressMonitor)
	 */
	public void setRawIncludepath(
			IIncludePathEntry[] newRawClasspath,
			IPath newOutputLocation,
			boolean canModifyResources,
			IProgressMonitor monitor)
			throws JavaScriptModelException {

		try {
			if (newRawClasspath == null) //are we already with the default classpath
				newRawClasspath = defaultClasspath();

			SetClasspathOperation op =
				new SetClasspathOperation(
					this,
					newRawClasspath,
					newOutputLocation,
					canModifyResources);
			op.runOperation(monitor);
		} catch (JavaScriptModelException e) {
			JavaModelManager.getJavaModelManager().getDeltaProcessor().flush();
			throw e;
		}
	}

	/**
	 * @see IJavaScriptProject#setRawIncludepath(IIncludePathEntry[],IPath,IProgressMonitor)
	 */
	public void setRawIncludepath(
		IIncludePathEntry[] entries,
		IPath outputLocation,
		IProgressMonitor monitor)
		throws JavaScriptModelException {

		setRawIncludepath(
			entries,
			outputLocation,
			true/*can change resource (as per API contract)*/,
			monitor);
	}

	/**
	 * @see IJavaScriptProject
	 */
	public void setRawIncludepath(
		IIncludePathEntry[] entries,
		IProgressMonitor monitor)
		throws JavaScriptModelException {

		setRawIncludepath(
			entries,
			getOutputLocation()/*don't change output*/,
			true/*can change resource (as per API contract)*/,
			monitor);
	}

	/**
	 * Record a shared persistent property onto a project.
	 * Note that it is orthogonal to IResource persistent properties, and client code has to decide
	 * which form of storage to use appropriately. Shared properties produce real resource files which
	 * can be shared through a VCM onto a server. Persistent properties are not shareable.
	 *
	 * shared properties end up in resource files, and thus cannot be modified during
	 * delta notifications (a CoreException would then be thrown).
	 *
	 * @param key String
	 * @param value String
	 * @see JavaProject#getSharedProperty(String key)
	 * @throws CoreException
	 */
	public void setSharedProperty(String key, String value) throws CoreException {

		//IFile rscFile = this.project.getFile(key);
		IFile rscFile = this.project.getFile(key);
		byte[] bytes = null;
		try {
			bytes = value.getBytes(org.eclipse.wst.jsdt.internal.compiler.util.Util.UTF_8); // .classpath always encoded with UTF-8
		} catch (UnsupportedEncodingException e) {
			Util.log(e, "Could not write .jsdtscope with UTF-8 encoding "); //$NON-NLS-1$
			// fallback to default
			bytes = value.getBytes();
		}
		InputStream inputStream = new ByteArrayInputStream(bytes);
		// update the resource content
		if (rscFile.exists()) {
			if (rscFile.isReadOnly()) {
				// provide opportunity to checkout read-only .classpath file (23984)
				ResourcesPlugin.getWorkspace().validateEdit(new IFile[]{rscFile}, null);
			}
			rscFile.setContents(inputStream, IResource.FORCE, null);
		} else {
			rscFile.create(inputStream, IResource.FORCE, null);
		}
	}

	/**
	 * If a cycle is detected, then cycleParticipants contains all the paths of projects involved in this cycle (directly and indirectly),
	 * no cycle if the set is empty (and started empty)
	 * @param prereqChain ArrayList
	 * @param cycleParticipants HashSet
	 * @param workspaceRoot IWorkspaceRoot
	 * @param traversed HashSet
	 * @param preferredClasspaths Map
	 */
	public void updateCycleParticipants(
			ArrayList prereqChain,
			HashSet cycleParticipants,
			IWorkspaceRoot workspaceRoot,
			HashSet traversed,
			Map preferredClasspaths){

		IPath path = this.getPath();
		prereqChain.add(path);
		traversed.add(path);
		try {
			IIncludePathEntry[] classpath = null;
			if (preferredClasspaths != null) classpath = (IIncludePathEntry[])preferredClasspaths.get(this);
			if (classpath == null) classpath = getResolvedClasspath();
			for (int i = 0, length = classpath.length; i < length; i++) {
				IIncludePathEntry entry = classpath[i];

				if (entry.getEntryKind() == IIncludePathEntry.CPE_PROJECT){
					IPath prereqProjectPath = entry.getPath();
					int index = cycleParticipants.contains(prereqProjectPath) ? 0 : prereqChain.indexOf(prereqProjectPath);
					if (index >= 0) { // refer to cycle, or in cycle itself
						for (int size = prereqChain.size(); index < size; index++) {
							cycleParticipants.add(prereqChain.get(index));
						}
					} else {
						if (!traversed.contains(prereqProjectPath)) {
							IResource member = workspaceRoot.findMember(prereqProjectPath);
							if (member != null && member.getType() == IResource.PROJECT){
								JavaProject javaProject = (JavaProject)JavaScriptCore.create((IProject)member);
								javaProject.updateCycleParticipants(prereqChain, cycleParticipants, workspaceRoot, traversed, preferredClasspaths);
							}
						}
					}
				}
			}
		} catch(JavaScriptModelException e){
			// project doesn't exist: ignore
		}
		prereqChain.remove(path);
	}

	/**
	 * Reset the collection of package fragment roots (local ones) - only if opened.
	 */
	public void updatePackageFragmentRoots(){

			if (this.isOpen()) {
				try {
					JavaProjectElementInfo info = getJavaProjectElementInfo();
					computeChildren(info);
					info.resetCaches(); // discard caches (hold onto roots and pkg fragments)
				} catch(JavaScriptModelException e){
					try {
						close(); // could not do better
					} catch(JavaScriptModelException ex){
						// ignore
					}
				}
			}
	}

	/*
	 * Update eclipse preferences from old preferences.
	 */
	 private void updatePreferences(IEclipsePreferences preferences) {

	 	Preferences oldPreferences = loadPreferences();
	 	if (oldPreferences != null) {
	 		String[] propertyNames = oldPreferences.propertyNames();
			for (int i = 0; i < propertyNames.length; i++){
				String propertyName = propertyNames[i];
			    String propertyValue = oldPreferences.getString(propertyName);
			    if (!"".equals(propertyValue)) { //$NON-NLS-1$
				    preferences.put(propertyName, propertyValue);
			    }
			}
			try {
				// save immediately new preferences
				preferences.flush();
			} catch (BackingStoreException e) {
				// fails silently
			}
		}
	 }
		public LibrarySuperType getCommonSuperType() {
			String superTypeName = null;
			String superTypeContainer = null;



			try {
//				IFolder rscPath = this.project.getFolder(JavaProject.SHARED_PROPERTIES_DIRECTORY);
//				if(!rscPath.exists()) rscPath.create(true, true, new NullProgressMonitor());

				IPath fullPath = new Path(JavaProject.SHARED_PROPERTIES_DIRECTORY);//.append(LibrarySuperType.SUPER_TYPE_NAME);

				superTypeName = getSharedProperty(fullPath.append(LibrarySuperType.SUPER_TYPE_NAME).toString());
				superTypeContainer = getSharedProperty(fullPath.append(LibrarySuperType.SUPER_TYPE_CONTAINER).toString());
			} catch (CoreException ex) {
				// unable to read, probably doesn't exist
			}
			if (superTypeContainer==null || superTypeName==null)
				return null;
			return new LibrarySuperType(new Path(superTypeContainer),this,superTypeName);

		}

		public void setCommonSuperType(LibrarySuperType newSuperType) {
			String superTypeName = newSuperType.getSuperTypeName();
			String superTypeContainer = newSuperType.getRawContainerPath().toString();

			try {
				IFolder rscPath = this.project.getFolder(JavaProject.SHARED_PROPERTIES_DIRECTORY);
				if(!rscPath.exists()) rscPath.create(true, true, new NullProgressMonitor());

				IPath fullPath = new Path(JavaProject.SHARED_PROPERTIES_DIRECTORY);
				//.append(LibrarySuperType.SUPER_TYPE_NAME);

				setSharedProperty(fullPath.append(LibrarySuperType.SUPER_TYPE_NAME).toString(), superTypeName);
				setSharedProperty(fullPath.append(LibrarySuperType.SUPER_TYPE_CONTAINER).toString(), superTypeContainer);
			} catch (CoreException ex) {
				// TODO Auto-generated catch block
				ex.printStackTrace();
			}

		}

		public ITypeRoot findTypeRoot(String fullyQualifiedName) throws JavaScriptModelException {
			
			HandleFactory handleFactory=new HandleFactory();
			Openable openable = handleFactory.createOpenable(fullyQualifiedName, null);
			if (openable instanceof ITypeRoot)
				return (ITypeRoot) openable;
			return null;
//			IPath path = new Path(fullyQualifiedName);
//			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
//			if (root.getLocation().isPrefixOf(path))
//				path=path.removeFirstSegments(root.getLocation().segmentCount());
//			IResource resource = root.findMember(path);
//			if (resource.exists())
//			{
//				 PackageFragmentRoot packageFragmentRoot = (PackageFragmentRoot)getPackageFragmentRoot(path.removeLastSegments(1));
//				path=path.removeFirstSegments(packageFragmentRoot.getPath().segmentCount());
//				String[] simpleNames = path.segments();
//				String[] pkgName;
//				int length = simpleNames.length-1;
//				if (length > 0) {
//					pkgName = new String[length];
//					System.arraycopy(simpleNames, 0, pkgName, 0, length);
//				} else {
//					pkgName = CharOperation.NO_STRINGS;
//				}
//				PackageFragment pkgFragment = packageFragmentRoot.getPackageFragment(pkgName);
//				String simpleName= simpleNames[length];
//				if (org.eclipse.wst.jsdt.internal.core.util.Util.isJavaLikeFileName(simpleName)) {
//					IJavaScriptUnit unit= pkgFragment.getCompilationUnit(simpleName);
//					return  unit;
//				} else {
//					IClassFile classFile= pkgFragment.getClassFile(simpleName);
//					return classFile;
//				}
//			}
//			return null;
		}
}
