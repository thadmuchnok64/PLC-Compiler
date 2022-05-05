package edu.ufl.cise.plc;

import java.io.Console;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.DelayQueue;

import javax.lang.model.element.ModuleElement.DirectiveKind;
import javax.print.DocFlavor.STRING;

import org.hamcrest.core.IsInstanceOf;
import org.junit.jupiter.api.Named;

import edu.ufl.cise.plc.IToken.Kind;
import edu.ufl.cise.plc.ast.ASTNode;
import edu.ufl.cise.plc.ast.ASTVisitor;
import edu.ufl.cise.plc.ast.AssignmentStatement;
import edu.ufl.cise.plc.ast.BinaryExpr;
import edu.ufl.cise.plc.ast.BooleanLitExpr;
import edu.ufl.cise.plc.ast.ColorConstExpr;
import edu.ufl.cise.plc.ast.ColorExpr;
import edu.ufl.cise.plc.ast.ConditionalExpr;
import edu.ufl.cise.plc.ast.ConsoleExpr;
import edu.ufl.cise.plc.ast.Declaration;
import edu.ufl.cise.plc.ast.Dimension;
import edu.ufl.cise.plc.ast.Expr;
import edu.ufl.cise.plc.ast.FloatLitExpr;
import edu.ufl.cise.plc.ast.IdentExpr;
import edu.ufl.cise.plc.ast.IntLitExpr;
import edu.ufl.cise.plc.ast.NameDef;
import edu.ufl.cise.plc.ast.NameDefWithDim;
import edu.ufl.cise.plc.ast.PixelSelector;
import edu.ufl.cise.plc.ast.Program;
import edu.ufl.cise.plc.ast.ReadStatement;
import edu.ufl.cise.plc.ast.ReturnStatement;
import edu.ufl.cise.plc.ast.StringLitExpr;
import edu.ufl.cise.plc.ast.Types.Type;
import edu.ufl.cise.plc.ast.UnaryExpr;
import edu.ufl.cise.plc.ast.UnaryExprPostfix;
import edu.ufl.cise.plc.ast.VarDeclaration;
import edu.ufl.cise.plc.ast.WriteStatement;

import static edu.ufl.cise.plc.ast.Types.Type.*;

public class TypeCheckVisitor implements ASTVisitor {

	SymbolTable symbolTable = new SymbolTable();
	Program root;

	record Pair<T0, T1> (T0 t0, T1 t1) {
	}; // may be useful for constructing lookup tables.

	private void check(boolean condition, ASTNode node, String message) throws TypeCheckException {
		if (!condition) {
			throw new TypeCheckException(message, node.getSourceLoc());
		}
	}

	// #region TYPE RETURNS

	// The type of a BooleanLitExpr is always BOOLEAN.
	// Set the type in AST Node for later passes (code generation)
	// Return the type for convenience in this visitor.
	@Override
	public Object visitBooleanLitExpr(BooleanLitExpr booleanLitExpr, Object arg) throws Exception {
		booleanLitExpr.setType(Type.BOOLEAN);
		return Type.BOOLEAN;
	}

	@Override
	public Object visitStringLitExpr(StringLitExpr stringLitExpr, Object arg) throws Exception {
		stringLitExpr.setType(Type.STRING);
		return Type.STRING;
	}

	@Override
	public Object visitIntLitExpr(IntLitExpr intLitExpr, Object arg) throws Exception {
		intLitExpr.setType(Type.INT);
		return Type.INT;
	}

	@Override
	public Object visitFloatLitExpr(FloatLitExpr floatLitExpr, Object arg) throws Exception {
		floatLitExpr.setType(Type.FLOAT);
		return Type.FLOAT;
	}

	@Override
	public Object visitColorConstExpr(ColorConstExpr colorConstExpr, Object arg) throws Exception {
		colorConstExpr.setType(Type.COLOR);
		return Type.COLOR;
	}

	@Override
	public Object visitConsoleExpr(ConsoleExpr consoleExpr, Object arg) throws Exception {
		consoleExpr.setType(Type.CONSOLE);
		return Type.CONSOLE;
	}

