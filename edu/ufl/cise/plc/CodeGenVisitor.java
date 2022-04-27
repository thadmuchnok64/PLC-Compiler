package edu.ufl.cise.plc;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.ModuleElement.DirectiveKind;
import javax.print.DocFlavor.STRING;
import javax.xml.transform.Source;

import org.hamcrest.core.IsInstanceOf;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.params.shadow.com.univocity.parsers.annotations.Replace;

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
import edu.ufl.cise.plc.runtime.ColorTuple;
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

    boolean usesColor = false;
    @Override
    public Object visitColorConstExpr(ColorConstExpr colorConstExpr, Object arg) throws Exception {
        usesColor = true;
        return "ColorTuple.unpack(Color."+colorConstExpr.getText()+".getRGB())";
    }

    boolean usesConsole = false;

    @Override
    public Object visitConsoleExpr(ConsoleExpr consoleExpr, Object arg) throws Exception {
        //return ConsoleIO.readValueFromConsole("INT", "HUH");
        usesConsole = true;
        //if(consoleExpr.getCoerceTo()!=null)
        return "ConsoleIO.readValueFromConsole(\""+consoleExpr.getCoerceTo().toString()+"\", \"Please enter a value\")";
       // return "ConsoleIO.readValueFromConsole(\""+consoleExpr.getCoerceTo().toString()+"\", \"Please enter a value\")";

    }

    private String ColorTupFixer(String s){
        usesColor = true;
        if(s.contains(".getRed()"))
        {
        s = s.replace(".getRed()", "");
        s = "ColorTuple.getRed(ColorTuple.unpack("+s+"))";
        }
        if(s.contains(".getBlue()")){
        s = s.replace(".getBlue()", "");
        s = "ColorTuple.getBlue(ColorTuple.unpack("+s+"))";
        }
        if(s.contains(".getGreen()")){
        s = s.replace(".getGreen()", "");
        s = "ColorTuple.getGreen(ColorTuple.unpack("+s+"))";
        }
        return s;
    }

    @Override
    public Object visitColorExpr(ColorExpr colorExpr, Object arg) throws Exception {
        if(arg instanceof String){
            arg = true;
        }
        return "new ColorTuple("+ColorTupFixer(""+colorExpr.getRed().visit(this, arg))+","+ColorTupFixer(""+colorExpr.getGreen().visit(this, arg))+","+ColorTupFixer(""+colorExpr.getBlue().visit(this, arg))+")";
    }

    @Override
    public Object visitUnaryExpr(UnaryExpr unaryExpression, Object arg) throws Exception {
        if(unaryExpression.getOp().getText().contains("getWidth")
        ||unaryExpression.getOp().getText().contains("getHeight"))
        {
            return ""+unaryExpression.getExpr().visit(this, arg)+"."+unaryExpression.getOp().getText()+"()";
        }
        if(unaryExpression.getOp().getText().contains("getRed")
        ||unaryExpression.getOp().getText().contains("getBlue")
        ||unaryExpression.getOp().getText().contains("getGreen")){
            usesColor = true;
            return ""+unaryExpression.getExpr().visit(this, arg)+"."+unaryExpression.getOp().getText()+"()";

        }
        return ""+unaryExpression.getFirstToken().getText()+unaryExpression.getExpr().visit(this, arg);
    }

    @Override
    public Object visitBinaryExpr(BinaryExpr binaryExpr, Object arg) throws Exception {
        
        if(binaryExpr.getLeft().getType()==Type.COLOR&&binaryExpr.getRight().getType()==Type.COLOR){
            switch(binaryExpr.getOp().getText()){
                case "==","!=",">",">=","<=","<":
                usesColor = true;
                return "(("+binaryExpr.getLeft().visit(this, arg)+").red"+binaryExpr.getOp().getText()+"("+binaryExpr.getRight().visit(this, arg)+").red && ("+binaryExpr.getLeft().visit(this, arg)+").green"+binaryExpr.getOp().getText()+"("+binaryExpr.getRight().visit(this, arg)+").green && ("+binaryExpr.getLeft().visit(this, arg)+").blue"+binaryExpr.getOp().getText()+"("+binaryExpr.getRight().visit(this, arg)+").blue)";
                default:
                usesColor = true;
                return "new ColorTuple("+binaryExpr.getLeft().visit(this, arg)+".red"+binaryExpr.getOp().getText()+binaryExpr.getRight().visit(this, arg)+".red,"+binaryExpr.getLeft().visit(this, arg)+".green"+binaryExpr.getOp().getText()+binaryExpr.getRight().visit(this, arg)+".green,"+binaryExpr.getLeft().visit(this, arg)+".blue"+binaryExpr.getOp().getText()+binaryExpr.getRight().visit(this, arg)+".blue)";
            }
        }
        if(arg instanceof String&&((String)arg).equals("image")){
            usesColor = true;
            return "new ColorTuple(ColorTuple.getRed("+binaryExpr.getLeft().visit(this, arg)+")"+binaryExpr.getOp().getText()+"ColorTuple.getRed("+binaryExpr.getRight().visit(this, arg)+"), ColorTuple.getGreen("+binaryExpr.getLeft().visit(this, arg)+")"+binaryExpr.getOp().getText()+"ColorTuple.getGreen("+binaryExpr.getRight().visit(this, arg)+"), ColorTuple.getBlue("+binaryExpr.getLeft().visit(this, arg)+")"+binaryExpr.getOp().getText()+"ColorTuple.getBlue("+binaryExpr.getRight().visit(this, arg)+"))";

        }
        
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
            if(binaryExpr.getRight().getType()==Type.STRING&&binaryExpr.getLeft().getType()==Type.STRING){
                if(binaryExpr.getOp().getKind()==Kind.EQUALS){
                    return "(" + binaryExpr.getLeft().visit(this,arg) + ".equals(" + binaryExpr.getRight().visit(this,arg)+"))";
                } else if(binaryExpr.getOp().getKind()==Kind.NOT_EQUALS){
                    return "!(" + binaryExpr.getLeft().visit(this,arg) + ".equals(" + binaryExpr.getRight().visit(this,arg)+"))";
                } else{
                    throw new UnsupportedOperationException("what the fuck");
                }
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
        
        return dimension.getWidth().visit(this, arg)+","+dimension.getHeight().visit(this, arg);
    }

    @Override
    public Object visitPixelSelector(PixelSelector pixelSelector, Object arg) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }
    boolean usesImages = false;;
    @Override
    public Object visitAssignmentStatement(AssignmentStatement assignmentStatement, Object arg) throws Exception {
        String con = "";
        if(assignmentStatement.getTargetDec()!=null&&assignmentStatement.getTargetDec().getType()!=assignmentStatement.getExpr().getType()){
            if(assignmentStatement.getTargetDec().getType().toString().toLowerCase().equals("image")){
                usesImages=true;
                if(assignmentStatement.getSelector()!=null){
                    assignmentStatement.getSelector().visit(this, arg);
                }
                if(assignmentStatement.getExpr().visit(this,null).equals("CONDITION")){
                    usesBools = true;
                    if(((String)conditionalStatements[1]).contains(".getRGB(")&&!((String)conditionalStatements[1]).contains("ColorTuple.unpack(")){
                        conditionalStatements[1] = "(ColorTuple.unpack("+conditionalStatements[1]+"))";
                    }
                    if(((String)conditionalStatements[2]).contains(".getRGB(")&&!((String)conditionalStatements[1]).contains("ColorTuple.unpack(")){
                        conditionalStatements[2] = "(ColorTuple.unpack("+conditionalStatements[2]+"))";
                    }
                    return "for( int "+ assignmentStatement.getSelector().getX().visit(this,null) + " = 0; " + assignmentStatement.getSelector().getX().visit(this,null)+" < "+assignmentStatement.getName()+".getWidth(); "+assignmentStatement.getSelector().getX().visit(this,null)+"++) "+ "\n\tfor( int "+ assignmentStatement.getSelector().getY().visit(this,null) + " = 0; " + assignmentStatement.getSelector().getY().visit(this,null)+" < "+assignmentStatement.getName()+".getHeight(); "+assignmentStatement.getSelector().getY().visit(this,null)+"++) \n\t\tImageOps.setColor("+assignmentStatement.getName()+","+assignmentStatement.getSelector().getX().visit(this,null)+","+assignmentStatement.getSelector().getY().visit(this,null)+","+
                    "\n("+conditionalStatements[0]+")?\n("+conditionalStatements[1]+"):("+conditionalStatements[2]+"));\n";
                }

                if(assignmentStatement.getExpr() instanceof ColorExpr){
                    usesColor = true;
                    usesBools = true;
                    if(assignmentStatement.getExpr() instanceof ColorExpr){
                        return "for( int "+ assignmentStatement.getSelector().getX().visit(this,null) + " = 0; " + assignmentStatement.getSelector().getX().visit(this,null)+" < "+assignmentStatement.getName()+".getWidth(); "+assignmentStatement.getSelector().getX().visit(this,null)+"++) "+ "\n\tfor( int "+ assignmentStatement.getSelector().getY().visit(this,null) + " = 0; " + assignmentStatement.getSelector().getY().visit(this,null)+" < "+assignmentStatement.getName()+".getHeight(); "+assignmentStatement.getSelector().getY().visit(this,null)+"++) \n\t\tImageOps.setColor("+assignmentStatement.getName()+","+assignmentStatement.getSelector().getX().visit(this,null)+","+assignmentStatement.getSelector().getY().visit(this,null)+
                ","+assignmentStatement.getExpr().visit(this,arg)+");\n";
                    }
                    return "for( int "+ assignmentStatement.getSelector().getX().visit(this,null) + " = 0; " + assignmentStatement.getSelector().getX().visit(this,null)+" < "+assignmentStatement.getName()+".getWidth(); "+assignmentStatement.getSelector().getX().visit(this,null)+"++) "+ "\n\tfor( int "+ assignmentStatement.getSelector().getY().visit(this,null) + " = 0; " + assignmentStatement.getSelector().getY().visit(this,null)+" < "+assignmentStatement.getName()+".getHeight(); "+assignmentStatement.getSelector().getY().visit(this,null)+"++) \n\t\tImageOps.setColor("+assignmentStatement.getName()+","+assignmentStatement.getSelector().getX().visit(this,null)+","+assignmentStatement.getSelector().getY().visit(this,null)+
                ",new ColorTuple("+assignmentStatement.getExpr().visit(this,arg)+"));\n";
                } if(assignmentStatement.getExpr() instanceof ColorConstExpr){
                    //String p = ""+assignmentStatement.getExpr().visit(this,arg);
                   // p.replace(".getRGB()", "");
                   usesImages = true;
                   return "for( int x = 0; x < "+assignmentStatement.getName()+".getWidth(); x++)\n\t"+
                   "for( int y = 0; y < "+ assignmentStatement.getName()+".getHeight(); y++) \n\t\t"+
                   "ImageOps.setColor("+assignmentStatement.getName()+
                   ",x,y,"+assignmentStatement.getExpr().visit(this,arg)+");\n"+assignmentStatement.getExpr().visit(this,arg)+";\n";   
                }
                if(assignmentStatement.getExpr().getCoerceTo()!=null&&assignmentStatement.getExpr().getCoerceTo().name()=="COLOR"){
                    usesColor = true;
                    usesImages = true;
            return "for( int x = 0; x < "+assignmentStatement.getName()+".getWidth(); x++)\n\t"+
            "for( int y = 0; y < "+ assignmentStatement.getName()+".getHeight(); y++) \n\t\t"+
            "ImageOps.setColor("+assignmentStatement.getName()+
            ",x,y,new ColorTuple("+assignmentStatement.getExpr().visit(this, arg)+","+assignmentStatement.getExpr().visit(this, arg)+","+assignmentStatement.getExpr().visit(this, arg)+"));\n";  
           // return "ImageOps.setColor("+assignmentStatement.getName()+",x,y,new ColorTuple("+assignmentStatement.getExpr().visit(this, arg)+","+assignmentStatement.getExpr().visit(this, arg)+","+assignmentStatement.getExpr().visit(this, arg)+"));";
            
        }
            usesImages = true;
                return "for( int "+ assignmentStatement.getSelector().getX().visit(this,null) + " = 0; " + 
                assignmentStatement.getSelector().getX().visit(this,null)+" < "+assignmentStatement.getName()+".getWidth(); "+
                assignmentStatement.getSelector().getX().visit(this,null)+"++) "+ "\n\tfor( int "+ 
                assignmentStatement.getSelector().getY().visit(this,null) + " = 0; " + 
                assignmentStatement.getSelector().getY().visit(this,null)+" < "+
                assignmentStatement.getName()+".getHeight(); "+
                assignmentStatement.getSelector().getY().visit(this,null)+"++) \n\t\tImageOps.setColor("+
                assignmentStatement.getName()+","+
                assignmentStatement.getSelector().getX().visit(this,null)+","+
                assignmentStatement.getSelector().getY().visit(this,null)+
                ","+assignmentStatement.getExpr().visit(this,"image")+");\n";

            }else{
            con = "("+convertTypeToString(assignmentStatement.getTargetDec().getType().toString().toLowerCase())+")";
    
        }
        }
        if(assignmentStatement.getExpr().getCoerceTo()!=null&&assignmentStatement.getExpr().getCoerceTo().name()=="COLOR"){
                    usesColor = true;
                    usesImages= true;
                    if(assignmentStatement.getTargetDec()instanceof NameDefWithDim){
            return "for( int x = 0; x < "+assignmentStatement.getName()+".getWidth(); x++)\n\t"+
            "for( int y = 0; y < "+ assignmentStatement.getName()+".getHeight(); y++) \n\t\t"+
            "ImageOps.setColor("+assignmentStatement.getName()+
            ",x,y,new ColorTuple("+assignmentStatement.getExpr().visit(this, arg)+","+assignmentStatement.getExpr().visit(this, arg)+","+assignmentStatement.getExpr().visit(this, arg)+"));\n";  
                    } else{ 
                        usesBools = true;
                        return "ImageOps.binaryTupleOp(ImageOps.OP."+(((BinaryExpr)assignmentStatement.getExpr())).getOp().getKind()+","+assignmentStatement.getName()+",new ColorTuple("+
                    ((BinaryExpr)assignmentStatement.getExpr()).getRight().visit(this, arg)+","+
                    ((BinaryExpr)assignmentStatement.getExpr()).getRight().visit(this, arg)+","+
                    ((BinaryExpr)assignmentStatement.getExpr()).getRight().visit(this, arg)+"));"; 

                    }
           // return "ImageOps.setColor("+assignmentStatement.getName()+",x,y,new ColorTuple("+assignmentStatement.getExpr().visit(this, arg)+","+assignmentStatement.getExpr().visit(this, arg)+","+assignmentStatement.getExpr().visit(this, arg)+"));";
            
        }
        if(assignmentStatement.getTargetDec()!=null&&assignmentStatement.getTargetDec().getDim()!=null&&assignmentStatement.getExpr().getType()==Type.IMAGE){
            usesImages = true;
            usesColor = true;
            return "ImageOps.resize("+assignmentStatement.getName()+" , " + assignmentStatement.getExpr().visit(this, arg)+".getWidth(),"+assignmentStatement.getExpr().visit(this, arg)+".getHeight());\n"+
            "for( int x = 0; x < "+assignmentStatement.getName()+".getWidth(); x++)\n\t"+
            "for( int y = 0; y < "+ assignmentStatement.getName()+".getHeight(); y++) \n\t\t"+
            "ImageOps.setColor("+assignmentStatement.getName()+
            ",x,y,(ColorTuple.unpack("+assignmentStatement.getExpr().visit(this, arg)+".getRGB(x,y))));\n";
        
        }
        return assignmentStatement.getName() + " = " +con+ assignmentStatement.getExpr().visit(this, arg) +";\n";

    }

