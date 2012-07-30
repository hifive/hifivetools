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
package org.eclipse.wst.jsdt.internal.core.search;

import java.util.HashMap;
import java.util.HashSet;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.wst.jsdt.core.IField;
import org.eclipse.wst.jsdt.core.IFunction;
import org.eclipse.wst.jsdt.core.IJavaScriptElement;
import org.eclipse.wst.jsdt.core.IJavaScriptUnit;
import org.eclipse.wst.jsdt.core.IMember;
import org.eclipse.wst.jsdt.core.IPackageFragmentRoot;
import org.eclipse.wst.jsdt.core.IType;
import org.eclipse.wst.jsdt.core.JavaScriptCore;
import org.eclipse.wst.jsdt.core.JavaScriptModelException;
import org.eclipse.wst.jsdt.core.WorkingCopyOwner;
import org.eclipse.wst.jsdt.core.compiler.CharOperation;
import org.eclipse.wst.jsdt.core.infer.InferredAttribute;
import org.eclipse.wst.jsdt.core.infer.InferredMethod;
import org.eclipse.wst.jsdt.core.infer.InferredType;
import org.eclipse.wst.jsdt.core.search.IJavaScriptSearchConstants;
import org.eclipse.wst.jsdt.core.search.IJavaScriptSearchScope;
import org.eclipse.wst.jsdt.core.search.SearchDocument;
import org.eclipse.wst.jsdt.core.search.SearchParticipant;
import org.eclipse.wst.jsdt.core.search.SearchPattern;
import org.eclipse.wst.jsdt.core.search.SearchRequestor;
import org.eclipse.wst.jsdt.core.search.TypeNameMatch;
import org.eclipse.wst.jsdt.internal.compiler.ASTVisitor;
import org.eclipse.wst.jsdt.internal.compiler.CompilationResult;
import org.eclipse.wst.jsdt.internal.compiler.DefaultErrorHandlingPolicies;
import org.eclipse.wst.jsdt.internal.compiler.ast.ASTNode;
import org.eclipse.wst.jsdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.wst.jsdt.internal.compiler.ast.LocalDeclaration;
import org.eclipse.wst.jsdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.wst.jsdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.wst.jsdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.wst.jsdt.internal.compiler.env.AccessRestriction;
import org.eclipse.wst.jsdt.internal.compiler.env.AccessRuleSet;
import org.eclipse.wst.jsdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.wst.jsdt.internal.compiler.lookup.Binding;
import org.eclipse.wst.jsdt.internal.compiler.lookup.BlockScope;
import org.eclipse.wst.jsdt.internal.compiler.lookup.ClassScope;
import org.eclipse.wst.jsdt.internal.compiler.lookup.CompilationUnitScope;
import org.eclipse.wst.jsdt.internal.compiler.lookup.Scope;
import org.eclipse.wst.jsdt.internal.compiler.parser.Parser;
import org.eclipse.wst.jsdt.internal.compiler.problem.DefaultProblemFactory;
import org.eclipse.wst.jsdt.internal.compiler.problem.ProblemReporter;
import org.eclipse.wst.jsdt.internal.core.CompilationUnit;
import org.eclipse.wst.jsdt.internal.core.DefaultWorkingCopyOwner;
import org.eclipse.wst.jsdt.internal.core.JavaModelManager;
import org.eclipse.wst.jsdt.internal.core.JavaProject;
import org.eclipse.wst.jsdt.internal.core.search.indexing.IIndexConstants;
import org.eclipse.wst.jsdt.internal.core.search.indexing.IndexManager;
import org.eclipse.wst.jsdt.internal.core.search.matching.ConstructorDeclarationPattern;
import org.eclipse.wst.jsdt.internal.core.search.matching.DeclarationOfAccessedFieldsPattern;
import org.eclipse.wst.jsdt.internal.core.search.matching.DeclarationOfReferencedMethodsPattern;
import org.eclipse.wst.jsdt.internal.core.search.matching.DeclarationOfReferencedTypesPattern;
import org.eclipse.wst.jsdt.internal.core.search.matching.JavaSearchPattern;
import org.eclipse.wst.jsdt.internal.core.search.matching.LocalVariablePattern;
import org.eclipse.wst.jsdt.internal.core.search.matching.MatchLocator;
import org.eclipse.wst.jsdt.internal.core.search.matching.MethodPattern;
import org.eclipse.wst.jsdt.internal.core.search.matching.MultiTypeDeclarationPattern;
import org.eclipse.wst.jsdt.internal.core.search.matching.OrPattern;
import org.eclipse.wst.jsdt.internal.core.search.matching.QualifiedTypeDeclarationPattern;
import org.eclipse.wst.jsdt.internal.core.search.matching.SecondaryTypeDeclarationPattern;
import org.eclipse.wst.jsdt.internal.core.search.matching.TypeDeclarationPattern;
import org.eclipse.wst.jsdt.internal.core.util.Messages;
import org.eclipse.wst.jsdt.internal.core.util.Util;

/**
 * Search basic engine. Public search engine (see {@link org.eclipse.wst.jsdt.core.search.SearchEngine}
 * for detailed comment), now uses basic engine functionalities.
 * Note that search basic engine does not implement depreciated functionalities...
 */
public class BasicSearchEngine {

	/*
	 * A default parser to parse non-reconciled working copies
	 */
	private Parser parser;
	private CompilerOptions compilerOptions;

	/*
	 * A list of working copies that take precedence over their original
	 * compilation units.
	 */
	private IJavaScriptUnit[] workingCopies;

	/*
	 * A working copy owner whose working copies will take precedent over
	 * their original compilation units.
	 */
	private WorkingCopyOwner workingCopyOwner;

	/**
	 * For tracing purpose.
	 */
	public static boolean VERBOSE = false;

	/*
	 * Creates a new search basic engine.
	 */
	public BasicSearchEngine() {
		// will use working copies of PRIMARY owner
	}

	/**
	 * @see org.eclipse.wst.jsdt.core.search.SearchEngine#SearchEngine(IJavaScriptUnit[]) for detailed comment.
	 */
	public BasicSearchEngine(IJavaScriptUnit[] workingCopies) {
		this.workingCopies = workingCopies;
	}

	char convertTypeKind(int typeDeclarationKind) {
		switch(typeDeclarationKind) {
			case TypeDeclaration.CLASS_DECL : return IIndexConstants.CLASS_SUFFIX;
			default : return IIndexConstants.TYPE_SUFFIX;
		}
	}
	/**
	 * @see org.eclipse.wst.jsdt.core.search.SearchEngine#SearchEngine(WorkingCopyOwner) for detailed comment.
	 */
	public BasicSearchEngine(WorkingCopyOwner workingCopyOwner) {
		this.workingCopyOwner = workingCopyOwner;
	}

	/**
	 * @see org.eclipse.wst.jsdt.core.search.SearchEngine#createHierarchyScope(IType) for detailed comment.
	 */
	public static IJavaScriptSearchScope createHierarchyScope(IType type) throws JavaScriptModelException {
		return createHierarchyScope(type, DefaultWorkingCopyOwner.PRIMARY);
	}

	/**
	 * @see org.eclipse.wst.jsdt.core.search.SearchEngine#createHierarchyScope(IType,WorkingCopyOwner) for detailed comment.
	 */
	public static IJavaScriptSearchScope createHierarchyScope(IType type, WorkingCopyOwner owner) throws JavaScriptModelException {
		return new HierarchyScope(type, owner);
	}

	/**
	 * @see org.eclipse.wst.jsdt.core.search.SearchEngine#createJavaSearchScope(IJavaScriptElement[]) for detailed comment.
	 */
	public static IJavaScriptSearchScope createJavaSearchScope(IJavaScriptElement[] elements) {
		return createJavaSearchScope(elements, true);
	}

	/**
	 * @see org.eclipse.wst.jsdt.core.search.SearchEngine#createJavaSearchScope(IJavaScriptElement[], boolean) for detailed comment.
	 */
	public static IJavaScriptSearchScope createJavaSearchScope(IJavaScriptElement[] elements, boolean includeReferencedProjects) {
		int includeMask = IJavaScriptSearchScope.SOURCES | IJavaScriptSearchScope.APPLICATION_LIBRARIES | IJavaScriptSearchScope.SYSTEM_LIBRARIES;
		if (includeReferencedProjects) {
			includeMask |= IJavaScriptSearchScope.REFERENCED_PROJECTS;
		}
		return createJavaSearchScope(elements, includeMask);
	}

	/**
	 * @see org.eclipse.wst.jsdt.core.search.SearchEngine#createJavaSearchScope(IJavaScriptElement[], int) for detailed comment.
	 */
	public static IJavaScriptSearchScope createJavaSearchScope(IJavaScriptElement[] elements, int includeMask) {
		JavaSearchScope scope = new JavaSearchScope();
		HashSet visitedProjects = new HashSet(2);
		for (int i = 0, length = elements.length; i < length; i++) {
			IJavaScriptElement element = elements[i];
			if (element != null) {
				try {
					if (element instanceof JavaProject) {
						scope.add((JavaProject)element, includeMask, visitedProjects);
					} else {
						scope.add(element);
					}
				} catch (JavaScriptModelException e) {
					// ignore
				}
			}
		}
		return scope;
	}

