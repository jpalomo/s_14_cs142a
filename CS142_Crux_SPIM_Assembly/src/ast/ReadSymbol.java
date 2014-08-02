package ast;

public class ReadSymbol extends Command implements Expression {
	
	private crux.Symbol symbol;

	public ReadSymbol(int lineNum, int charPos, crux.Symbol sym) {
		super(lineNum, charPos);
		this.symbol = sym;
	}
	
	public crux.Symbol symbol()
	{
		return symbol;
	}
	
	@Override
	public String toString()
	{
		return super.toString() + "[" + symbol.name() + "]";
	}

	@Override
	public void accept(CommandVisitor visitor) {
		visitor.visit(this);
	}
	
}