	// Visits the child expressions to get their type (and ensure they are correctly
	// typed)
	// then checks the given conditions.
	@Override
	public Object visitColorExpr(ColorExpr colorExpr, Object arg) throws Exception {
		Type redType = (Type) colorExpr.getRed().visit(this, arg);
		Type greenType = (Type) colorExpr.getGreen().visit(this, arg);
		Type blueType = (Type) colorExpr.getBlue().visit(this, arg);
		check(redType == greenType && redType == blueType, colorExpr, "color components must have same type");
		check(redType == Type.INT || redType == Type.FLOAT, colorExpr, "color component type must be int or float");
		Type exprType = (redType == Type.INT) ? Type.COLOR : Type.COLORFLOAT;
		colorExpr.setType(exprType);
		return exprType;
	}

	// #endregion

	// Maps forms a lookup table that maps an operator expression pair into result
	// type.
	// This more convenient than a long chain of if-else statements.
	// Given combinations are legal; if the operator expression pair is not in the
	// map, it is an error.
	Map<Pair<Kind, Type>, Type> unaryExprs = Map.of(
			new Pair<Kind, Type>(Kind.BANG, BOOLEAN), BOOLEAN,
			new Pair<Kind, Type>(Kind.MINUS, FLOAT), FLOAT,
			new Pair<Kind, Type>(Kind.MINUS, INT), INT,
			new Pair<Kind, Type>(Kind.COLOR_OP, INT), INT,
			new Pair<Kind, Type>(Kind.COLOR_OP, COLOR), INT,
			new Pair<Kind, Type>(Kind.COLOR_OP, IMAGE), IMAGE,
			new Pair<Kind, Type>(Kind.IMAGE_OP, IMAGE), INT);

	// Visits the child expression to get the type, then uses the above table to
	// determine the result type
	// and check that this node represents a legal combination of operator and
	// expression type.
	@Override
	public Object visitUnaryExpr(UnaryExpr unaryExpr, Object arg) throws Exception {
		// !, -, getRed, getGreen, getBlue
		Kind op = unaryExpr.getOp().getKind();
		Type exprType = (Type) unaryExpr.getExpr().visit(this, arg);
		// Use the lookup table above to both check for a legal combination of operator
		// and expression, and to get result type.
		Type resultType = unaryExprs.get(new Pair<Kind, Type>(op, exprType));
		check(resultType != null, unaryExpr, "incompatible types for unaryExpr");
		// Save the type of the unary expression in the AST node for use in code
		// generation later.
		unaryExpr.setType(resultType);
		// return the type for convenience in this visitor.
		return resultType;
	}

	// This method has several cases. Work incrementally and test as you go.
	@Override
	public Object visitBinaryExpr(BinaryExpr binaryExpr, Object arg) throws Exception {

		Kind op = binaryExpr.getOp().getKind();

		Type leftType = (Type) binaryExpr.getLeft().visit(this, arg);
		Type rightType = (Type) binaryExpr.getRight().visit(this, arg);
		Type resultType = null;
		if(rightType==null&&leftType==null){
			return Type.INT;
		}
		switch (op) {// AND, OR, PLUS, MINUS, TIMES, DIV, MOD, EQUALS, NOT_EQUALS, LT, LE, GT,GE
			case EQUALS, NOT_EQUALS -> {
				check(leftType == rightType, binaryExpr, "incompatible types for comparison");
				resultType = Type.BOOLEAN;
			}
			case PLUS,MINUS -> {
				if (leftType == Type.INT && rightType == Type.INT)
					resultType = Type.INT;
				else if (leftType == Type.FLOAT||leftType == Type.INT && rightType == Type.FLOAT||rightType == Type.INT)
					resultType = Type.FLOAT;
				else if (leftType == Type.IMAGE && rightType == Type.IMAGE)
					resultType = Type.IMAGE;
				else if(leftType == Type.COLOR&&rightType == Type.COLOR){
					resultType = Type.COLOR;
				}
				else if(leftType==Type.COLORFLOAT||rightType==Type.COLORFLOAT)
					resultType = Type.COLORFLOAT;
				else
					check(false, binaryExpr, "incompatible types for operator");
			}
			case TIMES,DIV,MOD -> {
				if (leftType == Type.INT && rightType == Type.INT)
					resultType = Type.INT;
				else if (leftType == Type.INT && rightType == Type.INT)
					resultType = Type.INT;
				else if (leftType == Type.FLOAT||leftType == Type.INT && rightType == Type.FLOAT||rightType == Type.INT)
					resultType = Type.FLOAT;
				else if (leftType == Type.IMAGE && rightType == Type.IMAGE)
					resultType = Type.IMAGE;
					else if (leftType == Type.IMAGE && (rightType == Type.INT||rightType == Type.FLOAT)){
						resultType = Type.IMAGE;
					}else if(leftType == Type.COLOR&&rightType == Type.COLOR){
						resultType = Type.COLOR;
					}
				else if(leftType==Type.COLORFLOAT||rightType==Type.COLORFLOAT)
					resultType = Type.COLORFLOAT;
				else if (leftType == Type.BOOLEAN && rightType == Type.BOOLEAN)
					resultType = Type.BOOLEAN;
					else if (leftType == Type.IMAGE && (rightType == Type.INT || rightType == Type.FLOAT))
					resultType = Type.IMAGE;
					else if (leftType == Type.COLOR && (rightType == Type.COLOR||rightType == Type.FLOAT||rightType == Type.INT))
					resultType = Type.COLOR;
				else if ((leftType == Type.COLOR||leftType == Type.FLOAT||leftType == Type.INT)&& rightType == Type.COLOR)
					resultType = Type.COLOR;
				else
					check(false, binaryExpr, "incompatible types for operator");
			}
			case LT, LE, GT, GE -> {
				if (leftType == rightType)
					resultType = Type.BOOLEAN;
				else if(leftType == Type.INT || leftType == Type.FLOAT && rightType == Type.INT || rightType == Type.FLOAT)
					resultType = Type.BOOLEAN;
				else
					check(false, binaryExpr, "incompatible types for operator");
			}
			default -> {
				throw new Exception("compiler error");
			}
		}
		binaryExpr.setType(resultType);
		//if(leftType != rightType)
		//binaryExpr.setCoerceTo(resultType);
		return resultType;
	}