	/**
	 * @see org.eclipse.wst.jsdt.core.search.SearchEngine#createTypeNameMatch(IType, int) for detailed comment.
	 */
	public static TypeNameMatch createTypeNameMatch(IType type, int modifiers) {
		return new JavaSearchTypeNameMatch(type, modifiers);
	}

	/**
	 * @see org.eclipse.wst.jsdt.core.search.SearchEngine#createWorkspaceScope() for detailed comment.
	 */
	public static IJavaScriptSearchScope createWorkspaceScope() {
		return JavaModelManager.getJavaModelManager().getWorkspaceScope();
	}

	/**
	 * Searches for matches to a given query. Search queries can be created using helper
	 * methods (from a String pattern or a Java element) and encapsulate the description of what is
	 * being searched (for example, search method declarations in a case sensitive way).
	 *
	 * @param scope the search result has to be limited to the given scope
	 * @param requestor a callback object to which each match is reported
	 */
	void findMatches(SearchPattern pattern, SearchParticipant[] participants, IJavaScriptSearchScope scope, SearchRequestor requestor, IProgressMonitor monitor) throws CoreException {
		if (monitor != null && monitor.isCanceled()) throw new OperationCanceledException();
		try {
			if (VERBOSE) {
				Util.verbose("Searching for pattern: " + pattern.toString()); //$NON-NLS-1$
				Util.verbose(scope.toString());
			}
			if (participants == null) {
				if (VERBOSE) Util.verbose("No participants => do nothing!"); //$NON-NLS-1$
				return;
			}

			/* initialize progress monitor */
			int length = participants.length;
			if (monitor != null)
				monitor.beginTask(Messages.engine_searching, 100 * length);
			IndexManager indexManager = JavaModelManager.getJavaModelManager().getIndexManager();
			requestor.beginReporting();
			for (int i = 0; i < length; i++) {
				if (monitor != null && monitor.isCanceled()) throw new OperationCanceledException();

				SearchParticipant participant = participants[i];
				try {
					if (monitor != null) monitor.subTask(Messages.bind(Messages.engine_searching_indexing, new String[] {participant.getDescription()}));
					participant.beginSearching();
					requestor.enterParticipant(participant);
					PathCollector pathCollector = new PathCollector();
					indexManager.performConcurrentJob(
						new PatternSearchJob(pattern, participant, scope, pathCollector),
						IJavaScriptSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
						monitor==null ? null : new SubProgressMonitor(monitor, 50));
					if (monitor != null && monitor.isCanceled()) throw new OperationCanceledException();

					// locate index matches if any (note that all search matches could have been issued during index querying)
					if (monitor != null) monitor.subTask(Messages.bind(Messages.engine_searching_matching, new String[] {participant.getDescription()}));
					String[] indexMatchPaths = pathCollector.getPaths();
					if (indexMatchPaths != null) {
						pathCollector = null; // release
						int indexMatchLength = indexMatchPaths.length;
						SearchDocument[] indexMatches = new SearchDocument[indexMatchLength];
						for (int j = 0; j < indexMatchLength; j++) {
							indexMatches[j] = participant.getDocument(indexMatchPaths[j]);
						}
						SearchDocument[] matches = MatchLocator.addWorkingCopies(pattern, indexMatches, getWorkingCopies(), participant);
						participant.locateMatches(matches, pattern, scope, requestor, monitor==null ? null : new SubProgressMonitor(monitor, 50));
					}
				} finally {
					requestor.exitParticipant(participant);
					participant.doneSearching();
				}
			}
		} finally {
			requestor.endReporting();
			if (monitor != null)
				monitor.done();
		}
	}
	/**
	 * Returns a new default Java search participant.
	 *
	 * @return a new default Java search participant
	 * @since 3.0
	 */
	public static SearchParticipant getDefaultSearchParticipant() {
		return new JavaSearchParticipant();
	}


	/**
	 * @param matchRule
	 */
	public static String getMatchRuleString(final int matchRule) {
		if (matchRule == 0) {
			return "R_EXACT_MATCH"; //$NON-NLS-1$
		}
		StringBuffer buffer = new StringBuffer();
		for (int i=1; i<=8; i++) {
			int bit = matchRule & (1<<(i-1));
			if (bit != 0 && buffer.length()>0) buffer.append(" | "); //$NON-NLS-1$
			switch (bit) {
				case SearchPattern.R_PREFIX_MATCH:
					buffer.append("R_PREFIX_MATCH"); //$NON-NLS-1$
					break;
				case SearchPattern.R_CASE_SENSITIVE:
					buffer.append("R_CASE_SENSITIVE"); //$NON-NLS-1$
					break;
				case SearchPattern.R_EQUIVALENT_MATCH:
					buffer.append("R_EQUIVALENT_MATCH"); //$NON-NLS-1$
					break;
				case SearchPattern.R_ERASURE_MATCH:
					buffer.append("R_ERASURE_MATCH"); //$NON-NLS-1$
					break;
				case SearchPattern.R_FULL_MATCH:
					buffer.append("R_FULL_MATCH"); //$NON-NLS-1$
					break;
				case SearchPattern.R_PATTERN_MATCH:
					buffer.append("R_PATTERN_MATCH"); //$NON-NLS-1$
					break;
				case SearchPattern.R_REGEXP_MATCH:
					buffer.append("R_REGEXP_MATCH"); //$NON-NLS-1$
					break;
				case SearchPattern.R_CAMELCASE_MATCH:
					buffer.append("R_CAMELCASE_MATCH"); //$NON-NLS-1$
					break;
			}
		}
		return buffer.toString();
	}

	/**
	 * Return kind of search corresponding to given value.
	 *
	 * @param searchFor
	 */
	public static String getSearchForString(final int searchFor) {
		switch (searchFor) {
			case IJavaScriptSearchConstants.TYPE:
				return ("TYPE"); //$NON-NLS-1$
			case IJavaScriptSearchConstants.METHOD:
				return ("METHOD"); //$NON-NLS-1$
			case IJavaScriptSearchConstants.PACKAGE:
				return ("PACKAGE"); //$NON-NLS-1$
			case IJavaScriptSearchConstants.CONSTRUCTOR:
				return ("CONSTRUCTOR"); //$NON-NLS-1$
			case IJavaScriptSearchConstants.FIELD:
				return ("FIELD"); //$NON-NLS-1$
			case IJavaScriptSearchConstants.CLASS:
				return ("CLASS"); //$NON-NLS-1$
			case IJavaScriptSearchConstants.VAR:
				return ("VAR"); //$NON-NLS-1$
			case IJavaScriptSearchConstants.FUNCTION:
				return ("FUNCTION"); //$NON-NLS-1$
		}
		return "UNKNOWN"; //$NON-NLS-1$
	}

	private Parser getParser() {
		if (this.parser == null) {
			this.compilerOptions = new CompilerOptions(JavaScriptCore.getOptions());
			ProblemReporter problemReporter =
				new ProblemReporter(
					DefaultErrorHandlingPolicies.proceedWithAllProblems(),
					this.compilerOptions,
					new DefaultProblemFactory());
			this.parser = new Parser(problemReporter, true);
		}
		return this.parser;
	}

	/*
	 * Returns the list of working copies used by this search engine.
	 * Returns null if none.
	 */
	private IJavaScriptUnit[] getWorkingCopies() {
		IJavaScriptUnit[] copies;
		if (this.workingCopies != null) {
			if (this.workingCopyOwner == null) {
				copies = JavaModelManager.getJavaModelManager().getWorkingCopies(DefaultWorkingCopyOwner.PRIMARY, false/*don't add primary WCs a second time*/);
				if (copies == null) {
					copies = this.workingCopies;
				} else {
					HashMap pathToCUs = new HashMap();
					for (int i = 0, length = copies.length; i < length; i++) {
						IJavaScriptUnit unit = copies[i];
						pathToCUs.put(unit.getPath(), unit);
					}
					for (int i = 0, length = this.workingCopies.length; i < length; i++) {
						IJavaScriptUnit unit = this.workingCopies[i];
						pathToCUs.put(unit.getPath(), unit);
					}
					int length = pathToCUs.size();
					copies = new IJavaScriptUnit[length];
					pathToCUs.values().toArray(copies);
				}
			} else {
				copies = this.workingCopies;
			}
		} else if (this.workingCopyOwner != null) {
			copies = JavaModelManager.getJavaModelManager().getWorkingCopies(this.workingCopyOwner, true/*add primary WCs*/);
		} else {
			copies = JavaModelManager.getJavaModelManager().getWorkingCopies(DefaultWorkingCopyOwner.PRIMARY, false/*don't add primary WCs a second time*/);
		}
		if (copies == null) return null;

		// filter out primary working copies that are saved
		IJavaScriptUnit[] result = null;
		int length = copies.length;
		int index = 0;
		for (int i = 0; i < length; i++) {
			CompilationUnit copy = (CompilationUnit)copies[i];
			try {
				if (!copy.isPrimary()
						|| copy.hasUnsavedChanges()
						|| copy.hasResourceChanged()) {
					if (result == null) {
						result = new IJavaScriptUnit[length];
					}
					result[index++] = copy;
				}
			}  catch (JavaScriptModelException e) {
				// copy doesn't exist: ignore
			}
		}
		if (index != length && result != null) {
			System.arraycopy(result, 0, result = new IJavaScriptUnit[index], 0, index);
		}
		return result;
	}

