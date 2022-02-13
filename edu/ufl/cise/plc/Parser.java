package edu.ufl.cise.plc;

import edu.ufl.cise.plc.ast.ASTNode;

public class Parser implements IParser {

    String input;

    public Parser(String _input){

        input = _input;

    }

    @Override
    public ASTNode parse() throws PLCException {

        Lexer lexer = new Lexer(input);
        //do lexer stuff here
        return null;
    }


    
}