	@Override
	public Object visitIdentExpr(IdentExpr identExpr, Object arg) throws Exception {
		String name = identExpr.getText();
	//	if(arg instanceof Declaration){
	//		symbolTable.lookup(name).setInitialized(true);
	//		symbolTable.lookup())
	//	}
		Declaration dec = symbolTable.lookup(name);
		if(arg instanceof Boolean){
			if((Boolean) arg == true){
				//Type type = dec.getType();
				if(symbolTable.contains(name)){
					Type type = dec.getType();
					identExpr.setType(type);
					return type;
				}
				return null;
		}
		}
		if (arg instanceof Type) {
			if ((Type) arg == BOOLEAN && dec.getType() == INT) {
				identExpr.setCoerceTo(INT);

			} else {
				identExpr.setCoerceTo((Type) arg);
			}
		}
		check(dec != null, identExpr, "undefined identifier " + name);
		check(dec.isInitialized(), identExpr, "using uninitialized variable");
		identExpr.setDec(dec); // save declaration--will be useful later.
		Type type = dec.getType();
		identExpr.setType(type);
		return type;
	}

	@Override
	public Object visitConditionalExpr(ConditionalExpr conditionalExpr, Object arg) throws Exception {
		conditionalExpr.getTrueCase().visit(this, arg);
		conditionalExpr.getFalseCase().visit(this, arg);
		conditionalExpr.getCondition().visit(this, arg);
		if (conditionalExpr.getCondition().getType() == Type.BOOLEAN||(arg instanceof Boolean&&(boolean)arg==true)
				&& conditionalExpr.getTrueCase().getType() == conditionalExpr.getFalseCase().getType()) {
					conditionalExpr.setType(conditionalExpr.getTrueCase().getType());

					//if (arg instanceof Type && ((Type) arg) == conditionalExpr.getTrueCase().getType())
				return conditionalExpr.getType();

		}
		throw new TypeCheckException("your return types in your condition are kinda quirky");
		// throw new UnsupportedOperationException();
	}

	@Override
	public Object visitDimension(Dimension dimension, Object arg) throws Exception {
		Type xType = (Type) dimension.getWidth().visit(this, arg);
		check(xType == Type.INT, dimension.getWidth(), "only ints as dimension components");
		Type yType = (Type) dimension.getHeight().visit(this, arg);
		check(yType == Type.INT, dimension.getHeight(), "only ints as dimension components");
		return null;
	}

	@Override
	// This method can only be used to check PixelSelector objects on the right hand
	// side of an assignment.
	// Either modify to pass in context info and add code to handle both cases, or
	// when on left side
	// of assignment, check fields from parent assignment statement.
	public Object visitPixelSelector(PixelSelector pixelSelector, Object arg) throws Exception {
		Type xType = (Type) pixelSelector.getX().visit(this, arg);
		check(xType == Type.INT, pixelSelector.getX(), "only ints as pixel selector components");
		Type yType = (Type) pixelSelector.getY().visit(this, arg);
		check(yType == Type.INT, pixelSelector.getY(), "only ints as pixel selector components");
		return null;
	}