	/*
	 * Returns the list of working copies used to do the search on the given Java element.
	 */
	private IJavaScriptUnit[] getWorkingCopies(IJavaScriptElement element) {
		if (element instanceof IMember) {
			IJavaScriptUnit cu = ((IMember)element).getJavaScriptUnit();
			if (cu != null && cu.isWorkingCopy()) {
				IJavaScriptUnit[] copies = getWorkingCopies();
				int length = copies == null ? 0 : copies.length;
				if (length > 0) {
					IJavaScriptUnit[] newWorkingCopies = new IJavaScriptUnit[length+1];
					System.arraycopy(copies, 0, newWorkingCopies, 0, length);
					newWorkingCopies[length] = cu;
					return newWorkingCopies;
				}
				return new IJavaScriptUnit[] {cu};
			}
		}
		return getWorkingCopies();
	}

	boolean match(char patternTypeSuffix, int modifiers) {
		switch(patternTypeSuffix) {
			case IIndexConstants.CLASS_SUFFIX :
				return modifiers == 0;
		}
		return true;
	}

	boolean match(char patternTypeSuffix, char[] patternPkg, char[] patternTypeName, int matchRule, int typeKind, char[] pkg, char[] typeName) {
		if (typeName==null)
			typeName=CharOperation.NO_CHAR;
		switch(patternTypeSuffix) {
			case IIndexConstants.CLASS_SUFFIX :
				if (typeKind != TypeDeclaration.CLASS_DECL) return false;
				break;
			case IIndexConstants.TYPE_SUFFIX : // nothing
		}

		boolean isCaseSensitive = (matchRule & SearchPattern.R_CASE_SENSITIVE) != 0;
		if (patternPkg != null && !CharOperation.equals(patternPkg, pkg, isCaseSensitive))
				return false;

		if (patternTypeName != null) {
			boolean isCamelCase = (matchRule & SearchPattern.R_CAMELCASE_MATCH) != 0;
			int matchMode = matchRule & JavaSearchPattern.MATCH_MODE_MASK;
			if (!isCaseSensitive && !isCamelCase) {
				patternTypeName = CharOperation.toLowerCase(patternTypeName);
			}
			boolean matchFirstChar = !isCaseSensitive || patternTypeName[0] == typeName[0];
			if (isCamelCase && matchFirstChar && CharOperation.camelCaseMatch(patternTypeName, typeName)) {
				return true;
			}
			switch(matchMode) {
				case SearchPattern.R_EXACT_MATCH :
					if (!isCamelCase) {
						return matchFirstChar && CharOperation.equals(patternTypeName, typeName, isCaseSensitive);
					}
					// fall through next case to match as prefix if camel case failed
				case SearchPattern.R_PREFIX_MATCH :
					return matchFirstChar && CharOperation.prefixEquals(patternTypeName, typeName, isCaseSensitive);
				case SearchPattern.R_PATTERN_MATCH :
					return CharOperation.match(patternTypeName, typeName, isCaseSensitive);
				case SearchPattern.R_REGEXP_MATCH :
					// TODO (frederic) implement regular expression match
					break;
			}
		}
		return true;

	}

	/**
	 * Searches for matches of a given search pattern. Search patterns can be created using helper
	 * methods (from a String pattern or a Java element) and encapsulate the description of what is
	 * being searched (for example, search method declarations in a case sensitive way).
	 *
	 * @see org.eclipse.wst.jsdt.core.search.SearchEngine#search(SearchPattern, SearchParticipant[], IJavaScriptSearchScope, SearchRequestor, IProgressMonitor)
	 * 	for detailed comment
	 */
	public void search(SearchPattern pattern, SearchParticipant[] participants, IJavaScriptSearchScope scope, SearchRequestor requestor, IProgressMonitor monitor) throws CoreException {
		if (VERBOSE) {
			Util.verbose("BasicSearchEngine.search(SearchPattern, SearchParticipant[], IJavaScriptSearchScope, SearchRequestor, IProgressMonitor)"); //$NON-NLS-1$
		}
		findMatches(pattern, participants, scope, requestor, monitor);
	}