String override[] = new String[2];

    @Override
    public Object visitWriteStatement(WriteStatement writeStatement, Object arg) throws Exception {
        //\"\"\"\n"+writeStatement.getSource().getText()+"\"\"\"
        usesConsole = true;
        if(writeStatement.getSource().getType()==Type.IMAGE){
            return "ConsoleIO.displayImageOnScreen("+writeStatement.getSource().getText()+");\n";
        } else if(writeStatement.getDest().getType()==Type.STRING){
            usesFileIO = true;
            return "FileURLIO.writeValue("+writeStatement.getSource().getText()+", "+writeStatement.getDest().visit(this, arg)+");\n";
        }
        else if(writeStatement.getSource().getType()==Type.COLOR||writeStatement.getSource().getType()==Type.COLORFLOAT){
            //if(writeStatement.getSource() instanceof ColorExpr){
                return "ConsoleIO.console.println("+writeStatement.getSource().visit(this, arg)+");\n";
            //}
        }
        String x = writeStatement.getSource().getText();
        boolean contains = false;
        contains = x.contains("\"");
       x = x.replace("\"", "");
       x = x.replace("\\ADDQUOTE", "\"");
       // x = x.replace("\t", "");
       if(contains){
        return "ConsoleIO.console.println(\"\"\"\n"+x+"\"\"\");\n";
       } else{
        return "ConsoleIO.console.println("+x+");\n";

       }
    }

    @Override
    public Object visitReadStatement(ReadStatement readStatement, Object arg) throws Exception {
        if(readStatement.getSource().getType()==Type.STRING&&readStatement.getSource().getCoerceTo()==Type.IMAGE){
            usesFileIO = true;
            return readStatement.getName()+"= FileURLIO.readImage("+readStatement.getSource().visit(this, arg)+","+readStatement.getTargetDec().getDim().visit(this, arg)+");\n";
        } else if(readStatement.getSource().getType()==Type.STRING&&readStatement.getSource().getCoerceTo()==Type.FLOAT){
           usesFileIO = true;
            return readStatement.getName()+"= (Float) FileURLIO.readValueFromFile(("+readStatement.getSource().visit(this, arg)+"));";
        } else if(readStatement.getSource().getType()==Type.STRING&&readStatement.getTargetDec().getType()==Type.INT){
            usesFileIO = true;
             return readStatement.getName()+"= (Integer) FileURLIO.readValueFromFile(("+readStatement.getSource().visit(this, arg)+"));";
         } else if(readStatement.getSource().getType()==Type.STRING&&readStatement.getTargetDec().getType()==Type.COLOR){
            usesFileIO = true;
             return readStatement.getName()+"= (ColorTuple) FileURLIO.readValueFromFile(("+readStatement.getSource().visit(this, arg)+"));";
         } else if(readStatement.getSource().getType()==Type.STRING&&readStatement.getTargetDec().getType()==Type.BOOLEAN){
            usesFileIO = true;
             return readStatement.getName()+"= (Boolean) FileURLIO.readValueFromFile(("+readStatement.getSource().visit(this, arg)+"));";
         }
        return readStatement.getName() + " = (" + convertTypeToString(readStatement.getTargetDec().getType().toString().toLowerCase()) +")"+ readStatement.getSource().visit(this, arg)+";\n";
    }

