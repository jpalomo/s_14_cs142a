package types;

public class FloatType extends Type {
    
    @Override
    public String toString()
    {
        return "float";
    }
       
    @Override
    public boolean equivalent(Type that)
    {
        if (that == null)
            return false;
        if (!(that instanceof FloatType))
            return false;
        return true;
    }

	@Override	
	public Type add(Type that){
		if(thatEqualThis(that))	{
			return this;
		}

		return super.add(that);
	}

	@Override 
	public Type sub(Type that) {
		if(thatEqualThis(that))	{
			return this;
		}

		return super.sub(that);
	}

	@Override
	public Type mul(Type that){
		if(thatEqualThis(that)) {
			return this;
		}
		return super.mul(that);
	}

	@Override
	public Type div(Type that){
		if(thatEqualThis(that))	{
			return this;
		}
		return super.div(that);
	}

	private boolean thatEqualThis(Type that) {
		return that instanceof FloatType; 
	}

	@Override
    public Type compare(Type that) {
        if (!(that instanceof FloatType))
            return super.compare(that);
        return new BoolType();
    }
}
