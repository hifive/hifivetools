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

import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.wst.jsdt.core.IJavaScriptElement;
import org.eclipse.wst.jsdt.core.IJavaScriptModelStatus;
import org.eclipse.wst.jsdt.core.IJavaScriptModelStatusConstants;
import org.eclipse.wst.jsdt.core.IJavaScriptProject;
import org.eclipse.wst.jsdt.core.IJsGlobalScopeContainer;
import org.eclipse.wst.jsdt.core.IPackageFragment;
import org.eclipse.wst.jsdt.core.JavaScriptCore;
import org.eclipse.wst.jsdt.core.JavaScriptModelException;
import org.eclipse.wst.jsdt.core.JsGlobalScopeContainerInitializer;
import org.eclipse.wst.jsdt.internal.core.util.Messages;

/**
 * @see IJavaScriptModelStatus
 */

public class JavaModelStatus extends Status implements IJavaScriptModelStatus, IJavaScriptModelStatusConstants, IResourceStatus {

	/**
	 * The elements related to the failure, or <code>null</code>
	 * if no elements are involved.
	 */
	protected IJavaScriptElement[] elements = new IJavaScriptElement[0];
	/**
	 * The path related to the failure, or <code>null</code>
	 * if no path is involved.
	 */
	protected IPath path;
	/**
	 * The <code>String</code> related to the failure, or <code>null</code>
	 * if no <code>String</code> is involved.
	 */
	protected String string;
	/**
	 * Empty children
	 */
	protected final static IStatus[] NO_CHILDREN = new IStatus[] {};
	protected IStatus[] children= NO_CHILDREN;

	/**
	 * Singleton OK object
	 */
	public static final IJavaScriptModelStatus VERIFIED_OK = new JavaModelStatus(OK, OK, Messages.status_OK);

	/**
	 * Constructs an Java model status with no corresponding elements.
	 */
	public JavaModelStatus() {
		// no code for an multi-status
		super(ERROR, JavaScriptCore.PLUGIN_ID, 0, "JavaScriptModelStatus", null); //$NON-NLS-1$
	}
	/**
	 * Constructs an Java model status with no corresponding elements.
	 */
	public JavaModelStatus(int code) {
		super(ERROR, JavaScriptCore.PLUGIN_ID, code, "JavaScriptModelStatus", null); //$NON-NLS-1$
		this.elements= JavaElement.NO_ELEMENTS;
	}
	/**
	 * Constructs an Java model status with the given corresponding
	 * elements.
	 */
	public JavaModelStatus(int code, IJavaScriptElement[] elements) {
		super(ERROR, JavaScriptCore.PLUGIN_ID, code, "JavaScriptModelStatus", null); //$NON-NLS-1$
		this.elements= elements;
		this.path= null;
	}
	/**
	 * Constructs an Java model status with no corresponding elements.
	 */
	public JavaModelStatus(int code, String string) {
		this(ERROR, code, string);
	}
	/**
	 * Constructs an Java model status with no corresponding elements.
	 */
	public JavaModelStatus(int severity, int code, String string) {
		super(severity, JavaScriptCore.PLUGIN_ID, code, "JavaScriptModelStatus", null); //$NON-NLS-1$
		this.elements= JavaElement.NO_ELEMENTS;
		this.path= null;
		this.string = string;
	}
	/**
	 * Constructs an Java model status with no corresponding elements.
	 */
	public JavaModelStatus(int code, Throwable throwable) {
		super(ERROR, JavaScriptCore.PLUGIN_ID, code, "JavaScriptModelStatus", throwable); //$NON-NLS-1$
		this.elements= JavaElement.NO_ELEMENTS;
	}
	/**
	 * Constructs an Java model status with no corresponding elements.
	 */
	public JavaModelStatus(int code, IPath path) {
		super(ERROR, JavaScriptCore.PLUGIN_ID, code, "JavaScriptModelStatus", null); //$NON-NLS-1$
		this.elements= JavaElement.NO_ELEMENTS;
		this.path= path;
	}
	/**
	 * Constructs an Java model status with the given corresponding
	 * element.
	 */
	public JavaModelStatus(int code, IJavaScriptElement element) {
		this(code, new IJavaScriptElement[]{element});
	}
	/**
	 * Constructs an Java model status with the given corresponding
	 * element and string
	 */
	public JavaModelStatus(int code, IJavaScriptElement element, String string) {
		this(code, new IJavaScriptElement[]{element});
		this.string = string;
	}