	public void searchAllBindingNames(
			final char[] packageName,
			final char[] bindingName,
			final int bindingType,
			final int matchRule,
			IJavaScriptSearchScope scope,
			final IRestrictedAccessBindingRequestor nameRequestor,
			int waitingPolicy,
			boolean doParse,
			IProgressMonitor progressMonitor)  throws JavaScriptModelException {

			if (VERBOSE) {
				Util.verbose("BasicSearchEngine.searchAllBindingNames(char[], char[], int, int, IJavaScriptSearchScope, IRestrictedAccessTypeRequestor, int, IProgressMonitor)"); //$NON-NLS-1$
				Util.verbose("	- package name: "+(packageName==null?"null":new String(packageName))); //$NON-NLS-1$ //$NON-NLS-2$
				Util.verbose("	- type name: "+(bindingName==null?"null":new String(bindingName))); //$NON-NLS-1$ //$NON-NLS-2$
				Util.verbose("	- match rule: "+getMatchRuleString(matchRule)); //$NON-NLS-1$
				Util.verbose("	- bindingType for: "+bindingType); //$NON-NLS-1$
				Util.verbose("	- scope: "+scope); //$NON-NLS-1$
			}

			IndexManager indexManager = JavaModelManager.getJavaModelManager().getIndexManager();
			SearchPattern searchPattern=null;
			char suffix=0;
			switch(bindingType){

				case Binding.TYPE :
				{
					

					suffix = IIndexConstants.CLASS_SUFFIX;
					searchPattern = new TypeDeclarationPattern(
							packageName,
							null, // do find member types
							bindingName,
							suffix,
							matchRule);

					break;
				}
				case Binding.VARIABLE :
				case Binding.LOCAL :
				case Binding.FIELD :
				{
					searchPattern = new   LocalVariablePattern(true, false, false,bindingName,   matchRule);
				}
				break;
				case Binding.METHOD:
				{
					searchPattern = new MethodPattern(
							true,false,true,
							bindingName,
							null,null,null,null,
							null,null,null,
							matchRule);

				}
				break;
				default: // some combination
				{
					if ((bindingType & Binding.METHOD) >0)
					{
						searchPattern = new MethodPattern(
								true,false,true,
								bindingName,
								null,null,null,null,
								null,null,null,
								matchRule);

					}
					if ((bindingType & (Binding.VARIABLE |Binding.LOCAL |Binding.FIELD )) >0)
					{
						LocalVariablePattern localVariablePattern = new   LocalVariablePattern(true, false, false,bindingName,   matchRule);
						if (searchPattern==null)
							searchPattern=localVariablePattern;
						else
							searchPattern=new OrPattern(searchPattern,localVariablePattern);
					}
					if ((bindingType & Binding.TYPE) >0)
					{
						suffix = IIndexConstants.CLASS_SUFFIX;
						TypeDeclarationPattern typeDeclarationPattern = new TypeDeclarationPattern(
								packageName,
								null, // do find member types
								bindingName,
								suffix,
								matchRule);
							if (searchPattern==null)
								searchPattern=typeDeclarationPattern;
							else
								searchPattern=new OrPattern(searchPattern,typeDeclarationPattern);
					}
				}
			}
			final SearchPattern pattern =searchPattern;
			final char typeSuffix=suffix;

			// Get working copy path(s). Store in a single string in case of only one to optimize comparison in requestor
			final HashSet workingCopyPaths = new HashSet();
			String workingCopyPath = null;
			IJavaScriptUnit[] copies = getWorkingCopies();
			final int copiesLength = copies == null ? 0 : copies.length;
			if (copies != null) {
				if (copiesLength == 1) {
					workingCopyPath = copies[0].getPath().toString();
				} else {
					for (int i = 0; i < copiesLength; i++) {
						IJavaScriptUnit workingCopy = copies[i];
						workingCopyPaths.add(workingCopy.getPath().toString());
					}
				}
			}
			final String singleWkcpPath = workingCopyPath;

			// Index requestor
			IndexQueryRequestor searchRequestor = new IndexQueryRequestor(){
				public boolean acceptIndexMatch(String documentPath, SearchPattern indexRecord, SearchParticipant participant, AccessRuleSet access) {
					// Filter unexpected types
					JavaSearchPattern record = (JavaSearchPattern)indexRecord;

					// Accept document path
					AccessRestriction accessRestriction = null;
					int modifiers=ClassFileConstants.AccPublic;
					char[] packageName=null;
					char[] simpleBindingName=null;
					if (record instanceof MethodPattern) {
						MethodPattern methodPattern = (MethodPattern) record;
						simpleBindingName=methodPattern.selector;
						Path path = new Path(documentPath);
						String string = path.lastSegment();
						if (path.hasTrailingSeparator())	// is library
						{
							packageName=string.toCharArray();
						}
					}
					else if (record instanceof LocalVariablePattern)
					{
						LocalVariablePattern localVariablePattern = (LocalVariablePattern) record;
						simpleBindingName=localVariablePattern.name;
						Path path = new Path(documentPath);
						String string = path.lastSegment();
						if (path.hasTrailingSeparator())	// is library
						{
							packageName=string.toCharArray();
						}

					}else if (record instanceof TypeDeclarationPattern) {
						TypeDeclarationPattern typeDecPattern = (TypeDeclarationPattern)record;
						simpleBindingName=typeDecPattern.simpleName;
						Path path = new Path(documentPath);
						String string = path.lastSegment();
						if (path.hasTrailingSeparator())	// is library
						{
							packageName=string.toCharArray();
						}
					}

					nameRequestor.acceptBinding( bindingType,modifiers, packageName, simpleBindingName,   documentPath, accessRestriction);

					return true;
				}
			};

			try {
				if (progressMonitor != null) {
					progressMonitor.beginTask(Messages.engine_searching, 100);
				}
				// add type names from indexes
				indexManager.performConcurrentJob(
					new PatternSearchJob(
						pattern,
						getDefaultSearchParticipant(), // Java search only
						scope,
						searchRequestor),
					waitingPolicy,
					progressMonitor == null ? null : new SubProgressMonitor(progressMonitor, 100));

				// add type names from working copies
				if (copies != null && doParse) {
					for (int i = 0; i < copiesLength; i++) {
						IJavaScriptUnit workingCopy = copies[i];
						if (!scope.encloses(workingCopy)) continue;
						final String path = workingCopy.getPath().toString();
						if (workingCopy.isConsistent()) {
							char[] packageDeclaration = CharOperation.NO_CHAR;
							switch (bindingType)
							{
							case Binding.TYPE:
							{
								IType[] allTypes = workingCopy.getAllTypes();
								for (int j = 0, allTypesLength = allTypes.length; j < allTypesLength; j++) {
									IType type = allTypes[j];
									IJavaScriptElement parent = type.getParent();
									char[][] enclosingTypeNames;
									if (parent instanceof IType) {
										char[] parentQualifiedName = ((IType)parent).getTypeQualifiedName('.').toCharArray();
										enclosingTypeNames = CharOperation.splitOn('.', parentQualifiedName);
									} else {
										enclosingTypeNames = CharOperation.NO_CHAR_CHAR;
									}
									char[] simpleName = type.getElementName().toCharArray();
									int kind = TypeDeclaration.CLASS_DECL;
									
									if (match(typeSuffix, packageName, bindingName, matchRule, kind, packageDeclaration, simpleName)) {
										nameRequestor.acceptBinding(bindingType,type.getFlags(), packageDeclaration, simpleName,   path, null);
									}
								}
							}
							case Binding.METHOD:
							{
								IFunction[] allMethods = workingCopy.getFunctions();
								for (int j = 0, allMethodsLength = allMethods.length; j < allMethodsLength; j++) {
									IFunction method = allMethods[j];
									IJavaScriptElement parent = method.getParent();
//									char[][] enclosingTypeNames;
									if (parent instanceof IType) {
//										char[] parentQualifiedName = ((IType)parent).getTypeQualifiedName('.').toCharArray();
//										enclosingTypeNames = CharOperation.splitOn('.', parentQualifiedName);
									} else {
//										enclosingTypeNames = CharOperation.NO_CHAR_CHAR;
									}
									char[] simpleName = method.getElementName().toCharArray();
//									int kind;
									if (match(typeSuffix, packageName, bindingName, matchRule, 0, packageDeclaration, simpleName)) {
										nameRequestor.acceptBinding(bindingType,method.getFlags(), packageDeclaration, simpleName,   path, null);
									}
								}
							}
							break;
							case Binding.VARIABLE :
							case Binding.LOCAL :
							case Binding.FIELD :
							{
								IField[] allFields = workingCopy.getFields ();
								for (int j = 0, allFieldsLength = allFields.length; j < allFieldsLength; j++) {
									IField field = allFields[j];
									IJavaScriptElement parent = field.getParent();
									char[][] enclosingTypeNames;
									if (parent instanceof IType) {
										char[] parentQualifiedName = ((IType)parent).getTypeQualifiedName('.').toCharArray();
										enclosingTypeNames = CharOperation.splitOn('.', parentQualifiedName);
									} else {
										enclosingTypeNames = CharOperation.NO_CHAR_CHAR;
									}
									char[] simpleName = field.getElementName().toCharArray();
									int kind;
									if (match(typeSuffix, packageName, bindingName, matchRule, 0, packageDeclaration, simpleName)) {
										nameRequestor.acceptBinding(bindingType,field.getFlags(), packageDeclaration, simpleName,   path, null);
									}
								}
							}
							break;
							}
						} else {
							Parser basicParser = getParser();
							org.eclipse.wst.jsdt.internal.compiler.env.ICompilationUnit unit = (org.eclipse.wst.jsdt.internal.compiler.env.ICompilationUnit) workingCopy;
							CompilationResult compilationUnitResult = new CompilationResult(unit, 0, 0, this.compilerOptions.maxProblemsPerUnit);
							CompilationUnitDeclaration parsedUnit = basicParser.dietParse(unit, compilationUnitResult);
							if (parsedUnit != null) {
								basicParser.inferTypes(parsedUnit, null);
								final char[] packageDeclaration = parsedUnit.currentPackage == null ? CharOperation.NO_CHAR : CharOperation.concatWith(parsedUnit.currentPackage.getImportName(), '.');
								class AllTypeDeclarationsVisitor extends ASTVisitor {
//									public boolean visit(TypeDeclaration typeDeclaration, Scope blockScope) {
//										return false; // no local/anonymous type
//									}
									public boolean visit(TypeDeclaration typeDeclaration, CompilationUnitScope compilationUnitScope) {
										if (bindingType==Binding.TYPE &&
												match(typeSuffix, packageName, bindingName, matchRule, TypeDeclaration.kind(typeDeclaration.modifiers), packageDeclaration, typeDeclaration.name)) {
											nameRequestor.acceptBinding(bindingType,typeDeclaration.modifiers, packageDeclaration, typeDeclaration.name,  path, null);
										}
										return true;
									}
									public boolean visit(LocalDeclaration localDeclaration, BlockScope scope) {
										if ((scope instanceof CompilationUnitScope) && (bindingType==Binding.LOCAL || bindingType==Binding.FIELD || bindingType==Binding.VARIABLE )&&
												match(typeSuffix, packageName, bindingName, matchRule,0, packageDeclaration, localDeclaration.name)) {
											nameRequestor.acceptBinding(bindingType,localDeclaration.modifiers, packageDeclaration,  localDeclaration.name,  path, null);
										}
										return true;
									}
									public boolean visit(MethodDeclaration methodDeclaration, Scope scope) {
										if (bindingType==Binding.METHOD && methodDeclaration.selector!=null &&
												match(typeSuffix, packageName, bindingName, matchRule,0, packageDeclaration, methodDeclaration.selector)) {
											nameRequestor.acceptBinding(bindingType,methodDeclaration.modifiers, packageDeclaration,  methodDeclaration.selector,  path, null);
										}
										return true;
									}
//									public boolean visit(TypeDeclaration memberTypeDeclaration, ClassScope classScope) {
//										if (match(typeSuffix, packageName, bindingName, matchRule, TypeDeclaration.kind(memberTypeDeclaration.modifiers), packageDeclaration, memberTypeDeclaration.name)) {
//											// compute encloising type names
//											TypeDeclaration enclosing = memberTypeDeclaration.enclosingType;
//											char[][] enclosingTypeNames = CharOperation.NO_CHAR_CHAR;
//											while (enclosing != null) {
//												enclosingTypeNames = CharOperation.arrayConcat(new char[][] {enclosing.name}, enclosingTypeNames);
//												if ((enclosing.bits & ASTNode.IsMemberType) != 0) {
//													enclosing = enclosing.enclosingType;
//												} else {
//													enclosing = null;
//												}
//											}
//											// report
//											nameRequestor.acceptType(memberTypeDeclaration.modifiers, packageDeclaration, memberTypeDeclaration.name, enclosingTypeNames, path, null);
//										}
//										return true;
//									}
									public boolean visit(InferredType inferredType, BlockScope scope) {
										if (bindingType==Binding.TYPE &&
													match(typeSuffix, packageName, bindingName, matchRule, TypeDeclaration.kind(0), packageDeclaration, inferredType.getName())) {
												nameRequestor.acceptBinding(bindingType,0, packageDeclaration, inferredType.getName(),  path, null);
											}
										return true;
									}
									public boolean visit(InferredAttribute inferredField, BlockScope scope) {
										if ((scope instanceof CompilationUnitScope) && (bindingType==Binding.LOCAL || bindingType==Binding.FIELD || bindingType==Binding.VARIABLE )&&
													match(typeSuffix, packageName, bindingName, matchRule,0, packageDeclaration, inferredField.name)) {
												nameRequestor.acceptBinding(bindingType,inferredField.modifiers, packageDeclaration,  inferredField.name,  path, null);
										}
										return true;
									}
									public boolean visit(InferredMethod inferredMethod, BlockScope scope) {
										if (bindingType==Binding.METHOD && inferredMethod.name!=null &&
													match(typeSuffix, packageName, bindingName, matchRule,0, packageDeclaration, inferredMethod.name)) {
												nameRequestor.acceptBinding(bindingType,((MethodDeclaration)inferredMethod.getFunctionDeclaration()).modifiers, packageDeclaration,  inferredMethod.name,  path, null);
											}
										return true;
									}
								}
								parsedUnit.traverse(new AllTypeDeclarationsVisitor(), parsedUnit.scope);
							}
						}
					}
				}
			} finally {
				if (progressMonitor != null) {
					progressMonitor.done();
				}
			}
		}




