package edu.ufl.cise.plc;
import java.util.ArrayList;

import edu.ufl.cise.plc.IToken.Kind;

public class Lexer implements ILexer {
    
    ArrayList<Token> tokens;
    ArrayList<ArrayList<Character>> chars;
    int pos = 0;
    int line = 0;
    int currentIndex = 0;

    //Returns next object in array, and iterates the current index
    @Override
    public IToken next() throws LexicalException {
        currentIndex++;
        if(currentIndex>=tokens.size()){
            currentIndex = 0;
        }
        return tokens.get(currentIndex);
    }

    //Returns the next token in the array.
    @Override
    public IToken peek() throws LexicalException {
        if(currentIndex>=tokens.size()){
            return tokens.get(0);
        }
        return tokens.get(currentIndex);
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

        String str = "";
        State currentState = State.START;

        for(int i = 0; i < chars.size(); i++)
            for(int j = 0; j < chars.get(i).size(); j++){

            char ch = chars.get(i).get(j);
            int startPos;
            Token newToken;
            boolean endScan = true;

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
                            case IN_IDENT,HAVE_DOT,IN_FLOAT:
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
                    break;
                    default:
                        throw new UnsupportedOperationException("oopsie poopsie, looks like you made an invalid term.");
                    
                }

            // end of test



            switch(state)
            {
                case START:
                    startPos = pos;
                    switch(ch)
                    {
                        case ' ', '\t', '\n', '\r' :
                            {
                                pos++;
                                str = "";
                            }
                            break;
                        
                        case '+':
                        {
                            newToken = new Token(Kind.PLUS, ch+"", pos, 1, i, j);
                            break;
                        }
                        
                        

                        case '*':
                        {
                            newToken = new Token(Kind.TIMES, ch+"", pos, 1, i, j);
                        }
                            
                        case '=':
                        {
                            newToken = new Token(Kind.MINUS, ch+"", pos, 1, i, j);
                            break;
                        }
                            
                    }
                break;
                case IN_IDENT:
                {
                    newToken = new Token(Kind.IDENT,ch+"",pos,1,i,j);
                    break;
                }
                case HAVE_ZERO:
                break;
                case HAVE_DOT:
                break;
                case IN_FLOAT:
                break;
                case IN_NUM:
                {
                    newToken = new Token(Kind.INT_LIT,ch+"",pos,1,i,j);
                }
                break;
                case HAVE_EQ:
                break;
                case HAVE_MINUS:
                break;
            }
        }
        state = State.START;
        
    }
    
}