	@Override
	// This method several cases--you don't have to implement them all at once.
	// Work incrementally and systematically, testing as you go.
	public Object visitAssignmentStatement(AssignmentStatement assignmentStatement, Object arg) throws Exception {

		String localVar1 = null;
		String localVar2 = null;

		if((symbolTable.lookup(assignmentStatement.getName()) != null)&&assignmentStatement.getTargetDec()==null){
			assignmentStatement.setTargetDec(symbolTable.lookup(assignmentStatement.getName()));
		}
		
		if(assignmentStatement.getSelector()!=null && (symbolTable.lookup(assignmentStatement.getName()) != null)){

			//	Declaration d = new VarDeclaration(assignmentStatement.getFirstToken(), nameDef, op, expr) symbolTable.lookup(assignmentStatement.getName()).getDim().getWidth();
			localVar1 = assignmentStatement.getSelector().getX().getText();
			localVar2 = assignmentStatement.getSelector().getY().getText();


			symbolTable.insert(localVar1,new NameDef(null, "int",localVar1));
			symbolTable.insert(localVar2,new NameDef(null, "int",localVar2));


			assignmentStatement.getSelector().getX().visit(this, true);
			assignmentStatement.getSelector().getY().visit(this, true);

			assignmentStatement.getExpr().visit(this, true);


			
		} else{

		assignmentStatement.getExpr().visit(this, arg);
		}

		if (symbolTable.lookup(assignmentStatement.getName()) == null) {
			assignmentStatement.setTargetDec(new NameDef(assignmentStatement.getFirstToken(),
					assignmentStatement.getExpr().getType().toString().toLowerCase(), assignmentStatement.getName()));
			symbolTable.insert(assignmentStatement.getName(), assignmentStatement.getTargetDec());
			symbolTable.lookup(assignmentStatement.getName()).setInitialized(true);

		} else {
			
			if (symbolTable.lookup(assignmentStatement.getName()).getType() == assignmentStatement.getExpr()
					.getType()) {
				symbolTable.lookup(assignmentStatement.getName()).setInitialized(true);
			} else {
				if (symbolTable.lookup(assignmentStatement.getName()).getType() == IMAGE
						&& assignmentStatement.getExpr() instanceof IntLitExpr) {
					assignmentStatement.getExpr().setCoerceTo(COLOR);
					symbolTable.lookup(assignmentStatement.getName()).setInitialized(true);
				} else{
					assignmentStatement.getExpr().setCoerceTo(symbolTable.lookup(assignmentStatement.getName()).getType());
					symbolTable.lookup(assignmentStatement.getName()).setInitialized(true);
					assignmentStatement.setTargetDec(new NameDef(assignmentStatement.getFirstToken(),
					symbolTable.lookup(assignmentStatement.getName()).getType().toString().toLowerCase(), assignmentStatement.getName()));


				}
			}
			// throw new TypeCheckException("you uhh what, uhh hwat, what in the name of sam
			// hell did you try to do right there. you do realize how stupid that mistake
			// you made was, right?");
		}
		if(localVar1!=null){
			symbolTable.remove(localVar1);
		}
		if(localVar2!=null){
			symbolTable.remove(localVar2);
		}
		return null;
	}

	@Override
	public Object visitWriteStatement(WriteStatement writeStatement, Object arg) throws Exception {
		Type sourceType = (Type) writeStatement.getSource().visit(this, true);
		Type destType = (Type) writeStatement.getDest().visit(this, arg);
		check(destType == Type.STRING || destType == Type.CONSOLE, writeStatement,
				"illegal destination type for write");
		check(sourceType != Type.CONSOLE, writeStatement, "illegal source type for write");
		return null;
	}