	/**
	 * Searches for all secondary types in the given scope.
	 * The search can be selecting specific types (given a package or a type name
	 * prefix and match modes).
	 */
	public void searchAllSecondaryTypeNames(
			IPackageFragmentRoot[] sourceFolders,
			final IRestrictedAccessTypeRequestor nameRequestor,
			boolean waitForIndexes,
			IProgressMonitor progressMonitor)  throws JavaScriptModelException {

		if (VERBOSE) {
			Util.verbose("BasicSearchEngine.searchAllSecondaryTypeNames(IPackageFragmentRoot[], IRestrictedAccessTypeRequestor, boolean, IProgressMonitor)"); //$NON-NLS-1$
			StringBuffer buffer = new StringBuffer("	- source folders: "); //$NON-NLS-1$
			int length = sourceFolders.length;
			for (int i=0; i<length; i++) {
				if (i==0) {
					buffer.append('[');
				} else {
					buffer.append(',');
				}
				buffer.append(sourceFolders[i].getElementName());
			}
			buffer.append("]\n	- waitForIndexes: "); //$NON-NLS-1$
			buffer.append(waitForIndexes);
			Util.verbose(buffer.toString());
		}

		IndexManager indexManager = JavaModelManager.getJavaModelManager().getIndexManager();
		final TypeDeclarationPattern pattern = new SecondaryTypeDeclarationPattern();

		// Get working copy path(s). Store in a single string in case of only one to optimize comparison in requestor
		final HashSet workingCopyPaths = new HashSet();
		String workingCopyPath = null;
		IJavaScriptUnit[] copies = getWorkingCopies();
		final int copiesLength = copies == null ? 0 : copies.length;
		if (copies != null) {
			if (copiesLength == 1) {
				workingCopyPath = copies[0].getPath().toString();
			} else {
				for (int i = 0; i < copiesLength; i++) {
					IJavaScriptUnit workingCopy = copies[i];
					workingCopyPaths.add(workingCopy.getPath().toString());
				}
			}
		}
		final String singleWkcpPath = workingCopyPath;

		// Index requestor
		IndexQueryRequestor searchRequestor = new IndexQueryRequestor(){
			public boolean acceptIndexMatch(String documentPath, SearchPattern indexRecord, SearchParticipant participant, AccessRuleSet access) {
				// Filter unexpected types
				TypeDeclarationPattern record = (TypeDeclarationPattern)indexRecord;
				if (!record.secondary) {
					return true; // filter maint types
				}
				if (record.enclosingTypeNames == IIndexConstants.ONE_ZERO_CHAR) {
					return true; // filter out local and anonymous classes
				}
				switch (copiesLength) {
					case 0:
						break;
					case 1:
						if (singleWkcpPath.equals(documentPath)) {
							return true; // fliter out *the* working copy
						}
						break;
					default:
						if (workingCopyPaths.contains(documentPath)) {
							return true; // filter out working copies
						}
						break;
				}

				// Accept document path
				AccessRestriction accessRestriction = null;
				if (access != null) {
					// Compute document relative path
					int pkgLength = (record.pkg==null || record.pkg.length==0) ? 0 : record.pkg.length+1;
					int nameLength = record.simpleName==null ? 0 : record.simpleName.length;
					char[] path = new char[pkgLength+nameLength];
					int pos = 0;
					if (pkgLength > 0) {
						System.arraycopy(record.pkg, 0, path, pos, pkgLength-1);
						CharOperation.replace(path, '.', '/');
						path[pkgLength-1] = '/';
						pos += pkgLength;
					}
					if (nameLength > 0) {
						System.arraycopy(record.simpleName, 0, path, pos, nameLength);
						pos += nameLength;
					}
					// Update access restriction if path is not empty
					if (pos > 0) {
						accessRestriction = access.getViolatedRestriction(path);
					}
				}
				nameRequestor.acceptType(record.modifiers, record.pkg, record.simpleName, record.enclosingTypeNames, documentPath, accessRestriction);
				return true;
			}
		};

		// add type names from indexes
		try {
			if (progressMonitor != null) {
				progressMonitor.beginTask(Messages.engine_searching, 100);
			}
			indexManager.performConcurrentJob(
				new PatternSearchJob(
					pattern,
					getDefaultSearchParticipant(), // Java search only
					createJavaSearchScope(sourceFolders),
					searchRequestor),
				waitForIndexes
					? IJavaScriptSearchConstants.WAIT_UNTIL_READY_TO_SEARCH
					: IJavaScriptSearchConstants.FORCE_IMMEDIATE_SEARCH,
				progressMonitor == null ? null : new SubProgressMonitor(progressMonitor, 100));
		} catch (OperationCanceledException oce) {
			// do nothing
		} finally {
			if (progressMonitor != null) {
				progressMonitor.done();
			}
		}
	}

