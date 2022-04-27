package edu.ufl.cise.plc;
import java.lang.Thread.State;
import java.util.ArrayList;
import java.util.HashMap;
import edu.ufl.cise.plc.IToken.Kind;

public class Lexer implements ILexer {
    

    private enum State {

        START, 
        IN_IDENT,
        HAVE_ZERO,
        HAVE_DOT, 
        IN_FLOAT,
        IN_NUM,
        HAVE_EQ,
        //HAVE_MINUS,
        //HAVE_PLUS,
        //HAVE_MULTIPLY,
        //HAVE_DIVISION,
        HAVE_BIZZARE,
        HAVE_STRING,
        HAVE_COMMENT,

    };

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
        theKindMap.put("fi", Kind.KW_FI);
        theKindMap.put("write", Kind.KW_WRITE);
        theKindMap.put("console", Kind.KW_CONSOLE);
        theKindMap.put("void", Kind.KW_VOID);

        //weird characters
        theKindMap.put("&", Kind.AND);
        theKindMap.put("|", Kind.OR);
        theKindMap.put(",", Kind.COMMA);
        theKindMap.put(">=", Kind.GE);
        theKindMap.put("<=", Kind.LE);
        theKindMap.put("=", Kind.ASSIGN);
        theKindMap.put("==", Kind.EQUALS);
        theKindMap.put(">", Kind.GT);
        theKindMap.put("<", Kind.LT);
        theKindMap.put(";", Kind.SEMI);
        theKindMap.put("->", Kind.RARROW);
        theKindMap.put("<-", Kind.LARROW);
        theKindMap.put("^", Kind.RETURN);
        theKindMap.put("[", Kind.LSQUARE);
        theKindMap.put("]", Kind.RSQUARE);
        theKindMap.put("<<", Kind.LANGLE); 
        theKindMap.put(">>", Kind.RANGLE);
        theKindMap.put("+", Kind.PLUS);
        theKindMap.put("-", Kind.MINUS);
        theKindMap.put("*", Kind.TIMES);
        theKindMap.put("%", Kind.MOD);
        theKindMap.put("/", Kind.DIV);
        theKindMap.put("(", Kind.LPAREN);
        theKindMap.put(")", Kind.RPAREN);
        theKindMap.put("!", Kind.BANG);
        theKindMap.put("!=", Kind.NOT_EQUALS);

    }




    public IToken MakeToken(boolean increments) throws LexicalException{
        
        str = "";

        if(column<0){

        column = 0;
        }

        State currentState = State.START;
        Token newToken;
        int posX = column;
        int startPos = posX;
        int posY = row;
        int startLine = posY;
        boolean endScan = false;
        //boolean stringMode = false;
        //boolean commentMode = false;

        posX = posX -1;
        char lastChar = '0';
        while (!endScan&&(posX+1)<chars.get(posY).size()){
            posX++;
            boolean whitespace = false;
            char ch = chars.get(posY).get(posX);
            //int startPos;
             endScan = true;
            //Kind prevState;
            if(currentState!=State.HAVE_STRING){
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
                            case HAVE_BIZZARE:
                            //end scan
                            break;
                            case HAVE_ZERO:
                            //end scan
                            break;
                            default:
                            if(ch=='0'&&currentState!=State.IN_NUM&&currentState!=State.IN_FLOAT&&currentState!=State.HAVE_DOT){
                                currentState = State.HAVE_ZERO;
                            } else{
                            currentState=State.IN_NUM;
                            }
                            str = str + ch;
                            endScan = false;
                            break;
                        }
                    break;
                    case '!', '<', '>', '&','|','(',')','[',']','=','-','/','*','+','%','^',',',';':
                            //stuff
                            if(currentState == State.START || currentState == State.HAVE_BIZZARE){
                                endScan = false;
                                str = str + ch;
                                currentState = State.HAVE_BIZZARE;
                                try {
                                    
                                if(!theKindMap.containsKey(str + chars.get(posY).get(posX+1)))
                                {
                                    if(chars.get(posY).get(posX+1)=='\n'||chars.get(posY).get(posX+1)=='\r'){
                                        posY++;
                                        posX = -1;
                                        
                                    }
                                    endScan = true;
                                    posX++;
                                }
                            } catch (Exception e) {
                                if(ch==';'){
                                    posX++;
                                    if(increments){
                                        column = posX;
                                        row = posY;
                                    }
                                    newToken = new Token(Kind.SEMI,null, 1, startLine,startPos);
                                    break;
                                }
                                posX++;
                                    if(increments){
                                        column = posX;
                                        row = posY;
                                    }
                                newToken = new Token(Kind.EOF,null, 1, startLine,startPos);
                                break;
                            }
                            }
                    break; 
                    case '.':
                    //check for Float
                    if(currentState == State.HAVE_ZERO||currentState==State.IN_NUM){
                        currentState = State.IN_FLOAT;
                        str = str + ch;
                        endScan = false;
                    } else if(currentState==State.START) {
                       // currentState = State.HAVE_ZERO;
                        //str = str + ch;
                       // endScan = false;
                       throw new LexicalException("Uhhhh looks like you put a decimal statement without a zero. I guess that's not supposed to work in this programming language for some reason???");
                    }
                    break;
                    case '\b','\t','\n','\f','\r','"','\'','\\',' ','#':
                        
                        if(ch=='\n'||ch=='\r'){
                            posY++;
                            posX = -1;
                            if(currentState==State.START){
                                startLine++;
                                startPos = -1;
                                endScan = false;
                            }
                        }
                        else if((ch=='\"')&&currentState==State.START){
                            currentState = State.HAVE_STRING;
                            str = str + ch;
                            endScan = false;
                        } else if(ch=='#'){
                            if(currentState==State.START){
                                if(chars.size()-1>posY){
                              //  posY++;
                                endScan = false;
                                startLine++;
                                }
                                posX = -1;
                                startPos = -1;
                            }
                           // commentMode = true;
                           // str = str + ch;
                        }
                        else if(currentState==State.START){
                            whitespace = true;
                            startPos++;
                            //Skip white space if nothing is scanned yet.
                            endScan = false;
                        }
                        else if(ch==' '){
                            whitespace = true;
                            if(!endScan){
                                startPos++;
                            }
                        } else if(ch=='\t'){
                            whitespace = true;
                            if(currentState==State.START){
                                endScan=false;
                            }
                        }
                        
                        if(currentState==State.START&&posY<chars.size()-1&&!whitespace){
                            posY++;
                            posX = -1;
                            startLine++;
                            startPos++;
                            endScan=false;
                        }

                    break;
                    default:
                    if(currentState==State.START)
                        throw new LexicalException("oopsie poopsie, looks like you made an invalid term.");
                    break;

                   
                    
                   
                }
            } else if (currentState==State.HAVE_STRING){
                str = str + ch;
                endScan = false;
                if(ch=='\"'&&lastChar=='\\'){
                    str = str + "ADDQUOTE";
                   // str = str + '\"';

                }
                if(ch=='\"'&&lastChar!='\\'){
                    
                    posX++;//hmm
                    endScan = true;
                }
                if(!endScan&&posX==chars.get(posY).size()-1){
                    posY++;
                    posX=-1;
                }
            } else if (currentState==State.HAVE_COMMENT){
                str = str + ch;
                endScan = false;
                if(ch=='\n'){
                    endScan = true;
                    posY++;
                }
            }
            lastChar = ch;
    }
        switch(currentState)
            {
                case START:
                    //if it's still start, make an end of file token.
                    newToken = new Token(Kind.EOF,str,str.length(),startLine,startPos);
                    break;
                case IN_IDENT:
                {
                    if(theKindMap.containsKey(str)){
                        newToken = new Token(theKindMap.get(str),str,str.length(),startLine,startPos);
                    } else {
                    newToken = new Token(Kind.IDENT,str,str.length(),startLine,startPos);
                    }
                    break;
                }
                case HAVE_ZERO:
                newToken = new Token(Kind.INT_LIT,str,str.length(),startLine,startPos);
                break;
                case HAVE_DOT:
                    throw new UnsupportedOperationException("excuse me what in the name of ass did you just attempt to compile");
                case IN_FLOAT:
                    newToken = new Token(Kind.FLOAT_LIT,str,str.length(),startLine,startPos);
                break;
                case IN_NUM:
                {
                    try{
                        int x = Integer.parseInt(str);
                    newToken = new Token(Kind.INT_LIT,str,str.length(),startLine,startPos);
                    } catch(Exception e){
                        newToken = new Token(theKindMap.get(str),str,str.length(),startLine,startPos);
                        throw new LexicalException("You piece of shit, you entered a number that is WAAAAAYYY TOO BIG. What are you even doing, kid?!");
                    }
                }
                break;
                case HAVE_STRING:
                    newToken = new Token(Kind.STRING_LIT,str,str.length(),startLine,startPos);
                break;
                case HAVE_BIZZARE:
                if(theKindMap.containsKey(str)){
                newToken = new Token(theKindMap.get(str),str,str.length(),startLine,startPos);
                } else {
                    throw new UnsupportedOperationException("Uhhh looks like you made an invalid token, buckaroo");
                }
                break;
                default:
                throw new LexicalException("How the fuck did you get here?");
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

    }

    //Returns the next token in the array.
    @Override
    public IToken peek() throws LexicalException {
       
        IToken token = MakeToken(false);
        return token;

    }
    

    private State state;
    public Lexer(String input)
    { 
        generateReservedMap();

        chars = new ArrayList<ArrayList<Character>>();
        chars.add(new ArrayList<Character>());
        char[] c = input.toCharArray();


        int line = 0;
        boolean extraLine = true;
        for(int i = 0 ; i < c.length; i++){
            if(c[i]!='\n'){
                extraLine = false;
            }
            if(c[i]!='\n'||!extraLine){
                chars.get(line).add(c[i]);

            }
            if(c[i]=='\n'&&!extraLine){
                line++;
                extraLine = true;
                chars.add(new ArrayList<Character>());
            } else {

            }
        }
    }
}
