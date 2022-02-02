import edu.ufl.cise.plc.IToken;

public class Token implements IToken
{
    
    Kind kind;
    String input;
    int pos;
    int length;

    

    // Returns the kind - TM
    @Override public Kind getKind() {
        return kind;
    }

    @Override public String getText()
    {
        return input;
    }
}