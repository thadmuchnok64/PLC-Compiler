package edu.ufl.cise.plc.ast;

import edu.ufl.cise.plc.IToken;
import edu.ufl.cise.plc.ast.Types.Type;

public abstract class Declaration extends ASTNode {

	public Declaration(IToken firstToken) {
		super(firstToken);
	}
	
}
