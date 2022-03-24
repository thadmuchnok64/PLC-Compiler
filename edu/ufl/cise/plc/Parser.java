package edu.ufl.cise.plc;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

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
import edu.ufl.cise.plc.ast.Types.Type;
import edu.ufl.cise.plc.ast.NameDef;
import edu.ufl.cise.plc.ast.Program;
import edu.ufl.cise.plc.ast.VarDeclaration;
import edu.ufl.cise.plc.ast.ColorExpr;
import edu.ufl.cise.plc.ast.ColorConstExpr;
import edu.ufl.cise.plc.ast.ConsoleExpr;
import edu.ufl.cise.plc.ast.ReturnStatement;
import edu.ufl.cise.plc.ast.ReadStatement;
import edu.ufl.cise.plc.ast.WriteStatement;
import edu.ufl.cise.plc.ast.NameDefWithDim;
import edu.ufl.cise.plc.ast.Dimension;
import edu.ufl.cise.plc.ast.AssignmentStatement;

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
    public ASTNode recursionParse(ArrayList<IToken> list, int offSet) throws PLCException{
        for(int i = 0 ; i <offSet;i++){
            list.remove(0);
        }
        return recursionParse(list);
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
                    

                Expr x;
                Expr y;
                IToken name = null;
                    try {
                        int i = 1;
                            i++;
                            ArrayList<IToken> newList = new ArrayList<>();
                            while(list.get(i).getKind()!=Kind.COMMA){
                                newList.add(list.get(i));
                                i++;
                            }
                            i++;
                            x = (Expr)recursionParse(newList);
                            newList.clear();
                            //int ifCount = 0;
                            while((list.get(i).getKind()!=Kind.RSQUARE)){
                                
                                newList.add(list.get(i));
                                i++;
                            }
                            i++;
                            if(list.size()>i){
                                name = list.get(i);
                            }
                            y = (Expr)recursionParse(newList);
        } 
        catch (IndexOutOfBoundsException e) {
            throw new SyntaxException("Nice one.");
        }
        if(t.getKind()==Kind.TYPE){
            
            Dimension dim = new Dimension(t,x,y);
            a = new NameDefWithDim(t,t,name,dim);
        } else{
        a= new PixelSelector(t, x, y);
        }
                   break;
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
                case TYPE,KW_VOID:
                    try {
                        List<NameDef> params;
                        params = new ArrayList<>();
                        ArrayList<IToken> paramTokens = new ArrayList<>();
                        if(list.size()>2&&list.get(2).getKind()!=Kind.LPAREN&&list.get(1).getKind()==Kind.LSQUARE){
                            int i = 2;
                            ArrayList<IToken> tlist = new ArrayList<>();
                            ArrayList<IToken> tlist2 = new ArrayList<>();

                            for( i=i ; i < list.size();i++){
                                if(list.get(i).getKind()==Kind.COMMA){
                                    i++;
                                    break;
                                }
                                tlist.add(list.get(i));
                            }
                            for( i=i ; i < list.size();i++){
                                if(list.get(i).getKind()==Kind.RSQUARE){
                                    i++;
                                    break;
                                }
                                tlist2.add(list.get(i));
                            }
                            Dimension dim = new Dimension(list.get(2),(Expr)recursionParse(tlist),(Expr)recursionParse(tlist2));
                            //return new NameDefWithDim(t,t,list.get(i),dim);
                            //Expr e =(Expr)recursionParse(list,i+2);
                            if(list.size()>i+1)
                            return new VarDeclaration(t,new NameDefWithDim(t,t,list.get(i),dim),list.get(i+1),(Expr)recursionParse(list,i+2));
                            else
                            return new NameDefWithDim(t,t,list.get(i),dim);
                        }
                        else if(list.size()<=2|| list.get(2).getKind()!=Kind.LPAREN){
                            if(list.size()>3){
                            return new VarDeclaration(t,new NameDef(list.get(0),list.get(0),list.get(1)),list.get(2),(Expr)recursionParse(list,3));
                            } else{
                                return new VarDeclaration(t,new NameDef(list.get(0),list.get(0),list.get(1)),null,null);
                            }
                        }
                        int i;
                        int parenCounter=0;
                        for(i = 3; i<list.size();i++){
                            if(list.get(i).getKind()==Kind.RPAREN&&parenCounter<1){
                                try {
                                    if(paramTokens.size() > 2){
                                        params.add((NameDefWithDim)recursionParse(paramTokens));
                                    } else{
                                    params.add(new NameDef(list.get(i),paramTokens.get(0),paramTokens.get(1)));
                                    }
                                    paramTokens.clear();
                                } catch (Exception e) {
                                    break;
                                }
                                break;
                            } else{
                                if(list.get(i).getKind()==Kind.LPAREN||list.get(i).getKind()==Kind.LSQUARE){
                                    paramTokens.add(list.get(i));
                                    parenCounter++;
                                } else if(list.get(i).getKind()==Kind.RPAREN||list.get(i).getKind()==Kind.RSQUARE) {
                                    paramTokens.add(list.get(i));
                                    parenCounter--;
                                }
                                else if(list.get(i).getKind()!=Kind.COMMA||parenCounter>0){
                                paramTokens.add(list.get(i));
                                } else{
                                    if(paramTokens.size() > 2){
                                        params.add((NameDefWithDim)recursionParse(paramTokens));
                                    } else{
                                params.add(new NameDef(list.get(i),paramTokens.get(0),paramTokens.get(1)));
                                    }
                                paramTokens.clear();
                                }
                            }
                        }
                        List<ASTNode> nodeList = new ArrayList();
                        boolean initiated = false;
                        for( i = i+1 ; i<list.size();i++){
                            if(list.get(i).getKind()!=Kind.SEMI){
                            paramTokens.add(list.get(i));
                            if(list.size()-1==i&&initiated){

                                throw new SyntaxException("spaghetti");
                            }
                            initiated = true;
                            } else{
                                ASTNode nop = recursionParse(paramTokens);
                                if(nop instanceof Expr &&(!(nop instanceof BinaryExpr)||((BinaryExpr)nop).getOp().getKind()!=Kind.ASSIGN)){
                                    throw new SyntaxException("uhm. I think you're missing something in that, line, silly.");
                                }
                                nodeList.add(nop);
                                paramTokens.clear();
                            }
                        }
                        if(list.get(1).getKind()!=Kind.IDENT){
                            throw new SyntaxException("What kind of shit did you just try to name your function???!?!1 Dumbass");
                        }
                        a = new Program(t,Type.toType(t.getText()),list.get(1).getText(),params,nodeList);
                    } catch (Exception e) {
                        //test
                        throw new SyntaxException("Okay that was my fault. TOTALLY my fault. I'm the one who didn't bother to fix their method and did something funky with it. I TOTALLY am responsible for this.");
                    }
                break;
                case COLOR_CONST:
                a = new ColorConstExpr(t);
                 

                break;
                case LANGLE:
                ArrayList<IToken> tokenlist = new ArrayList<>();

                try {
                    int i;
                    Expr b=null,c=null,d=null;
                    for(i = 1 ; i < list.size();i++){
                        if(list.get(i).getKind()==Kind.COMMA){
                            b = (Expr)recursionParse(tokenlist);
                            tokenlist.clear();
                            break;
                        } else{
                            tokenlist.add(list.get(i));

                        }
                    }
                    for(i = i+1 ; i < list.size();i++){
                        if(list.get(i).getKind()==Kind.COMMA){
                            c = (Expr)recursionParse(tokenlist);
                            tokenlist.clear();
                            break;
                        } else{
                            tokenlist.add(list.get(i));

                        }
                    }
                    for(i = i+1 ; i < list.size();i++){
                        if(list.get(i).getKind()==Kind.RANGLE){
                            d = (Expr)recursionParse(tokenlist);
                            tokenlist.clear();
                            break;
                        } else{
                            tokenlist.add(list.get(i));

                        }
                    }

                    a= new ColorExpr(t,b,c,d);

                } catch (Exception e) {
                    throw new SyntaxException("Looks like you tried to make a color that doesn't exist, dumbo");
                }
                break;
                case RETURN:
                ArrayList<IToken> tlist = new ArrayList<>();
                    for(int i = 1;i<list.size();i++){
                        if(list.get(i).getKind()==Kind.SEMI){
                            break;
                        }
                        tlist.add(list.get(i));
                    }
                    return new ReturnStatement(t,(Expr)recursionParse(tlist));
                
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
                case KW_CONSOLE:
                            a = new ConsoleExpr(t);
                break;
                case KW_WRITE:
                ArrayList<IToken> ylist = new ArrayList<>();
                int v;
                for(v = 1 ; v < list.size();v++){
                    if(list.get(v).getKind()==Kind.RARROW){

                        break;
                    }
                    ylist.add(list.get(v));
                }
                return new WriteStatement(t,(Expr)recursionParse(ylist),(Expr)recursionParse(list,v+1));
                
                    default:
                //ligma
                break;
            }
            BinaryExpr bin,bin2;
                if(list.size()>(1+parenShift)){
                    switch(list.get(1+parenShift).getKind()){
                        case IDENT:
                        if(t.getKind()!=Kind.KW_VOID&&t.getKind()!=Kind.TYPE){
                            throw new SyntaxException("looks like you didn't put a type or void before your function, shitferbrains.");
                        }
                        break;
                        case ASSIGN:
                        return new AssignmentStatement(t,a.getText(),null,(Expr)recursionParse(list,2));
                        case LSQUARE:
                            

                            Expr x;
                            Expr y;
                            IToken name = null;
                                try {
                                    int i = 1;
                                        i++;
                                        ArrayList<IToken> newList = new ArrayList<>();
                                        while(list.get(i).getKind()!=Kind.COMMA){
                                            newList.add(list.get(i));
                                            i++;
                                        }
                                        i++;
                                        x = (Expr)recursionParse(newList);
                                        newList.clear();
                                        //int ifCount = 0;
                                        while((list.get(i).getKind()!=Kind.RSQUARE)){
                                            
                                            newList.add(list.get(i));
                                            i++;
                                        }
                                        parenShift += i;
                                        i++;
                            if(list.size()>i){
                                name = list.get(i);
                            }
                                        y = (Expr)recursionParse(newList);
                                        
                    } 

                    
                    catch (IndexOutOfBoundsException e) {
                        throw new SyntaxException("Nice one.");
                    }
                    Expr n = (Expr)a;
                    if(t.getKind()==Kind.TYPE&&t.getText()=="image"){
                        Dimension dim = new Dimension(t,x,y);
                        a = new NameDefWithDim(t,t,name,dim);
                    } else{
                    a= new UnaryExprPostfix(t,n,new PixelSelector(t,x,y));
                    }
                    if(list.size()>2+parenShift!=true){
                        break;
                    }
                        case PLUS, MINUS,AND,OR,EQUALS,TIMES,DIV,GE,GT,LT,LE,MOD,NOT_EQUALS,LARROW:
                        if(list.get(1+parenShift).getKind()==Kind.LARROW){
                            if(a instanceof UnaryExprPostfix){
                                return new ReadStatement(t,t.getText(),((UnaryExprPostfix)a).getSelector(),(Expr)recursionParse(list,2));
                            }
                            return new ReadStatement(t,t.getText(),null,(Expr)recursionParse(list,2));
                            }
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

                                    ArrayList<BinaryExpr> binList = new ArrayList<BinaryExpr>();
                                if(_b.getLeft() instanceof BinaryExpr){
                                    BinaryExpr lefty = (BinaryExpr)_b.getLeft();
                                    binList.add(lefty);
                                    while(lefty instanceof BinaryExpr){
                                    
                                    //bin2 = new BinaryExpr(first,bin,lefty.getOp(),_b.getLeft());
                                    
                                    if(!compareOp(op.getKind(), lefty.getOp().getKind())){
                                        
                                        BinaryExpr lefty2 = null;
                                        if(lefty.getLeft()!=null&&lefty.getLeft() instanceof BinaryExpr){
                                            lefty2 = (BinaryExpr)lefty.getLeft();

                                        } 
                                        if(lefty2!=null&&!compareOp(op.getKind(), lefty2.getOp().getKind())){
                                            lefty = (BinaryExpr)lefty.getLeft();
                                            binList.add(lefty);
                                        } else{
                                        bin = new BinaryExpr(first,(Expr)a,op,lefty.getLeft());
                                        for(int i = binList.size()-1;i>=0;i--){
                                        bin = new BinaryExpr(first, bin, binList.get(i).getOp(), binList.get(i).getRight());
                                        }
                                        return new BinaryExpr(first,bin,_b.getOp(),_b.getRight());
                                    }
                                       /*
                                        bin = new BinaryExpr(first,(Expr)a,op,lefty.getLeft());
                                        bin2 = new BinaryExpr(first,bin,lefty.getOp(),_b.getLeft());
                                        return new BinaryExpr(first,bin2,_b.getOp(),_b.getRight());
                                        */


                                    } else{
                                        if(lefty.getLeft()!=null&&lefty.getLeft() instanceof BinaryExpr){
                                        lefty = (BinaryExpr)lefty.getLeft();
                                        binList.add(lefty);
                                        } else{
                                            break;
                                        }

                                    }
                                }
                                }


                                bin = new BinaryExpr(first,(Expr)a,op,_b.getLeft());
                                return new BinaryExpr(first,bin,_b.getOp(),_b.getRight());
                                }
                                //return new BinaryExpr(first, left, op, right)
                            } 
                            if(b instanceof Expr) {
                                if(op.getKind() ==Kind.ASSIGN){
                                    return new AssignmentStatement(first,((IdentExpr)((UnaryExprPostfix)a).getExpr()).getText(),((UnaryExprPostfix)a).getSelector(),(Expr)b);
                                }
                                return new BinaryExpr(first,(Expr)a,op,(Expr)b);
                            }
                        } else{
                            throw new SyntaxException("Oopsie you made a stinky and forgot something important. Clean it up, you bastard");
                        }
                        
                    //a= new PixelSelector(t, x, y);

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
            }
                return true;
            
            case GE, EQUALS, GT, LE, LT,NOT_EQUALS:
                switch(r){
                    case OR,AND,GE, EQUALS, GT, LE, LT,NOT_EQUALS:
                    return false;
                    default:
                    return true;
                }
            case PLUS, MINUS:
            switch(r){
                case OR,AND:
                return false;
                case GE, EQUALS, GT, LE, LT,NOT_EQUALS,PLUS,MINUS:
                return false;
                default:
                return true;
            }
            case TIMES, DIV, MOD:
            switch(r){
                case OR,AND:
                return false;
                case GE, EQUALS, GT, LE, LT,NOT_EQUALS:
                return false;
                case PLUS, MINUS,TIMES,DIV,MOD:
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