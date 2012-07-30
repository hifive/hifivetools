/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.wst.jsdt.internal.core.search.indexing;

import java.io.IOException;
import java.net.URI;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.wst.jsdt.core.IIncludePathEntry;
import org.eclipse.wst.jsdt.core.JavaScriptModelException;
import org.eclipse.wst.jsdt.internal.compiler.util.SimpleLookupTable;
import org.eclipse.wst.jsdt.internal.core.ClasspathEntry;
import org.eclipse.wst.jsdt.internal.core.JavaModel;
import org.eclipse.wst.jsdt.internal.core.JavaModelManager;
import org.eclipse.wst.jsdt.internal.core.JavaProject;
import org.eclipse.wst.jsdt.internal.core.index.Index;
import org.eclipse.wst.jsdt.internal.core.search.processing.JobManager;
import org.eclipse.wst.jsdt.internal.core.util.Util;

public class IndexBinaryFolder extends IndexRequest {
	IContainer folder;
	char[][] exclusionPatterns;

	public IndexBinaryFolder(IContainer folder, IndexManager manager) {
		super(folder.getFullPath(), manager);
		this.folder = folder;
		
		// need to check for exclusion patterns
		try {
			JavaModel model = JavaModelManager.getJavaModelManager().getJavaModel();
			JavaProject javaProject = (JavaProject) model.getJavaProject(folder.getProject());
			IIncludePathEntry[] newResolvedClasspath = javaProject.getResolvedClasspath();
			IPath folderPath = folder.getFullPath();
			for (int i = 0; i < newResolvedClasspath.length; i++) {
				boolean found = false;
				// Request indexing
				int entryKind = newResolvedClasspath[i].getEntryKind();
				switch (entryKind) {
					case IIncludePathEntry.CPE_LIBRARY:
						IPath newPath = newResolvedClasspath[i].getPath();
						if(newPath.equals(folderPath)) {
							exclusionPatterns = ((ClasspathEntry)newResolvedClasspath[i]).fullExclusionPatternChars();
							found = true;
						}
						break;
				}
				if(found)
					break;
			}
		} catch (JavaScriptModelException e) {
			// project doesn't exist
			return;
		}
	}
	
	public boolean equals(Object o) {
		if (o instanceof IndexBinaryFolder)
			return this.folder.equals(((IndexBinaryFolder) o).folder);
		return false;
	}
	/**
	 * Ensure consistency of a folder index. Need to walk all nested resources,
	 * and discover resources which have either been changed, added or deleted
	 * since the index was produced.
	 */
	public boolean execute(IProgressMonitor progressMonitor) {

		if (this.isCancelled || progressMonitor != null && progressMonitor.isCanceled()) return true;
		if (!this.folder.isAccessible()) return true; // nothing to do

		Index index = this.manager.getIndexForUpdate(this.containerPath, true, /*reuse index file*/ true /*create if none*/);
		if (index == null) return true;
		ReadWriteMonitor monitor = index.monitor;
		if (monitor == null) return true; // index got deleted since acquired

		try {
			monitor.enterRead(); // ask permission to read

			String[] paths = index.queryDocumentNames(""); // all file names //$NON-NLS-1$
			int max = paths == null ? 0 : paths.length;
			final SimpleLookupTable indexedFileNames = new SimpleLookupTable(max==0 ? 33 : max+11);
			final String OK = "OK"; //$NON-NLS-1$
			final String DELETED = "DELETED"; //$NON-NLS-1$
			if (paths == null) {
				this.folder.accept(new IResourceProxyVisitor() {
					public boolean visit(IResourceProxy proxy) {
						if (isCancelled) return false;
						if (proxy.getType() == IResource.FILE) {
							if (org.eclipse.wst.jsdt.internal.compiler.util.Util.isClassFileName(proxy.getName())) {
								IFile file = (IFile) proxy.requestResource();
								if(exclusionPatterns == null || !Util.isExcluded(file, null, exclusionPatterns)) {
									String containerRelativePath = Util.relativePath(file.getFullPath(), containerPath.segmentCount());
									indexedFileNames.put(containerRelativePath, file);
								}
							}
							return false;
						}
						return true;
					}
				}, IResource.NONE);
			} else {
				for (int i = 0; i < max; i++) {
					indexedFileNames.put(paths[i], DELETED);
				}
				final long indexLastModified = index.getIndexFile().lastModified();
				this.folder.accept(
					new IResourceProxyVisitor() {
						public boolean visit(IResourceProxy proxy) throws CoreException {
							if (isCancelled) return false;
							if (proxy.getType() == IResource.FILE) {
								if (org.eclipse.wst.jsdt.internal.compiler.util.Util.isClassFileName(proxy.getName())) {
									IFile file = (IFile) proxy.requestResource();
									if(exclusionPatterns == null || !Util.isExcluded(file, null, exclusionPatterns)) {
										URI location = file.getLocationURI();
										if (location != null) {
											String containerRelativePath = Util.relativePath(file.getFullPath(), containerPath.segmentCount());
											indexedFileNames.put(containerRelativePath,
												indexedFileNames.get(containerRelativePath) == null
														|| indexLastModified <
														EFS.getStore(location).fetchInfo().getLastModified()
													? (Object) file
													: (Object) OK);
										}
									}
								}
								return false;
							}
							return true;
						}
					},
					IResource.NONE
				);
			}

			Object[] names = indexedFileNames.keyTable;
			Object[] values = indexedFileNames.valueTable;
			for (int i = 0, length = names.length; i < length; i++) {
				String name = (String) names[i];
				if (name != null) {
					if (this.isCancelled) return false;

					Object value = values[i];
					if (value != OK) {
						if (value == DELETED)
							this.manager.remove(name, this.containerPath);
						else {
							this.manager.addBinary((IFile) value, this.containerPath);
						}
					}
				}
			}

			// request to save index when all class files have been indexed... also sets state to SAVED_STATE
			this.manager.request(new SaveIndex(this.containerPath, this.manager));
		} catch (CoreException e) {
			if (JobManager.VERBOSE) {
				Util.verbose("-> failed to index " + this.folder + " because of the following exception:", System.err); //$NON-NLS-1$ //$NON-NLS-2$
				e.printStackTrace();
			}
			this.manager.removeIndex(this.containerPath);
			return false;
		} catch (IOException e) {
			if (JobManager.VERBOSE) {
				Util.verbose("-> failed to index " + this.folder + " because of the following exception:", System.err); //$NON-NLS-1$ //$NON-NLS-2$
				e.printStackTrace();
			}
			this.manager.removeIndex(this.containerPath);
			return false;
		} finally {
			monitor.exitRead(); // free read lock
		}
		return true;
	}
	public int hashCode() {
		return this.folder.hashCode();
	}
	protected Integer updatedIndexState() {
		return IndexManager.REBUILDING_STATE;
	}
	public String toString() {
		return "indexing binary folder " + this.folder.getFullPath(); //$NON-NLS-1$
	}
}