	/**
	 * Constructs an Java model status with the given corresponding
	 * element and path
	 */
	public JavaModelStatus(int code, IJavaScriptElement element, IPath path) {
		this(code, new IJavaScriptElement[]{element});
		this.path = path;
	}

	/**
	 * Constructs an Java model status with the given corresponding
	 * element, path and string
	 */
	public JavaModelStatus(int code, IJavaScriptElement element, IPath path, String string) {
		this(code, new IJavaScriptElement[]{element});
		this.path = path;
		this.string = string;
	}

	/**
     * Constructs an Java model status with the given corresponding
     * element and path
     */
    public JavaModelStatus(int severity, int code, IJavaScriptElement element, IPath path, String msg) {
    	super(severity, JavaScriptCore.PLUGIN_ID, code, "JavaScriptModelStatus", null); //$NON-NLS-1$
    	this.elements= new IJavaScriptElement[]{element};
    	this.path = path;
    	this.string = msg;
    }

    /**
	 * Constructs an Java model status with no corresponding elements.
	 */
	public JavaModelStatus(CoreException coreException) {
		super(ERROR, JavaScriptCore.PLUGIN_ID, CORE_EXCEPTION, "JavaScriptModelStatus", coreException); //$NON-NLS-1$
		elements= JavaElement.NO_ELEMENTS;
	}
	protected int getBits() {
		int severity = 1 << (getCode() % 100 / 33);
		int category = 1 << ((getCode() / 100) + 3);
		return severity | category;
	}
	/**
	 * @see IStatus
	 */
	public IStatus[] getChildren() {
		return children;
	}
	/**
	 * @see IJavaScriptModelStatus
	 */
	public IJavaScriptElement[] getElements() {
		return elements;
	}
	/**
	 * Returns the message that is relevant to the code of this status.
	 */
	public String getMessage() {
		Throwable exception = getException();
		if (exception == null) {
			switch (getCode()) {
				case CORE_EXCEPTION :
					return Messages.status_coreException;

				case BUILDER_INITIALIZATION_ERROR:
					return Messages.build_initializationError;

				case BUILDER_SERIALIZATION_ERROR:
					return Messages.build_serializationError;

				case DEVICE_PATH:
					return Messages.bind(Messages.status_cannotUseDeviceOnPath, getPath().toString());

				case DOM_EXCEPTION:
					return Messages.status_JDOMError;

				case ELEMENT_DOES_NOT_EXIST:
					return Messages.bind(Messages.element_doesNotExist, ((JavaElement)elements[0]).toStringWithAncestors());

				case ELEMENT_NOT_ON_CLASSPATH:
					return Messages.bind(Messages.element_notOnClasspath, ((JavaElement)elements[0]).toStringWithAncestors());

				case EVALUATION_ERROR:
					return Messages.bind(Messages.status_evaluationError, string);

				case INDEX_OUT_OF_BOUNDS:
					return Messages.status_indexOutOfBounds;

				case INVALID_CONTENTS:
					return Messages.status_invalidContents;

				case INVALID_DESTINATION:
					return Messages.bind(Messages.status_invalidDestination, ((JavaElement)elements[0]).toStringWithAncestors());

				case INVALID_ELEMENT_TYPES:
					StringBuffer buff= new StringBuffer(Messages.operation_notSupported);
					for (int i= 0; i < elements.length; i++) {
						if (i > 0) {
							buff.append(", "); //$NON-NLS-1$
						}
						buff.append(((JavaElement)elements[i]).toStringWithAncestors());
					}
					return buff.toString();

				case INVALID_NAME:
					return Messages.bind(Messages.status_invalidName, string);

				case INVALID_PACKAGE:
					return Messages.bind(Messages.status_invalidPackage, string);

				case INVALID_PATH:
					if (string != null) {
						return string;
					} else {
						return Messages.bind(
							Messages.status_invalidPath,
							new String[] {getPath() == null ? "null" : getPath().toString()} //$NON-NLS-1$
						);
					}

				case INVALID_PROJECT:
					return Messages.bind(Messages.status_invalidProject, string);

				case INVALID_RESOURCE:
					return Messages.bind(Messages.status_invalidResource, string);

				case INVALID_RESOURCE_TYPE:
					return Messages.bind(Messages.status_invalidResourceType, string);

				case INVALID_SIBLING:
					if (string != null) {
						return Messages.bind(Messages.status_invalidSibling, string);
					} else {
						return Messages.bind(Messages.status_invalidSibling, ((JavaElement)elements[0]).toStringWithAncestors());
					}

				case IO_EXCEPTION:
					return Messages.status_IOException;

				case NAME_COLLISION:
					if (elements != null && elements.length > 0) {
						IJavaScriptElement element = elements[0];
						if (element instanceof PackageFragment && ((PackageFragment) element).isDefaultPackage()) {
							return Messages.operation_cannotRenameDefaultPackage;
						}
					}
					if (string != null) {
						return string;
					} else {
						return Messages.bind(Messages.status_nameCollision, "");  //$NON-NLS-1$
					}
				case NO_ELEMENTS_TO_PROCESS:
					return Messages.operation_needElements;

				case NULL_NAME:
					return Messages.operation_needName;

				case NULL_PATH:
					return Messages.operation_needPath;

				case NULL_STRING:
					return Messages.operation_needString;

				case PATH_OUTSIDE_PROJECT:
					return Messages.bind(Messages.operation_pathOutsideProject, new String[] {string, ((JavaElement)elements[0]).toStringWithAncestors()});

				case READ_ONLY:
					IJavaScriptElement element = elements[0];
					String name = element.getElementName();
					if (element instanceof IPackageFragment && name.equals(IPackageFragment.DEFAULT_PACKAGE_NAME)) {
						return Messages.status_defaultPackageReadOnly;
					}
					return Messages.bind(Messages.status_readOnly, name);

				case RELATIVE_PATH:
					return Messages.bind(Messages.operation_needAbsolutePath, getPath().toString());

				case TARGET_EXCEPTION:
					return Messages.status_targetException;

				case UPDATE_CONFLICT:
					return Messages.status_updateConflict;

				case NO_LOCAL_CONTENTS :
					return Messages.bind(Messages.status_noLocalContents, getPath().toString());

				case CP_CONTAINER_PATH_UNBOUND:
					IJavaScriptProject javaProject = (IJavaScriptProject)elements[0];
					JsGlobalScopeContainerInitializer initializer = JavaScriptCore.getJsGlobalScopeContainerInitializer(this.path.segment(0));
					String description = null;
					if (initializer != null) description = initializer.getDescription(this.path, javaProject);
					if (description == null) description = path.makeRelative().toString();
					return Messages.bind(Messages.classpath_unboundContainerPath, new String[] {description, javaProject.getElementName()});

				case INVALID_CP_CONTAINER_ENTRY:
					javaProject = (IJavaScriptProject)elements[0];
					IJsGlobalScopeContainer container = null;
					description = null;
					try {
						container = JavaScriptCore.getJsGlobalScopeContainer(path, javaProject);
					} catch(JavaScriptModelException e){
						// project doesn't exist: ignore
					}
					if (container == null) {
						 initializer = JavaScriptCore.getJsGlobalScopeContainerInitializer(path.segment(0));
						if (initializer != null) description = initializer.getDescription(path, javaProject);
					} else {
						description = container.getDescription();
					}
					if (description == null) description = path.makeRelative().toString();
					return Messages.bind(Messages.classpath_invalidContainer, new String[] {description, javaProject.getElementName()});

				case CP_VARIABLE_PATH_UNBOUND:
					javaProject = (IJavaScriptProject)elements[0];
					return Messages.bind(Messages.classpath_unboundVariablePath, new String[] {path.makeRelative().toString(), javaProject.getElementName()});

				case INCLUDEPATH_CYCLE:
					javaProject = (IJavaScriptProject)elements[0];
					return Messages.bind(Messages.classpath_cycle, javaProject.getElementName());

				case DISABLED_CP_EXCLUSION_PATTERNS:
					javaProject = (IJavaScriptProject)elements[0];
					String projectName = javaProject.getElementName();
					IPath newPath = path;
					if (path.segment(0).toString().equals(projectName)) {
						newPath = path.removeFirstSegments(1);
					}
					return Messages.bind(Messages.classpath_disabledInclusionExclusionPatterns, new String[] {newPath.makeRelative().toString(), projectName});



				case CANNOT_RETRIEVE_ATTACHED_JSDOC :
					if (elements != null && elements.length == 1) {
						if (this.string != null) {
							return Messages.bind(Messages.status_cannot_retrieve_attached_javadoc, ((JavaElement)elements[0]).toStringWithAncestors(), this.string);
						}
						return Messages.bind(Messages.status_cannot_retrieve_attached_javadoc, ((JavaElement)elements[0]).toStringWithAncestors(), ""); //$NON-NLS-1$
					}
					if (this.string != null) {
						return Messages.bind(Messages.status_cannot_retrieve_attached_javadoc, this.string, "");//$NON-NLS-1$
					}
					break;

				case UNKNOWN_JSDOC_FORMAT :
					return Messages.bind(Messages.status_unknown_javadoc_format, ((JavaElement)elements[0]).toStringWithAncestors());

				case DEPRECATED_VARIABLE :
					javaProject = (IJavaScriptProject)elements[0];
					return Messages.bind(Messages.classpath_deprecated_variable, new String[] {path.segment(0).toString(), javaProject.getElementName(), this.string});
			}
			if (string != null) {
				return string;
			} else {
				return ""; //$NON-NLS-1$
			}
		} else {
			String message = exception.getMessage();
			if (message != null) {
				return message;
			} else {
				return exception.toString();
			}
		}
	}
	/**
	 * @see IJavaScriptModelStatus#getPath()
	 */
	public IPath getPath() {
		return path;
	}
	/**
	 * @see IStatus#getSeverity()
	 */
	public int getSeverity() {
		if (children == NO_CHILDREN) return super.getSeverity();
		int severity = -1;
		for (int i = 0, max = children.length; i < max; i++) {
			int childrenSeverity = children[i].getSeverity();
			if (childrenSeverity > severity) {
				severity = childrenSeverity;
			}
		}
		return severity;
	}
	/**
	 * @see IJavaScriptModelStatus#isDoesNotExist()
	 */
	public boolean isDoesNotExist() {
		int code = getCode();
		return code == ELEMENT_DOES_NOT_EXIST || code == ELEMENT_NOT_ON_CLASSPATH;
	}
	/**
	 * @see IStatus#isMultiStatus()
	 */
	public boolean isMultiStatus() {
		return children != NO_CHILDREN;
	}
	/**
	 * @see IStatus#isOK()
	 */
	public boolean isOK() {
		return getCode() == OK;
	}
	/**
	 * @see IStatus#matches(int)
	 */
	public boolean matches(int mask) {
		if (! isMultiStatus()) {
			return matches(this, mask);
		} else {
			for (int i = 0, max = children.length; i < max; i++) {
				if (matches((JavaModelStatus) children[i], mask))
					return true;
			}
			return false;
		}
	}
	/**
	 * Helper for matches(int).
	 */
	protected boolean matches(JavaModelStatus status, int mask) {
		int severityMask = mask & 0x7;
		int categoryMask = mask & ~0x7;
		int bits = status.getBits();
		return ((severityMask == 0) || (bits & severityMask) != 0) && ((categoryMask == 0) || (bits & categoryMask) != 0);
	}
	/**
	 * Creates and returns a new <code>IJavaScriptModelStatus</code> that is a
	 * a multi-status status.
	 *
	 * @see IStatus#isMultiStatus()
	 */
	public static IJavaScriptModelStatus newMultiStatus(IJavaScriptModelStatus[] children) {
		JavaModelStatus jms = new JavaModelStatus();
		jms.children = children;
		return jms;
	}
	/**
	 * Returns a printable representation of this exception for debugging
	 * purposes.
	 */
	public String toString() {
		if (this == VERIFIED_OK){
			return "JavaScriptModelStatus[OK]"; //$NON-NLS-1$
		}
		StringBuffer buffer = new StringBuffer();
		buffer.append("JavaScript Model Status ["); //$NON-NLS-1$
		buffer.append(getMessage());
		buffer.append("]"); //$NON-NLS-1$
		return buffer.toString();
	}
}
