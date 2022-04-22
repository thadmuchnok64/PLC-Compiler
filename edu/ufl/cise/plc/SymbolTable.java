package edu.ufl.cise.plc;
import java.util.HashMap;
import edu.ufl.cise.plc.ast.Declaration;

public class SymbolTable {


HashMap<String,Declaration> entries = new HashMap<>();
  //returns true if name successfully inserted in symbol table, false if already present
  public boolean insert(String name, Declaration declaration) {
    return (entries.putIfAbsent(name,declaration) == null);
  }

  public boolean remove(String name){
    if(lookup(name)==null){
      return false;
    }
     entries.remove(name);
     return true;
  }

  //returns Declaration if present, or null if name not declared.
  public Declaration lookup(String name) {
    return entries.get(name);
  }
  
  public boolean contains(String name){
    return entries.containsKey(name);
  }

}
