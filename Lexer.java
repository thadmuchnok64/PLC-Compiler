import java.util.ArrayList;

public class Lexer implements ILexer {
    
    ArrayList<ArrayList<Character>> chars;
    
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
            
        }
    }

}