	@Override
	public Object visitReadStatement(ReadStatement readStatement, Object arg) throws Exception {
		try {
			readStatement.getSource().visit(this, arg);
			if (readStatement.getSource().getType() != STRING && readStatement.getSource().getType() != CONSOLE) {
				throw new TypeCheckException("stinky");
			}
			if (symbolTable.lookup(readStatement.getName()) == null) {
				readStatement.setTargetDec(new NameDef(readStatement.getFirstToken(),
						readStatement.getSource().getType().toString().toLowerCase(), readStatement.getName()));
				symbolTable.insert(readStatement.getName(), readStatement.getTargetDec());
				symbolTable.lookup(readStatement.getName()).setInitialized(true);

			} else {
				if (symbolTable.lookup(readStatement.getName()).getType() == readStatement.getSource().getType()) {
					symbolTable.lookup(readStatement.getName()).setInitialized(true);
				} else {
					if (symbolTable.lookup(readStatement.getName()).getType() == IMAGE
							&& readStatement.getSource().getType() == INT) {
						readStatement.getSource().setCoerceTo(COLOR);
						symbolTable.lookup(readStatement.getName()).setInitialized(true);
						readStatement.setTargetDec(new NameDef(readStatement.getFirstToken(),
								readStatement.getSource().getType().toString().toLowerCase(), readStatement.getName()));
					} else if (symbolTable.lookup(readStatement.getName()).getType() == IMAGE
					&& readStatement.getSource().getType() == STRING) {
				readStatement.getSource().setCoerceTo(IMAGE);
				symbolTable.lookup(readStatement.getName()).setInitialized(true);
				readStatement.setTargetDec(symbolTable.lookup(readStatement.getName()));
				//readStatement.setTargetDec(new NameDefWithDim(readStatement.getFirstToken(),readStatement.getSource().getType().toString().toLowerCase(), readStatement.getName(), dim);
			}	else if (symbolTable.lookup(readStatement.getName()).getType() == INT
							&& readStatement.getSource().getType() == STRING) {
						readStatement.setTargetDec(new NameDef(readStatement.getFirstToken(),
								symbolTable.lookup(readStatement.getName()).getType().toString().toLowerCase(),
								readStatement.getName()));
						readStatement.getSource().setCoerceTo(STRING);
						symbolTable.lookup(readStatement.getName()).setInitialized(true);
					} else if (symbolTable.lookup(readStatement.getName()).getType() == INT
							&& readStatement.getSource().getType() == CONSOLE) {
						readStatement.setTargetDec(new NameDef(readStatement.getFirstToken(),
								symbolTable.lookup(readStatement.getName()).getType().toString().toLowerCase(),
								readStatement.getName()));
						readStatement.getSource().setCoerceTo(INT);
						symbolTable.lookup(readStatement.getName()).setInitialized(true);
					} else if(symbolTable.lookup(readStatement.getName()).getType() == FLOAT
					&&(readStatement.getSource().getType()==STRING||readStatement.getSource().getType()==CONSOLE)){
						readStatement.setTargetDec(new NameDef(readStatement.getFirstToken(),
						symbolTable.lookup(readStatement.getName()).getType().toString().toLowerCase(),
						readStatement.getName()));
				readStatement.getSource().setCoerceTo(FLOAT);
				symbolTable.lookup(readStatement.getName()).setInitialized(true);
					} else if(symbolTable.lookup(readStatement.getName()).getType() == BOOLEAN
					&&(readStatement.getSource().getType()==STRING||readStatement.getSource().getType()==CONSOLE)){
						readStatement.setTargetDec(new NameDef(readStatement.getFirstToken(),
						symbolTable.lookup(readStatement.getName()).getType().toString().toLowerCase(),
						readStatement.getName()));
				readStatement.getSource().setCoerceTo(BOOLEAN);
				symbolTable.lookup(readStatement.getName()).setInitialized(true);
					} else if(symbolTable.lookup(readStatement.getName()).getType() == STRING
					&&(readStatement.getSource().getType()==STRING||readStatement.getSource().getType()==CONSOLE)){
						readStatement.setTargetDec(new NameDef(readStatement.getFirstToken(),
						symbolTable.lookup(readStatement.getName()).getType().toString().toLowerCase(),
						readStatement.getName()));
				readStatement.getSource().setCoerceTo(STRING);
				symbolTable.lookup(readStatement.getName()).setInitialized(true);
					} else if(symbolTable.lookup(readStatement.getName()).getType() == COLOR
					&&(readStatement.getSource().getType()==STRING||readStatement.getSource().getType()==CONSOLE)){
						readStatement.setTargetDec(new NameDef(readStatement.getFirstToken(),
						symbolTable.lookup(readStatement.getName()).getType().toString().toLowerCase(),
						readStatement.getName()));
				readStatement.getSource().setCoerceTo(COLOR);
				symbolTable.lookup(readStatement.getName()).setInitialized(true);
					}
				}
				// throw new TypeCheckException("you uhh what, uhh hwat, what in the name of sam
				// hell did you try to do right there. you do realize how stupid that mistake
				// you made was, right?");
			}
			return null;
		} catch (Exception e) {
			throw new TypeCheckException("What happened here? Did you use the right type for reading?");
		}
	}

