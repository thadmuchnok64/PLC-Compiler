import java.util.ArrayList;

public class Lexer implements ILexer {
    
    ArrayList<ArrayList<Token>> tokens;
    ArrayList<ArrayList<Character>> chars;
    int pos = 0;


    private enum State {START, IN_IDENT, HAVE_ZERO, HAVE_DOT, IN_FLOAT, IN_NUM, HAVE_EQ, HAVE_MINUS};
    private State state;
    public Lexer Lexer(String input)
    {
        char[] chars = input.toCharArray();
        for(char c : chars){

            char ch = chars[pos];
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
        state = State.START;
        
    }
    
}
