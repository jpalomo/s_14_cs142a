package types;

public class BoolType extends Type {
    
    public BoolType()
    {
        throw new RuntimeException("implement operators");
    }
    
    @Override
    public String toString()
    {
        return "bool";
    }

    @Override
    public boolean equivalent(Type that)
    {
        if (that == null)
            return false;
        if (!(that instanceof BoolType))
            return false;
        
        return true;
    }
	
	@Override
    public Type not() {
        return this;
    }	
	
	@Override
    public Type and(Type that) {
        if (that instanceof BoolType) 
         	return this;
		return super.and(that);
    }
	
	@Override
    public Type or(Type that) {
        if (that instanceof BoolType) 
         	return this;
		return super.or(that);
    }

	@Override
	public Type assign(Type source) {
		if (equivalent(source)) 
			return this;
		return super.assign(source);
	}
}    
