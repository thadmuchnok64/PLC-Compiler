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

    public Parser(String _input) throws LexicalException{

        input = _input;
        ILexer lexer = CompilerComponentFactory.getLexer(input);
        listOfTokens = new ArrayList<>();
                while(lexer.peek().getKind()!=Kind.EOF){
                    listOfTokens.add(lexer.next());
                }



    }

    public ASTNode recursionParse(ArrayList<IToken> list) throws PLCException{
        ASTNode a = null;
        int parenShift = 0;
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
                    //if(list.get(2).getKind() == Kind.COMMA)
                    //{
                        /*
                        ArrayList<IToken> listIdents = new ArrayList<IToken>();
                        ArrayList<IToken> listIdents2 = new ArrayList<IToken>();
                        listIdents.add(list.get(1));
                        listIdents2.add(list.get(3));
                        return new PixelSelector(list.get(0), (Expr)recursionParse(listIdents), (Expr)recursionParse(listIdents2));
                        */
                        ArrayList<IToken> partOne = new ArrayList<IToken>();
                        ArrayList<IToken> partTwo = new ArrayList<IToken>();
                        int commaIndex = 0;
                        int rSquareIndex = 0;
                        for(int i = list.indexOf(t) + 1; i < list.size(); i++)
                        {
                            if(list.get(i).getKind() == Kind.COMMA)
                            {
                                commaIndex = i;
                                break;
                            }
                            partOne.add(list.get(i));
                            
                            if(i == list.size() - 1)
                            {
                                throw new SyntaxException("Nice one");
                            }
                            
                        }

                        for(int i = commaIndex + 1; i < list.size(); i++)
                        {
                            
                            if(list.get(i).getKind() == Kind.RSQUARE)
                            {
                                rSquareIndex = i;
                                break;
                            }
                            partTwo.add(list.get(i));
                            if(i == list.size() - 1)
                            {
                                throw new SyntaxException("Nice one");
                            }
                        }
                        return new PixelSelector(list.get(0), (Expr)recursionParse(partOne), (Expr)recursionParse(partTwo));
                   // }
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
                case LPAREN:
                ArrayList<IToken> newList = new ArrayList<>();
                int i = 1;
                int parenCount = 0;
                            while(list.get(i).getKind()!=Kind.RPAREN||parenCount>0){
                                if(list.get(i).getKind()==Kind.LPAREN){
                                    parenCount++;
                                } else if (list.get(i).getKind()==Kind.RPAREN){
                                    parenCount--;
                                }

                                newList.add(list.get(i));
                                i++;
                            }
                            parenShift = i;
                            a=recursionParse(newList);
                break;
                    default:
                //ligma
                break;
            }
            BinaryExpr bin,bin2;
                if(list.size()>(1+parenShift)){
                    switch(list.get(1+parenShift).getKind()){
                        case PLUS, MINUS,AND,OR,EQUALS,TIMES,DIV,GE,GT,LT,LE,MOD:
                        if(list.size()>2+parenShift){
                            if(list.get(2+parenShift).getKind() == Kind.KW_IF)
                            {
                                throw new SyntaxException("Real good job there. No really. Im quite impressed. I don't know how you managed to mess it up this bad. I'm clapping. I'm happy for you. If only I was so blissfully ignorant.");
                            }
                            IToken op = list.get(1+parenShift);
                            IToken first = list.get(0);
                            for(int i = 0 ; i < 2+parenShift; i++)
                            list.remove(0);
                            ASTNode b = recursionParse(list);
                            if(b instanceof BinaryExpr){
                                BinaryExpr _b = (BinaryExpr)b;
                                if(!compareOp(op.getKind(), _b.getOp().getKind())){

                                if(_b.getLeft() instanceof BinaryExpr){
                                    BinaryExpr lefty = (BinaryExpr)_b.getLeft();
                                    if(!compareOp(op.getKind(), lefty.getOp().getKind())){
                                        bin = new BinaryExpr(first,(Expr)a,op,lefty.getLeft());
                                        bin2 = new BinaryExpr(first,bin,lefty.getOp(),_b.getLeft());
                                        return new BinaryExpr(first,bin2,_b.getOp(),_b.getRight());



                                    }
                                }


                                bin = new BinaryExpr(first,(Expr)a,op,_b.getLeft());
                                return new BinaryExpr(first,bin,_b.getOp(),_b.getRight());
                                }
                                //return new BinaryExpr(first, left, op, right)
                            } 
                            if(b instanceof Expr) {
                                return new BinaryExpr(first,(Expr)a,op,(Expr)b);
                            }
                        } else{
                            throw new SyntaxException("Oopsie you made a stinky and forgot something important. Clean it up, you bastard");
                        }
                        case LSQUARE:
                        try{
                        ArrayList<IToken> partOne = new ArrayList<IToken>();
                        ArrayList<IToken> partTwo = new ArrayList<IToken>();
                        int commaIndex = 0;
                        int rSquareIndex = 0;
                        for(int i = list.indexOf(t) + 1; i < list.size(); i++)
                        {
                            if(list.get(i).getKind() == Kind.COMMA && list.get(2).getKind() != Kind.COMMA)
                            {
                                commaIndex = i;
                                break;
                            }
                            partOne.add(list.get(i));
                            
                            if(i == list.size() - 1)
                            {
                                throw new SyntaxException("Nice one");
                            }
                            
                        }

                        for(int i = commaIndex + 1; i < list.size(); i++)
                        {
                            
                            if(list.get(i).getKind() == Kind.RSQUARE && list.get(i-1).getKind() != Kind.COMMA)
                            {
                                rSquareIndex = i;
                                break;
                            }
                            partTwo.add(list.get(i));
                            if(i == list.size() - 1)
                            {
                                throw new SyntaxException("Nice one");
                            }
                        }
                        if(rSquareIndex < list.size()-1)
                        {
                            ArrayList<IToken> pixelPart = new ArrayList<IToken>();
                            ArrayList<IToken> notPixelPart = new ArrayList<IToken>();
                            for(int i = 0; i < rSquareIndex + 1; i++)
                            {
                                pixelPart.add(list.get(i));
                            }
                            for(int i = rSquareIndex + 2; i < list.size(); i++)
                            { 
                                notPixelPart.add(list.get(i));
                            }
                            switch(list.get(rSquareIndex + 1).getKind())
                            {
                                case PLUS, TIMES, MINUS, DIV:
                                return new BinaryExpr(list.get(0), (Expr)recursionParse(pixelPart), list.get(rSquareIndex + 1), (Expr)recursionParse(notPixelPart));
                            }
                        }
                        else
                        {
                            ArrayList<IToken> listIdent = new ArrayList<IToken>();
                            listIdent.add(list.get(0));
                            list.remove(0);
                            return new UnaryExprPostfix(list.get(0), (Expr)recursionParse(listIdent), (PixelSelector)recursionParse(list));
                        }
                        /*if(list.get(3).getKind() == Kind.COMMA)
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
                        */
                    } catch (IndexOutOfBoundsException e) {
                        throw new SyntaxException("Nope. Forgot something, dipshit");
                    }
                        
                        break;
                    }
                }
            
        //}

        return a;
    }
    
    //Determines if left operator should take priority over right operator
    public boolean compareOp(Kind l, Kind r) throws SyntaxException{
        switch(l){
            case OR, AND:
            switch(r){
                case OR,AND:
                return false;
                default:
                return true;
            }
            case GE, EQUALS, GT, LE, LT:
                switch(r){
                    case OR,AND,GE, EQUALS, GT, LE, LT:
                    return false;
                    default:
                    return true;
                }
            case PLUS, MINUS:
            switch(r){
                case OR,AND:
                return false;
                case GE, EQUALS, GT, LE, LT, PLUS, MINUS:
                return false;
                default:
                return true;
            }
            case TIMES, DIV, MOD:
            switch(r){
                case OR,AND:
                return false;
                case GE, EQUALS, GT, LE, LT:
                return false;
                case TIMES, DIV, MOD,PLUS, MINUS:
                return false;
                default:
                return true;
            }
            default:
            throw new SyntaxException("ummm, what did you even attempt to do? I have no idea what you just did, and I wrote this language.");
            }
    }


    @Override
    public ASTNode parse() throws PLCException {
       return recursionParse(listOfTokens);
    }


    


    
}