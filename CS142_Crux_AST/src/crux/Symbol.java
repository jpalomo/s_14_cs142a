package crux;


public class Symbol {
    
    private String name;
	private int depth;

    public Symbol(String name) {
        this.name = name;
    }

    public String name() {
        return this.name;
    }
    
    public String toString() {
        return "Symbol(" + name + ")";
    }

    public static Symbol newError(String message) {
        return new ErrorSymbol(message);
    }

	public int getDepth() {
		return depth;
	}
}
class ErrorSymbol extends Symbol {
    public ErrorSymbol(String message) {
        super(message);
    }
}