public String convertTypeToString(String s){
    
    switch(s){
        case "string":
            s = "String";
            break;
        case "image":
        usesImages = true;
            s = "BufferedImage";
        break;
        case "color":
        usesColor = true;
        s = "ColorTuple";
        break;
            default:
            break;
    }

    return s;
}

    @Override
    public Object visitProgram(Program program, Object arg) throws Exception {
        String s ="";
        String packages;
        //"int y() ^42;"
       // s ="package " + packageName+ ";" + "public static int apply(){return 42;}}";

       packages ="package " + packageName+ ";\n";
        String type = program.getReturnType().toString().toLowerCase();
        
        type = convertTypeToString(type);
 
       s += "public class "+ program.getName() + "{ public static " + type+ " apply(";

       List<ASTNode> decsAndStatements = program.getDecsAndStatements();
      
       boolean multiple = false;
       for (ASTNode node : program.getParams()) {
         if(multiple){
            s+=", ";
         }
         multiple = true;
        s+= node.visit(this, true);
          
       }

       s+= "){\n";
       for (ASTNode node : decsAndStatements) {
          s+= node.visit(this, true);
       }

       s+= "}}";

       if(usesConsole){
        packages += "import edu.ufl.cise.plc.runtime.ConsoleIO;\n";
       }
       if(usesImages){
        packages += "import edu.ufl.cise.plc.runtime.ImageOps;\n import java.awt.image.BufferedImage;\n";
       }
       if(usesColor){
           packages +="import java.awt.Color;\nimport edu.ufl.cise.plc.runtime.ColorTuple;\n";
       }
       if(usesFileIO){
           packages += "import edu.ufl.cise.plc.runtime.FileURLIO;\n";
       }
       if(usesBools){
           packages += "import static edu.ufl.cise.plc.runtime.ImageOps.BoolOP.*;\n";
       }

       s = packages+"\n" + s;
       
        return s;
    }


    @Override
    public Object visitNameDef(NameDef nameDef, Object arg) throws Exception {
        return convertTypeToString(nameDef.getType().toString().toLowerCase())+" "+nameDef.getName();
    }

    @Override
    public Object visitNameDefWithDim(NameDefWithDim nameDefWithDim, Object arg) throws Exception {
        if(arg instanceof Boolean && (Boolean)arg == true){
            usesImages=true;
            return convertTypeToString(nameDefWithDim.getType().toString().toLowerCase())+" "+nameDefWithDim.getName()+"= new BufferedImage("+nameDefWithDim.getDim().getWidth().visit(this, null)+","+nameDefWithDim.getDim().getHeight().visit(this, null)+",BufferedImage.TYPE_INT_RGB);\n";
        }
        return convertTypeToString(nameDefWithDim.getType().toString().toLowerCase())+" "+nameDefWithDim.getName();

        //BufferedImage a= new BufferedImage(widthAndHeight,widthAndHeight,BufferedImage.TYPE_INT_RGB);
        //return null;
    }

    @Override
    public Object visitReturnStatement(ReturnStatement returnStatement, Object arg) throws Exception {
        Object expr;


        expr = returnStatement.getExpr().visit(this, arg);
        if(expr == "CONDITION"){
            return "if (" + conditionalStatements[0] +") { return " + conditionalStatements[1] +"; } else { return " + conditionalStatements[2]+"; }";
        } else{
        return "return " + expr +";\n";
        }



    }

    boolean usesFileIO = false;
    boolean usesBools = false;
    @Override
    public Object visitVarDeclaration(VarDeclaration declaration, Object arg) throws Exception {
        //String s =  ""+convertTypeToString(declaration.getType().toString().toLowerCase()) +" " +declaration.getName()+"; ";
        String typeCon ="";
       
        if(declaration.isInitialized()&&declaration.getExpr()!=null){
            if(declaration.getType()!=declaration.getExpr().getType()){
                typeCon = "("+convertTypeToString(declaration.getType().toString().toLowerCase())+")";
            }
            if(declaration.getExpr().visit(this, arg)=="CONDITION"){
                return ""+convertTypeToString(declaration.getType().toString().toLowerCase()) +" " +declaration.getName()+";\n  if (" + conditionalStatements[0] +") { " +declaration.getName() + " = "+ typeCon+conditionalStatements[1] + ";\n } else { " +declaration.getName() + " = "+ typeCon+ conditionalStatements[2]+";\n }";
            } else if(declaration.getNameDef().getType()==Type.IMAGE){
                if(declaration.getExpr() instanceof BinaryExpr){
                    usesImages = true;
                    if(((BinaryExpr)declaration.getExpr()).getRight().getType()==Type.IMAGE){
                        return convertTypeToString(declaration.getType().toString().toLowerCase())+" " +declaration.getNameDef().getName()+"=ImageOps.binaryImageImageOp( ImageOps.OP."+((BinaryExpr)declaration.getExpr()).getOp().getKind().toString()+","+((BinaryExpr)declaration.getExpr()).getLeft().visit(this, arg)+","+((BinaryExpr)declaration.getExpr()).getRight().visit(this, arg)+");\n"; 
                    }
                    else {
                    return convertTypeToString(declaration.getType().toString().toLowerCase())+" " +declaration.getNameDef().getName()+"=ImageOps.binaryImageScalarOp( ImageOps.OP."+((BinaryExpr)declaration.getExpr()).getOp().getKind().toString()+","+((BinaryExpr)declaration.getExpr()).getLeft().visit(this, arg)+","+((BinaryExpr)declaration.getExpr()).getRight().visit(this, arg)+");\n"; 
                    }
                } else {
                usesImages=true; usesFileIO = true;
                if(declaration.getExpr()instanceof ColorConstExpr||declaration.getExpr()instanceof ColorConstExpr){
                    usesImages= true;
                    return "BufferedImage "+declaration.getName()+" = new BufferedImage("+declaration.getDim().visit(this, arg)+",BufferedImage.TYPE_INT_RGB);\n"+
                    "for( int x = 0; x < "+declaration.getName()+".getWidth(); x++)\n\t"+
                    "for( int y = 0; y < "+ declaration.getName()+".getHeight(); y++) \n\t\t"+
                    "ImageOps.setColor("+declaration.getName()+
                    ",x,y,"+declaration.getExpr().visit(this,arg)+");\n"+declaration.getExpr().visit(this,arg)+";\n";  
                }
                if(declaration.getDim()!=null&&declaration.getExpr()instanceof IntLitExpr){
                   usesColor = true;
                   usesImages = true;
                    return "BufferedImage "+declaration.getName()+" = new BufferedImage("+declaration.getDim().visit(this, arg)+",BufferedImage.TYPE_INT_RGB);\n"+
                    "for( int x = 0; x < "+declaration.getName()+".getWidth(); x++)\n\t"+
                    "for( int y = 0; y < "+ declaration.getName()+".getHeight(); y++) \n\t\t"+
                    "ImageOps.setColor("+declaration.getName()+
                    ",x,y,new ColorTuple("+declaration.getExpr().visit(this,arg)+","+declaration.getExpr().visit(this,arg)+","+declaration.getExpr().visit(this,arg)+"));\n"; 
                }
                if(declaration.getDim()!=null&&declaration.getExpr().getType()==Type.IMAGE){
                    usesImages = true;
                    usesColor = true;
                    return "BufferedImage "+declaration.getName()+" = new BufferedImage("+declaration.getDim().visit(this, arg)+",BufferedImage.TYPE_INT_RGB);\n"+
                    "for( int x = 0; x < "+declaration.getName()+".getWidth(); x++)\n\t"+
                    "for( int y = 0; y < "+ declaration.getName()+".getHeight(); y++) \n\t\t"+
                    "ImageOps.setColor("+declaration.getName()+
                    ",x,y,(ColorTuple.unpack("+declaration.getExpr().visit(this, arg)+".getRGB(x,y))));\n";
                
                }
                if(declaration.getExpr() instanceof StringLitExpr||(declaration.getExpr() instanceof IdentExpr)){
                    if(declaration.getExpr().getType() == Type.IMAGE){
                        return convertTypeToString(declaration.getType().toString().toLowerCase())+" " +declaration.getNameDef().getName()+"="+declaration.getExpr().visit(this, arg)+";\n";
                    }
                    if(declaration.getDim()!=null){
                return convertTypeToString(declaration.getType().toString().toLowerCase())+" " +declaration.getNameDef().getName()+"=FileURLIO.readImage("+declaration.getExpr().visit(this, arg)+","+declaration.getDim().visit(this, arg)+");\n";
                } else {
                    return convertTypeToString(declaration.getType().toString().toLowerCase())+" " +declaration.getNameDef().getName()+"=FileURLIO.readImage("+declaration.getExpr().visit(this, arg)+",null,null);\n";
                }   
            }

                usesImages = true;
                usesColor = true;
                
                String v = (String)declaration.getExpr().visit(this, arg);
                if(((String)declaration.getExpr().visit(this, arg)).contains(".getRed()")){
                   v = v.replace(".getRed()", "");
                   return "BufferedImage "+declaration.getName()+" = ImageOps.extractRed("+v+");\n";

                }
                if(((String)declaration.getExpr().visit(this, arg)).contains(".getBlue()")){
                    v = v.replace(".getBlue()", "");
                    return "BufferedImage "+declaration.getName()+" = ImageOps.extractBlue("+v+");\n";

                 }
                 if(((String)declaration.getExpr().visit(this, arg)).contains(".getGreen()")){
                    v = v.replace(".getGreen()", "");
                    return "BufferedImage "+declaration.getName()+" = ImageOps.extractGreen("+v+");\n";

                 }

                 return "BufferedImage "+declaration.getName()+" = "+v+";\n";


               // return "BufferedImage "+declaration.getName()+" = new BufferedImage("+v+".getWidth(),"+v+".getHeight(),BufferedImage.TYPE_INT_RGB);\n"+
               // "for( int x = 0; x < "+declaration.getName()+".getWidth(); x++)\n\t"+
                //"for( int y = 0; y < "+ declaration.getName()+".getHeight(); y++) \n\t\t"+
                //"ImageOps.setColor("+declaration.getName()+
                //",x,y,(ColorTuple.unpack("+declaration.getExpr().visit(this, arg)+".getRGB(x,y))));\n";
            }
            }else if(declaration.getNameDef().getType()==Type.COLOR){
                usesColor = true;
                if(declaration.getExpr().getType() == Type.INT){
                    return "ColorTuple "+declaration.getName()+" = ColorTuple.unpack("+declaration.getExpr().visit(this, arg)+");\n";
                } else if( declaration.getExpr().getType() == Type.STRING){
                    usesFileIO = true;
                return "ColorTuple "+declaration.getName()+"= (ColorTuple) FileURLIO.readValueFromFile(("+declaration.getExpr().visit(this, arg)+"));";
                }
                return "ColorTuple "+declaration.getName()+" = "+declaration.getExpr().visit(this, arg)+";\n";
            }else{
                if(declaration.getOp().getKind()==Kind.LARROW&&(declaration.getExpr() instanceof StringLitExpr || declaration.getExpr() instanceof IdentExpr)){
                    return ""+convertTypeToString(declaration.getType().toString().toLowerCase()) +" " +declaration.getName() + " = (("+convertTypeToString(declaration.getType().toString().toLowerCase())+") FileURLIO.readValueFromFile("+declaration.getExpr().visit(this, arg)+"));";
                }
                if(declaration.getExpr().getType()==Type.COLOR&&(declaration.getType()==Type.INT||declaration.getType()==Type.FLOAT)){
                    usesColor = true;
                    return ""+convertTypeToString(declaration.getType().toString().toLowerCase()) +" " +declaration.getName() +" = ColorTuple.makePackedColor("+declaration.getExpr().visit(this, arg)+".red,"+declaration.getExpr().visit(this, arg)+".green,"+declaration.getExpr().visit(this, arg)+".blue"+");\n ";

                }
            return ""+convertTypeToString(declaration.getType().toString().toLowerCase()) +" " +declaration.getName() +" = "+typeCon+declaration.getExpr().visit(this, arg)+";\n ";
            }
        } else {
            return ""+convertTypeToString(declaration.getType().toString().toLowerCase()) +" " +declaration.getName()+";\n ";
        }

       // s +="; ";
       // return s;
    }

    @Override
    public Object visitUnaryExprPostfix(UnaryExprPostfix unaryExprPostfix, Object arg) throws Exception {
       if(unaryExprPostfix.getSelector()!=null){
           return ""+unaryExprPostfix.getFirstToken().getText()+".getRGB("+unaryExprPostfix.getSelector().getX().visit(this, arg)+","+unaryExprPostfix.getSelector().getY().visit(this, arg)+")";

       }
        return ""+unaryExprPostfix.getFirstToken().getText()+".getRGB(x, y)";
        //return null;
    }


}
