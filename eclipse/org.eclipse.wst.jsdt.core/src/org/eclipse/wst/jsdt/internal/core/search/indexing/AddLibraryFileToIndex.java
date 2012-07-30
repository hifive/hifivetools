/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.wst.jsdt.internal.core.search.indexing;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.wst.jsdt.core.search.SearchEngine;
import org.eclipse.wst.jsdt.core.search.SearchParticipant;
import org.eclipse.wst.jsdt.internal.compiler.util.Util;
import org.eclipse.wst.jsdt.internal.core.JavaModelManager;
import org.eclipse.wst.jsdt.internal.core.index.Index;
import org.eclipse.wst.jsdt.internal.core.search.JavaSearchDocument;
import org.eclipse.wst.jsdt.internal.core.search.processing.JobManager;

class AddLibraryFileToIndex extends IndexRequest {

	IPath absolutePath;
	public AddLibraryFileToIndex(IFile resource, IndexManager manager) {
		super(resource.getFullPath(), manager);
		this.absolutePath=resource.getLocation();
	}
	public AddLibraryFileToIndex(IPath jarPath, IndexManager manager) {
		// external JAR scenario - no resource
		super(jarPath, manager);
	}
	public boolean equals(Object o) {
		if (o instanceof AddLibraryFileToIndex) {
			if (this.containerPath != null)
				return this.containerPath.equals(((AddLibraryFileToIndex) o).containerPath);
		}
		return false;
	}
	public int hashCode() {
		if (this.containerPath != null)
			return this.containerPath.hashCode();
		return -1;
	}
	public boolean execute(IProgressMonitor progressMonitor) {

		if (this.isCancelled || progressMonitor != null && progressMonitor.isCanceled()) return true;

		try {
			// if index is already cached, then do not perform any check
			// MUST reset the IndexManager if a jar file is changed
			Index index = this.manager.getIndexForUpdate(this.containerPath, false, /*do not reuse index file*/ false /*do not create if none*/);
			if (index != null) {
				if (JobManager.VERBOSE)
					org.eclipse.wst.jsdt.internal.core.util.Util.verbose("-> no indexing required (index already exists) for " + this.containerPath); //$NON-NLS-1$
				return true;
			}

			index = this.manager.getIndexForUpdate(this.containerPath, true, /*reuse index file*/ true /*create if none*/);
			if (index == null) {
				if (JobManager.VERBOSE)
					org.eclipse.wst.jsdt.internal.core.util.Util.verbose("-> index could not be created for " + this.containerPath); //$NON-NLS-1$
				return true;
			}
			ReadWriteMonitor monitor = index.monitor;
			if (monitor == null) {
				if (JobManager.VERBOSE)
					org.eclipse.wst.jsdt.internal.core.util.Util.verbose("-> index for " + this.containerPath + " just got deleted"); //$NON-NLS-1$//$NON-NLS-2$
				return true; // index got deleted since acquired
			}
			try {
				// this path will be a relative path to the workspace in case the zipfile in the workspace otherwise it will be a path in the
				// local file system
				Path libraryFilePath = null;

				monitor.enterWrite(); // ask permission to write
					if (JavaModelManager.ZIP_ACCESS_VERBOSE)
						System.out.println("(" + Thread.currentThread() + ") [AddJarFileToIndex.execute()] Creating ZipFile on " + this.containerPath); //$NON-NLS-1$	//$NON-NLS-2$
					// external file -> it is ok to use toFile()
					libraryFilePath = (Path) this.containerPath;
					// path is already canonical since coming from a library classpath entry
			
				if (this.isCancelled) {
					if (JobManager.VERBOSE)
						org.eclipse.wst.jsdt.internal.core.util.Util.verbose("-> indexing of " + libraryFilePath.toString() + " has been cancelled"); //$NON-NLS-1$ //$NON-NLS-2$
					return false;
				}
			
				if (JobManager.VERBOSE)
					org.eclipse.wst.jsdt.internal.core.util.Util.verbose("-> indexing " + libraryFilePath.toString()); //$NON-NLS-1$
				long initialTime = System.currentTimeMillis();

//				String[] paths = index.queryDocumentNames(""); // all file names //$NON-NLS-1$
//				if (paths != null) {
//					int max = paths.length;
//					/* check integrity of the existing index file
//					 * if the length is equal to 0, we want to index the whole jar again
//					 * If not, then we want to check that there is no missing entry, if
//					 * one entry is missing then we recreate the index
//					 */
//					String EXISTS = "OK"; //$NON-NLS-1$
//					String DELETED = "DELETED"; //$NON-NLS-1$
//					SimpleLookupTable indexedFileNames = new SimpleLookupTable(max == 0 ? 33 : max + 11);
//					for (int i = 0; i < max; i++)
//						indexedFileNames.put(paths[i], DELETED);
//
//					if (Util.isClassFileName(libraryFilePath.toPortableString()))
//						indexedFileNames.put(libraryFilePath.toPortableString(), EXISTS);
//
//					boolean needToReindex = indexedFileNames.elementSize != max; // a new file was added
//					if (!needToReindex) {
//						Object[] valueTable = indexedFileNames.valueTable;
//						for (int i = 0, l = valueTable.length; i < l; i++) {
//							if (valueTable[i] == DELETED) {
//								needToReindex = true; // a file was deleted so re-index
//								break;
//							}
//						}
//						if (!needToReindex) {
//							if (JobManager.VERBOSE)
//								org.eclipse.wst.jsdt.internal.core.util.Util.verbose("-> no indexing required (index is consistent with library) for " //$NON-NLS-1$
//								+ zip.getName() + " (" //$NON-NLS-1$
//								+ (System.currentTimeMillis() - initialTime) + "ms)"); //$NON-NLS-1$
//							this.manager.saveIndex(index); // to ensure its placed into the saved state
//							return true;
//						}
//					}
//				}
//
				// Index the jar for the first time or reindex the jar in case the previous index file has been corrupted
				// index already existed: recreate it so that we forget about previous entries
				SearchParticipant participant = SearchEngine.getDefaultSearchParticipant();
				index = manager.recreateIndex(this.containerPath);
				if (index == null) {
					// failed to recreate index, see 73330
					manager.removeIndex(this.containerPath);
					return false;
				}

//				for (Enumeration e = zip.entries(); e.hasMoreElements();) {
//					if (this.isCancelled) {
//						if (JobManager.VERBOSE)
//							org.eclipse.wst.jsdt.internal.core.util.Util.verbose("-> indexing of " + zip.getName() + " has been cancelled"); //$NON-NLS-1$ //$NON-NLS-2$
//						return false;
//					}
//
//					// iterate each entry to index it
//					ZipEntry ze = (ZipEntry) e.nextElement();
//					if (Util.isClassFileName(ze.getName())) {
//						final byte[] classFileBytes = org.eclipse.wst.jsdt.internal.compiler.util.Util.getZipEntryByteContent(ze, zip);
//						JavaSearchDocument entryDocument = new JavaSearchDocument(ze, libraryFilePath, classFileBytes, participant);
//						this.manager.indexDocument(entryDocument, participant, index, this.containerPath);
//					}
//				}
				IPath filePath=(this.absolutePath!=null)?this.absolutePath : this.containerPath;
				File file = new File(filePath.toOSString());

				if (file.isFile())
					indexFile(file, participant, index, libraryFilePath);
				else
				{
					indexDirectory(file, participant, index, libraryFilePath);
				}

				this.manager.saveIndex(index);
				if (JobManager.VERBOSE)
					org.eclipse.wst.jsdt.internal.core.util.Util.verbose("-> done indexing of " //$NON-NLS-1$
						+ libraryFilePath.toString() + " (" //$NON-NLS-1$
						+ (System.currentTimeMillis() - initialTime) + "ms)"); //$NON-NLS-1$
			} finally {
				monitor.exitWrite(); // free write lock
			}
		} catch (IOException e) {
			if (JobManager.VERBOSE) {
				org.eclipse.wst.jsdt.internal.core.util.Util.verbose("-> failed to index " + this.containerPath + " because of the following exception:"); //$NON-NLS-1$ //$NON-NLS-2$
				e.printStackTrace();
			}
			manager.removeIndex(this.containerPath);
			return false;
		}
		return true;
	}
	protected Integer updatedIndexState() {
		return IndexManager.REBUILDING_STATE;
	}
	public String toString() {
		return "indexing " + this.containerPath.toString(); //$NON-NLS-1$
	}
	
	private void indexFile(File file,SearchParticipant participant, Index index, IPath libraryFilePath)
	{
		try {
			final char[] classFileChars = org.eclipse.wst.jsdt.internal.compiler.util.Util.getFileCharContent(file,null);
			String packageName="";
			JavaSearchDocument entryDocument = new JavaSearchDocument(  new Path(file.getAbsolutePath()), classFileChars, participant,packageName);
			this.manager.indexDocument(entryDocument, participant, index, this.containerPath);
		} catch (Exception ex)
		{}

	}

	private void indexDirectory(File file,SearchParticipant participant, Index index, IPath libraryFilePath)
	{
		File[] files = file.listFiles();
		if (files!=null)
		 for (int i = 0; i < files.length; i++) {
			if (files[i].isDirectory())
			  indexDirectory(files[i], participant, index, libraryFilePath);
			else if (Util.isClassFileName(files[i].getName()))
				indexFile(files[i], participant, index, libraryFilePath);

		}
	}
	
}
