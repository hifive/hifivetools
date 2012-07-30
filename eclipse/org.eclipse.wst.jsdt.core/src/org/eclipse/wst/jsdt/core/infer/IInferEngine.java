package org.eclipse.wst.jsdt.core.infer;

import org.eclipse.wst.jsdt.internal.compiler.ast.CompilationUnitDeclaration;

/**
 * Engine for inferring types from compilation unit.
 * 
 * <p>Clients may implement this interface but should expect some breakage by future releases.</p>
 * 
 * Provisional API: This class/interface is part of an interim API that is still under development and expected to 
 * change significantly before reaching stability. It is being made available at this early stage to solicit feedback 
 * from pioneering adopters on the understanding that any code that uses this API will almost certainly be broken 
 * (repeatedly) as the API evolves.
 * 
 * @since 3.2
 */
public interface IInferEngine {

	public final static char[] ANONYMOUS_PREFIX = {'_','_','_'};
	
	public static final char[] ANONYMOUS_CLASS_ID= {'a','n','o','n','y','m','o','u','s'};
	
	/**
     * Initializes inference engine. Always called before {@link #setCompilationUnit()}
     * to let engine prepare for next compilation unit.
     */
	void initialize();

	/**
     * Set compilation unit for processing.
     */
	void setCompilationUnit(CompilationUnitDeclaration parsedUnit);

	/**
     * Requests to perform type inference on provided compilation unit. Always called
     * after {@link #setCompilationUnit(CompilationUnitDeclaration)}
     */
	void doInfer();

	/**
     * Provides inference options to the engine.
     */
	void initializeOptions(InferOptions inferOptions);
}