	/**
	 * Searches for all top-level types and member types in the given scope.
	 * The search can be selecting specific types (given a package or a type name
	 * prefix and match modes).
	 *
	 * @see org.eclipse.wst.jsdt.core.search.SearchEngine#searchAllTypeNames(char[], int, char[], int, int, IJavaScriptSearchScope, org.eclipse.wst.jsdt.core.search.TypeNameRequestor, int, IProgressMonitor)
	 * 	for detailed comment
	 */
	public void searchAllTypeNames(
		final char[] packageName,
		final int packageMatchRule,
		final char[] typeName,
		final int typeMatchRule,
		int searchFor,
		IJavaScriptSearchScope scope,
		final IRestrictedAccessTypeRequestor nameRequestor,
		int waitingPolicy,
		IProgressMonitor progressMonitor)  throws JavaScriptModelException {

		if (VERBOSE) {
			Util.verbose("BasicSearchEngine.searchAllTypeNames(char[], char[], int, int, IJavaScriptSearchScope, IRestrictedAccessTypeRequestor, int, IProgressMonitor)"); //$NON-NLS-1$
			Util.verbose("	- package name: "+(packageName==null?"null":new String(packageName))); //$NON-NLS-1$ //$NON-NLS-2$
			Util.verbose("	- match rule: "+getMatchRuleString(packageMatchRule)); //$NON-NLS-1$
			Util.verbose("	- type name: "+(typeName==null?"null":new String(typeName))); //$NON-NLS-1$ //$NON-NLS-2$
			Util.verbose("	- match rule: "+getMatchRuleString(typeMatchRule)); //$NON-NLS-1$
			Util.verbose("	- search for: "+searchFor); //$NON-NLS-1$
			Util.verbose("	- scope: "+scope); //$NON-NLS-1$
		}

		// Create pattern
		IndexManager indexManager = JavaModelManager.getJavaModelManager().getIndexManager();
		final char typeSuffix;
		switch(searchFor){
			case IJavaScriptSearchConstants.CLASS :
				typeSuffix = IIndexConstants.CLASS_SUFFIX;
				break;
			default :
				typeSuffix = IIndexConstants.TYPE_SUFFIX;
				break;
		}
		final TypeDeclarationPattern pattern = packageMatchRule == SearchPattern.R_EXACT_MATCH
			? new TypeDeclarationPattern(
				packageName,
				null,
				typeName,
				typeSuffix,
				typeMatchRule)
			: new QualifiedTypeDeclarationPattern(
				packageName,
				packageMatchRule,
				typeName,
				typeSuffix,
				typeMatchRule);

		// Get working copy path(s). Store in a single string in case of only one to optimize comparison in requestor
		final HashSet workingCopyPaths = new HashSet();
		String workingCopyPath = null;
		IJavaScriptUnit[] copies = getWorkingCopies();
		final int copiesLength = copies == null ? 0 : copies.length;
		if (copies != null) {
			if (copiesLength == 1) {
				workingCopyPath = copies[0].getPath().toString();
			} else {
				for (int i = 0; i < copiesLength; i++) {
					IJavaScriptUnit workingCopy = copies[i];
					workingCopyPaths.add(workingCopy.getPath().toString());
				}
			}
		}
		final String singleWkcpPath = workingCopyPath;

		// Index requestor
		IndexQueryRequestor searchRequestor = new IndexQueryRequestor(){
			public boolean acceptIndexMatch(String documentPath, SearchPattern indexRecord, SearchParticipant participant, AccessRuleSet access) {
				// Filter unexpected types
				TypeDeclarationPattern record = (TypeDeclarationPattern)indexRecord;
				if (record.enclosingTypeNames == IIndexConstants.ONE_ZERO_CHAR) {
					return true; // filter out local and anonymous classes
				}
				switch (copiesLength) {
					case 0:
						break;
					case 1:
						if (singleWkcpPath.equals(documentPath)) {
							return true; // fliter out *the* working copy
						}
						break;
					default:
						if (workingCopyPaths.contains(documentPath)) {
							return true; // filter out working copies
						}
						break;
				}

				// Accept document path
				AccessRestriction accessRestriction = null;
				if (access != null) {
					// Compute document relative path
					int pkgLength = (record.pkg==null || record.pkg.length==0) ? 0 : record.pkg.length+1;
					int nameLength = record.simpleName==null ? 0 : record.simpleName.length;
					char[] path = new char[pkgLength+nameLength];
					int pos = 0;
					if (pkgLength > 0) {
						System.arraycopy(record.pkg, 0, path, pos, pkgLength-1);
						CharOperation.replace(path, '.', '/');
						path[pkgLength-1] = '/';
						pos += pkgLength;
					}
					if (nameLength > 0) {
						System.arraycopy(record.simpleName, 0, path, pos, nameLength);
						pos += nameLength;
					}
					// Update access restriction if path is not empty
					if (pos > 0) {
						accessRestriction = access.getViolatedRestriction(path);
					}
				}
				if (match(record.typeSuffix, record.modifiers)) {
					nameRequestor.acceptType(record.modifiers, record.pkg, record.simpleName, record.enclosingTypeNames, documentPath, accessRestriction);
				}
				return true;
			}
		};

		try {
			if (progressMonitor != null) {
				progressMonitor.beginTask(Messages.engine_searching, 100);
			}
			// add type names from indexes
			indexManager.performConcurrentJob(
				new PatternSearchJob(
					pattern,
					getDefaultSearchParticipant(), // Java search only
					scope,
					searchRequestor),
				waitingPolicy,
				progressMonitor == null ? null : new SubProgressMonitor(progressMonitor, 100));

			// add type names from working copies
			if (copies != null) {
				for (int i = 0; i < copiesLength; i++) {
					final IJavaScriptUnit workingCopy = copies[i];
					if (!scope.encloses(workingCopy)) continue;
					final String path = workingCopy.getPath().toString();
					if (workingCopy.isConsistent()) {
						char[] packageDeclaration = CharOperation.NO_CHAR;
						IType[] allTypes = workingCopy.getAllTypes();
						for (int j = 0, allTypesLength = allTypes.length; j < allTypesLength; j++) {
							IType type = allTypes[j];
							IJavaScriptElement parent = type.getParent();
							char[][] enclosingTypeNames;
							if (parent instanceof IType) {
								char[] parentQualifiedName = ((IType)parent).getTypeQualifiedName('.').toCharArray();
								enclosingTypeNames = CharOperation.splitOn('.', parentQualifiedName);
							} else {
								enclosingTypeNames = CharOperation.NO_CHAR_CHAR;
							}
							char[] simpleName = type.getElementName().toCharArray();
							int kind = TypeDeclaration.CLASS_DECL;
			
							if (match(typeSuffix, packageName, typeName, typeMatchRule, kind, packageDeclaration, simpleName)) {
								if (nameRequestor instanceof TypeNameMatchRequestorWrapper) {
									((TypeNameMatchRequestorWrapper)nameRequestor).requestor.acceptTypeNameMatch(new JavaSearchTypeNameMatch(type, type.getFlags()));
								} else {
									nameRequestor.acceptType(type.getFlags(), packageDeclaration, simpleName, enclosingTypeNames, path, null);
								}
							}
						}
					} else {
						Parser basicParser = getParser();
						org.eclipse.wst.jsdt.internal.compiler.env.ICompilationUnit unit = (org.eclipse.wst.jsdt.internal.compiler.env.ICompilationUnit) workingCopy;
						CompilationResult compilationUnitResult = new CompilationResult(unit, 0, 0, this.compilerOptions.maxProblemsPerUnit);
						CompilationUnitDeclaration parsedUnit = basicParser.dietParse(unit, compilationUnitResult);
						if (parsedUnit != null) {
							basicParser.inferTypes(parsedUnit, null);
							final char[] packageDeclaration = parsedUnit.currentPackage == null ? CharOperation.NO_CHAR : CharOperation.concatWith(parsedUnit.currentPackage.getImportName(), '.');
							class AllTypeDeclarationsVisitor extends ASTVisitor {
								public boolean visit(TypeDeclaration typeDeclaration, BlockScope blockScope) {
									return false; // no local/anonymous type
								}
								public boolean visit(TypeDeclaration typeDeclaration, CompilationUnitScope compilationUnitScope) {
									if (match(typeSuffix, packageName, typeName, typeMatchRule, TypeDeclaration.kind(typeDeclaration.modifiers), packageDeclaration, typeDeclaration.name)) {
										if (nameRequestor instanceof TypeNameMatchRequestorWrapper) {
											IType type = workingCopy.getType(new String(typeName));
											((TypeNameMatchRequestorWrapper)nameRequestor).requestor.acceptTypeNameMatch(new JavaSearchTypeNameMatch(type, typeDeclaration.modifiers));
										} else {
											nameRequestor.acceptType(typeDeclaration.modifiers, packageDeclaration, typeDeclaration.name, CharOperation.NO_CHAR_CHAR, path, null);
										}
									}
									return true;
								}
								public boolean visit(TypeDeclaration memberTypeDeclaration, ClassScope classScope) {
									if (match(typeSuffix, packageName, typeName, typeMatchRule, TypeDeclaration.kind(memberTypeDeclaration.modifiers), packageDeclaration, memberTypeDeclaration.name)) {
										// compute encloising type names
										TypeDeclaration enclosing = memberTypeDeclaration.enclosingType;
										char[][] enclosingTypeNames = CharOperation.NO_CHAR_CHAR;
										while (enclosing != null) {
											enclosingTypeNames = CharOperation.arrayConcat(new char[][] {enclosing.name}, enclosingTypeNames);
											if ((enclosing.bits & ASTNode.IsMemberType) != 0) {
												enclosing = enclosing.enclosingType;
											} else {
												enclosing = null;
											}
										}
										// report
										if (nameRequestor instanceof TypeNameMatchRequestorWrapper) {
											IType type = workingCopy.getType(new String(enclosingTypeNames[0]));
											for (int j=1, l=enclosingTypeNames.length; j<l; j++) {
												type = type.getType(new String(enclosingTypeNames[j]));
											}
											((TypeNameMatchRequestorWrapper)nameRequestor).requestor.acceptTypeNameMatch(new JavaSearchTypeNameMatch(type, 0));
										} else {
											nameRequestor.acceptType(memberTypeDeclaration.modifiers, packageDeclaration, memberTypeDeclaration.name, enclosingTypeNames, path, null);
										}
									}
									return true;
								}
								public boolean visit(InferredType inferredType, BlockScope scope) {
									if (match(typeSuffix, packageName, typeName, typeMatchRule, TypeDeclaration.kind(0), packageDeclaration, inferredType.getName())) {
										if (nameRequestor instanceof TypeNameMatchRequestorWrapper) {
											IType type = workingCopy.getType(new String(typeName));
											((TypeNameMatchRequestorWrapper)nameRequestor).requestor.acceptTypeNameMatch(new JavaSearchTypeNameMatch(type, 0));
										} else {
											nameRequestor.acceptType(0, packageDeclaration, inferredType.getName(), CharOperation.NO_CHAR_CHAR, path, null);
										}
									}
									return true;
								}
							}
							parsedUnit.traverse(new AllTypeDeclarationsVisitor(), parsedUnit.scope);
						}
					}
				}
			}
		} finally {
			if (progressMonitor != null) {
				progressMonitor.done();
			}
		}
	}

