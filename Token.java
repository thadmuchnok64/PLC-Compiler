import edu.ufl.cise.plc.IToken;

public class Token implements IToken
{
    
    Kind kind;
    String input;
    int pos;
    int length;


// Returns the kind - TM
    @Override
    public Kind getKind() {
        return kind;
    }

    // Returns the text of the token - TM
    @Override
    public String getText() {
        return input;
    }

    // Returns the Integer value of the token
    @Override
    public int getIntValue() {
        if(kind==Kind.INT_LIT){
            return Integer.parseInt(getText());
        } else {
            throw new NumberFormatException();
        }
    }

    @Override
    public String getStringValue() {
        if(kind == Kind.STRING_LIT)
        {
            String result = "";
            for(char c: input.toCharArray())
            {
                if(c == '"')
                {
                    continue;
                }
                result.concat(Character.toString(c));
            }
        }
        return result;
    }
    

}