	@Override
	public Object visitVarDeclaration(VarDeclaration declaration, Object arg) throws Exception {
		try {
			if(declaration.getExpr() instanceof ConsoleExpr){
				declaration.getExpr().setCoerceTo(declaration.getType());
			}

			if (symbolTable.lookup(declaration.getName()) == null) {
				if (declaration.getOp() != null && (declaration.getOp().getKind() == Kind.ASSIGN||declaration.getOp().getKind() == Kind.LARROW)) {
					declaration.setInitialized(true);
					if(declaration.getExpr()!=null){
						declaration.getExpr().visit(this, arg);
					}
					if (declaration.getExpr() instanceof IdentExpr) {
						
						visitIdentExpr((IdentExpr) declaration.getExpr(), declaration.getNameDef().getType());
						
						// declaration.setCoerceTo();
					}
				}
			//	if(declaration.getExpr() instanceof UnaryExpr&&((UnaryExpr)declaration.getExpr()).getExpr() instanceof IdentExpr){
			//		Dimension d = symbolTable.lookup(((IdentExpr)((UnaryExpr)declaration.getExpr()).getExpr()).getText()).getDim();
			//		declaration.
			//	}
				symbolTable.insert(declaration.getName(), declaration);
				return null;
			} else {
				if (declaration.getOp().getKind() == Kind.EQUALS) {
					symbolTable.lookup(declaration.getName()).setInitialized(true);
				}
				throw new TypeCheckException(
						"There is already a variable for " + declaration.getName() + ", you absolute chimp brain");
			}
		} catch (Exception e) {
			throw new TypeCheckException("You have commited a great atrocity in this code.");

		}
	}

	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {

		// Save root of AST so return type can be accessed in return statements
		root = program;

		// Check declarations and statements
		List<ASTNode> decsAndStatements = program.getDecsAndStatements();
		for (ASTNode node : program.getParams()) {
			node.visit(this, true);
		}
		for (ASTNode node : decsAndStatements) {
			node.visit(this, arg);
		}
		return program;
	}

	@Override
	public Object visitNameDef(NameDef nameDef, Object arg) throws Exception {
		if (symbolTable.lookup(nameDef.getName()) == null) {
			symbolTable.insert(nameDef.getName(), nameDef);
			if (arg instanceof Boolean) {
				symbolTable.lookup(nameDef.getName()).setInitialized((boolean) arg);
			}
			return null;
		} else {
			throw new TypeCheckException(
					"There is already a variable for " + nameDef.getName() + ", you absolute neanderthal");
		}
	}

	@Override
	public Object visitNameDefWithDim(NameDefWithDim nameDefWithDim, Object arg) throws Exception {
		if (symbolTable.lookup(nameDefWithDim.getName()) == null) {
			symbolTable.insert(nameDefWithDim.getName(), nameDefWithDim);
			if (arg instanceof Boolean) {
				symbolTable.lookup(nameDefWithDim.getName()).setInitialized((boolean) arg);
			}
			return null;
		} else {
			throw new TypeCheckException(
					"There is already a variable for " + nameDefWithDim.getName() + ", you absolute neanderthal");
		}
	}

	@Override
	public Object visitReturnStatement(ReturnStatement returnStatement, Object arg) throws Exception {
		Type returnType = root.getReturnType(); // This is why we save program in visitProgram.
		Type expressionType = (Type) returnStatement.getExpr().visit(this, returnType);
		if (!(returnStatement.getExpr() instanceof ConditionalExpr))
			check(returnType == expressionType, returnStatement, "return statement with invalid type");
		returnStatement.getExpr().setType(returnType);
		return null;
	}

	@Override
	public Object visitUnaryExprPostfix(UnaryExprPostfix unaryExprPostfix, Object arg) throws Exception {
		Type expType = (Type) unaryExprPostfix.getExpr().visit(this, null);
		check(expType == Type.IMAGE, unaryExprPostfix, "pixel selector can only be applied to image");
		unaryExprPostfix.getSelector().visit(this, arg);
		unaryExprPostfix.setType(Type.INT);
		unaryExprPostfix.setCoerceTo(COLOR);
		return Type.COLOR;
	}

}