	/**
	 * Searches for all top-level types and member types in the given scope using  a case sensitive exact match
	 * with the given qualified names and type names.
	 *
	 * @see org.eclipse.wst.jsdt.core.search.SearchEngine#searchAllTypeNames(char[][], char[][], IJavaScriptSearchScope, org.eclipse.wst.jsdt.core.search.TypeNameRequestor, int, IProgressMonitor)
	 * 	for detailed comment
	 */
	public void searchAllTypeNames(
		final char[][] qualifications,
		final char[][] typeNames,
		final int matchRule,
		int searchFor,
		IJavaScriptSearchScope scope,
		final IRestrictedAccessTypeRequestor nameRequestor,
		int waitingPolicy,
		IProgressMonitor progressMonitor)  throws JavaScriptModelException {

		if (VERBOSE) {
			Util.verbose("BasicSearchEngine.searchAllTypeNames(char[][], char[][], int, int, IJavaScriptSearchScope, IRestrictedAccessTypeRequestor, int, IProgressMonitor)"); //$NON-NLS-1$
			Util.verbose("	- package name: "+(qualifications==null?"null":new String(CharOperation.concatWith(qualifications, ',')))); //$NON-NLS-1$ //$NON-NLS-2$
			Util.verbose("	- type name: "+(typeNames==null?"null":new String(CharOperation.concatWith(typeNames, ',')))); //$NON-NLS-1$ //$NON-NLS-2$
			Util.verbose("	- match rule: "+matchRule); //$NON-NLS-1$
			Util.verbose("	- search for: "+searchFor); //$NON-NLS-1$
			Util.verbose("	- scope: "+scope); //$NON-NLS-1$
		}
		IndexManager indexManager = JavaModelManager.getJavaModelManager().getIndexManager();

		final char typeSuffix;
		switch(searchFor){
			case IJavaScriptSearchConstants.CLASS :
				typeSuffix = IIndexConstants.CLASS_SUFFIX;
				break;
			default :
				typeSuffix = IIndexConstants.TYPE_SUFFIX;
				break;
		}
		final MultiTypeDeclarationPattern pattern = new MultiTypeDeclarationPattern(qualifications, typeNames, typeSuffix, matchRule);

		// Get working copy path(s). Store in a single string in case of only one to optimize comparison in requestor
		final HashSet workingCopyPaths = new HashSet();
		String workingCopyPath = null;
		IJavaScriptUnit[] copies = getWorkingCopies();
		final int copiesLength = copies == null ? 0 : copies.length;
		if (copies != null) {
			if (copiesLength == 1) {
				workingCopyPath = copies[0].getPath().toString();
			} else {
				for (int i = 0; i < copiesLength; i++) {
					IJavaScriptUnit workingCopy = copies[i];
					workingCopyPaths.add(workingCopy.getPath().toString());
				}
			}
		}
		final String singleWkcpPath = workingCopyPath;

		// Index requestor
		IndexQueryRequestor searchRequestor = new IndexQueryRequestor(){
			public boolean acceptIndexMatch(String documentPath, SearchPattern indexRecord, SearchParticipant participant, AccessRuleSet access) {
				// Filter unexpected types
				QualifiedTypeDeclarationPattern record = (QualifiedTypeDeclarationPattern) indexRecord;
				if (record.enclosingTypeNames == IIndexConstants.ONE_ZERO_CHAR) {
					return true; // filter out local and anonymous classes
				}
				switch (copiesLength) {
					case 0:
						break;
					case 1:
						if (singleWkcpPath.equals(documentPath)) {
							return true; // fliter out *the* working copy
						}
						break;
					default:
						if (workingCopyPaths.contains(documentPath)) {
							return true; // filter out working copies
						}
						break;
				}

				// Accept document path
				AccessRestriction accessRestriction = null;
				if (access != null) {
					// Compute document relative path
					int qualificationLength = (record.qualification == null || record.qualification.length == 0) ? 0 : record.qualification.length + 1;
					int nameLength = record.simpleName == null ? 0 : record.simpleName.length;
					char[] path = new char[qualificationLength + nameLength];
					int pos = 0;
					if (qualificationLength > 0) {
						System.arraycopy(record.qualification, 0, path, pos, qualificationLength - 1);
						CharOperation.replace(path, '.', '/');
						path[qualificationLength-1] = '/';
						pos += qualificationLength;
					}
					if (nameLength > 0) {
						System.arraycopy(record.simpleName, 0, path, pos, nameLength);
						pos += nameLength;
					}
					// Update access restriction if path is not empty
					if (pos > 0) {
						accessRestriction = access.getViolatedRestriction(path);
					}
				}
				nameRequestor.acceptType(record.modifiers, record.pkg, record.simpleName, record.enclosingTypeNames, documentPath, accessRestriction);
				return true;
			}
		};

		try {
			if (progressMonitor != null) {
				progressMonitor.beginTask(Messages.engine_searching, 100);
			}
			// add type names from indexes
			indexManager.performConcurrentJob(
				new PatternSearchJob(
					pattern,
					getDefaultSearchParticipant(), // Java search only
					scope,
					searchRequestor),
				waitingPolicy,
				progressMonitor == null ? null : new SubProgressMonitor(progressMonitor, 100));

			// add type names from working copies
			if (copies != null) {
				for (int i = 0, length = copies.length; i < length; i++) {
					IJavaScriptUnit workingCopy = copies[i];
					final String path = workingCopy.getPath().toString();
					if (workingCopy.isConsistent()) {
						char[] packageDeclaration = CharOperation.NO_CHAR;
						IType[] allTypes = workingCopy.getAllTypes();
						for (int j = 0, allTypesLength = allTypes.length; j < allTypesLength; j++) {
							IType type = allTypes[j];
							IJavaScriptElement parent = type.getParent();
							char[][] enclosingTypeNames;
							char[] qualification = packageDeclaration;
							if (parent instanceof IType) {
								char[] parentQualifiedName = ((IType)parent).getTypeQualifiedName('.').toCharArray();
								enclosingTypeNames = CharOperation.splitOn('.', parentQualifiedName);
								qualification = CharOperation.concat(qualification, parentQualifiedName);
							} else {
								enclosingTypeNames = CharOperation.NO_CHAR_CHAR;
							}
							char[] simpleName = type.getElementName().toCharArray();
							char suffix = IIndexConstants.TYPE_SUFFIX;
							if (type.isClass()) {
								suffix = IIndexConstants.CLASS_SUFFIX;
							}
							if (pattern.matchesDecodedKey(new QualifiedTypeDeclarationPattern(qualification, simpleName, suffix, matchRule))) {
								nameRequestor.acceptType(type.getFlags(), packageDeclaration, simpleName, enclosingTypeNames, path, null);
							}
						}
					} else {
						Parser basicParser = getParser();
						org.eclipse.wst.jsdt.internal.compiler.env.ICompilationUnit unit = (org.eclipse.wst.jsdt.internal.compiler.env.ICompilationUnit) workingCopy;
						CompilationResult compilationUnitResult = new CompilationResult(unit, 0, 0, this.compilerOptions.maxProblemsPerUnit);
						CompilationUnitDeclaration parsedUnit = basicParser.dietParse(unit, compilationUnitResult);
						if (parsedUnit != null) {
							basicParser.inferTypes(parsedUnit, null);
							final char[] packageDeclaration = parsedUnit.currentPackage == null
								? CharOperation.NO_CHAR
								: CharOperation.concatWith(parsedUnit.currentPackage.getImportName(), '.');
							class AllTypeDeclarationsVisitor extends ASTVisitor {
								public boolean visit(TypeDeclaration typeDeclaration, BlockScope blockScope) {
									return false; // no local/anonymous type
								}
								public boolean visit(TypeDeclaration typeDeclaration, CompilationUnitScope compilationUnitScope) {
									SearchPattern decodedPattern =
										new QualifiedTypeDeclarationPattern(packageDeclaration, typeDeclaration.name, convertTypeKind(TypeDeclaration.kind(typeDeclaration.modifiers)), matchRule);
									if (pattern.matchesDecodedKey(decodedPattern)) {
										nameRequestor.acceptType(typeDeclaration.modifiers, packageDeclaration, typeDeclaration.name, CharOperation.NO_CHAR_CHAR, path, null);
									}
									return true;
								}
								public boolean visit(TypeDeclaration memberTypeDeclaration, ClassScope classScope) {
									// compute encloising type names
									char[] qualification = packageDeclaration;
									TypeDeclaration enclosing = memberTypeDeclaration.enclosingType;
									char[][] enclosingTypeNames = CharOperation.NO_CHAR_CHAR;
									while (enclosing != null) {
										qualification = CharOperation.concat(qualification, enclosing.name, '.');
										enclosingTypeNames = CharOperation.arrayConcat(new char[][] {enclosing.name}, enclosingTypeNames);
										if ((enclosing.bits & ASTNode.IsMemberType) != 0) {
											enclosing = enclosing.enclosingType;
										} else {
											enclosing = null;
										}
									}
									SearchPattern decodedPattern =
										new QualifiedTypeDeclarationPattern(qualification, memberTypeDeclaration.name, convertTypeKind(TypeDeclaration.kind(memberTypeDeclaration.modifiers)), matchRule);
									if (pattern.matchesDecodedKey(decodedPattern)) {
										nameRequestor.acceptType(memberTypeDeclaration.modifiers, packageDeclaration, memberTypeDeclaration.name, enclosingTypeNames, path, null);
									}
									return true;
								}
								public boolean visit(InferredType inferredType, BlockScope scope) {
									SearchPattern decodedPattern =
										new QualifiedTypeDeclarationPattern(packageDeclaration, inferredType.getName(), convertTypeKind(TypeDeclaration.kind(0)), matchRule);
									if (pattern.matchesDecodedKey(decodedPattern)) {
										nameRequestor.acceptType(0, packageDeclaration, inferredType.getName(), CharOperation.NO_CHAR_CHAR, path, null);
									}
									return true;
								}
							}
							parsedUnit.traverse(new AllTypeDeclarationsVisitor(), parsedUnit.scope);
						}
					}
				}
			}
		} finally {
			if (progressMonitor != null) {
				progressMonitor.done();
			}
		}
	}

