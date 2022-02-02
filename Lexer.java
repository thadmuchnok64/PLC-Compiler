import java.util.ArrayList;

import edu.ufl.cise.plc.IToken.Kind;

public class Lexer implements ILexer {
    
    ArrayList<Token> tokens;
    ArrayList<ArrayList<Character>> chars;
    int pos = 0;
    int line = 0;

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


        for(int i = 0; i < chars.length(); i++)
            for(int j = 0; chars.get(i).length; j++){

            char ch = chars.get(i).get(j);
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
                        {
                            Token newToken = new Token(Kind.PLUS, ch, pos, 1, i, j);
                            break;
                        }
                        
                        

                        case '*':
                        {
                            Token newToken = new Token(Kind.TIMES, ch, pos, 1, i, j);
                        }
                            
                        case '=':
                        {
                            Token newToken = new Token(Kind.MINUS, ch, pos, 1, i, j);
                            break;
                        }
                            
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
