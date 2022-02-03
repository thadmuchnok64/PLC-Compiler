package edu.ufl.cise.plc;
import java.lang.Thread.State;
import java.util.ArrayList;
import java.util.HashMap;

import edu.ufl.cise.plc.IToken.Kind;

public class Lexer implements ILexer {
    

    private enum State {START, IN_IDENT, HAVE_ZERO, HAVE_DOT, IN_FLOAT, IN_NUM, HAVE_EQ, HAVE_MINUS,HAVE_PLUS,HAVE_MULTIPLY,HAVE_DIVISION};
    ArrayList<Token> tokens;
    ArrayList<ArrayList<Character>> chars;
    int row = 0;
    int column = 0;
    int currentIndex = 0;

    String str;

    HashMap<String, Kind> theKindMap = new HashMap<String, Kind>();
    
    public void generateReservedMap()
    {
        //<type>
        theKindMap.put("string", Kind.TYPE);
        theKindMap.put("int", Kind.TYPE);
        theKindMap.put("float", Kind.TYPE);
        theKindMap.put("boolean", Kind.TYPE);
        theKindMap.put("color", Kind.TYPE);
        theKindMap.put("image", Kind.TYPE);
        theKindMap.put("void", Kind.TYPE);

        //<image_op>
        theKindMap.put("getWidth", Kind.IMAGE_OP);
        theKindMap.put("getHeight", Kind.IMAGE_OP);

        //<color_op>
        theKindMap.put("getRed", Kind.COLOR_OP);
        theKindMap.put("getGreen", Kind.COLOR_OP);
        theKindMap.put("getBlue", Kind.COLOR_OP);

        //<color_const>
        theKindMap.put("BLACK", Kind.COLOR_CONST);
        theKindMap.put("BLUE", Kind.COLOR_CONST);
        theKindMap.put("CYAN", Kind.COLOR_CONST);
        theKindMap.put("DARK_GRAY", Kind.COLOR_CONST);
        theKindMap.put("GRAY", Kind.COLOR_CONST);
        theKindMap.put("GREEN", Kind.COLOR_CONST);
        theKindMap.put("LIGHT_GRAY", Kind.COLOR_CONST);
        theKindMap.put("MAGENTA", Kind.COLOR_CONST);
        theKindMap.put("ORANGE", Kind.COLOR_CONST);
        theKindMap.put("PINK", Kind.COLOR_CONST);
        theKindMap.put("RED", Kind.COLOR_CONST);
        theKindMap.put("WHITE", Kind.COLOR_CONST);
        theKindMap.put("YELLOW", Kind.COLOR_CONST);

        //<boolean_lit>
        theKindMap.put("true", Kind.BOOLEAN_LIT);
        theKindMap.put("false", Kind.BOOLEAN_LIT);

        //<other_keywords>
        theKindMap.put("if", Kind.KW_IF);
        theKindMap.put("else", Kind.KW_ELSE);
        theKindMap.put("fi", Kind.KW_IF);
        theKindMap.put("write", Kind.KW_WRITE);
        theKindMap.put("console", Kind.KW_CONSOLE);   
    }
    


