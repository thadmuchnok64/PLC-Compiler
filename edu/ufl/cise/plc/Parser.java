package edu.ufl.cise.plc;

import java.util.ArrayList;

import edu.ufl.cise.plc.IToken.Kind;
import edu.ufl.cise.plc.ast.ASTNode;
import edu.ufl.cise.plc.ast.BooleanLitExpr;
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

    @Override
    public ASTNode parse() throws PLCException {

        ASTNode a = null;

        for(IToken t : listOfTokens){
            if(t.getKind()==Kind.BOOLEAN_LIT){
                a = new BooleanLitExpr(t);
            }
        }

        return a;
    }


    
}