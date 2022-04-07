package edu.ufl.cise.plc;

import java.util.List;
import java.util.Map;
import java.util.Set;

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
import edu.ufl.cise.plc.runtime.ConsoleIO;
import edu.ufl.cise.plc.ast.UnaryExpr;
import edu.ufl.cise.plc.ast.UnaryExprPostfix;
import edu.ufl.cise.plc.ast.VarDeclaration;
import edu.ufl.cise.plc.ast.WriteStatement;

public class CodeGenVisitor implements ASTVisitor {


    String packageName;
    public CodeGenVisitor(String _packageName) {
        packageName = _packageName;
    }

    

    @Override
    public Object visitBooleanLitExpr(BooleanLitExpr booleanLitExpr, Object arg) throws Exception {
        return booleanLitExpr.getText();
    }

    @Override
    public Object visitStringLitExpr(StringLitExpr stringLitExpr, Object arg) throws Exception {
        
        return stringLitExpr.getText();
    }

    @Override
    public Object visitIntLitExpr(IntLitExpr intLitExpr, Object arg) throws Exception {
        return intLitExpr.getValue();
    }

    @Override
    public Object visitFloatLitExpr(FloatLitExpr floatLitExpr, Object arg) throws Exception {
        return ""+floatLitExpr.getText()+"f";
    }

    @Override
    public Object visitColorConstExpr(ColorConstExpr colorConstExpr, Object arg) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object visitConsoleExpr(ConsoleExpr consoleExpr, Object arg) throws Exception {
        //return ConsoleIO.readValueFromConsole("INT", "HUH");
        return "ConsoleIO.readValueFromConsole(\""+consoleExpr.getCoerceTo().toString()+"\", \"Please enter a value\")";
    }

    @Override
    public Object visitColorExpr(ColorExpr colorExpr, Object arg) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object visitUnaryExpr(UnaryExpr unaryExpression, Object arg) throws Exception {
        return ""+unaryExpression.getFirstToken().getText()+unaryExpression.getExpr().visit(this, arg);
    }

    @Override
    public Object visitBinaryExpr(BinaryExpr binaryExpr, Object arg) throws Exception {
        
        
        String s ="";
        if(binaryExpr.getType()!=null)
        s = ""+binaryExpr.getType().name();
        String x = ""+binaryExpr.getCoerceTo();

        String con = "";
        con = ""+binaryExpr.getRight().visit(this,arg);

        String fuck = "";
        fuck = ""+binaryExpr.getLeft().visit(this,arg);

        if(con.equals("CONDITION")){

            //conditionalStatements[0] = conditionalExpr.getCondition().visit(this, arg);

            //true case
            conditionalStatements[1] = binaryExpr.getLeft().visit(this,arg) + " "+ binaryExpr.getOp().getText()+" "+conditionalStatements[1];
    
            //false case
            conditionalStatements[2] = binaryExpr.getLeft().visit(this,arg) + " "+ binaryExpr.getOp().getText()+" "+ conditionalStatements[2];

           return "CONDITION";

            // return "if (" + conditionalStatements[0] +") { "+binaryExpr.getLeft().visit(this,arg) + " "+ binaryExpr.getOp().getText() + conditionalStatements[1] +"; } else { "+ binaryExpr.getLeft().visit(this,arg) + " "+ binaryExpr.getOp().getText() + conditionalStatements[2]+"; }";

        } else if(fuck.equals("CONDITION")){

            conditionalStatements[1] = conditionalStatements[1] + " "+ binaryExpr.getOp().getText()+" "+binaryExpr.getRight().visit(this,arg);
    
            //false case
            conditionalStatements[2] = conditionalStatements[2] + " "+ binaryExpr.getOp().getText()+" "+ binaryExpr.getRight().visit(this,arg);

           return "CONDITION";

        }
        return "(" + binaryExpr.getLeft().visit(this,arg) + " "+ binaryExpr.getOp().getText()+" " + binaryExpr.getRight().visit(this,arg)+")";
        
    }

    @Override
    public Object visitIdentExpr(IdentExpr identExpr, Object arg) throws Exception {
        return identExpr.getText();
    }

    Object[] conditionalStatements ={"","",""};;