    public IToken MakeToken(boolean increments){
        
        str = "";
        State currentState = State.START;
        Token newToken;
        int posX = column;
        int startPos = posX;
        int posY = row;
        boolean endScan = false;

        posX = posX -1;

        while (!endScan){
            posX++;
            char ch = chars.get(posX).get(posY);
            //int startPos;
             endScan = true;
            //Kind prevState;

            //test switch - TM
                switch(ch){
                    //Check for ident starter
                    case 'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z','A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z','$','_':
                        if(currentState!=State.IN_IDENT&&currentState!=State.START){
                            break;
                        } else {
                        currentState = State.IN_IDENT;
                        str = str + ch;
                        endScan = false;
                        }
                    break;
                    // Check for Num
                    case '0','1','2','3','4','5','6','7','8','9':
                        switch(currentState)
                        {
                            case IN_IDENT,IN_FLOAT:
                            str = str + ch;
                            endScan = false;
                            break;
                            case HAVE_DOT:
                            currentState = State.IN_FLOAT;
                            str = str + ch;
                            endScan = false;
                            break;
                            default:
                            currentState=State.IN_NUM;
                            str = str + ch;
                            endScan = false;
                            break;
                        }
                    break;
                    case '.':
                    //check for Float
                    if(currentState == State.HAVE_ZERO||currentState==State.IN_NUM){
                        currentState = State.IN_FLOAT;
                        str = str + ch;
                        endScan = false;
                    } else if(currentState==State.START) {
                        currentState = State.HAVE_ZERO;
                        str = str + ch;
                        endScan = false;
                    }
                    break;
                    case '=':
                        if(currentState==State.START){
                            currentState= State.HAVE_EQ;
                        } else if(currentState==State.HAVE_EQ){
                            str = str + ch;
                            endScan = false;
                        }
                    break;
                    case '-':
                    if(currentState==State.START){
                        currentState= State.HAVE_MINUS;
                    } else if(currentState==State.HAVE_MINUS){
                        str = str + ch;
                        endScan = false;
                    }
                    case '+':
                    if(currentState==State.START){
                        currentState= State.HAVE_PLUS;
                    } else if(currentState==State.HAVE_PLUS){
                        str = str + ch;
                        endScan = false;
                    }
                    break;
                    case '*':
                    if(currentState==State.START){
                        currentState= State.HAVE_MULTIPLY;
                    } else if(currentState==State.HAVE_MULTIPLY){
                        str = str + ch;
                        endScan = false;
                    }
                    case '/':
                    if(currentState==State.START){
                        currentState= State.HAVE_DIVISION;
                    } else if(currentState==State.HAVE_DIVISION){
                        str = str + ch;
                        endScan = false;
                    }
                    break;
                    case '\b','\t','\n','\f','\r','"','\'','\\',' ':
                        if(currentState==State.START){
                            //Skip white space if nothing is scanned yet.
                            endScan = false;
                        }
                        if(ch=='\n'){
                            posY++;
                            posX = 0;
                        }
                        if(ch==' '){
                            if(!endScan){
                                startPos++;
                            }
                        }
                    break;
                    default:
                        throw new UnsupportedOperationException("oopsie poopsie, looks like you made an invalid term.");
                        //break;

                   
                    
                   
                }
        
    }
        
        switch(currentState)
            {
                case START:
                /*
                    switch(str)
                    {
                        case ' ', '\t', '\n', '\r' :
                            {
                                pos++;
                                str = "";
                            }
                            break;
                        
                        case '+':
                        {
                            newToken = new Token(Kind.PLUS, str, pos, 1, i, j);
                            break;
                        }
                        
                        

                        case '*':
                        {
                            newToken = new Token(Kind.TIMES, str, pos, 1, i, j);
                        }
                            
                        case '=':
                        {
                            newToken = new Token(Kind.MINUS, str, pos, 1, i, j);
                            break;
                        }
                            
                    }
                break;*/
                case IN_IDENT:
                {
                    newToken = new Token(Kind.IDENT,str,str.length(),posY,startPos);
                    break;
                }
                case HAVE_ZERO:
                break;
                case HAVE_DOT:
                break;
                case IN_FLOAT:
                    newToken = new Token(Kind.FLOAT_LIT,str,str.length(),posY,startPos);
                break;
                case IN_NUM:
                {
                    newToken = new Token(Kind.INT_LIT,str,str.length(),posY,startPos);
                }
                break;
                case HAVE_EQ:
                break;
                case HAVE_PLUS:
                break;
                case HAVE_MINUS:
                break;
            }

            if(increments){
                column = posX;
                row = posY;
            }

            return newToken;
    }


    //Returns next object in array, and iterates the current index
    @Override
    public IToken next() throws LexicalException {
        

        
        IToken token = MakeToken(true);
        
        return token;
        /*
        currentIndex++;
        if(currentIndex>=tokens.size()){
            currentIndex = 0;
        }
        return tokens.get(currentIndex);
        */
    }

    //Returns the next token in the array.
    @Override
    public IToken peek() throws LexicalException {
        IToken token = MakeToken(true);
        
        return token;
        /*
        if(currentIndex>=tokens.size()){
            return tokens.get(0);
        }
        return tokens.get(currentIndex);
        */
    }
    

    private State state;
    public Lexer(String input)
    {
        /*
        char[] charArray = input.toCharArray();
        for(char c: charArray)
        {
            switch(c)
            {
                case ' ', '\t', '\n', '\r':
                    line++;
                    chars.get(line).add(c);
                    break;
                default:
                    chars.get(line).add(c);
                    break;
            }
        }
        */

    }
}
