/*
 * This class models the symbol table found in a compiler.  By using a an 
 * array list of linked hash maps to track the scopes of the program.  Linked
 * hash maps are used in order to preserve the ordering in which the symbols
 * are added to the table and the current scope.
 * 
 * @author John Palomo
 * Date: 4/28/2014
 * CS 142A: Compilers and Interpreters
 * Spring 2014
 * 
 */
package crux;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SymbolTable {
	List<Map<String, Symbol>> symbolTable;
	private int depth;
    
    public SymbolTable() {
		symbolTable = new ArrayList<Map<String, Symbol>>();
		addNewScope();

		Map<String, Symbol> topLevelScope = symbolTable.get(symbolTable.size() - 1);

		topLevelScope.put("readInt", new Symbol("readInt"));
		topLevelScope.put("readFloat", new Symbol("readFloat"));
		topLevelScope.put("printBool", new Symbol("printBool"));
		topLevelScope.put("printInt", new Symbol("printInt"));
		topLevelScope.put("printFloat", new Symbol("printFloat"));
		topLevelScope.put("println", new Symbol("println"));

    }
    
	/**
	 * Iteratively search the implicit stack looking for a symbol associated with 
	 * the name value.
	 * 
	 * @param name - the name of the symbol to be searched for
	 * @return - the symbol if found on the stack
	 * @throws SymbolNotFoundError 
	 */
    public Symbol lookup(String name) throws SymbolNotFoundError
    {
		Symbol symbol = new Symbol(name);

		for(Map<String, Symbol> scope : symbolTable){
			if(scope.containsKey(name)) {
				return symbol;
			}
		} 
        throw new SymbolNotFoundError(name);
    }

	/**
	 * Creates a new map in the linked list represented the current scope being 
	 * entered. Pushes the current scope on to the implicit stack.
	 * 
	 * @return boolean indicating whether the creation and insertion of the new
	 * scope was successful.
	 */
	public boolean addNewScope() {
		LinkedHashMap<String, Symbol> scope = new LinkedHashMap<String, Symbol>();

		if(symbolTable.add(scope)) {
			return true;
		}

		return false;
	}

	/**
	 * Pops the currents scope of the implicit stack.
	 * 
	 * @return - boolean value indicating whether the removal was successful
	 */
	public boolean removeCurrentScope() {
		if(symbolTable.remove(symbolTable.size() - 1) != null) {
			return true;
		}
		return false;
	}

    /**
	 * Attempts to put the symbol associated with the name into the current scope.
	 * 
	 * @param name - the name of the symbol
	 * @return - the symbol object if successfully inserted into scope
	 * @throws RedeclarationError 
	 */   
    public Symbol insert(String name) throws RedeclarationError
    {
		int currentSize = symbolTable.size() - 1;
		Map<String, Symbol> currentScope = symbolTable.get(currentSize);

		Symbol symbol = new Symbol(name);
		if (!currentScope.containsKey(name)) {
			currentScope.put(name, symbol);
			return symbol;
		}

		throw new RedeclarationError(symbol); 
    }


   @Override 
    public String toString()
    {
		int level= 0;
        StringBuffer sb = new StringBuffer();
		for(Map<String, Symbol> map: symbolTable){
			Set<Map.Entry<String,Symbol>> mapSet = map.entrySet();
			Iterator<Map.Entry<String, Symbol>> it = mapSet.iterator();
			
			StringBuffer spacing = new StringBuffer();
			
			for(int i = 0; i < (level *2); i++) {
				spacing.append(" ");
			}

			while(it.hasNext()){
				Map.Entry entry = it.next();
				sb.append(spacing.toString() + entry.getValue().toString()); 
				sb.append("\n");
			}
			level++;
		}
        return sb.toString();
    }
}


class SymbolNotFoundError extends Error
{
    private static final long serialVersionUID = 1L;
    private String name;
    
    SymbolNotFoundError(String name)
    {
        this.name = name;
    }
    
    public String name()
    {
        return name;
    }
}

class RedeclarationError extends Error
{
    private static final long serialVersionUID = 1L;

    public RedeclarationError(Symbol sym)
    {
        super("Symbol " + sym + " being redeclared.");
    }
}
