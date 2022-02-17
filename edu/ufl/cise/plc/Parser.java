package edu.ufl.cise.plc;

import java.lang.reflect.Array;
import java.util.ArrayList;

import edu.ufl.cise.plc.IToken.Kind;
import edu.ufl.cise.plc.ast.ASTNode;
import edu.ufl.cise.plc.ast.BinaryExpr;
import edu.ufl.cise.plc.ast.BooleanLitExpr;
import edu.ufl.cise.plc.ast.ConditionalExpr;
import edu.ufl.cise.plc.ast.Expr;
import edu.ufl.cise.plc.ast.FloatLitExpr;
import edu.ufl.cise.plc.ast.IdentExpr;
import edu.ufl.cise.plc.ast.IntLitExpr;
import edu.ufl.cise.plc.ast.PixelSelector;
import edu.ufl.cise.plc.ast.StringLitExpr;
import edu.ufl.cise.plc.ast.UnaryExpr;
import edu.ufl.cise.plc.ast.UnaryExprPostfix;
import edu.ufl.cise.plc.CompilerComponentFactory;

public class Parser implements IParser {

    String input;
    ArrayList<IToken> listOfTokens;

    public Parser(String _input){

        input = _input;
        ILexer lexer = CompilerComponentFactory.getLexer(input);
        listOfTokens = new ArrayList<>();
            try {
                while(lexer.peek().getKind()!=Kind.EOF){
                    listOfTokens.add(lexer.next());
                }
            } catch (LexicalException e) {
                // idk why this needed a try and catch
                e.printStackTrace();
            }



    }

    public ASTNode recursionParse(ArrayList<IToken> list) throws PLCException{
        ASTNode a = null;

        //for(int i = 0; i < listOfTokens.size(); i++){
            IToken t = list.get(0);

            switch(t.getKind())
            {
                case BOOLEAN_LIT:
                    a = new BooleanLitExpr(t);
                    break;
                case STRING_LIT:
                    a = new StringLitExpr(t);
                    break;
                case FLOAT_LIT:
                    a = new FloatLitExpr(t);
                    break;
                case IDENT:
                    a = new IdentExpr(t);
                    break;
                case INT_LIT:
                    a = new IntLitExpr(t);
                    break;
                case LSQUARE:
                    if(list.get(2).getKind() == Kind.COMMA)
                    {
                        ArrayList<IToken> listIdents = new ArrayList<IToken>();
                        ArrayList<IToken> listIdents2 = new ArrayList<IToken>();
                        listIdents.add(list.get(1));
                        listIdents2.add(list.get(3));
                        return new PixelSelector(list.get(0), (Expr)recursionParse(listIdents), (Expr)recursionParse(listIdents2));
                    }
                case KW_IF:
                //IToken firstToken;
                Expr condition;
                Expr trueCase;
                Expr falseCase;
                    try {
                        int i = 1;
                        if(list.get(i).getKind()==Kind.LPAREN){
                            i++;
                            ArrayList<IToken> newList = new ArrayList<>();
                            while(list.get(i).getKind()!=Kind.RPAREN){
                                newList.add(list.get(i));
                                i++;
                            }
                            i++;
                            condition = (Expr)recursionParse(newList);
                            newList.clear();
                            int ifCount = 0;
                            while((list.get(i).getKind()!=Kind.KW_ELSE&&list.get(i).getKind()!=Kind.KW_FI)||ifCount>0){
                                if(list.get(i).getKind()==Kind.KW_IF){
                                    ifCount++;
                                } else if(list.get(i).getKind()==Kind.KW_FI){
                                    ifCount--;
                                }
                                newList.add(list.get(i));
                                i++;
                            }
                            trueCase = (Expr)recursionParse(newList);
                            newList.clear();
                           // if(list.get(i).getKind()==Kind.KW_ELSE){
                                i++;
                            while(list.get(i).getKind()!=Kind.KW_FI){
                                newList.add(list.get(i));
                                i++;
                            }
                            falseCase = (Expr)recursionParse(newList);

                        //}

                        } else{
                            throw new Exception();
                        }
                    } catch (Exception e) {
                        throw new SyntaxException("your if-statement is missing something, you sentient bag of burger meat");
                    }
                    a= new ConditionalExpr(t, condition, trueCase, falseCase);

                break;
                case BANG,MINUS,COLOR_OP, IMAGE_OP:
                    list.remove(0);
                    //ASTNode newNode = (Expr)recursionParse(list);
                    //if(newNode instanceof Expr)
                    return new UnaryExpr(t, t, (Expr)recursionParse(list));
                   // else 
                    //throw new LexicalException("You pile of catshit. Look at what you did to the code");
                default:
                //ligma
                break;
            }
                if(list.size()>1){
                    switch(list.get(1).getKind()){
                        case PLUS, MINUS,AND,OR:
                        if(list.size()>2){
                            if(list.get(2).getKind() == Kind.KW_IF)
                            {
                                throw new SyntaxException("Real good job there. No really. Im quite impressed. I don't know how you managed to mess it up this bad. I'm clapping. I'm happy for you. If only I was so blissfully ignorant.");
                            }
                            IToken op = list.get(1);
                            IToken first = list.get(0);
                            list.remove(0);
                            list.remove(0);
                            ASTNode b = recursionParse(list);
                            if(b instanceof Expr)
                            return new BinaryExpr(first,(Expr)a,op,(Expr)b);
                        } else{
                            throw new LexicalException("Oopsie you made a stinky. Clean it up, you bastard");
                        }
                        case LSQUARE:
                        if(list.get(3).getKind() == Kind.COMMA)
                        {
                            if(list.size() > 6)
                            {
                                ArrayList<IToken> partOne = new ArrayList<IToken>();
                                for(int i = 0; i < 6; i++)
                                {
                                    partOne.add(list.get(i));
                                }
                                ArrayList<IToken> partTwo = new ArrayList<IToken>();
                                for(int i = 7; i < list.size(); i++ )
                                {
                                    partTwo.add(list.get(i));
                                }
                                return new BinaryExpr(list.get(0), (Expr)recursionParse(partOne), list.get(6), (Expr)recursionParse(partTwo));
                            }
                            else
                            {
                                ArrayList<IToken> listIdent = new ArrayList<IToken>();
                                listIdent.add(list.get(0));
                                list.remove(0);
                                return new UnaryExprPostfix(list.get(0), (Expr)recursionParse(listIdent), (PixelSelector)recursionParse(list));
                            }
                           
                        }
                        
                        break;
                    }
                }
            
        //}

        return a;
    }
    

    @Override
    public ASTNode parse() throws PLCException {
       return recursionParse(listOfTokens);
    }


    
}