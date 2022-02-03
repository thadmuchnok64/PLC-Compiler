package edu.ufl.cise.plc;
import java.lang.Thread.State;
import java.util.ArrayList;

import edu.ufl.cise.plc.IToken.Kind;

public class Lexer implements ILexer {
    
    ArrayList<Token> tokens;
    ArrayList<ArrayList<Character>> chars;
    int row = 0;
    int column = 0;
    int currentIndex = 0;

    String str = "";






    public IToken MakeToken(boolean increments){
        
        State currentState = State.START;
        Token newToken;
        int posX = column;
        int startPos = posX;
        int posY = row;
        boolean endScan = false;

        while (!endScan){

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

                   if(!endScan){
                    posX++;
                   }
                }
        
    }
        
        switch(state)
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
                    newToken = new Token(Kind.IDENT,str,pos,1,i,j);
                    break;
                }
                case HAVE_ZERO:
                break;
                case HAVE_DOT:
                break;
                case IN_FLOAT:
                    newToken = new Token(Kind.FLOAT_LIT,str,pos,1,i,j);
                break;
                case IN_NUM:
                {
                    newToken = new Token(Kind.INT_LIT,str,pos,1,i,j);
                }
                break;
                case HAVE_EQ:
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
    
    private enum State {START, IN_IDENT, HAVE_ZERO, HAVE_DOT, IN_FLOAT, IN_NUM, HAVE_EQ, HAVE_MINUS};
    private State state;
    public Lexer(String input)
    {
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

    }
}
