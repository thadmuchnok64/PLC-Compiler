package edu.ufl.cise.plc;

import java.util.ArrayList;

import edu.ufl.cise.plc.IToken.Kind;
import edu.ufl.cise.plc.ast.ASTNode;
import edu.ufl.cise.plc.ast.BooleanLitExpr;
import edu.ufl.cise.plc.ast.Expr;
import edu.ufl.cise.plc.ast.FloatLitExpr;
import edu.ufl.cise.plc.ast.IdentExpr;
import edu.ufl.cise.plc.ast.IntLitExpr;
import edu.ufl.cise.plc.ast.StringLitExpr;
import edu.ufl.cise.plc.ast.UnaryExpr;
import edu.ufl.cise.plc.CompilerComponentFactory;

public class Parser implements IParser {

    String input;
    ArrayList<IToken> listOfTokens;

    public Parser(String _input){

        input = _input;
        ILexer lexer = CompilerComponentFactory.getLexer(input);
        listOfTokens = new ArrayList<>();
            try {
                while(lexer.peek().getKind()!=Kind.EOF){
                    listOfTokens.add(lexer.next());
                }
            } catch (LexicalException e) {
                // idk why this needed a try and catch
                e.printStackTrace();
            }



    }

    public ASTNode recursionParse(ArrayList<IToken> list) throws LexicalException{
        ASTNode a = null;

        //for(int i = 0; i < listOfTokens.size(); i++){
            IToken t = listOfTokens.get(0);
            switch(t.getKind())
            {
                case BOOLEAN_LIT:
                    return new BooleanLitExpr(t);
                case STRING_LIT:
                    return new StringLitExpr(t);
                case FLOAT_LIT:
                    return new FloatLitExpr(t);
                case IDENT:
                    return new IdentExpr(t);
                case INT_LIT:
                    return new IntLitExpr(t);
                case BANG,MINUS,COLOR_OP, IMAGE_OP:
                    list.remove(0);
                    ASTNode newNode = (Expr)recursionParse(list);
                    if(newNode instanceof Expr)
                    return new UnaryExpr(t, t, (Expr)recursionParse(list));
                    else 
                    throw new LexicalException("penisbutt");
                default:
                //ligma
            }
        //}

        return a;
    }
    

    @Override
    public ASTNode parse() throws PLCException {
       return recursionParse(listOfTokens);
    }


    
}