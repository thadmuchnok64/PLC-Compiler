import edu.ufl.cise.plc.IToken;

public class Token implements IToken
{
    
    Kind kind;
    String input;

@Override

// Returns the kind - TM
public Kind getKind() {
    return kind;
}

}