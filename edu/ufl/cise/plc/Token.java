package edu.ufl.cise.plc;
import javax.naming.OperationNotSupportedException;
import javax.xml.transform.Source;

public class Token implements IToken
{
    // - Token Data - //

    Kind kind;
    String input;
    int pos;
    int length;
    SourceLocation sourceLocation;

    // --------------- //

    

    public Token(Kind _kind, String _input, int _length, int line, int column)
    {
        kind = _kind;
        input = _input;
        //pos = _pos;
        sourceLocation = new SourceLocation(line, column);
    }

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
    
    // Returns source location - TM
    @Override
    public SourceLocation getSourceLocation() {
        return sourceLocation;
    }

    // Returns the Integer value of the token if the kind is an int - TM
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
            int index = 0;
            for(char c: input.toCharArray())
            {
                if(!(c == '"' && (index == 0 || index == input.toCharArray().length - 1 )))
                {
                    result = result.concat(Character.toString(c));
                }
                index++;
                
            }
            return result;
        } else{
            throw new UnsupportedOperationException("oh mah god, you must have done something REALLY wrong if you got this error...");
        }
    }
    
    // Returns the float value if the kind is a float - TM
    @Override
    public float getFloatValue() {
        if(kind==Kind.FLOAT_LIT){
            return Float.parseFloat(getText());
        } else {
            throw new NumberFormatException();
        }
    }

    @Override
    public boolean getBooleanValue() {
        if(kind==Kind.BOOLEAN_LIT){
            if(getText().equalsIgnoreCase("true")){
                return true;
            }  else {
                return false;
            }
        } else {
            throw new NumberFormatException();
        }
    
    }

    
    
}