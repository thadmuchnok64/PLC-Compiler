import java.util.ArrayList;

import edu.ufl.cise.plc.IToken.Kind;

public class Lexer implements ILexer {
    
    ArrayList<Token> tokens;
    ArrayList<ArrayList<Character>> chars;
    int pos = 0;
    int line = 0;

    private enum State {START, IN_IDENT, HAVE_ZERO, HAVE_DOT, IN_FLOAT, IN_NUM, HAVE_EQ, HAVE_MINUS};
    private State state;
    public Lexer Lexer(String input)
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


        for(char c : charArray){

            char ch = charArray[pos];
            int startPos;
            switch(state)
            {
                case START:
                    startPos = pos;
                    switch(ch)
                    {
                        case ' ', '\t', '\n', '\r' :
                            {pos++;}
                        break;
                        
                        case '+':
                            Token newToken = new Token(Kind.PLUS, ch, pos, 1, _sourceLocation)
                    }
                break;
                case IN_IDENT:
                break;
                case HAVE_ZERO:
                break;
                case HAVE_DOT:
                break;
                case IN_FLOAT:
                break;
                case IN_NUM:
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