	public void searchDeclarations(IJavaScriptElement enclosingElement, SearchRequestor requestor, SearchPattern pattern, IProgressMonitor monitor) throws JavaScriptModelException {
		if (VERBOSE) {
			Util.verbose("	- java element: "+enclosingElement); //$NON-NLS-1$
		}
		IJavaScriptSearchScope scope = createJavaSearchScope(new IJavaScriptElement[] {enclosingElement});
		IResource resource = enclosingElement.getResource();
		if (enclosingElement instanceof IMember) {
			IMember member = (IMember) enclosingElement;
			IJavaScriptUnit cu = member.getJavaScriptUnit();
			if (cu != null) {
				resource = cu.getResource();
			} else if (member.isBinary()) {
				// binary member resource cannot be used as this
				// see bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=148215
				resource = null;
			}
		}
		try {
			if (resource instanceof IFile) {
				try {
					requestor.beginReporting();
					if (VERBOSE) {
						Util.verbose("Searching for " + pattern + " in " + resource.getFullPath()); //$NON-NLS-1$//$NON-NLS-2$
					}
					SearchParticipant participant = getDefaultSearchParticipant();
					SearchDocument[] documents = MatchLocator.addWorkingCopies(
						pattern,
						new SearchDocument[] {new JavaSearchDocument(enclosingElement.getPath().toString(), participant)},
						getWorkingCopies(enclosingElement),
						participant);
					participant.locateMatches(
						documents,
						pattern,
						scope,
						requestor,
						monitor);
				} finally {
					requestor.endReporting();
				}
			} else {
				search(
					pattern,
					new SearchParticipant[] {getDefaultSearchParticipant()},
					scope,
					requestor,
					monitor);
			}
		} catch (CoreException e) {
			if (e instanceof JavaScriptModelException)
				throw (JavaScriptModelException) e;
			throw new JavaScriptModelException(e);
		}
	}

	/**
	 * Searches for all declarations of the fields accessed in the given element.
	 * The element can be a compilation unit, a source type, or a source method.
	 * Reports the field declarations using the given requestor.
	 *
	 * @see org.eclipse.wst.jsdt.core.search.SearchEngine#searchDeclarationsOfAccessedFields(IJavaScriptElement, SearchRequestor, IProgressMonitor)
	 * 	for detailed comment
	 */
	public void searchDeclarationsOfAccessedFields(IJavaScriptElement enclosingElement, SearchRequestor requestor, IProgressMonitor monitor) throws JavaScriptModelException {
		if (VERBOSE) {
			Util.verbose("BasicSearchEngine.searchDeclarationsOfAccessedFields(IJavaScriptElement, SearchRequestor, SearchPattern, IProgressMonitor)"); //$NON-NLS-1$
		}
		SearchPattern pattern = new DeclarationOfAccessedFieldsPattern(enclosingElement);
		searchDeclarations(enclosingElement, requestor, pattern, monitor);
	}

	/**
	 * Searches for all declarations of the types referenced in the given element.
	 * The element can be a compilation unit, a source type, or a source method.
	 * Reports the type declarations using the given requestor.
	 *
	 * @see org.eclipse.wst.jsdt.core.search.SearchEngine#searchDeclarationsOfReferencedTypes(IJavaScriptElement, SearchRequestor, IProgressMonitor)
	 * 	for detailed comment
	 */
	public void searchDeclarationsOfReferencedTypes(IJavaScriptElement enclosingElement, SearchRequestor requestor, IProgressMonitor monitor) throws JavaScriptModelException {
		if (VERBOSE) {
			Util.verbose("BasicSearchEngine.searchDeclarationsOfReferencedTypes(IJavaScriptElement, SearchRequestor, SearchPattern, IProgressMonitor)"); //$NON-NLS-1$
		}
		SearchPattern pattern = new DeclarationOfReferencedTypesPattern(enclosingElement);
		searchDeclarations(enclosingElement, requestor, pattern, monitor);
	}

	/**
	 * Searches for all declarations of the methods invoked in the given element.
	 * The element can be a compilation unit, a source type, or a source method.
	 * Reports the method declarations using the given requestor.
	 *
	 * @see org.eclipse.wst.jsdt.core.search.SearchEngine#searchDeclarationsOfSentMessages(IJavaScriptElement, SearchRequestor, IProgressMonitor)
	 * 	for detailed comment
	 */
	public void searchDeclarationsOfSentMessages(IJavaScriptElement enclosingElement, SearchRequestor requestor, IProgressMonitor monitor) throws JavaScriptModelException {
		if (VERBOSE) {
			Util.verbose("BasicSearchEngine.searchDeclarationsOfSentMessages(IJavaScriptElement, SearchRequestor, SearchPattern, IProgressMonitor)"); //$NON-NLS-1$
		}
		SearchPattern pattern = new DeclarationOfReferencedMethodsPattern(enclosingElement);
		searchDeclarations(enclosingElement, requestor, pattern, monitor);
	}
	
	/**
	 * <p>Used to search all constructor declarations for ones that match the given type name using the given role,
	 * in the given scope, reporting to the given requester.</p>
	 * 
	 * @param typeNamePattern type name pattern to search for
	 * @param typeMatchRule Search pattern matching rule to use with the given <code>typeNamePattern</code>
	 * @param scope scope of the search
	 * @param nameRequester requester to report findings to
	 * @param waitingPolicy Policy to use when waiting for the index
	 * @param progressMonitor monitor to report index search progress to
	 * 
	 * @see SearchPattern#R_CAMELCASE_MATCH
	 * @see SearchPattern#R_CASE_SENSITIVE
	 * @see SearchPattern#R_EQUIVALENT_MATCH
	 * @see SearchPattern#R_EXACT_MATCH
	 * @see SearchPattern#R_FULL_MATCH
	 * @see SearchPattern#R_PATTERN_MATCH
	 * @see SearchPattern#R_PREFIX_MATCH
	 * @see SearchPattern#R_REGEXP_MATCH
	 * 
	 * @see IJavaScriptSearchConstants#FORCE_IMMEDIATE_SEARCH
	 * @see IJavaScriptSearchConstants#CANCEL_IF_NOT_READY_TO_SEARCH
	 * @see IJavaScriptSearchConstants#WAIT_UNTIL_READY_TO_SEARCH
	 */
	public void searchAllConstructorDeclarations(
			final char[] typeNamePattern,
			final int typeMatchRule,
			IJavaScriptSearchScope scope,
			final IConstructorRequestor nameRequester,
			int waitingPolicy,
			IProgressMonitor progressMonitor) {
		
		// Debug
		if (VERBOSE) {
			Util.verbose("BasicSearchEngine.searchAllConstructorDeclarations(char[], char[], int, IJavaSearchScope, IRestrictedAccessConstructorRequestor, int, IProgressMonitor)"); //$NON-NLS-1$
			Util.verbose("	- type name: "+(typeNamePattern==null?"null":new String(typeNamePattern))); //$NON-NLS-1$ //$NON-NLS-2$
			Util.verbose("	- type match rule: "+getMatchRuleString(typeMatchRule)); //$NON-NLS-1$
			Util.verbose("	- scope: "+scope); //$NON-NLS-1$
		}

		// Create pattern
		IndexManager indexManager = JavaModelManager.getJavaModelManager().getIndexManager();
		final ConstructorDeclarationPattern pattern = new ConstructorDeclarationPattern(
				typeNamePattern,
				typeMatchRule);

		// Index requester
		IndexQueryRequestor searchRequestor = new IndexQueryRequestor(){
			public boolean acceptIndexMatch(String documentPath, SearchPattern indexRecord, SearchParticipant participant, AccessRuleSet access) {
				// Filter unexpected types
				ConstructorDeclarationPattern record = (ConstructorDeclarationPattern)indexRecord;
				
				
				// Accept document path
				AccessRestriction accessRestriction = null;
				if (access != null) {
					// Compute document relative path
					int nameLength = record.declaringSimpleName==null ? 0 : record.declaringSimpleName.length;
					char[] path = new char[nameLength];
					int pos = 0;
					
					if (nameLength > 0) {
						System.arraycopy(record.declaringSimpleName, 0, path, pos, nameLength);
						pos += nameLength;
					}
					// Update access restriction if path is not empty
					if (pos > 0) {
						accessRestriction = access.getViolatedRestriction(path);
					}
				}
				nameRequester.acceptConstructor(
						record.modifiers,
						record.declaringSimpleName,
						record.parameterCount,
						record.parameterTypes,
						record.parameterNames,
						documentPath,
						accessRestriction);
				return true;
			}
		};

		try {
			if (progressMonitor != null) {
				progressMonitor.beginTask(Messages.engine_searching, 1000);
			}
			// Find constructor declarations from index
			indexManager.performConcurrentJob(
				new PatternSearchJob(
					pattern,
					getDefaultSearchParticipant(), // JavaScript search only
					scope,
					searchRequestor),
				waitingPolicy,
				progressMonitor == null ? null : new SubProgressMonitor(progressMonitor, 1000));
		} finally {
			if (progressMonitor != null) {
				progressMonitor.done();
			}
		}
	}
}