    @Override
    public Object visitConditionalExpr(ConditionalExpr conditionalExpr, Object arg) throws Exception {

        //Object[] conditionalStrings = new String[3];

        // conditional
        conditionalStatements[0] = conditionalExpr.getCondition().visit(this, arg);

        //true case
        conditionalStatements[1] = conditionalExpr.getTrueCase().visit(this, arg);

        //false case
        conditionalStatements[2] = conditionalExpr.getFalseCase().visit(this, arg);



        return "CONDITION";
    }

    @Override
    public Object visitDimension(Dimension dimension, Object arg) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object visitPixelSelector(PixelSelector pixelSelector, Object arg) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object visitAssignmentStatement(AssignmentStatement assignmentStatement, Object arg) throws Exception {
        String con = "";
        if(assignmentStatement.getTargetDec().getType()!=assignmentStatement.getExpr().getType()){
            con = "("+convertTypeToString(assignmentStatement.getTargetDec().getType().toString().toLowerCase())+")";
        }
        return assignmentStatement.getName() + " = " +con+ assignmentStatement.getExpr().visit(this, arg) +";";
    }

    @Override
    public Object visitWriteStatement(WriteStatement writeStatement, Object arg) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object visitReadStatement(ReadStatement readStatement, Object arg) throws Exception {
        return readStatement.getName() + " = " + readStatement.getSource().visit(this, arg)+";";
    }

public String convertTypeToString(String s){
    
    switch(s){
        case "string":
            s = "String";
            break;
            default:
            break;
    }

    return s;
}

    @Override
    public Object visitProgram(Program program, Object arg) throws Exception {
        String s;
        //"int y() ^42;"
       // s ="package " + packageName+ ";" + "public static int apply(){return 42;}}";

       s ="package " + packageName+ ";" +"public class "+ program.getName() + "{ ";
        String type = program.getReturnType().toString().toLowerCase();
        
        type = convertTypeToString(type);
 
       s += " public static " + type+ " apply(";

       List<ASTNode> decsAndStatements = program.getDecsAndStatements();
      
       boolean multiple = false;
       for (ASTNode node : program.getParams()) {
         if(multiple){
            s+=", ";
         }
         multiple = true;
        s+= node.visit(this, true);
          
       }

       s+= "){ ";
       for (ASTNode node : decsAndStatements) {
          s+= node.visit(this, arg);
       }

       s+= "}}";

       
       
        return s;
    }


    @Override
    public Object visitNameDef(NameDef nameDef, Object arg) throws Exception {
        return convertTypeToString(nameDef.getType().toString().toLowerCase())+" "+nameDef.getName();
    }

    @Override
    public Object visitNameDefWithDim(NameDefWithDim nameDefWithDim, Object arg) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object visitReturnStatement(ReturnStatement returnStatement, Object arg) throws Exception {
        Object expr;


        expr = returnStatement.getExpr().visit(this, arg);
        if(expr == "CONDITION"){
            return "if (" + conditionalStatements[0] +") { return " + conditionalStatements[1] +"; } else { return " + conditionalStatements[2]+"; }";
        } else{
        return "return " + expr +";";
        }



    }

    @Override
    public Object visitVarDeclaration(VarDeclaration declaration, Object arg) throws Exception {
        //String s =  ""+convertTypeToString(declaration.getType().toString().toLowerCase()) +" " +declaration.getName()+"; ";
        String typeCon ="";
        if(declaration.isInitialized()&&declaration.getExpr()!=null){
            if(declaration.getType()!=declaration.getExpr().getType()){
                typeCon = "("+convertTypeToString(declaration.getType().toString().toLowerCase())+")";
            }
            if(declaration.getExpr().visit(this, arg)=="CONDITION"){
                return ""+convertTypeToString(declaration.getType().toString().toLowerCase()) +" " +declaration.getName()+";  if (" + conditionalStatements[0] +") { " +declaration.getName() + " = "+ typeCon+conditionalStatements[1] + "; } else { " +declaration.getName() + " = "+ typeCon+ conditionalStatements[2]+"; }";
            } else{
            return ""+convertTypeToString(declaration.getType().toString().toLowerCase()) +" " +declaration.getName() +" = "+typeCon+declaration.getExpr().visit(this, arg)+"; ";
            }
        } else {
            return ""+convertTypeToString(declaration.getType().toString().toLowerCase()) +" " +declaration.getName()+"; ";
        }

       // s +="; ";
       // return s;
    }

    @Override
    public Object visitUnaryExprPostfix(UnaryExprPostfix unaryExprPostfix, Object arg) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }


}
