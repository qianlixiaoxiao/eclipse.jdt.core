/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.problem.AbortCompilation;

public class ArrayQualifiedTypeReference extends QualifiedTypeReference {
	int dimensions;
	
	public ArrayQualifiedTypeReference(char[][] sources , int dim, long[] poss) {
		
		super( sources , poss);
		dimensions = dim ;
	}
	
	public int dimensions() {
		
		return dimensions;
	}
	
	protected TypeBinding getTypeBinding(Scope scope) {
		
		if (this.resolvedType != null)
			return this.resolvedType;
		if (dimensions > 255) {
			scope.problemReporter().tooManyDimensions(this);
		}
		try {
			TypeBinding leafComponentType = scope.getType(this.tokens, this.tokens.length);
			if (leafComponentType.isParameterizedType()) {
			    scope.problemReporter().illegalArrayOfParameterizedType(leafComponentType, this);
			}
			return scope.createArrayType(leafComponentType, dimensions);
		} catch (AbortCompilation e) {
			e.updateContext(this, scope.referenceCompilationUnit().compilationResult);
			throw e;
		}
	}
	
	public StringBuffer printExpression(int indent, StringBuffer output){
		
		super.printExpression(indent, output);
		for (int i = 0 ; i < dimensions ; i++) {
			output.append("[]"); //$NON-NLS-1$
		}
		return output;
	}
	
	public void traverse(ASTVisitor visitor, BlockScope scope) {
		
		visitor.visit(this, scope);
		visitor.endVisit(this, scope);
	}
	
	public void traverse(ASTVisitor visitor, ClassScope scope) {
		
		visitor.visit(this, scope);
		visitor.endVisit(this, scope);
	}
}
