import java.util.ArrayList;

public class Lexer implements ILexer {
    
    ArrayList<ArrayList<Character>> chars;
    int pos = 0;

    private enum State {START, IN_IDENT, HAVE_ZERO, HAVE_DOT, IN_FLOAT, IN_NUM, HAVE_EQ, HAVE_MINUS};
    private State state;
    public Lexer Lexer(String input)
    {
        state = State.START;
        LexerTime(input);
    }

    public LexerTime(String input)
    {
        while(true)
        {
            char ch = chars.get(pos);
            switch(state)
            {
                case START:
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
    }

}